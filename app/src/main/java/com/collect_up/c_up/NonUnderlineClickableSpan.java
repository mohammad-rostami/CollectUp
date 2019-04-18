/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up;

import android.text.TextPaint;
import android.text.style.ClickableSpan;

public abstract class NonUnderlineClickableSpan extends ClickableSpan {
  @Override
  public final void updateDrawState(TextPaint ds) {
    super.updateDrawState(ds);

    ds.setUnderlineText(false);
  }
}
