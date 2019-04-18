/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationCenter {

  public static final int emojiDidLoaded = 999;
  private static volatile NotificationCenter Instance = null;
  final private HashMap<Integer, Object> addAfterBroadcast = new HashMap<>();
  final private HashMap<Integer, ArrayList<Object>> observers = new HashMap<>();
  final private HashMap<Integer, Object> removeAfterBroadcast = new HashMap<>();
  private int broadcasting = 0;

  public static NotificationCenter getInstance() {
    NotificationCenter localInstance = Instance;
    if (localInstance == null)
    {
      synchronized (NotificationCenter.class)
      {
        localInstance = Instance;
        if (localInstance == null)
        {
          Instance = localInstance = new NotificationCenter();
        }
      }
    }
    return localInstance;
  }

  public void postNotificationName(int id, Object... args) {
    synchronized (observers)
    {
      broadcasting++;
      ArrayList<Object> objects = observers.get(id);
      if (objects != null)
      {
        for (Object obj : objects)
        {
          ((NotificationCenterDelegate) obj).didReceivedNotification(id, args);
        }
      }
      broadcasting--;
      if (broadcasting == 0)
      {
        if (!removeAfterBroadcast.isEmpty())
        {
          for (Map.Entry<Integer, Object> entry : removeAfterBroadcast.entrySet())
          {
            removeObserver(entry.getValue(), entry.getKey());
          }
          removeAfterBroadcast.clear();
        }
        if (!addAfterBroadcast.isEmpty())
        {
          for (Map.Entry<Integer, Object> entry : addAfterBroadcast.entrySet())
          {
            addObserver(entry.getValue(), entry.getKey());
          }
          addAfterBroadcast.clear();
        }
      }
    }
  }

  public void removeObserver(Object observer, int id) {
    synchronized (observers)
    {
      if (broadcasting != 0)
      {
        removeAfterBroadcast.put(id, observer);
        return;
      }
      ArrayList<Object> objects = observers.get(id);
      if (objects != null)
      {
        objects.remove(observer);
        if (objects.size() == 0)
        {
          observers.remove(id);
        }
      }
    }
  }

  public void addObserver(Object observer, int id) {
    synchronized (observers)
    {
      if (broadcasting != 0)
      {
        addAfterBroadcast.put(id, observer);
        return;
      }
      ArrayList<Object> objects = observers.get(id);
      if (objects == null)
      {
        observers.put(id, (objects = new ArrayList<>()));
      }
      if (objects.contains(observer))
      {
        return;
      }
      objects.add(observer);
    }
  }

  public interface NotificationCenterDelegate {
    void didReceivedNotification(int id, Object... args);
  }
}
