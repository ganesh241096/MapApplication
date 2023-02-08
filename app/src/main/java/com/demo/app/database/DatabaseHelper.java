package com.demo.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import kotlin.jvm.Volatile;


@Database(entities = {LocationObject.class},version = 1)
public abstract class DatabaseHelper extends RoomDatabase {
    @Volatile
    private static DatabaseHelper instance = null;
    public abstract LocationDAO locationDAO();

    public static synchronized DatabaseHelper getInstance(Context context){
        if(instance==null) {
            instance = Room.databaseBuilder(context.getApplicationContext()
                            , DatabaseHelper.class
                            , "db_location.db")
                    .build();
        }
        return instance;
    }

}
