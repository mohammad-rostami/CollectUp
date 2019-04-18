package com.collect_up.c_up.model;

import android.content.Context;

import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.orhanobut.hawk.Hawk;

import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 7/16/2016.
 */
public class Initial {

  private Profile Profile;
  private Shop Shop;
  private Complex Complex;
  private List<StickerPackage> StickerPackages;
  private static Initial instance = new Initial();

  public void initialize(final Context context) {
    HttpClient.get(String.format(Constants.Server.Profile.GET_INITIAL_DATA), new AsyncHttpResponser(context) {
      @Override
      public void onSuccess(int statusCode,
                            Header[] headers,
                            byte[] responseBody) {
        Initial initial = GsonParser.getObjectFromGson(responseBody, Initial.class);
        Profile profile = initial.getProfile();
        Logged.Models.setUserProfile(profile);
        Hawk.put("profileId", profile.getId());

        List<StickerPackage> stickerPackage = initial.getStickerPackages() == null ? new LinkedList<StickerPackage>() : initial.getStickerPackages();
        Logged.Models.setUserStickerPackages(context, stickerPackage);

        Shop shop = initial.getShop();
        Logged.Models.setUserShop(shop);

        Complex complex = initial.getComplex();
        Logged.Models.setUserComplex(complex);

        List<Shop> shopList = initial.getShopList();
        Logged.Models.setUserShopList(shopList);

        List<Complex> complexList = initial.getComplexList();
        Logged.Models.setUserComplexList(complexList);
      }

      @Override
      public void onFailure(int statusCode,
                            Header[] headers,
                            byte[] responseBody,
                            Throwable error) {
        super.onFailure(statusCode, headers, responseBody, error);

      }
    });

  }

  //Get the only object available
  public static Initial getInstance() {
    return instance;
  }

  public com.collect_up.c_up.model.Profile getProfile() {
    return Profile;
  }

  public void setProfile(com.collect_up.c_up.model.Profile profile) {
    Profile = profile;
  }

  public com.collect_up.c_up.model.Shop getShop() {
    return Shop;
  }

  public void setShop(com.collect_up.c_up.model.Shop shop) {
    Shop = shop;
  }

  public com.collect_up.c_up.model.Complex getComplex() {
    return Complex;
  }

  public void setComplex(com.collect_up.c_up.model.Complex complex) {
    Complex = complex;
  }

  public List<com.collect_up.c_up.model.Shop> getShopList() {
    return ShopList;
  }

  public void setShopList(List<com.collect_up.c_up.model.Shop> shopList) {
    ShopList = shopList;
  }

  public List<com.collect_up.c_up.model.Complex> getComplexList() {
    return ComplexList;
  }

  public void setComplexList(List<com.collect_up.c_up.model.Complex> complexList) {
    ComplexList = complexList;
  }

  public List<StickerPackage> getStickerPackages() {
    return StickerPackages;
  }

  public void setStickerPackages(List<StickerPackage> stickerPackages) {
    this.StickerPackages = stickerPackages;
  }

  List<Shop> ShopList;
  List<Complex> ComplexList;
}
