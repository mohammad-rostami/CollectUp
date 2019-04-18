package com.collect_up.c_up.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by collect-up3 on 10/31/2016.
 */

public class CompactMessage implements Parcelable {

  private String MessageId;
  private String Id;
  private String Text;
  private String ReceiverId;
  private String SenderId;
  private String SenderName;
  private String SenderImage;
  private String ChatId;
  private String SendDateTime;
  private int ContentType;
  private String ContentSize;
  private int MessageStatus;
  private String ContentAddress;
  private String FilePath;
  private String ThumbnailAddress;
  private long Rank;

  public String getId() {
    return Id;
  }

  public void setSender(Profile senderProfile) {
    setSenderId(senderProfile.getId());
    setSenderImage(senderProfile.getImageAddress());
    setSenderName(senderProfile.getName());
  }

  public long getRank() {
    return Rank;
  }

  public void setRank(long rank) {
    Rank = rank;
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

  public String getSenderImage() {
    return SenderImage;
  }

  public void setSenderImage(String senderImage) {
    SenderImage = senderImage;
  }

  public String getSenderName() {
    return SenderName;
  }

  public void setSenderName(String senderName) {
    SenderName = senderName;
  }

  public String getMessageId() {
    return MessageId;
  }

  public void setMessageId(String messageId) {
    MessageId = messageId;
  }

  public CompactMessage() {
  }

  protected CompactMessage(Parcel in) {
    MessageId = in.readString();
    Text = in.readString();
    ReceiverId = in.readString();
    SenderId = in.readString();
    SenderName = in.readString();
    SenderImage = in.readString();
    ChatId = in.readString();
    SendDateTime = in.readString();
    ContentSize = in.readString();
    ContentType = in.readInt();
    MessageStatus = in.readInt();
    ContentAddress = in.readString();
    ThumbnailAddress = in.readString();
  }

  public static final Creator<CompactMessage> CREATOR = new Creator<CompactMessage>() {
    @Override
    public CompactMessage createFromParcel(Parcel in) {
      return new CompactMessage(in);
    }

    @Override
    public CompactMessage[] newArray(int size) {
      return new CompactMessage[size];
    }
  };

  public String getReceiverId() {
    return ReceiverId;
  }

  public void setReceiverId(String receiverId) {
    ReceiverId = receiverId;
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

  public String getChatId() {
    return ChatId;
  }

  public void setChatId(String chatId) {
    ChatId = chatId;
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

  public String getTextType()

  {
    if (getContentType() == EnumMessageContentType.Image)
    {
      return "IMAGE";
    } else if (getContentType() == EnumMessageContentType.Sticker)
    {
      return "STICKER";

    } else if (getContentType() == EnumMessageContentType.File)
    {
      return "FILE";

    } else if (getContentType() == EnumMessageContentType.Audio)
    {
      return "AUDIO";

    } else if (getContentType() == EnumMessageContentType.Video)
    {
      return "VIDEO";

    } else if (getContentType() == EnumMessageContentType.Location)
    {

      return "LOCATION";

    }
    return getText();
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


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(MessageId);
    parcel.writeString(Text);
    parcel.writeString(ReceiverId);
    parcel.writeString(SenderId);
    parcel.writeString(SenderName);
    parcel.writeString(SenderImage);
    parcel.writeString(ChatId);
    parcel.writeString(SendDateTime);
    parcel.writeString(ContentSize);
    parcel.writeInt(ContentType);
    parcel.writeInt(MessageStatus);
    parcel.writeString(ContentAddress);
    parcel.writeString(ThumbnailAddress);
  }
}
