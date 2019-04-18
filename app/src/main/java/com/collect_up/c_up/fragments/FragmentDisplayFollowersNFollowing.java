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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.ContactAdapter;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.Profile;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.mikepenz.materialdrawer.Drawer;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

@SuppressLint ("ValidFragment")

public class FragmentDisplayFollowersNFollowing extends BaseFragment {

  @Bind (R.id.recycler_view_likes)
  UltimateRecyclerView mRecyclerView;
  private ContactAdapter mAdapter;
  private int mCurrentPage;
  private String profileId;
  private List<Profile> adapterDataSet = new ArrayList<>();
  private Drawer mDrawer;
  private View view;
  @Bind (R.id.progress_bar_loading)
  ProgressView mProgressBar;

  public FragmentDisplayFollowersNFollowing(Bundle args) {
    this.args = args;
  }

  public FragmentDisplayFollowersNFollowing() {
  }

  private Bundle args;


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.RequestCodes.UPDATE_UI.ordinal())
    {
      if (data != null && data.getParcelableExtra("profile") != null)
      {
        mAdapter.update((Profile) data.getParcelableExtra("profile"), ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle().toString());
      }
    }
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstPersonFollowingPage() {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    Pagination.getPersonFollowing(mCurrentPage, profileId, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        mProgressBar.setVisibility(View.GONE);

        // Reset items to make the pull to refresh right
        adapterDataSet.clear();

        adapterDataSet.addAll((List<Profile>) pageList);

        mAdapter = new ContactAdapter(FragmentDisplayFollowersNFollowing.this, adapterDataSet);

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
            loadFirstPersonFollowingPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMorePersonFollowing() {
    Pagination.getPersonFollowing(mCurrentPage, profileId, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Profile post : (List<Profile>) pageList)
        {
          mAdapter.insertInternal(adapterDataSet, post, adapterDataSet.size());
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
            loadMorePersonFollowing();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstPersonFollowersPage() {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    Pagination.getPersonFollowers(mCurrentPage, profileId, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        mProgressBar.setVisibility(View.GONE);

        // Reset items to make the pull to refresh right
        adapterDataSet.clear();

        adapterDataSet.addAll((List<Profile>) pageList);

        mAdapter = new ContactAdapter(FragmentDisplayFollowersNFollowing.this, adapterDataSet);

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
            loadFirstPersonFollowersPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMorePersonFollowers() {
    Pagination.getPersonFollowers(mCurrentPage, profileId, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Profile post : (List<Profile>) pageList)
        {
          mAdapter.insertInternal(adapterDataSet, post, adapterDataSet.size());
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
            loadMorePersonFollowers();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstShopFollowersPage() {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    Pagination.getShopFollowers(mCurrentPage, profileId, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        mProgressBar.setVisibility(View.GONE);

        // Reset items to make the pull to refresh right
        adapterDataSet.clear();

        adapterDataSet.addAll((List<Profile>) pageList);

        mAdapter = new ContactAdapter(FragmentDisplayFollowersNFollowing.this, adapterDataSet);

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
            loadFirstShopFollowersPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMoreShopFollowers() {
    Pagination.getShopFollowers(mCurrentPage, profileId, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Profile post : (List<Profile>) pageList)
        {
          mAdapter.insertInternal(adapterDataSet, post, adapterDataSet.size());
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
            loadMoreShopFollowers();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstComplexFollowersPage() {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    Pagination.getComplexFollowers(mCurrentPage, profileId, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        mProgressBar.setVisibility(View.GONE);

        // Reset items to make the pull to refresh right
        adapterDataSet.clear();

        adapterDataSet.addAll((List<Profile>) pageList);

        mAdapter = new ContactAdapter(FragmentDisplayFollowersNFollowing.this, adapterDataSet);

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
            loadFirstComplexFollowersPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMoreComplexFollowers() {
    Pagination.getComplexFollowers(mCurrentPage, profileId, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Profile post : (List<Profile>) pageList)
        {
          mAdapter.insertInternal(adapterDataSet, post, adapterDataSet.size());
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
            loadMoreComplexFollowers();
          }
        }, getActivity());
      }
    });
  }

  @Override
  public void onResume() {
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    int flag = args.getInt("flag");
    setHasOptionsMenu(true);
    ButterKnife.bind(this, view);
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null)
    {
      actionBar.show();
      if (flag == 0 || flag == 2 || flag == 3)
      {
        actionBar.setTitle(getString(R.string.followers));
      } else if (flag == 1)
      {
        actionBar.setTitle(getString(R.string.following));
      }
    } super.onResume();

    if (mDrawer != null)
    {
      DrawerHelper.update(mDrawer, getActivity());
      mDrawer.setSelection(-1);
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_post_likes, container, false);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();

      profileId = args.getString("id");
      int flag = args.getInt("flag");
      setHasOptionsMenu(true);
      ButterKnife.bind(this, view);
      ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
      if (actionBar != null)
      {
        actionBar.show();
        if (flag == 0 || flag == 2 || flag == 3)
        {
          actionBar.setTitle(getString(R.string.followers));
        } else if (flag == 1)
        {
          actionBar.setTitle(getString(R.string.following));
        }

      }


      LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
      mRecyclerView.setLayoutManager(linearLayoutManager);
      mRecyclerView.setHasFixedSize(true);
      mRecyclerView.addItemDividerDecoration(getContext());

      if (flag == 0)
      {
        loadFirstPersonFollowersPage();

        mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            loadFirstPersonFollowersPage();
          }
        });

        mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
          @Override
          public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
            loadMorePersonFollowers();
          }
        });
      } else if (flag == 1)
      {
        loadFirstPersonFollowingPage();

        mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            loadFirstPersonFollowingPage();
          }
        });

        mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
          @Override
          public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
            loadMorePersonFollowing();
          }
        });
      } else if (flag == 2)
      {
        loadFirstShopFollowersPage();

        mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            loadFirstShopFollowersPage();
          }
        });

        mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
          @Override
          public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
            loadMoreShopFollowers();
          }
        });
      } else if (flag == 3)
      {
        loadFirstComplexFollowersPage();

        mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            loadFirstComplexFollowersPage();
          }
        });

        mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
          @Override
          public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
            loadMoreComplexFollowers();
          }
        });
      }

      return view;
    } else
    {
      return view;

    }
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case android.R.id.home:
        FragmentHandler.onBackPressed(getContext());
        break;
    }

    return false;
  }

}
