/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.fragments.FragmentNotifications;
import com.collect_up.c_up.model.Notification;

import org.greenrobot.eventbus.EventBus;

import java.util.Observable;

public class AppNotificationUtils extends Observable {
  public static boolean notificationExists = false;
  public static int mUnSeenNotifications = 0;
  public static int mUnSeenRequestNotifications = 0;

  public int getUnSeenNotifications() {
    return mUnSeenNotifications;
  }

  public int getUnSeenRequestNotifications() {
    return mUnSeenRequestNotifications;
  }

  public void setUnSeenNotifications(int mUnSeenNotifications) {
    AppNotificationUtils.mUnSeenNotifications = mUnSeenNotifications;
    EventBus.getDefault().post(new OnNotifArrived(true));

    setChanged();
    notifyObservers();
  }

  public void setUnSeenRequestNotifications(int mUnSeenNotifications) {
    AppNotificationUtils.mUnSeenRequestNotifications = mUnSeenNotifications;
    EventBus.getDefault().post(new OnNotifArrived(true));

    setChanged();
    notifyObservers();
  }

  private boolean isRequestNotifi(Notification notification) {
    if (notification.getText().contains("Request"))
    {
      return true;
    } else
    {
      return false;
    }
  }

  public void makeNotification(final Context context, Notification notification) {
    setUnSeenNotifications(mUnSeenNotifications + 1);
    if (isRequestNotifi(notification))
    {
      setUnSeenRequestNotifications(mUnSeenRequestNotifications + 1);
    }

    final NotificationProducer notificationProducer = new NotificationProducer(notification, context);
    if (notificationProducer.getSendNotification())
    {
      if (!FragmentNotifications.isRunning)
      {
        final String notificationTitle = context.getString(R.string.app_name);
        if (notificationExists)
        {
          cancelCreatedNotification(context);

          Intent intent = new Intent(context, ActivityHome.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
          intent.putExtra("unseen_notifications", mUnSeenNotifications);
          intent.putExtra("unseen_requests", mUnSeenRequestNotifications);
          PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
          createNotification(context, notificationTitle, context.getString(R.string.you_have_new_notifications), pendingIntent, null, null);
        } else
        {
          if (!Utils.isNullOrEmpty(notificationProducer.getImageAddress()) && !notificationProducer.getImageAddress().equals("*"))
          {
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
              @Override
              protected Void doInBackground(Void... params) {
                Bitmap bitmap = Utils.getBitmapFromURL(context, Constants.General.BLOB_PROTOCOL + notificationProducer.getImageAddress());
                Bitmap postBitmap = null;
                if (notificationProducer.getPostImageAddress() != null)
                {
                  postBitmap = Utils.getBitmapFromURL(context, Constants.General.BLOB_PROTOCOL + notificationProducer.getPostImageAddress());
                }
                PendingIntent pendingIntent = null;

                if (notificationProducer.getIntent() != null)
                {
                  pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), notificationProducer.getIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
                }


                createNotification(context, notificationTitle, notificationProducer.getText(), pendingIntent, bitmap, postBitmap);

                return null;
              }
            };
            asyncTask.execute();
          } else
          {
            PendingIntent pendingIntent = null;
            if (notificationProducer.getIntent() != null)
            {
              pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), notificationProducer.getIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
            }
            createNotification(context, notificationTitle, notificationProducer.getText(), pendingIntent, null, null);

          }
        }
      }

      notificationExists = true;
    }
  }

  private void cancelCreatedNotification(Context context) {
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    manager.cancel("CUpApp", 1);
    notificationExists = false;
  }

  public void cancelNotification(Context context) {
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    manager.cancel("CUpApp", 1);
    notificationExists = false;
    setUnSeenNotifications(0);
  }

  public void seenRequestNotification(Context context) {
    setUnSeenRequestNotifications(mUnSeenRequestNotifications - 1);
  }

  private void createNotification(Context context, String contentTitle, String contentText, PendingIntent pendingIntent, Bitmap bitmap, Bitmap postBitmap) {
    android.app.Notification.Builder builder = new android.app.Notification.Builder(context);
    boolean vibrate = PreferenceHelper.getBoolean(context, "prefEnableVibrate");
    boolean sound = PreferenceHelper.getBoolean(context, "prefEnableSound");

    if (sound)
    {
      Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);
      builder.setSound(alarmSound);
    }
    if (vibrate)
    {
      builder.setVibrate(new long[] {500, 200, 200, 500, 500});
    }
    builder.setContentTitle(contentTitle).setContentText(contentText).setSmallIcon(R.drawable.ic_logo_notification).setPriority(android.app.Notification.PRIORITY_HIGH);

    builder.setAutoCancel(true);
    if (postBitmap != null)
    {
      builder.setStyle(new android.app.Notification.BigPictureStyle().bigPicture(postBitmap));
    }
    if (bitmap == null)
    {

      builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
        R.drawable.ic_no_logo));
    } else
    {

      builder.setLargeIcon(bitmap);
    }

    if (pendingIntent != null)
    {
      builder.setContentIntent(pendingIntent);
    }

    android.app.Notification notification = builder.build();
    notification.ledARGB = 0xff00ff00;
    notification.flags = android.app.Notification.FLAG_SHOW_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;
    notification.ledOnMS = 300;
    notification.ledOffMS = 1000;
    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify("CUpApp", 1, notification);
  }

}
