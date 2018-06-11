package com.example.gopinathana1074.anjalimapsapp;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;
    private static final double LATLNG_RECTANGLE_DIST = (5.0/60.0);
    private boolean gotMyLocationOnce;
    private boolean isGPSEnabled=false, isNetworkEnabled=false;
    private boolean trackingMyLocation = true;
    private String theProvider;

    private LatLng userLoc = null;

    private static final long MIN_TIME_BTWN_UPDATES = 1000*5;     //update every 5 seconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;

    private LocationListener locationListenerGPS;

    private static final int MY_LOC_ZOOM_FACTOR = 17;           //The higher the value, the more zoomed in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Add a marker on the map that shows your place of birth
        LatLng SanDiego = new LatLng(32.799487, -117.154625);
        mMap.addMarker(new MarkerOptions().position(SanDiego).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(SanDiego));

        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("AnjaliMapsApp", "Failed FINE permission check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("AnjaliMapsApp", "Failed COARSE permission check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        if( (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            )||(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) ){
            Log.d("AnjaliMapsApp", "Either FINE or COARSE Passed permission check");
            mMap.setMyLocationEnabled(true);
        }

        locationSearch = (EditText) findViewById(R.id.editText_SearchAddress);

        gotMyLocationOnce = false;
    }

    public void onSearch(View view){
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //Use LocationManager for user location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria =  new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("AnjaliMapsApp", "MapsActivity: onSearch: location = " + location);
        Log.d("AnjaliMapsApp", "MapsActivity: onSearch: provider = " + provider);
        //myLocation = locationManager.getLastKnownLocation(provider);
//        LatLng userLoc = null;
        try {
            //Check last known location
            //Need to specifically list the provider (network or gps)
            if(locationManager != null){        //if location is known
                Log.d("AnjaliMapsApp", "MapsActivity: onSearch: LocationManager is not null");
                if( (myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) ) != null ){
                    userLoc = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("AnjaliMapsApp", "MapsActivity: onSearch: using NETWORK_PROVIDER, current location = (Latitude = " + myLocation.getLatitude()+ ", Longitude = " + myLocation.getLongitude() + ")");
                    Toast.makeText(this, "UserLoc = (Latitude = " + myLocation.getLatitude()+ ", Longitude = " + myLocation.getLongitude() + ")", Toast.LENGTH_SHORT).show();

                }
                else if((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null ){
                    userLoc = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("AnjaliMapsApp", "MapsActivity: onSearch: using GPS_PROVIDER, current location = (Latitude = " + myLocation.getLatitude()+ ", Longitude = " + myLocation.getLongitude() + ")");
                    Toast.makeText(this, "UserLoc = (Latitude = " + myLocation.getLatitude()+ ", Longitude = " + myLocation.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d("AnjaliMapsApp", "MapsActivity: onSearch: myLocation is NULL");
                }
            }
        }
        catch(SecurityException | IllegalArgumentException e){
            Log.d("AnjaliMapsApp", "MapsActivity: onSearch: Exception on getLastKnown Location");
            Toast.makeText(this, "Illegal exception; couldn't find your location!", Toast.LENGTH_SHORT).show();
        }

        if(!location.matches("")){
            Log.d("AnjaliMapsApp", "MapsActivity: onSearch: Location string is not blank");

            //Create geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                //Get a list of addresses
                addressList = geocoder.getFromLocationName(location, 50,
                        userLoc.latitude - LATLNG_RECTANGLE_DIST,
                        userLoc.longitude - LATLNG_RECTANGLE_DIST,
                        userLoc.latitude + LATLNG_RECTANGLE_DIST,
                        userLoc.longitude + LATLNG_RECTANGLE_DIST);
            }
            catch(IOException e){
                e.printStackTrace();
                Log.d("AnjaliMapsApp", "MapsActivity: onSearch: Exception on filling addressList with results from geocoder");
                Toast.makeText(this, "Illegal exception on filling addressList with results from geocoder!", Toast.LENGTH_SHORT).show();
            }
            if(!addressList.isEmpty()){
                Log.d("AnjaliMapsApp", "MapsActivity: onSearch: Address list size = " + addressList.size());
                Toast.makeText(this, ""+addressList.size() + " results found", Toast.LENGTH_SHORT).show();

                for(int i=0; i<addressList.size(); i++){
                    Address address = addressList.get(i);
                    LatLng latlng = new LatLng(  address.getLatitude(), address.getLongitude()  );

                    mMap.addMarker(new MarkerOptions().position(latlng).title("" + i + ": "+ address.getSubThoroughfare()));
                    Log.d("AnjaliMapsApp", "MapsActivity: onSearch: added marker to searched location");
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                }



            }
            else{
                Log.d("AnjaliMapsApp", "MapsActivity: onSearch: NO RESULTS FOUND");
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();

            }
            theProvider=provider;


        }
    }
    public void changeView(View view){
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }
    public void getLocation(){
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            //isProviderEnabled returns true if user has enabled gps on phone
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(isGPSEnabled){
                Log.d("AnjaliMapsApp", "MapsActivity: getLocation: GPS is enabled.");
            }

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(isNetworkEnabled){
                Log.d("AnjaliMapsApp", "MapsActivity: getLocation: Network is enabled.");
            }

            if(!isGPSEnabled && !isNetworkEnabled){
                Log.d("AnjaliMapsApp", "MapsActivity: getLocation: No provider is enabled.");
            }
            else{
                if(isNetworkEnabled){
                    if(         ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            &&  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {        //check permissions
                        Log.d("AnjaliMapsApp", "MapsActivity: getLocation: Network is enabled, Coarse and Fine location permission granted.");
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if(isGPSEnabled){
                    //if(         ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS))
                }
            }

            trackingMyLocation = (isNetworkEnabled || isGPSEnabled);
        }
        catch(Exception e){
            Log.d("AnjaliMapsApp", "MapsActivity: getLocation: caught exception");
            e.printStackTrace();
        }
    }
    //locationlistener is an anonymous inner class
    //set up for callbacks from the requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAMarker(theProvider);

            //Check if doing one time via onMapReady -> if so, remove updates to both GPS and network
            if(!gotMyLocationOnce){
                locationManager.removeUpdates(this);

                locationManager.removeUpdates(locationListenerGPS);
            }
            else {  //Tracking location now, so we relaunch request for network
                Log.d("AnjaliMapsApp", "MapsActivity: locationListenerNetwork: onLocationChanged: Tracking location now, so relaunch request for network");

                if(         ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        &&  ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {        //check permissions
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                trackingMyLocation=true;

            }
        }
        public void trackMyLocation(View view){
            if(trackingMyLocation){
                locationManager.removeUpdates(locationListenerNetwork);
                locationManager.removeUpdates(locationListenerGPS);

                Toast.makeText(MapsActivity.this, "Tracking is now off", Toast.LENGTH_SHORT).show();
                trackingMyLocation = false;
            }
            else {
                getLocation();
               Toast.makeText(MapsActivity.this, "Tracking is now on", Toast.LENGTH_SHORT).show();
                trackingMyLocation = true;
            }
        }
        private void dropAMarker(String locationProvider) {
            if(         ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    &&  ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {        //if checkSelfPermission fails
                return;
            }

            myLocation = locationManager.getLastKnownLocation(locationProvider);

            userLoc = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            if(locationProvider.contains("GPS")) {
                mMap.addMarker(new MarkerOptions().position(userLoc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
            else if(locationProvider.contains("Network")){
                mMap.addMarker(new MarkerOptions().position(userLoc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }

            mMap.moveCamera( CameraUpdateFactory.newLatLng(userLoc) );
        }
        public void clear (View view){
            mMap.clear();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("AnjaliMapsApp", "MapsActivity: locationListenerNetwork: onStatusChanged: Status changed");
            Toast.makeText(MapsActivity.this, "Status changed", Toast.LENGTH_SHORT).show();
        }
        //don't have to worry about onProviderEnabled or disabled because GPS won't be disabled/enabled
        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
