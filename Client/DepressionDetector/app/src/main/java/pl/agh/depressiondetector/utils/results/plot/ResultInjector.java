package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import pl.agh.depressiondetector.database.entity.Result;
import pl.agh.depressiondetector.utils.DateUtils;

public abstract class ResultInjector {
    private static final float CIRCLE_RADIUS = 5;
    private static final float TEXT_SIZE = 10;

    private static final String LABEL = "Happiness";


    private LineChart lineChart;

    ResultInjector(LineChart lineChart) {
        this.lineChart = lineChart;
    }

    public void injectResults(List<Result> results) {
        List<Result> filteredResults = getFilteredResults(results);
        if (lineChart != null) {
            if (filteredResults != null && filteredResults.size() > 0) {
                Collections.sort(filteredResults, new Comparator<Result>() {
                    @Override
                    public int compare(Result r1, Result r2) {
                        if (DateUtils.getDateFromClientDateFormat(r1.date).before(DateUtils.getDateFromClientDateFormat(r2.date)))
                            return -1;
                        else
                            return 1;
                    }
                });
                List<Entry> entries = new ArrayList<>();

                AxisValueFormatter axisValueFormatter = new AxisValueFormatter(getFormat());
                Entry entry;
                for (Result result : filteredResults) {
                    entry = new Entry(filteredResults.indexOf(result), result.happinessLevel, result.date);
                    axisValueFormatter.addDate(result.date);
                    entries.add(entry);
                }

                LineDataSet dataSet = new LineDataSet(entries, LABEL);
                dataSet.setCircleRadius(CIRCLE_RADIUS);
                dataSet.setDrawCircleHole(false);
                dataSet.setDrawFilled(true);
                dataSet.setValueTextSize(TEXT_SIZE);

                LineData lineData = new LineData(dataSet);

                lineChart.setDescription(null);
                lineChart.setData(lineData);
                lineChart.getXAxis().setValueFormatter(axisValueFormatter);
                lineChart.getXAxis().setGranularity(1f);
                lineChart.getAxisLeft().setAxisMaximum(1f);
                lineChart.getAxisRight().setEnabled(false);
                lineChart.animateXY(1000, 1000);
            } else {
                lineChart.setData(null);
            }
            lineChart.invalidate();
        }
    }

    List<Result> getFilteredResults(List<Result> results) {
        if (results == null)
            return null;

        List<Result> filtered = new ArrayList<>();
        for (Result result : results) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            setDateBoundary(calendar);
            if (DateUtils.getDateFromClientDateFormat(result.date).after(calendar.getTime())) {
                filtered.add(result);
            }
        }

        return filtered;
    }

    abstract void setDateBoundary(Calendar calendar);
    String getFormat() {
        return "dd.MM.YY";
    }
}