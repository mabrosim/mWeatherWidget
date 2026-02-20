package fi.mabrosim.weatherwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity
 * WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {
    private static final String TAG_PERIODIC_WORK_REQUEST = "fi.mabrosim.weatherwidget.PERIODIC_REQUEST";
    private static final String TAG_ONE_TIME_WORK_REQUEST = "fi.mabrosim.weatherwidget.ONE_TIME_REQUEST";
    private static final long MIN_UPDATE_INTERVAL_MS = 60_000; // 1 minute

    private static long sLastUpdateTime = 0;
    private static int sLastMinWidth = -1;
    private static int sLastMinHeight = -1;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Clicks.ACTION_CLICK.equals(intent.getAction())) {
            Clicks.handleClickAction(context, mHandler);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWeatherWidget(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        // Fix #1: Only update if widget dimensions actually changed
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, -1);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, -1);
        if (minWidth != sLastMinWidth || minHeight != sLastMinHeight) {
            sLastMinWidth = minWidth;
            sLastMinHeight = minHeight;
            updateWeatherWidget(context);
        }
    }

    public static void updateWeatherWidget(Context context) {
        // Fix #2: Throttle updates to prevent rapid-fire loops
        long now = System.currentTimeMillis();
        if (now - sLastUpdateTime < MIN_UPDATE_INTERVAL_MS) {
            return;
        }
        sLastUpdateTime = now;

        // Fix #3: Use unique work with KEEP policy so duplicate requests are ignored
        WorkManager.getInstance(context).enqueueUniqueWork(
                TAG_ONE_TIME_WORK_REQUEST,
                ExistingWorkPolicy.KEEP,
                new OneTimeWorkRequest.Builder(UpdateWorker.class).build());
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(UpdateWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG_PERIODIC_WORK_REQUEST, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
    }

    @Override
    public void onDisabled(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(TAG_PERIODIC_WORK_REQUEST);
        super.onDisabled(context);
    }

    static void singleClickHandler(Context context) {
        if (isShowHint(context)) {
            showToast(context);
        }
        updateWeatherWidget(context);
    }

    static void doubleClickHandler(Context context) {
        Intent intent = new Intent(context, OpenUrlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        updateWeatherWidget(context);
    }

    static void tripleClickHandler(Context context) {
        Intent intent = new Intent(context, WeatherWidgetConfigureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void updateViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weatherwidget);
        WeatherData wd = WeatherData.getInstance();

        views.setTextViewText(R.id.tvTempNow, wd.getTempString());
        views.setTextViewText(R.id.tv_MinTemp, wd.getTempString(WeatherData.MIN_TEMP));
        views.setTextViewText(R.id.tv_MaxTemp, wd.getTempString(WeatherData.MAX_TEMP));

        Intent intent = new Intent(context, WeatherWidget.class);
        intent.setAction(Clicks.ACTION_CLICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.layoutWidget, pendingIntent);

        // Instruct the widget manager to update the widget
        ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
    }


    private static void showToast(Context context) {
        Toast.makeText(context, "1×tap → Refresh\n2×tap → Open URL\n3×tap → Settings", Toast.LENGTH_LONG).show();
    }

    private static boolean isShowHint(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(WeatherData.PREFS_NAME,
                Context.MODE_PRIVATE);
        return sharedPref.getBoolean(WeatherData.PREF_SHOW_HINT, true);
    }
}
