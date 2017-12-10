package pl.agh.depressiondetector.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import pl.agh.depressiondetector.database.entity.ResultType;

@Dao
public interface ResultTypeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(ResultType... resultTypes);

    @Query("SELECT id FROM ResultType WHERE name LIKE :name")
    int getTypeId(String name);

    @Query("SELECT COUNT(*) FROM ResultType")
    int countResultTypes();
}
