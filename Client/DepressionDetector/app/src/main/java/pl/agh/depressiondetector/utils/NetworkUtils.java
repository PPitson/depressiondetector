package pl.agh.depressiondetector.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.connection.HttpClient;

import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.HttpClient.JSON_TYPE;

public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        } else {
            final NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    public static boolean postJSONArray(JSONArray jsonArray, Context context, String encodedPathSegments) {
        RequestBody requestBody = RequestBody.create(JSON_TYPE, jsonArray.toString());
        return post("POST_JSON_ARRAY", requestBody, context, encodedPathSegments);
    }

    public static boolean postFile(File file, Context context, String encodedPathSegments, MediaType contentType) {
        RequestBody requestBody = RequestBody.create(contentType, file);
        return post("POST_FILE", requestBody, context, encodedPathSegments);
    }

    private static boolean post(String TAG, RequestBody requestBody, Context context, String encodedPathSegments) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addEncodedPathSegments(encodedPathSegments)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", getBasicCredentials(context))
                .post(requestBody)
                .build();

        try {
            Response response = HttpClient.getClient().newCall(request).execute();
            Log.i(TAG, "Server returned: " + response.message() + " with code " + response.code());
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getResults(String TAG, Context context, String encodedPathSegments) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addEncodedPathSegments(encodedPathSegments)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", getBasicCredentials(context))
                .get()
                .build();

        String result = null;
        try {
            Response response = HttpClient.getClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            Log.i(TAG, "Server returned: " + response.message() + " with code " + response.code());
            if (responseBody != null) {
                if (response.isSuccessful())
                    result = responseBody.string();
                responseBody.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String getBasicCredentials(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String name = preferences.getString(context.getString(R.string.pref_user_username), "");
        String password = preferences.getString(context.getString(R.string.pref_user_password), "");
        return Credentials.basic(name, password);
    }
}
