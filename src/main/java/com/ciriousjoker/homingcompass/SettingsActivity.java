package com.ciriousjoker.homingcompass;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class SettingsActivity extends AppCompatActivity {

    final static int REQUEST_CODE_ASK_INITIAL = 100;
    final static int REQUEST_CODE_LOAD_MY_LOCATIONS = 101;
    public static  String MY_PREFS_FILE;
    private static String KEY_DISTANCE;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;

    //static final String TAG = "SettingsActivity";

    private SeekBar seekBar_WidgetDuration;
    private TextView textView_SeekBarDescription;
    private TextView textView_Format;
    private Switch switch_ShowDistance;
    private LinearLayout linearLayout_Format;
    private Switch switch_ShowSettingsButton;

    private Switch switch_UpdateLocationConstantly;
    private TextView textView_UpdateLocationConstantly;
    private TextView textView_WhichLocation_Description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MY_PREFS_FILE = getString(R.string.shared_pref_file);
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = prefs.edit();

        int runCount = prefs.getInt(getString(R.string.shared_pref_flag_app_opened_previously), 0);

        if(runCount == 0) {
            Intent startIntroduction = new Intent(SettingsActivity.this, IntroActivity.class);
            startActivity(startIntroduction);
        } else if (runCount > 1) {
            // Show notifications about widgets
            int widgetIds[] = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
            if(widgetIds.length < 1){
                showCardNotification(getString(R.string.notice_no_widget), 1);
            } else if(widgetIds.length > 1){
                showCardNotification(getString(R.string.notice_too_many_widget, widgetIds.length), 2);
            }

            LatLng home_location = new LatLng(getDouble(prefs, getString(R.string.shared_pref_home_latitude), 0.0), getDouble(prefs, getString(R.string.shared_pref_home_longitude), 0.0));
            if(home_location.latitude == 0.0 && home_location.longitude == 0.0) {
                showCardNotification(getString(R.string.notice_map_preview_hint), 3);
            }
        }
        editor.putInt(getString(R.string.shared_pref_flag_app_opened_previously), ++runCount);
        editor.apply();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);



        // Pushing MapView Fragment
        Fragment fragment = Fragment.instantiate(this, MapsPreview.class.getName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();

        KEY_DISTANCE = getString(R.string.key_widget_distance);

        setup_Settings();
        updateWidget();


        ImageButton buttonMyLocations = (ImageButton) findViewById(R.id.buttonMyLocations);
        buttonMyLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMyLocationsActivity();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        setup_Settings();
    }

    private void updateWidget() {
        Intent intent = new Intent(getApplicationContext(), WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = {R.xml.widget_info};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intentStartAboutScreen = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intentStartAboutScreen);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setup_Settings() {
        setup_Button_UsedLocation();
        setup_Slider_WidgetDuration();
        setup_Switch_ShowDistance();
        setup_Button_UnitSystem();
        setup_Switch_ShowSettingsButton();
        setup_Switch_LocationType();
    }

    private void setup_Switch_LocationType() {
        switch_UpdateLocationConstantly = (Switch) findViewById(R.id.switch_UpdateLocationConstantly);
        textView_UpdateLocationConstantly = (TextView) findViewById(R.id.textView_UpdateLocationConstantly);

        switch_UpdateLocationConstantly.setChecked(prefs.getBoolean(getString(R.string.shared_pref_setting_constant_location_updates), true));

        if (switch_UpdateLocationConstantly.isChecked()) {
            textView_UpdateLocationConstantly.setText(R.string.settings_switch_update_location_constantly);
        } else {
            textView_UpdateLocationConstantly.setText(R.string.settings_switch_update_location_once);
        }

        switch_UpdateLocationConstantly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switch_UpdateLocationConstantly.isChecked()) {
                    textView_UpdateLocationConstantly.setText(R.string.settings_switch_update_location_constantly);
                } else {
                    textView_UpdateLocationConstantly.setText(R.string.settings_switch_update_location_once);
                }
                editor.putBoolean(getString(R.string.shared_pref_setting_constant_location_updates), switch_UpdateLocationConstantly.isChecked());
                editor.apply();
            }
        });
    }

    private void setup_Switch_ShowSettingsButton() {
        switch_ShowSettingsButton = (Switch) findViewById(R.id.switch_ShowSettingsButton);
        switch_ShowSettingsButton.setChecked(prefs.getBoolean(getString(R.string.shared_pref_setting_show_settings_button), true));
        switch_ShowSettingsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(getString(R.string.shared_pref_setting_show_settings_button), b);
                editor.apply();
                updateWidget();
            }
        });
    }

    private void setup_Button_UnitSystem() {
        textView_Format = (TextView) findViewById(R.id.textView_FormatDescription);
        textView_Format.setText(getResources().getStringArray(R.array.settings_format_options)[prefs.getInt(getString(R.string.shared_pref_setting_format), 0)]);

        LinearLayout linearLayout_Button_Format = (LinearLayout) findViewById(R.id.textView_Format);
        linearLayout_Button_Format.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFormatDialog();
            }
        });

    }

    private void setup_Switch_ShowDistance() {
        linearLayout_Format = (LinearLayout) findViewById(R.id.linearLayout_Format);
        switch_ShowDistance = (Switch) findViewById(R.id.switch_ShowDistance);

        switch_ShowDistance.setChecked(prefs.getBoolean(getString(R.string.shared_pref_setting_show_distance), false));
        if(!switch_ShowDistance.isChecked()){
            linearLayout_Format.setVisibility(View.GONE);
            View divider = findViewById(R.id.divider_after_unit_system);
            divider.setVisibility(View.GONE);
        }

        switch_ShowDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                View divider = findViewById(R.id.divider_after_unit_system);
                if(b){
                    linearLayout_Format.setVisibility(View.VISIBLE);
                    divider.setVisibility(View.VISIBLE);
                } else {
                    linearLayout_Format.setVisibility(View.GONE);
                    divider.setVisibility(View.GONE);
                }
                editor.putBoolean(getString(R.string.shared_pref_setting_show_distance), b);
                editor.apply();

                Intent intentSendDistanceToWidget = new Intent(SettingsActivity.this, WidgetProvider.class);
                intentSendDistanceToWidget.setAction(AppWidgetManager.EXTRA_CUSTOM_EXTRAS);
                intentSendDistanceToWidget.putExtra(KEY_DISTANCE, WidgetUpdateThread.formatDistance(getApplicationContext(), prefs, WidgetUpdateThread.getLastDistance(getApplicationContext(), prefs)));
                sendBroadcast(intentSendDistanceToWidget);

                updateWidget();
            }
        });
    }

    private void setup_Slider_WidgetDuration() {
        textView_SeekBarDescription = (TextView) findViewById(R.id.textView_SeekBarDescription);
        seekBar_WidgetDuration = (SeekBar) findViewById(R.id.seekBar_widget_duration);

        int widgetUpdateDuration = prefs.getInt(getString(R.string.shared_pref_setting_widget_update_duration), 5);

        updateWidgetUpdateSeekBarDescription(widgetUpdateDuration - 1);
        seekBar_WidgetDuration.setProgress(widgetUpdateDuration - 1);

        // Set listener to save any changes to the widget update duration
        seekBar_WidgetDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateWidgetUpdateSeekBarDescription(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt(getString(R.string.shared_pref_setting_widget_update_duration), seekBar.getProgress() + 1);
                editor.apply();
            }
        });
    }

    private void setup_Button_UsedLocation() {
        int previousLocationChoice = prefs.getInt(getString(R.string.shared_pref_widget_location), 0);
        CharSequence[] myLocationsArrayList = returnLocations();
        CharSequence usedLocation = myLocationsArrayList[previousLocationChoice];

        LinearLayout textView_WhichLocation = (LinearLayout) findViewById(R.id.textView_WhichLocation);
        textView_WhichLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentStartMyLocationsDialog = new Intent(SettingsActivity.this, WidgetSettingsDialog.class);
                intentStartMyLocationsDialog.putExtra(getString(R.string.key_intent_show_settings_button), false);
                startActivity(intentStartMyLocationsDialog);
            }
        });

        textView_WhichLocation_Description = (TextView) findViewById(R.id.textView_WhichLocation_Description);
        textView_WhichLocation_Description.setText(usedLocation);
    }

    private void showFormatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_dialog_format);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.settings_format_options), prefs.getInt(getString(R.string.shared_pref_setting_format), 0), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(getString(R.string.shared_pref_setting_format), which);
                editor.apply();

                Intent intentSendDistanceToWidget = new Intent(SettingsActivity.this, WidgetProvider.class);
                intentSendDistanceToWidget.setAction(AppWidgetManager.EXTRA_CUSTOM_EXTRAS);
                intentSendDistanceToWidget.putExtra(KEY_DISTANCE, WidgetUpdateThread.formatDistance(getApplicationContext(), prefs, WidgetUpdateThread.getLastDistance(getApplicationContext(), prefs)));
                sendBroadcast(intentSendDistanceToWidget);

                updateWidget();
                textView_Format.setText(getResources().getStringArray(R.array.settings_format_options)[which]);
                dialog.dismiss();

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void updateWidgetUpdateSeekBarDescription(int i) {
        String description = (i + 1) + " min";
        textView_SeekBarDescription.setText(description);
    }

    private void showCardNotification(String cardText, final int cardType) {
        if(!prefs.getBoolean(getString(R.string.shared_pref_flag_notice_dismissed) + "_" + cardType, false)){
            final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.settings_CardView_Layout);
            final CardView card = (CardView) LayoutInflater.from(this).inflate(R.layout.card_layout, linearLayout, false);
            TextView textView = (TextView) card.getChildAt(0);
            textView.setText(cardText);

            linearLayout.addView(card, 1);
            textView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                    int width = displayMetrics.widthPixels;

                    card.animate()
                            .translationX(-width)
                            .alpha(0)
                            .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    linearLayout.removeView(card);
                                    editor.putBoolean(getString(R.string.shared_pref_flag_notice_dismissed) + "_" + cardType, true);
                                    editor.apply();
                                }
                            });
                    return true;
                }
            });
        }
    }

    private void launchMyLocationsActivity() {
        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, REQUEST_CODE_LOAD_MY_LOCATIONS);
            }
        } else {
            Intent intentStartMyLocations = new Intent(SettingsActivity.this, MyLocationsActivity.class);
            startActivity(intentStartMyLocations);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_INITIAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SettingsActivity.this, R.string.notice_no_permission, Toast.LENGTH_LONG).show();
                }
                return;
            }
            case REQUEST_CODE_LOAD_MY_LOCATIONS: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SettingsActivity.this, R.string.notice_no_permission, Toast.LENGTH_LONG).show();
                } else {
                    Intent intentStartMyLocations = new Intent(SettingsActivity.this, MyLocationsActivity.class);
                    startActivity(intentStartMyLocations);
                }
            }
        }
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                SharedPreferences prefs = getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
                LatLng home_location = new LatLng(getDouble(prefs, getString(R.string.shared_pref_home_latitude), 0.0), getDouble(prefs, getString(R.string.shared_pref_home_longitude), 0.0));

                if (home_location.latitude != 0.0 || home_location.longitude != 0.0) {
                    if (data.getIntExtra(getString(R.string.key_intent_location_id), -1) == -1) {
                        Toast.makeText(this, R.string.notice_general_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.notice_no_location_set), Toast.LENGTH_SHORT).show();
                }
            }
        }
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

    public void showInfoDialog(View view) {
        int messageId = Integer.valueOf((String) view.getTag());
        String messageText = getResources().getStringArray(R.array.settings_info_dialog)[messageId];


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
        alertDialog.show();
    }
}