package pl.agh.depressiondetector.connection;

import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.API.PATH_RESULTS;
import static pl.agh.depressiondetector.connection.API.PATH_SOUND_FILES;

// TODO Consider Retrofit instead of OkHttp
public class HttpClient {

    private static final String TAG = "HttpClient";

    private static HttpClient instance;
    private OkHttpClient client;

    private HttpClient() {
        client = new OkHttpClient();
    }

    public static HttpClient getInstance() {
        if (instance == null)
            instance = new HttpClient();
        return instance;
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

            Response response;
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
                System.out.println(result);

            } else {
                if (response.body() != null)
                    response.body().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public int postAudioFile(File file) {
        int responseCode = -1;
        try {
            byte[] data = FileUtils.readFileToByteArray(file);
            String url = "https://" + HOST + "/" + PATH_SOUND_FILES;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "audio/amr;");
            conn.setDoOutput(true);
            OutputStream writer = conn.getOutputStream();
            writer.write(data);
            writer.flush();
            writer.close();
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            Log.e(TAG, "Error while posting audio file " + e);
        }

        return responseCode;
    }
}
