package com.demo.app.repository;

import android.annotation.SuppressLint;
import android.app.Application;

import com.demo.app.database.DatabaseHelper;
import com.demo.app.database.LocationObject;

import java.util.List;

import androidx.lifecycle.LiveData;

public class LocationRepository {
    private Application application;
    private DatabaseHelper databaseHelper;
    private LiveData<List<LocationObject>> getLocationListMutableLiveData;

    public LocationRepository(Application application) {
        this.application = application;
        databaseHelper = DatabaseHelper.getInstance(application);
        getLocationListMutableLiveData = databaseHelper.locationDAO().getLocationList();
    }


    public LiveData<List<LocationObject>> getLocationList() {
        return getLocationListMutableLiveData;
    }

}
