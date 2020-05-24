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