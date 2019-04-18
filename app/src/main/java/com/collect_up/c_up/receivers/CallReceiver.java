/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.receivers;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.collect_up.c_up.fragments.GetStartFragments;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Utils;

import java.lang.reflect.Method;
import java.util.Date;

public class CallReceiver extends PhoneCallReceiver {

  @Override
  protected void onIncomingCallStarted(Context ctx, String number, Date start) {
    try
    {
      TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
      Class c = Class.forName(tm.getClass().getName());
      Method m = c.getDeclaredMethod("getITelephony");
      m.setAccessible(true);
      ITelephony telephonyService = (ITelephony) m.invoke(tm);
      if (!Utils.isNullOrEmpty(number) && number.contains(Constants.General.APP_PHONE_NUMBER) && GetStartFragments.isVerified)
      {
        telephonyService.endCall();

              /*  if (!GetStartActivity.isFirstTimeCallReceived) {
                    Intent getStartActivity = new Intent(ctx, GetStartActivity.class);
                    getStartActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    getStartActivity.setAction("android.intent.action.PHONE_STATE");

                    ctx.startActivity(getStartActivity);
                }*/
      }

    } catch (Exception e)
    {
    }
  }
}