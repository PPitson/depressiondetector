package pl.agh.depressiondetector.utils.results.plot;

import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;

public class LastMonthResultsInjector extends ResultInjector {

    public LastMonthResultsInjector(LineChart lineChart, Context context) {
        super(lineChart, context);
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
