/*
 * Created by Collect-up  on January 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.listeners;

import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.Profile;

public interface IUpdateGroupChatInfoUICallbacks {
  void onChatInfoUpdated(String chatId, String groupName, String imageAddress);

  void onChatUpdated(CompactChat chat);

  void onMemberAdded(String ChatId, Profile profile);

  void onTitleChanged(String chatId, String newTitle);

  void onManagerAdded(String chatId, String newManagerId);

  void onMemberLeft(String chatId, String memberId);

  void onMemberRemoved(String chatId, String memberId);

  void onMemberJoined(String chatId, Profile profile);

  void onImageChanged(String chatId, String imageAddress);
}
