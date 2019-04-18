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

public class Profile extends BaseModel implements Parcelable {
  public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
    @Override
    public Profile createFromParcel(Parcel source) {
      return new Profile(source);
    }

    @Override
    public Profile[] newArray(int size) {
      return new Profile[size];
    }
  };
  private String Biography;
  private String Birthday;
  private int CountryCode;
  private List<String> Followers;
  private List<String> Following;
  private List<String> Requested;
  private boolean IsMan;
  private boolean IsOnline;
  private String Job;
  private List<String> Languages;
  private String BlockList;
  private String LastOnline;
  private String Lat;
  private String Long;
  private List<String> MutedProfilesId;
  private boolean IsPrivate;
  private boolean PrivateMessaging;
  private boolean SettingsUsername;
  private boolean SettingsGender;
  private boolean SettingsJob;
  private boolean SettingsEmail;
  private boolean SettingsPhoneNumber;
  private boolean SettingsBirthday;
  private boolean SettingsLocation;
  private boolean SettingsLanguages;
  private boolean SettingsBiography;
  private boolean IsOfficial;
  private boolean IsFollowing;

  public Profile() {
  }


  protected Profile(Parcel in) {

    Id = in.readString();
    ImageAddress = in.readString();
    CoverPhoto = in.readString();
    Name = in.readString();
    PhoneNumber = in.readString();
    Username = in.readString();

    Biography = in.readString();
    Birthday = in.readString();
    CountryCode = in.readInt();
    Followers = in.createStringArrayList();
    Following = in.createStringArrayList();
    Requested = in.createStringArrayList();
    IsMan = in.readByte() != 0;
    IsOnline = in.readByte() != 0;
    Job = in.readString();
    Languages = in.createStringArrayList();
    BlockList = in.readString();

    LastOnline = in.readString();
    Lat = in.readString();
    Long = in.readString();
    MutedProfilesId = in.createStringArrayList();
    IsPrivate = in.readByte() != 0;
    PrivateMessaging = in.readByte() != 0;
    SettingsUsername = in.readByte() != 0;
    SettingsGender = in.readByte() != 0;
    SettingsJob = in.readByte() != 0;
    SettingsEmail = in.readByte() != 0;
    SettingsPhoneNumber = in.readByte() != 0;
    SettingsBirthday = in.readByte() != 0;
    SettingsLocation = in.readByte() != 0;
    SettingsLanguages = in.readByte() != 0;
    SettingsBiography = in.readByte() != 0;
    IsOfficial = in.readByte() != 0;
    IsFollowing = in.readByte() != 0;

  }

  public boolean getSettingsUsername() {
    return SettingsUsername;
  }

  public void setSettingsUsername(boolean settingsUsername) {
    SettingsUsername = settingsUsername;
  }

  public boolean getSettingsGender() {
    return SettingsGender;
  }

  public void setSettingsGender(boolean settingsGender) {
    SettingsGender = settingsGender;
  }

  public boolean getSettingsJob() {
    return SettingsJob;
  }

  public void setSettingsJob(boolean settingsJob) {
    SettingsJob = settingsJob;
  }

  public boolean getSettingsEmail() {
    return SettingsEmail;
  }

  public void setSettingsEmail(boolean settingsEmail) {
    SettingsEmail = settingsEmail;
  }

  public boolean getSettingsPhoneNumber() {
    return SettingsPhoneNumber;
  }

  public void setSettingsPhoneNumber(boolean settingsPhoneNumber) {
    SettingsPhoneNumber = settingsPhoneNumber;
  }

  public boolean getSettingsBirthday() {
    return SettingsBirthday;
  }

  public void setSettingsBirthday(boolean settingsBirthday) {
    SettingsBirthday = settingsBirthday;
  }

  public boolean getSettingsLocation() {
    return SettingsLocation;
  }

  public void setSettingsLocation(boolean settingsLocation) {
    SettingsLocation = settingsLocation;
  }

  public boolean getSettingsLanguages() {
    return SettingsLanguages;
  }

  public void setSettingsLanguages(boolean settingsLanguages) {
    SettingsLanguages = settingsLanguages;
  }

  public boolean getSettingsBiography() {
    return SettingsBiography;
  }

  public void setSettingsBiography(boolean settingsBiography) {
    SettingsBiography = settingsBiography;
  }

  public boolean isOfficial() {
    return IsOfficial;
  }

  public void setOfficial(boolean official) {
    IsOfficial = official;
  }

  public String getBiography() {
    return Biography;
  }

  public void setBiography(String biography) {
    Biography = biography;
  }

  public String getBirthday() {
    return Birthday;
  }

  public void setBirthday(String birthday) {
    Birthday = birthday;
  }

  public int getCountryCode() {
    return CountryCode;
  }

  public void setCountryCode(int countryCode) {
    CountryCode = countryCode;
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

  public List<String> getFollowing() {
    if (Following == null)
    {
      Following = new ArrayList<>();
    }
    return Following;
  }

  public List<String> getRequested() {

    if (Requested == null)
    {
      Requested = new ArrayList<>();
    }
    return Requested;
  }

  public void setFollowing(List<String> following) {
    Following = following;
  }

  public void setRequested(List<String> requested) {
    Requested = requested;
  }


  public boolean getIsMan() {
    return IsMan;
  }

  public void setIsMan(boolean isMan) {
    IsMan = isMan;
  }

  public boolean getIsOnline() {
    return IsOnline;
  }

  public void setIsOnline(boolean isOnline) {
    IsOnline = isOnline;
  }

  public String getJob() {
    return Job;
  }

  public void setJob(String job) {
    Job = job;
  }

  public List<String> getLanguages() {
    if (Languages == null)
    {
      Languages = new ArrayList<>();
    }
    return Languages;
  }

  public void setLanguages(List<String> languages) {
    Languages = languages;
  }

  public String getLastOnline() {
    return LastOnline;
  }

  public void setLastOnline(String lastOnline) {
    LastOnline = lastOnline;
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

  public List<String> getMutedProfilesId() {
    if (MutedProfilesId == null)
    {
      MutedProfilesId = new ArrayList<>();
    }
    return MutedProfilesId;
  }

  public void setMutedProfilesId(List<String> mutedProfilesId) {
    MutedProfilesId = mutedProfilesId;
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

    dest.writeString(Biography);
    dest.writeString(Birthday);
    dest.writeInt(CountryCode);
    dest.writeStringList(Followers);
    dest.writeStringList(Following);
    dest.writeStringList(Requested);
    dest.writeByte(IsMan ? (byte) 1 : (byte) 0);
    dest.writeByte(IsOnline ? (byte) 1 : (byte) 0);
    dest.writeString(Job);
    dest.writeStringList(Languages);
    dest.writeString(BlockList);
    dest.writeString(LastOnline);
    dest.writeString(Lat);
    dest.writeString(Long);
    dest.writeStringList(MutedProfilesId);
    dest.writeByte(IsPrivate ? (byte) 1 : (byte) 0);
    dest.writeByte(PrivateMessaging ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsUsername ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsGender ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsJob ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsEmail ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsPhoneNumber ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsBirthday ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsLocation ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsLanguages ? (byte) 1 : (byte) 0);
    dest.writeByte(SettingsBiography ? (byte) 1 : (byte) 0);
    dest.writeByte(IsOfficial ? (byte) 1 : (byte) 0);
    dest.writeByte(IsFollowing ? (byte) 1 : (byte) 0);

  }

  public String getBlockList() {
    return BlockList;
  }

  public void setBlockList(String blockList) {
    BlockList = blockList;
  }

  public boolean getIsPrivate() {
    return IsPrivate;
  }

  public void setIsPrivate(boolean isPrivate) {
    IsPrivate = isPrivate;
  }

  public boolean getPrivateMessaging() {
    return PrivateMessaging;
  }

  public void setPrivateMessaging(boolean privateMessaging) {
    PrivateMessaging = privateMessaging;
  }

  public boolean isFollowing() {
    return IsFollowing;
  }

  public void setFollowing(boolean following) {
    IsFollowing = following;
  }
}
