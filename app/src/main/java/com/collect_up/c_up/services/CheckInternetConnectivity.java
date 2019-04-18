/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.collect_up.c_up.helpers.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class CheckInternetConnectivity extends Service {

  public static boolean isConnectedToInternet = false;

  public RealtimeService getmRealtimeService() {

    return RealtimeService.backgroundService;
  }


  @Override
  public void onCreate() {
    super.onCreate();
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        int lastConnectResult = 0;
        for (int i = 0; i < 5; i++)
        {
          if (hasActiveInternetConnection())
          {
            lastConnectResult++;
          }
        }
        if (lastConnectResult >= 3)
        {
          setIsConnected(true);
        } else
        {
          setIsConnected(false);
        }
      }
    }, 200, 1500);
  }

  public boolean hasActiveInternetConnection() {
    if (isNetworkAvailable())
    {
      try
      {

        HttpURLConnection urlc = (HttpURLConnection) (new URL(Constants.General.PROTOCOL + Constants.General.SERVER_URL + Constants.Server.OAuth.SERVER_STATE).openConnection());
        urlc.setRequestProperty("User-Agent", "Android");
        urlc.setRequestProperty("Connection", "close");
        urlc.setConnectTimeout(1500);
        urlc.connect();
        return (urlc.getResponseCode() == 200);
      } catch (IOException ignored)
      {

      }
    }
    return false;
  }

  public void setIsConnected(boolean connected) {
    if (isConnectedToInternet != connected)
    {
      if (connected)
      {
        if (getmRealtimeService() != null)
        {
          getmRealtimeService().initConnection();
        }
      } else
      {
        try
        {
          if (RealtimeService.connection != null)
          {
            RealtimeService.connection.stop();
          }
        } catch (Exception ex)
        {
        }
      }
    } else if (connected)
    {
      if (getmRealtimeService() != null && !getmRealtimeService().isConnectedToServer)
      {
        getmRealtimeService().initConnection();
      }
    }

    this.isConnectedToInternet = connected;
  }

  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager
      = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
