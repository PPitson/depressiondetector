package pl.agh.depressiondetector.analytics.smses;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import pl.agh.depressiondetector.scheduler.Uploader;

import static pl.agh.depressiondetector.connection.API.PATH_TEXT_MESSAGES;
import static pl.agh.depressiondetector.utils.FileUtils.getTextMessageFileName;
import static pl.agh.depressiondetector.utils.FileUtils.getTextMessagesDirectory;
import static pl.agh.depressiondetector.utils.NetworkUtils.postJSONArray;



public class TextMessageUploader implements Uploader {
    @Override
    public boolean upload(Context appContext) {
        boolean success = false;
        File directory = getTextMessagesDirectory();
        if (directory.exists()) {
            File file = new File(directory, getTextMessageFileName() + ".txt");
            try {
                if (postTxtFile(file, appContext))
                    success = file.delete();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    private boolean postTxtFile(File file, Context context) throws IOException, JSONException {
        FileInputStream fileInputStream = new FileInputStream(file);
        String jsonArrayString = readFileInputStream(fileInputStream);
        fileInputStream.close();
        JSONArray jsonArray = new JSONArray(jsonArrayString);
        return postJSONArray(jsonArray, context, PATH_TEXT_MESSAGES);
    }

    private String readFileInputStream(FileInputStream fileInputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        String line = bufferedReader.readLine();

        stringBuilder.append("[");
        stringBuilder.append(line);
        stringBuilder.append("]");

        bufferedReader.close();
        return stringBuilder.toString();
    }
}
