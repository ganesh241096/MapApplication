package com.demo.app;

import android.app.Application;

import com.demo.app.database.LocationObject;
import com.demo.app.repository.LocationRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainViewModel extends AndroidViewModel {
    private LocationRepository locationRepository;
    public MainViewModel(@NonNull Application application) {
        super(application);
        locationRepository = new LocationRepository(application);
    }
    public LiveData<List<LocationObject>> getLocationList() {
        return locationRepository.getLocationList();
    }

}
