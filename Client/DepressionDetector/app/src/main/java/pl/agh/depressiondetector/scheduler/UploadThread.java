package pl.agh.depressiondetector.scheduler;

import pl.agh.depressiondetector.analytics.AnalysedDataType;

import android.content.Context;
import android.content.SharedPreferences;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;


public class UploadThread extends Thread {

    private StateListener stateListener;
    private Context appContext;
    private boolean cancelled = false;

    UploadThread(StateListener stateListener, Context context) {
        this.stateListener = stateListener;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void run() {
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
