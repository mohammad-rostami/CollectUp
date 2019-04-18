/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.PostDisplayAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.AsyncTextHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.Comment;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.view.CustomEditText;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.mikepenz.materialdrawer.Drawer;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

@SuppressLint ("ValidFragment")

public class FragmentPostDisplay extends FragmentMentions {

  public static int pageSize = Pagination.PAGE_IN_REQUEST;
  @Bind (R.id.recycler_view)
  UltimateRecyclerView mRecyclerView;
  @NotEmpty (trim = true)
  @Bind (R.id.edit_text_comment)
  CustomEditText mEditTextComment;
  @Bind (R.id.image_button_post_comment)
  ImageButton mImageButtonPostComment;
  private Post mPost;
  private PostDisplayAdapter mAdapter;
  private Drawer mDrawer;
  private View fragmentSView;
  public String finalUserText;
  ArrayList<String> mentionsUser = new ArrayList<>();
  ArrayList<String> mentionsBuss = new ArrayList<>();

  ArrayList<String> mentionsEncrypt = new ArrayList<>();
  private View view;


  public void removeNullItem() {
    for (Comment comment : mAdapter.mComments)
    {
      if (comment == null)
      {
        int position = mAdapter.mComments.indexOf(null);
        mAdapter.mComments.remove(position);
        mAdapter.notifyBinderItemRemoved(mAdapter.mPostCommentsBinder, position);
        break;
      }
    }
  }


  public FragmentPostDisplay(Post mPost) {
    this.mPost = mPost;
  }

  public FragmentPostDisplay() {
  }

