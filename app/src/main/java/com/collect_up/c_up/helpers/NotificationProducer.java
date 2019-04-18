/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;
import android.content.Intent;

import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.fragments.FragmentPerson;
import com.collect_up.c_up.model.Initial;
import com.collect_up.c_up.model.Notification;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.Map;

public class NotificationProducer {
  private String imageAddress;
  private String text;
  private Context mContext;
  Initial initialModel = Initial.getInstance();

  public String getPostImageAddress() {
    return postImageAddress;
  }

  public void setPostImageAddress(String postImageAddress) {
    this.postImageAddress = postImageAddress;
  }

  private Intent mIntent;
  private boolean sendNotification;
  private String postImageAddress;

  public NotificationProducer(Notification notification, Context context) {
    mContext = context;
    produce(notification);
  }

  public Intent getIntent() {
    return mIntent;
  }

  public void setIntent(Intent intent) {
    mIntent = intent;
  }

  private void produce(Notification notification) {
    switch (notification.getText())
    {
      case "Like":
        postImageAddress = notification.getPost().getImageAddress();
        imageAddress = notification.getActor().getImageAddress();
        text = notification.getActor().getName() + " liked your post.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppLike");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
      case "ShopLike":
        imageAddress = notification.getPost().getImageAddress();
        text = notification.getActor().getName() + " liked your business's post.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppShopLike");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
      case "ComplexLike":
        imageAddress = notification.getPost().getImageAddress();
        text = notification.getActor().getName() + " liked your complex's post.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexLike");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
      case "Comment":
        postImageAddress = notification.getPost().getImageAddress();
        imageAddress = notification.getActor().getImageAddress();
        text = notification.getActor().getName() + " commented on your post.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComment");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
      case "ShopComment":
        imageAddress = notification.getPost().getImageAddress();
        text = notification.getActor().getName() + " commented on your business's post.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppShopComment");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
      case "ComplexComment":
        imageAddress = notification.getPost().getImageAddress();
        text = notification.getActor().getName() + " commented on your complex's post.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexComment");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
      case "Mention":
        postImageAddress = notification.getPost().getImageAddress();
        imageAddress = notification.getActor().getImageAddress();
        text = notification.getActor().getName() + " Mentioned you in a Comment.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComment");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
      case "ShopMention":
        imageAddress = notification.getPost().getImageAddress();
        text = notification.getActor().getName() + " Mentioned you in a Comment.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppShopComment");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
      case "ComplexMention":
        imageAddress = notification.getPost().getImageAddress();
        text = notification.getActor().getName() + " Mentioned you in a Comment.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexComment");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;


      case "Follow":
        imageAddress = "*";
        text = notification.getActor().getName() + " started following you.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppFollow");

        if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(notification.getActor().getId())))
        {
          mIntent = new Intent(mContext, ActivityHome.class);
          mIntent.putExtra("data", notification.getActor());
        }

        if (!Logged.Models.getUserProfile().getFollowers().contains(notification.getActor().getId()))
        {
          Profile profile = Logged.Models.getUserProfile();
          profile.getFollowers().add(notification.getActor().getId());
          Logged.Models.setUserProfile(profile);
        }

        break;
      case "ShopFollow":
        imageAddress = notification.getShop().getImageAddress();
        text = notification.getActor().getName() + " started following your business.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppShopFollow");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getShop());
        break;
      case "ComplexFollow":
        imageAddress = notification.getComplex().getImageAddress();
        text = notification.getActor().getName() + " started following your complex.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexFollow");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getComplex());
        break;
      case "RateProduct":
        imageAddress = notification.getProduct().getDefaultImageAddress();
        text = notification.getActor().getName() + " rated to your product.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppRateProduct");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getProduct());
        break;
      case "RequestPerson":
        imageAddress = notification.getActor().getImageAddress();
        text = notification.getActor().getName() + " has requested to follow you.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppFollow");
        if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(notification.getActor().getId())))
        {
          mIntent = new Intent(mContext, ActivityHome.class);
          mIntent.putExtra("data", notification.getActor());
        }

        break;
      case "RequestShopToComplex":
        imageAddress = notification.getShop().getImageAddress();
        text = notification.getShop().getName() + " has requested to join your complex.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppShopFollow");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getShop());

        break;
      case "RequestComplexToShop":
        imageAddress = notification.getComplex().getImageAddress();
        text = notification.getComplex().getName() + " has requested to join your business.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexFollow");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getComplex());
        break;
      case "PersonFollowAccept":
        imageAddress = notification.getActor().getImageAddress();
        text = notification.getActor().getName() + " accepted your follow request.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppFollow");
        if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(notification.getActor().getId())))
        {
          mIntent = new Intent(mContext, ActivityHome.class);
          mIntent.putExtra("data", notification.getActor());
        }

        if (!Logged.Models.getUserProfile().getFollowing().contains(notification.getActor().getId()))
        {
          Profile profile = Logged.Models.getUserProfile();
          profile.getFollowing().add(notification.getActor().getId());
          Logged.Models.setUserProfile(profile);
        }
        break;
      case "ComplexToShopAccept":
        imageAddress = notification.getShop().getImageAddress();
        text = notification.getShop().getName() + " accepted your join request.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppShopFollow");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getShop());
        break;
      case "ShopToComplexAccept":
        imageAddress = notification.getComplex().getImageAddress();
        text = notification.getComplex().getName() + " accepted your join request.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexFollow");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getComplex());
        if (Logged.Models.getUserShop() != null)
        {
          Shop shop = Logged.Models.getUserShop();
          shop.setComplex(notification.getComplex());
          Logged.Models.setUserShop(shop);
        }
        break;
      case "RateShop":
        imageAddress = notification.getShop().getImageAddress();
        text = notification.getActor().getName() + " rated to your business.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppRateShop");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getShop());
        break;
      case "RateComplex":
        imageAddress = notification.getComplex().getImageAddress();
        text = notification.getActor().getName() + " rated to your complex.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppRateComplex");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getComplex());
        break;
      case "ShopManager":
        imageAddress = notification.getShop().getImageAddress();
        text = notification.getActor().getName() + " added you as a business manager.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppShopManager");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getShop());
        initialModel.initialize(mContext);
        break;
      case "ComplexManager":
        imageAddress = notification.getComplex().getImageAddress();
        text = notification.getActor().getName() + " added you as a complex manager.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexManager");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getComplex());
        initialModel.initialize(mContext);
        break;
      case "Joined":

        imageAddress = "*";

        HashMap<String, String> contactsInHawk = Hawk.get("contacts");

        if (contactsInHawk != null)
        {
          for (Map.Entry<String, String> entry : contactsInHawk.entrySet())
          {
            if (entry.getKey().equals(notification.getActor().getPhoneNumber()))
            {
              text = entry.getValue() + " joined Collect-Up.";
              break;
            }
          }
        } else
        {
          text = notification.getActor().getName() + " joined Collect-Up.";
        }


        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppJoin");

        if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(notification.getActor().getId())))
        {
          mIntent = new Intent(mContext, ActivityHome.class);
          mIntent.putExtra("data", notification.getActor());
        }
        break;
      case "ShopEvent":

        imageAddress = notification.getShop().getImageAddress();
        text = notification.getShop().getName() + " created new event: " + notification.getEvent().getTitle() + ". ";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexFollow");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getShop());
        break;
      case "ComplexEvent":
        imageAddress = notification.getComplex().getImageAddress();
        text = notification.getComplex().getName() + " created new event: " + notification.getEvent().getTitle() + ". ";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppComplexFollow");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getComplex());
        break;
      case "TaggedInPost":
        postImageAddress = notification.getPost().getImageAddress();
        imageAddress = notification.getActor().getImageAddress();
        text = notification.getActor().getName() + " tagged you in post.";
        sendNotification = PreferenceHelper.getBoolean(mContext, "prefMuteAppLike");

        mIntent = new Intent(mContext, ActivityHome.class);
        mIntent.putExtra("data", notification.getPost());
        break;
    }
  }

  public String getImageAddress() {
    return imageAddress;
  }

  public void setImageAddress(String imageAddress) {
    this.imageAddress = imageAddress;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public boolean getSendNotification() {
    return sendNotification;
  }

  public void setSendNotification(boolean sendNotification) {
    this.sendNotification = sendNotification;
  }
}
