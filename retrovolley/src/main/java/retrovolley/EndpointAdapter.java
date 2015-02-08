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

import retrovolley.converter.Converter;
import retrovolley.converter.GsonConverter;
import retrovolley.httpstack.RetroHurlStack;
import retrovolley.httpstack.RetroStack;

/**
 * An interface that describes the basic endpoint object parameters
 * (like BASIC URL, HTTP HEADERS, etc.).
 * <br>
 * <b>DISCLAIMER: </b>By contract any implementation of this interface must have
 * a constructor with no parameters, otherwise the annotations processor will throw an exception.
 *
 * @author Konstantin Tarasenko
 */
public final class EndpointAdapter {


    final String endpoint;
    final RetroStack httpStack;
    final Converter converter;
    final RequestInterceptor requestInterceptor;
    final AuthStrategy authStrategy;

    private EndpointAdapter(String endpoint, RetroStack httpStack, Converter converter,
                            RequestInterceptor requestInterceptor, AuthStrategy authStrategy) {
        this.endpoint = endpoint;
        this.httpStack = httpStack;
        this.converter = converter;
        this.requestInterceptor = requestInterceptor;
        this.authStrategy = authStrategy;
    }

    /**
     * @return Request interceptor related to endpoint
     */
    public RequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    /**
     * @return The Endpoint's URL string
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @return HttpStack (transport layer) for the endpoint
     */
    public RetroStack getHttpStack() {
        return httpStack;
    }


    /**
     * @return Auth strategy related to current endpoint
     */
    public AuthStrategy getAuthStrategy() {
        return authStrategy;
    }


    /**
     * @return Response converter used for this endpoint
     */
    public Converter getConverter() {
        return converter;
    }

    public static class Builder {

        String endpoint;
        RetroStack httpStack;
        Converter converter;
        RequestInterceptor requestInterceptor;
        AuthStrategy authStrategy;

        public Builder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder setHttpStack(RetroStack httpStack) {
            this.httpStack = httpStack;
            return this;
        }

        public Builder setConverter(Converter converter) {
            this.converter = converter;
            return this;
        }

        public Builder setRequestInterceptor(RequestInterceptor requestInterceptor) {
            this.requestInterceptor = requestInterceptor;
            return this;
        }

        public Builder setAuthStrategy(AuthStrategy authStrategy) {
            this.authStrategy = authStrategy;
            return this;
        }

        /**
         * Create new {@link EndpointAdapter} instance
         */
        public EndpointAdapter build() {
            if (endpoint == null) {
                throw new IllegalArgumentException("Endpoint must not be null");
            }
            ensureSaneDefaults();
            return new EndpointAdapter(
                    endpoint,
                    httpStack,
                    converter,
                    requestInterceptor,
                    authStrategy
            );
        }

        /**
         * Ensures the adapter is build with usable parameters
         */
        private void ensureSaneDefaults() {
            if (httpStack == null) {
                httpStack = new RetroHurlStack();
            }
            if (converter == null) {
                converter = new GsonConverter();
            }
        }
    }
}
