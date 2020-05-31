package nic.goi.aarogyasetu.utility

import android.os.Build
import android.text.TextUtils
import android.util.Base64

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.Key
import java.security.KeyFactory
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PublicKey
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.GCMParameterSpec

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.SignatureException
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.models.EncryptedInfo
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants

/**
 * @author Niharika.Arora
 */
class DecryptionUtil : SecureUtil() {

    private var secretKey: Key? = null

    private val key: Key?
        @Throws(
            IOException::class,
            CertificateException::class,
            NoSuchAlgorithmException::class,
            InvalidKeyException::class,
            UnrecoverableEntryException::class,
            NoSuchPaddingException::class,
            NoSuchProviderException::class,
            BadPaddingException::class,
            KeyStoreException::class,
            IllegalBlockSizeException::class,
            InvalidAlgorithmParameterException::class
        )
        get() {
            if (secretKey == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    secretKey = getSecretKey()
                } else {
                    secretKey = getSecretKeyAPILessThanM()
                }
            }
            return secretKey
        }

    @Throws(
        UnrecoverableEntryException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        NoSuchProviderException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        InvalidAlgorithmParameterException::class,
        CertificateException::class
    )
    fun decryptData(encryptedInfo: EncryptedInfo): String {
        val decodedValue = Base64.decode(encryptedInfo.getData().getBytes(), Base64.DEFAULT)

        val cipher: Cipher

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val spec = GCMParameterSpec(128, encryptedInfo.getIv())
            cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
        } else {
            cipher = initCipherForLessThanM(key, false)
        }

        return String(cipher.doFinal(decodedValue), StandardCharsets.UTF_8)

    }

    companion object {

        @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, JwtException::class)
        fun decryptFile(jwtToken: String): Jws<Claims> {
            Logger.d(Constants.QR_SCREEN_TAG, "Decryption start")
            val publicKeyString =
                SharedPref.getStringParams(CoronaApplication.instance, SharedPrefsConstants.PUBLIC_KEY, Constants.EMPTY)
            if (!TextUtils.isEmpty(publicKeyString)) {
                val publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
                // create a key object from the bytes
                val keySpec = X509EncodedKeySpec(publicKeyBytes)
                val keyFactory = KeyFactory.getInstance("RSA")
                val publicKey = keyFactory.generatePublic(keySpec)
                return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jwtToken)
            } else {
                throw SignatureException("Public key is empty")
            }
        }
    }
}