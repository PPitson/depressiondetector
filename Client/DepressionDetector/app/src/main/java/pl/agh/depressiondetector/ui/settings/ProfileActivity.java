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
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.authentication.Authentication;
import pl.agh.depressiondetector.authentication.AuthenticationActivity;
import pl.agh.depressiondetector.model.User;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.connection.API.CONNECTION_ERROR;
import static pl.agh.depressiondetector.connection.API.SENT_EMAIL;
import static pl.agh.depressiondetector.connection.API.TIMEOUT_ERROR;
import static pl.agh.depressiondetector.utils.DateUtils.convertToHumanFriendlyFormat;
import static pl.agh.depressiondetector.utils.DateUtils.getDateFromClientDateFormat;


public class ProfileActivity extends AppCompatActivity {

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

        setupUserViews();
    }

    private User getUserFromPreferences() {
        User user = new User();
        user.name = preferences.getString(getString(R.string.pref_user_username), "");
        user.password = preferences.getString(getString(R.string.pref_user_password), "");
        user.email = preferences.getString(getString(R.string.pref_user_email), "");
        user.sex = preferences.getBoolean(getString(R.string.pref_user_sex), true);
        user.dateOfBirth = getDateFromClientDateFormat(preferences.getString(getString(R.string.pref_user_date_of_birth), ""));
        return user;
    }

    private void setupUserViews(){
        usernameTextView.setText(user.name);
        emailTextView.setText(user.email);
        sexTextView.setText(user.sex ? "Male" : "Female");
        dateOfBirthTextView.setText(convertToHumanFriendlyFormat(user.dateOfBirth));
    }

    @OnClick(R.id.delete_button)
    public void onDeleteClick() {
        new DeleteTask(this, user).execute();
    }

    @OnClick(R.id.logout_button)
    public void onLogoutClick() {
        removeCredentialPreferences();
        restartApplication();
    }

    private void removeCredentialPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.pref_user_email));
        editor.remove(getString(R.string.pref_user_password));
        editor.apply();
    }

    private void restartApplication() {
        finishAffinity();
        startActivity(new Intent(this, AuthenticationActivity.class));
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
