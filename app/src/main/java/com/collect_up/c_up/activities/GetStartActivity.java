/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentActivate;
import com.collect_up.c_up.fragments.FragmentRegister;
import com.collect_up.c_up.fragments.GetStartFragments;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Images;
import com.collect_up.c_up.helpers.Upload;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.receivers.CallReceiver;
import com.collect_up.c_up.receivers.SmsReceiver;
import com.collect_up.c_up.services.CheckInternetConnectivity;
import com.collect_up.c_up.services.RealtimeService;
import com.collect_up.c_up.view.RectangleNetworkImageView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class GetStartActivity extends AppCompatActivity {

  public static SmsReceiver mSmsReceiver;
  public static CallReceiver mCallReceiver;
  public static boolean isFirstTimeCallReceived;

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == UCrop.REQUEST_CROP)
    {
      handleCrop(resultCode, data);
    } else if (requestCode == Constants.RequestCodes.PICK_IMAGE.ordinal() && resultCode == RESULT_OK)
    {
      //    CropHelper.beginCrop(this, data.getData(), true);
    } else if (requestCode == Constants.RequestCodes.TAKE_PHOTO.ordinal() && resultCode == RESULT_OK)
    {
      //  CropHelper.beginCrop(this, data.getData(), true);
    } else if (requestCode == Constants.RequestCodes.UPDATE_SETTINGS.ordinal())
    {
      recreate();
    } else
    {
      //   CropHelper.beginCrop(this, data.getData(), true);
    }
  }

  @Override
  public void onBackPressed() {
    if (!GetStartFragments.tryAgainPressed)
    {
      finish();
    } else
    {
      GetStartFragments.tryAgainPressed = false;
      super.onBackPressed();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    try
    {
      if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction()))
      {
        int receivedActionCode = intent.getIntExtra("activation_code", 0);

        FragmentActivate activateFragment = (FragmentActivate) getSupportFragmentManager()
          .findFragmentByTag(FragmentActivate.class.getName());

        if (activateFragment != null && activateFragment.isVisible())
        {
          View activateFragmentView = activateFragment.getView();
          if (activateFragmentView != null)
          {
            Button checkButton = (Button) activateFragmentView.findViewById(R.id.button_check);
            EditText activationCodeEditText = (EditText) activateFragmentView.findViewById(R.id.edit_text_activation_code);

            if (activationCodeEditText != null)
            {
              activationCodeEditText.setText(Integer.toString(receivedActionCode));

              if (checkButton != null)
              {
                checkButton.callOnClick();
              }
            }
          }
        }
      } else if (intent.getAction().equals("android.intent.action.PHONE_STATE"))
      {
        // There is no API to check whether a receiver is registered or not.
        try
        {
          unregisterReceiver(mCallReceiver);
        } catch (IllegalArgumentException ignored)
        {

        }
        isFirstTimeCallReceived = true;
        replaceFragment(new FragmentRegister());
      }
    } catch (Exception e)
    {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Used to replace fragments inside fragments.
   *
   * @param fragmentInstance Fragment instance
   */
  public void replaceFragment(Fragment fragmentInstance) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    // Slide left to right and for the pop from the stack, right to left transition are defined.
    transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
      .addToBackStack(fragmentInstance.getClass().getName())
      .replace(R.id.fragment_placeholder, fragmentInstance, fragmentInstance.getClass()
        .getName())
      .commitAllowingStateLoss();
  }

  private void handleCrop(int resultCode, Intent result) {
    if (resultCode == RESULT_OK)
    {
      try
      {
        File tempFile = Images.createTempFile(this, Constants.General.APP_FOLDER_IMAGE_PATH);
        Images.saveBitmap(this, tempFile.getAbsolutePath(), Images.getBitmapFromUri(this, UCrop.getOutput(result)));
        String imagePath = Images.compressJpeg(this, tempFile.getAbsolutePath(), Constants.General.APP_FOLDER_IMAGE_PATH, true);
        replaceFragmentProfilePicture(imagePath);
      } catch (IOException e)
      {
      }

    } else if (resultCode == UCrop.RESULT_ERROR)
    {
      Toast.makeText(this, UCrop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void replaceFragmentProfilePicture(String imagePath) {
    FragmentRegister registerFragment = (FragmentRegister) getSupportFragmentManager()
      .findFragmentByTag(FragmentRegister.class.getName());
    Button registerButton = null;
    EditText usernameEditText = null;
    RectangleNetworkImageView profilePicture = null;
    if (registerFragment != null && registerFragment.isVisible())
    {
      View registerFragmentView = registerFragment.getView();
      if (registerFragmentView != null)
      {
        profilePicture = (RectangleNetworkImageView) registerFragmentView.findViewById(R.id.image_view_picture);
        registerButton = (Button) registerFragmentView.findViewById(R.id.button_register);
        usernameEditText = (EditText) registerFragmentView.findViewById(R.id.edit_text_username);
        registerButton.setEnabled(false);

        profilePicture.setTag(imagePath);
        profilePicture.setLocalImagePath(imagePath);
      }
    }

    final Button finalRegisterButton = registerButton;
    final EditText finalUsernameEditText = usernameEditText;
    final ImageView finalProfilePicture = profilePicture;

    if (finalRegisterButton != null)
    {

      if (finalRegisterButton.getTag() != null && finalRegisterButton.getTag().equals("login"))
      {
        new Upload(this, new File(imagePath), UUID.randomUUID().toString(), "image/jpeg").uploadImage(new IUploadCallback() {
          @Override
          public void onFileReceived(String fileName, String uploadedPath) {
            if (finalProfilePicture != null)
            {
              finalProfilePicture.setTag(uploadedPath);
            }

            if (finalRegisterButton != null && finalUsernameEditText != null && finalUsernameEditText.getError() == null && !Utils.isNullOrEmpty(finalUsernameEditText.getText().toString()))
            {
              finalRegisterButton.setEnabled(true);
            }
          }

          @Override
          public void onFailed(int statusCode) {
            if (finalProfilePicture != null)
            {
              finalProfilePicture.setTag(null);
            }

            if (finalRegisterButton != null && finalUsernameEditText != null && finalUsernameEditText.getError() == null && !Utils.isNullOrEmpty(finalUsernameEditText.getText().toString()))
            {
              finalRegisterButton.setEnabled(true);
            }
          }

          @Override
          public void onProgress(long bytesWritten, long totalSize) {

          }
        });
      } else
      {
        new Upload(this, new File(imagePath), UUID.randomUUID().toString(), "image/jpeg").uploadImage(new IUploadCallback() {
          @Override
          public void onFileReceived(String fileName, String uploadedPath) {
            if (finalProfilePicture != null)
            {
              finalProfilePicture.setTag(uploadedPath);
            }

            if (finalRegisterButton != null && finalUsernameEditText != null && finalUsernameEditText.getError() == null && !Utils.isNullOrEmpty(finalUsernameEditText.getText().toString()))
            {
              finalRegisterButton.setEnabled(true);
            }
          }

          @Override
          public void onFailed(int statusCode) {
            if (finalProfilePicture != null)
            {
              finalProfilePicture.setTag(null);
            }

            if (finalRegisterButton != null && finalUsernameEditText != null && finalUsernameEditText.getError() == null && !Utils.isNullOrEmpty(finalUsernameEditText.getText().toString()))
            {
              finalRegisterButton.setEnabled(true);
            }
          }

          @Override
          public void onProgress(long bytesWritten, long totalSize) {

          }
        });
      }
    }

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_get_start);

    startService(new Intent(getApplicationContext(), RealtimeService.class));
    startService(new Intent(getApplicationContext(), CheckInternetConnectivity.class));

    IntentFilter intentFilter = new IntentFilter("android.intent.action.DATA_SMS_RECEIVED");
    intentFilter.setPriority(10);
    intentFilter.addDataScheme("sms");
    intentFilter.addDataAuthority("*", "6734");

    IntentFilter callReceiverIntentFilter = new IntentFilter("android.intent.action.PHONE_STATE");
    callReceiverIntentFilter.setPriority(10);

    mSmsReceiver = new SmsReceiver();
    mCallReceiver = new CallReceiver();

    registerReceiver(mSmsReceiver, intentFilter);
    registerReceiver(mCallReceiver, callReceiverIntentFilter);

    // Following lines are related to get start fragment
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.fragment_placeholder, new GetStartFragments.WelcomeFragment())
      .commit();
  }

  @Override
  protected void onStop() {
    // There is no API to check whether a receiver is registered or not.
    try
    {
      unregisterReceiver(mSmsReceiver);
      unregisterReceiver(mCallReceiver);
    } catch (IllegalArgumentException ignored)
    {

    }
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    // There is no API to check whether a receiver is registered or not.
    try
    {
      unregisterReceiver(mSmsReceiver);
      unregisterReceiver(mCallReceiver);
    } catch (IllegalArgumentException ignored)
    {

    }
    super.onDestroy();
  }
}
