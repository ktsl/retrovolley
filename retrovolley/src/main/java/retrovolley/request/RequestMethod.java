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

import com.android.volley.Request;

/**
 * An enum that maps all volley request types currently supported
 *
 * @author Serghei Lotutovici
 */
public enum RequestMethod {

    /**
     * GET request method
     */
    GET(Request.Method.GET),

    /**
     * POST request method
     */
    POST(Request.Method.POST),

    /**
     * PUT request method
     */
    PUT(Request.Method.PUT),

    /**
     * DELETE request method
     */
    DELETE(Request.Method.DELETE);

    /**
     * Method integer flag from volley library
     */
    public final int method;

    /**
     * Build a request type object with specified flag
     *
     * @param method The method int flag
     */
    private RequestMethod(final int method) {
        this.method = method;
    }
}
