package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;

public class LastMonthResultsInjector extends ResultInjector {

    public LastMonthResultsInjector(LineChart lineChart, String resultsJSONField) {
        super(lineChart, resultsJSONField);
    }

    @Override
    void setDateBoundary(Calendar calendar) {
        calendar.add(Calendar.MONTH, -1);
    }
}
