package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;
import java.util.List;

import pl.agh.depressiondetector.database.entity.Result;

public class AllResultsInjector extends ResultInjector {

    public AllResultsInjector(LineChart lineChart) {
        super(lineChart);
    }

    @Override
    List<Result> getFilteredResults(List<Result> results) {
        return results;
    }

    @Override
    void setDateBoundary(Calendar calendar) {

    }
}
