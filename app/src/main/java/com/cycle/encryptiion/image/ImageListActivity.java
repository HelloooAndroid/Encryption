package com.cycle.encryptiion.image;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cycle.encryptiion.R;
import com.cycle.encryptiion.text.utils.AesUtils;
import com.cycle.encryptiion.text.utils.RsaUtils;

import static com.cycle.encryptiion.image.Const.mPasswordBytes;

public class ImageListActivity extends AppCompatActivity implements View.OnClickListener {
    static final String PASSWORD_HASH_PREF = "passwordhash";
    private static int IMAGE_PREVIEW_HEIGHT_DP = 300;
    private static int IMAGE_PREVIEW_WIDTH_DP = 300;
    private Context mContext;
    private AESImageAdapter mImageAdapter;
    private GridView gridView;
    private FloatingActionButton add_fab;
    private SharedPreferences mPreferences;
    private int mImagePreviewHeightPixels;
    private int mImagePreviewWidthPixels;
    private Handler mHandler;
    private String mStoredPasswordHash;
    private static final int ACTIVITY_SELECT_IMAGE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);
        mContext = this;

        mImagePreviewHeightPixels = (int) (IMAGE_PREVIEW_HEIGHT_DP * getResources().getDisplayMetrics().density + 0.5f);
        mImagePreviewWidthPixels = (int) (IMAGE_PREVIEW_WIDTH_DP * getResources().getDisplayMetrics().density + 0.5f);

        add_fab = findViewById(R.id.add_fab);
        add_fab.setOnClickListener(this);
        gridView = findViewById(R.id.grid_view);
        gridView.setColumnWidth(mImagePreviewWidthPixels);
        gridView.setNumColumns(2);
        gridView.setOnItemClickListener(new ImagePreviewClickListener());

        mHandler = new Handler();

        mImageAdapter = new AESImageAdapter(this, mPasswordBytes, mHandler, mImagePreviewHeightPixels, mImagePreviewWidthPixels);
        gridView.setAdapter(mImageAdapter);

        generateKey();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_fab:
                if (isStoragePermissionGranted()) {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            //android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
                    //showFileChooser();
                }
                break;
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACTIVITY_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    final String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    final String newFilePath = AESManager.encryptedFolderString() + filePath;

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(String.format(getString(R.string.confirm_encrypt), filePath));
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AESManager.encrypt(mPasswordBytes, filePath, newFilePath, ImageListActivity.this, null);
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, null);
                    builder.create().show();
                }
        }
    }

    class ImagePreviewClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*String filename = (String)mImageAdapter.getItem(position);
            Intent imageIntent = new Intent(mContext, ImageActivity.class);
            imageIntent.setAction(Intent.ACTION_VIEW);
            imageIntent.putExtra(ImageActivity.BUNDLE_FILENAME, filename);
            imageIntent.putExtra(ImageActivity.BUNDLE_PASSWORD, mPasswordBytes);
            startActivity(imageIntent);*/
        }
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

}
