package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by collect-up3 on 10/31/2016.
 */

public class EnumMessageContentType implements Parcelable {
  public static int Text = 1;
  public static int Sticker = 2;
  public static int Image = 3;
  public static int Audio = 4;
  public static int Video = 5;
  public static int File = 6;
  public static int Announcement = 7;
  public static int Forwarded = 8;
  public static int Location = 9;
  public static int SharedProduct = 10;
  public static int SharedBusiness = 11;
  public static int SharedComplex = 12;
  public static int SharedProfile = 13;
  public static int SharedPost = 14;
  public static int SharedEvent = 15;


  protected EnumMessageContentType(Parcel in) {
    Text = in.readInt();
    Sticker = in.readInt();
    Image = in.readInt();
    Audio = in.readInt();
    Video = in.readInt();
    File = in.readInt();
    Announcement = in.readInt();
    Forwarded = in.readInt();
    Location = in.readInt();
    SharedProduct = in.readInt();
    SharedBusiness = in.readInt();
    SharedComplex = in.readInt();
    SharedProfile = in.readInt();
    SharedPost = in.readInt();
    SharedEvent = in.readInt();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(Text);
    dest.writeInt(Sticker);
    dest.writeInt(Image);
    dest.writeInt(Audio);
    dest.writeInt(Video);
    dest.writeInt(File);
    dest.writeInt(Announcement);
    dest.writeInt(Forwarded);
    dest.writeInt(Location);
    dest.writeInt(SharedProduct);
    dest.writeInt(SharedBusiness);
    dest.writeInt(SharedComplex);
    dest.writeInt(SharedProfile);
    dest.writeInt(SharedPost);
    dest.writeInt(SharedEvent);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<EnumMessageContentType> CREATOR = new Creator<EnumMessageContentType>() {
    @Override
    public EnumMessageContentType createFromParcel(Parcel in) {
      return new EnumMessageContentType(in);
    }

    @Override
    public EnumMessageContentType[] newArray(int size) {
      return new EnumMessageContentType[size];
    }
  };
}
