/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;

public class Log {
  private String Platform;
  private String SdkInt;
  private String MethodName;
  private String Message;
  private String ClassName;
  private String LineNumber;
  private String IpAddress;
  private String MacAddress;

  public String getIpAddress() {
    return IpAddress;
  }

  public void setIpAddress(String ipAddress) {
    IpAddress = ipAddress;
  }

  public String getMacAddress() {
    return MacAddress;
  }

  public void setMacAddress(String macAddress) {
    MacAddress = macAddress;
  }

  public String getMethodName() {
    return MethodName;
  }

  public void setMethodName(String methodName) {
    MethodName = methodName;
  }

  public String getPlatform() {
    return Platform;
  }

  public void setPlatform(String platform) {
    Platform = platform;
  }

  public String getSdkInt() {
    return SdkInt;
  }

  public void setSdkInt(String sdkInt) {
    SdkInt = sdkInt;
  }

  public String getMessage() {
    return Message;
  }

  public void setMessage(String message) {
    Message = message;
  }

  public String getClassName() {
    return ClassName;
  }

  public void setClassName(String className) {
    ClassName = className;
  }

  public String getLineNumber() {
    return LineNumber;
  }

  public void setLineNumber(String lineNumber) {
    LineNumber = lineNumber;
  }
}
