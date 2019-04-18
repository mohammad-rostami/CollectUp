/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;

import com.collect_up.c_up.model.EnumMessageStatus;
import com.collect_up.c_up.model.realm.RCompactMessage;

import java.util.ArrayList;
import java.util.HashSet;

import io.realm.Realm;
import io.realm.RealmResults;

public class DatabaseUtils {
  public static ArrayList<RCompactMessage> unSeenMessages(Context context) {
    ArrayList<RCompactMessage> messageArrayList = new ArrayList<>();
    Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));

    RealmResults<RCompactMessage> messages1 = realm.where(RCompactMessage.class).findAll();
    for (RCompactMessage rMessage : messages1)
    {
      if (rMessage.getSenderId() != null
        && !rMessage.getSenderId().equals(Logged.Models.getUserProfile().getId())
        && rMessage.getMessageStatus() != EnumMessageStatus.Seen)

      {
        messageArrayList.add(rMessage);
      }
    }


    return removeDuplicates(messageArrayList);
  }

  private static ArrayList<RCompactMessage> removeDuplicates(ArrayList<RCompactMessage> list) {

    // Store unique items in result.
    ArrayList<RCompactMessage> result = new ArrayList<>();

    // Record encountered Strings in HashSet.
    HashSet<String> set = new HashSet<>();
    // Loop over argument list.
    for (RCompactMessage item : list)
    {

      // If String is not in set, add it to the list and the set.
      if (!set.contains(item.getMessageId()))
      {
        result.add(item);
        set.add(item.getMessageId());
      }
    }
    return result;
  }
}
