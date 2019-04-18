package com.collect_up.c_up.model;

/**
 * Created by collect-up3 on 7/9/2016.
 */
public class LoginCredential {
  private String PhoneNumber;
  private String VerificationCode;
  private String Device;
  private String Platform;
  private String Password;

  public String getPassword() {
    return Password;
  }

  public void setPassword(String password) {
    Password = password;
  }

  public String getPlatform() {
    return Platform;
  }

  public void setPlatform(String platform) {
    this.Platform = platform;
  }

  public String getPhoneNumber() {
    return PhoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.PhoneNumber = phoneNumber;
  }

  public String getVerificationCode() {
    return VerificationCode;
  }

  public void setVerificationCode(String verificationCode) {
    this.VerificationCode = verificationCode;
  }

  public String getDevice() {
    return Device;
  }

  public void setDevice(String device) {
    this.Device = device;
  }
}
