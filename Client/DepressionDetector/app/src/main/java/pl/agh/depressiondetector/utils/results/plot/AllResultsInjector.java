package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;

public class AllResultsInjector extends ResultInjector {

    public AllResultsInjector(LineChart lineChart, String resultsJSONField) {
        super(lineChart, resultsJSONField);
    }

    @Override
    JSONArray getJSONArray(String results) throws JSONException {
        return new JSONArray(results);
    }

    @Override
    void setDateBoundary(Calendar calendar) {

    }
}
