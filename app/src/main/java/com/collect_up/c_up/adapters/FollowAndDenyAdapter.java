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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.binders.RegisteredContactsForFollowBinder;
import com.collect_up.c_up.adapters.binders.UnregisteredContactsBinder;
import com.collect_up.c_up.adapters.interfaces.InviteCounter;
import com.collect_up.c_up.adapters.providers.Contacts;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.model.Profile;
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class FollowAndDenyAdapter extends UltimateDifferentViewTypeAdapter implements Filterable {

  private final Context mContext;
  public List<Profile> mProfiles = new ArrayList<>();
  private final TreeSet<Integer> mUnregisterPeoplePositions = new TreeSet<>();
  private final InviteCounter mInviteCounterListener;
  private SparseArray<Character> hasHeaderItems = new SparseArray<>();
  private ValueFilter valueFilter;
  private RegisteredContactsForFollowBinder registeredBinder;
  private List<Profile> orig;
  private UnregisteredContactsBinder unRegisteredContact;

  public FollowAndDenyAdapter(Context context, InviteCounter listener) {
    mContext = context;
    mInviteCounterListener = listener;
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
    if (!profileList.isEmpty())
    {
      mProfiles.clear();
      notifyDataSetChanged();

      profileList = removeYourself(profileList);

      mProfiles.addAll(profileList);
      registeredBinder = new RegisteredContactsForFollowBinder(this, mProfiles, mContext);
      putBinder(ContactTypes.TYPE_REGISTERED, registeredBinder);
      notifyDataSetChanged();
    }
  }

  public void addUnregisteredProfiles(List<Contacts.UnRegisteredContact> nameList) {
    if (!nameList.isEmpty())
    {
      for (Contacts.UnRegisteredContact e : nameList)
      {
        Profile profile = new Profile();
        profile.setName(e.getName());
        profile.setPhoneNumber(e.getPhoneNumber());
        mProfiles.add(profile);

        // save the position
        mUnregisterPeoplePositions.add(mProfiles.indexOf(profile));
      }
      unRegisteredContact = new UnregisteredContactsBinder(this, nameList, mContext, mInviteCounterListener);
      putBinder(ContactTypes.TYPE_UNREGISTERED, unRegisteredContact);
      notifyDataSetChanged();
    }
  }

  public void selectAllUnrgisterdContact() {
    unRegisteredContact.selectAll();
  }

  public void unSelectAllUnrgisterdContact() {
    if (unRegisteredContact != null)
    {
      unRegisteredContact.unSelectAll();
    }
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
  public Holder getViewHolder(View view) {
    return new Holder(view);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View v = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.fragment_tabs, viewGroup, false);

    return new Holder(v);
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
    if (i < mProfiles.size())
    {
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
    return -1;
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.item_sticky_header, viewGroup, false);

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

  @Override
  public Filter getFilter() {
    if (valueFilter == null)
    {
      valueFilter = new ValueFilter(this);
    }
    return valueFilter;
  }

  public enum ContactTypes {
    TYPE_REGISTERED,
    TYPE_UNREGISTERED
  }

  public interface ItemListener {
    void onProfilePictureClick(Profile profile);
  }

  public static class Holder extends UltimateRecyclerviewViewHolder {
    public View mView;

    public Holder(View v) {
      super(v);
      mView = v;
    }
  }

  static class StickyHeaderHolder extends UltimateRecyclerviewViewHolder {
    TextView text;

    public StickyHeaderHolder(View itemView) {
      super(itemView);
      text = (TextView) itemView.findViewById(R.id.text_view_header);
    }
  }

  private class ValueFilter extends Filter {
    private final FollowAndDenyAdapter _adapter;

    public ValueFilter(FollowAndDenyAdapter adapter) {
      _adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      final FilterResults oReturn = new FilterResults();
      final List<Profile> results = new ArrayList<Profile>();
      if (orig == null)
      {
        orig = mProfiles;
      }
      if (constraint != null)
      {
        if (orig != null & orig.size() > 0)
        {
          for (final Profile profile : orig)
          {
            try
            {
              if (profile.getName().toLowerCase().contains(constraint.toString().toLowerCase()) && profile.getId() != null)
              {
                results.add(profile);
              }
            } catch (Exception ex)
            {
            }

          }
        }
        oReturn.values = results;
      }
      return oReturn;

    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      mProfiles = (ArrayList<Profile>) results.values;
      clearBinderMap();
      putBinder(ContactTypes.TYPE_REGISTERED, registeredBinder);
      registeredBinder.isSearch(true);
      if (mProfiles != null)
      {
        registeredBinder.setDataSet(mProfiles);
        registeredBinder.notifyBinderDataSetChanged();
        notifyDataSetChanged();
      }
    }
  }
}



