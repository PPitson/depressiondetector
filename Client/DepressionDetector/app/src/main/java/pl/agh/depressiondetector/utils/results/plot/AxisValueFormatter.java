package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pl.agh.depressiondetector.utils.DateUtils;


public class AxisValueFormatter implements IAxisValueFormatter {
    private List<String> dates;
    private String format;

    AxisValueFormatter(String format) {
        this.dates = new ArrayList<>();
        this.format = format;
    }

    void addDate(String date) {
        dates.add(date);
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (0 <= (int) value && (int) value < dates.size()) {
            String dateString = dates.get((int) value);
            return new SimpleDateFormat(format, Locale.getDefault())
                    .format(DateUtils.getDateFromClientDateFormat(dateString));
        }
        return "";
    }
}
