/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;
import android.util.Log;

import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.model.StickerPackage;
import com.collect_up.c_up.model.realm.RStickerPackage;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class Logged {

  public static class Search {
    private static int mSearchIn = 1;
    private static int mFilterBy = 0;
    private static double locationLat = 0;
    private static double locationLng = 0;
    private static String locationName;

    public static int getFilterBy() {
      return mFilterBy;
    }

    public static void setFilterBy(int mFilterBy) {
      Search.mFilterBy = mFilterBy;
    }

    public static int getSearchIn() {
      return mSearchIn;
    }

    public static void setSearchIn(int mSearchIn) {
      Search.mSearchIn = mSearchIn;
    }

    public static double getLocationLat() {
      return locationLat;
    }

    public static void setLocationLat(double locationLat) {
      Search.locationLat = locationLat;
    }

    public static double getLocationLng() {
      return locationLng;
    }

    public static void setLocationLng(double locationLng) {
      Search.locationLng = locationLng;
    }

    public static String getLocationName() {
      return locationName;
    }

    public static void setLocationName(String locationName) {
      Search.locationName = locationName;
    }
  }

  public static class General {
    private static String tempTakePhotoFilePath;

    public static String getTempTakePhotoFilePath() {
      return tempTakePhotoFilePath;
    }

    public static void setTempTakePhotoFilePath(String tempTakePhotoFilePath) {
      General.tempTakePhotoFilePath = tempTakePhotoFilePath;
    }
  }

  public static class Models {


    public static CompactMessage ProductMessage;

    public static List<StickerPackage> getUserStickerPackages(Context context) {
      Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
      RealmResults<RStickerPackage> rStickerPackages = realm.where(RStickerPackage.class).findAll();
      List<StickerPackage> list = new ArrayList<>();
      for (int i = 0; i < rStickerPackages.size(); i++)
      {
        list.add(RToNonR.rStickerPackageToStickerPackage(rStickerPackages.get(i)));
      }
      return list;
    }

    public static void removeStickerPackage(Context context, final StickerPackage stickerPackge) {
      Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
      realm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          RealmResults<RStickerPackage> result = realm.where(RStickerPackage.class).equalTo("Id", stickerPackge.getId()).findAll();
          result.clear();

        }
      });
    }

    public static void setUserStickerPackages(Context context, final List<StickerPackage> stickerPackages) {

      Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
      realm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          for (int i = 0; i < stickerPackages.size(); i++)
          {
            realm.copyToRealmOrUpdate(RToNonR.stickerPackageToRStickerPackage(stickerPackages.get(i)));
            Log.i("sepehr", "execute: " + i);

          }
        }
      });
    }

    public static List<Shop> getUserShopList() {
      return Hawk.get("shopList");
    }

    public static void setUserShopList(List<Shop> shopList) {
      Hawk.put("shopList", shopList);
    }

    public static List<Complex> getUserComplexList() {
      return Hawk.get("complexList");
    }

    public static void setUserComplexList(List<Complex> shopList) {
      Hawk.put("complexList", shopList);
    }

    public static Profile getUserProfile() {
      return Hawk.get("profile");
    }

    public static void setUserProfile(Profile userProfile) {
      Hawk.put("profile", userProfile);
    }

    public static Shop getUserShop() {
      return Hawk.get("shop");
    }

    public static void setUserShop(Shop userShop) {
      Hawk.put("shop", userShop);
    }


    public static Complex getUserComplex() {
      return Hawk.get("complex");
    }

    public static void setUserComplex(Complex userComplex) {
      Hawk.put("complex", userComplex);
    }
  }
}
