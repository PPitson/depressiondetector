package pl.agh.depressiondetector.analytics;

import pl.agh.depressiondetector.analytics.mood.MoodUploader;
import pl.agh.depressiondetector.analytics.voice.VoiceUploader;
import pl.agh.depressiondetector.analytics.text.TextUploader;


public class UploaderFactory {

    public Uploader getUploader(AnalysedDataType type) {
        switch (type) {
            case MOOD:
                return new MoodUploader();
            case PHONE_CALL:
                return new VoiceUploader();
            case SMS:
                return new TextUploader();
            default:
                throw new IllegalArgumentException();
        }
    }
}