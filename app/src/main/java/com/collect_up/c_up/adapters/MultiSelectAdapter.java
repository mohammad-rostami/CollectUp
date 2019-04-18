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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.view.RectangleNetworkImageView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MultiSelectAdapter extends UltimateViewAdapter<MultiSelectAdapter.Holder>
  implements Filterable, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  private final Context mContext;
  ValueFilter valueFilter;
  private List<Profile> mFilteredManagers = new ArrayList<>();
  private List<String> checkedItems = new ArrayList<>();
  private List<Profile> mProfiles = new ArrayList<>();
  private SparseArray<Holder> mSparseArray = new SparseArray<>();
  private List<Profile> allProfiles = new ArrayList<>();

  public MultiSelectAdapter(Context context) {
    mContext = context;
  }

  private List<Profile> removeYourself(List<Profile> profiles) {
    List<Profile> newProfiles = new ArrayList<>();
    for (Profile profile : profiles)
    {
      if (!profile.getId().equals(Logged.Models.getUserProfile().getId()))
      {
        newProfiles.add(profile);
      }
    }

    return newProfiles;
  }

  public void addRegisteredProfiles(List<Profile> profileList) {
    mProfiles = new ArrayList<>();
    mFilteredManagers = new ArrayList<>();

    profileList = removeYourself(profileList);

    mProfiles.addAll(profileList);
    mFilteredManagers.addAll(profileList);
    allProfiles.addAll(profileList);

    notifyDataSetChanged();
  }

  public List<Profile> getCheckedProfileItems() {
    List<Profile> profileList = new ArrayList<>();
    for (String id : checkedItems)
    {
      for (Profile profile : mProfiles)
      {
        if (profile.getId().equals(id))
        {
          profileList.add(profile);
        }
      }
    }
    return profileList;
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
      .inflate(R.layout.inf_add_contact_multi_select, viewGroup, false);

    Holder holder = new Holder(view, true);

    holder.select.setOnCheckedChangeListener(this);
    holder.itemView.setOnClickListener(this);

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
    if (mSparseArray.indexOfKey(position) < 0)
    {
      mSparseArray.put(position, holder);
    }
    Profile item = mProfiles.get(position);

    HashMap<String, Integer> tag = new HashMap<>();
    tag.put(item.getId(), allProfiles.indexOf(item));

    holder.name.setText(item.getName());
    holder.username.setText(item.getUsername());

    holder.itemView.setTag(tag);
    holder.select.setTag(tag);

    if (!Utils.isNullOrEmpty(item.getImageAddress()))
    {
      holder.picture.clearDrawableBeforeDetached();
      holder.picture.setImageUrl(Constants.General.BLOB_PROTOCOL + item.getThumb(), MyApplication.getInstance().getImageLoader());
    } else
    {
      holder.picture.clearDrawableBeforeDetached();
      holder.picture.setImageResource(R.drawable.placeholder);
    }

    holder.select.setChecked(checkedItems.contains(item.getId()));
  }

  private Holder getHolderByPosition(int position) {
    return mSparseArray.valueAt(position);
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    HashMap<String, Integer> tag = (HashMap<String, Integer>) buttonView.getTag();
    String id = tag.keySet().iterator().next();
    if (isChecked && !checkedItems.contains(id))
    {
      checkedItems.add(id);
    } else if (!isChecked && checkedItems.contains(id))
    {
      checkedItems.remove(id);
    }
  }

  @Override
  public void onClick(View v) {
    HashMap<String, Integer> tag = (HashMap<String, Integer>) v.getTag();
    int position = tag.values().iterator().next();
    Holder holder = getHolderByPosition(position);
    if (holder != null)
    {
      if (!holder.select.isChecked())
      {
        holder.select.setTag(tag);
        holder.select.setChecked(true);
      } else
      {
        holder.select.setTag(tag);
        holder.select.setChecked(false);
      }
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.item_sticky_header, viewGroup, false);

    return new StickyHeaderHolder(view);
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
    ((StickyHeaderHolder) viewHolder).text.setText(Character.toString((char) Integer.parseInt(Long.toHexString(getHeaderId(i)), 16)));
  }

  public static class StickyHeaderHolder extends UltimateRecyclerviewViewHolder {
    TextView text;

    public StickyHeaderHolder(View itemView) {
      super(itemView);
      text = (TextView) itemView.findViewById(R.id.text_view_header);
    }
  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.checkbox_select)
    CheckBox select;
    @Bind (R.id.text_view_name)
    TextView name;
    @Bind (R.id.image_view_picture)
    RectangleNetworkImageView picture;
    @Bind (R.id.text_view_username)
    TextView username;

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
