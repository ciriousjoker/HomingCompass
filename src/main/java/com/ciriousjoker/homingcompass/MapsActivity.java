package com.ciriousjoker.homingcompass;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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

    private static final String TAG = "MapActivity";
    public static String MY_PREFS_FILE;
    private GoogleMap googleMap;
    private Marker homeMarker;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        MY_PREFS_FILE = getString(R.string.shared_pref_file);

        // Initialize toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.maps_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        intent = getIntent();

        // Initialize mapFragment
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        UiSettings uiSettings = googleMap.getUiSettings();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
        LatLng zoom_location = new LatLng(intent.getDoubleExtra(getString(R.string.key_intent_latitude), 0.0), intent.getDoubleExtra(getString(R.string.key_intent_longitude), 0.0));
        if (zoom_location.latitude != 0.0 || zoom_location.longitude != 0.0) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(zoom_location).zoom(12).build();

            homeMarker = googleMap.addMarker(new MarkerOptions().position(zoom_location).title(intent.getStringExtra(getString(R.string.key_intent_marker_title))).snippet(intent.getStringExtra(getString(R.string.key_intent_marker_snippet))));
            homeMarker.showInfoWindow();

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
        if(!prefs.getBoolean(getString(R.string.shared_pref_flag_maps_notice_dismissed), false)) {
            showInfoDialog();
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
        homeMarker = googleMap.addMarker(new MarkerOptions().position(point).title(intent.getStringExtra(getString(R.string.key_intent_marker_title))).snippet(intent.getStringExtra(getString(R.string.key_intent_marker_snippet))));
        homeMarker.showInfoWindow();

        // Save chosen location
        if (intent.getIntExtra(getString(R.string.key_intent_location_id), -1) == -1) {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
            putDouble(editor, getString(R.string.shared_pref_home_latitude), homeMarker.getPosition().latitude);
            putDouble(editor, getString(R.string.shared_pref_home_longitude), homeMarker.getPosition().longitude);
            editor.apply();
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void finish() {
        Intent data = new Intent();

        if (homeMarker != null) {
            data.putExtra(getString(R.string.key_intent_latitude), homeMarker.getPosition().latitude);
            data.putExtra(getString(R.string.key_intent_longitude), homeMarker.getPosition().longitude);
        } else {
            data.putExtra(getString(R.string.key_intent_latitude), 0.0);
            data.putExtra(getString(R.string.key_intent_longitude), 0.0);
        }

        data.putExtra(getString(R.string.key_intent_location_id), intent.getIntExtra(getString(R.string.key_intent_location_id), -1));
        setResult(RESULT_OK, data);

        super.finish();
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public void showInfoDialog() {
        String messageText = getResources().getString(R.string.maps_dialog_tutorial);


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        builder.setTitle(R.string.title_dialog_help);
        builder.setMessage(messageText);
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.button_dont_show_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                SharedPreferences.Editor prefs = getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE).edit();
                prefs.putBoolean(getString(R.string.shared_pref_flag_maps_notice_dismissed), true);
                prefs.apply();
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

}