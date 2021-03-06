package pl.agh.depressiondetector.ui.tabs;

import pl.agh.depressiondetector.connection.API;

public class TextMessagesResultsFragment extends TabFragment {
    @Override
    String getEncodedPathSegments() {
        return API.PATH_TEXT_RESULTS;
    }

    @Override
    String getResultJSONField() {
        return "text_happiness_level";
    }

    @Override
    String getResultType() {
        return "text";
    }

    @Override
    String getTAG() {
        return "TextMessagesResFragment";
    }
}
