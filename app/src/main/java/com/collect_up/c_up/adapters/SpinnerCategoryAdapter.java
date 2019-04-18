package com.collect_up.c_up.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Category;

/**
 * Created by collect-up3 on 7/27/2016.
 */

public class SpinnerCategoryAdapter extends ArrayAdapter<Category> {

  Category[] categories;
  Context context;

  public SpinnerCategoryAdapter(Context context, int resource, Category[] categories) {
    super(context, resource);
    this.categories = categories;
    this.context = context;
  }

  @Override
  public int getCount() {
    return categories.length;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    TextView label = new TextView(context);
    label.setTextColor(Color.BLACK);
    label.setText(categories[position].getName());

    return label;
  }

  @Override
  public Category getItem(int position) {
    return categories[position];
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    TextView label = new TextView(context);
    label.setTextColor(Color.BLACK);
    label.setPadding(Utils.dpToPx(5), Utils.dpToPx(5), Utils.dpToPx(5), Utils.dpToPx(5));
    label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    label.setText(categories[position].getName());

    return label;
  }
}
