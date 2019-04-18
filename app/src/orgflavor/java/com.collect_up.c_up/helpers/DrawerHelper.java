/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentComplexNew;
import com.collect_up.c_up.fragments.FragmentBusinessNew;
import com.collect_up.c_up.fragments.FragmentFollowAndDeny;
import com.collect_up.c_up.fragments.FragmentBusiness;
import com.collect_up.c_up.fragments.FragmentComplex;
import com.collect_up.c_up.fragments.FragmentFindPeople;
import com.collect_up.c_up.fragments.FragmentPerson;
import com.collect_up.c_up.fragments.FragmentPreferences;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Shop;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.List;

public class DrawerHelper {

  public static AccountHeader accountHeader;
  private static DrawerBuilder drawerBuilder;
  private static Drawer drawer;

  public static IProfile createDrawerProfile() {
    IProfile myDrawerProfile = new ProfileDrawerItem().withName("\u200E" + Logged.Models.getUserProfile().getName()).withIdentifier(100).withEmail("+" + Logged.Models.getUserProfile().getPhoneNumber());
    if (Utils.isNullOrEmpty(Logged.Models.getUserProfile().getImageAddress()))
    {
      myDrawerProfile.withIcon(R.drawable.placeholder_profile);
    } else
    {
      myDrawerProfile.withIcon(Constants.General.BLOB_PROTOCOL + Logged.Models.getUserProfile().getImageAddress());
    }
    return myDrawerProfile;
  }

  private static void setImageHeader(Activity activity, String url, ImageView img) {
    if (!Utils.isNullOrEmpty(url))
    {
      ImageHolder holder = new ImageHolder(Constants.General.PROTOCOL + url);
      holder.applyTo(img);
    } else
    {
      img.setImageResource(R.drawable.drawer_header);
    }

  }

  public static View createDrawerHeader(final Activity activity) {
    View view = activity.getLayoutInflater().inflate(R.layout.drawer_header, null, false);
    ImageView imgHeader = (ImageView) view.findViewById(R.id.material_drawer_account_header_background);
    setImageHeader(activity, Logged.Models.getUserProfile().getCoverPhoto(), imgHeader);
    return view;
  }

