package pl.agh.depressiondetector.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        return String.valueOf(System.currentTimeMillis());
    }

    public static String getTextMessageFileName() {
        return "text_messages";
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

    public static boolean deleteFiles(File ... files){
        boolean success = false;
        for(File file: files)
            success = file.delete();
        return success;
    }
}