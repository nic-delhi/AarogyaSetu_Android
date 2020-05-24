package nic.goi.aarogyasetu.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import nic.goi.aarogyasetu.BuildConfig;
import nic.goi.aarogyasetu.CoronaApplication;

import static android.security.keystore.KeyProperties.KEY_ALGORITHM_AES;

/**
 * @author Niharika.Arora
 */
abstract class SecureUtil {

    static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String SHARED_PREFERENCE_NAME = "aarogya_setu_sp";
    private static final String ENCRYPTED_KEY_NAME = "sk";
    private static final String RSA_ALGORITHM_NAME = "RSA";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final String CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA = "AndroidOpenSSL";
    private static final String CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES = "BC";
    private static final String AES_MODE_LESS_THAN_M = "AES/ECB/PKCS5Padding";
    private final static Object s_keyInitLock = new Object();


    Cipher initCipherForLessThanM(boolean isEncryptMode) throws NoSuchAlgorithmException,
            NoSuchPaddingException, NoSuchProviderException, CertificateException,
            KeyStoreException, UnrecoverableEntryException, IOException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        try {
            final Key secretKeyAPILessThanM = getSecretKeyAPILessThanM();
            return initCipherForLessThanM(secretKeyAPILessThanM, isEncryptMode);
        } catch (InvalidKeyException | IOException | IllegalArgumentException
                | BadPaddingException | IllegalBlockSizeException e) {
            // Since the keys can become bad (perhaps because of lock screen change)
            // drop keys in this case.
            removeKeys();
            throw e;
        }
    }

    Cipher initCipherForLessThanM(Key secretKeyAPILessThanM, boolean isEncryptMode) throws NoSuchAlgorithmException,
            NoSuchPaddingException, NoSuchProviderException, CertificateException,
            KeyStoreException, UnrecoverableEntryException, IOException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(AES_MODE_LESS_THAN_M, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES);
        try {
            if (isEncryptMode) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeyAPILessThanM);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKeyAPILessThanM);
            }
        } catch (InvalidKeyException | IllegalArgumentException e) {
            // Since the keys can become bad (perhaps because of lock screen change)
            // drop keys in this case.
            removeKeys();
            throw e;
        }
        return cipher;
    }

    private void removeKeys() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        synchronized (s_keyInitLock) {
            KeyStore keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE);
            keyStore.load(null);
            removeKeys(keyStore);
        }
    }

    private void removeKeys(KeyStore keyStore) throws KeyStoreException {
        synchronized (s_keyInitLock) {
            keyStore.deleteEntry(BuildConfig.ALIAS);
            removeSavedSharedPreferences();
        }
    }

    @SuppressLint("ApplySharedPref")
    private void removeSavedSharedPreferences() {
        SharedPreferences sharedPreferences = CoronaApplication.getInstance().getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }

    SecretKey getSecretKey() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE);
        keyStore.load(null);
        if (!keyStore.containsAlias(BuildConfig.ALIAS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, BuildConfig.KEYSTORE);
                keyGenerator.init(new KeyGenParameterSpec.Builder(BuildConfig.ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
                return keyGenerator.generateKey();

            }
        } else {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(BuildConfig.ALIAS, null)).getSecretKey();
        }
        return null;
    }

    void generateKeysForAPILessThanM() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, CertificateException, UnrecoverableEntryException, NoSuchPaddingException, KeyStoreException, InvalidKeyException, IOException {
        // Generate a key pair for encryption
        KeyStore keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE);
        keyStore.load(null);
        if (!keyStore.containsAlias(BuildConfig.ALIAS)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(CoronaApplication.getInstance().getContext())
                    .setAlias(BuildConfig.ALIAS)
                    .setSubject(new X500Principal("CN=" + BuildConfig.ALIAS))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .setKeySize(1024)
                    .build();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM_NAME, BuildConfig.KEYSTORE);
            kpg.initialize(spec);
            kpg.generateKeyPair();
        }

        saveEncryptedKey();
    }

    private void saveEncryptedKey() throws CertificateException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, UnrecoverableEntryException, IOException {
        SharedPreferences pref = CoronaApplication.getInstance().getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String encryptedKeyBase64encoded = pref.getString(ENCRYPTED_KEY_NAME, null);
        if (encryptedKeyBase64encoded == null) {
            byte[] key = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);
            byte[] encryptedKey = rsaEncryptKey(key);
            encryptedKeyBase64encoded = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded);
            edit.commit();
        }

    }

    private byte[] rsaEncryptKey(byte[] secret) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, NoSuchPaddingException, UnrecoverableEntryException, InvalidKeyException {

        KeyStore keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE);
        keyStore.load(null);

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(BuildConfig.ALIAS, null);
        Cipher inputCipher = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA);
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();

        return outputStream.toByteArray();
    }

    protected Key getSecretKeyAPILessThanM() throws CertificateException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, UnrecoverableEntryException, IOException, BadPaddingException, IllegalBlockSizeException {
        String encryptedKeyBase64Encoded = getSecretKeyFromSharedPreferences();
        if (TextUtils.isEmpty(encryptedKeyBase64Encoded)) {
            throw new InvalidKeyException("Saved key missing from shared preferences");
        }
        byte[] encryptedKey = Base64.decode(encryptedKeyBase64Encoded, Base64.DEFAULT);
        byte[] key = rsaDecryptKey(encryptedKey);
        return new SecretKeySpec(key, "AES");
    }

    private byte[] rsaDecryptKey(byte[] encrypted) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        KeyStore keyStore = KeyStore.getInstance(BuildConfig.KEYSTORE);
        keyStore.load(null);

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(BuildConfig.ALIAS, null);
        Cipher output = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA);
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
        return output.doFinal(encrypted);
    }

    private String getSecretKeyFromSharedPreferences() {
        SharedPreferences sharedPreferences = CoronaApplication.getInstance().getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ENCRYPTED_KEY_NAME, null);
    }
}
