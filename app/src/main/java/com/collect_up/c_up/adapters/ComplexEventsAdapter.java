/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.EventComplex;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ComplexEventsAdapter extends UltimateViewAdapter<ComplexEventsAdapter.Holder>
  implements View.OnClickListener {

  private final Context context;
  private final List<EventComplex> mEventComplexList;
  private final Complex mComplex;

  private OnFeedItemClickListener onFeedItemClickListener;

  public ComplexEventsAdapter(Context context,
                              List<EventComplex> eventComplexList,
                              Complex complex) {
    this.context = context;
    mEventComplexList = eventComplexList;
    mComplex = complex;
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
    if (viewHolder.dateTime == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      viewHolder.itemView.setLayoutParams(params1);

      return;
    }

    bindFeedItem(position, viewHolder);
  }

  private void bindFeedItem(int position, final Holder holder) {
    EventComplex item = mEventComplexList.get(position);

    holder.title.setText(item.getTitle());
    holder.text.setText(item.getMessage());
    holder.dateTime.setText(TimeHelper.getTimeAgo(context, TimeHelper.utcToTimezone(context, item
      .getInsertTime())));
    holder.startDate.setText(new SimpleDateFormat(Constants.General.DATE_PATTERN, Locale.getDefault())
      .format(TimeHelper.getDateFromServerDatePattern(context, item.getStartDateTime())));
    holder.endDate.setText(new SimpleDateFormat(Constants.General.DATE_PATTERN, Locale.getDefault())
      .format(TimeHelper.getDateFromServerDatePattern(context, item.getEndDateTime())));

    holder.more.setTag(item.getId() + "," + position);

    List<String> managersAndAdmin = new ArrayList<>();
    if (mComplex.getManagersId() != null)
    {
      managersAndAdmin.addAll(mComplex.getManagersId());
    }
    managersAndAdmin.add(mComplex.getAdminId());

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
    return mEventComplexList.size();
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  @Override
  public int getAdapterItemCount() {
    return mEventComplexList.size();
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
    switch (view.getId())
    {
      case R.id.image_button_more:
        String tag = (String) view.getTag();
        String[] splittedTags = tag.split(",");
        String eventId = splittedTags[0];
        int position = Integer.parseInt(splittedTags[1]);
        if (onFeedItemClickListener != null)
        {
          onFeedItemClickListener.onEventMoreClick(view, eventId, position);
        }
        break;
    }
  }

  public interface OnFeedItemClickListener {
    void onEventMoreClick(View view, String postId, int position);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.text_view_title)
    TextView title;
    @Bind (R.id.text_view_datetime)
    TextView dateTime;
    @Bind (R.id.expand_text_view)
    ExpandableTextView text;
    @Bind (R.id.text_view_start_date)
    TextView startDate;
    @Bind (R.id.text_view_end_date)
    TextView endDate;
    @Bind (R.id.image_button_more)
    ImageButton more;

    public Holder(View view, boolean isItem) {
      super(view);
      if (isItem)
      {
        ButterKnife.bind(this, view);
      }
    }
  }
}
