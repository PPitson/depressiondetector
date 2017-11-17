package pl.agh.depressiondetector.analytics.mood;

import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import pl.agh.depressiondetector.analytics.Uploader;

import static pl.agh.depressiondetector.connection.API.PATH_MOODS;
import static pl.agh.depressiondetector.utils.FileUtils.deleteFiles;
import static pl.agh.depressiondetector.utils.FileUtils.getMoodFile;
import static pl.agh.depressiondetector.utils.NetworkUtils.postJSON;


public class MoodUploader implements Uploader {
    @Override
    public boolean upload(Context appContext) {
        try {
            // TODO Resolve possible race condition
            File file = getMoodFile();
            String content = IOUtils.toString(new FileInputStream(file), "UTF-8");
            JSONArray json;
            if (content.isEmpty())
                json = new JSONArray();
            else
                json = new JSONArray(content);
            postJSON(json, appContext, PATH_MOODS);
            deleteFiles(file);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
