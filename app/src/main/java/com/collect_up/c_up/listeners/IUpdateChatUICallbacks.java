/*
 * Created by Collect-up  on January 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.listeners;

import com.collect_up.c_up.model.CompactMessage;

import java.util.ArrayList;
import java.util.HashMap;

public interface IUpdateChatUICallbacks {
  void onMessageReceived(CompactMessage message);

  void onMessageSent(String messageId);

  //void onMessageDelivered(ArrayList<String> messageIds);
  void onMessageSeen(String messageId);

  void onUpdateMessagesStatus(ArrayList<CompactMessage> compactMessages);

  void onInitMessages(ArrayList<CompactMessage> messages);

  void onUserStatusChanged(String profileId, String chatId, int status);

  void onHubResponse(HashMap<String, String> response);

}
