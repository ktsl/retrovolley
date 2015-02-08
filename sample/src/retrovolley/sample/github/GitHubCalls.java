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
package retrovolley.sample.github;

import retrovolley.annotation.Endpoint;
import retrovolley.annotation.GET;
import retrovolley.rest.RestCall;
import retrovolley.sample.Consts;

public enum GitHubCalls implements RestCall {

    @GET("/")
    @Endpoint(Consts.GITHUB_ENDPOINT)
    ROOT

}