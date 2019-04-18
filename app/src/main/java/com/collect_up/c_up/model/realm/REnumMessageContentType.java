package com.collect_up.c_up.model.realm;

import io.realm.RealmObject;

/**
 * Created by collect-up3 on 11/2/2016.
 */

public class REnumMessageContentType extends RealmObject {
  private int enumValue;

  public void setEnumValue(int val) {
    this.enumValue = val;
  }

  public int getEnumValue() {
    return enumValue;
  }
}
