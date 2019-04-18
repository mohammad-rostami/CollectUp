/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.RealmString;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.PostProfileTag;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.model.StickerPackage;
import com.collect_up.c_up.model.realm.RChat;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.model.realm.RComplex;
import com.collect_up.c_up.model.realm.RPost;
import com.collect_up.c_up.model.realm.RPostProfileTag;
import com.collect_up.c_up.model.realm.RProduct;
import com.collect_up.c_up.model.realm.RProfile;
import com.collect_up.c_up.model.realm.RShop;
import com.collect_up.c_up.model.realm.RStickerPackage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;

public class RToNonR {

  public static StickerPackage rStickerPackageToStickerPackage(RStickerPackage rStickerPackage) {
    StickerPackage stickerPackage = new StickerPackage();
    stickerPackage.setId(rStickerPackage.getId());
    stickerPackage.setCount(rStickerPackage.getCount());
    stickerPackage.setPackageName(rStickerPackage.getPackageName());
    stickerPackage.setStickers(realmListStringToListString(rStickerPackage.getStickers()));
    stickerPackage.setThumbnailAddress(rStickerPackage.getThumbnailAddress());
    return stickerPackage;
  }

  public static RStickerPackage stickerPackageToRStickerPackage(StickerPackage stickerPackage) {
    RStickerPackage rStickerPackage = new RStickerPackage();
    rStickerPackage.setId(stickerPackage.getId());
    rStickerPackage.setCount(stickerPackage.getCount());
    rStickerPackage.setPackageName(stickerPackage.getPackageName());
    rStickerPackage.setStickers(realmListStringToListString(stickerPackage.getStickers()));
    rStickerPackage.setThumbnailAddress(stickerPackage.getThumbnailAddress());
    return rStickerPackage;
  }

  public static CompactMessage rCompactMessageToCompactMessage(RCompactMessage rCompactMessage) {
    CompactMessage compactMessage = new CompactMessage();
    compactMessage.setId(rCompactMessage.getId() != null ? rCompactMessage.getId() : UUID.randomUUID().toString());
    compactMessage.setMessageId(rCompactMessage.getMessageId());
    compactMessage.setChatId(rCompactMessage.getChatId());
    compactMessage.setContentAddress(rCompactMessage.getContentAddress());
    compactMessage.setContentType(rCompactMessage.getContentType());
    compactMessage.setMessageStatus(rCompactMessage.getMessageStatus());
    compactMessage.setSendDateTime(rCompactMessage.getSendDateTime());
    compactMessage.setContentSize(rCompactMessage.getContentSize());
    compactMessage.setSenderImage(rCompactMessage.getSenderImage());
    compactMessage.setSenderId(rCompactMessage.getSenderId());
    compactMessage.setSenderName(rCompactMessage.getSenderName());
    compactMessage.setText(rCompactMessage.getText());
    compactMessage.setThumbnailAddress(rCompactMessage.getThumbnailAddress());
    compactMessage.setFilePath(rCompactMessage.getFilePath());

    return compactMessage;
  }


  public static RCompactMessage compactMessageToRCompactMessage(CompactMessage compactMessage) {
    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(MyApplication.context));

