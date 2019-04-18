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

import java.util.List;

public class Complex extends BaseModel implements Parcelable {
  public static final Parcelable.Creator<Complex> CREATOR = new Parcelable.Creator<Complex>() {
    @Override
    public Complex createFromParcel(Parcel source) {
      return new Complex(source);
    }

    @Override
    public Complex[] newArray(int size) {
      return new Complex[size];
    }
  };
  private String Address;
  private String AdminId;
  private String Description;
  private String Email;
  private List<String> Followers;
  private int BusinessLevel;
  private boolean IsFollowing;


  public List<String> getRequests() {
    return Requests;
  }

  public void setRequests(List<String> requests) {
    Requests = requests;
  }

  private List<String> Requests;
  private String Lat;
  private String Long;
  private List<String> ManagersId;
  private String RatesAverage;
  private int RatesCount;
  private List<String> ShopsId;
  private List<String> Tags;

  public Complex() {
    super(null);
  }

  protected Complex(Parcel in) {
    super(null);
    Id = in.readString();
    ImageAddress = in.readString();
    CoverPhoto = in.readString();
    Name = in.readString();
    PhoneNumber = in.readString();
    Username = in.readString();

    Address = in.readString();
    RatesCount = in.readInt();
    Followers = in.createStringArrayList();
    Requests = in.createStringArrayList();
    ShopsId = in.createStringArrayList();
    Email = in.readString();
    Description = in.readString();
    Lat = in.readString();
    Long = in.readString();
    AdminId = in.readString();
    ManagersId = in.createStringArrayList();
    RatesAverage = in.readString();
    Tags = in.createStringArrayList();
    BusinessLevel = in.readInt();
    IsFollowing = in.readByte() != 0;

  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(Id);
    dest.writeString(ImageAddress);
    dest.writeString(CoverPhoto);
    dest.writeString(Name);
    dest.writeString(PhoneNumber);
    dest.writeString(Username);

    dest.writeString(Address);
    dest.writeInt(RatesCount);
    dest.writeStringList(Followers);
    dest.writeStringList(Requests);
    dest.writeStringList(ShopsId);
    dest.writeString(Email);
    dest.writeString(Description);
    dest.writeString(Lat);
    dest.writeString(Long);
    dest.writeString(AdminId);
    dest.writeStringList(ManagersId);
    dest.writeString(RatesAverage);
    dest.writeStringList(Tags);
    dest.writeInt(BusinessLevel);
    dest.writeByte(IsFollowing ? (byte) 1 : (byte) 0);

  }

  public String getAddress() {
    return Address;
  }

  public void setAddress(String address) {
    Address = address;
  }

  public String getAdminId() {
    return AdminId;
  }

  public void setAdminId(String adminId) {
    AdminId = adminId;
  }

  public String getDescription() {
    return Description;
  }

  public void setDescription(String description) {
    Description = description;
  }

  public String getEmail() {
    return Email;
  }

  public void setEmail(String email) {
    Email = email;
  }

  public List<String> getFollowers() {
    return Followers;
  }

  public void setFollowers(List<String> followers) {
    Followers = followers;
  }

  public int getBusinessLevel() {
    return BusinessLevel;
  }

  public void setBusinessLevel(int businessLevel) {
    BusinessLevel = businessLevel;
  }

  public String getLat() {
    return Lat;
  }

  public void setLat(String lat) {
    Lat = lat;
  }

  public String getLong() {
    return Long;
  }

  public void setLong(String aLong) {
    Long = aLong;
  }

  public List<String> getManagersId() {
    return ManagersId;
  }

  public void setManagersId(List<String> managersId) {
    ManagersId = managersId;
  }

  public String getRatesAverage() {
    return RatesAverage;
  }

  public void setRatesAverage(String ratesAverage) {
    RatesAverage = ratesAverage;
  }

  public int getRatesCount() {
    return RatesCount;
  }

  public void setRatesCount(int ratesCount) {
    RatesCount = ratesCount;
  }

  public List<String> getShopsId() {
    return ShopsId;
  }

  public void setShopsId(List<String> shopsId) {
    ShopsId = shopsId;
  }

  public List<String> getTags() {
    return Tags;
  }

  public void setTags(List<String> tags) {
    Tags = tags;
  }

  public boolean isFollowing() {
    return IsFollowing;
  }

  public void setFollowing(boolean following) {
    IsFollowing = following;
  }
}
