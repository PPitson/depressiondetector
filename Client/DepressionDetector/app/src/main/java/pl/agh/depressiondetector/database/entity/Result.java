package pl.agh.depressiondetector.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = ResultType.class,
                                  parentColumns = "id",
                                  childColumns = "typeId"),
        indices = @Index(value = {"date", "typeId"}, unique = true))
public class Result {

    public Result(float happinessLevel, String date, int typeId) {
        this.happinessLevel = happinessLevel;
        this.date = date;
        this.typeId = typeId;
    }

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "happiness_level")
    public float happinessLevel;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "typeId")
    public int typeId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
