/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
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
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Product;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchProductsAdapter extends UltimateViewAdapter<SearchProductsAdapter.Holder>
  implements View.OnClickListener {

  private final Context mContext;
  final private List<Product> mProductList;
  private ItemListener mListener;

  public SearchProductsAdapter(Context context, List<Product> productList) {
    this.mProductList = productList;
    mContext = context;
  }

  public void update(Product newProduct, boolean deleted) {
    for (Product post : mProductList)
    {
      if (post.getId().equals(newProduct.getId()))
      {
        int pos = mProductList.indexOf(post);
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
      .inflate(R.layout.inf_search_products_item_grid, parent, false);
    Holder holder = new Holder(view, true);
    holder.picture.setOnClickListener(this);
    holder.name.setOnClickListener(this);

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
    int width = mContext.getResources()
      .getDisplayMetrics().widthPixels / 2;
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, width);

    holder.picture.setTag(item);
    holder.name.setTag(item);

    holder.name.setText(item.getName());
    if (!Utils.isNullOrEmpty(item.getShop().getLat()) && !Utils.isNullOrEmpty(Logged.Models.getUserProfile().getLat())
      && !Logged.Models.getUserProfile().getLat().equalsIgnoreCase("0"))
    {
      int destance = SepehrUtil.distFrom(Float.parseFloat(item.getShop().getLat()), Float.parseFloat(item.getShop().getLong()), Float.parseFloat(Logged.Models.getUserProfile().getLat()), Float.parseFloat(Logged.Models.getUserProfile().getLong()));
      holder.txtDistance.setText(destance + " km");
      holder.txtDistance.setVisibility(View.VISIBLE);
    } else
    {
      holder.txtDistance.setText("? " + " km");

    }
    if (!Utils.isNullOrEmpty(item.getDefaultImageAddress()))
    {
      MyApplication.getInstance().getImageLoader().displayImage(Constants.General.BLOB_PROTOCOL + item.getThumb(), holder.picture, MyApplication.imageOptionsWithAnimation);
    } else
    {
      holder.picture.setImageResource(R.drawable.placeholder);
    }
    holder.picture.setLayoutParams(params);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.image_view_picture:
      case R.id.text_view_name:
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
    ImageView picture;
    @Bind (R.id.txtDistance)
    TextView txtDistance;

    public Holder(View view, boolean isItem) {
      super(view);
      if (isItem)
      {
        ButterKnife.bind(this, view);
      }
    }
  }
}