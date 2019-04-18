/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ManagerAddAdapter extends UltimateViewAdapter<ManagerAddAdapter.Holder>
  implements Filterable, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  private Shop mShop;
  private Complex mComplex;
  private final List<Profile> mFilteredManagers = new ArrayList<>();
  ValueFilter valueFilter;
  private List<String> checkedItems = new ArrayList<>();
  private ItemListener mListener;
  private List<Profile> mProfiles = new ArrayList<>();

  public ManagerAddAdapter(Object data) {
    if (data instanceof Shop)
    {
      mShop = (Shop) data;

    } else
    {
      mComplex = (Complex) data;
    }
  }

  public void addManagers(List<Profile> profileList) {
    mProfiles.addAll(profileList);
    mFilteredManagers.addAll(profileList);

    for (Profile profile : mProfiles)
    {
      if (mShop != null)
      {
        if (mShop.getManagersId() != null && mShop.getManagersId()
          .contains(profile.getId()) && !checkedItems.contains(profile.getId()))
        {
          checkedItems.add(profile.getId());
        }
      } else
      {
        if (mComplex.getManagersId() != null && mComplex.getManagersId()
          .contains(profile.getId()) && !checkedItems.contains(profile.getId()))
        {
          checkedItems.add(profile.getId());
        }
      }
    }

    notifyDataSetChanged();
  }

  public List<String> getCheckedItems() {
    return checkedItems;
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
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.inf_add_manager, viewGroup, false);
    Holder holder = new Holder(view, true);

    holder.picture.setOnClickListener(this);
    holder.select.setOnCheckedChangeListener(this);

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    return mProfiles.size();
  }

  @Override
  public long generateHeaderId(int i) {
    String name = mProfiles.get(i).getName();
    if (name.length() > 0)
    {
      return name.charAt(0);
    } else
    {
      return -1;
    }
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    Profile item = mProfiles.get(position);

    holder.name.setText(item.getName());
    holder.name.setTag(item.getId());
    holder.username.setText(item.getUsername());
    holder.username.setTag(item);
    holder.picture.setTag(item);
    holder.select.setTag(item.getId());
    if (item != null && item.isOfficial())
    {
      holder.imgOfficial.setVisibility(View.VISIBLE);

    } else
    {
      holder.imgOfficial.setVisibility(View.GONE);
    }
    if (!Utils.isNullOrEmpty(item.getImageAddress()))
    {
      holder.picture.makeAllDefaults();

      holder.picture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(item.getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + item.getThumb());
    } else
    {
      holder.picture.makeAllDefaults();
      holder.picture.setText(item.getName());
    }

    holder.select.setChecked(checkedItems.contains(item.getId()));
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    String id = (String) buttonView.getTag();
    if (isChecked && !checkedItems.contains(id))
    {
      checkedItems.add((String) buttonView.getTag());
    } else if (!isChecked && checkedItems.contains(id))
    {
      checkedItems.remove(buttonView.getTag());
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.image_view_picture:
        Profile profile = (Profile) v.getTag();
        if (mListener != null)
        {
          mListener.onProfilePictureClick(profile);
        }
        break;
    }
  }

  public void setListener(ItemListener listener) {
    mListener = listener;
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  public interface ItemListener {
    void onProfilePictureClick(Profile profile);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.checkbox_select)
    CheckBox select;
    @Bind (R.id.text_view_name)
    TextView name;
    @Bind (R.id.image_view_picture)
    ComplexAvatarView picture;
    @Bind (R.id.text_view_username)
    TextView username;
    @Bind (R.id.imgOfficial)
    ImageView imgOfficial;

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
        ArrayList<Profile> filterList = new ArrayList<>();
        for (int i = 0; i < mFilteredManagers.size(); i++)
        {
          if ((mFilteredManagers.get(i)
            .getName()
            .toLowerCase()).contains(constraint.toString()
            .toLowerCase()))
          {
            filterList.add(mFilteredManagers.get(i));
          }
        }
        results.count = filterList.size();
        results.values = filterList;
      } else
      {
        results.count = mFilteredManagers.size();
        results.values = mFilteredManagers;
      }

      return results;
    }

    @Override
    @SuppressWarnings ("unchecked")
    protected void publishResults(CharSequence constraint, FilterResults results) {
      mProfiles = (ArrayList<Profile>) results.values;
      notifyDataSetChanged();
    }
  }
}
