package com.cycle.encryptiion.text;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cycle.encryptiion.R;
import com.cycle.encryptiion.text.utils.AesUtils;
import com.cycle.encryptiion.text.utils.RsaUtils;

public class TextEncryptionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TextEncryptionActivity";
    byte[] encryptedMsgBytes;
    TextView result;
    EditText message;
    Button encrypt, decrypt;
    private String mSignatureStr;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption);

        mContext = this;

        message = findViewById(R.id.message);
        result = findViewById(R.id.result);
        encrypt = findViewById(R.id.encrypt);
        decrypt = findViewById(R.id.decrypt);

        encrypt.setOnClickListener(this);
        decrypt.setOnClickListener(this);


        generateKey();


    }

    private void generateKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                AesUtils.generateKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) {
            try {
                RsaUtils.generateRSA_KeyPair(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                RsaUtils.generateAndStoreAES(mContext);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.encrypt:
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        encryptedMsgBytes = AesUtils.encryptMsg(mContext, message.getText().toString());
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        encryptedMsgBytes = RsaUtils.encryptMsg(mContext, message.getText().toString());
                    }
                    result.setText(new String(encryptedMsgBytes));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.decrypt:
                String decryptedMsg = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        decryptedMsg = AesUtils.decryptMsg(mContext, encryptedMsgBytes);
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        decryptedMsg = RsaUtils.decryptMsg(mContext, encryptedMsgBytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.setText(decryptedMsg);
                break;
        }
    }
}
