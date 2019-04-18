package com.yalantis.ucrop.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 */
public class MediaContent implements Parcelable {

  public final static int IS_IMAGE = 0;
  public final static int IS_VIDEO = 1;

  protected MediaContent(Parcel in) {
    uri = in.readParcelable(Uri.class.getClassLoader());
    type = in.readInt();
    isCamera = in.readByte() != 0;
    isChecked = in.readByte() != 0;
    time = in.readLong();
  }

  public static final Creator<MediaContent> CREATOR = new Creator<MediaContent>() {
    @Override
    public MediaContent createFromParcel(Parcel in) {
      return new MediaContent(in);
    }

    @Override
    public MediaContent[] newArray(int size) {
      return new MediaContent[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeParcelable(uri, i);
    parcel.writeInt(type);
    parcel.writeByte((byte) (isCamera ? 1 : 0));
    parcel.writeByte((byte) (isChecked ? 1 : 0));
    parcel.writeLong(time);
  }

  public enum CameraTypes {
    Image, Video
  }

  private Uri uri;
  private int type;
  private boolean isCamera = false;
  private CameraTypes cameraTypes;
  private boolean isChecked = false;
  private long time;

  public void setTime(long time) {
    this.time = time;
  }

  public MediaContent(Uri uri, int type, boolean isCamera, CameraTypes cameraTypes, boolean isChecked, long time) {
    this.uri = uri;
    this.type = type;
    this.isCamera = isCamera;
    this.cameraTypes = cameraTypes;
    this.isChecked = isChecked;
    this.time = time;
  }

  public long getTime() {
    return time;
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setChecked(boolean checked) {
    isChecked = checked;
  }

  public MediaContent(Uri uri, int type) {
    this.uri = uri;
    this.type = type;
  }

  public MediaContent(Uri uri, int type, boolean isCamera, CameraTypes cameraTypes) {
    this.uri = uri;
    this.type = type;
    this.isCamera = isCamera;
    this.cameraTypes = cameraTypes;
  }

  public boolean isCamera() {
    return isCamera;
  }

  public void setCamera(boolean camera) {
    isCamera = camera;
  }

  public CameraTypes getCameraTypes() {
    return cameraTypes;
  }

  public void setCameraTypes(CameraTypes cameraTypes) {
    this.cameraTypes = cameraTypes;
  }


  public Uri getUri() {
    return uri;
  }

  public int getType() {
    return type;
  }

}
