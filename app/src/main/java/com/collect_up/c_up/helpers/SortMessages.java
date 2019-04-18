/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.model.realm.RCompactMessage;

import java.util.Comparator;

public enum SortMessages implements Comparator<RCompactMessage> {

  DESC
    {
      @Override
      public int compare(RCompactMessage lhs, RCompactMessage rhs) {
        long lhsDateTime = lhs.getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(MyApplication.getInstance().getApplicationContext(), lhs.getSendDateTime()) : Long.valueOf(lhs.getSendDateTime());
        long rhsDateTime = rhs.getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(MyApplication.getInstance().getApplicationContext(), rhs.getSendDateTime()) : Long.valueOf(rhs.getSendDateTime());
        return Long.valueOf(rhsDateTime).compareTo(lhsDateTime);
      }
    },
  ASC
    {
      @Override
      public int compare(RCompactMessage lhs, RCompactMessage rhs) {
        long lhsDateTime = lhs.getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(MyApplication.getInstance().getApplicationContext(), lhs.getSendDateTime()) : Long.valueOf(lhs.getSendDateTime());
        long rhsDateTime = rhs.getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(MyApplication.getInstance().getApplicationContext(), rhs.getSendDateTime()) : Long.valueOf(rhs.getSendDateTime());
        return Long.valueOf(lhsDateTime).compareTo(rhsDateTime);
      }
    };
}
