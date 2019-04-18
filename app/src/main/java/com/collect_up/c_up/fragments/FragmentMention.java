package com.collect_up.c_up.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.MentionAdapter;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.listeners.OnItemClickListener;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by collect-up3 on 5/9/2016.
 */
public class FragmentMention extends Fragment {

  private UltimateRecyclerView mRecyclerView;
  private int mCurrentPage;
  private MentionAdapter mAdapter;
  private List<Profile> adapterDataSetProfile = new ArrayList<>();
  private List<Shop> adapterDataSetShop = new ArrayList<>();
  private List<CheckOut> adapterDataSetHashtag = new ArrayList<>();


  private View view;
  private String mQuery;
  private String mPostId = "0";
  private Pagination.MentionMode mMentionMode;

  public FragmentMention() {
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    view = inflater.inflate(R.layout.fragment_mention, container, false);
    view.setBackgroundColor(getResources().getColor(R.color.suggestion_background));
    view.setVisibility(View.GONE);
    mRecyclerView = (UltimateRecyclerView) view.findViewById(R.id.recycler_view);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
    mRecyclerView.setLayoutManager(linearLayoutManager);
    mRecyclerView.setHasFixedSize(false);
    mRecyclerView.enableDefaultSwipeRefresh(false);
    int padding = getResources().getDimensionPixelSize(R.dimen.list_padding);
    mRecyclerView.setPadding(padding, 0, padding, 0);

    mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
      @Override
      public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
        loadMorePages(mQuery, mPostId, mMentionMode);
      }
    });
    return view;
  }

  private void notifyAdapter() {
    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
  }


  public void loadFirstHashtagPage(final String query) {
    mCurrentPage = 1;
    mQuery = query;
    mMentionMode = Pagination.MentionMode.HASHTAG;

    Pagination.getHashTags(mCurrentPage, query, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {


        // Reset items to make the pull to refresh right
        if (pageList.size() <= 0)
        {
          ((FragmentMentions) getParentFragment()).setVisibilityMentionFragment(false);

        } else
        {
          view.setVisibility(View.VISIBLE);
          ((FragmentMentions) getParentFragment()).setVisibilityMentionFragment(true);
          adapterDataSetHashtag.clear();
          notifyAdapter();

          adapterDataSetHashtag.addAll((List<CheckOut>) pageList);

          mAdapter = new MentionAdapter(FragmentMention.this, adapterDataSetHashtag, new OnItemClickListener() {
            @Override
            public void onClick(Profile profile) {

              ((FragmentMentions) getParentFragment()).setProfileToMention(profile);

            }

            @Override
            public void onClick(Shop shop) {
              ((FragmentMentions) getParentFragment()).setShopToMention(shop);
            }

            @Override
            public void onClick(String hashTag) {
              ((FragmentMentions) getParentFragment()).setHastag(hashTag);

            }
          });

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
      }

      @Override
      public void onFailure() {
        mRecyclerView.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstHashtagPage(query);
          }
        }, getActivity());
      }
    });
  }

  public void loadFirstMentionPage(final String query, final String postId, final Pagination.MentionMode mode) {
    // reset current page to make the pull to refresh right
    mCurrentPage = 1;
    mQuery = query.replace("\n", "").replace("\r", "").replace("#", "");
    mPostId = postId;
    mMentionMode = mode;
    Pagination.getMentions(mCurrentPage, mQuery, postId, getContext(), mode, new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {


        // Reset items to make the pull to refresh right
        if (pageList.size() <= 0)
        {
          ((FragmentMentions) getParentFragment()).setVisibilityMentionFragment(false);
        } else
        {
          view.setVisibility(View.VISIBLE);
          ((FragmentMentions) getParentFragment()).setVisibilityMentionFragment(true);
          switch (mode)
          {
            case BUSINESS:
              adapterDataSetShop.clear();
              notifyAdapter();

              adapterDataSetShop.addAll((List<Shop>) pageList);

              mAdapter = new MentionAdapter(FragmentMention.this, Pagination.MentionMode.BUSINESS, adapterDataSetShop, new OnItemClickListener() {
                @Override
                public void onClick(Profile profile) {

                  ((FragmentMentions) getParentFragment()).setProfileToMention(profile);

                }

                @Override
                public void onClick(Shop shop) {
                  ((FragmentMentions) getParentFragment()).setShopToMention(shop);
                }

                @Override
                public void onClick(String hashTag) {
                  ((FragmentMentions) getParentFragment()).setHastag(hashTag);

                }
              });
              break;
            case USER:
              adapterDataSetProfile.clear();
              notifyAdapter();

              adapterDataSetProfile.addAll((List<Profile>) pageList);

              mAdapter = new MentionAdapter(FragmentMention.this, adapterDataSetProfile, Pagination.MentionMode.USER, new OnItemClickListener() {
                @Override
                public void onClick(Profile profile) {
                  ((FragmentMentions) getParentFragment()).setProfileToMention(profile);
                }

                @Override
                public void onClick(Shop shop) {
                  ((FragmentMentions) getParentFragment()).setShopToMention(shop);

                }

                @Override
                public void onClick(String hashTag) {
                  ((FragmentMentions) getParentFragment()).setHastag(hashTag);

                }
              });

              break;
          }

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
      }

      @Override
      public void onFailure() {
        mRecyclerView.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstMentionPage(query, postId, mode);
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMorePages(final String query, final String postId, final Pagination.MentionMode mode) {
    switch (mMentionMode)
    {
      case BUSINESS:
      case USER:
        Pagination.getMentions(mCurrentPage, query, postId, getContext(), mode, new IPaginationCallback() {
          @Override
          public <T> void onPageReceived(List<T> pageList) {
            switch (mode)
            {
              case BUSINESS:
                for (Shop shop : (List<Shop>) pageList)
                {
                  mAdapter.insertInternal(adapterDataSetShop, shop, adapterDataSetProfile.size());
                }
                break;
              case USER:
                for (Profile post : (List<Profile>) pageList)
                {
                  mAdapter.insertInternal(adapterDataSetProfile, post, adapterDataSetProfile.size());
                }
                break;
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
                loadMorePages(query, postId, mode);
              }
            }, getActivity());
          }
        });
        break;
      case HASHTAG:
        Pagination.getHashTags(mCurrentPage, query, getContext(), new IPaginationCallback() {
          @Override
          public <T> void onPageReceived(List<T> pageList) {
            for (CheckOut hashtatg : (List<CheckOut>) pageList)
            {
              mAdapter.insertInternal(adapterDataSetHashtag, hashtatg, adapterDataSetHashtag.size());
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
                loadMorePages(query, postId, mode);
              }
            }, getActivity());
          }
        });
    }

  }

}
