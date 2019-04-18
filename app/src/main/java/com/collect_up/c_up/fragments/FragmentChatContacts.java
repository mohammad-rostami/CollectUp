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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.ContactsAdapter;
import com.collect_up.c_up.adapters.MultiSelectAdapter;
import com.collect_up.c_up.adapters.interfaces.ContactsCallback;
import com.collect_up.c_up.adapters.providers.Contacts;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.Profile;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

@SuppressLint ("ValidFragment")
public class FragmentChatContacts extends BaseFragment implements View.OnFocusChangeListener, SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {

  @Bind (R.id.recycler_view)
  public UltimateRecyclerView recyclerView;
  private ContactsAdapter mAdapter;
  private SearchView mSearchView;
  private MultiSelectAdapter mMultiSelectAdapter;
  private View view;
  Intent intent;

  public FragmentChatContacts(Intent intent) {
    this.intent = intent;
  }

  public FragmentChatContacts() {
  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.contacts);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      setHasOptionsMenu(true);
      view = inflater.inflate(R.layout.fragment_contacts, container, false);
      ButterKnife.bind(this, view);
      boolean isMultiSelect = false;
      boolean joinRoom = false;
      boolean showInvites = true;
      String title = getString(R.string.contacts);
      CompactChat chat = null;
      if (intent != null)
      {
        Bundle bundle = intent.getExtras();
        isMultiSelect = bundle.getBoolean("multi_select", false);
        joinRoom = bundle.getBoolean("join_room", false);
        chat = bundle.getParcelable("chat");
        showInvites = bundle.getBoolean("show_invites", true);

        if (!Utils.isNullOrEmpty(bundle.getString("title")))
        {
          title = bundle.getString("title");
        }
      }


      ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
      if (actionBar != null)
      {
        actionBar.show();
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
      }


      recyclerView.setHasFixedSize(false);
      recyclerView.addItemDividerDecoration(getContext());
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      recyclerView.setLayoutManager(layoutManager);

      if (!isMultiSelect)
      {
        mAdapter = new ContactsAdapter(getContext(), joinRoom, chat);
        getContacts(showInvites);
        recyclerView.setAdapter(mAdapter);
      } else
      {
        mMultiSelectAdapter = new MultiSelectAdapter(getContext());
        getContactsForMultiSelectAdapter();
        recyclerView.setAdapter(mMultiSelectAdapter);
      }

      final boolean finalIsMultiSelect = isMultiSelect;
      final boolean finalShowInvites = showInvites;
      recyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          if (finalIsMultiSelect)
          {
            getContactsForMultiSelectAdapter();
          } else
          {
            getContacts(finalShowInvites);
          }
        }
      });


      return view;
    } else
    {
      return view;
    }
  }


  private void getContacts(final boolean showInvites) {
    new Contacts().getAllContacts(getContext(), new ContactsCallback() {
      @Override
      public void onContactsReceived(List<Profile> profiles,
                                     List<Contacts.UnRegisteredContact> contacts) {
        mAdapter.addRegisteredProfiles(profiles);
        if (showInvites)
        {
          mAdapter.addUnregisteredProfiles(contacts);
        }
      }
    }, showInvites);
  }

  private void getContactsForMultiSelectAdapter() {
    new Contacts().getAllContacts(getContext(), new ContactsCallback() {
      @Override
      public void onContactsReceived(List<Profile> profiles,
                                     List<Contacts.UnRegisteredContact> contacts) {
        mMultiSelectAdapter.addRegisteredProfiles(profiles);
      }
    }, false);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case android.R.id.home:
        break;
      case R.id.action_done:
        ArrayList<Profile> members = new ArrayList<>(mMultiSelectAdapter.getCheckedProfileItems());
        if (members.size() >= 2)
        {
          FragmentHandler.replaceFragment(getContext(), fragmentType.GROUPCHAT_INIT, new Intent(getContext(), FragmentGroupChatInit.class).putParcelableArrayListExtra("members", members));
        } else
        {
          Toast.makeText(getContext(), R.string.toast_error_minimum_group_member, Toast.LENGTH_SHORT).show();
        }
        break;
    }
    return false;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    boolean isMultiSelect = false;
    Bundle bundle = intent.getExtras();

    if (bundle != null)
    {
      isMultiSelect = bundle.getBoolean("multi_select");
    }
    inflater.inflate(R.menu.menu_contacts, menu);
    if (!isMultiSelect && menu.findItem(R.id.action_done) != null)
    {
      menu.findItem(R.id.action_done).setVisible(false);
    }
    menu.findItem(R.id.action_select_all).setVisible(false);

    // Associate searchable configuration with the SearchView
    SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
    MenuItem searchMenu = menu.findItem(R.id.action_search);
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
  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus)
    {
      mSearchView.clearFocus();
    }
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    if (mAdapter != null)
    {
      mAdapter.getFilter().filter(query);
    } else if (mMultiSelectAdapter != null)
    {
      mMultiSelectAdapter.getFilter().filter(query);
    }

    return true;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    if (mAdapter != null)
    {
      mAdapter.getFilter().filter(newText);
    } else if (mMultiSelectAdapter != null)
    {
      mMultiSelectAdapter.getFilter().filter(newText);
    }

    return true;
  }

  @Override
  public boolean onMenuItemActionExpand(MenuItem item) {
    // Must return true because of pressing back button on the search view.
    return true;
  }

  @Override
  public boolean onMenuItemActionCollapse(MenuItem item) {
    // Must return true because of pressing back button on the search view.
    return true;
  }

}
