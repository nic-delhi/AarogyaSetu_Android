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