  @Override
  public void onResume() {
    super.onResume();

    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.post);
    ((ActivityHome) getActivity()).hideButtonBar(true);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    if (mDrawer != null)
    {
      DrawerHelper.update(mDrawer, getActivity());
      mDrawer.setSelection(-1);
    }
  }

  @SuppressWarnings ("unchecked")
  public void loadCommentPages(final int pageNumber) {
    Pagination.getPostComments(pageNumber, mPost.getId(), getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        removeNullItem();

        List<Comment> oldList = new ArrayList<>();
        oldList.addAll(mAdapter.mComments);

        mAdapter.clearInternal(mAdapter.mComments);

        mAdapter.addComments(oldList);
        mAdapter.addComments((List<Comment>) pageList);
        pageSize = pageList.size();
        if (pageSize > 0 && pageSize == Pagination.PAGE_IN_REQUEST)
        {
          mAdapter.insertInternal(mAdapter.mComments, null, mAdapter.mComments.size());
        } else
        {
          removeNullItem();
        }
      }

      @Override
      public void onFailure() {
        mRecyclerView.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadCommentPages(pageNumber);
          }
        }, getActivity());
      }
    });
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    if (view == null)
    {
      getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      setHasOptionsMenu(true);
      view = inflater.inflate(R.layout.fragment_display_post, container, false);
      ButterKnife.bind(this, view);

      fragmentSView = view.findViewById(R.id.fragmentSuggestionlist);
      fragmentSView.setVisibility(View.GONE);
      mEditTextComment.setHandleDismissingKeyboard(new CustomEditText.handleDismissingKeyboard() {
        @Override
        public void dismissKeyboard() {
          if (fragmentSView.getVisibility() == View.VISIBLE)
          {
            fragmentSView.setVisibility(View.GONE);
          } else
          {
            InputMethodManager imm = (InputMethodManager)
              getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEditTextComment.getWindowToken(), 0);
            mEditTextComment.clearFocus();
          }
        }

      });
      final FragmentMention fragmentMention = new FragmentMention();
      FragmentManager manager = getChildFragmentManager();
      final FragmentTransaction transaction = manager.beginTransaction();
      transaction.replace(R.id.fragmentSuggestionlist, fragmentMention).commit();
      mEditTextComment.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          fragmentSView.setVisibility(View.GONE);

          return false;
        }
      });

      mEditTextComment.addTextChangedListener(new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void afterTextChanged(Editable inputText) {

          if (inputText.toString().contains(Constants.General.MENTION_USER_SIGN))
          {
            int cursorPosition = mEditTextComment.getSelectionStart();
            int finalPos = SepehrUtil.getLastCharPos(Constants.General.MENTION_USER_SIGN, inputText.toString(), cursorPosition);
            String atUserText = inputText.toString().substring(finalPos, cursorPosition);


            finalUserText = getUserSplite(atUserText);
            if (!Utils.isNullOrEmpty(finalUserText) && !finalUserText.contains(" "))
            {
              fragmentMention.loadFirstMentionPage(finalUserText.trim(), mPost.getId(), Pagination.MentionMode.USER);

            } else
            {
              fragmentSView.setVisibility(View.GONE);
            }

          }
          if (inputText.toString().contains(Constants.General.MENTION_BUSINESS_SIGN))
          {

            int cursorPosition = mEditTextComment.getSelectionStart();
            int finalPos = SepehrUtil.getLastCharPos(Constants.General.MENTION_BUSINESS_SIGN, inputText.toString(), cursorPosition);
            String atUserText = inputText.toString().substring(finalPos, cursorPosition);


            finalUserText = getBusinessSplite(atUserText);
            if (!Utils.isNullOrEmpty(finalUserText) && !finalUserText.contains(" "))
            {
              fragmentMention.loadFirstMentionPage(finalUserText.trim(), mPost.getId(), Pagination.MentionMode.BUSINESS);

            } else
            {
              fragmentSView.setVisibility(View.GONE);
            }

          }
        }


      });
      mAdapter = new PostDisplayAdapter(this, mPost);

      mRecyclerView.setHasFixedSize(false);
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      mRecyclerView.setLayoutManager(layoutManager);


      loadCommentPages(1);

      mRecyclerView.addItemDividerDecoration(getContext());

      mRecyclerView.setAdapter(mAdapter);

      return view;
    } else
    {
      return view;
    }
  }


  private String getBusinessSplite(String business) {
    String[] splitedText = business.split("\\" + Constants.General.MENTION_BUSINESS_SIGN);
    String result = "";
    if (splitedText.length > 0)
    {
      result = splitedText[splitedText.length - 1];
    }
    return result;
  }

  private String getUserSplite(String user) {
    String[] splitedText = user.split(Constants.General.MENTION_USER_SIGN);
    String result = "";
    if (splitedText.length > 0)
    {
      result = splitedText[splitedText.length - 1];
    }
    return result;
  }

  @Override
  public void setVisibilityMentionFragment(boolean visible) {
    if (visible)
    {
      fragmentSView.setVisibility(View.VISIBLE);
    } else
    {
      fragmentSView.setVisibility(View.GONE);
    }
  }


  private void updatePostAsync() {
    HttpClient.get(String.format(Constants.Server.Post.GET_POST, mPost.getId()), new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        mPost = GsonParser.getObjectFromGson(responseBody, Post.class);
        mAdapter.updatePost(mPost);
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

      }
    });
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_post_display, menu);
  }


  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    checkPrivileges(menu);
  }


  @Override
  public void setProfileToMention(Profile profile) {
    ArrayList<Object> result = setMentioned(profile, mEditTextComment.getText().toString(), mEditTextComment.getSelectionStart());
    mentionsUser.add((String) result.get(0));
    mentionsEncrypt.add((String) result.get(1));
    mEditTextComment.setText((String) result.get(2));
    mEditTextComment.setSelection((Integer) result.get(3));
  }

  @Override
  public void setShopToMention(Shop shop) {
    ArrayList<Object> result = setMentioned(shop, mEditTextComment.getText().toString(), mEditTextComment.getSelectionStart());
    mentionsUser.add((String) result.get(0));
    mentionsEncrypt.add((String) result.get(1));
    mEditTextComment.setText((String) result.get(2));
    mEditTextComment.setSelection((Integer) result.get(3));
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case android.R.id.home:
        break;
      case R.id.action_edit:
        FragmentHandler.replaceFragment(getContext(), fragmentType.EDITPOST, mPost);
        break;
      case R.id.action_delete:
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
        builder.message(R.string.sure_to_delete_post)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .title(R.string.delete_confirmation)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.im_sure)
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
            HttpClient.delete(String.format(Constants.Server.Post.DELETE_BY_ID, mPost
              .getId()), new AsyncHttpResponser(getContext()) {
              @Override
              public void onSuccess(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody) {
                builder.dismiss();
                FragmentHandler.onBackPressed(getContext());

              }

              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                Toast.makeText(getContext(), getString(R.string.toast_error_deleting_post), Toast.LENGTH_SHORT)
                  .show();
              }
            });
          }
        });

        builder.show();
        break;
      case R.id.action_report:
        final com.rey.material.app.SimpleDialog builder1 = new com.rey.material.app.SimpleDialog(getContext());
        builder1.message(R.string.sure_to_report_post)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .title(R.string.report_post)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.action_report)
          .negativeAction(R.string.cancel)
          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .setCancelable(true);

        builder1.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            builder1.dismiss();

          }
        });
        builder1.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            builder1.dismiss();
            String f = String.format(Constants.Server.Post.GET_REPORT, mPost.getId());
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
        builder1.show();
    }
    return false;
  }

  private void checkPrivileges(Menu menu) {

    MenuItem actionEdit = menu.findItem(R.id.action_edit);
    MenuItem actionDelete = menu.findItem(R.id.action_delete);
    MenuItem actionReport = menu.findItem(R.id.action_report);

    // Disable following items by default and enable them if the user has the right access.
    actionEdit.setVisible(false);
    actionDelete.setVisible(false);
    actionReport.setVisible(true);

    // If the logged user is the admin
    if (mPost.getSender().getId().equals(Logged.Models.getUserShop() != null ? Logged.Models.getUserShop().getId() : "")
      || mPost.getSender().getId().equals(Logged.Models.getUserProfile().getId())
      || mPost.getSender().getId().equals(Logged.Models.getUserComplex() != null ? Logged.Models.getUserComplex().getId() : "")
      || mPost.getSenderShop() != null && mPost.getSenderShop().getManagersId().contains(Logged.Models.getUserProfile().getId())
      || mPost.getSenderComplex() != null && mPost.getSenderComplex().getManagersId().contains(Logged.Models.getUserProfile().getId()))
    {
      actionEdit.setVisible(true);
      actionDelete.setVisible(true);
      actionReport.setVisible(false);
    }
  }

  @OnClick (R.id.image_button_post_comment)
  public void postComment() {
    fragmentSView.setVisibility(View.GONE);

    if (mEditTextComment.getText().toString().trim().length() == 0)
    {
      return;
    }

    mImageButtonPostComment.setEnabled(false);

    final Comment commentToPost = new Comment();
    commentToPost.setSender(Logged.Models.getUserProfile());
    String comment = mEditTextComment.getText().toString().trim();
    for (int i = 0; i < mentionsUser.size(); i++)
    {
      comment = comment.replaceFirst("\\" + mentionsUser.get(i), mentionsEncrypt.get(i));
    }
    commentToPost.setText(comment);
    mentionsUser.clear();
    commentToPost.setPostId(mPost.getId());

    HttpClient.post(getContext(), Constants.Server.Comment.POST, new Gson().toJson(commentToPost, Comment.class), "application/json", new AsyncTextHttpResponser(getContext()) {
      @Override
      public void onFailure(int statusCode,
                            Header[] headers,
                            String responseString,
                            Throwable throwable) {
        super.onFailure(statusCode, headers, responseString, throwable);

        Toast.makeText(getContext(), getString(R.string.toast_error_post_comment), Toast.LENGTH_SHORT)
          .show();

        mImageButtonPostComment.setEnabled(true);
      }

      @Override
      public void onSuccess(int statusCode, Header[] headers, String responseString) {
        mentionsUser.clear();
        mentionsEncrypt.clear();
        commentToPost.setId(Utils.removeExtraQuotations(responseString));
        mEditTextComment.setText("");

        mAdapter.addComment(commentToPost);
        mRecyclerView.scrollVerticallyToPosition(1);

        mImageButtonPostComment.setEnabled(true);

        // Increase comments count
        if (!Utils.isNullOrEmpty(mPost.getCommentsCount()))
        {
          int commentsCount = Integer.valueOf(mPost.getCommentsCount());
          mPost.setCommentsCount(Integer.toString(commentsCount + 1));
          mAdapter.notifyPostBinder(mPost);
        } else
        {
          mPost.setCommentsCount("1");
        }
      }
    });
  }

}
