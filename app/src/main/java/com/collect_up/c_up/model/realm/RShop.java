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

@SuppressWarnings ("ClassHasNoToStringMethod")
public class RShop extends RealmObject {
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

  public RComplex getComplex() {
    return Complex;
  }

  public void setComplex(RComplex complex) {
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

  public RealmList<RealmString> getFollowers() {
    return Followers;
  }

  public void setFollowers(RealmList<RealmString> followers) {
    Followers = followers;
  }

  public RealmList<RealmString> getRequests() {
    return Requests;
  }

  public void setRequests(RealmList<RealmString> requests) {
    Requests = requests;
  }

  public String getImageAddress() {
    return ImageAddress;
  }

  public void setImageAddress(String imageAddress) {
    ImageAddress = imageAddress;
  }

  public String getLat() {
    return Lat;
  }

  public void setLat(String lat) {
    Lat = lat;
  }

  public String getCoverPhoto() {
    return CoverPhoto;
  }

  public void setCoverPhoto(String coverPhoto) {
    CoverPhoto = coverPhoto;
  }

  public String getLong() {
    return Long;
  }

  public void setLong(String aLong) {
    Long = aLong;
  }

  public RealmList<RealmString> getManagersId() {
    return ManagersId;
  }

  public void setManagersId(RealmList<RealmString> managersId) {
    ManagersId = managersId;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getPhoneNumber() {
    return PhoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    PhoneNumber = phoneNumber;
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

  public RealmList<RealmString> getTags() {
    return Tags;
  }

  public void setTags(RealmList<RealmString> tags) {
    Tags = tags;
  }

  public String getUsername() {
    return Username;
  }

  public void setUsername(String username) {
    Username = username;
  }

  private String Address;
  private String AdminId;
  private RComplex Complex;
  private String Description;
  private String Email;
  private RealmList<RealmString> Followers;
  private RealmList<RealmString> Requests;
  private String ImageAddress;
  private String Lat;
  private String CoverPhoto;
  private String Long;
  private RealmList<RealmString> ManagersId;
  private String Name;
  private String PhoneNumber;
  private String RatesAverage;
  private int RatesCount;
  private RealmList<RealmString> Tags;
  private String Username;
  private int BusinessLevel;
  private boolean IsFollowing;

  public boolean getIsFollowing() {
    return IsFollowing;
  }

  public void setIsFollowing(boolean following) {
    IsFollowing = following;
  }

  public int getBusinessLevel() {
    return BusinessLevel;
  }

  public void setBusinessLevel(int businessLevel) {
    BusinessLevel = businessLevel;
  }

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }


  @PrimaryKey

  private String Id;
}
