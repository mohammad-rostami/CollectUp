/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Product implements Parcelable {
  public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
    @Override
    public Product createFromParcel(Parcel source) {
      return new Product(source);
    }

    @Override
    public Product[] newArray(int size) {
      return new Product[size];
    }
  };
  private String DefaultImageAddress;
  private String Description;
  private String ExtraProperties;
  private String Id;
  private List<String> ImageAddresses;
  private List<String> ImageThumbnails;
  private String Name;
  private String PdfAddress;
  private String PdfThumbnail;
  private String RatesAverage;
  private String RatesCount;
  private Shop Shop;
  private List<String> Tags;
  private List<String> InternalCategoryIds;
  private String VideoAddress;
  private String LocalNames;
  private String VideoThumbnail;
  private long ViewCount;


  public Product() {
  }

  protected Product(Parcel in) {
    Id = in.readString();
    Name = in.readString();
    LocalNames = in.readString();
    VideoThumbnail = in.readString();
    DefaultImageAddress = in.readString();
    PdfThumbnail = in.readString();
    ImageAddresses = in.createStringArrayList();
    ImageThumbnails = in.createStringArrayList();
    VideoAddress = in.readString();
    PdfAddress = in.readString();
    ExtraProperties = in.readString();
    Shop = in.readParcelable(com.collect_up.c_up.model.Shop.class.getClassLoader());
    Description = in.readString();
    RatesCount = in.readString();
    RatesAverage = in.readString();
    Tags = in.createStringArrayList();
    InternalCategoryIds = in.createStringArrayList();
    ViewCount = in.readLong();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(Id);
    dest.writeString(Name);
    dest.writeString(LocalNames);
    dest.writeString(VideoThumbnail);
    dest.writeString(DefaultImageAddress);
    dest.writeString(PdfThumbnail);
    dest.writeStringList(ImageAddresses);
    dest.writeStringList(ImageThumbnails);
    dest.writeString(VideoAddress);
    dest.writeString(PdfAddress);
    dest.writeString(ExtraProperties);
    dest.writeParcelable(Shop, 0);
    dest.writeString(Description);
    dest.writeString(RatesCount);
    dest.writeString(RatesAverage);
    dest.writeStringList(Tags);
    dest.writeStringList(InternalCategoryIds);
    dest.writeLong(ViewCount);
  }

  public List<String> getInternalCategoryIds() {
    return InternalCategoryIds;
  }

  public void setInternalCategoryIds(List<String> internalCategoryIds) {
    InternalCategoryIds = internalCategoryIds;
  }

  public String getDefaultImageAddress() {
    return DefaultImageAddress;
  }

  public long getViewCount() {
    return ViewCount;
  }

  public void setViewCount(long viewCount) {
    ViewCount = viewCount;
  }

  public String getThumb() {
    return DefaultImageAddress.replace("images", "thumbnails");
  }

  public void setDefaultImageAddress(String defaultImageAddress) {
    DefaultImageAddress = defaultImageAddress;
  }

  public String getDescription() {
    return Description;
  }

  public void setDescription(String description) {
    Description = description;
  }

  public String getExtraProperties() {
    return ExtraProperties;
  }

  public void setExtraProperties(String extraProperties) {
    ExtraProperties = extraProperties;
  }

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public List<String> getImageAddresses() {
    return ImageAddresses;
  }

  public void setImageAddresses(List<String> imageAddresses) {
    ImageAddresses = imageAddresses;
  }

  public List<String> getImageThumbnails() {
    return ImageThumbnails;
  }

  public void setImageThumbnails(List<String> imageThumbnails) {
    ImageThumbnails = imageThumbnails;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getPdfAddress() {
    return PdfAddress;
  }

  public void setPdfAddress(String pdfAddress) {
    PdfAddress = pdfAddress;
  }

  public String getPdfThumbnail() {
    return PdfThumbnail;
  }

  public void setPdfThumbnail(String pdfThumbnail) {
    PdfThumbnail = pdfThumbnail;
  }

  public String getRatesAverage() {
    return RatesAverage;
  }

  public void setRatesAverage(String ratesAverage) {
    RatesAverage = ratesAverage;
  }

  public String getRatesCount() {
    return RatesCount;
  }

  public void setRatesCount(String ratesCount) {
    RatesCount = ratesCount;
  }

  public com.collect_up.c_up.model.Shop getShop() {
    return Shop;
  }

  public void setShop(com.collect_up.c_up.model.Shop shop) {
    Shop = shop;
  }

  public List<String> getTags() {
    return Tags;
  }

  public void setTags(List<String> tags) {
    Tags = tags;
  }

  public String getVideoAddress() {
    return VideoAddress;
  }

  public void setVideoAddress(String videoAddress) {
    VideoAddress = videoAddress;
  }

  public String getLocalNames() {
    return LocalNames;
  }

  public void setLocalNames(String localNames) {
    LocalNames = localNames;
  }

  public String getVideoThumbnail() {
    return VideoThumbnail;
  }

  public void setVideoThumbnail(String videoThumbnail) {
    VideoThumbnail = videoThumbnail;
  }
}
