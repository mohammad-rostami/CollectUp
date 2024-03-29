/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.chat;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NativeLoader {

  private final static int LIB_VERSION = 4;
  private final static String LIB_NAME = "chat." + LIB_VERSION;
  private final static String LIB_SO_NAME = "lib" + LIB_NAME + ".so";
  private final static String LOCALE_LIB_SO_NAME = "lib" + LIB_NAME + "loc.so";

  private static volatile boolean nativeLoaded = false;

  public static synchronized void initNativeLibs(Context context) {
    if (nativeLoaded)
    {
      return;
    }

    try
    {
      String folder;

      try
      {
        if (Build.CPU_ABI.equalsIgnoreCase("armeabi-v7a"))
        {
          folder = "armeabi-v7a";
        } else if (Build.CPU_ABI.equalsIgnoreCase("armeabi"))
        {
          folder = "armeabi";
        } else if (Build.CPU_ABI.equalsIgnoreCase("x86"))
        {
          folder = "x86";
        } else if (Build.CPU_ABI.equalsIgnoreCase("mips"))
        {
          folder = "mips";
        } else
        {
          folder = "armeabi";
        }
      } catch (Exception e)
      {
        folder = "armeabi";
      }


      String javaArch = System.getProperty("os.arch");
      if (javaArch != null && javaArch.contains("686"))
      {
        folder = "x86";
      }

      File destFile = getNativeLibraryDir(context);
      if (destFile != null)
      {
        destFile = new File(destFile, LIB_SO_NAME);
        if (destFile.exists())
        {
          try
          {
            System.loadLibrary(LIB_NAME);
            nativeLoaded = true;
            return;
          } catch (Error e)
          {
          }
        }
      }

      File destDir = new File(context.getFilesDir(), "lib");
      destDir.mkdirs();

      File destLocalFile = new File(destDir, LOCALE_LIB_SO_NAME);
      if (destLocalFile.exists())
      {
        try
        {
          System.load(destLocalFile.getAbsolutePath());
          nativeLoaded = true;
          return;
        } catch (Error e)
        {
        }
        destLocalFile.delete();
      }


      if (loadFromZip(context, destDir, destLocalFile, folder))
      {
        return;
      }

            /*
            folder = "x86";
                destLocalFile = new File(context.getFilesDir().getAbsolutePath() + "/libtmessages86.so");
                if (!loadFromZip(context, destLocalFile, folder)) {
                    destLocalFile = new File(context.getFilesDir().getAbsolutePath() + "/libtmessagesarm.so");
                    folder = "armeabi";
                    loadFromZip(context, destLocalFile, folder);
                }
             */
    } catch (Throwable e)
    {
    }

    try
    {
      System.loadLibrary(LIB_NAME);
      nativeLoaded = true;
    } catch (Error e)
    {
    }
  }

  private static File getNativeLibraryDir(Context context) {
    File f = null;
    if (context != null)
    {
      try
      {
        f = new File((String) ApplicationInfo.class.getField("nativeLibraryDir")
          .get(context.getApplicationInfo()));
      } catch (Throwable th)
      {
      }
    }
    if (f == null)
    {
      assert context != null;
      f = new File(context.getApplicationInfo().dataDir, "lib");
    }
    if (f.isDirectory())
    {
      return f;
    }
    return null;
  }

  private static boolean loadFromZip(Context context,
                                     File destDir,
                                     File destLocalFile,
                                     String folder) {
    try
    {
      for (File file : destDir.listFiles())
      {
        file.delete();
      }
    } catch (Exception e)
    {
    }

    ZipFile zipFile = null;
    InputStream stream = null;
    try
    {
      zipFile = new ZipFile(context.getApplicationInfo().sourceDir);
      ZipEntry entry = zipFile.getEntry("lib/" + folder + "/" + LIB_SO_NAME);
      if (entry == null)
      {
        throw new Exception("Unable to find file in apk:" + "lib/" + folder + "/" + LIB_NAME);
      }
      stream = zipFile.getInputStream(entry);

      OutputStream out = new FileOutputStream(destLocalFile);
      byte[] buf = new byte[4096];
      int len;
      while ((len = stream.read(buf)) > 0)
      {
        Thread.yield();
        out.write(buf, 0, len);
      }
      out.close();

      if (Build.VERSION.SDK_INT >= 9)
      {
        destLocalFile.setReadable(true, false);
        destLocalFile.setExecutable(true, false);
        destLocalFile.setWritable(true);
      }

      try
      {
        System.load(destLocalFile.getAbsolutePath());
        nativeLoaded = true;
      } catch (Error e)
      {
      }
      return true;
    } catch (Exception e)
    {
    } finally
    {
      if (stream != null)
      {
        try
        {
          stream.close();
        } catch (Exception e)
        {
        }
      }
      if (zipFile != null)
      {
        try
        {
          zipFile.close();
        } catch (Exception e)
        {
        }
      }
    }
    return false;
  }
}
