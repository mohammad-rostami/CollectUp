/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view.chat;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.collect_up.c_up.chat.AndroidUtilities;


public class SizeNotifierRelativeLayout extends RelativeLayout {

    public SizeNotifierRelativeLayoutDelegate delegate;
    private Rect rect = new Rect();

    public SizeNotifierRelativeLayout(Context context) {
        super(context);
    }

    public SizeNotifierRelativeLayout(android.content.Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SizeNotifierRelativeLayout(android.content.Context context,
                                      AttributeSet attrs,
                                      int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Calculate the soft keyboard height and report back to listener
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (delegate != null) {
            View rootView = this.getRootView();
            int usableViewHeight = rootView.getHeight() - AndroidUtilities.statusBarHeight - AndroidUtilities
                    .getViewInset(getContext(),rootView);
            this.getWindowVisibleDisplayFrame(rect);
            int keyboardHeight = usableViewHeight - (rect.bottom - rect.top);
            delegate.onSizeChanged(keyboardHeight);
        }
    }


    public interface SizeNotifierRelativeLayoutDelegate {
        void onSizeChanged(int keyboardHeight);
    }


}
