package com.mcodefactory.wheredidipark;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int DEFAULT_ZOOM = 16;
    private static final LatLng DEFAULT_LOCATION = new LatLng(44.787197, 20.457273);
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LatLng currentPosition;
    private LatLng savedLocation;
    private String savedNote;
    private boolean isInGarage;
    private String garageLevel;
    private String garageRow;
    private String garageZone;
    private String garageSpot;
    Marker vehiclePositionMarker;
    MarkerOptions markerOptions;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;

    private SharedPreferences sharedPreferences;
    public static final String SHARED_PREFERENCES_FILE_KEY = "com.mcodefactory.wheredidipark.sharedpreference_key";
    private static final String VEHICLE_LOCATION_LAT_KEY = "lat";
    private static final String VEHICLE_LOCATION_LNG_KEY = "lng";
    public static final String DEFAULT_STRING = "default";

    MenuItem deleteLocationItem;
    MenuItem addNoteItem;
    MenuItem shareLocationItem;
    MenuItem navigateItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, MODE_PRIVATE);

        retrieveSavedLocation();

        retrieveNoteInfo();

        markerOptions = new MarkerOptions();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(this, R.drawable.ic_car)))
                .title(getResources().getString(R.string.marker_title));
        if (savedLocation != null) {
            markerOptions.position(savedLocation);
            vehiclePositionMarker = mMap.addMarker(markerOptions);
        }
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String toastMessage = "";
                if (isInGarage) {
                    if (!garageLevel.equals(DEFAULT_STRING)) {
                        toastMessage = toastMessage +
                                getResources().getString(R.string.level_label_text) +
                                " " + garageLevel;
                    }
                    if (!garageZone.equals(DEFAULT_STRING)) {
                        toastMessage = toastMessage + ", " +
                                getResources().getString(R.string.zone_label_text) +
                                " " + garageZone;
                    }
                    if (!garageRow.equals(DEFAULT_STRING)) {
                        toastMessage = toastMessage + ", " +
                                getResources().getString(R.string.row_label_text) +
                                " " + garageRow;
                    }
                    if (!garageSpot.equals(DEFAULT_STRING)) {
                        toastMessage = toastMessage + ", " +
                                getResources().getString(R.string.spot_number_label) +
                                " " + garageSpot;
                    }
                    if (!savedNote.equals(DEFAULT_STRING)) {
                        toastMessage = toastMessage + "\n" + savedNote;
                    }
               } else {
                    if (!savedNote.equals(DEFAULT_STRING)) {
                        toastMessage = savedNote;
                    }
                }

                if (!toastMessage.equals("")) {
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
                }
            }
        });

        checkLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                buildGoogleApiClient();
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,
                        new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.d(TAG, "The retrieved location is null.");
                            return;
                        }
                        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Fused Location provider obtained the " + location.getLongitude() +
                                " longitude and " + location.getLatitude() + " latitude");
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        locationPermissionGranted = true;
                        getDeviceLocation();
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));

                        Toast.makeText(this, R.string.location_permission_denied_message,
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
        updateLocationUI();

    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                currentPosition = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        deleteLocationItem = menu.findItem(R.id.delete_location);
        addNoteItem = menu.findItem(R.id.add_note);
        shareLocationItem = menu.findItem(R.id.share_location);
        navigateItem = menu.findItem(R.id.navigate);
        if (savedLocation == null) {
            disableMenuItems();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (item.getItemId()) {
            case R.id.save_location:
                if (currentPosition != null) {
                    editor.clear();
                    editor.putString(VEHICLE_LOCATION_LAT_KEY, String.valueOf(currentPosition.latitude));
                    editor.putString(VEHICLE_LOCATION_LNG_KEY, String.valueOf(currentPosition.longitude));
                    editor.commit();
                    markerOptions.position(currentPosition);
                    if (vehiclePositionMarker != null) {
                        vehiclePositionMarker.remove();
                        vehiclePositionMarker = mMap.addMarker(markerOptions);
                    }
                    enableMenuItems();
                } else {
                    Toast.makeText(this,
                            getResources().getString(R.string.current_position_null_message),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.delete_location:
                editor.clear();
                editor.commit();
                if (vehiclePositionMarker != null) {
                    vehiclePositionMarker.remove();
                }
                disableMenuItems();
                break;

            case R.id.add_note:
                Intent intent = new Intent();
                intent.setClass(this, AddNote.class);
                startActivity(intent);
                break;

            case R.id.navigate:
                String stringLat = sharedPreferences.getString(VEHICLE_LOCATION_LAT_KEY, DEFAULT_STRING);
                String stringLng = sharedPreferences.getString(VEHICLE_LOCATION_LNG_KEY, DEFAULT_STRING);
                if (stringLat.equals(DEFAULT_STRING) || stringLng.equals(DEFAULT_STRING)) {
                    Toast.makeText(this,
                            getResources().getString(R.string.navigation_error_message),
                            Toast.LENGTH_SHORT).show();
                }
                Double latitude = Double.parseDouble(stringLat);
                Double longitude = Double.parseDouble(stringLng);
                Uri intentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=w");
                Intent navigationIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                navigationIntent.setPackage("com.google.android.apps.maps");
                if (navigationIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(navigationIntent);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_navigation_app_installed_message),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.share_location:

                stringLat = sharedPreferences.getString(VEHICLE_LOCATION_LAT_KEY, DEFAULT_STRING);
                stringLng = sharedPreferences.getString(VEHICLE_LOCATION_LNG_KEY, DEFAULT_STRING);
                if (stringLat.equals(DEFAULT_STRING) || stringLng.equals(DEFAULT_STRING)) {
                    Toast.makeText(this,
                            getResources().getString(R.string.sharing_error_message),
                            Toast.LENGTH_SHORT).show();
                }
                latitude = Double.parseDouble(stringLat);
                longitude = Double.parseDouble(stringLng);

                String uri = "http://maps.google.com/maps?saddr=" +latitude+","+longitude;

                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String subject = getResources().getString(R.string.location_sharing_explanation);
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
                startActivity(Intent.createChooser(shareIntent, getResources()
                        .getString(R.string.share_window_title)));
                break;
        }
        return true;
    }

    private void enableMenuItems() {
        deleteLocationItem.setEnabled(true);
        deleteLocationItem.setIcon(R.drawable.ic_map_marker_off);

        addNoteItem.setEnabled(true);
        addNoteItem.setIcon(R.drawable.ic_clipboard_text);

        shareLocationItem.setEnabled(true);
        shareLocationItem.setIcon(R.drawable.ic_share_variant);

        navigateItem.setEnabled(true);
        navigateItem.setIcon(R.drawable.ic_directions);
    }

    private void disableMenuItems() {
        deleteLocationItem.setEnabled(false);
        deleteLocationItem.setIcon(R.drawable.ic_map_marker_off_disabled);

        addNoteItem.setEnabled(false);
        addNoteItem.setIcon(R.drawable.ic_clipboard_text_disabled);

        shareLocationItem.setEnabled(false);
        shareLocationItem.setIcon(R.drawable.ic_share_variant_disabled);

        navigateItem.setEnabled(false);
        navigateItem.setIcon(R.drawable.ic_directions_disabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveNoteInfo();
    }

    private void retrieveNoteInfo() {
        savedNote = sharedPreferences.getString(AddNote.LOCATION_NOTE_KEY, DEFAULT_STRING);
        isInGarage = sharedPreferences.getBoolean(AddNote.IS_IN_GARAGE_KEY, false);
        if (isInGarage) {
            garageLevel = sharedPreferences.getString(AddNote.LEVEL_KEY, DEFAULT_STRING);
            garageRow = sharedPreferences.getString(AddNote.ROW_KEY, DEFAULT_STRING);
            garageZone = sharedPreferences.getString(AddNote.ZONE_KEY, DEFAULT_STRING);
            garageSpot = sharedPreferences.getString(AddNote.SPOT_NUMBER_KEY, DEFAULT_STRING);
        }
    }
    private void retrieveSavedLocation() {
        String latitudeString = sharedPreferences.getString(VEHICLE_LOCATION_LAT_KEY, DEFAULT_STRING);
        String longitudeString = sharedPreferences.getString(VEHICLE_LOCATION_LNG_KEY, DEFAULT_STRING);
        if (!latitudeString.equals(DEFAULT_STRING) && !longitudeString.equals(DEFAULT_STRING)) {
            double latitude = Double.valueOf(latitudeString);
            double longitude = Double.valueOf(longitudeString);
            savedLocation = new LatLng(latitude, longitude);
        }
    }

    private static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }
}
