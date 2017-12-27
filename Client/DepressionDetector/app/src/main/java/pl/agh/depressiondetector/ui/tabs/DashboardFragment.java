package pl.agh.depressiondetector.ui.tabs;

import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.connection.API;

public class DashboardFragment extends TabFragment {

    @Override
    int getResource() {
        return R.layout.fragment_dashboard;
    }

    @Override
    int getSwipeRefreshLayoutId() {
        return R.id.swipe_update_results_dashboard;
    }

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
        return "DashboardFragment";
    }
}
