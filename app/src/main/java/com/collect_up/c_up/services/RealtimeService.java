/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.collect_up.c_up.BuildConfig;
import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.IRealtimeConnectionCallbacks;
import com.collect_up.c_up.listeners.IUpdateChatUICallbacks;
import com.collect_up.c_up.listeners.IUpdateConversationUICallbacks;
import com.collect_up.c_up.listeners.IUpdateGroupChatInfoUICallbacks;
import com.collect_up.c_up.listeners.IUpdateGroupChatUICallbacks;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageStatus;
import com.collect_up.c_up.model.Notification;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.model.realm.RProfile;
import com.collect_up.c_up.receivers.RealtimeReceiver;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmResults;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.Credentials;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.transport.AutomaticTransport;
import microsoft.aspnet.signalr.client.transport.ClientTransport;

import static com.collect_up.c_up.MyApplication.context;

public class RealtimeService extends Service implements IRealtimeConnectionCallbacks,
        IUpdateChatUICallbacks,
        IUpdateConversationUICallbacks,
        IUpdateGroupChatUICallbacks,
        IUpdateGroupChatInfoUICallbacks {

    public static HubConnection connection = null;
    public static HubProxy hub = null;
    public static RealtimeService backgroundService;
    public static boolean isInitializing = false;
    private static BroadcastReceiver broadcastReceiver = new RealtimeReceiver();
    private static Context mContext = null;
    private final IBinder mBinder = new LocalBinder();
    public boolean isConnectedToServer = false;
    private Handler mHandler; // to display Toast message
    private int httpClientTimeout = 180 * 1000;
    private static final String AUTH = "Authorization";

    public RealtimeService() {

    }

    public static boolean getIsInitializing() {
        return isInitializing;
    }

    public static void setIsInitializing(boolean x) {
        if (isInitializing != x) {
            invokeIsInitializing(x);
        }

        isInitializing = x;
    }

    public static void invokeIsInitializing(boolean x) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("isInitializing", x);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onInitializing");
        mContext.sendBroadcast(intent);
    }

    public static void invokeUpdateUserStatus(int profileStatus, CompactChat chatId, boolean isGroup) {
        hub.invoke("UpdateUserStatus", profileStatus, new Gson().toJson(chatId, CompactChat.class), isGroup);
    }

    public static void invokeGetMessagesStatus(String messages) {
        hub.invoke("GetMessagesStatus", messages);
    }

    public static void invokeGetUpdateProfileStatus(String profileIds, Action<String> doneCallback, ErrorCallback errorCallback) {
        //  hub.invoke(String.class, "UpdateProfileStatus", profileIds).done(doneCallback).onError(errorCallback);
    }


    public static void invokeJoinRoom(String profileId, String chatId, Action<Void> doneCallback, ErrorCallback errorCallback) {
        hub.invoke("JoinRoom", profileId + "," + chatId).done(doneCallback).onError(errorCallback);
    }

    public static void invokeLeaveRoom(String profileId, String chatId, Action<Void> doneCallback, ErrorCallback errorCallback) {
        hub.invoke("LeaveRoom", profileId + "," + chatId).done(doneCallback).onError(errorCallback);
    }

    public static void invokeMessageSeen(String messageId) {
        hub.invoke("MessageSeen", messageId);
    }

    public static void invokeNewChat(String value, Action<Void> doneCallback, ErrorCallback errorCallback) {
        hub.invoke("NewChat", value).done(doneCallback).onError(errorCallback);
    }

    public static void invokeUpdateChat(String chatId, String chatName, String imageAddress, Action<Void> doneCallback, ErrorCallback errorCallback) {
        hub.invoke("UpdateChat", chatId + "," + imageAddress + "," + chatName).done(doneCallback).onError(errorCallback);
    }

    public static void invokeSendMessage(final CompactMessage message, boolean isGroup) {

        // baraye inke object sangin nashe, faghat ID sender set shode
        //Profile newProfile = new Profile();
        // newProfile.setId(Logged.Models.getUserProfile().getId());
        message.setSender(Logged.Models.getUserProfile());
        hub.invoke(String.class, "SendMessage", new Gson().toJson(message, CompactMessage.class), isGroup).done(new Action<String>() {
            @Override
            public void run(String messageId) throws Exception {
                Intent intent = new Intent(mContext, RealtimeReceiver.class);
                intent.putExtra("id", message.getId());
                intent.putExtra("message_id", messageId);
                intent.putExtra("method", "onHubResponse");
                intent.setAction(Constants.General.UPDATE_CHAT_UI);
                mContext.sendBroadcast(intent);
            }
        });

    }


    public static void invokeGetChat(String chatId, Action<String> doneCallback, ErrorCallback errorCallback) {
        hub.invoke(String.class, "GetChat", chatId).done(doneCallback).onError(errorCallback);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        backgroundService = this;

        if (mContext == null) {
            mContext = getApplicationContext();
        }

        mHandler = new Handler(Looper.getMainLooper());

        if (hub == null) {
            initConnection();
        }

        registerReceiver(broadcastReceiver, new IntentFilter(Constants.General.UPDATE_CHAT_UI));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        initConnection();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        initConnection();
        return mBinder;
    }

    private void setMeOnline() {
        Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(mContext));

        final RProfile profile = realm.where(RProfile.class).equalTo("Id", Logged.Models.getUserProfile().getId()).findFirst();
        if (profile != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    profile.setIsOnline(true);
                    profile.setLastOnline(Long.toString(System.currentTimeMillis()));
                    realm.copyToRealmOrUpdate(profile);
                }
            });
        } else {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RProfile profile = RToNonR.profileToRProfile(Logged.Models.getUserProfile());
                    profile.setIsOnline(true);
                    profile.setLastOnline(Long.toString(System.currentTimeMillis()));
                    realm.copyToRealmOrUpdate(profile);
                }
            });
        }
        realm.close();
    }

    private void setMeOffline() {
        Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(mContext));
        final RProfile profile = realm.where(RProfile.class).equalTo("Id", Logged.Models.getUserProfile().getId()).findFirst();
        if (profile != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    profile.setIsOnline(false);
                    profile.setLastOnline(Long.toString(System.currentTimeMillis()));
                    realm.copyToRealmOrUpdate(profile);
                }
            });
        } else {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RProfile profile = RToNonR.profileToRProfile(Logged.Models.getUserProfile());
                    profile.setIsOnline(false);
                    profile.setLastOnline(Long.toString(System.currentTimeMillis()));
                    realm.copyToRealmOrUpdate(profile);
                }
            });
        }
        realm.close();
    }

    public void initConnection() {

        /*REVIEW barha etefagh oftade ke vaghti be tore static az method haye inja estefde mikonim ke dakheleshon
            az hub etefade shode mesle  RealtimeService.invokeSendMessage(message);
            hub null hast ke man ehtemal midam bekhatere ine ke vaghti onCreate() ejra mishe
            goftim hub == null bood initConnection() ejra beshe bad mirese be inshart va isConnectedToServer true hast dar nahayat
            hub null mimone va exception migirim. albate faghat ye farziast. ya in moshkel ro dorost mikone
            ya bayad begardim bebinim chera service run nashode chon age run ham nashe hub null hast.
         */

        if (hub != null && isConnectedToServer) {
            return;
        }

        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        microsoft.aspnet.signalr.client.Logger logger = new microsoft.aspnet.signalr.client.Logger() {
            @Override
            public void log(String s, LogLevel logLevel) {
                Log.d(RealtimeService.class.getSimpleName(), s);
            }
        };
        if (!BuildConfig.DEBUG)
        //todo change it later
        {
            connection = new HubConnection(Constants.General.PROTOCOL + Constants.General.SERVER_URL + "/signalr", "", false, logger);
        } else {
            connection = new HubConnection(Constants.General.BLOB_PROTOCOL + Constants.General.SERVER_URL + "/signalr", "", false, logger);
        }
        if (Logged.Models.getUserProfile() != null) {
            Credentials credentials = new Credentials() {
                @Override
                public void prepareRequest(Request request) {
                    request.addHeader(AUTH, Utils.getTokenValue());

                }
            };
            connection.setCredentials(credentials);
        }

        connection.error(new ErrorCallback() {
            @Override
            public void onError(final Throwable throwable) {
                if (BuildConfig.DEBUG) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_LONG).show();
                            throwable.printStackTrace();
                        }
                    });
                }
            }
        });

        connection.stateChanged(new StateChangedCallback() {
            @Override
            public void stateChanged(ConnectionState connectionState, ConnectionState connectionState1) {
                if (connectionState1 == ConnectionState.Disconnected) {
                    isConnectedToServer = false;
                } else if (connectionState1 == ConnectionState.Connected) {
                    isConnectedToServer = true;
                }
            }
        });

        connection.reconnecting(new Runnable() {
            @Override
            public void run() {
                onReconnecting();
            }
        });

        connection.connected(new Runnable() {
            @Override
            public void run() {
                if (Logged.Models.getUserProfile() != null) {
                    onConnected();
                    getUnSeenMessagesStatus();
                    // sendUnSentMessages();
                    setMeOnline();
                }
            }
        });

        connection.closed(new Runnable() {
            @Override
            public void run() {
                if (Logged.Models.getUserProfile() != null) {
                    onDisconnected();
                    setMeOffline();
                }
            }
        });

        connection.reconnected(new Runnable() {
            @Override
            public void run() {
                if (Logged.Models.getUserProfile() != null) {
                    onReconnected();
                    getUnSeenMessagesStatus();
                    //sendUnSentMessages();
                    setMeOnline();
                }
            }
        });

        hub = connection.createHubProxy("NewHub");//chub

        initClientEvents();

        ClientTransport clientTransport = new AutomaticTransport(connection.getLogger());
        SignalRFuture<Void> signalRFuture = connection.start(clientTransport);

        try {
            signalRFuture.get();
        } catch (Exception e) {
        }

    }

    private void getInitChats() {
        Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(mContext));

        setIsInitializing(true);
        HttpClient.get(String.format(Constants.Server.Profile.GET_INIT_CHATS, Logged.Models.getUserProfile().getId(), true), httpClientTimeout, new AsyncHttpResponser(mContext, Looper.getMainLooper()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                CompactChat[] chats = GsonParser.getArrayFromGson(responseBody, CompactChat[].class);
                if (chats != null) {
                    onInitChats(chats);
                    HttpClient.get(String.format(Constants.Server.Profile.GET_INIT_CHATS, false), httpClientTimeout, new AsyncHttpResponser(mContext, Looper.getMainLooper()) {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            CompactMessage[] chats = GsonParser.getArrayFromGson(responseBody, CompactMessage[].class);
                            if (chats != null) {
                                onInitMessages(new ArrayList<>(Arrays.asList(chats)));
                                setIsInitializing(false);
                            } else {
                                Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            super.onFailure(statusCode, headers, responseBody, error);

                            setIsInitializing(false);
                        }
                    });
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                setIsInitializing(false);

            }
        });
        realm.close();
    }

    private void getInitNotifications() {
        HttpClient.get(String.format(Constants.Server.Notification.GET_INIT_NOTIFICATIONS), httpClientTimeout, new AsyncHttpResponser(mContext, Looper.getMainLooper()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Notification[] notifications = GsonParser.getArrayFromGson(responseBody, Notification[].class);
                if (notifications != null) {
                    for (Notification notification : notifications) {
                        if (notification.getActor() == null) {
                        } else {
                            Utils.addContact(mContext, notification.getActor());
                        }
                        MyApplication.getInstance().getObserver().makeNotification(mContext, notification);
                    }
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

            }
        });
    }

    private void onInitChats(CompactChat[] chats) {
        Intent intent = new Intent(getApplicationContext(), RealtimeReceiver.class);
        intent.putExtra("chats", new ArrayList<>(Arrays.asList(chats)));
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onInitChats");
        getApplicationContext().sendBroadcast(intent);
    }

    @Override
    public void onConnected() {
        if (BuildConfig.DEBUG) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "connected", Toast.LENGTH_LONG).show();
                }
            });
        }


        getInitChats();
        getInitNotifications();
    }

    @Override
    public void onDisconnected() {
        if (BuildConfig.DEBUG) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Disconnected", Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    public void onReconnected() {
        if (BuildConfig.DEBUG) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Reconnected", Toast.LENGTH_LONG).show();
                }
            });
        }

        getInitChats();
        getInitNotifications();
    }

    @Override
    public void onReconnecting() {
        if (BuildConfig.DEBUG) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.context, "Reconnecting", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void getUnSeenMessagesStatus() {
        Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(mContext));
        RealmResults<RCompactMessage> messages = realm.where(RCompactMessage.class).equalTo("SenderId", Logged.Models.getUserProfile().getId()).findAll();
        ArrayList<String> rMessages = new ArrayList<>();
        for (RCompactMessage message : messages) {
            if (message.getMessageStatus() != EnumMessageStatus.Seen) {
                rMessages.add(message.getId());
            }
        }
        if (rMessages.size() > 0) {
        }
        realm.close();
    }

    private void sendUnSentMessages() {
        //// TODO: 11/3/2016 must be api
        Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(mContext));
    /*for (RMessageStatus messageStatus : messageStatuses)
    {
      RCompactMessage message = realm.where(RCompactMessage.class).equalTo("MessageId", messageStatus.getId()).findFirst();
      if (message != null)
      {
        CompactMessage convertedMessage = RToNonR.rCompactMessageToCompactMessage(message);

        if (!Utils.isNullOrEmpty(convertedMessage.getSender().getLastOnline()) && !convertedMessage.getSender().getLastOnline().contains("T"))
        {
          String lastOnline = TimeHelper.convertLongToServerDate(Long.valueOf(convertedMessage.getSender().getLastOnline()));
          convertedMessage.getSender().setLastOnline(lastOnline);
        } else
        {
          convertedMessage.getSender().setLastOnline(convertedMessage.getSender().getLastOnline());
        }

        if (ChatAdapter.isText(message))
        {
          RealtimeService.invokeSendMessage(convertedMessage);
        } else if (ChatAdapter.isImage(message))
        {
          UploadAsyncImage asynk = new UploadAsyncImage();
          asynk.execute(convertedMessage);
        } else if (ChatAdapter.isVideo(message))
        {
          UploadAsyncVideo asynk = new UploadAsyncVideo();
          asynk.execute(convertedMessage);
        } else if (ChatAdapter.isFile(message))
        {
          UploadAsyncFile asynk = new UploadAsyncFile();
          asynk.execute(convertedMessage);
        } else if (ChatAdapter.isSharedItem(message))
        {
          RealtimeService.invokeSendMessage(convertedMessage);
        }
      }
    }*/
        realm.close();
    }

    public void ReciveMessage(CompactMessage message) {
        onMessageReceived(message);
    }

    @Override
    public void onHubResponse(HashMap<String, String> response) {
    }

    @Override
    public void onMessageReceived(CompactMessage message) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("message", message);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onMessageReceived");
        mContext.sendBroadcast(intent);
    }


    @Override
    public void onMessageSent(String messageId) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("message_id", messageId);
        intent.putExtra("method", "onMessageSent");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onMessageDelivered(ArrayList<String> messageIds) {
    }

    @Override
    public void onMessageSeen(String messageId) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("messageIds", messageId);
        intent.putExtra("method", "onMessageSeen");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onMemberAdded(String chatId, Profile profile) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("profile", profile);
        intent.putExtra("method", "onMemberAdded");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onTitleChanged(String chatId, String newTitle) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("newTitle", newTitle);
        intent.putExtra("method", "onTitleChanged");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onManagerAdded(String chatId, String newManagerId) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("newManagerId", newManagerId);
        intent.putExtra("method", "onManagerAdded");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onMemberLeft(String chatId, String memberId) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("memberId", memberId);
        intent.putExtra("method", "onMemberLeft");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onMemberRemoved(String chatId, String memberId) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("memberId", memberId);
        intent.putExtra("method", "onMemberRemoved");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onMemberJoined(String chatId, Profile profile) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("profile", profile);
        intent.putExtra("method", "onMemberJoined");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onImageChanged(String chatId, String imageAddress) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("imageAddress", imageAddress);
        intent.putExtra("method", "onImageChanged");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onUpdateMessagesStatus(ArrayList<CompactMessage> messagesStatus) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);

        intent.putExtra("messages_status", messagesStatus);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onUpdateMessagesStatus");
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onInitMessages(ArrayList<CompactMessage> messageList) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("messages", messageList);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onInitMessages");
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onUserStatusChanged(String chatId, String profileId, int status) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);

        intent.putExtra("profileId", profileId);
        intent.putExtra("chatId", chatId);
        intent.putExtra("status", status);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onUserStatusChanged");
        mContext.sendBroadcast(intent);
    }

    public void Notification(final Notification notification) {
        if (notification != null) {
            invokeNotificationReceived(notification.getId());

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    if (notification.getActor() == null) {
                    } else {
                        Utils.addContact(mContext, notification.getActor());
                    }
                    MyApplication.getInstance().getObserver().makeNotification(mContext, notification);
                }
            };
            new Handler(Looper.getMainLooper()).post(task);
        }
    }


    public static void invokeNotificationReceived(String notificationId) {
        hub.invoke("NotificationReceived", notificationId);
    }

    public void UserStatusChanged(String chatId, String profileId, int status) {
        onUserStatusChanged(chatId, profileId, status);
    }

    public void MemberAdded(String chatId, Profile profile) {
        onMemberAdded(chatId, profile);
    }

    public void TitleChanged(String chatId, String NewTitle) {
        onTitleChanged(chatId, NewTitle);
    }

    public void ManagerAdded(String chatId, String newManagerId) {
        onManagerAdded(chatId, newManagerId);
    }

    public void MemberLeft(String chatId, String memberId) {
        onMemberLeft(chatId, memberId);
    }

    public void MemberRemoved(String chatId, String memberId) {
        onMemberRemoved(chatId, memberId);
    }

    public void MemberJoined(String chatId, Profile profile) {
        onMemberJoined(chatId, profile);
    }

    public void ImageChanged(String chatId, String imageAddress) {
        onImageChanged(chatId, imageAddress);
    }

    public void ChatInfoUpdated(String input) {
        String[] strings = input.split(",");
        String chatId = strings[0];
        String imageAddress = strings[1];
        String chatName = strings[2];
        onChatInfoUpdated(chatId, chatName, imageAddress);
    }

    public void MessageSent(String messageId) {
        onMessageSent(messageId);
    }

    public void MessageDelivered(String messagesId) {
        ArrayList<String> strings = new ArrayList<>();
        assert messagesId != null;
        Collections.addAll(strings, messagesId.split(","));
        onMessageDelivered(strings);
    }

    public void MessageSeen(String messagesId) {
        onMessageSeen(messagesId);
    }

    public void NewChat(CompactChat chat) {
        onNewChat(chat);
    }

    @Override
    public void onNewChat(CompactChat chatId) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chat", chatId);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onNewChat");
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onInitChats(ArrayList<CompactChat> chat) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chat", chat);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onInitChat");
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onMessageReceivedForConversation(CompactMessage message) {
        //empty
    }

    @Override
    public void onMessageSeenForConversation(String messagesId) {
        //empty
    }

    @Override
    public void onAddSettingsMessageForConversation(String messageId, String chatId, String messageText, String dateTime) {
        //empty
    }

    @Override
    public void onChatInfoUpdatedForConversation(String chatId, String groupName, String imageAddress) {
        //empty
    }

    @Override
    public void onChatDeleted(CompactChat chat) {
        //empty
    }

    @Override
    public void onIsInitializing(boolean isInitializing) {
        //empty
    }

    public void ChatUpdated(CompactChat chat) {
        onChatUpdated(chat);
    }

    public void AddSettingsMessage(String input) {
        String[] splitted = input.split(",");
        String messageId = splitted[0];
        String chatId = splitted[1];
        String messageText = splitted[2];
        String dateTime = splitted[3];

        onAddSettingsMessage(messageId, chatId, messageText, dateTime);
    }

    @Override
    public void onAddSettingsMessage(String messageId, String chatId, String messageText, String dateTime) {

        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("message_text", messageText);
        intent.putExtra("message_id", messageId);
        intent.putExtra("datetime", dateTime);
        intent.putExtra("method", "onAddSettingsMessage");
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onChatUpdated(CompactChat chat) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chat", chat);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onChatUpdated");
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onChatInfoUpdated(String chatId, String groupName, String imageAddress) {
        Intent intent = new Intent(mContext, RealtimeReceiver.class);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("group_name", groupName);
        intent.putExtra("image_address", imageAddress);
        intent.setAction(Constants.General.UPDATE_CHAT_UI);
        intent.putExtra("method", "onChatInfoUpdated");
        mContext.sendBroadcast(intent);
    }

    public void InitMessages(ArrayList<CompactMessage> messages) {
        onInitMessages(messages);
    }


    private void initClientEvents() {
        hub.subscribe(this);
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public RealtimeService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return RealtimeService.this;
        }
    }
}
