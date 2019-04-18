/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters.providers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityVideoPlayer;
import com.collect_up.c_up.adapters.interfaces.ContactsCallback;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.SortProfiles;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RProfile;
import com.collect_up.c_up.view.CustomMultiPermissionDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.realm.Realm;

public class Contacts implements Comparator<Contacts.UnRegisteredContact> {

  private static List<Profile> registeredContacts;


  public static void setAllContactsToLogged(final Activity context) {
    CustomMultiPermissionDialog dialogPermissionListener =
      CustomMultiPermissionDialog.Builder
        .withContext(context)
        .withTitle(R.string.permission_title)
        .withMessage(R.string.permission_contact)
        .withButtonText(android.R.string.ok)
        .build();
    MultiplePermissionsListener basePermission = new MultiplePermissionsListener() {
      @Override
      public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (report.areAllPermissionsGranted())
        {
          new AsyncTask<Void, Boolean, TreeMap<String, String>>() {
            @Override
            protected void onPreExecute() {
              super.onPreExecute();
            }

            @Override
            protected void onPostExecute(TreeMap<String, String> hashMap) {
              super.onPostExecute(hashMap);
              final TreeMap<String, String> standardizedContacts = new TreeMap<>();

              // Standardize phone numbers
              for (Map.Entry<String, String> entry : hashMap.entrySet())
              {
                standardizedContacts.put(Utils.standardizePhoneNumber(entry.getKey(), Logged.Models.getUserProfile().getCountryCode()), entry.getValue());
              }

              Hawk.put("contacts", standardizedContacts);

              List<String> stringList = new ArrayList<>();

              for (Map.Entry<String, String> entry : standardizedContacts.entrySet())
              {
                stringList.add(entry.getKey());
              }

              Utils.syncContacts(context, TextUtils.join(",", stringList));
            }

            @Override
            protected TreeMap<String, String> doInBackground(Void... params) {


              return Utils.getContactsMobileNumbersAndNames(context);

            }
          }.execute();

          // Standardize phone numbers

        }
      }

      @Override
      public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
        token.continuePermissionRequest();
      }
    };
    CompositeMultiplePermissionsListener compositePermissionListener = new CompositeMultiplePermissionsListener(basePermission, dialogPermissionListener);
    Dexter.withActivity(context).
      withPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION)
      .withListener(compositePermissionListener)
      .check();

  }

  public static int getRegisteredContactCount() {
    return registeredContacts.size();
  }

  public void getAllContacts(final Context context, final ContactsCallback callback, final boolean returnNonProfiles) {
    final Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));

    registeredContacts = new ArrayList<>();
    final List<Contacts.UnRegisteredContact> unregisteredContacts = new ArrayList<>();

    final HashMap<String, String> contactsInHawk = Hawk.get("contacts");

    if (contactsInHawk != null)
    {

      new AsyncTask<Void, Boolean, TreeMap<String, String>>() {
        @Override
        protected TreeMap<String, String> doInBackground(Void... params) {
          for (Map.Entry<String, String> entry : contactsInHawk.entrySet())
          {
            RProfile profile = Realm.getInstance(SepehrUtil.getRealmConfiguration(context)).where(RProfile.class).equalTo("PhoneNumber", entry.getKey()).findFirst();
            if (profile != null)
            {
              if (profile.getId().equals(Logged.Models.getUserProfile().getId()))
              {
                continue;
              }
              registeredContacts.add(RToNonR.rProfileToProfile(profile));
            } else
            {
              if (entry.getKey().equals(Logged.Models.getUserProfile().getPhoneNumber()))
              {
                continue;
              }
              UnRegisteredContact unRegisteredContact = new UnRegisteredContact();
              unRegisteredContact.Name = entry.getValue();
              unRegisteredContact.PhoneNumber = entry.getKey();
              unregisteredContacts.add(unRegisteredContact);
            }
          }
          return null;
        }

        @Override
        protected void onPostExecute(TreeMap<String, String> stringStringTreeMap) {
          super.onPostExecute(stringStringTreeMap);
          Collections.sort(registeredContacts, new SortProfiles());

          if (returnNonProfiles)
          {
            Collections.sort(unregisteredContacts, Contacts.this);
            callback.onContactsReceived(registeredContacts, unregisteredContacts);
          } else
          {
            callback.onContactsReceived(registeredContacts, null);
          }

          realm.close();
        }
      }.execute();


    }


  }

  @Override
  public int compare(Contacts.UnRegisteredContact lhs, Contacts.UnRegisteredContact rhs) {
    return lhs.Name.toLowerCase().compareToIgnoreCase(rhs.Name.toLowerCase());
  }

  public static class UnRegisteredContact {
    String Name;
    String PhoneNumber;
    boolean checked;

    public String getName() {
      return Name;
    }

    public void setName(String name) {
      Name = name;
    }

    public void setChecking(boolean isCheck) {
      checked = isCheck;
    }

    public String getPhoneNumber() {
      return PhoneNumber;
    }

    public boolean getChecked() {
      return checked;
    }

    public void setPhoneNumber(String phoneNumber) {
      PhoneNumber = phoneNumber;
    }
  }
}