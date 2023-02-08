package com.demo.app;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.demo.app.database.DatabaseHelper;
import com.demo.app.database.LocationDAO;
import com.demo.app.database.LocationObject;
import com.demo.app.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, OnMapReadyCallback {
    private ActivityMainBinding binding;
    private GoogleMap map;
    private MainViewModel mainViewModel;
    private DatabaseHelper databaseHelper;
    private Polyline arc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        // set data to auto completed textview
        setDataToAutoCompleteTextView();

        binding.btnClear.setOnClickListener(this::onClick);
        binding.btnSearch.setOnClickListener(this::onClick);
    }

    private void setDataToAutoCompleteTextView() {
        mainViewModel.getLocationList().observe(this, locationObjects -> {
            // set data from room database to autocompleted text view
            setData(locationObjects);
        });
    }

    private void setData(List<LocationObject> locationObjects) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (locationObjects != null && !locationObjects.isEmpty()) {
                                ArrayList<String> suggetions = new ArrayList<>();
                                for (int i = 0; i < locationObjects.size(); i++) {
                                    if (!suggetions.contains(locationObjects.get(i).getPickup_address())) {
                                        suggetions.add(locationObjects.get(i).getPickup_address());
                                    }
                                    if (!suggetions.contains(locationObjects.get(i).getDrop_address())) {
                                        suggetions.add(locationObjects.get(i).getDrop_address());
                                    }
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>
                                        (MainActivity.this, android.R.layout.select_dialog_item, suggetions);

                                binding.etPickupPoint.setAdapter(adapter);
                                binding.etDropPoint.setAdapter(adapter);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btnClear:
                binding.etPickupPoint.setText("");
                binding.etDropPoint.setText("");
                if (map != null) map.clear();
                break;
            case R.id.btnSearch:
                if (isValid()) {
                    if (map != null) map.clear();
                    final LatLng[] pickUp = new LatLng[1];
                    final LatLng[] drop = new LatLng[1];

                    Thread thread1 = new Thread() {
                        public void run() {
                            pickUp[0] = getLocationFromAddress(binding.etPickupPoint.getText().toString().trim());
                            setupMarker(new LatLng(pickUp[0].latitude, pickUp[0].longitude));
                        }
                    };
                    Thread thread2 = new Thread() {
                        public void run() {
                            drop[0] = getLocationFromAddress(binding.etDropPoint.getText().toString().trim());
                            setupMarker(new LatLng(drop[0].latitude, drop[0].longitude));
                        }
                    };
                    thread1.start();
                    try {
                        thread1.join();
                        thread2.start();
                        thread2.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // drop polygon
                    drawArc(new LatLng(pickUp[0].latitude, pickUp[0].longitude),
                            new LatLng(drop[0].latitude, drop[0].longitude));
                    // move to camera pickup location
                    moveMapCamera(new LatLng(pickUp[0].latitude, pickUp[0].longitude), 10);

                    // save location on room database
                    LocationObject locationObject =
                            new LocationObject(0,
                                    String.valueOf(pickUp[0].latitude), String.valueOf(pickUp[0].longitude), binding.etPickupPoint.getText().toString().trim(),
                                    String.valueOf(drop[0].latitude), String.valueOf(drop[0].longitude), binding.etDropPoint.getText().toString().trim()
                            );
                    new InsertLocationAsyncTask(databaseHelper.locationDAO()).execute(locationObject);
                }
                break;
        }

    }

    private static class InsertLocationAsyncTask extends AsyncTask<LocationObject, Void, Void> {
        private LocationDAO dao;

        private InsertLocationAsyncTask(LocationDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(LocationObject... model) {
            // below line is use to insert our modal in dao.
            dao.insertBook(model[0]);
            return null;
        }

    }

    private void setupMarker(final LatLng latLng) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MarkerOptions options = new MarkerOptions();
                            options.flat(true);
                            options.anchor(0.5f, 0.5f);
                            options.position(latLng);
                            options.icon(bitmapDescriptorFromVector(MainActivity.this, R.drawable.ic_sorce_marker));
                            map.addMarker(options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();


    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng latLng = null;
        try {
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            System.out.println("LatLong :" + location.getLatitude() + " " + location.getLongitude());
            return latLng;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void moveMapCamera(LatLng latLng, float zoom) {
        try {
            map.stopAnimation();
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        } catch (Exception e) {
        }
    }

    private boolean isValid() {
        if (isNullOrEmpty(binding.etPickupPoint.getText().toString().trim())) {
            showToast(getResources().getString(R.string.enter_pickup_point), 4);
            return false;
        }
        if (isNullOrEmpty(binding.etDropPoint.getText().toString().trim())) {
            showToast(getResources().getString(R.string.enter_drop_point), 4);
            return false;
        }
        if (binding.etDropPoint.getText().toString().trim().equalsIgnoreCase(binding.etPickupPoint.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_choose_diffrent_location), 4);
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }

    private void drawArc(LatLng start, LatLng end) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (arc != null) {
                                arc.remove();
                                arc = null;
                            }
                            List<LatLng> alLatLng = new ArrayList<>();

                            double cLat = ((start.latitude + end.latitude) / 2);
                            double cLon = ((start.longitude + end.longitude) / 2);

                            //add skew and arcHeight to move the midPoint
                            if (Math.abs(start.longitude - end.longitude) < 0.0001) {
                                cLon -= 0.019257;
                            } else {
                                cLat += 0.019257;
                            }

                            double tDelta = 1.0 / 5000;
                            for (double t = 0; t <= 1.0; t += tDelta) {
                                double oneMinusT = (1.0 - t);
                                double t2 = Math.pow(t, 2);
                                double lon = oneMinusT * oneMinusT * start.longitude
                                        + 2 * oneMinusT * t * cLon
                                        + t2 * end.longitude;
                                double lat = oneMinusT * oneMinusT * start.latitude
                                        + 2 * oneMinusT * t * cLat
                                        + t2 * end.latitude;
                                alLatLng.add(new LatLng(lat, lon));
                            }

                            PolylineOptions arcOptions = new PolylineOptions().addAll(alLatLng).width(10).color(getResources().getColor(R.color.black));
                            arc = map.addPolyline(arcOptions);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();

    }
}