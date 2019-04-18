/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.activities.ActivityPickLocation;
import com.collect_up.c_up.adapters.ChatAdapter;
import com.collect_up.c_up.chat.AndroidUtilities;
import com.collect_up.c_up.chat.NotificationCenter;
import com.collect_up.c_up.chat.widgets.Emoji;
import com.collect_up.c_up.helpers.AppUtils;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.ChatNotificationUtils;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.JsonHttpResponser;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.SortMessages;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.IUpdateChatUICallbacks;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.listeners.IloadMoreClickListenr;
import com.collect_up.c_up.listeners.StartlessChatScrollListener;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.EnumMessageStatus;
import com.collect_up.c_up.model.EnumProfileStatus;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.StickerPackage;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.model.realm.RProfile;
import com.collect_up.c_up.receivers.RealtimeReceiver;
import com.collect_up.c_up.services.RealtimeService;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.collect_up.c_up.view.chat.EmojiView;
import com.collect_up.c_up.view.chat.SizeNotifierRelativeLayout;
import com.collect_up.c_up.view.chat.StickerView;
import com.github.developerpaul123.filepickerlibrary.FilePickerActivity;
import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rey.material.app.BottomSheetDialog;
import com.yalantis.ucrop.Picker;
import com.yalantis.ucrop.model.MediaContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;

import static com.collect_up.c_up.fragments.FragmentPostDisplay.pageSize;
import static com.collect_up.c_up.helpers.Constants.General.MAX_CHAT_SELECT_IMAGE;
import static com.collect_up.c_up.helpers.Constants.General.MAX_CHAT_SELECT_VIDEO;

@SuppressLint ("ValidFragment")

