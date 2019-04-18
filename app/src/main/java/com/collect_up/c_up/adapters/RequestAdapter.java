/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.NonUnderlineClickableSpan;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Notification;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.rey.material.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class RequestAdapter extends UltimateViewAdapter<RequestAdapter.Holder> implements View.OnClickListener {

  private Context mContext;
  private List<Notification> mNotifications = new ArrayList<>();
  private ItemsClickListener mItemsClickListener;
  private int mUnSeenNotification;

  public RequestAdapter(Context context, List<Notification> notifications, int unSeenNotification) {
    mContext = context;
    mNotifications = notifications;
    mUnSeenNotification = unSeenNotification;
  }

  @Override
  public Holder getViewHolder(View view) {
    return new Holder(view, false);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inf_request, viewGroup, false);

    Holder holder = new Holder(view, true);

    holder.profilePicture.setOnClickListener(this);

    return holder;
  }

  @Override
  public int getAdapterItemCount() {
    return mNotifications.size();
  }

  @Override
  public long generateHeaderId(int i) {
    return 0;
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
    return null;
  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    if (holder.profilePicture == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }
    /*holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.layout_reject_swipe);
    holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, R.id.layout_accept_swipe);
    holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
    holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
      @Override
      public void onStartOpen(SwipeLayout layout) {

      }

      @Override
      public void onOpen(SwipeLayout layout) {
        YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.reject));
        YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.accept));

      }

      @Override
      public void onStartClose(SwipeLayout layout) {

      }

      @Override
      public void onClose(SwipeLayout layout) {

      }

      @Override
      public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

      }

      @Override
      public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

      }
    });*/
    final Notification notification = mNotifications.get(position);
    ProduceStrings strings = new ProduceStrings();
    produce(notification, strings, holder);

    if (notification.getActor() != null)
    {
      if (Utils.isNullOrEmpty(notification.getActor().getImageAddress()))
      {
        holder.profilePicture.makeAllDefaults();
        holder.profilePicture.setText(notification.getActor().getName());
      } else
      {
        holder.profilePicture.makeAllDefaults();
        holder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(notification.getActor().getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + notification.getActor().getThumb());
      }
      holder.profilePicture.setTag(notification.getActor());

    } else
    {
      if (notification.getText().equalsIgnoreCase("RequestShopToComplex"))
      {
        if (Utils.isNullOrEmpty(notification.getShop().getImageAddress()))
        {
          holder.profilePicture.makeAllDefaults();
          holder.profilePicture.setText(notification.getShop().getName());
        } else
        {
          holder.profilePicture.makeAllDefaults();
          holder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(notification.getShop().getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + notification.getShop().getThumb());
        }
        holder.profilePicture.setTag(notification.getShop());

      } else
      {
        if (Utils.isNullOrEmpty(notification.getComplex().getImageAddress()))
        {
          holder.profilePicture.makeAllDefaults();
          holder.profilePicture.setText(notification.getComplex().getName());
        } else
        {
          holder.profilePicture.makeAllDefaults();
          holder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(notification.getComplex().getName()).setImageUrl(Constants.General.BLOB_PROTOCOL + notification.getComplex().getThumb());
        }
        holder.profilePicture.setTag(notification.getComplex());

      }

    }

    holder.profilePicture.setTag(notification);

    if (position < mUnSeenNotification)
    {
      holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.comment_new_color));
    } else
    {
      holder.itemView.setBackgroundColor(Color.WHITE);
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.image_view_profile_picture:
        if (mItemsClickListener != null)
        {
          Profile profile = (Profile) v.getTag();
          mItemsClickListener.onProfilePictureClick(profile);
        }
        break;
    }
  }

  public void setItemsClickListener(ItemsClickListener itemsClickListener) {
    mItemsClickListener = itemsClickListener;
  }

  private Spannable createSpan(String text, String time, Holder holder, final Notification notification) {

    Spannable textSpannable;

    if (!text.equalsIgnoreCase("null"))
    {
      textSpannable = new SpannableString(text);
      float timeAgoTextSize = 0.8f;
      float nameTextSize = 1.1f;
      int nameEnd;
      int nameStart;
      int timeAgoStart = text.indexOf(time);
      int timeAgoEnd = text.indexOf(time) + time.length();
      if (notification.getActor() != null)
      {
        nameStart = text.indexOf(notification.getActor().getName());
        nameEnd = text.indexOf(notification.getActor().getName()) + notification.getActor().getName().length();
      } else
      {
        if (notification.getText().equalsIgnoreCase("RequestShopToComplex"))
        {

          nameStart = text.indexOf(notification.getShop().getName());
          nameEnd = text.indexOf(notification.getShop().getName()) + notification.getShop().getName().length();
        } else
        {
          nameStart = text.indexOf(notification.getComplex().getName());
          nameEnd = text.indexOf(notification.getComplex().getName()) + notification.getComplex().getName().length();
        }
      }
      // Styling the time in the text
      textSpannable.setSpan(new ForegroundColorSpan(Color.GRAY), timeAgoStart, timeAgoEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      textSpannable.setSpan(new RelativeSizeSpan(timeAgoTextSize), timeAgoStart, timeAgoEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

      holder.text.setMovementMethod(LinkMovementMethod.getInstance());
      textSpannable.setSpan(new NonUnderlineClickableSpan() {
        @Override
        public void onClick(View widget) {
          Intent intent;
          if (notification.getActor() != null)
          {
            FragmentHandler.replaceFragment(mContext, fragmentType.PROFILE, notification.getActor());
            return;


          } else
          {
            if (notification.getText().equalsIgnoreCase("RequestShopToComplex"))
            {

              FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, notification.getShop());
              return;

            } else
            {
              FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
              return;

            }
          }

        }
      }, nameStart, nameEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

      // Styling the name in the text
      textSpannable.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorAccent)), nameStart, nameEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      textSpannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), nameStart, nameEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      textSpannable.setSpan(new RelativeSizeSpan(nameTextSize), nameStart, nameEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      return textSpannable;

    } else
    {
      return new SpannableString("");
    }

  }

  private void produce(final Notification notification, ProduceStrings strings, final Holder holder) {
    final int pos = mNotifications.indexOf(notification);
    String timeAgo = TimeHelper.getTimeAgo(mContext, TimeHelper.utcToTimezone(mContext, notification.getInsertTime()));


    switch (notification.getText())
    {

      case "RequestPerson":

        strings.imageAddress = notification.getActor().getThumb();
        strings.text = notification.getActor().getName() + " has requested to follow you. " + timeAgo;
        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.PROFILE, notification.getActor());
            return;

          }
        });
        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            HttpClient.get(String.format(Constants.Server.Request.FOLLOW_RESULT, notification.getId(), true), new AsyncHttpResponser(mContext) {

              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                Log.i("sepehr", "onFailure: ");

              }
            });
            // holder.swipeLayout.open(true, SwipeLayout.DragEdge.Left);
          }
        });
        holder.btnReject.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            HttpClient.get(String.format(Constants.Server.Request.FOLLOW_RESULT, notification.getId(), false), new AsyncHttpResponser(mContext) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                Log.i("sepehr", "onFailure: ");
              }
            });
            //holder.swipeLayout.open(true, SwipeLayout.DragEdge.Right);
          }
        });
        /*holder.btnAccept.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            HttpClient.get(String.format(Constants.Server.Request.FOLLOW_RESULT, notification.getId(), true), new AsyncHttpResponser(mContext) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

              }
            });
          }
        });

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            HttpClient.get(String.format(Constants.Server.Request.FOLLOW_RESULT, notification.getId(), false), new AsyncHttpResponser(mContext) {

              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

              }
            });

          }
        });*/
        break;
      case "RequestShopToComplex":
        strings.imageAddress = notification.getShop().getThumb();
        strings.text = notification.getShop().getName() + " has requested to join your complex. " + timeAgo;
        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, notification.getShop());
          }
        });
        holder.btnReject.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            HttpClient.get(String.format(Constants.Server.Request.SHOP_TO_COMPLEX, notification.getId(), false), new AsyncHttpResponser(mContext) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

              }
            });
          }
        });

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            HttpClient.get(String.format(Constants.Server.Request.SHOP_TO_COMPLEX, notification.getId(), true), new AsyncHttpResponser(mContext) {

              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

              }
            });

          }
        });


        /*holder.btnaccept.setonclicklistener(new view.onclicklistener() {
          @override
          public void onclick(view v) {
            httpclient.get(string.format(constants.server.request.shop_to_complex, notification.getid(), true), new asynchttpresponser(mcontext) {
              @override
              public void onsuccess(int statuscode, header[] headers, byte[] responsebody) {
                mnotifications.remove(notification);
                notifyitemremoved(pos);
                myapplication.getinstance().getobserver().seenrequestnotification(mcontext);

              }


              @override
              public void onfailure(int statuscode,
                                    header[] headers,
                                    byte[] responsebody,
                                    throwable error) {
                super.onfailure(statuscode, headers, responsebody, error);

              }
            });
          }
        });

        holder.btncancel.setonclicklistener(new view.onclicklistener() {
          @override
          public void onclick(view v) {

            httpclient.get(string.format(constants.server.request.shop_to_complex, notification.getid(), false), new asynchttpresponser(mcontext) {

              @override
              public void onsuccess(int statuscode, header[] headers, byte[] responsebody) {
                mnotifications.remove(notification);
                notifyitemremoved(pos);
                myapplication.getinstance().getobserver().seenrequestnotification(mcontext);

              }


              @override
              public void onfailure(int statuscode,
                                    header[] headers,
                                    byte[] responsebody,
                                    throwable error) {
                super.onfailure(statuscode, headers, responsebody, error);

              }
            });

          }
        });*/
        break;
      case "RequestComplexToShop":
        strings.imageAddress = notification.getComplex().getThumb();
        strings.text = notification.getComplex().getName() + " has requested to join your business. " + timeAgo;
        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
          }
        });
        holder.btnReject.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            HttpClient.get(String.format(Constants.Server.Request.COMPLEX_TO_SHOP, notification.getId(), false), new AsyncHttpResponser(mContext) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);
                Shop shop = Logged.Models.getUserShop();
                shop.setComplex(notification.getComplex());
                Logged.Models.setUserShop(shop);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

              }
            });
          }
        });

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            HttpClient.get(String.format(Constants.Server.Request.COMPLEX_TO_SHOP, notification.getId(), true), new AsyncHttpResponser(mContext) {

              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

              }
            });

          }
        });

       /* holder.btnAccept.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            HttpClient.get(String.format(Constants.Server.Request.COMPLEX_TO_SHOP, notification.getId(), true), new AsyncHttpResponser(mContext) {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);
                Shop shop = Logged.Models.getUserShop();
                shop.setComplex(notification.getComplex());
                Logged.Models.setUserShop(shop);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

              }
            });
          }
        });

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            HttpClient.get(String.format(Constants.Server.Request.COMPLEX_TO_SHOP, notification.getId(), false), new AsyncHttpResponser(mContext) {

              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mNotifications.remove(notification);
                notifyItemRemoved(pos);
                MyApplication.getInstance().getObserver().seenRequestNotification(mContext);

              }


              @Override
              public void onFailure(int statusCode,
                                    Header[] headers,
                                    byte[] responseBody,
                                    Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

              }
            });

          }
        });*/
        break;
    }
    if (strings.text != null)
    {
      holder.text.setText(createSpan("\u200E" + strings.text, timeAgo, holder, notification));
    }
  }

  public interface ItemsClickListener {
    void onProfilePictureClick(Profile profile);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {

    @Bind (R.id.text_view_name)
    TextView text;
    @Bind (R.id.btn_accept)
    ImageButton btnAccept;
    @Bind (R.id.btn_cancel)
    ImageButton btnReject;
    @Bind (R.id.image_view_picture)
    ComplexAvatarView profilePicture;

    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }

  public static class ProduceStrings {
    private String imageAddress;
    private String text;
  }
}
