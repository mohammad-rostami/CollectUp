/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.view.chat;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.chat.AndroidUtilities;
import com.collect_up.c_up.chat.widgets.Emoji;

import java.util.ArrayList;

public class EmojiView extends LinearLayout {
    private ArrayList<EmojiGridAdapter> adapters = new ArrayList<>();
    private int[] icons = {R.drawable.ic_emoji_recent,
            R.drawable.ic_emoji_smile,
            R.drawable.ic_emoji_flower,
            R.drawable.ic_emoji_bell,
            R.drawable.ic_emoji_car,
            R.drawable.ic_emoji_symbol};
    private Listener listener;
    private ViewPager pager;
    private FrameLayout recentsWrap;
    private ArrayList<GridView> views = new ArrayList<>();

    public EmojiView(Context paramContext) {
        super(paramContext);
        init();
    }

    public EmojiView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    public EmojiView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < Emoji.data.length; i++) {
            GridView gridView = new GridView(getContext());
            //  if (AndroidUtilities.isTablet()) {
            //     gridView.setColumnWidth(AndroidUtilities.dp(60));
            // } else {
            gridView.setColumnWidth(AndroidUtilities.dp(45));
            // }
            gridView.setNumColumns(-1);
            views.add(gridView);

            EmojiGridAdapter localEmojiGridAdapter = new EmojiGridAdapter(Emoji.data[i]);
            gridView.setAdapter(localEmojiGridAdapter);
            //  AndroidUtilities.setListViewEdgeEffectColor(gridView, 0xff999999);
            adapters.add(localEmojiGridAdapter);
        }

        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_chat_emojies));
        pager = new ViewPager(getContext());
        pager.setAdapter(new EmojiPagesAdapter());
        PagerSlidingTabStrip tabs = new PagerSlidingTabStrip(getContext(), PagerSlidingTabStrip.EMOJIE);
        tabs.setViewPager(pager);
        tabs.setShouldExpand(true);
     //   tabs.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.accent));
      //  tabs.setIndicatorHeight(AndroidUtilities.dp(0f));
      //  tabs.setUnderlineHeight(AndroidUtilities.dp(0f));
       // tabs.setUnderlineColor(0x66000000);
      //  tabs.setTabBackground(0);
        LinearLayout localLinearLayout = new LinearLayout(getContext());
        localLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        localLinearLayout.addView(tabs, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        ImageView localImageView = new ImageView(getContext());
        localImageView.setImageResource(R.drawable.ic_emoji_backspace);
        localImageView.setScaleType(ImageView.ScaleType.CENTER);
        localImageView.setColorFilter(Color.argb(255, 136, 149, 157));

        localImageView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_chat_backspace));
        localImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (EmojiView.this.listener != null) {
                    EmojiView.this.listener.onBackspace();
                }
            }
        });
        localLinearLayout.addView(localImageView, new LinearLayout.LayoutParams(AndroidUtilities.dp(61), LayoutParams.MATCH_PARENT));
        recentsWrap = new FrameLayout(getContext());
        recentsWrap.addView(views.get(0));
        TextView localTextView = new TextView(getContext());
        localTextView.setText(getContext().getString(R.string.NoRecent));
        localTextView.setTextSize(18.0f);
        localTextView.setTextColor(getContext().getResources().getColor(R.color.divider));
        localTextView.setGravity(17);
        recentsWrap.addView(localTextView);
        views.get(0).setEmptyView(localTextView);
        addView(localLinearLayout, new LinearLayout.LayoutParams(-1, AndroidUtilities.dp(48.0f)));
        addView(pager);
        loadRecentEmojis();
        if (Emoji.data[0] == null || Emoji.data[0].length == 0) {
            pager.setCurrentItem(1);
        }
    }

    public void loadRecentEmojis() {
        String str = getContext().getSharedPreferences("emoji", 0)
                .getString("recents", "");
        String[] arrayOfString = null;
        if ((str.length() > 0)) {
            arrayOfString = str.split(",");
            Emoji.data[0] = new long[arrayOfString.length];
        }
        if (arrayOfString != null) {
            for (int i = 0; i < arrayOfString.length; i++) {
                Emoji.data[0][i] = Long.parseLong(arrayOfString[i]);
            }
            adapters.get(0).data = Emoji.data[0];
            adapters.get(0).notifyDataSetChanged();
        }
    }

    private void addToRecent(long paramLong) {
        if (this.pager.getCurrentItem() == 0) {
            return;
        }
        ArrayList<Long> localArrayList = new ArrayList<>();
        long[] currentRecent = Emoji.data[0];
        boolean was = false;
        for (long aCurrentRecent : currentRecent) {
            if (paramLong == aCurrentRecent) {
                localArrayList.add(0, paramLong);
                was = true;
            } else {
                localArrayList.add(aCurrentRecent);
            }
        }
        if (!was) {
            localArrayList.add(0, paramLong);
        }
        Emoji.data[0] = new long[Math.min(localArrayList.size(), 50)];
        for (int q = 0; q < Emoji.data[0].length; q++) {
            Emoji.data[0][q] = localArrayList.get(q);
        }
        adapters.get(0).data = Emoji.data[0];
        adapters.get(0).notifyDataSetChanged();
        saveRecentEmojis();
    }

    private void saveRecentEmojis() {
        ArrayList<Long> localArrayList = new ArrayList<>();
        long[] arrayOfLong = Emoji.data[0];
        int i = arrayOfLong.length;
        for (int j = 0; ; j++) {
            if (j >= i) {
                getContext().getSharedPreferences("emoji", 0)
                        .edit()
                        .putString("recents", TextUtils.join(",", localArrayList))
                        .commit();
                return;
            }
            localArrayList.add(arrayOfLong[j]);
        }
    }

    private String convert(long paramLong) {
        String str = "";
        for (int i = 0; ; i++) {
            if (i >= 4) {
                return str;
            }
            int j = (int) (0xFFFF & paramLong >> 16 * (3 - i));
            if (j != 0) {
                str = str + (char) j;
            }
        }
    }

    public void invalidateViews() {
        for (GridView gridView : views) {
            if (gridView != null) {
                gridView.invalidateViews();
            }
        }
    }

    public void onMeasure(int paramInt1, int paramInt2) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), MeasureSpec.EXACTLY), View.MeasureSpec
                .makeMeasureSpec(View.MeasureSpec.getSize(paramInt2), MeasureSpec.EXACTLY));
    }

    public void setListener(Listener paramListener) {
        this.listener = paramListener;
    }

    public interface Listener {
        void onBackspace();

        void onEmojiSelected(String paramString);
    }

    private class EmojiGridAdapter extends BaseAdapter {
        long[] data;

        public EmojiGridAdapter(long[] arg2) {
            this.data = arg2;
        }

        public int getCount() {
            return data.length;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return data[i];
        }

        public View getView(int i, View view, ViewGroup paramViewGroup) {
            ImageView imageView = (ImageView) view;
            if (imageView == null) {
                imageView = new ImageView(EmojiView.this.getContext()) {
                    public void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2) {
                        setMeasuredDimension(View.MeasureSpec.getSize(paramAnonymousInt1), View.MeasureSpec
                                .getSize(paramAnonymousInt1));
                    }
                };
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (EmojiView.this.listener != null) {
                            EmojiView.this.listener.onEmojiSelected(EmojiView.this.convert((Long) view
                                    .getTag()));
                        }
                        EmojiView.this.addToRecent((Long) view.getTag());
                    }
                });
                imageView.setBackgroundResource(R.drawable.list_selector);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
            }
            imageView.setImageDrawable(Emoji.getEmojiBigDrawable(data[i]));
            imageView.setTag(data[i]);
            return imageView;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    private class EmojiPagesAdapter extends PagerAdapter
            implements PagerSlidingTabStrip.IconTabProvider {

        public int getCount() {
            return views.size();
        }

        public Object instantiateItem(ViewGroup paramViewGroup, int paramInt) {
            View localObject;
            if (paramInt == 0) {
                localObject = recentsWrap;
            } else {
                localObject = views.get(paramInt);
            }
            paramViewGroup.addView(localObject);
            return localObject;
        }

        public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
            View localObject;
            if (paramInt == 0) {
                localObject = recentsWrap;
            } else {
                localObject = views.get(paramInt);
            }
            paramViewGroup.removeView(localObject);
        }

        public boolean isViewFromObject(View paramView, Object paramObject) {
            return paramView == paramObject;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }

        public int getPageIconResId(int paramInt) {
            return icons[paramInt];
        }

        @Override
        public String getPageIconAddress(int position) {
            return null;
        }
    }
}