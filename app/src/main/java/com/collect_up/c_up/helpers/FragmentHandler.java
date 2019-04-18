package com.collect_up.c_up.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.fragments.FragmentAllStickers;
import com.collect_up.c_up.fragments.FragmentBlockedList;
import com.collect_up.c_up.fragments.FragmentBusiness;
import com.collect_up.c_up.fragments.FragmentBusinessEdit;
import com.collect_up.c_up.fragments.FragmentBusinessNew;
import com.collect_up.c_up.fragments.FragmentChat;
import com.collect_up.c_up.fragments.FragmentChatContacts;
import com.collect_up.c_up.fragments.FragmentComplex;
import com.collect_up.c_up.fragments.FragmentComplexBusinessAdd;
import com.collect_up.c_up.fragments.FragmentComplexEdit;
import com.collect_up.c_up.fragments.FragmentComplexNew;
import com.collect_up.c_up.fragments.FragmentConversations;
import com.collect_up.c_up.fragments.FragmentDeniedList;
import com.collect_up.c_up.fragments.FragmentDisplayFollowersNFollowing;
import com.collect_up.c_up.fragments.FragmentEventNew;
import com.collect_up.c_up.fragments.FragmentFindPeople;
import com.collect_up.c_up.fragments.FragmentFollowAndDeny;
import com.collect_up.c_up.fragments.FragmentGroupChat;
import com.collect_up.c_up.fragments.FragmentGroupChatInfo;
import com.collect_up.c_up.fragments.FragmentGroupChatInit;
import com.collect_up.c_up.fragments.FragmentHashTags;
import com.collect_up.c_up.fragments.FragmentManageStickers;
import com.collect_up.c_up.fragments.FragmentManagerAdd;
import com.collect_up.c_up.fragments.FragmentManagers;
import com.collect_up.c_up.fragments.FragmentNotifications;
import com.collect_up.c_up.fragments.FragmentPerson;
import com.collect_up.c_up.fragments.FragmentPersonEdit;
import com.collect_up.c_up.fragments.FragmentPostDisplay;
import com.collect_up.c_up.fragments.FragmentPostEdit;
import com.collect_up.c_up.fragments.FragmentPostLikes;
import com.collect_up.c_up.fragments.FragmentPostNew;
import com.collect_up.c_up.fragments.FragmentPreferences;
import com.collect_up.c_up.fragments.FragmentProductEdit;
import com.collect_up.c_up.fragments.FragmentProductNew;
import com.collect_up.c_up.fragments.FragmentProductProfile;
import com.collect_up.c_up.fragments.FragmentRequests;
import com.collect_up.c_up.fragments.FragmentSearch;
import com.collect_up.c_up.fragments.FragmentShare;
import com.collect_up.c_up.fragments.FragmentTagPeople;
import com.collect_up.c_up.fragments.FragmentTimeline;
import com.collect_up.c_up.fragments.FragmentTwoStepEnable;
import com.collect_up.c_up.fragments.FragmetBusinessComplexAdd;
import com.collect_up.c_up.fragments.FragmnetTwoStepEnter;
import com.collect_up.c_up.fragments.PrefFragmentAccountPrivacy;
import com.collect_up.c_up.fragments.PrefFragmentManageNotifications;
import com.collect_up.c_up.fragments.PrefFragmentSessionManager;
import com.collect_up.c_up.fragments.PrefFragmentTwoStep;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;


public class FragmentHandler {
    static Fragment fragment = null;

    public static void replaceFragment(Context context, fragmentType type, Object data) {
        try {
            replaceFragment(context, type, data, true);
        } catch (Exception e) {
        }
    }

