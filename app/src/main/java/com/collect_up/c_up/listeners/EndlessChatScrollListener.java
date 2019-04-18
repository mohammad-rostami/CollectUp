/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.listeners;

import android.widget.AbsListView;
import android.widget.ListView;

import com.collect_up.c_up.adapters.ChatAdapter;
import com.collect_up.c_up.adapters.GroupChatAdapter;
import com.collect_up.c_up.helpers.SortMessages;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.realm.RCompactMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EndlessChatScrollListener implements AbsListView.OnScrollListener {

  private ChatAdapter chatAdapter;
  private GroupChatAdapter groupChatAdapter;
  private List<RCompactMessage> messageList;
  private ListView listView;
  private int currentPage = 0;
  private int previousTotal = 0;
  private boolean loading = true;
  private int totalMessagesInAPage = 100;

  public EndlessChatScrollListener(ListView listView, List<RCompactMessage> messageList, ChatAdapter adapter) {
    this.listView = listView;
    this.messageList = messageList;
    this.chatAdapter = adapter;
  }

  public EndlessChatScrollListener(ListView listView, List<RCompactMessage> messageList, GroupChatAdapter adapter) {
    this.listView = listView;
    this.messageList = messageList;
    this.groupChatAdapter = adapter;
  }

  public List<RCompactMessage> loadMore(int page) {
    int startFrom = page * totalMessagesInAPage;
    List<RCompactMessage> messages = new ArrayList<>();

    // +addSettingMessagesCount because of not counting add setting messages
    for (int i = startFrom, addSettingMessagesCount = 0; i < (startFrom) + totalMessagesInAPage + addSettingMessagesCount; i++)
    {
      if (i < messageList.size())
      {
        messages.add(messageList.get(i));
        if (messageList.get(i).getContentType() == EnumMessageContentType.Announcement)
        {
          addSettingMessagesCount++;
        }
      } else
      {
        break;
      }
    }

    Collections.sort(messages, SortMessages.ASC);

    return messages;
  }

  @Override
  public void onScroll(AbsListView view, final int firstVisibleItem,
                       int visibleItemCount, final int totalItemCount) {

    if (loading)
    {
      // -1 because of load more button at top
      if (totalItemCount - 1 > previousTotal)
      {
        loading = false;
        previousTotal = totalItemCount;
        currentPage++;
      }
    }

    if (!loading)
    {
      if (listView.getFirstVisiblePosition() == 0 && listView.getChildAt(listView.getChildCount() - 1) != null)
      {
        List<RCompactMessage> messages = loadMore(currentPage);
        int nextPageCount = loadMore(currentPage).size();

        if (chatAdapter != null)
        {
          //chatAdapter.onLoadMoreReady(messages, nextPageCount != 0);
        } else if (groupChatAdapter != null)
        {
          //groupChatAdapter.onLoadMoreReady(messages, nextPageCount != 0);
        }

        loading = true;
      }
    }
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {

  }
}