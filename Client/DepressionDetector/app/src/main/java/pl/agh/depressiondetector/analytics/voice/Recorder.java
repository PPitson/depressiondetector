package pl.agh.depressiondetector.analytics.voice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import pl.agh.depressiondetector.utils.FileUtils;

public class Recorder implements OnCompleteListener<Location> {
    private MediaRecorder mediaRecorder;
    private File outputFile;

    private Context context;

    private Double[] location = {0.0, 0.0};

    Recorder(int audioSource, Context context) throws IOException {
        this.context = context;

        File outputDir = FileUtils.getTemporaryDirectory();
        FileUtils.createDirectory(outputDir);

        outputFile = new File(outputDir, FileUtils.getTemporaryVoiceFileName() + ".amr");
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(audioSource);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.prepare();
    }

    void startRecording() {
        mediaRecorder.start();
    }

    void resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            mediaRecorder.resume();
    }

    void pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            mediaRecorder.pause();
    }

    void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
    }

    void saveOutputFile() {
        stopRecording();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            saveFile();
        } else {
            LocationServices
                    .getFusedLocationProviderClient(context)
                    .getLastLocation()
                    .addOnCompleteListener(this);
        }
    }

    private void saveFile() {
        File outputDir = FileUtils.getVoiceDirectory();
        FileUtils.createDirectory(outputDir);
        FileUtils.copyFile(outputFile, new File(outputDir, FileUtils.getVoiceFileName(Arrays.toString(location)) + ".amr"));
        deleteOutputFile();
    }

    void deleteOutputFile() {
        outputFile.delete();
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
        saveFile();
    }
}
