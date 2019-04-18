/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model.realm;

import io.realm.RealmObject;

@SuppressWarnings ("ClassHasNoToStringMethod")
public class RPostProfileTag extends RealmObject {
  private RProfile Profile;
  private RPost Post;
  private int X, Y;

  public RProfile getProfile() {
    return Profile;
  }

  public void setProfile(RProfile profile) {
    Profile = profile;
  }

  public RPost getPost() {
    return Post;
  }

  public void setPost(RPost post) {
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
}