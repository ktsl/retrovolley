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

import com.android.volley.ServerError;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.PoolingByteArrayOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains some logic that could be shared between
 * all network layer classes to help perform request and read a response
 *
 * @author Konstantin Tarasenko
 */
class NetworkHelper {

    /**
     * Debug tag
     */
    private static final String TAG = NetworkHelper.class.getSimpleName();

    protected final ByteArrayPool mPool;

    public NetworkHelper(ByteArrayPool pool) {
        mPool = pool;
    }

    /**
     * Reads the contents of HttpEntity into a byte[].
     */
    private byte[] entityToBytes(HttpEntity entity) throws IOException, ServerError {
        PoolingByteArrayOutputStream bytes =
                new PoolingByteArrayOutputStream(mPool, (int) entity.getContentLength());
        byte[] buffer = null;
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }
            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources by "consuming the content".
                entity.consumeContent();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                Logging.e("Can't read response", e);
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }

    /**
     * Converts Headers[] to Map<String, String>.
     */
    Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (Header header : headers) {
            result.put(header.getName(), header.getValue());
        }
        return result;
    }


    byte[] getResponse(HttpResponse httpResponse) throws IOException, ServerError {
        /* Some responses such as 204s do not have content.  We must check. */
        if (httpResponse.getEntity() != null) {
            return entityToBytes(httpResponse.getEntity());
        } else {
            /* Add 0 byte response as a way of honestly representing a no-content request. */
            return new byte[0];
        }
    }

}
