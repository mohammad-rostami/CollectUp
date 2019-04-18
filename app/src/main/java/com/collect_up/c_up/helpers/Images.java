/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Contains helper static methods to work with images.
 */
public class Images {

  public static int[] getImageDimensions(String filePath) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(filePath, options);

    return new int[] {options.outWidth, options.outHeight};
  }

  /**
   * Converts an array of bytes to bitmap.
   *
   * @param bytes An image in bytes
   * @return Converted bitmap
   */
  public static Bitmap bytesToBitmap(byte[] bytes) {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
  }

  /**
   * Makes bitmap from Uri
   *
   * @param context  Context
   * @param imageUri Image URI
   * @return Made bitmap
   * @throws IOException
   */
  public static Bitmap getBitmapFromUri(Context context, Uri imageUri) throws IOException {
    return MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
  }

  public static int calculateInSampleSize(BitmapFactory.Options options,
                                          int reqWidth,
                                          int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth)
    {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth)
      {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  /**
   * Makes bitmap from file path.
   *
   * @param filePath File path
   * @return Made bitmap
   */
  public static Bitmap getBitmapFromFilePath(Context context, String filePath) {
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

  public static String compressJpeg(Context context, String filePath, String rootPath, boolean replace) throws IOException {


    Bitmap bitmap = getBitmapFromFilePath(context, filePath);
    File file;
    if (replace)
    {
      file = new File(filePath);
    } else
    {
      int index = filePath.lastIndexOf("/");
      String fileName = filePath.substring(index + 1);
      file = createTempFile(context, rootPath, fileName);
    }

    long size_file = getFileSize(new File(filePath));

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
      outputStream = new FileOutputStream(file);
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
    return file.getAbsolutePath();
  }

  public static void saveBitmap(Context context, String fileName, Bitmap bitmap) {
    FileOutputStream out = null;
    try
    {
      Utils.checkForAppPathsExistence(context);

      out = new FileOutputStream(fileName);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
      // PNG is a lossless format, the compression factor (100) is ignored
    } catch (Exception e)
    {
    } finally
    {
      try
      {
        if (out != null)
        {
          out.close();
        }
      } catch (IOException e)
      {
      }
    }
  }

  /**
   * Gets the URI of an image.
   *
   * @param context Context
   * @param bitmap  An image
   * @return URI of the image
   */
  public static Uri getImageUriFromBitmap(Context context, Bitmap bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
    {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
      String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
      return Uri.parse(path);

    } else
    {
      String path = Constants.General.APP_FOLDER_IMAGE_PATH + "Title.jpg";
      saveBitmap(context, path, bitmap);
      return Uri.parse(path);
    }

  }

  public static File createTempFile(Context context, String rootPath) throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

    String imageFileName = "JPEG_" + timeStamp + "_";

    Utils.checkForAppPathsExistence(context);

    File image = File.createTempFile(imageFileName,  /* prefix */
      ".jpg",         /* suffix */
      new File(rootPath)      /* directory */);

    Logged.General.setTempTakePhotoFilePath(image.getAbsolutePath());
    // Save a file: path for use with ACTION_VIEW intents
    return image;
  }

  public static File createTempFile(Context context, String rootPath, String fileName) throws IOException {
    Utils.checkForAppPathsExistence(context);


    File image = File.createTempFile(fileName,  /* prefix */
      ".jpg",         /* suffix */
      new File(rootPath)      /* directory */);

    Logged.General.setTempTakePhotoFilePath(image.getAbsolutePath());
    // Save a file: path for use with ACTION_VIEW intents
    return image;
  }

  public static Uri getUriFromFileName(String fileName) {
    return Uri.fromFile(new File(fileName));
  }

  public static byte[] bitmapToBytes(Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

    return stream.toByteArray();
  }

  public static String getImageUrlWithAuthority(Context context, Uri uri) {
    InputStream is = null;
    if (uri.getAuthority() != null)
    {
      try
      {
        is = context.getContentResolver().openInputStream(uri);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        return writeToTempImageAndGetPathUri(context, bmp);
      } catch (FileNotFoundException e)
      {
        e.printStackTrace();
      } finally
      {
        try
        {
          is.close();
        } catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public static void deleteImage(Context context, String imagePath) {
    File file = new File(imagePath);
    boolean deleted = file.delete();
    if (deleted)
    {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
      {
        Intent mediaScanIntent = new Intent(
          Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
      } else
      {
        context.sendBroadcast(new Intent(
          Intent.ACTION_MEDIA_MOUNTED,
          Uri.parse("file://"
            + Environment.getExternalStorageDirectory())));
      }
    }
  }

  public static String writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
    return getRealPathFromURI(inContext, Uri.parse(path));
  }

  public static String getRealPathFromURI(Context context, Uri uri) {
    String path;
    if ("content".equals(uri.getScheme()))
    {
      Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
      cursor.moveToFirst();
      int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
      path = cursor.getString(idx);
      cursor.close();
    } else
    {
      path = uri.getPath();
    }

    return path;
  }

  /**
   * Resizes bitmap files if the file width is greater than the new width parameter.
   *
   * @param file     Bitmap file
   * @param newWidth New width
   * @return Resized bitmap
   */
  public Bitmap resize(Bitmap file, int newWidth) {
    int width = file.getWidth();
    if (width > newWidth)
    {
      int height = file.getHeight();
      float scaleWidth = ((float) newWidth) / width;
      // Create a matrix for the manipulation
      Matrix matrix = new Matrix();
      // Resize the bitmap
      matrix.postScale(scaleWidth, scaleWidth);

      // Recreate the new bitmap
      return Bitmap.createBitmap(file, 0, 0, width, height, matrix, false);
    }

    return file;
  }
}
