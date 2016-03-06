package fi.mabrosim.weatherwidget;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class GetHTMLTask extends AsyncTask<String, Void, Void> {

    private final OnTaskCompleted listener;

    public GetHTMLTask(OnTaskCompleted listener) {
        this.listener = listener;
    }

    protected Void doInBackground(String... urls) {
        String output = null;

        for (String url : urls) {
            output = getOutputFromUrl(url);
        }
        if (output != null && !output.isEmpty()) {
            parseDataFromHtml(output);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        listener.onTaskCompleted();
    }

    private Float parseTempFromHtml(String s, String search_template) {
        int index = s.indexOf(search_template);

        if (index != -1) {
            int start = index + search_template.length();
            Pattern p = Pattern.compile("[-]?[0-9]*\\.?[0-9]+");
            Matcher m = p.matcher(s.substring(start, start + 5));
            if (m.find()) {
                return Float.valueOf(m.group());
            }
        }
        return Float.NaN;
    }

    private void parseDataFromHtml(String s) {
        final String TEMPLATE_TEMP_NOW = "<tempnow unit=\"C\">";
        final String TEMPLATE_TEMP_MIN = "<templo unit=\"C\">";
        final String TEMPLATE_TEMP_MAX = "<temphi unit=\"C\">";
        WeatherData wd = WeatherData.getInstance();
        wd.setTemp(parseTempFromHtml(s, TEMPLATE_TEMP_NOW));
        wd.setTemp(parseTempFromHtml(s, TEMPLATE_TEMP_MIN), WeatherData.MIN_TEMP);
        wd.setTemp(parseTempFromHtml(s, TEMPLATE_TEMP_MAX), WeatherData.MAX_TEMP);
    }

    private String getOutputFromUrl(String url) {
        StringBuilder output = new StringBuilder("");
        try {
            InputStream stream = getHttpConnection(url);
            if (stream != null) {
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s;
                while ((s = buffer.readLine()) != null)
                    output.append(s);

                buffer.close();
                stream.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return output.toString();
    }

    // Makes HttpURLConnection and returns InputStream
    private InputStream getHttpConnection(String urlString)
            throws IOException {
        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        return stream;
    }


    public interface OnTaskCompleted {
        void onTaskCompleted();
    }
}
