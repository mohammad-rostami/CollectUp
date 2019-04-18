/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

public class BasicGridLayoutManager extends GridLayoutManager {
    private final UltimateViewAdapter mAdapter;

    protected static class VIEW_TYPES {
        public static final int HEADER = 1;
        public static final int FOOTER = 2;
    }

    protected final GridLayoutManager.SpanSizeLookup mSpanSizeLookUp = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            if (mAdapter.getItemViewType(position) == VIEW_TYPES.FOOTER) {
                return getSpanCount();
            } else if (mAdapter.getItemViewType(position) == VIEW_TYPES.HEADER) {
                return getSpanCount();
            } else {
                return getNormalSpanCount();
            }
        }
    };

    protected int getNormalSpanCount() {
        return 1;
    }

    protected GridLayoutManager.SpanSizeLookup decideSpanSizeCal() {
        return mSpanSizeLookUp;
    }

    public BasicGridLayoutManager(Context context, int spanCount, UltimateViewAdapter mAdapter) {
        super(context, spanCount);
        this.mAdapter = mAdapter;
        setSpanSizeLookup(decideSpanSizeCal());
    }
}