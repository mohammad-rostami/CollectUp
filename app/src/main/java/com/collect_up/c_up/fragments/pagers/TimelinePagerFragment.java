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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.TimelineExploreAdapter;
import com.collect_up.c_up.adapters.TimelineFollowingAdapter;
import com.collect_up.c_up.fragments.FragmentShare;
import com.collect_up.c_up.fragments.FragmentTimeline;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.AsyncTextHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.OnBackPressed;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.PostCacheHandler;
import com.collect_up.c_up.helpers.PostUtils;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.view.BasicGridLayoutManager;
import com.collect_up.c_up.view.PostContextMenu;
import com.collect_up.c_up.view.PostContextMenuManager;
import com.collect_up.c_up.view.SpacesItemDecoration;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.app.BottomSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

public class TimelinePagerFragment extends Fragment
  implements TimelineFollowingAdapter.OnFeedItemClickListener,
  PostContextMenu.OnFeedContextMenuItemClickListener,
  TimelineExploreAdapter.ItemListener {
  public static final String ARG_OBJECT = "object";
  public final static int mColumns = 3;
  private final String mCurrentUserId = Logged.Models.getUserProfile().getId();
  private final List<Post> mWorldPostList = new ArrayList<>();
  public CopyOnWriteArrayList<Post> mFollowingPostList = new CopyOnWriteArrayList<>();
  public TimelineFollowingAdapter mFollowingAdapter;
  private int mCurrentFollowingPage = 1;
  private int mCurrentWorldPage = 1;
  private TimelineExploreAdapter mWorldAdapter;
  private BasicGridLayoutManager mWorldLayoutManager;
  private LinearLayoutManager mFollowinglayoutManager;
  private UltimateRecyclerView mRecyclerView;
  private boolean isfirts = true;
  private View rootView;
  public static boolean isViewWithCatalog;
  private int lastViewPosition = 0;
  private Realm mRealm;

  private void notifyFollowingAdapter() {
    if (mFollowingAdapter != null)
    {
      mFollowingAdapter.notifyDataSetChanged();
    }
  }

  private void notifyExploreAdapter() {
    if (mWorldAdapter != null)
    {
      mWorldAdapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data == null)
    {
      return;
    }
    if (requestCode == Constants.RequestCodes.UPDATE_UI.ordinal())
    {
      if (data.getParcelableExtra("post") != null)
      {
        if (data.getParcelableExtra("post") instanceof Post)
        {
          Post post = PostUtils.convertToPostTimeline((Post) data.getParcelableExtra("post"));
          if (mFollowingAdapter != null)
          {
            mFollowingAdapter.updatePost(post, data.getBooleanExtra("deleted", false));
          }
          if (mWorldAdapter != null)
          {
            mWorldAdapter.updatePost(post, data.getBooleanExtra("deleted", false));
          }
        } else if (data.getParcelableExtra("post") instanceof Post)
        {
          Post post = PostUtils.convertToPostTimeline((Post) data.getParcelableExtra("post"));
          if (mFollowingAdapter != null)
          {
            mFollowingAdapter.updatePost(post, data.getBooleanExtra("deleted", false));
          }
          if (mWorldAdapter != null)
          {
            mWorldAdapter.updatePost(post, data.getBooleanExtra("deleted", false));
          }
        } else if (data.getParcelableExtra("post") instanceof Post)
        {
          Post post = PostUtils.convertToPostTimeline((Post) data.getParcelableExtra("post"));
          if (mFollowingAdapter != null)
          {
            mFollowingAdapter.updatePost(post, data.getBooleanExtra("deleted", false));
          }
          if (mWorldAdapter != null)
          {
            mWorldAdapter.updatePost(post, data.getBooleanExtra("deleted", false));
          }
        }
      } else if (data.getParcelableExtra("profile") != null)
      {
        if (mFollowingAdapter != null)
        {
          mFollowingAdapter.updatePostProfile(data.getParcelableExtra("profile"));
        }
      }
    }
  }

  @Override
  public void onPause() {
    isViewWithCatalog = false;
    super.onPause();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getActivity()));

    if (rootView == null)
    {
      rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

      int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);

      Bundle args = getArguments();

      mRecyclerView = (UltimateRecyclerView) rootView.findViewById(R.id.recycler_view);
      mRecyclerView.setHasFixedSize(false);
      mFollowinglayoutManager = new LinearLayoutManager(getActivity());

      if (args.getInt(ARG_OBJECT) == 0)
      {
        mRecyclerView.setLayoutManager(mFollowinglayoutManager);
        mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            loadFirstFollowingPage();
          }
        });
        mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
          @Override
          public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
            loadMoreFollowing();
          }
        });
        mRecyclerView.addOnScrollListener(
          new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
              super.onScrolled(recyclerView, dx, dy);
              PostContextMenuManager.getInstance().onRecyclerViewScroll(dy);
            }
          });
        loadFirstFollowingPage();


      } else
      {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            loadFirstWorldPage();
          }
        });

        mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
          @Override
          public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
            loadMoreWorld();
          }
        });

        loadFirstWorldPage();

        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        mWorldAdapter = new TimelineExploreAdapter(mWorldPostList, getContext());

        mRecyclerView.setLayoutManager(mWorldLayoutManager);
        mRecyclerView.setPadding(0, 0, 0, 0);


      }
      return rootView;
    } else
    {
      return rootView;
    }
  }

  private void showFollowingPosts(List<Post> pageList) {
    isfirts = false;
    mFollowingPostList.clear();

    notifyFollowingAdapter();

    mFollowingPostList.addAll(pageList);
    mFollowingAdapter = new TimelineFollowingAdapter(getActivity(), mFollowingPostList);
    mFollowingAdapter.setOnFeedItemClickListener(TimelinePagerFragment.this);
    mRecyclerView.setAdapter(mFollowingAdapter);
    mCurrentFollowingPage++;

    notifyFollowingAdapter();

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

  private void showExplorePosts(List<Post> pageList) {
    isfirts = false;
    mWorldPostList.clear();

    notifyExploreAdapter();

    mWorldPostList.addAll((List<Post>) pageList);
    if (isViewWithCatalog)
    {
      CopyOnWriteArrayList<Post> listWorld = new CopyOnWriteArrayList<>();
      listWorld.addAll(mWorldPostList);
      mFollowingAdapter = new TimelineFollowingAdapter(getActivity(), listWorld);
      mRecyclerView.setLayoutManager(mFollowinglayoutManager);
      mFollowingAdapter.setOnFeedItemClickListener(this);
      mRecyclerView.setAdapter(mFollowingAdapter);

    } else
    {
      mWorldAdapter = new TimelineExploreAdapter(mWorldPostList, getContext());
      mWorldLayoutManager = new BasicGridLayoutManager(getActivity(), mColumns, mWorldAdapter);
      mRecyclerView.setLayoutManager(mWorldLayoutManager);
      mRecyclerView.setAdapter(mWorldAdapter);
      mWorldAdapter.setListener(TimelinePagerFragment.this);

    }

    mCurrentWorldPage++;
    mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
      @Override
      public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
        loadMoreWorld();
      }
    });

    notifyExploreAdapter();

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

  @SuppressWarnings ("unchecked")
  public void loadFirstFollowingPage() {
    // reset current page to make the pull to refresh right
    final List<Post> followingPostCache = PostCacheHandler.getFollowingPosts(mRealm);
    if (followingPostCache != null && isfirts)
    {
      showFollowingPosts(followingPostCache);

    }
    mCurrentFollowingPage = 1;
    Pagination.getFollowingPosts(mCurrentFollowingPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {


        if (pageList.size() == 0)
        {
          setEmtyView();
          mFollowingPostList.clear();
          mFollowingAdapter = new TimelineFollowingAdapter(getActivity(), mFollowingPostList);
          mRecyclerView.setAdapter(mFollowingAdapter);
          notifyFollowingAdapter();

          return;
        }
        PostCacheHandler.setFollowingPosts(RToNonR.postTimelineToRpostTimelineList((List<Post>) pageList), mRealm);
        showFollowingPosts((List<Post>) pageList);


      }

      @Override
      public void onFailure() {
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstFollowingPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  public void loadFirstWorldPage() {

    mCurrentWorldPage = 1;
    Pagination.getWorldPosts(mCurrentWorldPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        if (pageList.size() == 0)
        {
          setEmtyView();
          mWorldPostList.clear();
          mWorldAdapter = new TimelineExploreAdapter(mWorldPostList, getContext());
          mRecyclerView.setAdapter(mWorldAdapter);
          notifyExploreAdapter();

          return;
        }
        // Reset items to make the pull to refresh right
        // PostCacheHandler.setExplorePosts((List<Post>) pageList);
        showExplorePosts((List<Post>) pageList);
      }

      @Override
      public void onFailure() {

        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstWorldPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMoreFollowing() {
    Pagination.getFollowingPosts(mCurrentFollowingPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Post post : (List<Post>) pageList)
        {
          mFollowingAdapter.insertInternal(mFollowingPostList, post, mFollowingPostList.size());
        }
        mCurrentFollowingPage++;

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
            loadMoreFollowing();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMoreWorld() {
    Pagination.getWorldPosts(mCurrentWorldPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (Post post : (List<Post>) pageList)
        {
          mWorldAdapter.insertInternal(mWorldPostList, post, mWorldPostList.size());
        }

        mCurrentWorldPage++;

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
            loadMoreWorld();
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
  public <T> void onMoreClick(View v, String postId, final T post, final int position) {
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
  public void onProfilePictureClick(Post post) {
    Intent intent = null;
    if (post.getSenderProfile() != null)
    {
      FragmentHandler.replaceFragment(getContext(), fragmentType.PROFILE, post.getSenderProfile());
      return;

    } else if (post.getSenderComplex() != null)
    {
      FragmentHandler.replaceFragment(getContext(), fragmentType.COMPLEX, post.getSenderComplex());
      return;

    } else if (post.getSenderShop() != null)
    {

      FragmentHandler.replaceFragment(getContext(), fragmentType.BUSINESS, post.getSenderShop());
      return;

    }

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
        HttpClient.post(getContext(), Constants.Server.Post.POST, new Gson().toJson(newPost, Post.class), "application/json", new AsyncTextHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Toast.makeText(getContext(), getContext().getString(R.string.toast_has_been_shared_on_profile), Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onFailure(int statusCode,
                                Header[] headers,
                                byte[] responseBody,
                                Throwable error) {
            Toast.makeText(getContext(), getString(R.string.toast_error_sharing_post), Toast.LENGTH_SHORT)
              .show();
          }
        });

      }
    });
    builder.show();
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

  @Override
  public <T> void onShareToChats(int feedItem, T post) {
    Intent intent = new Intent(getActivity(), FragmentShare.class);
    CompactMessage message = new CompactMessage();
    message.setId(UUID.randomUUID().toString());
    message.setSender(Logged.Models.getUserProfile());

    message.setSendDateTime(Long.toString(System.currentTimeMillis()));
    String json = null;
    if (((Post) post).getSenderProfile() != null)
    {
      message.setContentType(EnumMessageContentType.SharedPost);
      message.setText(((Post) post).getSenderProfile().getName());
      json = new Gson().toJson(PostUtils.convertToPostTimeline((Post) post), Post.class);
    } else if (((Post) post).getSenderComplex() != null)
    {
      message.setContentType(EnumMessageContentType.SharedPost);
      message.setText(((Post) post).getSenderComplex().getName());
      json = new Gson().toJson(PostUtils.convertToPostTimeline((Post) post), Post.class);
    } else if (((Post) post).getSenderShop() != null)
    {
      message.setContentType(EnumMessageContentType.SharedPost);
      message.setText(((Post) post).getSenderShop().getName());
      json = new Gson().toJson(PostUtils.convertToPostTimeline((Post) post), Post.class);
    }

    message.setContentSize(json);
    intent.putExtra("message", message);
    FragmentHandler.replaceFragment(getContext(), fragmentType.SHARE, intent);

  }

  @Override
  public void onPostImageClick(Post post, int position) {
    Intent intent = null;

    isViewWithCatalog = true;
    lastViewPosition = position;
    mFollowinglayoutManager.scrollToPosition(lastViewPosition);
    mRecyclerView.setLayoutManager(mFollowinglayoutManager);
    CopyOnWriteArrayList<Post> listWorld = new CopyOnWriteArrayList<>();
    listWorld.addAll(mWorldPostList);
    mFollowingAdapter = new TimelineFollowingAdapter(getActivity(), listWorld);
    mFollowingAdapter.setOnFeedItemClickListener(this);

    mRecyclerView.setAdapter(mFollowingAdapter);
    ((FragmentTimeline) getParentFragment()).hideTabBar(true);
    ((FragmentTimeline) getParentFragment()).hideToolbar(false);
  }

  @Override
  public void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onStop() {
    EventBus.getDefault().unregister(this);
    super.onStop();
  }

  @Subscribe (threadMode = ThreadMode.MAIN)
  public void onMessageEvent(OnBackPressed event) {
    ((FragmentTimeline) getParentFragment()).hideToolbar(true);
    ((FragmentTimeline) getParentFragment()).hideTabBar(false);
    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

    isViewWithCatalog = false;
    mWorldLayoutManager.scrollToPosition(lastViewPosition);
    mRecyclerView.setLayoutManager(mWorldLayoutManager);
    mRecyclerView.setAdapter(mWorldAdapter);

  }
}
