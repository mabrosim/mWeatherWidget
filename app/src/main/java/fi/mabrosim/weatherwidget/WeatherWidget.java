package fi.mabrosim.weatherwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity
 * WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {

    private static final int DOUBLE_CLICK_DELAY = 400;
    private static final String UPDATE_INTERVAL_EXPIRED = "com.mabrosim.weather_widget" +
            ".UPDATE_INTERVAL_EXPIRED";
    private static final String CLICK = "com.mabrosim.weather_widget.CLICK";

    private void getWeatherData(final Context context) {
        WeatherData.getInstance().invalidate();
        updateWeatherWidget(context);

        GetHTMLTask task = new GetHTMLTask(new GetHTMLTask.OnTaskCompleted() {
            @Override
            public void onTaskCompleted() {
                updateWeatherWidget(context);
            }
        });
        task.execute(WeatherData.PARSE_URL);
    }

    private String getClockTime() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    private void updateWeatherWidget(Context context) {
        ComponentName thisWidget = new ComponentName(context, getClass());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(CLICK), 0);
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

    private void showToast(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_custom_layout, new LinearLayout(context));
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private boolean isShowHint(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(WeatherData.PREFS_NAME,
                Context.MODE_PRIVATE);
        return sharedPref.getBoolean(WeatherData.PREF_SHOW_HINT, true);
    }

    private void singleClickHandler(Context context) {
        if (isShowHint(context)) {
            showToast(context);
        }
        getWeatherData(context);
    }

    private void doubleClickHandler(Context context) {
        Intent intent = new Intent(context, OpenUrlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        getWeatherData(context);
    }

    private void tripleClickHandler(Context context) {
        Intent intent = new Intent(context, WeatherWidgetConfigureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void handleClickAction(final Context context) {

        int clickCount = WeatherData.getInstance().getClicks();
        WeatherData.getInstance().setClicks(++clickCount);

        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {

                int clickCount = WeatherData.getInstance().getClicks();

                if (clickCount == 1) {
                    singleClickHandler(context);
                }
                if (clickCount == 2) {
                    doubleClickHandler(context);
                }
                if (clickCount == 3) {
                    tripleClickHandler(context);
                }

                WeatherData.getInstance().setClicks(0);
            }
        };

        if (clickCount == 1) new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(DOUBLE_CLICK_DELAY);
                    }
                    handler.sendEmptyMessage(0);
                } catch (InterruptedException ignored) {
                }
            }
        }.start();
    }

    private void handleUpdateAction(Context context) {
        getWeatherData(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(CLICK)) {
            handleClickAction(context);
        } else if (intent.getAction().equals(UPDATE_INTERVAL_EXPIRED)) {
            handleUpdateAction(context);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        getWeatherData(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        getWeatherData(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    @Override
    public void onEnabled(Context context) {
        if (context != null) {
            PendingIntent mPendingIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent(UPDATE_INTERVAL_EXPIRED), 0);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.SECOND, 5);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setRepeating(
                    AlarmManager.RTC, cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, mPendingIntent);
        }
    }

    @Override
    public void onDisabled(Context context) {
    }
}
