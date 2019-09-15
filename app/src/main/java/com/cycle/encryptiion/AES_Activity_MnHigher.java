package com.cycle.encryptiion;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class AES_Activity_MnHigher extends AppCompatActivity {

    private static final String TAG = "AES_Activity";
    byte[] encryptedMsg;
    TextView result;
    EditText message;
    Button encrypt, decrypt;
    private String mSignatureStr;

    KeyStore keyStore;
    String ALIAS_NAME = "alias";    //Alias name for key
    private static final byte[] FIXED_IV = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}; //initialization vector

    private final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private final String AES_MODE = "AES/GCM/NoPadding";

    /*Generate random key*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    public SecretKey generateKey()
            throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchProviderException {
        keyStore  = KeyStore.getInstance("AndroidKeyStore");
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
            return  keyGenerator.generateKey();
        }

        return null;
    }

    private java.security.Key getSecretKey(Context context) throws Exception {
        return keyStore.getKey(ALIAS_NAME, null);
    }



    /* Encrypt the message. */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public byte[] encryptMsg(String message)
            throws Exception {
        Cipher cipher = null;
        cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(getApplicationContext()), new GCMParameterSpec(128, FIXED_IV));
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return cipherText;
    }

    /* Decrypt the message, given derived encContentValues and initialization vector. */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String decryptMsg(byte[] cipherText)
            throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(getApplicationContext()), new GCMParameterSpec(128, FIXED_IV));
        byte[] decryptString = cipher.doFinal(cipherText);
        return new String(decryptString, "UTF-8");

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aes);

        message = findViewById(R.id.message);
        result = findViewById(R.id.result);
        encrypt = findViewById(R.id.encrypt);
        decrypt = findViewById(R.id.decrypt);

        encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    encryptedMsg = encryptMsg(message.getText().toString());
                    result.setText(new String(encryptedMsg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String decryptedMsg = null;
                try {
                    decryptedMsg = decryptMsg(encryptedMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.setText(decryptedMsg);

            }
        });


        try {
            generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
