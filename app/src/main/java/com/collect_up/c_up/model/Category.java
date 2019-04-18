package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by collect-up3 on 7/27/2016.
 */
public class Category implements Parcelable {
  private int Count;
  private String Id;
  private String Name;

  protected Category(Parcel in) {
    Id = in.readString();
    Name = in.readString();
    Count = in.readInt();
  }

  public Category() {
  }

  public static final Creator<Category> CREATOR = new Creator<Category>() {
    @Override
    public Category createFromParcel(Parcel in) {
      return new Category(in);
    }

    @Override
    public Category[] newArray(int size) {
      return new Category[size];
    }
  };

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

  public int getCount() {
    return Count;
  }

  public void setCount(int count) {
    this.Count = count;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(Id);
    dest.writeString(Name);
    dest.writeInt(Count);
  }
}
