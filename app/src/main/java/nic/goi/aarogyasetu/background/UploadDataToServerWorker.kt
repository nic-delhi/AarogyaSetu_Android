package nic.goi.aarogyasetu.background

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

import nic.goi.aarogyasetu.utility.UploadDataUtil

/**
 * @author Niharika
 */

class UploadDataToServerWorker(@NonNull context: Context, @NonNull workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    @NonNull
    @Override
    fun doWork(): Result {
        val uploadDataUtil = UploadDataUtil()
        uploadDataUtil.start()
        return Result.success()
    }
}
