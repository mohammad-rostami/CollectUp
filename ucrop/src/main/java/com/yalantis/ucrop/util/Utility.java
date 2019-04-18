package com.yalantis.ucrop.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.yalantis.ucrop.model.MediaContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Utility {
  public static Bitmap getBitmapFromFilePath(String filePath) {
    InputStream fileInputStream = null;
    try
    {
      fileInputStream = new FileInputStream(filePath);
    } catch (FileNotFoundException e)
    {
    }
    int reqWidth = 755;

    BitmapFactory.Options bitmapOptionsd = new BitmapFactory.Options();
    bitmapOptionsd.inJustDecodeBounds = true;
    BitmapFactory.decodeStream(fileInputStream, null, bitmapOptionsd);

    int req = bitmapOptionsd.outWidth / reqWidth;

    BitmapFactory.Options bitmapOptionsdf = new BitmapFactory.Options();

    bitmapOptionsdf.inSampleSize = req;
    bitmapOptionsdf.inJustDecodeBounds = false;

    InputStream fileInputStreams = null;
    try
    {
      fileInputStreams = new FileInputStream(filePath);
    } catch (FileNotFoundException e)
    {
    }
    return BitmapFactory.decodeStream(fileInputStreams, null, bitmapOptionsdf);
  }

  public static long getFileSize(final File file) {
    if (file == null || !file.exists())
    {
      return 0;
    }
    if (!file.isDirectory())
    {
      return file.length();
    }
    final List<File> dirs = new LinkedList<File>();
    dirs.add(file);
    long result = 0;
    while (!dirs.isEmpty())
    {
      final File dir = dirs.remove(0);
      if (!dir.exists())
      {
        continue;
      }
      final File[] listFiles = dir.listFiles();
      if (listFiles == null || listFiles.length == 0)
      {
        continue;
      }
      for (final File child : listFiles)
      {
        result += child.length();
        if (child.isDirectory())
        {
          dirs.add(child);
        }
      }
    }

    return result;
  }

  public static Bitmap compressJpeg(Context context, String path) throws IOException {


    Bitmap bitmap = getBitmapFromFilePath(path);
  /*  File file;
    if (replace)
    {
      file = new File(uri.getPath());
    } else
    {
      int index = uri.getPath().lastIndexOf("/");
      String fileName = uri.getPath().substring(index + 1);
      file = createTempFile(context, rootPath, fileName);
    }*/

    long size_file = getFileSize(new File(path));

    size_file = (size_file) / 1000;
    int quality = 1;

    if (size_file <= 250)
    {
      quality = 100;
    } else if (size_file > 250 && size_file < 500)
    {

      quality = 90;

    } else if (size_file >= 500 && size_file < 1000)
    {

      quality = 70;

    } else if (size_file >= 1000 && size_file < 1500)
    {

      quality = 60;

    } else if (size_file >= 1500 && size_file <= 2500)
    {

      quality = 50;
    } else
    {
      quality = 35;
    }

    OutputStream outputStream = null;
    try
    {
      int index = path.lastIndexOf("/");
      String fileName = path.substring(index + 1);
      File file = File.createTempFile(fileName, ".jpg", context.getCacheDir());

      outputStream = new FileOutputStream(file);
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
    return bitmap;
    //return file.getAbsolutePath();
  }

  public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
    int sourceWidth = source.getWidth();
    int sourceHeight = source.getHeight();

    // Compute the scaling factors to fit the new height and width, respectively.
    // To cover the final image, the final scaling will be the bigger
    // of these two.
    float xScale = (float) newWidth / sourceWidth;
    float yScale = (float) newHeight / sourceHeight;
    float scale = Math.max(xScale, yScale);

    // Now get the size of the source bitmap when scaled
    float scaledWidth = scale * sourceWidth;
    float scaledHeight = scale * sourceHeight;

    // Let's find out the upper left coordinates if the scaled bitmap
    // should be centered in the new size give by the parameters
    float left = (newWidth - scaledWidth) / 2;
    float top = (newHeight - scaledHeight) / 2;

    // The target rectangle for the new, scaled version of the source bitmap will now
    // be
    RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

    // Finally, we create a new bitmap of the specified size and draw our new,
    // scaled bitmap onto it.
    Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
    Canvas canvas = new Canvas(dest);
    canvas.drawBitmap(source, null, targetRect, null);

    return dest;
  }

  public static Bitmap getThumbnail(ContentResolver cr, String path, int type) throws Exception {

    if (type == MediaContent.IS_IMAGE)
    {
      Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.MediaColumns._ID},
        MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
      if (ca != null && ca.moveToFirst())
      {
        int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
        ca.close();
        return MediaStore.Images.Thumbnails.getThumbnail(cr, id,
          MediaStore.Images.Thumbnails.MICRO_KIND, null);
      }

      if (ca != null)
      {
        ca.close();
      }
    } else
    {
      return ThumbnailUtils.createVideoThumbnail(path,
        MediaStore.Images.Thumbnails.MINI_KIND);
    }

    return null;

  }

  /**
   * A safe way to get an instance of the Camera object.
   */
  public static Camera getCameraInstance() {


    Camera c;
    try
    {
      c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera instance
    } catch (Exception e)
    {
      // Camera is not available (in use or does not exist)
      return null;
    }
    return c; // returns null if camera is unavailable
  }

  /**
   * Check if this device has a camera
   */
  public static boolean checkCameraHardware(Context context) {
    // this device has a camera
    // no camera on this device
    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
  }

  public static String getTime(long milliseconds) {

    long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
    long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));

    if (minutes > 9 && seconds > 9)
    {
      return String.format(Locale.getDefault(), " %d:%d",
        minutes, seconds);
    } else if (minutes > 9 && seconds <= 9)
    {
      return String.format(Locale.getDefault(), " %d:0%d",
        minutes, seconds);
    } else if (minutes <= 9 && seconds > 9)
    {
      return String.format(Locale.getDefault(), " 0%d:%d",
        minutes, seconds);
    } else
    {
      return String.format(Locale.getDefault(), " 0%d:0%d",
        minutes, seconds);
    }
  }

  public static long getMediaTime(Uri uri, Context context) {
try {
  MediaMetadataRetriever retriever = new MediaMetadataRetriever();
  retriever.setDataSource(context, uri);
  String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
  if (time == null) {
    return 0;
  }
  return Long.parseLong(time);
}
catch(Exception ex) {

  return  0;
}
  }

}
