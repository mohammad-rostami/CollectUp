/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.model;

public class Notification {


  private String ReceiverId;
  private String Text;
  private String Description;
  private boolean IsSent;
  private Profile Actor;
  private Post Post;
  private Product Product;
  private Complex Complex;
  private Shop Shop;
  private String InsertTime;

  public Event getEvent() {
    return this.Event;
  }

  public void setEvent(Event event) {
    this.Event = event;
  }


  private Event Event;
  private String Id;
  private boolean Accepted;
  private boolean Rejected;


  public boolean isAccepted() {
    return Accepted;
  }

  public void setAccepted(boolean accepted) {
    Accepted = accepted;
  }

  public boolean isRejected() {
    return Rejected;
  }

  public void setRejected(boolean rejected) {
    Rejected = rejected;
  }


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

  public String getDescription() {
    return Description;
  }

  public void setDescription(String description) {
    Description = description;
  }

  public boolean isSent() {
    return IsSent;
  }

  public void setIsSent(boolean isSent) {
    IsSent = isSent;
  }

  public Profile getActor() {
    return Actor;
  }

  public void setActor(Profile actor) {
    Actor = actor;
  }

  public Post getPost() {
    return Post;
  }

  public void setPost(Post post) {
    this.Post = post;
  }


  public Product getProduct() {
    return Product;
  }

  public void setProduct(Product product) {
    Product = product;
  }

  public Complex getComplex() {
    return Complex;
  }

  public void setComplex(Complex complex) {
    Complex = complex;
  }

  public Shop getShop() {
    return Shop;
  }

  public void setShop(Shop shop) {
    Shop = shop;
  }

  public String getInsertTime() {
    return InsertTime;
  }

  public void setInsertTime(String insertTime) {
    InsertTime = insertTime;
  }


  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }
}
