package nic.goi.aarogyasetu.localization

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources

import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants

import java.util.Locale

import nic.goi.aarogyasetu.prefs.SharedPrefsConstants.DEFAULT_LANGUAGE_CODE

/**
 * Created by Kshitij Khatri on 23/03/20.
 * Updated by Niharika.Arora
 */
object LocaleManager {

    fun setNewLocale(c: Context, language: String) {
        updateResources(c, language)
    }

    fun getLanguage(c: Context): String {
        return SharedPref.getStringParams(c, SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE, DEFAULT_LANGUAGE_CODE)
    }

    private fun updateResources(context: Context?, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        if (context != null) {
            val res = context!!.getResources()
            val config = Configuration(res.getConfiguration())
            config.locale = locale
            res.updateConfiguration(config, res.getDisplayMetrics())
        }
    }
}
