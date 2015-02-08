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

import retrovolley.rest.RestCall;

/**
 * A request info object that allows the caller to set the request url.
 *
 * @author Serghei Lotutovici
 */
class DynamicRequestInfo extends RequestInfo {

    private String mUrl;

    public DynamicRequestInfo(RestCall restCall, String url) {
        super(restCall);

        /* Validate input */
        if (!isDynamic()) {
            throw new IllegalArgumentException("The rest call must be a dynamic call");
        }

        if (url == null) {
            throw new IllegalArgumentException("The url must not be null");
        }

        /* Save request urls */
        mUrl = url;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }
}
