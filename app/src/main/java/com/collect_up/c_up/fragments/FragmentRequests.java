package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.RequestAdapter;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.Notification;
import com.collect_up.c_up.model.Profile;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.mikepenz.materialdrawer.Drawer;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by collect-up3 on 5/29/2016.
 */
@SuppressLint ("ValidFragment")

public class FragmentRequests extends BaseFragment implements RequestAdapter.ItemsClickListener {
  public static boolean isRunning;
  @Bind (R.id.recycler_view)
  UltimateRecyclerView mRecyclerView;
  @Bind (R.id.progress_bar_loading)
  ProgressView mProgressBar;
  boolean isFirstTime;
  private int mCurrentPage;
  private final List<Notification> mDataSet = new ArrayList<>();
  private RequestAdapter mAdapter;
  private int mUnseenNotifications = -1;
  private Drawer mDrawer;
  private View view;


  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstPage() {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    Pagination.getNotificationsRequests(mCurrentPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        mProgressBar.setVisibility(View.GONE);
        // Reset items to make the pull to refresh right


        mDataSet.clear();

        mDataSet.addAll((List<Notification>) pageList);

        mAdapter = new RequestAdapter(getContext(), mDataSet, mUnseenNotifications);
        mAdapter.setItemsClickListener(FragmentRequests.this);

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setRefreshing(false);

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

  @Override
  public void onResume() {
    super.onResume();
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.requests);
    ((ActivityHome) getActivity()).changeButtonBackgroud(2);

    if (mDrawer != null)
    {
      DrawerHelper.update(mDrawer, getActivity());
      mDrawer.setSelection(-1);
    }
  }


  @SuppressWarnings ("unchecked")
  private void loadMorePages() {
    Pagination.getNotificationsRequests(mCurrentPage, getContext(), new IPaginationCallback() {
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
      view = inflater.inflate(R.layout.fragment_requests, container, false);
      setHasOptionsMenu(true);
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
      mRecyclerView.setLayoutManager(linearLayoutManager);
      mRecyclerView.setHasFixedSize(true);

      loadFirstPage();

      mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          loadFirstPage();
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
      return view;
    }
  }


  @Override
  public void onProfilePictureClick(Profile profile) {
    FragmentHandler.replaceFragment(getContext(), fragmentType.PROFILE, profile);
  }

  @Override
  public void onPause() {
    super.onPause();
    isRunning = false;
  }

}