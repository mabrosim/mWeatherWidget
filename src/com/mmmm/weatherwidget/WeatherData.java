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
    private static final WeatherData ourInstance = new WeatherData();
    private float mNowTemp;
    private float mMinTemp;
    private float mMaxTemp;
    private int mClicks = 0;
    private boolean mShowHint = true;
    private boolean mValid = false;

    private WeatherData() {
    }

    public static WeatherData getInstance() {
        return ourInstance;
    }

    public float getNowTemp() {
        return mNowTemp;
    }

    public void setNowTemp(float mNowTemp) {
        this.mNowTemp = mNowTemp;
    }

    public float getMinTemp() {
        return mMinTemp;
    }

    public void setMinTemp(float mMinTemp) {
        this.mMinTemp = mMinTemp;
    }

    public float getMaxTemp() {
        return mMaxTemp;
    }

    public void setMaxTemp(float mMaxTemp) {
        this.mMaxTemp = mMaxTemp;
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

    public boolean isValid() {
        return mValid;
    }

    public void setValid(boolean isValid) {
        this.mValid = isValid;
    }
}

