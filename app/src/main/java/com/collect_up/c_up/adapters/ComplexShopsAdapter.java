/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Shop;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ComplexShopsAdapter extends UltimateViewAdapter<ComplexShopsAdapter.Holder>
  implements View.OnClickListener {

  private final Context mContext;
  final private List<Shop> mShopList;
  private ItemListener mListener;

  public ComplexShopsAdapter(Context context, List<Shop> shopList) {
    this.mShopList = shopList;
    mContext = context;
  }

  public void updateShop(Shop newShop, boolean deleted) {
    for (Shop product : mShopList)
    {
      if (product.getId().equals(newShop.getId()))
      {
        int pos = mShopList.indexOf(product);
        if (deleted)
        {
          mShopList.remove(pos);
          notifyItemRemoved(pos);
        } else
        {
          mShopList.set(pos, newShop);
          notifyItemChanged(pos);
        }
        break;
      }
    }
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.fragment_complex_tab_business_item_grid, parent, false);
    Holder holder = new Holder(view, true);
    holder.picture.setOnClickListener(this);

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    return mShopList.size();
  }

  @Override
  public long generateHeaderId(int position) {
    return 0;
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    if (holder.picture == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }

    Shop item = mShopList.get(position);
    int width = mContext.getResources()
      .getDisplayMetrics().widthPixels / 2;
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, width);

    holder.picture.setTag(item);

    holder.name.setText(item.getName());
    setPicture(holder, item);

    holder.picture.setLayoutParams(params);
  }

  private void setPicture(final Holder holder, Shop shop) {
    holder.picture.setImageResource(R.drawable.placeholder);
    if (!Utils.isNullOrEmpty(shop.getImageAddress()))
    {
      String imagePath = Constants.General.BLOB_PROTOCOL + shop.getThumb();

      ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
      imageLoader.loadImage(imagePath, new SimpleImageLoadingListener() {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
          holder.picture.setImageBitmap(loadedImage);
        }
      });
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.image_view_picture:
        Shop shop = (Shop) v.getTag();
        if (mListener != null)
        {
          mListener.onShopPictureClick(shop);
        }
        break;
    }
  }

  @Override
  public UltimateRecyclerviewViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

  }

  public void setListener(ItemListener listener) {
    mListener = listener;
  }

  public interface ItemListener {
    void onShopPictureClick(Shop shop);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.text_view_name)
    TextView name;
    @Bind (R.id.image_view_picture)
    ImageView picture;

    public Holder(View view, boolean isItem) {
      super(view);
      if (isItem)
      {
        ButterKnife.bind(this, view);
      }
    }
  }
}