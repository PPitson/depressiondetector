package pl.agh.depressiondetector.authentication;

import android.content.Context;
import android.support.design.widget.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import pl.agh.depressiondetector.R;

import static pl.agh.depressiondetector.utils.DateUtils.getDateFromClientDateFormat;

class Validator {

    // Source: http://howtodoinjava.com/regex/java-regex-validate-email-address/
    private static final String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    private Context context;
    private Pattern emailPattern;

    Validator(Context context) {
        this.context = context;
    }

    boolean validFieldNotEmpty(TextInputLayout field) {
        String text = field.getEditText().getText().toString().trim();
        return validFieldNotEmpty(field, text);
    }

    boolean validFieldNotEmpty(TextInputLayout field, String text) {
        if (text.isEmpty()) {
            field.setError(context.getString(R.string.error_field_empty));
            return false;
        } else {
            field.setErrorEnabled(false);
            return true;
        }
    }

    boolean validEmailField(TextInputLayout field) {
        String email = field.getEditText().getText().toString().trim();
        if (validFieldNotEmpty(field, email)) {
            if (isEmailValid(email)) {
                field.setErrorEnabled(false);
                return true;
            } else {
                field.setError(context.getString(R.string.error_email_invalid));
                return false;
            }
        }

        return false;
    }

    boolean validDateField(TextInputLayout field) {
        String text = field.getEditText().getText().toString();
        if (validFieldNotEmpty(field, text)) {

            Date date = getDateFromClientDateFormat(text);
            if (date == null)
                return false;

            if (isOlderThanNow(date)) {
                field.setErrorEnabled(false);
                return true;
            } else {
                field.setError(context.getString(R.string.error_date_from_past));
                return false;
            }
        }

        return false;
    }

    private boolean isOlderThanNow(Date date) {
        return date.getTime() < System.currentTimeMillis();
    }

    private boolean isEmailValid(String email) {
        if (emailPattern == null)
            emailPattern = Pattern.compile(emailRegex);

        return emailPattern.matcher(email).matches();
    }
}
