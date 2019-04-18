package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by collect-up3 on 10/31/2016.
 */

public class EnumProfileStatus implements Parcelable {
  public static int None = 0;
  public static int IsTyping = 1;
  public static int RecordingAudio = 2;
  public static int SendingFile = 3;


  protected EnumProfileStatus(Parcel in) {
    None = in.readInt();
    IsTyping = in.readInt();
    RecordingAudio = in.readInt();
    SendingFile = in.readInt();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(None);
    dest.writeInt(IsTyping);
    dest.writeInt(RecordingAudio);
    dest.writeInt(SendingFile);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<EnumProfileStatus> CREATOR = new Creator<EnumProfileStatus>() {
    @Override
    public EnumProfileStatus createFromParcel(Parcel in) {
      return new EnumProfileStatus(in);
    }

    @Override
    public EnumProfileStatus[] newArray(int size) {
      return new EnumProfileStatus[size];
    }
  };
}
