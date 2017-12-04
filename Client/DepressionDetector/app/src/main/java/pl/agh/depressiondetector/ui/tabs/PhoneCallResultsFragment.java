package pl.agh.depressiondetector.ui.tabs;

import android.widget.AdapterView;

import pl.agh.depressiondetector.connection.API;

public class PhoneCallResultsFragment extends TabFragment {
    @Override
    String getEncodedPathSegments() {
        return API.PATH_VOICE_RESULTS;
    }

    @Override
    String getResultJSONField() {
        return "voice_happiness_level";
    }

    @Override
    String getTAG() {
        return "PhoneCallResFragment";
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}