  public static AccountHeader createAccountHeader(final Activity activity) {

    return new AccountHeaderBuilder()
      .withActivity(activity)
      .withSelectionListEnabledForSingleProfile(false)
      //.withHeaderBackground(createDrawerHeader(activity))
      .withProfileImagesClickable(true)
      .addProfiles(createDrawerProfile())
      .withAccountHeader(createDrawerHeader(activity))

      //  .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
      .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {

        @Override
        public boolean onProfileChanged(View view, IProfile iProfile, boolean b) {
          if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(Logged.Models.getUserProfile().getId())))
          {
            FragmentHandler.replaceFragment(activity, fragmentType.PROFILE, Logged.Models.getUserProfile());
         /* if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(Logged.Models.getUserProfile().getId()))) {
            Intent intent = new Intent(activity, FragmentPerson.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("profile", Logged.Models.getUserProfile());
            activity.startActivity(intent);*/
          }
          return false;
        }
      })
      .withOnAccountHeaderSelectionViewClickListener(new AccountHeader.OnAccountHeaderSelectionViewClickListener() {
        @Override
        public boolean onClick(View view, IProfile iProfile) {
          if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(Logged.Models.getUserProfile().getId())))
          {
            //    FragmentHandler.replaceFragment(activity, fragmentType.PROFILE, Logged.Models.getUserProfile());
                           /* Intent intent = new Intent(activity, FragmentPerson.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra("profile", Logged.Models.getUserProfile());
                            activity.startActivity(intent);*/

          }
          return false;
        }
      })
      .build();
  }

  public static IDrawerItem createShopItem(final Activity activity) {
    IDrawerItem shopPrimaryDrawerItem = null;
    final Shop myShop = Logged.Models.getUserShop();
    final List<Shop> shopList = Logged.Models.getUserShopList();

    if (myShop == null && (shopList == null || shopList.size() == 0))
    {
      shopPrimaryDrawerItem = new PrimaryDrawerItem()
        .withIdentifier(2)
        .withName(R.string.new_shop)
        .withIcon(CommunityMaterial.Icon.cmd_shopping)
        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
            if (!FragmentBusinessNew.isRunning)
            {
              FragmentHandler.replaceFragment(activity, fragmentType.NEWBUSINESS, null);
            }

            // activity.startActivity(new Intent(activity, FragmentBusinessNew.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            return false;
          }
        });

    } else if (myShop == null && (shopList != null && shopList.size() > 0))
    {
      shopPrimaryDrawerItem = new ExpandableDrawerItem()
        .withName(R.string.business)
        .withIcon(CommunityMaterial.Icon.cmd_shopping)
        .withSelectable(false)
        .withSubItems(new SecondaryDrawerItem()
          .withName(R.string.new_shop)
          .withIcon(CommunityMaterial.Icon.cmd_plus)
          .withLevel(2)
          .withIdentifier(2000)
          .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
              if (!FragmentBusinessNew.isRunning)
              {
                FragmentHandler.replaceFragment(activity, fragmentType.NEWBUSINESS, null);
              }

              //  activity.startActivity(new Intent(activity, FragmentBusinessNew.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

              return false;
            }
          }));
      for (int i = 0; i < shopList.size(); i++)
      {

        final Shop shop = shopList.get(i);
        SecondaryDrawerItem childItems = (SecondaryDrawerItem) new SecondaryDrawerItem()
          .withName("\u200E\u200E\u200E\u200E\u200E\u200E" + shopList.get(i).getName())
          .withLevel(2)
          .withIcon(CommunityMaterial.Icon.cmd_shopping)
          .withIdentifier(2001 + i)
          .withIsExpanded(true).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
              if (!FragmentBusiness.isRunning || (FragmentBusiness.isRunning && !FragmentBusiness.shopId.equals(shop.getId())))
              {
                FragmentHandler.replaceFragment(activity, fragmentType.BUSINESS, shop);

              }
              return false;
          /*      //            if (FragmentBusiness.isStop) {
                if (FragmentBusiness.getInstance() != null)
                  FragmentBusiness.getInstance().finish();
                //          }
                Intent intent = new Intent(activity, FragmentBusiness.class);
                intent.putExtra("shop", myShop);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);

              }
              return false;*/
            }
          });
        ((ExpandableDrawerItem) shopPrimaryDrawerItem).withSubItems(childItems);
      }
    } else if (myShop != null && (shopList == null || shopList.size() == 0))
    {
      shopPrimaryDrawerItem = new PrimaryDrawerItem()
        .withIdentifier(2)
        .withName("\u200E\u200E\u200E\u200E\u200E\u200E" + activity.getResources().getString(R.string.business) + " (" + myShop.getName() + ")")
        .withIcon(CommunityMaterial.Icon.cmd_shopping)
        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            if (!FragmentBusiness.isRunning || (FragmentBusiness.isRunning && !FragmentBusiness.shopId.equals(myShop.getId())))
            {
              FragmentHandler.replaceFragment(activity, fragmentType.BUSINESS, myShop);

            }
            return false;

             /* //  if (FragmentBusiness.isStop) {
              if (FragmentBusiness.getInstance() != null)
                FragmentBusiness.getInstance().finish();
              //  }
              Intent intent = new Intent(activity, FragmentBusiness.class);
              intent.putExtra("shop", myShop);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              activity.startActivity(intent);


            }
            return false;*/
          }
        });
    } else if (myShop != null && (shopList != null && shopList.size() > 0))
    {

      shopPrimaryDrawerItem = new ExpandableDrawerItem()
        .withName(R.string.business)
        .withIcon(CommunityMaterial.Icon.cmd_shopping)
        .withSelectable(false)
        .withSubItems(new SecondaryDrawerItem()
          .withName("\u200E\u200E\u200E\u200E\u200E\u200E" + myShop.getName())
          .withIcon(CommunityMaterial.Icon.cmd_shopping)
          .withIconColor(ContextCompat.getColor(activity, R.color.colorAccent))
          .withTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
          .withLevel(2)
          .withIdentifier(2000)
          .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
              // if (!FragmentBusiness.isRunning || (FragmentBusiness.isRunning && !FragmentBusiness.shopId.equals(myShop.getId()))) {
              if (!FragmentBusiness.isRunning || (FragmentBusiness.isRunning && !FragmentBusiness.shopId.equals(myShop.getId())))
              {
                FragmentHandler.replaceFragment(activity, fragmentType.BUSINESS, myShop);

              }
              return false;
                /*//    if (FragmentBusiness.isStop) {
                if (FragmentBusiness.getInstance() != null)
                  FragmentBusiness.getInstance().finish();
                //   }
                Intent intent = new Intent(activity, FragmentBusiness.class);
                intent.putExtra("shop", myShop);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);


              }
              return false;*/
            }
          }));
      for (int i = 0; i < shopList.size(); i++)
      {

        final Shop shop = shopList.get(i);
        SecondaryDrawerItem childItems = (SecondaryDrawerItem) new SecondaryDrawerItem()
          .withName("\u200E\u200E\u200E\u200E\u200E\u200E" + shopList.get(i).getName())
          .withLevel(2)
          .withIcon(CommunityMaterial.Icon.cmd_shopping)
          .withIdentifier(2001 + i)
          .withIsExpanded(true).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
              //  if (!FragmentBusiness.isRunning || (FragmentBusiness.isRunning && !FragmentBusiness.shopId.equals(shop.getId()))) {
              if (!FragmentBusiness.isRunning || (FragmentBusiness.isRunning && !FragmentBusiness.shopId.equals(myShop.getId())))
              {
                FragmentHandler.replaceFragment(activity, fragmentType.BUSINESS, shop);

              }
              return false;
/*
                //  if (FragmentBusiness.isStop) {
                if (FragmentBusiness.getInstance() != null)

                  FragmentBusiness.getInstance().finish();
                //  }
                Intent intent = new Intent(activity, FragmentBusiness.class);
                intent.putExtra("shop", shop);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);


              }
              return false;*/
            }
          });
        ((ExpandableDrawerItem) shopPrimaryDrawerItem).withSubItems(childItems);
      }
    }
    return shopPrimaryDrawerItem;
  }

  public static void update(Drawer drawer, Activity activity) {
    accountHeader.updateProfile(createDrawerProfile());
    accountHeader.getHeaderBackgroundView().setImageURI(null);
    setImageHeader(activity,
      Logged.Models.getUserProfile().getCoverPhoto(),
      accountHeader.getHeaderBackgroundView());
    drawer.updateItem(createShopItem(activity));
    drawer.updateItem(createComplexItem(activity));
    drawer.updateItem(createProfileItem(activity));
  }

  public static IDrawerItem createComplexItem(final Activity activity) {


    IDrawerItem complexPrimaryDrawerItem = null;
    final Complex myComplex = Logged.Models.getUserComplex();
    final List<Complex> complexList = Logged.Models.getUserComplexList();

    if (myComplex == null && (complexList == null || complexList.size() == 0))
    {
      complexPrimaryDrawerItem = new PrimaryDrawerItem()
        .withIdentifier(3)
        .withName(R.string.new_complex)
        .withIcon(CommunityMaterial.Icon.cmd_city)
        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
            if (!FragmentComplexNew.isRunning)
            {
              FragmentHandler.replaceFragment(activity, fragmentType.NEWCOMPLEX, null);

              // activity.startActivity(new Intent(activity, FragmentComplexNew.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }

            return false;
          }
        });

    } else if (myComplex == null && (complexList != null && complexList.size() > 0))
    {
      complexPrimaryDrawerItem = new ExpandableDrawerItem()
        .withName(R.string.complex)
        .withIcon(CommunityMaterial.Icon.cmd_city)
        .withSelectable(false)
        .withSubItems(new SecondaryDrawerItem()
          .withName(R.string.new_complex)
          .withIcon(CommunityMaterial.Icon.cmd_plus)
          .withLevel(3)
          .withIdentifier(2000)
          .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
              if (!FragmentComplexNew.isRunning)
              {
                FragmentHandler.replaceFragment(activity, fragmentType.NEWCOMPLEX, null);

                //activity.startActivity(new Intent(activity, FragmentComplexNew.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
              }

              return false;
            }
          }));
      for (int i = 0; i < complexList.size(); i++)
      {

        final Complex complex = complexList.get(i);
        SecondaryDrawerItem childItems = (SecondaryDrawerItem) new SecondaryDrawerItem()
          .withName("\u200E\u200E\u200E\u200E\u200E\u200E" + complexList.get(i).getName())
          .withLevel(3)
          .withIcon(CommunityMaterial.Icon.cmd_city)
          .withIdentifier(2001 + i)
          .withIsExpanded(true).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
              if (!FragmentComplex.isRunning || (FragmentComplex.isRunning && !FragmentComplex.complexId.equals(complex.getId())))
              {
                FragmentHandler.replaceFragment(activity, fragmentType.COMPLEX, complex);

              }
              return false;
                /*if (FragmentComplex.getInstance() != null)
                  FragmentComplex.getInstance().finish();
                Intent intent = new Intent(activity, FragmentComplex.class);
                intent.putExtra("complex", complex);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);

              }
              return false;*/
            }
          });
        ((ExpandableDrawerItem) complexPrimaryDrawerItem).withSubItems(childItems);
      }
    } else if (myComplex != null && (complexList == null || complexList.size() == 0))
    {
      complexPrimaryDrawerItem = new PrimaryDrawerItem()
        .withIdentifier(3)
        .withName("\u200E\u200E\u200E\u200E\u200E\u200E" + activity.getResources().getString(R.string.complex) + " (" + myComplex.getName() + ")")
        .withIcon(CommunityMaterial.Icon.cmd_city)
        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            if (!FragmentComplex.isRunning || (FragmentComplex.isRunning && !FragmentComplex.complexId.equals(myComplex.getId())))
            {
              FragmentHandler.replaceFragment(activity, fragmentType.COMPLEX, myComplex);

            }
            return false;

              /*if (FragmentComplex.getInstance() != null)
                FragmentComplex.getInstance().finish();
              Intent intent = new Intent(activity, FragmentComplex.class);
              intent.putExtra("complex", myComplex);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              activity.startActivity(intent);

            }
            return false;*/
          }
        });
    } else if (myComplex != null && (complexList != null && complexList.size() > 0))
    {

      complexPrimaryDrawerItem = new ExpandableDrawerItem()
        .withName(R.string.complex)
        .withIcon(CommunityMaterial.Icon.cmd_city)
        .withSelectable(false)
        .withSubItems(new SecondaryDrawerItem()
          .withName("\u200E\u200E\u200E\u200E\u200E\u200E" + myComplex.getName())
          .withIcon(CommunityMaterial.Icon.cmd_city)
          .withIconColor(ContextCompat.getColor(activity, R.color.colorAccent))
          .withTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
          .withLevel(3)
          .withIdentifier(2000)
          .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
              if (!FragmentComplex.isRunning || (FragmentComplex.isRunning && !FragmentComplex.complexId.equals(myComplex.getId())))
              {
                FragmentHandler.replaceFragment(activity, fragmentType.COMPLEX, myComplex);

              }
              return false;
                /*if (FragmentComplex.getInstance() != null)
                  FragmentComplex.getInstance().finish();
                Intent intent = new Intent(activity, FragmentComplex.class);
                intent.putExtra("complex", myComplex);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);

              }
              return false;*/
            }
          }));
      for (int i = 0; i < complexList.size(); i++)
      {

        final Complex complex = complexList.get(i);
        SecondaryDrawerItem childItems = (SecondaryDrawerItem) new SecondaryDrawerItem()
          .withName("\u200E\u200E\u200E\u200E\u200E\u200E" + complexList.get(i).getName())
          .withLevel(3)
          .withIcon(CommunityMaterial.Icon.cmd_city)
          .withIdentifier(2001 + i)
          .withIsExpanded(true).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
              if (!FragmentComplex.isRunning || (FragmentComplex.isRunning && !FragmentComplex.complexId.equals(myComplex.getId())))
              {
                FragmentHandler.replaceFragment(activity, fragmentType.COMPLEX, myComplex);

              }
              return false;
                /*if (FragmentComplex.getInstance() != null)
                  FragmentComplex.getInstance().finish();

                Intent intent = new Intent(activity, FragmentComplex.class);
                intent.putExtra("complex", complex);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);

              }
              return false;*/
            }
          });
        ((ExpandableDrawerItem) complexPrimaryDrawerItem).withSubItems(childItems);
      }
    }
    return complexPrimaryDrawerItem;

  }

  public static PrimaryDrawerItem createProfileItem(final Activity activity) {
    return new PrimaryDrawerItem().withIdentifier(1).withName(R.string.profile).withIcon(CommunityMaterial.Icon.cmd_account).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
      @Override
      public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
        if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(Logged.Models.getUserProfile().getId())))
        {
          FragmentHandler.replaceFragment(activity, fragmentType.PROFILE, Logged.Models.getUserProfile());
       /* if (!FragmentPerson.isRunning || (FragmentPerson.isRunning && !FragmentPerson.profileId.equals(Logged.Models.getUserProfile().getId()))) {
          Intent intent = new Intent(activity, FragmentPerson.class);
          intent.putExtra("profile", Logged.Models.getUserProfile());
          intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
          activity.startActivity(intent);*/
        }
        return false;
      }
    });
  }

  public static Drawer forAllActivities(final Activity activity, Toolbar toolbar, int selectedItem) {

    accountHeader = createAccountHeader(activity);

    IDrawerItem shopPrimaryDrawerItem = createShopItem(activity);

    IDrawerItem complexPrimaryDrawerItem = createComplexItem(activity);

    PrimaryDrawerItem profileItem = createProfileItem(activity);
    //Create the drawer
    drawerBuilder = new DrawerBuilder()
      .withActivity(activity)
      .withSelectedItem(selectedItem);

    if (toolbar != null)
    {
      drawerBuilder.withToolbar(toolbar)
        .withActionBarDrawerToggle(true)
        .withActionBarDrawerToggleAnimated(false);
    }

    drawerBuilder.withAccountHeader(accountHeader) //set the AccountHeader we created earlier for the header
      .addDrawerItems(
        profileItem,
        shopPrimaryDrawerItem,
        complexPrimaryDrawerItem,
        new PrimaryDrawerItem().withIdentifier(4).withName(R.string.contacts).withIcon(CommunityMaterial.Icon.cmd_account_box).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {

            if (!FragmentFollowAndDeny.isRunning)
            {
            /*  Intent intent = new Intent(activity, FragmentFollowAndDeny.class);
              intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
              activity.startActivity(intent);*/
              FragmentHandler.replaceFragment(activity, fragmentType.CONTACTS, null);
            }

            return false;
          }
        }),
        new PrimaryDrawerItem().withIdentifier(6).withName(R.string.find_people).withIcon(CommunityMaterial.Icon.cmd_account_search).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
            if (!FragmentFindPeople.isRunning)
            {
            /*  Intent intent = new Intent(activity, FragmentFindPeople.class);
              intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
              activity.startActivity(intent);*/
              FragmentHandler.replaceFragment(activity, fragmentType.FINDPEOPLE, null);

            }
            return false;
          }
        }),
        new SectionDrawerItem().withName(R.string.others),
        new SecondaryDrawerItem().withIdentifier(5).withName(R.string.settings).withIdentifier(5).withIcon(CommunityMaterial.Icon.cmd_settings).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
            if (!FragmentPreferences.isRunning)
            {
            /*  Intent intent = new Intent(activity, FragmentPreferences.class);
              activity.startActivity(intent);
*/
              FragmentHandler.replaceFragment(activity, fragmentType.PREFERENCE, null);

            }
            return false;
          }
        }),
        new SecondaryDrawerItem().withName(R.string.about).withIcon(CommunityMaterial.Icon.cmd_information).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(Constants.General.BLOB_PROTOCOL + Constants.General.WEBSITE_URL));
            activity.startActivity(intent);

            return false;
          }
        })
      );
    drawer = drawerBuilder.build();
    return drawer;
  }

  public static Drawer getDrawer() {
    return drawer;
  }
}
