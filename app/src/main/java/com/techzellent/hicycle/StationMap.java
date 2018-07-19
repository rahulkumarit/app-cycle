package com.techzellent.hicycle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.techzellent.hicycle.models.Stations;
import com.techzellent.hicycle.util.PermissionResultCallback;
import com.techzellent.hicycle.util.PermissionUtils;
import com.techzellent.hicycle.wsCalling.WSUtils;
import com.techzellent.hicycle.wsCalling.WsCalling;
import com.techzellent.hicycle.wsCalling.WsReponse;

import org.json.JSONObject;

public class StationMap extends FragmentActivity
        implements OnMapReadyCallback,
        PermissionResultCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, WsReponse {
    private GoogleMap mMap;
    private ArrayList<String> permissions = new ArrayList<>();
    private PermissionUtils permissionUtils;
    private Location currentLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private int FINE_LOCATION_REQUEST_CODE = 1;
    private String TAG = StationMap.class.getSimpleName();
    private FloatingActionButton fabHire;
    private TextView tvTitle;
    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_map);
        tvTitle = findViewById(R.id.tv_title);
        progress = findViewById(R.id.progress);
        tvTitle.setText("Bicycle Stations");
        permissionUtils = new PermissionUtils(StationMap.this);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(StationMap.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        permissionUtils.check_permission(permissions, "We need you location to server you the best and show Bicycle Stations", FINE_LOCATION_REQUEST_CODE);
        fabHire = (FloatingActionButton) findViewById(R.id.fabHire);
        fabHire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StationMap.this, Hire.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker at last location and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        WsCalling.postResponse(WSUtils.STATION_WS_CODE, WSUtils.WS_STATIONS, this);
        getCurrentLocation();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Using the util method when received permission result
        // Using the util method when received permission result
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void PermissionGranted(int request_code) {

        if (request_code == FINE_LOCATION_REQUEST_CODE) {
            getCurrentLocation();
        }
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {

    }

    @Override
    public void PermissionDenied(int request_code) {

    }

    @Override
    public void NeverAskAgain(int request_code) {

    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((StationMap.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED) {
                try {
                    mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(StationMap.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                currentLocation = location;
//                                addMarkerToCurrentLocation();
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            try {
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(StationMap.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            currentLocation = location;
//                            addMarkerToCurrentLocation();
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addMarkerToCurrentLocation() {
        LatLng loc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(loc).title("Currenet Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.0f));
    }

    @Override
    public void successReposse(int responseCode, String response) {
        setMultipleMarker(response);
    }

    @Override
    public void errorResponse(int responseCode, String exception) {

    }

    private void setMultipleMarker(String response) {
        if (!TextUtils.isEmpty(response)) {
            Gson gson = new Gson();
            Stations stations = gson.fromJson(response, Stations.class);
            if (stations.getStatus()) {
                for (int i = 0; i < stations.getStationList().size(); i++) {
                    Stations.StationList stationList = stations.getStationList().get(i);
                    double lat = stationList.getLat();
                    double lon = stationList.getLng();
                    LatLng loc = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(loc).title(stationList.getSname()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18.0f));
                }
            }
        }
    }

}
