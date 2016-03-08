package fi.mabrosim.weatherwidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity
 * WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {
    private static final String UPDATE_INTERVAL_EXPIRED = "fi.mabrosim.weatherwidget.action.UPDATE_INTERVAL_EXPIRED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWeatherWidget(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        updateWeatherWidget(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    @Override
    public void onEnabled(Context context) {
        if (context != null) {
            PendingIntent mPendingIntent = PendingIntent.getService(
                    context, 0, new Intent(UPDATE_INTERVAL_EXPIRED), 0);

            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setRepeating(
                    AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, mPendingIntent);
        }
    }

    @Override
    public void onDisabled(Context context) {
    }

    public static void updateWeatherWidget(Context context) {
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends IntentService {
        private static final String  TAG      = "WeatherWidgetUpdateService";
        private final        Handler mHandler = new Handler();

        public UpdateService() {
            super(TAG);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            if (Clicks.ACTION_CLICK.equals(intent.getAction())) {
                Clicks.handleClickAction(this, mHandler);
            } else {
                getWeatherData(this);
            }
        }

        private static void getWeatherData(final Context context) {
            WeatherData.getInstance().invalidate();
            WeatherWidget.updateViews(context);

            GetHTMLTask task = new GetHTMLTask(new GetHTMLTask.OnTaskCompleted() {
                @Override
                public void onTaskCompleted() {
                    WeatherWidget.updateViews(context);
                }
            });
            task.execute(WeatherData.PARSE_URL);
        }
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

    private static void updateViews(Context context) {
        ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, new Intent(Clicks.ACTION_CLICK), 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weatherwidget);
        WeatherData wd = WeatherData.getInstance();

        views.setTextViewText(R.id.tvTempNow, wd.getTempString());
        views.setTextViewText(R.id.tv_MinTemp, wd.getTempString(WeatherData.MIN_TEMP));
        views.setTextViewText(R.id.tv_MaxTemp, wd.getTempString(WeatherData.MAX_TEMP));
        views.setTextViewText(R.id.textTimestamp, getClockTime());

        views.setOnClickPendingIntent(R.id.layoutWidget, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(thisWidget, views);
    }

    private static String getClockTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private static void showToast(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_custom_layout, new LinearLayout(context));
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private static boolean isShowHint(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(WeatherData.PREFS_NAME,
                Context.MODE_PRIVATE);
        return sharedPref.getBoolean(WeatherData.PREF_SHOW_HINT, true);
    }
}
