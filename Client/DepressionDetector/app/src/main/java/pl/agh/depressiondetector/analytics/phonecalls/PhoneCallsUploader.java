package pl.agh.depressiondetector.analytics.phonecalls;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.agh.depressiondetector.connection.HttpClient;
import pl.agh.depressiondetector.scheduler.Uploader;

import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.API.PATH_SOUND_FILES;
import static pl.agh.depressiondetector.connection.HttpClient.AMR_TYPE;
import static pl.agh.depressiondetector.connection.HttpClient.JSON_TYPE;
import static pl.agh.depressiondetector.utils.DateUtils.convertToServerDateFormat;
import static pl.agh.depressiondetector.utils.FileUtils.deleteFiles;
import static pl.agh.depressiondetector.utils.FileUtils.getPhoneCallsDirectory;
import static pl.agh.depressiondetector.utils.NetworkUtils.getBasicCredentials;


public class PhoneCallsUploader implements Uploader {

    @Override
    public boolean upload(Context appContext) {
        boolean success = false;
        File directory = getPhoneCallsDirectory();
        File[] records = directory.listFiles();
        if (records != null && records.length > 0) {
            success = postRecordFiles(appContext, records);
            if (success)
                deleteFiles(records);
        }

        return success;
    }

    private boolean postRecordFiles(Context context, File... files) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            addRecordsMetaDataToMultipart(builder, files);
            addRecordsToMultipart(builder, files);

            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host(HOST)
                    .addEncodedPathSegments(PATH_SOUND_FILES)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", getBasicCredentials(context))
                    .post(builder.build())
                    .build();

            Response response = HttpClient.getClient().newCall(request).execute();

            Log.i("POST_RECORD_FILES", "Server returned: " + response.message() + " with code " + response.code());

            return response.isSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addRecordsMetaDataToMultipart(MultipartBody.Builder builder, File... records) throws JSONException {
        JSONObject json = new JSONObject();
        for (File file : records) {
            String name = file.getName();
            JSONObject meta = new JSONObject();
            String date = convertToServerDateFormat(Long.valueOf(name.split("\\.")[0]));
            meta.put("date", date);
            json.put(name, meta);
        }

        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"data\""),
                RequestBody.create(JSON_TYPE, json.toString()));
    }

    private void addRecordsToMultipart(MultipartBody.Builder builder, File... records) {
        for (int i = 0; i < records.length; i++)
            builder.addPart(
                    Headers.of("Content-Disposition", String.format("form-data; name=\"file%s\"; filename=\"%s\"", i, records[i].getName())),
                    RequestBody.create(AMR_TYPE, records[i]));
    }
}
