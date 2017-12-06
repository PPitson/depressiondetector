package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;

public class LastYearResultsInjector extends ResultInjector {

    public LastYearResultsInjector(LineChart lineChart, String resultsJSONField) {
        super(lineChart, resultsJSONField);
    }

    @Override
    void setDateBoundary(Calendar calendar) {
        calendar.add(Calendar.YEAR, -1);
    }

}
