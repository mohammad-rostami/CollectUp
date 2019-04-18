/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up;

import io.realm.RealmObject;

@SuppressWarnings ("ClassHasNoToStringMethod")
public class RealmString extends RealmObject {
  private String string;

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }
}
