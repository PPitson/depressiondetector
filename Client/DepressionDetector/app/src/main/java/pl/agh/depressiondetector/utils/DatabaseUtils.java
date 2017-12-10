package pl.agh.depressiondetector.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pl.agh.depressiondetector.database.AppDatabase;
import pl.agh.depressiondetector.database.dao.ResultTypeDao;
import pl.agh.depressiondetector.database.entity.Result;
import pl.agh.depressiondetector.database.entity.ResultType;
import pl.agh.depressiondetector.ui.tabs.TabFragment;

public class DatabaseUtils {
    public static void setup(AppDatabase appDatabase) {
        new SetupDatabaseAsyncTask(appDatabase).execute();
    }

    public static void insertResults(AppDatabase appDatabase, String results, String type, String ...args) {
        new InsertResultsAsyncTask(appDatabase, results, type).execute(args);
    }

    public static void insertResults(AppDatabase appDatabase, TabFragment tabFragment, String results, String type, String ...args) {
        new InsertResultsAsyncTask(appDatabase, results, type, tabFragment).execute(args);
    }

    public static void getResults(AppDatabase appDatabase, TabFragment tabFragment, String resultType) {
        new GetResultsAsyncTask(appDatabase, tabFragment, resultType).execute();
    }

    private static class SetupDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {
        private final AppDatabase appDatabase;

        private SetupDatabaseAsyncTask(AppDatabase appDatabase) {
            this.appDatabase = appDatabase;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ResultTypeDao resultTypeDao = appDatabase.getResultTypeDao();

            if (resultTypeDao.countResultTypes() < 4) {
                resultTypeDao.insertAll(
                        new ResultType("overview"),
                        new ResultType("mood"),
                        new ResultType("text"),
                        new ResultType("voice")
                );
            }
            return null;
        }
    }

    private static class InsertResultsAsyncTask extends AsyncTask<String, Void, List<Result>> {
        private final AppDatabase appDatabase;
        private final String results;
        private final String type;

        private TabFragment tabFragment;

        private InsertResultsAsyncTask(AppDatabase appDatabase, String results, String type) {
            this.appDatabase = appDatabase;
            this.results = results;
            this.type = type;
        }

        private InsertResultsAsyncTask(AppDatabase appDatabase, String results, String type, TabFragment tabFragment) {
            this.appDatabase = appDatabase;
            this.results = results;
            this.tabFragment = tabFragment;
            this.type = type;
        }

        @Override
        protected List<Result> doInBackground(String ...strings) {
            int typeId = appDatabase.getResultTypeDao().getTypeId(type);
            try {
                JSONArray jsonArray = new JSONArray(results);
                List<Result> resultList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    resultList.add(new Result((float) jsonObject.getDouble(strings[0]),
                            jsonObject.getString(strings[1]), typeId));
                }
                appDatabase.getResultDao().insertAll(resultList);
                return resultList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Result> results) {
            if (tabFragment != null) {
                tabFragment.displayResults(results);
            }
        }
    }

    private static class GetResultsAsyncTask extends AsyncTask<Void, Void, List<Result>> {
        private final AppDatabase appDatabase;
        private TabFragment tabFragment;
        private String type;

        private GetResultsAsyncTask(AppDatabase appDatabase, TabFragment tabFragment, String type) {
            this.appDatabase = appDatabase;
            this.tabFragment = tabFragment;
            this.type = type;
        }

        @Override
        protected List<Result> doInBackground(Void... voids) {
            return appDatabase.getResultDao().findByType(type);
        }

        @Override
        protected void onPostExecute(List<Result> results) {
            if (results != null && tabFragment != null) {
                tabFragment.displayResults(results);
            }
        }
    }
}