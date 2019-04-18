/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.collect_up.c_up.R;


public class SessionPreference extends Preference {
  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getLastOnline() {
    return lastOnline;
  }

  public void setLastOnline(String lastOnline) {
    this.lastOnline = lastOnline;
  }

  private String deviceName;

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  private String ipAddress;
  private String lastOnline;

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  private String platform;

  @TargetApi (Build.VERSION_CODES.LOLLIPOP)
  public SessionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    setLayoutResource(R.layout.session_prefrence);

  }

  public SessionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setLayoutResource(R.layout.session_prefrence);

  }

  public SessionPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    setLayoutResource(R.layout.session_prefrence);

  }

  public SessionPreference(Context context) {
    super(context);
    setLayoutResource(R.layout.session_prefrence);

  }


  @Override
  public void onBindViewHolder(PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);
    TextView txtDevice = (TextView) holder.findViewById(R.id.txtDevice);
    txtDevice.setText(deviceName);
    TextView txtIp = (TextView) holder.findViewById(R.id.txtIp);
    txtIp.setText(ipAddress);
    TextView txtLastOnline = (TextView) holder.findViewById(R.id.txtLastOnline);
    txtLastOnline.setText(lastOnline);
    TextView txtPlatform = (TextView) holder.findViewById(R.id.txtPlatform);
    txtPlatform.setText(platform);
  }


}
