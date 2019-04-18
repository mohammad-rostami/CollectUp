package com.collect_up.c_up.adapters.interfaces;

import com.collect_up.c_up.adapters.providers.Contacts;

import java.util.ArrayList;

/**
 * Created by collect-up3 on 5/1/2016.
 */
public interface InviteCounter {
  void countChecked(ArrayList<Contacts.UnRegisteredContact> contacts);
}
