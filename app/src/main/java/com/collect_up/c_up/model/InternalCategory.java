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

public class InternalCategory implements Parcelable {
  public static final Creator<InternalCategory> CREATOR = new Creator<InternalCategory>() {
    @Override
    public InternalCategory createFromParcel(Parcel source) {
      return new InternalCategory(source);
    }

    @Override
    public InternalCategory[] newArray(int size) {
      return new InternalCategory[size];
    }
  };
  private String Id;
  private String Name;
  private String BusinessId;
  private int Count;

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getBusinessId() {
    return BusinessId;
  }

  public void setBusinessId(String businessId) {
    BusinessId = businessId;
  }

  public int getCount() {
    return Count;
  }

  public void setCount(int count) {
    Count = count;
  }

  public InternalCategory() {
  }

  protected InternalCategory(Parcel in) {
    Id = in.readString();
    Name = in.readString();
    BusinessId = in.readString();
    Count = in.readInt();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(Id);
    dest.writeString(Name);
    dest.writeString(BusinessId);
    dest.writeInt(Count);

  }

}
