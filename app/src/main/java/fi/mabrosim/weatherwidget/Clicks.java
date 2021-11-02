package fi.mabrosim.weatherwidget;

import android.content.Context;
import android.os.Handler;

public final class Clicks {
    public static final String ACTION_CLICK = "fi.mabrosim.weatherwidget.action.CLICK";

    private static final Clicks sInstance         = new Clicks();
    private static final int    CLICK_DELAY_IN_MS = 380;
    private              int    mClickCount       = 0;

    // the class is singleton, prevent instantiation
    private Clicks() {
    }

    static void handleClickAction(final Context context, final Handler handler) {
        int clickCount = sInstance.mClickCount;
        sInstance.mClickCount = ++clickCount;

        if (clickCount == 1) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handleClicks(context);
                }
            }, CLICK_DELAY_IN_MS);
        }
    }

    private static void handleClicks(Context context) {
        switch (sInstance.mClickCount) {
            case 2: {
                WeatherWidget.doubleClickHandler(context);
                break;
            }
            case 3: {
                WeatherWidget.tripleClickHandler(context);
                break;
            }
            default: {
                WeatherWidget.singleClickHandler(context);
                break;
            }
        }
        sInstance.mClickCount = 0;
    }
}

