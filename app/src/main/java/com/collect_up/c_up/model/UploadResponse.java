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

public class UploadResponse implements Parcelable {
  public static final Parcelable.Creator<UploadResponse> CREATOR = new Parcelable.Creator<UploadResponse>() {
    @Override
    public UploadResponse createFromParcel(Parcel source) {
      return new UploadResponse(source);
    }

    @Override
    public UploadResponse[] newArray(int size) {
      return new UploadResponse[size];
    }
  };
  private boolean IsExists;
  private String Content;
  private String FileName;

  public UploadResponse() {
  }

  protected UploadResponse(Parcel in) {
    IsExists = in.readByte() != 0;
    Content = in.readString();
    FileName = in.readString();
  }

  public boolean getIsExists() {
    return IsExists;
  }

  public void setIsExists(boolean isExists) {
    IsExists = isExists;
  }

  public String getContent() {
    return Content;
  }

  public void setContent(String content) {
    Content = content;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeByte(IsExists ? (byte) 1 : (byte) 0);
    dest.writeString(Content);
    dest.writeString(FileName);
  }

  public String getFileName() {
    return FileName;
  }

  public void setFileName(String fileName) {
    FileName = fileName;
  }
}
