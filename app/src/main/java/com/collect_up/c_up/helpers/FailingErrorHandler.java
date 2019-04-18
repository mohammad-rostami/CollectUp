package com.collect_up.c_up.helpers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.collect_up.c_up.activities.GetStartActivity;
import com.orhanobut.hawk.Hawk;

import java.io.File;

/**
 * Created by collect-up3 on 7/19/2016.
 */
public class FailingErrorHandler {
    public static void onErrorHandler(int statusCode, Context context) {
        switch (statusCode) {
            case 404:
                break;
            case 403:
                clearApplicationData(context);
                Hawk.clear();
                Intent intent = new Intent(context, GetStartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(new Intent());
                break;
        }
    }

    private static void clearApplicationData(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s + " DELETED");
                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
