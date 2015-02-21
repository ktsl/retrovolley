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

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import retrovolley.EndpointAdapter;
import retrovolley.Logging;
import retrovolley.RetroVolley;
import retrovolley.converter.ConversionException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A request that parses the response into a PJO (Plain Java Object).
 * The resulting class is specified via the clazz parameter.
 *
 * @author Bogdan Nistor
 * @author Serghei Lotutovici
 */
public class RetroRequest<T> extends Request<T> {

    /**
     * // TODO To be removed
     * Request Listener
     */
    private final RequestListener<T> mRequestListener;

    /**
     * Request post params. Will be ignored if mJsonBody is set.
     */
    private Map<String, String> mParams;

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

    private final Type mType;
    private final EndpointAdapter mEndpointAdapter;

    /**
     * {@inheritDoc}
     *
     * @param type Response class object
     */
    RetroRequest(
            int method,
            String url,
            RequestListener<T> requestListener,
            Map<String, String> headers,
            Map<String, String> postParams,
            boolean shouldCache,
            long cacheTimeInMillis,
            Type type,
            RetryPolicy retryPolicy,
            EndpointAdapter endpointAdapter) {
        super(method, url, requestListener);

        mRequestListener = requestListener;
        mHeaders = headers;
        mParams = postParams;
        mCacheTimeInMillis = cacheTimeInMillis;
        mType = type;
        mEndpointAdapter = endpointAdapter;

        setShouldCache(shouldCache);
        setRetryPolicy(retryPolicy);
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

            for (Map.Entry<String, String> postParamEntry : mParams.entrySet()) {
                postParams.put(postParamEntry.getKey(), postParamEntry.getValue());
            }
        }

        return postParams;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response<T> parseNetworkResponse(NetworkResponse response) {
        Logging.d("Parsing network response");
        /* Get the response data */
        String json;
        try {
            json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException uee) {
            Logging.e("Encoding is not supported ", uee);
            return Response.error(new ParseError(uee));
        }

        /*
         * Try to pars the response first. If we catch a JsonParsException,
         * we will try to pars the server response for errors.
         */
        try {

            /* If the class type is string then we don't need to parse the response from json */
            T result;
            if (mType == String.class) {
                result = (T) json;
            } else {
                result = (T) mEndpointAdapter.getConverter().fromBody(json, mType);
            }

            /* Return the parsed result in a response wrapper */
            return shouldCache() ?
                    Response.success(result, InternalHttpHeaderParser.parseIgnoreCacheHeaders(response, getCacheTimeInMillis())) :
                    Response.success(result, HttpHeaderParser.parseCacheHeaders(response));

        } catch (ConversionException ce) {
            /* Throw a general exception error */
            Logging.e("Cannot convert response to json", ce);
            return Response.error(new ParseError(ce));
        }
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

    public EndpointAdapter getEndpointAdapter() {
        return mEndpointAdapter;
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
