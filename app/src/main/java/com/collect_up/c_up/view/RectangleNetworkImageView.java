/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.lang.reflect.InvocationTargetException;

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class RectangleNetworkImageView extends ImageView {
  public boolean mCircled;
  @ColorRes
  private int mBorderColor = Color.WHITE;
  private float mBorderWidth = 0F;
  private Bitmap mLocalBitmap;
  private boolean mShowLocal;
  @DrawableRes
  private int mBackgroundResource;
  private boolean mAsCircle = true;
  private Drawable mDrawableBeforeDetached;
  private Uri imageUri;

  public RectangleNetworkImageView(Context context) {
    this(context, null);
    init();
  }

  public RectangleNetworkImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
    initAttrs(attrs);
    init();
  }

  public RectangleNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initAttrs(attrs);
    init();
  }

  @Override
  public void setImageResource(int resId) {
    if (resId != 0)
    {
      mShowLocal = true;
      this.mLocalBitmap = BitmapFactory.decodeResource(getResources(), resId);
      requestLayout();
    }
  }

  @Override
  public void setImageURI(Uri uri) {
    if (uri != null)
    {
      mShowLocal = true;
    }
    this.mLocalBitmap = BitmapFactory.decodeFile(Utils.getPath(getContext(), uri));
    imageUri = uri;
    super.setImageURI(uri);
  }

  public Uri getImageURI() {
    return imageUri;
  }

  public void setAsCircle(boolean asCircle) {
    mAsCircle = asCircle;
  }

  private void init() {
    if (mBackgroundResource != 0)
    {
      setBackgroundResource(mBackgroundResource);
    }
  }

  @Override
  public void setBackgroundResource(int resid) {
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resid);
    if (bitmap != null)
    {
      mShowLocal = true;
    }
    this.mLocalBitmap = bitmap;
    requestLayout();
  }

  private void initAttrs(AttributeSet attrs) {
    TypedArray customTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircledNetworkImageView);
    int[] androidAttrs = {android.R.attr.src};

    TypedArray androidTypedArray = getContext().obtainStyledAttributes(attrs, androidAttrs);

    mBorderColor = customTypedArray.getResourceId(R.styleable.CircledNetworkImageView_cniv_borderColor, Color.WHITE);
    mBorderWidth = customTypedArray.getDimension(R.styleable.CircledNetworkImageView_cniv_borderWidth, 0F);
    mAsCircle = customTypedArray.getBoolean(R.styleable.CircledNetworkImageView_cniv_asCircle, true);
    mBackgroundResource = androidTypedArray.getResourceId(0, 0);

    androidTypedArray.recycle();
    customTypedArray.recycle();
  }

  /**
   * Method used to blue_circle a bitmap.
   *
   * @param bitmap The bitmap to blue_circle
   * @return The circled bitmap
   */
  public Bitmap getCircleBitmap(Bitmap bitmap) {
    if (bitmap != null)
    {
      int size = Math.min(bitmap.getWidth(), bitmap.getHeight());

      Bitmap canvasBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
      BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
      Paint paint = new Paint();
      paint.setAntiAlias(true);
      paint.setShader(shader);

      Canvas canvas = new Canvas(canvasBitmap);
      float radius = ((float) size) / 2f;
      canvas.drawCircle(size / 2, size / 2, radius, paint);
      if (mBorderWidth != 0F)
      {
        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(getResources().getColor(mBorderColor));
        borderPaint.setStrokeWidth(mBorderWidth);
        canvas.drawCircle(size / 2, size / 2, radius - mBorderWidth / 2, borderPaint);
      }

      return canvasBitmap;
    }
    return null;
  }

  /**
   * We want to make sure that the ImageView has the same height and width
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (mAsCircle)
    {
      Drawable drawable = getDrawable();
      if (drawable != null)
      {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int diw = drawable.getIntrinsicWidth();
        if (diw > 0)
        {
          int height = width * drawable.getIntrinsicHeight() / diw;
          setMeasuredDimension(width, height);
        } else
        {
          super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
      } else
      {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      }
    } else
    {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    if (mAsCircle)
    {
      //Let's blue_circle the image
      Drawable drawable = getDrawable();
      if (!mCircled && drawable != null)
      {

        //We use reflection here in case that the drawable isn't a
        //BitmapDrawable but it contains a public getBitmap method.
        try
        {
          Bitmap bitmap = (Bitmap) drawable.getClass().getMethod("getBitmap").invoke(drawable);
          bitmap = getCircleBitmap(bitmap);

          setImageBitmap(bitmap);
        } catch (IllegalAccessException ignored)
        {
        } catch (InvocationTargetException ignored)
        {
        } catch (NoSuchMethodException ignored)
        {
        }

        //Mark as circled even if it failed, because if it fails once,
        //It will fail again.
        mCircled = true;
      }
    }

    super.onDraw(canvas);
  }

  @Override
  protected void onDetachedFromWindow() {
    mCircled = false;
    mDrawableBeforeDetached = getDrawable();
    super.onDetachedFromWindow();
  }

  public void setLocalImageBitmap(Bitmap bitmap) {
    if (bitmap != null)
    {
      mShowLocal = true;
    }
    this.mLocalBitmap = bitmap;
    requestLayout();
  }

  @Override
  protected void onAttachedToWindow() {
    if (mDrawableBeforeDetached != null)
    {
      setImageDrawable(mDrawableBeforeDetached);
    }
    super.onAttachedToWindow();
  }

  // This is a fix for clicking on the load more into comments activities
  public void clearDrawableBeforeDetached() {
    mDrawableBeforeDetached = null;
  }

  public void setLocalImagePath(String path) {
    if (!TextUtils.isEmpty(path))
    {
      mShowLocal = true;
    }
    this.mLocalBitmap = BitmapFactory.decodeFile(path);
    requestLayout();
  }

  @Override
  public void setImageBitmap(Bitmap bm) {
    mCircled = false;
    super.setImageBitmap(bm);
  }

  public void setImageUrl(String url, ImageLoader imageLoader) {
    mShowLocal = false;
    imageLoader.loadImage(url, new SimpleImageLoadingListener() {
      @Override
      public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        setImageBitmap(loadedImage);
      }
    });
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (mShowLocal)
    {
      Bitmap bitmap = mLocalBitmap;
      if (mAsCircle)
      {
        bitmap = getCircleBitmap(mLocalBitmap);
      }
      setImageBitmap(bitmap);
    }
  }
}