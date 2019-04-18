/*
 * Created by Collect-up  on January 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.listeners;

import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.model.Login;
import com.orhanobut.hawk.Hawk;

import cz.msebera.android.httpclient.Header;

public abstract class LoginSuccess {
  protected LoginSuccess() {
  }

  public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
    Login login = GsonParser.getObjectFromGson(responseBody, Login.class);
    Hawk.put("login", login);
  }

  public abstract void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error);
}