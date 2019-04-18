package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by collect-up3 on 1/12/2017.
 */

public class PostProfileTag implements Parcelable {
  private Profile Profile;
  private Post Post;
  private int X, Y;

  public com.collect_up.c_up.model.Profile getProfile() {
    return Profile;
  }

  public void setProfile(com.collect_up.c_up.model.Profile profile) {
    Profile = profile;
  }

  public com.collect_up.c_up.model.Post getPost() {
    return Post;
  }

  public void setPost(com.collect_up.c_up.model.Post post) {
    Post = post;
  }

  public int getX() {
    return X;
  }

  public void setX(int x) {
    X = x;
  }

  public int getY() {
    return Y;
  }

  public void setY(int y) {
    Y = y;
  }


  public static final Creator<PostProfileTag> CREATOR = new Creator<PostProfileTag>() {
    @Override
    public PostProfileTag createFromParcel(Parcel in) {
      return new PostProfileTag(in);
    }

    @Override
    public PostProfileTag[] newArray(int size) {
      return new PostProfileTag[size];
    }
  };

  public PostProfileTag() {

  }

  protected PostProfileTag(Parcel in) {
    Profile = in.readParcelable(Profile.class.getClassLoader());
    Post = in.readParcelable(Post.class.getClassLoader());
    X = in.readInt();
    Y = in.readInt();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(Profile, 0);
    dest.writeParcelable(Post, 0);
    dest.writeInt(X);
    dest.writeInt(Y);
  }
}

