/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.BlockedListAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.view.BasicGridLayoutManager;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.divideritemdecoration.HorizontalDividerItemDecoration;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

public class FragmentBlockedList extends BaseFragment implements BlockedListAdapter.OnViewClickListener {

  @Bind (R.id.recycler_view)
  UltimateRecyclerView mRecyclerView;
  @Bind (R.id.progress_bar_loading)
  ProgressView loading;
  private Realm mRealm;
  private BlockedListAdapter mAdapter;
  private List<Profile> mBlockedProfiles = new ArrayList<>();
  private int mCurrentPage;
  private BasicGridLayoutManager mLayoutManager;
  private View view;

  @Override
  public void onResume() {
    super.onResume();

    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_blocked_list);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);
    ((ActivityHome) getActivity()).hideButtonBar(true);

  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_pref_blocked_list, container, false);
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      setHasOptionsMenu(true);

      mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

      mRecyclerView.setHasFixedSize(true);
      mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMorePages();
        }
      });
      mRealm.close();
      loadFirstPage();
      return view;
    } else
    {
      return view;
    }
  }


  private void notifyProfilesAdapter() {
    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
  }

  private void showBlockUsers(List<Profile> pageList) {
    loading.setVisibility(View.GONE);
    mBlockedProfiles.clear();

    notifyProfilesAdapter();

    mBlockedProfiles.addAll((List<Profile>) pageList);

    mAdapter = new BlockedListAdapter(getContext(), mBlockedProfiles, BlockedListAdapter.MODE_UNBLOCK);
    mLayoutManager = new BasicGridLayoutManager(getContext(), 1, mAdapter);
    mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).color(getResources()
      .getColor(R.color.chat_subtitle))
      .build());
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter.setOnViewClickListener(this);
    mRecyclerView.setAdapter(mAdapter);
    loading.setVisibility(View.GONE);

    mCurrentPage++;

    notifyProfilesAdapter();

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

  private void setEmtyView()

  {
    View view = mRecyclerView.getEmptyView();
    if (view != null)
    {
      ViewGroup viewGroup = ((ViewGroup) view.getParent());
      if (viewGroup != null)
      {
        viewGroup.removeView(view);
        viewGroup.addView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty_list_view, null));
      }
    } else
    {
      mRecyclerView.setEmptyView(R.layout.empty_list_view);
    }
  }

  public void loadFirstPage() {
    loading.setVisibility(View.VISIBLE);

    mCurrentPage = 1;
    Pagination.getBlockedList(mCurrentPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        if (pageList.size() == 0)
        {
          setEmtyView();
        }
        showBlockUsers((List<Profile>) pageList);

      }

      @Override
      public void onFailure() {
        mRecyclerView.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstPage();
            loading.setVisibility(View.GONE);

          }
        }, getActivity());
      }
    });
  }

  private void loadMorePages() {
    Pagination.getBlockedList(mCurrentPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Profile post : (List<Profile>) pageList)
        {
          mAdapter.insertInternal(mBlockedProfiles, post, mBlockedProfiles.size());
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


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch (item.getItemId())
    {
      case android.R.id.home:

        break;
    }

    return true;
  }

  @Override
  public void onClick(final Profile profile, int viewId, final int pos) {
    switch (viewId)
    {
      case R.id.image_view_picture:
        FragmentHandler.replaceFragment(getContext(), fragmentType.PROFILE, profile);


        break;
      case R.id.button_following:
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
        builder.message(R.string.sure_to_unblock_contact)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .title(R.string.unblock_contact_title)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.unblock)
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
            HttpClient.get(String.format(Constants.Server.Profile.GET_UNBLOCK, profile.getId()), new AsyncHttpResponser(getContext()) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                builder.dismiss();
                removeFromAdapterDataSource(profile.getId());
                mAdapter.mProfiles = mBlockedProfiles;
                mAdapter.notifyItemRemoved(pos);
              }

              @Override
              public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                Toast.makeText(getContext(), R.string.toast_error_unblocking, Toast.LENGTH_SHORT).show();

              }
            });
          }
        });

        builder.show();
        break;
      default:
    }

  }

  private void removeFromAdapterDataSource(String memberId) {
    for (Profile blockedProfiles : mBlockedProfiles)
    {
      if (blockedProfiles.getId().equals(memberId))
      {
        mBlockedProfiles.remove(blockedProfiles);
        break;
      }
    }

    // return deniedProfiles;
  }
}
