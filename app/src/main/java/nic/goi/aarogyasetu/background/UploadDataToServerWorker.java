/*
 * Copyright 2020 Government of India
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