    public static void replaceFragment(Context context, fragmentType type, Object data, boolean addStack) {
        switch (type) {
            case TIMELINE:
                fragment = FragmentTimeline.newInstance(0);
                break;
            case SEARCH:
                fragment = FragmentSearch.newInstance(1);
                break;
            case NOTIFICATIONS:
                fragment = new FragmentNotifications((Intent) data);
                break;
            case CONVERSATIONS:
                fragment = FragmentConversations.newInstance(3);
                break;

            case PROFILE:
                fragment = new FragmentPerson((Profile) data);

                break;
            case BUSINESS:
                fragment = new FragmentBusiness((Shop) data);
                break;
            case COMPLEX:
                fragment = new FragmentComplex((Complex) data);
                break;
            case PRODUCT:
                fragment = new FragmentProductProfile((Product) data);
                break;
            case CONTACTS:
                fragment = new FragmentFollowAndDeny();
                break;
            case FINDPEOPLE:
                fragment = new FragmentFindPeople();
                break;
            case PREFERENCE:
                fragment = new FragmentPreferences();
                break;
            case DISPLAYPOST:
                fragment = new FragmentPostDisplay((Post) data);
                break;
            case NEWPOST:
                fragment = new FragmentPostNew(data);
                break;
            case EDITPOST:
                fragment = new FragmentPostEdit((Post) data);
                break;
            case ADD_BUSINESS_COMPLEX:
                fragment = new FragmetBusinessComplexAdd((Shop) data);
                break;
            case ADD_COMPLEX_BUSINESS:
                fragment = new FragmentComplexBusinessAdd((Complex) data);
                break;
            case NEWBUSINESS:
                fragment = new FragmentBusinessNew();
                break;
            case NEWCOMPLEX:
                fragment = new FragmentComplexNew();
                break;
            case EDITBUSINESS:
                fragment = new FragmentBusinessEdit((Shop) data);
                break;
            case EDITCOMPLEX:
                fragment = new FragmentComplexEdit((Complex) data);
                break;
            case EDITPERSON:
                fragment = new FragmentPersonEdit();
                break;
            case NEWEVENT:
                fragment = new FragmentEventNew(data);
                break;
            case ADD_MANAGER:
                fragment = new FragmentManagerAdd(data);
                break;
            case SET_ADMIN:
                fragment = new FragmentManagers(data);
                break;
            case REQUESTS:
                fragment = new FragmentRequests();
                break;
            case CHATCONTACTS:
                fragment = new FragmentChatContacts((Intent) data);
                break;
            case FOLLOWNFOLLOWING:
                fragment = new FragmentDisplayFollowersNFollowing((Bundle) data);
                break;
            case LIKES:
                fragment = new FragmentPostLikes((String) data);
                break;
            case CHAT:
                fragment = new FragmentChat((CompactChat) data);
                break;
            case GROUPCHAT:
                fragment = new FragmentGroupChat((CompactChat) data);
                break;
            case GROUPCHAT_INIT:
                fragment = new FragmentGroupChatInit((Intent) data);
                break;
            case GROUPCHAT_INFO:
                fragment = new FragmentGroupChatInfo((CompactChat) data);
                break;
            case EDITPRODUCT:
                fragment = new FragmentProductEdit((Product) data);
                break;
            case NEWPRODUCT:
                fragment = new FragmentProductNew((Shop) data);
                break;
            case HASHTAG:
                fragment = new FragmentHashTags((String) data);
                break;
            case PREF_NOTIFS:
                fragment = new PrefFragmentManageNotifications();
                break;
            case PREF_BLOCKLIST:
                fragment = new FragmentBlockedList();
                break;
            case PREF_DENIEDLIST:
                fragment = new FragmentDeniedList();
                break;
            case PREF_MANAGE_STICKER:
                fragment = new FragmentManageStickers();
                break;
            case PREF_ALLSTICKERS:
                fragment = new FragmentAllStickers();
                break;
            case PREF_TWOSTEP_ENTER:
                fragment = new FragmnetTwoStepEnter();
                break;
            case PREF_TWOSTEP:
                fragment = new PrefFragmentTwoStep((boolean) data);
                break;
            case PREF_SESSIONMANAGER:
                fragment = new PrefFragmentSessionManager();
                break;
            case PREF_PRIVACY:
                fragment = new PrefFragmentAccountPrivacy();
                break;
            case TWOSTEP_ENABLE:
                fragment = new FragmentTwoStepEnable((int) data);
                break;
            case SHARE:
                fragment = new FragmentShare((Intent) data);
                break;
            case TAG_PEOPLE:
                fragment = new FragmentTagPeople((Intent) data);
                break;

        }
        try {
  /*    FragmentManager fm = getActivity().getSupportFragmentManager();
      for (int i = 0; i < fm.getBackStackEntryCount(); ++i)
      {
        fm.popBackStack();
      }*/

            final FragmentManager fragmentManager = (ActivityHome.instance).getSupportFragmentManager();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (!addStack) { //for (int i = 0; i < fragmentManager.getBackStackEntryCount()-1; ++i)
    /*  {
        fragmentManager.popBackStack();
      }*/
                ((AppCompatActivity) context).onBackPressed();//F  onBackPressed(context);
                //fragmentManager.popBackStack(FragmentHome.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            fragmentTransaction.addToBackStack(fragment.getClass().getName());

            //.setCustomAnimations(R.anim.s_up, R.anim.s_down, R.anim.s_up, R.anim.s_down)
            fragmentTransaction.replace(R.id.fragmentContainer, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_NONE);


            fragmentTransaction.commit();

        } catch (Exception ex) {
            android.util.Log.i("sepehr", "replaceFragment: " + ex.getMessage());
        }

    }

    public static Fragment getFragment() {
        return fragment;
    }

    public static void onBackPressed(final Context context) {

        //  getCurrentFragment(context);
        //((AppCompatActivity) context).onBackPressed();
        try {
            ((AppCompatActivity) context).getSupportFragmentManager().popBackStack();
        } catch (IllegalStateException ignored) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }

        //  ((AppCompatActivity) context).getSupportFragmentManager().getFragments()
    }

    private static Fragment getCurrentFragment(final Context context) {
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
        Fragment currentFragment = fragmentManager.findFragmentByTag(fragmentTag);
        return currentFragment;
    }

}
