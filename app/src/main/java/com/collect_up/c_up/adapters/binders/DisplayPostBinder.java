/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters.binders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentShare;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.view.CircledNetworkImageView;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.collect_up.c_up.view.PostContextMenu;
import com.collect_up.c_up.view.PostContextMenuManager;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.multiViewTypes.DataBinder;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.rey.material.app.BottomSheetDialog;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import io.github.meness.timelinepostcontainer.TimelinePostContainer;
import io.github.meness.timelinepostcontainer.Type;


public class DisplayPostBinder extends DataBinder<DisplayPostBinder.Holder>
        implements View.OnClickListener, PostContextMenu.OnFeedContextMenuItemClickListener {
    private final Context mContext;
    Post dataSet;

    public DisplayPostBinder(UltimateDifferentViewTypeAdapter dataBindAdapter,
                             Post post,
                             Context context) {
        super(dataBindAdapter);
        this.dataSet = post;
        this.mContext = context;
    }

    public void updatePost(Post post) {
        dataSet = post;
        notifyBinderDataSetChanged();
    }

    private void like(final Holder holder) {
        holder.smallLikeButton.setImageResource(R.drawable.ic_heart);
        holder.smallLikeButton.setEnabled(false);

        HashMap<Holder, Boolean> tag = new HashMap<>();
        tag.put(holder, true);

        holder.smallLikeButton.setTag(tag);
        holder.totalLikes.setText(Integer.toString(Integer.parseInt(holder.totalLikes.getText().toString()) + 1));

        HttpClient.get(String.format(Constants.Server.Post.GET_LIKE_POST, dataSet.getId()), new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                holder.smallLikeButton.setEnabled(true);
                dataSet.getLikes().add(Logged.Models.getUserProfile().getId());
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                holder.smallLikeButton.setEnabled(true);
                holder.smallLikeButton.setImageResource(R.drawable.ic_heart_outline);
                HashMap<Holder, Boolean> tag = new HashMap<>();
                tag.put(holder, false);
                holder.smallLikeButton.setTag(tag);
                holder.totalLikes.setText(Integer.toString(Integer.parseInt(holder.totalLikes.getText().toString()) - 1));
            }
        });
    }

    private void likeOrUnlike(Holder holder, boolean liked) {
        if (liked) {
            unlike(holder);
        } else {
            like(holder);
        }
    }

    private void unlike(final Holder holder) {
        holder.smallLikeButton.setImageResource(R.drawable.ic_heart_outline);
        holder.smallLikeButton.setEnabled(false);

        HashMap<Holder, Boolean> tag = new HashMap<>();
        tag.put(holder, false);

        holder.smallLikeButton.setTag(tag);
        holder.totalLikes.setText(Integer.toString(Integer.parseInt(holder.totalLikes.getText().toString()) - 1));

        HttpClient.get(String.format(Constants.Server.Post.GET_UNLIKE_POST, dataSet.getId()), new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                holder.smallLikeButton.setEnabled(true);
                dataSet.getLikes().remove(Logged.Models.getUserProfile().getId());
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                holder.smallLikeButton.setEnabled(true);
                holder.smallLikeButton.setImageResource(R.drawable.ic_heart);
                HashMap<Holder, Boolean> tag = new HashMap<>();
                tag.put(holder, true);
                holder.smallLikeButton.setTag(tag);
                holder.totalLikes.setText(Integer.toString(Integer.parseInt(holder.totalLikes.getText()
                        .toString()) + 1));
            }
        });
    }

    @Override
    public Holder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_display, parent, false);

        Holder holder = new Holder(view);

        holder.smallLikeButton.setOnClickListener(this);
        holder.mLikesContainer.setOnClickListener(this);
        holder.senderName.setOnClickListener(this);
        holder.senderPicture.setOnClickListener(this);
        holder.moreButton.setOnClickListener(this);
        holder.btnTag.setOnClickListener(this);

        return holder;
    }


    @Override
    public void bindViewHolder(final Holder holder, int position) {
        holder.txtViews.setText(Utils.getReadableCount(dataSet.getViewCount()));
        if (dataSet.getProfileTags() != null && dataSet.getProfileTags().size() > 0) {
            holder.btnTag.setVisibility(View.VISIBLE);
        } else {
            holder.btnTag.setVisibility(View.GONE);

        }
        if (dataSet.getLikes() != null) {
            holder.totalLikes.setText(Utils.getReadableCount(dataSet.getLikes().size()));
        } else {
            holder.totalLikes.setText("0");
        }

        if (!Utils.isNullOrEmpty(dataSet.getCommentsCount())) {
            holder.totalComments.setText(Utils.getReadableCount(Long.valueOf(dataSet.getCommentsCount())));
        } else {
            holder.totalComments.setText("0");
        }

        HashMap<Post, Integer> tag = new HashMap<>();
        tag.put(dataSet, position);

        holder.moreButton.setTag(tag);
        if (!Utils.isNullOrEmpty(dataSet.getSender().getImageAddress())) {
            MyApplication.getInstance().getImageLoader().loadImage(Constants.General.BLOB_PROTOCOL + (dataSet.getSender()).getThumb(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.senderPicture.setImageBitmap(loadedImage);
                }
            });
        } else {
            holder.senderPicture.setImageResource(R.drawable.placeholder);
        }


        if (!Utils.isNullOrEmpty(dataSet.getVideoAddress())) {
            holder.timelinePostContainer.setVisibility(View.VISIBLE);
            holder.timelinePostContainer.setImagePath(Constants.General.BLOB_PROTOCOL + dataSet.getImageAddress());
            holder.timelinePostContainer.setVideoPath(Constants.General.BLOB_PROTOCOL + dataSet.getVideoAddress());
            holder.timelinePostContainer.setSize(dataSet.getSize());
            holder.timelinePostContainer.setVideoProperties(dataSet.getVideoProperties());

            holder.timelinePostContainer.setImageLoader(MyApplication.getInstance().getImageLoader()).build(Type.VIDEO);
        } else if (!Utils.isNullOrEmpty(dataSet.getImageAddress())) {

            holder.timelinePostContainer.setVisibility(View.VISIBLE);
            holder.timelinePostContainer.setImagePath(Constants.General.BLOB_PROTOCOL + dataSet.getImageAddress());
            holder.timelinePostContainer.setSize(dataSet.getSize());

            holder.timelinePostContainer.setImageLoader(MyApplication.getInstance().getImageLoader()).build(Type.IMAGE);
        } else {
            holder.timelinePostContainer.setVisibility(View.GONE);
        }

        if (!Utils.isNullOrEmpty(dataSet.getText())) {
            holder.postText.setVisibility(View.VISIBLE);

            SpannableString textSpannable;
            //////////////////////////////////////////

            textSpannable = SepehrUtil.decryptMention(mContext, dataSet.getText(), false);

            holder.postText.setText(textSpannable);
            holder.postText.setTransformationMethod(new LinkTransformationMethod((Activity) mContext));
            holder.postText.setMovementMethod(LinkMovementMethod.getInstance());

        } else {
            holder.postText.setVisibility(View.GONE);
        }

        holder.senderName.setText(dataSet.getSender().getName());

        Date insertDate = new Date(TimeHelper.utcToTimezone(mContext, dataSet.getInsertTime()));
        String timeAgo = TimeHelper.getTimeAgo(mContext, insertDate.getTime());
        holder.postDateTime.setText(timeAgo);

        HashMap<Holder, Boolean> holderStringHashMap = new HashMap<>();
        if (dataSet.getLikes() != null) {
            boolean liked = dataSet.getLikes().contains(Logged.Models.getUserProfile().getId());
            holderStringHashMap.put(holder, liked);

            if (liked) {
                holder.smallLikeButton.setImageResource(R.drawable.ic_heart);
            } else {
                holder.smallLikeButton.setImageResource(R.drawable.ic_heart_outline);
            }

        } else {
            holderStringHashMap.put(holder, false);
            holder.smallLikeButton.setImageResource(R.drawable.ic_heart_outline);
        }

        holder.smallLikeButton.setTag(holderStringHashMap);
        holder.itemView.setTag(dataSet);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_button_like_small:
                HashMap<Holder, Boolean> tag = (HashMap<Holder, Boolean>) v.getTag();
                likeOrUnlike(tag.keySet().iterator().next(), tag.values().iterator().next());
                break;
            case R.id.linear_layout_post_likes:
                FragmentHandler.replaceFragment(mContext, fragmentType.LIKES, dataSet.getId());

                // mContext.startActivity(new Intent(mContext, FragmentPostLikes.class).putExtra("post_id", dataSet.getId()));
                break;
            case R.id.image_view_picture:
            case R.id.text_view_name:
                if (!dataSet.getSender().getId().equals(Logged.Models.getUserProfile().getId())) {

                    if (dataSet.getSenderProfile() != null) {
                        FragmentHandler.replaceFragment(mContext, fragmentType.PROFILE, dataSet.getSender());
                    } else if (dataSet.getSenderShop() != null) {
                        FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, dataSet.getSender());
                    } else if (dataSet.getSenderComplex() != null) {
                        FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, dataSet.getSender());
                    }


                }
                break;
            case R.id.image_button_more:
                HashMap<Post, Integer> moreTag = (HashMap<Post, Integer>) v.getTag();
                Post post = moreTag.keySet().iterator().next();
                int position = moreTag.values().iterator().next();
                onMoreClick(v, post.getId(), post, position);
                break;
            case R.id.btnTag:
                if (v.getTag() == null) {
                    v.setTag("showed");
                    for (int i = 0; i < dataSet.getProfileTags().size(); i++) {
                        SepehrUtil.addPeople(getContext(), (FrameLayout) v.getParent(), dataSet.getProfileTags().get(i));
                    }
                } else {
                    for (int i = 0; i < ((FrameLayout) v.getParent()).getChildCount(); i++) {
                        if (((FrameLayout) v.getParent()).getChildAt(i) instanceof RelativeLayout) {
                            ((FrameLayout) v.getParent()).removeViewAt(i);
                            i = 0;
                        }
                    }
                    v.setTag(null);
                }
                break;
        }
    }

    private void onMoreClick(View v, String postId, final Post post, final int position) {
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
    public void onPostCancelClick() {
        PostContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onReportClick(final String postId, int feedItem) {
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(mContext);
        builder.message(R.string.sure_to_report_post)
                .messageTextColor(ContextCompat.getColor(mContext, R.color.primary_text))
                .title(R.string.report_confirmation)
                .titleColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                .positiveAction(R.string.action_report)
                .negativeAction(R.string.cancel)
                .actionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
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
                HttpClient.get(f, new AsyncHttpResponser(mContext) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(mContext, mContext.getString(R.string.toast_successful_report), Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          byte[] responseBody,
                                          Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);

                        Toast.makeText(mContext, mContext.getString(R.string.toast_error_report), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });
        builder.show();
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public <T> void onShareOnProfile(int feedItem, final T post) {
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(mContext);
        builder.message(R.string.sure_to_share_post_on_profile)
                .messageTextColor(ContextCompat.getColor(mContext, R.color.primary_text))
                .title(R.string.share_confirmation)
                .titleColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                .positiveAction(R.string.share)
                .negativeAction(R.string.cancel)
                .actionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
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
                HttpClient.post(getContext(), Constants.Server.Post.POST, new Gson().toJson(newPost, Post.class), "application/json", new AsyncHttpResponser(mContext) {
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

                        Toast.makeText(getContext(), mContext.getString(R.string.toast_error_sharing_post), Toast.LENGTH_SHORT)
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
        message.setContentType(EnumMessageContentType.SharedPost);
        message.setText(((Post) post).getText());
        message.setSendDateTime(Long.toString(System.currentTimeMillis()));
        message.setContentSize(new Gson().toJson(post, Post.class));
        intent.putExtra("message", message);
        FragmentHandler.replaceFragment(getContext(), fragmentType.SHARE, intent);
    }

    static class Holder extends UltimateRecyclerviewViewHolder {
        CircledNetworkImageView senderPicture;
        TextView senderName;
        TextView postDateTime;
        TextView postText;
        TextView totalLikes;
        ImageButton smallLikeButton;
        TimelinePostContainer timelinePostContainer;
        TextView totalComments;
        TextView txtViews;
        ImageButton moreButton;
        LinearLayout mLikesContainer;
        ImageView btnTag;
        FrameLayout peopleHolder;

        public Holder(View view) {
            super(view);
            senderPicture = (CircledNetworkImageView) view.findViewById(R.id.image_view_picture);
            senderName = (TextView) view.findViewById(R.id.text_view_name);
            postDateTime = (TextView) view.findViewById(R.id.text_view_datetime);
            postText = (TextView) view.findViewById(R.id.text_view_text);
            totalLikes = (TextView) view.findViewById(R.id.text_view_total_likes);
            smallLikeButton = (ImageButton) view.findViewById(R.id.image_button_like_small);
            timelinePostContainer = (TimelinePostContainer) view.findViewById(R.id.timelinePostContainer);
            totalComments = (TextView) view.findViewById(R.id.text_view_total_comments);
            moreButton = (ImageButton) view.findViewById(R.id.image_button_more);
            mLikesContainer = (LinearLayout) view.findViewById(R.id.linear_layout_post_likes);
            txtViews = (TextView) view.findViewById(R.id.txtViews);
            btnTag = (ImageView) view.findViewById(R.id.btnTag);
            peopleHolder = (FrameLayout) view.findViewById(R.id.peopleHolder);
        }
    }
}
