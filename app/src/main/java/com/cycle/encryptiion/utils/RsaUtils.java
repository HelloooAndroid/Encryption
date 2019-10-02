package com.cycle.encryptiion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class RsaUtils {
    private static final String AES_MODE = "AES/ECB/PKCS7Padding";
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String KEY_ALIAS = "vDB";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final String TAG = "ENCRYPTION_LOG";
    static final String ENCRYPTED_AES_KEY = "encrypted_AES";
    static final String SHARED_PREFENCE_NAME = "Key_pref";
    static KeyStore keyStore;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void generateRSA_KeyPair(Context context) throws Exception {
        keyStore = KeyStore.getInstance(AndroidKeyStore);
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(1, 30);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context).setAlias(KEY_ALIAS).setSubject(new X500Principal("CN=vDB")).setSerialNumber(BigInteger.TEN).setStartDate(start.getTime()).setEndDate(end.getTime()).build();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", AndroidKeyStore);
            kpg.initialize(spec);
            kpg.generateKeyPair();
        }
    }

    public static  void generateAndStoreAES(Context context) throws Exception {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, 0);
        if (pref.getString(ENCRYPTED_AES_KEY, null) == null) {
            byte[] randomKey = new byte[16];
            new SecureRandom().nextBytes(randomKey);
            String enryptedKeyB64 = Base64.encodeToString(rsaEncrypt(randomKey), 0);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(ENCRYPTED_AES_KEY, enryptedKeyB64);
            edit.commit();
        }
    }

    public static  byte[] rsaEncrypt(byte[] randomKey) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        Cipher inputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
        inputCipher.init(1, privateKeyEntry.getCertificate().getPublicKey());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(randomKey);
        cipherOutputStream.close();
        return outputStream.toByteArray();
    }

    public static  byte[] rsaDecrypt(byte[] encrypted) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        Cipher output = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
        output.init(2, privateKeyEntry.getPrivateKey());
        CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(encrypted), output);
        ArrayList<Byte> values = new ArrayList<>();
        while (true) {
            int read = cipherInputStream.read();
            int nextByte = read;
            if (read == -1) {
                break;
            }
            values.add(Byte.valueOf((byte) nextByte));
        }
        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = ((Byte) values.get(i)).byteValue();
        }
        return bytes;
    }

    public static  Key getSecretKey(Context context2) throws Exception {
        String enryptedKeyB64 = context2.getSharedPreferences(SHARED_PREFENCE_NAME, 0).getString(ENCRYPTED_AES_KEY, null);
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("getSecretKey:");
        sb.append(enryptedKeyB64);
        Log.d(str, sb.toString());
        return new SecretKeySpec(rsaDecrypt(Base64.decode(enryptedKeyB64, 0)), "AES");
    }

    public static  byte[] encryptMsg(Context context2, String input) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE, "BC");
        c.init(1, getSecretKey(context2));
            return Base64.encodeToString(c.doFinal(input.getBytes()), 0).getBytes();
    }

    public static  String decryptMsg(Context context2, byte[] encrypted) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE, "BC");
        c.init(2, getSecretKey(context2));
        return new String(c.doFinal(Base64.decode(encrypted, 0)),"UTF-8");
    }
}













