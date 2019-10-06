package com.cycle.encryptiion.image;

import android.content.Context;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageErrorView extends LinearLayout {

	private TextView title;
	private TextView filename;
	
	public ImageErrorView(Context context, String message, String file) {
		super(context);
		
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(0xffaaaaaa);
		
		title = new TextView(context);
		title.setText(message);
		title.setTextSize(25);
		title.setPadding(5, 5, 5, 5);
		
		filename = new TextView(context);
		filename.setText(file);
		filename.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		filename.setTextSize(20);
		filename.setPadding(5, 5, 5, 5);
		
		addView(title);
		addView(filename);
	}

}