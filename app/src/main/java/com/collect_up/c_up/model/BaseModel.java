package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.collect_up.c_up.helpers.Utils;

/**
 * Created by collect-up3 on 10/2/2016.
 */
public class BaseModel implements Parcelable {
  public static final int BUSINESS_LEVEL_NONE = 0;
  public static final int BUSINESS_LEVEL_DIAMOND = 1;
  public static final int BUSINESS_LEVEL_GOLD = 2;
  public static final int BUSINESS_LEVEL_SILVER = 3;
  public static final int BUSINESS_LEVEL_BRONZE = 4;
  public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {
    @Override
    public BaseModel createFromParcel(Parcel source) {
      return new BaseModel(source);
    }

    @Override
    public BaseModel[] newArray(int size) {
      return new BaseModel[size];
    }
  };

  protected BaseModel(Parcel in) {
    if (in != null)
    {
      Id = in.readString();
      ImageAddress = in.readString();
      CoverPhoto = in.readString();
      Name = in.readString();
      PhoneNumber = in.readString();
      Username = in.readString();
    }
  }

  public BaseModel() {
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
  }

  protected String Id;
  protected String ImageAddress;
  protected String CoverPhoto;
  protected String Name;
  protected String PhoneNumber;
  protected String Username;

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
    }
    return "";
  }

  public void setImageAddress(String imageAddress) {
    ImageAddress = imageAddress;
  }

  public String getCoverPhoto() {
    return CoverPhoto;
  }

  public void setCoverPhoto(String coverPhoto) {
    CoverPhoto = coverPhoto;
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
}
