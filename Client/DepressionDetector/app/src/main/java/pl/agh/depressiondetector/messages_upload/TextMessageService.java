package pl.agh.depressiondetector.messages_upload;

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

public class TextMessageService extends Service {

    private final static String TAG = "TextMessageService";

    private TextMessagesObserver textMessagesObserver;

    private ContentResolver contentResolver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        contentResolver = this.getContentResolver();

        textMessagesObserver = new TextMessagesObserver(new Handler());
        contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, textMessagesObserver);

        return START_STICKY;
    }

    private class TextMessagesObserver extends ContentObserver {

        private String lastTextMessageId;

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
                    //TODO: send textMessage to the server or save it on the device's memory to process later
                }
                cursor.close();
            }
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
