package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by collect-up3 on 10/30/2016.
 */

public class CompactChat implements Parcelable {
  private String ChatId;
  private String Title;
  private CompactMessage LastMessage;
  private String ProfileThumbnailAddress;
  private String ReceiverId;
  private String LastOnline;
  private long MembersCount;
  private long UnSeenMessageCount;
  private boolean IsGroup;
  private boolean AmISuperAdmin;
  private boolean AmIManager;

  public CompactMessage getLastMessage() {
    return LastMessage;
  }

  public void setLastMessage(CompactMessage lastMessage) {
    LastMessage = lastMessage;
  }


  public CompactChat() {
  }

  protected CompactChat(Parcel in) {
    ChatId = in.readString();
    Title = in.readString();
    LastMessage = in.readParcelable(CompactMessage.class.getClassLoader());
    ProfileThumbnailAddress = in.readString();
    ReceiverId = in.readString();
    LastOnline = in.readString();
    MembersCount = in.readLong();
    UnSeenMessageCount = in.readLong();
    IsGroup = in.readByte() != 0;
    AmIManager = in.readByte() != 0;
    AmISuperAdmin = in.readByte() != 0;
  }

  public static final Creator<CompactChat> CREATOR = new Creator<CompactChat>() {
    @Override
    public CompactChat createFromParcel(Parcel in) {
      return new CompactChat(in);
    }

    @Override
    public CompactChat[] newArray(int size) {
      return new CompactChat[size];
    }
  };

  public String getChatId() {
    return ChatId;
  }

  public void setChatId(String chatId) {
    ChatId = chatId;
  }

  public String getLastOnline() {
    return LastOnline;
  }

  public void setLastOnline(String lastOnline) {
    LastOnline = lastOnline;
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


  public long getUnSeenMessageCount() {
    return UnSeenMessageCount;
  }

  public void setUnSeenMessageCount(long unSeenMessageCount) {
    UnSeenMessageCount = unSeenMessageCount;
  }

  public boolean isGroup() {
    return IsGroup;
  }

  public void setIsGroup(boolean group) {
    IsGroup = group;
  }


  public long getMembersCount() {
    return MembersCount;
  }

  public void setMembersCount(long membersCount) {
    MembersCount = membersCount;
  }

  public String getReceiverId() {
    return ReceiverId;
  }

  public void setReceiverId(String receiverId) {
    ReceiverId = receiverId;
  }


  public boolean isAmIManager() {
    return AmIManager;
  }

  public void setAmIManager(boolean amIManager) {
    AmIManager = amIManager;
  }

  public boolean AmISuperAdmin() {
    return AmISuperAdmin;
  }

  public void setAmISuperAdmin(boolean amISuperAdmin) {
    AmISuperAdmin = amISuperAdmin;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(ChatId);
    parcel.writeString(Title);
    parcel.writeParcelable(LastMessage, 0);
    parcel.writeString(ProfileThumbnailAddress);
    parcel.writeString(ReceiverId);
    parcel.writeString(LastOnline);
    parcel.writeLong(MembersCount);
    parcel.writeLong(UnSeenMessageCount);
    parcel.writeByte(IsGroup ? (byte) 1 : (byte) 0);
    parcel.writeByte(AmIManager ? (byte) 1 : (byte) 0);
    parcel.writeByte(AmISuperAdmin ? (byte) 1 : (byte) 0);
  }
}
