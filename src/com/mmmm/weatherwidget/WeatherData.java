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

public class WeatherData {
    public static final String URL =
            "http://weather.willab.fi/weather.html";
    public static final int MIN_TEMP = 1;
    public static final int MAX_TEMP = 2;

    private static final WeatherData ourInstance = new WeatherData();
    private final Temperature mNowTemp = new Temperature();
    private final Temperature mMinTemp = new Temperature();
    private final Temperature mMaxTemp = new Temperature();

    private int mClicks = 0;
    private boolean mShowHint = true;

    private WeatherData() {
    }

    public static WeatherData getInstance() {
        return ourInstance;
    }

    public void setTemp(Float temp, int kind) {
        if (kind == MIN_TEMP) {
            mMinTemp.setTemp(temp);
        } else if (kind == MAX_TEMP) {
            mMaxTemp.setTemp(temp);
        }
        setTemp(temp);
    }

    public void setTemp(Float temp) {
        mNowTemp.setTemp(temp);
    }

    public String getTempString() {
        return mNowTemp.toString() + " \u00B0C";
    }

    public String getTempString(int kind) {
        if (kind == MIN_TEMP) {
            return mMinTemp.toString();
        } else if (kind == MAX_TEMP) {
            return mMaxTemp.toString();
        }
        return getTempString();
    }

    public int getClicks() {
        return mClicks;
    }

    public void setClicks(int mClicks) {
        this.mClicks = mClicks;
    }

    public boolean isShowHint() {
        return mShowHint;
    }

    public void setShowHint(boolean mShowHint) {
        this.mShowHint = mShowHint;
    }

    public void invalidate() {
        mNowTemp.setTemp(Float.NaN);
        mMinTemp.setTemp(Float.NaN);
        mMaxTemp.setTemp(Float.NaN);
    }

    private class Temperature {
        private final String mDefaultString = "--";
        private Float mTemp;

        public void setTemp(Float mTemp) {
            this.mTemp = mTemp;
        }

        @Override
        public String toString() {
            if (mTemp.isNaN()) {
                return this.mDefaultString;
            } else
                return String.valueOf(mTemp);
        }
    }
}

