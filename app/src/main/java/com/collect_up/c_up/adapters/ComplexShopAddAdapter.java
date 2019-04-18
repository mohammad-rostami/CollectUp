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
import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class ComplexShopAddAdapter extends UltimateViewAdapter<ComplexShopAddAdapter.ViewHolder>
        implements Filterable, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private final Context mContext;
    private final List<Shop> mFilteredShops = new ArrayList<>();
    ValueFilter valueFilter;
    private List<String> checkedItems = new ArrayList<>();
    private ItemListener mListener;
    private List<Shop> mShops = new ArrayList<>();

    public ComplexShopAddAdapter(Context context, List<Shop> shopList, Complex complex) {
        mContext = context;
        mShops.addAll(shopList);
        mFilteredShops.addAll(shopList);

        for (Shop shop : mShops) {
            if ((complex.getShopsId() != null && complex.getShopsId().contains(shop.getId()) &&
                    shop.getId() != null &&
                    !checkedItems.contains(shop.getId()) &&
                    shop.getComplex() != null) && Logged.Models.getUserComplex().getShopsId().contains(shop.getId())) {
                checkedItems.add(shop.getId());
            }
        }
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_picture:
                Shop shop = (Shop) v.getTag();
                if (mListener != null) {
                    mListener.onProfilePictureClick(shop);
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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.inf_complex_add_business, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view, true);
        viewHolder.select.setOnCheckedChangeListener(this);
        viewHolder.picture.setOnClickListener(this);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        super.getItemCount();
        return mShops.size();
    }

    @Override
    public int getAdapterItemCount() {
        return mShops.size();
    }

    @Override
    public long generateHeaderId(int i) {
        return 0;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (holder.title == null) {
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            holder.itemView.setLayoutParams(params1);

            return;
        }

        final Shop item = mShops.get(position);
        switch (item.getBusinessLevel()) {
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
        if (Logged.Models.getUserComplex() != null && item.getRequests().contains(Logged.Models.getUserComplex().getId())) {
            holder.select.setVisibility(View.GONE);
            holder.btnRequested.setVisibility(View.VISIBLE);
            holder.btnRequested.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HttpClient.get(String.format(Constants.Server.Request.CANCEL_REQUEST_COMPLEX_TO_SHOP, item.getId(), Logged.Models.getUserComplex().getId()), new AsyncHttpResponser(mContext) {
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
        } else {
            holder.select.setVisibility(View.VISIBLE);
            holder.btnRequested.setVisibility(View.GONE);
        }
        holder.title.setText(item.getName());
        holder.picture.setTag(item);
        holder.select.setTag(item.getId());

        AsyncTask<Void, Void, String> countryAndCityAsync = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                LatLng latLng = new LatLng(Double.valueOf(item.getLat()), Double.valueOf(item.getLong()));
                return Location.getCountryAndCity(mContext, latLng);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (!Utils.isNullOrEmpty(s)) {
                    holder.location.setText(s);
                }
            }
        };
        countryAndCityAsync.execute();


        if (item.getImageAddress() != null) {
            holder.picture.clearDrawableBeforeDetached();
            holder.picture.setImageUrl(Constants.General.BLOB_PROTOCOL + item.getThumb(), MyApplication.getInstance().getImageLoader());
        } else {
            holder.picture.clearDrawableBeforeDetached();
            holder.picture.setImageResource(R.drawable.placeholder);
        }

        holder.select.setChecked(checkedItems.contains(item.getId()));
    }

    public List<String> getCheckedItems() {
        return checkedItems;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String id = (String) buttonView.getTag();
        if (isChecked && !checkedItems.contains(id)) {
            checkedItems.add((String) buttonView.getTag());
        } else if (!isChecked && checkedItems.contains(id)) {
            checkedItems.remove(buttonView.getTag());
        }
    }

    public void setListener(ItemListener listener) {
        mListener = listener;
    }

    public interface ItemListener {
        void onProfilePictureClick(Shop shop);
    }

    static class ViewHolder extends UltimateRecyclerviewViewHolder {
        @Bind(R.id.text_view_location)
        TextView location;
        @Bind(R.id.image_view_picture)
        RectangleNetworkImageView picture;
        @Bind(R.id.checkbox_select)
        CheckBox select;
        @Bind(R.id.text_view_title)
        TextView title;
        @Bind(R.id.btnRequested)
        Button btnRequested;
        @Bind(R.id.imgBusinessLevel)
        ImageView imgBusinessLevel;

        public ViewHolder(View itemView, boolean isItem) {
            super(itemView);
            if (isItem) {
                ButterKnife.bind(this, itemView);
            }
        }
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<Shop> filterList = new ArrayList<>();
                for (int i = 0; i < mFilteredShops.size(); i++) {
                    if ((mFilteredShops.get(i)
                            .getName()
                            .toLowerCase()).contains(constraint.toString()
                            .toLowerCase())) {
                        filterList.add(mFilteredShops.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mFilteredShops.size();
                results.values = mFilteredShops;
            }

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mShops = (ArrayList<Shop>) results.values;
            notifyDataSetChanged();
        }
    }
}
