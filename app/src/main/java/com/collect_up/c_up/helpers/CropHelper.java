/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.net.Uri;
import android.support.v4.app.Fragment;

import com.collect_up.c_up.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class CropHelper {
  public static void beginCrop(Fragment activity, Uri source, boolean isSquere) {
    Uri destination = Uri.fromFile(new File(activity.getContext().getCacheDir(), "cropped"));
    UCrop uCrop =
      UCrop.of(source, destination)
//        .withAspectRatio(16, 9)
//      .withMaxResultSize(100, 150)

      ;
    UCrop.Options options = new UCrop.Options();
    options.setCompressionQuality(5);
    options.setActiveWidgetColor(activity.getResources().getColor(R.color.colorAccent));
    options.setToolbarColor(activity.getResources().getColor(R.color.colorAccent));
    options.setStatusBarColor(activity.getResources().getColor(R.color.primary_dark));

    if (isSquere)
    {
      uCrop.withAspectRatio(1, 1);
    } else
    {
      options.setFreeStyleCropEnabled(true);
    }


    uCrop.withOptions(options);
    uCrop.start(activity.getActivity(), activity);
    // Crop.of(source, destination).asSquare().withMaxSize(400, 400).start(activity);
  }
}
