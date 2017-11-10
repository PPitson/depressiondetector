package pl.agh.depressiondetector.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class ResultAcquirer extends AsyncTask<Void, Void, String> {
    private final String TAG;
    private final Context context;
    private final ResultInjector resultInjector;
    private final String encodedPathSegments;

    public ResultAcquirer(String TAG, Context context, ResultInjector resultInjector, String encodedPathSegments) {
        this.TAG = TAG;
        this.context = context;
        this.resultInjector = resultInjector;
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
            resultInjector.injectResults(s);
    }
}
