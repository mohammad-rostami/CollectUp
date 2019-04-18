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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.GroupChatInfoMembersAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.ChoosePhoto;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.listeners.IUpdateGroupChatInfoUICallbacks;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactChatMember;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.model.realm.RProfile;
import com.collect_up.c_up.receivers.RealtimeReceiver;
import com.collect_up.c_up.services.RealtimeService;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.app.BottomSheetDialog;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.MediaContent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmResults;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;

@SuppressLint ("ValidFragment")

public class FragmentGroupChatInfo extends BaseFragment implements Comparator<CompactChatMember>, ChoosePhoto.OnDialogButtonClick, IUpdateGroupChatInfoUICallbacks, GroupChatInfoMembersAdapter.OnViewClick {

  public static boolean isRunning;
  public static String chatId;
  @Bind (R.id.image_view_picture)
  ComplexAvatarView mImageViewPicture;
  @Bind (R.id.text_view_title)
  TextView mTextViewTitle;
  @Bind (R.id.text_view_subtitle)
  TextView mTextViewSubtitle;
  @Bind (R.id.toolbar)
  Toolbar mToolbar;
  @Bind (R.id.recycler_view)
  UltimateRecyclerView mRecyclerView;
  private BottomSheetDialog mDialog;
  private CompactChat mChat;
  private GroupChatInfoMembersAdapter mAdapter;
  private RealtimeReceiver mUpdateChatUIReceiver;
  private View view;
  private int mCurrentPage;
  private List<CompactChatMember> profileList = new LinkedList<>();

  public FragmentGroupChatInfo(CompactChat mChat) {
    this.mChat = mChat;
  }

  public FragmentGroupChatInfo() {
  }

  @Override

  public void onStop() {
    super.onStop();
    isRunning = false;
    chatId = null;
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
    chatId = mChat.getChatId();
  }

  @OnClick (R.id.text_view_title)
  public void groupNameClick() {
    com.rey.material.app.SimpleDialog builder = initGroupNameEditDialog();
    builder.show();

  }

  private com.rey.material.app.SimpleDialog initGroupNameEditDialog() {
    View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_group_name, null, false);
    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
    builder.titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    builder.setCancelable(true);
    builder.title(R.string.choose_new_name);
    builder.positiveAction(R.string.save);
    builder.negativeAction(R.string.cancel);
    builder.actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

