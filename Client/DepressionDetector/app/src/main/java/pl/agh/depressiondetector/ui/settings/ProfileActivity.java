package pl.agh.depressiondetector.ui.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.authentication.Authentication;
import pl.agh.depressiondetector.authentication.AuthenticationActivity;
import pl.agh.depressiondetector.connection.API;
import pl.agh.depressiondetector.model.User;
import pl.agh.depressiondetector.utils.NetworkUtils;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.connection.API.CONNECTION_ERROR;
import static pl.agh.depressiondetector.connection.API.SENT_EMAIL;
import static pl.agh.depressiondetector.connection.API.TIMEOUT_ERROR;


public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    @BindView(R.id.username_text_view)
    TextView usernameTextView;

    @BindView(R.id.email_text_view)
    TextView emailTextView;

    @BindView(R.id.sex_text_view)
    TextView sexTextView;

    @BindView(R.id.date_of_birth_text_view)
    TextView dateOfBirthTextView;

    private User user;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        user = getUserFromPreferences();

        Map<String, TextView> textViews = new HashMap<>();
        textViews.put("username", usernameTextView);
        textViews.put("email", emailTextView);
        textViews.put("sex", sexTextView);
        textViews.put("date_of_birth", dateOfBirthTextView);
        new GetUserInfoTask(TAG, this, API.PATH_USER, textViews).execute();
    }

    private User getUserFromPreferences() {
        String name = preferences.getString(getString(R.string.pref_user_username), "");
        String password = preferences.getString(getString(R.string.pref_user_password), "");
        User user = new User();
        user.name = name;
        user.password = password;
        // TODO init remaining values
        return user;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_profile_delete:
                new DeleteTask(this, user).execute();
                break;
            case R.id.item_profile_logout:
                removeCredentialPreferences();
                restartApplication();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeCredentialPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.pref_user_username));
        editor.remove(getString(R.string.pref_user_password));
        editor.apply();
    }

    private void restartApplication() {
        finishAffinity();
        startActivity(new Intent(this, AuthenticationActivity.class));
    }

    private class GetUserInfoTask extends AsyncTask<Void, Void, String> {

        private final String TAG;
        private final Context context;
        private final String encodedPathSegments;

        private final Map<String, TextView> textViews;

        GetUserInfoTask(String TAG, Context context, String encodedPathSegments, Map<String, TextView> textViews) {
            this.TAG = TAG;
            this.context = context;
            this.encodedPathSegments = encodedPathSegments;
            this.textViews = textViews;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return NetworkUtils.get(TAG, context, encodedPathSegments);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject json = new JSONObject(s);
                Iterator jsonKeys = json.keys();

                while (jsonKeys.hasNext()) {
                    String key = (String) jsonKeys.next();
                    if (textViews.containsKey(key)) {
                        textViews.get(key).setText(json.getString(key));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DeleteTask extends AsyncTask<Void, User, String> {

        private Context context;
        private User user;
        private ProgressDialog dialog;

        DeleteTask(Context context, User user) {
            this.context = context;
            this.user = user;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ProfileActivity.this);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            return Authentication.delete(user);
        }

        @Override
        protected void onPostExecute(String message) {
            dialog.cancel();
            switch (message) {
                case SENT_EMAIL:
                    showDeleteInfoDialog();
                    break;
                case TIMEOUT_ERROR:
                    ToastUtils.show(context, getString(R.string.error_timeout));
                    break;
                case CONNECTION_ERROR:
                    ToastUtils.show(context, getString(R.string.error_connection));
                    break;
                default:
                    ToastUtils.show(context, getString(R.string.error_unknown));
                    break;
            }
        }

        private void showDeleteInfoDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.delete_successful);
            builder.setMessage(R.string.delete_message);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeCredentialPreferences();
                    restartApplication();
                }
            });
            builder.create().show();
        }
    }
}
