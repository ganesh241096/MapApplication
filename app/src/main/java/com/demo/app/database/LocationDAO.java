package com.demo.app.database;


import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


@Dao
public interface LocationDAO {

    @Insert
    void insertBook(LocationObject user);

    @Update
    void updateBook(LocationObject user);

    @Delete
    void deleteBook(LocationObject user);

    @Query("DELETE FROM location WHERE LID = :LId")
    abstract void deleteByBookId(String LId);

    @Query("SELECT * FROM location")
    LiveData<List<LocationObject>> getLocationList();
}
