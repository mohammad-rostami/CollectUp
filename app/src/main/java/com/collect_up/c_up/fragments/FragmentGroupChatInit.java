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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.GroupChatInitMembersAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.ChoosePhoto;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.NewGroup;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.view.RectangleNetworkImageView;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rey.material.app.BottomSheetDialog;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.MediaContent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;

@SuppressLint ("ValidFragment")

public class FragmentGroupChatInit extends BaseFragment implements
  ChoosePhoto.OnDialogButtonClick,
  Validator.ValidationListener {

  @NotEmpty (trim = true)
  @Bind (R.id.edit_text_group_name)
  EditText mEditTextGroupName;
  @Bind (R.id.recycler_view)
  UltimateRecyclerView mRecyclerView;
  @Bind (R.id.image_view_picture)
  RectangleNetworkImageView mPicture;
  @Bind (R.id.text_view_members_count)
  TextView mMembersCount;
  private GroupChatInitMembersAdapter mAdapter;
  private ArrayList<Profile> mMembers = new ArrayList<>();
  private BottomSheetDialog mDialog;
  private Validator mValidator;
  private Menu mMenu;
  private View view;
  private Intent intent;

  public FragmentGroupChatInit(Intent intent) {
    this.intent = intent;
  }

  public FragmentGroupChatInit() {
  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((ActivityHome) getActivity()).changeButtonBackgroud(3);

    super.onResume();
  }

  @Nullable
  @Override

  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_group_chat_init, container, false);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      ButterKnife.bind(this, view);
      setHasOptionsMenu(true);
      Bundle bundle = intent.getExtras();

      if (bundle != null)
      {
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

        mMembers = bundle.getParcelableArrayList("members");
        mAdapter = new GroupChatInitMembersAdapter(getContext(), mMembers);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        mMembersCount.setText(getResources().getQuantityString(R.plurals.members, mMembers.size(), mMembers.size()));
      }
      return view;
    } else
    {
      return view;
    }
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_done_discard, menu);
    MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
    Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
    if (menuItemLoaderIcon != null)
    {
      try
      {
        menuItemLoaderIcon.mutate();
        menuItemLoaderIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        menuItemLoader.setIcon(menuItemLoaderIcon);
      } catch (IllegalStateException e)
      {
        Log.i("sepehr", String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
      }
      ((Animatable) menuItemLoader.getIcon()).start();
      menuItemLoader.setVisible(false);
    }
    menu.findItem(R.id.action_search).setVisible(false);
    mMenu = menu;

  }

  @OnClick (R.id.image_view_picture)
  public void onPictureClick() {
    mDialog = new ChoosePhoto(getActivity(), FragmentGroupChatInit.this, false).show();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case R.id.action_done:
        mValidator.validate(true);
        break;
      case android.R.id.home:

        break;
    }
    return true;
  }

  private void invokeNewChat() throws IOException {
    if (mMembers.size() >= 2 && mEditTextGroupName.getText().toString().trim().length() >= 1)
    {

      mMenu.findItem(R.id.action_done).setVisible(false);
      mMenu.findItem(R.id.menu_loader).setVisible(true);

      if (!mPicture.getTag().equals(""))
      {
        String path = (String) mPicture.getTag();
        new Upload(getContext(), new File(path), UUID.randomUUID().toString(), "image/*").uploadImage(new IUploadCallback() {
          @Override
          public void onFileReceived(String fileName, String uploadedPath) {

          }

          @Override
          public void onFailed(int statusCode) {
            mMenu.findItem(R.id.action_done).setVisible(true);
            mMenu.findItem(R.id.menu_loader).setVisible(false);

            Toast.makeText(getContext(), R.string.toast_error_create_new_chat, Toast.LENGTH_LONG).show();
          }

          @Override
          public void onProgress(long bytesWritten, long totalSize) {

          }
        });
      } else
      {

        ArrayList<String> membersId = new ArrayList<String>();
        for (int i = 0; i < mMembers.size(); i++)
        {
          membersId.add(mMembers.get(i).getId());
        }
        NewGroup newGroup = new NewGroup();
        newGroup.setTitle(mEditTextGroupName.getText().toString());
        newGroup.setMembersId(membersId);
        HttpClient.post(getContext(), Constants.Server.Messaging.POST_NEW_GROUP, new Gson().toJson(newGroup, NewGroup.class), ContentType.APPLICATION_JSON.toString(), new AsyncHttpResponser(getContext()) {
          @Override
          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            super.onSuccess(statusCode, headers, responseBody);
            CompactChat chat = GsonParser.getObjectFromGson(responseBody, CompactChat.class);
            FragmentHandler.replaceFragment(getContext(), fragmentType.GROUPCHAT, chat, false);
          }

          @Override
          public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            super.onFailure(statusCode, headers, responseBody, error);
            mMenu.findItem(R.id.action_done).setVisible(true);
            mMenu.findItem(R.id.menu_loader).setVisible(false);

          }
        });
      }
    }
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
    mPicture.setImageDrawable(null);
    Bitmap bitmap = Images.getBitmapFromUri(getContext(), uri);
    Uri newUri = Images.getImageUriFromBitmap(getContext(), bitmap);
    String path = Utils.getPath(getContext(), newUri);
    mPicture.setTag(path);
    mPicture.setLocalImageBitmap(bitmap);
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
    mDialog.dismiss();
    mPicture.setImageResource(R.drawable.take_picture_chat_group);
    mPicture.setTag("");
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
  public void onValidationSucceeded() {
    try
    {
      invokeNewChat();
    } catch (IOException e)
    {
    }
  }

  @Override
  public void onValidationFailed(List<ValidationError> errors) {
    for (ValidationError error : errors)
    {
      View view = error.getView();
      String message = error.getCollatedErrorMessage(getContext());
      if (view instanceof EditText)
      {
        ((EditText) view).setError(message);
      }
    }
  }

}
