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

import android.util.Log;

import com.android.volley.VolleyError;

/**
 * Logging related utils and constants
 *
 * @author Konstantin Tarasenko
 */
public class Logging {

    public static final String TAG = "RetroVolley";

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;
    public static final int NO_LOGS = 8;

    public static void d(String msg){
        if (RetroVolley.getLoggingLevel() <= DEBUG){
            Log.d(TAG, msg);
        }
    }

    public static void d(String msg, Throwable t) {
        if (RetroVolley.getLoggingLevel() <= DEBUG){
            Log.d(TAG, msg, t);
        }
    }

    public static void e(String msg, Throwable t){
        if (RetroVolley.getLoggingLevel() <= ERROR){
            Log.e(TAG, msg, t);
        }
    }


}
