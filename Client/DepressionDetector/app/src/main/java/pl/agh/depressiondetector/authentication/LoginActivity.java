package pl.agh.depressiondetector.authentication;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.BindView;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;

public class LoginActivity extends AppCompatActivity {


    @BindView(R.id.textInputEditText_login)
    TextInputEditText login;

    @BindView(R.id.textInputEditText_password)
    TextInputEditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @OnClick(R.id.button_login)
    public void onLoginClick(View view) {

    }
}
