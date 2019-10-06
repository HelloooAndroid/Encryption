package com.cycle.encryptiion.text.utils;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;


public class AesUtils {
    private static final byte[] FIXED_IV = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}; //initialization vector
    private final static String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    static KeyStore keyStore;
    static String ALIAS_NAME = "alias";    //Alias name for key

    /* Encrypt the message. */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static byte[] encryptMsg(Context context,String message)
            throws Exception {
        Cipher cipher = null;
        cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, FIXED_IV));
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.encodeToString(cipherText, 0).getBytes();
    }

    /* Decrypt the message, given derived encContentValues and initialization vector. */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public  static String decryptMsg(Context context,byte[] cipherText)
            throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, FIXED_IV));
        byte[] decryptString = cipher.doFinal(Base64.decode(cipherText, 0));
        return new String(decryptString, "UTF-8");

    }

    public static java.security.Key getSecretKey() throws Exception {
        return keyStore.getKey(ALIAS_NAME, null);
    }

    /*Generate random key*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SecretKey generateKey()
            throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchProviderException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(ALIAS_NAME)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(ALIAS_NAME,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setRandomizedEncryptionRequired(false)
                            .build());
            return keyGenerator.generateKey();
        }

        return null;
    }
}
