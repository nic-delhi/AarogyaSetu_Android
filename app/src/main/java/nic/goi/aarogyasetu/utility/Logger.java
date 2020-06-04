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

package nic.goi.aarogyasetu.utility;

import android.util.Log;

import nic.goi.aarogyasetu.BuildConfig;


/**
 * @author chandrapal.yadav
 */

public class Logger {

    private static boolean enableLog = BuildConfig.DEBUG;

    public static boolean isEnableLog() {
        return enableLog;
    }

    public static void setEnableLog(boolean enableLog) {
        Logger.enableLog = enableLog;
    }


    public static void d(String tag, String msg) {
        if (isEnableLog())
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isEnableLog())
            Log.e(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (isEnableLog())
            Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isEnableLog())
            Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isEnableLog())
            Log.w(tag, msg);
    }
}
