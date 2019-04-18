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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.StickersAdapter;
import com.collect_up.c_up.adapters.StickersBottomSheetAdapter;
import com.collect_up.c_up.chat.AndroidUtilities;
import com.collect_up.c_up.chat.widgets.Emoji;
import com.collect_up.c_up.fragments.FragmentAllStickers;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.listeners.IStickerChangeStateListener;
import com.collect_up.c_up.model.StickerPackage;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.rey.material.app.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class StickerView extends LinearLayout {
  private ArrayList<StickerAdapter> adapters = new ArrayList<>();
  private int[] icons = {R.drawable.ic_emoji_recent,
    R.drawable.ic_emoji_smile,
    R.drawable.ic_emoji_flower,
    R.drawable.ic_emoji_bell,
    R.drawable.ic_emoji_car,
    R.drawable.ic_emoji_symbol};
  private Listener stickerClicListener;
  private ViewPager pager;
  private FrameLayout recentsWrap;
  private int columnWidth = 70;
  private ArrayList<RecyclerView> views = new ArrayList<>();
  private HashMap<String, List<IStickerChangeStateListener>> listeners = new HashMap<String, List<IStickerChangeStateListener>>();

  public StickerView(Context paramContext, Listener listener) {
    super(paramContext);
    setListener(listener);
    init();
  }

  public StickerView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }

  public StickerView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
    init();
  }

  private void init() {
    setOrientation(LinearLayout.VERTICAL);
    List<StickerPackage> stickers = Logged.Models.getUserStickerPackages(getContext());
    for (int i = 0; i < stickers.size(); i++)
    {
      RecyclerView gridView = new RecyclerView(getContext());
      //  if (AndroidUtilities.isTablet()) {
      //     gridView.setColumnWidth(AndroidUtilities.dp(60));
      // } else {
      RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 5);
      gridView.setLayoutManager(layoutManager);
      //gridView.setColumnWidth(AndroidUtilities.dp(columnWidth));
      // }
      // gridView.setNumColumns(-1);
      views.add(gridView);
      List<StickerPackage> packages = new ArrayList<>();
      packages.add(stickers.get(i));
      StickerAdapter stickerGridAdapter = new StickerAdapter(packages, StickerAdapter.TYPE_GRID);
      stickerGridAdapter.setClickListener(this.stickerClicListener);
      gridView.setAdapter(stickerGridAdapter);
      //  AndroidUtilities.setListViewEdgeEffectColor(gridView, 0xff999999);
      adapters.add(stickerGridAdapter);
    }
    RecyclerView gridView = new RecyclerView(getContext());
    //  if (AndroidUtilities.isTablet()) {
    //     gridView.setColumnWidth(AndroidUtilities.dp(60));
    // } else {
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
    gridView.setLayoutManager(layoutManager);
    //gridView.setColumnWidth(AndroidUtilities.dp(columnWidth));
    // }
    // gridView.setNumColumns(-1);
    views.add(gridView);

    StickerAdapter trendsAdapter = new StickerAdapter(Logged.Models.getUserStickerPackages(getContext()), StickerAdapter.TYPE_LIST);
    gridView.setAdapter(trendsAdapter);
    adapters.add(trendsAdapter);

    //  AndroidUtilities.setListViewEdgeEffectColor(gridView, 0xff999999);
   /* GridView list = new GridView(getContext());
    list.setNumColumns(1);

    views.add(list);

    StickerAdapter localEmojiGridAdapter = new StickerAdapter(Logged.Models.getUserStickerPackages(getContext()).get(0));
    list.setAdapter(localEmojiGridAdapter);*/
    pager = new ViewPager(getContext());
    pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        if (position == Logged.Models.getUserStickerPackages(getContext()).size())
        {
          Pagination.getStickers(1, getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
              // Reset items to make the pull to refresh right
              adapters.get(Logged.Models.getUserStickerPackages(getContext()).size()).data = (List<StickerPackage>) pageList;//Emoji.data[0];
              adapters.get(Logged.Models.getUserStickerPackages(getContext()).size()).notifyDataSetChanged();
            }

            @Override
            public void onFailure() {
            }
          });
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });
    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_chat_emojies));
    //StickerPagerAdapter adapterPager = new StickerPagerAdapter(getContext().getch(), stickers, listener);
    pager.setAdapter(new StciekrPagesAdapter());
    PagerSlidingTabStrip tabs = new PagerSlidingTabStrip(getContext(), PagerSlidingTabStrip.STICKER);
    tabs.setViewPager(pager);
    tabs.setShouldExpand(false);
    //tabs.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.accent));
    //  tabs.setIndicatorHeight(AndroidUtilities.dp(0f));
    // tabs.setUnderlineHeight(AndroidUtilities.dp(0));
    // tabs.setUnderlineColor(0x66000000);
    // tabs.setTabBackground(0);
    LinearLayout localLinearLayout = new LinearLayout(getContext());
    localLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
    localLinearLayout.addView(tabs, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
    ImageView localImageView = new ImageView(getContext());
    localImageView.setImageResource(R.drawable.ic_sticker_setting);

    // localImageView.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_selector));
    localImageView.setScaleType(ImageView.ScaleType.CENTER);
    localImageView.setColorFilter(Color.argb(255, 136, 149, 157));

    localImageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_selector));
    localImageView.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_MANAGE_STICKER, null);

        //   getContext().startActivity(new Intent(getContext(), FragmentManageStickers.class));
      }
    });
    localLinearLayout.addView(localImageView, new LinearLayout.LayoutParams(AndroidUtilities.dp(61), LayoutParams.MATCH_PARENT));
    recentsWrap = new FrameLayout(getContext());
    recentsWrap.addView(views.get(0));
   /* TextView localTextView = new TextView(getContext());
    localTextView.setText(getContext().getString(R.string.NoRecent));
    localTextView.setTextSize(18.0f);
    localTextView.setTextColor(getContext().getResources().getColor(R.color.divider));
    localTextView.setGravity(17);
    recentsWrap.addView(localTextView);
    views.get(0).setEmptyView(localTextView);*/
    addView(localLinearLayout, new LinearLayout.LayoutParams(-1, AndroidUtilities.dp(48.0f)));
    addView(pager);
    loadRecentEmojis();
    if (Emoji.data[0] == null || Emoji.data[0].length == 0)
    {
      pager.setCurrentItem(1);
    }

  }

  public void loadRecentEmojis() {
    String str = getContext().getSharedPreferences("emoji", 0)
      .getString("recents", "");
    String[] arrayOfString = null;
    if ((str.length() > 0))
    {
      arrayOfString = str.split(",");
      Emoji.data[0] = new long[arrayOfString.length];
    }
    if (arrayOfString != null)
    {
      for (int i = 0; i < arrayOfString.length; i++)
      {
        Emoji.data[0][i] = Long.parseLong(arrayOfString[i]);
      }
      //
      // adapters.get(0).data = Logged.Models.getUserStickerPackages(getContext()).get(0);//Emoji.data[0];
      adapters.get(0).notifyDataSetChanged();
    }
  }

  private void addToRecent(String paramLong) {
       /* if (this.pager.getCurrentItem() == 0) {
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
        adapters.get(0).data = Logged.Models.getUserStickerPackages().get(0);//Emoji.data[0];
        adapters.get(0).notifyDataSetChanged();
        saveRecentEmojis();*/
  }

  private void saveRecentEmojis() {
    ArrayList<Long> localArrayList = new ArrayList<>();
    long[] arrayOfLong = Emoji.data[0];
    int i = arrayOfLong.length;
    for (int j = 0; ; j++)
    {
      if (j >= i)
      {
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
    for (int i = 0; ; i++)
    {
      if (i >= 4)
      {
        return str;
      }
      int j = (int) (0xFFFF & paramLong >> 16 * (3 - i));
      if (j != 0)
      {
        str = str + (char) j;
      }
    }
  }

  public void invalidateViews() {
   /* for (GridView gridView : views)
    {
      if (gridView != null)
      {
        gridView.invalidateViews();
      }
    }*/
  }

/*  public void onMeasure(int paramInt1, int paramInt2) {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), MeasureSpec.EXACTLY), View.MeasureSpec
      .makeMeasureSpec(View.MeasureSpec.getSize(paramInt2), MeasureSpec.EXACTLY));
  }*/

  public void setListener(Listener paramListener) {
    this.stickerClicListener = paramListener;
  }

  public interface Listener {
    void onBackspace();

    void onStickerSelected(String paramString, StickerPackage stickerPackage);

  }

  private class StickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<StickerPackage> data = new ArrayList<>();
    public static final int TYPE_GRID = 0;
    public static final int TYPE_LIST = 1;
    private int typeMode;
    private int count = 0;
    private Listener listener;

    public class ViewHolderGrid extends RecyclerView.ViewHolder {
      private ImageView imgStickerIcon;

      public ViewHolderGrid(View view) {
        super(view);

        imgStickerIcon = (ImageView) view.findViewById(R.id.imgSticker);
      }
    }

    public class ViewHolderList extends RecyclerView.ViewHolder {
      private TextView txtTitle;
      private TextView txtDesc;
      private Button btnAdd;
      private RecyclerView list;

      public ViewHolderList(View view) {
        super(view);

        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtDesc = (TextView) view.findViewById(R.id.txtDesc);
        btnAdd = (Button) view.findViewById(R.id.btnAdd);
        list = (RecyclerView) view.findViewById(R.id.list);
      }
    }

    public void setClickListener(Listener listener) {
      this.listener = listener;
    }

    public StickerAdapter(List<StickerPackage> arg2, int viewType) {
      this.data = arg2;
      typeMode = viewType;
    }

    public StickerAdapter(List<StickerPackage> arg2, int viewType, int count) {
      this.data = arg2;
      this.typeMode = viewType;
      this.count = count;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = null;
      switch (viewType)
      {
        case TYPE_GRID:
          view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stickerpage, parent, false);
          return new StickerAdapter.ViewHolderGrid(view);
        case TYPE_LIST:
          view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend_sticker, parent, false);
          return new StickerAdapter.ViewHolderList(view);
        default:
          return null;
      }

    }

    @Override
    public int getItemViewType(int position) {
      return typeMode;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

      switch (holder.getItemViewType())
      {
        case TYPE_GRID:


          ((ViewHolderGrid) holder).imgStickerIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
              StickerPackage stickerPackage = (StickerPackage) view.getTag();


              if (StickerAdapter.this.listener != null)
              {
                StickerAdapter.this.listener.onStickerSelected(stickerPackage.getStickers().get(position), stickerPackage);
              }
              StickerView.this.addToRecent(stickerPackage.getStickers().get(position));
            }
          });

          File cacheDir = StorageUtils.getCacheDirectory(getContext());

          ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
            .diskCache(new UnlimitedDiskCache(cacheDir))
            .build();

          ImageLoader.getInstance().init(config);
          ImageLoader.getInstance().loadImage(Constants.General.BLOB_PROTOCOL + data.get(0).getStickers().get(position), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
              ((ViewHolderGrid) holder).imgStickerIcon.setImageBitmap(loadedImage);
            }
          });
          ((ViewHolderGrid) holder).imgStickerIcon.setTag(data.get(0));
          break;
        case TYPE_LIST:
          ((ViewHolderList) holder).list.setTag(data.get(position));
          ((ViewHolderList) holder).btnAdd.setTag(data.get(position));

          ((ViewHolderList) holder).txtTitle.setText(data.get(position).getPackageName().toString());
          ((ViewHolderList) holder).txtDesc.setText(data.get(position).getCount() + " Stickers");

          RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 5);
          ((ViewHolderList) holder).list.setLayoutManager(layoutManager);
          List<StickerPackage> packages = new ArrayList<>();
          packages.add(data.get(position));
          StickerAdapter stickerGridAdapter = new StickerAdapter(packages, StickerAdapter.TYPE_GRID, 5);
          stickerGridAdapter.setClickListener(new Listener() {
            @Override
            public void onBackspace() {

            }

            @Override
            public void onStickerSelected(String paramString, StickerPackage stickerPackage) {
              StickerView.this.showButtomView(stickerPackage);
            }
          });
          ((ViewHolderList) holder).list.setAdapter(stickerGridAdapter);
          final IStickerChangeStateListener listener = new IStickerChangeStateListener() {
            @Override
            public void onStickerAdded(StickerPackage stickerPackage) {
              ((ViewHolderList) holder).btnAdd.setText("Remove");
            }

            @Override
            public void onStickerRemoved(StickerPackage stickerPackage) {
              ((ViewHolderList) holder).btnAdd.setText("Add");

            }
          };
          List<IStickerChangeStateListener> array = listeners.get(data.get(position).getId()) != null ? listeners.get(data.get(position).getId()) : new ArrayList<IStickerChangeStateListener>();
          array.add(listener);
          listeners.put(data.get(position).getId(), array);

          ((ViewHolderList) holder).btnAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
              if (((Button) view).getText().equals("Add"))
              {
                addStcikerPackage(data.get(position), true);
              } else
              {
                addStcikerPackage(data.get(position), false);
              }

            }
          });

          break;
      }

    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public int getItemCount() {
      switch (typeMode)
      {
        case TYPE_LIST:
          return data.size();
        case TYPE_GRID:
          if (data.get(0).getStickers().size() > 5 && count == 5)
          {
            return count;
          } else
          {
            return data.get(0).getStickers().size();
          }
        default:
          return 0;
      }
    }


  }

  private void addStcikerPackage(final StickerPackage stickerPackage, boolean isAdd) {
    if (isAdd)
    {
      HttpClient.get(String.format(Constants.Server.Stickers.ADD_TO_PROFILE_PACKAGES,
        stickerPackage.getId()), new AsyncHttpResponser(getContext()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          super.onSuccess(statusCode, headers, responseBody);
          Toast.makeText(getContext(), stickerPackage.getPackageName() + " added", Toast.LENGTH_SHORT).show();
          List<StickerPackage> packages = Logged.Models.getUserStickerPackages(getContext());
          packages.add(stickerPackage);
          Logged.Models.setUserStickerPackages(getContext(), packages);
     /*     Iterator<IStickerChangeStateListener> iter = listeners.iterator();

          while (iter.hasNext())
          {
            IStickerChangeStateListener str = iter.next();

            if (str != null)
            {
              str.onStickerAdded(stickerPackage);
              iter.remove();

            }
          }*/
          for (int i = 0; i < listeners.size(); i++)
          {
            if (listeners.get(stickerPackage.getId()) != null)
            {
              for (int j = 0; j < listeners.get(stickerPackage.getId()).size(); j++)
              {
                listeners.get(stickerPackage.getId()).get(j).onStickerAdded(stickerPackage);

              }

            }
          }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);
        }
      });
    } else
    {
      HttpClient.get(String.format(Constants.Server.Stickers.REMOVE_PACKGE_PROFILE,
        stickerPackage.getId()), new AsyncHttpResponser(getContext()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          super.onSuccess(statusCode, headers, responseBody);
          Toast.makeText(getContext(), stickerPackage.getPackageName() + " removed", Toast.LENGTH_SHORT).show();
          List<StickerPackage> packages = Logged.Models.getUserStickerPackages(getContext());
          for (int i = 0; i < packages.size(); i++)
          {
            if (packages.get(i).getId().equalsIgnoreCase(stickerPackage.getId()))
            {
              Logged.Models.removeStickerPackage(getContext(), packages.get(i));
              packages.remove(i);
              for (int j = 0; j < listeners.size(); j++)
              {
                if (listeners.get(stickerPackage.getId()) != null)
                {
                  for (int k = 0; k < listeners.get(stickerPackage.getId()).size(); k++)
                  {
                    listeners.get(stickerPackage.getId()).get(k).onStickerRemoved(stickerPackage);

                  }
                }
              }

              break;
            }
          }
          //    Logged.Models.setUserStickerPackages(getContext(), packages);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);
        }
      });
    }
  }

  private class StciekrPagesAdapter extends PagerAdapter
    implements PagerSlidingTabStrip.IconTabProvider {

    public int getCount() {
      return views.size();
    }

    public Object instantiateItem(ViewGroup paramViewGroup, int paramInt) {
      View localObject;
      if (paramInt == 0)
      {
        localObject = recentsWrap;
      } else
      {
        localObject = views.get(paramInt);
      }
      paramViewGroup.addView(localObject);
      return localObject;
    }

    public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
      View localObject;
      if (paramInt == 0)
      {
        localObject = recentsWrap;
      } else
      {
        localObject = views.get(paramInt);
      }
      paramViewGroup.removeView(localObject);
    }

    public boolean isViewFromObject(View paramView, Object paramObject) {
      return paramView == paramObject;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
      if (observer != null)
      {
        super.unregisterDataSetObserver(observer);
      }
    }


    @Override
    public int getPageIconResId(int position) {

      List<StickerPackage> packageList = Logged.Models.getUserStickerPackages(getContext());
      if (packageList.size() == position)
      {
        return R.drawable.ic_smiles_trend;
      } else
      {
        return 0;
      }
    }

    @Override
    public String getPageIconAddress(int position) {
      List<StickerPackage> packageList = Logged.Models.getUserStickerPackages(getContext());
      if (packageList.size() == position)
      {
        return "";
      } else
      {
        return packageList.get(position).getThumbnailAddress();
      }
    }
  }


  private void showButtomView(final StickerPackage stickerPackage) {
    boolean exist = false;
    final BottomSheetDialog mDialog = new BottomSheetDialog(getContext());
    mDialog.contentView(R.layout.bottom_sheet_sticker)
      .inDuration(300)
      .cancelable(true);

    final com.rey.material.widget.ImageButton btnAdd = (com.rey.material.widget.ImageButton) mDialog.findViewById(R.id.btnAdd);
    List<StickerPackage> stickers = Logged.Models.getUserStickerPackages(getContext());
    if (stickers.size() > 0)
    {
      for (int i = 0; i < stickers.size(); i++)
      {
        if (stickers.get(i).getId().equalsIgnoreCase(stickerPackage.getId()))
        {

          exist = true;
          break;
        }
      }
    }
    if (exist)
    {
      btnAdd.setImageResource(R.drawable.ic_remove_circle_black_48dp);
      btnAdd.setTag(R.drawable.ic_remove_circle_black_48dp);
    } else
    {
      btnAdd.setImageResource(R.drawable.ic_add_circle_black_48dp);
      btnAdd.setTag(R.drawable.ic_add_circle_black_48dp);
    }

    TextView txtTitle = (TextView) mDialog.findViewById(R.id.txtTitle);
    txtTitle.setText(stickerPackage.getPackageName() + " (" + stickerPackage.getCount() + ")");
    RecyclerView recyclerView = (RecyclerView) mDialog.findViewById(R.id.recycler_view);
    StickersBottomSheetAdapter adapter = new StickersBottomSheetAdapter(getContext(), stickerPackage.getStickers());
    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 5);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
    IStickerChangeStateListener listener = new IStickerChangeStateListener() {
      @Override
      public void onStickerAdded(StickerPackage stickerPackage) {
        btnAdd.setImageResource(R.drawable.ic_remove_circle_black_48dp);
        btnAdd.setTag(R.drawable.ic_remove_circle_black_48dp);
      }

      @Override
      public void onStickerRemoved(StickerPackage stickerPackage) {
        btnAdd.setImageResource(R.drawable.ic_add_circle_black_48dp);
        btnAdd.setTag(R.drawable.ic_add_circle_black_48dp);
      }
    };
    List<IStickerChangeStateListener> array = listeners.get(stickerPackage.getId()) != null ? listeners.get(stickerPackage.getId()) : new ArrayList<IStickerChangeStateListener>();
    array.add(listener);
    listeners.put(stickerPackage.getId(), array);

    btnAdd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if ((Integer) v.getTag() == R.drawable.ic_remove_circle_black_48dp)
        {
          addStcikerPackage(stickerPackage, false);

          /*HttpClient.get(String.format(Constants.Server.Stickers.REMOVE_PACKGE_PROFILE,
            stickerPackage.getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              super.onSuccess(statusCode, headers, responseBody);
              btnAdd.setImageResource(R.drawable.ic_add_circle_black_48dp);
              btnAdd.setTag(R.drawable.ic_add_circle_black_48dp);
              Toast.makeText(getContext(), stickerPackage.getPackageName() + " removed", Toast.LENGTH_SHORT).show();
              List<StickerPackage> packages = Logged.Models.getUserStickerPackages(getContext());
              for (int i = 0; i < packages.size(); i++)
              {
                if (packages.get(i).getId().equalsIgnoreCase(stickerPackage.getId()))
                {
                  Logged.Models.removeStickerPackage(getContext(), packages.get(i));
                  packages.remove(i);

                  break;
                }
              }
              //    Logged.Models.setUserStickerPackages(getContext(), packages);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);
            }
          });*/
        } else
        {
          addStcikerPackage(stickerPackage, true);

          /*HttpClient.get(String.format(Constants.Server.Stickers.ADD_TO_PROFILE_PACKAGES,
            stickerPackage.getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              super.onSuccess(statusCode, headers, responseBody);
              btnAdd.setImageResource(R.drawable.ic_remove_circle_black_48dp);
              btnAdd.setTag(R.drawable.ic_remove_circle_black_48dp);
              Toast.makeText(getContext(), stickerPackage.getPackageName() + " added", Toast.LENGTH_SHORT).show();
              List<StickerPackage> packages = Logged.Models.getUserStickerPackages(getContext());
              packages.add(stickerPackage);
              Logged.Models.setUserStickerPackages(getContext(), packages);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);
            }
          });*/
        }

        mDialog.dismiss();
      }
    });
    mDialog.show();
  }
}