    builder.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        builder.dismiss();
      }
    });
    builder.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    builder.setContentView(view);


    final EditText editTextGroupName = (EditText) view.findViewById(R.id.edit_text_group_name);
    editTextGroupName.setText(mTextViewTitle.getText().toString());
    editTextGroupName.setSelection(0, editTextGroupName.getText().toString().length());

    builder.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String groupName = editTextGroupName.getText().toString().trim().replace(",", "");
        if (Utils.isNullOrEmpty(groupName) || mTextViewTitle.getText().toString().trim().equals(groupName))
        {
          builder.dismiss();
        } else
        {
          HttpClient.get(String.format(Constants.Server.Messaging.GET_SET_GROUP_TITlE, mChat.getChatId(), editTextGroupName.getText().toString()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              super.onSuccess(statusCode, headers, responseBody);
              builder.dismiss();
              mTextViewTitle.setText(editTextGroupName.getText().toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);
            }
          });

        }
      }
    });

    return builder;
  }


  @OnClick (R.id.image_view_picture)
  public void onPictureClick() {
    mDialog = new ChoosePhoto(getActivity(), FragmentGroupChatInfo.this, false).show();

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
    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    ((ActivityHome) getActivity()).changeButtonBackgroud(3);

    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    if (mUpdateChatUIReceiver == null)
    {
      mUpdateChatUIReceiver = new RealtimeReceiver(this);
    }
    IntentFilter intentFilter = new IntentFilter(Constants.General.UPDATE_CHAT_UI);
    getActivity().registerReceiver(mUpdateChatUIReceiver, intentFilter);

    mImageViewPicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(mChat.getTitle()).setImageUrl(Constants.General.BLOB_PROTOCOL + mChat.getProfileThumbnailAddress());

    mTextViewSubtitle.setText(mChat.getMembersCount() + " Members");//getResources().getQuantityString(R.plurals.members, mChat.getMembers().size(), mChat.getMembers().size()));

    if (mAdapter != null)
    {
      mAdapter.mChat = mChat;
    }
    loadFirstMemberPages();
    mRealm.close();
  }

  private void loadMoreMembers() {
    Pagination.getStickers(mCurrentPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (CompactChatMember member : (List<CompactChatMember>) pageList)
        {
          mAdapter.insertInternal(profileList, member, profileList.size());
        }

        mCurrentPage++;

        if (pageList.size() != Pagination.PAGE_IN_REQUEST)
        {
          if (mRecyclerView.isLoadMoreEnabled())
          {
            mRecyclerView.disableLoadmore();
          }
        } else
        {
          if (!mRecyclerView.isLoadMoreEnabled())
          {
            mRecyclerView.reenableLoadmore();
          }
        }
      }

      @Override
      public void onFailure() {
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadMoreMembers();
          }
        }, getActivity());
      }
    });
  }

  private void loadFirstMemberPages() {
    mCurrentPage = 1;
    Pagination.getGroupMembers(getContext(), mChat.getChatId(), mCurrentPage, new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        // Reset items to maeke the pull to refresh right
        profileList.clear();
        profileList.addAll((List<CompactChatMember>) pageList);

        Collections.sort(profileList, FragmentGroupChatInfo.this);
        // Add a null to check it in the adapter and add a "Add member" item
        profileList.add(null);

        mAdapter = new GroupChatInfoMembersAdapter(getContext(), profileList, mChat);
        mAdapter.setViewClickListener(FragmentGroupChatInfo.this);

        mRecyclerView.setAdapter(mAdapter);


        final List<String> memberIds = new ArrayList<>();

        for (CompactChatMember member : profileList)
        {
          if (member != null && !member.getUserId().equals(Logged.Models.getUserProfile().getId()))
          {
            memberIds.add(member.getUserId());
          }
        }
        RealtimeService.invokeGetUpdateProfileStatus(TextUtils.join(",", memberIds), new Action<String>() {
          @Override
          public void run(final String s) throws Exception {
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

                final String[] dates = s.split(",");
                for (String profileId : memberIds)
                {
                  final int pos = memberIds.indexOf(profileId);
                  final RProfile profile = mRealm.where(RProfile.class).equalTo("Id", profileId).findFirst();

                  if (dates[pos].contains("T"))
                  {
                    mRealm.executeTransaction(new Realm.Transaction() {
                      @Override
                      public void execute(Realm realm) {
                        profile.setLastOnline(dates[pos]);
                        profile.setIsOnline(false);
                        realm.copyToRealmOrUpdate(profile);
                      }
                    });
                  } else
                  {
                    mRealm.executeTransaction(new Realm.Transaction() {
                      @Override
                      public void execute(Realm realm) {
                        profile.setLastOnline(Long.toBinaryString(System.currentTimeMillis()));
                        profile.setIsOnline(true);
                        realm.copyToRealmOrUpdate(profile);
                      }
                    });
                  }
                }

                if (mAdapter != null)
                {
                  mAdapter.notifyDataSetChanged();
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

        mAdapter.notifyDataSetChanged();

        mCurrentPage++;

        if (pageList.size() != Pagination.PAGE_IN_REQUEST)
        {
          if (mRecyclerView.isLoadMoreEnabled())
          {
            mRecyclerView.disableLoadmore();
          }
        } else
        {
          if (!mRecyclerView.isLoadMoreEnabled())
          {
            mRecyclerView.enableLoadmore();
          }
        }
      }

      @Override
      public void onFailure() {
        mRecyclerView.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstMemberPages();
          }
        }, getActivity());
      }
    });
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

      view = inflater.inflate(R.layout.fragment_group_chat_info, container, false);

      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
      setHasOptionsMenu(true);
      mToolbar.inflateMenu(R.menu.menu_group_chat_info);
      onPrepareOptionsMenu(mToolbar.getMenu());
      mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          onOptionsItemSelected(item);
          return false;
        }
      });
      chatId = mChat.getChatId();
      initGroupNameEditDialog();
      mRecyclerView.setHasFixedSize(true);
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      mRecyclerView.setLayoutManager(layoutManager);
      mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMoreMembers();
        }
      });
      mTextViewTitle.setText(mChat.getTitle());
      loadFirstMemberPages();


      return view;
    } else
    {
      return view;
    }

  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_group_chat_info, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.action_add_member).setVisible(false);
    menu.findItem(R.id.action_edit).setVisible(false);
    menu.findItem(R.id.action_change_picture).setVisible(false);
    menu.findItem(R.id.action_delete_leave).setVisible(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));

    switch (item.getItemId())
    {
      case R.id.action_change_picture:
        onPictureClick();
        break;
      case R.id.action_add_member:
        Intent intent = new Intent(getContext(), FragmentChatContacts.class);

        intent.putExtra("join_room", true);
        intent.putExtra("chat", mChat);
        intent.putExtra("show_invites", false);
        intent.putExtra("title", getString(R.string.contacts));
        FragmentHandler.replaceFragment(getContext(), fragmentType.CHATCONTACTS, intent);

        break;
      case R.id.action_delete_leave:

        final com.rey.material.app.SimpleDialog sureBuilder = new com.rey.material.app.SimpleDialog(getContext());
        sureBuilder.message(R.string.sure_to_delete_and_leave_group)
          .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
          .title(R.string.delete_leave_title)
          .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .positiveAction(R.string.delete)
          .negativeAction(R.string.cancel)
          .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
          .setCancelable(true);

        sureBuilder.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            sureBuilder.dismiss();

          }
        });
        sureBuilder.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            HttpClient.get(String.format(Constants.Server.Messaging.GET_LEAVE_GROUP, mChat.getChatId()), new AsyncHttpResponser(getContext()) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                final Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
                mRealm.executeTransaction(new Realm.Transaction() {
                  @Override
                  public void execute(
                    Realm realm) {
                    RealmResults<RCompactMessage> messages = mRealm.where(RCompactMessage.class).equalTo("ChatId", mChat.getChatId()).findAll();
                    for (int i = 0; i < messages.size(); i++)
                    {
                      RCompactMessage rMessage = messages.get(i);

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

              @Override
              public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
              }
            });


            sureBuilder.dismiss();
          }
        });

        sureBuilder.show();
        break;
      case android.R.id.home:
        break;
      case R.id.action_edit:
        com.rey.material.app.SimpleDialog builder = initGroupNameEditDialog();
        builder.show();
        break;
    }
    mRealm.close();

    return true;
  }


  private void handleCrop(int resultCode, Intent result) {
    if (resultCode == getActivity().RESULT_OK)
    {
      MediaContent content = (MediaContent) result.getBundleExtra("data").getParcelableArrayList("data").get(0);

      try
      {
        replaceProfilePicture(content.getUri());
      } catch (IOException e)
      {
      }
    } else if (resultCode == UCrop.RESULT_ERROR)
    {
      Toast.makeText(getContext(), UCrop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void replaceProfilePicture(Uri uri) throws IOException {
    Bitmap bitmap = Images.getBitmapFromUri(getContext(), uri);
    Uri newUri = Images.getImageUriFromBitmap(getContext(), bitmap);
    String path = Utils.getPath(getContext(), newUri);

    new Upload(getContext(), new File(path), UUID.randomUUID().toString(),
      "image/jpeg").uploadImage(new IUploadCallback() {
      @Override
      public void onFileReceived(String fileName, final String uploadedPath) {
        HttpClient.get(String.format(Constants.Server.Messaging.GET_SET_GROUP_IMAGE, mChat.getChatId(), uploadedPath), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            super.onSuccess(statusCode, headers, responseBody);
            mImageViewPicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(mChat.getTitle()).setImageUrl(Constants.General.BLOB_PROTOCOL + uploadedPath);

          }

          @Override
          public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);
          }
        });

      }

      @Override
      public void onFailed(int statusCode) {

      }

      @Override
      public void onProgress(long bytesWritten, long totalSize) {

      }
    });

    mImageViewPicture.setTag(path);
    mImageViewPicture.makeAllDefaults();

    mImageViewPicture.setImageUri(uri);
  }

  private boolean amIIn() {
    return mChat.AmISuperAdmin();

  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data != null)
    {
      handleCrop(resultCode, data);
    }

  }

  @Override
  public void onDialogTakePhoto() {
    mDialog.dismiss();
  }

  @Override
  public void onDialogRemovePhoto() {
    HttpClient.get(String.format(Constants.Server.Messaging.GET_SET_GROUP_IMAGE, mChat.getChatId(), ""), new AsyncHttpResponser(getContext()) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        super.onSuccess(statusCode, headers, responseBody);
        mDialog.dismiss();
        mImageViewPicture.setDefaultImage(R.drawable.ic_camera_gray);
        mImageViewPicture.setTag("");
        mChat.setProfileThumbnailAddress(null);
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);
      }
    });

  }

  @Override
  public void onDialogFromGallery() {
    Utils.pickImage(this);
    mDialog.dismiss();
  }

  @Override
  public void onDialogFromVideo() {

  }

  @Override
  public void onViewClick(Profile profile) {
    FragmentHandler.replaceFragment(getContext(), fragmentType.PROFILE, profile);
  }

  @Override
  public void onViewLongClick(final Profile profile, final int pos) {

    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
    builder.message(R.string.sure_to_remove_member_from_group)
      .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
      .title(R.string.remove_from_group_title)
      .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
      .positiveAction(R.string.delete)
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

        HttpClient.get(String.format(Constants.Server.Messaging.GET_REMOVE_GROUP_MEMBER, mChat.getChatId(), profile.getId()), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            super.onSuccess(statusCode, headers, responseBody);
            builder.dismiss();
            mAdapter.notifyItemRemoved(pos);
            profileList.remove(pos);
            mAdapter.notifyItemRangeChanged(pos, profileList.size());

          }

          @Override
          public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);
          }
        });

      }
    });

    builder.show();
  }

  @Override
  public void onChatInfoUpdated(String chatId, String groupName, String imageAddress) {
    Realm mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
    mChat = RToNonR.rChatToChat(mRealm.where(RChat.class).equalTo("Id", chatId).findFirst());
    mTextViewTitle.setText(groupName);

    mChat = RToNonR.rChatToChat(mRealm.where(RChat.class).equalTo("Id", chatId).findFirst());

    if (Utils.isNullOrEmpty(imageAddress))
    {
      mImageViewPicture.setDefaultImage(R.drawable.ic_camera_gray);
    } else
    {
      mImageViewPicture.makeAllDefaults();

      mImageViewPicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(mChat.getTitle()).setImageUrl(Constants.General.BLOB_PROTOCOL + imageAddress);

    }

    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }
    mRealm.close();
  }

  @Override
  public void onChatUpdated(CompactChat chat) {
    mChat = chat;
    mTextViewSubtitle.setText(chat.getMembersCount() + " Members");//getResources().getQuantityString(R.plurals.members, mChat.getMembers().size(), mChat.getMembers().size()));
    mAdapter.mChat = mChat;
  }

  @Override
  public void onMemberAdded(String ChatId, Profile profile) {

  }

  @Override
  public void onTitleChanged(String chatId, String newTitle) {

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
  public void onImageChanged(String chatId, String imageAddress) {

  }

  @Override
  public int compare(CompactChatMember lhs, CompactChatMember rhs) {
    return lhs.getName().compareToIgnoreCase(rhs.getName());
  }

}
