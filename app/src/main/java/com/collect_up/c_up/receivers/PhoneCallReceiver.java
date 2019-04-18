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
import android.telephony.TelephonyManager;

import java.util.Date;

public abstract class PhoneCallReceiver extends BroadcastReceiver {

  //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

  private static int lastState = TelephonyManager.CALL_STATE_IDLE;

  @Override
  public void onReceive(Context context, Intent intent) {
//
//    //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
//    String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
//    String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
//    int state = 0;
//    if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING))
//    {
//      state = TelephonyManager.CALL_STATE_RINGING;
//    }
//
//    onCallStateChanged(context, state, number);
  }

  //Derived classes should override these to respond to specific events of interest
  protected void onIncomingCallStarted(Context ctx, String number, Date start) {
  }

  //Deals with actual events

  //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
  public void onCallStateChanged(Context context, int state, String number) {
    if (lastState == state)
    {
      //No change, debounce extras
      return;
    }
    switch (state)
    {
      case TelephonyManager.CALL_STATE_RINGING:
        Date callStartTime = new Date();
        onIncomingCallStarted(context, number, callStartTime);
        break;
    }
    lastState = state;
  }
}