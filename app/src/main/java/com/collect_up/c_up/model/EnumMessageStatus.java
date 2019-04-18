package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by collect-up3 on 10/31/2016.
 */

public class EnumMessageStatus implements Parcelable {
  public static int Sent = 1;
  public static int Delivered = 2;
  public static int Seen = 3;
  public static int Draft = 4;
  public static int Failed = 5;


  protected EnumMessageStatus(Parcel in) {
    Sent = in.readInt();
    Delivered = in.readInt();
    Seen = in.readInt();
    Draft = in.readInt();
    Failed = in.readInt();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(Sent);
    dest.writeInt(Delivered);
    dest.writeInt(Seen);
    dest.writeInt(Draft);
    dest.writeInt(Failed);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<EnumMessageStatus> CREATOR = new Creator<EnumMessageStatus>() {
    @Override
    public EnumMessageStatus createFromParcel(Parcel in) {
      return new EnumMessageStatus(in);
    }

    @Override
    public EnumMessageStatus[] newArray(int size) {
      return new EnumMessageStatus[size];
    }
  };
}
