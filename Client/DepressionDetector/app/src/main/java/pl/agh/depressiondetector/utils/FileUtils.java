package pl.agh.depressiondetector.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {

    private FileUtils() {
    }

    public static File getAudioDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/Audio");
    }

    public static File getTextMessagesDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/TextMessages");
    }

    public static boolean createDirectory(File file) {
        return file.exists() || file.mkdirs();
    }

    public static String getPhoneCallFileName() {
        return "phone_call" + getDateString();
    }

    public static String getTextMessageFileName() {
        return "text_message" + getDateString() + ".txt";
    }

    private static String getDateString() {
        return new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault()).format(new Date());
    }
}
