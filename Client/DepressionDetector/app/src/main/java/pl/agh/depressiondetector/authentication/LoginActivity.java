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
import android.widget.CheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.MainActivity;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.model.User;
import pl.agh.depressiondetector.utils.NetworkUtils;
import pl.agh.depressiondetector.utils.ServicesManager;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.connection.API.*;

public class LoginActivity extends AppCompatActivity {

    private Validator validator;

    @BindView(R.id.textInputLayout_username)
    TextInputLayout usernameLayout;

    @BindView(R.id.textInputEditText_username)
    TextInputEditText usernameView;

    @BindView(R.id.textInputLayout_password)
    TextInputLayout passwordLayout;

    @BindView(R.id.textInputEditText_password)
    TextInputEditText passwordView;

    @BindView(R.id.checkbox_keep_logged_in)
    CheckBox keepLoggedInView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        validator = new Validator(this);
    }

    @OnClick(R.id.button_login)
    public void onLoginClick(View view) {
        if (validateFields()) {
            User user = new User();
            user.name = usernameView.getText().toString().trim();
            user.password = passwordView.getText().toString().trim();
            singInUser(user);
        }
    }

    private boolean validateFields() {
        boolean valid = validator.validFieldNotEmpty(usernameLayout);
        valid &= validator.validFieldNotEmpty(passwordLayout);

        return valid;
    }

    private void singInUser(User user) {
        if (!NetworkUtils.isNetworkAvailable(this))
            ToastUtils.show(this, getString(R.string.error_network));
        else
            new LoginTask(user).execute();
    }

    private class LoginTask extends AsyncTask<Void, User, String> {

        private User user;
        private ProgressDialog dialog;

        LoginTask(User user) {
            this.user = user;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            return Authentication.login(user);
        }

        @Override
        protected void onPostExecute(String message) {
            dialog.cancel();
            Context context = LoginActivity.this;
            switch (message) {
                case LOGIN_USER_LOGGED_IN:
                    if (keepLoggedInView.isChecked())
                        saveCredentials(user);
                    ServicesManager.startServices(context);
                    startActivity(new Intent(context, MainActivity.class));
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

    private void saveCredentials(User user) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        preferences.edit()
                .putString(getString(R.string.pref_user_username), user.name)
                .putString(getString(R.string.pref_user_password), user.password)
                .apply();
    }

    private void finishWithParent(){
        setResult(RESULT_OK, null);
        finish();
    }
}
