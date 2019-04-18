package com.yalantis.ucrop;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

/**
 * this class is a builder class for this module
 */

public class Picker {

  private int ImageLimit = -1;
  private int VideoLimit = -1;

  private int Minimum = -1;
  private boolean onlyImages = false;

  private int hAspect = -1;
  private int wAspect = -1;

  private boolean Multiple = false;
  private boolean FreeStyle = false;

  private Fragment fragment;

  private PickerBuilder builder;

  public Picker(PickerBuilder builder) {

    ImageLimit = builder.ImageLimit;
    VideoLimit = builder.VideoLimit;
    Minimum = builder.Minimum;
    onlyImages = builder.onlyImages;
    hAspect = builder.hAspect;
    wAspect = builder.wAspect;
    fragment = builder.context;
    Multiple = builder.Multiple;
    FreeStyle = builder.FreeStyle;
    this.builder = builder;

  }

  public void start() {
    MultiplePermissionsListener storagePermission = DialogOnAnyDeniedMultiplePermissionsListener.Builder
      .withContext(fragment.getContext())
      .withTitle("Storage permission")
      .withMessage(R.string.permission_storage)
      .withButtonText(android.R.string.ok)
      .build();
    MultiplePermissionsListener basePermission = new MultiplePermissionsListener() {
      @Override
      public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (report.areAllPermissionsGranted())
        {
          Intent intent = new Intent(fragment.getActivity(), MainActivity.class);
          intent.putExtra("data", builder);

          if (fragment.getActivity() instanceof Activity)
          {
            fragment.startActivityForResult(intent, 1);
          } else
          {
            Log.e("Picker", "mContext should be an instanceof Activity.");
          }
        }
      }

      @Override
      public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
        token.continuePermissionRequest();

      }
    };

    Dexter.withActivity(fragment.getActivity())
      .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
      .withListener(new CompositeMultiplePermissionsListener(storagePermission, basePermission))
      .check();

  }

  public static class PickerBuilder implements Parcelable {

    private int ImageLimit = -1;
    private int VideoLimit = -1;

    private int Minimum = -1;
    private boolean onlyImages = false;

    private int hAspect = -1;
    private int wAspect = -1;

    private Fragment context;

    private boolean Multiple = false;
    private boolean FreeStyle = false;

    public PickerBuilder(Fragment context, int imageLimit, int videoLimit) {
      ImageLimit = imageLimit;
      VideoLimit = videoLimit;
      this.context = context;
    }


    protected PickerBuilder(Parcel in) {
      ImageLimit = in.readInt();
      VideoLimit = in.readInt();
      Minimum = in.readInt();
      onlyImages = in.readByte() != 0;
      hAspect = in.readInt();
      wAspect = in.readInt();
      Multiple = in.readByte() != 0;
      FreeStyle = in.readByte() != 0;
    }

    public static final Creator<PickerBuilder> CREATOR = new Creator<PickerBuilder>() {
      @Override
      public PickerBuilder createFromParcel(Parcel in) {
        return new PickerBuilder(in);
      }

      @Override
      public PickerBuilder[] newArray(int size) {
        return new PickerBuilder[size];
      }
    };

    public PickerBuilder setMultiple(boolean multiple) {
      Multiple = multiple;
      return this;
    }

    public PickerBuilder setFreeStyle(boolean freeStyle) {
      FreeStyle = freeStyle;
      return this;
    }

    public PickerBuilder setMinimum(int minimum) {
      Minimum = minimum;
      return this;
    }

    public PickerBuilder setOnlyImages(boolean onlyImages) {
      this.onlyImages = onlyImages;
      return this;
    }

    public PickerBuilder setAspectRatio(int width, int height) {
      this.hAspect = height;
      this.wAspect = width;
      return this;
    }

    public boolean isMultiple() {
      return Multiple;
    }

    public boolean isFreeStyle() {
      return FreeStyle;
    }

    public int getImageLimit() {
      return ImageLimit;
    }

    public int getVideoLimit() {
      return VideoLimit;
    }

    public int getMinimum() {
      return Minimum;
    }

    public boolean isOnlyImages() {
      return onlyImages;
    }

    public int gethAspect() {
      return hAspect;
    }

    public int getwAspect() {
      return wAspect;
    }

    public Fragment getContext() {
      return context;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
      parcel.writeInt(ImageLimit);
      parcel.writeInt(VideoLimit);
      parcel.writeInt(Minimum);
      parcel.writeByte((byte) (onlyImages ? 1 : 0));
      parcel.writeInt(hAspect);
      parcel.writeInt(wAspect);
      parcel.writeByte((byte) (Multiple ? 1 : 0));
      parcel.writeByte((byte) (FreeStyle ? 1 : 0));
    }
  }

}
