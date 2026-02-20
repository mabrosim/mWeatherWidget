package fi.mabrosim.weatherwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Only validate appWidgetId when launched during widget placement
        Intent intent = getIntent();
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                finish();
                return;
            }

            int appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish();
                return;
            }
        }

        setContentView(R.layout.weatherwidget_configure);

        findViewById(R.id.button_dismiss).setOnClickListener(view -> finish());

        @SuppressWarnings("UseSwitchCompatOrMaterialCode")
        Switch switchHint = findViewById(R.id.switch1);
        switchHint.setChecked(isShowHint());
        switchHint.setOnCheckedChangeListener((compoundButton, b) -> setShowHint(b));

        setResult(RESULT_OK);
    }
}
