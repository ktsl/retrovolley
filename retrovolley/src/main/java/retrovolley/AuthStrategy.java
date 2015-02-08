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

import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpStack;
import retrovolley.converter.Converter;

/**
 * @author  Konstantin Tarasenko
 */
public interface AuthStrategy {

    /**
     * Tries to reauthorise authorise user using stored credentials.
     * Should perform all network queries in the same thread blocking execution
     *
     * @param stack  Http stack to perform requests
     * @param converter Converter suitable for current endpoint setup
     * @param helper Network helper
     * @return true if authentication were performed
     * @throws VolleyError
     */
    boolean  authenticate(HttpStack stack, Converter converter, NetworkHelper helper) throws VolleyError;
}
