/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Locale;


public class PagerSlidingTabStrip extends HorizontalScrollView {

  private final PageListener pageListener = new PageListener();
  public OnPageChangeListener delegatePageListener;
  private int currentPosition = 0;
  private float currentPositionOffset = 0f;
  private LinearLayout.LayoutParams defaultTabLayoutParams;
  private int dividerPadding = 12;
  private int indicatorColor = 0xFF666666;
  private int indicatorHeight = 8;
  private int lastScrollX = 0;
  private Locale locale;
  private ViewPager pager;
  private Paint rectPaint;
  private int scrollOffset = 52;
  private boolean shouldExpand = false;
  private int tabBackgroundResId = R.drawable.background_tab;
  private int tabCount;
  private int tabPadding = 24;
  private int tabTextColor = 0xFF666666;
  private int tabTextSize = 12;
  private Typeface tabTypeface = null;
  private int tabTypefaceStyle = Typeface.BOLD;
  private LinearLayout tabsContainer;
  private boolean textAllCaps = true;
  private int underlineColor = 0x1A000000;
  private int underlineHeight = 2;
  public static final int STICKER = 0;
  public static final int EMOJIE = 1;
  private int type;

  public PagerSlidingTabStrip(Context context, int type) {
    this(context, null);
    this.type = type;
  }

  public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    setFillViewport(true);
    setWillNotDraw(false);

    tabsContainer = new LinearLayout(context);
    tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
    tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    addView(tabsContainer);

    DisplayMetrics dm = getResources().getDisplayMetrics();

    scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
    indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
    underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
    dividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
    tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
    tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

    rectPaint = new Paint();
    rectPaint.setAntiAlias(true);
    rectPaint.setStyle(Style.FILL);

    defaultTabLayoutParams = new LinearLayout.LayoutParams(Utils.dpToPx(50), LayoutParams.MATCH_PARENT);

    if (locale == null)
    {
      locale = getResources().getConfiguration().locale;
    }
  }

  public int getDividerPadding() {
    return dividerPadding;
  }

  public void setDividerPadding(int dividerPaddingPx) {
    this.dividerPadding = dividerPaddingPx;
    invalidate();
  }

  public int getIndicatorColor() {
    return this.indicatorColor;
  }

  public void setIndicatorColor(int indicatorColor) {
    this.indicatorColor = indicatorColor;
    invalidate();
  }

  public int getIndicatorHeight() {
    return indicatorHeight;
  }

  public void setIndicatorHeight(int indicatorLineHeightPx) {
    this.indicatorHeight = indicatorLineHeightPx;
    invalidate();
  }

  public int getScrollOffset() {
    return scrollOffset;
  }

  public void setScrollOffset(int scrollOffsetPx) {
    this.scrollOffset = scrollOffsetPx;
    invalidate();
  }

  public boolean getShouldExpand() {
    return shouldExpand;
  }

  public void setShouldExpand(boolean shouldExpand) {
    this.shouldExpand = shouldExpand;
    tabsContainer.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
    updateTabStyles();
    requestLayout();
  }

  public int getTabBackground() {
    return tabBackgroundResId;
  }

  public void setTabBackground(int resId) {
    this.tabBackgroundResId = resId;
  }

  public int getTabPaddingLeftRight() {
    return tabPadding;
  }

  public void setTabPaddingLeftRight(int paddingPx) {
    this.tabPadding = paddingPx;
    updateTabStyles();
  }

  public int getTextColor() {
    return tabTextColor;
  }

  public void setTextColor(int textColor) {
    this.tabTextColor = textColor;
    updateTabStyles();
  }

  public int getTextSize() {
    return tabTextSize;
  }

  public void setTextSize(int textSizePx) {
    this.tabTextSize = textSizePx;
    updateTabStyles();
  }

  public int getUnderlineColor() {
    return underlineColor;
  }

  public void setUnderlineColor(int underlineColor) {
    this.underlineColor = underlineColor;
    invalidate();
  }

  public int getUnderlineHeight() {
    return underlineHeight;
  }

  public void setUnderlineHeight(int underlineHeightPx) {
    this.underlineHeight = underlineHeightPx;
    invalidate();
  }

  public boolean isTextAllCaps() {
    return textAllCaps;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (isInEditMode() || tabCount == 0)
    {
      return;
    }

    final int height = getHeight();

    // draw indicator line

    rectPaint.setColor(indicatorColor);

    // default: line below current tab
    View currentTab = tabsContainer.getChildAt(currentPosition);
    float lineLeft = currentTab.getLeft();
    float lineRight = currentTab.getRight();

    // if there is an offset, start interpolating left and right coordinates between current and next tab
    if (currentPositionOffset > 0f && currentPosition < tabCount - 1)
    {

      View nextTab = tabsContainer.getChildAt(currentPosition + 1);
      final float nextTabLeft = nextTab.getLeft();
      final float nextTabRight = nextTab.getRight();

      lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
      lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
    }

    canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);

    // draw underline

    rectPaint.setColor(underlineColor);
    canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);
  }

