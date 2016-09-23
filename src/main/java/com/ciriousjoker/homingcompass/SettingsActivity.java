package com.ciriousjoker.homingcompass;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


public class SettingsActivity extends AppCompatActivity {

    private static String KEY_DISTANCE;

    final static int REQUEST_CODE_ASK_INITIAL = 100;
    final static int REQUEST_CODE_LOAD_MAP = 101;
    public static  String MY_PREFS_FILE;

    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;

    //static final String TAG = "SettingsActivity";

    SeekBar seekBar_WidgetDuration;
    TextView textView_SeekBarDescription;
    TextView textView_Format;
    Switch switch_ShowDistance;
    LinearLayout textView_Format_Layout;
    Switch switch_ShowSettingsButton;

    Switch switch_UpdateLocationConstantly;
    TextView textView_UpdateLocationConstantly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MY_PREFS_FILE = getString(R.string.shared_pref_filename);
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

        KEY_DISTANCE = getString(R.string.widget_key_distance);

        loadSettings();
        updateWidget();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        loadSettings();
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

    private void loadSettings() {
        // Load the widget duration
        textView_SeekBarDescription = (TextView) findViewById(R.id.textView_SeekBarDescription);
        seekBar_WidgetDuration = (SeekBar) findViewById(R.id.seekBar_widget_duration);
        textView_Format_Layout = (LinearLayout) findViewById(R.id.textView_Format);

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

        // Load ShowDistance switch
        switch_ShowDistance = (Switch) findViewById(R.id.switch_ShowDistance);

        switch_ShowDistance.setChecked(prefs.getBoolean(getString(R.string.shared_pref_setting_show_distance), false));
        if(!switch_ShowDistance.isChecked()){
            textView_Format_Layout.setVisibility(View.GONE);
            View divider = findViewById(R.id.divider_after_unit_system);
            divider.setVisibility(View.GONE);
        }

        switch_ShowDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                View divider = findViewById(R.id.divider_after_unit_system);
                if(b){
                    textView_Format_Layout.setVisibility(View.VISIBLE);
                    divider.setVisibility(View.VISIBLE);
                } else {
                    textView_Format_Layout.setVisibility(View.GONE);
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

        // Unit system
        textView_Format = (TextView) textView_Format_Layout.getChildAt(1);
        textView_Format.setText(getResources().getStringArray(R.array.settings_format_options)[prefs.getInt(getString(R.string.shared_pref_setting_format), 0)]);
        textView_Format_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFormatDialog();
            }
        });

        // Load ShowSettingsButton switch
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


        // Load UpdateLocationConstantly switch
        switch_UpdateLocationConstantly = (Switch) findViewById(R.id.switch_UpdateLocationConstantly);
        textView_UpdateLocationConstantly = (TextView) findViewById(R.id.textView_UpdateLocationConstantly);

        switch_UpdateLocationConstantly.setChecked(prefs.getBoolean(getString(R.string.shared_pref_setting_constant_location_updates), true));

        if(switch_UpdateLocationConstantly.isChecked()) {
            textView_UpdateLocationConstantly.setText(R.string.settings_switch_update_location_constantly);
        } else {
            textView_UpdateLocationConstantly.setText(R.string.settings_switch_update_location_once);
        }

        switch_UpdateLocationConstantly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(switch_UpdateLocationConstantly.isChecked()) {
                    textView_UpdateLocationConstantly.setText(R.string.settings_switch_update_location_constantly);
                } else {
                    textView_UpdateLocationConstantly.setText(R.string.settings_switch_update_location_once);
                }
                editor.putBoolean(getString(R.string.shared_pref_setting_constant_location_updates), switch_UpdateLocationConstantly.isChecked());
                editor.apply();
            }
        });
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
            case  REQUEST_CODE_LOAD_MAP: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SettingsActivity.this, R.string.notice_no_permission, Toast.LENGTH_LONG).show();
                } else {
                    Intent intentStartMapChooser = new Intent(SettingsActivity.this, MapsActivity.class);
                    startActivity(intentStartMapChooser);
                }
            }
        }
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}