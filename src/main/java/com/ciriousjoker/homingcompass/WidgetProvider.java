package com.ciriousjoker.homingcompass;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;


public class WidgetProvider extends AppWidgetProvider{

    public static String MY_PREFS_FILE;
    private static String KEY_DISTANCE;
    private static String KEY_ROTATION;
    String widgetStringDistance;
    //private static final String TAG = "WidgetProvider";
    private int SettingsButton_Visibility;
    private int Distance_Visibility;

    private RemoteViews remoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Log.i(TAG, "onUpdate()");
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        KEY_DISTANCE = context.getString(R.string.key_widget_distance);
        KEY_ROTATION = context.getString(R.string.key_widget_rotation);
        MY_PREFS_FILE = context.getString(R.string.shared_pref_file);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        adjustVisibility(context, remoteViews);

        // Register onClickListeners
        remoteViews.setOnClickPendingIntent(R.id.widget_button_settings, PendingIntent.getActivity(context, 0, new Intent(context, WidgetSettingsDialog.class), 0));
        remoteViews.setOnClickPendingIntent(R.id.widget_background, PendingIntent.getService(context, 0, new Intent(context, WidgetIntentService.class), 0));
        remoteViews.setOnClickPendingIntent(R.id.widget_needle, PendingIntent.getService(context, 0, new Intent(context, WidgetIntentService.class), 0));


        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        updateWidgetNow(context, remoteViews);
    }

    private void adjustVisibility(Context context, RemoteViews rm) {

        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
        if (prefs.getBoolean(context.getString(R.string.shared_pref_setting_show_settings_button), true)){
            SettingsButton_Visibility = View.VISIBLE;
        } else {
            SettingsButton_Visibility = View.GONE;
        }

        if (prefs.getBoolean(context.getString(R.string.shared_pref_setting_show_distance), false)){
            Distance_Visibility = View.VISIBLE;
        } else {
            Distance_Visibility = View.GONE;
        }


        rm.setViewVisibility(R.id.widget_button_settings, SettingsButton_Visibility);
        rm.setViewVisibility(R.id.widget_distance, Distance_Visibility);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        adjustVisibility(context, remoteViews);

        if(intent.hasExtra(KEY_ROTATION)){
            remoteViews.setImageViewResource(R.id.widget_needle, getNeedleResourceId(context, intent.getIntExtra(KEY_ROTATION, 0)));
        }

        if(intent.hasExtra(KEY_DISTANCE)){
            widgetStringDistance = intent.getStringExtra(KEY_DISTANCE);
            remoteViews.setTextViewText(R.id.widget_distance, widgetStringDistance);
        }

        remoteViews.setViewVisibility(R.id.widget_button_settings, SettingsButton_Visibility);
        remoteViews.setViewVisibility(R.id.widget_distance, Distance_Visibility);

        updateWidgetNow(context, remoteViews);
    }

    private int getNeedleResourceId(Context context, int rotationAngle) {
        return context.getResources().getIdentifier(context.getString(R.string.key_widget_needle_resource) + rotationAngle, context.getString(R.string.key_widget_needle_resource_group), context.getPackageName());
    }

    public void updateWidgetNow(Context context, RemoteViews remoteViews){
        ComponentName widgetComponent = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager.getInstance(context).updateAppWidget(widgetComponent, remoteViews);
    }
}
