/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.app.Notification;
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
import com.collect_up.c_up.fragments.FragmentConversations;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.model.realm.RCompactMessage;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class ChatNotificationUtils {

  private static void createNotification(Context context, String contentTitle, String contentText, boolean vibrate, boolean sound, PendingIntent pendingIntent, Bitmap bitmap) {
    if (PreferenceHelper.getBoolean(context, "prefMuteChatNotifications"))
    {
      Notification.Builder builder = new Notification.Builder(context);
      if (sound)
      {
        Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);
        builder.setSound(alarmSound);
      }
      if (vibrate)
      {
        builder.setVibrate(new long[] {500, 200, 200, 500, 500});
      }
      if (contentText.contains("lat"))
      {
        contentText = context.getResources().getString(R.string.location);
      }
      builder.setContentTitle(contentTitle).setContentText(contentText).setSmallIcon(R.drawable.ic_logo_notification).setPriority(Notification.PRIORITY_HIGH);

      if (bitmap == null)
      {
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
          R.drawable.ic_no_logo));
      } else
      {
        builder.setLargeIcon(bitmap);
      }

      builder.setAutoCancel(true);
      if (pendingIntent == null)
      {
        if (!FragmentConversations.isRunning)
        {
          Intent intent = new Intent(context, FragmentConversations.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
          PendingIntent pending = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
          builder.setContentIntent(pending);
        }
      } else
      {
        builder.setContentIntent(pendingIntent);
      }
      Notification notification = builder.build();
      notification.ledARGB = 0xff00ff00;
      notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
      notification.ledOnMS = 300;
      notification.ledOffMS = 1000;
      ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify("CUpChat", 0, notification);
    }
  }

  public static void sendNotification(final Context context, final ArrayList<RCompactMessage> messages, final boolean vibrate, final boolean sound) {
    if (messages.size() < 1)
    {
      return;
    }
    final List<Object> gharardad = notificationTextMaker(context, messages);
    if (gharardad.isEmpty())
    {
      return;
    }
    PendingIntent pendingIntent = null;
    if (!gharardad.get(3).equals("null"))
    {
      Intent intent;
      RChat chat = (RChat) gharardad.get(3);
      if (chat.getIsGroup())
      {
        intent = new Intent(context, ActivityHome.class);
        intent.putExtra("ischat", false);

      } else
      {
        intent = new Intent(context, ActivityHome.class);
        intent.putExtra("ischat", true);

      }
      intent.putExtra("data", RToNonR.rChatToChat((RChat) gharardad.get(3)));
      pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    Bitmap[] bitmaps = new Bitmap[1];
    bitmaps[0] = null;
    if (gharardad.get(1).equals("null") || gharardad.get(1).equals(""))
    {
      bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

      createNotification(context, (String) gharardad.get(0), (String) gharardad.get(2), vibrate, sound, pendingIntent, bitmaps[0]);
    } else
    {
      final PendingIntent finalPendingIntent = pendingIntent;

      AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          Bitmap bitmap = Utils.getBitmapFromURL(context, Constants.General.BLOB_PROTOCOL + gharardad.get(1));

          createNotification(context, (String) gharardad.get(0), (String) gharardad.get(2), vibrate, sound, finalPendingIntent, bitmap);

          return null;
        }
      };
      asyncTask.execute();
    }
  }

  public static List<Object> notificationTextMaker(Context context, ArrayList<RCompactMessage> messages) {
    ArrayList<Object> output = new ArrayList<>();
    Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));

    if (messages.size() == 1 && !AppUtils.isChatMuted(messages.get(0).getChatId()))
    {
      RChat chat = realm.where(RChat.class).equalTo("ChatId", messages.get(0).getChatId()).findFirst();
      if (chat != null)
      {
        String chatName = null;
        String imageAddress = null;

        chatName = chat.getTitle();
        imageAddress = chat.getProfileThumbnailAddress();


        output.add(chatName);
        output.add(imageAddress == null ? "null" : imageAddress);

        int contentType = messages.get(0).getContentType();
        if (contentType == EnumMessageContentType.Sticker)
        {
          output.add("STICKER");

        } else if (contentType == EnumMessageContentType.Location)
        {
          output.add("LOCATION");

        } else if (contentType == EnumMessageContentType.File)
        {
          output.add("FILE");

        } else if (contentType == EnumMessageContentType.Image)
        {
          output.add("IMAGE");

        } else if (contentType == EnumMessageContentType.Video)
        {
          output.add("VIDEO");

        } else if (contentType == EnumMessageContentType.Audio)
        {
          output.add("AUDIO");

        } else
        {
          output.add(messages.get(0).getText());
        }


        output.add(chat);
      }
    } else
    {
      List<String> chatIds = new ArrayList<>();
      int newMessagesCount = 0;
      for (RCompactMessage message : messages)
      {
        if (!AppUtils.isChatMuted(message.getChatId()) && chatIds.indexOf(message.getChatId()) < 0)
        {
          chatIds.add(message.getChatId());
        }
        // count messages in un-muted chats only
        if (!AppUtils.isChatMuted(message.getChatId()))
        {
          newMessagesCount++;
        }
      }

      if (chatIds.size() == 0)
      {
        return output;
      }

      if (chatIds.size() == 1)
      {
        RChat chat = realm.where(RChat.class).equalTo("ChatId", chatIds.get(0)).findFirst();

        String chatName = null;
        String imageAddress = null;

        chatName = chat.getTitle();
        imageAddress = chat.getProfileThumbnailAddress();


        output.add(chatName);
        output.add(imageAddress == null ? "null" : imageAddress);
        output.add(String.format(context.getString(R.string.notification_chats_messages_one_chat), newMessagesCount));
        output.add(chat);
      } else
      {
        output.add(context.getString(R.string.app_name));
        output.add("null");
        output.add(String.format(context.getString(R.string.notification_chats_messages), newMessagesCount, chatIds.size()));
        output.add("null");
      }
    }

    return output;
  }

  public static void cancelNotification(Context context) {
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    manager.cancel("CUpChat", 0);
  }
}
