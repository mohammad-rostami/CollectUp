/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;

import com.collect_up.c_up.model.CompactChat;

import java.util.Comparator;

public class SortChats implements Comparator<CompactChat> {
  private final Context mContext;

  public SortChats(Context context) {
    mContext = context;
  }


  @Override
  public int compare(CompactChat lhs, CompactChat rhs) {
    if (lhs.getLastMessage() != null && rhs.getLastMessage() != null)
    {
      long lhsDateTime = lhs.getLastMessage().getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(mContext, lhs.getLastMessage().getSendDateTime()) : Long.valueOf(lhs.getLastMessage().getSendDateTime());
      long rhsDateTime = rhs.getLastMessage().getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(mContext, rhs.getLastMessage().getSendDateTime()) : Long.valueOf(rhs.getLastMessage().getSendDateTime());
      return Long.valueOf(rhsDateTime).compareTo(lhsDateTime);
    } else
    {
      return 0;
    }
  }
}