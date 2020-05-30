package nic.goi.aarogyasetu.utility

import nic.goi.aarogyasetu.models.LanguageDTO

import java.util.ArrayList

/**
 * Created by Kshitij Khatri on 21/03/20.
 */
object LanguageList {

    val languageList: List<LanguageDTO>
        get() {
            val list = ArrayList()
            list.add(LanguageDTO("en", "English"))
            list.add(LanguageDTO("hi", "हिंदी"))
            list.add(LanguageDTO("gu", "ગુજરાતી"))
            list.add(LanguageDTO("ka", "ಕನ್ನಡ"))
            list.add(LanguageDTO("te", "తెలుగు"))
            list.add(LanguageDTO("od", "ଓଡ଼ିଆ"))
            list.add(LanguageDTO("ta", "தமிழ்"))
            list.add(LanguageDTO("ma", "मराठी"))
            list.add(LanguageDTO("mal", "മലയാളം"))
            list.add(LanguageDTO("ba", "বাংলা"))
            list.add(LanguageDTO("pu", "ਪੰਜਾਬੀ"))
            list.add(LanguageDTO("as", "অসমীয়া"))
            return list
        }
}
