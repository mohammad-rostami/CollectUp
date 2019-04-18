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


public class EventComplex implements Parcelable {
  private String Title;
  private String Message;
  private String StartDateTime;
  private String EndDateTime;
  private String InsertTime;
  private String ComplexId;
  private String Id;

  public String getComplexId() {
    return ComplexId;
  }

  public String getId() {
    return Id;
  }

  public String getTitle() {
    return Title;
  }

  public void setComplexId(String complexId) {
    ComplexId = complexId;
  }

  public void setId(String id) {
    Id = id;
  }

  public void setTitle(String title) {
    Title = title;
  }

  public String getMessage() {
    return Message;
  }

  public void setMessage(String message) {
    Message = message;
  }

  public String getStartDateTime() {
    return StartDateTime;
  }

  public void setStartDateTime(String startDateTime) {
    StartDateTime = startDateTime;
  }

  public String getEndDateTime() {
    return EndDateTime;
  }

  public void setEndDateTime(String endDateTime) {
    EndDateTime = endDateTime;
  }

  public String getInsertTime() {
    return InsertTime;
  }

  public void setInsertTime(String insertTime) {
    InsertTime = insertTime;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(Title);
    dest.writeString(Message);
    dest.writeString(StartDateTime);
    dest.writeString(EndDateTime);
    dest.writeString(InsertTime);
    dest.writeString(ComplexId);
    dest.writeString(Id);
  }

  public EventComplex() {
  }

  protected EventComplex(Parcel in) {
    Title = in.readString();
    Message = in.readString();
    StartDateTime = in.readString();
    EndDateTime = in.readString();
    InsertTime = in.readString();
    ComplexId = in.readString();
    Id = in.readString();
  }

  public static final Parcelable.Creator<EventComplex> CREATOR = new Parcelable.Creator<EventComplex>() {
    @Override
    public EventComplex createFromParcel(Parcel source) {
      return new EventComplex(source);
    }

    @Override
    public EventComplex[] newArray(int size) {
      return new EventComplex[size];
    }
  };
}
