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
import com.collect_up.c_up.fragments.pagers.PersonPagerFragment;
import com.collect_up.c_up.model.Profile;

public class PersonPagerFragmentAdapter extends FragmentStatePagerAdapter {
  private final Context mContext;
  private final Profile mProfile;

  public PersonPagerFragmentAdapter(Context context, FragmentManager fm, Profile profile) {
    super(fm);
    mContext = context;
    mProfile = profile;
  }

  @Override
  public int getItemPosition(Object object) {
    return super.getItemPosition(object);
  }

  @Override
  public Fragment getItem(int position) {
    Fragment fragment = new PersonPagerFragment();
    Bundle args = new Bundle();
    args.putInt(PersonPagerFragment.ARG_OBJECT, position);
    args.putParcelable("profile", mProfile);
    fragment.setArguments(args);

    return fragment;
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
        pageTitle = mContext.getString(R.string.tab_info);
        break;
    }

    return pageTitle;
  }
}
