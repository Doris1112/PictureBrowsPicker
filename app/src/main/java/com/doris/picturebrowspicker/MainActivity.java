package com.doris.picturebrowspicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.doris.picture.library.brows.listener.SaveImageListener;
import com.doris.picture.library.brows.PictureBrows;
import com.doris.picture.library.picker.entity.PicturePickerMediaType;
import com.doris.picture.library.picker.utils.PicturePicker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Doris
 * @date 2018/12/3
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PICTURE_PICKER = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onMainViewClicked(View view) {
        switch (view.getId()) {
            case R.id.pictureBrows:
                // 浏览图片
                PictureBrows.build(this)
                        // 设置图片or图片集合(可添加Base64图片，这个操作会清空所有图片只留下设置图片)
//                        .setImage(PictureData.IMAGE1)
                        .setImages(getImages())
                        // 添加图片or图片集合(在原有图片的基础上再继续添加图片)
//                        .addImages(getImages())
//                        .addImage(PictureData.IMAGE1)
                        // 设置进入时展示第position张图片(以0开始)
                        .setPosition(1)
                        // 是否可以保存图片(默认不可以保存)
                        .isSave(true)
                        // 设置图片保存路径(以“/”结束，不包含图片名称)
                        .setSavePath(PictureData.SAVE_IMGE_PATH)
                        // 保存图片成功是否需要刷新媒体库(默认刷新)
                        .isRefresh(true)
                        // 设置保存图片名称or名称集合(以“.图片格式”结束，不包含路径) 图片名称数量必须与图片数量对应
//                        .setName(String.format("JPEG_%s.jpg", PictureUtils.getDataTimeString()))
                        .setNames(getNames())
                        // 添加保存图片名称or名称集合
//                        .addName(String.format("JPEG_%s.jpg", PictureUtils.getDataTimeString()))
//                        .addNames(getNames())
                        // 设置图片保存成功或失败监听
                        .setSaveImageListener(new SaveImageListener() {
                            @Override
                            public void onSuccess(String absolutePath) {
                                Log.d(TAG, "onSuccess: 保存图片成功的绝对路径" +
                                        absolutePath);
                            }

                            @Override
                            public void onFail() {
                                Log.d(TAG, "onFail: ");
                            }
                        })
                        // 跳转
                        .start();
                break;
            case R.id.picturePicker:
                PicturePicker.from(this)
                        .choose(PicturePickerMediaType.ofImage(), true)
                        .countable(true)
                        .maxSelectable(4)
                        .forResult(REQUEST_PICTURE_PICKER);
                break;
            default:
                break;
        }
    }

    private List<String> getImages() {
        List<String> images = new ArrayList<>();
        images.add(PictureData.IMAGE_BASE64.toString());
        images.add(PictureData.IMAGE1);
        images.add(PictureData.IMAGE2);
        images.add(PictureData.IMAGE3);
        images.add(PictureData.IMAGE4);
        images.add(PictureData.IMAGE5);
        images.add(PictureData.IMAGE6);
        images.add(PictureData.IMAGE7);
        images.add(PictureData.IMAGE8);
        images.add(PictureData.IMAGE9);
        return images;
    }

    private List<String> getNames() {
        List<String> images = new ArrayList<>();
        images.add("1.jpg");
        images.add("2.jpg");
        images.add("3.jpg");
        images.add("4.jpg");
        images.add("5.jpg");
        images.add("6.jpg");
        images.add("7.jpg");
        images.add("8.jpg");
        images.add("9.jpg");
        images.add("10.jpg");
        return images;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PICTURE_PICKER) {
            // 选择图片
            List<Uri> uris = PicturePicker.obtainResult(data);
            List<String> paths = PicturePicker.obtainPathResult(data);
            Log.d(TAG, "onActivityResult: uris.size()=" + uris.size() +
                    ", paths.size()=" + paths.size());
            for (String path : paths) {
                Log.d(TAG, "onActivityResult: " + path);
            }
        }
    }
}
