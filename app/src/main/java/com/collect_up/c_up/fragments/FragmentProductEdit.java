/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.interfaces.OnRemoveItemLisetener;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.Category;
import com.collect_up.c_up.model.InternalCategory;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.collect_up.c_up.view.MultiSpinner;
import com.github.developerpaul123.filepickerlibrary.FilePickerActivity;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.RadioButton;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.Picker;
import com.yalantis.ucrop.model.MediaContent;

import org.apmem.tools.layouts.FlowLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import me.gujun.android.taggroup.TagGroup;

@SuppressLint("ValidFragment")

public class FragmentProductEdit extends BaseFragment
        implements Validator.ValidationListener, TagGroup.OnTagLimitationExceedListener {
    final List<String> imagesViewsTagsList = new ArrayList<>();
    private final HashMap<String, String> mHashMap = new HashMap<>();
    private ImageButton mAddItemImageButton;
    private int mAddedImageCounter = 0;
    private FlowLayout mAddedItemsContainerLinearLayout;
    private EditText mDescriptionEditText;
    private BottomSheetDialog mDialog;
    private EditText mExtraPropertiesEditText;
    @NotEmpty(trim = true)
    private EditText mNameEditText;
    private Product mProduct = new Product();
    private TagGroup mTagGroup;
    private Validator mValidator;
    private Menu mMenu;
    private String tag;
    private View view;
    private MultiSpinner mSpinnerInternalCategories;
    private EditText edtCat;
    private ImageView bntNewCat;

    public FragmentProductEdit(Product mProduct) {
        this.mProduct = mProduct;
    }

    public FragmentProductEdit() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tag = UUID.randomUUID().toString();
        if (requestCode == Constants.RequestCodes.PICK_FILE.ordinal()) {
            if (resultCode == getActivity().RESULT_OK) {
                String filePath = data.
                        getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
                if (filePath != null) {
                    handleResult(filePath, tag);
                }
            } else {
                mDialog.findViewById(R.id.btnCatalog).setEnabled(true);
            }
        }
        if (data != null && requestCode != Constants.RequestCodes.PICK_FILE.ordinal()) {
            final ArrayList<MediaContent> media = data.getBundleExtra("data").getParcelableArrayList("data");
            for (int i = 0; i < media.size(); i++) {
                if (media.get(i).getType() == MediaContent.IS_IMAGE && resultCode == getActivity().RESULT_OK) {
                    List<String> paths = data.getStringArrayListExtra(me.nereo.multi_image_selector.utils.Constants.EXTRA_RESULT);

                    if (mAddedImageCounter < Constants.General.MAX_SELECT_IMAGE) {
                        handleResult(1, media.get(i).getUri(), tag);
                        mAddedImageCounter++;
                    }

                    if (mAddedImageCounter == Constants.General.MAX_SELECT_IMAGE) {
                        mDialog.findViewById(R.id.btnGallery).setEnabled(false);
                    }

                } else if (media.get(i).getType() == MediaContent.IS_VIDEO) {
                    if (resultCode == getActivity().RESULT_OK) {
                        handleResult(2, media.get(i).getUri(), tag);
                    } else {
                        mDialog.findViewById(R.id.btnVideo).setEnabled(true);
                    }
                }
            }
        }
    }

    private void handleResult(final int type, Uri uri, String tag2) {
        String absolutePath = type != 1 ? uri.getPath() /*Utils.getPath(getContext(), uri)*/ : Images.getRealPathFromURI(getContext(), uri);
        try {
            absolutePath = type == 1 ? Images.compressJpeg(getContext(), absolutePath, Constants.General.APP_FOLDER_IMAGE_PATH, false) : absolutePath;

        } catch (IOException e) {
            e.printStackTrace();
        }
        final CircularProgressView progressView = addItem(type, absolutePath, uri, tag2);

        if (type == 3) {
            new Upload(getContext(), new File(absolutePath), tag2, "application/pdf").uploadPdf(new IUploadCallback() {
                @Override
                public void onFileReceived(String fileName, String uploadedPath) {
                    mProduct.setPdfAddress(uploadedPath);
                    mProduct.setPdfThumbnail(fileName);
                    progressView.setVisibility(View.GONE);

                }

                @Override
                public void onFailed(int statusCode) {
                    afterUploadFailed(type);
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    progressView.setProgress((bytesWritten * 1.0F / totalSize) * 100.0F);
                }
            });
        } else if (type == 2) {
            int[] wxh = Utils.getLocalVideoWidthAndHeight(absolutePath);
            final String finalAbsolutePath = absolutePath;
            new Upload(getContext(), new File(absolutePath), tag2, "video/*").uploadVideoWithConvertCrop(wxh[0], wxh[1], new IUploadCallback() {
                @Override
                public void onFileReceived(String fileName, String uploadedPath) {
                    mProduct.setVideoAddress(uploadedPath);
                    mProduct.setLocalNames(fileName);

                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(finalAbsolutePath);
                    long duration = Long.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 2;
                    Bitmap bitmap = metadataRetriever.getFrameAtTime(duration, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    metadataRetriever.release();
                    try {
                        String savedBitmapFilePath = Utils.saveBitmap(getContext(), bitmap, Constants.General.APP_FOLDER_VIDEO_THUMB_PATH);
                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        String compressJpegPath = Images.compressJpeg(getContext(), savedBitmapFilePath, Constants.General.APP_FOLDER_VIDEO_THUMB_PATH, true);
                        uploadVideoThumbnail(compressJpegPath, type);
                    } catch (IOException e) {
                    }

                    progressView.setVisibility(View.GONE);
                }

                @Override
                public void onFailed(int statusCode) {
                    afterUploadFailed(type);
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    progressView.setProgress((bytesWritten * 1.0F / totalSize) * 100.0F);
                }
            });
        } else if (type == 1) {

            new Upload(getContext(), new File(absolutePath), tag2, "image*//*").uploadImage(new IUploadCallback() {
                @Override
                public void onFileReceived(String fileName, String uploadedPath) {
                    mHashMap.put(fileName, uploadedPath);
                    mProduct.setDefaultImageAddress(uploadedPath);
                    progressView.setVisibility(View.GONE);
                }

                @Override
                public void onFailed(int statusCode) {
                    afterUploadFailed(type);
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    progressView.setProgress((bytesWritten * 1.0F / totalSize) * 100.0F);
                }
            });
        }

        mDialog.dismiss();
    }

    private void handleResult(String filePath, String tag) {

        final CircularProgressView progressView = addItem(filePath, tag);

        new Upload(getContext(), new File(filePath), tag, "application/pdf").uploadPdf(new IUploadCallback() {
            @Override
            public void onFileReceived(String fileName, String uploadedPath) {
                mProduct.setPdfAddress(uploadedPath);
                mProduct.setPdfThumbnail(fileName);
                progressView.setVisibility(View.GONE);

            }

            @Override
            public void onFailed(int statusCode) {
                afterUploadFailed(3);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                progressView.setProgress((bytesWritten * 1.0F / totalSize) * 100.0F);
            }
        });

        mDialog.dismiss();
    }

    private CircularProgressView addItem(int type, final String path, Uri uri, String tag2) {

        final ImageView mAddedViewItem = new ImageView(getContext());
        final RadioButton radio = new RadioButton(getContext());

        final FrameLayout frameLayout = new FrameLayout(getContext());
        FlowLayout.LayoutParams frameLayoutParams = new FlowLayout.LayoutParams(Utils.dpToPx(80), Utils.dpToPx(80));
        frameLayoutParams.rightMargin = 15;
        frameLayoutParams.bottomMargin = 18;
        frameLayout.setLayoutParams(frameLayoutParams);

        FrameLayout.LayoutParams itemParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final CircularProgressView progressView = addProgress(tag2);
        if (type == 3) {
            mAddedViewItem.setLayoutParams(itemParams);

            mAddedViewItem.setBackgroundColor(getResources().getColor(android.R.color.white));
            mAddedViewItem.setImageDrawable(getResources().getDrawable(R.drawable.ic_catalogue));
            mAddedViewItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mAddedViewItem.setBackgroundColor(getResources().getColor(android.R.color.white));
            mAddedViewItem.setImageDrawable(getResources().getDrawable(R.drawable.ic_catalogue));

            mAddedViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);
                    mProduct.setPdfAddress(null);
                    mDialog.findViewById(R.id.btnCatalog).setEnabled(true);
                }
            });

            progressView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyApplication.getInstance().cancelUploadHandler((String) progressView.getTag());
                    ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);
                    mProduct.setPdfAddress(null);
                    mDialog.findViewById(R.id.btnCatalog).setEnabled(true);
                }
            });
        } else if (type == 2) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bmp;

            retriever.setDataSource(getContext(), uri);
            bmp = retriever.getFrameAtTime();

            mAddedViewItem.setImageBitmap(Utils.cropBitmap(bmp));

            mAddedViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);
                    mProduct.setVideoAddress(null);
                    mDialog.findViewById(R.id.btnVideo).setEnabled(true);
                }
            });

            progressView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyApplication.getInstance().cancelUploadHandler((String) progressView.getTag());
                    ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);
                    mProduct.setVideoAddress(null);
                    mDialog.findViewById(R.id.btnVideo).setEnabled(true);
                }
            });
        } else if (type == 1) {
            Bitmap bmp = Images.getBitmapFromFilePath(getContext(), uri.getPath());

            mAddedViewItem.setImageBitmap(bmp);//Utils.cropBitmap(bmp));
            mAddedViewItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            radio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 1; i < mAddedItemsContainerLinearLayout.getChildCount(); i++) {
                        RadioButton radiobutton = (RadioButton) ((FrameLayout) mAddedItemsContainerLinearLayout.getChildAt(i)).getChildAt(4);
                        if (radiobutton == null) {
                            radiobutton = (RadioButton) ((FrameLayout) mAddedItemsContainerLinearLayout.getChildAt(i)).getChildAt(3);
                        }
                        if (radiobutton != null) {
                            radiobutton.setChecked(false);
                        }
                    }
                    radio.setChecked(true);
                    String[] st = path.split("/");
                    String stt = st[st.length - 1];
                    mProduct.setDefaultImageAddress(mHashMap.get(stt));
                    Toast.makeText(getContext(), getString(R.string.set_as_default), Toast.LENGTH_SHORT)
                            .show();
                }
            });
            mAddedViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            mAddedViewItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String[] st = path.split("/");
                    String stt = st[st.length - 1];
                    mProduct.setDefaultImageAddress(mHashMap.get(stt));
                    Toast.makeText(getContext(), getString(R.string.set_as_default), Toast.LENGTH_SHORT)
                            .show();

                    return true;
                }
            });

            progressView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAddedImageCounter--;
                    MyApplication.getInstance().cancelUploadHandler((String) progressView.getTag());
                    ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);
                    mDialog.findViewById(R.id.btnGallery).setEnabled(true);

                    for (String s : imagesViewsTagsList) {
                        if (s.equals(mAddedViewItem.getTag())) {
                            imagesViewsTagsList.remove(s);
                            break;
                        }
                    }
                }
            });
        }
        String tag = path + "," + type;
        mAddedViewItem.setTag(tag);
        frameLayout.setTag(tag);

        imagesViewsTagsList.add(tag);

        mAddedViewItem.setLayoutParams(itemParams);
        View view = new View(getContext());
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.alpha_background));
        frameLayout.addView(mAddedViewItem);
        frameLayout.addView(progressView);
        RadioGroup radioGroup = new RadioGroup(getContext());
        FrameLayout.LayoutParams lParamRadio = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lParamRadio.gravity = Gravity.LEFT | Gravity.BOTTOM;
        radio.setLayoutParams(lParamRadio);
        ImageView imgColse = new ImageView(getContext());
        imgColse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddedImageCounter--;
                ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);

                mDialog.findViewById(R.id.btnGallery).setEnabled(true);

                for (String s : imagesViewsTagsList) {
                    if (s.equals(mAddedViewItem.getTag())) {
                        imagesViewsTagsList.remove(s);
                        break;
                    }
                }
            }
        });
        FrameLayout.LayoutParams lParamsClose = new FrameLayout.LayoutParams(Utils.dpToPx(20), Utils.dpToPx(20));
        lParamsClose.gravity = Gravity.RIGHT | Gravity.TOP;
        lParamsClose.setMargins(0, Utils.dpToPx(3), Utils.dpToPx(3), 0);
        imgColse.setLayoutParams(lParamsClose);
        imgColse.setImageResource(R.drawable.ic_close_white_24dp);
        frameLayout.addView(view);
        frameLayout.addView(imgColse);
        if (type == 1) {
            frameLayout.addView(radio);
        }
        mAddedItemsContainerLinearLayout.addView(frameLayout);

        return progressView;
    }

    private void afterUploadFailed(int type) {
        for (int i = 0; i < mAddedItemsContainerLinearLayout.getChildCount(); i++) {
            View itemLayout = mAddedItemsContainerLinearLayout.getChildAt(i);
            if (itemLayout instanceof FrameLayout) {
                String[] tags = ((String) itemLayout.getTag()).split(",");
                String[] layoutTags = ((String) itemLayout.getTag()).split(",");
                if (layoutTags[0].equals(tags[0])) {
                    mAddedItemsContainerLinearLayout.removeViewAt(i);
                }
                if (type == 1) {
                    mAddedImageCounter--;
                    mDialog.findViewById(R.id.btnGallery).setEnabled(true);
                } else if (type == 2) {
                    mDialog.findViewById(R.id.btnVideo).setEnabled(true);
                } else if (type == 3) {
                    mDialog.findViewById(R.id.btnCatalog).setEnabled(true);
                }
                break;
            }
        }
    }

    private void uploadVideoThumbnail(String path, final int type) {
        Upload upload = new Upload(getContext(), new File(path), UUID.randomUUID().toString(), "image/jpeg");
        upload.uploadImage(new IUploadCallback() {
            @Override
            public void onFileReceived(String fileName, String uploadedPath) {
                if (Utils.isNullOrEmpty(fileName) || Utils.isNullOrEmpty(uploadedPath)) {
                    Toast.makeText(getContext(), getString(R.string.toast_error_upload_image_failed), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mProduct.setVideoThumbnail(uploadedPath);
                }
            }

            @Override
            public void onFailed(int statusCode) {
                afterUploadFailed(type);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {

            }
        });
    }

    private CircularProgressView addItem(String path, String tag2) {

        final ImageView mAddedViewItem = new ImageView(getContext());

        final FrameLayout frameLayout = new FrameLayout(getContext());
        FlowLayout.LayoutParams frameLayoutParams = new FlowLayout.LayoutParams(Utils.dpToPx(80), Utils.dpToPx(80));
        frameLayoutParams.rightMargin = 15;
        frameLayoutParams.bottomMargin = 18;
        frameLayout.setLayoutParams(frameLayoutParams);

        FrameLayout.LayoutParams itemParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final CircularProgressView progressView = addProgress(tag2);
        mAddedViewItem.setBackgroundColor(getResources().getColor(android.R.color.white));
        mAddedViewItem.setImageDrawable(getResources().getDrawable(R.drawable.ic_catalogue));

        mAddedViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);
                mProduct.setPdfAddress(null);
                mDialog.findViewById(R.id.btnCatalog).setEnabled(true);
            }
        });

        progressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstance().cancelUploadHandler((String) progressView.getTag());
                ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);
                mProduct.setPdfAddress(null);
                mDialog.findViewById(R.id.btnCatalog).setEnabled(true);
            }
        });
        String tag = path + "," + 3;
        mAddedViewItem.setTag(tag);
        frameLayout.setTag(tag);

        imagesViewsTagsList.add(tag);

        mAddedViewItem.setLayoutParams(itemParams);
        frameLayout.addView(mAddedViewItem);
        frameLayout.addView(progressView);
        frameLayout.setForeground(getResources().getDrawable(R.drawable.ic_close_white_24dp));
        frameLayout.setForegroundGravity(Gravity.CENTER);
        mAddedItemsContainerLinearLayout.addView(frameLayout);

        return progressView;
    }

    private CircularProgressView addProgress(String tag) {
        CircularProgressView mProgressView = new CircularProgressView(getContext());
        mProgressView.setThickness(16);
        mProgressView.setColor(getResources().getColor(R.color.colorAccent));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Utils.dpToPx(64), Utils.dpToPx(64));
        layoutParams.gravity = Gravity.CENTER;
        mProgressView.setLayoutParams(layoutParams);
        mProgressView.setTag(tag);
        return mProgressView;
    }

    @Override
    public void onResume() {

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.edit_product);
        ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_product_edit, container, false);
            setHasOptionsMenu(true);
            bntNewCat = (ImageView) view.findViewById(R.id.btnNewCat);
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            mAddItemImageButton = (ImageButton) view.findViewById(R.id.image_button_add_item);
            mNameEditText = (EditText) view.findViewById(R.id.edit_text_name);
            mExtraPropertiesEditText = (EditText) view.findViewById(R.id.edit_text_extra_properties);
            mDescriptionEditText = (EditText) view.findViewById(R.id.edit_text_description);
            mTagGroup = (TagGroup) view.findViewById(R.id.tag_group);
            mAddedItemsContainerLinearLayout = (FlowLayout) view.findViewById(R.id.linear_layout_container);
            mSpinnerInternalCategories = (MultiSpinner) view.findViewById(R.id.spinner_internal_categories);
            bntNewCat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showNewCatDialog();
                }
            });
            fillInternalSpinner();

            mNameEditText.setText(mProduct.getName());
            mExtraPropertiesEditText.setText(mProduct.getExtraProperties());
            mDescriptionEditText.setText(mProduct.getDescription());

            // TagGroup should be in this positions to prevent from showing toast for the first time.
            mTagGroup.setLimitation(Constants.General.TAG_LIMITATION);
            if (mProduct.getTags() != null) {
                mTagGroup.setTags(mProduct.getTags());
            }
            mTagGroup.setOnLimitationExceedListener(this);

            // Set focus on the name edit text
            mNameEditText.requestFocus();

            initDialog();

            mAddItemImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAddItemDialog();
                }
            });

            initItems();

            mValidator = new Validator(this);
            mValidator.setValidationListener(this);
            return view;
        } else {
            return view;
        }
    }

    private void fillInternalSpinner() {
        HttpClient.get(String.format(Constants.Server.Shop.GET_INTERNALCATEGORY, mProduct.getShop().getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                List<Category> categories = new ArrayList<>();

                InternalCategory[] internalCategoris = GsonParser.getArrayFromGson(responseBody, InternalCategory[].class);
                if (internalCategoris != null) {
                    if (internalCategoris.length == 0) {
                        Category category = new Category();
                        category.setId("0");
                        category.setName("New Category");
                        category.setCount(0);
                        categories.add(category);
                        mSpinnerInternalCategories.setItems(categories);
                    } else {
                        for (int i = 0; i < internalCategoris.length; i++) {
                            Category category = new Category();
                            category.setId(internalCategoris[i].getId());
                            category.setName(internalCategoris[i].getName());
                            category.setCount(internalCategoris[i].getCount());
                            categories.add(category);
                        }
                        mSpinnerInternalCategories.setItems(categories);
                    }
                    mSpinnerInternalCategories.setRemoveListener(new OnRemoveItemLisetener() {
                        @Override
                        public void onRemove(final int index, final String id) {
                            HttpClient.get(String.format(Constants.Server.Shop.GET_REMOVE_INTERNALCATEGORY, mProduct.getShop().getId(), id), new AsyncHttpResponser(getContext()) {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    super.onSuccess(statusCode, headers, responseBody);
                                    mSpinnerInternalCategories.removeItem(index);
                                    if (mSpinnerInternalCategories.getSelectedItem().size() == 0 && index == 0) {
                                        List<Category> categories = new ArrayList<>();

                                        Category category = new Category();
                                        category.setId("0");
                                        category.setName("New Category");
                                        category.setCount(0);
                                        categories.add(category);
                                        mSpinnerInternalCategories.setItems(categories);
                                        mSpinnerInternalCategories.buildSelectedItemString();
                                        mSpinnerInternalCategories.closeDialog();
                                    }

                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    super.onFailure(statusCode, headers, responseBody, error);
                                }
                            });
                        }
                    });
                    mSpinnerInternalCategories.setListener(new MultiSpinner.OnMultipleItemsSelectedListener() {
                        @Override
                        public void selectedItem(List<Category> categories) {
                            if (categories.size() > 0 && categories.get(0).getId().equals("0")) {
                                showNewCatDialog();
                            } else {
                                mSpinnerInternalCategories.buildSelectedItemString();
                            }


                        }
                    });

                    StringBuilder selectedString = new StringBuilder();

                    for (int i = 0; i < categories.size(); i++) {
                        for (int j = 0; j < mProduct.getInternalCategoryIds().size(); j++) {
                            if (categories.get(i).getId().equals(mProduct.getInternalCategoryIds().get(j))) {
                                selectedString.append(categories.get(i).getName() + ", ");

                            }
                        }

                    }
                    mSpinnerInternalCategories.setSelection(selectedString.toString());
                    mSpinnerInternalCategories.buildSelectedItemString();
                } else {
                    Toast.makeText(getContext(), "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }
        });
    }

    private void showNewCatDialog() {
        final SimpleDialog mDialog = new SimpleDialog(getContext());
        mDialog
                .title(R.string.title_new_cat)

                .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                .positiveAction(R.string.ok)
                .negativeAction(R.string.cancel)
                .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                .setCancelable(true);
        mDialog.layoutParams(Utils.getScreenWidthPX(getContext()) - Utils.dpToPx(40), ViewGroup.LayoutParams.WRAP_CONTENT);
        mDialog.setContentView(R.layout.dialog_new_category);
        mDialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpClient.get(String.format(Constants.Server.Shop.GET_ADD_INTERNALCATEGORY, mProduct.getShop().getId(), edtCat.getText().toString()), new AsyncHttpResponser(getContext()) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        super.onSuccess(statusCode, headers, responseBody);
                        mDialog.dismiss();
                        fillInternalSpinner();

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);
                    }
                });
            }
        });
        mDialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
        edtCat = (EditText) mDialog.findViewById(R.id.edtCat);

    }

    private void initDialog() {
        mDialog = new BottomSheetDialog(getContext());
        mDialog.contentView(R.layout.bottom_sheet_product)
                .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
                .inDuration(300)
                .cancelable(true);
    }

    private void openAddItemDialog() {
        initDialog();

        ImageButton addPhotos = (ImageButton) mDialog.findViewById(R.id.btnGallery);
        ImageButton addVideo = (ImageButton) mDialog.findViewById(R.id.btnVideo);
        ImageButton addPdf = (ImageButton) mDialog.findViewById(R.id.btnCatalog);

        addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                v.setEnabled(false);
            }
        });

        addPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                v.setEnabled(false);
                Utils.pickPdf(FragmentProductEdit.this, Constants.RequestCodes.PICK_FILE.ordinal());
            }
        });

        addPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionListener dialogPermissionListener =
                        CustomPermissionDialog.Builder
                                .withContext(getContext())
                                .withTitle(R.string.permission_title)
                                .withMessage(R.string.permission_storage)
                                .withButtonText(android.R.string.ok)
                                .build();
                PermissionListener basePermission = new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {


                        mDialog.dismiss();
                        final Picker.PickerBuilder pickerBuilder = new Picker.PickerBuilder(FragmentProductEdit.this, Constants.General.MAX_SELECT_IMAGE, Constants.General.MAX_SELECT_VIDEO)
                                .setMultiple(true)
                                .setFreeStyle(true);
                        Picker picker = new Picker(pickerBuilder);
                        picker.start();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();

                    }
                };
                CompositePermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
                Dexter.withActivity(getActivity())
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(compositePermissionListener)
                        .check();

            }
        });

        mDialog.show();
    }

    private void initItems() {
        if (mProduct.getImageAddresses() != null) {
            for (int i = 0; i < mProduct.getImageAddresses().size(); i++) {
                String image = mProduct.getImageAddresses().get(i);
                String thumbnail = mProduct.getImageThumbnails().get(i);
                mHashMap.put(thumbnail, image);
                initAddedItems(image, thumbnail, 1);
                mAddedImageCounter++;
            }
            if (mProduct.getImageAddresses().size() == 4) {
                mDialog.findViewById(R.id.btnGallery).setEnabled(false);
            }
        }
        if (mProduct.getVideoAddress() != null) {
            initAddedItems(mProduct.getVideoAddress(), mProduct.getVideoThumbnail(), 2);
            mDialog.findViewById(R.id.btnVideo).setEnabled(false);
            imagesViewsTagsList.add(mProduct.getLocalNames() + "," + 2);
        }
        if (mProduct.getPdfAddress() != null) {
            initAddedItems(mProduct.getPdfAddress(), mProduct.getPdfThumbnail(), 3);
            mDialog.findViewById(R.id.btnCatalog).setEnabled(false);
            imagesViewsTagsList.add(mProduct.getPdfThumbnail() + "," + 3);
        }
    }

    private void initAddedItems(final String address, final String thumbnail, final int type) {

        final ImageView mAddedViewItem = new ImageView(getContext());
        final RadioButton radio = new RadioButton(getContext());

        final FrameLayout frameLayout = new FrameLayout(getContext());
        FlowLayout.LayoutParams frameLayoutParams = new FlowLayout.LayoutParams(Utils.dpToPx(80), Utils.dpToPx(80));
        frameLayoutParams.rightMargin = 15;
        frameLayoutParams.bottomMargin = 18;
        frameLayout.setLayoutParams(frameLayoutParams);
        String tag = null;

        FrameLayout.LayoutParams itemParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (type == 3) {
            tag = thumbnail + "," + type;
            mAddedViewItem.setBackgroundColor(getResources().getColor(android.R.color.white));
            mAddedViewItem.setImageDrawable(getResources().getDrawable(R.drawable.ic_catalogue));

            mAddedViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    frameLayout.setVisibility(View.GONE);
                    frameLayout.removeAllViews();
                    frameLayout.setForeground(null);
                    mProduct.setPdfAddress(null);
                    mDialog.findViewById(R.id.btnCatalog).setEnabled(true);
                }
            });
        } else if (type == 2) {
            tag = thumbnail + "," + type;

            try {
                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                metadataRetriever.setDataSource(getContext(), Uri.fromFile(new File(thumbnail)));
                long duration = Long.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 2;
                Bitmap bitmap = metadataRetriever.getFrameAtTime(duration, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                mAddedViewItem.setImageBitmap(Utils.cropBitmap(bitmap));
                metadataRetriever.release();
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }

                mAddedViewItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        frameLayout.setVisibility(View.GONE);
                        frameLayout.removeAllViews();
                        frameLayout.setForeground(null);
                        mProduct.setVideoAddress(null);
                        mDialog.findViewById(R.id.btnVideo).setEnabled(true);
                    }
                });
            } catch (IllegalArgumentException e) {


                tag = thumbnail + "," + type;
                mAddedViewItem.setBackgroundColor(getResources().getColor(android.R.color.black));

                mAddedViewItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        frameLayout.setVisibility(View.GONE);
                        frameLayout.removeAllViews();
                        frameLayout.setForeground(null);
                        mProduct.setPdfAddress(null);
                        mDialog.findViewById(R.id.btnCatalog).setEnabled(true);
                    }
                });


            }


        } else if (type == 1) {
            tag = thumbnail + "," + type;
            mAddedViewItem.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Picasso.with(getContext()).load(Constants.General.BLOB_PROTOCOL + address).into(mAddedViewItem);
            radio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 1; i < mAddedItemsContainerLinearLayout.getChildCount(); i++) {
                        RadioButton radiobutton = (RadioButton) ((FrameLayout) mAddedItemsContainerLinearLayout.getChildAt(i)).getChildAt(4);
                        if (radiobutton == null) {
                            radiobutton = (RadioButton) ((FrameLayout) mAddedItemsContainerLinearLayout.getChildAt(i)).getChildAt(3);
                        }
                        if (radiobutton != null) {
                            radiobutton.setChecked(false);
                        }
                    }
                    // if (!isChecked)
                    radio.setChecked(true);
                    String[] st = thumbnail.split("/");
                    String stt = st[st.length - 1];
                    mProduct.setDefaultImageAddress(mHashMap.get(stt));
                    Toast.makeText(getContext(), getString(R.string.set_as_default), Toast.LENGTH_SHORT)
                            .show();
                }
            });
            mAddedViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            mAddedViewItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String[] st = thumbnail.split("/");
                    String stt = st[st.length - 1];
                    mProduct.setDefaultImageAddress(mHashMap.get(stt));
                    Toast.makeText(getContext(), getString(R.string.set_as_default), Toast.LENGTH_SHORT)
                            .show();

                    return true;
                }
            });
        }
        mAddedViewItem.setTag(tag);
        frameLayout.setTag(tag);

        imagesViewsTagsList.add(tag);

        mAddedViewItem.setLayoutParams(itemParams);
        View view = new View(getContext());
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.alpha_background));

        frameLayout.addView(mAddedViewItem);
        RadioGroup radioGroup = new RadioGroup(getContext());
        FrameLayout.LayoutParams lParamRadio = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lParamRadio.gravity = Gravity.LEFT | Gravity.BOTTOM;
        radio.setLayoutParams(lParamRadio);
        ImageView imgColse = new ImageView(getContext());
        imgColse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 3) {
                    mProduct.setPdfAddress(null);
                }
                mAddedImageCounter--;
                ((ViewGroup) frameLayout.getParent()).removeView(frameLayout);

                mDialog.findViewById(R.id.btnGallery).setEnabled(true);

                for (String s : imagesViewsTagsList) {
                    if (s.equals(mAddedViewItem.getTag())) {
                        imagesViewsTagsList.remove(s);
                        break;
                    }
                }
            }
        });
        FrameLayout.LayoutParams lParamsClose = new FrameLayout.LayoutParams(Utils.dpToPx(20), Utils.dpToPx(20));
        lParamsClose.gravity = Gravity.RIGHT | Gravity.TOP;
        lParamsClose.setMargins(0, Utils.dpToPx(3), Utils.dpToPx(3), 0);
        imgColse.setLayoutParams(lParamsClose);
        imgColse.setImageResource(R.drawable.ic_close_white_24dp);
        frameLayout.addView(view);
        frameLayout.addView(imgColse);
        if (type == 1) {
            frameLayout.addView(radio);
        }
        mAddedItemsContainerLinearLayout.addView(frameLayout);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_done_discard, menu);
        MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
        Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
        if (menuItemLoaderIcon != null) {
            try {
                menuItemLoaderIcon.mutate();
                menuItemLoaderIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                menuItemLoader.setIcon(menuItemLoaderIcon);
            } catch (IllegalStateException e) {
                Log.i("sepehr", String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
            }
            ((Animatable) menuItemLoader.getIcon()).start();
            menuItemLoader.setVisible(false);
        }
        menu.findItem(R.id.action_search).setVisible(false);
        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                break;
            case R.id.action_done:
                mValidator.validate(true);
                break;
        }

        return false;
    }

    @Override
    public void onValidationSucceeded() {
        mMenu.findItem(R.id.action_done).setVisible(false);
        mMenu.findItem(R.id.menu_loader).setVisible(true);

        mProduct.setName(mNameEditText.getText().toString().trim());
        mProduct.setDescription(mDescriptionEditText.getText().toString().trim());
        mProduct.setTags(getTags());
        mProduct.setExtraProperties(mExtraPropertiesEditText.getText().toString().trim());
        List<Category> categories = mSpinnerInternalCategories.getSelectedItem();
        List<String> internalCategories = new ArrayList<>();

        for (int i = 0; i < categories.size(); i++) {
            internalCategories.add(categories.get(i).getId());
        }
        mProduct.setInternalCategoryIds(internalCategories);
        List<String> uploadedImageAddresses = new ArrayList<>();
        List<String> uploadedImageThumbnails = new ArrayList<>();

        for (HashMap.Entry<String, String> h : mHashMap.entrySet()) {
            for (String s : imagesViewsTagsList) {
                if (s.contains(h.getKey())) {
                    uploadedImageAddresses.add(h.getValue());
                    uploadedImageThumbnails.add(h.getKey());
                }
            }
        }

        if (uploadedImageAddresses.size() == 0) {
            Toast.makeText(getContext(), getString(R.string.upload_an_image), Toast.LENGTH_SHORT)
                    .show();
            mMenu.findItem(R.id.action_done).setVisible(true);
            mMenu.findItem(R.id.menu_loader).setVisible(false);

            return;
        }

        if (Utils.isNullOrEmpty(mProduct.getDefaultImageAddress())) {
            mProduct.setDefaultImageAddress(uploadedImageAddresses.get(0));
        } else {
            if (!uploadedImageAddresses.contains(mProduct.getDefaultImageAddress())) {
                mProduct.setDefaultImageAddress(uploadedImageAddresses.get(0));
            }
        }

        mProduct.setImageAddresses(uploadedImageAddresses);
        mProduct.setImageThumbnails(uploadedImageThumbnails);

        HttpClient.put(getContext(), Constants.Server.Product.PUT, new Gson().toJson(mProduct, Product.class), "application/json", new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                FragmentHandler.replaceFragment(getContext(), fragmentType.PRODUCT, mProduct);
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                mMenu.findItem(R.id.action_done).setVisible(true);
                mMenu.findItem(R.id.menu_loader).setVisible(false);

                Toast.makeText(getContext(), getString(R.string.toast_error_updating_product), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getContext());
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            }
        }
    }

    private List<String> getTags() {
        return Arrays.asList(mTagGroup.getTags());
    }

    @Override
    public void onLimitationExceed() {
        Utils.displayToast(getContext(), getString(R.string.toast_error_tag_limitation_exceed), Gravity.CENTER, Toast.LENGTH_SHORT);
    }
}
