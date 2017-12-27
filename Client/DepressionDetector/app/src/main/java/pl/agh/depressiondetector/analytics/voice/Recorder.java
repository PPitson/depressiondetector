package pl.agh.depressiondetector.analytics.voice;

import android.media.MediaRecorder;
import android.os.Build;

import java.io.File;
import java.io.IOException;

import pl.agh.depressiondetector.utils.FileUtils;

public class Recorder {
    private MediaRecorder mediaRecorder;
    private File outputFile;

    Recorder(int audioSource) throws IOException {
        File outputDir = FileUtils.getTemporaryDirectory();
        FileUtils.createDirectory(outputDir);

        outputFile = new File(outputDir, FileUtils.getVoiceFileName() + ".amr");
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
        if (Build.VERSION.SDK_INT >= 24)
            mediaRecorder.resume();
    }

    void pauseRecording() {
        if (Build.VERSION.SDK_INT >= 24)
            mediaRecorder.pause();
    }

    void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
    }

    void saveOutputFile() {
        stopRecording();
        File outputDir = FileUtils.getVoiceDirectory();
        FileUtils.createDirectory(outputDir);
        FileUtils.copyFile(outputFile, new File(outputDir, outputFile.getName()));
    }

    void deleteOutputFile() {
        outputFile.delete();
    }
}
