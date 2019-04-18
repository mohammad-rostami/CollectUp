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

import com.collect_up.c_up.BuildConfig;
import com.collect_up.c_up.R;


public class NonClickableVersionPreference extends Preference {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NonClickableVersionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public NonClickableVersionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NonClickableVersionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NonClickableVersionPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        String title = String.format(getContext().getString(R.string.pref_version), BuildConfig.VERSION_NAME);
        setTitle(title);
        setSelectable(false);
    }
}
