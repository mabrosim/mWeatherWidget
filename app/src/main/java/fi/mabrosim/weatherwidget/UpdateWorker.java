package fi.mabrosim.weatherwidget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UpdateWorker extends Worker {

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        final Context context = getApplicationContext();

        // doWork() already runs on a background thread, so fetch synchronously
        // Don't invalidate before fetch — keep stale data as fallback on network failure
        new GetHTMLTask().execute(WeatherData.PARSE_URL);

        // Post UI update to the main thread
        new Handler(Looper.getMainLooper()).post(() -> WeatherWidget.updateViews(context));

        return Result.success();
    }
}
