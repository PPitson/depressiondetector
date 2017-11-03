package pl.agh.depressiondetector.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class FileUtils {

    private FileUtils() {
    }

    public static File getPhoneCallsDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/PhoneCalls");
    }

    public static File getTextMessagesDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/TextMessages");
    }

    public static File getTemporaryDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/Temporary");
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

    public static boolean copyFile(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}