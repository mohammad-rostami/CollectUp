/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;


public class Comment {
  private String Id;
  private String Text;
  private Profile Sender;
  private String InsertTime;
  private String PostId;
  private int MentionMode;

  public int getMentionMode() {
    return MentionMode;
  }

  public void setMentionMode(int mentionMode) {
    MentionMode = mentionMode;
  }

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public String getText() {
    return Text;
  }

  public void setText(String text) {
    Text = text;
  }

  public Profile getSender() {
    return Sender;
  }

  public void setSender(Profile sender) {
    Sender = sender;
  }

  public String getInsertTime() {
    return InsertTime;
  }

  public void setInsertTime(String insertTime) {
    InsertTime = insertTime;
  }

  public String getPostId() {
    return PostId;
  }

  public void setPostId(String postId) {
    PostId = postId;
  }
}
