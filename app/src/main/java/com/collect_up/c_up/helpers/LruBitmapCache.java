/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;


public class LruBitmapCache extends LruCache<String, Bitmap> {
  public LruBitmapCache(Context context) {
    this(getDefaultLruCacheSize(context));
  }

  public LruBitmapCache(int sizeInKiloBytes) {
    super(sizeInKiloBytes);
  }

  public static int getDefaultLruCacheSize(Context context) {
    int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    return 1024 * 1024 * memClass / 8;
  }

  @Override
  protected int sizeOf(String key, Bitmap value) {
    return value.getByteCount() / 1024;
  }

}