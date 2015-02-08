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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

import retrovolley.Logging;

/**
 * A {@link Converter} which uses GSON for serialization and de-serialization of entities.
 *
 * @author Serghei Lotutovici
 */
public class GsonConverter implements Converter {

    /**
     * The GSON object
     */
    private final Gson mGson;

    /**
     * Constructs an entity of GsonConverter with default GSON object
     */
    public GsonConverter() {
        this(new Gson());
    }

    /**
     * Constructs an entity of GsonConverter with custom GSON object
     *
     * @param gson The GSON object to set
     */
    public GsonConverter(Gson gson) {
        mGson = gson;
    }

    @Override
    public Object fromBody(String body, Type type) throws ConversionException {

        try {
            return mGson.fromJson(body, type);
        } catch (JsonSyntaxException jse) {
            throw new ConversionException(jse);
        }

    }

    @Override
    public String toBody(Object obj) {
        return mGson.toJson(obj);
    }
}
