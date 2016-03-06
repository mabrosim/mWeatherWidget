package fi.mabrosim.weatherwidget;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;


public class OpenUrlActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_preview);

        WebView wv = (WebView) findViewById(R.id.webView);
        wv.loadUrl(WeatherData.URL);
    }
}
