package pl.agh.depressiondetector.analytics.mood;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.utils.ToastUtils;

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static pl.agh.depressiondetector.utils.DateUtils.convertToServerDateFormat;
import static pl.agh.depressiondetector.utils.FileUtils.getMoodFile;

public class MoodActivity extends AppCompatActivity {

    public static final String EXTRA_TIME = "EXTRA_TIME";
    private static final String TAG = "MoodActivity";

    private static final int MOOD_AWFUL = 1;
    private static final int MOOD_BAD = 2;
    private static final int MOOD_SO_SO = 3;
    private static final int MOOD_GOOD = 4;
    private static final int MOOD_RAD = 5;
    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);
        ButterKnife.bind(this);
        long time = getIntent().getLongExtra(EXTRA_TIME, -1);
        if (time != -1)
            date = new Date(time);
        else {
            ToastUtils.show(this, getString(R.string.unexpected_error));
            finish();
        }
    }

    @OnClick(R.id.textView_mood_awful)
    public void onMoodAwfulClick(){
        saveMoodAndFinish(MOOD_AWFUL);
    }

    @OnClick(R.id.textView_mood_bad)
    public void onMoodBadClick(){
        saveMoodAndFinish(MOOD_BAD);
    }

    @OnClick(R.id.textView_mood_so_so)
    public void onMoodSoSoClick(){
        saveMoodAndFinish(MOOD_SO_SO);
    }

    @OnClick(R.id.textView_mood_good)
    public void onMoodGoodClick(){
        saveMoodAndFinish(MOOD_GOOD);
    }

    @OnClick(R.id.textView_mood_rad)
    public void onMoodRadClick(){
        saveMoodAndFinish(MOOD_RAD);
    }

    private void saveMoodAndFinish(int mood){
        saveMood(mood);
        finish();
    }

    private void saveMood(int mood) {
        try {
            JSONArray json;
            File file = getMoodFile();
            String content = IOUtils.toString(new FileInputStream(file), "UTF-8");

            if (content.isEmpty())
                json = new JSONArray();
            else
                json = new JSONArray(content);

            JSONObject today = new JSONObject();
            today.put("date", convertToServerDateFormat(date));
            today.put("mood_level", mood);
            json.put(today);

            writeStringToFile(file, json.toString());
        } catch (IOException | JSONException e) {
            Log.i(TAG, e.toString());
        }
    }
}
