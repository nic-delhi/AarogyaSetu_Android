package nic.goi.aarogyasetu.utility;

import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import nic.goi.aarogyasetu.CoronaApplication;
import nic.goi.aarogyasetu.models.EncryptedInfo;
import nic.goi.aarogyasetu.prefs.SharedPref;
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants;

/**
 * @author Niharika.Arora
 */
public class DecryptionUtil extends SecureUtil {

    private Key secretKey;

    private Key getKey() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, NoSuchPaddingException, NoSuchProviderException, BadPaddingException, KeyStoreException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        if (secretKey == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                secretKey = getSecretKey();
            } else {
                secretKey = getSecretKeyAPILessThanM();
            }
        }
        return secretKey;
    }

    public String decryptData(final EncryptedInfo encryptedInfo)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException {
        byte[] decodedValue = Base64.decode(encryptedInfo.getData().getBytes(), Base64.DEFAULT);

        final Cipher cipher;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final GCMParameterSpec spec = new GCMParameterSpec(128, encryptedInfo.getIv());
            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), spec);
        } else {
            cipher = initCipherForLessThanM(getKey(), false);
        }

        return new String(cipher.doFinal(decodedValue), StandardCharsets.UTF_8);

    }

    public static Jws<Claims> decryptFile(String jwtToken) throws NoSuchAlgorithmException, InvalidKeySpecException, JwtException {
        Logger.d(Constants.QR_SCREEN_TAG, "Decryption start");
        String publicKeyString = SharedPref.getStringParams(CoronaApplication.instance, SharedPrefsConstants.PUBLIC_KEY, Constants.EMPTY);
        if (!TextUtils.isEmpty(publicKeyString)) {
            byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
            // create a key object from the bytes
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jwtToken);
        } else {
            throw new SignatureException("Public key is empty");
        }
    }
}