/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;


public class NonClickableSummaryPreference extends Preference {
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public NonClickableSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  public NonClickableSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public NonClickableSummaryPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public NonClickableSummaryPreference(Context context) {
    super(context);
    init();
  }

  private void init() {
    setSelectable(false);
  }
}
