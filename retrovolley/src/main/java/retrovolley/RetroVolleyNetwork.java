/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2015 Konstantin Tarasenko
 * Copyright (C) 2015 Serghei (Serj) Lotutovici
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrovolley;

import android.os.SystemClock;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.HttpStack;
import retrovolley.converter.Converter;
import retrovolley.request.RetroRequest;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A network performing Volley requests over an {@link HttpStack}.
 * Extended to support the oauth2 flow
 *
 * @author Konstantin Tarasenko
 */
class RetroVolleyNetwork implements Network {

    private static final String TAG = RetroVolleyNetwork.class.getSimpleName();

    private static final int SLOW_REQUEST_THRESHOLD_MS = 3000;
    private static final int DEFAULT_POOL_SIZE = 4096;

    private final NetworkHelper mNetworkHelper;

    /**
     */
    public RetroVolleyNetwork() {
        // If a pool isn't passed in, then build a small default pool that will give us a lot of
        // benefit and not use too much memory.
        this(new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    /**
     * @param pool      a buffer pool that improves GC performance in copy operations
     */
    public RetroVolleyNetwork(ByteArrayPool pool) {
        mNetworkHelper = new NetworkHelper(pool);
    }

    @Override
    public NetworkResponse performRequest(Request<?> req) throws VolleyError {
        if (req instanceof RetroRequest) {
            RetroRequest<?> request = (RetroRequest<?>) req;

            EndpointAdapter adapter = request.getEndpointAdapter();
            HttpStack stack = adapter.getHttpStack();
            Converter converter = adapter.getConverter();
            AuthStrategy authStrategy = adapter.getAuthStrategy();
            RequestInterceptor interceptor = adapter.getRequestInterceptor();

            long requestStart = SystemClock.elapsedRealtime();

            while (true) {
                HttpResponse httpResponse = null;
                byte[] responseContents = null;
                Map<String, String> responseHeaders = new HashMap<String, String>();
                try {
                    /* Gather headers. */
                    Map<String, String> headers = new HashMap<String, String>();
                    addCacheHeaders(headers, request.getCacheEntry());
                    if (interceptor != null) {
                        interceptor.intercept(request);
                    }
                    httpResponse = stack.performRequest(request, headers);
                    StatusLine statusLine = httpResponse.getStatusLine();
                    int statusCode = statusLine.getStatusCode();

                    responseHeaders = mNetworkHelper.convertHeaders(httpResponse.getAllHeaders());
                    /* Handle cache validation. */
                    if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
                        return new NetworkResponse(
                                HttpStatus.SC_NOT_MODIFIED,
                                request.getCacheEntry() == null ? null : request.getCacheEntry().data,
                                responseHeaders,
                                true
                        );
                    }

                    responseContents = mNetworkHelper.getResponse(httpResponse);

                    /* Log request time */
                    long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                    logSlowRequests(requestLifetime, request, responseContents, statusLine);

                    /* Throw exception if status code is not valid */
                    if (statusCode < 200 || statusCode > 299) {
                        throw new IOException();
                    }

                    return new NetworkResponse(statusCode, responseContents, responseHeaders, false);

                } catch (SocketTimeoutException ste) {
                    attemptRetryOnException("socket", request, new TimeoutError());
                } catch (ConnectTimeoutException cte) {
                    attemptRetryOnException("connection", request, new TimeoutError());
                } catch (MalformedURLException mue) {
                    throw new RuntimeException("Bad URL " + request.getUrl(), mue);
                } catch (AuthFailureError ae) {
                    tryRelogin(ae, request, authStrategy, stack, converter);
                } catch (IOException ioe) {

                    int statusCode;
                    if (httpResponse != null) {
                        statusCode = httpResponse.getStatusLine().getStatusCode();
                    } else {
                        throw new NoConnectionError(ioe);
                    }

                    if (responseContents != null) {
                        NetworkResponse networkResponse = new NetworkResponse(statusCode, responseContents, responseHeaders, false);

                        if (statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_FORBIDDEN) {
                            AuthFailureError ex = new AuthFailureError(networkResponse);
                            tryRelogin(ex, request, authStrategy, stack, converter);
                        } else {
                            // TODO: Only throw ServerError for 5xx status codes.
                            throw new ServerError(networkResponse);
                        }
                    } else {
                        throw new NetworkError(ioe);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Use request builder in order to use RetroVolley");
        }
    }

    private void tryRelogin(AuthFailureError ex, Request<?> request, AuthStrategy authStrategy, HttpStack httpStack, Converter converter) throws VolleyError {
        if (authStrategy != null && authStrategy.authenticate(httpStack, converter, mNetworkHelper)) {
            attemptRetryOnException("auth", request, ex);
        } else {
            throw ex;
        }
    }


    /**
     * Logs requests that took over SLOW_REQUEST_THRESHOLD_MS to complete.
     */
    private void logSlowRequests(long requestLifetime, Request<?> request,
                                 byte[] responseContents, StatusLine statusLine) {
        if (requestLifetime > SLOW_REQUEST_THRESHOLD_MS) {
            // TODO implement logging
        }
    }

    /**
     * Attempts to prepare the request for a retry. If there are no more attempts remaining in
     * the requests retry policy, a timeout exception is thrown.
     *
     * @param request The request to use.
     */
    private static void attemptRetryOnException(String logPrefix, Request<?> request, VolleyError exception)
            throws VolleyError {
        Logging.d("Retrying on exception " + logPrefix, exception);
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int oldTimeout = request.getTimeoutMs();

        try {
            retryPolicy.retry(exception);
        } catch (VolleyError ve) {
            Logging.e("Retry attempt failed", ve);
            request.addMarker(String.format("%s-timeout-giveup [timeout=%s]", logPrefix, oldTimeout));
            throw ve;
        }

        request.addMarker(String.format("%s-retry [timeout=%s]", logPrefix, oldTimeout));
    }

    /**
     * Add a cache header to the request
     *
     * @param headers Request headers
     * @param entry   The cache entry to add
     */
    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry) {
        /* If there's no cache entry, we're done. */
        if (entry == null) {
            return;
        }

        if (entry.etag != null) {
            headers.put("If-None-Match", entry.etag);
        }

        if (entry.serverDate > 0) {
            Date refTime = new Date(entry.serverDate);
            headers.put("If-Modified-Since", DateUtils.formatDate(refTime));
        }
    }

}

