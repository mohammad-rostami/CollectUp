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
import com.collect_up.c_up.adapters.pagers.PersonPagerFragmentAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.JsonHttpResponser;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.model.realm.RProfile;
import com.collect_up.c_up.receivers.RealtimeReceiver;
import com.collect_up.c_up.view.CircledNetworkImageView;
import com.mikepenz.materialdrawer.Drawer;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

import static com.collect_up.c_up.R.string.profile;

@SuppressLint ("ValidFragment")
public class FragmentPerson extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

  public static boolean isRunning;
  public static String profileId;
  private final int mCurrentTab = 1;
  @Bind (R.id.app_bar_layout)
  AppBarLayout appBarLayout;
  @Bind (R.id.tab_layout)
  TabLayout tabLayout;
  @Bind (R.id.text_view_title)
  TextView titleTextView;
  @Bind (R.id.toolbar)
  Toolbar toolbar;
  @Bind (R.id.collapsing_toolbar_layout)
  CollapsingToolbarLayout mCollapsingToolbarLayout;
  @Bind (R.id.viewpagerBusiness)
  ViewPager viewPager;
  @Bind (R.id.image_view_picture)
  CircledNetworkImageView pictureImageView;
  @Bind (R.id.image_view_background)
  ImageView mBluredImageView;
  @Bind (R.id.imgOfficial)
  ImageView imgOfficial;
  private Profile mProfile;
  private Menu optionsMenu;
  private Drawer mDrawer;
  private PersonPagerFragmentAdapter pager;
  public boolean isBlocked;
  private View view;


  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
    profileId = null;
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
  public void onDestroy() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    if (DrawerHelper.getDrawer() != null)
    {
      DrawerHelper.getDrawer().setSelection(-1);
    }
    super.onDestroy();
  }


  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    if (mDrawer != null)
    {
      DrawerHelper.update(mDrawer, getActivity());

      Profile myProfile = Logged.Models.getUserProfile();

      if (myProfile != null && mProfile.getId().equals(myProfile.getId()))
      {
        mDrawer.setSelection(1);
      } else
      {
        mDrawer.setSelection(-1);
      }
    }

    super.onResume();
  }


  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
    profileId = mProfile.getId();
  }

  private void initActionBar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null)
    {
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
    }
  }

  public void initViews(Profile profile) {
    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(MyApplication.context));
    RProfile rProfile = realm.where(RProfile.class).equalTo("Id", profile.getId()).findFirst();
    titleTextView.setText(profile.getName());
    if (rProfile != null && rProfile.getIsOfficial())
    {
      imgOfficial.setVisibility(View.VISIBLE);

    } else
    {
      imgOfficial.setVisibility(View.GONE);
    } if (!Utils.isNullOrEmpty(mProfile.getCoverPhoto()))

    {

      MyApplication.getInstance().getImageLoader().displayImage(Constants.General.BLOB_PROTOCOL + mProfile.getCoverPhoto(), mBluredImageView);
    } else
    {

      mBluredImageView.setImageResource(R.drawable.blur_background);
    }
    if (!Utils.isNullOrEmpty(mProfile.getImageAddress()))
    {

      MyApplication.getInstance().getImageLoader().displayImage(Constants.General.BLOB_PROTOCOL + mProfile.getThumb(), pictureImageView);

      pictureImageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(getContext(), ActivityImageViewer.class);
          intent.putExtra("address", Constants.General.BLOB_PROTOCOL + mProfile.getImageAddress());
          intent.putExtra("title", mProfile.getName());
          startActivity(intent);
        }
      });


    } else

    {
      pictureImageView.setImageResource(R.drawable.placeholder_profile);
      pictureImageView.setOnClickListener(null);
    }

  }

  private void checkPrivileges(Menu menu) {
    MenuItem actionSendMessage = menu.findItem(R.id.action_send_message);

    // Disable following items by default and enable them if the user has the right access.
    actionSendMessage.setVisible(true);

    // If the logged user is the admin
    if (mProfile.getId().equals(Logged.Models.getUserProfile().getId()))
    {
      actionSendMessage.setIcon(R.drawable.ic_settings);
      actionSendMessage.setTitle(R.string.settings);
    } else
    {
      if (mProfile.getPrivateMessaging())
      {
        if ((mProfile.getFollowing().contains((String) Hawk.get("profileId")) || mProfile.getFollowers().contains((String) Hawk.get("profileId"))))
        {
          actionSendMessage.setVisible(true);
        } else
        {
          actionSendMessage.setVisible(false);
        }
      } else
      {
        actionSendMessage.setVisible(true);
      }
    }
  }

  private void amI() {
    if (Logged.Models.getUserProfile().getId().equals(mProfile.getId()))
    {
      optionsMenu.findItem(R.id.action_deny).setVisible(false);
      optionsMenu.findItem(R.id.action_block).setVisible(false);
    } else
    {
      if (isUserDenied())
      {
        optionsMenu.findItem(R.id.action_deny).setTitle(R.string.action_allow_user);
      }
      if (isBlocked)
      {
        optionsMenu.findItem(R.id.action_block).setTitle(R.string.action_unblock_user);

      }
    }
  }

  private boolean isUserDenied() {
    return Logged.Models.getUserProfile().getMutedProfilesId().contains(mProfile.getId());
  }

  private void changeMenuBlock() {
  }

  public FragmentPerson(Profile mProfile) {
    this.mProfile = mProfile;
  }

  public FragmentPerson() {
  }

  @Nullable

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_person_profile, container, false);
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

      toolbar.setTitleTextColor(Color.WHITE);
      toolbar.setTitle(profile);
      toolbar.inflateMenu(R.menu.menu_person_profile);
      toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          onOptionsItemSelected(item);
          return false;
        }
      });
      optionsMenu = toolbar.getMenu();

      checkPrivileges(optionsMenu);
      amI();
      initActionBar();

      assert mProfile != null;
      profileId = mProfile.getId();

      appBarLayout.addOnOffsetChangedListener(this);

      pager = new PersonPagerFragmentAdapter(getContext(), getChildFragmentManager(), mProfile);
      viewPager.setAdapter(pager);
      // Set current tab to info tab
      viewPager.setCurrentItem(mCurrentTab);
      tabLayout.setupWithViewPager(viewPager);

      initViews(mProfile);

      mCollapsingToolbarLayout.setTitleEnabled(false);
      mCollapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white));
      mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.white));
      HttpClient.get(String.format(Constants.Server.Profile.GET_ISBLOCK, mProfile.getId()), new JsonHttpResponser(getContext()) {

          @Override
          public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);

            try
            {
              isBlocked = response.getBoolean("IsBlocked");
              if (isBlocked)
              {

                changeMenuBlock();
              } else
              {
              }
            } catch (JSONException e)
            {
              e.printStackTrace();
            }
          }

          @Override
          public void onFailure(int statusCode, Header[] headers, String responseString, Throwable
            throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
          }


        }

      );
      Profile myProfile = Logged.Models.getUserProfile();
      if (myProfile != null && mProfile.getId().equals(myProfile.getId()))
      {
        mDrawer = DrawerHelper.forAllActivities(getActivity(), toolbar, 1);
      } else
      {
        mDrawer = DrawerHelper.forAllActivities(getActivity(), toolbar, -1);
      }
      mDrawer.closeDrawer();
      return view;
    } else
    {
      return view;
    }
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_person_profile, menu);
    optionsMenu = menu;
  }


  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    checkPrivileges(menu);

    amI();

  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case R.id.action_send_message:
        if (!mProfile.getId().equals((String) Hawk.get("profileId")))
        {
          final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
          CompactChat chat;
          RChat existChat = mRealm.where(RChat.class).equalTo("ReceiverId", mProfile.getId()).findFirst();
          if (existChat != null)
          {
            chat = RToNonR.rChatToChat(existChat);
          } else
          {
            chat = new CompactChat();
            chat.setReceiverId(mProfile.getId());
            chat.setMembersCount(2);
            chat.setAmIManager(true);
            chat.setAmISuperAdmin(true);
            chat.setChatId("");
            chat.setUnSeenMessageCount(0);
            chat.setIsGroup(false);
            chat.setProfileThumbnailAddress(mProfile.getImageAddress());
            chat.setTitle(mProfile.getName());
          }
          Intent intent = new Intent(getContext(), RealtimeReceiver.class);
          intent.putExtra("chat", chat);
          intent.setAction(Constants.General.UPDATE_CHAT_UI);
          intent.putExtra("method", "onNewChat");
          getContext().sendBroadcast(intent);
          mRealm.close();

        } else
        {
          FragmentHandler.replaceFragment(getContext(), fragmentType.PREFERENCE, null);
        }

        break;
      case R.id.action_block:
        final com.rey.material.app.SimpleDialog blockBuilder = new com.rey.material.app.SimpleDialog(getContext());

        if (!isBlocked)
        {


          blockBuilder.message(R.string.sure_to_block_contact)
            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
            .title(R.string.block_contact_title)
            .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
            .positiveAction(R.string.action_block)
            .negativeAction(R.string.cancel)
            .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
          blockBuilder.setCancelable(true);
          blockBuilder.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              blockBuilder.dismiss();

            }
          });
          blockBuilder.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              HttpClient.get(String.format(Constants.Server.Profile.GET_BLOCK, mProfile.getId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                  super.onSuccess(statusCode, headers, responseBody);
                  changeMenuBlock();
                  isBlocked = true;

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                  super.onFailure(statusCode, headers, responseBody, error);
                }
              });
              blockBuilder.dismiss();


            }
          });

          blockBuilder.show();
        } else

        {

          blockBuilder.message(R.string.sure_to_unblock_contact)
            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
            .title(R.string.unblock_contact_title)
            .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
            .positiveAction(R.string.unblock)
            .negativeAction(R.string.cancel)
            .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
          blockBuilder.setCancelable(true);
          blockBuilder.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              blockBuilder.dismiss();

            }
          });
          blockBuilder.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              HttpClient.get(String.format(Constants.Server.Profile.GET_UNBLOCK, mProfile.getId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                  super.onSuccess(statusCode, headers, responseBody);
                  isBlocked = false;
                  getActivity().invalidateOptionsMenu();
                  String blockedList = Logged.Models.getUserProfile().getBlockList() != null ? Logged.Models.getUserProfile().getBlockList() : "";
                  blockedList = blockedList.replace(mProfile.getId() + ",", "");
                  Logged.Models.getUserProfile().setBlockList(blockedList);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                  super.onFailure(statusCode, headers, responseBody, error);
                }
              });
              blockBuilder.dismiss();


            }
          });

          blockBuilder.show();
        }

        break;
      case R.id.action_deny:
        if (isUserDenied())

        {

          final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
          builder.message(R.string.sure_to_allow_contact)
            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
            .title(R.string.allow_contact_title)
            .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
            .positiveAction(R.string.allow)
            .negativeAction(R.string.cancel)
            .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
          builder.setCancelable(true);
          builder.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              builder.dismiss();

            }
          });
          builder.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              HttpClient.get(String.format(Constants.Server.Profile.GET_ALLOW, mProfile.getId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                  builder.dismiss();
                  if (Logged.Models.getUserProfile().getMutedProfilesId().contains(mProfile.getId()))
                  {
                    Profile profile = Logged.Models.getUserProfile();
                    profile.getMutedProfilesId().remove(mProfile.getId());
                    Logged.Models.setUserProfile(profile);
                  }
                  optionsMenu.findItem(R.id.action_deny).setTitle(R.string.action_deny_user);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                  super.onFailure(statusCode, headers, responseBody, error);

                  Toast.makeText(getContext(), R.string.toast_error_deny, Toast.LENGTH_SHORT).show();
                }
              });
            }
          });

          builder.show();

        } else

        {

          final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
          builder.message(R.string.sure_to_deny_contact)
            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
            .title(R.string.allow_contact_title)
            .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
            .positiveAction(R.string.deny)
            .negativeAction(R.string.cancel)
            .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
          builder.setCancelable(true);
          builder.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              builder.dismiss();

            }
          });
          builder.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              HttpClient.get(String.format(Constants.Server.Profile.GET_DENY, mProfile.getId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                  builder.dismiss();
                  if (!Logged.Models.getUserProfile().getMutedProfilesId().contains(mProfile.getId()))
                  {
                    Profile profile = Logged.Models.getUserProfile();
                    profile.getMutedProfilesId().add(mProfile.getId());
                    Logged.Models.setUserProfile(profile);
                  }
                  optionsMenu.findItem(R.id.action_deny).setTitle(R.string.action_allow_user);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                  super.onFailure(statusCode, headers, responseBody, error);

                  Toast.makeText(getContext(), R.string.toast_error_deny, Toast.LENGTH_SHORT).show();
                }
              });
            }
          });

          builder.show();

        }

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
          onOffsetChanged(true);
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
        toolbar.getMenu().setGroupVisible(R.id.menu_group_hide, false);//setGroupVisible(R.id.menu_group_hide, false);
        toolbar.setTitle("");
        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primary_dark));
      } else
      {
        toolbar.getMenu().setGroupVisible(R.id.menu_group_hide, true);
        toolbar.invalidate();
        onPrepareOptionsMenu(optionsMenu);
        toolbar.setTitle(profile);
        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorAccent));
      }
    }
  }

}
