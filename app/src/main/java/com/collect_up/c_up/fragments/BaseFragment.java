package com.collect_up.c_up.fragments;

import android.support.v4.app.Fragment;

import com.collect_up.c_up.activities.ActivityHome;

/**
 * Created by collect-up3 on 9/11/2016.
 */
public class BaseFragment extends Fragment {

  public boolean isVisibleToUser() {
    return false;
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ActivityHome) getActivity()).hideButtonBar(false);
  }
}
