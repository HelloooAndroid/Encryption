package com.cycle.encryptiion.image;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cycle.encryptiion.R;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AESImageAdapter extends BaseAdapter {

    static final String TAG = "ImageAdapter";
    static final String[] filePathColumn = {MediaStore.Images.Media.DATA};
    static final Uri EXTERNAL_IMAGE_URI = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    int mGalleryItemBackground;
    private Context mContext;
    private ArrayList<File> mFiles;
    private byte[] mPassword;
    private Handler mHandler;
    private AdapterBroadcastReceiver mBroadcastReciever;
    private ConcurrentHashMap<String, AESBitmap> mBitmaps = new ConcurrentHashMap<String, AESBitmap>();

    private int mImagePreviewHeight;
    private int mImagePreviewWidth;

    public AESImageAdapter(Context c, byte[] password, Handler handler, int imagePreviewHeight, int imagePreviewWidth) {
        super();
        mContext = c;
        mPassword = password;
        mHandler = handler;

        mImagePreviewHeight = imagePreviewHeight;
        mImagePreviewWidth = imagePreviewWidth;

        AESManager.createEncryptedFolder();

        mBroadcastReciever = new AdapterBroadcastReceiver();
        mContext.registerReceiver(mBroadcastReciever, new IntentFilter(AESManager.ACTION_ENCRYPTED_FILES_CHANGED), null, mHandler);
        refresh();
    }

    public static int delete(Context context, String filename) {
        return context.getContentResolver().delete(EXTERNAL_IMAGE_URI,
                String.format("%s = ?", filePathColumn[0]),
                new String[]{filename});
    }

    public void refresh() {
        mFiles = AESManager.listFiles();
        AESImageAdapter.this.notifyDataSetChanged();
    }

    public int getCount() {
        return mFiles.size();
    }

    public Object getItem(int position) {
        return mFiles.get(position).getAbsolutePath();
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        String imageFile = (String) getItem(position);
        ImageView i = new ImageView(mContext);
        AESBitmap b;

        synchronized (mBitmaps) {
            if (mBitmaps.containsKey(imageFile)) {
                b = mBitmaps.get(imageFile);
                if (b.error() == true) {
                    View v = new ImageErrorView(mContext, mContext.getString(R.string.load_error), imageFile);
                    v.setLayoutParams(new AbsListView.LayoutParams(mImagePreviewWidth, mImagePreviewHeight));
                    return v;
                } else if (b.finished() == false) {
                    View v = new ImageErrorView(mContext, mContext.getString(R.string.loading), imageFile);
                    v.setLayoutParams(new AbsListView.LayoutParams(mImagePreviewWidth, mImagePreviewHeight));
                    return v;
                } else {
                    i.setImageBitmap(b.bitmap());
                }
            } else {
                mBitmaps.put(imageFile, new AESBitmap(null, false, false));
                new LoadAESImage().executeOnExecutor(LoadAESImage.THREAD_POOL_EXECUTOR, imageFile);
                View v = new ImageErrorView(mContext, mContext.getString(R.string.loading), imageFile);
                return v;
            }
        }

        i.setLayoutParams(new AbsListView.LayoutParams(mImagePreviewWidth, mImagePreviewHeight));
        BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
        drawable.setAntiAlias(true);
        return i;
    }

    private class LoadAESImage extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... imageFile) {
            Bitmap b;
            // somebody else might have already loaded our image.
            synchronized (mBitmaps) {
                if (mBitmaps.containsKey(imageFile[0]) &&
                        (mBitmaps.get(imageFile[0]).finished() == true)) {
                    Log.d(TAG, "LoadAESImage redundant for " + imageFile[0]);
                    return false;
                }
            }
            try {
                AESInputStream is = new AESInputStream(new FileInputStream(imageFile[0]), mPassword);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);
                is.close();

                int fileWidth = options.outWidth;
                int fileHeight = options.outHeight;

                if (fileWidth == -1 || fileHeight == -1) {
                    mBitmaps.put(imageFile[0], new AESBitmap(null, true, true));
                    return false;
                }

                float widthRatio = (float) mImagePreviewWidth / (float) fileWidth;
                float heightRatio = (float) mImagePreviewHeight / (float) fileHeight;
                int realPreviewWidth, realPreviewHeight;
                int sample;
                if (widthRatio < heightRatio) {
                    realPreviewWidth = (int) (fileWidth * widthRatio);
                    realPreviewHeight = (int) (fileHeight * widthRatio);
                    sample = (int) ((1 / widthRatio) + 1.0f);
                } else {
                    realPreviewWidth = (int) (fileWidth * heightRatio);
                    realPreviewHeight = (int) (fileHeight * heightRatio);
                    sample = (int) ((1 / heightRatio) + 1.0f);
                }

                is = new AESInputStream(new FileInputStream(imageFile[0]), mPassword);

                options = new BitmapFactory.Options();

                options.inJustDecodeBounds = false;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.outHeight = realPreviewHeight;
                options.outWidth = realPreviewWidth;
                options.inSampleSize = sample;

                b = BitmapFactory.decodeStream(is, null, options);

                is.close();

                if (b == null) {
                    mBitmaps.put(imageFile[0], new AESBitmap(null, true, true));
                    return false;
                }

                mBitmaps.put(imageFile[0], new AESBitmap(b, false, true));

            } catch (Exception e) {
                e.printStackTrace();
                mBitmaps.put(imageFile[0], new AESBitmap(null, true, true));
                return false;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                mBitmaps.put(imageFile[0], new AESBitmap(null, true, true));
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean b) {
            AESImageAdapter.this.notifyDataSetChanged();
        }
    }

    private class AdapterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            AESImageAdapter.this.refresh();
        }
    }

}