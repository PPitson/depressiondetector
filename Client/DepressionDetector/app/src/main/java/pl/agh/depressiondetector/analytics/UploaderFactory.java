package pl.agh.depressiondetector.analytics;

import pl.agh.depressiondetector.analytics.mood.MoodUploader;
import pl.agh.depressiondetector.analytics.phonecalls.PhoneCallsUploader;
import pl.agh.depressiondetector.analytics.smses.TextMessageUploader;


public class UploaderFactory {

    public Uploader getUploader(AnalysedDataType type) {
        switch (type) {
            case MOOD:
                return new MoodUploader();
            case PHONE_CALL:
                return new PhoneCallsUploader();
            case SMS:
                return new TextMessageUploader();
            default:
                throw new IllegalArgumentException();
        }
    }
}