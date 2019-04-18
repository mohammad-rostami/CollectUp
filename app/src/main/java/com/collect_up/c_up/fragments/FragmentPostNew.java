package com.collect_up.c_up.fragments;/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.chat.AndroidUtilities;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.ChoosePhoto;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.BaseModel;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.PostProfileTag;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.collect_up.c_up.view.CustomEditText;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.collect_up.c_up.view.RectangleNetworkImageView;
import com.collect_up.c_up.view.chat.SizeNotifierRelativeLayout;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rey.material.app.BottomSheetDialog;
import com.yalantis.ucrop.Picker;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.MediaContent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

@SuppressLint("ValidFragment")

public class FragmentPostNew extends FragmentMentions implements SizeNotifierRelativeLayout.SizeNotifierRelativeLayoutDelegate, View.OnClickListener, ChoosePhoto.OnDialogButtonClick {

    public static boolean isRunning;
    private final Post mPost = new Post();

    private FrameLayout mContainerLayout;
    private BottomSheetDialog mDialog;
    private FloatingActionButton mInsertImageImageButton;
    private ImageButton mInsertVideoImageButton;
    private CustomEditText mPostBodyEditText;
    private BaseModel postType;
    private CircularProgressView mProgressView;
    private Menu mMenu;
    private String finalUserText;
    private View fragmentSView;
    ArrayList<String> mentionsUser = new ArrayList<>();
    ArrayList<String> mentionsEncrypt = new ArrayList<>();
    private Long videoDuration;
    private String videoPath;
    private String finalHashtagText;
    private List<String> hashTags = new LinkedList<>();
    private SizeNotifierRelativeLayout sizeNotifierRelativeLayout;
    private boolean isFocused;
    private View view;
    private Toolbar toolbar;
    private ComplexAvatarView imgTitle;
    private Intent intent;
    private TextView btnTag;


    @Override
    public void setPeople(ArrayList<PostProfileTag> peoples) {
        super.setPeople(peoples);
        mPost.setProfileTags(peoples);
        btnTag.setText(getResources().getString(R.string.tag) + " " + (peoples.size() == 0 ? "" : peoples.size()));
    }

    @Override
    public void onStop() {
        isRunning = false;

   /* if (mProgressView != null)
    {
      mProgressView.callOnClick();
    }*/

        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        isRunning = true;
    }

    @Override
    public void onDestroy() {
        if (mProgressView != null) {
            mProgressView.callOnClick();
        }

        super.onDestroy();
    }

    public FragmentPostNew(Object postType) {
        if (postType instanceof BaseModel) {
            this.postType = (BaseModel) postType;
        } else {
            this.intent = (Intent) postType;
            this.postType = ((Intent) postType).getParcelableExtra("postType");
        }
    }

