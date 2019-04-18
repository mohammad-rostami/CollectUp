/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings ("ClassHasNoToStringMethod")
public class RChat extends RealmObject {

  private String Title;
  private RCompactMessage LastMessage;
  private String ProfileThumbnailAddress;
  private String ReceiverId;
  private String LastOnline;
  private long MembersCount;
  private long UnSeenMessageCount;
  private boolean IsGroup;
  private boolean AmISuperAdmin;
  private boolean AmIManager;

  @PrimaryKey
  private String ChatId;


  public RChat() {
  }

  public String getLastOnline() {
    return LastOnline;
  }

  public void setLastOnline(String lastOnline) {
    LastOnline = lastOnline;
  }

  public String getChatId() {
    return ChatId;
  }

  public void setChatId(String chatId) {
    ChatId = chatId;
  }

  public RCompactMessage getLastMessage() {
    return LastMessage;
  }

  public void setLastMessage(RCompactMessage lastMessage) {
    LastMessage = lastMessage;
  }

  public String getReceiverId() {
    return ReceiverId;
  }

  public void setReceiverId(String receiverId) {
    ReceiverId = receiverId;
  }

  public long getMembersCount() {
    return MembersCount;
  }

  public void setMembersCount(long membersCount) {
    MembersCount = membersCount;
  }

  public long getUnSeenMessageCount() {
    return UnSeenMessageCount;
  }

  public void setUnSeenMessageCount(long unSeenMessageCount) {
    UnSeenMessageCount = unSeenMessageCount;
  }

  public boolean isAmIManager() {
    return AmIManager;
  }

  public void setAmIManager(boolean amIManager) {
    AmIManager = amIManager;
  }

  public String getTitle() {
    return Title;
  }

  public void setTitle(String title) {
    Title = title;
  }

  public String getProfileThumbnailAddress() {
    return ProfileThumbnailAddress;
  }

  public void setProfileThumbnailAddress(String profileThumbnailAddress) {
    ProfileThumbnailAddress = profileThumbnailAddress;
  }

  public boolean getIsGroup() {
    return IsGroup;
  }

  public void setIsGroup(boolean group) {
    IsGroup = group;
  }

  public boolean getAmISuperAdmin() {
    return AmISuperAdmin;
  }

  public void setAmISuperAdmin(boolean amISuperAdmin) {
    AmISuperAdmin = amISuperAdmin;
  }
}