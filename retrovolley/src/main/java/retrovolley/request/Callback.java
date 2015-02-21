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
import com.android.volley.VolleyError;

/**
 * Main callback for {@link retrovolley.request.RetroRequest} that provides methods
 * executed on the main thread
 *
 * @author Serj Lotutovici
 */
public interface Callback<T> {

    /**
     * Called when a successful HTTP response was received
     *
     * @param body     Parsed response body
     * @param response The actual network response
     */
    void success(T body, NetworkResponse response);

    /**
     * Called when an unsuccessful HTTP response was received due to network failure,
     * non-2XX status code, or unexpected exception.
     *
     * @param error The Volley error
     */
    void failure(VolleyError error);

    /**
     * Called before the requires is added to the {@link com.android.volley.RequestQueue}
     */
    void before();

}
