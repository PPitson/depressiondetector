package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;

public class LastWeekResultInjector extends ResultInjector {

    public LastWeekResultInjector(LineChart lineChart, String resultsJSONField) {
        super(lineChart, resultsJSONField);
    }

    @Override
    void setDateBoundary(Calendar calendar) {
        int dayOfTheWeek = calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DATE, dayOfTheWeek - 7);
    }
}
