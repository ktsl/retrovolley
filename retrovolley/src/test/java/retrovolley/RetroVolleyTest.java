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

import com.android.volley.toolbox.NoCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;

/**
 * @author Konstantin Tarasenko
 */
@RunWith(RobolectricTestRunner.class)
@Config(
        manifest = Config.NONE,
        shadows = {retrovolley.shadows.ShadowSystemClock.class}
)
public class RetroVolleyTest {

    @Before
    public void setUp() {
        // Null the request queue before starting testing init
        RetroVolley.getInstance().requestQueue = null;
    }

    @Test
    public void testGetRetroInstanceNoCache() {
        RetroVolley.init(new NoCache());
        assertNotNull(RetroVolley.getInstance().getRequestQueue());
    }


    @Test(expected = IllegalStateException.class)
    public void testGetRetroInstanceFails() {
        RetroVolley.getInstance().getRequestQueue();
    }
}
