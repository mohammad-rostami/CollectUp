/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.github.meness.timelinepostcontainer.Listeners;
import io.github.meness.timelinepostcontainer.TimelinePostContainer;
import io.github.meness.timelinepostcontainer.Type;
import io.github.meness.timelinepostcontainer.interfaces.ITapListener;

public class ComplexTimelineAdapter extends UltimateViewAdapter<ComplexTimelineAdapter.Holder>
  implements View.OnClickListener, ITapListener {

  private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
  private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
  private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

  private final Context context;
  private final Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimations = new HashMap<>();
  private final ArrayList<Integer> likedPositions = new ArrayList<>();
  private final String mCurrentProfileId = Logged.Models.getUserProfile()
    .getId();
  private List<Post> mPosts = new ArrayList<>();
  private OnFeedItemClickListener onFeedItemClickListener;

  public ComplexTimelineAdapter(Context context, List<Post> postList) {
    this.context = context;
    mPosts = postList;
  }

  public void updatePost(Post newPost, boolean deleted) {
    for (Post post : mPosts)
    {
      if (post.getId().equals(newPost.getId()))
      {
        int pos = mPosts.indexOf(post);
        if (deleted)
        {
          mPosts.remove(pos);
          notifyItemRemoved(pos);
        } else
        {
          mPosts.set(pos, newPost);
          notifyItemChanged(pos);
        }
        break;
      }
    }
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.fragment_timeline_tab, viewGroup, false);
    Holder holder = new Holder(view, true);

    holder.btnComments.setOnClickListener(this);
    holder.btnMore.setOnClickListener(this);
    Listeners listeners = new Listeners();
    listeners.tap = this;
    holder.mTimelinePostContainer.setListeners(listeners);
    holder.mTimelinePostContainer.setImageLoader(MyApplication.getInstance().getImageLoader());
    holder.likeImageButton.setOnClickListener(this);
    holder.likesContainer.setOnClickListener(this);
    holder.totalComments.setOnClickListener(this);
    holder.btnOverflow.setOnClickListener(this);
    holder.btnTag.setOnClickListener(this);

    return holder;
  }


  @Override
  public int getItemCount() {
    super.getItemCount();
    return mPosts.size();
  }

  @Override
  public int getAdapterItemCount() {
    return mPosts.size();
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public void onBindViewHolder(Holder viewHolder, int position) {
    if (viewHolder.totalComments == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      viewHolder.itemView.setLayoutParams(params1);

      return;
    }

    bindFeedItem(position, viewHolder);
  }

  private void bindFeedItem(int position, final Holder holder) {
    final Post item = mPosts.get(position);
    holder.btnOverflow.setTag(item);
    holder.btnFollow.setVisibility(View.GONE);
    if (item.getProfileTags() != null && item.getProfileTags().size() > 0)
    {
      holder.peopleHolder.setVisibility(View.VISIBLE);
      Hashtable<ComplexTimelineAdapter.Holder, Post> hashtableTagPeople = new Hashtable<>();
      hashtableTagPeople.put(holder, item);
      holder.peopleHolder.setTag(hashtableTagPeople);
    } else
    {
      holder.peopleHolder.setVisibility(View.GONE);

    }
    holder.nameTextView.setText(item.getSender().getName());
    holder.txtViews.setText(Utils.getReadableCount(item.getViewCount()));

    int likesCount = item.getLikes().size();
    holder.likesTextView.setText(Utils.getReadableCount(likesCount));

    if (!Utils.isNullOrEmpty(item.getSender().getImageAddress()))
    {
      holder.profilePicture.makeAllDefaults();
      holder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), true).setText(item.getSender().getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + item.getSender().getThumb());
    } else
    {
      holder.profilePicture.makeAllDefaults();
      holder.profilePicture.setText(item.getSender().getName());
    }
    holder.profilePicture.setTag(item.getSender());

    if (!Utils.isNullOrEmpty(item.getCommentsCount()))
    {
      holder.totalComments.setText(Utils.getReadableCount(Long.valueOf(item.getCommentsCount())));
    } else
    {
      holder.totalComments.setText("0");
    }

    holder.postDateTime.setText(TimeHelper.getTimeAgo(context, TimeHelper.utcToTimezone(context, item
      .getInsertTime())));

    if (!Utils.isNullOrEmpty(item.getVideoAddress()))
    {
      holder.mTimelinePostContainer.setVisibility(View.VISIBLE);
      holder.mTimelinePostContainer.setSize(item.getSize());

      holder.mTimelinePostContainer.setImagePath(Constants.General.BLOB_PROTOCOL + item.getImageAddress());
      holder.mTimelinePostContainer.setVideoPath(Constants.General.BLOB_PROTOCOL + item.getVideoAddress());
      holder.mTimelinePostContainer.setVideoProperties(item.getVideoProperties());

      holder.mTimelinePostContainer.build(Type.VIDEO);
    } else if (!Utils.isNullOrEmpty(item.getImageAddress()))
    {
      holder.imageCC.setVisibility(View.VISIBLE);
      holder.mTimelinePostContainer.setVisibility(View.VISIBLE);
      holder.mTimelinePostContainer.setSize(item.getSize());

      holder.mTimelinePostContainer.setImagePath(Constants.General.BLOB_PROTOCOL + item.getImageAddress());
      holder.mTimelinePostContainer.build(Type.IMAGE);
    }
    // No video and no Image
    else
    {
      holder.mTimelinePostContainer.setVisibility(View.GONE);
    }

    LinearLayout.LayoutParams itemViewParams = (LinearLayout.LayoutParams) holder.expandableTextView.getLayoutParams();
    itemViewParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
    holder.expandableTextView.setLayoutParams(itemViewParams);

    if (!Utils.isNullOrEmpty(item.getText()))
    {
      SpannableString textSpannable = SepehrUtil.decryptMention(context, item.getText(), false);

      holder.expandableTextView.setText(textSpannable);
      ((TextView) (holder.expandableTextView.getChildAt(0))).setTransformationMethod(new LinkTransformationMethod((Activity) context));
      ((TextView) (holder.expandableTextView.getChildAt(0))).setMovementMethod(LinkMovementMethod.getInstance());
      holder.expandableTextView.setVisibility(View.VISIBLE);
    } else
    {
      holder.expandableTextView.setVisibility(View.GONE);
    }

    holder.btnComments.setTag(item);
    holder.totalComments.setTag(item);

    holder.likesTextView.setText(Integer.toString(item.getLikes().size()));

    holder.footerContainer.setTag(item);

    HashMap<Post, Integer> hashMap = new HashMap<>();
    hashMap.put(item, position);

    holder.btnMore.setTag(hashMap);

    Hashtable<Holder, Post> hashtagPost = new Hashtable<>();
    hashtagPost.put(holder, item);
    holder.mTimelinePostContainer.setTag(hashtagPost);

    Hashtable<Holder, String> hashtableTag = new Hashtable<>();
    hashtableTag.put(holder, "false");
    holder.likeImageButton.setTag(hashtableTag);

    if (item.getLikes().contains(mCurrentProfileId))
    {
      hashtableTag.put(holder, "true");
      holder.likeImageButton.setTag(hashtableTag);
      holder.likeImageButton.setImageResource(R.drawable.ic_heart);
    } else
    {
      holder.likeImageButton.setImageResource(R.drawable.ic_heart_outline);
    }

    holder.likesContainer.setTag(item.getId());

    if (likeAnimations.containsKey(holder))
    {
      likeAnimations.get(holder).cancel();
    }
    resetLikeAnimationState(holder);
  }

  private void resetLikeAnimationState(Holder holder) {
    likeAnimations.remove(holder);
    holder.vBgLike.setVisibility(View.GONE);
    holder.ivLike.setVisibility(View.GONE);
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
    this.onFeedItemClickListener = onFeedItemClickListener;
  }

  private void unlikePost(final Holder holder, final Post post) {

    holder.likeImageButton.setEnabled(false);

    updateUnlikes(holder);

    HttpClient.get(String.format(Constants.Server.Post.GET_UNLIKE_POST, post.getId()), new AsyncHttpResponser(context) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        holder.likeImageButton.setEnabled(true);

        Post newPost = GsonParser.getObjectFromGson(responseBody, Post.class);
        updatePostsObject(newPost);
        updateTags(holder, newPost, false);
      }

      @Override
      public void onFailure(int statusCode,
                            Header[] headers,
                            byte[] responseBody,
                            Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

        holder.likeImageButton.setEnabled(true);

        Toast.makeText(context, context.getString(R.string.toast_error_unlike), Toast.LENGTH_SHORT)
          .show();
        updateLikesCount(holder);
      }
    });
  }

  @Override
  public void onClick(final View view) {
    switch (view.getId())
    {
      case R.id.image_button_comments:
      case R.id.text_view_total_comments:
        Post post = (Post) view.getTag();
        if (onFeedItemClickListener != null)
        {
          onFeedItemClickListener.onCommentsClick(view, post);
        }
        break;
      case R.id.image_button_more:
        HashMap<Post, Integer> tag = (HashMap<Post, Integer>) view.getTag();
        Post Post = tag.keySet().iterator().next();
        int position = tag.values().iterator().next();
        if (onFeedItemClickListener != null)
        {
          onFeedItemClickListener.onTimelineMoreClick(view, Post.getId(), Post, position);
        }
        break;
      case R.id.image_button_like_small:
        Post post2 = (Post) ((LinearLayout) view.getParent()).getTag();
        final Hashtable hashTags = (Hashtable<Holder, String>) view.getTag();
        Holder holder = (Holder) hashTags.keys().nextElement();
        String isLiked = (String) hashTags.values().iterator().next();
        if (isLiked.equals("false"))
        {
          likePost(holder, post2);
        } else
        {
          unlikePost(holder, post2);
        }
        break;
      case R.id.linear_layout_post_likes:
        String postId = (String) view.getTag();
        if (onFeedItemClickListener != null)
        {
          onFeedItemClickListener.onLikesCountClick(postId);
        }
        break;
      case R.id.btnOverflow:
        showPupupMore(view, (Post) view.getTag());
        break;
      case R.id.btnTag:
        final Hashtable<ComplexTimelineAdapter.Holder, Post> tagPeopleMap = (Hashtable<ComplexTimelineAdapter.Holder, Post>) ((FrameLayout) view.getParent()).getTag();
        final ComplexTimelineAdapter.Holder btnTagPeople = tagPeopleMap.keys().nextElement();
        final Post postData = tagPeopleMap.values().iterator().next();

        if (view.getTag() == null)
        {
          view.setTag("showed");
          for (int i = 0; i < postData.getProfileTags().size(); i++)
          {
            SepehrUtil.addPeople(context, btnTagPeople.peopleHolder, postData.getProfileTags().get(i));

          }
        } else
        {
          for (int i = 0; i < (btnTagPeople.peopleHolder).getChildCount(); i++)
          {
            if (btnTagPeople.peopleHolder.getChildAt(i) instanceof RelativeLayout)
            {
              btnTagPeople.peopleHolder.removeViewAt(i);
              i = 0;
            }
          }
          view.setTag(null);
        }
        break;
    }
  }


  private void animatePhotoLike(final Holder holder) {
    holder.vBgLike.setVisibility(View.VISIBLE);
    holder.ivLike.setVisibility(View.VISIBLE);

    holder.vBgLike.setScaleY(0.5f);
    holder.vBgLike.setScaleX(0.5f);
    holder.vBgLike.setAlpha(1f);
    holder.vBgLike.setLayoutParams(new FrameLayout.LayoutParams(holder.mTimelinePostContainer.getHeight(), holder.mTimelinePostContainer.getHeight(), Gravity.CENTER));
    holder.ivLike.setScaleY(0.1f);
    holder.ivLike.setScaleX(0.1f);
    holder.ivLike.setColorFilter(Color.argb(255, 255, 255, 255));

    AnimatorSet animatorSet = new AnimatorSet();

    ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
    bgScaleYAnim.setDuration(200);
    bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
    ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
    bgScaleXAnim.setDuration(200);
    bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
    ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
    bgAlphaAnim.setDuration(200);
    bgAlphaAnim.setStartDelay(150);
    bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

    ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
    imgScaleUpYAnim.setDuration(300);
    imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
    ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
    imgScaleUpXAnim.setDuration(300);
    imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

    ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
    imgScaleDownYAnim.setDuration(300);
    imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
    ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
    imgScaleDownXAnim.setDuration(300);
    imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

    animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
    animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        resetLikeAnimationState(holder);
      }
    });
    animatorSet.start();
  }

  private void likePost(final Holder holder, final Post post) {
    holder.likeImageButton.setEnabled(false);

    updateLikesCount(holder);

    HttpClient.get(String.format(Constants.Server.Post.GET_LIKE_POST, post.getId()), new AsyncHttpResponser(context) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        holder.likeImageButton.setEnabled(true);

        Post newPost = GsonParser.getObjectFromGson(responseBody, Post.class);
        updatePostsObject(newPost);
        updateTags(holder, newPost, true);
      }

      @Override
      public void onFailure(int statusCode,
                            Header[] headers,
                            byte[] responseBody,
                            Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

        holder.likeImageButton.setEnabled(true);
        Toast.makeText(context, context.getString(R.string.toast_error_like), Toast.LENGTH_SHORT)
          .show();
        updateUnlikes(holder);
      }
    });
  }

  private void updateLikesCount(final Holder holder) {
    int likesCount = Integer.parseInt(holder.likesTextView.getText().toString()) + 1;
    holder.likesTextView.setText(Integer.toString(likesCount));
    updateHeartButton(holder, true);
    Hashtable<Holder, String> hashTags = new Hashtable<>();
    hashTags.put(holder, "true");
    holder.likeImageButton.setTag(hashTags);
    holder.likeImageButton.setImageResource(R.drawable.ic_heart);
  }

  private void updatePostsObject(Post newPost) {
    mPosts.set(getPostPosition(newPost.getId()), newPost);
  }

  private void updateTags(Holder holder, Post post, boolean liked) {
    holder.btnComments.setTag(post);
    holder.footerContainer.setTag(post);

    Hashtable<Holder, Post> holderAndPostTag = new Hashtable<>();
    holderAndPostTag.put(holder, post);

    holder.mTimelinePostContainer.setTag(holderAndPostTag);

    Hashtable<Holder, String> holderAndBoolean = new Hashtable<>();
    holderAndBoolean.put(holder, Boolean.toString(liked).toLowerCase());

    holder.likeImageButton.setTag(holderAndBoolean);
  }

  private void updateUnlikes(final Holder holder) {
    int likesCount = Integer.parseInt(holder.likesTextView.getText().toString()) - 1;
    holder.likesTextView.setText(Integer.toString(likesCount));
    holder.likeImageButton.setImageResource(R.drawable.ic_heart_outline);
    Hashtable<Holder, String> hashTags = new Hashtable<>();
    hashTags.put(holder, "false");
    holder.likeImageButton.setTag(hashTags);
  }

  private void updateHeartButton(final Holder holder, boolean animated) {
    if (animated)
    {
      if (!likeAnimations.containsKey(holder))
      {
        AnimatorSet animatorSet = new AnimatorSet();
        likeAnimations.put(holder, animatorSet);

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.likeImageButton, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.likeImageButton, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.likeImageButton, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            holder.likeImageButton.setImageResource(R.drawable.ic_heart);
          }
        });

        animatorSet.play(rotationAnim);
        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

        animatorSet.addListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            resetLikeAnimationState(holder);
          }
        });

        animatorSet.start();
      }
    } else
    {
      if (likedPositions.contains(holder.getPosition()))
      {
        holder.likeImageButton.setImageResource(R.drawable.ic_heart);
      } else
      {
        holder.likeImageButton.setImageResource(R.drawable.ic_heart_outline);
      }
    }
  }

  private int getPostPosition(String postId) {
    int pos = -1;
    for (Post post : mPosts)
    {
      if (post.getId().equals(postId))
      {
        pos = mPosts.indexOf(post);
      }
    }

    return pos;
  }

  @Override
  public <T> void onDoubleTap(MotionEvent e, Type type, T parentTag) {
    final Hashtable postPictureTags = (Hashtable<Holder, Post>) parentTag;
    Holder postPictureHolder = (Holder) postPictureTags.keys().nextElement();
    Post postPicturePost = (Post) postPictureTags.values()
      .iterator()
      .next();
    Hashtable<Holder, String> btnLikeTags = (Hashtable<Holder, String>) postPictureHolder.likeImageButton
      .getTag();

    if (btnLikeTags.values().iterator().next().equals("false"))
    {
      animatePhotoLike(postPictureHolder);
      likePost(postPictureHolder, postPicturePost);
    }
  }

  @Override
  public void onSingleTap(MotionEvent e, Type type) {

  }

  private void checkPrivileges(Menu menu, Post post) {

    MenuItem actionEdit = menu.findItem(R.id.action_edit);
    MenuItem actionDelete = menu.findItem(R.id.action_delete);
    MenuItem actionReport = menu.findItem(R.id.action_report);

    // Disable following items by default and enable them if the user has the right access.
    actionEdit.setVisible(false);
    actionDelete.setVisible(false);
    actionReport.setVisible(true);

    // If the logged user is the admin
    if (post.getSender().getId().equals(Logged.Models.getUserShop() != null ? Logged.Models.getUserShop().getId() : "")
      || post.getSender().getId().equals(Logged.Models.getUserProfile().getId())
      || post.getSender().getId().equals(Logged.Models.getUserComplex() != null ? Logged.Models.getUserComplex().getId() : "")
      || post.getSenderShop() != null && post.getSenderShop().getManagersId().contains(Logged.Models.getUserProfile().getId())
      || post.getSenderComplex() != null && post.getSenderComplex().getManagersId().contains(Logged.Models.getUserProfile().getId()))
    {
      actionEdit.setVisible(true);
      actionDelete.setVisible(true);
      actionReport.setVisible(false);
    }
  }

  public void showPupupMore(View view, final Post post) {
    PopupMenu popupMenu = new PopupMenu(context, view);
    MenuInflater inflater = popupMenu.getMenuInflater();
    inflater.inflate(R.menu.menu_post_display, popupMenu.getMenu());
    popupMenu.show();
    checkPrivileges(popupMenu.getMenu(), post);
    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId())
        {
          case R.id.action_edit:
            FragmentHandler.replaceFragment(context, fragmentType.EDITPOST, post);
            break;
          case R.id.action_delete:
            final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(context);
            builder.message(R.string.sure_to_delete_post)
              .messageTextColor(ContextCompat.getColor(context, R.color.primary_text))
              .title(R.string.delete_confirmation)
              .titleColor(ContextCompat.getColor(context, R.color.colorAccent))
              .positiveAction(R.string.im_sure)
              .negativeAction(R.string.cancel)
              .actionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
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
                HttpClient.delete(String.format(Constants.Server.Post.DELETE_BY_ID, post
                  .getId()), new AsyncHttpResponser(context) {
                  @Override
                  public void onSuccess(int statusCode,
                                        Header[] headers,
                                        byte[] responseBody) {
                    builder.dismiss();
                    updatePost(post, true);

                  }

                  @Override
                  public void onFailure(int statusCode,
                                        Header[] headers,
                                        byte[] responseBody,
                                        Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                    Toast.makeText(context, context.getString(R.string.toast_error_deleting_post), Toast.LENGTH_SHORT)
                      .show();
                  }
                });
              }
            });

            builder.show();
            break;
          case R.id.action_report:
            final com.rey.material.app.SimpleDialog builder1 = new com.rey.material.app.SimpleDialog(context);
            builder1.message(R.string.sure_to_report_post)
              .messageTextColor(ContextCompat.getColor(context, R.color.primary_text))
              .title(R.string.report_post)
              .titleColor(ContextCompat.getColor(context, R.color.colorAccent))
              .positiveAction(R.string.action_report)
              .negativeAction(R.string.cancel)
              .actionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
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
                String f = String.format(Constants.Server.Post.GET_REPORT, post.getId());
                HttpClient.get(f, new AsyncHttpResponser(context) {
                  @Override
                  public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Toast.makeText(context, context.getString(R.string.toast_successful_report), Toast.LENGTH_SHORT)
                      .show();
                  }

                  @Override
                  public void onFailure(int statusCode,
                                        Header[] headers,
                                        byte[] responseBody,
                                        Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                    Toast.makeText(context, context.getString(R.string.toast_error_report), Toast.LENGTH_SHORT)
                      .show();
                  }
                });
              }
            });
            builder1.show();
        }
        return false;
      }
    });

  }

  public interface OnFeedItemClickListener {
    void onCommentsClick(View v, Post post);

    <T> void onTimelineMoreClick(View view, String postId, T post, int position);

    void onLikesCountClick(String postId);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.image_button_comments)
    ImageButton btnComments;
    @Bind (R.id.image_button_like_small)
    ImageButton likeImageButton;
    @Bind (R.id.image_button_more)
    ImageButton btnMore;
    @Bind (R.id.vBgLike)
    View vBgLike;
    @Bind (R.id.ivLike)
    ImageView ivLike;
    @Bind (R.id.text_view_name)
    TextView nameTextView;
    @Bind (R.id.image_view_picture)
    ComplexAvatarView profilePicture;
    @Bind (R.id.text_view_likes)
    TextView likesTextView;
    @Bind (R.id.linear_layout_footer_container)
    LinearLayout footerContainer;
    @Bind (R.id.linear_layout_post_likes)
    LinearLayout likesContainer;
    @Bind (R.id.text_view_datetime)
    TextView postDateTime;
    @Bind (R.id.image_cc)
    FrameLayout imageCC;
    @Bind (R.id.timelinePostContainer)
    TimelinePostContainer mTimelinePostContainer;
    @Bind (R.id.text_view_total_comments)
    TextView totalComments;
    @Bind (R.id.expand_text_view)
    ExpandableTextView expandableTextView;
    @Bind (R.id.txtViews)
    TextView txtViews;
    @Bind (R.id.btnFollow)
    Button btnFollow;
    @Bind (R.id.btnOverflow)
    ImageButton btnOverflow;
    @Bind (R.id.btnTag)
    ImageView btnTag;
    @Bind (R.id.peopleHolder)
    FrameLayout peopleHolder;

    public Holder(View view, boolean isItem) {
      super(view);
      if (isItem)
      {
        ButterKnife.bind(this, view);
      }
    }
  }
}
