package fi.mabrosim.weatherwidget;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Synchronous task that fetches weather data from the given URL and parses it.
 * Must be called from a background thread (e.g. inside {@link UpdateWorker#doWork()}).
 */
class GetHTMLTask {

    /**
     * Fetches the URL and parses weather data from the response.
     * This is a blocking call — do not call from the main thread.
     */
    public void execute(String url) {
        try {
            InputStream stream = getHttpConnection(url);
            if (stream != null) {
                try {
                    parseXml(stream);
                } finally {
                    stream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseXml(InputStream stream) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, "UTF-8");

        WeatherData wd = WeatherData.getInstance();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                switch (tag) {
                    case "tempnow":
                        wd.setTemp(parseFloatFromText(parser));
                        break;
                    case "templo":
                        wd.setTemp(parseFloatFromText(parser), WeatherData.MIN_TEMP);
                        break;
                    case "temphi":
                        wd.setTemp(parseFloatFromText(parser), WeatherData.MAX_TEMP);
                        break;
                }
            }
            eventType = parser.next();
        }
    }

    private Float parseFloatFromText(XmlPullParser parser) {
        try {
            String text = parser.nextText();
            if (text != null && !text.isEmpty()) {
                return Float.valueOf(text.trim());
            }
        } catch (Exception e) {
            // ignore parse errors
        }
        return Float.NaN;
    }

    // Makes HttpURLConnection and returns InputStream
    private InputStream getHttpConnection(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return httpConnection.getInputStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
