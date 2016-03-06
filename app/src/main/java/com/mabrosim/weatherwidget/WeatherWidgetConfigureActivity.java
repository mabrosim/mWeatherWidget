package com.mabrosim.weatherwidget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * The configuration screen for the {@link WeatherWidget WeatherWidget} AppWidget.
 */
public class WeatherWidgetConfigureActivity extends Activity {

    private boolean isShowHint() {
        SharedPreferences sharedPref = getSharedPreferences(WeatherData.PREFS_NAME,
                Context.MODE_PRIVATE);
        return sharedPref.getBoolean(WeatherData.PREF_SHOW_HINT, true);
    }

    private void setShowHint(boolean b) {
        SharedPreferences.Editor sharedPrefEditor = getSharedPreferences(WeatherData.PREFS_NAME,
                Context.MODE_PRIVATE).edit();

        sharedPrefEditor.putBoolean(WeatherData.PREF_SHOW_HINT, b);
        sharedPrefEditor.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.weatherwidget_configure);

        findViewById(R.id.button_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Switch switchHint = (Switch) findViewById(R.id.switch1);
        switchHint.setChecked(isShowHint());
        switchHint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setShowHint(b);
            }
        });

        setResult(RESULT_OK);
    }
}



