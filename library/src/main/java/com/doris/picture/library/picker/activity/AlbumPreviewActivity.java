package com.doris.picture.library.picker.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.doris.picture.library.picker.activity.base.BasePreviewActivity;
import com.doris.picture.library.picker.adapter.PreviewPagerAdapter;
import com.doris.picture.library.picker.entity.Album;
import com.doris.picture.library.picker.entity.Item;
import com.doris.picture.library.picker.utils.collection.AlbumMediaCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class AlbumPreviewActivity extends BasePreviewActivity implements
        AlbumMediaCollection.AlbumMediaCallbacks {

    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_ITEM = "extra_item";

    private AlbumMediaCollection mCollection = new AlbumMediaCollection();

    private boolean mIsAlreadySetPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCollection.onCreate(this, this);
        Album album = getIntent().getParcelableExtra(EXTRA_ALBUM);
        mCollection.load(album);

        Item item = getIntent().getParcelableExtra(EXTRA_ITEM);
        if (mSpec.countable) {
            mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
        } else {
            mCheckView.setChecked(mSelectedCollection.isSelected(item));
        }
        updateSize(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCollection.onDestroy();
    }

    @Override
    public void onAlbumMediaLoad(Cursor cursor) {
        List<Item> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(Item.valueOf(cursor));
        }
        if (items.isEmpty()) {
            return;
        }

        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mPager.getAdapter();
        adapter.addAll(items);
        adapter.notifyDataSetChanged();
        if (!mIsAlreadySetPosition) {
            mIsAlreadySetPosition = true;
            Item selected = getIntent().getParcelableExtra(EXTRA_ITEM);
            int selectedIndex = items.indexOf(selected);
            mPager.setCurrentItem(selectedIndex, false);
            mPreviousPos = selectedIndex;
        }
    }

    @Override
    public void onAlbumMediaReset() {

    }
}
