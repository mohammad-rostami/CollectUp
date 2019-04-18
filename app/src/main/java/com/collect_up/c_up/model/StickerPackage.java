package com.collect_up.c_up.model;

import java.util.List;

/**
 * Created by collect-up3 on 8/7/2016.
 */
public class StickerPackage {
  private String PackageName;
  private String ThumbnailAddress;
  private int Count;
  private String Id;

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public List<String> getStickers() {
    return Stickers;
  }

  public void setStickers(List<String> stickers) {
    Stickers = stickers;
  }

  private List<String> Stickers;

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
