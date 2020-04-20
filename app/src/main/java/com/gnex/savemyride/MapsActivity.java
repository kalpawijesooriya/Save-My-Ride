package com.gnex.savemyride;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gnex.savemyride.DirectionHelpers.DataParser;
import com.gnex.savemyride.DirectionHelpers.FetchURL;
import com.gnex.savemyride.DirectionHelpers.Maker;
import com.gnex.savemyride.DirectionHelpers.TaskLoadedCallback;
import com.gnex.savemyride.Models.MarkerModel;
import com.gnex.savemyride.Models.PlaceInfo;
import com.gnex.savemyride.Models.RuleBreaks;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.libraries.places.compat.GeoDataClient;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.AutocompletePrediction;
import com.google.android.libraries.places.compat.Place;
import com.google.android.libraries.places.compat.PlaceBufferResponse;
import com.google.android.libraries.places.compat.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.gnex.savemyride.AppConstants.COARSE_LOCATION;
import static com.gnex.savemyride.AppConstants.DEFAULT_ZOOM;
import static com.gnex.savemyride.AppConstants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, TaskLoadedCallback, GoogleMap.OnMarkerClickListener,LocationListener  {

    private static final String TAG = "MapActivity";
    Float avgSpeed=0.00F;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public static GoogleMap mMap;
    private Boolean mLocationPermissionGranted = false;
    float nCurrentSpeed;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter,mPlaceAutocompleteAdapter1,mPlaceAutocompleteAdapter2;
    private GeoDataClient mGoogleApiClient;
    private PlaceInfo mPlace;

    private Polyline mCurrentPolyline;
    private Location mCurrentLocation,mOriginLocation;
    FloatingActionButton  go,myLocation,searchFab;
    private AutoCompleteTextView mSearchText,edtTxtOrigin,edtTxtDestination;
    LinearLayout toFrom;
    RelativeLayout search,bottpmRel,notificationRel,distance_rel,speed_rel;
    Button startNav;
    JSONObject distance ,duration,start_location;
    Marker  markerStart,markerEnd;
    private  TextView time,far,distance_txt,speed_txt;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean isGPS = false;
    private LocationRequest locationRequest;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136)
    );
    private static DecimalFormat df2 = new DecimalFormat("#");
    private TextView notification,speed_warn;
    private  List <MarkerModel> markerModels;
    Handler handler;
    String  userid;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        List<String> list = new ArrayList<String>();
        list.add("Select location on map");
        list.add("Select location from saved places");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.spinner_item_layout);

        List<RuleBreaks> ruleBrakes=new ArrayList<RuleBreaks>();


        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1 * 1000); // 10 seconds
        locationRequest.setFastestInterval(1 * 1000); // 5 seconds


        mSearchText = findViewById(R.id.inputSearch);
        edtTxtOrigin=findViewById(R.id.edtTxtOrigin);
        edtTxtDestination=findViewById(R.id.edtTxtDestination);
        distance_txt=findViewById(R.id.distance_txt);

        time=findViewById(R.id.time);
        far=findViewById(R.id.distance);
        startNav=findViewById(R.id.startNav);

        go =findViewById(R.id.go);
        myLocation =findViewById(R.id.myLocation);
        searchFab=findViewById(R.id.searchFab);
        toFrom=findViewById(R.id.toFrom);
        search=(RelativeLayout)findViewById(R.id.relLayout);
        notificationRel= findViewById(R.id.notificationRel);

        bottpmRel=findViewById(R.id.bottpmRel);
        distance_rel=findViewById(R.id.distance_rel);
        notification= findViewById(R.id.notification);
        speed_rel=findViewById(R.id.speed_rel);
        speed_txt= findViewById(R.id.speed);
        speed_warn=findViewById(R.id.speed_warn);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        go.setOnLongClickListener(new View.OnLongClickListener() {

            @SuppressLint("RestrictedApi")
            @Override
            public boolean onLongClick(View arg) {
                search.setVisibility(View.INVISIBLE);
                toFrom.setVisibility(View.VISIBLE);
                go.setVisibility(View.INVISIBLE);
                searchFab.setVisibility(View.VISIBLE);
                mMap.clear();
            return false;
            }
        });

        searchFab.setOnLongClickListener(new View.OnLongClickListener() {

            @SuppressLint("RestrictedApi")
            @Override
            public boolean onLongClick(View arg) {
                mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                MapsActivity.this, R.raw.normal));
                edtTxtDestination.setText("");
                toFrom.setVisibility(View.INVISIBLE);
                search.setVisibility(View.VISIBLE);
                searchFab.setVisibility(View.INVISIBLE);
                go.setVisibility(View.VISIBLE);
                bottpmRel.setVisibility(View.INVISIBLE);
                notificationRel.setVisibility(View.INVISIBLE);
                distance_rel.setVisibility(View.INVISIBLE);
                speed_rel.setVisibility(View.INVISIBLE);
                stopLocationUpdates();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                LatLng ll = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 8));
                mMap.clear();
                return false;
            }
        });

        locationCallback = new LocationCallback() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {

                }
                for (Location location : locationResult.getLocations()) {
                    markerStart.remove();
                    markerStart = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(),location.getLongitude()))
                            .draggable(false).visible(true).title("your Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

                    LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                    float zoom = mMap.getCameraPosition().zoom;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoom));

                    for (int i = 0; i < markerModels.size(); ++i) {


                        double dist = Maker.getStraightLineDistance(ll, markerModels.get(i).getLocation());




                       if(dist>=0.01 && dist<=0.1&& markerModels.get(i).isStstusPassed())
                       {
                           notificationRel.setVisibility(View.INVISIBLE);
                           distance_rel.setVisibility(View.INVISIBLE);
                           speed_warn.setVisibility(View.INVISIBLE);
                           speed_txt.setTextColor(ContextCompat.getColor(MapsActivity.this, R.color.colorAccent));

                           if(!ruleBrakes.isEmpty()){


                               Float sumSpeed=0.00F;

                               for (int j = 0; j < ruleBrakes.size(); j++) {
                                   sumSpeed=sumSpeed+ruleBrakes.get(j).speed;
                               }
                               avgSpeed=sumSpeed/ruleBrakes.size();
                               Date currentTime = Calendar.getInstance().getTime();
                               String key = mDatabase.child("RuleBreaks").child(currentUser.getUid()).push().getKey();

                               RuleBreaks ruleBreaks1 = new RuleBreaks(currentTime.toString(),avgSpeed,markerModels.get(i).getSpeed(),markerModels.get(i).getLocation(),markerModels.get(i).getType());
                               mDatabase.child("RuleBreaks").child(currentUser.getUid()).child(key).setValue(ruleBreaks1);
                               ruleBrakes.clear();
                           }

                      }

                        try {
                        if (dist<=0.040){
                                   if ( !markerModels.get(i).isStstusPassed()) {
                                       notificationRel.setVisibility(View.VISIBLE);
                                       distance_rel.setVisibility(View.VISIBLE);
                                   }

                                   if(dist<=0.01)
                                    {
                                        markerModels.get(i).setStstusPassed(true);
                                        if (nCurrentSpeed>= Float.valueOf(markerModels.get(i).getSpeed()))
                                        {
                                            speed_warn.setText("Your Speed is Too High \n Slow down to " + markerModels.get(i).getSpeed()+" Km/h");
                                            speed_warn.setVisibility(View.VISIBLE);

                                            speed_txt.setTextColor(ContextCompat.getColor(MapsActivity.this, R.color.colorRed));
                                            RuleBreaks obj=new RuleBreaks();
                                            obj.speed= nCurrentSpeed;
                                            ruleBrakes.add(obj);
                                            playSound();

                                       }else {
                                            speed_warn.setVisibility(View.INVISIBLE);
                                            speed_txt.setTextColor(ContextCompat.getColor(MapsActivity.this, R.color.colorAccent));

                                        }

                                    }


                                   if (!markerModels.get(i).isStstus())
                                   {
                                       playSound();
                                       markerModels.get(i).setStstus(true);

                                   }



                               notification.setText( markerModels.get(i).getType()+ " in " +markerModels.get(i).getName());
                               distance_txt.setText( String.valueOf(df2.format(dist*1000))+ "m More");


                           }




                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
        };

        startNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                markerStart.remove();
                DataParser obj=new DataParser();
                JSONObject startLocation=  obj.getStart_location();


                try {


                         mMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        MapsActivity.this, R.raw.style_json));




                    markerStart = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                            .draggable(false).visible(true).title("your Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));


                    LatLng ll = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 20));
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    toFrom.setVisibility(View.INVISIBLE);
                    speed_rel.setVisibility(View.VISIBLE);
                    speed_txt.setText("-.-\nkm/h");
                    startNav.setVisibility(View.INVISIBLE);
                    startLocationUpdates();
                    doStuff();

                } catch (Exception e) {
                    e.printStackTrace();
                }






            }
        });

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
      currentUser = mAuth.getCurrentUser();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getLDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            //"Show My location"(GPS icon) button removed, because our custom search bar will block its view
            mMap.getUiSettings();

            init();

        }

        mMap.setOnMarkerClickListener(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length>0){
                    for(int i=0;i< grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }





    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                AppConstants.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                     COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted =true;
                initMap();
            }
            else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void init(){
        Log.d(TAG,"init: initializing");

        mGoogleApiClient = Places.getGeoDataClient(this,null);

        mSearchText.setOnItemClickListener(mAutoCompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter( this,mGoogleApiClient,LAT_LNG_BOUNDS,null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute method for searching
                    geolocate();
                }

                return false;
            }

        });

        edtTxtOrigin.setOnItemClickListener(mAutoCompleteClickListener1);

        mPlaceAutocompleteAdapter1 = new PlaceAutocompleteAdapter( this,mGoogleApiClient,LAT_LNG_BOUNDS,null);
        edtTxtOrigin.setAdapter(mPlaceAutocompleteAdapter1);
        edtTxtOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute method for searching
                    geolocate();
                }

                return false;
            }

        });

        edtTxtDestination.setOnItemClickListener(mAutoCompleteClickListener2);

        mPlaceAutocompleteAdapter2 = new PlaceAutocompleteAdapter( this,mGoogleApiClient,LAT_LNG_BOUNDS,null);
        edtTxtDestination.setAdapter(mPlaceAutocompleteAdapter2);
        edtTxtDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute method for searching
                    geolocate();
                }

                return false;
            }

        });


        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: Clicked gps icon");
                getLDeviceLocation();
            }
        });


        hideSoftKeyboard(this, getWindow().getDecorView().getRootView());
    }


    @Override
    public void onTaskDone(Object... values) throws JSONException {
        if(mCurrentPolyline!=null){
            mCurrentPolyline.remove();
        }
        bottpmRel.setVisibility(View.VISIBLE);

        mCurrentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        markerModels= (List<MarkerModel>) values[1];
        DataParser obj=new DataParser();
        duration  = obj.getDuration();
        distance = obj.getDistance();
        time.setText(duration.getString("text"));
        far.setText("( " +distance.getString( "text")+" )");
        if (markerModels!=null){
          for (int i=0;i<markerModels.size();i++)
          {
              if (markerModels.get(i).getType().equals("Curve")){
                  MarkerOptions options = new MarkerOptions()
                          .position(markerModels.get(i).getLocation())
                          .title("RoadSign").icon(BitmapDescriptorFactory.fromResource(R.drawable.curve));
                  mMap.addMarker(options);
              }
              else  if (markerModels.get(i).getType().equals("School")){
                  MarkerOptions options = new MarkerOptions()
                          .position(markerModels.get(i).getLocation())
                          .title("RoadSign").icon(BitmapDescriptorFactory.fromResource(R.drawable.school));
                  mMap.addMarker(options);
              }
              else  if (markerModels.get(i).getType().equals("4wy Junction")){
                  MarkerOptions options = new MarkerOptions()
                          .position(markerModels.get(i).getLocation())
                          .title("RoadSign").icon(BitmapDescriptorFactory.fromResource(R.drawable.crossraod));
                  mMap.addMarker(options);
              }
              else  if (markerModels.get(i).getType().equals("90-Curve")){
                  MarkerOptions options = new MarkerOptions()
                          .position(markerModels.get(i).getLocation())
                          .title("RoadSign").icon(BitmapDescriptorFactory.fromResource(R.drawable.nine));
                  mMap.addMarker(options);
              }
              else  if (markerModels.get(i).getType().equals("Elbow-Curve")){
                  MarkerOptions options = new MarkerOptions()
                          .position(markerModels.get(i).getLocation())
                          .title("RoadSign").icon(BitmapDescriptorFactory.fromResource(R.drawable.elbow));
                  mMap.addMarker(options);
              }


          }


        }
    }


    private void getLDeviceLocation(){
        Log.d(TAG,"getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"onComplete: found location !");
                            Location currentLocation = (Location) task.getResult();
                            // mCurrentLocation = currentLocation;
                            ////////////////temporary//////////////////
                            Location LL = new  Location("");
                            LL.setLatitude(currentLocation.getLatitude());
                            LL.setLongitude(currentLocation.getLongitude());


                            mCurrentLocation = LL;
                            mOriginLocation=LL;
                            moveCamera( new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,"My location");
                        }
                        else{
                            Log.d(TAG,"onComplete: current location is null");
                            Toast.makeText(MapsActivity.this,"Unable to get Current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
            Log.e(TAG,"getLDeviceLocation: SecurityException: "+e.getMessage());
        }
    }



    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void geolocate() {
        Log.d(TAG,"geoLocate: geoLocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString,1);
        }
        catch (IOException e) {
            Log.e(TAG,"geoLocate: IOException"+ e.getMessage());
        }
        if(list.size()>0){
            Address address = list.get(0);

            Log.d(TAG,"geoLocate: found a location : "+ address.toString());
            //
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
        }
    }
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void moveCamera(LatLng latLng, float zoom,String title){
        Log.d(TAG,"moveCamer: moving the camera to: lat:"+latLng.latitude+" long:"+latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        if(title != "My location"){

            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard(this, getWindow().getDecorView().getRootView());
    }

    private AdapterView.OnItemClickListener mAutoCompleteClickListener  = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mMap.clear();
            hideSoftKeyboard(getBaseContext(), getWindow().getDecorView().getRootView());
            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            Task<PlaceBufferResponse> placeResult = mGoogleApiClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mSearchPlaceDetailsCallback);

        }
    };

    private AdapterView.OnItemClickListener mAutoCompleteClickListener1  = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mMap.clear();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter1.getItem(i);
            final String placeId = item.getPlaceId();

            Task<PlaceBufferResponse> placeResult = mGoogleApiClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mFromPlaceDetailsCallback);

        }
    };

    private AdapterView.OnItemClickListener mAutoCompleteClickListener2  = new AdapterView.OnItemClickListener() {

        @SuppressLint("RestrictedApi")
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {



            hideSoftKeyboard(getBaseContext(), getWindow().getDecorView().getRootView());
            final AutocompletePrediction item = mPlaceAutocompleteAdapter2.getItem(i);
            final String placeId = item.getPlaceId();

            Task<PlaceBufferResponse> placeResult = mGoogleApiClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mToPlaceDetailsCallback);

        }
    };


    private OnCompleteListener<PlaceBufferResponse> mSearchPlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                try{
                    mPlace = new PlaceInfo() ;
                    mPlace.setAddress(place.getAddress().toString());
                    mPlace.setName(place.getName().toString());
                    mPlace.setPhonenumber(place.getPhoneNumber().toString());
                    mPlace.setLatlng(place.getLatLng());
                    mPlace.setRating(place.getRating());
                    mPlace.setWebSite(place.getWebsiteUri());
                    mPlace.setId(place.getId());

                }catch (NullPointerException ex){
                    Log.e(TAG,"onComplete: NullPointerException " + ex.getMessage());
                }

                moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                        place.getViewport().getCenter().longitude),DEFAULT_ZOOM,"Search Place");
                places.release();
            } catch (RuntimeRemoteException e) {

                Log.e(TAG, "Place query did not complete.", e);

                return;
            }
        }
    };

    private OnCompleteListener<PlaceBufferResponse> mToPlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                try{
                    mPlace = new PlaceInfo() ;
                    mPlace.setAddress(place.getAddress().toString());
                    mPlace.setName(place.getName().toString());
                    mPlace.setPhonenumber(place.getPhoneNumber().toString());
                    mPlace.setLatlng(place.getLatLng());
                    mPlace.setRating(place.getRating());
                    mPlace.setWebSite(place.getWebsiteUri());
                    mPlace.setId(place.getId());
                    startNav.setVisibility(View.VISIBLE);
                }catch (NullPointerException ex){
                    Log.e(TAG,"onComplete: NullPointerException " + ex.getMessage());
                }

                moveCameraDestination(new LatLng(place.getViewport().getCenter().latitude,
                        place.getViewport().getCenter().longitude),8,mPlace);
                places.release();
            } catch (RuntimeRemoteException e) {

                Log.e(TAG, "Place query did not complete.", e);

                return;
            }
        }
    };

    private OnCompleteListener<PlaceBufferResponse> mFromPlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                try{
                    mPlace = new PlaceInfo() ;
                    mPlace.setAddress(place.getAddress().toString());
                    mPlace.setName(place.getName().toString());
                    mPlace.setPhonenumber(place.getPhoneNumber().toString());
                    mPlace.setLatlng(place.getLatLng());
                    mPlace.setRating(place.getRating());
                    mPlace.setWebSite(place.getWebsiteUri());
                    mPlace.setId(place.getId());

                }catch (NullPointerException ex){
                    Log.e(TAG,"onComplete: NullPointerException " + ex.getMessage());
                }

                Location LL = new  Location("");
                LL.setLatitude(place.getViewport().getCenter().latitude);
                LL.setLongitude( place.getViewport().getCenter().longitude);
                mOriginLocation=LL;

                moveCameraStatPoint(new LatLng(place.getViewport().getCenter().latitude,
                        place.getViewport().getCenter().longitude),DEFAULT_ZOOM,"Origin");
                places.release();
            } catch (RuntimeRemoteException e) {

                Log.e(TAG, "Place query did not complete.", e);

                return;
            }
        }
    };

    private void moveCameraStatPoint(LatLng latLng, float zoom,String title){
        Log.d(TAG,"moveCamer: moving the camera to: lat:"+latLng.latitude+" long:"+latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        if(title != "My location"){

          MarkerOptions option=   new MarkerOptions()
                    .position(latLng)
                    .title(title).icon(BitmapDescriptorFactory.fromResource(R.drawable.origin));
          markerStart=  mMap.addMarker(option);
        }
        hideSoftKeyboard(this, getWindow().getDecorView().getRootView());
    }


    @SuppressLint("RestrictedApi")
    private void moveCameraDestination(final LatLng latLng, float zoom, PlaceInfo placeinfo){
        Log.d(TAG,"moveCamer: moving the camera to: lat:"+latLng.latitude+" long:"+latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));


        if(placeinfo !=null){
            try{
                String snippet = "Address" + placeinfo.getAddress() +"\n"
                        +"Phone Number" + placeinfo.getPhonenumber() +"\n"
                        +"Website" + placeinfo.getWebSite() +"\n"
                        +"Rating" + placeinfo.getRating() +"\n";

                if(mOriginLocation==mCurrentLocation){
                    mMap.clear();
                    MarkerOptions option=  new MarkerOptions()
                            .position(new LatLng(mOriginLocation.getLatitude(),mOriginLocation.getLongitude()))
                            .title("Your Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.origin));
                  markerStart = mMap.addMarker(option);
                }

                MarkerOptions option2=   new MarkerOptions()
                        .position(latLng)
                        .title(placeinfo.getName())
                        .snippet(snippet).icon(BitmapDescriptorFactory.fromResource(R.drawable.end));
                markerEnd = mMap.addMarker(option2);

                showDirections(new LatLng(mOriginLocation.getLatitude(),mOriginLocation.getLongitude()),latLng,"driving");

            }
            catch (NullPointerException ex){
                Log.e(TAG,"moveCamera: NullPointerException "+ex.getMessage());
            }
        }else
        {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }
        hideSoftKeyboard(this, getWindow().getDecorView().getRootView());
    }



    private void showDirections(LatLng origin,LatLng destination, String travelMode){

        String url = getDirectionsUrl(origin,destination,travelMode,"bus");
        new FetchURL(MapsActivity.this).execute(url,"driving");
        ////////////////////////////////////////////

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {

                Toast.makeText(MapsActivity.this,"xx",Toast.LENGTH_SHORT).show();
            }
        };

//        MyAsyncTask runner = new MyAsyncTask(this,handler);
//        runner.execute(origin,destination);
        ////////////////////////////////////////////
    }

    private String getDirectionsUrl(LatLng origin,LatLng destination, String travelMode, String transitMode ){

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude ;
        String mode = "mode="+travelMode;
        String tranMode = "transit_mode=" +transitMode;
        String params  = str_origin+"&"+str_dest+"&"+mode+"&"+tranMode;
        String output = "json";
        String url =  "https://maps.googleapis.com/maps/api/directions/"+output+"?"+params+"&key=AIzaSyBTGlGKhncI56mjYunuR2SiCt-rb5X2Zkc";
        return url;
    }
    private void startLocationUpdates() {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }
    private void doStuff(){
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (lm != null){
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, (LocationListener) this);
            //commented, this is from the old version
            // this.onLocationChanged(null);
        }
        Toast.makeText(this,"Waiting for GPS connection!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {





       nCurrentSpeed = location.getSpeed() * 3.6f;
        speed_txt.setText(String.format("%.2f", nCurrentSpeed)+ "\nkm/h" );


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void playSound() throws IllegalArgumentException,
        SecurityException,
        IllegalStateException,
        IOException {

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(this, soundUri);
        final AudioManager audioManager = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            // Uncomment the following line if you aim to play it repeatedly
            //mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }
    }

    public void stopLocationUpdates(){

        mFusedLocationClient.removeLocationUpdates(locationCallback);

    }
}
