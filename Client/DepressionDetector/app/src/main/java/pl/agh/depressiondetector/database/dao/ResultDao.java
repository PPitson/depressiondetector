package pl.agh.depressiondetector.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pl.agh.depressiondetector.database.entity.Result;

@Dao
public interface ResultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Result> results);

    @Query("SELECT * FROM Result " +
            "INNER JOIN ResultType " +
            "ON Result.typeId = ResultType.id " +
            "WHERE ResultType.name LIKE :type")
    List<Result> findByType(String type);
}
