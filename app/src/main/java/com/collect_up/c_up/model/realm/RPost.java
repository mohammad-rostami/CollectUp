/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model.realm;

import com.collect_up.c_up.RealmString;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class RPost extends RealmObject {
  public String getImageAddress() {
    return ImageAddress;
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

  public RealmList<RealmString> getLikes() {
    return Likes;
  }

  public void setLikes(RealmList<RealmString> likes) {
    Likes = likes;
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

  public RComplex getSenderComplex() {
    return SenderComplex;
  }

  public void setSenderComplex(RComplex senderComplex) {
    SenderComplex = senderComplex;
  }

  public RProfile getSenderProfile() {
    return SenderProfile;
  }

  public void setSenderProfile(RProfile senderProfile) {
    SenderProfile = senderProfile;
  }

  public RShop getSenderShop() {
    return SenderShop;
  }

  public void setSenderShop(RShop senderShop) {
    SenderShop = senderShop;
  }

  public String getCommentsCount() {
    return CommentsCount;
  }

  public void setCommentsCount(String commentsCount) {
    CommentsCount = commentsCount;
  }

  public String getImageSize() {
    return ImageSize;
  }

  public void setImageSize(String imageSize) {
    ImageSize = imageSize;
  }

  public RealmList<RealmString> getTags() {
    return Tags;
  }

  public void setTags(RealmList<RealmString> tags) {
    Tags = tags;
  }

  public String getVideoProperties() {
    return VideoProperties;
  }

  public void setVideoProperties(String videoProperties) {
    VideoProperties = videoProperties;
  }

  public String getId() {
    return Id;
  }

  public long getViewCount() {
    return ViewCount;
  }

  public void setViewCount(long viewCount) {
    ViewCount = viewCount;
  }

  public void setId(String id) {
    Id = id;
  }

  public RealmList<RPostProfileTag> getProfileTags() {
    return ProfileTags;
  }

  public void setProfileTags(RealmList<RPostProfileTag> profileTags) {
    ProfileTags = profileTags;
  }

  private String ImageAddress;
  private String InsertTime;
  private RealmList<RealmString> Likes;
  private String Text;
  private String VideoAddress;
  private RComplex SenderComplex;
  private RProfile SenderProfile;
  private RShop SenderShop;
  private String CommentsCount;
  private String ImageSize;
  private RealmList<RealmString> Tags;
  private RealmList<RPostProfileTag> ProfileTags;

  private String VideoProperties;
  private long ViewCount;

  @PrimaryKey
  private String Id;

}
