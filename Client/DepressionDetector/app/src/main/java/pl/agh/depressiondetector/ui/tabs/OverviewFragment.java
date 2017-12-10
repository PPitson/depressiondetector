package pl.agh.depressiondetector.ui.tabs;

import android.widget.AdapterView;

import pl.agh.depressiondetector.connection.API;

public class OverviewFragment extends TabFragment {

    @Override
    String getEncodedPathSegments() {
        return API.PATH_MEAN_RESULTS;
    }

    @Override
    String getResultJSONField() {
        return "mean_happiness_level";
    }

    @Override
    String getResultType() {
        return "overview";
    }

    @Override
    String getTAG() {
        return "OverviewFragment";
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
