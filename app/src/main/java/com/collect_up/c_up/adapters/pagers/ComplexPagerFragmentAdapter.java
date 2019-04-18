/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters.pagers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentComplexInfo;
import com.collect_up.c_up.fragments.pagers.ComplexPagerFragment;
import com.collect_up.c_up.model.Complex;

public class ComplexPagerFragmentAdapter extends FragmentStatePagerAdapter {
  public Context mContext;
  public Complex mComplex;

  public ComplexPagerFragmentAdapter(Context context, FragmentManager fm, Complex complex) {
    super(fm);
    mContext = context;
    mComplex = complex;
  }

  @Override
  public Fragment getItem(int position) {
    Fragment fragment;
    if (position == 3)
    {
      fragment = new FragmentComplexInfo();

    } else
    {
      fragment = new ComplexPagerFragment();

    }
    Bundle args = new Bundle();
    args.putInt(ComplexPagerFragment.ARG_OBJECT, position);
    args.putParcelable("complex", mComplex);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public int getCount() {
    return 4;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    CharSequence pageTitle = null;
    // Get right title for each tab
    switch (position)
    {
      case 0:
        pageTitle = mContext.getString(R.string.tab_timeline);
        break;
      case 1:
        pageTitle = mContext.getString(R.string.tab_shops);
        break;
      case 2:
        pageTitle = mContext.getString(R.string.tab_events);
        break;
      case 3:
        pageTitle = mContext.getString(R.string.tab_info);
        break;
    }

    return pageTitle;
  }

}
