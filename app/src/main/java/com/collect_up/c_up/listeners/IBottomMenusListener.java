/*
 * Created by Collect-up  on January 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.listeners;

/**
 * Bottom menus callback
 * Activities which have bottom menus, should implement this.
 */
public interface IBottomMenusListener {
  void onMenuNotificationsClick();

  void onMenuTimelineClick();

  void onMenuAddClick();

  void onMenuShopClick();

  void onConversationClick();
}
