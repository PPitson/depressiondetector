package pl.agh.depressiondetector.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.connection.HttpClient;
import pl.agh.depressiondetector.recording.PhoneCallService;

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
    public void onGetResultsClick(View view) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return HttpClient.getInstance().getLatestResult();
            }

            @Override
            protected void onPostExecute(String s) {
                System.out.println(s);
                if (resultsView != null) {
                    int i = s.indexOf("happy");
                    resultsView.setText("You were " + s.substring(i + 9, i + 14) + " happy last time");    // TODO JSON Parsing
                }
            }
        }.execute();
    }
}
