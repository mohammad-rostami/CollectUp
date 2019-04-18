/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.BaseModel;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.services.Location;
import com.collect_up.c_up.view.RectangleNetworkImageView;
import com.google.android.gms.maps.model.LatLng;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.rey.material.widget.Button;
import com.rey.material.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class ShopComplexAddAdapter extends UltimateViewAdapter<ShopComplexAddAdapter.Holder>
  implements Filterable, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

  private final Shop mShop;
  private final Context mContext;
  private final List<Complex> mFilteredComplex = new ArrayList<>();
  ValueFilter valueFilter;
  private ItemListener mListener;
  private List<Complex> mComplex = new ArrayList<>();
  private int checkedItem = -1;
  private SparseArray<Holder> holderList = new SparseArray<>();

  public ShopComplexAddAdapter(Context context,
                               List<Complex> complexList,
                               Shop shop) {
    mContext = context;
    mComplex.addAll(complexList);
    mShop = shop;
    mFilteredComplex.addAll(complexList);
  }

  @Override
  public Filter getFilter() {
    if (valueFilter == null)
    {
      valueFilter = new ValueFilter();
    }
    return valueFilter;
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.image_view_picture:
      case R.id.text_view_title:
        Complex complex = (Complex) v.getTag();
        if (complex.getName().equalsIgnoreCase("none") && complex.getAdminId() == null)
        {
        } else if (mListener != null)
        {
          mListener.onProfilePictureClick(complex);
        }
        break;
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.inf_business_add_complex, viewGroup, false);
    Holder holder = new Holder(view, true);
    holder.select.setOnCheckedChangeListener(this);
    holder.picture.setOnClickListener(this);
    holder.title.setOnClickListener(this);
    return holder;
  }

  @Override
  public int getItemCount() {
    super.getItemCount();
    return mComplex.size();
  }

  @Override
  public int getAdapterItemCount() {
    return mComplex.size();
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public void onBindViewHolder(final Holder holder, int position) {
    if (holder.title == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }

    if (holderList.indexOfKey(position) < 0)
    {
      holderList.put(position, holder);
    }
    final Complex item = mComplex.get(position);

    switch (item.getBusinessLevel())
    {
      case BaseModel.BUSINESS_LEVEL_NONE:
        holder.imgBusinessLevel.setVisibility(View.GONE);
        break;
      case BaseModel.BUSINESS_LEVEL_DIAMOND:
        holder.imgBusinessLevel.setVisibility(View.VISIBLE);
        holder.imgBusinessLevel.setImageResource(R.drawable.businesslevel_diamond);
        break;
      case BaseModel.BUSINESS_LEVEL_GOLD:
        holder.imgBusinessLevel.setVisibility(View.VISIBLE);
        holder.imgBusinessLevel.setImageResource(R.drawable.businesslevel_gold);
        break;
      case BaseModel.BUSINESS_LEVEL_SILVER:
        holder.imgBusinessLevel.setVisibility(View.VISIBLE);
        holder.imgBusinessLevel.setImageResource(R.drawable.businesslevel_silver);
        break;
      case BaseModel.BUSINESS_LEVEL_BRONZE:
        holder.imgBusinessLevel.setVisibility(View.VISIBLE);
        holder.imgBusinessLevel.setImageResource(R.drawable.businesslevel_bronze);
        break;
    }
    if (Logged.Models.getUserShop() != null && item.getRequests().contains(Logged.Models.getUserShop().getId()))
    {
      holder.btnRequested.setVisibility(View.VISIBLE);
      holder.select.setVisibility(View.GONE);
      holder.btnRequested.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
          HttpClient.get(String.format(Constants.Server.Request.CANCEL_REQUEST_SHOP_TO_COMPLEX, Logged.Models.getUserShop().getId(), item.getId()), new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              holder.btnRequested.setVisibility(View.GONE);
              holder.select.setVisibility(View.VISIBLE);
            }


            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);

            }
          });
        }
      });

    } else
    {
      holder.btnRequested.setVisibility(View.GONE);
      holder.select.setVisibility(View.VISIBLE);
    }
    holder.title.setText(item.getName());
    holder.picture.setTag(item);
    holder.title.setTag(item);
    holder.select.setTag(position);

    AsyncTask<Void, Void, String> countryAndCityAsync = new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... params) {
        LatLng latLng = item.getLong() != null || item.getLat() != null ? new LatLng(Double.valueOf(item.getLat()), Double.valueOf(item.getLong())) : new LatLng(0, 0);
        return Location.getCountryAndCity(mContext, latLng);
      }

      @Override
      protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (!Utils.isNullOrEmpty(s))
        {
          holder.location.setVisibility(View.VISIBLE);
          holder.location.setText(s);

        }
      }
    };
    countryAndCityAsync.execute();

    if (item.getImageAddress() != null)

    {
      holder.picture.clearDrawableBeforeDetached();
      holder.picture.setImageUrl(Constants.General.BLOB_PROTOCOL + item.getThumb(), MyApplication.getInstance().getImageLoader());
    } else

    {
      if (item.getName().equalsIgnoreCase("none") && item.getAdminId() == null)
      {
        holder.picture.setVisibility(View.GONE);
      } else
      {
        holder.picture.setVisibility(View.VISIBLE);
        holder.picture.clearDrawableBeforeDetached();
        holder.picture.setImageResource(R.drawable.placeholder);
      }
    }

    if ((checkedItem != -1 && checkedItem == position)
      || ((checkedItem == -1 || checkedItem == 0) && (mShop.getComplex() != null && mComplex.get(position).getId() != null && mComplex.get(position).getId().equals(mShop.getComplex().getId()))))

    {
      holder.select.setChecked(true);
    } else

    {
      holder.select.setChecked(false);
    }
    setCheckedNone(checkedItem);
  }

  private void setCheckedNone(int checkedItem) {
    if (getSelectedItem() == null && checkedItem == -1)
    {
      holderList.valueAt(0).select.setChecked(true);
    }

  }

  public Complex getSelectedItem() {
    Complex value = null;
    for (int i = 0; i < holderList.size(); i++)
    {
      Holder item = holderList.valueAt(i);
      if (item.getLayoutPosition() == checkedItem)
      {
        value = (Complex) item.picture.getTag();
      }
    }
    return value;
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int holder = (int) buttonView.getTag();

    if (isChecked)
    {
      for (int i = 0; i < holderList.size(); i++)
      {
        Holder item = holderList.valueAt(i);
        if (checkedItem != -1 && item.getLayoutPosition() != holder)
        {
          item.select.setChecked(false);
        }
      }
      checkedItem = holder;
    }
  }

  public void setListener(ItemListener listener) {
    mListener = listener;
  }

  public interface ItemListener {
    void onProfilePictureClick(Complex complex);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.text_view_location)
    TextView location;
    @Bind (R.id.image_view_picture)
    RectangleNetworkImageView picture;
    @Bind (R.id.radio_button_select)
    RadioButton select;
    @Bind (R.id.text_view_title)
    TextView title;
    @Bind (R.id.btnRequested)
    Button btnRequested;
    @Bind (R.id.imgBusinessLevel)
    ImageView imgBusinessLevel;


    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }

  private class ValueFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      FilterResults results = new FilterResults();

      if (constraint != null && constraint.length() > 0)
      {
        ArrayList<Complex> filterList = new ArrayList<>();
        for (int i = 0; i < mFilteredComplex.size(); i++)
        {
          if ((mFilteredComplex.get(i)
            .getName()
            .toLowerCase()).contains(constraint.toString()
            .toLowerCase()))
          {
            filterList.add(mFilteredComplex.get(i));
          }
        }
        results.count = filterList.size();
        results.values = filterList;
      } else
      {
        results.count = mFilteredComplex.size();
        results.values = mFilteredComplex;
      }

      return results;
    }

    @Override
    @SuppressWarnings ("unchecked")
    protected void publishResults(CharSequence constraint, FilterResults results) {
      mComplex = (ArrayList<Complex>) results.values;
      notifyDataSetChanged();
    }
  }
}
