package com.doris.picture.library.picker.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doris.picture.library.PictureUtils;
import com.doris.picture.library.R;
import com.doris.picture.library.picker.adapter.AlbumMediaAdapter;
import com.doris.picture.library.picker.adapter.AlbumsAdapter;
import com.doris.picture.library.picker.entity.Album;
import com.doris.picture.library.picker.entity.Item;
import com.doris.picture.library.picker.entity.SelectionSpec;
import com.doris.picture.library.picker.fragment.MediaSelectionFragment;
import com.doris.picture.library.picker.utils.PathUtils;
import com.doris.picture.library.picker.utils.PhotoMetadataUtils;
import com.doris.picture.library.picker.utils.collection.AlbumCollection;
import com.doris.picture.library.picker.utils.collection.SelectedItemCollection;
import com.doris.picture.library.picker.utils.compat.MediaStoreCompat;
import com.doris.picture.library.picker.widget.AlbumsSpinner;
import com.doris.picture.library.picker.widget.CheckRadioView;
import com.doris.picture.library.picker.widget.IncapableDialog;

import java.util.ArrayList;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class PicturePickerActivity extends AppCompatActivity implements
        AlbumCollection.AlbumCallbacks, AdapterView.OnItemSelectedListener,
        MediaSelectionFragment.SelectionProvider, View.OnClickListener,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener,
        AlbumMediaAdapter.OnPhotoCapture {

    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private MediaStoreCompat mMediaStoreCompat;
    private SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    private SelectionSpec mSpec;

    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsAdapter mAlbumsAdapter;
    private TextView mButtonPreview;
    private TextView mButtonApply;
    private View mContainer;
    private View mEmptyView;

    private LinearLayout mOriginalLayout;
    private CheckRadioView mOriginal;
    private boolean mOriginalEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSpec = SelectionSpec.getInstance();
        setTheme(mSpec.themeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_picker);

        Toolbar toolbar = findViewById(R.id.picture_picker_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Drawable navigationIcon = toolbar.getNavigationIcon();
        TypedArray ta = getTheme().obtainStyledAttributes(new int[]{R.attr.albumElementColor});
        int color = ta.getColor(0, 0);
        ta.recycle();
        navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        if (mSpec.capture) {
            mMediaStoreCompat = new MediaStoreCompat(this);
        }

        mButtonPreview = findViewById(R.id.picture_picker_btn_preview);
        mButtonApply = findViewById(R.id.picture_picker_btn_apply);
        mContainer = findViewById(R.id.picture_picker_container);
        mEmptyView = findViewById(R.id.picture_picker_empty_view);
        mOriginalLayout = findViewById(R.id.picture_picker_original_layout);
        mOriginal = findViewById(R.id.picture_picker_original);

        mButtonPreview.setOnClickListener(this);
        mOriginalLayout.setOnClickListener(this);
        mButtonApply.setOnClickListener(this);

        mSelectedCollection.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(PictureUtils.CHECK_STATE);
        }
        mSelectedCollection.setDefaultSelection(mSpec.selectorList);
        updateBottomToolbar();

        mAlbumsAdapter = new AlbumsAdapter(this, null, false);
        mAlbumsSpinner = new AlbumsSpinner(this);
        mAlbumsSpinner.setOnItemSelectedListener(this);
        mAlbumsSpinner.setSelectedTextView((TextView) findViewById(R.id.picture_picker_selected_album));
        mAlbumsSpinner.setPopupAnchorView(toolbar);
        mAlbumsSpinner.setAdapter(mAlbumsAdapter);

        mAlbumCollection.onCreate(this, this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);

        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (PictureUtils.checkPermissionAllGranted(this,
                permissions)) {
            mAlbumCollection.loadAlbums();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions, PictureUtils.REQUEST_READ_PERMISSION);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
        mAlbumCollection.onSaveInstanceState(outState);
        outState.putBoolean(PictureUtils.CHECK_STATE, mOriginalEnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumCollection.onDestroy();
        mSpec.onCheckedListener = null;
        mSpec.onSelectedListener = null;
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
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == PictureUtils.REQUEST_CODE_PREVIEW) {
            Bundle resultBundle = data.getBundleExtra(PictureUtils.EXTRA_RESULT_BUNDLE);
            ArrayList<Item> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
            mOriginalEnable = data.getBooleanExtra(PictureUtils.EXTRA_RESULT_ORIGINAL_ENABLE, false);
            int collectionType = resultBundle.getInt(SelectedItemCollection.STATE_COLLECTION_TYPE,
                    SelectedItemCollection.COLLECTION_UNDEFINED);
            if (data.getBooleanExtra(PictureUtils.EXTRA_RESULT_APPLY, false)) {
                ArrayList<Uri> selectedUris = new ArrayList<>();
                ArrayList<String> selectedPaths = new ArrayList<>();
                if (selected != null) {
                    for (Item item : selected) {
                        selectedUris.add(item.getContentUri());
                        selectedPaths.add(PathUtils.getPath(this, item.getContentUri()));
                    }
                } else {
                    selected = new ArrayList<>();
                }
                setResultFinish(selectedUris, selected, selectedPaths);
            } else {
                mSelectedCollection.overwrite(selected, collectionType);
                Fragment mediaSelectionFragment = getSupportFragmentManager().findFragmentByTag(
                        MediaSelectionFragment.class.getSimpleName());
                if (mediaSelectionFragment instanceof MediaSelectionFragment) {
                    ((MediaSelectionFragment) mediaSelectionFragment).refreshMediaGrid();
                }
                updateBottomToolbar();
            }
        } else if (requestCode == PictureUtils.REQUEST_CODE_CAPTURE) {
            Uri uri = mMediaStoreCompat.getCurrentPhotoUri();
            String path = mMediaStoreCompat.getCurrentPhotoPath();
            if (mSpec.crop && mSpec.singleSelectionModeEnabled() && isCrop(path)) {
                Intent intent = new Intent(this, CropActivity.class);
                mSpec.cropUri = uri;
                startActivityForResult(intent, PictureUtils.REQUEST_CODE_CROP);
            } else {
                setResultFinish(uri, path);
            }
        } else if (requestCode == PictureUtils.REQUEST_CODE_CROP) {
            setResultFinish((Uri) data.getParcelableExtra(PictureUtils.EXTRA_RESULT_CROP_URI),
                    data.getStringExtra(PictureUtils.EXTRA_RESULT_CROP_PATH));
        }
    }

    private void setResultFinish(ArrayList<Uri> uris, ArrayList<Item> items, ArrayList<String> paths) {
        if (mSpec.crop && mSpec.singleSelectionModeEnabled() &&
                isCrop(paths.get(0))) {
            Intent intent = new Intent(this, CropActivity.class);
            mSpec.cropUri = uris.get(0);
            startActivityForResult(intent, PictureUtils.REQUEST_CODE_CROP);
        } else {
            Intent result = new Intent();
            result.putParcelableArrayListExtra(PictureUtils.EXTRA_RESULT_SELECTION, uris);
            result.putParcelableArrayListExtra(PictureUtils.EXTRA_RESULT_SELECTION_ITEM, items);
            result.putStringArrayListExtra(PictureUtils.EXTRA_RESULT_SELECTION_PATH, paths);
            result.putExtra(PictureUtils.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
            setResult(RESULT_OK, result);
            finish();
        }
    }

    private void setResultFinish(Uri uri, String path) {
        if (mSpec.isRefresh) {
            PictureUtils.updateMedia(this, path);
        }
        ArrayList<Uri> selected = new ArrayList<>();
        selected.add(uri);
        ArrayList<String> selectedPath = new ArrayList<>();
        selectedPath.add(path);
        Intent result = new Intent();
        result.putParcelableArrayListExtra(PictureUtils.EXTRA_RESULT_SELECTION, selected);
        result.putStringArrayListExtra(PictureUtils.EXTRA_RESULT_SELECTION_PATH, selectedPath);
        setResult(RESULT_OK, result);
        finish();
    }

    private void updateBottomToolbar() {
        int selectedCount = mSelectedCollection.count();
        if (selectedCount == 0) {
            mButtonPreview.setEnabled(false);
            mButtonApply.setEnabled(false);
            mButtonApply.setText(getString(R.string.button_sure_default));
        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
            mButtonPreview.setEnabled(true);
            mButtonApply.setText(R.string.button_sure_default);
            mButtonApply.setEnabled(true);
        } else {
            mButtonPreview.setEnabled(true);
            mButtonApply.setEnabled(true);
            mButtonApply.setText(getString(R.string.button_sure, selectedCount));
        }

        if (mSpec.originalMap) {
            mOriginalLayout.setVisibility(View.VISIBLE);
            updateOriginalState();
        } else {
            mOriginalLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void updateOriginalState() {
        mOriginal.setChecked(mOriginalEnable);
        if (countOverMaxSize() > 0) {
            if (mOriginalEnable) {
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.error_over_original_size, mSpec.originalMaxSize));
                incapableDialog.show(getSupportFragmentManager(),
                        IncapableDialog.class.getName());

                mOriginal.setChecked(false);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.picture_picker_btn_preview) {
            Intent intent = new Intent(this, SelectedPreviewActivity.class);
            intent.putExtra(PictureUtils.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
            intent.putExtra(PictureUtils.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
            startActivityForResult(intent, PictureUtils.REQUEST_CODE_PREVIEW);
        } else if (v.getId() == R.id.picture_picker_btn_apply) {
            ArrayList<Uri> selectedUris = (ArrayList<Uri>) mSelectedCollection.asListOfUri();
            ArrayList<String> selectedPaths = (ArrayList<String>) mSelectedCollection.asListOfString();
            ArrayList<Item> items = (ArrayList<Item>) mSelectedCollection.asList();
            setResultFinish(selectedUris, items, selectedPaths);
        } else if (v.getId() == R.id.picture_picker_original_layout) {
            int count = countOverMaxSize();
            if (count > 0) {
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.error_over_original_count, count, mSpec.originalMaxSize));
                incapableDialog.show(getSupportFragmentManager(), IncapableDialog.class.getName());
                return;
            }
            mOriginalEnable = !mOriginalEnable;
            mOriginal.setChecked(mOriginalEnable);
            if (mSpec.onCheckedListener != null) {
                mSpec.onCheckedListener.onCheck(mOriginalEnable);
            }
        }
    }

    /**
     * 判断需不需要裁剪
     */
    private boolean isCrop(String path) {
        return path.endsWith("jpg") || path.endsWith("jpeg") || path.endsWith("png")
                || path.endsWith("bmp") || path.endsWith("webp");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAlbumCollection.setStateCurrentSelection(position);
        mAlbumsAdapter.getCursor().moveToPosition(position);
        Album album = Album.valueOf(mAlbumsAdapter.getCursor());
        if (album.isAll() && SelectionSpec.getInstance().capture) {
            album.addCaptureCount();
        }
        onAlbumSelected(album);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onAlbumLoad(final Cursor cursor) {
        mAlbumsAdapter.swapCursor(cursor);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                cursor.moveToPosition(mAlbumCollection.getCurrentSelection());
                mAlbumsSpinner.setSelection(PicturePickerActivity.this,
                        mAlbumCollection.getCurrentSelection());
                Album album = Album.valueOf(cursor);
                if (album.isAll() && SelectionSpec.getInstance().capture) {
                    album.addCaptureCount();
                }
                onAlbumSelected(album);
            }
        });
    }

    @Override
    public void onAlbumReset() {
        mAlbumsAdapter.swapCursor(null);
    }

    private void onAlbumSelected(Album album) {
        if (album.isAll() && album.isEmpty()) {
            mContainer.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mContainer.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            Fragment fragment = MediaSelectionFragment.newInstance(album);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.picture_picker_container, fragment, MediaSelectionFragment.class.getSimpleName())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onUpdate() {
        updateBottomToolbar();
        if (mSpec.onSelectedListener != null) {
            mSpec.onSelectedListener.onSelected(
                    mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
        }
    }

    @Override
    public void onMediaClick(Album album, Item item, int adapterPosition) {
        Intent intent = new Intent(this, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
        intent.putExtra(PictureUtils.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(PictureUtils.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
        startActivityForResult(intent, PictureUtils.REQUEST_CODE_PREVIEW);
    }

    @Override
    public SelectedItemCollection provideSelectedItemCollection() {
        return mSelectedCollection;
    }

    @Override
    public void capture() {
        String[] permissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (PictureUtils.checkPermissionAllGranted(this, permissions)) {
            if (mMediaStoreCompat != null) {
                mMediaStoreCompat.dispatchCaptureIntent(this, mSpec.saveImagePath, PictureUtils.REQUEST_CODE_CAPTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions, PictureUtils.REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean have = true;
        for (int item : grantResults) {
            if (item != PackageManager.PERMISSION_GRANTED) {
                have = false;
            }
        }
        if (!have){
            Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show();
            return;
        }
        switch (requestCode) {
            case PictureUtils.REQUEST_CAMERA_PERMISSION:
                if (mMediaStoreCompat != null) {
                    mMediaStoreCompat.dispatchCaptureIntent(this, mSpec.saveImagePath, PictureUtils.REQUEST_CODE_CAPTURE);
                }
                break;
            case PictureUtils.REQUEST_READ_PERMISSION:
                mAlbumCollection.loadAlbums();
                break;
            default:
                break;
        }
    }
}
