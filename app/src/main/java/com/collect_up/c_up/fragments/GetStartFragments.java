/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.GetStartActivity;

/**
 * This fragment contains all fragments related to GetStartActivity
 */
public class GetStartFragments {

  public static String phoneNumber;
  public static String areaCode;
  public static boolean tryAgainPressed = false;
  public static boolean isVerified;
  public static boolean hasTwoStep;
  public static boolean isEmailVerified;

  public static class WelcomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);

      View inflate = inflater.inflate(R.layout.fragment_get_start, container, false);

      inflate.findViewById(R.id.button_get_start)
        .setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            ((GetStartActivity) getActivity()).replaceFragment(new FragmentGetPhoneNumber());
          }
        });

      return inflate;
    }
  }


}
