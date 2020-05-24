package nic.goi.aarogyasetu.utility

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.analytics.EventParams
import nic.goi.aarogyasetu.localization.LocaleManager
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants
import java.lang.Exception
import kotlin.concurrent.thread

/**
 * @author Aman kapoor
 */
object AnalyticsUtils {

    object UserTraits {
        const val TRAITS_IS_LOGGED_IN = "is_loggedin"
        const val TRAITS_VERSION_CODE = "version_code"
        const val TRAITS_LANGUAGE = "lang"
        const val INSTALL_SOURCE = "install_source"

    }

    @JvmStatic
    fun updateUserTraits() {
        thread {
            val firebaseAnalytics = FirebaseAnalytics.getInstance(CoronaApplication.instance)
            val uniqueId = SharedPref.getStringParams(
                CoronaApplication.getInstance(),
                SharedPrefsConstants.UNIQUE_ID,
                ""
            )
            val installData = CorUtility.getInstallTimes()
            firebaseAnalytics.setUserProperty(
                UserTraits.TRAITS_IS_LOGGED_IN,
                "${AuthUtility.isSignedIn()}"
            )

            firebaseAnalytics.setUserProperty(
                UserTraits.TRAITS_VERSION_CODE,
                "${BuildConfig.VERSION_CODE}"
            )

            firebaseAnalytics.setUserProperty(
                UserTraits.TRAITS_LANGUAGE,
                LocaleManager.getLanguage(CoronaApplication.instance)
            )
            try {
                firebaseAnalytics.setUserProperty(
                    UserTraits.INSTALL_SOURCE,
                    CoronaApplication.instance.packageManager.getInstallerPackageName(BuildConfig.APPLICATION_ID)
                )
            }catch (e:Exception){}
        }
    }

    @JvmStatic
    @JvmOverloads
    fun sendBasicEvent(
        eventName: String,
        screenName: String,
        error: String? = null,
        extras: Bundle? = null
    ) {
        val bundle = Bundle()
        bundle.putString(EventParams.PROP_SCREEN_NAME, screenName)
        if (!error.isNullOrBlank()) {
            bundle.putString(EventParams.PROP_ERROR, error)
        }
        if (extras != null) {
            bundle.putAll(extras)
        }

        val firebaseAnalytics = FirebaseAnalytics.getInstance(CoronaApplication.instance)
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    @JvmStatic
    @JvmOverloads
    fun sendEvent(eventName: String, bundle: Bundle? = null) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(CoronaApplication.instance)
        firebaseAnalytics.logEvent(eventName, bundle)
    }

}