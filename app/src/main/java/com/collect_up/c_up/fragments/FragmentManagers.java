package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by collect-up3 on 12/11/2016.
 */
@SuppressLint ("ValidFragment")
public class FragmentManagers extends BaseFragment {
  public final static int MODE_SHOP = 1;
  public final static int MODE_COMPLEX = 2;
  @Bind (R.id.recycler_view_likes)
  UltimateRecyclerView mRecyclerView;
  private ContactAdapter mAdapter;
  private Object object;
  private List<Profile> adapterDataSet = new ArrayList<>();
  private int mCurrentPage;
  private View view;
  @Bind (R.id.progress_bar_loading)
  ProgressView mProgressBar;
  int mode;


  public FragmentManagers(Object object) {
    this.object = object;
    if (object instanceof Shop)

    {
      mode = MODE_SHOP;
    } else
    {
      mode = MODE_COMPLEX;
    }
  }

  public FragmentManagers() {
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.RequestCodes.UPDATE_UI.ordinal())
    {
      if (data.getParcelableExtra("profile") != null)
      {
        mAdapter.update((Profile) data.getParcelableExtra("profile"), null);
      }

    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
    container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_post_likes, container, false);
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
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMorePages();
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
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.managers);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstPage() {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    if (mode == MODE_SHOP)
    {
      Pagination.getShopManagers(getContext(), ((Shop) object).getId(), mCurrentPage, new IPaginationCallback() {
        @Override
        public <T> void onPageReceived(List<T> pageList) {
          mProgressBar.setVisibility(View.GONE);

          // Reset items to make the pull to refresh right
          adapterDataSet.clear();

          adapterDataSet.addAll((List<Profile>) pageList);

          mAdapter = new ContactAdapter(FragmentManagers.this, adapterDataSet);

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
    } else
    {
      Pagination.getComplexManagers(getContext(), ((Complex) object).getId(), mCurrentPage, new IPaginationCallback() {
        @Override
        public <T> void onPageReceived(List<T> pageList) {
          mProgressBar.setVisibility(View.GONE);

          // Reset items to make the pull to refresh right
          adapterDataSet.clear();

          adapterDataSet.addAll((List<Profile>) pageList);

          mAdapter = new ContactAdapter(FragmentManagers.this, adapterDataSet);

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
  }

  @SuppressWarnings ("unchecked")
  private void loadMorePages() {
    if (mode == MODE_SHOP)

    {
      Pagination.getShopManagers(getContext(), ((Shop) object).getId(), mCurrentPage, new IPaginationCallback() {
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
              loadMorePages();
            }
          }, getActivity());
        }
      });
    } else

    {
    }

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

  public int getMode() {
    return mode;
  }

  public String getOwnId() {

    if (mode == MODE_SHOP)
    {
      return ((Shop) object).getId();

    } else
    {
      return ((Complex) object).getId();

    }
  }
}
