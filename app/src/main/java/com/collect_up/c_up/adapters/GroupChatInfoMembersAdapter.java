/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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
import com.collect_up.c_up.model.CompactChatMember;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GroupChatInfoMembersAdapter extends UltimateViewAdapter<GroupChatInfoMembersAdapter.Holder> implements View.OnClickListener, View.OnLongClickListener {
  private final Context mContext;
  public CompactChat mChat;
  private List<CompactChatMember> mProfiles;
  private OnViewClick mViewClickListener;

  public GroupChatInfoMembersAdapter(Context context, List<CompactChatMember> profiles, CompactChat chat) {
    mProfiles = profiles;
    mContext = context;
    mChat = chat;
  }

  public void updateDataSet(List<CompactChatMember> profiles) {
    mProfiles = profiles;
    notifyDataSetChanged();
  }

  public void setViewClickListener(OnViewClick listener) {
    if (listener != null)
    {
      mViewClickListener = listener;
    }
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inf_contacts_group_chat, viewGroup, false);

    return new Holder(view, true);
  }

  @Override
  public int getAdapterItemCount() {
    return mProfiles.size();
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    CompactChatMember compactChatMember = mProfiles.get(position);

    if (compactChatMember != null)
    {
      holder.container.setOnClickListener(this);
      holder.image.setOnClickListener(this);
      holder.name.setOnClickListener(this);
      holder.groupCreatorIcon.setOnClickListener(this);

      if (mChat != null && mChat.AmISuperAdmin())
      {// mChat.getAdminId().equals(Logged.Models.getUserProfile().getId())) {
        holder.container.setOnLongClickListener(this);
        holder.image.setOnLongClickListener(this);
        holder.name.setOnLongClickListener(this);
        holder.groupCreatorIcon.setOnLongClickListener(this);
      }
      Profile profile = new Profile();
      profile.setId(compactChatMember.getUserId());
      profile.setImageAddress(compactChatMember.getThumbnailAddress());
      profile.setName(compactChatMember.getName());
      profile.setUsername(compactChatMember.getUserName());
      HashMap<String, Object> hashMap = new HashMap<>();
      hashMap.put("profile", profile);
      hashMap.put("pos", position);
      holder.name.setTag(hashMap);
      holder.image.setTag(hashMap);
      holder.container.setTag(hashMap);
      holder.groupCreatorIcon.setTag(hashMap);

      holder.lastSeen.setVisibility(View.VISIBLE);

      // Display group creator icon for the owner.
      if (!compactChatMember.getIsSuperAdmin())
      {//!mChat.getAdminId().equals(profilegetgetUserId())) {
        holder.groupCreatorIcon.setVisibility(View.INVISIBLE);
      } else
      {
        holder.groupCreatorIcon.setVisibility(View.VISIBLE);

        holder.groupCreatorIcon.setOnLongClickListener(null);
        holder.name.setOnLongClickListener(null);
        holder.image.setOnLongClickListener(null);
        holder.container.setOnLongClickListener(null);
      }

      if (Logged.Models.getUserProfile().getId().equals(compactChatMember.getUserId()))
      {
        holder.groupCreatorIcon.setOnClickListener(null);
        holder.name.setOnClickListener(null);
        holder.image.setOnClickListener(null);
        holder.container.setOnClickListener(null);
      }

      if (Utils.isNullOrEmpty(compactChatMember.getThumbnailAddress()))
      {
        holder.image.makeAllDefaults();
        holder.image.setText(compactChatMember.getName());
      } else
      {
        holder.image.makeAllDefaults();
        holder.image.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(compactChatMember.getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + compactChatMember.getThumbnailAddress());
      }

      try
      {
               /* if (profile.getIsOnline()) {
                    holder.lastSeen.setText(Html.fromHtml("<font color=#03A9F4>" + mContext.getString(R.string.online) + "</font>"));
                } else {*/
        holder.lastSeen.setText(String.format(mContext.getString(R.string.last_seen), TimeHelper.makeLastSeen(mContext, new Date(compactChatMember.getLastOnline().contains("T") ? TimeHelper.utcToTimezone(mContext, compactChatMember.getLastOnline()) : Long.valueOf(compactChatMember.getLastOnline())))));
        //}
      } catch (NumberFormatException ignored)
      {
        //REVIEW If anytime lastOnline is not valid Long value, no error thrown (just one time occurred)
      }

      holder.name.setText(compactChatMember.getName());
    } else
    {
      holder.lastSeen.setVisibility(View.GONE);

      holder.image.makeAllDefaults();
      holder.image.setDefaultImage(R.drawable.ic_account_plus);

      holder.name.setText(R.string.add_member);

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
    Intent intent = new Intent(mContext, FragmentHandler.class);
    intent.putExtra("chat", mChat);
    intent.putExtra("show_invites", false);
    intent.putExtra("title", mContext.getString(R.string.contacts));
    intent.putExtra("join_room", true);
    FragmentHandler.replaceFragment(mContext, fragmentType.CHATCONTACTS, intent);

  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  @Override
  public boolean onLongClick(View v) {
    HashMap<String, Object> hashMap = (HashMap<String, Object>) v.getTag();
    if (mViewClickListener != null)
    {
      mViewClickListener.onViewLongClick((Profile) hashMap.get("profile"), (int) hashMap.get("pos"));
    }
    return true;
  }

  public interface OnViewClick {
    void onViewClick(Profile profile);

    void onViewLongClick(Profile profile, int pos);
  }

  @Override
  public void onClick(View v) {
    HashMap<String, Object> hashMap = (HashMap<String, Object>) v.getTag();

    Profile profile = (Profile) hashMap.get("profile");
    if (mViewClickListener != null)
    {
      mViewClickListener.onViewClick(profile);
    }
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
