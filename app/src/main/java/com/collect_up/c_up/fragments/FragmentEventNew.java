/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Event;
import com.collect_up.c_up.model.Shop;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

@SuppressLint ("ValidFragment")
public class FragmentEventNew extends BaseFragment
  implements Validator.ValidationListener {
  public FragmentEventNew() {
  }

  public FragmentEventNew(Object data) {
    if (data instanceof Shop)
    {
      this.mShop = (Shop) data;

    } else
    {
      this.mComplex = (Complex) data;
    }
  }

  @NotEmpty

  @Bind (R.id.edit_text_message)
  EditText mMessageEditText;
  @NotEmpty
  @Bind (R.id.edit_text_title)
  EditText mTitleEditText;
  @Bind (R.id.text_view_date_start)
  TextView mDateStart;
  @Bind (R.id.text_view_date_end)
  TextView mDateEnd;
  private Shop mShop;
  private Complex mComplex;
  private Validator mValidator;
  private Menu mMenu;
  public static boolean isRunning;
  private View view;

  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
  }

  @Override
  public void onResume() {
    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.new_event);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);

    super.onResume();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_new_event, container, false);
      setHasOptionsMenu(true);
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();

      mDateStart.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          try
          {
            openPickers(v.getId());
          } catch (ParseException e)
          {
          }
        }
      });
      mDateEnd.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          try
          {
            openPickers(v.getId());
          } catch (ParseException e)
          {
          }
        }
      });

      Calendar calendar = Calendar.getInstance();
      Date date = new Date(calendar.getTimeInMillis());
      mDateStart.setText(new SimpleDateFormat(Constants.General.EVENT_PATTERN, Locale.getDefault()).format(date));
      mDateStart.setTag(String.format("%1$d/%2$d/%3$d %4$d:%5$d", calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)));

      mDateEnd.setText(new SimpleDateFormat(Constants.General.EVENT_PATTERN, Locale.getDefault()).format(date));
      mDateEnd.setTag(String.format("%1$d/%2$d/%3$d %4$d:%5$d", calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)));

      mValidator = new Validator(this);
      mValidator.setValidationListener(this);

      return view;
    } else
    {
      return view;
    }
  }


  private void openPickers(@IdRes final int displayInputLayout) throws ParseException {
    final TextView textView = (TextView) view.findViewById(displayInputLayout);
    final Date textViewTagDate = new SimpleDateFormat(Constants.General.EVENT_PATTERN, Locale.getDefault()).parse(textView.getText().toString());
    final Calendar now = Calendar.getInstance();
    // Add 5 years to current year to use with the date picker.
    final Calendar maxDate = Calendar.getInstance();
    maxDate.add(Calendar.YEAR, 5);

    final com.rey.material.app.DatePickerDialog datePicker = new com.rey.material.app.DatePickerDialog(getContext());

    datePicker.dateRange(now.getTimeInMillis(), maxDate.getTimeInMillis());

    datePicker.date(textViewTagDate.getTime());

    datePicker.negativeAction(R.string.cancel);
    datePicker.negativeActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        datePicker.dismiss();
      }
    });

    datePicker.positiveAction(R.string.pick_time);
    datePicker.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        datePicker.dismiss();

        final com.rey.material.app.TimePickerDialog timePicker = new com.rey.material.app.TimePickerDialog(getContext());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(textViewTagDate.getTime());
        timePicker.hour(calendar.get(Calendar.HOUR));
        timePicker.minute(calendar.get(Calendar.MINUTE));
        timePicker.positiveAction(R.string.set);
        timePicker.negativeAction(R.string.cancel);
        timePicker.negativeActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            timePicker.dismiss();
          }
        });
        timePicker.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            timePicker.dismiss();

            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDay(), timePicker.getHour(), timePicker.getMinute());

            TextView textView = (TextView) view.findViewById(displayInputLayout);
            textView.setTag(String.format("%1$d/%2$d/%3$d %4$d:%5$d", datePicker.getMonth() + 1, datePicker.getDay(), datePicker.getYear(), timePicker.getHour(), timePicker.getMinute()));
            textView.setText(new SimpleDateFormat(Constants.General.EVENT_PATTERN, Locale.getDefault()).format(new Date(calendar.getTimeInMillis())));
          }
        });

        timePicker.show();
      }
    });

    datePicker.show();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_done_discard, menu);
    MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
    Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
    if (menuItemLoaderIcon != null)
    {
      try
      {
        menuItemLoaderIcon.mutate();
        menuItemLoaderIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        menuItemLoader.setIcon(menuItemLoaderIcon);
      } catch (IllegalStateException e)
      {
        Log.i("sepehr", String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
      }
      ((Animatable) menuItemLoader.getIcon()).start();
      menuItemLoader.setVisible(false);
    }
    menu.findItem(R.id.action_search).setVisible(false);
    mMenu = menu;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case R.id.action_done:
        mValidator.validate(true);
        break;
      case android.R.id.home:
        Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());
        break;
    }
    return false;
  }

  @Override
  public void onValidationSucceeded() {
    if (mDateStart.getTag() != null && !Utils.isNullOrEmpty((String) mDateStart.getTag()) && mDateEnd.getTag() != null && !Utils.isNullOrEmpty((String) mDateEnd.getTag()))
    {
      Event event = new Event();
      event.setTitle(mTitleEditText.getText().toString());
      event.setMessage(mMessageEditText.getText().toString());
      event.setStartDateTime((String) mDateStart.getTag());
      event.setEndDateTime((String) mDateEnd.getTag());
      event.setShopId(mShop != null ? mShop.getId() : null);
      event.setComplexId(mComplex != null ? mComplex.getId() : null);

      mMenu.findItem(R.id.action_done).setVisible(false);
      mMenu.findItem(R.id.menu_loader).setVisible(true);

      HttpClient.post(getContext(), Constants.Server.Event.POST, new Gson().toJson(event, Event.class), "application/json", new AsyncHttpResponser(getContext()) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
          Utils.hideSoftKeyboard(getContext(), getActivity().getWindow().getDecorView());

          if (mShop != null && mShop.getId() != null)
          {
            FragmentHandler.replaceFragment(getContext(), fragmentType.BUSINESS, mShop);
          } else
          {
            FragmentHandler.replaceFragment(getContext(), fragmentType.COMPLEX, mComplex);
          }

        }

        @Override
        public void onFailure(int statusCode,
                              Header[] headers,
                              byte[] responseBody,
                              Throwable error) {
          super.onFailure(statusCode, headers, responseBody, error);

          Toast.makeText(getContext(), R.string.toast_error_create_new_event, Toast.LENGTH_SHORT).show();
          mMenu.findItem(R.id.action_done).setVisible(true);
          mMenu.findItem(R.id.menu_loader).setVisible(false);


        }
      });
    } else
    {
      Toast.makeText(getContext(), R.string.toast_end_and_start_date_necessary, Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onValidationFailed(List<ValidationError> errors) {
    for (ValidationError error : errors)
    {
      View view = error.getView();
      String message = error.getCollatedErrorMessage(getContext());
      if (view instanceof EditText)
      {
        ((EditText) view).setError(message);
      }
    }
  }

}
