package pl.agh.depressiondetector.messages_upload;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class TextMessagesService extends Service {

    private final static String TAG = "TextMessagesService";

    private TextMessagesObserver textMessagesObserver;

    private ContentResolver contentResolver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        textMessagesObserver = new TextMessagesObserver(new Handler());
        contentResolver = this.getApplicationContext().getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms/out"), true, textMessagesObserver);

        Toast.makeText(this.getApplicationContext(), "Starting TextMessagesService", Toast.LENGTH_LONG).show();

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
            super.onChange(selfChange, uri);
            Toast.makeText(getApplicationContext(), "sms in outbox", Toast.LENGTH_LONG).show();
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
