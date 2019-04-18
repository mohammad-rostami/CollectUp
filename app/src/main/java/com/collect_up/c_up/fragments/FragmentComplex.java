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
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.activities.ActivityImageViewer;
import com.collect_up.c_up.adapters.pagers.ComplexPagerFragmentAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.BaseModel;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.services.RealtimeService;
import com.collect_up.c_up.view.CircledNetworkImageView;
import com.google.gson.Gson;
import com.mikepenz.materialdrawer.Drawer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

@SuppressLint ("ValidFragment")

public class FragmentComplex extends BaseFragment
  implements AppBarLayout.OnOffsetChangedListener {
  public static boolean isRunning;
  public static String complexId;
  private final Profile mCurrentUser = Logged.Models.getUserProfile();
  @Bind (R.id.app_bar_layout)
  public AppBarLayout appBarLayout;
  @Bind (R.id.toolbar)
  public Toolbar toolbar;
  @Bind (R.id.image_view_picture)
  CircledNetworkImageView pictureImageView;
  @Bind (R.id.image_view_background)
  ImageView mBluredImageView;
  @Bind (R.id.imgOfficial)
  ImageView imgOfficial;

  ComplexPagerFragmentAdapter complexPagerFragmentAdapter;
  @Bind (R.id.tab_layout)
  TabLayout tabLayout;
  @Bind (R.id.text_view_title)
  TextView titleTextView;
  @Bind (R.id.viewpagerBusiness)
  ViewPager viewPager;

  private CollapsingToolbarLayout mCollapsingToolbarLayout;
  private Menu mOptionsMenu;
  private Complex mComplex;
  private Drawer mDrawer;
  private int mCurrentItem = 3;
  private View view;


  public FragmentComplex() {
  }


  public FragmentComplex(Complex shop) {
    this.mComplex = shop;
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    try
    {
      if (isVisibleToUser)
      {
        isRunning = true;
      } else
      {
        isRunning = false;
      }
    } catch (Exception ex)
    {
    }
  }

  @Override
  public void onStop() {
    isRunning = false;
    complexId = null;
    super.onStop();
  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.complex);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();

    if (mDrawer != null)
    {

      DrawerHelper.update(mDrawer, getActivity());

      Complex myComplex = Logged.Models.getUserComplex();

      if (myComplex != null && mComplex.getId().equals(myComplex.getId()))
      {
        mDrawer.setSelection(3);
      } else
      {
        mDrawer.setSelection(-1);
      }
    }
  }


  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
    complexId = mComplex.getId();
  }


  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_complex, container, false);
      ButterKnife.bind(this, view);

      ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

      toolbar.setTitle(R.string.complex);
      toolbar.inflateMenu(R.menu.menu_complex);
      toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          onOptionsItemSelected(item);
          return false;
        }
      });
      mOptionsMenu = toolbar.getMenu();
      checkPrivileges(mOptionsMenu);
      toolbar.setTitleTextColor(Color.WHITE);

      initActionBar();


      appBarLayout.addOnOffsetChangedListener(this);
      complexPagerFragmentAdapter = new ComplexPagerFragmentAdapter(getActivity(), getChildFragmentManager(), mComplex);
      viewPager.setAdapter(complexPagerFragmentAdapter);
      viewPager.setOffscreenPageLimit(4);
      tabLayout.setupWithViewPager(viewPager);
      // Set current tab to info tab
      viewPager.setCurrentItem(mCurrentItem);

      initViews();

      mCollapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
      mCollapsingToolbarLayout.setTitleEnabled(false);
      mCollapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white));
      mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.white));

      Complex myComplex = Logged.Models.getUserComplex();

      if (myComplex != null && mComplex.getId().equals(myComplex.getId()))
      {
        mDrawer = DrawerHelper.forAllActivities(getActivity(), toolbar, 3);
      } else
      {
        mDrawer = DrawerHelper.forAllActivities(getActivity(), toolbar, -1);
      }
      return view;
    } else
    {
      return view;
    }


  }

  private void initViews() {
    switch (mComplex.getBusinessLevel())
    {
      case BaseModel.BUSINESS_LEVEL_NONE:
        imgOfficial.setVisibility(View.GONE);
        break;
      case BaseModel.BUSINESS_LEVEL_DIAMOND:
        imgOfficial.setImageResource(R.drawable.businesslevel_diamond);
        break;
      case BaseModel.BUSINESS_LEVEL_GOLD:
        imgOfficial.setImageResource(R.drawable.businesslevel_gold);
        break;
      case BaseModel.BUSINESS_LEVEL_SILVER:
        imgOfficial.setImageResource(R.drawable.businesslevel_silver);
        break;
      case BaseModel.BUSINESS_LEVEL_BRONZE:
        imgOfficial.setImageResource(R.drawable.businesslevel_bronze);
        break;
    }
    if (mComplex != null)
    {
      titleTextView.setText(mComplex.getName());
    }
    pictureImageView.setImageResource(R.drawable.placeholder);
    if (!Utils.isNullOrEmpty(mComplex.getCoverPhoto()))
    {

      MyApplication.getInstance().getImageLoader().displayImage(Constants.General.BLOB_PROTOCOL + mComplex.getCoverPhoto(), mBluredImageView);
    } else
    {
      mBluredImageView.setImageResource(R.drawable.blur_background);
    }

    if (!Utils.isNullOrEmpty(mComplex.getImageAddress()))
    {
      MyApplication.getInstance().getImageLoader().displayImage(Constants.General.BLOB_PROTOCOL + mComplex.getThumb(), pictureImageView);


      pictureImageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(getContext(), ActivityImageViewer.class);
          intent.putExtra("address", Constants.General.BLOB_PROTOCOL + mComplex.getImageAddress());
          intent.putExtra("title", mComplex.getName());
          startActivity(intent);
        }
      });
    } else
    {
      pictureImageView.setImageResource(R.drawable.placeholder);
      pictureImageView.setOnClickListener(null);
    }

  }

  private void initActionBar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null)
    {
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
    }
  }


  @Override
  public void onDestroy() {
    if (DrawerHelper.getDrawer() != null)
    {
      DrawerHelper.getDrawer().setSelection(-1);
    }
    super.onDestroy();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_complex, menu);
    MenuItem item = menu.findItem(R.id.action_chat);

    if (mComplex.getAdminId().equalsIgnoreCase(Logged.Models.getUserProfile().getId()))
    {
      item.setVisible(false);
    } else
    {
      item.setVisible(true);
    }
    mOptionsMenu = menu;
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    checkPrivileges(menu);

  }


  private void checkPrivileges(Menu menu) {

    MenuItem actionEdit = menu.findItem(R.id.action_edit);
    MenuItem actionDelete = menu.findItem(R.id.action_delete);
    MenuItem actionNewEvent = menu.findItem(R.id.action_new_event);
    MenuItem actionAddManager = menu.findItem(R.id.action_add_manager);
    MenuItem actionAddShop = menu.findItem(R.id.action_add_shop);
    MenuItem actionAddPost = menu.findItem(R.id.action_add_post);
    MenuItem actionSetAdmin = menu.findItem(R.id.action_set_admin);

    // Disable following items by default and enable them if the user has the right access.
    actionEdit.setVisible(false);
    actionDelete.setVisible(false);
    actionNewEvent.setVisible(false);
    actionAddManager.setVisible(false);
    actionAddShop.setVisible(false);
    actionAddPost.setVisible(false);
    actionSetAdmin.setVisible(false);

    List<String> havePermission = new ArrayList<>();

    if (mComplex.getManagersId() != null)
    {
      havePermission.addAll(mComplex.getManagersId());
    }
    // Add admin to the have permission group to check in just one loop
    havePermission.add(mComplex.getAdminId());

    for (String profileId : havePermission)
    {
      if (profileId.equals(mCurrentUser.getId()))
      {
        actionNewEvent.setVisible(true);
        actionAddShop.setVisible(true);
        actionAddPost.setVisible(true);
        break;
      }
    }

    // If the logged user is the admin
    if (mComplex.getAdminId().equals(mCurrentUser.getId()))
    {
      actionEdit.setVisible(true);
      actionDelete.setVisible(true);
      actionAddManager.setVisible(true);
      actionSetAdmin.setVisible(true);

    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case R.id.action_share:
        Intent intent = new Intent(getContext(), FragmentShare.class);
        CompactMessage message = new CompactMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSender(Logged.Models.getUserProfile());
        message.setContentType(EnumMessageContentType.SharedComplex);
        message.setText(mComplex.getName());
        message.setSendDateTime(Long.toString(System.currentTimeMillis()));
        mComplex.setFollowers(null);
        message.setContentSize(new Gson().toJson(mComplex, Complex.class));
        intent.putExtra("message", message);
        FragmentHandler.replaceFragment(getContext(), fragmentType.SHARE, intent);

        //startActivity(intent);
        break;
      case R.id.action_chat:
        Logged.Models.ProductMessage = null;
        if (!mComplex.getAdminId().equalsIgnoreCase(Logged.Models.getUserProfile().getId()))
        {
          final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
          final CompactChat chat;
          RChat existChat = mRealm.where(RChat.class).equalTo("ReceiverId", mComplex.getAdminId()).findFirst();
          if (existChat != null)
          {
            chat = RToNonR.rChatToChat(existChat);
          } else
          {
            chat = new CompactChat();
            chat.setReceiverId(mComplex.getAdminId());
            chat.setMembersCount(2);
            chat.setAmIManager(true);
            chat.setAmISuperAdmin(true);
            chat.setChatId("");
            chat.setUnSeenMessageCount(0);
            chat.setIsGroup(false);
            chat.setProfileThumbnailAddress(mComplex.getImageAddress());
            chat.setTitle(mComplex.getName());
          }


          final CompactMessage sendMessage = new CompactMessage();
          sendMessage.setId(UUID.randomUUID().toString());
          sendMessage.setChatId(chat.getChatId());
          sendMessage.setReceiverId(mComplex.getAdminId());
          sendMessage.setSender(Logged.Models.getUserProfile());
          sendMessage.setContentType(EnumMessageContentType.SharedProduct);
          sendMessage.setText(mComplex.getName());
          sendMessage.setSendDateTime(Long.toString(System.currentTimeMillis()));
          sendMessage.setContentSize(new Gson().toJson(mComplex, Complex.class));


          Logged.Models.ProductMessage = sendMessage;
          RealtimeService.invokeSendMessage(sendMessage, false);

          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(sendMessage));
              FragmentHandler.replaceFragment(getContext(), fragmentType.CHAT, chat);


            }
          });

        }
        break;
      case R.id.action_add_manager:
        FragmentHandler.replaceFragment(getContext(), fragmentType.ADD_MANAGER, mComplex);
        break;
      case R.id.action_set_admin:
        FragmentHandler.replaceFragment(getContext(), fragmentType.SET_ADMIN, mComplex);
        break;
      case R.id.action_new_event:
        FragmentHandler.replaceFragment(getContext(), fragmentType.NEWEVENT, mComplex);
        break;
      case R.id.action_edit:
        FragmentHandler.replaceFragment(getContext(), fragmentType.EDITCOMPLEX, mComplex);
        break;
      case R.id.action_delete:
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
        builder.message(R.string.sure_to_delete_complex)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .title(R.string.are_you_sure)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.im_sure)
          .negativeAction(R.string.cancel)
          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        builder.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            builder.dismiss();

          }
        });
        builder.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            HttpClient.delete(String.format(Constants.Server.Complex.DELETE_BY_ID, mComplex
              .getId()), new AsyncHttpResponser(getContext()) {
              @Override
              public void onSuccess(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody) {
                if (mComplex.getId().equals(Logged.Models.getUserComplex().getId()))
                {
                  Logged.Models.setUserComplex(null);

                  // agar complex ke delete mishavad, dar shop man set shode bood, az shop hazfesh kon.
                  if (Logged.Models.getUserShop() != null)
                  {
                    Complex myShopComplex = Logged.Models.getUserShop().getComplex();
                    if (myShopComplex != null && myShopComplex.getId().equals(mComplex.getId()))
                    {
                      Shop shop = Logged.Models.getUserShop();
                      shop.setComplex(null);
                      Logged.Models.setUserShop(shop);

                    }
                  }
                }
                builder.dismiss();
                FragmentHandler.onBackPressed(getContext());
                DrawerHelper.update(DrawerHelper.getDrawer(), getActivity());
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
      case R.id.action_add_shop:
        FragmentHandler.replaceFragment(getContext(), fragmentType.ADD_COMPLEX_BUSINESS, mComplex);
        break;
      case R.id.action_add_post:
        FragmentHandler.replaceFragment(getContext(), fragmentType.NEWPOST, mComplex);
        break;
      case android.R.id.home:
        break;
    }
    return false;
  }

  @Override
  public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
    int verticalPos = -1 * (appBarLayout.getHeight() - toolbar.getHeight() - 150);

    if (i != 0)
    {
      if (i <= verticalPos)
      {
        if (mDrawer.getActionBarDrawerToggle().isDrawerIndicatorEnabled())
        {
          onOffsetChanged(true);
        }
      } else if (i > verticalPos && i < verticalPos + (-verticalPos / 2))
      {
        if (!mDrawer.getActionBarDrawerToggle().isDrawerIndicatorEnabled())
        {
          onOffsetChanged(false);
        }
      }
    } else
    {
      if (!mDrawer.getActionBarDrawerToggle().isDrawerIndicatorEnabled())
      {
        onOffsetChanged(false);
      }
    }
  }

  private void onOffsetChanged(boolean hide) {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null)
    {
      if (hide)
      {
        toolbar.getMenu().setGroupVisible(R.id.menu_group_hide, false);
        toolbar.setTitle("");
        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primary_dark));
      } else
      {
        toolbar.getMenu().setGroupVisible(R.id.menu_group_hide, true);
        toolbar.invalidate();
        onPrepareOptionsMenu(mOptionsMenu);
        toolbar.setTitle(R.string.complex);
        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorAccent));
      }
    }
  }


}
