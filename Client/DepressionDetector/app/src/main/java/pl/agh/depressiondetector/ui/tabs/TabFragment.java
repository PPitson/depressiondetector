package pl.agh.depressiondetector.ui.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.agh.depressiondetector.R;
import pl.agh.depressiondetector.database.AppDatabase;
import pl.agh.depressiondetector.database.entity.Result;
import pl.agh.depressiondetector.utils.DatabaseUtils;
import pl.agh.depressiondetector.utils.NetworkUtils;
import pl.agh.depressiondetector.utils.results.ResultAcquirer;
import pl.agh.depressiondetector.utils.results.plot.AllResultsInjector;
import pl.agh.depressiondetector.utils.results.plot.LastMonthResultsInjector;
import pl.agh.depressiondetector.utils.results.plot.LastWeekResultInjector;
import pl.agh.depressiondetector.utils.results.plot.LastYearResultsInjector;
import pl.agh.depressiondetector.utils.results.plot.ResultInjector;


public abstract class TabFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    String TAG;

    @BindView(R.id.results_lineChart)
    LineChart lineChart;

    @BindView(R.id.results_spinner)
    Spinner spinner;

    ResultInjector resultInjector;
    List<Result> results;
    String resultJSONField;
    String resultType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_results, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        TAG = getTAG();
        resultJSONField = getResultJSONField();
        resultType = getResultType();

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.main_chart_types, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);

        resultInjector = new LastWeekResultInjector(lineChart);

        String encodedPathSegments = getEncodedPathSegments();
        if (results == null) {
            if (NetworkUtils.isNetworkAvailable(getContext()) && encodedPathSegments != null) {
                new ResultAcquirer(TAG, getContext(), this, encodedPathSegments, resultJSONField, resultType).execute();
            } else {
                DatabaseUtils.getResults(AppDatabase.getAppDatabase(getContext()), this, resultType);
            }
        }
    }

    public void displayResults(List<Result> results) {
        this.results = results;
        resultInjector.injectResults(results);
    }

    ResultInjector getResultInjector(String type, LineChart lineChart) {
        if (type.equals(getString(R.string.main_chart_last_week)))
            return new LastWeekResultInjector(lineChart);
        if (type.equals(getString(R.string.main_chart_last_month)))
            return new LastMonthResultsInjector(lineChart);
        if (type.equals(getString(R.string.main_chart_last_year)))
            return new LastYearResultsInjector(lineChart);
        else
            return new AllResultsInjector(lineChart);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        resultInjector = getResultInjector((String) adapterView.getItemAtPosition(i), lineChart);
        if (results != null)
            resultInjector.injectResults(results);
    }

    abstract String getEncodedPathSegments();

    abstract String getResultJSONField();

    abstract String getResultType();

    abstract String getTAG();
}
