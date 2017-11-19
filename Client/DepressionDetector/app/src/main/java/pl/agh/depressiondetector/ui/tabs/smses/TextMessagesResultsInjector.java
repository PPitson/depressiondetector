package pl.agh.depressiondetector.ui.tabs.smses;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pl.agh.depressiondetector.utils.ResultInjector;


public class TextMessagesResultsInjector implements ResultInjector {
    private GraphView graphView;

    TextMessagesResultsInjector(GraphView graphView) {
        this.graphView = graphView;
    }

    @Override
    public void injectResults(String results) {
        if (graphView != null) {
            try {
                JSONArray jsonArray = new JSONArray(results);
                DataPoint[] dataPoints = new DataPoint[jsonArray.length()];
                JSONObject json;

                for (int i = 0; i < jsonArray.length(); i++) {
                    json = jsonArray.getJSONObject(i);
                    dataPoints[i] = new DataPoint(parseDate(json.getString("date")), Double.valueOf(json.getString("text_happiness_level")));
                }

                LineGraphSeries<DataPoint> lineGraphSeries = new LineGraphSeries<>(dataPoints);
                graphView.addSeries(lineGraphSeries);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private Date parseDate(String date) throws ParseException {
        return new SimpleDateFormat("dd-mm-yyyy", Locale.getDefault()).parse(date);
    }
}
