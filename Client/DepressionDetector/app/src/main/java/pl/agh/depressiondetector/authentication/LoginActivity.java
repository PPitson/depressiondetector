package pl.agh.depressiondetector.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.agh.depressiondetector.MainActivity;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.connection.HttpClient;
import pl.agh.depressiondetector.model.User;
import pl.agh.depressiondetector.utils.ServicesManager;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.connection.API.*;
import static pl.agh.depressiondetector.connection.HttpClient.JSON_TYPE;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
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

    private void singInUser(final User user) {
        new AsyncTask<Void, Void, String>() {

            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(LoginActivity.this);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                String message = UNKNOWN_ERROR;
                try {
                    HttpUrl url = new HttpUrl.Builder()
                            .scheme("https")
                            .host(HOST)
                            .addEncodedPathSegments(PATH_LOGIN)
                            .build();

                    JSONObject json = new JSONObject();
                    json.put(USERNAME, user.name);
                    json.put(PASSWORD, user.password);

                    Request request = new Request.Builder()
                            .url(url)
                            .post(RequestBody.create(JSON_TYPE, json.toString()))
                            .build();

                    Response response = HttpClient.getClient().newCall(request).execute();

                    ResponseBody body = response.body();
                    if (body != null) {
                        message = new JSONObject(body.string()).optString(MESSAGE, UNKNOWN_ERROR);

                        if (response.isSuccessful())
                            Log.i(TAG, "User " + user.name + " successfully logged in");
                        else
                            Log.i(TAG, "User wasn't logged in. Server returned: " + response.message() + " with code " + response.code());

                        body.close();
                    }
                } catch(SocketTimeoutException e){
                    message = TIMEOUT_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    message = CONNECTION_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    message = UNKNOWN_ERROR;
                    e.printStackTrace();
                }
                return message;
            }

            @Override
            protected void onPostExecute(String message) {
                dialog.cancel();
                switch (message) {
                    case LOGIN_USER_LOGGED_IN:
                        if (keepLoggedInView.isChecked())
                            saveCredentials(user);
                        ServicesManager.startServices(LoginActivity.this);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                        break;
                    case LOGIN_LOGIN_DOES_NOT_EXIST:
                        ToastUtils.show(LoginActivity.this, getString(R.string.error_login_does_not_exist));
                        break;
                    case LOGIN_PASSWORD_INVALID:
                        ToastUtils.show(LoginActivity.this, getString(R.string.error_password_invalid));
                        break;
                    case TIMEOUT_ERROR:
                        ToastUtils.show(LoginActivity.this, getString(R.string.error_timeout));
                        break;
                    case CONNECTION_ERROR:
                        ToastUtils.show(LoginActivity.this, getString(R.string.error_connection));
                        break;
                    default:
                        ToastUtils.show(LoginActivity.this, getString(R.string.error_unknown));
                        break;
                }
            }
        }.execute();
    }

    private void saveCredentials(User user) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        preferences.edit()
                .putString(getString(R.string.pref_user_username), user.name)
                .putString(getString(R.string.pref_user_password), user.password)
                .apply();
    }
}
