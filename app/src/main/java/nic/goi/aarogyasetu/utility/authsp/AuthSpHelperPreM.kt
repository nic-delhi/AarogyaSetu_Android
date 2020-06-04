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

package nic.goi.aarogyasetu.utility.authsp

import android.content.Context
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.models.Converters
import nic.goi.aarogyasetu.models.EncryptedInfo
import nic.goi.aarogyasetu.utility.DecryptionUtil
import nic.goi.aarogyasetu.utility.EncryptionUtil

/**
 * @author Aman kapoor
 */
class AuthSpHelperPreM : AuthSpHelper {

    companion object {
        private const val SHARED_PREFERENCE_NAME = "auth_pref_lollipop"
    }

    private val encryptionUtil by lazy {
        EncryptionUtil.getInstance()
    }

    private val decryptionUtil by lazy {
        DecryptionUtil()
    }

    private val sharedPreferences by lazy {
        CoronaApplication.getInstance().context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    override fun getString(key: String, defaultValue: String?) : String? {
        return try {
            val encryptedValue = sharedPreferences.getString(key, defaultValue)
            if (encryptedValue.isNullOrBlank()) {
                defaultValue
            } else {
                val encryptedInfo = Converters.fromString(encryptedValue)
                decryptionUtil.decryptData(encryptedInfo)
            }
        } catch (e : Exception) {
            defaultValue
        }
    }

    override fun putString(key: String, value: String?) {
        try {
            val encryptedInfo = encryptionUtil.encrypt(value)
            val encryptedValue = Converters.fromArrayList(encryptedInfo)
            sharedPreferences.edit().putString(key, encryptedValue).apply()
        } catch (e : Exception) {
            //do nothing
        }
    }

    override fun removeKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}