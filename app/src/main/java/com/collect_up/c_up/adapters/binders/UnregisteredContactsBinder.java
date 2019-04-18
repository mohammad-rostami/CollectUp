/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters.binders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.interfaces.InviteCounter;
import com.collect_up.c_up.adapters.providers.Contacts;
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.multiViewTypes.DataBinder;
import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UnregisteredContactsBinder extends DataBinder<UnregisteredContactsBinder.ViewHolder> {
  private InviteCounter mInviteCounter;
  private ArrayList<Contacts.UnRegisteredContact> invited = new ArrayList<>();
  private List<Contacts.UnRegisteredContact> dataSet;

  public UnregisteredContactsBinder(UltimateDifferentViewTypeAdapter dataBindAdapter,
                                    List<Contacts.UnRegisteredContact> dataSet,
                                    Context context, InviteCounter listener) {
    super(dataBindAdapter);
    this.dataSet = dataSet;
    mInviteCounter = listener;
  }


  @Override
  public ViewHolder newViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.inf_contacts_invite, parent, false);

    ViewHolder viewHolder = new ViewHolder(view);

    return viewHolder;
  }

  @Override
  public void bindViewHolder(final ViewHolder holder, final int position) {
    try
    {


      final Contacts.UnRegisteredContact item = dataSet.get(position - Contacts.getRegisteredContactCount());
      HashMap<String, Integer> tag = new HashMap<>();
      tag.put(item.getName(), position - Contacts.getRegisteredContactCount());
      holder.chkInvite.setTag(tag);
      holder.itemView.setTag(tag);
      holder.name.setTag(item.getPhoneNumber());

      holder.name.setText(item.getName());
      holder.chkInvite.setOnCheckedChangeListener(null);
      if (item.getChecked())
      {
        holder.chkInvite.setChecked(true);

      } else
      {

        holder.chkInvite.setChecked(false);
      }
      holder.chkInvite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          item.setChecking(isChecked);
          if (isChecked)
          {
            invited.add(item);
          } else
          {
            invited.remove(item);
          }
          mInviteCounter.countChecked(invited);
        }
      });
      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          holder.chkInvite.toggle();

        }
      });
    } catch (Exception ex)
    {
      return;
    }
  }

  public void selectAll() {
    for (int i = 0; i < dataSet.size(); i++)
    {
      dataSet.get(i).setChecking(true);
      invited.add(dataSet.get(i));
    }

    mInviteCounter.countChecked(invited);

  }

  public void unSelectAll() {
    if (dataSet != null)
    {
      for (int i = 0; i < dataSet.size(); i++)
      {
        dataSet.get(i).setChecking(false);

      }
      invited.clear();
    }
  }


  @Override
  public int getItemCount() {
    return dataSet.size() + Contacts.getRegisteredContactCount();
  }


  static class ViewHolder extends UltimateRecyclerviewViewHolder {
    TextView name;
    CheckBox chkInvite;


    public ViewHolder(View view) {
      super(view);
      name = (TextView) view.findViewById(R.id.text_view_name);
      chkInvite = (CheckBox) view.findViewById(R.id.checkbox_invite_contact);
    }
  }
}
