package pl.agh.depressiondetector.analytics.phonecalls;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.connection.HttpClient;

import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.API.PATH_SOUND_FILES;
import static pl.agh.depressiondetector.connection.HttpClient.AMR_TYPE;


class PostAudioTask extends AsyncTask<File, Void, Void> {

    private static final String TAG = "PostAudioTask";

    private Context context;

    PostAudioTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(File... files) {
        // TODO Add buffor or something, to wait for internet connection
        uploadSoundFile(files[0]);
        return null;
    }

    private boolean uploadSoundFile(File file) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String name = preferences.getString(context.getString(R.string.pref_user_username), "");
            String password = preferences.getString(context.getString(R.string.pref_user_password), "");

            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host(HOST)
                    .addEncodedPathSegments(PATH_SOUND_FILES)
                    .build();

            RequestBody requestBody = RequestBody.create(AMR_TYPE, file);

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(name, password))
                    .post(requestBody)
                    .build();

            Response response = HttpClient.getClient().newCall(request).execute();
            Log.i(TAG, "Server returned: " + response.message() + " with code " + response.code());
            return response.isSuccessful();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}