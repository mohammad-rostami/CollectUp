package com.collect_up.c_up.model.realm;

import com.collect_up.c_up.RealmString;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by collect-up3 on 8/7/2016.
 */
@SuppressWarnings ("ClassHasNoToStringMethod")

public class RStickerPackage extends RealmObject {
  private String PackageName;
  private String ThumbnailAddress;
  private int Count;

  @PrimaryKey
  private String Id;

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public RealmList<RealmString> getStickers() {
    return Stickers;
  }

  public void setStickers(RealmList<RealmString> stickers) {
    Stickers = stickers;
  }

  private RealmList<RealmString> Stickers;

  public String getPackageName() {
    return PackageName;
  }

  public void setPackageName(String packageName) {
    PackageName = packageName;
  }

  public String getThumbnailAddress() {
    return ThumbnailAddress;
  }

  public void setThumbnailAddress(String thumbnailAddress) {
    ThumbnailAddress = thumbnailAddress;
  }

  public int getCount() {
    return Count;
  }

  public void setCount(int count) {
    Count = count;
  }


}
