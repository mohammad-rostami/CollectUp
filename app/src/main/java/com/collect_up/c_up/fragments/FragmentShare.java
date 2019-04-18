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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.ConversationsAdapter;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.SortChats;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.services.RealtimeService;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.animators.SlideInLeftAnimator;
import com.marshalchen.ultimaterecyclerview.divideritemdecoration.HorizontalDividerItemDecoration;
import com.rey.material.app.SimpleDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

@SuppressLint ("ValidFragment")
public class FragmentShare extends BaseFragment implements ConversationsAdapter.OnItemClickListener {

  public static boolean isRunning;
  @Bind (R.id.recycler_view)
  UltimateRecyclerView recyclerView;
  private ConversationsAdapter mAdapter;
  private CompactChat mChat;
  private List<CompactChat> mChats = new ArrayList<>();
  private List<RCompactMessage> mMessages = new ArrayList<>();
  private CompactMessage comingMessage;
  private View view;
  Intent intent;

  public FragmentShare(Intent intent) {
    this.intent = intent;
  }

  public FragmentShare() {
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
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.share);

    super.onResume();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_share, container, false);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      ButterKnife.bind(this, view);
      Bundle bundle = intent.getExtras();
      if (bundle != null && bundle.getParcelable("message") != null)
      {
        comingMessage = bundle.getParcelable("message");
      } else if (intent == null)
      {

        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
        builder.message(R.string.no_extra_sent)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .setCancelable(false);
        builder.show();
        return view;
      }

      Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

      RealmResults<RChat> chats = mRealm.where(RChat.class).findAll();

      List<CompactChat> chats2 = new ArrayList<>();
      for (RChat chat : chats)

      {
        chats2.add(RToNonR.rChatToChat(chat));
      }

      sortChats(chats2);

      mChats.addAll(chats2);

      for (CompactChat chat : mChats)

      {
        RealmResults<RCompactMessage> messages = mRealm.where(RCompactMessage.class)
          .equalTo("ChatId", chat.getChatId())
          .findAll();
        RCompactMessage message = getLastMessage(messages);
        if (message != null)
        {
          mMessages.add(message);


        } else
        {
          RCompactMessage rMessage = new RCompactMessage();
          rMessage.setId(chat.getChatId());
          rMessage.setChatId(chat.getChatId());
          rMessage.setSendDateTime(Long.toString(Utils.localNow()));
          mMessages.add(rMessage);
        }
      }

      recyclerView.setHasFixedSize(false);
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      recyclerView.setLayoutManager(layoutManager);
      recyclerView.setItemAnimator(new

        SlideInLeftAnimator()

      );
      recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).

        color(getResources().getColor(R.color.chat_subtitle)).build()

      );

      mAdapter = new ConversationsAdapter(getContext(), mChats);

      mAdapter.setListener(this);

      recyclerView.setAdapter(mAdapter);


      mRealm.close();
      return view;
    } else
    {
      return view;
    }
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch (item.getItemId())
    {
      case android.R.id.home:
        break;
    }

    return true;
  }

  @Override
  public void onViewClick(final CompactChat chat) {
    mChat = chat;


    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
    builder.message(R.string.sure_to_share_message_to_other)
      .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
      .title(R.string.share_confirmation)
      .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .positiveAction(R.string.send)
      .negativeAction(R.string.cancel)
      .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .setCancelable(true);

    builder.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        builder.dismiss();

      }
    });
    builder.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PermissionListener dialogPermissionListener =
          CustomPermissionDialog.Builder
            .withContext(getContext())
            .withTitle(R.string.permission_title)
            .withMessage(R.string.permission_storage)
            .withButtonText(android.R.string.ok)
            .build();
        PermissionListener basePermission = new PermissionListener() {
          @Override
          public void onPermissionGranted(PermissionGrantedResponse response) {

            handleInputIntent(intent, chat);
            builder.dismiss();

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
    builder.show();


  }

  @Override
  public void onViewLongClick(final CompactChat chat) {
    //empty
  }

  void sendMessage(RCompactMessage message) {
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    comingMessage = RToNonR.rCompactMessageToCompactMessage(message);
    comingMessage.setChatId(mChat.getChatId());
    comingMessage.setSendDateTime(Long.toString(System.currentTimeMillis()));
    comingMessage.setSender(Logged.Models.getUserProfile());

    RealtimeService.invokeSendMessage(comingMessage, mChat.isGroup() ? true : false);
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(comingMessage));
      }
    });
    if (!mChat.isGroup())
    {
      FragmentHandler.replaceFragment(getContext(), fragmentType.CHAT, mChat, false);

    } else
    {
      FragmentHandler.replaceFragment(getContext(), fragmentType.GROUPCHAT, mChat, false);

    }
    mRealm.close();
  }

  void handleInputIntent(final Intent intent, final CompactChat chat) {

    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    String action = intent.getAction();
    String type = intent.getType();
    if (Intent.ACTION_SEND.equals(action) && type != null)
    {
      if (type.startsWith("image/"))
      {
        Uri streamUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        final String imageAddress = Images.getImageUrlWithAuthority(getContext(), streamUri);

        UploadAsyncImage asynk = new UploadAsyncImage();
        asynk.execute(imageAddress);
      } else if (type.startsWith("video/"))
      {
        Uri streamUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        String videoPath = Utils.getPath(getContext(), streamUri);

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(getContext(), streamUri);
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
      } else if (type.startsWith("text/"))
      {

        String intentText = intent.getStringExtra(Intent.EXTRA_TEXT);

        final RCompactMessage message = new RCompactMessage();
        message.setSenderId(Logged.Models.getUserProfile().getId());
        message.setText(intentText);
        message.setSendDateTime(Long.toString(System.currentTimeMillis()));


        mRealm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            mRealm.copyToRealmOrUpdate(message);
          }
        });

        sendMessage(message);

      }
    } else
    {
      comingMessage.setChatId(chat.getChatId());
      comingMessage.setSendDateTime(Long.toString(System.currentTimeMillis()));
      comingMessage.setSender(Logged.Models.getUserProfile());
      RealtimeService.invokeSendMessage(comingMessage, mChat.isGroup() ? true : false);
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          mRealm.copyToRealmOrUpdate(RToNonR.compactMessageToRCompactMessage(comingMessage));
        }
      });
      if (!chat.isGroup())
      {

        FragmentHandler.replaceFragment(getContext(), fragmentType.CHAT, chat, false);
      } else
      {
        FragmentHandler.replaceFragment(getContext(), fragmentType.GROUPCHAT, chat, false);
      }
      mRealm.close();
    }

  }


  private class UploadAsyncImage extends AsyncTask<String, Void, Integer> {
    private SimpleDialog dialog;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      dialog = Utils.createLoadingDialog(getContext());
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
          cancel(true);
        }
      });
    }

    @Override
    protected void onPostExecute(Integer somethingChanged) {
      super.onPostExecute(somethingChanged);
      if (somethingChanged == 1)
      {
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
          message.setSendDateTime(Logged.Models.getUserProfile().getId());
          message.setFilePath(Utils.formatLocalPathsProperty(newPath, "", ""));
          message.setThumbnailAddress(imageAddress);
          int[] imageDimensions = Images.getImageDimensions(newPath);
          message.setContentSize(imageDimensions[0] + "," + imageDimensions[1]);
          message.setContentType(EnumMessageContentType.Image);
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

              dialog.show();

              new Upload(getContext(), new File(newPath), message.getMessageId(),
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
                  MyApplication.mUploadingFiles.remove(message.getMessageId());

                  Images.deleteImage(getContext(), imageAddress);
                  sendMessage(message);
                  dialog.dismiss();

                }

                @Override
                public void onFailed(int statusCode) {
                  mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                    }
                  });
                  MyApplication.mUploadingFiles.remove(message.getMessageId());
                  dialog.dismiss();

                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                  if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
                  {
                    float progress = (bytesWritten * 1.0F / totalSize) * 100.0F;
                    MyApplication.mUploadingFiles.put(message.getMessageId(), progress);
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

  private class UploadAsyncVideo extends AsyncTask<String, Void, Integer> {

    private ProgressDialog dialog;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      dialog = new ProgressDialog(getContext());
      dialog.setMessage(getString(R.string.file_picker_progress_dialog_loading));
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
          cancel(true);
        }
      });
    }

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
        int[] imageDimensions = Images.getImageDimensions(imageAddress);
        message.setContentSize(imageDimensions[0] + "," + imageDimensions[1]);
        message.setText(getString(R.string.video_message));
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
                  RealtimeService.invokeSendMessage(RToNonR.rCompactMessageToCompactMessage(message), mChat.isGroup() ? true : false);
                }
              }

              @Override
              public void onFailed(int statusCode) {
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(Realm realm) {
                  }
                });
                dialog.dismiss();


              }

              @Override
              public void onProgress(long bytesWritten, long totalSize) {

              }
            });
            dialog.show();
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
                  MyApplication.mUploadingFiles.remove(message.getMessageId());

                  sendMessage(message);
                  dialog.dismiss();

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
                dialog.dismiss();

              }

              @Override
              public void onProgress(long bytesWritten, long totalSize) {
                if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
                {
                  float progress = (bytesWritten * 1.0F / totalSize) * 100.0F;
                  MyApplication.mUploadingFiles.put(message.getMessageId(), progress);
                }
              }
            });
            comingMessage = RToNonR.rCompactMessageToCompactMessage(message);
            mRealm.close();
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
      }

    }


  }

}

