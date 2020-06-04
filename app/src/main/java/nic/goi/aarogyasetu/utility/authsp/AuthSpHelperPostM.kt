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

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nic.goi.aarogyasetu.BuildConfig

/**
 * @author Aman kapoor
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class AuthSpHelperPostM : AuthSpHelper {

    private val advancedKeyAlias: String by lazy {
        // Custom Advanced Master Key
        val advancedSpec = KeyGenParameterSpec.Builder(
            BuildConfig.AUTH_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val hasStrongBox =
                    nic.goi.aarogyasetu.CoronaApplication.instance.packageManager.hasSystemFeature(
                        PackageManager.FEATURE_STRONGBOX_KEYSTORE
                    )
                if (hasStrongBox) {
                    setIsStrongBoxBacked(true)
                }
            }
        }.build()

        MasterKeys.getOrCreate(advancedSpec)
    }

    private val authSp: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "auth_pref",
            advancedKeyAlias,
            nic.goi.aarogyasetu.CoronaApplication.instance,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        authSp
    }

    override fun getString(key: String, defaultValue: String?) = authSp.getString(key, defaultValue)

    override fun putString(key: String, value: String?) {
        authSp.edit().putString(key, value).apply()
    }

    override fun removeKey(key: String) {
        authSp.edit().remove(key).apply()
    }
}