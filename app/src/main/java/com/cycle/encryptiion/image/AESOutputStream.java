package com.cycle.encryptiion.image;

import android.os.Build;

import com.cycle.encryptiion.text.utils.AesUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESOutputStream extends OutputStream {

	//private byte[] IV = new byte[16];
	private static final byte[] IV = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}; //initialization vector
	private int contentSize;
	private CipherOutputStream cos;
	
	public AESOutputStream(OutputStream os, byte[] k, int contentSize) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		super();
		
		// read the first four bytes as an integer, which is the image size
		byte[] imgSizeArray = ByteBuffer.allocate(4).putInt(contentSize).array();
		
		Cipher c;
		c = Cipher.getInstance("AES/GCM/NoPadding");
		SecretKeySpec keySpec = new SecretKeySpec(k, "AES");
		c.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec);

		/*try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				c.init(Cipher.ENCRYPT_MODE, AesUtils.getSecretKey()*//*, new GCMParameterSpec(128, IV)*//*);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}*/

		os.write(imgSizeArray);
		os.write(c.getIV());
		
		cos = new CipherOutputStream(os, c);

	}

	public byte[] getIV() {
		return IV;
	}
	
	public int getContentSize() {
		return contentSize;
	}
	
	@Override
	public void write(int c) throws IOException {
		cos.write(c);
	}
	
	@Override
	public void write(byte[] c) throws IOException {
		cos.write(c);
	}
	
	@Override
	public void close() throws IOException {
		cos.close();
	}

}
