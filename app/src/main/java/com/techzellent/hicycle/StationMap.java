package com.techzellent.hicycle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.techzellent.hicycle.models.Stations;
import com.techzellent.hicycle.util.AlertUtil;
import com.techzellent.hicycle.util.PermissionResultCallback;
import com.techzellent.hicycle.util.PermissionUtils;
import com.techzellent.hicycle.util.StaticUtils;
import com.techzellent.hicycle.wsCalling.WSUtils;
import com.techzellent.hicycle.wsCalling.WsCalling;
import com.techzellent.hicycle.wsCalling.WsReponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StationMap extends FragmentActivity
        implements OnMapReadyCallback,
        PermissionResultCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, WsReponse {
    private static final long LOCATION_REFRESH_TIME = 10000;
    private static final float LOCATION_REFRESH_DISTANCE = 10.0f;
    private GoogleMap mMap;
    private ArrayList<String> permissions = new ArrayList<>();
    private PermissionUtils permissionUtils;
    private Location currentLocation;
    private int FINE_LOCATION_REQUEST_CODE = 1;
    private String TAG = StationMap.class.getSimpleName();
    private FloatingActionButton fabHire;
    private TextView tvTitle;
    private ProgressBar progress;
    private LocationManager mLocationManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 5 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


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

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
        WsCalling.postResponse(WSUtils.STATION_WS_CODE, WSUtils.WS_STATIONS, this);
        if (StaticUtils.checkGpsOrLoaction(this)) {
            getCurrentLocation();
        } else {
            AlertUtil.showAletDailog(this, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, 555);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                                addMarkerToCurrentLocation();
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
                            addMarkerToCurrentLocation();
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

        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();


        mMap.addMarker(new MarkerOptions().
                position(loc)
                .title("Current Loaction")
                .snippet(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f));
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
                    if (stationList.getCycleNum() == 0) {
                        mMap.addMarker(new MarkerOptions().
                                position(loc)
                                .title("Bicycle Stations")
                                .snippet(stationList.getSname())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_zero)));
                    } else if (stationList.getCycleNum() == 1) {
                        mMap.addMarker(new MarkerOptions().
                                position(loc)
                                .title("Bicycle Stations")
                                .snippet(stationList.getSname())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_one)));
                    } else if (stationList.getCycleNum() == 2) {
                        mMap.addMarker(new MarkerOptions().
                                position(loc)
                                .title("Bicycle Stations")
                                .snippet(stationList.getSname())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_two)));
                    } else if (stationList.getCycleNum() == 3) {
                        mMap.addMarker(new MarkerOptions().
                                position(loc)
                                .title("Bicycle Stations")
                                .snippet(stationList.getSname())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_three)));
                    } else if (stationList.getCycleNum() == 4) {
                        mMap.addMarker(new MarkerOptions().
                                position(loc)
                                .title("Bicycle Stations")
                                .snippet(stationList.getSname())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_four)));
                    } else if (stationList.getCycleNum() == 5) {
                        mMap.addMarker(new MarkerOptions().
                                position(loc)
                                .title("Bicycle Stations")
                                .snippet(stationList.getSname())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_five)));
                    } else {
                        mMap.addMarker(new MarkerOptions().
                                position(loc)
                                .title("Bicycle Stations")
                                .snippet(stationList.getSname())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_five_plus)));
                    }
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f));
                }
            }
        }
    }

    boolean isLocation = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 555) {
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((StationMap.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            currentLocation = (Location) locationResult.getLastLocation();
                            String result = "Current Location Latitude is " +
                                    currentLocation.getLatitude() + "\n" +
                                    "Current location Longitude is " + currentLocation.getLongitude();
                            Log.e("current location:", result);
                            if (!isLocation) {
                                addMarkerToCurrentLocation();
                            }
                            isLocation = true;
                        }
                    }, Looper.myLooper());
                }

            } else {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        currentLocation = (Location) locationResult.getLastLocation();
                        String result = "Current Location Latitude is " +
                                currentLocation.getLatitude() + "\n" +
                                "Current location Longitude is " + currentLocation.getLongitude();
                        Log.e("current location:", result);

                        if (!isLocation) {
                            addMarkerToCurrentLocation();
                        }

                        isLocation = true;
                    }
                }, Looper.myLooper());
            }

        }
    }
}


