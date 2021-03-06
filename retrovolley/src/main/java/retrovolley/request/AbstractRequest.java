/*
 * Copyright (C) 2015 Serghei (Serj) Lotutovici
 * Copyright (C) 2015 Konstantin Tarasenko
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
package retrovolley.request;

import android.util.Pair;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrovolley.RetroVolley;

/**
 * An abstract request class that defines the basic request build logic for all api calls.
 * This class is made package private to block undesired inheritance.
 *
 * @author Serghei Lotutovici
 * @author Bogdan Nistor
 */
abstract class AbstractRequest<T> extends Request<T> {

    /**
     * Default caching time (how long should the request response be cached).
     */
    private static final long DEFAULT_CACHE_TIME_MILLIS = -1L;

    /**
     * Response listener
     */
    private final RequestListener<T> mRequestListener;

    /**
     * Request post params. Will be ignored if mJsonBody is set.
     */
    private List<Pair<String, String>> mParams;

    /**
     * Request headers.
     */
    private Map<String, String> mHeaders;

    /**
     * Json body, for request that require json objects instead of post params
     */
    private String mJsonBody;

    /**
     * Request response caching time in milliseconds
     */
    private long mCacheTimeInMillis;

    /**
     * Main request constructor. Applies all required fields for request customization.
     *
     * @param method            The request method (GET, POST, SET, e.t.c.)
     * @param url               The request url
     * @param requestListener   Generic request listener
     * @param headers           The request headers map
     * @param params            The request parameters map
     * @param shouldCache       Cache flag, if the request should cache then true
     * @param cacheTimeInMillis Time of caching value in milliseconds
     */
    AbstractRequest(
            int method,
            String url,
            RequestListener<T> requestListener,
            Map<String, String> headers,
            List<Pair<String, String>> params,
            boolean shouldCache,
            long cacheTimeInMillis) {
        super(method, url, requestListener);

        /* Init all fields */
        this.mRequestListener = requestListener;
        this.mHeaders = headers;
        this.mParams = params;
        this.mCacheTimeInMillis = cacheTimeInMillis;

        /* Set if the request should cache */
        setShouldCache(shouldCache);
    }

    /**
     * Constructs a request with caching option disabled.
     *
     * @param method          The request method (GET, POST, SET, e.t.c.)
     * @param url             The request url
     * @param requestListener Generic request listener
     * @param headers         The request headers map
     * @param params          The request parameters map
     */
    AbstractRequest(
            int method,
            String url,
            RequestListener<T> requestListener,
            Map<String, String> headers,
            List<Pair<String, String>> params) {
        this(method, url, requestListener, headers, params, false, DEFAULT_CACHE_TIME_MILLIS);
    }

    /**
     * Constructs a request with empty headers and disabled caching options.
     *
     * @param method          The request method (GET, POST, SET, e.t.c.)
     * @param url             The request url
     * @param requestListener Generic request listener
     */
    AbstractRequest(
            int method,
            String url,
            RequestListener<T> requestListener) {
        this(method, url, requestListener, new HashMap<String, String>(), new ArrayList<Pair<String, String>>() {
        });
    }

    /**
     * Build silent request with not callbacks involved.
     *
     * @param method  The request method (GET, POST, SET, e.t.c.)
     * @param url     The request url
     * @param headers The request headers map
     * @param params  The request parameters map
     */
    AbstractRequest(
            int method,
            String url,
            Map<String, String> headers,
            List<Pair<String, String>> params) {
        this(method, url, null, headers, params);
    }

    @Override
    protected void deliverResponse(T response) {
        if (mRequestListener != null) {
            mRequestListener.onResponse(response);
        }
    }

    /**
     * Overriding a deprecated method in case, some internal methods use it.<br>
     * <p/>
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=UTF-8";
    }

    /**
     * Overriding a deprecated method in case, some internal methods use it.<br>
     * <p/>
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    protected String getPostParamsEncoding() {
        return getParamsEncoding();
    }

    @Override
    protected String getParamsEncoding() {
        return "UTF-8";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        byte[] body = null;

        if (mJsonBody != null) {
            try {
                body = mJsonBody.getBytes(getParamsEncoding());
            } catch (UnsupportedEncodingException uee) {
                body = mJsonBody.getBytes();
            }
        } else {
            Map<String, String> params = getParams();
            if (params != null && !params.isEmpty()) {
                body = encodeParameters(params, getParamsEncoding());
            }
        }

        return body;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }

        if (mHeaders != null) {
            headers.putAll(mHeaders);
        }

        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> postParams = super.getParams();

        if (mParams != null) {
            if (postParams == null || postParams.equals(Collections.emptyMap())) {
                postParams = new HashMap<String, String>();
            }

            for (Pair<String, String> postParamsPair : mParams) {
                postParams.put(postParamsPair.first, postParamsPair.second);
            }
        }

        return postParams;
    }

    /**
     * Execute the request by adding it to the applications request queue
     */
    public void execute() {
        /* Notify the listener that the request is being added to the queue */
        if (mRequestListener != null) {
            mRequestListener.onExecute();
        }

        /* Add request to the applications request queue */
        RetroVolley.getInstance().getRequestQueue().add(this);
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {

            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }

            return encodedParams.toString().getBytes(paramsEncoding);

        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    /**
     * @return Get the cache time in milliseconds
     */
    long getCacheTimeInMillis() {
        return mCacheTimeInMillis;
    }

    /**
     * Set the json body, this will override the usage of post params
     *
     * @param jsonBody The json body to set
     */
    public void setJsonBody(final String jsonBody) {
        mJsonBody = jsonBody;
    }

}
