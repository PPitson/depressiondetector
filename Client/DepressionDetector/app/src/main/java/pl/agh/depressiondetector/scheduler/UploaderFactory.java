package pl.agh.depressiondetector.scheduler;

import pl.agh.depressiondetector.analytics.AnalysedDataType;
import pl.agh.depressiondetector.analytics.phonecalls.PhoneCallsUploader;
import pl.agh.depressiondetector.analytics.smses.TextMessageUploader;


class UploaderFactory {

    Uploader getUploader(AnalysedDataType type) {
        switch (type) {
            case PHONE_CALL:
                return new PhoneCallsUploader();
            case SMS:
                return new TextMessageUploader();
            default:
                throw new IllegalArgumentException();
        }
    }
}