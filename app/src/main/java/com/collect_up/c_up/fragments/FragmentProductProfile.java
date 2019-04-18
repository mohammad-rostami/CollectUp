/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.SuggestionProductAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.services.RealtimeService;
import com.collect_up.c_up.view.CircledNetworkImageView;
import com.collect_up.c_up.view.CustomSlider;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.google.gson.Gson;
import com.mikepenz.materialdrawer.Drawer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.rey.material.app.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

@SuppressLint("ValidFragment")
public class FragmentProductProfile extends BaseFragment
        implements RatingBar.OnRatingBarChangeListener, RatingBar.OnTouchListener {

    public static boolean isRunning;
    private final Profile mCurrentUser = Logged.Models.getUserProfile();
    @Bind(R.id.text_view_description)
    TextView descriptionTextView;
    @Bind(R.id.text_view_tags)
    TextView tagTextView;
    @Bind(R.id.txtAverageRate)
    TextView txtAverageRate;
    @Bind(R.id.text_view_price)
    TextView priceTextView;
    @Bind(R.id.btnChatProductAdmin)
    FloatingActionButton btnChatProductAdmin;
    @Bind(R.id.slider_layout_gallery)
    SliderLayout gallerySliderLayout;
    @Bind(R.id.rating_bar)
    RatingBar ratingBar;
    @Bind(R.id.text_view_votes)
    TextView votesTextView;
    @Bind(R.id.txtViews)
    TextView txtViews;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.scroll_view)
    ScrollView mScrollView;
    @Bind(R.id.image_view_picture)
    CircledNetworkImageView picture;
    @Bind(R.id.text_view_title)
    TextView title;
    @Bind(R.id.text_view_subtitle)
    TextView subtitle;
    @Bind(R.id.text_view_phone_number)
    TextView phoneNumber;
    @Bind(R.id.layoutRate)
    LinearLayout layoutRate;
    @Bind(R.id.relatedList)
    RecyclerView relatedList;
    private Product mProduct;
    private Drawer mDrawer;
    private Menu mMenu;
    private View view;
    private Menu mOptionsMenu;

    public FragmentProductProfile(Product product) {
        this.mProduct = product;
    }

    public FragmentProductProfile() {
    }

    private void getRelatedPosts() {
        HttpClient.get(String.format(Constants.Server.Product.GET_RELATED_PRODUCTS, mProduct.getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                Product[] products = GsonParser.getArrayFromGson(responseBody, Product[].class);
                if (products != null) {
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                    relatedList.setLayoutManager(layoutManager);
                    SuggestionProductAdapter adapter = new SuggestionProductAdapter(getContext(), products);
                    relatedList.setAdapter(adapter);
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

    private void getUpdatedProductAsync() {
        HttpClient.get(String.format(Constants.Server.Product.GET_PRODUCT, mProduct.getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mProduct = GsonParser.getObjectFromGson(responseBody, Product.class);
                initViews();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

            }
        });
    }

    private void initViews() {
        if (!TextUtils.isEmpty(mProduct.getShop().getImageAddress())) {
            MyApplication.getInstance().getImageLoader().loadImage(Constants.General.BLOB_PROTOCOL + mProduct.getShop().getThumb(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    picture.setImageBitmap(loadedImage);
                }
            });
        } else {
            picture.setImageResource(R.drawable.placeholder);
        }

        if (!TextUtils.isEmpty(mProduct.getShop().getPhoneNumber())) {
            phoneNumber.setText(mProduct.getShop().getPhoneNumber());
        } else {
            phoneNumber.setVisibility(View.GONE);
        }
        title.setText(mProduct.getShop().getName());
        subtitle.setText(mProduct.getName());

        gallerySliderLayout.removeAllSliders();

        if (mProduct.getImageAddresses() != null) {
            final List<String> imageList = new ArrayList<>();
            for (int i = 0; i < mProduct.getImageAddresses().size(); i++) {
                CustomSlider sliderView = new CustomSlider(getActivity(), "image", mProduct.getImageAddresses().get(i), null, null, i);
                imageList.add(mProduct.getImageAddresses().get(i));
                sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
                sliderView.setImageArray(imageList);
                gallerySliderLayout.addSlider(sliderView);
            }


        }

        if (!Utils.isNullOrEmpty(mProduct.getVideoAddress()) && !Utils.isNullOrEmpty(mProduct.getVideoThumbnail())) {
            CustomSlider sliderView = new CustomSlider(getActivity(), "video", mProduct.getVideoThumbnail(), mProduct.getVideoAddress(), null, 0);
            sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
            gallerySliderLayout.addSlider(sliderView);
        }

        if (!Utils.isNullOrEmpty(mProduct.getPdfAddress())) {
            CustomSlider sliderView = new CustomSlider(getActivity(), "pdf", null, null, mProduct.getPdfAddress(), 0);
            sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
            gallerySliderLayout.addSlider(sliderView);
        }

        if (!Utils.isNullOrEmpty(mProduct.getRatesCount()) && !Utils.isNullOrEmpty(mProduct.getRatesAverage())) {
            votesTextView.setText(Utils.getReadableCount(Integer.valueOf(mProduct.getRatesCount())));
            ratingBar.setRating(Float.valueOf(mProduct.getRatesAverage()));


            txtAverageRate.setText(mProduct.getRatesAverage().matches("[0-9]+\\.[0-9]*") ? mProduct.getRatesAverage() : mProduct.getRatesAverage() + ".0");
        } else {
            votesTextView.setText("0");
            ratingBar.setRating(0F);
            txtAverageRate.setText("0.0");

        }
        txtViews.setText(Utils.getReadableCount(mProduct.getViewCount()));
        if (mProduct.getShop().getAdminId() != null) {
            if (!mProduct.getShop().getAdminId().equalsIgnoreCase(Logged.Models.getUserProfile().getId())) {

                if (getActivity() != null) {

                    btnChatProductAdmin.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.colorAccent)));

                } else {

                    btnChatProductAdmin.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorAccent)));

                }

                btnChatProductAdmin.setEnabled(true);
            } else {
                btnChatProductAdmin.setEnabled(false);

                if (getActivity() != null) {
                    btnChatProductAdmin.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.gray)));
                } else {
                    btnChatProductAdmin.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.gray)));

                }
            }
        }


        if (ratingBar != null) {
            setRatingColor(ratingBar, R.color.divider, R.color.rating_color);
        }
        descriptionTextView.setText(mProduct.getDescription());
        descriptionTextView.setTransformationMethod(new LinkTransformationMethod(getActivity()));
        descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());
        layoutRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog mSheetDialog = new BottomSheetDialog(getContext());

                mSheetDialog.contentView(R.layout.bottom_sheet_rate)
                        .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .inDuration(300)
                        .cancelable(true)
                        .show();
                RatingBar bar = (RatingBar) mSheetDialog.findViewById(R.id.rating_bar);
                bar.setOnTouchListener(FragmentProductProfile.this);
                setRatingColor(bar, R.color.accent_opacity, R.color.colorAccent);

                bar.setOnRatingBarChangeListener(FragmentProductProfile.this);
            }
        });

        if (Utils.isNullOrEmpty(mProduct.getDescription())) {
            descriptionTextView.setVisibility(View.GONE);
        } else {
            descriptionTextView.setVisibility(View.VISIBLE);
        }

        priceTextView.setText(mProduct.getExtraProperties());
        if (Utils.isNullOrEmpty(mProduct.getExtraProperties())) {
            priceTextView.setVisibility(View.GONE);
        } else {
            priceTextView.setVisibility(View.VISIBLE);
        }
        String tags = null;
        if (mProduct.getTags() != null && mProduct.getTags().size() > 0) {
            for (int i = 0; i < mProduct.getTags().size(); i++) {
                tags += mProduct.getTags().get(i) + " , ";
            }
            tagTextView.setText(tags.substring(0, tags.length() - 2).replaceAll("null", ""));
            tagTextView.setVisibility(View.VISIBLE);


        } else {
            tagTextView.setVisibility(View.GONE);
        }

    }


    @Override
    public void onStop() {
        gallerySliderLayout.stopAutoCycle();
        isRunning = false;
        super.onStop();
    }


    @Override
    public void onDestroy() {
        gallerySliderLayout.stopAutoCycle();
        super.onDestroy();
    }

    private void checkPrivileges(Menu menu) {
        MenuItem actionEdit = menu.findItem(R.id.action_edit);
        MenuItem actionDelete = menu.findItem(R.id.action_delete);

        // Disable following items by default and enable them if the user has the right access.
        actionEdit.setVisible(false);
        actionDelete.setVisible(false);

        List<String> havePermission = mProduct.getShop().getManagersId();
        // Add admin to the have permission group to check in just one loop
        havePermission.add(mProduct.getShop().getAdminId());

        if (havePermission.contains(mCurrentUser.getId())) {
            actionEdit.setVisible(true);
            actionDelete.setVisible(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {

            view = inflater.inflate(R.layout.fragment_product, container, false);
            ButterKnife.bind(this, view);
            setHasOptionsMenu(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            mToolbar.inflateMenu(R.menu.menu_product);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    onOptionsItemSelected(item);
                    return false;
                }
            });
            mOptionsMenu = mToolbar.getMenu();
            checkPrivileges(mOptionsMenu);
            getUpdatedProductAsync();
            RelativeLayout.LayoutParams galleryLayoutParams = (RelativeLayout.LayoutParams) gallerySliderLayout
                    .getLayoutParams();
            galleryLayoutParams.height = getResources().getDisplayMetrics().widthPixels;
            gallerySliderLayout.setLayoutParams(galleryLayoutParams);
            gallerySliderLayout.setCustomIndicator((PagerIndicator) view.findViewById(R.id.custom_indicator));

            initViews();
            getRelatedPosts();
            mDrawer = DrawerHelper.forAllActivities(getActivity(), mToolbar, -1);
            return view;
        } else {
            return view;
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_product, menu);
        mMenu = menu;

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        checkPrivileges(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent intent = new Intent(getContext(), FragmentShare.class);
                CompactMessage message = new CompactMessage();
                message.setId(UUID.randomUUID().toString());
                message.setSender(Logged.Models.getUserProfile());
                message.setContentType(EnumMessageContentType.SharedProduct);
                message.setText(mProduct.getName());
                message.setSendDateTime(Long.toString(System.currentTimeMillis()));
                message.setContentSize(new Gson().toJson(mProduct, Product.class));
                intent.putExtra("message", message);
                FragmentHandler.replaceFragment(getContext(), fragmentType.SHARE, intent);

                break;
            case R.id.action_edit:
                FragmentHandler.replaceFragment(getContext(), fragmentType.EDITPRODUCT, mProduct);
                break;
            case android.R.id.home:
                break;
            case R.id.action_delete:
                final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
                builder.message(R.string.sure_to_delete_product)
                        .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
                        .title(R.string.are_you_sure)
                        .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                        .positiveAction(R.string.im_sure)
                        .negativeAction(R.string.cancel)
                        .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                        .setCancelable(true);

                builder.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();

                    }
                });
                builder.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HttpClient.delete(String.format(Constants.Server.Product.DELETE_BY_ID, mProduct
                                .getId()), new AsyncHttpResponser(getContext()) {
                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  byte[] responseBody) {
                                builder.dismiss();
                                FragmentHandler.onBackPressed(getContext());

                            }

                            @Override
                            public void onFailure(int statusCode,
                                                  Header[] headers,
                                                  byte[] responseBody,
                                                  Throwable error) {
                                super.onFailure(statusCode, headers, responseBody, error);

                                Toast.makeText(getContext(), getString(R.string.toast_error_deleting_complex), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    }
                });
                builder.show();
                break;
        }
        return false;
    }

    @Override
    public void onResume() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

        super.onResume();

        if (mDrawer != null) {
            DrawerHelper.update(mDrawer, getActivity());
            mDrawer.setSelection(-1);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        isRunning = true;
    }

    private void setRatingColor(RatingBar bar, int colorNormal, int colorFill) {
        LayerDrawable stars = (LayerDrawable) bar.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(ContextCompat.getColor(getContext(), colorNormal), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(ContextCompat.getColor(getContext(), colorFill), PorterDuff.Mode.SRC_ATOP);

    }

    @Override
    public void onRatingChanged(final RatingBar ratingBar, final float rating, boolean fromUser) {
        String url = String.format(Constants.Server.Product.GET_RATE, mProduct.getId(), Float.toString(rating));
        if (fromUser) {
            HttpClient.get(url, new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    CheckOut checkOut = GsonParser.getObjectFromGson(responseBody, CheckOut.class);
                    votesTextView.setText(Integer.toString(checkOut.getId()));
                    String averageVotes = checkOut.getValue();
                    FragmentProductProfile.this.ratingBar.setRating(Float.valueOf(averageVotes));
                    mProduct.setRatesCount(Integer.toString(checkOut.getId()));
                    mProduct.setRatesAverage(checkOut.getValue());
                    txtAverageRate.setText(averageVotes.matches("[0-9]+\\.[0-9]*") ? averageVotes : averageVotes + ".0");

                    setRatingColor(ratingBar, R.color.green_opacity, R.color.green);

                }

                @Override
                public void onFailure(int statusCode,
                                      Header[] headers,
                                      byte[] responseBody,
                                      Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                    Toast.makeText(getContext(), getString(R.string.toast_error_updating_rate), Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }

    @OnClick(R.id.btnChatProductAdmin)
    public void chatWithAdmin() {

        if (!mProduct.getShop().getAdminId().equalsIgnoreCase(Logged.Models.getUserProfile().getId())) {

            final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
            final CompactChat chat;
            RChat existChat = mRealm.where(RChat.class).equalTo("ReceiverId", mProduct.getShop().getAdminId()).findFirst();
            if (existChat != null) {
                chat = RToNonR.rChatToChat(existChat);
            } else {
                chat = new CompactChat();
                chat.setReceiverId(mProduct.getShop().getAdminId());
                chat.setMembersCount(2);
                chat.setAmIManager(true);
                chat.setAmISuperAdmin(true);
                chat.setChatId("");
                chat.setUnSeenMessageCount(0);
                chat.setIsGroup(false);
                chat.setProfileThumbnailAddress(mProduct.getShop().getImageAddress());
                chat.setTitle(mProduct.getShop().getName());
            }


            final CompactMessage message = new CompactMessage();
            message.setId(UUID.randomUUID().toString());
            message.setChatId(chat.getChatId());
            message.setReceiverId(mProduct.getShop().getAdminId());
            message.setSender(Logged.Models.getUserProfile());
            message.setContentType(EnumMessageContentType.SharedProduct);
            message.setText(mProduct.getName());
            message.setSendDateTime(Long.toString(System.currentTimeMillis()));
            message.setContentSize(new Gson().toJson(mProduct, Product.class));


            Logged.Models.ProductMessage = message;
            RealtimeService.invokeSendMessage(message, false);

            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));
                    FragmentHandler.replaceFragment(getContext(), fragmentType.CHAT, chat);

                }
            });

        }
    }

    @OnClick(R.id.text_view_title)
    public void openShopProfileForTitle() {
        FragmentHandler.replaceFragment(getContext(), fragmentType.BUSINESS, mProduct.getShop());
    }

    @OnClick(R.id.image_view_picture)
    public void openShopProfileForPicture() {
        FragmentHandler.replaceFragment(getContext(), fragmentType.BUSINESS, mProduct.getShop());
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setRatingColor((RatingBar) v, R.color.accent_opacity, R.color.colorAccent);

        return false;
    }

}
