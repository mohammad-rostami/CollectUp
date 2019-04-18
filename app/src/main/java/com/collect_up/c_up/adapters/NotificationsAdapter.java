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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.NonUnderlineClickableSpan;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Notification;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotificationsAdapter extends UltimateViewAdapter<NotificationsAdapter.Holder> {

  private Context mContext;
  private List<Notification> mNotifications = new ArrayList<>();
  private ItemsClickListener mItemsClickListener;
  private int mUnSeenNotification;

  public NotificationsAdapter(Context context, List<Notification> notifications, int unSeenNotification) {
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
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);

    Holder holder = new Holder(view, true);


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
  public void onBindViewHolder(final Holder holder, int position) {
    if (holder.picture == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }

    Notification notification = mNotifications.get(position);
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
      if (notification.getText().equalsIgnoreCase("ComplexEvent"))
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

      } else if (notification.getText().equalsIgnoreCase("ShopEvent"))
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
      } else if (notification.getText().equalsIgnoreCase("ComplexToShopAccept"))
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
    if (Utils.isNullOrEmpty(strings.imageAddress))
    {
      holder.picture.setVisibility(View.VISIBLE);
      holder.picture.setImageResource(R.drawable.ic_description_edit_text);
    } else
    {
      if (strings.imageAddress.equals("*"))
      {
        holder.picture.setVisibility(View.GONE);
      } else
      {
        holder.picture.setVisibility(View.VISIBLE);
        ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
        imageLoader.loadImage(Constants.General.BLOB_PROTOCOL + strings.imageAddress, new SimpleImageLoadingListener() {
          @Override
          public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            holder.picture.setImageBitmap(loadedImage);
          }
        });
      }
    }

    holder.picture.setTag(notification);

    if (position < mUnSeenNotification)
    {
      holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.comment_new_color));
    } else
    {
      holder.itemView.setBackgroundColor(Color.WHITE);
    }
  }

  public void setItemsClickListener(ItemsClickListener itemsClickListener) {
    mItemsClickListener = itemsClickListener;
  }

  private Spannable createSpan(String text, String time, Holder holder, final Notification notification) {
    Spannable textSpannable = new SpannableString(text);

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
      if (notification.getText().equalsIgnoreCase("ComplexEvent"))
      {
        nameStart = text.indexOf(notification.getComplex().getName());
        nameEnd = text.indexOf(notification.getComplex().getName()) + notification.getComplex().getName().length();
      } else if (notification.getText().equalsIgnoreCase("ShopEvent"))
      {
        nameStart = text.indexOf(notification.getShop().getName());
        nameEnd = text.indexOf(notification.getShop().getName()) + notification.getShop().getName().length();
      } else if (notification.getText().equalsIgnoreCase("ShopToComplexAccept"))
      {
        nameStart = text.indexOf(notification.getComplex().getName());
        nameEnd = text.indexOf(notification.getComplex().getName()) + notification.getComplex().getName().length();

      } else if (notification.getText().equalsIgnoreCase("ComplexToShopAccept"))
      {
        nameStart = text.indexOf(notification.getShop().getName());
        nameEnd = text.indexOf(notification.getShop().getName()) + notification.getShop().getName().length();

      } else


      {
        nameStart = text.indexOf(notification.getShop().getName());
        nameEnd = text.indexOf(notification.getShop().getName()) + notification.getShop().getName().length();
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
          if (notification.getText().equalsIgnoreCase("ShopToComplexAccept"))
          {
            FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
            return;

          } else
          {
            FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getShop());
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
  }

  private void produce(final Notification notification, ProduceStrings strings, Holder holder) {

    String timeAgo = TimeHelper.getTimeAgo(mContext, TimeHelper.utcToTimezone(mContext, notification.getInsertTime()));
    holder.profilePicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent;
        if (notification.getText().equalsIgnoreCase("ShopToComplexAccept"))
        {

          FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
          return;

        } else if (notification.getText().equalsIgnoreCase("ComplexToShopAccept"))
        {

          FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, notification.getShop());
          return;
        } else if (notification.getText().equalsIgnoreCase("ComplexEvent"))
        {

          FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
          return;

        } else if (notification.getText().equalsIgnoreCase("ShopEvent"))
        {

          FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, notification.getShop());
          return;
        } else
        {
          FragmentHandler.replaceFragment(mContext, fragmentType.PROFILE, notification.getActor());
          return;

        }


      }
    });
    switch (notification.getText())
    {
      case "Like":
        if (Utils.isNullOrEmpty(notification.getPost().getImageAddress()))
        {
          strings.imageAddress = "";

        } else
        {
          strings.imageAddress = notification.getPost().getThumb();
        }
        strings.text = notification.getActor().getName() + " liked your post. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "ShopLike":
        strings.imageAddress = notification.getPost().getThumb();
        strings.text = notification.getActor().getName() + " liked your business's post. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "ComplexLike":
        strings.imageAddress = notification.getPost().getThumb();
        strings.text = notification.getActor().getName() + " liked your complex's post. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "Comment":
        if (Utils.isNullOrEmpty(notification.getPost().getImageAddress()))
        {
          strings.imageAddress = "";

        } else
        {
          strings.imageAddress = notification.getPost().getThumb();
        }
        strings.text = notification.getActor().getName() + " commented on your post. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "ShopComment":
        strings.imageAddress = notification.getPost().getThumb();
        strings.text = notification.getActor().getName() + " commented on your business's post. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "ComplexComment":
        strings.imageAddress = notification.getPost().getThumb();
        strings.text = notification.getActor().getName() + " commented on your complex's post. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;


      case "Mention":
        strings.imageAddress = notification.getPost().getThumb();
        strings.text = notification.getActor().getName() + " Mentioned you in a Commnet. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "TagedInPost":
      case "TaggedInPost":
        strings.imageAddress = notification.getPost().getThumb();
        strings.text = notification.getActor().getName() + " tagged you in post. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "ShopMention":
        strings.imageAddress = notification.getPost().getThumb();
        strings.text = notification.getActor().getName() + " Mentioned you in a Commnet. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "ComplexMention":
        strings.imageAddress = notification.getPost().getThumb();
        strings.text = notification.getActor().getName() + " Mentioned you in a Commnet. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, notification.getPost());
          }
        });
        break;
      case "Follow":
        strings.imageAddress = "*";
        strings.text = notification.getActor().getName() + " started following you. " + timeAgo;
        break;
      case "ShopFollow":
        strings.imageAddress = notification.getShop().getThumb();
        strings.text = notification.getActor().getName() + " started following your business. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, notification.getShop());
          }
        });
        break;
      case "ComplexFollow":
        strings.imageAddress = notification.getComplex().getThumb();
        strings.text = notification.getActor().getName() + " started following your complex. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
          }
        });
        break;

      case "PersonFollowAccept":
        strings.imageAddress = "*";
        strings.text = notification.getActor().getName() + " accepted your follow request. " + timeAgo;

        break;
      case "ShopToComplexAccept":
        strings.imageAddress = "*";
        strings.text = notification.getComplex().getName() + " accepted your join request. " + timeAgo;

        break;
      case "ComplexToShopAccept":
        strings.imageAddress = "*";
        strings.text = notification.getShop().getName() + " accepted your join request. " + timeAgo;

        break;
      case "RateProduct":
        strings.imageAddress = notification.getProduct().getDefaultImageAddress();
        strings.text = notification.getActor().getName() + " rated to your product. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.PRODUCT, notification.getProduct());
          }
        });
        break;
      case "RateShop":
        strings.imageAddress = notification.getShop().getThumb();
        strings.text = notification.getActor().getName() + " rated to your business. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, notification.getShop());
          }
        });
        break;
      case "RateComplex":
        strings.imageAddress = notification.getComplex().getThumb();
        strings.text = notification.getActor().getName() + " rated to your complex. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
          }
        });
        break;
      case "ShopManager":
        strings.imageAddress = notification.getShop().getThumb();
        strings.text = notification.getActor().getName() + " added you as a business manager. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, notification.getShop());
          }
        });
        break;
      case "ComplexManager":
        strings.imageAddress = notification.getComplex().getThumb();
        strings.text = notification.getActor().getName() + " added you as a complex manager. " + timeAgo;

        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
          }
        });
        break;
      case "Joined":
        strings.imageAddress = "*";
        strings.text = notification.getActor().getName() + " joined " + mContext.getString(R.string.app_name) + ". " + timeAgo;
        break;
      case "ShopEvent":
        strings.imageAddress = "*";
        strings.text = notification.getShop().getName() + " created new event: " + notification.getEvent().getTitle() + ". " + timeAgo;
        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, notification.getShop());
          }
        });
        break;
      case "ComplexEvent":
        strings.imageAddress = "*";
        strings.text = notification.getComplex().getName() + " created new event: " + notification.getEvent().getTitle() + ". " + timeAgo;
        holder.picture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, notification.getComplex());
          }
        });
        break;
    }

    holder.text.setText(createSpan("\u200E" + strings.text, timeAgo, holder, notification));
  }

  public interface ItemsClickListener {
    void onProfilePictureClick(Profile profile);
  }

  static class Holder extends UltimateRecyclerviewViewHolder {

    @Bind (R.id.image_view_profile_picture)
    ComplexAvatarView profilePicture;
    @Bind (R.id.text_view_text)
    TextView text;
    @Bind (R.id.image_view_picture)
    ImageView picture;

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
