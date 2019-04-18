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
import com.collect_up.c_up.fragments.FragmentBusinessInfo;
import com.collect_up.c_up.fragments.pagers.BusinessPagerFragment;
import com.collect_up.c_up.model.Shop;

public class ShopPagerFragmentAdapter extends FragmentStatePagerAdapter {
  private Context mContext;
  private Shop mShop;

  public ShopPagerFragmentAdapter(Context context, FragmentManager fm, Shop shop) {
    super(fm);
    mContext = context;
    mShop = shop;
  }

  @Override
  public Fragment getItem(int position) {
    Fragment fragment;
    if (position == 3)
    {
      fragment = new FragmentBusinessInfo();

    } else
    {
      fragment = new BusinessPagerFragment();

    }
    Bundle args = new Bundle();
    args.putInt(BusinessPagerFragment.ARG_OBJECT, position);
    args.putParcelable("shop", mShop);
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
        pageTitle = mContext.getString(R.string.tab_products);
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

  @Override
  public int getItemPosition(Object object) {
    return super.getItemPosition(object);
  }

}
