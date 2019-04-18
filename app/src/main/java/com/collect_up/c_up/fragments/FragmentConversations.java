/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.ConversationsAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.SortChats;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.IUpdateConversationUICallbacks;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.EnumProfileStatus;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.receivers.RealtimeReceiver;
import com.collect_up.c_up.services.RealtimeService;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.divideritemdecoration.HorizontalDividerItemDecoration;
import com.mikepenz.materialdrawer.Drawer;
import com.rey.material.app.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmResults;

@SuppressLint ("ValidFragment")

public class FragmentConversations extends BaseFragment
  implements IUpdateConversationUICallbacks,
  ConversationsAdapter.OnItemClickListener,
  Comparator<RCompactMessage>,
  View.OnFocusChangeListener,
  SearchView.OnQueryTextListener,
  MenuItemCompat.OnActionExpandListener {

  public static boolean isRunning;
  @Bind (R.id.recycler_view)
  UltimateRecyclerView recyclerView;
  List<String> membersIsTypingIds = new ArrayList<>();
  List<Integer> membersIsTypingPoss = new ArrayList<>();
  HashMap<String, String> chat2persons = new HashMap<>();
  private ConversationsAdapter mAdapter;
  private List<CompactChat> mChats = new ArrayList<>();
  private RealtimeReceiver mUpdateChatUIReceiver;
  private Drawer mDrawer;
  private SearchView mSearchView;

  private static final String KEY_POSITION = "position";
  private View view;
  private int mCurrentPage = 1;
  private List<CompactChat> chatList = new LinkedList<>();

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    try
    {
      if (isVisibleToUser)
      {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.conversations);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

      } else
      {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
      }
    } catch (Exception ex)
    {
    }
  }


  public static FragmentConversations newInstance(int position) {
    FragmentConversations frag = new FragmentConversations();
    Bundle args = new Bundle();

    args.putInt(KEY_POSITION, position);
    frag.setArguments(args);

    return (frag);
  }

  private RCompactMessage getLastMessage(RealmResults<RCompactMessage> messages) {
    long max = 0;
    RCompactMessage message = null;
    for (RCompactMessage rMessage : messages)
    {
      long time;
      if (rMessage.getSendDateTime().contains("T"))
      {
        time = TimeHelper.utcToTimezone(getContext(), rMessage.getSendDateTime());
      } else
      {
        time = Long.valueOf(rMessage.getSendDateTime());
      }
      if (time > max)
      {
        max = time;
        message = rMessage;
      }
    }

    return message;
  }

  private void sortChats(List<CompactChat> chats) {
    Collections.sort(chats, new SortChats(getContext()));
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();

    inflater.inflate(R.menu.menu_search, menu);
    menu.findItem(R.id.action_search).setVisible(false);
    super.onCreateOptionsMenu(menu, inflater);
  }

  private void loadMoreChat() {
    Pagination.getChatList(getContext(), mCurrentPage, new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(final List<T> pageList) {

        Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
        realm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            for (CompactChat chat : (List<CompactChat>) pageList)
            {
              realm.copyToRealmOrUpdate(RToNonR.chatToRChat(chat));

            }
          }

        });

        realm.close();

        mCurrentPage++;
        if (pageList.size() != Pagination.PAGE_IN_REQUEST)
        {
          loadMoreChat();
        }

      }

      @Override
      public void onFailure() {

      }
    });
  }

  private void loadLocalChats() {
    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    final RealmResults<RChat> chats = realm.where(RChat.class).findAll();
    recyclerView.setHasFixedSize(true);
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).color(getResources()
      .getColor(R.color.chat_subtitle))
      .build());
    chatList.clear();
    for (RChat rChat : chats)
    {
      chatList.add(RToNonR.rChatToChat(rChat));
    }
    sortChats(chatList);

    mAdapter = new ConversationsAdapter(getContext(), chatList);
    mAdapter.setListener(FragmentConversations.this);
    recyclerView.setAdapter(mAdapter);
  }

  private void loadFirstChatPages() {
    //   mCurrentPage = 1;
    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));


    Pagination.getChatList(getContext(), mCurrentPage, new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(final List<T> pageList) {
        // Reset items to maeke the pull to refresh right
        if (pageList.size() > 0)
        {
          recyclerView.hideEmptyView();
        }
        realm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            for (CompactChat chat : (List<CompactChat>) pageList)
            {
              realm.copyToRealmOrUpdate(RToNonR.chatToRChat(chat));
            }
          }
        });


        mCurrentPage++;
        if (pageList.size() == Pagination.PAGE_IN_REQUEST)
        {
          loadFirstChatPages();
          if (isVisible())
          {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.updating);
          }

        } else
        {
          loadLocalChats();
          if (isVisible())

          {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.conversations);
          }

        }

      }

      @Override
      public void onFailure() {

      }
    });
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_conversations, container, false);
      ButterKnife.bind(this, view);

      ((AppCompatActivity) getActivity()).getSupportActionBar().show();

      FloatingActionButton fabSpeedDial = (FloatingActionButton) view.findViewById(R.id.btnFab);
      fabSpeedDial.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          final BottomSheetDialog mDialog = new BottomSheetDialog(getContext());
          mDialog.contentView(R.layout.bottom_sheet_new_chat)
            .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
            .inDuration(300)
            .cancelable(true)
            .show();
          ImageButton btnNewchat = (ImageButton) mDialog.findViewById(R.id.btnNewChat);
          ImageButton btnNewgroup = (ImageButton) mDialog.findViewById(R.id.btnGroupChat);
          btnNewchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent bundle = new Intent();
              bundle.putExtra("show_invites", false);
              bundle.putExtra("title", getString(R.string.action_new_chat));
              FragmentHandler.replaceFragment(getContext(), fragmentType.CHATCONTACTS, bundle);
              mDialog.dismiss();
            }
          });
          btnNewgroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent bundle = new Intent();
              bundle.putExtra("show_invites", false);
              bundle.putExtra("multi_select", true);
              bundle.putExtra("title", getString(R.string.action_new_group));
              FragmentHandler.replaceFragment(getContext(), fragmentType.CHATCONTACTS, bundle);

              mDialog.dismiss();

            }
          });
        }
      });
      loadLocalChats();
      loadFirstChatPages();
      onIsInitializing(RealtimeService.getIsInitializing());


      return view;
    } else
    {
      return view;
    }
  }

  @Override
  public void onStop() {
    isRunning = false;
    super.onStop();

  }

  @Override
  public void onStart() {
    isRunning = true;
    super.onStart();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mUpdateChatUIReceiver != null)
    {
      getActivity().unregisterReceiver(mUpdateChatUIReceiver);
    }
  }


  @Override
  public void onResume() {
    super.onResume();
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.conversations);
    ((ActivityHome) getActivity()).changeButtonBackgroud(3);

    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    if (mUpdateChatUIReceiver == null)
    {
      mUpdateChatUIReceiver = new RealtimeReceiver(this);
    }
    IntentFilter intentFilter = new IntentFilter(Constants.General.UPDATE_CHAT_UI);
    getActivity().registerReceiver(mUpdateChatUIReceiver, intentFilter);

    onIsInitializing(RealtimeService.getIsInitializing());
    if (mAdapter != null && mAdapter.getChats() != null)
    {
      mChats = mAdapter.getChats();
    }
    mChats.clear();


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }

    RealmResults<RChat> chats = mRealm.where(RChat.class).findAll();

    List<CompactChat> chats2 = new ArrayList<>();
    for (RChat rChat : chats)
    {
      chats2.add(RToNonR.rChatToChat(rChat));
    }

    sortChats(chats2);

    mChats.addAll(chats2);


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }

    mRealm.close();
  }


  @Override
  public void onNewChat(CompactChat chat1) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    mChats.clear();


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }

    RealmResults<RChat> chats = mRealm.where(RChat.class).findAll();
    List<CompactChat> chats2 = new ArrayList<>();
    for (RChat rChat : chats)
    {
      chats2.add(RToNonR.rChatToChat(rChat));
    }

    sortChats(chats2);

    mChats.addAll(chats2);

    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
    mRealm.close();
  }

  @Override
  public void onInitChats(ArrayList<CompactChat> comingChats) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(MyApplication.context));
    mChats.clear();
    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }

    RealmResults<RChat> chats = mRealm.where(RChat.class).findAll();
    List<CompactChat> chats2 = new ArrayList<>();
    for (RChat rChat : chats)
    {
      chats2.add(RToNonR.rChatToChat(rChat));
    }

    sortChats(chats2);

    mChats.addAll(chats2);


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
    mRealm.close();
  }

  @Override
  public void onMessageReceivedForConversation(CompactMessage comingMessage) {
    // Must be inside UI thread, to make sure no 'only the original thread can touch its views' exception would be throw.
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
        mChats.clear();


        if (mAdapter != null)
        {
          mAdapter.notifyDataSetChanged();
        }

        RealmResults<RChat> chats = mRealm.where(RChat.class).findAll();
        List<CompactChat> chats2 = new ArrayList<>();
        for (RChat rChat : chats)
        {
          chats2.add(RToNonR.rChatToChat(rChat));
        }

        sortChats(chats2);

        mChats.addAll(chats2);


        if (mAdapter != null)
        {
          mAdapter.notifyDataSetChanged();
        }
        mRealm.close();
      }
    });
  }


  @Override
  public void onMessageSeenForConversation(String messagesId) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }

    mRealm.close();
  }

  @Override
  public void onAddSettingsMessageForConversation(String messageId, String chatId, String messageText, String dateTime) {
    RCompactMessage message = new RCompactMessage();
    message.setChatId(chatId);
    message.setMessageId(messageId);
    message.setText(messageText);
    message.setContentType(EnumMessageContentType.Announcement);
    message.setSendDateTime(dateTime);


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
  }


  private int getUniqueIdsFromTyping(int pos) {
    int count = 0;
    for (Integer integer : membersIsTypingPoss)
    {
      if (integer == pos)
      {
        count++;
      }
    }

    return count;
  }

