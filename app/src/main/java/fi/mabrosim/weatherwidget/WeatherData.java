package fi.mabrosim.weatherwidget;

public class WeatherData {
    public static final String URL = "http://weather.willab.fi/weather.html";
    public static final String PARSE_URL = "http://weather.willab.fi/weather.xml";
    public static final String PREFS_NAME = "fi.mabrosim.weatherwidget.WeatherWidget";
    public static final String PREF_SHOW_HINT = PREFS_NAME + "_SHOW_HINT";
    public static final int MIN_TEMP = 1;
    public static final int MAX_TEMP = 2;

    private static final WeatherData ourInstance = new WeatherData();
    private final Temperature mNowTemp = new Temperature();
    private final Temperature mMinTemp = new Temperature();
    private final Temperature mMaxTemp = new Temperature();

    private int mClicks = 0;

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
        } else {
            setTemp(temp);
        }
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

    public void invalidate() {
        mNowTemp.setTemp(Float.NaN);
        mMinTemp.setTemp(Float.NaN);
        mMaxTemp.setTemp(Float.NaN);
    }

    private class Temperature {
        private final String mDefaultString = "--";
        private Float mTemp;

        public void setTemp(Float t) {
            this.mTemp = t;
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

