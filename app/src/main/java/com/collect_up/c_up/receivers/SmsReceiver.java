/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.GetStartActivity;
import com.collect_up.c_up.helpers.Logged;

public class SmsReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    //Prevent from starting activity if already logged in.
    if (Logged.Models.getUserProfile() != null)
    {
      return;
    }
    // Get the data (SMS data) bound to intent
    Bundle bundle = intent.getExtras();

    if (bundle != null)
    {

      final Object[] pdusObj = (Object[]) bundle.get("pdus");

      assert pdusObj != null;
      for (Object aPdusObj : pdusObj)
      {

        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);

        //String senderNumber = currentMessage.getDisplayOriginatingAddress();
        String messageBody = currentMessage.getDisplayMessageBody();

        // If the message body contains the app name
        if (messageBody.toLowerCase().contains(context.getString(R.string.app_name).toLowerCase()))
        {
          Intent getStartActivity = new Intent(context, GetStartActivity.class);
          getStartActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          getStartActivity.setAction("android.provider.Telephony.SMS_RECEIVED");

          if (!messageBody.contains(":") || !TextUtils.isDigitsOnly(messageBody.split(":")[1].trim()) || messageBody.split(":")[1].trim().length() != 5)
          {
            return;
          }

          int smsCode = Integer.parseInt(messageBody.split(":")[1].trim());

          getStartActivity.putExtra("activation_code", smsCode);
          context.startActivity(getStartActivity);
        }
      }
    }
  }
}