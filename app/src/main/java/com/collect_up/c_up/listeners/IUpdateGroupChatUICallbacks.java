/*
 * Created by Collect-up  on January 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.listeners;

import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.Profile;

import java.util.ArrayList;
import java.util.HashMap;

public interface IUpdateGroupChatUICallbacks {
  void onMessageReceived(CompactMessage message);

  void onMessageSent(String messageId);

  void onMessageDelivered(ArrayList<String> messageIds);

  void onMessageSeen(String messageId);

  void onAddSettingsMessage(String messageId, String chatId, String messageText, String dateTime);

  void onUpdateMessagesStatus(ArrayList<CompactMessage> messagesStatus);

  void onInitMessages(ArrayList<CompactMessage> messages);

  void onUserStatusChanged(String profileId, String chatId, int status);

  void onChatUpdated(CompactChat chat);

  void onChatInfoUpdated(String chatId, String groupName, String imageAddress);

  void onHubResponse(HashMap<String, String> response);

  void onMemberAdded(String ChatId, Profile profile);

  void onTitleChanged(String chatId, String newTitle);

  void onManagerAdded(String chatId, String newManagerId);

  void onMemberLeft(String chatId, String memberId);

  void onMemberRemoved(String chatId, String memberId);

  void onMemberJoined(String chatId, Profile profile);

  void onImageChanged(String chatId, String imageAddress);

}
