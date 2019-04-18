package com.collect_up.c_up.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.interfaces.OnRemoveItemLisetener;
import com.collect_up.c_up.model.Category;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.CompoundButton;

import java.util.List;

/**
 * Created by collect-up3 on 1/22/2017.
 */

public class MultiChoiceAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {

  private final boolean isRemovableItem;
  private List<Category> mItems;
  private boolean[] mSelected;
  private int mLastSelectedIndex;
  private SimpleDialog.OnSelectionChangedListener mOnSelectionChangedListener;
  private ListView mListView;
  private OnRemoveItemLisetener removeListener;

  public MultiChoiceAdapter(ListView listView, boolean isRemovableItem, SimpleDialog.OnSelectionChangedListener listener) {
    this.mOnSelectionChangedListener = listener;
    this.mListView = listView;
    this.isRemovableItem = isRemovableItem;
  }

  public void setRemoveListener(OnRemoveItemLisetener removeListener) {
    this.removeListener = removeListener;
  }

  public void setItems(List<Category> items, int... selectedIndexes) {
    mItems = items;

    if (mSelected == null || mSelected.length != items.size())
    {
      mSelected = new boolean[items.size()];
    }

    for (int i = 0; i < mSelected.length; i++)
      mSelected[i] = false;

    if (selectedIndexes != null)
    {
      for (int index : selectedIndexes)
        if (index >= 0 && index < mSelected.length)
        {
          mSelected[index] = true;
          mLastSelectedIndex = index;
        }
    }

    notifyDataSetChanged();
  }

  public int getLastSelectedIndex() {
    return mLastSelectedIndex;
  }

  public CharSequence getLastSelectedValue() {
    return mItems.get(mLastSelectedIndex).getName();
  }

  public int[] getSelectedIndexes() {
    int count = 0;
    for (int i = 0; i < mSelected.length; i++)
      if (mSelected[i])
      {
        count++;
      }

    if (count == 0)
    {
      return null;
    }

    int[] result = new int[count];
    count = 0;
    for (int i = 0; i < mSelected.length; i++)
      if (mSelected[i])
      {
        result[count] = i;
        count++;
      }

    return result;
  }

  public CharSequence[] getSelectedValues() {
    int count = 0;
    for (int i = 0; i < mSelected.length; i++)
      if (mSelected[i])
      {
        count++;
      }

    if (count == 0)
    {
      return null;
    }

    CharSequence[] result = new CharSequence[count];
    count = 0;
    for (int i = 0; i < mSelected.length; i++)
      if (mSelected[i])
      {
        result[count] = mItems.get(i).getName();
        count++;
      }

    return result;
  }

  @Override
  public int getCount() {
    return mItems == null ? 0 : mItems.size();
  }

  @Override
  public Object getItem(int position) {
    return mItems == null ? 0 : mItems.get(position);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    View view = convertView;
    if (view == null)
    {
      view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multichoice, null);
    }
    Object item = getItem(position);

    if (item != null)
    {
      CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
      checkBox.setTag(position);
      checkBox.setOnCheckedChangeListener(this);
      checkBox.setText(mItems.get(position).getName());
      checkBox.setCheckedImmediately(mSelected[position]);

      ImageView btnRemove = (ImageView) view.findViewById(R.id.btnRemove);
      if (!isRemovableItem)
      {
        btnRemove.setVisibility(View.GONE);
      } else
      {
        btnRemove.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            removeListener.onRemove(position, mItems.get(position).getId());
          }
        });
      }
    }


    return view;
  }

  @Override
  public void onCheckedChanged(android.widget.CompoundButton v, boolean isChecked) {
    int position = (Integer) v.getTag();
    if (mSelected[position] != isChecked)
    {
      mSelected[position] = isChecked;

      if (mOnSelectionChangedListener != null)
      {
        mOnSelectionChangedListener.onSelectionChanged(position, mSelected[position]);
      }
    }
  }

}
