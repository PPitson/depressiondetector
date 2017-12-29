package pl.agh.depressiondetector.analytics.mood;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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

public class MoodActivity extends AppCompatActivity implements OnCompleteListener<Location> {

    public static final String EXTRA_TIME = "EXTRA_TIME";
    private static final String TAG = "MoodActivity";

    private static final int MOOD_AWFUL = 1;
    private static final int MOOD_BAD = 2;
    private static final int MOOD_SO_SO = 3;
    private static final int MOOD_GOOD = 4;
    private static final int MOOD_RAD = 5;
    private Date date;

    private int mood;
    private Double[] location = new Double[]{0.0, 0.0};

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
        setMood(MOOD_AWFUL);
        saveMoodAndFinish();
    }

    @OnClick(R.id.textView_mood_bad)
    public void onMoodBadClick(){
        setMood(MOOD_BAD);
        saveMoodAndFinish();
    }

    @OnClick(R.id.textView_mood_so_so)
    public void onMoodSoSoClick(){
        setMood(MOOD_SO_SO);
        saveMoodAndFinish();
    }

    @OnClick(R.id.textView_mood_good)
    public void onMoodGoodClick(){
        setMood(MOOD_GOOD);
        saveMoodAndFinish();
    }

    @OnClick(R.id.textView_mood_rad)
    public void onMoodRadClick(){
        setMood(MOOD_RAD);
        saveMoodAndFinish();
    }

    public void setMood(int mood) {
        this.mood = mood;
    }

    public void setLocation(Double[] location) {
        this.location = location;
    }

    private void saveMoodAndFinish(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            saveMood();
            finish();
        } else {
            LocationServices
                    .getFusedLocationProviderClient(this)
                    .getLastLocation()
                    .addOnCompleteListener(this);
        }
    }

    private void saveMood() {
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
            today.put("location", new JSONArray(location));
            json.put(today);

            writeStringToFile(file, json.toString());
        } catch (IOException | JSONException e) {
            Log.i(TAG, e.toString());
        }
    }

    @Override
    public void onComplete(@NonNull Task<Location> task) {
        if (task.isSuccessful()) {
            Location currentLocation = task.getResult();
            if (currentLocation != null)
                setLocation(new Double[]{currentLocation.getLongitude(), currentLocation.getLatitude()});
        }
        saveMood();
        finish();
    }
}
