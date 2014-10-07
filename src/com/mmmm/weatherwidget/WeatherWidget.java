/*
 Copyright (c) 2014, Maxim Abrosimov
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation and/or other materials
 provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 OF SUCH DAMAGE.
 */

package com.mmmm.weatherwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
    private static final String TAG = "WeatherWidget";
    private static final String UPDATE_INTERVAL_EXPIRED = "com.mmmm.weather_widget" +
            ".UPDATE_INTERVAL_EXPIRED";
    private static final String CLICK = "com.mmmm.weather_widget.CLICK";

    static void setBackgroundColor(Context context, String c) {
        Log.d(TAG, "setBackgroundColor: " + c);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weatherwidget);

        int color = Color.parseColor(c);
        views.setInt(R.id.layoutWidget, "setBackgroundColor", color);
        appWidgetManager.updateAppWidget(thisWidget, views);
    }

    private void getWeatherData(final Context context) {
        Log.d(TAG, "getWeatherData");

        WeatherData.getInstance().invalidate();
        updateWeatherWidget(context);

        GetHTMLTask task = new GetHTMLTask(new GetHTMLTask.OnTaskCompleted() {
            @Override
            public void onTaskCompleted() {
                Log.d(TAG, "onTaskCompleted");
                updateWeatherWidget(context);
            }
        });

        Log.d(TAG, "task status " + task.getStatus().toString());
        task.execute(WeatherData.URL);
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

    private void singleClickHandler(Context context) {
        if (WeatherData.getInstance().isShowHint()) {
            showToast(context);
        }
        getWeatherData(context);
    }

    private void doubleClickHandler(Context context) {
        Intent intent = new Intent(context, OpenUrlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
        Log.d(TAG, "handleUpdateAction");
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
        Log.d(TAG, "onUpdate");
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
        Log.d(TAG, "onDeleted");
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled");
        if (context != null) {
            PendingIntent mPendingIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent(UPDATE_INTERVAL_EXPIRED), 0);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.SECOND, 5);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setRepeating(
                    AlarmManager.RTC, cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES /*900000*/, mPendingIntent);
        }
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled");
    }
}
