/*
 * Created by Collect-up  on January 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.listeners;

import java.util.List;

public interface IPaginationCallback {
  <T> void onPageReceived(List<T> pageList);

  void onFailure();
}
