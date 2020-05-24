package nic.goi.aarogyasetu.background;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import nic.goi.aarogyasetu.utility.UploadDataUtil;

/**
 * @author Niharika
 */

public class UploadDataToServerWorker extends Worker {

    public UploadDataToServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        UploadDataUtil uploadDataUtil = new UploadDataUtil();
        uploadDataUtil.start();
        return Result.success();
    }
}
