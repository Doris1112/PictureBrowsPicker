package com.doris.picture.library.picker.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.doris.picture.library.picker.activity.base.BasePreviewActivity;
import com.doris.picture.library.picker.entity.Item;
import com.doris.picture.library.picker.utils.collection.SelectedItemCollection;

import java.util.List;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class SelectedPreviewActivity extends BasePreviewActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE);
        List<Item> selected = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
        mAdapter.addAll(selected);
        mAdapter.notifyDataSetChanged();
        if (mSpec.countable) {
            mCheckView.setCheckedNum(1);
        } else {
            mCheckView.setChecked(true);
        }
        mPreviousPos = 0;
        updateSize(selected.get(0));
    }
}
