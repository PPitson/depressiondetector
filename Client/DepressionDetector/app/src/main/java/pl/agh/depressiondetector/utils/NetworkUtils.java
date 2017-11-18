package pl.agh.depressiondetector.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.connection.HttpClient;

import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.HttpClient.JSON_TYPE;

public final class NetworkUtils {

    private static final String TAG = "NetworkUtils";

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

    public static boolean postJSON(JSONObject jsonObject, Context context, String encodedPathSegments) {
        RequestBody requestBody = RequestBody.create(JSON_TYPE, jsonObject.toString());
        return post(requestBody, context, encodedPathSegments);
    }

    public static boolean postJSON(JSONArray jsonArray, Context context, String encodedPathSegments) {
        RequestBody requestBody = RequestBody.create(JSON_TYPE, jsonArray.toString());
        return post(requestBody, context, encodedPathSegments);
    }

    public static boolean postFile(File file, Context context, String encodedPathSegments, MediaType contentType) {
        RequestBody requestBody = RequestBody.create(contentType, file);
        return post(requestBody, context, encodedPathSegments);
    }

    private static boolean post(RequestBody requestBody, Context context, String encodedPathSegments) {
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
            Log.i(TAG, "Server returned: " + response.message() + " with code " + response.code() + " for path " + encodedPathSegments);
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getBasicCredentials(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String name = preferences.getString(context.getString(R.string.pref_user_username), "");
        String password = preferences.getString(context.getString(R.string.pref_user_password), "");
        return Credentials.basic(name, password);
    }
}
