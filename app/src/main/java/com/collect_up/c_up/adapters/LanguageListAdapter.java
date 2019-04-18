/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.rey.material.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.List;

public class LanguageListAdapter extends ArrayAdapter<String> implements View.OnClickListener {

  public ArrayList<String> selectedLanguages = new ArrayList<>();
  private Context mContext;

  public LanguageListAdapter(Context context, List<String> objects, ArrayList<String> alreadySelected) {
    super(context, R.layout.list_item_checked, objects);
    mContext = context;
    selectedLanguages.addAll(alreadySelected);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null)
    {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.list_item_checked, parent, false);

      holder = new ViewHolder();
      holder.languageName = (CheckedTextView) convertView.findViewById(android.R.id.text1);
      convertView.setTag(holder);
    }
    holder = (ViewHolder) convertView.getTag();
    holder.languageName.setText(getItem(position));
    holder.languageName.setOnClickListener(this);
    holder.languageName.setTag(getItem(position));

    if (selectedLanguages.contains(getItem(position)))
    {
      holder.languageName.setChecked(true);
    } else
    {
      holder.languageName.setChecked(false);
    }

    return convertView;
  }

  @Override
  public void onClick(View v) {
    if (selectedLanguages.contains((String) v.getTag()))
    {
      selectedLanguages.remove((String) v.getTag());
      ((CheckedTextView) v).setChecked(false);
      return;
    }

    if (selectedLanguages.size() < Constants.General.MAX_SELECT_LANGUAGE)
    {
      selectedLanguages.add((String) v.getTag());
      ((CheckedTextView) v).setChecked(true);
    } else
    {
      ((CheckedTextView) v).setChecked(false);
      Toast.makeText(mContext, R.string.toast_error_max_select_language_exceed, Toast.LENGTH_SHORT).show();
    }
  }

  static class ViewHolder {
    CheckedTextView languageName;
  }
}
