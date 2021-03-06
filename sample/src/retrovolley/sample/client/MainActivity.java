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
package retrovolley.sample.client;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.android.volley.VolleyError;
import retrovolley.request.RequestBuilder;
import retrovolley.request.RequestListener;
import retrovolley.sample.R;
import retrovolley.sample.github.GitHubCalls;


public class MainActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        new RequestBuilder<String>(GitHubCalls.ROOT)
                .setRequestListener(new RequestListener<String>() {
                    @Override
                    public void onExecute() {
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTextView.setText(error.getMessage());
                    }

                    @Override
                    public void onResponse(String response) {
                        mTextView.setText(response);
                    }
                }).execute();
    }

}
