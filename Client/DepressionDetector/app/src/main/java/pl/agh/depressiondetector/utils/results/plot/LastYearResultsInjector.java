package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;

public class LastYearResultsInjector extends ResultInjector {

    public LastYearResultsInjector(LineChart lineChart) {
        super(lineChart);
    }

    @Override
    void setDateBoundary(Calendar calendar) {
        calendar.add(Calendar.YEAR, -1);
    }

}
