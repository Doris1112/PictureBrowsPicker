package com.doris.picture.library.picker.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.doris.picture.library.R;
import com.doris.picture.library.picker.adapter.PreviewPagerAdapter;
import com.doris.picture.library.picker.entity.IncapableCause;
import com.doris.picture.library.picker.entity.Item;
import com.doris.picture.library.picker.entity.SelectionSpec;
import com.doris.picture.library.picker.fragment.PreviewItemFragment;
import com.doris.picture.library.picker.listener.OnFragmentInteractionListener;
import com.doris.picture.library.picker.utils.PhotoMetadataUtils;
import com.doris.picture.library.picker.utils.collection.SelectedItemCollection;
import com.doris.picture.library.picker.widget.CheckRadioView;
import com.doris.picture.library.picker.widget.CheckView;
import com.doris.picture.library.picker.widget.IncapableDialog;

/**
 * @author Doris
 * @date 2018/12/4
 */
public abstract class BasePreviewActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, OnFragmentInteractionListener {

    public static final String EXTRA_DEFAULT_BUNDLE = "extra_default_bundle";
    public static final String EXTRA_RESULT_BUNDLE = "extra_result_bundle";
    public static final String EXTRA_RESULT_APPLY = "extra_result_apply";
    public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    public static final String CHECK_STATE = "checkState";

    protected final SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    protected SelectionSpec mSpec;
    protected ViewPager mPager;

    protected PreviewPagerAdapter mAdapter;

    protected CheckView mCheckView;
    protected TextView mButtonBack;
    protected TextView mButtonApply;
    protected TextView mSize;

    protected int mPreviousPos = -1;

    private LinearLayout mOriginalLayout;
    private CheckRadioView mOriginal;
    protected boolean mOriginalEnable;

    private FrameLayout mBottomToolbar;
    private FrameLayout mTopToolbar;
    private boolean mIsToolbarHide = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(SelectionSpec.getInstance().themeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_picker_media_preview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mSpec = SelectionSpec.getInstance();

        if (savedInstanceState == null) {
            mSelectedCollection.onCreate(getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE));
            mOriginalEnable = getIntent().getBooleanExtra(EXTRA_RESULT_ORIGINAL_ENABLE, false);
        } else {
            mSelectedCollection.onCreate(savedInstanceState);
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }

        initWidget();