/*    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!shouldExpand || MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            return;
        }

        int myWidth = getMeasuredWidth();
        tabsContainer.measure(MeasureSpec.EXACTLY | myWidth, heightMeasureSpec);
    }*/

  public void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    if (!shouldExpand)
    {
      post(new Runnable() {
        public void run() {
          PagerSlidingTabStrip.this.notifyDataSetChanged();
        }
      });
    }
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    currentPosition = savedState.currentPosition;
    requestLayout();
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.currentPosition = currentPosition;
    return savedState;
  }

  public void setAllCaps(boolean textAllCaps) {
    this.textAllCaps = textAllCaps;
  }

  public void setIndicatorColorResource(int resId) {
    this.indicatorColor = getResources().getColor(resId);
    invalidate();
  }

  public void setOnPageChangeListener(OnPageChangeListener listener) {
    this.delegatePageListener = listener;
  }

  public void setTextColorResource(int resId) {
    this.tabTextColor = getResources().getColor(resId);
    updateTabStyles();
  }

  public void setTypeface(Typeface typeface, int style) {
    this.tabTypeface = typeface;
    this.tabTypefaceStyle = style;
    updateTabStyles();
  }

  public void setUnderlineColorResource(int resId) {
    this.underlineColor = getResources().getColor(resId);
    invalidate();
  }

  public void setViewPager(ViewPager pager) {
    this.pager = pager;

    if (pager.getAdapter() == null)
    {
      throw new IllegalStateException("ViewPager does not have adapter instance.");
    }

    pager.setOnPageChangeListener(pageListener);

    notifyDataSetChanged();
  }

  public void notifyDataSetChanged() {

    tabsContainer.removeAllViews();

    tabCount = pager.getAdapter().getCount();

    for (int i = 0; i < tabCount; i++)
    {

      if (pager.getAdapter() instanceof IconTabProvider)
      {
        if (type == EMOJIE)
        {
          addIconTab(i, ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));

        } else
        {
          addIconTab(i, ((IconTabProvider) pager.getAdapter()).getPageIconAddress(i));

        }
      } else
      {
        //      addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
      }

    }

    updateTabStyles();

    getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

      @SuppressWarnings ("deprecation")
      @SuppressLint ("NewApi")
      @Override
      public void onGlobalLayout() {

        if (Build.VERSION.SDK_INT < 16)
        {
          getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else
        {
          getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }

        currentPosition = pager.getCurrentItem();
        scrollToChild(currentPosition, 0);
      }
    });

    updateExpanded();

  }

  private void addIconTab(final int position, int resId) {

    ImageButton tab = new ImageButton(getContext());
    tab.setFocusable(true);
    tab.setImageResource(resId);
    tab.setColorFilter(Color.argb(255, 136, 149, 157));
    tab.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        pager.setCurrentItem(position);
      }
    });

    tabsContainer.addView(tab);
    tab.setSelected(position == currentPosition);
  }

  private void addIconTab(final int position, String imageUrl) {

    final ImageView tab = new ImageView(getContext());
    tab.setLayoutParams(new ViewGroup.LayoutParams(Utils.dpToPx(50), ViewGroup.LayoutParams.MATCH_PARENT));
    tab.setMaxWidth(Utils.dpToPx(50));
    tab.setPadding(Utils.dpToPx(10), Utils.dpToPx(10), Utils.dpToPx(10), Utils.dpToPx(10));
    tab.setFocusable(true);
    tab.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    tab.setAdjustViewBounds(true);
    ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
    if (imageUrl.equalsIgnoreCase(""))
    {
      tab.setImageResource(R.drawable.ic_smiles_trend);
    } else
    {
      imageLoader.displayImage(Constants.General.BLOB_PROTOCOL + imageUrl, tab);
    }

    tab.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        pager.setCurrentItem(position);
      }
    });

    tabsContainer.addView(tab);
    tab.setSelected(position == currentPosition);
  }

  private void addTextTab(final int position, String title) {

    TextView tab = new TextView(getContext());
    tab.setText(title);
    tab.setFocusable(true);
    tab.setGravity(Gravity.CENTER);
    tab.setSingleLine();

    tab.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        pager.setCurrentItem(position);
      }
    });

    tabsContainer.addView(tab);

  }

  private void updateTabStyles() {

    for (int i = 0; i < tabCount; i++)
    {

      View v = tabsContainer.getChildAt(i);

      v.setLayoutParams(defaultTabLayoutParams);
      v.setBackgroundResource(tabBackgroundResId);
      if (shouldExpand)
      {
        //   v.setPadding(0, 0, 0, 0);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.0F));
      } //else {
      //v.setPadding(tabPadding, 0, tabPadding, 0);
      //}

      if (v instanceof TextView)
      {

        TextView tab = (TextView) v;
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
        tab.setTypeface(tabTypeface, tabTypefaceStyle);
        tab.setTextColor(tabTextColor);

        // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
        // pre-ICS-build
        if (textAllCaps)
        {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
          {
            tab.setAllCaps(true);
          } else
          {
            tab.setText(tab.getText().toString().toUpperCase(locale));
          }
        }
      }
    }
  }

  private void scrollToChild(int position, int offset) {
    if (tabCount == 0)
    {
      return;
    }

    int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

    if (position > 0 || offset > 0)
    {
      newScrollX -= scrollOffset;
    }

    if (newScrollX != lastScrollX)
    {
      lastScrollX = newScrollX;
      scrollTo(newScrollX, 0);
    }
  }

  private void updateExpanded() {

  }

  public interface IconTabProvider {
    public int getPageIconResId(int position);

    public String getPageIconAddress(int position);
  }

  static class SavedState extends BaseSavedState {
    public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
      @Override
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
    int currentPosition;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      currentPosition = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeInt(currentPosition);
    }
  }

  private class PageListener implements OnPageChangeListener {

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      currentPosition = position;
      currentPositionOffset = positionOffset;

      scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position)
        .getWidth()));

      invalidate();

      if (delegatePageListener != null)
      {
        delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
      }
    }

    @Override
    public void onPageSelected(int position) {
      if (delegatePageListener != null)
      {
        delegatePageListener.onPageSelected(position);
      }
      for (int a = 0; a < tabsContainer.getChildCount(); a++)
      {
        tabsContainer.getChildAt(a).setSelected(a == position);
      }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
      if (state == ViewPager.SCROLL_STATE_IDLE)
      {
        scrollToChild(pager.getCurrentItem(), 0);
      }

      if (delegatePageListener != null)
      {
        delegatePageListener.onPageScrollStateChanged(state);
      }
    }

  }
}
