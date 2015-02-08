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

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;

import retrovolley.Logging;
import retrovolley.converter.ConversionException;
import retrovolley.EndpointAdapter;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * A request that parses the response into a PJO (Plain Java Object).
 * The resulting class is specified via the clazz parameter.
 *
 * @author Bogdan Nistor
 * @author Serghei Lotutovici
 */
public class PojoRequest<T> extends AbstractRequest<T> {

    private final Type mType;
    private final EndpointAdapter mEndpointAdapter;

    /**
     * {@inheritDoc}
     *
     * @param type Response class object
     */
    PojoRequest(
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
        super(method, url, requestListener, headers, postParams, shouldCache, cacheTimeInMillis);
        mType = type;
        mEndpointAdapter = endpointAdapter;
        setRetryPolicy(retryPolicy);
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

    public EndpointAdapter getEndpointAdapter() {
        return mEndpointAdapter;
    }
}
