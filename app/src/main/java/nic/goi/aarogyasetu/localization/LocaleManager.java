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
