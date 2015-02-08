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
package retrovolley.converter;

import java.lang.reflect.Type;

/**
 * Arbiter for converting objects to and from their representation in HTTP.
 * <p/>
 * Inspired by retrofit implementation.
 *
 * @author Serghei Lotutovici
 */
public interface Converter {

    /**
     * Convert from string response to java object
     *
     * @param body The string value of the HTTP response body
     * @param type Target object type
     * @return Instance of {@code type} which will be casted by the caller
     * @throws retrovolley.converter.ConversionException In case the conversion resulted in a failure
     */
    Object fromBody(String body, Type type) throws ConversionException;

    /**
     * Convert java object to an HTTP body
     *
     * @param obj The object to convert
     * @return The HTTP body as a string value
     */
    String toBody(Object obj);

}
