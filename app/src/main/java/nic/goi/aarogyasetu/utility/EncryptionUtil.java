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


import android.os.Build;
import android.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import nic.goi.aarogyasetu.models.EncryptedInfo;

/**
 * @author Niharika.Arora
 */
public class EncryptionUtil extends SecureUtil {

    private byte[] iv;

    private static final EncryptionUtil instance = new EncryptionUtil();

    public static EncryptionUtil getInstance() {
        return instance;
    }

    public String encryptText(final String textToEncrypt)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchPaddingException, InvalidKeyException, IOException,
            InvalidAlgorithmParameterException, SignatureException, BadPaddingException,
            IllegalBlockSizeException, NoSuchProviderException, CertificateException {
        final Cipher cipher = getCipher();

        iv = cipher.getIV();

        byte[] encryption = cipher.doFinal(textToEncrypt.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryption, Base64.DEFAULT);
    }

    public EncryptedInfo encrypt(final String textToEncrypt)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchPaddingException, InvalidKeyException, IOException,
            InvalidAlgorithmParameterException, SignatureException, BadPaddingException,
            IllegalBlockSizeException, NoSuchProviderException, CertificateException {
        final Cipher cipher = getCipher();

        iv = cipher.getIV();

        byte[] encryption = cipher.doFinal(textToEncrypt.getBytes(StandardCharsets.UTF_8));
        final String data = Base64.encodeToString(encryption, Base64.DEFAULT);
        final EncryptedInfo encryptedInfo = new EncryptedInfo();
        encryptedInfo.setData(data);
        encryptedInfo.setIv(iv);
        return encryptedInfo;
    }

    private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException, UnrecoverableEntryException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        } else {
            generateKeysForAPILessThanM();
            cipher = initCipherForLessThanM(true);
        }
        return cipher;
    }

    public byte[] getIv() {
        return iv;
    }
}