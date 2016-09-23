package com.ciriousjoker.homingcompass;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class WidgetSettingsDialog extends Activity {
    final static int REQUEST_CODE_LOAD_MY_LOCATIONS = 101;
    private SharedPreferences.Editor editor;
    private AlertDialog alertDialog;
    private boolean waiting_for_permission_result = false;

    @Override
    protected void onDestroy() {
        try {
            alertDialog.dismiss();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        try {
            alertDialog.dismiss();
        } catch (Exception ignored) {
        }
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.dialog_widget_settings)

        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_file), MODE_PRIVATE);
        editor = prefs.edit();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_my_locations));

        int previousLocationChoice = prefs.getInt(getString(R.string.shared_pref_widget_location), 0);
        builder.setSingleChoiceItems(returnLocations(), previousLocationChoice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(getString(R.string.shared_pref_widget_location), which);
                editor.apply();
                dialog.dismiss();
            }
        });

        alertDialog = builder.create();

        if (getIntent().getBooleanExtra(getString(R.string.key_intent_show_settings_button), true)) {
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.button_open_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intentStartMyLocationsDialog = new Intent(WidgetSettingsDialog.this, SettingsActivity.class);
                    startActivity(intentStartMyLocationsDialog);
                    dialogInterface.dismiss();
                }
            });
        }

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_add_location), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                launchMyLocationsActivity();
            }
        });

        alertDialog.show();

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (!waiting_for_permission_result) {
                    finish();
                }
            }
        });
    }

    private CharSequence[] returnLocations() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_file), MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(getString(R.string.shared_pref_my_locations_file), "");
        Type listOfObjects = new TypeToken<ArrayList<MyLocationItem>>() {
        }.getType();

        ArrayList<MyLocationItem> myLocationItems = gson.fromJson(json, listOfObjects);
        if (myLocationItems == null) {
            myLocationItems = new ArrayList<>();
        }

        ArrayList<String> arrayList_MyLocations = new ArrayList<>();
        arrayList_MyLocations.add(getString(R.string.maps_marker_home_title));
        for (MyLocationItem object : myLocationItems) {
            arrayList_MyLocations.add(object.getName());
        }
        return arrayList_MyLocations.toArray(new CharSequence[arrayList_MyLocations.size()]);
    }

    private void launchMyLocationsActivity() {
        if (ActivityCompat.checkSelfPermission(WidgetSettingsDialog.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WidgetSettingsDialog.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                waiting_for_permission_result = true;
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, REQUEST_CODE_LOAD_MY_LOCATIONS);
            }
        } else {
            alertDialog.dismiss();
            Intent intentStartMyLocations = new Intent(WidgetSettingsDialog.this, MyLocationsActivity.class);
            startActivity(intentStartMyLocations);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOAD_MY_LOCATIONS: {
                waiting_for_permission_result = false;
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(WidgetSettingsDialog.this, R.string.notice_no_permission, Toast.LENGTH_LONG).show();
                    alertDialog.show();
                } else {
                    Intent intentStartMyLocations = new Intent(WidgetSettingsDialog.this, MyLocationsActivity.class);
                    startActivity(intentStartMyLocations);
                }
            }
        }
    }
}
