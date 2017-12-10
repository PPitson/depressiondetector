package pl.agh.depressiondetector.utils.results;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.agh.depressiondetector.database.AppDatabase;
import pl.agh.depressiondetector.database.entity.Result;
import pl.agh.depressiondetector.ui.tabs.TabFragment;
import pl.agh.depressiondetector.utils.DatabaseUtils;
import pl.agh.depressiondetector.utils.NetworkUtils;

public class ResultAcquirer extends AsyncTask<Void, Void, String> {
    private final String TAG;
    private final Context context;
    private final TabFragment tabFragment;
    private final String encodedPathSegments;
    private final String resultJSONField;
    private final String resultsType;
    private final String DATE_JSON_FIELD = "date";

    public ResultAcquirer(String TAG, Context context, TabFragment tabFragment,
                          String encodedPathSegments, String resultJSONField, String resultsType) {
        this.TAG = TAG;
        this.context = context;
        this.tabFragment = tabFragment;
        this.encodedPathSegments = encodedPathSegments;
        this.resultJSONField = resultJSONField;
        this.resultsType = resultsType;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return NetworkUtils.get(TAG, context, encodedPathSegments);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        Log.i(TAG, "Results are: " + s);
        AppDatabase appDatabase = AppDatabase.getAppDatabase(context);
        if (s != null)
            DatabaseUtils.insertResults(appDatabase, tabFragment, s, resultsType, resultJSONField, DATE_JSON_FIELD);
        else {
            tabFragment.setOfflineModeText();
            DatabaseUtils.getResults(appDatabase, tabFragment, resultsType);
        }
    }
}
