package pl.agh.depressiondetector.authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.MainActivity;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.analytics.AnalyticsAdapter;
import pl.agh.depressiondetector.model.User;
import pl.agh.depressiondetector.scheduler.UploadScheduler;
import pl.agh.depressiondetector.settings.FirstConfigurationActivity;
import pl.agh.depressiondetector.utils.NetworkUtils;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.connection.API.*;

public class LoginActivity extends AppCompatActivity {

    private User user;
    private Validator validator;

    @BindView(R.id.textInputLayout_email)
    TextInputLayout emailLayout;

    @BindView(R.id.textInputEditText_email)
    TextInputEditText emailView;

    @BindView(R.id.textInputLayout_password)
    TextInputLayout passwordLayout;

    @BindView(R.id.textInputEditText_password)
    TextInputEditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        user = new User();
        validator = new Validator(this);
    }

    @OnClick(R.id.button_login)
    public void onLoginClick() {
        if (validateFields()) {
            user.email = emailView.getText().toString().trim();
            user.password = passwordView.getText().toString().trim();
            singInUser();
        }
    }

    private boolean validateFields() {
        boolean valid = validator.validEmailField(emailLayout);
        valid &= validator.validFieldNotEmpty(passwordLayout);

        return valid;
    }

    private void singInUser() {
        if (!NetworkUtils.isNetworkAvailable(this))
            ToastUtils.show(this, getString(R.string.error_network));
        else
            new LoginTask().execute();
    }

    private class LoginTask extends AsyncTask<Void, Void, RequestResult> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.show();
        }

        @Override
        protected RequestResult doInBackground(Void... params) {
            return Authentication.login(user);
        }

        @Override
        protected void onPostExecute(RequestResult requestResult) {
            dialog.cancel();
            Context context = LoginActivity.this;
            switch (requestResult.message) {
                case LOGIN_USER_LOGGED_IN:
                    saveCredentials(requestResult.json);
                    startActivity(new Intent(context, FirstConfigurationActivity.class));
                    finishWithParent();
                    break;
                case LOGIN_LOGIN_DOES_NOT_EXIST:
                    ToastUtils.show(context, getString(R.string.error_login_does_not_exist));
                    break;
                case LOGIN_PASSWORD_INVALID:
                    ToastUtils.show(context, getString(R.string.error_password_invalid));
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
    }

    private void saveCredentials(JSONObject jsonUser) {
        try {
            String name = jsonUser.getString(USERNAME);
            boolean sex = jsonUser.getString(SEX).equals(SEX_MALE);
            String dateOfBirth = jsonUser.getString(DATE_OF_BIRTH);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit()
                    .putString(getString(R.string.pref_user_username), name)
                    .putString(getString(R.string.pref_user_email), user.email)
                    .putString(getString(R.string.pref_user_password), user.password)
                    .putBoolean(getString(R.string.pref_user_sex), sex)
                    .putString(getString(R.string.pref_user_date_of_birth), dateOfBirth)
                    .apply();
        } catch (JSONException e) {
            ToastUtils.show(this, getString(R.string.error_internal));
        }
    }

    private void finishWithParent() {
        setResult(RESULT_OK, null);
        finish();
    }
}
