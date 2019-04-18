/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;

public class Activate {
  private String Name;
  private String PhoneNumber;
  private int ActivationCode;

  public int getActivationCode() {
    return ActivationCode;
  }

  public void setActivationCode(int activationCode) {
    ActivationCode = activationCode;
  }

  public String getPhoneNumber() {
    return PhoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    PhoneNumber = phoneNumber;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }
}