    RCompactMessage RcompactMessage = new RCompactMessage();
    RcompactMessage.setId(compactMessage.getId() != null ? compactMessage.getId() : UUID.randomUUID().toString());
    RcompactMessage.setMessageId(compactMessage.getMessageId());
    RcompactMessage.setChatId(compactMessage.getChatId());
    RcompactMessage.setContentAddress(compactMessage.getContentAddress());
    RcompactMessage.setContentType(compactMessage.getContentType());
    RcompactMessage.setMessageStatus(compactMessage.getMessageStatus());
    RcompactMessage.setSendDateTime(compactMessage.getSendDateTime());
    RcompactMessage.setContentSize(compactMessage.getContentSize());
    RcompactMessage.setSenderImage(compactMessage.getSenderImage());
    RcompactMessage.setSenderId(compactMessage.getSenderId());
    RcompactMessage.setSenderName(compactMessage.getSenderName());
    RcompactMessage.setText(compactMessage.getText());
    RcompactMessage.setContentAddress(compactMessage.getContentAddress());
    RcompactMessage.setThumbnailAddress(compactMessage.getThumbnailAddress());
    RcompactMessage.setFilePath(compactMessage.getFilePath());
    if (realm.where(RCompactMessage.class).equalTo("MessageId", compactMessage.getMessageId()).findFirst() != null)
    {
      RcompactMessage.setRank(realm.where(RCompactMessage.class).equalTo("MessageId", compactMessage.getMessageId()).findFirst().getRank());
    } else
    {
      RcompactMessage.setRank(realm.where(RCompactMessage.class).equalTo("ChatId", compactMessage.getChatId()).max("Rank") != null ? realm.where(RCompactMessage.class).equalTo("ChatId", compactMessage.getChatId()).max("Rank").longValue() + 1 : 0);
    }
    return RcompactMessage;
  }

  public static CompactChat rChatToChat(RChat rChat) {
    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(MyApplication.context));

    CompactChat chat = new CompactChat();
    chat.setChatId(rChat.getChatId());
    chat.setIsGroup(rChat.getIsGroup());
    chat.setAmIManager(rChat.isAmIManager());
    chat.setAmISuperAdmin(rChat.getAmISuperAdmin());
    chat.setTitle(rChat.getTitle());
    chat.setProfileThumbnailAddress(rChat.getProfileThumbnailAddress());
    if (rChat.getLastMessage() != null)
    {
      chat.setLastMessage(rCompactMessageToCompactMessage(rChat.getLastMessage()));
    }
    chat.setMembersCount(rChat.getMembersCount());
    chat.setReceiverId(rChat.getReceiverId());
    chat.setUnSeenMessageCount(rChat.getUnSeenMessageCount());
    chat.setLastOnline(rChat.getLastOnline());
    return chat;
  }

  public static RChat chatToRChat(CompactChat chat) {

    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(MyApplication.context));

    RChat rChat = new RChat();
    rChat.setChatId(chat.getChatId());
    rChat.setIsGroup(chat.isGroup());
    rChat.setAmIManager(chat.isAmIManager());
    rChat.setAmISuperAdmin(chat.AmISuperAdmin());
    rChat.setTitle(chat.getTitle());
    rChat.setProfileThumbnailAddress(chat.getProfileThumbnailAddress());
    if (chat.getLastMessage() != null)
    {
      RChat dbRchat = realm.where(RChat.class).equalTo("ChatId", chat.getChatId()).findFirst();
      if (dbRchat == null || (dbRchat != null && dbRchat.getLastMessage().getMessageId() == null))
      {
        rChat.setLastMessage(compactMessageToRCompactMessage(chat.getLastMessage()));
      } else
      {
        rChat.setLastMessage(dbRchat.getLastMessage());
      }
    }
    rChat.setMembersCount(chat.getMembersCount());
    rChat.setReceiverId(chat.getReceiverId());
    rChat.setUnSeenMessageCount(chat.getUnSeenMessageCount());
    rChat.setLastOnline(chat.getLastOnline());
    return rChat;
  }


  private static List<String> realmListStringToListString(RealmList<RealmString> realmStrings) {
    if (realmStrings == null)
    {
      return null;
    }

    List<String> stringList = new ArrayList<>();
    for (RealmString realmString : realmStrings)
    {
      stringList.add(realmString.getString());
    }

    return stringList;
  }

  private static RealmList<RealmString> realmListStringToListString(List<String> realmStrings) {
    if (realmStrings == null)
    {
      return null;
    }

    RealmList<RealmString> stringList = new RealmList<>();
    for (String realmString : realmStrings)
    {
      RealmString realmString1 = new RealmString();
      realmString1.setString(realmString);
      stringList.add(realmString1);
    }

    return stringList;
  }

  private static ArrayList<PostProfileTag> ListPostProfileTagToRealmListPostProfileTag(RealmList<RPostProfileTag> rPostProfileTag) {
    if (rPostProfileTag == null)
    {
      return null;
    }

    ArrayList<PostProfileTag> postProfileTagList = new ArrayList<>();
    for (RPostProfileTag realmpostProfileTag : rPostProfileTag)
    {
      PostProfileTag postProfileTag1 = new PostProfileTag();
      postProfileTag1.setX(realmpostProfileTag.getX());
      postProfileTag1.setY(realmpostProfileTag.getY());
      if (realmpostProfileTag.getPost() != null)
      {
        postProfileTag1.setPost(rPostToPost(realmpostProfileTag.getPost()));
      }
      postProfileTag1.setProfile(rProfileToProfile(realmpostProfileTag.getProfile()));
      postProfileTagList.add(postProfileTag1);
    }

    return postProfileTagList;
  }

  private static RealmList<RPostProfileTag> realmListPostProfileTagToListPostProfileTag(List<PostProfileTag> postProfileTag) {
    if (postProfileTag == null)
    {
      return null;
    }

    RealmList<RPostProfileTag> postProfileTagList = new RealmList<>();
    for (PostProfileTag realmpostProfileTag : postProfileTag)
    {
      RPostProfileTag realmpostProfileTag1 = new RPostProfileTag();
      realmpostProfileTag1.setX(realmpostProfileTag.getX());
      realmpostProfileTag1.setY(realmpostProfileTag.getY());
      if (realmpostProfileTag.getPost() != null)
      {
        realmpostProfileTag1.setPost(postTimelineToRpostTimeline(realmpostProfileTag.getPost()));
      }
      realmpostProfileTag1.setProfile(profileToRProfile(realmpostProfileTag.getProfile()));
      postProfileTagList.add(realmpostProfileTag1);
    }

    return postProfileTagList;
  }

  public static Profile rProfileToProfile(RProfile rProfile) {
    Profile profile = new Profile();
    profile.setBlockList(rProfile.getBlockList());
    profile.setFollowers(realmListStringToListString(rProfile.getFollowers()));
    profile.setId(rProfile.getId());
    profile.setCountryCode(rProfile.getCountryCode());
    profile.setFollowing(realmListStringToListString(rProfile.getFollowing()));
    profile.setBiography(rProfile.getBiography());
    profile.setImageAddress(rProfile.getImageAddress());
    profile.setCoverPhoto(rProfile.getCoverPhoto());
    profile.setIsMan(rProfile.getIsMan());
    profile.setIsOnline(rProfile.getIsOnline());
    profile.setJob(rProfile.getJob());
    profile.setLanguages(realmListStringToListString(rProfile.getLanguages()));
    profile.setLastOnline(rProfile.getLastOnline());
    profile.setLat(rProfile.getLat());
    profile.setLong(rProfile.getLong());
    profile.setMutedProfilesId(realmListStringToListString(rProfile.getMutedProfilesId()));
    profile.setName(rProfile.getName());
    profile.setPhoneNumber(rProfile.getPhoneNumber());
    profile.setUsername(rProfile.getUsername());
    profile.setBirthday(rProfile.getBirthday());
    profile.setPrivateMessaging(rProfile.getPrivateMessaging());
    profile.setIsPrivate(rProfile.getIsPrivate());
    profile.setSettingsBiography(rProfile.getSettingsBiography());
    profile.setSettingsBirthday(rProfile.getSettingsBirthday());
    profile.setSettingsEmail(rProfile.getSettingsEmail());
    profile.setSettingsGender(rProfile.getSettingsGender());
    profile.setSettingsJob(rProfile.getSettingsJob());
    profile.setSettingsLanguages(rProfile.getSettingsLanguages());
    profile.setSettingsLocation(rProfile.getSettingsLocation());
    profile.setSettingsPhoneNumber(rProfile.getSettingsPhoneNumber());
    profile.setSettingsUsername(rProfile.getSettingsUsername());
    profile.setRequested(realmListStringToListString(rProfile.getRequested()));
    profile.setFollowing(rProfile.getIsFollowing());
    profile.setOfficial(rProfile.getIsOfficial());
    return profile;
  }

  public static RShop shopToRshop(Shop rShop) {
    RShop shop = new RShop();
    shop.setAdminId(rShop.getAdminId());
    shop.setRatesAverage(rShop.getRatesAverage());
    shop.setPhoneNumber(rShop.getPhoneNumber());
    shop.setRatesCount(rShop.getRatesCount());
    shop.setUsername(rShop.getUsername());
    shop.setTags(realmListStringToListString(rShop.getTags()));
    shop.setName(rShop.getName());
    shop.setRequests(realmListStringToListString(rShop.getRequests()));
    shop.setLong(rShop.getLong());
    shop.setLat(rShop.getLat());
    shop.setManagersId(realmListStringToListString(rShop.getManagersId()));
    shop.setCoverPhoto(rShop.getCoverPhoto());
    shop.setAddress(rShop.getAddress());
    if (rShop.getComplex() != null)
    {
      shop.setComplex(complexToRcomplex(rShop.getComplex()));
    }
    shop.setDescription(rShop.getDescription());
    shop.setEmail(rShop.getEmail());
    shop.setFollowers(realmListStringToListString(rShop.getFollowers()));
    shop.setId(rShop.getId());
    shop.setImageAddress(rShop.getImageAddress());
    shop.setBusinessLevel(rShop.getBusinessLevel());
    shop.setIsFollowing(rShop.isFollowing());
    return shop;
  }

  public static Shop rShopToShop(RShop rShop) {
    Shop shop = new Shop();
    shop.setAdminId(rShop.getAdminId());
    shop.setRatesAverage(rShop.getRatesAverage());
    shop.setPhoneNumber(rShop.getPhoneNumber());
    shop.setRatesCount(rShop.getRatesCount());
    shop.setUsername(rShop.getUsername());
    shop.setTags(realmListStringToListString(rShop.getTags()));
    shop.setName(rShop.getName());
    shop.setRequests(realmListStringToListString(rShop.getRequests()));
    shop.setLong(rShop.getLong());
    shop.setLat(rShop.getLat());
    shop.setManagersId(realmListStringToListString(rShop.getManagersId()));
    shop.setCoverPhoto(rShop.getCoverPhoto());
    shop.setAddress(rShop.getAddress());
    if (rShop.getComplex() != null)
    {
      shop.setComplex(rComplexToComplex(rShop.getComplex()));
    }
    shop.setDescription(rShop.getDescription());
    shop.setEmail(rShop.getEmail());
    shop.setFollowers(realmListStringToListString(rShop.getFollowers()));
    shop.setId(rShop.getId());
    shop.setImageAddress(rShop.getImageAddress());
    shop.setBusinessLevel(rShop.getBusinessLevel());
    shop.setFollowing(rShop.getIsFollowing());
    return shop;
  }

  public static RPost postTimelineToRpostTimeline(Post rPost) {
    RPost postTimeLine = new RPost();
    postTimeLine.setImageAddress(rPost.getImageAddress());
    postTimeLine.setCommentsCount(rPost.getCommentsCount());
    postTimeLine.setId(rPost.getId());
    postTimeLine.setImageSize(rPost.getSize());
    postTimeLine.setInsertTime(rPost.getInsertTime());
    postTimeLine.setLikes(realmListStringToListString(rPost.getLikes()));
    if (rPost.getSenderComplex() != null)
    {
      postTimeLine.setSenderComplex(complexToRcomplex(rPost.getSenderComplex()));
    }
    if (rPost.getSenderProfile() != null)
    {
      postTimeLine.setSenderProfile(profileToRProfile(rPost.getSenderProfile()));
    }
    if (rPost.getSenderShop() != null)
    {
      postTimeLine.setSenderShop(shopToRshop(rPost.getSenderShop()));
    }
    postTimeLine.setTags(realmListStringToListString(rPost.getTags()));
    postTimeLine.setText(rPost.getText());
    postTimeLine.setVideoAddress(rPost.getVideoAddress());
    postTimeLine.setVideoProperties(rPost.getVideoProperties());
    postTimeLine.setViewCount(rPost.getViewCount());
    if (rPost.getProfileTags() != null)
    {
      postTimeLine.setProfileTags(realmListPostProfileTagToListPostProfileTag(rPost.getProfileTags()));
    }
    return postTimeLine;
  }

  public static Post rPostToPost(RPost rPost) {
    Post postTimeLine = new Post();
    postTimeLine.setImageAddress(rPost.getImageAddress());
    postTimeLine.setCommentsCount(rPost.getCommentsCount());
    postTimeLine.setId(rPost.getId());
    postTimeLine.setSize(rPost.getImageSize());
    postTimeLine.setInsertTime(rPost.getInsertTime());
    postTimeLine.setLikes(realmListStringToListString(rPost.getLikes()));
    if (rPost.getSenderComplex() != null)
    {
      postTimeLine.setSenderComplex(rComplexToComplex(rPost.getSenderComplex()));
    }
    if (rPost.getSenderProfile() != null)
    {
      postTimeLine.setSenderProfile(rProfileToProfile(rPost.getSenderProfile()));
    }
    if (rPost.getSenderShop() != null)
    {
      postTimeLine.setSenderShop(rShopToShop(rPost.getSenderShop()));
    }
    postTimeLine.setTags(realmListStringToListString(rPost.getTags()));
    postTimeLine.setText(rPost.getText());
    postTimeLine.setVideoAddress(rPost.getVideoAddress());
    postTimeLine.setVideoProperties(rPost.getVideoProperties());
    postTimeLine.setViewCount(rPost.getViewCount());
    if (rPost.getProfileTags() != null)
    {
      postTimeLine.setProfileTags(ListPostProfileTagToRealmListPostProfileTag(rPost.getProfileTags()));
    }
    return postTimeLine;
  }

  public static List<RPost> postTimelineToRpostTimelineList(List<Post> list) {
    List<RPost> newpost = new LinkedList<>();
    for (int i = 0; i < list.size(); i++)
    {
      newpost.add(postTimelineToRpostTimeline(list.get(i)));
    }
    return newpost;
  }

  public static List<Post> rPostToPostList(List<RPost> list) {
    List<Post> newpost = new LinkedList<>();
    for (int i = 0; i < list.size(); i++)
    {
      newpost.add(rPostToPost(list.get(i)));
    }
    return newpost;
  }

  public static RComplex complexToRcomplex(Complex rComplex) {
    RComplex complex = new RComplex();
    complex.setAdminId(rComplex.getAdminId());
    complex.setImageAddress(rComplex.getImageAddress());
    complex.setRatesAverage(rComplex.getRatesAverage());
    complex.setAddress(rComplex.getAddress());
    complex.setDescription(rComplex.getDescription());
    complex.setEmail(rComplex.getEmail());
    complex.setFollowers(realmListStringToListString(rComplex.getFollowers()));
    complex.setId(rComplex.getId());
    complex.setLat(rComplex.getLat());
    complex.setLong(rComplex.getLong());
    complex.setManagersId(realmListStringToListString(rComplex.getManagersId()));
    complex.setName(rComplex.getName());
    complex.setPhoneNumber(rComplex.getPhoneNumber());
    complex.setRatesCount(rComplex.getRatesCount());
    complex.setUsername(rComplex.getUsername());
    complex.setTags(realmListStringToListString(rComplex.getTags()));
    complex.setShopsId(realmListStringToListString(rComplex.getShopsId()));
    complex.setRequests(realmListStringToListString(rComplex.getRequests()));
    complex.setBusinessLevel(rComplex.getBusinessLevel());
    complex.setIsFollowing(rComplex.isFollowing());
    return complex;
  }

  public static Complex rComplexToComplex(RComplex rComplex) {
    Complex complex = new Complex();
    complex.setAdminId(rComplex.getAdminId());
    complex.setImageAddress(rComplex.getImageAddress());
    complex.setRatesAverage(rComplex.getRatesAverage());
    complex.setAddress(rComplex.getAddress());
    complex.setDescription(rComplex.getDescription());
    complex.setEmail(rComplex.getEmail());
    complex.setFollowers(realmListStringToListString(rComplex.getFollowers()));
    complex.setId(rComplex.getId());
    complex.setLat(rComplex.getLat());
    complex.setLong(rComplex.getLong());
    complex.setManagersId(realmListStringToListString(rComplex.getManagersId()));
    complex.setName(rComplex.getName());
    complex.setPhoneNumber(rComplex.getPhoneNumber());
    complex.setRatesCount(rComplex.getRatesCount());
    complex.setUsername(rComplex.getUsername());
    complex.setTags(realmListStringToListString(rComplex.getTags()));
    complex.setShopsId(realmListStringToListString(rComplex.getShopsId()));
    complex.setRequests(realmListStringToListString(rComplex.getRequests()));
    complex.setBusinessLevel(rComplex.getBusinessLevel());
    complex.setFollowing(rComplex.getIsFollowing());
    return complex;
  }

  public static RProduct productToRprocut(Product rProduct) {
    RProduct product = new RProduct();
    product.setVideoAddress(rProduct.getVideoAddress());
    product.setTags(realmListStringToListString(rProduct.getTags()));
    product.setDefaultImageAddress(rProduct.getDefaultImageAddress());
    product.setDescription(rProduct.getDescription());
    product.setExtraProperties(rProduct.getExtraProperties());
    product.setId(rProduct.getId());
    product.setImageAddresses(realmListStringToListString(rProduct.getImageAddresses()));
    product.setImageThumbnails(realmListStringToListString(rProduct.getImageThumbnails()));
    product.setLocalNames(rProduct.getLocalNames());
    product.setPdfAddress(rProduct.getPdfAddress());
    product.setPdfThumbnail(rProduct.getPdfThumbnail());
    product.setName(rProduct.getName());
    product.setRatesAverage(rProduct.getRatesAverage());
    product.setRatesCount(rProduct.getRatesCount());
    if (rProduct.getShop() != null)
    {
      product.setShop(shopToRshop(rProduct.getShop()));
    }
    product.setVideoThumbnail(rProduct.getVideoThumbnail());
    product.setInternalCategoryIds(realmListStringToListString(rProduct.getInternalCategoryIds()));
    return product;
  }

  public static RProfile profileToRProfile(Profile rProfile) {
    RProfile profile = new RProfile();
    profile.setBlockList(rProfile.getBlockList());
    profile.setFollowers(realmListStringToListString(rProfile.getFollowers()));
    profile.setId(rProfile.getId());
    profile.setCountryCode(rProfile.getCountryCode());
    profile.setFollowing(realmListStringToListString(rProfile.getFollowing()));
    profile.setBiography(rProfile.getBiography());
    profile.setImageAddress(rProfile.getImageAddress());
    profile.setCoverPhoto(rProfile.getCoverPhoto());
    profile.setIsMan(rProfile.getIsMan());
    profile.setIsOnline(rProfile.getIsOnline());
    profile.setJob(rProfile.getJob());
    profile.setLanguages(realmListStringToListString(rProfile.getLanguages()));
    profile.setLastOnline(rProfile.getLastOnline());
    profile.setLat(rProfile.getLat());
    profile.setLong(rProfile.getLong());
    profile.setMutedProfilesId(realmListStringToListString(rProfile.getMutedProfilesId()));
    profile.setName(rProfile.getName());
    profile.setPhoneNumber(rProfile.getPhoneNumber());
    profile.setUsername(rProfile.getUsername());
    profile.setBirthday(rProfile.getBirthday());
    profile.setPrivateMessaging(rProfile.getPrivateMessaging());
    profile.setIsPrivate(rProfile.getIsPrivate());
    profile.setSettingsBiography(rProfile.getSettingsBiography());
    profile.setSettingsBirthday(rProfile.getSettingsBirthday());
    profile.setSettingsEmail(rProfile.getSettingsEmail());
    profile.setSettingsGender(rProfile.getSettingsGender());
    profile.setSettingsJob(rProfile.getSettingsJob());
    profile.setSettingsLanguages(rProfile.getSettingsLanguages());
    profile.setSettingsLocation(rProfile.getSettingsLocation());
    profile.setSettingsPhoneNumber(rProfile.getSettingsPhoneNumber());
    profile.setSettingsUsername(rProfile.getSettingsUsername());
    profile.setRequested(realmListStringToListString(rProfile.getRequested()));
    profile.setIsOfficial(rProfile.isOfficial());
    profile.setIsFollowing(rProfile.isFollowing());

    return profile;
  }
}
