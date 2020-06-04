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

package nic.goi.aarogyasetu.localization;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import nic.goi.aarogyasetu.prefs.SharedPref;
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants;

import java.util.Locale;

import static nic.goi.aarogyasetu.prefs.SharedPrefsConstants.DEFAULT_LANGUAGE_CODE;

/**
 * Created by Kshitij Khatri on 23/03/20.
 * Updated by Niharika.Arora
 */
public class LocaleManager {

    public static void setNewLocale(Context c, String language) {
        updateResources(c, language);
    }

    public static String getLanguage(Context c) {
        return SharedPref.getStringParams(c, SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE, DEFAULT_LANGUAGE_CODE);
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        if (context != null) {
            Resources res = context.getResources();
            Configuration config = new Configuration(res.getConfiguration());
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
    }
}
