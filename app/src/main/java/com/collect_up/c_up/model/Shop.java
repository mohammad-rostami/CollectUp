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

import java.util.ArrayList;
import java.util.List;

public class Shop extends BaseModel implements Parcelable {

  public static final Parcelable.Creator<Shop> CREATOR = new Parcelable.Creator<Shop>() {
    @Override
    public Shop createFromParcel(Parcel source) {
      return new Shop(source);
    }

    @Override
    public Shop[] newArray(int size) {
      return new Shop[size];
    }
  };
  private String Address;
  private String AdminId;
  private Complex Complex;
  private String Description;
  private String Email;
  private List<String> Followers;
  private List<String> Requests;
  private String Lat;
  private int BusinessLevel;
  private boolean IsFollowing;


  private String Long;
  private List<String> ManagersId;
  private String RatesAverage;
  private int RatesCount;
  private List<String> Tags;

  public List<Category> getCategories() {
    return Categories;
  }

  public void setCategories(List<Category> categories) {
    this.Categories = categories;
  }

  private List<Category> Categories;


  public Shop() {

  }

  public List<String> getRequests() {
    if (Requests == null)
    {
      Requests = new ArrayList<>();
    }
    return Requests;
  }

  public void setRequests(List<String> requests) {
    Requests = requests;
  }

  protected Shop(Parcel in) {
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
    Tags = in.createStringArrayList();
    Email = in.readString();
    Complex = in.readParcelable(Complex.class.getClassLoader());
    Description = in.readString();
    Lat = in.readString();
    Long = in.readString();
    AdminId = in.readString();
    ManagersId = in.createStringArrayList();
    RatesAverage = in.readString();
    Categories = in.createTypedArrayList(Category.CREATOR);
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
    dest.writeStringList(Tags);
    dest.writeString(Email);
    dest.writeParcelable(Complex, 0);
    dest.writeString(Description);
    dest.writeString(Lat);
    dest.writeString(Long);
    dest.writeString(AdminId);
    dest.writeStringList(ManagersId);
    dest.writeString(RatesAverage);
    dest.writeTypedList(Categories);
    dest.writeInt(BusinessLevel);
    dest.writeByte(IsFollowing ? (byte) 1 : (byte) 0);

  }

  public String getAddress() {
    return Address;
  }

  public void setAddress(String address) {
    Address = address;
  }

  public int getBusinessLevel() {
    return BusinessLevel;
  }

  public void setBusinessLevel(int businessLevel) {
    BusinessLevel = businessLevel;
  }

  public String getAdminId() {
    return AdminId;
  }

  public void setAdminId(String adminId) {
    AdminId = adminId;
  }

  public com.collect_up.c_up.model.Complex getComplex() {
    return Complex;
  }

  public void setComplex(com.collect_up.c_up.model.Complex complex) {
    Complex = complex;
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
    if (Followers == null)
    {
      Followers = new ArrayList<>();
    }
    return Followers;
  }

  public void setFollowers(List<String> followers) {
    Followers = followers;
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
    if (ManagersId == null)
    {
      ManagersId = new ArrayList<>();
    }
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

  public List<String> getTags() {
    if (Tags == null)
    {
      Tags = new ArrayList<>();
    }
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
