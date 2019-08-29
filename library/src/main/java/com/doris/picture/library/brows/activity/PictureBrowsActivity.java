package com.doris.picture.library.brows.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.doris.picture.library.R;
import com.doris.picture.library.brows.adapter.PictureBrowsAdapter;
import com.doris.picture.library.brows.listener.SaveImageListener;
import com.doris.picture.library.PictureUtils;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import me.relex.circleindicator.CircleIndicator;

/**
 * @author Doris
 * @date 2018/12/3
 */
public class PictureBrowsActivity extends AppCompatActivity {

    private String mSavePath;
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<>();
    private int mPosition = 0;
    private boolean mIsSave, mIsRefresh = true;

    private ViewPager mViewPager;
    private CircleIndicator mIndicator;
    private TextView mImageLength;
    private ImageView mSaveBtn;
    private ProgressBar mSaveProgress;
    private Map<String, String> mSaveNames = new HashMap<>();
    private Map<String, Bitmap> mSaveBitmaps = new HashMap<>();
    private Map<String, Integer> mCurrentSaveImages = new HashMap<>();
    private Handler mHandler = new SaveImageHandler(this);

    public static SaveImageListener mSaveImageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 设置布局
        setContentView(R.layout.activity_picture_brows);
        // 初始化控件
        mViewPager = findViewById(R.id.picture_brows_pager);
        mIndicator = findViewById(R.id.picture_brows_indicator);
        mImageLength = findViewById(R.id.picture_brows_length);
        mSaveBtn = findViewById(R.id.picture_brows_save);
        mSaveProgress = findViewById(R.id.picture_brows_save_progress);
        TextView empty = findViewById(R.id.picture_brows_empty);
        // 初始化界面
        if (!initArgs(getIntent())) {
            mViewPager.setVisibility(View.GONE);
            mIndicator.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        } else {
            if (mNames != null && mNames.size() == mImages.size()) {
                for (int i = 0; i < mNames.size(); i++) {
                    mSaveNames.put(mImages.get(i), mNames.get(i));
                    setIsSaveFile(mImages.get(i), mNames.get(i));
                }
            }
            showImages();
            saveStatusUpdateUi(mPosition);
        }
    }

    private boolean initArgs(Intent intent) {
        mImages = intent.getStringArrayListExtra(PictureUtils.EXTRA_IMAGE);
        mNames = intent.getStringArrayListExtra(PictureUtils.EXTRA_SAVE_NAME);
        mPosition = intent.getIntExtra(PictureUtils.EXTRA_POSITION, mPosition);
        mIsSave = intent.getBooleanExtra(PictureUtils.EXTRA_SAVE, mIsSave);
        mSavePath = intent.getStringExtra(PictureUtils.EXTRA_SAVE_PATH);
        mIsRefresh = intent.getBooleanExtra(PictureUtils.EXTRA_REFRESH, mIsRefresh);
        return mImages != null && mImages.size() > 0;
    }

    private void showImages() {
        List<View> mViews = new ArrayList<>();
        if (mImages.size() == 1) {
            mViews.add(getItemView(mImages.get(0)));
            mIndicator.setVisibility(View.GONE);
        } else {
            for (String image : mImages) {
                mViews.add(getItemView(image));
            }
            if (mViews.size() > PictureUtils.MAX_INDICATOR) {
                mIndicator.setVisibility(View.GONE);
                mImageLength.setVisibility(View.VISIBLE);
                mImageLength.setText(String.format("1/%s", mImages.size()));
            }
        }
        mViewPager.setAdapter(new PictureBrowsAdapter(mViews));
        mIndicator.setViewPager(mViewPager);
        mViewPager.setCurrentItem(mPosition);
        mViewPager.setOffscreenPageLimit(mViews.size());
        mViewPager.setPageMargin(10);
        pageChange();

        if (mIsSave) {
            mSaveBtn.setVisibility(View.VISIBLE);
            mSaveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 获取写入权限
                    String[] permissions = new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (PictureUtils.checkPermissionAllGranted(PictureBrowsActivity.this,
                            permissions)) {
                        saveImage();
                    } else {
                        ActivityCompat.requestPermissions(PictureBrowsActivity.this,
                                permissions, PictureUtils.REQUEST_WRITE_PERMISSION);
                    }
                }
            });
        }
    }

    private void pageChange() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mImageLength.setText(String.format("%s/%s", position + 1, mImages.size()));
                saveStatusUpdateUi(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void saveStatusUpdateUi(int position){
        if (!mIsSave){
            return;
        }
        Integer status = mCurrentSaveImages.get(mImages.get(position));
        if (status == null) {
            mSaveProgress.setVisibility(View.GONE);
            mSaveBtn.setVisibility(View.VISIBLE);
            mSaveBtn.setImageResource(R.drawable.picture_save_img);
            return;
        }
        switch (status) {
            case PictureUtils.SAVE_STATUS_DOWNLOAD:
                mSaveBtn.setVisibility(View.GONE);
                mSaveProgress.setVisibility(View.VISIBLE);
                break;
            case PictureUtils.SAVE_STATUS_SUCCESS:
                mSaveProgress.setVisibility(View.GONE);
                mSaveBtn.setVisibility(View.VISIBLE);
                mSaveBtn.setImageResource(R.drawable.picture_done);
                break;
            case PictureUtils.SAVE_STATUS_FAIL:
                mSaveProgress.setVisibility(View.GONE);
                mSaveBtn.setVisibility(View.VISIBLE);
                mSaveBtn.setImageResource(R.drawable.picture_warning);
                break;
            default:
                mSaveProgress.setVisibility(View.GONE);
                mSaveBtn.setVisibility(View.VISIBLE);
                mSaveBtn.setImageResource(R.drawable.picture_save_img);
                break;
        }
    }

    private void setIsSaveFile(String url, String name){
        File file = new File(mSavePath + name);
        if (file.exists()){
            mCurrentSaveImages.put(url, PictureUtils.SAVE_STATUS_SUCCESS);
        }
    }

    private View getItemView(final String picturePath) {
        View view = LayoutInflater.from(this).inflate(R.layout.picture_brows_item, null);
        PhotoView photoView = view.findViewById(R.id.picture_brows_photo_view);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (!TextUtils.isEmpty(picturePath)) {
            setPicture(picturePath, photoView);
        } else {
            photoView.setImageResource(R.drawable.picture_error);
        }
        return view;
    }

    private void setPicture(String picturePath, PhotoView photoView) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.picture_loading)
                .error(R.drawable.picture_error);
        if (picturePath.substring(0, PictureUtils.START_LENGTH)
                .equalsIgnoreCase(PictureUtils.START_HTTP)) {
            // 网络图片
            Glide.with(this).asBitmap().load(picturePath).apply(options).into(photoView);
        } else if (picturePath.substring(0, PictureUtils.START_LENGTH)
                .equalsIgnoreCase(PictureUtils.START_DATA)) {
            // base64图片
            Bitmap bitmap = PictureUtils.stringToBitmap(picturePath);
            mSaveBitmaps.put(picturePath, bitmap);
            Glide.with(this).load(bitmap).apply(options).into(photoView);
        } else {
            // 本地图片文件
            Glide.with(this).asBitmap().load(new File(picturePath)).apply(options).into(photoView);
        }
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
                saveImage();
            } else {
                Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveImage() {
        final String pictureUrl = mImages.get(mViewPager.getCurrentItem());
        String pictureName = mSaveNames.get(pictureUrl);
        final String path;
        if (mSaveBitmaps.get(pictureUrl) != null) {
            path = mSavePath + (TextUtils.isEmpty(pictureName) ?
                    "IMG_" + PictureUtils.getDataTimeString() + ".jpg" : pictureName);
            if (PictureUtils.saveImg(mSaveBitmaps.get(pictureUrl), path)) {
                mCurrentSaveImages.put(pictureUrl, PictureUtils.SAVE_STATUS_SUCCESS);
                mSaveProgress.setVisibility(View.GONE);
                mSaveBtn.setVisibility(View.VISIBLE);
                mSaveBtn.setImageResource(R.drawable.picture_done);
                if (mSaveImageListener != null) {
                    mSaveImageListener.onSuccess(path);
                }
                if (mIsRefresh) {
                    PictureUtils.updateMedia(PictureBrowsActivity.this, path);
                }
            } else {
                mCurrentSaveImages.put(pictureUrl, PictureUtils.SAVE_STATUS_FAIL);
                mSaveProgress.setVisibility(View.GONE);
                mSaveBtn.setVisibility(View.VISIBLE);
                mSaveBtn.setImageResource(R.drawable.picture_warning);
                if (mSaveImageListener != null) {
                    mSaveImageListener.onFail();
                }
            }
        } else {
            path = mSavePath + (TextUtils.isEmpty(pictureName) ?
                    "IMG_" + PictureUtils.getDataTimeString() + pictureUrl.substring(
                            pictureUrl.lastIndexOf(".")) : pictureName);
            mCurrentSaveImages.put(pictureUrl, PictureUtils.SAVE_STATUS_DOWNLOAD);
            mSaveBtn.setVisibility(View.GONE);
            mSaveProgress.setVisibility(View.VISIBLE);
            newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    downloadImage(pictureUrl, path);
                }
            });
        }
    }

    private ExecutorService newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1,
                0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new SaveImageThreadFactory());
    }

    private void downloadImage(String imageUrl, String path) {
        try {
            InputStream inputStream = new FileInputStream(Glide.with(PictureBrowsActivity.this)
                    .asFile().load(imageUrl).submit().get());
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
            int hasRead;
            while ((hasRead = inputStream.read()) != -1) {
                fileOutputStream.write(hasRead);
            }
            fileOutputStream.close();
            inputStream.close();
            Message message = new Message();
            message.what = PictureUtils.SAVE_STATUS_SUCCESS;
            message.setData(new Bundle());
            message.getData().putString(PictureUtils.EXTRA_IMAGE_URL, imageUrl);
            message.getData().putString(PictureUtils.EXTRA_IMAGE_PATH, path);
            mHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            Message message = new Message();
            message.what = PictureUtils.SAVE_STATUS_FAIL;
            message.setData(new Bundle());
            message.getData().putString(PictureUtils.EXTRA_IMAGE_URL, imageUrl);
            mHandler.sendMessage(message);
        }
    }

    public void downloadImageSuccess(String imageUrl, String path) {
        mCurrentSaveImages.put(imageUrl, PictureUtils.SAVE_STATUS_SUCCESS);
        if (mSaveImageListener != null) {
            mSaveImageListener.onSuccess(path);
        }
        String currentImage = mImages.get(mViewPager.getCurrentItem());
        if (!currentImage.equals(imageUrl)) {
            return;
        }
        mSaveProgress.setVisibility(View.GONE);
        mSaveBtn.setVisibility(View.VISIBLE);
        mSaveBtn.setImageResource(R.drawable.picture_done);
        if (mIsRefresh) {
            PictureUtils.updateMedia(PictureBrowsActivity.this, path);
        }
    }

    public void downloadImageFail(String imageUrl) {
        mCurrentSaveImages.put(imageUrl, PictureUtils.SAVE_STATUS_FAIL);
        String currentImage = mImages.get(mViewPager.getCurrentItem());
        if (mSaveImageListener != null) {
            mSaveImageListener.onFail();
        }
        if (!currentImage.equals(imageUrl)) {
            return;
        }
        mSaveProgress.setVisibility(View.GONE);
        mSaveBtn.setVisibility(View.VISIBLE);
        mSaveBtn.setImageResource(R.drawable.picture_warning);
    }

    private static class SaveImageHandler extends Handler {

        WeakReference<PictureBrowsActivity> mActivity;

        SaveImageHandler(PictureBrowsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.e("doris", "handleMessage: ");
            if (mActivity.get() == null || mActivity.get().isFinishing()) {
                return;
            }
            String imageUrl = msg.getData().getString(PictureUtils.EXTRA_IMAGE_URL);
            String imagePath = msg.getData().getString(PictureUtils.EXTRA_IMAGE_PATH);
            switch (msg.what) {
                case PictureUtils.SAVE_STATUS_SUCCESS:
                    mActivity.get().downloadImageSuccess(imageUrl, imagePath);
                    break;
                case PictureUtils.SAVE_STATUS_FAIL:
                    mActivity.get().downloadImageFail(imageUrl);
                    break;
                default:
                    break;
            }
        }
    }

    private class SaveImageThreadFactory implements ThreadFactory {

        private AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "保存图片线程" + threadNumber.getAndIncrement());
        }
    }

}