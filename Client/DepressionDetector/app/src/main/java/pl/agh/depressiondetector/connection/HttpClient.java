package pl.agh.depressiondetector.connection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class HttpClient {

    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType AMR_TYPE = MediaType.parse("audio/amr");

    private static HttpClient instance;
    private static OkHttpClient client;

    private HttpClient() {
        client = new OkHttpClient();
    }

    public static OkHttpClient getClient() {
        if (instance == null)
            instance = new HttpClient();
        return client;
    }
}
