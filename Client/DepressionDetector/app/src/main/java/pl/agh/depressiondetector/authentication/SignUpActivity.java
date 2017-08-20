package pl.agh.depressiondetector.authentication;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.RadioButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
import pl.agh.depressiondetector.utils.NetworkUtils;
import pl.agh.depressiondetector.utils.ServicesManager;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.connection.API.*;
import static pl.agh.depressiondetector.connection.HttpClient.JSON_TYPE;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private SimpleDateFormat clientDateFormat = new SimpleDateFormat(CLIENT_DATE_FORMAT, Locale.US);

    private final Calendar calendar = Calendar.getInstance();
    private Validator validator;

    @BindView(R.id.textInputLayout_username)
    TextInputLayout usernameLayout;

    @BindView(R.id.textInputEditText_username)
    TextInputEditText usernameView;

    @BindView(R.id.textInputLayout_password)
    TextInputLayout passwordLayout;

    @BindView(R.id.textInputEditText_password)
    TextInputEditText passwordView;

    @BindView(R.id.textInputLayout_email)
    TextInputLayout emailLayout;

    @BindView(R.id.textInputEditText_email)
    TextInputEditText emailView;

    @BindView(R.id.radio_man)
    RadioButton manView;

    @BindView(R.id.textInputLayout_date_of_birth)
    TextInputLayout dateOfBirthLayout;

    @BindView(R.id.textInputEditText_date_of_birth)
    TextInputEditText dateOfBirthView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        validator = new Validator(this);
    }

    @OnClick(R.id.textInputEditText_date_of_birth)
    public void onDateOfBirthClick(View view) {
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateOfBirthView();
            }

        };

        new DatePickerDialog(this,
                onDateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateOfBirthView() {
        dateOfBirthView.setText(clientDateFormat.format(calendar.getTime()));
    }

    @OnClick(R.id.button_google_sign_up)
    public void onGoogleSignUpClick(View view) {
        ToastUtils.show(this, "Implement me");  // TODO
    }

    @OnClick(R.id.button_sign_up)
    public void onSignUpClick(View view) {
        if (validateFields()) {
            User user = new User();
            user.name = usernameView.getText().toString().trim();
            user.password = passwordView.getText().toString().trim();
            user.email = emailView.getText().toString().trim();
            user.sex = manView.isChecked();
            user.dateOfBirth = calendar.getTime();
            singUpUser(user);
        }
    }

    private boolean validateFields() {
        boolean valid = validator.validFieldNotEmpty(usernameLayout);
        valid &= validator.validFieldNotEmpty(passwordLayout);
        valid &= validator.validEmailField(emailLayout);
        valid &= validator.validDateField(dateOfBirthLayout, clientDateFormat);

        return valid;
    }

    private void singUpUser(final User user) {

        if (!NetworkUtils.isNetworkAvailable(SignUpActivity.this))
            ToastUtils.show(this, getString(R.string.error_network));
        else
            new AsyncTask<Void, Void, String>() {

                ProgressDialog dialog;

                @Override
                protected void onPreExecute() {
                    dialog = new ProgressDialog(SignUpActivity.this);
                    dialog.show();
                }

                @Override
                protected String doInBackground(Void... params) {
                    String message = UNKNOWN_ERROR;
                    try {
                        HttpUrl apiUrl = new HttpUrl.Builder()
                                .scheme("https")
                                .host(HOST)
                                .addEncodedPathSegments(PATH_REGISTER)
                                .build();

                        JSONObject json = user.toJSON();

                        Request request = new Request.Builder()
                                .url(apiUrl)
                                .post(RequestBody.create(JSON_TYPE, json.toString()))
                                .build();

                        Response response = HttpClient.getClient().newCall(request).execute();

                        ResponseBody body = response.body();
                        if (body != null) {
                            message = new JSONObject(body.string()).optString(MESSAGE, UNKNOWN_ERROR);

                            if (response.isSuccessful())
                                Log.i(TAG, "User " + user.name + " was created");
                            else
                                Log.i(TAG, "User wasn't created. Server returned: " + response.message() + " with code " + response.code());

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
                        case SIGNUP_USER_REGISTERED:
                            saveCredentials(user);
                            ServicesManager.startServices(SignUpActivity.this);
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                            break;
                        case SIGNUP_LOGIN_ALREADY_USED:
                            ToastUtils.show(SignUpActivity.this, getString(R.string.error_login_exists));
                            break;
                        case SIGNUP_EMAIL_ALREADY_USED:
                            ToastUtils.show(SignUpActivity.this, getString(R.string.error_email_exists));
                            break;
                        case TIMEOUT_ERROR:
                            ToastUtils.show(SignUpActivity.this, getString(R.string.error_timeout));
                            break;
                        case CONNECTION_ERROR:
                            ToastUtils.show(SignUpActivity.this, getString(R.string.error_connection));
                            break;
                        default:
                            ToastUtils.show(SignUpActivity.this, getString(R.string.error_unknown));
                            break;
                    }
                }
            }.execute();
    }


    private void saveCredentials(User user) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignUpActivity.this);
        preferences.edit()
                .putString(getString(R.string.pref_user_username), user.name)
                .putString(getString(R.string.pref_user_password), user.password)
                .putString(getString(R.string.pref_user_email), user.email)
                .apply();
    }
}
