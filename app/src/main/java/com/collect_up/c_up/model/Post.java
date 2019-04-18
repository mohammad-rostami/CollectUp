/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.collect_up.c_up.helpers.Utils;

import java.util.ArrayList;
import java.util.List;


public class Post implements Parcelable {
  public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
    @Override
    public Post createFromParcel(Parcel source) {
      return new Post(source);
    }

    @Override
    public Post[] newArray(int size) {
      return new Post[size];
    }
  };
  public String ImageAddress;
  public String InsertTime;
  public List<String> Likes;
  public ArrayList<PostProfileTag> ProfileTags;
  public String Text;
  public String VideoAddress;
  private long ViewCount;

  public ArrayList<PostProfileTag> getProfileTags() {
    return ProfileTags;
  }

  public void setProfileTags(ArrayList<PostProfileTag> profileTags) {
    ProfileTags = profileTags;
  }

  public BaseModel getSender() {
    if (getSenderComplex() != null)
    {
      return getSenderComplex();
    } else if (getSenderShop() != null)
    {
      return getSenderShop();
    } else if (getSenderProfile() != null)
    {
      return getSenderProfile();
    }
    return null;
  }

  public List<String> getTags() {
    return Tags;
  }

  public void setTags(List<String> tags) {
    Tags = tags;
  }

  private String Id;
  private Complex SenderComplex;
  private Profile SenderProfile;
  private Shop SenderShop;
  private String CommentsCount;
  public String ImageSize;
  public List<String> Tags;

  public String getVideoProperties() {
    return VideoProperties;
  }

  public void setVideoProperties(String videoProperties) {
    VideoProperties = videoProperties;
  }

  public String VideoProperties;

  public String getSize() {
    return ImageSize;
  }

  public void setSize(String size) {
    this.ImageSize = size;
  }

  public Post() {
  }

  protected Post(Parcel in) {
    Id = in.readString();
    SenderProfile = in.readParcelable(Profile.class.getClassLoader());
    SenderComplex = in.readParcelable(Complex.class.getClassLoader());
    SenderShop = in.readParcelable(Shop.class.getClassLoader());
    ImageAddress = in.readString();
    VideoAddress = in.readString();
    Text = in.readString();
    Likes = in.createStringArrayList();
    InsertTime = in.readString();
    CommentsCount = in.readString();
    ImageSize = in.readString();
    VideoProperties = in.readString();
    Tags = in.createStringArrayList();
    ViewCount = in.readLong();
    ProfileTags = in.createTypedArrayList(PostProfileTag.CREATOR);


  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(Id);
    dest.writeParcelable(SenderProfile, 0);
    dest.writeParcelable(SenderComplex, 0);
    dest.writeParcelable(SenderShop, 0);
    dest.writeString(ImageAddress);
    dest.writeString(VideoAddress);
    dest.writeString(Text);
    dest.writeStringList(Likes);
    dest.writeString(InsertTime);
    dest.writeString(CommentsCount);
    dest.writeString(ImageSize);
    dest.writeString(VideoProperties);
    dest.writeStringList(Tags);
    dest.writeLong(ViewCount);
    dest.writeTypedList(ProfileTags);

  }

  public long getViewCount() {
    return ViewCount;
  }

  public void setViewCount(long viewCount) {
    ViewCount = viewCount;
  }

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public String getImageAddress() {
    return ImageAddress;
  }

  public String getThumb() {
    if (!Utils.isNullOrEmpty(ImageAddress))
    {
      return ImageAddress.replace("images", "thumbnails");
    } else
    {
      return "";
    }
  }

  public void setImageAddress(String imageAddress) {
    ImageAddress = imageAddress;
  }

  public String getInsertTime() {
    return InsertTime;
  }

  public void setInsertTime(String insertTime) {
    InsertTime = insertTime;
  }

  public List<String> getLikes() {
    return Likes;
  }

  public void setLikes(List<String> likes) {
    Likes = likes;
  }

  public Complex getSenderComplex() {
    return SenderComplex;
  }

  public void setSenderComplex(Complex senderComplex) {
    SenderComplex = senderComplex;
  }

  public Profile getSenderProfile() {
    return SenderProfile;
  }

  public void setSenderProfile(Profile senderProfile) {
    SenderProfile = senderProfile;
  }

  public Shop getSenderShop() {
    return SenderShop;
  }

  public void setSenderShop(Shop senderShop) {
    SenderShop = senderShop;
  }

  public String getText() {
    return Text;
  }

  public void setText(String text) {
    Text = text;
  }

  public String getVideoAddress() {
    return VideoAddress;
  }

  public void setVideoAddress(String videoAddress) {
    VideoAddress = videoAddress;
  }

  public String getCommentsCount() {
    return CommentsCount;
  }

  public void setCommentsCount(String commentsCount) {
    CommentsCount = commentsCount;
  }
}
