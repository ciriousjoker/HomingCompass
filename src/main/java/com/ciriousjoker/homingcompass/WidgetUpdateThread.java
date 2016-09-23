package com.ciriousjoker.homingcompass;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class WidgetUpdateThread extends Thread implements Runnable {

    public static String MY_PREFS_FILE;
    static boolean isRunning;
    private final Handler handler;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleApiClient.ConnectionCallbacks connectionCallbacks;
    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener;
    private SensorEventListener sensorEventListener;
    private String KEY_DISTANCE;
    private String KEY_ROTATION;
    private SensorManager sensorManager;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Location homeLocation = new Location("");
    private Location lastLocation;
    private float distanceInMeters;
    private String TAG;
    private Context c;
    private boolean locationToastShown = false;
    private LocationListener locationListener;

    public WidgetUpdateThread(Context context) {
        c = context;

        handler = new Handler();

        TAG = "CustomThread_" + System.currentTimeMillis();
        KEY_DISTANCE = c.getString(R.string.key_widget_distance);
        KEY_ROTATION = c.getString(R.string.key_widget_rotation);
        MY_PREFS_FILE = c.getString(R.string.shared_pref_file);

        prefs = c.getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
    }

    static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public static String formatDistance(Context context, SharedPreferences prefs, double distance) {
        String formattedString = "";
        long formattedDistance;

        int format_choice = prefs.getInt(context.getString(R.string.shared_pref_setting_format), 0);

        switch (format_choice) {
            case 0:
                if (distance >= 1000) {
                    formattedDistance = Math.round(distance / 1000);
                    formattedString = String.valueOf(formattedDistance) + context.getString(R.string.format_kilometer);
                } else {
                    formattedDistance = Math.round(distance);
                    formattedString = String.valueOf(formattedDistance) + context.getString(R.string.format_meter);
                }
                break;
            case 1:
                if (distance >= 1609) {
                    formattedDistance = Math.round(distance / 1609);
                    formattedString = String.valueOf(formattedDistance) + context.getString(R.string.format_miles);
                } else {
                    formattedDistance = Math.round(distance * 3.28084);
                    formattedString = String.valueOf(formattedDistance) + context.getString(R.string.format_feet);
                }
                break;
            default:
        }

        return formattedString;
    }

    @NonNull
    public static Double getLastDistance(Context context, SharedPreferences prefs) {
        return getDouble(prefs, context.getString(R.string.shared_pref_last_distance), 0);
    }

    private GoogleApiClient.OnConnectionFailedListener createOnConnectionFailedListener() {
        if(onConnectionFailedListener == null) {
            onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                }
            };
        }
        return onConnectionFailedListener;
    }

    private GoogleApiClient.ConnectionCallbacks createConnectionCallback() {
        if(connectionCallbacks == null) {
            connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    //Log.i(TAG, "Connected to the Google API");

                    if (hasPermission()) {
                        try {
                            if (!prefs.getBoolean(c.getString(R.string.shared_pref_setting_constant_location_updates), true)) {
                                lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                if (lastLocation == null) {
                                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, createLocationListener());
                                }// else {
                                //Log.i(TAG, "getLastLocation returned: " + lastLocation.getLatitude() + " / " + lastLocation.getLongitude());
                                //}
                            } else {
                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, createLocationListener());
                            }
                        } catch (SecurityException e) {
                            //Log.i(TAG, "FusedLocationApi requestLocationUpdates failed: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            };
        }
        return connectionCallbacks;
    }

    private LocationListener createLocationListener() {
        if(locationListener == null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //if(Looper.myLooper() == Looper.getMainLooper()) {
                    //Log.i(TAG, "onLocationChanged()");
                    //}

                    homeLocation = getHomeLocation();
                    if(homeLocation == null) {
                        quit();
                        return;
                    }

                    setCurrentLocation(location);

                    if(!prefs.getBoolean(c.getString(R.string.shared_pref_setting_constant_location_updates), true)) {
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, createLocationListener());
                    }
                }
            };
        }
        return locationListener;
    }

    @Nullable
    private Location getHomeLocation() {
        prefs = c.getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);

        MyLocationItem myLocationItems = returnLocations().get(prefs.getInt(c.getString(R.string.shared_pref_widget_location), 0));

        homeLocation.setLatitude(myLocationItems.getLatitude());
        homeLocation.setLongitude(myLocationItems.getLongitude());

        if(homeLocation.getLatitude() == 0.0 && homeLocation.getLongitude() == 0.0) {
            showToast(c.getString(R.string.notice_no_location_set));

            Intent intentStartSettingsActivity = new Intent(c, SettingsActivity.class);
            intentStartSettingsActivity.putExtra(c.getString(R.string.key_permission_start_service), true);
            intentStartSettingsActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(intentStartSettingsActivity);
            return null;
        }

        return homeLocation;
    }

    @Override
    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;

            LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showToast(c.getString(R.string.notice_gps_deactivated));
                launchLocationSettingsIntent();
                quit();
            } else {
                super.start();
            }
        }
    }

    @Override
    public void run() {
        super.run();
        initializeWidget();

        if (!hasPermission()) {
            quit();
            return;
        }

        sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);


        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addConnectionCallbacks(createConnectionCallback())
                .addOnConnectionFailedListener(createOnConnectionFailedListener())
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2 * 1000)
                .setFastestInterval(1000);

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);



        mGoogleApiClient.connect();
        sensorManager.registerListener(createSensorEventListener(), sensor, SensorManager.SENSOR_DELAY_UI);

        final long initTime = System.currentTimeMillis();
        Runnable runnableCleanup = new Runnable() {
            @Override
            public void run() {
                prefs = c.getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
                int widgetUpdateDuration = prefs.getInt(c.getString(R.string.shared_pref_setting_widget_update_duration), 5) * 60000;
                if (System.currentTimeMillis() > (initTime + widgetUpdateDuration)) {
                    quit();
                } else {
                    if(isRunning) {
                        handler.postDelayed(this, 5000);
                    }
                }
            }
        };

        if(isRunning) {
            handler.postDelayed(runnableCleanup, 5000);
        }
    }

    private SensorEventListener createSensorEventListener() {
        if(sensorEventListener == null) {
            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {

                    homeLocation = getHomeLocation();
                    if(homeLocation == null) {
                        quit();
                        return;
                    }

                    Location currentLocation = getCurrentLocation();
                    if(currentLocation == null) {
                        if(!locationToastShown) {
                            showToast(c.getString(R.string.notice_location_first_time));
                            locationToastShown = true;
                        }
                        return;
                    }


                    distanceInMeters = currentLocation.distanceTo(homeLocation);

                    float directionInDegree = currentLocation.bearingTo(homeLocation);

                    GeomagneticField geomagneticField = new GeomagneticField(
                            Double.valueOf(currentLocation.getLatitude()).floatValue(),
                            Double.valueOf(currentLocation.getLongitude()).floatValue(),
                            Double.valueOf(currentLocation.getAltitude()).floatValue(),
                            System.currentTimeMillis()
                    );

                    directionInDegree += geomagneticField.getDeclination();

                    int widget_degree = calculateNeedleHeading(Math.round(sensorEvent.values[0]), directionInDegree);
                    String widget_distance = formatDistance(c, prefs, distanceInMeters);

                    updateWidget(widget_degree, widget_distance);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };
        }
        return sensorEventListener;
    }

    private void updateWidget(int widget_degree, String widget_distance) {
        Intent intentUpdateWidget = new Intent(c, WidgetProvider.class);
        intentUpdateWidget.setAction(AppWidgetManager.EXTRA_CUSTOM_EXTRAS);
        intentUpdateWidget.putExtra(KEY_ROTATION, widget_degree);
        intentUpdateWidget.putExtra(KEY_DISTANCE, widget_distance);
        c.sendBroadcast(intentUpdateWidget);
    }

    private Location getCurrentLocation() {
        Location location = new Location("");

        if(lastLocation == null) {
            location.setLatitude(getDouble(prefs, c.getString(R.string.shared_pref_last_latitude), 0.0));
            location.setLongitude(getDouble(prefs, c.getString(R.string.shared_pref_last_longitude), 0.0));
            location.setAltitude(getDouble(prefs, c.getString(R.string.shared_pref_last_altitude), 0.0));
        } else {
            location = lastLocation;
        }

        if(location.getLatitude() == 0.0 && location.getLongitude() == 0.0 && location.getAltitude() == 0.0) {
            return null;
        }
        return location;
    }

    private void setCurrentLocation(Location location) {
        if (lastLocation == null) {
            editor = prefs.edit();
            putDouble(editor, c.getString(R.string.shared_pref_last_latitude), location.getLatitude());
            putDouble(editor, c.getString(R.string.shared_pref_last_longitude), location.getLongitude());
            putDouble(editor, c.getString(R.string.shared_pref_last_altitude), location.getAltitude());
            editor.apply();
        }
        lastLocation = location;
    }

    public void quit() {
        if(sensorManager != null) {
            sensorManager.unregisterListener(createSensorEventListener());
        }

        if(mGoogleApiClient != null) {
            if(mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, createLocationListener());
                mGoogleApiClient.disconnect();
            }
        }

        if(lastLocation != null) {
            editor = prefs.edit();
            putDouble(editor, c.getString(R.string.shared_pref_last_latitude), lastLocation.getLatitude());
            putDouble(editor, c.getString(R.string.shared_pref_last_longitude), lastLocation.getLongitude());
            putDouble(editor, c.getString(R.string.shared_pref_last_altitude), lastLocation.getAltitude());
            editor.apply();
        }
        isRunning = false;
    }

    private void initializeWidget() {
        Intent intent = new Intent(c, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = {R.xml.widget_info};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        c.sendBroadcast(intent);
    }

    private void launchLocationSettingsIntent() {
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(locationSettingsIntent);
    }

    public void showToast(String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }

    private boolean hasPermission() {
        return !(ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    @Contract(pure = true)
    private int calculateNeedleHeading(float heading, float direction) {
        float needle_degree;

        float adjusted_direction = 360 + direction;
        while( adjusted_direction >= 360 ){
            adjusted_direction -= 360;
        }

        needle_degree = 360 + adjusted_direction - heading;
        while( needle_degree >= 360 ){
            needle_degree -= 360;
        }
        return Math.round(needle_degree);
    }

    private ArrayList<MyLocationItem> returnLocations() {
        SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.shared_pref_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(c.getString(R.string.shared_pref_my_locations_file), "");
        Type listOfObjects = new TypeToken<ArrayList<MyLocationItem>>() {
        }.getType();

        ArrayList<MyLocationItem> myLocationItems = gson.fromJson(json, listOfObjects);
        if (myLocationItems == null) {
            myLocationItems = new ArrayList<>();
        }

        MyLocationItem item = new MyLocationItem(c.getString(R.string.maps_marker_home_title));
        item.setLatitude(getDouble(prefs, c.getString(R.string.shared_pref_home_latitude), 0.0));
        item.setLongitude(getDouble(prefs, c.getString(R.string.shared_pref_home_longitude), 0.0));

        myLocationItems.add(0, item);

        return myLocationItems;
    }
}