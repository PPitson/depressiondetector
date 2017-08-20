package pl.agh.depressiondetector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.agh.depressiondetector.connection.HttpClient;
import pl.agh.depressiondetector.settings.ProfileActivity;
import pl.agh.depressiondetector.settings.SettingsActivity;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.API.PATH_RESULTS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.textview_main_results)
    TextView resultsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_main_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.item_main_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_main_getresults)
    public void onGetResultsClick() {
        // TODO Replace somewhere, add showing all results
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return getLatestResult();
            }

            @Override
            protected void onPostExecute(String result) {
                Log.i(TAG, "Results are: " + result);
                if (resultsView != null) {
                    if (result.contains("happy")) {
                        int i = result.indexOf("happy");
                        resultsView.setText("You were " + result.substring(i + 9, i + 14) + " happy last time");
                    } else
                        ToastUtils.show(MainActivity.this, "Empty results!");
                }
            }
        }.execute();
    }

    private String getLatestResult() {
        String result = null;
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String login = preferences.getString(getString(R.string.pref_user_username), "");
            String password = preferences.getString(getString(R.string.pref_user_password), "");

            String credential = Credentials.basic(login, password);

            HttpUrl apiUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(HOST)
                    .addEncodedPathSegments(PATH_RESULTS)
                    .build();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", credential)
                    .get()
                    .build();

            Response response = HttpClient.getClient().newCall(request).execute();
            ResponseBody body = response.body();
            Log.i(TAG, "Server returned: " + response.message() + " with code " + response.code());
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
