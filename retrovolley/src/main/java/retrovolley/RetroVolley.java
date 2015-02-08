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
package retrovolley;

import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.DiskBasedCache;
import com.sun.istack.internal.Nullable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An utility class, that stands as a network layer provider.
 *
 * @author Serghei Lotutovici
 */
public class RetroVolley {

    /**
     * The default caching directory name
     */
    private static final String DEFAULT_CACHE_DIR_NAME = "RetroVolley";

    /**
     * Applications main request queue
     */
    RequestQueue requestQueue;

    /**
     * List of supported endpoints
     */
    final Map<String, EndpointAdapter> endpointsMap;

    /**
     * Level of logging used for library
     */
    private int loggingLevel = Logging.NO_LOGS;

    /**
     * Singleton holder
     */
    private static class StaticHolder {
        public static final RetroVolley INSTANCE = new RetroVolley();
    }


    /**
     * This is a utility class no instance should be created
     */
    private RetroVolley() {
        super();
        endpointsMap = new LinkedHashMap<String, EndpointAdapter>();
    }


    /**
     * Returns an instance of Api object
     *
     * @return api instance
     */
    public static RetroVolley getInstance() {
        return StaticHolder.INSTANCE;
    }

    /**
     * Initialize {@link retrovolley.RetroVolley} with custom cache implementation
     * Build request queue and start it.
     *
     * @param cache Cache Implementation
     */
    public static void init(Cache cache) {
        final Network network = new RetroVolleyNetwork();
        StaticHolder.INSTANCE.requestQueue = new RequestQueue(cache, network);
        StaticHolder.INSTANCE.requestQueue.start();
    }

    /**
     * Initialize {@link retrovolley.RetroVolley} with a disk base cache
     *
     * @param context      Application context to get access to the applications cache directory
     * @param cacheDirName The name of {@link retrovolley.RetroVolley}'s cache directory
     */
    public static void init(Context context, String cacheDirName) {
        final File cacheDirPath = context.getCacheDir();
        final File cacheDir = new File(cacheDirPath, cacheDirName);
        init(new DiskBasedCache(cacheDir));
    }



    /**
     * Initialize {@link retrovolley.RetroVolley} with a disk base cache with a default name for the cache directory
     *
     * @param context Application context to get access to the applications cache directory
     */
    public static void init(Context context) {
        init(context, DEFAULT_CACHE_DIR_NAME);
    }

    /**
     * Add supported endpoint to RetroVolley initialization
     *
     * @param name    The endpoint unique name
     * @param adapter The endpoint's adapter
     */
    public static void supportEndpoint(String name, EndpointAdapter adapter) {
        if (StaticHolder.INSTANCE.endpointsMap == null) {
            throwInitializationException();
        }
        StaticHolder.INSTANCE.endpointsMap.put(name, adapter);
    }

    /**
     * Add all supported endpoints to RetroVolley initialization
     *
     * @param endpointsMap A collection of all supported endpoints with unique keys
     */
    public static void supportAllEndpoints(Map<String, EndpointAdapter> endpointsMap) {
        if (StaticHolder.INSTANCE.endpointsMap == null) {
            throwInitializationException();
        }
        StaticHolder.INSTANCE.endpointsMap.putAll(endpointsMap);
    }

    /**
     * Clears all cookies
     */
    public void clearCookies() {
        throw new UnsupportedOperationException("not implemented yet");
    }


    /**
     * Set logging level library. Use {@link retrovolley.Logging#VERBOSE} for maximum logging and
     * {@link retrovolley.Logging#NO_LOGS} to turn logging off (default value)
     * @param level integer value of logging level (constant from {@link retrovolley.Logging}
     */
    public static void setLoggingLevel(int level){
        StaticHolder.INSTANCE.loggingLevel = level;
    }

    /**
     * Returns current logging level (for internal use)
     * @return integer value of level (less for more verbose logs)
     */
    static int getLoggingLevel(){
        return StaticHolder.INSTANCE.loggingLevel;
    }

    /**
     * @return The applications request queue
     * @throws java.lang.IllegalStateException If the network layer is not initialized
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            throwInitializationException();
        }

        return requestQueue;
    }

    /**
     * Get the adapter associated with provided name.
     * If no name specified the first entry will be returned
     *
     * @param name The endpoint name
     * @return {@link retrovolley.EndpointAdapter} associated with name
     */
    @Nullable
    public EndpointAdapter getAdapter(String name) {
        if (endpointsMap == null || endpointsMap.isEmpty()) {
            throw new IllegalStateException("No supported endpoints");
        }

        if (name == null) {
            return endpointsMap.entrySet().iterator().next().getValue();
        }

        return endpointsMap.get(name);
    }

    /**
     * Throw an initialization exception notifying the caller to call
     * {@link RetroVolley#init(com.android.volley.Cache)} first.
     */
    private static void throwInitializationException() {
        throw new IllegalStateException("Api layer not initialized! Call #init() first.");
    }
}
