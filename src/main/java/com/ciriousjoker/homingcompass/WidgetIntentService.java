package com.ciriousjoker.homingcompass;


import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;


public class WidgetIntentService extends Service {
    //private static final String TAG = "WidgetIntentService";

    static WidgetUpdateThread myCustomThread;

    @Override
    public void onCreate() {
        //Log.i(TAG, "Service created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(hasPermission()) {
            myCustomThread = new WidgetUpdateThread(getApplicationContext());

            if(!WidgetUpdateThread.isRunning) {
                myCustomThread.start();
            }

            try {
                myCustomThread.join();
            } catch (Exception ignored) { }
        }
        return Service.START_NOT_STICKY;
    }

    private boolean hasPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent intentStartPermissionActivity = new Intent(WidgetIntentService.this, PermissionActivity.class);
            intentStartPermissionActivity.putExtra(getString(R.string.key_permission_start_service), true);
            intentStartPermissionActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentStartPermissionActivity);
            return false;
        } else {
            return true;
        }
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
