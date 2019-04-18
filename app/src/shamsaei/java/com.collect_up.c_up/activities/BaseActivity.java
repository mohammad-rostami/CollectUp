/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentBusinessNew;
import com.collect_up.c_up.fragments.FragmentComplexNew;
import com.collect_up.c_up.fragments.FragmentEventNew;
import com.collect_up.c_up.fragments.FragmentProductNew;
import com.collect_up.c_up.helpers.AppNotificationUtils;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IBottomMenusListener;
import com.rey.material.app.BottomSheetDialog;

import java.util.Observer;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity implements IBottomMenusListener {

  public BaseActivity() {
  }

   /* @Override
    public final void onMenuNotificationsClick() {
        if (!NotificationsActivity.isRunning) {
            Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("unseen_notifications", AppNotificationUtils.mUnSeenNotifications);
            intent.putExtra("unseen_request_notifications", AppNotificationUtils.mUnSeenRequestNotifications);
            startActivity(intent);
        }
    }

    @Override
    public final void onMenuTimelineClick() {
        if (!TimelineActivity.isRunning) {
            startActivity(new Intent(getApplicationContext(), TimelineActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        }
    }*/

  private void showLimitedMessageDialog(@StringRes int res, boolean isForShop) {
    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(this);
    builder.message(res)
      .messageTextColor(ContextCompat.getColor(this, R.color.primary_text));
    if (isForShop)
    {
      builder.positiveAction(R.string.new_shop).positiveActionTextColor(ContextCompat.getColor(this, R.color.default_white));
      builder.negativeAction(R.string.cancel);
      builder.negativeActionClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          builder.dismiss();

        }
      });
      builder.positiveActionClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (!FragmentBusinessNew.isRunning)
          {
            FragmentHandler.replaceFragment(BaseActivity.this, fragmentType.NEWBUSINESS, null);

            //  startActivity(new Intent(BaseActivity.this, FragmentBusinessNew.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
          }
          builder.dismiss();

        }
      }).positiveActionBackground(R.drawable.corner_radius_button);

    }
    builder.show();
  }

  @Override
  public final void onMenuAddClick() {
    if (Logged.Models.getUserShop() != null)
    {
      final BottomSheetDialog mSheetDialog = new BottomSheetDialog(this);

      mSheetDialog.contentView(R.layout.bottom_sheet_menu_add)
        .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
        .inDuration(300)
        .cancelable(true)
        .show();

      ImageButton newProduct = (ImageButton) mSheetDialog.findViewById(R.id.image_button_new_product);
      ImageButton newShop = (ImageButton) mSheetDialog.findViewById(R.id.image_button_new_shop);
      ImageButton newComplex = (ImageButton) mSheetDialog.findViewById(R.id.image_button_new_complex);
      ImageButton newPost = (ImageButton) mSheetDialog.findViewById(R.id.image_button_new_post);
      ImageButton newEvent = (ImageButton) mSheetDialog.findViewById(R.id.image_button_new_event);

           /* if (Logged.Models.getUserShop() != null)
            {
                newShop.setBackgroundResource(R.drawable.bottom_sheet_menu_add_shop_disabled);
                newShop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSheetDialog.dismiss();
                        showLimitedMessageDialog(R.string.shops_are_limited_for_you, false);
                    }
                });
            } else
            {*/
      newShop.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mSheetDialog.dismiss();
          if (!FragmentBusinessNew.isRunning)
          {
            FragmentHandler.replaceFragment(BaseActivity.this, fragmentType.NEWBUSINESS, null);
            //  startActivity(new Intent(BaseActivity.this, FragmentBusinessNew.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
          }

        }
      });
      // }

            /*if (Logged.Models.getUserComplex() != null)
            {
                newComplex.setBackgroundResource(R.drawable.bottom_sheet_menu_add_complex_disabled);
                newComplex.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSheetDialog.dismiss();
                        showLimitedMessageDialog(R.string.complexes_are_limited_for_you, false);
                    }
                });
            } else
            {*/
      newComplex.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mSheetDialog.dismiss();
          if (!FragmentComplexNew.isRunning)
          {
            FragmentHandler.replaceFragment(BaseActivity.this, fragmentType.NEWCOMPLEX, null);
          }

        }
      });
      // }

      if (Logged.Models.getUserShop() != null)
      {
        newProduct.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mSheetDialog.dismiss();
            if (!FragmentProductNew.isRunning)
            {
              FragmentHandler.replaceFragment(BaseActivity.this, fragmentType.NEWPRODUCT, Logged.Models.getUserShop());

              //startActivity(new Intent(BaseActivity.this, FragmentProductNew.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).putExtra("shop", Logged.Models.getUserShop()));
            }
          }
        });
      } else
      {
        newProduct.setBackgroundResource(R.drawable.bottom_sheet_menu_add_product_disabled);
        newProduct.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mSheetDialog.dismiss();
            showLimitedMessageDialog(R.string.you_dont_have_any_shops, true);
          }
        });
      }

      if (Logged.Models.getUserShop() != null)
      {
        newEvent.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mSheetDialog.dismiss();
            if (!FragmentEventNew.isRunning)
            {
              FragmentHandler.replaceFragment(BaseActivity.this, fragmentType.NEWEVENT, Logged.Models.getUserShop());

              //     startActivity(new Intent(BaseActivity.this, FragmentEventNew.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).putExtra("shop", Logged.Models.getUserShop()));
            }
          }
        });
      } else
      {
        newEvent.setBackgroundResource(R.drawable.bottom_sheet_menu_add_event_disabled);
        newEvent.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mSheetDialog.dismiss();
            showLimitedMessageDialog(R.string.you_dont_have_any_shops, true);
          }
        });
      }

      newPost.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mSheetDialog.dismiss();
          FragmentHandler.replaceFragment(BaseActivity.this, fragmentType.NEWPOST, Logged.Models.getUserProfile());
               /* if (!FragmentPersonPostNew.isRunning)
                    startActivity(new Intent(BaseActivity.this, FragmentPersonPostNew.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).putExtra("profile", Logged.Models.getUserProfile()));
*/
        }
      });
    } else
    {
      FragmentHandler.replaceFragment(BaseActivity.this, fragmentType.NEWPOST, Logged.Models.getUserProfile());
    }
  }
 /* @Override
  public void onMenuShopClick() {
    if (!FragmentSearch.isRunning)
    {
      startActivity(new Intent(getApplicationContext(), FragmentSearch.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }
  }*/

 /* @Override
  public final void onConversationClick() {
    if (!FragmentConversations.isRunning)
    {
      startActivity(new Intent(getApplicationContext(), FragmentConversations.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }
  }*/

  @Override
  public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);
    initButterKnife();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  private void initButterKnife() {
    ButterKnife.bind(this);
  }

  public void setContentViewWithoutInject(int layoutResId) {
    super.setContentView(layoutResId);
  }
}
