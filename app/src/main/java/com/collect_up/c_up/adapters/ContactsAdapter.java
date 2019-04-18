/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.binders.RegisteredContactsBinder;
import com.collect_up.c_up.adapters.binders.UnregisteredContactsBinder;
import com.collect_up.c_up.adapters.providers.Contacts;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.Profile;
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ContactsAdapter extends UltimateDifferentViewTypeAdapter implements Filterable {

  private final Context mContext;
  private final TreeSet<Integer> mUnregisterPeoplePositions = new TreeSet<>();
  private final boolean mJoinRoom;
  private final CompactChat mChat;
  ValueFilter valueFilter;
  private List<Profile> mFilteredProfiles = new ArrayList<>();
  private List<Profile> mProfiles = new ArrayList<>();
  private RegisteredContactsBinder mRegisteredBinder;
  private SparseArray<Character> hasHeaderItems = new SparseArray<>();

  public ContactsAdapter(Context context, boolean joinRoom, @Nullable CompactChat chat) {
    mContext = context;
    mJoinRoom = joinRoom;
    mChat = chat;
  }

  private List<Profile> removeYourself(List<Profile> profiles) {
    List<Profile> newProfiles = new ArrayList<>();
    for (Profile profile : profiles)
    {
      if (!profile.getId().equals(Logged.Models.getUserProfile().getId()) && !profile.getPhoneNumber().equals(Logged.Models.getUserProfile().getPhoneNumber()))
      {
        newProfiles.add(profile);
      }
    }

    return newProfiles;
  }

  public void addRegisteredProfiles(List<Profile> profileList) {
    mProfiles = new ArrayList<>();
    mFilteredProfiles = new ArrayList<>();

    profileList = removeYourself(profileList);

    mProfiles.addAll(profileList);
    mFilteredProfiles.addAll(profileList);
    mRegisteredBinder = new RegisteredContactsBinder(this, mProfiles, mContext, mJoinRoom, mChat);
    putBinder(ContactTypes.TYPE_REGISTERED, mRegisteredBinder);
    notifyDataSetChanged();
  }

  public void addUnregisteredProfiles(List<Contacts.UnRegisteredContact> nameList) {
    List<Contacts.UnRegisteredContact> unregisteredContacts = new ArrayList<>();
    for (Contacts.UnRegisteredContact e : nameList)
    {
      Profile profile = new Profile();
      profile.setName(e.getName());
      profile.setPhoneNumber(e.getPhoneNumber());
      mProfiles.add(profile);
      mFilteredProfiles.add(profile);
      // save the position
      mUnregisterPeoplePositions.add(mProfiles.indexOf(profile));
      unregisteredContacts.add(e);
    }

    putBinder(ContactTypes.TYPE_UNREGISTERED, new UnregisteredContactsBinder(this, unregisteredContacts, mContext, null));
    notifyDataSetChanged();
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
  public int getItemCount() {
    return mProfiles.size();
  }

  @Override
  public Enum getEnumFromPosition(int i) {
    if (!mUnregisterPeoplePositions.contains(i))
    {
      return ContactTypes.TYPE_REGISTERED;
    } else
    {
      return ContactTypes.TYPE_UNREGISTERED;
    }
  }

  @Override
  public Enum getEnumFromOrdinal(int i) {
    return ContactTypes.values()[i];
  }

  @Override
  public UltimateRecyclerviewViewHolder getViewHolder(View view) {
    return new ViewHolder(view);
  }

  @Override
  public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup viewGroup) {
    View v = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.fragment_tabs, viewGroup, false);
    return new ViewHolder(v);
  }

  @Override
  public int getAdapterItemCount() {
    return mProfiles.size();
  }

  private void addToItemsHasHeader(int position) {
    if (hasHeaderItems.indexOfKey(position) < 0)
    {
      hasHeaderItems.put(position, mProfiles.get(position).getName().toUpperCase().charAt(0));
    }
  }

  @Override
  public long generateHeaderId(int i) {
    String name = mProfiles.get(i).getName();
    if (!mUnregisterPeoplePositions.contains(i) && name.length() > 0)
    {

      if (hasHeaderItems.size() > 0)
      {
        int lastItemHasHeaderKey = hasHeaderItems.keyAt(hasHeaderItems.size() - 1);
        char lastItemHasHeaderChar = hasHeaderItems.get(lastItemHasHeaderKey);

        if (name.toUpperCase().charAt(0) != lastItemHasHeaderChar)
        {
          addToItemsHasHeader(i);
          return name.toUpperCase().charAt(0);
        } else
        {
          return lastItemHasHeaderChar;
        }
      } else
      {
        addToItemsHasHeader(i);
        return name.toUpperCase().charAt(0);
      }

    } else
    {
      // means set the header visibility to gone
      return -1;
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.item_sticky_header, viewGroup, false);

    return new StickyHeaderHolder(view);
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
    if (getHeaderId(i) != -1)
    {
      ((StickyHeaderHolder) viewHolder).text.setVisibility(View.VISIBLE);
      ((StickyHeaderHolder) viewHolder).text.setText(Character.toString((char) Integer.parseInt(Long.toHexString(getHeaderId(i)), 16)));
    } else
    {
      ((StickyHeaderHolder) viewHolder).text.setVisibility(View.GONE);
    }
  }

  public enum ContactTypes {
    TYPE_REGISTERED,
    TYPE_UNREGISTERED
  }

  public static class StickyHeaderHolder extends UltimateRecyclerviewViewHolder {
    TextView text;

    public StickyHeaderHolder(View itemView) {
      super(itemView);
      text = (TextView) itemView.findViewById(R.id.text_view_header);
    }
  }

  static class ViewHolder extends UltimateRecyclerviewViewHolder {
    public View mView;

    public ViewHolder(View v) {
      super(v);
      mView = v;
    }
  }

  private class ValueFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      FilterResults results = new FilterResults();

      if (constraint != null && constraint.length() > 0)
      {
        ArrayList<Profile> filterList = new ArrayList<>();
        for (int i = 0; i < mFilteredProfiles.size(); i++)
        {
          if (mFilteredProfiles.get(i)
            .getName()
            .toLowerCase()
            .contains(constraint.toString().toLowerCase()))
          {
            filterList.add(mFilteredProfiles.get(i));
          }
        }
        results.count = filterList.size();
        results.values = filterList;
      } else
      {
        results.count = mFilteredProfiles.size();
        results.values = mFilteredProfiles;
      }
      return results;
    }

    @Override
    @SuppressWarnings ("unchecked")
    protected void publishResults(CharSequence constraint, FilterResults results) {
      mProfiles = (ArrayList<Profile>) results.values;
      notifyDataSetChanged();
      mRegisteredBinder.setDataSet(mProfiles);
      mRegisteredBinder.notifyBinderDataSetChanged();
    }
  }
}
