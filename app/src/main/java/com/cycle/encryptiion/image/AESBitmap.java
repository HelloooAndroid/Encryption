package com.cycle.encryptiion.image;

import android.graphics.Bitmap;

public class AESBitmap {

	private Bitmap b;
	private Boolean error;
	private Boolean finished;
	
	AESBitmap(Bitmap b, Boolean error, Boolean finished) {
		this.b = b;
		this.error = error;
		this.finished = finished;
	}
	
	public Bitmap bitmap() {
		return b;
	}
	
	public Boolean error() {
		return error;
	}
	
	public Boolean finished() {
		return finished;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AESBitmap) {
			AESBitmap that = (AESBitmap)o;
			if (this.b.equals(that.b) &&
					this.error == that.error &&
					this.finished == that.finished)
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return b.hashCode();
	}
	
}
