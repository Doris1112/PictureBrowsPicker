package com.doris.picturebrowspicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.doris.picture.library.brows.PictureBrows;
import com.doris.picture.library.picker.PicturePicker;
import com.doris.picture.library.picker.entity.PicturePickerMediaType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Doris
 * @date 2018/12/3
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PICTURE_PICKER = 1001;
    private static final int REQUEST_PICTURE_CROP = 1002;

    private TextView mPictures;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPictures = findViewById(R.id.pictureList);
    }

    public void onMainViewClicked(View view) {
        switch (view.getId()) {
            case R.id.pictureBrows:
                // 浏览图片
                PictureBrows.build(this)
                        .setImages(getImages())
                        .isSave(true)
                        .setSavePath(PictureData.SAVE_IMG_PATH)
                        .setNames(getNames())
                        .start();
                break;
            case R.id.picturePicker:
                PicturePicker.from(this)
                        .choose(PicturePickerMediaType.ofImage())
                        .countable(true)
                        .maxSelectable(9)
                        .forResult(REQUEST_PICTURE_PICKER);
                break;
            case R.id.picturePickerCrop:
                PicturePicker.from(this)
                        .choose(PicturePickerMediaType.ofImage())
                        .capture(true)
                        .crop(true)
                        .countable(false)
                        .forResult(REQUEST_PICTURE_PICKER);
                break;
            case R.id.pictureCrop:
                if (uri != null){
                    PicturePicker.from(this).crop()
                            .cropUri(uri)
                            .onlyCropForResult(REQUEST_PICTURE_CROP);
                }
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
            mPictures.setText(String.format("uri.size=%s, paths.size=%s",
                    uris.size(), paths.size()));
            mPictures.append("\n");
            if (uris.size() > 0){
                uri = uris.get(0);
            }
            for (String path : paths) {
                mPictures.append(path);
                mPictures.append("\n");
            }
        } else if (requestCode == REQUEST_PICTURE_CROP){
            Uri uri = PicturePicker.obtainCropResult(data);
            String path = PicturePicker.obtainCropPathResult(data);
            mPictures.setText(uri.toString());
            mPictures.append("\n");
            mPictures.append(path);
        }
    }
}
