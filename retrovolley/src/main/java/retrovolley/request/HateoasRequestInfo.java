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

import retrovolley.rest.Hateoasles;
import retrovolley.rest.RestCall;

/**
 * A request info that works only with hateoas links
 *
 * @author Serghei Lotutovici
 */
class HateoasRequestInfo extends RequestInfo {

    /**
     * The hateoas request url
     */
    private String mUrl;

    /**
     * Build a hateoas request info
     *
     * @param restCall The rest call to parse
     * @param link     The hateoas link object to use
     */
    public HateoasRequestInfo(RestCall restCall, Hateoasles link) {
        super(restCall);

        /* Validate input */

        if (!isHateoas()) {
            throw new IllegalArgumentException("The rest call must be a hateoas call");
        }

        if (link == null) {
            throw new IllegalArgumentException("HateoasLink must not be null");
        }

        mUrl = link.getPath();
        setMethod(link.getMethod().method);
    }

    /**
     * {@inheritDoc}
     * <br>
     * Skip building the url
     */
    @Override
    public String getUrl() {
        return mUrl;
    }

}
