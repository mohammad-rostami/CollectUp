package com.collect_up.c_up.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.providers.Contacts;
import com.collect_up.c_up.fragments.BaseFragment;
import com.collect_up.c_up.fragments.FragmentConversations;
import com.collect_up.c_up.fragments.FragmentNotifications;
import com.collect_up.c_up.fragments.FragmentSearch;
import com.collect_up.c_up.fragments.FragmentTimeline;
import com.collect_up.c_up.fragments.pagers.TimelinePagerFragment;
import com.collect_up.c_up.helpers.AppNotificationUtils;
import com.collect_up.c_up.helpers.DrawerHelper;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.OnBackPressed;
import com.collect_up.c_up.helpers.OnNotifArrived;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.NavigationButtonListener;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Initial;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.services.CheckInternetConnectivity;
import com.collect_up.c_up.services.RealtimeService;
import com.collect_up.c_up.view.CustomNotidicationCounter;
import com.mikepenz.materialdrawer.Drawer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by collect-up3 on 9/11/2016.
 */


public class ActivityHome extends BaseActivity implements
  View.OnClickListener {
  @Bind (R.id.toolbar)
  Toolbar toolbar;
  @Bind (R.id.frame_layout_notifications)
  FrameLayout menuNotification;
  @Bind (R.id.image_button_timeline)
  ImageButton menuTimeline;
  @Bind (R.id.image_button_add)
  ImageButton menuAdd;
  @Bind (R.id.image_button_shop)
  ImageButton menuShop;
  @Bind (R.id.image_button_chat)
  ImageButton menuChat;
  @Bind (R.id.layout_bottom_menu)
  LinearLayout buttonBar;
  @Bind (R.id.text_view_unseen_notifications)
  CustomNotidicationCounter unSeenNotifications;

  Class fragmentClass;
  private Drawer mDrawer;
  private ViewPagerAdapter adapter;
  public static ActivityHome instance = null;

  @Subscribe (threadMode = ThreadMode.MAIN)
  public void onMessageEvent(NavigationButtonListener event) {
    changeButtonBackgroud(event.possition);
  }

  public void hideButtonBar(boolean hide) {
    if (hide)
    {
      buttonBar.setVisibility(View.GONE);
    } else
    {
      buttonBar.setVisibility(View.VISIBLE);
    }
  }

  @Subscribe (threadMode = ThreadMode.MAIN)
  public void onNotifArrived(OnNotifArrived event) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!FragmentNotifications.isRunning)
        {
          if (AppNotificationUtils.mUnSeenNotifications <= 0)
          {
            unSeenNotifications.setVisibility(View.GONE);
          } else
          {
            unSeenNotifications.setVisibility(View.VISIBLE);
            unSeenNotifications.setText(Integer.toString(AppNotificationUtils.mUnSeenNotifications));
          }
        }
      }
    });
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

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.image_button_timeline:
        onMenuTimelineClick();

        break;
      case R.id.image_button_shop:
        onMenuShopClick();
        break;
      case R.id.frame_layout_notifications:
        onMenuNotificationsClick();
        break;
      case R.id.image_button_chat:
        onConversationClick();
        break;
      case R.id.image_button_add:
        onMenuAddClick();
        break;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mDrawer != null)
    {
      DrawerHelper.update(mDrawer, this);
      mDrawer.setSelection(-1);
    }
  }

  @Override
  public void onBackPressed() {
    if (DrawerHelper.getDrawer().isDrawerOpen())
    {
      DrawerHelper.getDrawer().closeDrawer();
      return;
    }
    int count = getSupportFragmentManager().getBackStackEntryCount();

    if (count <= 1 && !TimelinePagerFragment.isViewWithCatalog)
    {
      finish();      //additional code
    } else
    {
      if (TimelinePagerFragment.isViewWithCatalog)
      {
        EventBus.getDefault().post(new OnBackPressed(true));
      } else
      {
        super.onBackPressed();
      }
    }

  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    instance = this;
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);

    getWindow().getDecorView().setBackgroundColor(getResources().getColor(android.R.color.white));

    if (Logged.Models.getUserProfile() == null
      || Logged.Models.getUserProfile().getUsername() == null
      || Logged.Models.getUserProfile().getName() == null)
    {
      Intent intent = new Intent(this, GetStartActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
      finish();
    } else
    {
      Contacts.setAllContactsToLogged(this);
      startService(new Intent(this, RealtimeService.class));
      startService(new Intent(this, CheckInternetConnectivity.class));
      Initial initialModel = Initial.getInstance();
      initialModel.initialize(this);
      FragmentHandler.replaceFragment(this, fragmentType.TIMELINE, null);
      //   setupViewPager(pager);
      toolbar.setVisibility(View.GONE);
      setSupportActionBar(toolbar);


      menuChat.setOnClickListener(this);
      menuAdd.setOnClickListener(this);
      menuShop.setOnClickListener(this);
      menuNotification.setOnClickListener(this);
      menuTimeline.setOnClickListener(this);
      mDrawer = DrawerHelper.forAllActivities(this, null, -1);
      if (AppNotificationUtils.mUnSeenNotifications <= 0)
      {
        unSeenNotifications.setVisibility(View.GONE);
      } else
      {
        if (Integer.parseInt(AppNotificationUtils.mUnSeenNotifications + "") > 99)
        {
          unSeenNotifications.setText("+99");
        } else
        {
          unSeenNotifications.setText(AppNotificationUtils.mUnSeenNotifications + "");
        }
      }
      changeButtonBackgroud(0);

      mDrawer = DrawerHelper.forAllActivities(this, toolbar, -1);
    }
    if (getIntent().getClipData() != null)
    {
      onNewIntent(getIntent());
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    String type = intent.getType();
    if (type != null && (type.startsWith("image/") || type.startsWith("video/") || type.startsWith("text/")))
    {
      intent.putExtra("postType", Logged.Models.getUserProfile());
      FragmentHandler.replaceFragment(this, fragmentType.NEWPOST, intent);
      return;
    }
    Object data = intent.getParcelableExtra("data");
    if (data == null && intent.getIntExtra("unseen_notifications", -1) != -1)
    {
      FragmentHandler.replaceFragment(this, fragmentType.NOTIFICATIONS, intent);

    }
    if (data instanceof Post)
    {
      FragmentHandler.replaceFragment(this, fragmentType.DISPLAYPOST, data);
    } else if (data instanceof Profile)
    {
      FragmentHandler.replaceFragment(this, fragmentType.PROFILE, data);

    } else if (data instanceof Shop)
    {
      FragmentHandler.replaceFragment(this, fragmentType.BUSINESS, data);

    } else if (data instanceof Complex)
    {
      FragmentHandler.replaceFragment(this, fragmentType.COMPLEX, data);

    } else if (data instanceof Product)
    {
      FragmentHandler.replaceFragment(this, fragmentType.PRODUCT, data);

    } else if (data instanceof CompactChat)
    {
      if (intent.getBooleanExtra("ischat", true))
      {
        FragmentHandler.replaceFragment(this, fragmentType.CHAT, data);
      } else
      {
        FragmentHandler.replaceFragment(this, fragmentType.GROUPCHAT, data);
      }

    }

  }

  private void addOrReplace(Class fragClass, String tag) {
    BaseFragment fragment = null;
    try
    {
      fragment = (BaseFragment) fragClass.newInstance();
    } catch (InstantiationException e)
    {
      e.printStackTrace();
    } catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();

    if (fragmentManager.findFragmentByTag(tag) != null && fragment.isVisibleToUser())
    {
    } else
    {


      transaction
        .replace(R.id.rootLayout, fragment, tag)
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        .commit();
    }


  }


  @Override
  public void onMenuNotificationsClick() {
    if (!FragmentNotifications.isRunning)
    {
      changeButtonBackgroud(2);
      unSeenNotifications.setVisibility(View.GONE);
      FragmentHandler.replaceFragment(this, fragmentType.NOTIFICATIONS, null);
    }
  }

  @Override
  public void onMenuTimelineClick() {
    if (!FragmentTimeline.isRunning)
    {
      FragmentHandler.replaceFragment(this, fragmentType.TIMELINE, null);
      changeButtonBackgroud(0);
    }
  }

  @Override
  public void onMenuShopClick() {
    if (!FragmentSearch.isRunning)
    {
      FragmentHandler.replaceFragment(this, fragmentType.SEARCH, null);

      changeButtonBackgroud(1);
    }
  }

  @Override
  public void onConversationClick() {
    if (!FragmentConversations.isRunning)
    {
      changeButtonBackgroud(3);
      //  EventBus.getDefault().post(new NavigationButtonListener(3));
      FragmentHandler.replaceFragment(this, fragmentType.CONVERSATIONS, null);
    }
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

  public void changeButtonBackgroud(int tabPos) {
    switch (tabPos)
    {
      case -1:
        menuTimeline.setBackground(null);
        menuTimeline.setImageResource(R.drawable.ic_home_white_line);

        menuShop.setBackgroundResource(R.drawable.bottom_menu);
        menuShop.setImageResource(R.drawable.ic_shop_white_line);
        menuNotification.setBackground(null);
        ((ImageButton) menuNotification.getChildAt(0)).setImageResource(R.drawable.ic_earth_line);
        menuChat.setBackgroundResource(R.drawable.bottom_menu);
        menuChat.setImageResource(R.drawable.ic_chat_white_line);
        break;
      case 0:
        menuTimeline.setBackgroundResource(R.drawable.bottom_menu_selected);
        menuTimeline.setImageResource(R.drawable.ic_home_white);

        menuShop.setBackgroundResource(R.drawable.bottom_menu);
        menuShop.setImageResource(R.drawable.ic_shop_white_line);
        menuNotification.setBackground(null);
        ((ImageButton) menuNotification.getChildAt(0)).setImageResource(R.drawable.ic_earth_line);
        menuChat.setBackgroundResource(R.drawable.bottom_menu);
        menuChat.setImageResource(R.drawable.ic_chat_white_line);
        break;
      case 1:
        menuShop.setBackgroundResource(R.drawable.bottom_menu_selected);
        menuShop.setImageResource(R.drawable.ic_shop_white);

        menuTimeline.setBackgroundResource(R.drawable.bottom_menu);
        menuTimeline.setImageResource(R.drawable.ic_home_white_line);
        menuNotification.setBackgroundResource(R.drawable.bottom_menu);
        ((ImageButton) menuNotification.getChildAt(0)).setImageResource(R.drawable.ic_earth_line);
        menuChat.setBackgroundResource(R.drawable.bottom_menu);
        menuChat.setImageResource(R.drawable.ic_chat_white_line);
        break;
      case 2:
        menuNotification.setBackgroundResource(R.drawable.bottom_menu_selected_frame_layout);
        ((ImageButton) menuNotification.getChildAt(0)).setImageResource(R.drawable.ic_earth);

        menuTimeline.setBackgroundResource(R.drawable.bottom_menu);
        menuTimeline.setImageResource(R.drawable.ic_home_white_line);
        menuShop.setBackgroundResource(R.drawable.bottom_menu);
        menuShop.setImageResource(R.drawable.ic_shop_white_line);
        menuChat.setBackgroundResource(R.drawable.bottom_menu);
        menuChat.setImageResource(R.drawable.ic_chat_white_line);
        break;
      case 3:
        menuChat.setBackgroundResource(R.drawable.bottom_menu_selected);
        menuChat.setImageResource(R.drawable.ic_chat_white);

        menuTimeline.setBackgroundResource(R.drawable.bottom_menu);
        menuTimeline.setImageResource(R.drawable.ic_home_white_line);
        menuShop.setBackgroundResource(R.drawable.bottom_menu);
        menuShop.setImageResource(R.drawable.ic_shop_white_line);
        menuNotification.setBackgroundResource(R.drawable.bottom_menu);
        ((ImageButton) menuNotification.getChildAt(0)).setImageResource(R.drawable.ic_earth_line);
        break;


    }
  }
}