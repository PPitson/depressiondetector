package pl.agh.depressiondetector.scheduler;

import pl.agh.depressiondetector.analytics.AnalysedDataType;
import pl.agh.depressiondetector.analytics.mood.MoodUploader;
import pl.agh.depressiondetector.analytics.phonecalls.PhoneCallsUploader;
import pl.agh.depressiondetector.analytics.smses.SmsUploader;


class UploaderFactory {

    Uploader getUploader(AnalysedDataType type) {
        switch (type) {
            case MOOD:
                return new MoodUploader();
            case PHONE_CALL:
                return new PhoneCallsUploader();
            case SMS:
                return new SmsUploader();
            default:
                throw new IllegalArgumentException();
        }
    }
}