/*
  private String getTypingMemberName(String profileId, RChat chat) {
    String name = null;
    for (RProfile profile : chat.getMembers())
    {
      if (profile.getId().equals(profileId))
      {
        name = profile.getName();
        break;
      }
    }
    return name;
  }
*/

  @Override
  public void onUserStatusChanged(String profileId, String chatId, final int status) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    int pos = -1;
    for (CompactChat chat : mChats)
    {
      if (chat.getChatId().equals(chatId))
      {
        pos = mChats.indexOf(chat);
      }
    }
    if (pos != -1)
    {
      CompactChat chatt = mChats.get(pos);
      if (status == EnumProfileStatus.IsTyping)
      {
        if (chatt.getLastMessage() != null)
        {

          if (chatt.isGroup())
          {
            membersIsTypingIds.add(profileId);
            membersIsTypingPoss.add(pos);
            int uniqueIdsCount = getUniqueIdsFromTyping(pos);
            if (uniqueIdsCount > 1)
            {
              String coloredText = getResources().getQuantityString(R.plurals.members, uniqueIdsCount, uniqueIdsCount) + " " + getString(R.string.is_typing);
              chatt.getLastMessage().setText(coloredText);
            } else
            {

            }
          } else
          {
            chatt.getLastMessage().setText(getContext().getString(R.string.is_typing));
            chat2persons.put(chatId, profileId);
          }

        }

      } else

      {

        mChats.clear();


        if (mAdapter != null)
        {
          mAdapter.notifyDataSetChanged();
        }

        RealmResults<RChat> chats = mRealm.where(RChat.class).findAll();
        List<CompactChat> chats2 = new ArrayList<>();
        for (RChat rChat : chats)
        {
          chats2.add(RToNonR.rChatToChat(rChat));
        }

        sortChats(chats2);

        mChats.addAll(chats2);

        if (chatt.isGroup())
        {
          if (membersIsTypingIds.contains(profileId))
          {
            int ind = membersIsTypingIds.indexOf(profileId);
            membersIsTypingIds.remove(ind);
            membersIsTypingPoss.remove(ind);

          }
        } else
        {
          if (chat2persons.containsKey(chatId) && chat2persons.containsValue(profileId))
          {
            chat2persons.remove(chatId);
          }
        }


      }

      if (mAdapter != null)

      {
        mAdapter.notifyDataSetChanged();
      }

    }

    mRealm.close();
  }

  @Override
  public void onChatInfoUpdatedForConversation(String chatId, final String groupName, final String imageAddress) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    for (final CompactChat chat : mChats)
    {
      if (chat.getChatId().equals(chatId))
      {
        mRealm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            chat.setTitle(groupName);
            chat.setProfileThumbnailAddress(imageAddress);
          }
        });
        break;
      }
    }

    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
    mRealm.close();
  }

  @Override
  public void onChatDeleted(CompactChat chat) {
    for (CompactChat rChat : mChats)
    {
      if (rChat.getChatId().equals(chat.getChatId()))
      {
        int pos = mChats.indexOf(rChat);


        mChats.remove(pos);
        break;
      }
    }

    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onInitMessages(ArrayList<CompactMessage> comingMessages) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    RealmResults<RChat> chats = mRealm.where(RChat.class).findAll();

    mChats.clear();


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
    List<CompactChat> chats2 = new ArrayList<>();
    for (RChat rChat : chats)
    {
      chats2.add(RToNonR.rChatToChat(rChat));
    }

    sortChats(chats2);

    mChats.addAll(chats2);


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
    mRealm.close();
  }

  @Override
  public void onIsInitializing(boolean isInitializing) {
    if (isInitializing)
    {
      //toolbar.setTitle(R.string.updating);
    } else
    {
      //  toolbar.setTitle(R.string.conversations);
    }
  }

  @Override
  public void onMemberAdded(String ChatId, Profile profile) {

  }

  @Override
  public void onTitleChanged(String chatId, final String newTitle) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    for (final CompactChat chat : mChats)
    {
      if (chat.getChatId().equals(chatId))
      {
        mRealm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            chat.setTitle(newTitle);
          }
        });
        break;
      }
    }

    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
    mRealm.close();
  }

  @Override
  public void onManagerAdded(String chatId, String newManagerId) {

  }

  @Override
  public void onMemberLeft(String chatId, String memberId) {

  }

  @Override
  public void onMemberRemoved(String chatId, String memberId) {

  }

  @Override
  public void onMemberJoined(String chatId, Profile profile) {

  }

  @Override
  public void onImageChanged(String chatId, final String imageAddress) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    for (final CompactChat chat : mChats)
    {
      if (chat.getChatId().equals(chatId))
      {
        mRealm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            chat.setProfileThumbnailAddress(imageAddress);
          }
        });
        break;
      }
    }

    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
    mRealm.close();
  }

  @Override
  public void onViewClick(CompactChat chat) {
    if (!chat.isGroup())
    {
      FragmentHandler.replaceFragment(getContext(), fragmentType.CHAT, chat);
    } else
    {
      FragmentHandler.replaceFragment(getContext(), fragmentType.GROUPCHAT, chat);
    }
  }

  @Override
  public void onViewLongClick(final CompactChat chat) {
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    final BottomSheetDialog mDialog = new BottomSheetDialog(getContext());
    mDialog.contentView(R.layout.bottom_sheet_conversation)
      .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
      .inDuration(300)
      .cancelable(true);

    ImageButton delete = (ImageButton) mDialog.findViewById(R.id.btnDeleteChat);
    TextView txtRemove = (TextView) mDialog.findViewById(R.id.txtRemove);
    final ImageButton clear = (ImageButton) mDialog.findViewById(R.id.btnClearHistory);

    if (chat.isGroup())
    {
      txtRemove.setText(R.string.action_delete_leave);
    }
    mDialog.show();

    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        mDialog.dismiss();
        final com.rey.material.app.SimpleDialog sureBuilder = new com.rey.material.app.SimpleDialog(getContext());
        sureBuilder
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.delete)
          .negativeAction(R.string.cancel)
          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));


        if (chat.isGroup())
        {
          sureBuilder.title(R.string.delete_leave_title);
          sureBuilder.message(R.string.sure_to_delete_and_leave_group);
        } else
        {
          sureBuilder.title(R.string.delete_chat_title);
          sureBuilder.message(R.string.sure_to_delete_chat);
        }
        sureBuilder.setCancelable(true);
        sureBuilder.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            sureBuilder.dismiss();

          }
        });
        sureBuilder.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int pos = -1;
            for (CompactChat chat1 : mChats)
            {
              if (chat1.getChatId().equals(chat.getChatId()))
              {
                pos = mChats.indexOf(chat1);
              }
            }
            final int finalPos = pos;
            if (chat.isGroup())
            {
              HttpClient.get(String.format(Constants.Server.Messaging.GET_LEAVE_GROUP, chat.getChatId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                  super.onSuccess(statusCode, headers, responseBody);
                  final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
                  mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(
                      Realm realm) {
                      RealmResults<RCompactMessage> messages = mRealm.where(RCompactMessage.class).equalTo("ChatId", chat.getChatId()).findAll();
                      for (int i = 0; i < messages.size(); i++)
                      {
                        RCompactMessage rMessage = messages.get(i);

                        rMessage.removeFromRealm();
                      }
                      RChat rChat = mRealm.where(RChat.class).equalTo("ChatId", chat.getChatId()).findFirst();
                      if (chat != null)
                      {
                        rChat.removeFromRealm();
                        mChats.remove(chat);
                        mAdapter.notifyItemRemoved(finalPos);

                      }

                    }
                  });
                  mAdapter.notifyItemRangeChanged(finalPos, mChats.size());

                  mRealm.close();


                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                  super.onFailure(statusCode, headers, responseBody, error);
                }
              });

            } else
            {
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                  RealmResults<RCompactMessage> messages = mRealm.where(RCompactMessage.class).equalTo("ChatId", chat.getChatId()).findAll();
                  messages.clear();


                  RChat rChat1 = mRealm.where(RChat.class).equalTo("ChatId", chat.getChatId()).findFirst();
                  if (rChat1 != null)
                  {
                    if (mChats.indexOf(chat) >= 0)
                    {
                      mChats.remove(chat);
                    }
                    rChat1.removeFromRealm();
                  }
                }
              });

              mAdapter.notifyItemRemoved(finalPos);
            }

            sureBuilder.dismiss();
          }
        });

        sureBuilder.show();
      }

    });

    clear.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog.dismiss();
        final com.rey.material.app.SimpleDialog confirmDialog = new com.rey.material.app.SimpleDialog(getContext());
        confirmDialog.message(R.string.sure_to_clear_history_chat)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .title(R.string.clear_history_title)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.clear)
          .negativeAction(R.string.cancel)
          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .setCancelable(true);

        confirmDialog.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            confirmDialog.dismiss();

          }
        });
        confirmDialog.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            final RealmResults<RCompactMessage> messages = mRealm.where(RCompactMessage.class)
              .equalTo("ChatId", chat.getChatId())
              .findAll();

            for (RCompactMessage message : messages)
            {
              MyApplication.getInstance().cancelUploadHandler(message.getMessageId());
            }

            mRealm.executeTransaction(new Realm.Transaction() {
              @Override
              public void execute(Realm realm) {
                messages.clear();
              }
            });

            int pos = mChats.indexOf(chat);

            if (mAdapter != null)
            {
              mAdapter.notifyItemChanged(pos);
            }

            confirmDialog.dismiss();
          }
        });

        confirmDialog.show();
      }
    });
    mRealm.close();
  }

  @Override
  public int compare(RCompactMessage lhs, RCompactMessage rhs) {
    long lhsDateTime = lhs.getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(getContext(), lhs.getSendDateTime()) : Long.valueOf(lhs.getSendDateTime());
    long rhsDateTime = rhs.getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(getContext(), rhs.getSendDateTime()) : Long.valueOf(rhs.getSendDateTime());
    return Long.valueOf(rhsDateTime).compareTo(lhsDateTime);
  }

  @Override
  public boolean onMenuItemActionExpand(MenuItem item) {
    return true;
  }

  @Override
  public boolean onMenuItemActionCollapse(MenuItem item) {
    return true;
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus)
    {
      mSearchView.clearFocus();
    }
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    if (mAdapter != null)
    {
      mAdapter.getFilter().filter(query);
    }

    return true;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    if (mAdapter != null)
    {
      mAdapter.getFilter().filter(newText);
    }

    return true;
  }
}
