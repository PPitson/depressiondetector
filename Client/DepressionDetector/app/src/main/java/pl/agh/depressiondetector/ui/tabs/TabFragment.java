package pl.agh.depressiondetector.ui.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.Unbinder;
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


public abstract class TabFragment extends Fragment {
    String TAG;

    @BindView(R.id.results_lineChart)
    LineChart lineChart;

    @BindView(R.id.results_spinner)
    Spinner spinner;

    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.offline_text_view)
    TextView offlineTextView;

    private Unbinder unbinder;
    ViewPager viewPager;

    ResultInjector resultInjector;
    List<Result> results;
    String resultJSONField;
    String resultType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getResource(), container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TAG = getTAG();
        resultJSONField = getResultJSONField();
        resultType = getResultType();

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.main_chart_types, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        swipeRefreshLayout = ButterKnife.findById(view, getSwipeRefreshLayoutId());
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener());

        lineChart.setOnChartGestureListener(new OnLineChartGestureListener());

        resultInjector = new LastWeekResultInjector(lineChart, getContext());

        if (results == null) {
            swipeRefreshLayout.setRefreshing(true);
            updateResults();
        }
    }

    public void displayResults(List<Result> results) {
        this.results = results;
        resultInjector.injectResults(results);
        swipeRefreshLayout.setRefreshing(false);
    }

    ResultInjector getResultInjector(String type, LineChart lineChart) {
        if (type.equals(getString(R.string.main_chart_last_week)))
            return new LastWeekResultInjector(lineChart, getContext());
        if (type.equals(getString(R.string.main_chart_last_month)))
            return new LastMonthResultsInjector(lineChart, getContext());
        if (type.equals(getString(R.string.main_chart_last_year)))
            return new LastYearResultsInjector(lineChart, getContext());
        else
            return new AllResultsInjector(lineChart, getContext());
    }

    private void updateResults() {
        String encodedPathSegments = getEncodedPathSegments();
        if (NetworkUtils.isNetworkAvailable(getContext()) && encodedPathSegments != null) {
            new ResultAcquirer(TAG, getContext(), this, encodedPathSegments, resultJSONField, resultType).execute();
            offlineTextView.setText("");
        } else {
            DatabaseUtils.getResults(AppDatabase.getAppDatabase(getContext()), this, resultType);
            setOfflineModeText();
        }
    }

    @OnItemSelected(R.id.results_spinner)
    public void onSpinnerItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        resultInjector = getResultInjector((String) adapterView.getItemAtPosition(i), lineChart);
        if (results != null)
            resultInjector.injectResults(results);
    }

    int getResource() {
        return R.layout.fragment_results;
    }

    int getSwipeRefreshLayoutId() {
        return R.id.swipe_update_results;
    }

    public void setOfflineModeText() {
        offlineTextView.setText(R.string.offline_mode);
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    abstract String getEncodedPathSegments();

    abstract String getResultJSONField();

    abstract String getResultType();

    abstract String getTAG();

    private class OnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {
            updateResults();
        }
    }

    private class OnLineChartGestureListener implements OnChartGestureListener {

        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            swipeRefreshLayout.setEnabled(true);
            if (viewPager != null)
                viewPager.setEnabled(true);
        }

        @Override
        public void onChartLongPressed(MotionEvent me) {

        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
            swipeRefreshLayout.setEnabled(false);
            if (viewPager != null)
                viewPager.setEnabled(false);
        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {
            swipeRefreshLayout.setEnabled(false);
            if (viewPager != null)
                viewPager.setEnabled(false);
        }
    }
}
