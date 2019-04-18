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

public class EventContextMenu extends LinearLayout {
    private static final int CONTEXT_MENU_WIDTH = Utils.dpToPx(200);

    private int feedItem = - 1;
    private String eventId;

    private OnFeedContextMenuItemClickListener onItemClickListener;

    public EventContextMenu(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_event_context_menu, this, true);
        setBackgroundResource(R.drawable.bg_container_shadow);
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(CONTEXT_MENU_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void bindToItem(int feedItem, String eventId) {
        this.feedItem = feedItem;
        this.eventId = eventId;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ButterKnife.bind(this);
    }

    public void dismiss() {
        ((ViewGroup) getParent()).removeView(EventContextMenu.this);
    }

    @OnClick (R.id.btnDelete)
    public void onSharePhotoClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onDeleteClick(feedItem, eventId);
            EventContextMenuManager.getInstance().hideContextMenu();
        }
    }

    @OnClick (R.id.btnReject)
    public void onCancelClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onEventCancelClick();
        }
    }

    public void setOnFeedMenuItemClickListener(OnFeedContextMenuItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnFeedContextMenuItemClickListener {

        void onDeleteClick(int itemPosition, String eventId);

        void onEventCancelClick();
    }
}