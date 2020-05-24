package nic.goi.aarogyasetu.utility;

import nic.goi.aarogyasetu.models.LanguageDTO;

import java.util.ArrayList;
import java.util.List;

import nic.goi.aarogyasetu.models.LanguageDTO;

/**
 * Created by Kshitij Khatri on 21/03/20.
 */
public class LanguageList {

    public static List<LanguageDTO> getLanguageList() {
        List<LanguageDTO> list = new ArrayList<>();
        list.add(new LanguageDTO("en", "English"));
        list.add(new LanguageDTO("hi", "हिंदी"));
        list.add(new LanguageDTO("gu", "ગુજરાતી"));
        list.add(new LanguageDTO("ka", "ಕನ್ನಡ"));
        list.add(new LanguageDTO("te", "తెలుగు"));
        list.add(new LanguageDTO("od", "ଓଡ଼ିଆ"));
        list.add(new LanguageDTO("ta", "தமிழ்"));
        list.add(new LanguageDTO("ma", "मराठी"));
        list.add(new LanguageDTO("mal", "മലയാളം"));
        list.add(new LanguageDTO("ba", "বাংলা"));
        list.add(new LanguageDTO("pu", "ਪੰਜਾਬੀ"));
        list.add(new LanguageDTO("as", "অসমীয়া"));
        return list;
    }
}
