package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;


public class AxisValueFormatter implements IAxisValueFormatter {
    List<String> dates;

    AxisValueFormatter() {
        this.dates = new ArrayList<>();
    }

    void addDate(String date) {
        dates.add(date);
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return dates.get((int) value);
    }
}