public class FragmentChat extends BaseFragment
  implements SizeNotifierRelativeLayout.SizeNotifierRelativeLayoutDelegate,
  NotificationCenter.NotificationCenterDelegate,
  IUpdateChatUICallbacks,
  View.OnKeyListener,
  TextWatcher, ChatAdapter.OnMessageClick {

  public static boolean isRunning;
  public CompactChat mChat;
  public static String chatId;
  @Bind (R.id.toolbar)
  public Toolbar toolbar;
  @Bind (R.id.list_view_chats)
  ListView chatsList;
  @Bind (R.id.edit_text_message)
  EditText messageText;
  @Bind (R.id.image_view_picture)
  ComplexAvatarView picture;
  @Bind (R.id.image_view_send)
  ImageView send;
  @Bind (R.id.image_view_attach)
  ImageView attach;
  @Bind (R.id.image_view_emoji)
  ImageView showEmoji;
  @Bind (R.id.imgStickers)
  ImageView showSticker;
  @Bind (R.id.text_view_subtitle)
  TextView subtitle;
  @Bind (R.id.linear_layout_empty_container)
  LinearLayout emptyContainer;
  @Bind (R.id.text_view_title)
  TextView title;
  @Bind (R.id.text_view_removed_from_group_text)
  TextView removedFromGroupText;
  @Bind (R.id.linear_layout_chat_bottom_container_removed_from_group)
  LinearLayout chatBottomContainerRemovedFromGroup;
  @Bind (R.id.linear_layout_chat_bottom_container)
  LinearLayout chatBottomContainer;
  private int mCurrentPage = 0;

  public FragmentChat(CompactChat mChat) {
    this.mChat = mChat;
  }

  List<String> rMessagesIdsNotSenderMe = new ArrayList<>();
  private EmojiView emojiView;
  private int keyboardHeight;
  private boolean keyboardVisible;
  private ChatAdapter listAdapter;
  private RealtimeReceiver mUpdateChatUIReceiver;
  private boolean showingEmoji;
  private boolean showingSticker;
  private SizeNotifierRelativeLayout sizeNotifierRelativeLayout;
  private WindowManager.LayoutParams windowLayoutParams;
  private boolean isTyping;
  private long timeTyping;
  private Calendar lastDateCalendar = Calendar.getInstance();
  private Menu mMenu;
  private StickerView stickerView;
  public boolean isBlocked;
  private LatLng mPickedLocation;
  private View view;

  @OnClick (R.id.image_view_send)
  public void send() {
    sendMessage();
  }

  public FragmentChat() {
  }

  private void sendSticker(String sticker) {

    final CompactMessage mMessage = new CompactMessage();

    mMessage.setContentType(EnumMessageContentType.Sticker);
    mMessage.setId(UUID.randomUUID().toString());
    mMessage.setChatId(mChat.getChatId());
    mMessage.setContentAddress(sticker);
    mMessage.setSendDateTime(Long.toString(Utils.localNow()));
    mMessage.setSender(Logged.Models.getUserProfile());
    RealtimeService.invokeSendMessage(mMessage, false);
    timeTyping = 0;
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        RCompactMessage rMessage = RToNonR.compactMessageToRCompactMessage(mMessage);
        mRealm.copyToRealmOrUpdate(rMessage);
      }
    });

    mRealm.close();
    listAdapter.insertItem(RToNonR.compactMessageToRCompactMessage(mMessage), true);

    messageText.setText("");

    moveChatListToEnd();
  }

  private void sendMessage() {
    if (messageText.getText().toString().trim().length() == 0)
    {
      return;
    }

    final CompactMessage mMessage = new CompactMessage();
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    mMessage.setId(UUID.randomUUID().toString());
    mMessage.setReceiverId(mChat.getReceiverId());
    mMessage.setChatId(mChat.getChatId());
    mMessage.setText(messageText.getText().toString().trim());
    mMessage.setContentType(EnumMessageContentType.Text);
    mMessage.setSendDateTime(Long.toString(Utils.localNow()));
    mMessage.setSender(Logged.Models.getUserProfile());
    mMessage.setRank(mRealm.where(RCompactMessage.class).equalTo("ChatId", mChat.getChatId()).max("Rank") != null ? mRealm.where(RCompactMessage.class).equalTo("ChatId", mChat.getChatId()).max("Rank").longValue() + 1 :
      0);

    timeTyping = 0;
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {

        RCompactMessage rMessage = RToNonR.compactMessageToRCompactMessage(mMessage);
        RealtimeService.invokeSendMessage(mMessage, false);

        mRealm.copyToRealmOrUpdate(rMessage);
      }
    });

    mRealm.close();
    listAdapter.insertItem(RToNonR.compactMessageToRCompactMessage(mMessage), true);

    messageText.setText("");

    moveChatListToEnd();
  }

  private void moveChatListToEnd() {
    chatsList.setSelection(listAdapter.getCount() - 1);
  }

  @OnClick (R.id.edit_text_message)
  public void messageText() {
    int lastVisiblePosition = chatsList.getLastVisiblePosition();
    if (!keyboardVisible && lastVisiblePosition == listAdapter.getMessages().size() - 1)
    {
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          moveChatListToEnd();
        }
      }, 500);
    }

    if (showingEmoji)
    {
      hideEmojiPopup();
    }
    if (showingSticker)
    {
      hideStickerPopup();
    }
  }

  /**
   * Hides the emoji popup
   */
  public void hideEmojiPopup() {
    if (showingEmoji)
    {
      showEmojiPopup(false);
    }
  }

  public void hideStickerPopup() {
    if (showingSticker)
    {
      showStickerPopup(false);
    }
  }

  /**
   * Show or hide the emoji popup
   *
   * @param show
   */
  private void showStickerPopup(boolean show) {
    if (Logged.Models.getUserStickerPackages(getContext()).size() > 0)
    {

      showingSticker = show;

      if (show)
      {

        if (stickerView == null)
        {

          stickerView = new StickerView(getActivity(), new StickerView.Listener() {
            @Override
            public void onBackspace() {
              messageText.dispatchKeyEvent(new KeyEvent(0, 67));
            }

            public void onStickerSelected(String sticker, StickerPackage stickerPackage) {
              int i = messageText.getSelectionEnd();
              if (i < 0)
              {
                i = 0;
              }

              sendSticker(sticker);
            }
          });


          windowLayoutParams = new WindowManager.LayoutParams();
          windowLayoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;

          windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
          windowLayoutParams.token = getActivity().getWindow()
            .getDecorView()
            .getWindowToken();
          windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }

        final int currentHeight;

        if (keyboardHeight <= 0)
        {
          keyboardHeight = MyApplication.getInstance()
            .getSharedPreferences("emoji", 0)
            .getInt("kbd_height", AndroidUtilities.dp(200));
        }

        currentHeight = keyboardHeight;

        WindowManager wm = (WindowManager) MyApplication.getInstance()
          .getSystemService(Activity.WINDOW_SERVICE);

        windowLayoutParams.height = currentHeight;
        windowLayoutParams.width = AndroidUtilities.displaySize.x;
        try
        {
          if (stickerView.getParent() != null)
          {
            wm.removeViewImmediate(stickerView);

          }
        } catch (Exception e)
        {
        }

        try
        {
          wm.addView(stickerView, windowLayoutParams);
        } catch (Exception e)
        {
          return;
        }

        if (!keyboardVisible)
        {
          if (sizeNotifierRelativeLayout != null)
          {
            sizeNotifierRelativeLayout.setPadding(0, 0, 0, currentHeight);
          }

          return;
        }

      } else
      {
        removeStickerWindow();
        removeEmojiWindow();
        if (sizeNotifierRelativeLayout != null)
        {
          sizeNotifierRelativeLayout.post(new Runnable() {
            public void run() {
              if (sizeNotifierRelativeLayout != null)
              {
                sizeNotifierRelativeLayout.setPadding(0, 0, 0, 0);
              }
            }
          });
        }
      }
    } else
    {
      FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_ALLSTICKERS, null);

      return;
    }
  }

  private void showEmojiPopup(boolean show) {
    showingEmoji = show;

    if (show)
    {

      if (emojiView == null)
      {
        emojiView = new EmojiView(getActivity());

        emojiView.setListener(new EmojiView.Listener() {
          public void onBackspace() {
            messageText.dispatchKeyEvent(new KeyEvent(0, 67));
          }

          public void onEmojiSelected(String symbol) {
            int i = messageText.getSelectionEnd();
            if (i < 0)
            {
              i = 0;
            }
            try
            {
              CharSequence localCharSequence = Emoji.replaceEmoji(getContext(), symbol, messageText
                .getPaint()
                .getFontMetricsInt(), AndroidUtilities.dp(20));
              messageText.setText(messageText.getText().insert(i, localCharSequence));
              int j = i + localCharSequence.length();
              messageText.setSelection(j, j);
            } catch (Exception e)
            {
            }
          }
        });


        windowLayoutParams = new WindowManager.LayoutParams();
        windowLayoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        windowLayoutParams.token = getActivity().getWindow()
          .getDecorView()
          .getWindowToken();
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
      }

      final int currentHeight;

      if (keyboardHeight <= 0)
      {
        keyboardHeight = MyApplication.getInstance()
          .getSharedPreferences("emoji", 0)
          .getInt("kbd_height", AndroidUtilities.dp(200));
      }

      currentHeight = keyboardHeight;

      WindowManager wm = (WindowManager) MyApplication.getInstance()
        .getSystemService(Activity.WINDOW_SERVICE);

      windowLayoutParams.height = currentHeight;
      windowLayoutParams.width = AndroidUtilities.displaySize.x;

      try
      {
        if (emojiView.getParent() != null)
        {
          wm.removeViewImmediate(emojiView);
        }
      } catch (Exception e)
      {
      }

      try
      {
        wm.addView(emojiView, windowLayoutParams);
      } catch (Exception e)
      {
        return;
      }

      if (!keyboardVisible)
      {
        if (sizeNotifierRelativeLayout != null)
        {
          sizeNotifierRelativeLayout.setPadding(0, 0, 0, currentHeight);
        }

        return;
      }

    } else
    {
      removeEmojiWindow();
      removeStickerWindow();
      if (sizeNotifierRelativeLayout != null)
      {
        sizeNotifierRelativeLayout.post(new Runnable() {
          public void run() {
            if (sizeNotifierRelativeLayout != null)
            {
              sizeNotifierRelativeLayout.setPadding(0, 0, 0, 0);
            }
          }
        });
      }
    }
  }


  /**
   * Remove emoji window
   */
  private void removeEmojiWindow() {
    if (emojiView == null)
    {
      return;
    }
    try
    {
      if (emojiView.getParent() != null)
      {
        WindowManager wm = (WindowManager) MyApplication.getInstance()
          .getSystemService(Context.WINDOW_SERVICE);
        wm.removeViewImmediate(emojiView);
      }
    } catch (Exception e)
    {
    }
  }

  private void removeStickerWindow() {
    if (stickerView == null)
    {
      return;
    }
    try
    {
      if (stickerView.getParent() != null)
      {
        WindowManager wm = (WindowManager) MyApplication.getInstance()
          .getSystemService(Context.WINDOW_SERVICE);
        wm.removeViewImmediate(stickerView);
      }
    } catch (Exception e)
    {
    }
  }

  @OnClick (R.id.image_view_emoji)
  public void showEmoji() {
    showEmojiPopup(true);

    int lastVisiblePosition = chatsList.getLastVisiblePosition();
    if (lastVisiblePosition == listAdapter.getMessages().size() - 1)
    {
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          moveChatListToEnd();
        }
      }, 500);
    }
  }

  @OnClick (R.id.imgStickers)
  public void showSticker() {

    showStickerPopup(true);
    int lastVisiblePosition = chatsList.getLastVisiblePosition();
    if (lastVisiblePosition == listAdapter.getMessages().size() - 1)
    {
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          moveChatListToEnd();
        }
      }, 500);
    }
  }

  @OnClick (R.id.image_view_attach)
  public void attachFile() {
    hideEmojiPopup();
    hideStickerPopup();
    final BottomSheetDialog mSheetDialog = new BottomSheetDialog(getContext());

    mSheetDialog.contentView(R.layout.bottom_sheet_chat_attach)
      .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
      .inDuration(300)
      .cancelable(true)
      .show();

    // Initialize views
    TextView camera = (TextView) mSheetDialog.findViewById(R.id.text_view_camera);
    TextView gallery = (TextView) mSheetDialog.findViewById(R.id.text_view_gallery);
    TextView video = (TextView) mSheetDialog.findViewById(R.id.text_view_video);
    TextView file = (TextView) mSheetDialog.findViewById(R.id.text_view_file);
    TextView location = (TextView) mSheetDialog.findViewById(R.id.text_view_location);
    TextView hide = (TextView) mSheetDialog.findViewById(R.id.text_view_hide);

    // Set click listener for views
    hide.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mSheetDialog.dismiss();
      }
    });
    camera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mSheetDialog.dismiss();
      }
    });
    gallery.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PermissionListener dialogPermissionListener =
          CustomPermissionDialog.Builder
            .withContext(getActivity())
            .withTitle(R.string.permission_title)
            .withMessage(R.string.permission_storage)
            .withButtonText(android.R.string.ok)
            .build();
        PermissionListener basePermission = new PermissionListener() {
          @Override
          public void onPermissionGranted(PermissionGrantedResponse response) {

            mSheetDialog.dismiss();
            final Picker.PickerBuilder pickerBuilder = new Picker.PickerBuilder(FragmentChat.this, MAX_CHAT_SELECT_IMAGE, MAX_CHAT_SELECT_VIDEO)
              .setMultiple(true)
              .setFreeStyle(true);

            Picker picker = new Picker(pickerBuilder);
            picker.start();
          }

          @Override
          public void onPermissionDenied(PermissionDeniedResponse response) {

          }

          @Override
          public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
            token.continuePermissionRequest();

          }
        };
        CompositePermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
        Dexter.withActivity(getActivity())
          .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
          .withListener(compositePermissionListener)
          .check();

      }
    });
    video.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        mSheetDialog.dismiss();
      }
    });
    file.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mSheetDialog.dismiss();
        Utils.pickFile(FragmentChat.this, Constants.RequestCodes.PICK_FILE.ordinal());
      }
    });
    location.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSheetDialog.dismiss();
        Intent intent = new Intent(getContext(), ActivityPickLocation.class);
        intent.putExtra("return", true);
        intent.putExtra("location", mPickedLocation);
        startActivityForResult(intent, Constants.RequestCodes.PICK_LOCATION.ordinal());
      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.RequestCodes.PICK_FILE.ordinal() && resultCode == getActivity().RESULT_OK)
    {
      String fileAddress = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);

      UploadAsyncFile asynk = new UploadAsyncFile();
      asynk.execute(fileAddress);
      return;

    } else if (requestCode == Constants.RequestCodes.SHARE_PRODUCT.ordinal() && resultCode == getActivity().RESULT_OK)
    {
      final CompactMessage message = data.getParcelableExtra("message");

      RealtimeService.invokeSendMessage(message, false);

      listAdapter.insertItem(RToNonR.compactMessageToRCompactMessage(message), true);
      final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));
        }
      });
      mRealm.close();
      return;

    } else if (requestCode == Constants.RequestCodes.PICK_LOCATION.ordinal() && resultCode == getActivity().RESULT_OK)
    {
      mPickedLocation = data.getParcelableExtra("location");

      final CompactMessage mMessage = new CompactMessage();

      mMessage.setContentType(EnumMessageContentType.Location);
      mMessage.setText(mPickedLocation.toString());
      mMessage.setMessageId(UUID.randomUUID().toString());
      mMessage.setChatId(mChat.getChatId());
      mMessage.setId(UUID.randomUUID().toString());
      mMessage.setSendDateTime(Long.toString(Utils.localNow()));
      mMessage.setSender(Logged.Models.getUserProfile());
      RealtimeService.invokeSendMessage(mMessage, false);
      timeTyping = 0;
      final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          RCompactMessage rMessage = RToNonR.compactMessageToRCompactMessage(mMessage);
          mRealm.copyToRealmOrUpdate(rMessage);
        }
      });

      mRealm.close();
      listAdapter.insertItem(RToNonR.compactMessageToRCompactMessage(mMessage), true);

      messageText.setText("");

      moveChatListToEnd();
      return;
    }
    if (data != null)
    {
      final ArrayList<MediaContent> media = data.getBundleExtra("data").getParcelableArrayList("data");
      if (media != null && media.size() > 0)
      {
        for (int i = 0; i < media.size(); i++)
        {


          if (media.get(i).getType() == MediaContent.IS_VIDEO && resultCode == getActivity().RESULT_OK)
          {
            String videoPath = media.get(i).getUri().getPath();//Utils.getPath(getContext(), media.get(i).getUri());

            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(getContext(), media.get(i).getUri());
            long duration = Long.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 2;
            Bitmap bitmap = metadataRetriever.getFrameAtTime(duration, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            metadataRetriever.release();
            try
            {
              String savedBitmapFilePath = Utils.saveBitmap(getContext(), bitmap, Constants.General.APP_FOLDER_VIDEO_THUMB_PATH);
              if (!bitmap.isRecycled())
              {
                bitmap.recycle();
              }
              String compressJpegPath = Images.compressJpeg(getContext(), savedBitmapFilePath, Constants.General.APP_FOLDER_VIDEO_THUMB_PATH, true);
              UploadAsyncVideo async = new UploadAsyncVideo();
              async.execute(compressJpegPath, videoPath);
            } catch (IOException e)
            {
            }

          } else if (media.get(i).getType() == MediaContent.IS_IMAGE && resultCode == getActivity().RESULT_OK)
          {
            final int finalI = i;
            Thread f = new Thread(new Runnable() {
              @Override
              public void run() {
                UploadAsyncImage asynk = new UploadAsyncImage();
                asynk.execute(media.get(finalI).getUri().getPath());
                try
                {
                  Thread.sleep(300);
                } catch (InterruptedException e)
                {
                }
              }
            });
            f.start();
          }
        }
      }
    }
  }


  /**
   * Check if the emoji popup is showing
   *
   * @return
   */

  public boolean isEmojiPopupShowing() {
    return showingEmoji;
  }

  public boolean isStickerPopupShowing() {
    return showingSticker;
  }

  @Override
  public void onPause() {
    super.onPause();
    hideEmojiPopup();
    hideStickerPopup();

    if (mUpdateChatUIReceiver != null)
    {
      getActivity().unregisterReceiver(mUpdateChatUIReceiver);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    ((ActivityHome) getActivity()).changeButtonBackgroud(3);
    ((ActivityHome) getActivity()).hideButtonBar(true);
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    if (mUpdateChatUIReceiver == null)
    {
      mUpdateChatUIReceiver = new RealtimeReceiver(this);
    }
    IntentFilter intentFilter = new IntentFilter(Constants.General.UPDATE_CHAT_UI);
    getActivity().registerReceiver(mUpdateChatUIReceiver, intentFilter);

    List<String> stringList = new ArrayList<>();

    if (mChat.getLastMessage() != null)
    {
      RealtimeService.invokeMessageSeen(mChat.getLastMessage().getMessageId());

      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {


          RChat chat = mRealm.where(RChat.class).equalTo("ChatId", mChat.getChatId()).findFirst();
          chat.setUnSeenMessageCount(0);
          realm.copyToRealmOrUpdate(chat);
        }
      });
    }
    notifyAdapter();
    //   moveChatListToEnd();

    initViews();
    mRealm.close();
  }

  private boolean doesAdapterContainsMessage(String messageId) {
    boolean hasIt = false;
    for (RCompactMessage message : listAdapter.getMessages())
    {
      if (message != null && message.getMessageId().equals(messageId))
      {
        hasIt = true;
        break;
      }
    }

    return hasIt;
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
    chatId = mChat.getChatId();
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    if (!Utils.isNullOrEmpty(s.toString()))
    {
      timeTyping = System.currentTimeMillis();
    }
  }

  @Override
  public void afterTextChanged(Editable s) {
    if (messageText.getText().toString().trim().equals("") && attach.getVisibility() != View.VISIBLE)
    {
      send.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
      send.setVisibility(View.GONE);
      attach.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
      attach.setVisibility(View.VISIBLE);

    } else if (!messageText.getText()
      .toString().trim()
      .equals("") && send.getVisibility() != View.VISIBLE)
    {
      send.setImageResource(R.drawable.ic_chat_send_active);
      send.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
      send.setVisibility(View.VISIBLE);
      attach.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
      attach.setVisibility(View.GONE);
    }
  }

  /**
   * Updates emoji views when they are complete loading
   *
   * @param id
   * @param args
   */
  @Override
  public void didReceivedNotification(int id, Object... args) {
    if (id == NotificationCenter.emojiDidLoaded)
    {
      if (emojiView != null)
      {
        emojiView.invalidateViews();
      }

      if (chatsList != null)
      {
        chatsList.invalidateViews();
      }
    }
  }

  private void loadMoreMessages(final int page) {

    Pagination.getBirateralMessages(getContext(), chatId, page, new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(final List<T> pageList) {

        if (pageList.size() == 0)
        {
          return;
        }
        final List<RCompactMessage> messages = new ArrayList<RCompactMessage>();

        for (final CompactMessage compactMessage : (List<CompactMessage>) pageList)
        {
          messages.add(RToNonR.compactMessageToRCompactMessage(compactMessage));
        }
        Collections.sort(messages, SortMessages.ASC);
        listAdapter.insertItemsAtTop(messages);

      }

      @Override
      public void onFailure() {

      }
    });
  }

  public static boolean containsId(ArrayList<RCompactMessage> list, String id) {
    for (RCompactMessage object : list)
    {
      if (object != null && object.getMessageId() != null && object.getMessageId().equals(id))
      {
        return true;
      }
    }
    return false;
  }

  private static ArrayList<RCompactMessage> removeDuplicates(ArrayList<RCompactMessage> list) {
    ArrayList<RCompactMessage> set = new ArrayList<>();
    if (list.size() > 0)
    {
      set.add(list.get(0));

      for (RCompactMessage message : list)
      {
        if (!containsId(set, message.getMessageId()))
        {
          set.add(message);
        }
      }
    }
    return set;
  }

  private void loadLocalMessages() {
    mCurrentPage = 1;
    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(MyApplication.context));
    ArrayList<RCompactMessage> messages = new ArrayList<>();
    Number maxRank = realm.where(RCompactMessage.class).equalTo("ChatId", mChat.getChatId()).max("Rank");
    if (maxRank == null)
    {
      listAdapter = new ChatAdapter(getActivity(), messages, chatsList);
      return;
    } RealmResults<RCompactMessage> rCompactMessage = realm.where(RCompactMessage.class)
      .equalTo("ChatId", mChat.getChatId())
      .greaterThanOrEqualTo("Rank", (maxRank.longValue() + 1) - pageSize)
      .lessThan("Rank", maxRank.longValue() + 1)
      .findAllSorted("Rank", Sort.DESCENDING);
    messages.addAll(rCompactMessage.subList(0, rCompactMessage.size()));
    messages = removeDuplicates(messages);
    Collections.sort(messages, SortMessages.ASC);

    messages = addDateAnnonce(messages);


    listAdapter = new ChatAdapter(getActivity(), messages, chatsList);
    listAdapter.setLoadMoreListener(new IloadMoreClickListenr() {
                                      @Override
                                      public void onLoadMoreClick() {
                                        mCurrentPage++;
                                        loadMoreMessages(mCurrentPage);
                                      }
                                    }
    );
    final List<RCompactMessage> finalMessages = messages;
    chatsList.setOnScrollListener(new StartlessChatScrollListener() {
      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
      }

      @Override
      public void onLoadMore(int page, int totalItemsCount) {
        if (finalMessages.size() >= pageSize)
        {
          super.onLoadMore(page, totalItemsCount);
          listAdapter.onLoadMoreReady(true);
        }
      }

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
      }
    });

    chatsList.setEmptyView(emptyContainer);
    listAdapter.setListener(FragmentChat.this);

    chatsList.setAdapter(listAdapter);


    listAdapter.notifyDataSetChanged();


    moveChatListToEnd();
  }

  private ArrayList<RCompactMessage> addDateAnnonce(ArrayList<RCompactMessage> messages) {
    lastDateCalendar.clear();

    ArrayList<RCompactMessage> arrayListRMessage = new ArrayList<>();

    for (RCompactMessage message : messages)
    {
      String time = getTimeSettingMessage(message.getSendDateTime());
      if (time != null)
      {
        RCompactMessage settingMessage = new RCompactMessage();
        settingMessage.setMessageId("");
        settingMessage.setChatId(mChat.getChatId());
        settingMessage.setContentType(EnumMessageContentType.Announcement);
        settingMessage.setSendDateTime(Long.toString(message.getSendDateTime().contains("T") ? TimeHelper.utcToTimezone(getContext(), message.getSendDateTime()) - 1 : Long.valueOf(message.getSendDateTime()) - 1));
        settingMessage.setText(time);
        arrayListRMessage.add(settingMessage);
      }

      /*for (int i = 0; i < rCompactMessages.size(); i++)
      {
        if (message.getMessageId().equals(rCompactMessages.get(i).getMessageId()))
        {
          message.setFilePath(rCompactMessages.get(i).getFilePath());
        }

      }*/
      arrayListRMessage.add(message);
    }
    return arrayListRMessage;
  }

  private void loadFirstMessagePages() {
    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(MyApplication.context));

    Pagination.getBirateralMessages(getContext(), mChat.getChatId(), mCurrentPage, new IPaginationCallback() {
        @Override
        public <T> void onPageReceived(final List<T> pageList) {
          // Reset items to maeke the pull to refresh right

          final ArrayList<RCompactMessage> messages = new ArrayList<>();

          realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

              for (CompactMessage message : (List<CompactMessage>) pageList)
              {
                messages.add(RToNonR.compactMessageToRCompactMessage(message));

                if (realm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst() == null)
                {
                  realm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));
                }

              }
            }
          });
          if (messages.size() > 0)
          {
            RealtimeService.invokeMessageSeen(messages.get(0).getMessageId());
            Collections.sort(messages, SortMessages.ASC);
          }


          final RealmResults<RCompactMessage> rCompactMessages = realm.where(RCompactMessage.class).equalTo("ChatId", mChat.getChatId()).findAll();
          realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

              for (int i = rCompactMessages.size() - 1; i >= 0; i--)
              {
                if (!rCompactMessages.get(i).getSenderId().equals(Logged.Models.getUserProfile().getId())
                  && rCompactMessages.get(i).getMessageStatus() != EnumMessageStatus.Seen)
                {
                  rCompactMessages.get(i).setMessageStatus(EnumMessageStatus.Seen);
                }
              }


            }
          });

          List<RCompactMessage> arrayListRMessage = addDateAnnonce(messages);

          while (listAdapter.getMessages().size() >= 1)
          {
            if (listAdapter.getMessages().get(0) == null && listAdapter.getMessages().size() == 1)
            {
              break;
            } else if (listAdapter.getMessages().get(0) != null && listAdapter.getMessages().size() == 1)
            {

              listAdapter.getMessages().remove(0);
            } else
            {
              listAdapter.getMessages().remove(1);
            }

          }


          for (RCompactMessage rCompactMessage : arrayListRMessage)

          {
            listAdapter.insertItem(rCompactMessage, true);
          }

          listAdapter.setListener(FragmentChat.this);

          chatsList.setAdapter(listAdapter);


          listAdapter.notifyDataSetChanged();


          moveChatListToEnd();

        }

        @Override
        public void onFailure() {

        }
      }

    );
    realm.close();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

      view = inflater.inflate(R.layout.fragment_chat, container, false);
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
      {

        view.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.chat_background_portrait));

      } else
      {
        view.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.chat_background_portrait));
      }
      ButterKnife.bind(this, view);
      setHasOptionsMenu(true);
      toolbar.inflateMenu(R.menu.menu_chat);
      onPrepareOptionsMenu(toolbar.getMenu());
      ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
      toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          onOptionsItemSelected(item);
          return false;
        }
      });

      lastDateCalendar.clear();

      checkIfOrientationChanged(getResources().getConfiguration());


      chatId = mChat.getChatId();
      HttpClient.get(String.format(Constants.Server.Profile.GET_ISBLOCK, mChat.getReceiverId()), new

        JsonHttpResponser(getContext()) {

          @Override
          public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);

            try
            {
              isBlocked = response.getBoolean("IsBlocked");
              if (isBlocked)
              {

                changeViewBlock(mChat.getReceiverId());
              } else
              {
              }
            } catch (JSONException e)
            {
              e.printStackTrace();
            }
          }

          @Override
          public void onFailure(int statusCode, Header[] headers, String responseString, Throwable
            throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
          }


        }

      );
      initViews();
      loadLocalMessages();
      loadFirstMessagePages();

      final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));


      AndroidUtilities.statusBarHeight = Utils.getStatusBarHeight(getContext());
      messageText.setOnKeyListener(this);

      Timer timer = new Timer();
      timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          if (System.currentTimeMillis() - timeTyping > 2000)
          {
            setIsTyping(false);
          } else
          {
            setIsTyping(true);
          }
        }
      }, 200, 200);

      messageText.addTextChangedListener(this);

      sizeNotifierRelativeLayout = (SizeNotifierRelativeLayout) view.findViewById(R.id.chat_layout);
      sizeNotifierRelativeLayout.delegate = this;

      NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);

      for (String messageId : rMessagesIdsNotSenderMe)
      {
        final RCompactMessage rCompactMessage = new RCompactMessage();
        rCompactMessage.setMessageId(messageId);
        rCompactMessage.setMessageStatus(EnumMessageStatus.Seen);
        mRealm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            mRealm.copyToRealmOrUpdate(rCompactMessage);
          }
        });
      }

      mRealm.close();
      if (mChat.getLastOnline() != null && mChat.getLastOnline().contains("T"))
      {
        subtitle.setText(String.format(getString(R.string.last_seen), TimeHelper.makeLastSeen(getContext(), new Date(TimeHelper.utcToTimezone(getContext(), mChat.getLastOnline())))));

      } else
      {
        subtitle.setText(R.string.online);
      }
      RealtimeService.invokeGetUpdateProfileStatus(mChat.getReceiverId(), new Action<String>() {
        @Override
        public void run(final String s) throws Exception {
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
              final RProfile profile = mRealm.where(RProfile.class).equalTo("Id", mChat.getReceiverId()).findFirst();

              if (s.contains("T"))
              {
                subtitle.setText(R.string.online);//String.format(getString(R.string.last_seen), TimeHelper.makeLastSeen(getContext(), new Date(TimeHelper.utcToTimezone(getContext(), s)))));
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    profile.setLastOnline(s);
                    profile.setIsOnline(false);
                    realm.copyToRealmOrUpdate(profile);
                  }
                });
              } else
              {
                subtitle.setText(R.string.online);
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    profile.setLastOnline(Long.toBinaryString(System.currentTimeMillis()));
                    profile.setIsOnline(true);
                    realm.copyToRealmOrUpdate(profile);
                  }
                });
              }
              mRealm.close();
            }
          });

        }
      }, new ErrorCallback() {
        @Override
        public void onError(Throwable throwable) {

        }
      });

      ChatNotificationUtils.cancelNotification(getContext());
      return view;
    } else
    {
      return view;
    }
  }


  private void initViews() {


    if (!mChat.getTitle().equalsIgnoreCase(Logged.Models.getUserProfile().getName())
      && mChat.getReceiverId().equalsIgnoreCase(Logged.Models.getUserProfile().getId()))
    {

      title.setText(mChat.getTitle());
      picture.makeAllDefaults();
      picture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(mChat.getTitle()).setImageUrl(Constants.General.BLOB_PROTOCOL + mChat.getProfileThumbnailAddress());

    } else
    {
      picture.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          openProfileActivity();
        }
      });
      title.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          openProfileActivity();
        }
      });
      subtitle.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          openProfileActivity();
        }
      });
      title.setText(mChat.getTitle());
      picture.makeAllDefaults();
      picture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(mChat.getTitle()).setImageUrl(Constants.General.BLOB_PROTOCOL + mChat.getProfileThumbnailAddress());
    }
    //  }
     /* if (mChat.getmember.getIsOnline())
      {
        subtitle.setText(getString(R.string.online));
      } else
      {
        if (member.getLastOnline().contains("T"))
        {
          subtitle.setText(String.format(getString(R.string.last_seen), TimeHelper.makeLastSeen(getContext(), new Date(TimeHelper
            .utcToTimezone(getContext(), member.getLastOnline())))));
        } else
        {
          subtitle.setText(member.getLastOnline());
        }
      }*/


    messageText.requestFocus();
    chatBottomContainerRemovedFromGroup.setVisibility(View.GONE);
    chatBottomContainer.setVisibility(View.VISIBLE);
  }

  private String getTimeSettingMessage(String comingDate) {
    Calendar calendar = Calendar.getInstance();
    if (comingDate.contains("T"))
    {
      calendar.setTimeInMillis(TimeHelper.utcToTimezone(getContext(), comingDate));
    } else
    {
      calendar.setTimeInMillis(Long.valueOf(comingDate));
    }

    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

    long diff = Math.abs(calendar.getTimeInMillis() - lastDateCalendar.getTimeInMillis());

    if (diff + 1000 > TimeHelper.DAY_MILLIS)
    {
      lastDateCalendar.setTimeInMillis(calendar.getTimeInMillis());
      return TimeHelper.getChatSettingsTimeAgo(getActivity().getApplicationContext(), calendar.getTime());
    }

    return null;
  }

  public void setIsTyping(boolean isTyping) {
    if (this.isTyping != isTyping)
    {
      this.isTyping = isTyping;
      if (isTyping)
      {
        RealtimeService.invokeUpdateUserStatus(EnumProfileStatus.IsTyping, mChat, false);
      } else
      {
        RealtimeService.invokeUpdateUserStatus(EnumProfileStatus.None, mChat, false);
      }
    }
  }

  private void openProfileActivity() {
    Profile profile = new Profile();
    profile.setUsername(mChat.getTitle());
    profile.setId(mChat.getReceiverId());
    profile.setImageAddress(mChat.getProfileThumbnailAddress());
    FragmentHandler.replaceFragment(getContext(), fragmentType.PROFILE, profile);

  }


  private void unBlock(final String memberId) {

    HttpClient.get(String.format(Constants.Server.Profile.GET_UNBLOCK, memberId), new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        super.onSuccess(statusCode, headers, responseBody);
        isBlocked = false;
        chatBottomContainerRemovedFromGroup.setVisibility(View.GONE);
        chatBottomContainer.setVisibility(View.VISIBLE);
        String blockedList = Logged.Models.getUserProfile().getBlockList() != null ? Logged.Models.getUserProfile().getBlockList() : "";
        blockedList = blockedList.replace(memberId + ",", "");
        Logged.Models.getUserProfile().setBlockList(blockedList);
        messageText.requestFocus();
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);
      }
    });

  }


  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    checkIfOrientationChanged(newConfig);
  }

  private void checkIfOrientationChanged(Configuration configuration) {
    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
    {
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
    chatId = null;

  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    mMenu = menu;

    inflater.inflate(R.menu.menu_chat, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    if (isBlocked)
    {
      menu.findItem(R.id.action_block).setTitle(R.string.unblock);
    } else
    {
      menu.findItem(R.id.action_block).setTitle(R.string.action_block);
    }

    if (AppUtils.isChatMuted(mChat.getChatId()))
    {
      menu.findItem(R.id.action_mute_notifications).setTitle(R.string.unmute_notifications);
    } else
    {
      menu.findItem(R.id.action_mute_notifications).setTitle(R.string.action_mute_notifications);
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case R.id.action_mute_notifications:
        if (AppUtils.isChatMuted(mChat.getChatId()))
        {
          AppUtils.unMuteChat(mChat.getChatId());
          toolbar.getMenu().findItem(R.id.action_mute_notifications).setTitle(R.string.action_mute_notifications);
        } else
        {
          AppUtils.muteChat(mChat.getChatId());
          toolbar.getMenu().findItem(R.id.action_mute_notifications).setTitle(R.string.unmute_notifications);
        }
        break;
      case R.id.action_delete:
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
        builder.message(R.string.sure_to_delete_chat)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .title(R.string.action_delete_chat)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.delete)
          .negativeAction(R.string.cancel)
          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        builder.setCancelable(true);
        builder.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            builder.dismiss();
          }
        });
        builder.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            RealtimeService.invokeLeaveRoom(Logged.Models.getUserProfile().getId(), mChat.getChatId(), new Action<Void>() {
              @Override
              public void run(Void aVoid) throws Exception {
                getActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

                    mRealm.executeTransaction(new Realm.Transaction() {
                      @Override
                      public void execute(
                        Realm realm) {
                        RealmResults<RCompactMessage> messages = mRealm.where(RCompactMessage.class).equalTo("ChatId", mChat.getChatId()).findAll();
                        for (int i = 0; i < messages.size(); i++)
                        {
                          RCompactMessage rMessage = messages.get(i);


                          MyApplication.getInstance().cancelUploadHandler(rMessage.getMessageId());
                          rMessage.removeFromRealm();
                        }

                        RChat chat = mRealm.where(RChat.class).equalTo("ChatId", mChat.getChatId()).findFirst();
                        if (chat != null)
                        {
                          chat.removeFromRealm();
                        }
                      }
                    });

                    FragmentHandler.onBackPressed(getContext());
                    mRealm.close();
                  }
                });

              }
            }, new ErrorCallback() {
              @Override
              public void onError(Throwable throwable) {

              }
            });

            builder.dismiss();
          }
        });

        builder.show();
        break;
      case R.id.action_block:
        if (!isBlocked)
        {

          final com.rey.material.app.SimpleDialog builder1 = new com.rey.material.app.SimpleDialog(getContext());
          builder1.message(R.string.sure_to_block_contact)
            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
            .title(R.string.block_contact_title)
            .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
            .positiveAction(R.string.action_block)
            .negativeAction(R.string.cancel)
            .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
          builder1.setCancelable(true);
          builder1.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              builder1.dismiss();
            }
          });
          builder1.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              block(mChat.getChatId());
              builder1.dismiss();
            }
          });
          builder1.show();
        } else
        {
          final com.rey.material.app.SimpleDialog builder2 = new com.rey.material.app.SimpleDialog(getContext());
          builder2.message(R.string.sure_to_unblock_contact)
            .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
            .title(R.string.unblock_contact_title)
            .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
            .positiveAction(R.string.unblock)
            .negativeAction(R.string.cancel)
            .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
          builder2.setCancelable(true);
          builder2.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              builder2.dismiss();
            }
          });
          builder2.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              unBlock(mChat.getChatId());
              builder2.dismiss();


            }
          });
          builder2.show();
        }

        break;

    }
    return false;
  }

  private void changeViewBlock(final String memberId) {
    chatBottomContainer.setVisibility(View.GONE);
    chatBottomContainerRemovedFromGroup.setVisibility(View.VISIBLE);
    removedFromGroupText.setText(R.string.unblock);
    String blockedList = Logged.Models.getUserProfile().getBlockList() != null ? Logged.Models.getUserProfile().getBlockList() : "";
    blockedList += memberId + ",";
    Logged.Models.getUserProfile().setBlockList(blockedList);
    hideEmojiPopup();
    hideStickerPopup();

    Utils.hideSoftKeyboard(getContext(), messageText);


    chatBottomContainerRemovedFromGroup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
        builder.message(R.string.sure_to_unblock_contact)
          .messageTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text))
          .title(R.string.unblock_contact_title)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.unblock)
          .negativeAction(R.string.cancel)
          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        builder.setCancelable(true);
        builder.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            builder.dismiss();

          }
        });
        builder.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            unBlock(memberId);
            builder.dismiss();


          }
        });

        builder.show();
      }
    });
  }

  private void block(final String memberId) {
    HttpClient.get(String.format(Constants.Server.Profile.GET_BLOCK, memberId), new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        super.onSuccess(statusCode, headers, responseBody);
        changeViewBlock(memberId);
        isBlocked = true;

      }

      @Override
      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);
      }
    });

  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK)
    {
      if (isEmojiPopupShowing())
      {
        hideEmojiPopup();
        return true;
      } else if (isStickerPopupShowing())
      {
        hideStickerPopup();
        return false;

      } else
      {
        getActivity().onBackPressed();

        return true;
      }
    }
    // If the event is a key-down event on the "enter" button

    else if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
    {
      // Perform action on key press

      if (v == messageText)
      {
        sendMessage();
      }

      return true;
    } else
    {
      return false;
    }
  }

  @Override
  public void onHubResponse(HashMap<String, String> response) {
    Log.i("sepehr", "onHubResponse: " + response);
    Utils.playMessageSound(getContext(), false);
    if (listAdapter != null && listAdapter.getMessages() != null)
    {
      List<RCompactMessage> rCompactMessages = listAdapter.getMessages();
      for (RCompactMessage message : rCompactMessages)
      {
        if (message != null && message.getId() != null && message.getId().equals(response.get("id").toString()))
        {
          message.setMessageStatus(EnumMessageStatus.Sent);
          message.setMessageId(response.get("message_id").toString());

        }
      }
      notifyAdapter();
    }

  }

  @Override
  public void onMessageReceived(CompactMessage message) {
    if (message.getSenderId().equals(Logged.Models.getUserProfile().getId()))
    {
      return;
    }
    Utils.playMessageSound(getContext(), true);
    final RCompactMessage rMessage = RToNonR.compactMessageToRCompactMessage(message);
    Utils.playMessageSound(getContext(), true);
    RealtimeService.invokeMessageSeen(message.getMessageId());
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    final RealmResults<RCompactMessage> rCompactMessages = mRealm.where(RCompactMessage.class).equalTo("ChatId", mChat.getChatId()).findAll();
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {

        for (int i = rCompactMessages.size() - 1; i >= 0; i--)
        {
          if (rCompactMessages.get(i).getMessageStatus() != EnumMessageStatus.Seen)
          {
            rCompactMessages.get(i).setMessageStatus(EnumMessageStatus.Seen);
          }
        }

      }
    });
    listAdapter.insertItem(rMessage, true);
    moveChatListToEnd();
  }

  @Override
  public void onMessageSent(final String messageId) {
    Utils.playMessageSound(getContext(), false);
    for (final RCompactMessage rCompactMessage : listAdapter.getMessages())
    {
      if (rCompactMessage != null && rCompactMessage.getMessageId().equals(messageId))
      {
        if (rCompactMessage.getMessageStatus() != EnumMessageStatus.Seen)
        {
          Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              rCompactMessage.setMessageStatus(EnumMessageStatus.Sent);
            }
          });
          mRealm.close();

        }
        break;
      }
    }

    notifyAdapter();
  }

  private void notifyAdapter() {
    if (listAdapter != null)
    {
      listAdapter.notifyDataSetChanged();
    }
  }


  @Override
  public void onMessageSeen(final String messageId) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    final List<RCompactMessage> messages = listAdapter.getMessages();

    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        for (RCompactMessage rCompactMessage : messages)
        {
          if (rCompactMessage != null && rCompactMessage.getMessageId() != null)// && rCompactMessage.getMessageId().equals(messageId))
          {
            rCompactMessage.setMessageStatus(EnumMessageStatus.Seen);
          }
        }
      }
    });
    mRealm.close();
    notifyAdapter();

  }

  @Override
  public void onUpdateMessagesStatus(ArrayList<CompactMessage> compactMessages) {
    for (CompactMessage compMessage : compactMessages)
    {
      List<RCompactMessage> rCompactMessages = listAdapter.getMessages();
      for (RCompactMessage message : rCompactMessages)
      {
        if (message != null && message.getMessageId().equals(compMessage.getMessageId()))
        {
          int pos = compactMessages.indexOf(rCompactMessages);
          listAdapter.updateMessageStatus(pos, RToNonR.compactMessageToRCompactMessage(compMessage));
        }
      }
    }

    notifyAdapter();
  }

  @Override
  public void onInitMessages(ArrayList<CompactMessage> messages) {
    final List<RCompactMessage> rMessage = new ArrayList<>();
    String ids = "";

    for (CompactMessage message : messages)
    {
      rMessage.add(RToNonR.compactMessageToRCompactMessage(message));
      ids += message.getMessageId() + ",";
    }

    RealtimeService.invokeMessageSeen(Logged.Models.getUserProfile()
      .getId() + "," + ids);

    // Don't add messages which already exist in the adapter
    List<RCompactMessage> adapterMessages = listAdapter.getMessages();
    for (RCompactMessage message : rMessage)
    {
      boolean exists = false;
      for (RCompactMessage messageAdapter : adapterMessages)
      {
        if (messageAdapter.getMessageId().equals(message.getMessageId()))
        {
          exists = true;
          break;
        }

      }
      if (!exists)
      {
        Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
        listAdapter.insertItem(message, false);
        mRealm.close();
      }
    }

    notifyAdapter();
    moveChatListToEnd();
  }

  @Override
  public void onUserStatusChanged(String profileId, String chatId, final int status) {
    if (profileId.equals(Logged.Models.getUserProfile().getId()))
    {
      return;
    }
    if (chatId.equals(mChat.getChatId()))
    {
      Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
      final RProfile profile = mRealm.where(RProfile.class).equalTo("Id", profileId).findFirst();
      if (status == EnumProfileStatus.IsTyping)
      {
        //Date date = new Date(TimeHelper.utcToTimezone(getContext(), status + ""));
        subtitle.setText(getString(R.string.is_typing));

      } else
      {

        if (!Utils.isNullOrEmpty(chatId))
        {
          if (mChat.getChatId().equals(chatId))
          {
            subtitle.setText(getString(R.string.online));
          }
        } else
        {
          subtitle.setText(profile.getLastOnline());
        }
      }
    }
  }

  @Override
  public void onSizeChanged(int height) {

    Rect localRect = new Rect();
    getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);

    WindowManager wm = (WindowManager) MyApplication.getInstance()
      .getSystemService(Activity.WINDOW_SERVICE);
    if (wm == null || wm.getDefaultDisplay() == null)
    {
      return;
    }

    if (height > AndroidUtilities.dp(50) && keyboardVisible)
    {
      keyboardHeight = height;
      MyApplication.getInstance()
        .getSharedPreferences("emoji", 0)
        .edit()
        .putInt("kbd_height", keyboardHeight)
        .commit();
    }

    if (showingEmoji || showingSticker)
    {
      int newHeight;

      newHeight = keyboardHeight;

      if (windowLayoutParams.width != AndroidUtilities.displaySize.x || windowLayoutParams.height != newHeight)
      {
        windowLayoutParams.width = AndroidUtilities.displaySize.x;
        windowLayoutParams.height = newHeight;

        wm.updateViewLayout(emojiView, windowLayoutParams);
        if (!keyboardVisible)
        {
          sizeNotifierRelativeLayout.post(new Runnable() {
            @Override
            public void run() {
              if (sizeNotifierRelativeLayout != null)
              {
                sizeNotifierRelativeLayout.setPadding(0, 0, 0, windowLayoutParams.height);
                sizeNotifierRelativeLayout.requestLayout();
              }
            }
          });
        }
      }
    }


    boolean oldValue = keyboardVisible;
    keyboardVisible = height > 0;
    if (keyboardVisible && sizeNotifierRelativeLayout.getPaddingBottom() > 0)
    {
      showEmojiPopup(false);
      showStickerPopup(false);
    } else if (!keyboardVisible && keyboardVisible != oldValue && (showingEmoji | showingSticker))
    {
      showEmojiPopup(false);
      showStickerPopup(false);

    }
  }

  @Override
  public void onMessageLongClick(final RCompactMessage message, final int itemPosition) {
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    final BottomSheetDialog mDialog = new BottomSheetDialog(getContext());
    mDialog.contentView(R.layout.bottom_sheet_chat)
      .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
      .inDuration(300)
      .cancelable(true)
      .show();

    hideEmojiPopup();
    hideStickerPopup();
    ImageButton forward = (ImageButton) mDialog.findViewById(R.id.btnForward);
    ImageButton copy = (ImageButton) mDialog.findViewById(R.id.btnCopy);
    LinearLayout copyLayout = (LinearLayout) mDialog.findViewById(R.id.copyLayout);
    ImageButton delete = (ImageButton) mDialog.findViewById(R.id.btnDelete);
    ImageButton retry = (ImageButton) mDialog.findViewById(R.id.btnRetry);
    if (message.getContentType() == EnumMessageContentType.Image
      || message.getContentType()==EnumMessageContentType.Audio
      || message.getContentType()==EnumMessageContentType.File
      || message.getContentType()==EnumMessageContentType.Location
      || message.getContentType()==EnumMessageContentType.Sticker
      || message.getContentType()==EnumMessageContentType.Video)
    {
      copyLayout.setVisibility(View.GONE);
    }

    retry.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog.dismiss();
        if (ChatAdapter.isSticker(message))
        {
          RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), false);
        } else if (ChatAdapter.isText(message))
        {
          RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), false);
        } else if (ChatAdapter.isImage(message))
        {
          UploadAsyncImageForRetry asynk = new UploadAsyncImageForRetry();
          asynk.execute(message);
        } else if (ChatAdapter.isVideo(message))
        {
          UploadAsyncVideoForRetry asynk = new UploadAsyncVideoForRetry();
          asynk.execute(RToNonR.rCompactMessageToCompactMessage(message));
        } else if (ChatAdapter.isFile(message))
        {
          UploadAsyncFileForRetry asynk = new UploadAsyncFileForRetry();
          asynk.execute(RToNonR.rCompactMessageToCompactMessage(message));
        } else if (ChatAdapter.isSharedItem(message))
        {
          RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), false);

        } else if (ChatAdapter.isLocation(message))
        {
          RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), false);
        }
      }
    });

    forward.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog.dismiss();
        Intent intent = new Intent(getContext(), FragmentShare.class);
        intent.putExtra("message", RToNonR.rCompactMessageToCompactMessage(message));
        FragmentHandler.replaceFragment(getContext(), fragmentType.SHARE, intent);

      }/**/
    });

    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog.dismiss();
        listAdapter.completelyRemoveItem(message);
        Utils.displayToast(getContext(), getString(R.string.toast_message_deleted), Gravity.CENTER, Toast.LENGTH_SHORT);
      }
    });

    copy.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog.dismiss();
        Utils.saveStringToClipboard(getContext(), message.getText());
        Utils.displayToast(getContext(), getString(R.string.toast_message_copied), Gravity.CENTER, Toast.LENGTH_SHORT);
      }
    });

    if (!ChatAdapter.isText(message))
    {
      copy.setEnabled(false);
    }
    mRealm.close();
  }

  @Override
  public void onCancelClick(RCompactMessage message, int itemPosition) {
    listAdapter.completelyRemoveItem(message);
  }


  private class UploadAsyncVideo extends AsyncTask<String, Void, Integer> {
    @Override
    protected Integer doInBackground(String... params) {
      final String imageAddress = params[0];
      final String videoAddress = params[1];

      long videoBytes = new File(videoAddress).length();
      long imageBytes = new File(imageAddress).length();
      //20MB
      if ((videoBytes > Constants.General.CHAT_FILE_MAX_SIZE) || (imageBytes > Constants.General.CHAT_FILE_MAX_SIZE))
      {
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getContext(), getString(R.string.file_size_should_be_smaller), Toast.LENGTH_LONG).show();
          }
        });
      } else
      {
        final boolean[] hasImageUploaded = {false};
        final boolean[] hasVideoUploaded = {false};
        final RCompactMessage message = new RCompactMessage();
        message.setSenderId(Logged.Models.getUserProfile().getId());
        message.setThumbnailAddress(imageAddress);
        message.setContentAddress(videoAddress);
        message.setFilePath(Utils.formatLocalPathsProperty(imageAddress, videoAddress, ""));
        message.setId(UUID.randomUUID().toString());
        int[] imageDimensions = Images.getImageDimensions(imageAddress);
        message.setContentSize(imageDimensions[0] + "," + imageDimensions[1]);
        message.setChatId(mChat.getChatId());
        message.setContentType(EnumMessageContentType.Video);
        message.setSendDateTime(Long.toString(System.currentTimeMillis()));


        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

            mRealm.executeTransaction(new Realm.Transaction() {
              @Override
              public void execute(Realm realm) {
                mRealm.copyToRealmOrUpdate(message);
              }
            });

            listAdapter.insertItem(message, true);

            moveChatListToEnd();

            new Upload(getContext(), new File(imageAddress), UUID.randomUUID().toString(),
              "image/jpeg").uploadImage(new IUploadCallback() {
              @Override
              public void onFileReceived(String fileName, String uploadedPath) {
                hasImageUploaded[0] = true;
                final String path = Constants.General.BLOB_PROTOCOL + uploadedPath;
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    message.setThumbnailAddress(path);
                    mRealm.copyToRealmOrUpdate(message);
                  }
                });

                if (hasVideoUploaded[0])
                {
                  RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), false);
                }
              }

              @Override
              public void onFailed(int statusCode) {
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {

                  }
                });

                listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());
              }

              @Override
              public void onProgress(long bytesWritten, long totalSize) {

              }
            });

            new Upload(getContext(), new File(videoAddress), message.getMessageId(),
              "video/*").uploadVideo(new IUploadCallback() {
              @Override
              public void onFileReceived(String fileName, String uploadedPath) {
                hasVideoUploaded[0] = true;
                final String path = Constants.General.BLOB_PROTOCOL + uploadedPath;
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    message.setContentAddress(path);
                    mRealm.copyToRealmOrUpdate(message);
                  }
                });
                if (hasImageUploaded[0])
                {
                  RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), false);
                  MyApplication.mUploadingFiles.remove(message.getMessageId());
                  listAdapter.notifyDataSetChanged();
                }
              }

              @Override
              public void onFailed(int statusCode) {
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {
                  }
                });
                MyApplication.mUploadingFiles.remove(message.getMessageId());
                listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());

              }

              @Override
              public void onProgress(long bytesWritten, long totalSize) {
                if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
                {
                  float progress = (bytesWritten * 1.0F / totalSize) * 100.0F;
                  listAdapter.getListener().get(message.getMessageId()).onProgressListener(progress);
                  MyApplication.mUploadingFiles.put(message.getMessageId(), progress);

                }
              }
            });
          }
        });
        return 1;
      }


      return 0;
    }

    @Override
    protected void onPostExecute(Integer somethingChanged) {
      super.onPostExecute(somethingChanged);
      if (somethingChanged == 1)
      {
        if (listAdapter != null)
        {
          listAdapter.notifyDataSetChanged();
        }
        moveChatListToEnd();
      }

    }


  }

  private class UploadAsyncFile extends AsyncTask<String, Void, Integer> {
    @Override
    protected void onPostExecute(Integer somethingChanged) {
      super.onPostExecute(somethingChanged);
      if (somethingChanged == 1)
      {
        if (listAdapter != null)
        {
          listAdapter.notifyDataSetChanged();
        }
        moveChatListToEnd();
      }

    }

    @Override
    protected Integer doInBackground(String... params) {
      final String pdfAddress = params[0];

      long pdfBytes = new File(pdfAddress).length();
      //20MB
      if (pdfBytes > Constants.General.CHAT_FILE_MAX_SIZE)
      {
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getContext(), getString(R.string.file_size_should_be_smaller), Toast.LENGTH_LONG).show();
          }
        });
      } else
      {
        final RCompactMessage message = new RCompactMessage();
        message.setSenderId(Logged.Models.getUserProfile().getId());
        message.setFilePath(Utils.formatLocalPathsProperty("", "", pdfAddress));
        message.setContentAddress(pdfAddress);
        message.setId(UUID.randomUUID().toString());
        message.setChatId(mChat.getChatId());
        message.setContentSize(AppUtils.exportFileName(pdfAddress));
        message.setSendDateTime(Long.toString(System.currentTimeMillis()));
        message.setContentType(EnumMessageContentType.File);


        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

            mRealm.executeTransaction(new Realm.Transaction() {
              @Override
              public void execute(Realm realm) {
                mRealm.copyToRealmOrUpdate(message);
              }
            });

            listAdapter.insertItem(message, true);

            moveChatListToEnd();

            new Upload(getContext(), new File(pdfAddress), message.getMessageId(),
              "application/pdf").uploadPdf(new IUploadCallback() {
              @Override
              public void onFileReceived(String fileName, String uploadedPath) {
                final String path = Constants.General.BLOB_PROTOCOL + uploadedPath;
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {
                    message.setContentAddress(path);
                    mRealm.copyToRealmOrUpdate(message);
                  }
                });
                RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), false);
                MyApplication.mUploadingFiles.remove(message.getMessageId());
                listAdapter.notifyDataSetChanged();
              }

              @Override
              public void onFailed(int statusCode) {
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {
                  }
                });
                MyApplication.mUploadingFiles.remove(message.getMessageId());
                listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());

              }

              @Override
              public void onProgress(long bytesWritten, long totalSize) {
                if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
                {
                  float progress = (bytesWritten * 1.0F / totalSize) * 100.0F;
                  listAdapter.getListener().get(message.getMessageId()).onProgressListener(progress);
                  MyApplication.mUploadingFiles.put(message.getMessageId(), progress);
                                   /* if (progress == 100.0F) {
                                        listAdapter.notifyDataSetChanged();

                                    }*/
                }
              }
            });
          }
        });
        return 1;
      }

      return 0;
    }
  }

  private class UploadAsyncImage extends AsyncTask<String, Void, Integer> {

    @Override
    protected void onPostExecute(Integer somethingChanged) {
      super.onPostExecute(somethingChanged);
      if (somethingChanged == 1)
      {
        if (listAdapter != null)
        {
          listAdapter.notifyDataSetChanged();
        }
        moveChatListToEnd();
      }
    }

    @Override
    protected Integer doInBackground(String... params) {
      final String imageAddress = params[0];


      long imageBytes = new File(imageAddress).length();
      //20MB
      if (imageBytes > Constants.General.CHAT_FILE_MAX_SIZE)
      {
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getContext(), getString(R.string.file_size_should_be_smaller), Toast.LENGTH_LONG).show();
          }
        });
      } else
      {

        try
        {
          final String newPath = Images.compressJpeg(getContext(), imageAddress, Constants.General.APP_FOLDER_IMAGE_PATH, false);
          final RCompactMessage message = new RCompactMessage();
          message.setSenderId(Logged.Models.getUserProfile().getId());
          message.setFilePath(Utils.formatLocalPathsProperty(newPath, "", ""));
          message.setThumbnailAddress(imageAddress);
          message.setContentAddress(imageAddress);
          message.setMessageId("");
          message.setId(UUID.randomUUID().toString());
          int[] imageDimensions = Images.getImageDimensions(newPath);
          message.setContentSize(imageDimensions[0] + "," + imageDimensions[1]);
          message.setChatId(mChat.getChatId());
          message.setSendDateTime(Long.toString(System.currentTimeMillis()));
          message.setContentType(EnumMessageContentType.Image);
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                  mRealm.copyToRealmOrUpdate(message);
                }
              });

              listAdapter.insertItem(message, true);

              moveChatListToEnd();

              new Upload(getContext(), new File(newPath), message.getMessageId(),
                "image/jpeg").uploadImage(new IUploadCallback() {
                @Override
                public void onFileReceived(final String fileName, final String uploadedPath) {
                  mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                      message.setContentAddress(Constants.General.BLOB_PROTOCOL + uploadedPath);
                      message.setThumbnailAddress(Constants.General.BLOB_PROTOCOL + uploadedPath);
                      mRealm.copyToRealmOrUpdate(message);
                    }
                  });
                  RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), false);
                  MyApplication.mUploadingFiles.remove(message.getMessageId());
                  if (listAdapter != null)
                  {
                    listAdapter.notifyDataSetChanged();
                  }
                  moveChatListToEnd();
                }

                @Override
                public void onFailed(int statusCode) {
                  mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                    }
                  });
                  MyApplication.mUploadingFiles.remove(message.getMessageId());
                  listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());

                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                  if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
                  {
                    float progress = (bytesWritten * 1.0F / totalSize) * 100.0F;
                    listAdapter.getListener().get(message.getMessageId()).onProgressListener(progress);
                    MyApplication.mUploadingFiles.put(message.getMessageId(), progress);
                                       /* if (progress == 100.0F) {
                                            listAdapter.notifyDataSetChanged();
                                        }*/
                  }
                }
              });
            }
          });
        } catch (IOException e)
        {
        }
        return 1;
      }

      return 0;
    }
  }

  private class UploadAsyncVideoForRetry extends AsyncTask<CompactMessage, Void, Void> {
    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (listAdapter != null)
      {
        listAdapter.notifyDataSetChanged();
      }
    }

    @Override
    protected Void doInBackground(CompactMessage... params) {
      final CompactMessage message = params[0];
      final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
      RCompactMessage rMessage = mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst();

      final boolean[] hasImageUploaded = {false};
      final boolean[] hasVideoUploaded = {false};

      final String imagePath = rMessage.getFilePath().split(",")[0];
      final String videoPath = rMessage.getFilePath().split(",")[1];

      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {

          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            }
          });
          MyApplication.mUploadingFiles.put(message.getMessageId(), 0.0F);
          listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());

          new Upload(getContext(), new File(imagePath), UUID.randomUUID().toString(),
            "image/jpeg").uploadImage(new IUploadCallback() {
            @Override
            public void onFileReceived(String fileName, String uploadedPath) {
              hasImageUploaded[0] = true;
              final String path = Constants.General.BLOB_PROTOCOL + uploadedPath;
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                  message.setThumbnailAddress(path);
                  mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));
                }
              });

              if (hasVideoUploaded[0])
              {
                RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst()), false);
                MyApplication.mUploadingFiles.remove(message.getMessageId());
                if (listAdapter != null)
                {
                  listAdapter.notifyDataSetChanged();
                }
              }
            }

            @Override
            public void onFailed(int statusCode) {
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                }
              });
              listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {

            }
          });

          new Upload(getContext(), new File(videoPath), message.getMessageId(),
            "video/*").uploadVideo(new IUploadCallback() {
            @Override
            public void onFileReceived(String fileName, String uploadedPath) {
              hasVideoUploaded[0] = true;
              final String path = Constants.General.BLOB_PROTOCOL + uploadedPath;
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                  message.setContentAddress(path);
                  mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));
                }
              });
              if (hasImageUploaded[0])
              {
                RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst()), false);
                MyApplication.mUploadingFiles.remove(message.getMessageId());
                if (listAdapter != null)
                {
                  listAdapter.notifyDataSetChanged();
                }
              }
            }

            @Override
            public void onFailed(int statusCode) {
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                }
              });
              MyApplication.mUploadingFiles.remove(message.getMessageId());
              listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
              if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
              {
                float progress = (bytesWritten * 1.0F / totalSize) * 100.0F;
                MyApplication.mUploadingFiles.put(message.getMessageId(), progress);
                listAdapter.notifyDataSetChanged();
              }
            }
          });
          mRealm.close();
        }
      });

      return null;
    }
  }

  private class UploadAsyncFileForRetry extends AsyncTask<CompactMessage, Void, Void> {
    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (listAdapter != null)
      {
        listAdapter.notifyDataSetChanged();
      }
    }

    @Override
    protected Void doInBackground(CompactMessage... params) {
      final CompactMessage message = params[0];
      final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
      RCompactMessage rMessage = mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst();

      final String fileAddress = rMessage.getFilePath().split(",")[2];

      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {

          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            }
          });
          MyApplication.mUploadingFiles.put(message.getMessageId(), 0.0F);
          listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());

          new Upload(getContext(), new File(fileAddress), message.getMessageId(),
            "application/pdf").uploadPdf(new IUploadCallback() {
            @Override
            public void onFileReceived(String fileName, String uploadedPath) {
              final String path = Constants.General.BLOB_PROTOCOL + uploadedPath;
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                  message.setContentAddress(path);
                  mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(message));
                }
              });
              RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst()), false);
              MyApplication.mUploadingFiles.remove(message.getMessageId());
              if (listAdapter != null)
              {
                listAdapter.notifyDataSetChanged();
              }
            }

            @Override
            public void onFailed(int statusCode) {
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                }
              });
              MyApplication.mUploadingFiles.remove(message.getMessageId());
              listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
              if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
              {
                float progress = (bytesWritten * 1.0F / totalSize) * 100.0F;
                MyApplication.mUploadingFiles.put(message.getMessageId(), progress);
                listAdapter.notifyDataSetChanged();
              }
            }
          });
          mRealm.close();
        }
      });
      return null;
    }
  }

  private class UploadAsyncImageForRetry extends AsyncTask<RCompactMessage, Void, Void> {
    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (listAdapter != null)
      {
        listAdapter.notifyDataSetChanged();
      }
    }

    @Override
    protected Void doInBackground(RCompactMessage... params) {
      final RCompactMessage message = params[0];
      final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
      RCompactMessage rMessage = mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst();

      final String imagePath = rMessage.getFilePath().split(",")[0];

      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            }
          });
          MyApplication.mUploadingFiles.put(message.getMessageId(), 0.0F);
          listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("Id", message.getMessageId()).findFirst());

          new Upload(getContext(), new File(imagePath), message.getMessageId(),
            "image/jpeg").uploadImage(new IUploadCallback() {
            @Override
            public void onFileReceived(final String fileName, final String uploadedPath) {
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                  message.setThumbnailAddress(Constants.General.BLOB_PROTOCOL + uploadedPath);
                  mRealm.copyToRealmOrUpdate(message);
                }
              });
              RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst()), false);
              MyApplication.mUploadingFiles.remove(message.getMessageId());
              if (listAdapter != null)
              {
                listAdapter.notifyDataSetChanged();
              }
            }

            @Override
            public void onFailed(int statusCode) {
              mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                }
              });
              MyApplication.mUploadingFiles.remove(message.getMessageId());
              listAdapter.updateStatus(mRealm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst());
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
              if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
              {
                float progress = (bytesWritten * 1.0F / totalSize) * 100.0F;
                listAdapter.getListener().get(message.getMessageId()).onProgressListener(progress);
                MyApplication.mUploadingFiles.put(message.getMessageId(), progress);
                               /* if (progress == 100.0F) {
                                    listAdapter.notifyDataSetChanged();
                                }*/
              }
            }
          });

          mRealm.close();
        }
      });
      return null;
    }
  }
}
