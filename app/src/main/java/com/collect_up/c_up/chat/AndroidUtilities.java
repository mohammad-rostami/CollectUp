/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.collect_up.c_up.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;


public class AndroidUtilities {

  public static float density = 1;
  public static int statusBarHeight = 0;
  public static Point displaySize = new Point();

  static
  {
    density = MyApplication.getInstance().getResources().getDisplayMetrics().density;
    checkDisplaySize();
  }

  public static int dp(float value) {
    return (int) Math.ceil(density * value);
  }

  public static void runOnUIThread(Runnable runnable) {
    runOnUIThread(runnable, 0);
  }

  public static void runOnUIThread(Runnable runnable, long delay) {
    if (delay == 0)
    {
      MyApplication.applicationHandler.post(runnable);
    } else
    {
      MyApplication.applicationHandler.postDelayed(runnable, delay);
    }
  }

  public native static void loadBitmap(String path,
                                       Bitmap bitmap,
                                       int scale,
                                       int width,
                                       int height,
                                       int stride);

  public static boolean copyFile(InputStream sourceFile, File destFile) throws IOException {
    OutputStream out = new FileOutputStream(destFile);
    byte[] buf = new byte[4096];
    int len;
    while ((len = sourceFile.read(buf)) > 0)
    {
      Thread.yield();
      out.write(buf, 0, len);
    }
    out.close();
    return true;
  }

  public static boolean copyFile(File sourceFile, File destFile) throws IOException {
    if (!destFile.exists())
    {
      destFile.createNewFile();
    }

    FileChannel channel = new FileInputStream(sourceFile).getChannel();
    FileChannel fileChannel = new FileOutputStream(destFile).getChannel();
    fileChannel.transferFrom(channel, 0, channel.size());

    channel.close();
    fileChannel.close();

    return true;
  }

  public static void checkDisplaySize() {
    try
    {
      WindowManager manager = (WindowManager) MyApplication.getInstance()
        .getSystemService(Context.WINDOW_SERVICE);
      if (manager != null)
      {
        Display display = manager.getDefaultDisplay();
        if (display != null)
        {
          if (android.os.Build.VERSION.SDK_INT < 13)
          {
            displaySize.set(display.getWidth(), display.getHeight());
          } else
          {
            display.getSize(displaySize);
          }
        }
      }
    } catch (Exception ignored)
    {
    }
  }

  public static int getViewInset(Context context, View view) {
    if (view == null || Build.VERSION.SDK_INT < 21)
    {
      return 0;
    }
    try
    {
      Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
      mAttachInfoField.setAccessible(true);
      Object mAttachInfo = mAttachInfoField.get(view);
      if (mAttachInfo != null)
      {
        Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
        mStableInsetsField.setAccessible(true);
        Rect insets = (Rect) mStableInsetsField.get(mAttachInfo);
        return insets.bottom;
      }
    } catch (Exception e)
    {
    }
    return 0;
  }


}
