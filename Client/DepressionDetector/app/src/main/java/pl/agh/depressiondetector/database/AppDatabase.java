package pl.agh.depressiondetector.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import pl.agh.depressiondetector.database.dao.ResultDao;
import pl.agh.depressiondetector.database.dao.ResultTypeDao;
import pl.agh.depressiondetector.database.entity.Result;
import pl.agh.depressiondetector.database.entity.ResultType;

@Database(entities = {
        Result.class,
        ResultType.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ResultDao getResultDao();
    public abstract ResultTypeDao getResultTypeDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                    "result-database").build();
        }

        return instance;
    }
}
