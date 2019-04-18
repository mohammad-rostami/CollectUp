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
import android.widget.TextView;

import com.collect_up.c_up.R;

import java.util.List;

public class CountryAutoCompleteAdapter extends ArrayAdapter<String> {

  private List<String> dataSet;

  public CountryAutoCompleteAdapter(Context context, List<String> objects) {
    super(context, R.layout.simple_spinner_item);
    dataSet = objects;
  }

  @Override
  public int getCount() {
    return dataSet.size();
  }

  @Override
  public String getItem(int index) {
    String[] data = dataSet.get(index).split(",");
    return data[0];
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null)
    {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.simple_spinner_item, parent, false);
    }
    TextView textView = (TextView) convertView.findViewById(android.R.id.text1);

    textView.setText(getItem(position));

    return convertView;
  }
}
