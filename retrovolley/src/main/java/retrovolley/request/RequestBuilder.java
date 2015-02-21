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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import retrovolley.rest.Hateoasles;
import retrovolley.rest.RestCall;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An abstract request builder, that should be used as facade layer
 * for different request constructions.
 *
 * @author Serghei Lotutovici
 */
public class RequestBuilder<T> {

    /**
     * Debug tag for RequestBuilder
     */
    private static final String TAG = RequestBuilder.class.getName();

    /**
     * Default request cache time (8 hours)
     */
    private static final long DEFAULT_REQUEST_CACHE_TIME = 8 * 60 * 60 * 1000;

    /**
     * Request parameters
     */
    private Map<String, String> mParams;

    /**
     * Additional headers specified by the request builder
     */
    private Map<String, String> mHeaders;

    /**
     * Url parameters map
     */
    private Map<String, String> mRestParams;

    /**
     * The request body, disables the parameter usage
     */
    private String mBody;

    /**
     * Request flow listener
     */
    private Callback<T> mCallback;

    /**
     * Should cache flag
     */
    private boolean mShouldCache;

    /**
     * Request tag object
     */
    private Object mTag;

    /**
     * Annotations processor object that is initialized from a restCall object instance
     */
    private final RequestInfo mRequestInfo;

    /**
     * Initial timeout for request
     */
    private int mTimeout = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;

    /**
     * Number of retries
     */
    private int mNumberOfRetries = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;

    /**
     * used to increase request timeout for
     * next attempts multiplying current timeout value by this amount
     */
    private float mBackOffMultiplier = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;

    /**
     * Creates an instance of the request builder
     */
    public RequestBuilder(final RestCall restCall) {
        this(new RequestInfo(restCall));
    }

    /**
     * Create an instance of the request builder from a hateoas link
     *
     * @param restCall The rest call
     * @param link     The hateoas link to use
     */
    public RequestBuilder(final RestCall restCall, final Hateoasles link) {
        this(new HateoasRequestInfo(restCall, link));
    }

    public RequestBuilder(final RestCall restCall, final String url) {
        this(new DynamicRequestInfo(restCall, url));
    }

    private RequestBuilder(RequestInfo requestInfo) {
        super();
        mBody = null;
        mParams = new HashMap<String, String>();
        mHeaders = new HashMap<String, String>();
        mRestParams = new HashMap<String, String>();
        mShouldCache = false;
        mRequestInfo = requestInfo;
        if (requestInfo.getMaxNumRetries() > -1) {
            mNumberOfRetries = requestInfo.getMaxNumRetries();
        }
    }

    protected Map<String, String> getParams() {
        return mParams;
    }

    protected Map<String, String> getHeaders() {
        return mHeaders;
    }

    protected String getBody() {
        return mBody;
    }

    protected boolean getShouldCache() {
        return mShouldCache;
    }

    protected RetryPolicy getRetryPolicy() {
        return new DefaultRetryPolicy(mTimeout, mNumberOfRetries, mBackOffMultiplier);
    }


    /**
     * Add a request parameter for the request. <br>
     * <b>Note: </b> If {@link RequestBuilder#setBody(java.lang.String)}
     * is called, all parameters will be ignored.
     *
     * @param key   The parameter key
     * @param value The parameter value
     * @return The same builder instance
     */
    public RequestBuilder addParam(String key, String value) {
        mParams.put(key, value);
        return this;
    }

    /**
     * Add additional request header.
     *
     * @param key   The header key
     * @param value The header value
     * @return The same builder instance
     */
    public RequestBuilder addHeader(String key, String value) {
        mHeaders.put(key, value);
        return this;
    }

    /**
     * Add rest url parameters that will be replaced on runtyme
     *
     * @param key   The param key that matches the one set in RestCall object
     * @param value The param value that needs to be replaced
     * @return The same builder instance
     */
    public RequestBuilder addRestParam(String key, String value) {
        RequestInfo.validateParameterName(mRequestInfo, key);
        mRestParams.put(key, value);
        return this;
    }

    /**
     * Set a request body to the request.<br>
     * <b>Note: </b> Calling this method with a non null parameter will ignore the usage of
     * request parameters.
     *
     * @param body The string body to set.
     * @return Same builder instance.
     */
    public RequestBuilder setBody(String body) {
        mBody = body;
        return this;
    }

