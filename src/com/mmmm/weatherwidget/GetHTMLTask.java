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

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


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
            int temp_index_starts = index + search_template.length();
            return Float.parseFloat(s.substring(temp_index_starts, temp_index_starts + 4));
        } else
            return Float.NaN;
    }

    private void parseDataFromHtml(String s) {
        final String TEMPLATE_TEMP_NOW = "<p class=\"tempnow\">";
        final String TEMPLATE_TEMP_MIN = "low: ";
        final String TEMPLATE_TEMP_MAX = "24 hour high: ";
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
            ex.printStackTrace();
        }
        return stream;
    }


    public interface OnTaskCompleted {
        void onTaskCompleted();
    }
}
