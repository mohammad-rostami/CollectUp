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

import com.collect_up.c_up.fragments.FragmentChat;
import com.collect_up.c_up.fragments.FragmentConversations;
import com.collect_up.c_up.fragments.FragmentGroupChat;
import com.collect_up.c_up.fragments.FragmentGroupChatInfo;
import com.collect_up.c_up.helpers.AppUtils;
import com.collect_up.c_up.helpers.ChatNotificationUtils;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.DatabaseUtils;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.PreferenceHelper;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IUpdateChatUICallbacks;
import com.collect_up.c_up.listeners.IUpdateConversationUICallbacks;
import com.collect_up.c_up.listeners.IUpdateGroupChatInfoUICallbacks;
import com.collect_up.c_up.listeners.IUpdateGroupChatUICallbacks;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.EnumMessageStatus;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.model.realm.RCompactMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

public class RealtimeReceiver extends BroadcastReceiver {
  private static IUpdateChatUICallbacks mUpdateChatUICallbacks;
  private static IUpdateConversationUICallbacks mUpdateConversationUICallbacks;
  private static IUpdateGroupChatUICallbacks mUpdateGroupChatUICallbacks;
  private static IUpdateGroupChatInfoUICallbacks mUpdateGroupChatInfoUICallbacks;

  public RealtimeReceiver(IUpdateChatUICallbacks callback) {
    mUpdateChatUICallbacks = callback;
  }

  public RealtimeReceiver(IUpdateGroupChatInfoUICallbacks callback) {
    mUpdateGroupChatInfoUICallbacks = callback;
  }

  public RealtimeReceiver(IUpdateGroupChatUICallbacks callback) {
    mUpdateGroupChatUICallbacks = callback;
  }

  public RealtimeReceiver(IUpdateConversationUICallbacks callback) {
    mUpdateConversationUICallbacks = callback;
  }

