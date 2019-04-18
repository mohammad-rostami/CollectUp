/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;

public class CheckOut {
  private int Id;
  private String Value;

  public String getValue() {
    return Value;
  }

  public void setValue(String value) {
    Value = value;
  }

  public int getId() {
    return Id;
  }

  public void setId(int id) {
    Id = id;
  }
}
