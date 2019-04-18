/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters.binders;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.PostDisplayAdapter;
import com.collect_up.c_up.fragments.FragmentPostDisplay;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Comment;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.multiViewTypes.DataBinder;

import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class PostCommentsBinder extends DataBinder<PostCommentsBinder.ViewHolder>
  implements View.OnClickListener, View.OnLongClickListener {
  private final FragmentPostDisplay mFragmentPost;
  private final PostDisplayAdapter mDataBindAdapter;
  List<Comment> dataSet;
  int pageNumber = 2;
  private Post mPost;

  public PostCommentsBinder(UltimateDifferentViewTypeAdapter dataBindAdapter,
                            List<Comment> comments, Post post,
                            FragmentPostDisplay context) {
    super(dataBindAdapter);

    mDataBindAdapter = (PostDisplayAdapter) dataBindAdapter;
    dataSet = comments;
    mFragmentPost = context;
    mPost = post;
  }

  public void updatePost(Post post) {
    mPost = post;
    notifyBinderDataSetChanged();
  }

  @Override
  public ViewHolder newViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.inf_comment, parent, false);

    ViewHolder viewHolder = new ViewHolder(view);
    viewHolder.name.setOnClickListener(this);
    viewHolder.picture.setOnClickListener(this);
    viewHolder.itemView.setOnLongClickListener(this);
    viewHolder.commentTextClickable.setOnLongClickListener(this);

    return viewHolder;
  }

  @Override
  public void bindViewHolder(ViewHolder holder, int position) {
    Comment item = dataSet.get(position - 1);

    if (item != null)
    {


      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.picture.getLayoutParams();
      params.leftMargin = 0;
      holder.picture.setLayoutParams(params);

      holder.dateTime.setVisibility(View.VISIBLE);
      holder.commentTextClickable.setVisibility(View.VISIBLE);
      Linkify.addLinks(holder.commentTextClickable, Linkify.WEB_URLS);

      holder.name.setVisibility(View.VISIBLE);

      holder.picture.setOnClickListener(this);

      holder.itemView.setTag(item);
      holder.commentTextClickable.setTag(item);

      if (!Utils.isNullOrEmpty(item.getInsertTime()))
      {
        Date insertDate = new Date(TimeHelper.utcToTimezone(mFragmentPost.getContext(), item.getInsertTime()));
        String timeAgo = TimeHelper.getTimeAgo(mFragmentPost.getContext(), insertDate.getTime());
        holder.dateTime.setText(timeAgo);
        holder.itemView.setBackgroundColor(Color.WHITE);
      } else
      {
        holder.dateTime.setText(mFragmentPost.getString(R.string.just_now));
        holder.itemView.setBackgroundColor(mFragmentPost.getResources().getColor(R.color.comment_new_color));
      }

      //////////////////////////////////////////
      SpannableString textSpannable = SepehrUtil.decryptMention(mFragmentPost.getContext(), item.getText(), true);

      //////////////////////////////////////////////

      holder.commentTextClickable.setText(textSpannable);
      holder.commentTextClickable.setTransformationMethod(new LinkTransformationMethod(mFragmentPost.getActivity()));
      holder.commentTextClickable.setMovementMethod(LinkMovementMethod.getInstance());

      if (Utils.isNullOrEmpty(item.getSender().getImageAddress()))
      {
        holder.picture.makeAllDefaults();
        holder.picture.setText(item.getSender().getName());
      } else
      {
        holder.picture.makeAllDefaults();
        holder.picture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(item.getSender().getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + item.getSender().getThumb());
      }

      holder.name.setText(item.getSender().getName());

      holder.picture.setTag(item.getSender());
      holder.name.setTag(item.getSender());
    } else
    {
      holder.itemView.setBackgroundColor(Color.WHITE);

      holder.picture.makeAllDefaults();
      holder.picture.setDefaultImage(R.drawable.circle_add_more);

      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.picture.getLayoutParams();
      params.leftMargin = Utils.dpToPx((mFragmentPost.getResources().getConfiguration().screenWidthDp / 2) - 40);
      holder.picture.setLayoutParams(params);

      holder.name.setVisibility(View.GONE);
      holder.commentTextClickable.setVisibility(View.GONE);
      holder.dateTime.setVisibility(View.GONE);

      holder.picture.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          ((FragmentPostDisplay) mFragmentPost).loadCommentPages(pageNumber);
          pageNumber++;
        }
      });
    }

  }

  @Override
  public int getItemCount() {
    return dataSet.size();
  }

  @Override
  public boolean onLongClick(View v) {
    final Comment comment = (Comment) v.getTag();
    final int position = dataSet.indexOf(comment);

    if (comment.getSender().getId().equals(Logged.Models.getUserProfile().getId()) || mPost.getSender()
      .getId()
      .equals(Logged.Models.getUserProfile().getId()))
    {
      final com.rey.material.app.SimpleDialog dialog = new com.rey.material.app.SimpleDialog(mFragmentPost.getContext());
      dialog.message(mFragmentPost.getString(R.string.want_to_delete_comment))
        .messageTextColor(ContextCompat.getColor(mFragmentPost.getContext(), R.color.primary_text))
        .title(R.string.are_you_sure)
        .titleColor(ContextCompat.getColor(mFragmentPost.getContext(), R.color.colorAccent))
        .positiveAction(R.string.im_sure)
        .negativeAction(R.string.cancel)
        .actionTextColor(ContextCompat.getColor(mFragmentPost.getContext(), R.color.colorAccent))
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
          HttpClient.delete(String.format(Constants.Server.Comment.DELETE_BY_ID, comment
            .getId()), new AsyncHttpResponser(mFragmentPost.getContext()) {
            @Override
            public void onSuccess(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody) {
              dialog.dismiss();

              dataSet.remove(position);
              // plus 1 is because of the post itself position
              notifyBinderItemRemoved(position + 1);

              // Decrease comments count
              int commentsCount = Integer.valueOf(mPost.getCommentsCount());
              mPost.setCommentsCount(Integer.toString(commentsCount - 1));

              mDataBindAdapter.notifyPostBinder(mPost);
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);

              Toast.makeText(mFragmentPost.getContext(), mFragmentPost.getString(R.string.toast_error_deleting_comment), Toast.LENGTH_SHORT)
                .show();
            }
          });
        }
      });
      dialog.show();
    }

    return true;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.text_view_comment_name:
      case R.id.image_view_comment_profile_picture:
        Profile profile = (Profile) v.getTag();
        FragmentHandler.replaceFragment(mFragmentPost.getContext(), fragmentType.PROFILE, profile);

        break;
    }
  }

  static class ViewHolder extends UltimateRecyclerviewViewHolder {
    TextView name;
    ComplexAvatarView picture;
    TextView dateTime;
    TextView commentTextClickable;

    public ViewHolder(View view) {
      super(view);
      name = (TextView) view.findViewById(R.id.text_view_comment_name);
      picture = (ComplexAvatarView) view.findViewById(R.id.image_view_comment_profile_picture);
      dateTime = (TextView) view.findViewById(R.id.text_view_datetime);
      commentTextClickable = (TextView) view.findViewById(R.id.expandable_text);
    }
  }


}
