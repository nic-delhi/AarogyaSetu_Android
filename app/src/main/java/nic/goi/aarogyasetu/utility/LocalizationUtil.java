package nic.goi.aarogyasetu.utility;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import nic.goi.aarogyasetu.CoronaApplication;
import nic.goi.aarogyasetu.localization.LocaleManager;
import nic.goi.aarogyasetu.prefs.SharedPref;
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants;

import java.util.Map;

/**
 * Created by Kshitij Khatri on 23/03/20.
 */

public class LocalizationUtil {
    private static final boolean IS_DYNAMIC_LANGUAGE_SELECTION_ENABLED = false;

    /**
     * This method enables  the app to add support for multiple language at the runtime by making use of the shared prefs
     *
     * @param context To get and put String res values from R.string XML and Shared prefs
     * @param strId   The string resourse id for which the localised String is needed
     * @return The localised String value if found in shared prefs otherwise the default string value from the R.string XML file
     */
    public static String getLocalisedString(Context context, int strId) {

        /**
         *  If the dynamic language support is OFF from the Firebase's side. This ConfigConstants allows us to
         *  use the standard localization of the Android platform and loads correct locale specific language from the
         *  appropriate locale strings.xml file
         * */
        if (!IS_DYNAMIC_LANGUAGE_SELECTION_ENABLED) {
            String lan = SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE, null);
            if (lan != null)
                LocaleManager.setNewLocale(context, lan);

            return context.getResources().getString(strId);
        }

        if (context == null) //keeping a safety check here
            context = CoronaApplication.getInstance().getApplicationContext();
        String localisedString = null;
        String strResEntryName = getStrResEntryName(context, strId);

        if (strResEntryName != null) {
            String currentLang = SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE, "en");
            strResEntryName = currentLang + "_" + strResEntryName;

            localisedString = SharedPref.getStringParams(context, strResEntryName, "");
            if (!localisedString.isEmpty()) {

                //Formatter formatter = new Formatter(sb, Locale.US);
                localisedString = String.format(null, localisedString, null);
                //localisedString=localisedString.replace("\\\\","\\");
            }
        }

        if (localisedString == null || localisedString.isEmpty())
            localisedString = context.getResources().getString(strId);
        return localisedString;
    }


    /**
     * This method extracts the string resource id's entry name to stoer local string values against those keys
     *
     * @param ctx   To get String res entry name from R.string XML
     * @param strId The string resourse id for which the entry name  is needed
     * @return The res entry name for the passed string res id. For example if you passed R.string.app_name the method will
     * return "app_name" entry name
     */
    private static String getStrResEntryName(Context ctx, int strId) {
        String strResEntryName = null;
        try {
            if (strId != 0)
                strResEntryName = ctx.getResources().getResourceEntryName(strId);
        } catch (Resources.NotFoundException e) {
            //do nothing
        }
        return strResEntryName;
    }


    /**
     * This method enables the app to add support for dynamically changing the language. It does store the local string values in the
     * shared prefs for later use to display the localised language in the app
     *
     * @param localStrMap The values for local string values with R.string resId keys
     */
    public static void storeLocalizedStringMapping(Context context, Map<String, String> localStrMap) {
        for (Map.Entry<String, String> entry : localStrMap.entrySet()) {
            SharedPref.setStringParams(context, entry.getKey(), entry.getValue());
        }
    }


    public static SpannableStringBuilder getSpannableString(Context context, int resId, @NonNull String[] values) {
        String finalString = null;

        if (!IS_DYNAMIC_LANGUAGE_SELECTION_ENABLED) {
            String lan = SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE, null);
            if (lan != null)
                LocaleManager.setNewLocale(context, lan);
        }

        switch (values.length) {
            case 1:
                finalString = context.getString(resId, values[0]);
                break;
            case 2:
                finalString = context.getString(resId, values[0], values[1]);
                break;
            case 3:
                finalString = context.getString(resId, values[0], values[1], values[2]);
                break;
            default:
                finalString = context.getString(resId);
                break;
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(finalString);

        for (String value : values) {
            int startIndex = finalString.indexOf(value);
            if (startIndex >= 0)
                spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), startIndex, startIndex + value.length(), 0);
        }
        return spannableStringBuilder;
    }

}
