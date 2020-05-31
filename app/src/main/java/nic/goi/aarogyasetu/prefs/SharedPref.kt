package nic.goi.aarogyasetu.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by kshitij.khatri on 24-Jul-17.
 */


object SharedPref {
    private val PREFS_NAME = "FightCorona_prefs"
    private var _sharedPreferences: SharedPreferences? = null

    fun getInstance(aContext: Context): SharedPreferences {
        if (_sharedPreferences == null)
            _sharedPreferences = aContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        return _sharedPreferences
    }

    fun getStringParams(aContext: Context, a_ParamName: String, defaultvalue: String): String {
        return getInstance(aContext).getString(a_ParamName, defaultvalue)

    }

    fun setStringParams(aContext: Context, a_ParamName: String, a_ParamValue: String) {

        val editor = getInstance(aContext).edit()
        editor.putString(a_ParamName, a_ParamValue)
        editor.commit()

    }

    fun setBooleanParams(aContext: Context, a_ParamName: String, a_ParamValue: Boolean) {

        val editor = getInstance(aContext).edit()
        editor.putBoolean(a_ParamName, a_ParamValue)
        editor.apply()

    }

    fun getBooleanParams(aContext: Context, a_ParamName: String): Boolean {
        return getInstance(aContext).getBoolean(a_ParamName, false)

    }

    fun hasKey(context: Context, key: String): Boolean {
        return getInstance(context).contains(key)
    }

}

