/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.ContactAdapter;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.services.Location;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
@SuppressLint ("ValidFragment")

public class FragmentFindPeople extends BaseFragment implements
  SearchView.OnQueryTextListener,
  MenuItemCompat.OnActionExpandListener,
  View.OnFocusChangeListener {

  public static boolean isRunning;
  @Bind (R.id.recycler_view_likes)
  UltimateRecyclerView mRecyclerView;
  @Bind (R.id.progress_bar_loading)
  ProgressView mProgressBar;
  /*
    @Bind(R.id.toolbar)
    Toolbar mToolbar;*/
  private ContactAdapter mAdapter;
  private List<Profile> adapterDataSet = new ArrayList<>();
  private int mCurrentPage;
  private SearchView mSearchView;
  private Menu mMenu;
  private MenuItem searchMenu;
  private View view;

  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;

    getActivity().startService(new Intent(getContext(), Location.class));
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_find_people, menu);
    mMenu = menu;

    // Associate searchable configuration with the SearchView
    SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
    searchMenu = menu.findItem(R.id.action_search);
    MenuItemCompat.setOnActionExpandListener(searchMenu, this);
    mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
    mSearchView.setIconifiedByDefault(false);
    mSearchView.setIconified(false);
    mSearchView.setOnQueryTextFocusChangeListener(this);
    mSearchView.setOnQueryTextListener(this);
    mSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getContext(), FragmentFindPeople.class)));
    mSearchView.clearFocus();

  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();

    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_post_likes, container, false);
      ButterKnife.bind(this, view);
      mProgressBar.setVisibility(View.GONE);

      setHasOptionsMenu(true);
      LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
      mRecyclerView.setLayoutManager(linearLayoutManager);
      mRecyclerView.setHasFixedSize(true);

      loadFirstPage("");

      mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          loadFirstPage(mSearchView.getQuery() != null ? mSearchView.getQuery().toString() : "");
        }
      });

      mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMorePages(mSearchView.getQuery() != null ? mSearchView.getQuery().toString() : "");
        }
      });


      return view;
    } else
    {
      return view;
    }
  }


  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.find_people);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();

  }

  @Override
  public void onDestroy() {
    if (DrawerHelper.getDrawer() != null)
    {

      DrawerHelper.getDrawer().setSelection(-1);
    }
    super.onDestroy();

  }

  private void notifyAdapter() {
    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
  }


  @SuppressWarnings ("unchecked")
  public void loadFirstPage(final String query) {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    Pagination.findPeople(mCurrentPage, query, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        // Reset items to make the pull to refresh right
        adapterDataSet.clear();

        notifyAdapter();

        adapterDataSet.addAll((List<Profile>) pageList);

        mAdapter = new ContactAdapter(FragmentFindPeople.this, adapterDataSet);

        mRecyclerView.setAdapter(mAdapter);
        notifyAdapter();

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
        mRecyclerView.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstPage(query);
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMorePages(final String query) {
    Pagination.findPeople(mCurrentPage, query, getContext(), new IPaginationCallback() {
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
            loadMorePages(query);
          }
        }, getActivity());
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case android.R.id.home:
        break;
    }

    return false;
  }


  @Override
  public boolean onQueryTextSubmit(String query) {

    loadFirstPage(query.trim());

    return true;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
  /*  if (TextUtils.isEmpty(newText.trim())) {
      onQueryTextSubmit("");
    }*/
    return true;
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus)
    {
      searchMenu.collapseActionView();
      //mSearchView.setQuery("", false);
    }
  }

  @Override
  public boolean onMenuItemActionExpand(MenuItem item) {
    return true;
  }

  @Override
  public boolean onMenuItemActionCollapse(MenuItem item) {
    return true;
  }
}
