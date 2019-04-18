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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.FollowAndDenyAdapter;
import com.collect_up.c_up.adapters.interfaces.ContactsCallback;
import com.collect_up.c_up.adapters.interfaces.InviteCounter;
import com.collect_up.c_up.adapters.providers.Contacts;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Profile;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

@SuppressLint ("ValidFragment")

public class FragmentFollowAndDeny extends BaseFragment implements InviteCounter, SearchView.OnQueryTextListener, View.OnFocusChangeListener {

  public static boolean isRunning;

  @Bind (R.id.recycler_view)
  public UltimateRecyclerView recyclerView;
  @Bind (R.id.btn_invite)
  LinearLayout btnInvite;
  private FollowAndDenyAdapter mAdapter;
  private ArrayList<Contacts.UnRegisteredContact> _contacts = new ArrayList<>();
  private SearchView mSearchView;
  private View view;

  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
    Utils.hideSoftKeyboard(getActivity(), mSearchView);
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;

    getContacts();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_contacts, menu);
    if (menu.findItem(R.id.action_done) != null)
    {
      menu.findItem(R.id.action_done).setVisible(false);
    }

    SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
    MenuItem searchMenu = menu.findItem(R.id.action_search);
    mSearchView = (SearchView) searchMenu.getActionView();

    // Change the search view hint color using a hack!
    mSearchView.setQueryHint(Html.fromHtml("<font color = #40ffffff>" + getResources().getString(R.string.hint_search) + "</font>"));
    mSearchView.setIconifiedByDefault(false);
    mSearchView.setIconified(false);
    mSearchView.setOnQueryTextFocusChangeListener(this);
    mSearchView.setOnQueryTextListener(this);
    mSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getContext(), FragmentChatContacts.class)));
    mSearchView.clearFocus();
    mSearchView.setFocusable(true);
    mSearchView.requestFocusFromTouch();
  }

  @Override
  public void onDestroy() {
    if (DrawerHelper.getDrawer() != null)
    {

      DrawerHelper.getDrawer().setSelection(-1);
    }
    super.onDestroy();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();

    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_follow_and_deny, container, false);
      ButterKnife.bind(this, view);
      setHasOptionsMenu(true);


      recyclerView.setHasFixedSize(false);
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      recyclerView.setLayoutManager(layoutManager);

      mAdapter = new FollowAndDenyAdapter(getContext(), this);
      btnInvite.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          String sendto = "smsto:";
          Intent sharingIntent;

          for (int i = 0; i < _contacts.size(); i++)
          {
            sendto += _contacts.get(i).getPhoneNumber() + ";";
          }
          String textMessage = String.format(getString(R.string.invite_from_sms), Logged.Models
            .getUserProfile()
            .getName(), Constants.General.PLAYSTOR_URL);
          if (_contacts.size() == 0)
          {
            sharingIntent = new Intent(Intent.ACTION_SEND);

            sharingIntent.setType("text/plain");

            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Join to Collect-Up");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textMessage);
          } else
          {
            sharingIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(sendto.substring(0, sendto.length() - 1)));
            sharingIntent.putExtra("sms_body", textMessage);

          }


          if (_contacts.size() == 0)
          {
            startActivity(Intent.createChooser(sharingIntent, "share"));
          } else
          {
            startActivity(sharingIntent);
          }

        }
      });


      recyclerView.addItemDividerDecoration(getContext());


      recyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          mAdapter.unSelectAllUnrgisterdContact();

          getContacts();
        }
      });

      recyclerView.setAdapter(mAdapter);

      return view;
    } else
    {
      return view;
    }
  }


  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
    mAdapter.unSelectAllUnrgisterdContact();

  }

  private void getContacts() {
    _contacts.clear();
    ((TextView) (btnInvite.getChildAt(2))).setText("");
    new Contacts().getAllContacts(getContext(), new ContactsCallback() {
      @Override
      public void onContactsReceived(List<Profile> profiles,
                                     List<Contacts.UnRegisteredContact> contacts) {
        mAdapter.addRegisteredProfiles(profiles);
        mAdapter.addUnregisteredProfiles(contacts);
      }
    }, true);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case android.R.id.home:
        break;
      case R.id.action_select_all:
        if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.action_unselect_all)))
        {
          item.setTitle(R.string.action_select_all);
          mAdapter.unSelectAllUnrgisterdContact();
          getContacts();
          ((TextView) (btnInvite.getChildAt(2))).setText("");


        } else
        {
          item.setTitle(R.string.action_unselect_all);
          mAdapter.selectAllUnrgisterdContact();
          new Contacts().getAllContacts(getContext(), new ContactsCallback() {
            @Override
            public void onContactsReceived(List<Profile> profiles,
                                           List<Contacts.UnRegisteredContact> contacts) {

              _contacts = (ArrayList<Contacts.UnRegisteredContact>) contacts;
              ((TextView) (btnInvite.getChildAt(2))).setText("(" + contacts.size() + ")");
            }
          }, true);
          mAdapter.notifyDataSetChanged();
        }

        break;
    }
    return true;
  }


  @Override
  public void countChecked(ArrayList<Contacts.UnRegisteredContact> contacts) {
    _contacts = contacts;
    if (contacts.size() == 0)
    {
      ((TextView) (btnInvite.getChildAt(2))).setText("");
    } else

    {
      ((TextView) (btnInvite.getChildAt(2))).setText("(" + contacts.size() + ")");
    }
  }

  @Override
  public boolean onQueryTextSubmit(String query) {


    return true;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    if (newText.equalsIgnoreCase(""))
    {
      getContacts();
    } else if (mAdapter != null)
    {
      mAdapter.getFilter().filter(newText);
    }
    return false;
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus)
    {
      Utils.hideSoftKeyboard(getActivity(), mSearchView);
    }
  }
}