    private void addProgress(String tag) {
        mProgressView = new CircularProgressView(getContext());
        mProgressView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_close_circle));
        mProgressView.setThickness(Utils.dpToPx(5));
        mProgressView.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Utils.dpToPx(64), Utils.dpToPx(64));
        layoutParams.gravity = Gravity.CENTER;
        mProgressView.setLayoutParams(layoutParams);
        mProgressView.setTag(tag);
        mProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstance().cancelUploadHandler((String) mProgressView.getTag());
                mContainerLayout.removeAllViews();
                mContainerLayout.setForeground(null);
                mPost.setImageAddress(null);
                mPost.setVideoAddress(null);
                mPost.setVideoProperties(null);
            }
        });
        mContainerLayout.addView(mProgressView);
        mContainerLayout.setForeground(getResources().getDrawable(R.drawable.ic_close_white_24dp));
        mContainerLayout.setForegroundGravity(Gravity.CENTER);
    }

    private void addViewToContainer(Intent result) {
        ImageView imageView = new ImageView(getContext());
        Uri uri;
        Bitmap bitmap = null;
        uri = result == null ? Images.getUriFromFileName(Logged.General.getTempTakePhotoFilePath()) : UCrop.getOutput(result);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
        } catch (IOException ignored) {

        }

        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        String savedImage = Images.getImageUriFromBitmap(getContext(), bitmap).getPath();
        String path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Cursor c = getContext().getContentResolver().query(Uri.parse("content://media/" + savedImage), null, null, null, null);
            c.moveToNext();
            path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));

            c.close();
        } else {
            path = savedImage;
        }
        if (mProgressView != null && mProgressView.getTag() != null) {
            MyApplication.getInstance().cancelUploadHandler((String) mProgressView.getTag());
        }

        mContainerLayout.removeAllViews();
        mContainerLayout.setForeground(null);
        mContainerLayout.addView(imageView);

        String tag = UUID.randomUUID().toString();

        uploadImage(path, false, tag);
        addProgress(tag);
    }

    private void addViewToContainer(Uri result) {
        RectangleNetworkImageView imageView = new RectangleNetworkImageView(getContext());
        imageView.setAsCircle(false);
        Bitmap bitmap = null;
        Uri uri = result == null ? Images.getUriFromFileName(Logged.General.getTempTakePhotoFilePath()) : result;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
        } catch (IOException e) {
        }

        imageView.setLocalImageBitmap(bitmap);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageBitmap(bitmap);
        if (mProgressView != null && mProgressView.getTag() != null) {
            MyApplication.getInstance().cancelUploadHandler((String) mProgressView.getTag());
        }

        mContainerLayout.removeAllViews();
        mContainerLayout.setForeground(null);
        mContainerLayout.addView(imageView);

        String tag = UUID.randomUUID().toString();

        assert result != null;
        String path = getRealPathFromURI(getContext(), result) != null ? getRealPathFromURI(getContext(), result) : result.getPath();
        uploadImage(path, false, tag);

        addProgress(tag);
    }

    private void afterUploadSucceed() {
        mContainerLayout.setForeground(null);
        mContainerLayout.removeView(mProgressView);
        int margin = Utils.dpToPx(8);
        int padding = Utils.dpToPx(5);
        int size = Utils.dpToPx(28);


        ImageView imageView = new ImageView(getActivity());
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_close_white_24dp));

        imageView.setPadding(padding, padding, padding, padding);
        imageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_close_circle));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.gravity = Gravity.TOP | Gravity.END;
        params.rightMargin = margin;
        params.topMargin = margin;
        imageView.setLayoutParams(params);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContainerLayout.removeAllViews();
            }
        });
        mContainerLayout.addView(imageView);
        if (mPost.getVideoAddress() == null) {
            btnTag = new TextView(getContext());
            FrameLayout.LayoutParams txtParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txtParams.gravity = Gravity.BOTTOM;
            txtParams.leftMargin = margin;
            txtParams.bottomMargin = margin;
            btnTag.setLayoutParams(txtParams);
            btnTag.setText(R.string.tag);
            btnTag.setGravity(Gravity.CENTER);
            btnTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            btnTag.setTextColor(ContextCompat.getColor(getContext(), R.color.default_white));
            btnTag.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.corner_radius));
            Drawable img = ContextCompat.getDrawable(getContext(), R.drawable.ic_person_white_18dp);
            btnTag.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            btnTag.setPadding(padding, padding, padding, padding);
            btnTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Drawable drawable = ((ImageView) mContainerLayout.getChildAt(0)).getDrawable();
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    Intent intent = new Intent(getContext(), FragmentHandler.class);
                    intent.putExtra("image", bitmap);
                    intent.putParcelableArrayListExtra("peoples", mPost.getProfileTags());
                    FragmentHandler.replaceFragment(getContext(), fragmentType.TAG_PEOPLE, intent);
                }
            });
            mContainerLayout.addView(btnTag);

        }
    }

    private void emptyContainer() {
        mContainerLayout.removeAllViews();
        mContainerLayout.setForeground(null);
        Toast.makeText(getContext(), R.string.toast_error_upload_failed, Toast.LENGTH_LONG).show();
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception ex) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void addVideo(String path) {

        if (Utils.isNullOrEmpty(path)) {
            return;
        }
        try {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

            videoPath = path;
            metadataRetriever.setDataSource(getContext(), Uri.parse(path));
            String tag = UUID.randomUUID().toString();


            videoDuration = Long.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            String compressJpegPath = null;
            Bitmap bitmap = null;
            metadataRetriever.release();

            bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
            if (bitmap != null) {
                String savedBitmapFilePath = Utils.saveBitmap(getContext(), bitmap, Constants.General.APP_FOLDER_VIDEO_THUMB_PATH);
                compressJpegPath = Images.compressJpeg(getContext(), savedBitmapFilePath, Constants.General.APP_FOLDER_VIDEO_THUMB_PATH, true);
                uploadVideo(videoPath, compressJpegPath, tag);

            } else {
                Toast.makeText(getContext(), R.string.toast_error_couldnt_get_thumbnail, Toast.LENGTH_SHORT).show();
            }


            View videoIconLengthSize = LayoutInflater.from(getContext()).inflate(R.layout.video_icon_length_size, null);
            TextView fileSize = (TextView) videoIconLengthSize.findViewById(R.id.text_view_file_size);
            TextView fileDuration = (TextView) videoIconLengthSize.findViewById(R.id.text_view_file_length);
            fileSize.setText(Utils.readableFileSize(new File(videoPath).length()));
            fileDuration.setText(Utils.readableVideoDuration(videoDuration));

            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(bitmap);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            if (mProgressView != null && mProgressView.getTag() != null) {
                MyApplication.getInstance().cancelUploadHandler((String) mProgressView.getTag());
            }

            mContainerLayout.removeAllViews();

            mContainerLayout.addView(imageView);

            FrameLayout.LayoutParams videoIconLengthParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            videoIconLengthParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
            videoIconLengthSize.setLayoutParams(videoIconLengthParams);

            mContainerLayout.addView(videoIconLengthSize);

            addProgress(tag);
        } catch (IOException e) {
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mMenu != null) {
            mMenu.findItem(R.id.action_done).setVisible(false);
            mMenu.findItem(R.id.menu_loader).setVisible(true);

        }
        if (data != null) {
            Uri uri = null;
            MediaContent content = null;
            if (data.getBundleExtra("data") != null) {
                content = (MediaContent) data.getBundleExtra("data").getParcelableArrayList("data").get(0);
            }
            try {
                uri = data.getClipData().getItemAt(0).getUri();

            } catch (Exception ex) {
            }

            if (mMenu != null) {
                mMenu.findItem(R.id.action_done).setVisible(false);
                mMenu.findItem(R.id.menu_loader).setVisible(true);

            }
            if (uri == null) {
                if (content.getType() == MediaContent.IS_IMAGE) {
                    addViewToContainer(content.getUri());
                } else {
                    addVideo(content.getUri().getPath());
                }
            } else {
                if (data.getClipData().getDescription().toString().contains("image")) {

                    addViewToContainer(uri);

                } else {
                    addVideo(getRealPathFromURI(getContext(), data.getClipData().getItemAt(0).getUri()));
                }

            }

        }
    }


    void handleInputIntent(final Intent intent) {
        if (intent != null) {
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


                    String action = intent.getAction();
                    String type = intent.getType();
                    if (Intent.ACTION_SEND.equals(action) && type != null) {
                        if (type.startsWith("image/")) {
                            onActivityResult(Constants.RequestCodes.PICK_IMAGE.ordinal(), getActivity().RESULT_OK, intent);

                        } else if (type.startsWith("video/")) {
                            onActivityResult(Constants.RequestCodes.PICK_VIDEO.ordinal(), getActivity().RESULT_OK, intent);

                        } else if (type.startsWith("text/")) {
                            String intentText = intent.getStringExtra(Intent.EXTRA_TEXT);
                            mPostBodyEditText.setText(intentText);
                        }
                    }
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    token.continuePermissionRequest();

                }
            };

            PermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
            Dexter.withActivity(getActivity())
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(compositePermissionListener)
                    .check();


        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.new_post);
        ((ActivityHome) getActivity()).changeButtonBackgroud(-1);
        ((ActivityHome) getActivity()).hideButtonBar(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {

            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

            view = inflater.inflate(R.layout.fragment_new_post, container, false);

            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.new_post);
            toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentHandler.onBackPressed(getContext());
                }
            });
            toolbar.inflateMenu(R.menu.menu_done_discard);
            toolbar.getMenu().findItem(R.id.action_search).setVisible(false);

            setHasOptionsMenu(true);

            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    onOptionsItemSelected(item);
                    return false;
                }
            });

            imgTitle = (ComplexAvatarView) view.findViewById(R.id.image_view_picture);
            imgTitle.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(postType.getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + postType.getImageAddress());
            mPostBodyEditText = (CustomEditText) view.findViewById(R.id.edit_text_post_body);
            mInsertImageImageButton = (FloatingActionButton) view.findViewById(R.id.btnInsert);
            mInsertVideoImageButton = (ImageButton) view.findViewById(R.id.image_button_insert_video);
            mContainerLayout = (FrameLayout) view.findViewById(R.id.linear_layout_container);
            fragmentSView = view.findViewById(R.id.fragmentSuggestionlist);

            sizeNotifierRelativeLayout = (SizeNotifierRelativeLayout) view.findViewById(R.id.postLayout);
            sizeNotifierRelativeLayout.delegate = this;

            mPostBodyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        isFocused = true;
                    } else {
                        isFocused = false;
                    }
                }
            });
            fragmentSView.setVisibility(View.GONE);
            mPostBodyEditText.setHandleDismissingKeyboard(new CustomEditText.handleDismissingKeyboard() {
                @Override
                public void dismissKeyboard() {
                    if (fragmentSView.getVisibility() == View.VISIBLE) {
                        fragmentSView.setVisibility(View.GONE);
                    } else if (isFocused) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mPostBodyEditText.getWindowToken(), 0);
                        mPostBodyEditText.clearFocus();
                    } else {
                    }
                }
            });
            final FragmentMention fragmentMention = new FragmentMention();
            FragmentManager manager = getChildFragmentManager();
            final FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragmentSuggestionlist, fragmentMention).commit();
            mPostBodyEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    fragmentSView.setVisibility(View.GONE);

                    return false;
                }
            });

            mPostBodyEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable inputText) {
                    if (inputText.toString().contains(Constants.General.HASHTAG_SIGN)) {
                        int cursorPosition = mPostBodyEditText.getSelectionStart();
                        int finalPos = SepehrUtil.getLastCharPos(Constants.General.HASHTAG_SIGN, inputText.toString(), cursorPosition);
                        String hashtagText = inputText.toString().substring(finalPos, cursorPosition);


                        finalHashtagText = getHashtagSplite(hashtagText);
                        if (!Utils.isNullOrEmpty(finalHashtagText) && !finalHashtagText.contains(" ")) {
                            fragmentMention.loadFirstHashtagPage(finalHashtagText.trim());
                        } else {
                            fragmentSView.setVisibility(View.GONE);
                        }

                    }
                    if (inputText.toString().contains(Constants.General.MENTION_USER_SIGN)) {

                        int cursorPosition = mPostBodyEditText.getSelectionStart();
                        int finalPos = SepehrUtil.getLastCharPos(Constants.General.MENTION_USER_SIGN, inputText.toString(), cursorPosition);
                        String atUserText = inputText.toString().substring(finalPos, cursorPosition);


                        finalUserText = getUserSplite(atUserText);
                        if (!Utils.isNullOrEmpty(finalUserText) && !finalUserText.contains(" ")) {
                            fragmentMention.loadFirstMentionPage(finalUserText.trim(), "0", Pagination.MentionMode.USER);
                        } else {
                            fragmentSView.setVisibility(View.GONE);
                        }

                    }
                    if (inputText.toString().contains(Constants.General.MENTION_BUSINESS_SIGN)) {

                        int cursorPosition = mPostBodyEditText.getSelectionStart();
                        int finalPos = SepehrUtil.getLastCharPos(Constants.General.MENTION_BUSINESS_SIGN, inputText.toString(), cursorPosition);
                        String atUserText = inputText.toString().substring(finalPos, cursorPosition);


                        finalUserText = getBusinessSplite(atUserText);
                        if (!Utils.isNullOrEmpty(finalUserText) && !finalUserText.contains(" ")) {
                            fragmentMention.loadFirstMentionPage(finalUserText.trim(), "0", Pagination.MentionMode.BUSINESS);

                        } else {
                            fragmentSView.setVisibility(View.GONE);
                        }

                    }
                }

                private String getBusinessSplite(String business) {
                    String[] splitedText = business.split("\\" + Constants.General.MENTION_BUSINESS_SIGN);
                    String result = "";
                    if (splitedText.length > 0) {
                        result = splitedText[splitedText.length - 1];
                    }
                    return result;
                }

                private String getUserSplite(String user) {
                    String[] splitedText = user.split(Constants.General.MENTION_USER_SIGN);
                    String result = "";
                    if (splitedText.length > 0) {
                        result = splitedText[splitedText.length - 1];
                    }
                    return result;

                }

                private String getHashtagSplite(String hashtag) {
                    String[] splitedText = hashtag.split(Constants.General.HASHTAG_SIGN);
                    String result = "";
                    if (splitedText.length > 0) {
                        result = splitedText[splitedText.length - 1];
                    }
                    return result;

                }
            });

            mInsertImageImageButton.setOnClickListener(this);
            mInsertVideoImageButton.setOnClickListener(this);
            if (intent != null) {
                handleInputIntent(intent);
            }
            return view;
        } else

        {
            return view;
        }

    }


    @Override
    public void setVisibilityMentionFragment(boolean visible) {
        if (visible) {
            fragmentSView.setVisibility(View.VISIBLE);
        } else {
            fragmentSView.setVisibility(View.GONE);
        }
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

    }

    @Override
    public void onClick(View v) {

        try {
            switch (v.getId()) {

                case R.id.btnInsert:

                    final Picker.PickerBuilder pickerBuilder = new Picker.PickerBuilder(FragmentPostNew.this, -1, -1)
                            .setMultiple(false)
                            .setFreeStyle(true);

                    Picker picker = new Picker(pickerBuilder);
                    picker.start();
                    break;
                case R.id.image_button_insert_video:
                    Utils.pickVideo(this);

                    break;
            }
        } catch (Exception ex) {

            Toast.makeText(getContext(), ex.getMessage() + ex.getClass(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mMenu = menu;
    }

    @Override
    public void setProfileToMention(Profile profile) {
        ArrayList<Object> result = setMentioned(profile, mPostBodyEditText.getText().toString(), mPostBodyEditText.getSelectionStart());
        mentionsUser.add((String) result.get(0));
        mentionsEncrypt.add((String) result.get(1));
        mPostBodyEditText.setText((String) result.get(2));
        mPostBodyEditText.setSelection((Integer) result.get(3));
    }

    @Override
    public void setHastag(String hashTag) {
        ArrayList<Object> result = setHashtaged(hashTag, mPostBodyEditText.getText().toString(), mPostBodyEditText.getSelectionStart());


        mPostBodyEditText.setText((String) result.get(1));
        mPostBodyEditText.setSelection((Integer) result.get(2));
    }

    @Override
    public void setShopToMention(Shop shop) {
        ArrayList<Object> result = setMentioned(shop, mPostBodyEditText.getText().toString(), mPostBodyEditText.getSelectionStart());
        mentionsUser.add((String) result.get(0));
        mentionsEncrypt.add((String) result.get(1));
        mPostBodyEditText.setText((String) result.get(2));
        mPostBodyEditText.setSelection((Integer) result.get(3));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.home:

                Toast.makeText(getContext(), "asdasd", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_done:
                String postText = mPostBodyEditText.getText().toString().trim();

                for (int i = 0; i < mentionsUser.size(); i++) {
                    postText = postText.replaceFirst("\\" + mentionsUser.get(i), mentionsEncrypt.get(i));
                }
                Pattern startPatternHashtag = Pattern.compile("#\\w*");
                Matcher startMatcherHashtag = startPatternHashtag.matcher(mPostBodyEditText.getText().toString());
                while (startMatcherHashtag.find()) {

                    hashTags.add(startMatcherHashtag.group().replace("#", ""));
                }
                mPost.setTags(hashTags);
                mPost.setText(postText);
                mentionsUser.clear();
                if (postType instanceof Profile) {
                    mPost.setSenderProfile((Profile) postType);
                } else if (postType instanceof Shop) {
                    mPost.setSenderShop((Shop) postType);

                } else {
                    mPost.setSenderComplex((Complex) postType);

                }
                if (!Utils.isNullOrEmpty(mPost.getImageAddress()) || !Utils.isNullOrEmpty(mPost.getVideoAddress()) || !Utils.isNullOrEmpty(mPost.getText())) {
                    post(mPost);
                }
                break;
            case android.R.id.home:
                break;
        }
        return false;
    }

    private void post(Post post) {

        if (post.getVideoAddress() != null) {
            if (post.getThumb() == null) {
                return;
            }
        }
        mMenu.findItem(R.id.action_done).setVisible(false);
        mMenu.findItem(R.id.menu_loader).setVisible(true);
        HttpClient.post(getContext(), Constants.Server.Post.POST, new Gson().toJson(post, Post.class), "application/json", new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mentionsUser.clear();
                mentionsEncrypt.clear();
                String id = Utils.removeExtraQuotations(new String(responseBody));
                mPost.setId(id);

                if (getActivity().getWindow() != null) {

                    Utils.hideSoftKeyboard(getActivity(), getActivity().getWindow().getDecorView());
                }
                FragmentHandler.onBackPressed(getContext());
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                mMenu.findItem(R.id.action_done).setVisible(true);
                mMenu.findItem(R.id.menu_loader).setVisible(false);

                Toast.makeText(getContext(), getString(R.string.toast_error_posting_post), Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }

    @Override
    public void onDialogTakePhoto() {
        mDialog.dismiss();
    }

    @Override
    public void onDialogRemovePhoto() {

    }

    @Override
    public void onDialogFromGallery() {
        Utils.pickImage(this);
        mDialog.dismiss();
    }

    @Override
    public void onDialogFromVideo() {
        Utils.pickVideo(this);

    }

    private void uploadImage(String path, final boolean isThumb, String tag) {
        Upload upload = new Upload(getContext(), new File(path), tag, "image/jpeg");
        mPost.setSize(SepehrUtil.uriToRatio(Uri.parse(path)));

        upload.uploadImage(new IUploadCallback() {
            @Override
            public void onFileReceived(String fileName, String uploadedPath) {
                if (Utils.isNullOrEmpty(fileName) || Utils.isNullOrEmpty(uploadedPath)) {
                    mContainerLayout.removeAllViews();
                    Toast.makeText(getContext(), getString(R.string.toast_error_upload_image_failed), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    if (!isThumb) {
                        mPost.setVideoAddress(null);
                    }
                    mPost.setImageAddress(uploadedPath);
                    afterUploadSucceed();
                }
                mMenu.findItem(R.id.action_done).setVisible(true);
                mMenu.findItem(R.id.menu_loader).setVisible(false);

            }

            @Override
            public void onFailed(int statusCode) {
                emptyContainer();
                mMenu.findItem(R.id.action_done).setVisible(true);
                mMenu.findItem(R.id.menu_loader).setVisible(false);

            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                mProgressView.setProgress((bytesWritten * 1.0F / totalSize) * 100.0F);
            }
        });
    }

    private void uploadVideo(String path, final String videoThumb, String tag) {
        int[] wxh = Utils.getLocalVideoWidthAndHeight(path);
        Upload upload = new Upload(getContext(), new File(path), tag, "video/*");
        upload.uploadVideoWithConvertCrop(wxh[0], wxh[1], new IUploadCallback() {
            @Override
            public void onFileReceived(String fileName, String uploadedPath) {
                if (Utils.isNullOrEmpty(fileName) || Utils.isNullOrEmpty(uploadedPath)) {
                    Toast.makeText(getContext(), getString(R.string.toast_upload_video_failed), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    uploadImage(videoThumb, true, UUID.randomUUID().toString());
                    mPost.setVideoAddress(uploadedPath);
                    mPost.setVideoProperties(Utils.readableFileSize(new File(videoPath).length())
                            + " , " +
                            Utils.readableVideoDuration(videoDuration));
                    // afterUploadSucceed();
                }
            }

            @Override
            public void onFailed(int statusCode) {
                emptyContainer();
                mMenu.findItem(R.id.action_done).setVisible(true);
                mMenu.findItem(R.id.menu_loader).setVisible(false);

            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                mProgressView.setProgress((bytesWritten * 1.0F / totalSize) * 100.0F);
            }
        });
    }


    @Override
    public void onSizeChanged(int keyboardHeight) {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;

        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        int height = SepehrUtil.getScreenHeight(getActivity()) - AndroidUtilities.statusBarHeight - keyboardHeight - actionBarHeight;

        fragmentSView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));//getResources().getDimensionPixelSize(R.dimen.materialize_toolbar)));

    }
}
