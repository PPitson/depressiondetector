package pl.agh.depressiondetector.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {

    public static File getAudioDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/Audio");
    }

    public static boolean createDirectory(File file) {
        return file.exists() || file.mkdirs();
    }

    public static String getPhoneCallFileName(){
        String dateString = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault()).format(new Date());
        return "phone_call" + dateString;
    }
}
