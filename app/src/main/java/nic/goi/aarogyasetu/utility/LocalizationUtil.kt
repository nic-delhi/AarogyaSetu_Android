package nic.goi.aarogyasetu.utility

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan

import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.localization.LocaleManager
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants

/**
 * Created by Kshitij Khatri on 23/03/20.
 */

object LocalizationUtil {
    private val IS_DYNAMIC_LANGUAGE_SELECTION_ENABLED = false

    /**
     * This method enables  the app to add support for multiple language at the runtime by making use of the shared prefs
     *
     * @param context To get and put String res values from R.string XML and Shared prefs
     * @param strId   The string resourse id for which the localised String is needed
     * @return The localised String value if found in shared prefs otherwise the default string value from the R.string XML file
     */
    fun getLocalisedString(context: Context?, strId: Int): String? {
        var context = context

        /**
         * If the dynamic language support is OFF from the Firebase's side. This ConfigConstants allows us to
         * use the standard localization of the Android platform and loads correct locale specific language from the
         * appropriate locale strings.xml file
         */
        if (!IS_DYNAMIC_LANGUAGE_SELECTION_ENABLED) {
            val lan = SharedPref.getStringParams(
                CoronaApplication.getInstance(),
                SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                null
            )
            if (lan != null)
                LocaleManager.setNewLocale(context, lan)

            return context!!.getResources().getString(strId)
        }

        if (context == null)
        //keeping a safety check here
            context = CoronaApplication.getInstance().getApplicationContext()
        var localisedString: String? = null
        var strResEntryName = getStrResEntryName(context, strId)

        if (strResEntryName != null) {
            val currentLang = SharedPref.getStringParams(
                CoronaApplication.getInstance(),
                SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                "en"
            )
            strResEntryName = currentLang + "_" + strResEntryName

            localisedString = SharedPref.getStringParams(context, strResEntryName, "")
            if (!localisedString!!.isEmpty()) {

                //Formatter formatter = new Formatter(sb, Locale.US);
                localisedString = String.format(null, localisedString, null)
                //localisedString=localisedString.replace("\\\\","\\");
            }
        }

        if (localisedString == null || localisedString.isEmpty())
            localisedString = context!!.getResources().getString(strId)
        return localisedString
    }


    /**
     * This method extracts the string resource id's entry name to stoer local string values against those keys
     *
     * @param ctx   To get String res entry name from R.string XML
     * @param strId The string resourse id for which the entry name  is needed
     * @return The res entry name for the passed string res id. For example if you passed R.string.app_name the method will
     * return "app_name" entry name
     */
    private fun getStrResEntryName(ctx: Context?, strId: Int): String? {
        var strResEntryName: String? = null
        try {
            if (strId != 0)
                strResEntryName = ctx!!.getResources().getResourceEntryName(strId)
        } catch (e: Resources.NotFoundException) {
            //do nothing
        }

        return strResEntryName
    }


    /**
     * This method enables the app to add support for dynamically changing the language. It does store the local string values in the
     * shared prefs for later use to display the localised language in the app
     *
     * @param localStrMap The values for local string values with R.string resId keys
     */
    fun storeLocalizedStringMapping(context: Context, localStrMap: Map<String, String>) {
        for (entry in localStrMap.entrySet()) {
            SharedPref.setStringParams(context, entry.getKey(), entry.getValue())
        }
    }


    fun getSpannableString(context: Context, resId: Int, @NonNull values: Array<String>): SpannableStringBuilder {
        var finalString: String? = null

        if (!IS_DYNAMIC_LANGUAGE_SELECTION_ENABLED) {
            val lan = SharedPref.getStringParams(
                CoronaApplication.getInstance(),
                SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                null
            )
            if (lan != null)
                LocaleManager.setNewLocale(context, lan)
        }

        when (values.size) {
            1 -> finalString = context.getString(resId, values[0])
            2 -> finalString = context.getString(resId, values[0], values[1])
            3 -> finalString = context.getString(resId, values[0], values[1], values[2])
            else -> finalString = context.getString(resId)
        }
        val spannableStringBuilder = SpannableStringBuilder(finalString)

        for (value in values) {
            val startIndex = finalString!!.indexOf(value)
            if (startIndex >= 0)
                spannableStringBuilder.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + value.length(), 0)
        }
        return spannableStringBuilder
    }

}
