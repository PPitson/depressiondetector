package pl.agh.depressiondetector.utils.results;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import pl.agh.depressiondetector.ui.tabs.TabFragment;
import pl.agh.depressiondetector.utils.NetworkUtils;

public class ResultAcquirer extends AsyncTask<Void, Void, String> {
    private final String TAG;
    private final Context context;
    private final TabFragment tabFragment;
    private final String encodedPathSegments;

    public ResultAcquirer(String TAG, Context context, TabFragment tabFragment, String encodedPathSegments) {
        this.TAG = TAG;
        this.context = context;
        this.tabFragment = tabFragment;
        this.encodedPathSegments = encodedPathSegments;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return NetworkUtils.getResults(TAG, context, encodedPathSegments);
    }

    @Override
    protected void onPostExecute(String s) {
        Log.i(TAG, "Results are: " + s);
        if (s != null)
            tabFragment.displayResults(s);
    }
}
