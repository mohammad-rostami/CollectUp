/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.chat.AndroidUtilities;
import com.collect_up.c_up.chat.widgets.Emoji;
import com.collect_up.c_up.helpers.AppUtils;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.EnumMessageStatus;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConversationsAdapter extends UltimateViewAdapter<ConversationsAdapter.Holder>
  implements View.OnClickListener,
  View.OnLongClickListener,
  Filterable {

  private List<CompactChat> mChatList;
  private final Context mContext;
  private final Profile mCurrentUser = Logged.Models.getUserProfile();
  private OnItemClickListener mListener;
  private ValueFilter valueFilter;

  public ConversationsAdapter(Context context,
                              List<CompactChat> chatList,
                              List<RCompactMessage> messageList, List<Integer> unSeenMessages) {
    mChatList = chatList;
    mContext = context;

  }

  public ConversationsAdapter(Context context,
                              List<CompactChat> chatList) {
    mChatList = chatList;
    mContext = context;
  }

  public List<CompactChat> getChats() {
    return mChatList;
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    if (holder.picture == null)
    {

      return;
    }

    CompactChat chat = mChatList.get(position);

    holder.container.setTag(chat);
    holder.groupIcon.setTag(chat);
    holder.picture.setTag(chat);
    holder.lastMessage.setTag(chat);
    holder.name.setTag(chat);
    holder.status.setTag(chat);
    holder.time.setTag(chat);
    holder.unseenMessagesCount.setTag(chat);
    holder.mutedChat.setTag(chat);

    if (!chat.isGroup())
    {
      holder.groupIcon.setVisibility(View.GONE);
      holder.name.setText(chat.getTitle());

      if (!Utils.isNullOrEmpty(chat.getProfileThumbnailAddress()))
      {
        holder.picture.makeAllDefaults();
        holder.picture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(chat.getTitle()).setImageUrl(Constants.General.BLOB_PROTOCOL + chat.getProfileThumbnailAddress());
      } else
      {
        holder.picture.makeAllDefaults();
        holder.picture.setText(chat.getTitle());
      }

    } else
    {
      holder.groupIcon.setVisibility(View.VISIBLE);
      holder.name.setText(chat.getTitle());

      if (!Utils.isNullOrEmpty(chat.getProfileThumbnailAddress()))
      {
        holder.picture.makeAllDefaults();
        holder.picture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(chat.getTitle()).setImageUrl(Constants.General.BLOB_PROTOCOL + chat.getProfileThumbnailAddress());
      } else
      {
        holder.picture.makeAllDefaults();
        holder.picture.setText(chat.getTitle());
      }
    }

    if (mChatList.get(position).getUnSeenMessageCount() < 1)
    {
      holder.unseenMessagesCount.setVisibility(View.GONE);
    } else
    {
      if (AppUtils.isChatMuted(chat.getChatId()))
      {
        holder.unseenMessagesCount.setEnabled(false);
      } else
      {
        holder.unseenMessagesCount.setEnabled(true);
      }
      if (chat.getLastMessage().getContentType() == EnumMessageContentType.Announcement)
      {
        holder.unseenMessagesCount.setText(mChatList.get(position).getUnSeenMessageCount() - 1 + "");
        if (holder.unseenMessagesCount.getText().toString().equalsIgnoreCase("0"))
        {
          holder.unseenMessagesCount.setVisibility(View.GONE);
        }
      } else
      {
        holder.unseenMessagesCount.setText(mChatList.get(position).getUnSeenMessageCount() + "");
        holder.unseenMessagesCount.setVisibility(View.VISIBLE);

      }

    }


    if (AppUtils.isChatMuted(chat.getChatId()))
    {
      holder.mutedChat.setVisibility(View.VISIBLE);
    } else
    {
      holder.mutedChat.setVisibility(View.GONE);
    }

    if (mChatList.get(position).getLastMessage() != null)
    {
      CharSequence sequence = Emoji.replaceEmoji(mContext, chat.getLastMessage().getTextType() != null ? chat.getLastMessage().getTextType() : "", holder.lastMessage
        .getPaint()
        .getFontMetricsInt(), AndroidUtilities.dp(16));
      if (chat.getLastMessage().getContentType() == EnumMessageContentType.Announcement)
      {
        holder.lastMessage.setText(Html.fromHtml("<font color = #" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.colorAccent)) + ">" + sequence + "</font>"));
      } else
      {
        if (sequence.toString().contains(mContext.getString(R.string.is_typing)))
        {
          holder.lastMessage.setText(Html.fromHtml("<font color = #" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.colorAccent)) + ">" + sequence + "</font>"));
        } else
        {
          if (chat.isGroup() && chat.getLastMessage().getSenderId() != null)
          {
            // If the chat is a group, the text must be '<color>Alireza:</color> ...'
            Spanned text = Html.fromHtml("<font color = #" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.colorAccent)).replace("ff", "") + ">" + (!chat.getLastMessage().getSenderId().equals(Logged.Models.getUserProfile().getId()) ? chat.getLastMessage().getSenderName() : mContext.getString(R.string.you)) + ":</font></b> " + sequence);
            holder.lastMessage.setText(text);
          } else
          {
            if (sequence.toString().contains("lat"))
            {
              sequence = mContext.getResources().getString(R.string.location);
            }
            holder.lastMessage.setText(sequence);
          }
        }
      }
      Date date = new Date();
      if (chat.getLastMessage().getSendDateTime().contains("T"))
      {
        date.setTime(TimeHelper.utcToTimezone(mContext, chat.getLastMessage().getSendDateTime()));
      } else
      {
        date.setTime(Long.valueOf(chat.getLastMessage().getSendDateTime()));
      }
      holder.time.setText(TimeHelper.getChatTimeAgo(mContext, date, true));

      if (chat.getLastMessage().getContentType() != EnumMessageContentType.Announcement && chat.getLastMessage().getSenderId().equals(mCurrentUser.getId()))
      {
        int messageStatus = chat.getLastMessage().getMessageStatus();

        if (messageStatus != 0)
        {
          changeStatus(holder, messageStatus);
          holder.status.setVisibility(View.VISIBLE);
        } else
        {
          holder.status.setVisibility(View.GONE);
        }

      } else
      {
        holder.status.setVisibility(View.GONE);
      }
    } else
    {
      holder.lastMessage.setText(null);
      if (chat.getLastMessage() != null)
      {
        Date dates = new Date(TimeHelper.utcToTimezone(mContext, chat.getLastMessage().getSendDateTime()));

        holder.time.setText(TimeHelper.getChatTimeAgo(mContext, dates, true));
      }
      holder.status.setVisibility(View.GONE);
    }
  }

  private void changeStatus(Holder holder, int mode) {
    if (mode == EnumMessageStatus.Sent)
    {
      holder.status.setImageResource(R.drawable.ic_single_tick);
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (mode == EnumMessageStatus.Delivered)
    {
      holder.status.setImageResource(R.drawable.ic_double_tick);
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (mode == EnumMessageStatus.Seen)
    {
      holder.status.setImageResource(R.drawable.ic_double_tick);
      holder.status.setColorFilter(Color.argb(255, 1, 87, 155));
    } else if (mode == EnumMessageStatus.Failed)
    {
      holder.status.setImageResource(R.drawable.ic_failed);
      holder.status.clearColorFilter();
    } else
    {
      holder.status.setImageResource(R.drawable.ic_clock);
      holder.status.setColorFilter(Color.argb(255, 1, 87, 155));
    }
  }

  @Override
  public void onClick(View v) {
    CompactChat chat = (CompactChat) v.getTag();
    if (mListener != null)
    {
      mListener.onViewClick(chat);
    }
  }

  @Override
  public boolean onLongClick(View v) {
    if (mListener != null)
    {
      mListener.onViewLongClick((CompactChat) v.getTag());
    }
    return false;
  }

  public void setListener(OnItemClickListener listener) {
    mListener = listener;
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.item_conversation, viewGroup, false);

    Holder holder = new Holder(view, true);
    holder.container.setOnClickListener(this);
    holder.container.setOnLongClickListener(this);
    holder.status.setOnClickListener(this);
    holder.status.setOnLongClickListener(this);
    holder.unseenMessagesCount.setOnClickListener(this);
    holder.unseenMessagesCount.setOnLongClickListener(this);
    holder.time.setOnClickListener(this);
    holder.time.setOnLongClickListener(this);
    holder.lastMessage.setOnClickListener(this);
    holder.lastMessage.setOnLongClickListener(this);
    holder.groupIcon.setOnClickListener(this);
    holder.groupIcon.setOnLongClickListener(this);
    holder.picture.setOnClickListener(this);
    holder.picture.setOnLongClickListener(this);
    holder.name.setOnClickListener(this);
    holder.name.setOnLongClickListener(this);
    holder.mutedChat.setOnClickListener(this);
    holder.mutedChat.setOnLongClickListener(this);

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    return mChatList.size();
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public Filter getFilter() {
    if (valueFilter == null)
    {
      valueFilter = new ValueFilter();
    }
    return valueFilter;
  }

  public interface OnItemClickListener {
    void onViewClick(CompactChat chat);

    void onViewLongClick(CompactChat chat);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.text_view_last_message)
    TextView lastMessage;
    @Bind (R.id.text_view_name)
    TextView name;
    @Bind (R.id.image_view_picture)
    ComplexAvatarView picture;
    @Bind (R.id.image_view_status)
    ImageView status;
    @Bind (R.id.text_view_time)
    TextView time;
    @Bind (R.id.relative_layout_container)
    RelativeLayout container;
    @Bind (R.id.text_view_chat_unseen_messages_count)
    TextView unseenMessagesCount;
    @Bind (R.id.image_view_group_icon)
    ImageView groupIcon;
    @Bind (R.id.image_view_muted_chat)
    ImageView mutedChat;

    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }

  private class ValueFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      FilterResults results = new FilterResults();

      if (constraint != null && constraint.length() > 0)
      {
        List<CompactChat> filterList = new ArrayList<>();
        for (int i = 0; i < mChatList.size(); i++)
        {
          if (mChatList.get(i)
            .getTitle()
            .toLowerCase()
            .contains(constraint.toString().toLowerCase()))
          {
            filterList.add(mChatList.get(i));
          }
        }
        results.count = filterList.size();
        results.values = filterList;
      } else
      {
        results.count = mChatList.size();
        results.values = mChatList;
      }
      return results;
    }

    @Override
    @SuppressWarnings ("unchecked")
    protected void publishResults(CharSequence constraint, FilterResults results) {
      mChatList = (List<CompactChat>) results.values;
      if (mChatList != null)
      {
        notifyDataSetChanged();
      }
    }
  }
}
