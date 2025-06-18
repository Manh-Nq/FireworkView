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

public class AppWidget2 extends AppWidgetProvider {
    public static final String ACTION_UPDATE_DATA = "com.example.fireworkview.ACTION_UPDATE_DATA_2";
    public static final String ACTION_PLAY = "com.example.fireworkview.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.fireworkview.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.fireworkview.ACTION_STOP";
    public static final String EXTRA_DATA_TEXT = "data_text";
    private static final String PREFS_NAME = "com.example.fireworkview.AppWidget2";
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

        Log.d("ManhNQ", "AppWidget2 onReceive: " + intent.getAction());

        if (ACTION_UPDATE_DATA.equals(intent.getAction())) {
            String dataText = intent.getStringExtra(EXTRA_DATA_TEXT);
            if (dataText != null) {
                saveDataText(context, dataText);
                updateAllWidgets(context);
            }
        } else if (ACTION_PLAY.equals(intent.getAction())) {
            // Start firework
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.putExtra("action", "play");
            context.startActivity(launchIntent);
        } else if (ACTION_PAUSE.equals(intent.getAction())) {
            // Pause firework
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.putExtra("action", "pause");
            context.startActivity(launchIntent);
        } else if (ACTION_STOP.equals(intent.getAction())) {
            // Stop firework
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.putExtra("action", "stop");
            context.startActivity(launchIntent);
        }
    }

    private void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
            new android.content.ComponentName(context, AppWidget2.class));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create intents for buttons
        Intent playIntent = new Intent(context, AppWidget2.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(context, AppWidget2.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 1, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(context, AppWidget2.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get the layout for the widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_2);
        
        // Set the click listeners for buttons
        views.setOnClickPendingIntent(R.id.widget_play_button, playPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_pause_button, pausePendingIntent);
        views.setOnClickPendingIntent(R.id.widget_stop_button, stopPendingIntent);

        // Get saved text
        String dataText = getDataText(context);
        views.setTextViewText(R.id.widget_data_text, dataText);

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
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