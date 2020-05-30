package nic.goi.aarogyasetu.utility


import android.os.Build
import android.util.Base64

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SignatureException
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException

import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

import nic.goi.aarogyasetu.models.EncryptedInfo

/**
 * @author Niharika.Arora
 */
class EncryptionUtil : SecureUtil() {

    var iv: ByteArray? = null
        private set

    private val cipher: Cipher
        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            InvalidKeyException::class,
            NoSuchProviderException::class,
            InvalidAlgorithmParameterException::class,
            KeyStoreException::class,
            CertificateException::class,
            IOException::class,
            UnrecoverableEntryException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class
        )
        get() {
            val cipher: Cipher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            } else {
                generateKeysForAPILessThanM()
                cipher = initCipherForLessThanM(true)
            }
            return cipher
        }

    @Throws(
        UnrecoverableEntryException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class,
        SignatureException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchProviderException::class,
        CertificateException::class
    )
    fun encryptText(textToEncrypt: String): String {
        val cipher = cipher

        iv = cipher.getIV()

        val encryption = cipher.doFinal(textToEncrypt.getBytes(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryption, Base64.DEFAULT)
    }

    @Throws(
        UnrecoverableEntryException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class,
        SignatureException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchProviderException::class,
        CertificateException::class
    )
    fun encrypt(textToEncrypt: String): EncryptedInfo {
        val cipher = cipher

        iv = cipher.getIV()

        val encryption = cipher.doFinal(textToEncrypt.getBytes(StandardCharsets.UTF_8))
        val data = Base64.encodeToString(encryption, Base64.DEFAULT)
        val encryptedInfo = EncryptedInfo()
        encryptedInfo.setData(data)
        encryptedInfo.setIv(iv)
        return encryptedInfo
    }

    companion object {

        val instance = EncryptionUtil()
    }
}