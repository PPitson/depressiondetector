package pl.agh.depressiondetector.utils.results.plot;

import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;

public class LastYearResultsInjector extends ResultInjector {

    public LastYearResultsInjector(LineChart lineChart, Context context) {
        super(lineChart, context);
    }

    @Override
    void setDateBoundary(Calendar calendar) {
        calendar.add(Calendar.YEAR, -1);
    }

}
