package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;

public class LastMonthResultsInjector extends ResultInjector {

    public LastMonthResultsInjector(LineChart lineChart) {
        super(lineChart);
    }

    @Override
    void setDateBoundary(Calendar calendar) {
        calendar.add(Calendar.MONTH, -1);
    }

    @Override
    String getFormat() {
        return "dd.MM";
    }
}
