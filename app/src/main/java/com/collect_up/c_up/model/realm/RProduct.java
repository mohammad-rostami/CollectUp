/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model.realm;

import com.collect_up.c_up.RealmString;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings ("ClassHasNoToStringMethod")
public class RProduct extends RealmObject {
  public RealmList<RealmString> getInternalCategoryIds() {
    return InternalCategoryIds;
  }

  public void setInternalCategoryIds(RealmList<RealmString> internalCategoryIds) {
    InternalCategoryIds = internalCategoryIds;
  }

  public String getVideoThumbnail() {
    return VideoThumbnail;
  }

  public void setVideoThumbnail(String videoThumbnail) {
    VideoThumbnail = videoThumbnail;
  }

  public String getDefaultImageAddress() {
    return DefaultImageAddress;
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

  public RealmList<RealmString> getImageAddresses() {
    return ImageAddresses;
  }

  public void setImageAddresses(RealmList<RealmString> imageAddresses) {
    ImageAddresses = imageAddresses;
  }

  public RealmList<RealmString> getImageThumbnails() {
    return ImageThumbnails;
  }

  public void setImageThumbnails(RealmList<RealmString> imageThumbnails) {
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

  public RShop getShop() {
    return Shop;
  }

  public void setShop(RShop shop) {
    Shop = shop;
  }

  public RealmList<RealmString> getTags() {
    return Tags;
  }

  public void setTags(RealmList<RealmString> tags) {
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

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  private String DefaultImageAddress;
  private String Description;
  private String ExtraProperties;
  private RealmList<RealmString> ImageAddresses;
  private RealmList<RealmString> ImageThumbnails;
  private String Name;
  private String PdfAddress;
  private String PdfThumbnail;
  private String RatesAverage;
  private String RatesCount;
  private RShop Shop;
  private RealmList<RealmString> Tags;
  private RealmList<RealmString> InternalCategoryIds;
  private String VideoAddress;
  private String LocalNames;
  private String VideoThumbnail;

  @PrimaryKey
  private String Id;

}
