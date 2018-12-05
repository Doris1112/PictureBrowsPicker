package com.doris.picture.library.picker.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.doris.picture.library.PictureUtils;
import com.doris.picture.library.R;
import com.doris.picture.library.picker.entity.SelectionSpec;
import com.steelkiwi.cropiwa.AspectRatio;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver;
import com.steelkiwi.cropiwa.shape.CropIwaOvalShape;

import java.io.File;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class CropActivity extends AppCompatActivity implements View.OnClickListener,
        CropIwaResultReceiver.Listener {

    private SelectionSpec mSpec;

    private CropIwaResultReceiver mCropResultReceiver;
    private CropIwaView mCropView;
    private String mSaveCropPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSpec = SelectionSpec.getInstance();
        setTheme(mSpec.themeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        Toolbar toolbar = findViewById(R.id.picture_picker_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mCropResultReceiver = new CropIwaResultReceiver();
        mCropResultReceiver.setListener(this);
        mCropResultReceiver.register(this);

        TextView mBtnCrop = findViewById(R.id.picture_picker_btn_crop);
        mBtnCrop.setOnClickListener(this);
        mCropView = findViewById(R.id.picture_picker_crop_view);
        TextView mEmpty = findViewById(R.id.picture_picker_crop_empty);
        if (mSpec.cropUri != null){
            mCropView.setImageUri(mSpec.cropUri);
        } else if (mSpec.cropBitmap != null){
            mCropView.setImage(mSpec.cropBitmap);
        } else {
            mEmpty.setVisibility(View.VISIBLE);
            mCropView.setVisibility(View.GONE);
            mBtnCrop.setVisibility(View.GONE);
        }
        if (mSpec.isOval) {
            mCropView.configureOverlay().setCropShape(
                    new CropIwaOvalShape(mCropView.configureOverlay()))
                    .setAspectRatio(new AspectRatio(mSpec.cropWidth, mSpec.cropHeight)).apply();
        } else {
            mCropView.configureOverlay().setAspectRatio(
                    new AspectRatio(mSpec.cropWidth, mSpec.cropHeight)).apply();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.picture_picker_btn_crop) {
            String[] permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (PictureUtils.checkPermissionAllGranted(this, permissions)) {
                saveCropImage();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions, PictureUtils.REQUEST_WRITE_PERMISSION);
            }
        }
    }

    private void saveCropImage(){
        // 裁剪
        File file = new File(getPath());
        mSaveCropPath = file.getAbsolutePath();
        mCropView.crop(new CropIwaSaveConfig.Builder(Uri.fromFile(file))
                .setCompressFormat(mSpec.cropCompressFormat)
                .setQuality(mSpec.cropQuality)
                .build());
    }

    private String getPath() {
        String path = mSpec.saveImagePath;
        if (TextUtils.isEmpty(mSpec.saveCropImageName)) {
            path += "IMG_" + PictureUtils.getDataTimeString();
            switch (mSpec.cropCompressFormat) {
                case PNG:
                    path += ".png";
                    break;
                case JPEG:
                    path += ".jpg";
                    break;
                case WEBP:
                    path += ".webp";
                    break;
                default:
                    break;
            }
        } else {
            path += mSpec.saveCropImageName;
        }
        return path;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PictureUtils.REQUEST_WRITE_PERMISSION) {
            boolean have = true;
            for (int item : grantResults) {
                if (item != PackageManager.PERMISSION_GRANTED) {
                    have = false;
                }
            }
            if (have) {
                saveCropImage();
            } else {
                Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onCropSuccess(Uri croppedUri) {
        Intent result = new Intent();
        result.putExtra(PictureUtils.EXTRA_RESULT_CROP_PATH, mSaveCropPath);
        result.putExtra(PictureUtils.EXTRA_RESULT_CROP_URI, croppedUri);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onCropFailed(Throwable e) {
        Toast.makeText(this, "图片裁剪失败！", Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCropResultReceiver.unregister(this);
    }
}
