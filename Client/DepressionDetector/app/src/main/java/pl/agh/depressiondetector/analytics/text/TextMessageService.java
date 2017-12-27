package pl.agh.depressiondetector.analytics.text;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import pl.agh.depressiondetector.utils.DateUtils;
import pl.agh.depressiondetector.utils.FileUtils;

public class TextMessageService extends Service {

    private final static String TAG = "TextMessageService";

    private TextMessagesObserver textMessagesObserver;

    private ContentResolver contentResolver;

    private String textMessageDate;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        contentResolver = this.getContentResolver();

        textMessagesObserver = new TextMessagesObserver(new Handler());
        contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, textMessagesObserver);

        return START_STICKY;
    }

    private class TextMessagesObserver extends ContentObserver {

        private String lastTextMessageId;

        private File outputFile;

        TextMessagesObserver(Handler handler) {
            super(handler);

            Cursor cursor = contentResolver.query(Telephony.Sms.Sent.CONTENT_URI, new String[] { "_id" }, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                lastTextMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                cursor.close();
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            String[] projection = {
                    "_id",
                    "body"
            };
            Cursor cursor = contentResolver.query(Telephony.Sms.Sent.CONTENT_URI, projection, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                if (!id.equals(lastTextMessageId)) {
                    lastTextMessageId = id;
                    String textMessage = cursor.getString(cursor.getColumnIndex("body"));
                    textMessageDate = DateUtils.convertToServerDateTimeFormat(new Date());
                    try {
                        saveTextMessage(textMessage);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }
        }

        private void saveTextMessage(String textMessage) throws IOException, JSONException {
            File outputDir = FileUtils.getTextMessagesDirectory();
            if (!FileUtils.createDirectory(outputDir))
                Log.e(TAG, "Uploading text messages: cannot create a new directory.");

            String fileName = FileUtils.getTextMessageFileName() + ".txt";
            outputFile = new File(outputDir, fileName);
            boolean fileExists = outputFile.exists();

            FileOutputStream fileOutputStream = new FileOutputStream(outputFile, true);
            if (fileExists)
                fileOutputStream.write(",".getBytes());
            fileOutputStream.write(formatTextMessage(textMessage).getBytes());
            fileOutputStream.close();
        }


        private String formatTextMessage(String textMessage) throws JSONException {
            JSONObject json = new JSONObject();
            json.put("message", textMessage);
            json.put("datetime", textMessageDate);

            return json.toString();
        }
    }

    @Override
    public void onDestroy() {
        contentResolver.unregisterContentObserver(textMessagesObserver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
