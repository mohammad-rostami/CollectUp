/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GroupChatInitMembersAdapter extends UltimateViewAdapter<GroupChatInitMembersAdapter.Holder> implements View.OnClickListener, View.OnLongClickListener {
  private final Context mContext;
  public CompactChat chatObject;
  private ArrayList<Profile> mProfiles;
  private CompactChat mChat;
  private OnViewClick mViewClickListener;

  public GroupChatInitMembersAdapter(Context context, CompactChat chat) {
    chatObject = chat;
    mContext = context;
  }

  public GroupChatInitMembersAdapter(Context context, ArrayList<Profile> profiles) {
    mProfiles = profiles;
    mContext = context;
  }

  public void setViewClickListener(OnViewClick listener) {
    if (listener != null)
    {
      mViewClickListener = listener;
    }
  }

  public void setChatForJoinRoom(@Nullable CompactChat chat) {
    mChat = chat;
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inf_contacts_group_chat, viewGroup, false);

    Holder holder = new Holder(view, true);

    holder.container.setOnClickListener(this);
    holder.image.setOnClickListener(this);
    holder.name.setOnClickListener(this);
    holder.groupCreatorIcon.setOnClickListener(this);
    holder.lastSeen.setOnClickListener(this);

   /* if (mChat != null && mChat.getc().equals(Logged.Models.getUserProfile().getId()))
    {
      holder.container.setOnLongClickListener(this);
      holder.image.setOnLongClickListener(this);
      holder.name.setOnLongClickListener(this);
      holder.groupCreatorIcon.setOnLongClickListener(this);
      holder.lastSeen.setOnLongClickListener(this);
    }*/

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    int size = 0;
    if (chatObject != null)
    {
      //size = chatObject.getMembers().size();
    } else if (mProfiles != null)
    {
      size = mProfiles.size();
    }

    return size;
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    Profile profile;
    if (chatObject != null)
    {
      profile = null;//chatObject.getMembers().get(position);
    } else
    {
      profile = mProfiles.get(position);
    }

    if (profile != null)
    {
      holder.name.setTag(profile);
      holder.image.setTag(profile);
      holder.container.setTag(profile);
      holder.groupCreatorIcon.setTag(profile);

      if (mChat != null)
      {

        holder.lastSeen.setVisibility(View.VISIBLE);

        // Display group creator icon for the owner.
       /* if (!mChat.getAdminId().equals(profile.getId()))
        {
          holder.groupCreatorIcon.setVisibility(View.INVISIBLE);
        } else
        {*/
          holder.groupCreatorIcon.setVisibility(View.VISIBLE);

          holder.groupCreatorIcon.setOnLongClickListener(null);
          holder.name.setOnLongClickListener(null);
          holder.image.setOnLongClickListener(null);
          holder.container.setOnLongClickListener(null);
       // }

        if (Logged.Models.getUserProfile().getId().equals(profile.getId()))
        {
          holder.groupCreatorIcon.setOnClickListener(null);
          holder.name.setOnClickListener(null);
          holder.image.setOnClickListener(null);
          holder.container.setOnClickListener(null);
        }

      } else
      {
        holder.lastSeen.setVisibility(View.GONE);
        holder.groupCreatorIcon.setVisibility(View.GONE);
      }

      if (Utils.isNullOrEmpty(profile.getImageAddress()))
      {
        holder.image.makeAllDefaults();
        holder.image.setText(profile.getName());
      } else
      {
        holder.image.makeAllDefaults();
        holder.image.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(profile.getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + profile.getThumb());
      }

      if (profile.getIsOnline())
      {
        holder.lastSeen.setText(Html.fromHtml("<font color=#03A9F4>" + mContext.getString(R.string.online) + "</font>"));
      } else
      {
        holder.lastSeen.setText(String.format(mContext.getString(R.string.last_seen), TimeHelper.getChatTimeAgo(mContext, new Date(TimeHelper.utcToTimezone(mContext, profile.getLastOnline())), true)));
      }

      holder.name.setText(profile.getName());
    } else
    {
      holder.lastSeen.setVisibility(View.GONE);
      holder.name.setText(R.string.add_member);

      holder.image.makeAllDefaults();
      holder.image.setDefaultImage(R.drawable.add_member);

      holder.container.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          openContactsActivity();
        }
      });

      holder.name.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          openContactsActivity();
        }
      });

      holder.image.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          openContactsActivity();
        }
      });

      holder.groupCreatorIcon.setOnClickListener(null);

      holder.groupCreatorIcon.setVisibility(View.INVISIBLE);

      holder.container.setOnLongClickListener(null);
      holder.lastSeen.setOnLongClickListener(null);
      holder.image.setOnLongClickListener(null);
      holder.name.setOnLongClickListener(null);
      holder.groupCreatorIcon.setOnLongClickListener(null);
    }
  }

  private void openContactsActivity() {
    Bundle bundle = new Bundle();
    bundle.putBoolean("join_room", true);
    bundle.putParcelable("chat", mChat);
    bundle.putBoolean("show_invites", false);
    bundle.putString("title", mContext.getString(R.string.contacts));
    FragmentHandler.replaceFragment(mContext, fragmentType.CHATCONTACTS, bundle);
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  @Override
  public void onClick(View v) {
    Profile profile = (Profile) v.getTag();
    if (mViewClickListener != null)
    {
      mViewClickListener.onViewClick(profile);
    }
  }

  @Override
  public boolean onLongClick(View v) {
    Profile profile = (Profile) v.getTag();
    if (mViewClickListener != null)
    {
      mViewClickListener.onViewLongClick(profile);
    }
    return true;
  }

  public interface OnViewClick {
    void onViewClick(Profile profile);

    void onViewLongClick(Profile profile);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.image_view_picture)
    public ComplexAvatarView image;
    @Bind (R.id.text_view_name)
    public TextView name;
    @Bind (R.id.relative_layout_container)
    public RelativeLayout container;
    @Bind (R.id.image_view_group_creator)
    public ImageView groupCreatorIcon;
    @Bind (R.id.text_view_last_seen)
    public TextView lastSeen;

    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }
}
