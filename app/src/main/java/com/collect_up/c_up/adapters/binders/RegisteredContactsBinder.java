/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters.binders;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.receivers.RealtimeReceiver;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.multiViewTypes.DataBinder;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;


public class RegisteredContactsBinder extends DataBinder<RegisteredContactsBinder.ViewHolder>
  implements View.OnClickListener {
  private final Context mContext;
  private List<Profile> dataSet;
  private boolean mJoinRoom;
  private CompactChat mChatForJoinRoom;

  public RegisteredContactsBinder(UltimateDifferentViewTypeAdapter dataBindAdapter,
                                  List<Profile> dataSet,
                                  Context context, boolean joinRoom, CompactChat chat) {
    super(dataBindAdapter);
    this.dataSet = dataSet;
    this.mContext = context;
    mJoinRoom = joinRoom;
    mChatForJoinRoom = chat;
  }

  public void setDataSet(List<Profile> profiles) {
    this.dataSet = profiles;
  }

  @Override
  public ViewHolder newViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.inf_contacts, parent, false);

    ViewHolder viewHolder = new ViewHolder(view);
    viewHolder.itemView.setOnClickListener(this);

    return new ViewHolder(view);
  }


  @Override
  public void bindViewHolder(ViewHolder holder, int position) {
    Profile item = dataSet.get(position);


    holder.itemView.setTag(item);
    holder.name.setText(item.getName());
    holder.username.setText(item.getUsername());

    if (Utils.isNullOrEmpty(item.getImageAddress()))
    {
      holder.picture.makeAllDefaults();
      holder.picture.setText(item.getName());
    } else
    {
      holder.picture.makeAllDefaults();
      holder.picture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(item.getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + item.getThumb());
    }
  }

  @Override
  public int getItemCount() {
    return 1;
  }

  @Override
  public void onClick(View v) {
    final Profile profile = (Profile) v.getTag();
    if (!mJoinRoom)
    {
      final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(mContext));
      CompactChat chat;
      RChat existChat = mRealm.where(RChat.class).equalTo("ReceiverId", profile.getId()).findFirst();
      if (existChat != null)
      {
        chat = RToNonR.rChatToChat(existChat);
      } else
      {
        chat = new CompactChat();
        chat.setReceiverId(profile.getId());
        chat.setMembersCount(2);
        chat.setAmIManager(true);
        chat.setAmISuperAdmin(true);
        chat.setChatId("");
        chat.setUnSeenMessageCount(0);
        chat.setIsGroup(false);
        chat.setProfileThumbnailAddress(profile.getImageAddress());
        chat.setTitle(profile.getName());
      }
      Intent intent = new Intent(mContext, RealtimeReceiver.class);
      intent.putExtra("chat", chat);
      intent.setAction(Constants.General.UPDATE_CHAT_UI);
      intent.putExtra("method", "onNewChat");
      mContext.sendBroadcast(intent);

    } else
    {

      HttpClient.get(String.format(Constants.Server.Messaging.GET_ADDMEMBER_TOGROUP, mChatForJoinRoom.getChatId(), profile.getId()), new AsyncHttpResponser(mContext) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          super.onSuccess(statusCode, headers, responseBody);
          FragmentHandler.onBackPressed(mContext);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);
        }
      });

    }
  }

  static class ViewHolder extends UltimateRecyclerviewViewHolder {
    TextView name;
    ComplexAvatarView picture;
    TextView username;

    public ViewHolder(View view) {
      super(view);
      name = (TextView) view.findViewById(R.id.text_view_name);
      username = (TextView) view.findViewById(R.id.text_view_username);
      picture = (ComplexAvatarView) view.findViewById(R.id.image_view_picture);
    }
  }
}
