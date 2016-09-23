package com.ciriousjoker.homingcompass;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback {

    private GoogleMap googleMap;
    private Marker homeMarker;

    public static String MY_PREFS_FILE;

    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        MY_PREFS_FILE = getString(R.string.shared_pref_filename);

        // Initialize toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.maps_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }


        // Initialize mapFragment
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        UiSettings uiSettings = googleMap.getUiSettings();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.finish();
        } else {
            googleMap.setMyLocationEnabled(true);
        }

        // Set various ui settings
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);

        googleMap.setOnMapLongClickListener(this);

        // Add marker if a previous location was defined
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
        LatLng home_location = new LatLng(getDouble(prefs, getString(R.string.shared_pref_home_latitude), 0.0), getDouble(prefs, getString(R.string.shared_pref_home_longitude), 0.0));
        if(home_location.latitude != 0.0 && home_location.longitude != 0.0) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(home_location).zoom(12).build();

            homeMarker = googleMap.addMarker(new MarkerOptions().position(home_location).title(getString(R.string.maps_marker_title)).snippet(getString(R.string.maps_marker_subtitle)));
            homeMarker.showInfoWindow();

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            // Show snackBarHint
            Snackbar snackBarHint = Snackbar.make(findViewById(R.id.mapCoordinatorLayout), R.string.notice_map_hint, Snackbar.LENGTH_LONG);
            View view = snackBarHint.getView();
            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            snackBarHint.show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng point) {
        // Remove previous markers
        if(homeMarker != null) {
            homeMarker.remove();
        }

        // Set new marker
        homeMarker = googleMap.addMarker(new MarkerOptions().position(point).title(getString(R.string.maps_marker_title)).snippet(getString(R.string.maps_marker_subtitle)));
        homeMarker.showInfoWindow();

        // Save chosen location
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        putDouble(editor, getString(R.string.shared_pref_home_latitude), homeMarker.getPosition().latitude);
        putDouble(editor, getString(R.string.shared_pref_home_longitude), homeMarker.getPosition().longitude);
        editor.apply();

        Log.i(TAG, "Location saved: " + homeMarker.getPosition().latitude + " / " + homeMarker.getPosition().longitude);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}
