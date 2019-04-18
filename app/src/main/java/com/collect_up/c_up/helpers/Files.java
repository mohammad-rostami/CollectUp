/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Contains helper static methods to work with images.
 */
public class Files {
  /**
   * Read file content from assets folder.
   *
   * @param context  Context
   * @param fileName File name
   * @return File content
   * @throws IOException
   */
  public static String readFileContentFromAssets(Context context, String fileName) throws
    IOException {
    String data;
    StringBuffer buffer = new StringBuffer();
    InputStream stream = context.getAssets().open(fileName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

    while ((data = reader.readLine()) != null)
    {
      buffer.append(data);
    }

    stream.close();

    return new String(buffer);
  }
}
