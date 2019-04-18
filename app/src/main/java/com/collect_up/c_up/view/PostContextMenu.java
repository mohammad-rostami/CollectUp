/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Utils;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostContextMenu<T> extends LinearLayout {
    private static final int CONTEXT_MENU_WIDTH = Utils.dpToPx(200);

    private int feedItem = -1;
    private OnFeedContextMenuItemClickListener onItemClickListener;
    private String postId;
    private T post;

    public PostContextMenu(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_post_context_menu, this, true);
        setBackgroundResource(R.drawable.bg_container_shadow);
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(CONTEXT_MENU_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void bindToItem(int feedItem, String postId, T post) {
        this.feedItem = feedItem;
        this.postId = postId;
        this.post = post;
    }

    public void dismiss() {
        ((ViewGroup) getParent()).removeView(PostContextMenu.this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnReject)
    public void onCancelClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onPostCancelClick();
        }
    }

    @OnClick(R.id.btnReport)
    public void onReportClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onReportClick(postId, feedItem);
            PostContextMenuManager.getInstance().hideContextMenu();
        }
    }

    @OnClick(R.id.btnShareOnProfile)
    public void onShareOnProfile() {
        if (onItemClickListener != null) {
            onItemClickListener.onShareOnProfile(feedItem, post);
            PostContextMenuManager.getInstance().hideContextMenu();
        }
    }

    @OnClick(R.id.btnShareToChats)
    public void onShareToChats() {
        if (onItemClickListener != null) {
            onItemClickListener.onShareToChats(feedItem, post);
            PostContextMenuManager.getInstance().hideContextMenu();
        }
    }

    public void setOnFeedMenuItemClickListener(OnFeedContextMenuItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnFeedContextMenuItemClickListener {
        void onPostCancelClick();

        void onReportClick(String postId, int feedItem);

        <T> void onShareOnProfile(int feedItem, T post);

        <T> void onShareToChats(int feedItem, T post);
    }
}