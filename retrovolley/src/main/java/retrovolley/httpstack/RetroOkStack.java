package retrovolley.httpstack;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLContext;

/**
 * An {@link com.android.volley.toolbox.HttpStack HttpStack} implementation which
 * uses OkHttp as its transport.
 */
public class RetroOkStack extends RetroHurlStack {
    private final OkUrlFactory mFactory;

    public RetroOkStack() {
        this(new OkHttpClient());
    }

    public RetroOkStack(OkHttpClient client) {
        if (client == null) {
            throw new NullPointerException("Client must not be null.");
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            client.setSslSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }

        mFactory = new OkUrlFactory(client);
    }

    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        return mFactory.open(url);
    }

    @Override
    public void clearCookies() {
       throw new UnsupportedOperationException("Not implemented yet");
    }
}