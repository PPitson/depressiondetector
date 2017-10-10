package pl.agh.depressiondetector.messages_upload;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
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
        textMessagesObserver = new TextMessagesObserver(new Handler());
        contentResolver = this.getContentResolver();
        contentResolver.registerContentObserver(Telephony.Sms.Sent.CONTENT_URI, true, textMessagesObserver);

        return START_STICKY;
    }

    private class TextMessagesObserver extends ContentObserver {

        TextMessagesObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.i(TAG, "SMS sent");
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
