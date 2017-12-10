package pl.agh.depressiondetector.ui.tabs;

import android.widget.AdapterView;

import pl.agh.depressiondetector.connection.API;

public class MoodResultsFragment extends TabFragment {

    @Override
    String getEncodedPathSegments() {
        return API.PATH_MODD_RESULTS;
    }

    @Override
    String getResultJSONField() {
        return "mood_happiness_level";
    }

    @Override
    String getResultType() {
        return "mood";
    }

    @Override
    String getTAG() {
        return "MoodResultsFragment";
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
