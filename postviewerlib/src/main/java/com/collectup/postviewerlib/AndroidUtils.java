package com.collectup.postviewerlib;

import android.content.res.Resources;
import android.os.Build;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by collect-up3 on 4/28/2016.
 */
public class AndroidUtils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public AndroidUtils() {
    }

    public static int dpToPx(int dp) {
        return (int)((float)dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int generateViewId() {
        if(Build.VERSION.SDK_INT >= 17) {
            return View.generateViewId();
        } else {
            int result;
            int newValue;
            do {
                result = sNextGeneratedId.get();
                newValue = result + 1;
                if(newValue > 16777215) {
                    newValue = 1;
                }
            } while(!sNextGeneratedId.compareAndSet(result, newValue));

            return result;
        }
    }
}
