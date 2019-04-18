package com.collect_up.c_up.util;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by collect-up3 on 4/25/2016.
 */
public class ComplexNetworkImageView extends ImageView {
  private static final Bitmap.Config BITMAP_CONFIG;
  private static final int COLORDRAWABLE_DIMENSION = 2;
  private final RectF mDrawableRect = new RectF();
  private final RectF mBorderRect = new RectF();
  private final Matrix mShaderMatrix = new Matrix();
  private final Paint mBitmapPaint = new Paint();
  private Bitmap mBitmap;
  private BitmapShader mBitmapShader;
  private int mBitmapWidth;
  private int mBitmapHeight;
  private float mDrawableRadius;
  private boolean mReady;
  private boolean mSetupPending;
  private boolean mShouldAnimate;
  private int mAlphaAnimatorDuration;
  private BackgroundDrawType mBackgroundDrawType;
  private float mRectRoundCorner;

  public ComplexNetworkImageView(Context context) {
    super(context);
    this.mBackgroundDrawType = BackgroundDrawType.CIRCLE;
    this.mRectRoundCorner = 0.0F;
    this.init();
  }

  private void init() {
    this.setScaleType(ScaleType.CENTER_CROP);
    this.mReady = true;
    if (this.mSetupPending)
    {
      this.setup();
      this.mSetupPending = false;
    }

  }

  private void setup() {
    if (!this.mReady)
    {
      this.mSetupPending = true;
    } else if (this.getWidth() != 0 || this.getHeight() != 0)
    {
      if (this.mBitmap == null)
      {
        this.invalidate();
      } else
      {
        this.mBitmapShader = new BitmapShader(this.mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        this.mBitmapPaint.setAntiAlias(true);
        this.mBitmapPaint.setShader(this.mBitmapShader);
        this.mBitmapHeight = this.mBitmap.getHeight();
        this.mBitmapWidth = this.mBitmap.getWidth();
        this.mBorderRect.set(0.0F, 0.0F, (float) this.getWidth(), (float) this.getHeight());
        this.mDrawableRect.set(this.mBorderRect);
        this.mDrawableRadius = Math.min(this.mDrawableRect.height() / 2.0F, this.mDrawableRect.width() / 2.0F);
        this.updateShaderMatrix();
        this.invalidate();
      }
    }
  }

  private void updateShaderMatrix() {
    float dx = 0.0F;
    float dy = 0.0F;
    this.mShaderMatrix.set((Matrix) null);
    float scale;
    if ((float) this.mBitmapWidth * this.mDrawableRect.height() > this.mDrawableRect.width() * (float) this.mBitmapHeight)
    {
      scale = this.mDrawableRect.height() / (float) this.mBitmapHeight;
      dx = (this.mDrawableRect.width() - (float) this.mBitmapWidth * scale) * 0.5F;
    } else
    {
      scale = this.mDrawableRect.width() / (float) this.mBitmapWidth;
      dy = (this.mDrawableRect.height() - (float) this.mBitmapHeight * scale) * 0.5F;
    }

    this.mShaderMatrix.setScale(scale, scale);
    this.mShaderMatrix.postTranslate((float) ((int) (dx + 0.5F)) + this.mDrawableRect.left, (float) ((int) (dy + 0.5F)) + this.mDrawableRect.top);
    this.mBitmapShader.setLocalMatrix(this.mShaderMatrix);
  }

  public ComplexNetworkImageView(Context context, BackgroundDrawType backgroundDrawType, float rectRoundCorner) {
    super(context);
    this.mBackgroundDrawType = BackgroundDrawType.CIRCLE;
    this.mRectRoundCorner = 0.0F;
    this.mBackgroundDrawType = backgroundDrawType;
    this.mRectRoundCorner = rectRoundCorner;
    this.init();
  }

  public void setAdjustViewBounds(boolean adjustViewBounds) {
    if (adjustViewBounds)
    {
      throw new IllegalArgumentException("adjustViewBounds not supported.");
    }
  }

  public void setImageResource(@DrawableRes int resId) {
    super.setImageResource(resId);
    this.mBitmap = this.getBitmapFromDrawable(this.getDrawable());
    this.setup();
  }

  private Bitmap getBitmapFromDrawable(Drawable drawable) {
    if (drawable == null)
    {
      return null;
    } else if (drawable instanceof BitmapDrawable)
    {
      return ((BitmapDrawable) drawable).getBitmap();
    } else
    {
      try
      {
        Bitmap e;
        if (drawable instanceof ColorDrawable)
        {
          e = Bitmap.createBitmap(2, 2, BITMAP_CONFIG);
        } else
        {
          e = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
        }

        Canvas canvas = new Canvas(e);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return e;
      } catch (Exception var4)
      {
        var4.printStackTrace();
        return null;
      }
    }
  }

  public void setImageBitmap(Bitmap bm) {
    super.setImageBitmap(bm);
    this.mBitmap = bm;
    this.setup();
    if (this.mShouldAnimate)
    {
      ObjectAnimator.ofFloat(this, "alpha", new float[] {0.0F, 1.0F}).setDuration((long) this.mAlphaAnimatorDuration).start();
    }

  }

  protected void onDraw(Canvas canvas) {
    if (this.mBitmap != null)
    {
      if (this.mBackgroundDrawType == BackgroundDrawType.CIRCLE)
      {
        canvas.drawCircle((float) this.getWidth() / 2.0F, (float) this.getHeight() / 2.0F, this.mDrawableRadius, this.mBitmapPaint);
      } else if (this.mBackgroundDrawType == BackgroundDrawType.RECTANGLE)
      {
        if (this.mRectRoundCorner == 0.0F)
        {
          canvas.drawRect(this.mBorderRect, this.mBitmapPaint);
        } else
        {
          canvas.drawRoundRect(this.mBorderRect, this.mRectRoundCorner, this.mRectRoundCorner, this.mBitmapPaint);
        }
      }

    }
  }

  public void setImageUrl(String url, ImageLoader imageLoader, boolean animate) {
    this.mShouldAnimate = animate;
    //url, 0, 0);
    imageLoader.loadImage(url, new SimpleImageLoadingListener() {
      @Override
      public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        setImageBitmap(loadedImage);
      }
    });
  }

  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    this.setup();
  }

  public ComplexNetworkImageView setAlphaAnimatorDuration(int alphaAnimatorDuration) {
    this.mAlphaAnimatorDuration = alphaAnimatorDuration;
    return this;
  }

  static
  {
    BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
  }
}
