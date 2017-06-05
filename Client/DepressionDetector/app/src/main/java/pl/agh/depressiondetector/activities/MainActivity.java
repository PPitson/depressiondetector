package pl.agh.depressiondetector.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.utils.PhoneCallService;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textview_main_results)
    TextView resultsView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Intent intent = new Intent(MainActivity.this, PhoneCallService.class);
        startService(intent);
    }

    @OnClick(R.id.button_main_getresults)
    public void onGetResultsClick(View view){
        // TODO connect to server
        resultsView.setText("You were 90% happy last time");
    }
}
