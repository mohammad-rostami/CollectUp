package com.collect_up.c_up.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.style.URLSpan;
import android.view.View;

import com.collect_up.c_up.R;
import com.collect_up.c_up.util.CustomTabsHelper;

/**
 * Created by collect-up3 on 8/17/2016.
 */
public class CustomTabsURLSpan extends URLSpan {
    private final Activity _Activity;

    public CustomTabsURLSpan(Activity activity, String url) {
        super(url);
        _Activity = activity;

    }

    public CustomTabsURLSpan(Parcel src) {
        super(src);
        _Activity = null;
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        builder.setToolbarColor(ContextCompat.getColor(_Activity, R.color.colorAccent));
        builder.addDefaultShareMenuItem();
        String packageName = CustomTabsHelper.getPackageNameToUse(_Activity);
        if (packageName != null)
            customTabsIntent.intent.setPackage(packageName);
        customTabsIntent.launchUrl(_Activity, Uri.parse(url));
    }


}
