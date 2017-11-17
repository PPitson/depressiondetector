package pl.agh.depressiondetector.analytics;

import android.content.Context;

public interface Uploader {

    /**
     * Inside this method Uploader should perform final data collection or processing and upload data to server.
     *
     * @param appContext Application context
     * @return True if data was successfully uploaded to server, false otherwise
     */
    boolean upload(Context appContext);
}