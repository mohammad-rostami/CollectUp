/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;

public class Event {
    private String Title;
    private String Message;
    private String StartDateTime;
    private String EndDateTime;
    private String InsertTime;
    private String ShopId;
    private String ComplexId;
    private String Id;

    private String ShopImage;
    private String ShopName;
    private String ShopUserName;
    private String ShopLat;
    private String ShopLong;


    public String getComplexId() {
        return ComplexId;
    }

    public void setComplexId(String complexId) {
        ComplexId = complexId;
    }

    public String getId() {
        return Id;
    }

    public String getShopId() {
        return ShopId;
    }

    public String getTitle() {
        return Title;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setShopId(String shopId) {
        ShopId = shopId;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getStartDateTime() {
        return StartDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        StartDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return EndDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        EndDateTime = endDateTime;
    }

    public String getInsertTime() {
        return InsertTime;
    }

    public void setInsertTime(String insertTime) {
        InsertTime = insertTime;
    }


    public String getShopImage() {
        return ShopImage;
    }

    public void setShopImage(String shopImage) {
        ShopImage = shopImage;
    }

    public String getShopName() {
        return ShopName;
    }

    public void setShopName(String shopName) {
        ShopName = shopName;
    }

    public String getShopUserName() {
        return ShopUserName;
    }

    public void setShopUserName(String shopUserName) {
        ShopUserName = shopUserName;
    }

    public String getShopLat() {
        return ShopLat;
    }

    public void setShopLat(String shopLat) {
        ShopLat = shopLat;
    }

    public String getShopLong() {
        return ShopLong;
    }

    public void setShopLong(String shopLong) {
        ShopLong = shopLong;
    }
}
