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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.collect_up.c_up.BuildConfig;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.pagers.TimelinePagerFragmentAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.orhanobut.hawk.Hawk;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

@SuppressLint ("ValidFragment")

public class FragmentTimeline extends BaseFragment {
  private View view;

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    try
    {
      if (isVisibleToUser)
      {
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
      } else
      {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      }
    } catch (Exception ex)
    {
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

  }

  public static boolean isRunning;
  private final int mCurrentTab = 0;
  @Bind (R.id.tab_layout)
  TabLayout tabLayout;
  @Bind (R.id.toolbar)
  Toolbar toolbar;
  @Bind (R.id.timelineViewPager)
  ViewPager viewPager;

  public static FragmentTimeline newInstance(int index) {
    FragmentTimeline f = new FragmentTimeline();
    Bundle args = new Bundle();
    args.putInt("index", index);
    f.setArguments(args);
    return f;
  }

  public void hideToolbar(boolean hide) {
    if (hide)
    {
      toolbar.setVisibility(View.GONE);
    } else
    {
      toolbar.setVisibility(View.VISIBLE);
      toolbar.setTitle(R.string.tab_explore);
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          getActivity().onBackPressed();
        }
      });
    }
  }

  public void hideTabBar(boolean hide) {
    if (hide)
    {
      tabLayout.setVisibility(View.GONE);
    } else
    {
      tabLayout.setVisibility(View.VISIBLE);
    }
  }


  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_timeline_holder, container, false);
      ButterKnife.bind(this, view);


      viewPager.setAdapter(new TimelinePagerFragmentAdapter(getContext(), getChildFragmentManager()));
      tabLayout.setupWithViewPager(viewPager);
      // Set current tab to info tab
      viewPager.setCurrentItem(mCurrentTab);

      checkForNewVersion();
      return view;

    } else
    {

      return view;
    }
  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    hideToolbar(true);
    ((ActivityHome) getActivity()).changeButtonBackgroud(0);
    tabLayout.setVisibility(View.VISIBLE);
    super.onResume();
  }

  @Override
  public void onStop() {
    isRunning = false;
    super.onStop();

  }

  @Override
  public void onStart() {
    isRunning = true;
    super.onStart();
  }

  private void checkForNewVersion() {
    HttpClient.get(String.format(Constants.Server.Init.GET_CHECK_NEW_VERSION, "NewAndroid"), new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

        long inviteDate = Hawk.get("inviteDate") != null ? (long) Hawk.get("inviteDate") : 0;
        try
        {
          String data = Utils.removeExtraQuotations(new String(responseBody));
          String version = data.toString().split("\\+\\+\\+")[0];
          String changeLog = data.toString().split("\\+\\+\\+")[1];
          if (!version.equals(BuildConfig.VERSION_NAME))
          {
            String[] newVersion = version.split("\\.");
            String[] currentVersion = BuildConfig.VERSION_NAME.split("\\.");

            if (Integer.valueOf(newVersion[0]) > Integer.valueOf(currentVersion[0]))
            {
              newVersionAvailable(version, changeLog);
            } else if (Integer.valueOf(newVersion[0]) == Integer.valueOf(currentVersion[0]))
            {
              if (Integer.valueOf(newVersion[1]) > Integer.valueOf(currentVersion[1]))
              {
                newVersionAvailable(version, changeLog);
              } else if (Integer.valueOf(newVersion[1]) == Integer.valueOf(currentVersion[1]))
              {
                if (Integer.valueOf(newVersion[2]) > Integer.valueOf(currentVersion[2]))
                {
                  newVersionAvailable(version, changeLog);
                } else
                {
                  friendInviter(inviteDate);
                }
              } else
              {
                friendInviter(inviteDate);
              }
            } else
            {
              friendInviter(inviteDate);
            }
          } else
          {
            friendInviter(inviteDate);
          }
        } catch (Exception ex)
        {
        }

      }

      @Override
      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

      }
    });
  }

  private void friendInviter(long invitedTime) {
    final int DAY_MILLI_SEC = 86400000;

    long def = System.currentTimeMillis() - invitedTime;
    if (def > DAY_MILLI_SEC * 5)
    {
      Hawk.put("inviteDate", System.currentTimeMillis());
      final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
      builder.message(R.string.dialog_invite)
        .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
        .title(R.string.invite_friends)
        .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
        .positiveAction(R.string.invite)
        .negativeAction(R.string.dismiss)
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
          builder.dismiss();
          FragmentHandler.replaceFragment(getContext(), fragmentType.CONTACTS, null);

        }

      });
      getActivity().runOnUiThread(new Runnable() {

        @Override
        public void run() {
          builder.show();

        }
      });
    }
  }

  private void newVersionAvailable(final String version, final String changeloge) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        String logs = changeloge.replace("\\r\\n", "<br>");
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
        builder
          .title(R.string.new_version_available)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.download)
          .negativeAction(R.string.dismiss)
          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .setCancelable(true);
        builder.layoutParams(Utils.getScreenWidthPX(getContext()) - Utils.dpToPx(40), ViewGroup.LayoutParams.WRAP_CONTENT);
        builder.setContentView(R.layout.dialog_custom_view);
        TextView txtMessage = (TextView) builder.findViewById(R.id.txtMessage);
        String message = "<b>Version:" + version + "</b>"
          + "<br>"
          + "<br>"
          + logs;
        txtMessage.setText(Html.fromHtml(message));
        builder.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            builder.dismiss();

          }
        });

        builder.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.General.PLAYSTOR_URL));
            startActivity(browserIntent);
          }
        });
        builder.show();
      }
    });
  }

}
