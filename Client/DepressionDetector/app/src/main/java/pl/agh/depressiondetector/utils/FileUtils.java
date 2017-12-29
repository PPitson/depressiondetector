package pl.agh.depressiondetector.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileUtils {

    private static final String TAG = "FileUtils";

    private FileUtils() {
    }

    public static File getVoiceDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/Voice");
    }

    public static File getTextDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/Text");
    }

    public static File getMoodDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/Mood");
    }

    public static File getTemporaryDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "/DepressionDetector/Temporary");
    }

    public static String getTemporaryVoiceFileName() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String getVoiceFileName(String ...params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(System.currentTimeMillis()));
        for (String param : params) {
            stringBuilder.append("_");
            stringBuilder.append(param.replaceAll("\\s+", ""));
        }
        return stringBuilder.toString();
    }

    public static String getTextFileName() {
        return "text_messages";
    }

    public static String getMoodFileName() {
        return "mood";
    }

    public static File getMoodFile() {
        File parent = getMoodDirectory();
        createDirectory(parent);

        File file = new File(parent, getMoodFileName());
        createFile(file);

        return file;
    }

    public static boolean createDirectory(File file) {
        return file.exists() || file.mkdirs();
    }

    public static boolean createFile(File file) {
        try {
            return file.exists() || file.createNewFile();
        } catch (IOException e) {
            Log.i(TAG, e.toString());
            return false;
        }
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

    public static boolean deleteFiles(File... files) {
        boolean success = false;
        for (File file : files)
            success = file.delete();
        return success;
    }
}