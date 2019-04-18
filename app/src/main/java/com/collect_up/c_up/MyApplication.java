/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.widget.ImageView;

import com.collect_up.c_up.activities.ActivityUnCaughtException;
import com.collect_up.c_up.chat.NativeLoader;
import com.collect_up.c_up.helpers.AppNotificationUtils;
import com.collect_up.c_up.helpers.Utils;
import com.crashlytics.android.Crashlytics;
import com.danikula.videocache.HttpProxyCacheServer;
import com.karumi.dexter.Dexter;
import com.loopj.android.http.RequestHandle;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;
import com.squareup.picasso.Picasso;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends MultiDexApplication {

  public static volatile Handler applicationHandler = null;
  public static String twoStepPass;
  public static HashMap<String, Float> mUploadingFiles = new HashMap<>();
  public static List<RequestHandle> mUploadingFileHandlers = new ArrayList<>();
  public static List<MyApplication.DownloadRequestStruct> mDownloadRequests = new ArrayList<>();
  public static ThinDownloadManager downloadManager;
  private static MyApplication mInstance;
  public static Context context;
  AppNotificationUtils appNotificationUtils;
  private ImageLoader mImageLoader;
  private HttpProxyCacheServer proxy;
  public static DisplayImageOptions imageOptionsWithAnimation;

  public static HttpProxyCacheServer getProxy(Context context) {
    MyApplication app = (MyApplication) context.getApplicationContext();
    return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
  }

  private HttpProxyCacheServer newProxy() {
    return new HttpProxyCacheServer(this);
  }

  public static boolean deleteDir(File dir) {
    if ((dir != null) && dir.isDirectory())
    {
      String[] children = dir.list();
      for (String aChildren : children)
      {
        boolean success = deleteDir(new File(dir, aChildren));
        if (!success)
        {
          return false;
        }
      }

      return dir.delete();
    }

    return false;
  }

  public void addDownloadRequest(DownloadRequest request, String messageId) {

    for (MyApplication.DownloadRequestStruct struct : mDownloadRequests)
    {
      if (struct.messageId.equals(messageId))
      {
        cancelDownloadRequest(messageId);
        break;
      }
    }

    int id = downloadManager.add(request);

    MyApplication.DownloadRequestStruct struct = new MyApplication.DownloadRequestStruct();

    struct.messageId = messageId;
    struct.progress = 0.0F;
    struct.request = request;
    struct.id = id;

    mDownloadRequests.add(struct);
  }

  public boolean cancelDownloadRequest(String messageId) {
    for (MyApplication.DownloadRequestStruct struct : mDownloadRequests)
    {
      if (struct.messageId.equals(messageId))
      {
        if (downloadManager != null)
        {

          if (struct.request.isCancelled())
          {
            mDownloadRequests.remove(struct);
            continue;
          }

          struct.request.cancel();
          mDownloadRequests.remove(struct);
          break;
        }
      }
    }

    return false;
  }

  @Nullable
  public MyApplication.DownloadRequestStruct hasRequestDownload(String messageId) {
    for (MyApplication.DownloadRequestStruct struct : mDownloadRequests)
    {
      if (struct.messageId.equals(messageId))
      {
        return struct;
      }
    }

    return null;
  }

  public void cancelUploadHandler(String tag) {
    if (!Utils.isNullOrEmpty(tag))
    {
      for (RequestHandle requestHandle : mUploadingFileHandlers)
      {
        if (requestHandle.getTag() != null && requestHandle.getTag().equals(tag))
        {
          requestHandle.cancel(false);
          mUploadingFileHandlers.remove(requestHandle);
          break;
        }
      }
    }
  }

  public ImageLoader getImageLoader() {
    ///  getRequestQueue();
    if (mImageLoader == null)
    {
      DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .resetViewBeforeLoading(true)
        .handler(new Handler())
        // .displayer(new FadeInBitmapDisplayer(500, true, true, true))
        .build();
      File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());
      ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getApplicationContext());

      try
      {
        LruDiskCache cache = new LruDiskCache(cacheDir, new HashCodeFileNameGenerator(), 100 * 1024 * 1024);

        // Create global configuration and initialize ImageLoader with this config
        config.diskCache(cache);
        config.defaultDisplayImageOptions(defaultOptions);
      } catch (IOException e)
      {
        e.printStackTrace();
      }
      if (BuildConfig.DEBUG)
      {
        config.writeDebugLogs();
      }
      mImageLoader = ImageLoader.getInstance();
      mImageLoader.init(config.build());
    }
    return this.mImageLoader;
  }


  public AppNotificationUtils getObserver() {
    return appNotificationUtils;
  }

  public void clearApplicationData() {
    File cache = getCacheDir();
    File appDir = new File(cache.getParent());
    if (appDir.exists())
    {
      String[] children = appDir.list();
      for (String s : children)
      {
        if (!s.equals("lib"))
        {
          deleteDir(new File(appDir, s));
        }
      }
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    final Fabric fabric = new Fabric.Builder(this)
      .kits(new Crashlytics())
      .debuggable(true)
      .build();
    Fabric.with(fabric);
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    //Dexter.initialize(getApplicationContext());
    context = getApplicationContext();
    mInstance = this;
    imageOptionsWithAnimation = new DisplayImageOptions.Builder()
      .cacheInMemory(true)
      .cacheOnDisk(true)
      .resetViewBeforeLoading(true)
      .handler(new Handler())
      .showImageOnLoading(R.drawable.placeholder)
      .displayer(new FadeInBitmapDisplayer(500, true, true, true))
      .build();
    //initialize and create the image loader logic
    DrawerImageLoader.init(new AbstractDrawerImageLoader() {
      @Override
      public void set(ImageView imageView, Uri uri, Drawable placeholder) {
        Picasso.with(getApplicationContext()).load(uri).placeholder(placeholder).into(imageView);
      }

      @Override
      public void cancel(ImageView imageView) {
        Picasso.with(getApplicationContext()).cancelRequest(imageView);
      }
    });

    downloadManager = new ThinDownloadManager();

    appNotificationUtils = new AppNotificationUtils();

    // Initialise Hawk
    Hawk.init(this)
      .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
      .setStorage(HawkBuilder.newSharedPrefStorage(this))
      .setLogLevel(LogLevel.NONE)
      .build();

    applicationHandler = new Handler(getInstance().getMainLooper());

    NativeLoader.initNativeLibs(MyApplication.getInstance());

    if (!BuildConfig.DEBUG)
    {
      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable e) {
          handleUncaughtException(e);
        }
      });
    }
  }

  public static synchronized MyApplication getInstance() {
    return mInstance;
  }

  // here you can handle all unexpected crashes
  public final void handleUncaughtException(Throwable e) {
    e.printStackTrace();
    Crashlytics.logException(e);

//// not all Android versions will print the stack trace automatically
//    Intent intent = new Intent(this, ActivityUnCaughtException.class);
//    intent.putExtra("exception", e);
//    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // required when starting from Application
//    startActivity(intent);
//    //   android.os.Process.killProcess(android.os.Process.myPid());
//    System.exit(0);
  }

  public void updateDownloadRequestProgress(String messageId, float progress) {
    for (MyApplication.DownloadRequestStruct struct : mDownloadRequests)
    {
      if (struct.messageId.equals(messageId))
      {
        int pos = mDownloadRequests.indexOf(struct);
        struct.progress = progress;
        mDownloadRequests.set(pos, struct);
      }
    }
  }

  public static class DownloadRequestStruct {
    public int id;
    public String messageId;
    public DownloadRequest request;
    public float progress;

    @Override
    public final String toString() {
      return "DownloadRequestStruct{" +
        "id=" + id +
        ", messageId='" + messageId + '\'' +
        ", request=" + request +
        ", progress=" + progress +
        '}';
    }
  }
}
