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
import com.collect_up.c_up.adapters.ManagerAddAdapter;
import com.collect_up.c_up.adapters.interfaces.ContactsCallback;
import com.collect_up.c_up.adapters.providers.Contacts;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.widget.ProgressView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

@SuppressLint ("ValidFragment")
public class FragmentManagerAdd extends BaseFragment
  implements SearchView.OnQueryTextListener,
  ManagerAddAdapter.ItemListener,
  MenuItemCompat.OnActionExpandListener,
  View.OnFocusChangeListener {

  @Bind (R.id.recycler_view)
  public UltimateRecyclerView recyclerView;
  private ManagerAddAdapter mAdapter;
  private Shop mShop;
  private Complex mComplex;
  private Menu mMenu;
  private MenuItem searchMenu;
  private SearchView mSearchView;
  private View view;
  @Bind (R.id.progress_bar_loading)
  ProgressView mProgressBar;

  public FragmentManagerAdd(Object data) {
    if (data instanceof Shop)
    {
      this.mShop = (Shop) data;

    } else
    {
      this.mComplex = (Complex) data;
    }
  }

  public FragmentManagerAdd() {
  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_add_manager, container, false);
      setHasOptionsMenu(true);
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      recyclerView.setHasFixedSize(true);
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      recyclerView.setLayoutManager(layoutManager);

      mAdapter = new ManagerAddAdapter(mShop != null ? mShop : mComplex);
      mAdapter.setListener(this);

      recyclerView.setSaveEnabled(true);
      recyclerView.addItemDividerDecoration(getContext());

      new Contacts().getAllContacts(getContext(), new ContactsCallback() {
        @Override
        public void onContactsReceived(List<Profile> profiles,
                                       List<Contacts.UnRegisteredContact> contacts) {
          mProgressBar.setVisibility(View.GONE);

          mAdapter.addManagers(profiles);
        }
      }, false);

      recyclerView.setAdapter(mAdapter);
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
        Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());
        break;
      case R.id.action_done:
        final List<String> managersId = mAdapter.getCheckedItems();

        CheckOut checkOut = new CheckOut();
        checkOut.setValue(TextUtils.join(",", managersId));

        mMenu.findItem(R.id.action_done).setVisible(false);
        mMenu.findItem(R.id.menu_loader).setVisible(true);

        HttpClient.post(getContext(), String.format(mShop != null ? Constants.Server.Shop.POST_ADD_MANAGERS : Constants.Server.Complex.POST_ADD_MANAGERS, mShop != null ? mShop.getId() : mComplex
          .getId()), new Gson().toJson(checkOut, CheckOut.class), "application/json", new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            mProgressBar.setVisibility(View.GONE);

            Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());

            if (mShop != null)
            {
              mShop.setManagersId(managersId);

              Logged.Models.setUserShop(mShop);
              FragmentHandler.replaceFragment(getContext(), fragmentType.BUSINESS, mShop);

            } else
            {
              mComplex.setManagersId(managersId);

              Logged.Models.setUserComplex(mComplex);
              FragmentHandler.replaceFragment(getContext(), fragmentType.COMPLEX, mComplex);

            }


          }

          @Override
          public void onFailure(int statusCode,
                                Header[] headers,
                                byte[] responseBody,
                                Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);
            mProgressBar.setVisibility(View.GONE);

            mMenu.findItem(R.id.action_done).setVisible(true);
            mMenu.findItem(R.id.menu_loader).setVisible(false);

            Toast.makeText(getContext(), getString(R.string.toast_error_updating_shop), Toast.LENGTH_SHORT)
              .show();
          }
        });
        break;
    }

    return false;
  }

  @Override
  public void onProfilePictureClick(Profile profile) {
    FragmentHandler.replaceFragment(getContext(), fragmentType.PROFILE, profile);
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
