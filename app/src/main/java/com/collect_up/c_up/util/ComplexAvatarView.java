package com.collect_up.c_up.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by collect-up3 on 4/25/2016.
 */
public class ComplexAvatarView extends FrameLayout {
  private ComplexNetworkImageView mNetworkImageView;
  private TextView mTextView;
  private Paint mPaint;
  @ColorInt
  private int mTextColor;
  private String mText;
  private int mTextGravity;
  private boolean mTextAllCaps;
  @ColorInt
  private int mViewBackgroundColor;
  private int mAlphaAnimatorDuration;
  private BackgroundDrawType mBackgroundDrawType;
  private float mRectRoundCorner;
  private RectF mRectBounds;
  private boolean mRandomizedBackgroundColor;
  private ImageLoader mImageLoader;
  private float mTextSize;
  @StyleRes
  private int mTextAppearance;
  private int mBorderWidth;
  @ColorInt
  private int mBorderColor;
  private boolean withAnimate;

  public ComplexAvatarView(Context context) {
    this(context, (AttributeSet) null);
  }

  public ComplexAvatarView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ComplexAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.mPaint = new Paint();
    this.mViewBackgroundColor = -1;
    this.mRectBounds = new RectF();
    this.mImageLoader = null;
    this.mTextSize = 14.0F;
    this.initAttrs(attrs);
    this.init();
    this.initPaint();
  }

  private void initAttrs(AttributeSet attrs) {
    TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.ComplexAvatarView);

    try
    {
      this.mTextColor = typedArray.getColor(R.styleable.ComplexAvatarView_cav_textColor, -16777216);
      this.mText = typedArray.getString(R.styleable.ComplexAvatarView_cav_text);
      this.mTextGravity = typedArray.getInt(R.styleable.ComplexAvatarView_cav_textGravity, 17);
      this.mTextSize = (float) typedArray.getDimensionPixelSize(R.styleable.ComplexAvatarView_cav_textSize, 24);
      this.mTextAllCaps = typedArray.getBoolean(R.styleable.ComplexAvatarView_cav_textAllCaps, true);
      this.mViewBackgroundColor = typedArray.getColor(R.styleable.ComplexAvatarView_cav_backgroundColor, -1);
      this.mAlphaAnimatorDuration = typedArray.getInteger(R.styleable.ComplexAvatarView_cav_alphaAnimatorDuration, 500);
      this.mBackgroundDrawType = BackgroundDrawType.values()[typedArray.getInt(R.styleable.ComplexAvatarView_cav_backgroundDrawType, BackgroundDrawType.CIRCLE.ordinal())];
      this.mRectRoundCorner = typedArray.getFloat(R.styleable.ComplexAvatarView_cav_roundCorner, 0.0F);
      this.mRandomizedBackgroundColor = typedArray.getBoolean(R.styleable.ComplexAvatarView_cav_randomizedBackgroundColor, false);
      this.mTextAppearance = typedArray.getResourceId(R.styleable.ComplexAvatarView_cav_textAppearance, -1);
      this.mBorderWidth = typedArray.getDimensionPixelSize(R.styleable.ComplexAvatarView_cav_borderWidth, 0);
      this.mBorderColor = typedArray.getColor(R.styleable.ComplexAvatarView_cav_borderColor, this.defaultBorderColor());
    } finally
    {
      typedArray.recycle();
    }

  }

  private void init() {
    this.setWillNotDraw(false);
    this.mNetworkImageView = new ComplexNetworkImageView(this.getContext(), this.mBackgroundDrawType, this.mRectRoundCorner);
    this.mNetworkImageView.setLayoutParams(this.generateDefaultLayoutParams());
    this.mNetworkImageView.setAlphaAnimatorDuration(this.mAlphaAnimatorDuration);
    this.mNetworkImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    this.addTextView();
    this.addView(this.mNetworkImageView, this.getChildCount());
  }

  private void initPaint() {
    if (this.mRandomizedBackgroundColor)
    {
      this.mPaint.setColor(MaterialColorPalette.randomColor());
    } else if (this.mViewBackgroundColor != -1)
    {
      this.mPaint.setColor(this.mViewBackgroundColor);
    } else
    {
      this.mPaint.setColor(MaterialColorPalette.randomColor());
    }

    this.mPaint.setAntiAlias(true);
    this.mPaint.setStyle(Paint.Style.FILL);
  }

  private int defaultBorderColor() {
    return this.getResources().getIdentifier("accent", "color", this.getContext().getPackageName()) != 0 ? this.getResources().getIdentifier("accent", "color", this.getContext().getPackageName()) : (this.getResources().getIdentifier("colorAccent", "color", this.getContext().getPackageName()) != 0 ? this.getResources().getIdentifier("colorAccent", "color", this.getContext().getPackageName()) : 17170445);
  }

  private void addTextView() {
    if (this.mTextView != null && this.mTextView.getParent() != null)
    {
      this.removeView(this.mTextView);
    }

    if (!TextUtils.isEmpty(this.mText))
    {
      this.mTextView = new TextView(this.getContext());
      this.mTextView.setLayoutParams(this.generateDefaultLayoutParams());
      this.mTextView.setGravity(17);
      this.mTextView.setText(this.getTwoLetters(this.mText));
      this.mTextView.setGravity(this.mTextGravity);
      this.mTextView.setAllCaps(this.mTextAllCaps);
      this.textAppearance(this.mTextAppearance);
      this.mTextView.setTextColor(this.mTextColor);
      this.mTextView.setTextSize(0, this.mTextSize);
      this.addView(this.mTextView, 0);
    }

  }

  private String getTwoLetters(String text) {
    if (text.isEmpty())
    {
      return null;
    } else if (text.length() == 1)
    {
      return text;
    } else if (text.contains(" "))
    {
      String[] strings = text.trim().split(" ");
      return Character.toString(strings[0].charAt(0)) + (strings.length > 1 ? Character.toString(strings[1].charAt(0)) : "");
    } else
    {
      return Character.toString(text.charAt(0)) + Character.toString(text.charAt(1));
    }
  }

  private void textAppearance(@StyleRes int style) {
    if (Build.VERSION.SDK_INT >= 23)
    {
      this.mTextView.setTextAppearance(style);
    } else
    {
      this.mTextView.setTextAppearance(this.getContext(), style);
    }

  }

  @TargetApi (21)
  public ComplexAvatarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    this.mPaint = new Paint();
    this.mViewBackgroundColor = -1;
    this.mRectBounds = new RectF();
    this.mImageLoader = null;
    this.mTextSize = 14.0F;
    this.initAttrs(attrs);
    this.init();
    this.initPaint();
  }

  public ComplexAvatarView setTextAppearance(@StyleRes int style) {
    this.mTextAppearance = style;
    this.textAppearance(style);
    return this;
  }

  public ComplexAvatarView setRectRoundCorner(float rectRoundCorner) {
    this.mRectRoundCorner = rectRoundCorner;
    return this;
  }

  public ComplexAvatarView setBackgroundDrawType(BackgroundDrawType backgroundDrawType) {
    this.mBackgroundDrawType = backgroundDrawType;
    return this;
  }

  public ComplexAvatarView setTextAllCaps() {
    this.mTextView.setAllCaps(true);
    return this;
  }

  public ComplexAvatarView setImageUri(Uri resource) {
    this.mNetworkImageView.setImageURI(resource);
    this.mPaint.setColor(0);
    if (this.mTextView != null)
    {
      this.removeView(this.mTextView);
    }

    return this;
  }

  public ComplexAvatarView setDefaultImage(@DrawableRes int resource) {
    this.mNetworkImageView.setImageResource(resource);
    this.mPaint.setColor(0);
    if (this.mTextView != null)
    {
      this.removeView(this.mTextView);
    }

    return this;
  }

  public ComplexAvatarView setImageUrl(String url, ImageLoader imageLoader) {
    this.mNetworkImageView.setImageUrl(url, imageLoader, withAnimate);
    return this;
  }

  public ComplexAvatarView setImageUrl(String url) {
    if (this.mImageLoader == null)
    {
      throw new NullPointerException("No image loader has specified. Set one image loader or use `setImageUrl(String, ImageLoader)` overload method.");
    } else
    {
      this.mNetworkImageView.setImageUrl(url, this.mImageLoader, withAnimate);
      return this;
    }
  }

  public void makeAllDefaults() {
    this.removeAllViews();
    this.init();
    this.initPaint();
  }

  public ComplexAvatarView setTextColor(@ColorInt int color) {
    this.mTextColor = color;
    this.mTextView.setTextColor(color);
    return this;
  }

  public ComplexAvatarView setTextGravity(int gravity) {
    this.mTextView.setGravity(gravity);
    return this;
  }

  public ComplexAvatarView setTextSize(float size) {
    this.mTextSize = size;
    this.mTextView.setTextSize(size);
    return this;
  }

  public ComplexAvatarView setTextView(@NonNull TextView view) {
    view.setText(this.getTwoLetters(view.getText().toString()));
    this.mTextView = view;
    this.mText = this.mTextView.getText().toString();
    this.mNetworkImageView.setImageResource(0);
    this.addTextView();
    return this;
  }

  public ComplexAvatarView setText(String text) {
    this.mText = text;
    this.mNetworkImageView.setImageResource(0);
    this.addTextView();
    return this;
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    this.setupRectBounds();
    if (this.mBackgroundDrawType == BackgroundDrawType.CIRCLE)
    {
      LayoutParams params = new LayoutParams(this.getWidth() - this.mBorderWidth * 2, this.getHeight() - this.mBorderWidth * 2);
      params.gravity = 17;
      this.mNetworkImageView.setLayoutParams(params);
      canvas.drawCircle((float) (this.getWidth() / 2), (float) (this.getHeight() / 2), (float) (this.getWidth() / 2 - this.mBorderWidth), this.mPaint);
      if (this.mBorderWidth != 0)
      {
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setColor(this.mBorderColor);
        this.mPaint.setStrokeWidth((float) this.mBorderWidth);
        canvas.drawCircle((float) (this.getWidth() / 2), (float) (this.getHeight() / 2), (float) (this.getWidth() / 2 - this.mBorderWidth / 2), this.mPaint);
      }
    } else if (this.mBackgroundDrawType == BackgroundDrawType.RECTANGLE)
    {
      if (this.mRectRoundCorner == 0.0F)
      {
        canvas.drawRect(this.mRectBounds, this.mPaint);
        if (this.mBorderWidth != 0)
        {
          this.mPaint.setStyle(Paint.Style.STROKE);
          this.mPaint.setColor(this.mBorderColor);
          this.mPaint.setStrokeWidth((float) this.mBorderWidth);
          canvas.drawRect(this.mRectBounds, this.mPaint);
        }
      } else
      {
        canvas.drawRoundRect(this.mRectBounds, this.mRectRoundCorner, this.mRectRoundCorner, this.mPaint);
        if (this.mBorderWidth != 0)
        {
          this.mPaint.setStyle(Paint.Style.STROKE);
          this.mPaint.setColor(this.mBorderColor);
          this.mPaint.setStrokeWidth((float) this.mBorderWidth);
          canvas.drawRoundRect(this.mRectBounds, this.mRectRoundCorner, this.mRectRoundCorner, this.mPaint);
        }
      }
    }

  }

  private void setupRectBounds() {
    this.mRectBounds.left = 0.0F;
    this.mRectBounds.top = 0.0F;
    this.mRectBounds.right = (float) this.getWidth();
    this.mRectBounds.bottom = (float) this.getHeight();
  }

  public ComplexAvatarView setViewBackgroundColor(int backgroundColor) {
    this.mViewBackgroundColor = backgroundColor;
    this.mPaint.setColor(this.mViewBackgroundColor);
    return this;
  }

  public ComplexAvatarView setAlphaAnimatorDuration(int alphaAnimatorDuration) {
    this.mAlphaAnimatorDuration = alphaAnimatorDuration;
    this.mNetworkImageView.setAlphaAnimatorDuration(alphaAnimatorDuration);
    return this;
  }

  public ComplexAvatarView setRandomizedBackgroundColor(boolean randomizedBackgroundColor) {
    this.mRandomizedBackgroundColor = randomizedBackgroundColor;
    return this;
  }

  public ImageLoader getImageLoader() {
    return this.mImageLoader;
  }

  public ComplexAvatarView setImageLoader(ImageLoader imageLoader, boolean withAnimate) {
    this.mImageLoader = imageLoader;
    this.withAnimate = withAnimate;
    return this;
  }

  public int getBorderColor() {
    return this.mBorderColor;
  }

  public ComplexAvatarView setBorderColor(@ColorInt int borderColor) {
    this.mBorderColor = borderColor;
    return this;
  }

  public int getBorderWidth() {
    return this.mBorderWidth;
  }

  public ComplexAvatarView setBorderWidth(int borderWidth) {
    this.mBorderWidth = borderWidth;
    return this;
  }
}
