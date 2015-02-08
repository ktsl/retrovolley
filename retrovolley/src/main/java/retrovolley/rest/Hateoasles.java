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
package retrovolley.rest;

import retrovolley.request.RequestMethod;

/**
 * Represents a HATEOAS REST call object, that can be used for requests that are managed by the API
 * <br>
 * <b>DISCLAIMER: </b>By contract any implementation of this interface must have two fields,
 * that will represent the request URI and the request method (GET, POST, PUT, etc.).
 *
 * @author Serghei Lotutovici
 */
public interface Hateoasles {

    /**
     * @return Full url path for the request
     */
    String getPath();

    /**
     * @return Which HTTP request method to use
     */
    RequestMethod getMethod();
}
