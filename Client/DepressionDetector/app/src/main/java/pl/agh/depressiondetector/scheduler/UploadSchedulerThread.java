package pl.agh.depressiondetector.scheduler;

import pl.agh.depressiondetector.analytics.AnalysedDataType;
import pl.agh.depressiondetector.analytics.UploaderFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;


final class UploadSchedulerThread extends Thread {

    private static final String TAG = "UploadSchedulerThread";

    private StateListener stateListener;
    private Context appContext;
    private boolean cancelled = false;

    UploadSchedulerThread(StateListener stateListener, Context context) {
        this.stateListener = stateListener;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void run() {
        Log.i(TAG, "Started uploading...");
        SharedPreferences preferences = getDefaultSharedPreferences(appContext);
        UploaderFactory factory = new UploaderFactory();
        for (AnalysedDataType type : AnalysedDataType.values())
            if (!cancelled)
                if (preferences.getBoolean(type.preferenceName, true))
                    factory.getUploader(type).upload(appContext);
        stateListener.onFinished();
    }

    void cancel() {
        cancelled = true;
    }

    interface StateListener {
        void onFinished();
    }
}
