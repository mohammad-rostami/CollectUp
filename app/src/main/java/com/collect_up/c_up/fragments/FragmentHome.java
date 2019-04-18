package com.collect_up.c_up.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.R;
import com.collect_up.c_up.listeners.NavigationButtonListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by collect-up3 on 9/19/2016.
 */
public class FragmentHome extends BaseFragment implements ViewPager.OnPageChangeListener {
  @Bind (R.id.homeViewpager)
  ViewPager viewPager;
  private ViewPagerAdapter adapter;
  private View view;

  @Subscribe (threadMode = ThreadMode.MAIN)
  public void onMessageEvent(NavigationButtonListener event) {
    viewPager.setCurrentItem(event.possition, false);
  }

  @Override
  public void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onStop() {
    EventBus.getDefault().unregister(this);
    super.onStop();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {

      view = inflater.inflate(R.layout.fragment_home, container, false);

      ButterKnife.bind(this, view);
      setupViewPager(viewPager);
      viewPager.addOnPageChangeListener(this);
      return view;

    } else
    {
      return view;
    }
  }

  private void setupViewPager(ViewPager viewPager) {
    adapter = new ViewPagerAdapter(getChildFragmentManager());
    adapter.addFragment(FragmentTimeline.newInstance(0));
    adapter.addFragment(FragmentSearch.newInstance(1));
    adapter.addFragment(FragmentConversations.newInstance(3));
    viewPager.setAdapter(adapter);
    viewPager.setCurrentItem(0);
    viewPager.setOffscreenPageLimit(4);
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

  }

  @Override
  public void onPageSelected(int position) {
    EventBus.getDefault().post(new NavigationButtonListener(position));

  }

  @Override
  public void onPageScrollStateChanged(int state) {

  }

  class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
      super(manager);
    }

    @Override
    public Fragment getItem(int position) {
      return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
      return mFragmentList.size();
    }

    public void addFragment(Fragment fragment) {
      mFragmentList.add(fragment);
    }


  }

  @Override
  public void onResume() {
    super.onResume();
  }
}
