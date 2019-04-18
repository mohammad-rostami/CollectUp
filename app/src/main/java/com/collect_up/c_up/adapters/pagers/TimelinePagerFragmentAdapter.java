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
import com.collect_up.c_up.fragments.pagers.TimelinePagerFragment;

public class TimelinePagerFragmentAdapter extends FragmentStatePagerAdapter {
  private final Context mContext;

  public TimelinePagerFragmentAdapter(Context context, FragmentManager fm) {
    super(fm);
    mContext = context;
  }

  @Override
  public Fragment getItem(int position) {
    Fragment fragment = new TimelinePagerFragment();
    Bundle args = new Bundle();
    args.putInt(TimelinePagerFragment.ARG_OBJECT, position);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public int getItemPosition(Object object) {
    return POSITION_NONE;
  }

  @Override
  public int getCount() {
    return 2;
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
        pageTitle = mContext.getString(R.string.tab_explore);
        break;
    }

    return pageTitle;
  }
}
