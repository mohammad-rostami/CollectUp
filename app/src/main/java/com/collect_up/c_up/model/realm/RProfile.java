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
public class RProfile extends RealmObject {
  private String Biography;
  private String Birthday;
  private int CountryCode;
  private String Email;
  private RealmList<RealmString> Followers;
  private RealmList<RealmString> Following;
  private RealmList<RealmString> Requested;


  @PrimaryKey

  private String Id;
  private String ImageAddress;
  private String CoverPhoto;
  private boolean IsMan;
  private boolean IsOnline;
  private String Job;

  public String getCoverPhoto() {
    return CoverPhoto;
  }

  public void setCoverPhoto(String coverPhoto) {
    CoverPhoto = coverPhoto;
  }

  private RealmList<RealmString> Languages;
  private String LastOnline;
  private String Lat;
  private String Long;
  private RealmList<RealmString> MutedProfilesId;
  private String Name;
  private String PhoneNumber;
  private String Username;
  private String BlockList;
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

  public RProfile() {
  }

  public RealmList<RealmString> getRequested() {
    return Requested;
  }

  public void setRequested(RealmList<RealmString> requested) {
    Requested = requested;
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

  public boolean getIsOfficial() {
    return IsOfficial;
  }

  public void setIsOfficial(boolean official) {
    IsOfficial = official;
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

  public RealmList<RealmString> getFollowing() {
    return Following;
  }

  public void setFollowing(RealmList<RealmString> following) {
    Following = following;
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

  public void setImageAddress(String imageAddress) {
    ImageAddress = imageAddress;
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

  public RealmList<RealmString> getLanguages() {
    return Languages;
  }

  public void setLanguages(RealmList<RealmString> languages) {
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

  public RealmList<RealmString> getMutedProfilesId() {
    return MutedProfilesId;
  }

  public void setMutedProfilesId(RealmList<RealmString> mutedProfilesId) {
    MutedProfilesId = mutedProfilesId;
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

  public String getUsername() {
    return Username;
  }

  public void setUsername(String username) {
    Username = username;
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

  public boolean getIsFollowing() {
    return IsFollowing;
  }

  public void setIsFollowing(boolean following) {
    IsFollowing = following;
  }
}
