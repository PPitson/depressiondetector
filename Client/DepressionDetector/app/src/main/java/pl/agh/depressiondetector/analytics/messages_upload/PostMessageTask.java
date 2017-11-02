package pl.agh.depressiondetector.analytics.messages_upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.connection.HttpClient;

import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.API.PATH_TEXT_MESSAGES;
import static pl.agh.depressiondetector.connection.HttpClient.JSON_TYPE;

class PostMessageTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "PostMessageTask";

    private Context context;

    PostMessageTask(Context context) {
        this.context = context;
    }

    // TODO: Change string to file if needed

    @Override
    protected Void doInBackground(String... strings) {
        uploadTextMessage(strings[0]);
        return null;
    }

    private boolean uploadTextMessage(String string) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String name = preferences.getString(context.getString(R.string.pref_user_username), "");
            String password = preferences.getString(context.getString(R.string.pref_user_password), "");

            JSONObject json = new JSONObject();
            json.put("message", string);

            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host(HOST)
                    .addEncodedPathSegments(PATH_TEXT_MESSAGES)
                    .build();

            RequestBody requestBody = RequestBody.create(JSON_TYPE, json.toString());

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(name, password))
                    .post(requestBody)
                    .build();

            Response response = HttpClient.getClient().newCall(request).execute();
            Log.i(TAG, "Server returned: " + response.message() + " with code " + response.code());
            return response.isSuccessful();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
