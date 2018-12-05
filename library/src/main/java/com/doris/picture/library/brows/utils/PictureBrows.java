package com.doris.picture.library.brows.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.doris.picture.library.brows.activity.PictureBrowsActivity;
import com.doris.picture.library.brows.listener.SaveImageListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Doris
 * @date 2018/12/3
 */
public class PictureBrows {

    private static final String TAG = PictureBrows.class.getSimpleName();

    /**
     * 图片
     */
    public static final String EXTRA_IMAGE = "image";
    public static final String EXTRA_POSITION = "imagePosition";
    /**
     * 是否可保存图片
     */
    public static final String EXTRA_SAVE = "imageSave";
    public static final String EXTRA_SAVE_PATH = "imageSavePath";
    public static final String EXTRA_SAVE_NAME = "imageSaveName";
    /**
     * 是否需要刷新媒体库
     */
    public static final String EXTRA_REFRESH = "imageRefresh";

    private Context mContext;

    private String mSavePath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/brows/";
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<>();
    private int mPosition;
    private boolean mIsSave, mIsRefresh = true;
    private SaveImageListener mSaveImageListener;

    private PictureBrows(Context context) {
        mContext = context;
        mkdirsFileDirectory();
    }

    public static PictureBrows build(Context context) {
        return new PictureBrows(context);
    }

    public PictureBrows setSavePath(String savePath) {
        if(!TextUtils.isEmpty(savePath)){
            mSavePath = savePath;
        }
        mkdirsFileDirectory();
        return this;
    }

    private void mkdirsFileDirectory(){
        File file = new File(mSavePath);
        if (!file.exists()) {
            if (!file.mkdirs()){
                Log.e(TAG, "PictureBrows: 创建文件夹失败");
            }
        }
    }

    public PictureBrows setImages(List<String> images) {
        mImages.clear();
        return addImages(images);
    }

    public PictureBrows setImage(String image) {
        mImages.clear();
        return addImage(image);
    }

    public PictureBrows addImages(List<String> images) {
        if (images != null && images.size() > 0) {
            mImages.addAll(images);
        }
        return this;
    }

    public PictureBrows addImage(String image) {
        if (!TextUtils.isEmpty(image)) {
            mImages.add(image);
        }
        return this;
    }

    public PictureBrows setNames(List<String> names) {
        mNames.clear();
        return addNames(names);
    }

    public PictureBrows setName(String name) {
        mNames.clear();
        return addName(name);
    }

    public PictureBrows addNames(List<String> names) {
        if (names != null && names.size() > 0) {
            mNames.addAll(names);
        }
        return this;
    }

    public PictureBrows addName(String name) {
        if (!TextUtils.isEmpty(name)) {
            mNames.add(name);
        }
        return this;
    }

    public PictureBrows setPosition(int position) {
        mPosition = position;
        return this;
    }

    public PictureBrows isSave(boolean isSave) {
        mIsSave = isSave;
        return this;
    }

    public PictureBrows isRefresh(boolean isRefresh) {
        mIsRefresh = isRefresh;
        return this;
    }

    public PictureBrows setSaveImageListener(SaveImageListener listener){
        mSaveImageListener = listener;
        return this;
    }

    public void start(){
        PictureBrowsActivity.mSaveImageListener = mSaveImageListener;

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(EXTRA_IMAGE, mImages);
        bundle.putStringArrayList(EXTRA_SAVE_NAME, mNames);
        Intent intent = new Intent(mContext, PictureBrowsActivity.class);
        intent.putExtras(bundle);
        intent.putExtra(EXTRA_POSITION, mPosition);
        intent.putExtra(EXTRA_SAVE, mIsSave);
        intent.putExtra(EXTRA_SAVE_PATH, mSavePath);
        intent.putExtra(EXTRA_REFRESH, mIsRefresh);
        mContext.startActivity(intent);
    }
}
