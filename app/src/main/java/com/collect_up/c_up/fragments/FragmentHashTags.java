package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.TimelineExploreAdapter;
import com.collect_up.c_up.adapters.TimelineFollowingAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.AsyncTextHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.PostUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 8/1/2016.
 */
@SuppressLint ("ValidFragment")

public class FragmentHashTags extends BaseFragment implements
  TimelineFollowingAdapter.OnFeedItemClickListener,
  PostContextMenu.OnFeedContextMenuItemClickListener,
  TimelineExploreAdapter.ItemListener {
  private UltimateRecyclerView mRecyclerView;
  private TimelineExploreAdapter mWorldAdapter;
  private TimelineFollowingAdapter mFollowingAdapter;
  private int mColumns = 3;
  private final List<Post> mWorldPostList = new ArrayList<>();
  private boolean isfirts = true;
  private BasicGridLayoutManager mWorldLayoutManager;
  private int mCurrentWorldPage = 1;
  private String hashtagString;
  public static boolean isRunning;
  public boolean isViewWithCatalog;
  private int lastViewPosition = 0;
  private LinearLayoutManager verticlaLayoutManager;
  private View view;


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {


    return super.onOptionsItemSelected(item);
  }

  public FragmentHashTags(String hashtagString) {
    this.hashtagString = hashtagString;
  }

  public FragmentHashTags() {
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_hashtags, container, false);
      ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
      actionBar.show();
      actionBar.setTitle(hashtagString);
      int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);

      setHasOptionsMenu(true);

      mRecyclerView = (UltimateRecyclerView) view.findViewById(R.id.recycler_view);
      mRecyclerView.setHasFixedSize(true);

      loadFirstWorldPage();

      mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

      mWorldAdapter = new TimelineExploreAdapter(mWorldPostList, getContext());

      mWorldLayoutManager = new BasicGridLayoutManager(getContext(), mColumns, mWorldAdapter);
      mRecyclerView.setLayoutManager(mWorldLayoutManager);
      mRecyclerView.setPadding(0, 0, 0, 0);

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
      return view;
    } else
    {
      return view;
    }
  }


  private void notifyTagsAdapter() {
    if (mWorldAdapter != null)
    {
      mWorldAdapter.notifyDataSetChanged();
    }
  }

  private void showTagsPosts(List<Post> pageList) {
    isfirts = false;
    mWorldPostList.clear();

    notifyTagsAdapter();

    mWorldPostList.addAll((List<Post>) pageList);
    if (isViewWithCatalog)
    {
      CopyOnWriteArrayList<Post> listWorld = new CopyOnWriteArrayList<>();
      listWorld.addAll(mWorldPostList);
      mFollowingAdapter = new TimelineFollowingAdapter(getContext(), listWorld);
      mRecyclerView.setLayoutManager(verticlaLayoutManager);
      mFollowingAdapter.setOnFeedItemClickListener(this);
      mRecyclerView.setAdapter(mFollowingAdapter);

    } else
    {

      mWorldAdapter = new TimelineExploreAdapter(mWorldPostList, getContext());
      mWorldLayoutManager = new BasicGridLayoutManager(getContext(), mColumns, mWorldAdapter);
      mRecyclerView.setLayoutManager(mWorldLayoutManager);
      mRecyclerView.setAdapter(mWorldAdapter);
      mWorldAdapter.setListener(this);

    }


    mCurrentWorldPage++;

    notifyTagsAdapter();

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

  public void loadFirstWorldPage() {

    mCurrentWorldPage = 1;
    Pagination.getTagsPosts(hashtagString, mCurrentWorldPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        if (pageList.size() == 0)
        {
          setEmtyView();
          mWorldPostList.clear();
          mWorldAdapter = new TimelineExploreAdapter(mWorldPostList, getContext());
          mRecyclerView.setAdapter(mWorldAdapter);
          notifyTagsAdapter();

          return;
        }
        // Reset items to make the pull to refresh right
        showTagsPosts((List<Post>) pageList);
      }

      @Override
      public void onFailure() {
        mRecyclerView.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstWorldPage();
          }
        }, getActivity());
      }
    });
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

  private void loadMoreWorld() {
    Pagination.getTagsPosts(hashtagString, mCurrentWorldPage, getContext(), new IPaginationCallback() {
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
  public void onPostImageClick(Post post, int position) {
    isViewWithCatalog = true;
    lastViewPosition = position;
    verticlaLayoutManager = new LinearLayoutManager(getContext());
    verticlaLayoutManager.scrollToPosition(lastViewPosition);
    mRecyclerView.setLayoutManager(verticlaLayoutManager);
    CopyOnWriteArrayList<Post> listWorld = new CopyOnWriteArrayList<>();
    listWorld.addAll(mWorldPostList);
    mFollowingAdapter = new TimelineFollowingAdapter(getContext(), listWorld);
    mFollowingAdapter.setOnFeedItemClickListener(this);
    mRecyclerView.setAdapter(mFollowingAdapter);
  }

  @Override
  public void onPostCancelClick() {
    PostContextMenuManager.getInstance().hideContextMenu();

  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.hashtags);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @Override
  public void onReportClick(final String postId, int feedItem) {
    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
    builder.message(R.string.sure_to_report_post)
      .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
      .title(R.string.report_post)
      .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .positiveAction(R.string.action_report)
      .negativeAction(R.string.cancel)
      .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
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
            Toast.makeText(getContext(), getString(R.string.toast_successful_report), Toast.LENGTH_SHORT)
              .show();
          }

          @Override
          public void onFailure(int statusCode,
                                Header[] headers,
                                byte[] responseBody,
                                Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);
            Toast.makeText(getContext(), getString(R.string.toast_error_report), Toast.LENGTH_SHORT)
              .show();
          }
        });
      }
    });
    builder.show();
  }

  @Override
  public <T> void onShareOnProfile(int feedItem, final T post) {
    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
    builder.message(R.string.sure_to_share_post_on_profile)
      .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
      .title(R.string.share_post)
      .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .positiveAction(R.string.share)
      .negativeAction(R.string.cancel)
      .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
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
            Toast.makeText(getContext(), getString(R.string.toast_has_been_shared_on_profile), Toast.LENGTH_SHORT).show();
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
    Intent intent = new Intent(getContext(), FragmentShare.class);
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
  public void onCommentsClick(View v, Post post) {
    FragmentHandler.replaceFragment(getContext(), fragmentType.DISPLAYPOST, post);
  }

  @Override
  public <T> void onMoreClick(View view, String postId, final T post, final int position) {
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
  public void onPause() {
    super.onPause();
    isRunning = false;
  }


  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;

  }

  @Override
  public void onStop() {
    super.onStop();
  }

}
