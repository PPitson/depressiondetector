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
import android.widget.DatePicker;
import android.widget.RadioButton;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.model.User;
import pl.agh.depressiondetector.settings.FirstConfigurationActivity;
import pl.agh.depressiondetector.utils.NetworkUtils;
import pl.agh.depressiondetector.utils.ToastUtils;

import static pl.agh.depressiondetector.connection.API.*;
import static pl.agh.depressiondetector.utils.DateUtils.convertToClientDateFormat;

public class SignUpActivity extends AppCompatActivity {

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
    public void onDateOfBirthClick() {
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
                R.style.DatePickerDialogTheme,
                onDateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateOfBirthView() {
        dateOfBirthView.setText(convertToClientDateFormat(calendar.getTime()));
    }

    @OnClick(R.id.button_sign_up)
    public void onSignUpClick() {
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
        valid &= validator.validDateField(dateOfBirthLayout);

        return valid;
    }

    private void singUpUser(User user) {
        if (!NetworkUtils.isNetworkAvailable(this))
            ToastUtils.show(this, getString(R.string.error_network));
        else
            new SingUpTask(user).execute();
    }

    private class SingUpTask extends AsyncTask<Void, Void, RequestResult> {

        private User user;
        private ProgressDialog dialog;

        SingUpTask(User user) {
            this.user = user;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(SignUpActivity.this);
            dialog.show();
        }

        @Override
        protected RequestResult doInBackground(Void... params) {
            return Authentication.register(user);
        }

        @Override
        protected void onPostExecute(RequestResult requestResult) {
            dialog.cancel();
            switch (requestResult.message) {
                case SIGNUP_USER_REGISTERED:
                    saveCredentials(user);
                    startActivity(new Intent(SignUpActivity.this, FirstConfigurationActivity.class));
                    finishWithParent();
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
    }

    private void saveCredentials(User user) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignUpActivity.this);
        preferences.edit()
                .putString(getString(R.string.pref_user_username), user.name)
                .putString(getString(R.string.pref_user_password), user.password)
                .putString(getString(R.string.pref_user_email), user.email)
                .putBoolean(getString(R.string.pref_user_sex), user.sex)
                .putString(getString(R.string.pref_user_date_of_birth), convertToClientDateFormat(user.dateOfBirth))
                .apply();
    }

    private void finishWithParent() {
        setResult(RESULT_OK, null);
        finish();
    }
}
