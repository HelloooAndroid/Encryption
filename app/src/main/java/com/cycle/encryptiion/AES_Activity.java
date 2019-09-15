package com.cycle.encryptiion;

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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES_Activity extends AppCompatActivity {

    public static final String SAMPLE_INPUT = "Hello, Android!";
    private static final int KEY_LEN = 16; //16, 24, 32 either of these will work.. as per requirement
    private static final String TAG = "AES_Activity";
    SecretKey secret = null;
    byte[] encryptedMsg;
    TextView result;
    EditText message;
    Button encrypt, decrypt;
    private String mSignatureStr;

    /*Generate random key*/
    public static SecretKey generateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] material = new byte[KEY_LEN];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(material);
        SecretKey secretKey = new SecretKeySpec(material, "AES");

        return secretKey;
    }



    /* Encrypt the message. */
    public static byte[] encryptMsg(String message, SecretKey secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, IllegalBlockSizeException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return cipherText;
    }

    /* Decrypt the message, given derived encContentValues and initialization vector. */
    public static String decryptMsg(byte[] cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        return decryptString;
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
                    encryptedMsg = encryptMsg(message.getText().toString(), secret);
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
                    decryptedMsg = decryptMsg(encryptedMsg, secret);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.setText(decryptedMsg);

            }
        });


        try {
            secret = generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
