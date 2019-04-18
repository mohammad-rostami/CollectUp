/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class PreferenceHelper {
  public static boolean getBoolean(Context context, String prefKey) {
    SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(context);

    return manager.getBoolean(prefKey, context.getResources().getBoolean(context.getResources().getIdentifier(prefKey, "bool", context.getPackageName())));
  }
}
