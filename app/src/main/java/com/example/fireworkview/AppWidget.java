package com.example.fireworkview;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AppWidget extends AppWidgetProvider {
    public static final String ACTION_OPEN_APP = "com.example.fireworkview.ACTION_OPEN_APP";
    public static final String ACTION_UPDATE_DATA = "com.example.fireworkview.ACTION_UPDATE_DATA";
    public static final String EXTRA_BUTTON_TEXT = "button_text";
    public static final String EXTRA_DATA_TEXT = "data_text";
    private static final String PREFS_NAME = "com.example.fireworkview.AppWidget";
    private static final String PREF_PREFIX_KEY = "widget_";
    private static final String PREF_DATA_TEXT = "widget_data_text";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d("ManhNQ", "onReceive: " + intent.getAction());

         if (ACTION_UPDATE_DATA.equals(intent.getAction())) {
            String dataText = intent.getStringExtra(EXTRA_DATA_TEXT);
            if (dataText != null) {
                saveDataText(context, dataText);
                updateAllWidgets(context);
            }
        } else if (ACTION_OPEN_APP.equals(intent.getAction())) {
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        }
    }

    private void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
            new android.content.ComponentName(context, AppWidget.class));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        Intent openAppIntent = new Intent(context, AppWidget.class);
        openAppIntent.setAction(ACTION_OPEN_APP);
        PendingIntent openAppPendingIntent = PendingIntent.getBroadcast(context, 1, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        
        // Set the click listeners for both buttons
        views.setOnClickPendingIntent(R.id.widget_open_button, openAppPendingIntent);

        // Get saved texts
        String buttonText = getButtonText(context);
        String dataText = getDataText(context);
        
        // Update views
        views.setTextViewText(R.id.widget_data_text, dataText);

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // Get saved button text from SharedPreferences
    static String getButtonText(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_PREFIX_KEY + "button_text", "Show Toast");
    }

    // Save data text to SharedPreferences
    static void saveDataText(Context context, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_DATA_TEXT, text);
        prefs.apply();
    }

    // Get saved data text from SharedPreferences
    static String getDataText(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_DATA_TEXT, "Initial Text");
    }
} 