package pl.agh.depressiondetector.connection;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.API.PATH_RESULTS;

public class HttpClient {

    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType AMR_TYPE = MediaType.parse("audio/amr");

    private static HttpClient instance;
    private static OkHttpClient client;

    private HttpClient() {
        client = new OkHttpClient();
    }

    public static HttpClient getInstance() {
        if (instance == null)
            instance = new HttpClient();
        return instance;
    }

    public static OkHttpClient getClient() {
        if (instance == null)
            instance = new HttpClient();
        return client;
    }

    @Nullable
    public String getLatestResult() {
        String result = null;
        try {
            HttpUrl apiUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(HOST)
                    .addEncodedPathSegments(PATH_RESULTS)
                    .build();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            if (body != null) {
                if (response.isSuccessful())
                    result = body.string();
                body.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
