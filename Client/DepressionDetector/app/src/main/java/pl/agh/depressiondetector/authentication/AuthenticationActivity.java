package pl.agh.depressiondetector.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import pl.agh.depressiondetector.MainActivity;
import pl.agh.depressiondetector.R;

public class AuthenticationActivity extends AppCompatActivity {

    private static final int EXIT_AUTHENTICATION_ACTIVITY = 2309;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.contains(getString(R.string.pref_user_username)) && preferences.contains(getString(R.string.pref_user_password)))
            startActivity(new Intent(this, MainActivity.class));
        else
            setContentView(R.layout.activity_authentication);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EXIT_AUTHENTICATION_ACTIVITY:
                if (resultCode == RESULT_OK)
                    finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onSignUpClick(View view) {
        startActivityForResult(new Intent(this, SignUpActivity.class), EXIT_AUTHENTICATION_ACTIVITY);
    }

    public void onLoginClick(View view) {
        startActivityForResult(new Intent(this, LoginActivity.class), EXIT_AUTHENTICATION_ACTIVITY);
    }
}
