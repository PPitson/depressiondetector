package pl.agh.depressiondetector.analytics;


public enum AnalysedDataType {

    PHONE_CALL("pref_analyse_phone_calls"),
    SMS("pref_analyse_text_messages");

    public final String preferenceName;

    AnalysedDataType(String preferenceName) {
        this.preferenceName = preferenceName;
    }
}