    /**
     * Set a request body to the request.<br>
     * <b>Note: </b> Calling this method with a non null parameter will ignore the usage of
     * request parameters.
     *
     * @param body The body object to set
     * @param <E>  The body's Java type.
     * @return Same builder instance.
     */
    public <E> RequestBuilder setBody(E body) {
        return setBody(mRequestInfo.getEndpoint().getConverter().toBody(body));
    }

    /**
     * Set the request callback
     *
     * @param callback The callback to set
     * @return Same builder instance
     */
    public RequestBuilder setCallback(Callback<T> callback) {
        mCallback = callback;
        return this;
    }

    public RequestBuilder shouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
        return this;
    }

    public RequestBuilder setTag(Object tag) {
        mTag = tag;
        return this;
    }

    /**
     * Sets initial socket timeout for requests
     *
     * @param timeoutMs in ms
     */
    public void setTimeout(int timeoutMs) {
        mTimeout = timeoutMs;
    }

    /**
     * Sets number of retries
     *
     * @param numberOfRetries
     */
    public void setNumberOfRetries(int numberOfRetries) {
        mNumberOfRetries = numberOfRetries;
    }

    /**
     * used to increase request timeout for consecutive attempts
     * multiplying current timeout value by this amount
     *
     * @param backOffMultiplier float value of multiplier
     */
    public void setBackOffMultiplier(float backOffMultiplier) {
        mBackOffMultiplier = backOffMultiplier;
    }

    /**
     * Build a basic POJO request. As a result there will be the specified Java object.
     *
     * @return A new instance of PojoRequest.
     */
    public RetroRequest<T> build() {

        /* Create request */
        RetroRequest<T> request = new RetroRequest<T>(
                mRequestInfo.getMethod(),
                buildUrl(),
                mCallback,
                getHeaders(),
                getParams(),
                getShouldCache(),
                DEFAULT_REQUEST_CACHE_TIME,
                mRequestInfo.getResponseType(),
                getRetryPolicy(),
                mRequestInfo.getEndpoint());

        /* Append body if set */
        if (getBody() != null) {
            request.setJsonBody(getBody());
        }

        /* Add tag to request tag */
        if (mTag != null) {
            request.setTag(mTag);
        }

        /* Return constructed request */
        return request;
    }

    /**
     * Build the POJO request and execute
     */
    public void execute() {
        build().execute();
    }

    /**
     * Build the url for request execution. Add additional parameters if necessary.
     *
     * @return The request url
     */
    private String buildUrl() {
        /* By default the url is stored in the request info object */
        String url = mRequestInfo.getUrl();

        /* Append get parameters if request method is GET */
        if (mRequestInfo.getMethod() == RequestMethod.GET.method) {
            url = buildGETUrl(url, getParams());
        }

        /* Insert rest params to the request path if needed */
        if (mRequestInfo.getRestParams() != null) {
            /* Check that we have the same number of parameters set */
            if (mRequestInfo.getRestParams().size() != mRestParams.size()) {
                throw new IllegalArgumentException(String.format(
                        "The number of url parameters: %s doesn't match the number of injected parameters: %s",
                        mRequestInfo.getRestParams().toString(),
                        mRestParams.toString()
                ));
            }

            /* Replace by key every parameter */
            for (Map.Entry<String, String> entry : mRestParams.entrySet()) {
                url = url.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return url;
    }

    /**
     * Prepare the GET request URL and append required parameters
     *
     * @param url    The url string value
     * @param params The parameters map
     * @return A new url with appended parameters
     */
    public static String buildGETUrl(String url, Map<String, String> params) {
        final StringBuilder urlBuilder = new StringBuilder(url);

        /* Simple null check */
        if (params != null && !params.isEmpty()) {
            /* Add question mark is not present */
            if (!url.endsWith("?")) {
                urlBuilder.append("?");
            }

            /* Create GET parameters */
            List<NameValuePair> getParams = new LinkedList<NameValuePair>();
            for (Map.Entry<String, String> param : params.entrySet()) {
                getParams.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }

            urlBuilder.append(URLEncodedUtils.format(getParams, "utf-8"));
        }

        return urlBuilder.toString();
    }
}
