package com.collect_up.c_up.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.MultiChoiceAdapter;
import com.collect_up.c_up.adapters.interfaces.OnRemoveItemLisetener;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Category;
import com.rey.material.app.SimpleDialog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by collect-up3 on 7/31/2016.
 */
public class MultiSpinner extends AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener {

    private OnRemoveItemLisetener removeListener;
    private MultiChoiceAdapter adapter;
    private SimpleDialog builder;
    private int[] selectedIndex;

    public interface OnMultipleItemsSelectedListener {

        void selectedItem(List<Category> strings);
    }

    private OnMultipleItemsSelectedListener listener;

    List<Category> _items = null;
    List<String> _String = new ArrayList<>();
    int[] mSelection = null;
    //   int[] mSelectionAtStart = null;
    // String _itemsAtStart = null;

    ArrayAdapter<String> simple_adapter;

    public MultiSpinner(Context context) {
        super(context);

        simple_adapter = new ArrayAdapter<>(context,
                R.layout.spinner_item);
        super.setAdapter(simple_adapter);
    }

    public MultiSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        simple_adapter = new ArrayAdapter<>(context,
                R.layout.spinner_item);
        super.setAdapter(simple_adapter);
    }

    public void setListener(OnMultipleItemsSelectedListener listener) {
        // _String = new String[_items.size()];
        for (int i = 0; i < _items.size(); i++) {
            _String.add(_items.get(i).getName());
        }
        this.listener = listener;
    }

    public void setRemoveListener(OnRemoveItemLisetener listener) {
        this.removeListener = listener;
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        int checked = isChecked ? 1 : 0;
        if (mSelection != null && which < mSelection.length) {
            mSelection[which] = checked;//set(which, checked);
            //       simple_adapter.clear();
            //     simple_adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        // setSelection(simple_adapter.getItem(0));
        if (mSelection != null) {
            ArrayList<Integer> arrayIndex = new ArrayList<>();
            for (int i = 0; i < mSelection.length; i++) {
                if (mSelection[i] == 1) {
                    arrayIndex.add(i);
                }
            }

            selectedIndex = new int[arrayIndex.size()];

            for (int i = 0; i < arrayIndex.size(); i++) {
                selectedIndex[i] = arrayIndex.get(i);
            }
        }
              /*if (mSelection.size() > 0) {
            mSelection.clear();
            for (int i = 0; i < _items.size(); i++) {
                mSelection.add(i, 0);
            }
        }*/
        if (_items == null || _items.size() == 0) {
            // do anything
        } else if (_items.get(0).getId().equals("0")) {
            listener.selectedItem(getSelectedItem());

            //   showNewCatDialog();
            // mDialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        } else {
            builder = new SimpleDialog(getContext());
            builder.title("Please select category");
            builder.maxHeight(SepehrUtil.getScreenHeight((Activity) getContext()) / 2);
            builder.titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            builder.layoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Utils.getScreenWidthPX(getContext()));
            builder.contentView(R.layout.multichoice_layout);

            ListView checkList = (ListView) builder.findViewById(R.id.checkList);
            adapter = new MultiChoiceAdapter(checkList, (removeListener == null ? false : true), new SimpleDialog.OnSelectionChangedListener() {
                @Override
                public void onSelectionChanged(int index, boolean selected) {
                    int selectedIndex = selected ? 1 : 0;
                    mSelection[index] = selectedIndex;
                }
            });
            adapter.setRemoveListener(new OnRemoveItemLisetener() {
                @Override
                public void onRemove(int index, String id) {
                    removeListener.onRemove(index, id);

                }
            });
            if (selectedIndex != null) {
                adapter.setItems(_items, selectedIndex);
            }
            checkList.setAdapter(adapter);
      /*builder.multiChoiceItems(_String, selectedIndex)
        .onSelectionChangedListener(new SimpleDialog.OnSelectionChangedListener() {
          @Override
          public void onSelectionChanged(int index, boolean selected) {
            int selectedIndex = selected ? 1 : 0;
            mSelection[index] = selectedIndex;//set(index, selectedIndex);//[index] = selectedIndex;
          }
        });*/
            //   _itemsAtStart = getSelectedItemsAsString();
            builder.positiveAction("Submit").positiveActionClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // System.arraycopy(mSelection, 0, mSelectionAtStart, 0, mSelection.length);
                    if (listener != null) {
                        listener.selectedItem(getSelectedItem());
                    }
                    //    listener.selectedStrings(getSelectedStrings());
                    builder.dismiss();
                }
            });
            builder.negativeAction("Cancel").negativeActionClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  simple_adapter.clear();
                    //     simple_adapter.add(_itemsAtStart);
                    //    System.arraycopy(mSelectionAtStart, 0, mSelection, 0, mSelectionAtStart.length);
                    builder.dismiss();

                }
            });
            builder.show();
        }
        return true;
    }

    public void removeItem(int index) {
        _items.remove(index);
        adapter.notifyDataSetChanged();


    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(List<Category> items) {
        _items = items;
        mSelection = new int[_items.size()];
        for (int i = 0; i < _items.size(); i++) {
            mSelection[i] = 0;///add(i, 0);
        }
        if (_items.get(0).getId().equals("0")) {
            mSelection[0] = 1;//set(0, 1);
            simple_adapter.add(_items.get(0).getName());
        }
        // mSelection[0] = 1;//set(0, 1);
        // simple_adapter.add(_items.get(0).getName());
        //   mSelectionAtStart = new int[_items.size()];
        //    simple_adapter.clear();
        //   simple_adapter.add(_items.get(0).getName());
        //Arrays.fill(mSelection, 0);
        //   mSelection[0] = 1;
        // mSelectionAtStart[0] = 1;
    }

    public void setSelection(List<String> selection) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = 0;//set(i, 0);///[i] = 0;
        }
        for (String sel : selection) {
            for (int j = 0; j < _items.size(); ++j) {
                if (_items.get(j).getName().equals(sel)) {
                    mSelection[j] = 1;//(j, 1);//[j] = 1;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }
 /*   public void setItems(List<String> items) {
        _items = items.toArray(new String[items.size()]);
        mSelection = new boolean[_items.length];
        mSelectionAtStart = new boolean[_items.length];
        simple_adapter.clear();
        simple_adapter.add(_items[0]);
        Arrays.fill(mSelection, false);
        mSelection[0] = true;
    }*/

    public void setSelectionById(ArrayList<String> ids) {

        for (String cell : ids) {
            for (int j = 0; j < _items.size(); ++j) {
                if (_items.get(j).getId().equals(cell)) {
                    mSelection[j] = 1;//.set(j, 1);
                }
            }
        }
    }

    public void setSelection(String selected) {
        String[] splitedItems = selected.split("\\, ");

        for (String cell : splitedItems) {
            for (int j = 0; j < _items.size(); ++j) {
                if (_items.get(j).getName().equals(cell)) {
                    mSelection[j] = 1;//.set(j, 1);
                }
            }
        }
        // simple_adapter.clear();
        //Fsimple_adapter.add(buildSelectedItemString());
    }

/*

    public void setSelection(int index) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = 0;
            mSelectionAtStart[i] = 0;
        }
        if (index >= 0 && index < mSelection.length) {
            mSelection[index] = 1;
            mSelectionAtStart[index] = 1;
        } else {
            throw new IllegalArgumentException("Index " + index
                    + " is out of bounds.");
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(int[] selectedIndices) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = 0;
            mSelectionAtStart[i] = 0;
        }
        for (int index : selectedIndices) {
            if (index >= 0 && index < mSelection.length) {
                mSelection[index] = 1;
                mSelectionAtStart[index] = 1;
            } else {
                throw new IllegalArgumentException("Index " + index
                        + " is out of bounds.");
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }
*/


    public List<Category> getSelectedItem() {
        List<Category> selection = new LinkedList<>();
        if (_items != null) {
            for (int i = 0; i < _items.size(); ++i) {
                if (mSelection[i] == 1) {
                    selection.add(_items.get(i));
                }
            }
        }
        return selection;
    }

    public void closeDialog() {
        builder.dismiss();
    }

    public String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.size(); ++i) {
            if (mSelection[i] == 1) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(_items.get(i).getName());
            }
        }
        simple_adapter.clear();
        simple_adapter.add(sb.toString());
        return sb.toString();
    }


}