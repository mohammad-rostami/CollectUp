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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShopProductsAdapter extends UltimateViewAdapter<ShopProductsAdapter.Holder>
  implements View.OnClickListener {

  private final Context mContext;
  final private List<Product> mProductList;
  private ItemListener mListener;

  public ShopProductsAdapter(Context context, List<Product> productList) {
    this.mProductList = productList;
    mContext = context;
  }

  public void updateProduct(Product newProduct, boolean deleted) {
    for (Product product : mProductList)
    {
      if (product.getId().equals(newProduct.getId()))
      {
        int pos = mProductList.indexOf(product);
        if (deleted)
        {
          mProductList.remove(pos);
          notifyItemRemoved(pos);
        } else
        {
          mProductList.set(pos, newProduct);
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
      .inflate(R.layout.fragment_business_tab_products_item_grid, parent, false);
    Holder holder = new Holder(view, true);
    holder.picture.setOnClickListener(this);

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    return mProductList.size();
  }

  @Override
  public long generateHeaderId(int position) {
    return 0;
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    if (holder.name == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }

    Product item = mProductList.get(position);
    int width = Utils.getScreenWidthPX(mContext) / 2;
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, width);

    holder.picture.setTag(item);

    holder.name.setText(item.getName());
    //   setPicture(holder, item);
    if (!Utils.isNullOrEmpty(item.getDefaultImageAddress()))
    {
      holder.picture.makeAllDefaults();
      holder.picture.setImageLoader(MyApplication.getInstance().getImageLoader(), true).setImageUrl(Constants.General.BLOB_PROTOCOL + item.getDefaultImageAddress());
    } else
    {
      holder.picture.makeAllDefaults();
      holder.picture.setDefaultImage(R.drawable.placeholder);
    }

    holder.picture.setLayoutParams(params);
  }

  private void setPicture(final Holder holder, Product product) {
    String imagePath = Constants.General.BLOB_PROTOCOL + product.getDefaultImageAddress();

    ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
    MyApplication.getInstance().getImageLoader().loadImage(imagePath, new SimpleImageLoadingListener() {
      @Override
      public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        //      holder.picture.setImageBitmap(loadedImage);
      }
    });
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.image_view_picture:
        Product product = (Product) v.getTag();
        if (mListener != null)
        {
          mListener.onProductPictureClick(product);
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
    void onProductPictureClick(Product product);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.text_view_name)
    TextView name;
    @Bind (R.id.image_view_picture)
    ComplexAvatarView picture;

    public Holder(View view, boolean isItem) {
      super(view);
      if (isItem)
      {
        ButterKnife.bind(this, view);
      }
    }
  }
}