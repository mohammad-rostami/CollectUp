/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings ("ClassHasNoToStringMethod")
public class RCompactMessage extends RealmObject {

  private String MessageId;
  private String Text;
  private String SenderId;
  private String ChatId;
  private String SenderName;
  private String SenderImage;
  private String SendDateTime;
  private String ContentSize;
  private int ContentType;
  private int MessageStatus;
  private String ContentAddress;
  private String ThumbnailAddress;
  private String FilePath;

  private long Rank;


  @PrimaryKey
  private String Id;

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public String getContentSize() {
    return ContentSize;
  }

  public String getFilePath() {
    return FilePath;
  }

  public void setFilePath(String filePath) {
    FilePath = filePath;
  }

  public void setContentSize(String contentSize) {
    ContentSize = contentSize;
  }


  public String getMessageId() {
    return MessageId;
  }

  public void setMessageId(String messageId) {
    MessageId = messageId;
  }

  public RCompactMessage() {
  }

  public String getChatId() {
    return ChatId;
  }

  public void setChatId(String chatId) {
    ChatId = chatId;
  }

  public String getText() {
    return Text;
  }

  public void setText(String text) {
    Text = text;
  }

  public String getSenderId() {
    return SenderId;
  }

  public void setSenderId(String senderId) {
    SenderId = senderId;
  }

  public String getSenderName() {
    return SenderName;
  }

  public void setSenderName(String senderName) {
    SenderName = senderName;
  }

  public String getSenderImage() {
    return SenderImage;
  }

  public void setSenderImage(String senderImage) {
    SenderImage = senderImage;
  }

  public String getSendDateTime() {
    return SendDateTime;
  }

  public void setSendDateTime(String sendDateTime) {
    SendDateTime = sendDateTime;
  }

  public int getContentType() {
    return ContentType;
  }

  public void setContentType(int contentType) {
    ContentType = contentType;
  }

  public int getMessageStatus() {
    return MessageStatus;
  }

  public void setMessageStatus(int messageStatus) {
    MessageStatus = messageStatus;
  }

  public String getContentAddress() {
    return ContentAddress;
  }

  public void setContentAddress(String contentAddress) {
    ContentAddress = contentAddress;
  }

  public String getThumbnailAddress() {
    return ThumbnailAddress;
  }

  public void setThumbnailAddress(String thumbnailAddress) {
    ThumbnailAddress = thumbnailAddress;
  }

  public long getRank() {
    return Rank;
  }

  public void setRank(long rank) {
    Rank = rank;
  }
}
