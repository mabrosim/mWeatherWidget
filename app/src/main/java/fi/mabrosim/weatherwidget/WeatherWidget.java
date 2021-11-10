package fi.mabrosim.weatherwidget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.JobIntentService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity
 * WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWeatherWidget(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        updateWeatherWidget(context);
    }

    public static void updateWeatherWidget(Context context) {
        //context.startService(new Intent(context, UpdateService.class));
        UpdateService.enqueueWork(context, new Intent());
    }

    public static class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateService.enqueueWork(context, intent);
        }
    }

    public static class UpdateService extends JobIntentService {
        private final        Handler mHandler = new Handler(Looper.getMainLooper());

        public static void enqueueWork(Context context, Intent work) {
            enqueueWork(context, UpdateService.class, 2, work);
        }

        @Override
        protected void onHandleWork(Intent intent) {
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

    @SuppressLint("UnspecifiedImmutableFlag")
    private static void updateViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weatherwidget);
        WeatherData wd = WeatherData.getInstance();

        views.setTextViewText(R.id.tvTempNow, wd.getTempString());
        views.setTextViewText(R.id.tv_MinTemp, wd.getTempString(WeatherData.MIN_TEMP));
        views.setTextViewText(R.id.tv_MaxTemp, wd.getTempString(WeatherData.MAX_TEMP));
        views.setTextViewText(R.id.textTimestamp, getClockTime());

        Intent intent = new Intent(Clicks.ACTION_CLICK, null, context, Receiver.class);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        }
        views.setOnClickPendingIntent(R.id.layoutWidget, pendingIntent);

        // Instruct the widget manager to update the widget
        ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
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
