package pl.agh.depressiondetector.utils.results.plot;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pl.agh.depressiondetector.utils.DateUtils;

public abstract class ResultInjector {
    private static final float CIRCLE_RADIUS = 5;
    private static final float TEXT_SIZE = 10;

    private static final String LABEL = "Happiness";

    private static final String DATE_JSON_FIELD = "date";

    private LineChart lineChart;
    private String resultsJSONField;

    ResultInjector(LineChart lineChart, String resultsJSONField) {
        this.lineChart = lineChart;
        this.resultsJSONField = resultsJSONField;
    }

    public void injectResults(String results) {
        if (lineChart != null) {
            try {
                JSONArray jsonArray = getJSONArray(results);
                if (jsonArray.length() > 0) {
                    List<Entry> entries = new ArrayList<>();
                    JSONObject json;

                    AxisValueFormatter axisValueFormatter = new AxisValueFormatter(getFormat());
                    Entry entry;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        json = jsonArray.getJSONObject(jsonArray.length() - 1 - i);
                        entry = new Entry(i, (float) json.getDouble(resultsJSONField));
                        axisValueFormatter.addDate(json.getString(DATE_JSON_FIELD));
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
                    lineChart.invalidate();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    JSONArray getJSONArray(String results) throws JSONException {
        JSONArray jsonArray = new JSONArray(results);
        List<JSONObject> filtered = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String date = jsonObject.getString(DATE_JSON_FIELD);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            setDateBoundary(calendar);
            if (DateUtils.getDateFromClientDateFormat(date).after(calendar.getTime())) {
                filtered.add(jsonObject);
            }
        }
        return new JSONArray(filtered);
    }

    abstract void setDateBoundary(Calendar calendar);
    String getFormat() {
        return "dd.MM.YY";
    }
}