package com.collect_up.c_up.fragments;

import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.model.PostProfileTag;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by collect-up3 on 5/12/2016.
 */
public class FragmentMentions extends BaseFragment {

  public void setProfileToMention(Profile profile) {

  }

  public void setPeople(ArrayList<PostProfileTag> peoples) {
  }

  public void setHastag(String hashTag) {
  }

  public void setShopToMention(Shop shop) {

  }

  protected ArrayList<Object> setMentioned(Profile profile, String input, int cursorPos) {

    int lastAtPos = SepehrUtil.getLastCharPos(Constants.General.MENTION_USER_SIGN, input, cursorPos);
    ArrayList<Object> object = new ArrayList<>();
    String firstString = input.substring(0, lastAtPos);
    String userName = Constants.General.MENTION_USER_SIGN + profile.getUsername();
    String lastString = input.substring(cursorPos, input.length());
    String finalText = firstString + userName + lastString;

    Gson gson = new Gson();
    String serializedProfile = gson.toJson(profile);
    String encryptedProfile = "<mention>" + serializedProfile + "</mention>";

    object.add(userName);
    object.add(encryptedProfile);
    object.add(finalText);
    object.add((firstString + userName).length());
    return object;

  }

  protected ArrayList<Object> setHashtaged(String hashtag, String input, int cursorPos) {

    int lastAtPos = SepehrUtil.getLastCharPos(Constants.General.HASHTAG_SIGN, input, cursorPos);
    ArrayList<Object> object = new ArrayList<>();
    String firstString = input.substring(0, lastAtPos);
    String hashTag = Constants.General.HASHTAG_SIGN + hashtag;
    String lastString = input.substring(cursorPos, input.length());
    String finalText = firstString + hashTag + lastString;


    object.add(hashTag);
    object.add(finalText);
    object.add((firstString + hashTag).length());
    return object;

  }

  protected ArrayList<Object> setMentioned(Shop shop, String input, int cursorPos) {

    int lastAtPos = SepehrUtil.getLastCharPos("\\" + Constants.General.MENTION_BUSINESS_SIGN, input, cursorPos);
    ArrayList<Object> object = new ArrayList<>();
    String firstString = input.substring(0, lastAtPos);
    String userName = Constants.General.MENTION_BUSINESS_SIGN + shop.getUsername();
    String lastString = input.substring(cursorPos, input.length());
    String finalText = firstString + userName + lastString;

    Gson gson = new Gson();
    String serializedProfile = gson.toJson(shop);
    String encryptedProfile = "<mention>" + serializedProfile + "</mention>";

    object.add(userName);
    object.add(encryptedProfile);
    object.add(finalText);
    object.add((firstString + userName).length());
    return object;

  }

  public void setVisibilityMentionFragment(boolean visible) {

  }
}
