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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.NotificationsAdapter;
import com.collect_up.c_up.helpers.AppNotificationUtils;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.Notification;
import com.collect_up.c_up.model.Profile;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

@SuppressLint ("ValidFragment")


public class FragmentNotifications extends BaseFragment implements View.OnClickListener, NotificationsAdapter.ItemsClickListener {

  public static boolean isRunning;
  @Bind (R.id.recycler_view)
  UltimateRecyclerView mRecyclerView;
  @Bind (R.id.progress_bar_loading)
  ProgressView mProgressBar;
  @Bind (R.id.btn_request)
  LinearLayout btnRequests;
  @Bind (R.id.txtReqCounter)
  TextView txtReqCounter;
  boolean isFirstTime;
  private int mCurrentPage;
  private final List<Notification> mDataSet = new ArrayList<>();
  private NotificationsAdapter mAdapter;
  private int mUnseenNotifications = -1;
  private int mUnseenRequestNotifications = -1;
  private static final String KEY_POSITION = "position";
  private View view;
  private boolean isViewShown = false;

  public FragmentNotifications(Intent intent) {
    try
    {
      this.mUnseenNotifications = intent.getIntExtra("unseen_notifications", -1);
      this.mUnseenRequestNotifications = intent.getIntExtra("unseen_requests", -1);
    } catch (Exception ex)
    {
    }
  }

  public FragmentNotifications() {
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    try
    {
      if (isVisibleToUser)
      {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        isViewShown = true;

      }
    } catch (Exception ex)
    {
    }
  }


  public void getRequstCount() {

    HttpClient.get(String.format(Constants.Server.Notification.REQUEST_COUNT), new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        mUnseenRequestNotifications = Integer.parseInt(GsonParser.getObjectFromGson(responseBody, CheckOut.class).getValue());
        AppNotificationUtils.mUnSeenRequestNotifications = mUnseenRequestNotifications;
        requestViewVisibility(mUnseenRequestNotifications);

      }


      @Override
      public void onFailure(int statusCode,
                            Header[] headers,
                            byte[] responseBody,
                            Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

      }
    });
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstPage() {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    Pagination.getNotifications(mCurrentPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        mProgressBar.setVisibility(View.GONE);
        // Reset items to make the pull to refresh right
        mDataSet.clear();

        mDataSet.addAll((List<Notification>) pageList);

        mAdapter = new NotificationsAdapter(getContext(), mDataSet, mUnseenNotifications);
        mAdapter.setItemsClickListener(FragmentNotifications.this);

        mRecyclerView.setAdapter(mAdapter);

        mCurrentPage++;

        if (pageList.size() != Pagination.PAGE_IN_REQUEST)
        {
          if (mRecyclerView.isLoadMoreEnabled())
          {
            mRecyclerView.disableLoadmore();
          }
        } else
        {
          if (!mRecyclerView.isLoadMoreEnabled())
          {
            mRecyclerView.enableLoadmore();
          }
        }
      }

      @Override
      public void onFailure() {
        mProgressBar.setVisibility(View.GONE);

        mRecyclerView.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstPage();
          }
        }, getActivity());
      }
    });
  }


  @SuppressWarnings ("unchecked")
  private void loadMorePages() {
    Pagination.getNotifications(mCurrentPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Notification post : (List<Notification>) pageList)
        {
          mAdapter.insertInternal(mDataSet, post, mDataSet.size());
        }
        mCurrentPage++;

        if (pageList.size() != Pagination.PAGE_IN_REQUEST)
        {
          if (mRecyclerView.isLoadMoreEnabled())
          {
            mRecyclerView.disableLoadmore();
          }
        } else
        {
          if (!mRecyclerView.isLoadMoreEnabled())
          {
            mRecyclerView.reenableLoadmore();
          }
        }
      }

      @Override
      public void onFailure() {
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadMorePages();
          }
        }, getActivity());
      }
    });
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_notifications, container, false);
      ButterKnife.bind(this, view);

      isFirstTime = true;
      btnRequests.setOnClickListener(this);
      LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
      mRecyclerView.setLayoutManager(linearLayoutManager);
      mRecyclerView.setHasFixedSize(true);
      getRequstCount();
      loadFirstPage();

      mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          loadFirstPage();
          getRequstCount();

        }
      });

      mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, int maxLastVisiblePosition) {
          loadMorePages();
        }
      });

      mRecyclerView.addItemDividerDecoration(getContext());

      return view;
    } else
    {
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      return view;
    }
  }

  @Override
  public void onStop() {
    isViewShown = false;
    isRunning = false;
    super.onStop();
  }

  @Override
  public void onStart() {
    isRunning = true;
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();

    AppNotificationUtils.mUnSeenNotifications = 0;

    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_notifications);
    ((ActivityHome) getActivity()).changeButtonBackgroud(2);

  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.btn_request:
        FragmentHandler.replaceFragment(getContext(), fragmentType.REQUESTS, null);
    }
  }

  @Override
  public void onProfilePictureClick(Profile profile) {
    FragmentHandler.replaceFragment(getContext(), fragmentType.PROFILE, profile);
  }


  private void requestViewVisibility(int notificationCount) {
    if (notificationCount == 0)
    {
      btnRequests.setVisibility(View.GONE);
    } else
    {
      if (Integer.parseInt(mUnseenRequestNotifications + "") > 99)
      {
        txtReqCounter.setText("+99");
      } else
      {
        txtReqCounter.setText(mUnseenRequestNotifications + "");
      }
      btnRequests.setVisibility(View.VISIBLE);
    }
  }

}
