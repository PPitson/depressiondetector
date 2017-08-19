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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.contains(getString(R.string.pref_user_name)))
            startActivity(new Intent(this, MainActivity.class));
        else
            setContentView(R.layout.activity_authentication);
    }

    public void onSignUpClick(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
