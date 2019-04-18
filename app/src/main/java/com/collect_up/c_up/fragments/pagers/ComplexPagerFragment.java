/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments.pagers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityPickLocation;
import com.collect_up.c_up.adapters.ComplexEventsAdapter;
import com.collect_up.c_up.adapters.ComplexInfoAdapter;
import com.collect_up.c_up.adapters.ComplexShopsAdapter;
import com.collect_up.c_up.adapters.ComplexTimelineAdapter;
import com.collect_up.c_up.fragments.FragmentShare;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.EventComplex;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.view.BasicGridLayoutManager;
import com.collect_up.c_up.view.EventContextMenu;
import com.collect_up.c_up.view.EventContextMenuManager;
import com.collect_up.c_up.view.PostContextMenu;
import com.collect_up.c_up.view.PostContextMenuManager;
import com.collect_up.c_up.view.SpacesItemDecoration;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.app.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

public class ComplexPagerFragment extends Fragment implements ComplexInfoAdapter.ItemListener,
  ComplexEventsAdapter.OnFeedItemClickListener,
  EventContextMenu.OnFeedContextMenuItemClickListener,
  PostContextMenu.OnFeedContextMenuItemClickListener,
  ComplexTimelineAdapter.OnFeedItemClickListener,
  ComplexShopsAdapter.ItemListener {
  public static final String ARG_OBJECT = "object";
  private final int mColumns = 2;
  private final List<EventComplex> mComplexEventList = new ArrayList<>();
  private final List<Post> mComplexPostList = new ArrayList<>();
  private final List<Shop> mComplexShopList = new ArrayList<>();
  private final String mCurrentUserId = Logged.Models.getUserProfile()
    .getId();
  public Complex mComplex;
  public ComplexEventsAdapter mComplexEventsAdapter;
  public UltimateRecyclerView mRecyclerView;
  private BasicGridLayoutManager mComplexShopLayoutManager;
  private ComplexShopsAdapter mComplexShopsAdapter;
  private ComplexTimelineAdapter mComplexTimelineAdapter;
  private int mCurrentEventPage = 1;
  private int mCurrentPostPage = 1;
  private int mCurrentShopPage = 1;
  private ComplexInfoAdapter adapter;

  // Suitable for reinitializing from onNewIntent()


  private void notifyEventAdapter() {
    if (mComplexEventsAdapter != null)
    {
      mComplexEventsAdapter.notifyDataSetChanged();
    }
  }

  private void notifyTimelineAdapter() {
    if (mComplexTimelineAdapter != null)
    {
      mComplexTimelineAdapter.notifyDataSetChanged();
    }
  }

  private void notifyShopAdapter() {
    if (mComplexShopsAdapter != null)
    {
      mComplexShopsAdapter.notifyDataSetChanged();
    }
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstEventPage() {
    // reset current page to make the pull to refresh right
    mCurrentEventPage = 1;
    Pagination.getComplexEvents(mCurrentEventPage, mComplex.getId(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        if (pageList.size() == 0)
        {
          setEmtyView();
          mComplexEventsAdapter = new ComplexEventsAdapter(getActivity(), mComplexEventList, mComplex);
          mRecyclerView.setAdapter(mComplexEventsAdapter);
          notifyEventAdapter();

          return;
        }
        // Reset items to make the pull to refresh right
        mComplexEventList.clear();

        notifyEventAdapter();

        mComplexEventList.addAll((List<EventComplex>) pageList);
        mComplexEventsAdapter = new ComplexEventsAdapter(getActivity(), mComplexEventList, mComplex);
        mComplexEventsAdapter.setOnFeedItemClickListener(ComplexPagerFragment.this);
        mRecyclerView.setAdapter(mComplexEventsAdapter);
        mCurrentEventPage++;

        notifyEventAdapter();

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
        //mRecyclerView.setEmptyView(R.layout.empty_list_view_cause_get_data_error);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstEventPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstPostPage() {
    // reset current page to make the pull to refresh right
    mCurrentPostPage = 1;
    Pagination.getComplexPosts(mCurrentPostPage, mComplex.getId(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        if (pageList.size() == 0)
        {
          setEmtyView();
          mComplexPostList.clear();
          mComplexTimelineAdapter = new ComplexTimelineAdapter(getActivity(), mComplexPostList);
          mRecyclerView.setAdapter(mComplexTimelineAdapter);
          notifyTimelineAdapter();

          return;
        }
        // Reset items to make the pull to refresh right
        mComplexPostList.clear();

        notifyTimelineAdapter();

        mComplexPostList.addAll((List<Post>) pageList);
        mComplexTimelineAdapter = new ComplexTimelineAdapter(getActivity(), mComplexPostList);
        mComplexTimelineAdapter.setOnFeedItemClickListener(ComplexPagerFragment.this);
        mRecyclerView.setAdapter(mComplexTimelineAdapter);
        mCurrentPostPage++;

        notifyTimelineAdapter();

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
            loadFirstPostPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstShopPage() {
    // reset current page to make the pull to refresh right
    mCurrentShopPage = 1;
    Pagination.getComplexShops(mCurrentShopPage, mComplex.getId(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        if (pageList.size() == 0)
        {
          setEmtyView();
          mComplexShopList.clear();
          mComplexShopsAdapter = new ComplexShopsAdapter(getActivity(), mComplexShopList);
          mRecyclerView.setAdapter(mComplexShopsAdapter);
          notifyShopAdapter();


          return;
        }
        // Reset items to make the pull to refresh right
        mComplexShopList.clear();

        notifyShopAdapter();

        mComplexShopList.addAll((List<Shop>) pageList);

        mComplexShopsAdapter = new ComplexShopsAdapter(getActivity(), mComplexShopList);
        mComplexShopsAdapter.setListener(ComplexPagerFragment.this);
        mComplexShopLayoutManager = new BasicGridLayoutManager(getActivity(), mColumns, mComplexShopsAdapter);

        mRecyclerView.setLayoutManager(mComplexShopLayoutManager);
        mRecyclerView.setAdapter(mComplexShopsAdapter);

        mCurrentShopPage++;

        notifyShopAdapter();

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
            loadFirstShopPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMoreEvents() {
    Pagination.getComplexEvents(mCurrentEventPage, mComplex.getId(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (EventComplex post : (List<EventComplex>) pageList)
        {
          mComplexEventsAdapter.insertInternal(mComplexEventList, post, mComplexEventList.size());
        }

        mCurrentEventPage++;

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
            loadMoreEvents();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMorePosts() {
    Pagination.getComplexPosts(mCurrentPostPage, mComplex.getId(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Post post : (List<Post>) pageList)
        {
          mComplexTimelineAdapter.insertInternal(mComplexPostList, post, mComplexPostList.size());
        }

        mCurrentPostPage++;

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
            loadMorePosts();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMoreShops() {
    Pagination.getComplexShops(mCurrentShopPage, mComplex.getId(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Shop post : (List<Shop>) pageList)
        {
          mComplexShopsAdapter.insertInternal(mComplexShopList, post, mComplexShopList.size());
        }

        mCurrentShopPage++;

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
            loadMoreShops();
          }
        }, getActivity());
      }
    });
  }

  @Override
  public void onCommentsClick(View v, Post post) {
    FragmentHandler.replaceFragment(getContext(), fragmentType.DISPLAYPOST, post);
  }

  @Override
  public <T> void onTimelineMoreClick(View view, String postId, final T post, final int position) {
    final BottomSheetDialog mDialog = new BottomSheetDialog(getContext());
    mDialog.contentView(R.layout.bottom_sheet_share)
      .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
      .inDuration(300)
      .cancelable(true);

    ImageButton btnShareOnProfile = (ImageButton) mDialog.findViewById(R.id.btnProfileShare);
    ImageButton btnShareChat = (ImageButton) mDialog.findViewById(R.id.btnChatShare);
    btnShareOnProfile.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onShareOnProfile(position, post);
        mDialog.dismiss();

      }
    });
    btnShareChat.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onShareToChats(position, post);
        mDialog.dismiss();

      }
    });
    mDialog.show();

  }

  @Override
  public void onLikesCountClick(String postId) {
    FragmentHandler.replaceFragment(getContext(), fragmentType.LIKES, postId);

  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    int spacingInPixels = (int) getResources().getDimension(R.dimen.grid_spacing);

    View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

    Bundle args = getArguments();

    mComplex = args.getParcelable("complex");

    mRecyclerView = (UltimateRecyclerView) rootView.findViewById(R.id.recycler_view);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setEmptyView(R.layout.empty_list_view);

    // Timeline
    if (args.getInt(ARG_OBJECT) == 0)
    {
      loadFirstPostPage();

      LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
      mRecyclerView.setLayoutManager(layoutManager);

      mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          loadFirstPostPage();
        }
      });

      mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMorePosts();
        }
      });

      mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
          super.onScrolled(recyclerView, dx, dy);
          PostContextMenuManager.getInstance().onRecyclerViewScroll(dy);
        }
      });
    }
    // Shops
    else if (args.getInt(ARG_OBJECT) == 1)
    {
      loadFirstShopPage();

      mRecyclerView.addItemDecoration(new SpacesItemDecoration(Utils.dpToPx(spacingInPixels)));

      mComplexShopsAdapter = new ComplexShopsAdapter(getActivity(), mComplexShopList);

      BasicGridLayoutManager layoutManager = new BasicGridLayoutManager(getActivity(), mColumns, mComplexShopsAdapter);
      mRecyclerView.setLayoutManager(layoutManager);
      mRecyclerView.setPadding(0, 0, 0, 0);

      mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          loadFirstShopPage();
        }
      });

      mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMoreShops();
        }
      });
    }
    // Events
    else if (args.getInt(ARG_OBJECT) == 2)
    {
      loadFirstEventPage();

      LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
      mRecyclerView.setLayoutManager(layoutManager);

      mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          loadFirstEventPage();
        }
      });

      mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMoreEvents();
        }
      });
      mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
          super.onScrolled(recyclerView, dx, dy);
          EventContextMenuManager.getInstance().onRecyclerViewScroll(dy);
        }
      });
    }
    // Info
    else if (args.getInt(ARG_OBJECT) == 3)
    {
      adapter = new ComplexInfoAdapter(getActivity(), mComplex);
      adapter.setClickListener(this);

      LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
      mRecyclerView.setLayoutManager(layoutManager);
      mRecyclerView.setAdapter(adapter);

      getUpdatedComplexAsync();
    }

    return rootView;
  }

  private void getUpdatedComplexAsync() {
    HttpClient.get(String.format(Constants.Server.Complex.GET_COMPLEX, mComplex.getId()), new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        if (isVisible())
        {
          final Complex complex = GsonParser.getObjectFromGson(responseBody, Complex.class);
          if (adapter != null)
          {
            adapter.updateDataSet(complex);
          }
        }
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

      }
    });
  }

  @Override
  public void onDeleteClick(final int itemPosition, final String eventId) {
    final com.rey.material.app.SimpleDialog dialog = new com.rey.material.app.SimpleDialog(getActivity());
    dialog.message(R.string.want_to_delete_event)
      .messageTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text))
      .title(R.string.are_you_sure)
      .titleColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
      .positiveAction(R.string.im_sure)
      .negativeAction(R.string.cancel)
      .actionTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
      .setCancelable(true);

    dialog.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();

      }
    });
    dialog.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        HttpClient.delete(String.format(Constants.Server.Event.DELETE_BY_ID, eventId), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            mComplexEventsAdapter.removeInternal(mComplexEventList, itemPosition);
            mComplexEventsAdapter.notifyItemRemoved(itemPosition);
            dialog.dismiss();
          }

          @Override
          public void onFailure(int statusCode,
                                Header[] headers,
                                byte[] responseBody,
                                Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

            dialog.dismiss();


          }
        });
      }
    });
    dialog.show();
  }

  @Override
  public void onEventCancelClick() {
    EventContextMenuManager.getInstance().hideContextMenu();
  }

  @Override
  public void onEventMoreClick(View view, String eventId, int position) {
    onDeleteClick(position, eventId);


  }

  @Override
  public void onMapClick(LatLng latLng) {
    Intent intent = new Intent(getActivity(), ActivityPickLocation.class);
    intent.putExtra("location", latLng);
    startActivityForResult(intent, Constants.RequestCodes.PICK_LOCATION.ordinal());
  }

  @Override
  public void onFollowersClick(String complexId) {
    Bundle bundle = new Bundle();
    bundle.putString("id", complexId);
    bundle.putInt("flag", 3);
    FragmentHandler.replaceFragment(getContext(), fragmentType.FOLLOWNFOLLOWING, bundle);

  }

  @Override
  public void onRatingBarChanged(final RatingBar[] ratingBar,
                                 float rating,
                                 boolean fromUser,
                                 final Complex complex,
                                 final TextView totalVotesView,
                                 final TextView txtAverage) {
    String url = String.format(Constants.Server.Complex.GET_RATE, complex.getId(), Float
      .toString(rating));
    HttpClient.get(url, new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        CheckOut checkOut = GsonParser.getObjectFromGson(responseBody, CheckOut.class);
        totalVotesView.setText(Integer.toString(checkOut.getId()));
        String averageVotes = checkOut.getValue();
        ratingBar[1].setRating(Float.valueOf(averageVotes));
        complex.setRatesAverage(checkOut.getValue());
        complex.setRatesCount(checkOut.getId());
        txtAverage.setText(averageVotes.matches("[0-9]+\\.[0-9]*") ? averageVotes : averageVotes + ".0");
        adapter.setRatingColor(ratingBar[0], R.color.green_opacity, R.color.green);
      }

      @Override
      public void onFailure(int statusCode,
                            Header[] headers,
                            byte[] responseBody,
                            Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

        Toast.makeText(getActivity(), getString(R.string.toast_error_updating_rate), Toast.LENGTH_SHORT)
          .show();
      }
    });
  }

  @Override
  public void onFollowClick(final Button view, final TextView totalFollowers, final Complex complex) {
    final Hashtable<ComplexInfoAdapter.ComplexInfoViewHolder, String> tags = (Hashtable<ComplexInfoAdapter.ComplexInfoViewHolder, String>) view
      .getTag();

    final ComplexInfoAdapter.ComplexInfoViewHolder holder = tags.keys().nextElement();

    //Prevent from multiple clicking on the view
    view.setEnabled(false);

    if (tags.values().iterator().next().equals("false"))
    {
      HttpClient.get(String.format(Constants.Server.Complex.GET_FOLLOW, complex.getId()), new AsyncHttpResponser(getContext()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          tags.put(holder, "true");
          view.setTag(tags);
          view.setText(R.string.unfollow);
          totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
            .toString()) + 1));
          view.setEnabled(true);
          complex.getFollowers().add(Logged.Models.getUserProfile().getId());
        }

        @Override
        public void onFailure(int statusCode,
                              Header[] headers,
                              byte[] responseBody,
                              Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);

          Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_following), Toast.LENGTH_SHORT)
            .show();
          view.setEnabled(true);
        }
      });
    } else
    {
      HttpClient.get(String.format(Constants.Server.Complex.GET_UNFOLLOW, complex.getId()), new AsyncHttpResponser(getContext()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          tags.put(holder, "false");
          view.setTag(tags);
          view.setText(R.string.follow);
          totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
            .toString()) - 1));
          view.setEnabled(true);
          complex.getFollowers().remove(Logged.Models.getUserProfile().getId());
        }

        @Override
        public void onFailure(int statusCode,
                              Header[] headers,
                              byte[] responseBody,
                              Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);

          Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_unfollowing), Toast.LENGTH_SHORT)
            .show();
          view.setEnabled(true);
        }
      });
    }
  }

  private void setEmtyView()

  {

    mRecyclerView.showEmptyView();

  }

  @Override
  public void onPostCancelClick() {
    PostContextMenuManager.getInstance().hideContextMenu();
  }

  @Override
  public void onReportClick(final String postId, int feedItem) {

    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getActivity());
    builder.message(R.string.sure_to_report_post)
      .messageTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text))
      .title(R.string.report_post)
      .titleColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
      .positiveAction(R.string.action_report)
      .negativeAction(R.string.cancel)
      .actionTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
      .setCancelable(true);

    builder.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        builder.dismiss();

      }
    });
    builder.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        builder.dismiss();
        String f = String.format(Constants.Server.Post.GET_REPORT, postId);
        HttpClient.get(f, new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.toast_successful_report), Toast.LENGTH_SHORT)
              .show();
          }

          @Override
          public void onFailure(int statusCode,
                                Header[] headers,
                                byte[] responseBody,
                                Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

            Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_report), Toast.LENGTH_SHORT)
              .show();
          }
        });
      }
    });
    builder.show();
  }

  @Override
  public <T> void onShareOnProfile(int feedItem, final T post) {

    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getActivity());
    builder.message(R.string.sure_to_share_post_on_profile)
      .messageTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text))
      .title(R.string.share_post)
      .titleColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
      .positiveAction(R.string.share)
      .negativeAction(R.string.cancel)
      .actionTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
      .setCancelable(true);

    builder.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        builder.dismiss();

      }
    });
    builder.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        builder.dismiss();
        Post oldPost = (Post) post;
        Post newPost = new Post();

        newPost.setSenderProfile(Logged.Models.getUserProfile());
        newPost.setImageAddress(oldPost.getImageAddress());
        newPost.setText(oldPost.getText());
        newPost.setVideoAddress(oldPost.getVideoAddress());
        newPost.setSize(oldPost.getSize());
        HttpClient.post(getContext(), Constants.Server.Post.POST, new Gson().toJson(newPost, Post.class), "application/json", new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Toast.makeText(getContext(), getContext().getString(R.string.toast_has_been_shared_on_profile), Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onFailure(int statusCode,
                                Header[] headers,
                                byte[] responseBody,
                                Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);

            Toast.makeText(getContext(), getString(R.string.toast_error_sharing_post), Toast.LENGTH_SHORT)
              .show();
          }
        });
      }
    });
    builder.show();
  }

  @Override
  public <T> void onShareToChats(int feedItem, T post) {
    Intent intent = new Intent(getActivity(), FragmentShare.class);
    CompactMessage message = new CompactMessage();
    message.setId(UUID.randomUUID().toString());
    message.setSender(Logged.Models.getUserProfile());
    message.setText(((Post) post).getSender().getName());
    message.setContentType(EnumMessageContentType.SharedPost);
    message.setSendDateTime(Long.toString(System.currentTimeMillis()));
    message.setContentSize(new Gson().toJson(post, Post.class));
    intent.putExtra("message", message);
    FragmentHandler.replaceFragment(getContext(), fragmentType.SHARE, intent);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.RequestCodes.UPDATE_UI.ordinal())
    {

      if (data != null && data.getParcelableExtra("post") != null)
      {
        if (mComplexTimelineAdapter != null)
        {
          mComplexTimelineAdapter.updatePost((Post) data.getParcelableExtra("post"), data.getBooleanExtra("deleted", false));
        }
      } else if (data != null && data.getParcelableExtra("profile") != null)
      {
        if (mComplexShopsAdapter != null)
        {
          mComplexShopsAdapter.updateShop((Shop) data.getParcelableExtra("profile"), data.getBooleanExtra("deleted", false));
        }
      }
    }
  }

  @Override
  public void onShopPictureClick(Shop shop) {
    FragmentHandler.replaceFragment(getActivity(), fragmentType.BUSINESS, shop);

  }
}
