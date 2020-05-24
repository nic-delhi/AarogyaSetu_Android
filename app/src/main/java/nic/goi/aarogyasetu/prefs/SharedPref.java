package nic.goi.aarogyasetu.prefs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kshitij.khatri on 24-Jul-17.
 */


public class SharedPref {
    private static final String PREFS_NAME = "FightCorona_prefs";
    private static SharedPreferences _sharedPreferences = null;


    private SharedPref() {
    }

    public static SharedPreferences getInstance(Context aContext) {
        if (_sharedPreferences == null)
            _sharedPreferences = aContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return _sharedPreferences;
    }

    public static String getStringParams(Context aContext, String a_ParamName, String defaultvalue) {
        return getInstance(aContext).getString(a_ParamName, defaultvalue);

    }

    public static void setStringParams(Context aContext, String a_ParamName, String a_ParamValue) {

        SharedPreferences.Editor editor = getInstance(aContext).edit();
        editor.putString(a_ParamName, a_ParamValue);
        editor.commit();

    }

    public static void setBooleanParams(Context aContext, String a_ParamName, boolean a_ParamValue) {

        SharedPreferences.Editor editor = getInstance(aContext).edit();
        editor.putBoolean(a_ParamName, a_ParamValue);
        editor.apply();

    }

    public static boolean getBooleanParams(Context aContext, String a_ParamName) {
        return getInstance(aContext).getBoolean(a_ParamName, false);

    }

    public static boolean hasKey(Context context, String key) {
        return getInstance(context).contains(key);
    }

}

