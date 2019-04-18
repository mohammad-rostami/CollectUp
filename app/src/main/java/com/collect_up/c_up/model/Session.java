package com.collect_up.c_up.model;

/**
 * Created by collect-up3 on 7/20/2016.
 */
public class Session {

  private String Device;
  private String LastOnline;
  private String IpAddress;
  private String Id;
  private String Platform;
  private boolean Online;
  private boolean ActiveSession;

  public String getPaltform() {
    return Platform;
  }

  public void setPaltform(String paltform) {
    Platform = paltform;
  }

  public boolean isOnline() {
    return Online;
  }

  public void setOnline(boolean online) {
    Online = online;
  }


  public boolean isActiveSession() {
    return ActiveSession;
  }

  public void setActiveSession(boolean activeSession) {
    ActiveSession = activeSession;
  }

  public String getDevice() {
    return Device;
  }

  public void setDevice(String device) {
    Device = device;
  }

  public String getLastOnline() {
    return LastOnline;
  }

  public void setLastOnline(String lastOnline) {
    LastOnline = lastOnline;
  }

  public String getIpAddress() {
    return IpAddress;
  }

  public void setIpAddress(String ipAddress) {
    IpAddress = ipAddress;
  }

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

}
