/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters.interfaces;

import com.collect_up.c_up.adapters.providers.Contacts;
import com.collect_up.c_up.model.Profile;

import java.util.List;

public interface ContactsCallback {
  void onContactsReceived(List<Profile> profiles, List<Contacts.UnRegisteredContact> contacts);
}
