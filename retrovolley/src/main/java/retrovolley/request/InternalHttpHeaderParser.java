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

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

/**
 * Custom Http header parser. Made package private to avoid unnecessary usage.
 *
 * @author Bogdan Nistor
 */
class InternalHttpHeaderParser extends HttpHeaderParser {
    private static final long DEFAULT_CACHE_TIME_IN_MILLIS = 60 * 60 * 1000;

    /**
     *
     * @param response The network response to parse headers from
     * @return A cache entry for the given response
     */
    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
        return parseIgnoreCacheHeaders(response, -1);
    }

    /**
     * Extracts a {@link com.android.volley.Cache.Entry} from a {@link com.android.volley.NetworkResponse}.
     * Cache-control headers are ignored. SoftTtl == 3 min, ttl == 24 hours.
     *
     * @param response The network response to parse headers from
     * @return A cache entry for the given response
     */
    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response, long cacheTimeInMillis) {
        /* Get headers to easier use */
        Map<String, String> headers = response.headers;

        /* Get the server date */
        long serverDate = 0;
        String date = headers.get("Date");
        if (date != null) {
            serverDate = parseDateAsEpoch(date);
        }

        /* Pars servers ETag values */
        final String serverETag = headers.get("ETag");

        /* Initialize caching time constrain values */
        final long now = System.currentTimeMillis();
        // Amount of time in which the cache will be hit, but also refreshed on background
        final long cacheHitButRefreshed = cacheTimeInMillis > 0 ? cacheTimeInMillis : DEFAULT_CACHE_TIME_IN_MILLIS;
        // Amount of time in which this cache entry expires completely
        final long cacheExpired = cacheTimeInMillis > 0 ? cacheTimeInMillis : DEFAULT_CACHE_TIME_IN_MILLIS;
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;

        /* Build cache entry */
        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverETag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;

        return entry;
    }
}