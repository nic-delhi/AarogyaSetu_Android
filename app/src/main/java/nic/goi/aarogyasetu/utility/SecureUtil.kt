package nic.goi.aarogyasetu.utility

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.TextUtils
import android.util.Base64

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import java.util.Calendar

import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.CoronaApplication

import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES

/**
 * @author Niharika.Arora
 */
internal abstract class SecureUtil {

    val secretKey: SecretKey?
        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchProviderException::class,
            InvalidAlgorithmParameterException::class,
            KeyStoreException::class,
            CertificateException::class,
            IOException::class,
            UnrecoverableEntryException::class
        )
        get() {
            val keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(BuildConfig.ALIAS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, BuildConfig.KEYSTORE)
                    keyGenerator.init(
                        KeyGenParameterSpec.Builder(
                            BuildConfig.ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        )
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build()
                    )
                    return keyGenerator.generateKey()

                }
            } else {
                return (keyStore.getEntry(BuildConfig.ALIAS, null) as KeyStore.SecretKeyEntry).getSecretKey()
            }
            return null
        }

    protected val secretKeyAPILessThanM: Key
        @Throws(
            CertificateException::class,
            NoSuchPaddingException::class,
            InvalidKeyException::class,
            NoSuchAlgorithmException::class,
            KeyStoreException::class,
            NoSuchProviderException::class,
            UnrecoverableEntryException::class,
            IOException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class
        )
        get() {
            val encryptedKeyBase64Encoded = secretKeyFromSharedPreferences
            if (TextUtils.isEmpty(encryptedKeyBase64Encoded)) {
                throw InvalidKeyException("Saved key missing from shared preferences")
            }
            val encryptedKey = Base64.decode(encryptedKeyBase64Encoded, Base64.DEFAULT)
            val key = rsaDecryptKey(encryptedKey)
            return SecretKeySpec(key, "AES")
        }

    private val secretKeyFromSharedPreferences: String
        get() {
            val sharedPreferences = CoronaApplication.getInstance().getContext()
                .getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getString(ENCRYPTED_KEY_NAME, null)
        }


    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        NoSuchProviderException::class,
        CertificateException::class,
        KeyStoreException::class,
        UnrecoverableEntryException::class,
        IOException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    fun initCipherForLessThanM(isEncryptMode: Boolean): Cipher {
        try {
            val secretKeyAPILessThanM = secretKeyAPILessThanM
            return initCipherForLessThanM(secretKeyAPILessThanM, isEncryptMode)
        } catch (e: InvalidKeyException) {
            // Since the keys can become bad (perhaps because of lock screen change)
            // drop keys in this case.
            removeKeys()
            throw e
        } catch (e: IOException) {
            removeKeys()
            throw e
        } catch (e: IllegalArgumentException) {
            removeKeys()
            throw e
        } catch (e: BadPaddingException) {
            removeKeys()
            throw e
        } catch (e: IllegalBlockSizeException) {
            removeKeys()
            throw e
        }

    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        NoSuchProviderException::class,
        CertificateException::class,
        KeyStoreException::class,
        UnrecoverableEntryException::class,
        IOException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    fun initCipherForLessThanM(secretKeyAPILessThanM: Key, isEncryptMode: Boolean): Cipher {
        val cipher = Cipher.getInstance(AES_MODE_LESS_THAN_M, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES)
        try {
            if (isEncryptMode) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeyAPILessThanM)
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKeyAPILessThanM)
            }
        } catch (e: InvalidKeyException) {
            // Since the keys can become bad (perhaps because of lock screen change)
            // drop keys in this case.
            removeKeys()
            throw e
        } catch (e: IllegalArgumentException) {
            removeKeys()
            throw e
        }

        return cipher
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    private fun removeKeys() {
        synchronized(s_keyInitLock) {
            val keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE)
            keyStore.load(null)
            removeKeys(keyStore)
        }
    }

    @Throws(KeyStoreException::class)
    private fun removeKeys(keyStore: KeyStore) {
        synchronized(s_keyInitLock) {
            keyStore.deleteEntry(BuildConfig.ALIAS)
            removeSavedSharedPreferences()
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun removeSavedSharedPreferences() {
        val sharedPreferences = CoronaApplication.getInstance().getContext()
            .getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()
    }

    @Throws(
        NoSuchProviderException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        CertificateException::class,
        UnrecoverableEntryException::class,
        NoSuchPaddingException::class,
        KeyStoreException::class,
        InvalidKeyException::class,
        IOException::class
    )
    fun generateKeysForAPILessThanM() {
        // Generate a key pair for encryption
        val keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE)
        keyStore.load(null)
        if (!keyStore.containsAlias(BuildConfig.ALIAS)) {
            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            end.add(Calendar.YEAR, 30)
            val spec = KeyPairGeneratorSpec.Builder(CoronaApplication.getInstance().getContext())
                .setAlias(BuildConfig.ALIAS)
                .setSubject(X500Principal("CN=" + BuildConfig.ALIAS))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .setKeySize(1024)
                .build()
            val kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM_NAME, BuildConfig.KEYSTORE)
            kpg.initialize(spec)
            kpg.generateKeyPair()
        }

        saveEncryptedKey()
    }

    @Throws(
        CertificateException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        NoSuchProviderException::class,
        UnrecoverableEntryException::class,
        IOException::class
    )
    private fun saveEncryptedKey() {
        val pref = CoronaApplication.getInstance().getContext()
            .getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        var encryptedKeyBase64encoded = pref.getString(ENCRYPTED_KEY_NAME, null)
        if (encryptedKeyBase64encoded == null) {
            val key = ByteArray(16)
            val secureRandom = SecureRandom()
            secureRandom.nextBytes(key)
            val encryptedKey = rsaEncryptKey(key)
            encryptedKeyBase64encoded = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
            val edit = pref.edit()
            edit.putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded)
            edit.commit()
        }

    }

    @Throws(
        KeyStoreException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        NoSuchProviderException::class,
        NoSuchPaddingException::class,
        UnrecoverableEntryException::class,
        InvalidKeyException::class
    )
    private fun rsaEncryptKey(secret: ByteArray): ByteArray {

        val keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE)
        keyStore.load(null)

        val privateKeyEntry = keyStore.getEntry(BuildConfig.ALIAS, null) as KeyStore.PrivateKeyEntry
        val inputCipher = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA)
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey())

        val outputStream = ByteArrayOutputStream()
        val cipherOutputStream = CipherOutputStream(outputStream, inputCipher)
        cipherOutputStream.write(secret)
        cipherOutputStream.close()

        return outputStream.toByteArray()
    }

    @Throws(
        KeyStoreException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        UnrecoverableEntryException::class,
        NoSuchProviderException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    private fun rsaDecryptKey(encrypted: ByteArray): ByteArray {

        val keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE)
        keyStore.load(null)

        val privateKeyEntry = keyStore.getEntry(BuildConfig.ALIAS, null) as KeyStore.PrivateKeyEntry
        val output = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA)
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey())
        return output.doFinal(encrypted)
    }

    companion object {

        val TRANSFORMATION = "AES/GCM/NoPadding"
        private val SHARED_PREFERENCE_NAME = "aarogya_setu_sp"
        private val ENCRYPTED_KEY_NAME = "sk"
        private val RSA_ALGORITHM_NAME = "RSA"
        private val RSA_MODE = "RSA/ECB/PKCS1Padding"
        private val CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA = "AndroidOpenSSL"
        private val CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES = "BC"
        private val AES_MODE_LESS_THAN_M = "AES/ECB/PKCS5Padding"
        private val s_keyInitLock = Object()
    }
}