  // Required for RealtimeService
  public RealtimeReceiver() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Constants.General.UPDATE_CHAT_UI))
    {
      switch (intent.getStringExtra("method"))
      {
        case "onMessageReceived":
          onMessageReceived(intent, context);
          break;
        case "onMessageSent":
          onMessageSent(context, intent);
          break;
        /*case "onMessageDelivered":
          onMessageDelivered(context, intent);
          break;*/
        case "onMessageSeen":
          onMessageSeen(context, intent);
          break;
        case "onChatInfoUpdated":
          onChatInfoUpdated(context, intent);
          break;
        case "onNewChat":
          onNewChat(context, intent);
          break;
        case "onChatUpdated":
          onChatUpdated(context, intent);
          break;
        case "onInitializing":
          onInitializing(intent);
          break;
        case "onAddSettingsMessage":
          onAddSettingsMessage(context, intent);
          break;
        case "onUpdateMessagesStatus":
          onUpdateMessagesStatus(intent);
          break;
        case "onInitMessages":
          onInitMessages(intent, context);
          break;
        case "onInitChats":
          onInitChats(context, intent);
          break;
        case "onUserStatusChanged":
          onUserStatusChanged(context, intent);
          break;
        case "onHubResponse":
          onHubRespose(context, intent);
          break;

        case "onMemberLeft":
          onMemberLeft(context, intent);
          break;
        case "onMemberAdded":
          onMemberAdded(context, intent);
          break;
        case "onTitleChanged":
          onTitleChanged(context, intent);
          break;
        case "onManagerAdded":
          onManagerAdded(context, intent);
          break;
        case "onMemberRemoved":
          onMemberRemoved(context, intent);
          break;
        case "onMemberJoined":
          onMemberJoined(context, intent);
          break;
        case "onImageChanged":
          onImageChanged(context, intent);
          break;
      }
    }
  }

  private void onInitializing(Intent intent) {
    final boolean isInitializing = intent.getBooleanExtra("isInitializing", false);

    if (mUpdateConversationUICallbacks != null)
    {
      mUpdateConversationUICallbacks.onIsInitializing(isInitializing);
    }
  }

  private void onChatInfoUpdated(Context context, Intent intent) {

    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));

    final String chatId = intent.getStringExtra("chat_id");
    final String groupName = intent.getStringExtra("group_name");
    final String imageAddress = intent.getStringExtra("image_address");

    final RChat chat = mRealm.where(RChat.class).equalTo("Id", chatId).findFirst();
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        chat.setTitle(groupName);
        chat.setProfileThumbnailAddress(imageAddress);
        realm.copyToRealmOrUpdate(chat);
      }
    });

    if (FragmentGroupChat.isRunning && FragmentGroupChat.chatId != null && FragmentGroupChat.chatId.equals(chatId))
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onChatInfoUpdated(chatId, groupName, imageAddress);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onChatInfoUpdatedForConversation(chatId, groupName, imageAddress);
      }
    } else if (FragmentGroupChatInfo.isRunning && FragmentGroupChatInfo.chatId != null && FragmentGroupChatInfo.chatId.equals(chatId))
    {
      if (mUpdateGroupChatInfoUICallbacks != null)
      {
        mUpdateGroupChatInfoUICallbacks.onChatInfoUpdated(chatId, groupName, imageAddress);
      }
    }
    mRealm.close();
  }

  private void onUserStatusChanged(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String profileId = intent.getStringExtra("profileId");
    final int status = intent.getIntExtra("status", 0);
    final String chatId = intent.getStringExtra("chatId");


    if (FragmentChat.isRunning)
    {
      if (mUpdateChatUICallbacks != null)
      {
        mUpdateChatUICallbacks.onUserStatusChanged(profileId, chatId, status);
      }
    } else if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onUserStatusChanged(profileId, chatId, status);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onUserStatusChanged(profileId, chatId, status);
      }
    }

    mRealm.close();
  }

  private void onMemberAdded(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String chatId = intent.getStringExtra("chatId");
    final Profile profile = intent.getParcelableExtra("profile");

    if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onMemberAdded(chatId, profile);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onMemberAdded(chatId, profile);
      }
    }

    mRealm.close();
  }

  private void onTitleChanged(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String chatId = intent.getStringExtra("chatId");
    final String newTitle = intent.getStringExtra("newTitle");
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        final RChat chat = realm.where(RChat.class).equalTo("ChatId", chatId).findFirst();
        chat.setTitle(newTitle);
        realm.copyToRealmOrUpdate(chat);
      }
    });
    if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onTitleChanged(chatId, newTitle);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onTitleChanged(chatId, newTitle);
      }
    }

    mRealm.close();
  }

  private void onManagerAdded(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String chatId = intent.getStringExtra("chatId");
    final String newManagerId = intent.getStringExtra("newManagerId");

    if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onManagerAdded(chatId, newManagerId);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onManagerAdded(chatId, newManagerId);
      }
    }

    mRealm.close();
  }

  private void onMemberLeft(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String chatId = intent.getStringExtra("chatId");
    final String memberId = intent.getStringExtra("memberId");

    if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onMemberLeft(chatId, memberId);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onMemberLeft(chatId, memberId);
      }
    }

    mRealm.close();
  }

  private void onMemberRemoved(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String chatId = intent.getStringExtra("chatId");
    final String memberId = intent.getStringExtra("memberId");

    if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onMemberRemoved(chatId, memberId);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onMemberRemoved(chatId, memberId);
      }
    }

    mRealm.close();
  }

  private void onMemberJoined(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String chatId = intent.getStringExtra("chatId");
    final Profile profile = intent.getParcelableExtra("profile");

    if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onMemberJoined(chatId, profile);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onMemberJoined(chatId, profile);
      }
    }

    mRealm.close();
  }

  private void onImageChanged(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String chatId = intent.getStringExtra("chatId");
    final String imageAddress = intent.getStringExtra("imageAddress");
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        final RChat chat = realm.where(RChat.class).equalTo("ChatId", chatId).findFirst();
        chat.setProfileThumbnailAddress(imageAddress);
        realm.copyToRealmOrUpdate(chat);
      }
    });
    if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onImageChanged(chatId, imageAddress);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onImageChanged(chatId, imageAddress);
      }
    }

    mRealm.close();
  }

  private void onHubRespose(final Context context, Intent intent) {
    final String messageId = intent.getExtras().getString("message_id");
    final String id = intent.getExtras().getString("id");
    final HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put("message_id", messageId);
    hashMap.put("id", id);
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));

    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        final RCompactMessage message = mRealm.where(RCompactMessage.class).equalTo("Id", id).findFirst();
        if (message != null)
        {
          message.setMessageId(messageId);
          message.setMessageStatus(EnumMessageStatus.Sent);
          realm.copyToRealmOrUpdate(message);
          RChat chat = mRealm.where(RChat.class).equalTo("ChatId", message.getChatId()).findFirst();
          if (chat != null)
          {
            chat.setLastMessage(message);
            realm.copyToRealm(chat);
          }
          if (FragmentChat.isRunning)
          {
            if (mUpdateChatUICallbacks != null)
            {
              mUpdateChatUICallbacks.onHubResponse(hashMap);
            }
          } else if (FragmentGroupChat.isRunning)
          {
            if (mUpdateGroupChatUICallbacks != null)
            {
              mUpdateGroupChatUICallbacks.onHubResponse(hashMap);
            }
          } else if (FragmentConversations.isRunning)
          {
            if (mUpdateConversationUICallbacks != null)
            {
            }
          }
        }
      }
    });

    mRealm.close();

  }

  private void onMessageReceived(Intent intent, final Context context) {
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final CompactMessage message = intent.getParcelableExtra("message");

    if (mRealm.where(RChat.class).equalTo("ChatId", message.getChatId()).count() < 1)
    {


      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          realm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));
        }
      });


      if (FragmentChat.isRunning && FragmentChat.chatId != null && FragmentChat.chatId.equals(message.getChatId()))
      {
        if (mUpdateChatUICallbacks != null)
        {
          mUpdateChatUICallbacks.onMessageReceived(message);
        }
      } else if (FragmentGroupChat.isRunning && FragmentGroupChat.chatId != null && FragmentGroupChat.chatId.equals(message.getChatId()))
      {
        if (mUpdateGroupChatUICallbacks != null)
        {
          mUpdateGroupChatUICallbacks.onMessageReceived(message);
        }
      } else
      {
        if (FragmentConversations.isRunning)
        {
          if (mUpdateConversationUICallbacks != null)
          {
            mUpdateConversationUICallbacks.onMessageReceivedForConversation(message);
          }
        }

        if (!AppUtils.isChatMuted(message.getChatId()))
        {
          ChatNotificationUtils.sendNotification(context, DatabaseUtils.unSeenMessages(context), PreferenceHelper.getBoolean(context, "prefEnableVibrate"), PreferenceHelper.getBoolean(context, "prefEnableSound"));
        }
      }
      mRealm.close();
    } else
    {
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));
        }
      });

      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          RChat rChat = mRealm.where(RChat.class).equalTo("ChatId", message.getChatId()).findFirst();
          if (rChat != null)
          {

            long messageDate = message.getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(context, message.getSendDateTime()) : Long.valueOf(message.getSendDateTime());
            long chatDate = rChat.getLastMessage().getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(context, rChat.getLastMessage().getSendDateTime()) : Long.valueOf(rChat.getLastMessage().getSendDateTime());
            if (messageDate > chatDate)
            {

              rChat.getLastMessage().setMessageId(message.getMessageId());
              rChat.getLastMessage().setChatId(message.getChatId());
              rChat.getLastMessage().setContentAddress(message.getContentAddress());
              rChat.getLastMessage().setContentType(message.getContentType());
              rChat.getLastMessage().setMessageStatus(message.getMessageStatus());
              rChat.getLastMessage().setSendDateTime(message.getSendDateTime());
              rChat.getLastMessage().setContentSize(message.getContentSize());
              rChat.getLastMessage().setSenderImage(message.getSenderImage());
              rChat.getLastMessage().setSenderId(message.getSenderId());
              rChat.getLastMessage().setSenderName(message.getSenderName());
              rChat.getLastMessage().setText(message.getText());
              rChat.getLastMessage().setContentAddress(message.getContentAddress());
              rChat.getLastMessage().setThumbnailAddress(message.getThumbnailAddress());
              rChat.getLastMessage().setFilePath(message.getFilePath());

              mRealm.copyToRealmOrUpdate(rChat);
            }
          }
        }
      });

      if (FragmentChat.isRunning && FragmentChat.chatId != null && FragmentChat.chatId.equals(message.getChatId()))
      {
        if (mUpdateChatUICallbacks != null)
        {
          mUpdateChatUICallbacks.onMessageReceived(message);
        }
      } else if (FragmentGroupChat.isRunning && FragmentGroupChat.chatId != null && FragmentGroupChat.chatId.equals(message.getChatId()))
      {
        if (mUpdateGroupChatUICallbacks != null)
        {
          mUpdateGroupChatUICallbacks.onMessageReceived(message);
        }
      } else
      {
        if (FragmentConversations.isRunning)
        {
          if (mUpdateConversationUICallbacks != null)
          {
            mUpdateConversationUICallbacks.onMessageReceivedForConversation(message);
          }
        }

        if (!AppUtils.isChatMuted(message.getChatId()))
        {
          ChatNotificationUtils.sendNotification(context, DatabaseUtils.unSeenMessages(context), PreferenceHelper.getBoolean(context, "prefEnableVibrate"), PreferenceHelper.getBoolean(context, "prefEnableSound"));
        }
      }
    }

    mRealm.close();
  }

  private void onInitMessages(Intent intent, Context context) {
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final ArrayList<CompactMessage> messages = intent.getParcelableArrayListExtra("messages");
    final List<String> messageIds = new ArrayList<>();
    if (!messages.isEmpty())
    {
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          for (final CompactMessage message : messages)
          {
            messageIds.add(message.getMessageId());
            if (mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).count() < 1)
            {
              mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));

              RChat rChat = mRealm.where(RChat.class).equalTo("ChatId", message.getChatId()).findFirst();
              if (rChat != null)
              {
                rChat.getLastMessage().setSendDateTime(message.getSendDateTime());
                mRealm.copyToRealmOrUpdate(rChat);
              }
            }
          }
        }
      });
    }


    if (FragmentChat.isRunning && FragmentChat.chatId != null)
    {
      ArrayList<CompactMessage> messageArrayList = new ArrayList<>();
      ArrayList<CompactMessage> messageArrayList2 = new ArrayList<>();

      for (CompactMessage message : messages)
      {
        if (FragmentChat.chatId.equals(message.getChatId()))
        {
          messageArrayList.add(message);
        } else
        {
          messageArrayList2.add(message);
        }
      }

      if (mUpdateChatUICallbacks != null)
      {
        if (messageArrayList.size() > 0)
        {
          mUpdateChatUICallbacks.onInitMessages(messageArrayList);
        }
      }

      if (messageArrayList2.size() > 0)
      {
        ChatNotificationUtils.sendNotification(context, DatabaseUtils.unSeenMessages(context), PreferenceHelper.getBoolean(context, "prefEnableVibrate"), PreferenceHelper.getBoolean(context, "prefEnableSound"));
      }
    } else if (FragmentGroupChat.isRunning && FragmentGroupChat.chatId != null)
    {

      ArrayList<CompactMessage> messageArrayList = new ArrayList<>();
      ArrayList<CompactMessage> messageArrayList2 = new ArrayList<>();

      for (CompactMessage message : messages)
      {
        if (FragmentGroupChat.chatId.equals(message.getChatId()))
        {
          messageArrayList.add(message);
        } else
        {
          messageArrayList2.add(message);
        }
      }

      if (mUpdateGroupChatUICallbacks != null)
      {
        if (messageArrayList.size() > 0)
        {
          mUpdateGroupChatUICallbacks.onInitMessages(messageArrayList);
        }
      }

      if (messageArrayList2.size() > 0)
      {
        ChatNotificationUtils.sendNotification(context, DatabaseUtils.unSeenMessages(context), PreferenceHelper.getBoolean(context, "prefEnableVibrate"), PreferenceHelper.getBoolean(context, "prefEnableSound"));
      }

    } else
    {

      if (FragmentConversations.isRunning)
      {
        mUpdateConversationUICallbacks.onInitMessages(messages);
      }

      ChatNotificationUtils.sendNotification(context, DatabaseUtils.unSeenMessages(context), PreferenceHelper.getBoolean(context, "prefEnableVibrate"), PreferenceHelper.getBoolean(context, "prefEnableSound"));
    }
    mRealm.close();
  }

  private void onMessageSent(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String messageSentId = intent.getStringExtra("message_id");

    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
      }
    });
    if (FragmentChat.isRunning)
    {
      if (mUpdateChatUICallbacks != null)
      {
        mUpdateChatUICallbacks.onMessageSent(messageSentId);
      }
    } else if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onMessageSent(messageSentId);
      }
    }
    mRealm.close();
  }

  private void onMessageDelivered(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final ArrayList<String> messageIds = intent.getStringArrayListExtra("messageIds");

    for (int i = 0; i < messageIds.size(); i++)
    {
      final int finalI = i;

      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
        }
      });
    }

    if (FragmentChat.isRunning)
    {
      if (mUpdateChatUICallbacks != null)
      {
      }
    } else if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onMessageDelivered(messageIds);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
      }
    }
    mRealm.close();
  }

  @SuppressWarnings ("unchecked")
  private void onMessageSeen(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String seenMessagesId = intent.getStringExtra("messageIds");

    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        RCompactMessage message = realm.where(RCompactMessage.class)
          .equalTo("MessageId", seenMessagesId)
          .findFirst();
        if (message != null)
        {
          message.setMessageStatus(EnumMessageStatus.Seen);
        }


      }
    });

    if (FragmentChat.isRunning)
    {
      if (mUpdateChatUICallbacks != null)
      {
        mUpdateChatUICallbacks.onMessageSeen(seenMessagesId);
      }
    } else if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onMessageSeen(seenMessagesId);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onMessageSeenForConversation(seenMessagesId);
      }
    }
    mRealm.close();
  }

  private void startChat(Context context, CompactChat chat) {
    FragmentHandler.replaceFragment(context, fragmentType.CHAT, chat);

  }

  private void startGroupChat(Context context, CompactChat chat) {
    FragmentHandler.replaceFragment(context, fragmentType.GROUPCHAT, chat);

  }

  private void createChatIfNotExists(Context context, final CompactChat chat, boolean startActivities) {
  }

  private void onNewChat(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final CompactChat chat = intent.getParcelableExtra("chat");


    if (!chat.isGroup()) //&& chat.getAdminId().equals(Logged.Models.getUserProfile().getId()))
    {
      startChat(context, chat);
    } else if (chat.isGroup())// && chat.getAdminId().equals(Logged.Models.getUserProfile().getId()))
    {
    }


    if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onNewChat(chat);
      }
    }
    mRealm.close();
  }

  private void onChatUpdated(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final CompactChat chat = intent.getParcelableExtra("chat");

    if (mRealm.where(RChat.class).equalTo("Id", chat.getChatId()).count() >= 1)
    {
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          realm.copyToRealmOrUpdate(RToNonR.chatToRChat(chat));
        }
      });
    }

    if (FragmentGroupChat.isRunning && FragmentGroupChat.chatId != null && FragmentGroupChat.chatId.equals(chat.getChatId()))
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onChatUpdated(chat);
      }
    } else if (FragmentGroupChatInfo.isRunning && FragmentGroupChatInfo.chatId != null && FragmentGroupChatInfo.chatId.equals(chat.getChatId()))
    {
      if (mUpdateGroupChatInfoUICallbacks != null)
      {
        mUpdateGroupChatInfoUICallbacks.onChatUpdated(chat);
      }
    }
    mRealm.close();
  }

  private void onInitChats(Context context, Intent intent) {
    final ArrayList<CompactChat> chats = intent.getParcelableArrayListExtra("chats");

    for (final CompactChat chat : chats)
    {
      createChatIfNotExists(context, chat, false);
    }

    if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onInitChats(chats);
      }
    }
  }

  private void onAddSettingsMessage(Context context, Intent intent) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
    final String chatId = intent.getStringExtra("chat_id");
    final String messageText = intent.getStringExtra("message_text");
    final String dateTime = intent.getStringExtra("datetime");
    final String messageId = intent.getStringExtra("message_id");

    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        RCompactMessage message = realm.createObject(RCompactMessage.class);
        message.setMessageId(messageId);
        message.setChatId(chatId);
        message.setText(messageText);
        message.setContentType(EnumMessageContentType.Announcement);
        message.setSendDateTime(dateTime);
        realm.copyToRealmOrUpdate(message);
      }
    });

    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        RChat rChat = realm.where(RChat.class).equalTo("Id", chatId).findFirst();
        if (rChat != null)
        {
          rChat.getLastMessage().setSendDateTime(dateTime);
          realm.copyToRealmOrUpdate(rChat);
        }
      }
    });

    if (FragmentGroupChat.isRunning && FragmentGroupChat.mChat.getChatId().equals(chatId))
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onAddSettingsMessage(messageId, chatId, messageText, dateTime);
      }
    } else if (FragmentConversations.isRunning)
    {
      if (mUpdateConversationUICallbacks != null)
      {
        mUpdateConversationUICallbacks.onAddSettingsMessageForConversation(messageId, chatId, messageText, dateTime);
      }
    }

    mRealm.close();
  }

  @SuppressWarnings ("unchecked")
  private void onUpdateMessagesStatus(Intent intent) {
    final ArrayList<CompactMessage> messages = intent.getParcelableArrayListExtra("messages");

    if (FragmentChat.isRunning)
    {
      if (mUpdateChatUICallbacks != null)
      {
        mUpdateChatUICallbacks.onUpdateMessagesStatus(messages);
      }
    } else if (FragmentGroupChat.isRunning)
    {
      if (mUpdateGroupChatUICallbacks != null)
      {
        mUpdateGroupChatUICallbacks.onUpdateMessagesStatus(messages);
      }
    }
  }
}
