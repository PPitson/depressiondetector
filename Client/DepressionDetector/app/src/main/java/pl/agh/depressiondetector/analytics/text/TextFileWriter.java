package pl.agh.depressiondetector.analytics.text;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.utils.DateUtils;
import pl.agh.depressiondetector.utils.FileUtils;
import pl.agh.depressiondetector.utils.ToastUtils;

public class TextFileWriter implements OnCompleteListener<Location> {
    private String text;
    private Date date;
    private Double[] location = null;

    private Context context;

    TextFileWriter(String text, Date date, Context context) {
        this.text = text;
        this.date = date;
        this.context = context;
    }

    void saveText() throws IOException, JSONException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            save();
        } else {
            LocationServices
                    .getFusedLocationProviderClient(context)
                    .getLastLocation()
                    .addOnCompleteListener(this);
        }
    }

    private void save() throws IOException, JSONException {
        File outputDir = FileUtils.getTextDirectory();
        FileUtils.createDirectory(outputDir);

        String fileName = FileUtils.getTextFileName() + ".txt";
        File outputFile = new File(outputDir, fileName);
        boolean fileExists = outputFile.exists();

        FileOutputStream fileOutputStream = new FileOutputStream(outputFile, true);
        if (fileExists)
            fileOutputStream.write(",".getBytes());

        fileOutputStream.write(formatText().getBytes());
        fileOutputStream.close();

    }

    private String formatText() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("message", text);
        json.put("datetime", DateUtils.convertToServerDateTimeFormat(date));
        json.put("location", location != null ? new JSONArray(location) : JSONObject.NULL);

        return json.toString();
    }

    private void setLocation(Double[] location) {
        this.location = location;
    }

    @Override
    public void onComplete(@NonNull Task<Location> task) {
        if (task.isSuccessful()) {
            Location currentLocation = task.getResult();
            if (currentLocation != null)
                setLocation(new Double[]{currentLocation.getLongitude(), currentLocation.getLatitude()});
        }

        try {
            save();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            ToastUtils.show(context, context.getString(R.string.unexpected_error));
        }
    }
}
