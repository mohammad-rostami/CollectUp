/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppUtils {
  public static boolean isChatMuted(String chatId) {
    List<String> mutedChats = Hawk.get("mutedChats");

    return (mutedChats != null) && mutedChats.contains(chatId);
  }

  public static void unMuteChat(String chatId) {
    List<String> mutedChats = Hawk.get("mutedChats");

    if (mutedChats == null)
    {
      mutedChats = new ArrayList<>();
    }

    if (mutedChats.contains(chatId))
    {
      mutedChats.remove(chatId);
      Hawk.put("mutedChats", mutedChats);
    }
  }

  public static String exportFileName(String filePath) {
    File file = new File(filePath);

    if (file.exists())
    {
      return file.getName();
    }

    return null;
  }

  public static void muteChat(String chatId) {
    List<String> mutedChats = Hawk.get("mutedChats");

    if (mutedChats == null)
    {
      mutedChats = new ArrayList<>();
    }

    if (!mutedChats.contains(chatId))
    {
      mutedChats.add(chatId);
      Hawk.put("mutedChats", mutedChats);
    }
  }
}
