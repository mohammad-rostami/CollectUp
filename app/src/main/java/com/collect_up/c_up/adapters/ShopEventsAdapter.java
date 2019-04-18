/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentBusiness;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Event;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.view.CircledNetworkImageView;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShopEventsAdapter extends UltimateViewAdapter<ShopEventsAdapter.Holder>
        implements View.OnClickListener {

    private final Context context;
    private final List<Event> mEventList;
    private final Shop mShop;

    private OnFeedItemClickListener onFeedItemClickListener;

    public ShopEventsAdapter(Context context, List<Event> eventList, Shop shop) {
        this.context = context;
        mEventList = eventList;
        mShop = shop;
    }

    @Override
    public Holder getViewHolder(View view) {
        return new Holder(view, false);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_tab_event, viewGroup, false);
        Holder holder = new Holder(view, true);

        holder.more.setOnClickListener(this);

        return holder;
    }

    @Override
    public void onBindViewHolder(Holder viewHolder, int position) {
        if (viewHolder.dateTime == null) {
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewHolder.itemView.setLayoutParams(params1);

            return;
        }

        bindFeedItem(position, viewHolder);
    }

    private void bindFeedItem(int position, final Holder holder) {
        Event item = mEventList.get(position);


        MyApplication.getInstance().getImageLoader().loadImage(Constants.General.BLOB_PROTOCOL + item.getShopImage(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.picture.setImageBitmap(loadedImage);
            }
        });

        holder.name.setText(item.getShopName());

        holder.title.setText(item.getTitle());
        holder.text.setText(item.getMessage());
        ((TextView) (holder.text.getChildAt(0))).setTransformationMethod(new LinkTransformationMethod((Activity) context));
        ((TextView) (holder.text.getChildAt(0))).setMovementMethod(LinkMovementMethod.getInstance());
        holder.dateTime.setText(TimeHelper.getTimeAgo(context, TimeHelper.utcToTimezone(context, item
                .getInsertTime())));
        holder.startDate.setText(new SimpleDateFormat(Constants.General.DATE_PATTERN, Locale.getDefault())
                .format(TimeHelper.getDateFromServerDatePattern(context, item.getStartDateTime())));
        holder.endDate.setText(new SimpleDateFormat(Constants.General.DATE_PATTERN, Locale.getDefault())
                .format(TimeHelper.getDateFromServerDatePattern(context, item.getEndDateTime())));

        holder.more.setTag(item.getId() + "," + position);

        List<String> managersAndAdmin = new ArrayList<>();
        if (mShop != null) {
            if (mShop.getManagersId() != null) {
                managersAndAdmin.addAll(mShop.getManagersId());
            }
            managersAndAdmin.add(mShop.getAdminId());
        }

        holder.more.setVisibility(managersAndAdmin.contains(Logged.Models.getUserProfile()
                .getId()) ? View.VISIBLE : View.GONE);
    }

    @Override
    public Holder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public int getItemCount() {
        super.getItemCount();
        return mEventList.size();
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getAdapterItemCount() {
        return mEventList.size();
    }

    @Override
    public long generateHeaderId(int i) {
        return 0;
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.image_button_more:
                String tag = (String) view.getTag();
                String[] splittedTags = tag.split(",");
                String eventId = splittedTags[0];
                int position = Integer.parseInt(splittedTags[1]);
                if (onFeedItemClickListener != null) {
                    onFeedItemClickListener.onEventMoreClick(view, eventId, position);
                }
                break;
            case R.id.image_view_picture:
            case R.id.text_view_name:
                FragmentBusiness complex = (FragmentBusiness) view.getTag();
                if (onFeedItemClickListener != null)
                {
                    onFeedItemClickListener.onBusinessImageClick(complex);
                }
                break;
        }
    }

    public interface OnFeedItemClickListener {
        void onEventMoreClick(View view, String postId, int position);
        void onBusinessImageClick(FragmentBusiness view);
    }

    static class Holder extends UltimateRecyclerviewViewHolder {
        @Bind(R.id.text_view_title)
        TextView title;
        @Bind(R.id.text_view_datetime)
        TextView dateTime;
        @Bind(R.id.expand_text_view)
        ExpandableTextView text;
        @Bind(R.id.text_view_start_date)
        TextView startDate;
        @Bind(R.id.text_view_end_date)
        TextView endDate;
        @Bind(R.id.image_button_more)
        ImageButton more;
        @Bind(R.id.image_view_picture)
        CircledNetworkImageView picture;
        @Bind(R.id.text_view_name)
        TextView name;

        public Holder(View view, boolean isItem) {
            super(view);
            if (isItem) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
