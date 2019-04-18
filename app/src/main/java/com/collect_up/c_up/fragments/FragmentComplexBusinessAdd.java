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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.ComplexShopAddAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Shop;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

@SuppressLint ("ValidFragment")
public class FragmentComplexBusinessAdd extends BaseFragment
  implements SearchView.OnQueryTextListener, ComplexShopAddAdapter.ItemListener,
  MenuItemCompat.OnActionExpandListener,
  View.OnFocusChangeListener {
  private final List<Shop> mShopList = new ArrayList<>();
  ComplexShopAddAdapter mAdapter;
  @Bind (R.id.recycler_view)
  UltimateRecyclerView mRecyclerView;
  @Bind (R.id.progress_bar_loading)
  ProgressView mProgressBar;
  private Complex mComplex;
  private int mCurrentPage = 1;
  private Menu mMenu;
  private SearchView mSearchView;
  private MenuItem searchMenu;
  private View view;

  public FragmentComplexBusinessAdd(Complex mComplex) {
    this.mComplex = mComplex;
  }

  public FragmentComplexBusinessAdd() {
  }


  @SuppressWarnings ("unchecked")
  public void loadFirstPage() {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    Pagination.getShops(mCurrentPage, mComplex.getLat(), mComplex.getLong(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        mProgressBar.setVisibility(View.GONE);

        // Reset items to make the pull to refresh right
        mShopList.clear();

        mShopList.addAll((List<Shop>) pageList);
        mAdapter = new ComplexShopAddAdapter(getContext(), mShopList, mComplex);

        mAdapter.setListener(FragmentComplexBusinessAdd.this);
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
  private void loadMoreData() {
    Pagination.getShops(mCurrentPage, mComplex.getLat(), mComplex.getLong(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Shop post : (List<Shop>) pageList)
        {
          mAdapter.insertInternal(mShopList, post, mShopList.size());
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
            loadMoreData();
          }
        }, getActivity());
      }
    });
  }


  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.add_shop);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_complex_add_business, container, false);
      setHasOptionsMenu(true);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      ButterKnife.bind(this, view);
      mRecyclerView.setHasFixedSize(true);
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      mRecyclerView.setLayoutManager(layoutManager);

      mRecyclerView.addItemDividerDecoration(getContext());

      loadFirstPage();

      mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          loadFirstPage();
        }
      });

      mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMoreData();
        }
      });
      return view;
    } else
    {
      return view;
    }
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_done_discard, menu);
    MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
    Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
    if (menuItemLoaderIcon != null)
    {
      try
      {
        menuItemLoaderIcon.mutate();
        menuItemLoaderIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        menuItemLoader.setIcon(menuItemLoaderIcon);
      } catch (IllegalStateException e)
      {
        Log.i("sepehr", String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
      }
      ((Animatable) menuItemLoader.getIcon()).start();
      menuItemLoader.setVisible(false);
    }
    mMenu = menu;
    SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
    searchMenu = menu.findItem(R.id.action_search);
    MenuItemCompat.setOnActionExpandListener(searchMenu, this);
    mSearchView = (SearchView) searchMenu.getActionView();

    // Change the search view hint color using a hack!
    mSearchView.setQueryHint(Html.fromHtml("<font color = #40ffffff>" + getResources().getString(R.string.hint_search) + "</font>"));

    mSearchView.setIconifiedByDefault(false);
    mSearchView.setIconified(false);
    mSearchView.setOnQueryTextFocusChangeListener(this);
    mSearchView.setOnQueryTextListener(this);
    mSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getContext(), FragmentChatContacts.class)));
    mSearchView.clearFocus();

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case android.R.id.home:
        break;
      case R.id.action_done:
        final List<String> shopIds = mAdapter.getCheckedItems();

        CheckOut checkOut = new CheckOut();
        checkOut.setValue(TextUtils.join(",", shopIds));

        mMenu.findItem(R.id.action_done).setVisible(false);
        mMenu.findItem(R.id.menu_loader).setVisible(true);


        HttpClient.post(getContext(), String.format(Constants.Server.Complex.POST_ADD_SHOPS, mComplex
          .getId()), new Gson().toJson(checkOut, CheckOut.class), "application/json", new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if (shopIds.size() == 0)
            {
              Logged.Models.getUserComplex().setShopsId(mComplex.getShopsId());
            }
            Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());

            FragmentHandler.replaceFragment(getContext(), fragmentType.COMPLEX, mComplex);

          }

          @Override
          public void onFailure(int statusCode,
                                Header[] headers,
                                byte[] responseBody,
                                Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

            mMenu.findItem(R.id.action_done).setVisible(true);
            mMenu.findItem(R.id.menu_loader).setVisible(false);


            Toast.makeText(getContext(), getString(R.string.toast_error_updating_complex), Toast.LENGTH_SHORT)
              .show();
          }
        });
        break;
    }
    return false;
  }

  @Override
  public void onProfilePictureClick(Shop shop) {
    FragmentHandler.replaceFragment(getContext(), fragmentType.BUSINESS, shop);

  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    if (mAdapter != null)
    {
      mAdapter.getFilter().filter(query);
    }

    return true;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    if (mAdapter != null)
    {
      mAdapter.getFilter().filter(newText);
    }

    return true;
  }


  @Override
  public boolean onMenuItemActionExpand(MenuItem item) {
    return true;
  }

  @Override
  public boolean onMenuItemActionCollapse(MenuItem item) {
    return true;
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus)
    {
      searchMenu.collapseActionView();
      mSearchView.setQuery("", false);
    }
  }
}
