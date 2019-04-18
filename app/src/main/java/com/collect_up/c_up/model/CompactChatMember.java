package com.collect_up.c_up.model;

/**
 * Created by collect-up3 on 10/31/2016.
 */

public class CompactChatMember {
  private boolean IsSuperAdmin;
  private boolean IsManager;
  private String UserId;
  private String ThumbnailAddress;
  private String Name;
  private String UserName;
  private String LastOnline;

  public boolean getIsSuperAdmin() {
    return IsSuperAdmin;
  }

  public void setIsSuperAdmin(boolean isSuperAdmin) {
    IsSuperAdmin = isSuperAdmin;
  }

  public boolean getIsManager() {
    return IsManager;
  }

  public void setIsManager(boolean isManager) {
    IsManager = isManager;
  }

  public String getUserId() {
    return UserId;
  }

  public void setUserId(String userId) {
    UserId = userId;
  }

  public String getThumbnailAddress() {
    return ThumbnailAddress;
  }

  public void setThumbnailAddress(String thumbnailAddress) {
    ThumbnailAddress = thumbnailAddress;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getUserName() {
    return UserName;
  }

  public void setUserName(String userName) {
    UserName = userName;
  }

  public String getLastOnline() {
    return LastOnline;
  }

  public void setLastOnline(String lastOnline) {
    LastOnline = lastOnline;
  }


}
