package com.example.gamer.trackme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";

    //static final vars
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 18f;
    private static final int DEFAULT_MILISEC = 2000;
    private static final int SET_INTERVAL_MLISECONDS = 1000;
    private static final int SET_FASTEST_INTERVAL_MLISECONDS = 1000;

    //vars for location
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mPrevious_Location;

    //vars
    private Boolean isZooming = true;
    private Boolean isStartTracking = false;
    private float isSumDistance = 0.0f;
    private Boolean isFirstTime = false;
    MenuItem _activityMenu = null;
    private ArrayList<LatLng> mTripPath = new ArrayList<>();
    private Bundle bundle = new Bundle();

    private Handler handler;
    private Runnable runnable;
    private Long mTimer = 0l;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);

        getLocationPermission();

        setToolbarTitle();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isStartTracking) {
                    // ...
                    mTimer++;
                    handler.postDelayed(this,SET_INTERVAL_MLISECONDS);
                }
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("MyBoolean", isStartTracking);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isStartTracking = savedInstanceState.getBoolean("MyBoolean");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        setToolbarTitle();
        if (_activityMenu!=null){
        if(isStartTracking){_activityMenu.setTitle("Stop");}
        else{_activityMenu.setTitle("Start");}}
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setToolbarTitle() {
        android.support.v7.widget.Toolbar tlb = findViewById(R.id._toolbar);
        String title = getCurrentUser() != null ? getApplicationName(this) + " - [ " + getCurrentUser() + " ]" :
                getApplicationName(this);
        tlb.setTitle(title);
        setSupportActionBar(tlb);
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id._map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //set request location tracker
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(SET_INTERVAL_MLISECONDS);
            mLocationRequest.setFastestInterval(SET_FASTEST_INTERVAL_MLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());

                //set locations last and current
                LatLng mCurrent_Location = new LatLng(location.getLatitude(), location.getLongitude());

                //move map camera
                if (isZooming) {moveCamera(mCurrent_Location, DEFAULT_ZOOM);}
                //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrent_Location, DEFAULT_ZOOM));

                //add Polyline and measure distance
                if (isStartTracking || isFirstTime) {
                    //add Polyline Tracking Movements
                    mGoogleMap.addPolyline((new PolylineOptions())
                            .add(mPrevious_Location, mCurrent_Location).width(6).color(Color.BLUE)
                            .visible(true));

                    //measure distance
                    Location mPreviousLocation = new Location("");
                    mPreviousLocation.setLatitude(mPrevious_Location.latitude);
                    mPreviousLocation.setLongitude(mPrevious_Location.longitude);

                    float isDistance = mPreviousLocation.distanceTo(location);
                    isSumDistance += isDistance;

                    //add current location to array
                    mTripPath.add(mPrevious_Location);
                }

                //Place current location marker
                if (isFirstTime){
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mPrevious_Location);
                    if (isStartTracking) {markerOptions.title("Start Position");} else {
                        markerOptions.title("Stop Position");
                        String testString = Integer.toString(Math.round(isSumDistance));
                        Toast.makeText(MapActivity.this,"Your distance is: " + testString + " meters.", Toast.LENGTH_LONG).show();
                    }
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mGoogleMap.addMarker(markerOptions);
                    isFirstTime = false;
                }
                //add path to Bundle
                bundle.putParcelableArrayList("path", mTripPath);
                //update previous location
                mPrevious_Location = mCurrent_Location;
            }
        }
    };

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient( this);

        try {
            if (mLocationPermissionsGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        //location found
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                        mPrevious_Location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    } else {
                        Toast.makeText(MapActivity.this,"Unable to get current location!", Toast.LENGTH_SHORT).show();
                    }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom),DEFAULT_MILISEC,null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater _inflater = getMenuInflater();
        _inflater.inflate(R.menu.toolbar_menu, menu);
        _activityMenu = menu.findItem(R.id.mnu_spot);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnu_spot:
                isZooming = !isZooming;
                if(isZooming){
                    _activityMenu.setIcon(R.drawable.ic_spot_target);
                } else {_activityMenu.setIcon(R.drawable.ic_unspot_target);}
                return true;
            case R.id.mnu_startstop:
                isStartTracking = !isStartTracking;
                if (isStartTracking) {
                    mGoogleMap.clear();
                    item.setTitle("Stop");
                    isFirstTime = true;
                    resetVars();
                    handler.postDelayed(runnable, SET_INTERVAL_MLISECONDS);
                } else {
                    item.setTitle("Start");
                    isFirstTime = true;
                    Toast.makeText(MapActivity.this,"Your time is: " + mTimer.toString() + " sec.", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.mnu_signin:
                Intent _intentlogin = new Intent(MapActivity.this, LoginActivity.class);
                _intentlogin.putExtra("class", this.getClass().getSimpleName());
                startActivity(_intentlogin);
                return true;
            case R.id.mnu_settings:
                Intent _intentsetings = new Intent(MapActivity.this, SettingsActivity.class);
                startActivity(_intentsetings);
                return true;
            case R.id.mnu_savetrip:
                Intent _intentpop = new Intent(MapActivity.this, popActivity.class);
                _intentpop.putExtra("distance", isSumDistance);
                _intentpop.putExtra("time", mTimer);
                _intentpop.putExtras(bundle);
                startActivity(_intentpop);
                return true;
            case R.id.mnu_rank:
                Intent _intentrank = new Intent(MapActivity.this, RankActivity.class);
                startActivity(_intentrank);
                return true;
            case R.id.mnu_fame:
                Intent _intentfame = new Intent(MapActivity.this, FameActivity.class);
                startActivity(_intentfame);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetVars() {
        isSumDistance = 0.0f;
        mTripPath.clear();
        mTimer = 0l;
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                                                    permissions,
                                                    LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private String getCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            String uid = user.getUid();

            String[] userName = email.split("@");
            return  userName[0];
        } else {
            return null;
        }
    }
}