        mPager.addOnPageChangeListener(this);
        mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null);
        mPager.setAdapter(mAdapter);
        mButtonBack.setOnClickListener(this);
        mButtonApply.setOnClickListener(this);
        mCheckView.setCountable(mSpec.countable);
        mCheckView.setOnClickListener(this);
        mOriginalLayout.setOnClickListener(this);
        updateApplyButton();
    }

    private void initWidget(){
        mPager = findViewById(R.id.picture_picker_pager);
        mBottomToolbar = findViewById(R.id.picture_picker_bottom_toolbar);
        mButtonBack = findViewById(R.id.picture_picker_btn_back);
        mOriginalLayout = findViewById(R.id.picture_picker_original_layout);
        mOriginal = findViewById(R.id.picture_picker_original);
        mSize = findViewById(R.id.picture_picker_size);
        mButtonApply = findViewById(R.id.picture_picker_btn_apply);
        mTopToolbar = findViewById(R.id.picture_picker_top_toolbar);
        mCheckView = findViewById(R.id.picture_picker_check_view);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mSelectedCollection.onSaveInstanceState(outState);
        outState.putBoolean("checkState", mOriginalEnable);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        sendBackResult(false);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.picture_picker_btn_back) {
            onBackPressed();
        } else if (v.getId() == R.id.picture_picker_btn_apply) {
            sendBackResult(true);
            finish();
        } else if (v.getId() == R.id.picture_picker_check_view){
            checkViewClick();
        } else if (v.getId() == R.id.picture_picker_original_layout){
            originalLayoutClick();
        }
    }

    private void checkViewClick(){
        Item item = mAdapter.getMediaItem(mPager.getCurrentItem());
        if (mSelectedCollection.isSelected(item)) {
            mSelectedCollection.remove(item);
            if (mSpec.countable) {
                mCheckView.setCheckedNum(CheckView.UNCHECKED);
            } else {
                mCheckView.setChecked(false);
            }
        } else {
            if (assertAddSelection(item)) {
                mSelectedCollection.add(item);
                if (mSpec.countable) {
                    mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
                } else {
                    mCheckView.setChecked(true);
                }
            }
        }
        updateApplyButton();
        if (mSpec.onSelectedListener != null) {
            mSpec.onSelectedListener.onSelected(
                    mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
        }
    }

    private void originalLayoutClick(){
        int count = countOverMaxSize();
        if (count > 0) {
            IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                    getString(R.string.error_over_original_count, count, mSpec.originalMaxSize));
            incapableDialog.show(getSupportFragmentManager(),
                    IncapableDialog.class.getName());
            return;
        }
        mOriginalEnable = !mOriginalEnable;
        mOriginal.setChecked(mOriginalEnable);
        if (!mOriginalEnable) {
            mOriginal.setColor(Color.WHITE);
        }
        if (mSpec.onCheckedListener != null) {
            mSpec.onCheckedListener.onCheck(mOriginalEnable);
        }
    }

    @Override
    public void onClick() {
        if (!mSpec.autoHideToolbar) {
            return;
        }
        if (mIsToolbarHide) {
            mTopToolbar.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(mTopToolbar.getMeasuredHeight())
                    .start();
            mBottomToolbar.animate()
                    .translationYBy(-mBottomToolbar.getMeasuredHeight())
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        } else {
            mTopToolbar.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(-mTopToolbar.getMeasuredHeight())
                    .start();
            mBottomToolbar.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(mBottomToolbar.getMeasuredHeight())
                    .start();
        }

        mIsToolbarHide = !mIsToolbarHide;

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mPager.getAdapter();
        if (mPreviousPos != -1 && mPreviousPos != position) {
            ((PreviewItemFragment) adapter.instantiateItem(mPager, mPreviousPos)).resetView();

            Item item = adapter.getMediaItem(position);
            if (mSpec.countable) {
                int checkedNum = mSelectedCollection.checkedNumOf(item);
                mCheckView.setCheckedNum(checkedNum);
                if (checkedNum > 0) {
                    mCheckView.setEnabled(true);
                } else {
                    mCheckView.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            } else {
                boolean checked = mSelectedCollection.isSelected(item);
                mCheckView.setChecked(checked);
                if (checked) {
                    mCheckView.setEnabled(true);
                } else {
                    mCheckView.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            }
            updateSize(item);
        }
        mPreviousPos = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateApplyButton() {
        int selectedCount = mSelectedCollection.count();
        if (selectedCount == 0) {
            mButtonApply.setText(R.string.button_sure_default);
            mButtonApply.setEnabled(false);
        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
            mButtonApply.setText(R.string.button_sure_default);
            mButtonApply.setEnabled(true);
        } else {
            mButtonApply.setEnabled(true);
            mButtonApply.setText(getString(R.string.button_sure, selectedCount));
        }

        if (mSpec.originalMap) {
            mOriginalLayout.setVisibility(View.VISIBLE);
            updateOriginalState();
        } else {
            mOriginalLayout.setVisibility(View.GONE);
        }
    }


    private void updateOriginalState() {
        mOriginal.setChecked(mOriginalEnable);
        if (!mOriginalEnable) {
            mOriginal.setColor(Color.WHITE);
        }

        if (countOverMaxSize() > 0) {

            if (mOriginalEnable) {
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.error_over_original_size, mSpec.originalMaxSize));
                incapableDialog.show(getSupportFragmentManager(),
                        IncapableDialog.class.getName());

                mOriginal.setChecked(false);
                mOriginal.setColor(Color.WHITE);
                mOriginalEnable = false;
            }
        }
    }


    private int countOverMaxSize() {
        int count = 0;
        int selectedCount = mSelectedCollection.count();
        for (int i = 0; i < selectedCount; i++) {
            Item item = mSelectedCollection.asList().get(i);
            if (item.isImage()) {
                float size = PhotoMetadataUtils.getSizeInMB(item.size);
                if (size > mSpec.originalMaxSize) {
                    count++;
                }
            }
        }
        return count;
    }

    protected void updateSize(Item item) {
        if (item.isGif()) {
            mSize.setVisibility(View.VISIBLE);
            mSize.setText(PhotoMetadataUtils.getSizeInMB(item.size) + "M");
        } else {
            mSize.setVisibility(View.GONE);
        }

        if (item.isVideo()) {
            mOriginalLayout.setVisibility(View.GONE);
        } else if (mSpec.originalMap) {
            mOriginalLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void sendBackResult(boolean apply) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(EXTRA_RESULT_APPLY, apply);
        intent.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
        setResult(Activity.RESULT_OK, intent);
    }

    private boolean assertAddSelection(Item item) {
        IncapableCause cause = mSelectedCollection.isAcceptable(item);
        IncapableCause.handleCause(this, cause);
        return cause == null;
    }
}
