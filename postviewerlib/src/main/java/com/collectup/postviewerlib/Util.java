package com.collectup.postviewerlib;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by collect-up3 on 5/1/2016.
 */
public class Util {

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels ;
        return width;
    }
}
