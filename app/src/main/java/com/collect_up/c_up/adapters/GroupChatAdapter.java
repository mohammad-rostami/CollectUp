/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityPickLocation;
import com.collect_up.c_up.chat.AndroidUtilities;
import com.collect_up.c_up.chat.widgets.Emoji;
import com.collect_up.c_up.fragments.FragmentBusiness;
import com.collect_up.c_up.fragments.FragmentComplex;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.TimeHelper;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IloadMoreClickListenr;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.EnumMessageStatus;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.util.ComplexAvatarView;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

public class GroupChatAdapter extends BaseAdapter {

  private final int mImageWidth;
  private final ListView mListView;
  private Activity mContext;
  private List<RCompactMessage> mMessages = new ArrayList<>();
  private Realm mRealm;
  private OnMessageClick mOnMessageClickListener;
  private OnProfileClick mOnProfileClick;
  private List<RCompactMessage> queueMessages = new ArrayList<>();
  private RCompactMessage mFirstMessage;
  private String mPickedLocation;
  private IloadMoreClickListenr mOnLoadMoreClickListener;

  public GroupChatAdapter(Activity context, List<RCompactMessage> messages, ListView listView) {
    mContext = context;
    mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(MyApplication.context));
    mMessages = messages;
    mListView = listView;

    mImageWidth = (mContext.getResources().getDisplayMetrics().widthPixels / 4) * 3;
  }

  public void completelyRemoveItem(final RCompactMessage message) {
    for (RCompactMessage rMessage : mMessages)
    {
      String id = rMessage.getMessageId();
      if (!Utils.isNullOrEmpty(message.getMessageId()) && message.getMessageId().equals(id))
      {
        mMessages.remove(message);
        break;
      } else if (message.getId().equals(rMessage.getId()))
      {
        mMessages.remove(message);
        break;
      }
    }

    Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(mContext));
    realm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        RCompactMessage rCompactMessage = realm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst();
        if (rCompactMessage != null)
        {
          rCompactMessage.removeFromRealm();
        }
      }
    });
    realm.close();

    MyApplication.mUploadingFiles.remove(message.getMessageId());
    MyApplication.getInstance().cancelUploadHandler(message.getMessageId());

    notifyDataSetChanged();
  }

  public void updateStatus(final RCompactMessage messsage) {
    for (final RCompactMessage rCompactMessage : mMessages)
    {
      if (rCompactMessage != null && rCompactMessage.getMessageId().equals(messsage.getMessageId()))
      {
        mRealm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            rCompactMessage.setMessageStatus(messsage.getMessageStatus());
            realm.copyToRealmOrUpdate(rCompactMessage);
          }
        });

        notifyDataSetChanged();
        break;
      }
    }
  }

  public void insertItemsAtTop(List<RCompactMessage> messages) {
    if (messages.size() > 0)
    {
      int index = mListView.getLastVisiblePosition();
      View v = mListView.getChildAt(0);
      int top = (v == null) ? 0 : v.getTop();


      mMessages.addAll(0, messages);
      notifyDataSetChanged();

      mListView.setSelectionFromTop(index, top);

    }

  }

  public void insertLoadMoreButtonAtTop() {
    if (mMessages.get(0) != null)
    {
      mMessages.add(0, null);
      notifyDataSetChanged();
    }
  }

  public void insertItem(RCompactMessage message, boolean notify) {
    if (message.getMessageStatus() == 0)
    {
      MyApplication.mUploadingFiles.put(message.getMessageId(), 0.0F);
    }
    mMessages.add(message);
    if (notify)
    {
      notifyDataSetChanged();
    }
  }

  public void insertMessages(List<RCompactMessage> messages) {
    if (messages.size() > 0)
    {
      mMessages.addAll(messages);
      notifyDataSetChanged();
    }

  }

  public List<RCompactMessage> getMessages() {
    return mMessages;
  }

  public void updateMessageStatus(int pos, RCompactMessage message) {
    mMessages.set(pos, message);
  }

  public void clearItems() {
    mMessages.clear();
    notifyDataSetChanged();
  }

  public void setListener(OnMessageClick listener) {
    if (listener != null)
    {
      mOnMessageClickListener = listener;
    }
  }

  public void setLoadMoreListener(IloadMoreClickListenr listener) {
    if (listener != null)
    {
      mOnLoadMoreClickListener = listener;
    }
  }

  public void setOnProfileClickListener(OnProfileClick listener) {
    if (listener != null)
    {
      mOnProfileClick = listener;
    }
  }

  private void setLongClick(RCompactMessage message, int position) {
    if (mOnMessageClickListener != null)
    {
      mOnMessageClickListener.onMessageLongClick(message, position);
    }
  }

  private void setClickProfile(String profileId, String profileName, String profileImage) {
    if (mOnProfileClick != null)
    {
      Profile profile = new Profile();
      profile.setId(profileId);
      profile.setName(profileName);
      profile.setImageAddress(profileImage);
      mOnProfileClick.onProfileClick(profile);
    }
  }

  private void setClickCancel(RCompactMessage message, int position) {
    if (mOnMessageClickListener != null)
    {
      mOnMessageClickListener.onCancelClick(message, position);
    }
  }

  @Override
  public int getCount() {
    return mMessages.size();
  }

  @Override
  public Object getItem(int position) {
    return mMessages.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public static boolean isSticker(RCompactMessage message) {
    return message.getContentType() == EnumMessageContentType.Sticker ? true : false;

  }

  public static boolean isImage(RCompactMessage message) {
    return message.getContentType() == EnumMessageContentType.Image ? true : false;

  }

  public static boolean isVideo(RCompactMessage message) {
    return message.getContentType() == EnumMessageContentType.Video ? true : false;

  }

  public static boolean isText(RCompactMessage message) {
    return message.getContentType() == EnumMessageContentType.Text ? true : false;

  }

  public static boolean isSharedItem(RCompactMessage message) {
    if (message.getContentType() == EnumMessageContentType.SharedBusiness
      || message.getContentType() == EnumMessageContentType.SharedComplex
      || message.getContentType() == EnumMessageContentType.SharedEvent
      || message.getContentType() == EnumMessageContentType.SharedPost
      || message.getContentType() == EnumMessageContentType.SharedProduct
      || message.getContentType() == EnumMessageContentType.SharedProfile)
    {
      return true;
    } else
    {
      return false;
    }
  }

  public static boolean isFile(RCompactMessage message) {
    // getSize() be onvane file name estefade mishavad.
    return message.getContentType() == EnumMessageContentType.File ? true : false;

  }

  public static boolean isLocation(RCompactMessage message) {
    return message.getContentType() == EnumMessageContentType.Location ? true : false;

  }


  private void setSharedProduct(SelfSharedViewHolder holder, RCompactMessage message, final Product product) {
    holder.sharedTitle.setText(message.getText());
    holder.sharedDesc.setText(product.getDescription());


    holder.sharedTitle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.PRODUCT, product);
      }
    });
    holder.sharedPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.PRODUCT, product);
      }
    });

    Picasso.with(mContext).load(Constants.General.BLOB_PROTOCOL + product.getDefaultImageAddress()).noPlaceholder().noFade().into(holder.sharedPicture);


    if (message.getMessageStatus() == EnumMessageStatus.Sent)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_single_tick));
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_double_tick));
      // Color gray
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_double_tick));
      // Color accent
      holder.status.setColorFilter(Color.argb(255, 3, 169, 244));
    } else
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_clock_white));
      holder.status.setColorFilter(Color.argb(255, 3, 169, 244));
    }

    Date date = new Date();
    if (message.getSendDateTime().contains("T"))
    {
      date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
    } else
    {
      date.setTime(Long.valueOf(message.getSendDateTime()));
    }

    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

    holder.time.setText(time);
  }

  private void setSharedProduct(OthersSharedViewHolder holder, RCompactMessage message, final Product product) {
    holder.sharedTitle.setText(message.getText());
    holder.sharedDesc.setText(product.getDescription());


    holder.sharedTitle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.PRODUCT, product);
      }
    });
    holder.sharedPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.PRODUCT, product);
      }
    });

    Picasso.with(mContext).load(Constants.General.BLOB_PROTOCOL + product.getDefaultImageAddress()).noPlaceholder().noFade().into(holder.sharedPicture);

    Date date = new Date();
    if (message.getSendDateTime().contains("T"))
    {
      date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
    } else
    {
      date.setTime(Long.valueOf(message.getSendDateTime()));
    }

    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

    holder.time.setText(time);
  }

  private void setSharedShop(SelfSharedViewHolder holder, RCompactMessage message, final Shop shop) {
    holder.sharedTitle.setText(message.getText());
    holder.sharedDesc.setText(shop.getDescription());

    final Intent intent = new Intent(mContext, FragmentBusiness.class);
    intent.putExtra("shop", shop);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    holder.sharedTitle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, shop);

      }
    });
    holder.sharedPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, shop);
      }
    });

    if (!Utils.isNullOrEmpty(shop.getImageAddress()))
    {
      Picasso.with(mContext).load(Constants.General.BLOB_PROTOCOL + shop.getImageAddress()).noPlaceholder().noFade().into(holder.sharedPicture);
    } else
    {
      holder.sharedPicture.setImageResource(R.drawable.placeholder);
    }


    if (message.getMessageStatus() == EnumMessageStatus.Sent)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_single_tick));
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_double_tick));
      // Color gray
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_double_tick));
      // Color accent
      holder.status.setColorFilter(Color.argb(255, 3, 169, 244));
    } else
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_clock_white));
      holder.status.setColorFilter(Color.argb(255, 3, 169, 244));
    }

    Date date = new Date();
    if (message.getSendDateTime().contains("T"))
    {
      date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
    } else
    {
      date.setTime(Long.valueOf(message.getSendDateTime()));
    }

    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

    holder.time.setText(time);
  }

  private void setSharedShop(OthersSharedViewHolder holder, RCompactMessage message, final Shop shop) {
    holder.sharedTitle.setText(message.getText());
    holder.sharedDesc.setText(shop.getDescription());

    final Intent intent = new Intent(mContext, FragmentBusiness.class);
    intent.putExtra("shop", shop);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    holder.sharedTitle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, shop);
      }
    });
    holder.sharedPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.BUSINESS, shop);
      }
    });

    if (!Utils.isNullOrEmpty(shop.getImageAddress()))
    {
      Picasso.with(mContext).load(Constants.General.BLOB_PROTOCOL + shop.getImageAddress()).noPlaceholder().noFade().into(holder.sharedPicture);
    } else
    {
      holder.sharedPicture.setImageResource(R.drawable.placeholder);
    }

    Date date = new Date();
    if (message.getSendDateTime().contains("T"))
    {
      date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
    } else
    {
      date.setTime(Long.valueOf(message.getSendDateTime()));
    }

    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

    holder.time.setText(time);
  }

  private void setSharedComplex(SelfSharedViewHolder holder, RCompactMessage message, final Complex complex) {
    holder.sharedTitle.setText(message.getText());
    holder.sharedDesc.setText(complex.getDescription());

    final Intent intent = new Intent(mContext, FragmentComplex.class);
    intent.putExtra("complex", complex);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    holder.sharedTitle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, complex);
      }
    });
    holder.sharedPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, complex);
      }
    });

    if (!Utils.isNullOrEmpty(complex.getImageAddress()))
    {
      Picasso.with(mContext).load(Constants.General.BLOB_PROTOCOL + complex.getImageAddress()).noPlaceholder().noFade().into(holder.sharedPicture);
    } else
    {
      holder.sharedPicture.setImageResource(R.drawable.placeholder);
    }


    if (message.getMessageStatus() == EnumMessageStatus.Sent)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_single_tick));
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_double_tick));
      // Color gray
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_double_tick));
      // Color accent
      holder.status.setColorFilter(Color.argb(255, 3, 169, 244));
    } else
    {
      holder.status.setImageDrawable(mContext.getResources()
        .getDrawable(R.drawable.ic_clock_white));
      holder.status.setColorFilter(Color.argb(255, 3, 169, 244));
    }

    Date date = new Date();
    if (message.getSendDateTime().contains("T"))
    {
      date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
    } else
    {
      date.setTime(Long.valueOf(message.getSendDateTime()));
    }

    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

    holder.time.setText(time);
  }

  private void setSharedComplex(OthersSharedViewHolder holder, RCompactMessage message, final Complex complex) {
    holder.sharedTitle.setText(message.getText());
    holder.sharedDesc.setText(complex.getDescription());

    final Intent intent = new Intent(mContext, FragmentComplex.class);
    intent.putExtra("complex", complex);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    holder.sharedTitle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, complex);
      }
    });
    holder.sharedPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.COMPLEX, complex);
      }
    });

    if (!Utils.isNullOrEmpty(complex.getImageAddress()))
    {
      Picasso.with(mContext).load(Constants.General.BLOB_PROTOCOL + complex.getImageAddress()).noPlaceholder().noFade().into(holder.sharedPicture);
    } else
    {
      holder.sharedPicture.setImageResource(R.drawable.placeholder);
    }

    Date date = new Date();
    if (message.getSendDateTime().contains("T"))
    {
      date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
    } else
    {
      date.setTime(Long.valueOf(message.getSendDateTime()));
    }

    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

    holder.time.setText(time);
  }

  private void setSharedPostTimeline(SelfSharedViewHolder holder, RCompactMessage message, final Post post) {
    holder.sharedTitle.setText(post.getSender().getName());
    if (!Utils.isNullOrEmpty(post.getText()))
    {
      holder.sharedDesc.setVisibility(View.VISIBLE);
      holder.sharedDesc.setText(post.getText());
    } else
    {
      holder.sharedDesc.setVisibility(View.GONE);
    }

    holder.sharedTitle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, post);

      }
    });
    holder.sharedPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, post);

      }
    });

    if (!Utils.isNullOrEmpty(post.getImageAddress()))
    {
      Picasso.with(mContext).load(Constants.General.BLOB_PROTOCOL + post.getImageAddress()).noPlaceholder().noFade().into(holder.sharedPicture);
    } else
    {
      holder.sharedPicture.setImageResource(R.drawable.placeholder);
    }

    if (message.getMessageStatus() == EnumMessageStatus.Sent)
    {
      holder.status.setImageResource(R.drawable.ic_single_tick);
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
    {
      holder.status.setImageResource(R.drawable.ic_double_tick);
      // Color gray
      holder.status.setColorFilter(Color.argb(127, 0, 0, 0));
    } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
    {
      holder.status.setImageResource(R.drawable.ic_double_tick);
      // Color accent
      holder.status.setColorFilter(Color.argb(255, 3, 169, 244));
    } else
    {
      holder.status.setImageResource(R.drawable.ic_clock_white);
      holder.status.setColorFilter(Color.argb(255, 3, 169, 244));
    }

    Date date = new Date();
    if (message.getSendDateTime().contains("T"))
    {
      date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
    } else
    {
      date.setTime(Long.valueOf(message.getSendDateTime()));
    }

    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

    holder.time.setText(time);
  }

  private void setSharedPostTimeline(OthersSharedViewHolder holder, RCompactMessage message, final Post post) {
    holder.sharedTitle.setText(post.getSender().getName());
    if (!Utils.isNullOrEmpty(post.getText()))
    {
      holder.sharedDesc.setVisibility(View.VISIBLE);
      holder.sharedDesc.setText(post.getText());
    } else
    {
      holder.sharedDesc.setVisibility(View.GONE);
    }


    holder.sharedTitle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, post);
      }
    });
    holder.sharedPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FragmentHandler.replaceFragment(mContext, fragmentType.DISPLAYPOST, post);
      }
    });

    if (!Utils.isNullOrEmpty(post.getImageAddress()))
    {
      Picasso.with(mContext).load(Constants.General.BLOB_PROTOCOL + post.getImageAddress()).noPlaceholder().noFade().into(holder.sharedPicture);
    } else
    {
      holder.sharedPicture.setImageResource(R.drawable.placeholder);
    }

    Date date = new Date();
    if (message.getSendDateTime().contains("T"))
    {
      date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
    } else
    {
      date.setTime(Long.valueOf(message.getSendDateTime()));
    }

    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

    holder.time.setText(time);
  }


  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    final RCompactMessage message = mMessages.get(position);
    SelfViewHolder selfHolder = null;
    SelfStickerViewHolder selfStickerHolder = null;
    SelfImageViewHolder selfImageHolder = null;
    SelfVideoViewHolder selfVideoHolder = null;
    SelfPdfViewHolder selfPdfHolder = null;
    SelfSharedViewHolder selfSharedHolder = null;
    SelfLocationViewHolder selfLocationHolder = null;
    OtherLocationViewHolder otherLocationHolder = null;
    OthersViewHolder othersHolder = null;
    OthersImageViewHolder othersImageHolder = null;
    OthersPdfViewHolder othersPdfHolder = null;
    OthersVideoViewHolder othersVideoHolder = null;
    OthersSharedViewHolder othersSharedHolder = null;
    OthersStickerViewHolder othersStickerHolder = null;
    SettingsViewHolder settingsHolder = null;
    LoadMoreViewHolder loadMoreHolder = null;

    if (message != null)
    {
      Date date = new Date();
      if (message.getSendDateTime().contains("T"))
      {
        date.setTime(TimeHelper.utcToTimezone(mContext, message.getSendDateTime()));
      } else
      {
        date.setTime(Long.valueOf(message.getSendDateTime()));
      }
      String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);

      if (message.getContentType() != EnumMessageContentType.Announcement)
      {
        if (message.getSenderId().equals(Logged.Models.getUserProfile().getId()))
        {

          if (convertView != null && (convertView.getTag() instanceof LoadMoreViewHolder
            || convertView.getTag() instanceof OthersSharedViewHolder
            || convertView.getTag() instanceof OthersPdfViewHolder
            || convertView.getTag() instanceof OthersViewHolder
            || convertView.getTag() instanceof OthersVideoViewHolder
            || convertView.getTag() instanceof OthersImageViewHolder
            || convertView.getTag() instanceof OthersStickerViewHolder
            || convertView.getTag() instanceof OtherLocationViewHolder
            || convertView.getTag() instanceof SettingsViewHolder))
          {
            convertView = null;
          }

          if (convertView != null && (convertView.getTag() instanceof SelfSharedViewHolder
            || convertView.getTag() instanceof SelfPdfViewHolder
            || convertView.getTag() instanceof SelfImageViewHolder
            || convertView.getTag() instanceof SelfVideoViewHolder
            || convertView.getTag() instanceof SelfStickerViewHolder
            || convertView.getTag() instanceof SelfLocationViewHolder
            || convertView.getTag() instanceof SelfViewHolder))
          {
            convertView = null;
          }

          if (convertView == null)
          {

            if (isSticker(message))
            {
              convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_self_sticker, null, false);
              selfStickerHolder = new SelfStickerViewHolder();
            } else if (isText(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_chat_self, null, false);
              selfHolder = new SelfViewHolder();
            } else if (isImage(message))
            {
              convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_self_image, null, false);
              selfImageHolder = new SelfImageViewHolder();
            } else if (isVideo(message))
            {
              convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_self_video, null, false);
              selfVideoHolder = new SelfVideoViewHolder();
            } else if (isFile(message))
            {
              convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_self_file, null, false);
              selfPdfHolder = new SelfPdfViewHolder();
            } else if (isSharedItem(message))
            {
              convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_self_share_item, null, false);
              selfSharedHolder = new SelfSharedViewHolder();
            } else if (isLocation(message))
            {
              convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_self_location, null, false);
              selfLocationHolder = new SelfLocationViewHolder();
            }
            if (isSticker(message))
            {
              selfStickerHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              selfStickerHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfStickerHolder.stickerImage = (ImageView) convertView.findViewById(R.id.image_view_sticker);
              selfStickerHolder.stickerImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfStickerHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              selfStickerHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfStickerHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);
              selfStickerHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfStickerHolder.time.setTag(message.getMessageId());
              convertView.setTag(selfStickerHolder);

            } else if (isText(message))
            {
              selfHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              selfHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfHolder.message = (TextView) convertView.findViewById(R.id.text_view_message);
              selfHolder.message.setTransformationMethod(new LinkTransformationMethod(mContext));
              selfHolder.message.setMovementMethod(LinkMovementMethod.getInstance());
              selfHolder.message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              selfHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);
              selfHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfHolder.time.setTag(message.getMessageId());
              convertView.setTag(selfHolder);
            } else if (isImage(message))
            {
              final SelfImageViewHolder finalSelfImageHolder = selfImageHolder;
              selfImageHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              selfImageHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfImageHolder.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfImageHolder.image = (ImageView) convertView.findViewById(R.id.image_view_image);
              selfImageHolder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfImageHolder.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfImageHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              selfImageHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfImageHolder.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfImageHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);

              selfImageHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfImageHolder.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfImageHolder.loading = (CircularProgressView) convertView.findViewById(R.id.progress_bar_loading);
              selfImageHolder.time.setTag(message.getMessageId());

              selfImageHolder.cancel = (ImageButton) convertView.findViewById(R.id.image_button_retry);
              selfImageHolder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickCancel(message, position);
                }
              });

              convertView.setTag(selfImageHolder);
            } else if (isVideo(message))
            {
              selfVideoHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              final SelfVideoViewHolder finalSelfVideoHolder = selfVideoHolder;
              selfVideoHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfVideoHolder.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfVideoHolder.image = (ImageView) convertView.findViewById(R.id.image_view_image);
              selfVideoHolder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfVideoHolder.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfVideoHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              selfVideoHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfVideoHolder.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfVideoHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);
              selfVideoHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfVideoHolder.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfVideoHolder.loading = (CircularProgressView) convertView.findViewById(R.id.progress_bar_loading);
              selfVideoHolder.retry = (ImageButton) convertView.findViewById(R.id.image_button_retry);
              selfVideoHolder.retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickCancel(message, position);
                }
              });
              selfVideoHolder.time.setTag(message.getMessageId());
              convertView.setTag(selfVideoHolder);
            } else if (isFile(message))
            {
              selfPdfHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              final SelfPdfViewHolder finalSelfPdfHolder1 = selfPdfHolder;
              selfPdfHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfPdfHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfPdfHolder.contentContainer = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container_content);
              selfPdfHolder.contentContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfPdfHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfPdfHolder.name = (TextView) convertView.findViewById(R.id.text_view_name);
              selfPdfHolder.name.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfPdfHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfPdfHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              selfPdfHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfPdfHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfPdfHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);
              selfPdfHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfPdfHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfPdfHolder.loading = (CircularProgressView) convertView.findViewById(R.id.progress_bar_loading);
              selfPdfHolder.retry = (ImageButton) convertView.findViewById(R.id.image_button_retry);
              selfPdfHolder.time.setTag(message.getMessageId());
              convertView.setTag(selfPdfHolder);
            } else if (isSharedItem(message))
            {
              selfSharedHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              selfSharedHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfSharedHolder.contentContainer = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container_content);
              selfSharedHolder.contentContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfSharedHolder.sharedPicture = (ImageView) convertView.findViewById(R.id.image_view_picture);
              selfSharedHolder.sharedPicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfSharedHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              selfSharedHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfSharedHolder.sharedTitle = (TextView) convertView.findViewById(R.id.text_view_name);
              selfSharedHolder.sharedTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfSharedHolder.sharedDesc = (TextView) convertView.findViewById(R.id.text_view_description);
              selfSharedHolder.sharedDesc.setTransformationMethod(new LinkTransformationMethod(mContext));
              selfSharedHolder.sharedDesc.setMovementMethod(LinkMovementMethod.getInstance());
              selfSharedHolder.sharedDesc.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfSharedHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);
              selfSharedHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfSharedHolder.time.setTag(message.getMessageId());
              convertView.setTag(selfSharedHolder);
            } else if (isLocation(message))
            {
              assert convertView != null;
              assert selfLocationHolder != null;
              selfLocationHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container_content);
              selfLocationHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfLocationHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);
              selfLocationHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                  setLongClick(message, position);
                  return true;
                }
              });

              selfLocationHolder.locationMapView = (ImageView) convertView.findViewById(R.id.map_view);
              selfLocationHolder.locationMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  Intent intent = new Intent(mContext, ActivityPickLocation.class);
                  intent.putExtra("return", false);
                  intent.putExtra("location", mPickedLocation);
                  mContext.startActivity(intent);
                }
              });
              selfLocationHolder.locationMapView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                  setLongClick(message, position);
                  return true;
                }
              });
              selfLocationHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              selfLocationHolder.time.setText(time);
              selfLocationHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                  setLongClick(message, position);
                  return true;
                }
              });

              selfLocationHolder.time.setTag(message.getMessageId());
              convertView.setTag(selfLocationHolder);
            }
          } else
          {
            if (isSticker(message))
            {
              selfStickerHolder = (SelfStickerViewHolder) convertView.getTag();
            } else if (isText(message))
            {
              selfHolder = (SelfViewHolder) convertView.getTag();
            } else if (isImage(message))
            {
              selfImageHolder = (SelfImageViewHolder) convertView.getTag();
            } else if (isVideo(message))
            {
              selfVideoHolder = (SelfVideoViewHolder) convertView.getTag();
            } else if (isFile(message))
            {
              selfPdfHolder = (SelfPdfViewHolder) convertView.getTag();
            } else if (isSharedItem(message))
            {
              selfSharedHolder = (SelfSharedViewHolder) convertView.getTag();
            } else if (isLocation(message))
            {
              selfLocationHolder = (SelfLocationViewHolder) convertView.getTag();
            }
          }
          if (isSticker(message))
          {

            ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
            final SelfStickerViewHolder finalSelfStickerHolder = selfStickerHolder;
            imageLoader.loadImage(Constants.General.BLOB_PROTOCOL + message.getThumbnailAddress(), new SimpleImageLoadingListener() {
              @Override
              public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                finalSelfStickerHolder.stickerImage.setImageBitmap(loadedImage);
              }
            });


            if (message.getMessageStatus() == EnumMessageStatus.Sent)
            {
              selfStickerHolder.status.setImageResource(R.drawable.ic_single_tick);
              selfStickerHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
            {
              selfStickerHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color gray
              selfStickerHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
            {
              selfStickerHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color accent
              selfStickerHolder.status.setColorFilter(Color.argb(255, 1, 87, 155));
            } else if (message.getMessageStatus() == EnumMessageStatus.Failed)
            {
              selfStickerHolder.status.setImageResource(R.drawable.ic_failed);
              selfStickerHolder.status.clearColorFilter();
            } else
            {
              selfStickerHolder.status.setImageResource(R.drawable.ic_clock);
              selfStickerHolder.status.setColorFilter(Color.argb(255, 1, 87, 155));
            }
          } else if (isText(message))
          {
            selfHolder.message.setText(Emoji.replaceEmoji(mContext, message.getText(), selfHolder.message
              .getPaint()
              .getFontMetricsInt(), AndroidUtilities.dp(16)));

            selfHolder.time.setText(time);


            if (message.getMessageStatus() == EnumMessageStatus.Sent)
            {
              selfHolder.status.setImageResource(R.drawable.ic_single_tick);
              selfHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
            {
              selfHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color gray
              selfHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
            {
              selfHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color accent
              selfHolder.status.setColorFilter(Color.argb(255, 3, 169, 244));
            } else if (message.getMessageStatus() == EnumMessageStatus.Failed)
            {
              selfHolder.status.setImageResource(R.drawable.ic_failed);
              selfHolder.status.clearColorFilter();
            } else
            {
              selfHolder.status.setImageResource(R.drawable.ic_clock);
              selfHolder.status.setColorFilter(Color.argb(255, 3, 169, 244));
            }
          } else if (isImage(message))
          {


            String localImagePath = message.getFilePath() != null ? message.getFilePath().split(",")[0] : null;
            final File file = localImagePath != null ? new File(localImagePath) : null;
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selfImageHolder.image.getLayoutParams();
            params.width = mImageWidth;
            params.height = mImageWidth;
            selfImageHolder.image.setLayoutParams(params);

            selfImageHolder.image.setImageURI(null);
            selfImageHolder.image.setImageDrawable(null);
            final View view = selfImageHolder.image;
            selfImageHolder.image.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                ArrayList<Uri> imageList = new ArrayList<Uri>();
                imageList.add(file != null ? Uri.fromFile(file) : Uri.parse(message.getThumbnailAddress()));
                Utils.displayImageInternalGallery(mContext, imageList, view, 0);

              }
            });
            if (file != null && file.exists())
            {
              Picasso.with(mContext).load(file).priority(Picasso.Priority.HIGH).noPlaceholder().noFade().into(selfImageHolder.image);
            } else
            {
              Picasso.with(mContext).load(message.getThumbnailAddress()).priority(Picasso.Priority.HIGH).noFade().noPlaceholder().into(selfImageHolder.image);
            }
            selfImageHolder.time.setText(time);


            if (message.getMessageStatus() == EnumMessageStatus.Sent)
            {
              selfImageHolder.status.setImageResource(R.drawable.ic_single_tick);
              selfImageHolder.status.setColorFilter(Color.argb(255, 255, 255, 255));
            } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
            {
              selfImageHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color gray
              selfImageHolder.status.setColorFilter(Color.argb(255, 255, 255, 255));
            } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
            {
              selfImageHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color accent
              selfImageHolder.status.setColorFilter(Color.argb(255, 3, 169, 244));
            } else if (message.getMessageStatus() == EnumMessageStatus.Failed)
            {
              selfImageHolder.status.setImageResource(R.drawable.ic_failed);
              selfImageHolder.status.clearColorFilter();
            } else
            {
              selfImageHolder.status.setImageResource(R.drawable.ic_clock_white);
              selfImageHolder.status.setColorFilter(Color.argb(255, 255, 255, 255));
            }


            if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
            {
              selfImageHolder.cancel.setVisibility(View.VISIBLE);
              selfImageHolder.loading.setVisibility(View.VISIBLE);
              selfImageHolder.loading.setProgress(MyApplication.mUploadingFiles.get(message.getMessageId()));
            } else
            {
              selfImageHolder.loading.setVisibility(View.GONE);
              selfImageHolder.cancel.setVisibility(View.GONE);
            }
          } else if (isVideo(message))
          {
            final File imageFile;

            final File videoFile;
            if (message.getFilePath() == null)
            {
              imageFile = null;
              videoFile = null;
            } else
            {
              String[] stringss = message.getFilePath().split(",");
              String localImageAddress = stringss[0];
              String localVideoAddress = stringss[1];

              imageFile = new File(localImageAddress);
              videoFile = new File(localVideoAddress);
            }


            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selfVideoHolder.image.getLayoutParams();
            params.width = mImageWidth;
            params.height = mImageWidth;
            selfVideoHolder.image.setLayoutParams(params);

            selfVideoHolder.retry.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (v.getTag().equals("play"))
                {
                  Utils.playVideoInGallery(mContext, videoFile == null ? Uri.parse(message.getContentAddress()) : Uri.fromFile(videoFile));
                }
              }
            });

            selfVideoHolder.image.setImageURI(null);
            selfVideoHolder.image.setImageDrawable(null);
            final SelfVideoViewHolder finalUserTypeSelfVideoHolder = selfVideoHolder;
            if (imageFile != null && imageFile.exists())
            {
              Picasso.with(mContext).load(imageFile).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(selfVideoHolder.image);
            } else
            {
              Picasso.with(mContext).load(message.getThumbnailAddress()).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(selfVideoHolder.image);
            }

            selfVideoHolder.time.setText(time);


            if (message.getMessageStatus() == EnumMessageStatus.Sent)
            {
              selfVideoHolder.status.setImageResource(R.drawable.ic_single_tick);
              selfVideoHolder.status.setColorFilter(Color.argb(255, 255, 255, 255));
            } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
            {
              selfVideoHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color gray
              selfVideoHolder.status.setColorFilter(Color.argb(255, 255, 255, 255));
            } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
            {
              selfVideoHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color accent
              selfVideoHolder.status.setColorFilter(Color.argb(255, 3, 169, 244));
            } else if (message.getMessageStatus() == EnumMessageStatus.Failed)
            {
              selfVideoHolder.status.setImageResource(R.drawable.ic_failed);
              selfVideoHolder.status.clearColorFilter();
            } else
            {
              selfVideoHolder.status.setImageResource(R.drawable.ic_clock_white);
              selfVideoHolder.status.setColorFilter(Color.argb(255, 255, 255, 255));
            }

            if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
            {
              finalUserTypeSelfVideoHolder.retry.setVisibility(View.VISIBLE);
              finalUserTypeSelfVideoHolder.retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickCancel(message, position);
                }
              });
              finalUserTypeSelfVideoHolder.loading.setVisibility(View.VISIBLE);
              finalUserTypeSelfVideoHolder.loading.setProgress(MyApplication.mUploadingFiles.get(message.getMessageId()));
            } else
            {
              if (videoFile != null && videoFile.exists())
              {
                finalUserTypeSelfVideoHolder.retry.setBackgroundResource(R.drawable.file_play);
                finalUserTypeSelfVideoHolder.retry.setTag("play");
              } else
              {
                finalUserTypeSelfVideoHolder.retry.setBackgroundResource(R.drawable.file_download);
                finalUserTypeSelfVideoHolder.retry.setTag("download");
              }

              finalUserTypeSelfVideoHolder.loading.setVisibility(View.GONE);

              finalUserTypeSelfVideoHolder.retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  ThinDownloadManager downloadManager = new ThinDownloadManager();
                  if (v.getTag().equals("play"))
                  {
                    Utils.playVideoInGallery(mContext, Uri.fromFile(new File(message.getFilePath().split(",")[1])));
                  } else if (v.getTag().equals("download"))
                  {
                    String[] videoStrings = message.getContentAddress().split("/");
                    Utils.checkForAppPathsExistence(mContext);
                    File downloadingMediaFile = new File(mContext.getCacheDir(), videoStrings[videoStrings.length - 1]);
                    DownloadRequest downloadRequest = createDownloadRequestForVideo(finalUserTypeSelfVideoHolder, position, message, Uri.fromFile(downloadingMediaFile), Uri.parse(message.getContentAddress()));
                    downloadManager.add(downloadRequest);
                    finalUserTypeSelfVideoHolder.loading.setVisibility(View.VISIBLE);
                    finalUserTypeSelfVideoHolder.retry.setTag("cancel");
                    finalUserTypeSelfVideoHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                  } else if (v.getTag().equals("cancel"))
                  {
                    downloadManager.cancelAll();
                    finalUserTypeSelfVideoHolder.loading.setVisibility(View.GONE);
                    finalUserTypeSelfVideoHolder.retry.setTag("download");
                    finalUserTypeSelfVideoHolder.retry.setBackgroundResource(R.drawable.file_download);
                  }
                }
              });
            }
          } else if (isFile(message))
          {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) selfPdfHolder.contentContainer.getLayoutParams();
            params.width = mImageWidth;
            selfPdfHolder.contentContainer.setLayoutParams(params);

            final File pdfFile = message.getFilePath() != null ? new File(message.getFilePath().split(",")[2]) : null;
            final SelfPdfViewHolder finalSelfPdfHolder = selfPdfHolder;
            selfPdfHolder.retry.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                ThinDownloadManager downloadManager = new ThinDownloadManager();
                if (v.getTag().equals("open"))
                {
                  Utils.displayFile(mContext, Uri.fromFile(pdfFile));
                } else if (v.getTag().equals("download"))
                {

                  String[] strings = message.getContentAddress().split("/");
                  Utils.checkForAppPathsExistence(mContext);
                  final Uri FileDestinationUri = Uri.parse(Constants.General.APP_FOLDER_FILE_PATH + strings[strings.length - 1]);
                  finalSelfPdfHolder.loading.setVisibility(View.VISIBLE);
                  finalSelfPdfHolder.retry.setTag("cancel");
                  finalSelfPdfHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                  DownloadRequest downloadRequest = createDownloadRequestForPdf(finalSelfPdfHolder, position, message, FileDestinationUri, Uri.parse(message.getContentAddress()));
                  downloadManager.add(downloadRequest);
                } else if (v.getTag().equals("cancel"))
                {
                  downloadManager.cancelAll();
                  finalSelfPdfHolder.loading.setVisibility(View.GONE);
                  finalSelfPdfHolder.retry.setTag("download");
                  finalSelfPdfHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
                }
              }
            });

            selfPdfHolder.name.setText(message.getContentSize());
            selfPdfHolder.time.setText(time);


            if (message.getMessageStatus() == EnumMessageStatus.Sent)
            {
              selfPdfHolder.status.setImageResource(R.drawable.ic_single_tick);
              selfPdfHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
            {
              selfPdfHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color gray
              selfPdfHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
            {
              selfPdfHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color accent
              selfPdfHolder.status.setColorFilter(Color.argb(255, 3, 169, 244));
            } else if (message.getMessageStatus() == EnumMessageStatus.Failed)
            {
              selfPdfHolder.status.setImageResource(R.drawable.ic_failed);
              selfPdfHolder.status.clearColorFilter();
            } else
            {
              selfPdfHolder.status.setImageResource(R.drawable.ic_clock_white);
              selfPdfHolder.status.setColorFilter(Color.argb(255, 3, 169, 244));
            }

            if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
            {
              selfPdfHolder.retry.setVisibility(View.VISIBLE);
              selfPdfHolder.retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickCancel(message, position);
                }
              });
              selfPdfHolder.loading.setVisibility(View.VISIBLE);
              selfPdfHolder.loading.setProgress(MyApplication.mUploadingFiles.get(message.getMessageId()));
            } else
            {
              selfPdfHolder.loading.setVisibility(View.GONE);
              if (pdfFile != null && pdfFile.exists())
              {
                selfPdfHolder.retry.setBackgroundResource(R.drawable.file_open);
                selfPdfHolder.retry.setTag("open");
              } else
              {
                selfPdfHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
                selfPdfHolder.retry.setTag("download");
              }
            }

          } else if (isSharedItem(message))
          {

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) selfSharedHolder.contentContainer.getLayoutParams();
            params.width = mImageWidth;
            selfSharedHolder.contentContainer.setLayoutParams(params);

            if (message.getContentType() == EnumMessageContentType.SharedProduct)
            {
              Product product = new Gson().fromJson(message.getContentSize(), Product.class);
              setSharedProduct(selfSharedHolder, message, product);
            } else if (message.getContentType() == EnumMessageContentType.SharedBusiness)
            {
              Shop shop = new Gson().fromJson(message.getContentSize(), Shop.class);
              setSharedShop(selfSharedHolder, message, shop);
            } else if (message.getContentType() == EnumMessageContentType.SharedComplex)
            {
              Complex complex = new Gson().fromJson(message.getContentSize(), Complex.class);
              setSharedComplex(selfSharedHolder, message, complex);
            } else if (message.getContentType() == EnumMessageContentType.SharedPost)
            {
              Post Post = new Gson().fromJson(message.getContentSize(), Post.class);
              setSharedPostTimeline(selfSharedHolder, message, Post);
            }

          } else if (isLocation(message))
          {

            assert selfLocationHolder != null;
            mPickedLocation = message.getText();

            Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(mPickedLocation);
            String[] latLngString = new String[2];
            while (m.find())
            {
              latLngString = m.group(1).split(",");
            }
            double lat = Double.parseDouble(latLngString[0]);
            double lng = Double.parseDouble(latLngString[1]);
            Picasso.with(mContext).load(SepehrUtil.getMapStaticUrl(mContext, lat, lng)).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(selfLocationHolder.locationMapView);


            if (message.getMessageStatus() == EnumMessageStatus.Sent)
            {
              selfLocationHolder.status.setImageResource(R.drawable.ic_single_tick);
              selfLocationHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
            {
              selfLocationHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color gray
              selfLocationHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
            {
              selfLocationHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color accent
              selfLocationHolder.status.setColorFilter(Color.argb(255, 1, 87, 155));
            } else if (message.getMessageStatus() == EnumMessageStatus.Failed)
            {
              selfLocationHolder.status.setImageResource(R.drawable.ic_failed);
              selfLocationHolder.status.clearColorFilter();
            } else
            {
              selfLocationHolder.status.setImageResource(R.drawable.ic_clock);
              selfLocationHolder.status.setColorFilter(Color.argb(255, 1, 87, 155));
            }
          }
        } else
        {

          if (convertView != null && (convertView.getTag() instanceof LoadMoreViewHolder
            || convertView.getTag() instanceof SelfSharedViewHolder
            || convertView.getTag() instanceof OthersPdfViewHolder
            || convertView.getTag() instanceof SelfPdfViewHolder
            || convertView.getTag() instanceof SelfViewHolder
            || convertView.getTag() instanceof SelfVideoViewHolder
            || convertView.getTag() instanceof SelfImageViewHolder
            || convertView.getTag() instanceof SelfLocationViewHolder
            || convertView.getTag() instanceof SettingsViewHolder
            || convertView.getTag() instanceof SelfStickerViewHolder))
          {
            convertView = null;
          }

          if (convertView != null && (convertView.getTag() instanceof OthersSharedViewHolder
            || convertView.getTag() instanceof OthersViewHolder
            || convertView.getTag() instanceof OthersVideoViewHolder
            || convertView.getTag() instanceof OthersStickerViewHolder
            || convertView.getTag() instanceof OtherLocationViewHolder
            || convertView.getTag() instanceof OthersImageViewHolder))
          {
            convertView = null;
          }

          if (convertView == null)
          {
            if (isSticker(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_chat_others_sticker, null, false);
              othersStickerHolder = new OthersStickerViewHolder();
            } else if (isText(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_group_chat_user1, null, false);
              othersHolder = new OthersViewHolder();
            } else if (isImage(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_group_chat_user1_image, null, false);
              othersImageHolder = new OthersImageViewHolder();
            } else if (isVideo((message)))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_group_chat_user1_video, null, false);
              othersVideoHolder = new OthersVideoViewHolder();
            } else if (isFile(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_group_chat_user1_pdf, null, false);
              othersPdfHolder = new OthersPdfViewHolder();
            } else if (isSharedItem(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_group_chat_user1_share_item, null, false);
              othersSharedHolder = new OthersSharedViewHolder();
            } else if (isLocation(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_group_chat_user1_location, null, false);
              otherLocationHolder = new OtherLocationViewHolder();
            }
            if (isSticker(message))
            {
              othersStickerHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              othersStickerHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersStickerHolder.stickerImage = (ImageView) convertView.findViewById(R.id.image_view_sticker);
              othersStickerHolder.stickerImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersStickerHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              othersStickerHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersStickerHolder.time.setTag(message.getMessageId());

              convertView.setTag(othersStickerHolder);
            } else if (isText(message))
            {
              othersHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              othersHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersHolder.message = (TextView) convertView.findViewById(R.id.text_view_message);
              othersHolder.message.setTransformationMethod(new LinkTransformationMethod(mContext));
              othersHolder.message.setMovementMethod(LinkMovementMethod.getInstance());
              othersHolder.message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              othersHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersHolder.profileName = (TextView) convertView.findViewById(R.id.text_view_name);
              othersHolder.profileName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersHolder.profileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersHolder.profilePicture = (ComplexAvatarView) convertView.findViewById(R.id.image_view_picture);
              othersHolder.profilePicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersHolder.profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersHolder.time.setTag(message.getMessageId());

              convertView.setTag(othersHolder);
            } else if (isImage(message))
            {
              othersImageHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              othersImageHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersImageHolder.image = (ImageView) convertView.findViewById(R.id.image_view_image);
              othersImageHolder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersImageHolder.loading = (CircularProgressView) convertView.findViewById(R.id.progress_bar_loading);
              othersImageHolder.loading.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersImageHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              othersImageHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersImageHolder.retry = (ImageButton) convertView.findViewById(R.id.image_button_retry);
              othersImageHolder.retry.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersImageHolder.profileName = (TextView) convertView.findViewById(R.id.text_view_name);
              othersImageHolder.profileName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersImageHolder.profileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersImageHolder.profilePicture = (ComplexAvatarView) convertView.findViewById(R.id.image_view_picture);
              othersImageHolder.profilePicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersImageHolder.profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersImageHolder.time.setTag(message.getMessageId());

              convertView.setTag(othersImageHolder);
            } else if (isVideo(message))
            {
              othersVideoHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              othersVideoHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersVideoHolder.image = (ImageView) convertView.findViewById(R.id.image_view_image);
              othersVideoHolder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersVideoHolder.loading = (CircularProgressView) convertView.findViewById(R.id.progress_bar_loading);
              othersVideoHolder.loading.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersVideoHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              othersVideoHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersVideoHolder.retry = (ImageButton) convertView.findViewById(R.id.image_button_retry);
              othersVideoHolder.retry.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersVideoHolder.profileName = (TextView) convertView.findViewById(R.id.text_view_name);
              othersVideoHolder.profileName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersVideoHolder.profileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersVideoHolder.profilePicture = (ComplexAvatarView) convertView.findViewById(R.id.image_view_picture);
              othersVideoHolder.profilePicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersVideoHolder.profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersVideoHolder.time.setTag(message.getMessageId());

              convertView.setTag(othersVideoHolder);
            } else if (isFile(message))
            {
              othersPdfHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              othersPdfHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersPdfHolder.name = (TextView) convertView.findViewById(R.id.text_view_name);
              othersPdfHolder.name.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersPdfHolder.contentContainer = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container_content);
              othersPdfHolder.contentContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersPdfHolder.loading = (CircularProgressView) convertView.findViewById(R.id.progress_bar_loading);
              othersPdfHolder.loading.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersPdfHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              othersPdfHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersPdfHolder.retry = (ImageButton) convertView.findViewById(R.id.image_button_retry);
              othersPdfHolder.retry.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersPdfHolder.profileName = (TextView) convertView.findViewById(R.id.text_view_person_name);
              othersPdfHolder.profileName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersPdfHolder.profileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersPdfHolder.profilePicture = (ComplexAvatarView) convertView.findViewById(R.id.image_view_picture);
              othersPdfHolder.profilePicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersPdfHolder.profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersPdfHolder.time.setTag(message.getMessageId());

              convertView.setTag(othersPdfHolder);
            } else if (isSharedItem(message))
            {
              othersSharedHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              othersSharedHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersSharedHolder.contentContainer = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container_content);
              othersSharedHolder.contentContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersSharedHolder.sharedTitle = (TextView) convertView.findViewById(R.id.text_view_name);
              othersSharedHolder.sharedTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersSharedHolder.sharedDesc = (TextView) convertView.findViewById(R.id.text_view_description);
              othersSharedHolder.sharedDesc.setTransformationMethod(new LinkTransformationMethod(mContext));
              othersSharedHolder.sharedDesc.setMovementMethod(LinkMovementMethod.getInstance());
              othersSharedHolder.sharedDesc.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersSharedHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              othersSharedHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersSharedHolder.sharedPicture = (ImageView) convertView.findViewById(R.id.image_view_picture);
              othersSharedHolder.sharedPicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersSharedHolder.sharedPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersSharedHolder.profilePicture = (ComplexAvatarView) convertView.findViewById(R.id.image_view_person_picture);
              othersSharedHolder.profilePicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersSharedHolder.profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });
              othersSharedHolder.time.setTag(message.getMessageId());

              convertView.setTag(othersSharedHolder);
            } else if (isLocation(message))
            {
              otherLocationHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container_content);
              otherLocationHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                  setLongClick(message, position);
                  return true;
                }
              });
              otherLocationHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);
              otherLocationHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                  setLongClick(message, position);
                  return true;
                }
              });

              otherLocationHolder.locationMapView = (ImageView) convertView.findViewById(R.id.map_view);
              otherLocationHolder.locationMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  Intent intent = new Intent(mContext, ActivityPickLocation.class);
                  intent.putExtra("return", false);
                  intent.putExtra("location", mPickedLocation);
                  mContext.startActivity(intent);
                }
              });
              otherLocationHolder.locationMapView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                  setLongClick(message, position);
                  return true;
                }
              });

              otherLocationHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              otherLocationHolder.time.setText(time);
              otherLocationHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                  setLongClick(message, position);
                  return true;
                }
              });
              otherLocationHolder.profilePicture = (ComplexAvatarView) convertView.findViewById(R.id.image_view_person_picture);
              otherLocationHolder.profilePicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              otherLocationHolder.profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClickProfile(message.getSenderId(), message.getSenderName(), message.getSenderImage());
                }
              });

              otherLocationHolder.time.setTag(message.getMessageId());
              convertView.setTag(otherLocationHolder);
            }

          } else
          {
            if (isSticker(message))
            {
              othersStickerHolder = (OthersStickerViewHolder) convertView.getTag();
            } else if (isText(message))
            {
              othersHolder = (OthersViewHolder) convertView.getTag();
            } else if (isImage(message))
            {
              othersImageHolder = (OthersImageViewHolder) convertView.getTag();
            } else if (isVideo(message))
            {
              othersVideoHolder = (OthersVideoViewHolder) convertView.getTag();
            } else if (isFile(message))
            {
              othersPdfHolder = (OthersPdfViewHolder) convertView.getTag();
            } else if (isSharedItem(message))
            {
              othersSharedHolder = (OthersSharedViewHolder) convertView.getTag();
            } else if (isLocation(message))
            {
              otherLocationHolder = (OtherLocationViewHolder) convertView.getTag();
            }
          }
          if (isSticker(message))
          {
            ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
            final OthersStickerViewHolder finalOtherStickerHolder = othersStickerHolder;
            imageLoader.loadImage(Constants.General.BLOB_PROTOCOL + message.getThumbnailAddress(), new SimpleImageLoadingListener() {
              @Override
              public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                finalOtherStickerHolder.stickerImage.setImageBitmap(loadedImage);
              }
            });

          } else if (isText(message))
          {
            othersHolder.message.setText(Emoji.replaceEmoji(mContext, message.getText(), othersHolder.message
              .getPaint()
              .getFontMetricsInt(), AndroidUtilities.dp(16)));
            othersHolder.time.setText(time);
            othersHolder.profileName.setText(message.getSenderName());
            final OthersViewHolder finalOthersHolder = othersHolder;
            finalOthersHolder.profilePicture.makeAllDefaults();

            finalOthersHolder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(message.getSenderName()).setImageUrl(Constants.General.BLOB_PROTOCOL + message.getSenderImage());

          } else if (isImage(message))
          {
            othersImageHolder.profileName.setText(message.getSenderName());
            final OthersImageViewHolder finalOthersImageHolder = othersImageHolder;
            finalOthersImageHolder.profilePicture.makeAllDefaults();

            finalOthersImageHolder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(message.getSenderName()).setImageUrl(Constants.General.BLOB_PROTOCOL + message.getSenderImage());

            othersImageHolder.image.setImageURI(null);
            othersImageHolder.image.setImageDrawable(null);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) othersImageHolder.image.getLayoutParams();
            params.height = mImageWidth;
            params.width = mImageWidth;
            othersImageHolder.image.setLayoutParams(params);
            final OthersImageViewHolder finalUserTypeOthersImageHolder = othersImageHolder;

            String[] stringss = message.getFilePath() != null ? message.getFilePath().split(",") : null;
            final File localImage = stringss != null ? new File(stringss[0]) : null;
            if (localImage == null || !localImage.exists())
            {
              final Uri downloadUri = Uri.parse(message.getThumbnailAddress());
              String[] strings = message.getThumbnailAddress().split("/");
              Utils.checkForAppPathsExistence(mContext);
              final Uri destinationUri = Uri.parse(Constants.General.APP_FOLDER_IMAGE_PATH + strings[strings.length - 1]);

              MyApplication.DownloadRequestStruct downloadRequestStruct = MyApplication.getInstance().hasRequestDownload(message.getMessageId());

              if (downloadRequestStruct != null)
              {
                othersImageHolder.loading.setVisibility(View.VISIBLE);
                othersImageHolder.retry.setVisibility(View.VISIBLE);
                othersImageHolder.retry.setTag("cancel");
                othersImageHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                othersImageHolder.loading.setProgress(downloadRequestStruct.progress);
              } else
              {
                othersImageHolder.loading.setVisibility(View.GONE);
                if (localImage == null || !localImage.exists())
                {
                  DownloadRequest request = createDownloadRequestForImage(finalUserTypeOthersImageHolder, position, message, destinationUri, downloadUri);
                  MyApplication.getInstance().addDownloadRequest(request, message.getMessageId());
                }
              }

              othersImageHolder.retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  if (v.getTag().equals("download"))
                  {
                    DownloadRequest request = createDownloadRequestForImage(finalUserTypeOthersImageHolder, position, message, destinationUri, downloadUri);
                    MyApplication.getInstance().addDownloadRequest(request, message.getMessageId());

                    finalUserTypeOthersImageHolder.loading.setVisibility(View.VISIBLE);
                    finalUserTypeOthersImageHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                    finalUserTypeOthersImageHolder.retry.setTag("cancel");
                  } else if (v.getTag().equals("cancel"))
                  {
                    MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
                    finalUserTypeOthersImageHolder.loading.setVisibility(View.GONE);
                    finalUserTypeOthersImageHolder.retry.setBackgroundResource(R.drawable.file_download);
                    finalUserTypeOthersImageHolder.retry.setTag("download");
                  }
                }
              });
            } else
            {
              finalUserTypeOthersImageHolder.loading.setVisibility(View.GONE);
              finalUserTypeOthersImageHolder.retry.setVisibility(View.GONE);
              finalUserTypeOthersImageHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  ArrayList<Uri> imageList = new ArrayList<Uri>();
                  imageList.add(Uri.fromFile(localImage));
                  Utils.displayImageInternalGallery(mContext, imageList, finalUserTypeOthersImageHolder.image, 0);

                }
              });
              Picasso.with(mContext).load(Uri.fromFile(localImage)).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(finalUserTypeOthersImageHolder.image);
            }

            othersImageHolder.time.setText(time);
          } else if (isVideo(message))
          {
            othersVideoHolder.profileName.setText(message.getSenderName());
            final OthersVideoViewHolder finalOthersVideoHolder = othersVideoHolder;
            finalOthersVideoHolder.profilePicture.makeAllDefaults();
            finalOthersVideoHolder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(message.getSenderName()).setImageUrl(Constants.General.BLOB_PROTOCOL + message.getSenderImage());

            othersVideoHolder.image.setImageURI(null);
            othersVideoHolder.image.setImageDrawable(null);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) othersVideoHolder.image.getLayoutParams();
            params.height = mImageWidth;
            params.width = mImageWidth;
            othersVideoHolder.image.setLayoutParams(params);
            final OthersVideoViewHolder finalUserTypeOthersVideoHolder = othersVideoHolder;

            Picasso.with(mContext).load(message.getThumbnailAddress()).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(othersVideoHolder.image);

            final Uri downloadVideoUri = Uri.parse(message.getContentAddress());
            String[] videoStrings = message.getContentAddress().split("/");
            Utils.checkForAppPathsExistence(mContext);
            final Uri videoDestinationUri = Uri.parse(Constants.General.APP_FOLDER_VIDEO_PATH + videoStrings[videoStrings.length - 1]);

            MyApplication.DownloadRequestStruct downloadRequestStruct = MyApplication.getInstance().hasRequestDownload(message.getMessageId());

            if (downloadRequestStruct != null)
            {
              othersVideoHolder.loading.setVisibility(View.VISIBLE);
              othersVideoHolder.retry.setVisibility(View.VISIBLE);
              othersVideoHolder.retry.setTag("cancel");
              othersVideoHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
              othersVideoHolder.loading.setProgress(downloadRequestStruct.progress);
            } else
            {
              othersVideoHolder.loading.setVisibility(View.GONE);

              // 3 is commas size in localPaths
              if (message.getFilePath() != null && !message.getFilePath().isEmpty() && message.getFilePath().split(",").length >= 2 && new File(message.getFilePath().split(",")[1]).exists())
              {
                othersVideoHolder.retry.setTag("play");
                othersVideoHolder.retry.setBackgroundResource(R.drawable.file_play);
              } else
              {
                othersVideoHolder.retry.setTag("download");
                othersVideoHolder.retry.setBackgroundResource(R.drawable.file_download);
              }
            }

            othersVideoHolder.retry.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                if (v.getTag().equals("download"))
                {
                  DownloadRequest request = createDownloadRequestForVideo(finalUserTypeOthersVideoHolder, position, message, videoDestinationUri, downloadVideoUri);
                  MyApplication.getInstance().addDownloadRequest(request, message.getMessageId());
                  finalUserTypeOthersVideoHolder.loading.setVisibility(View.VISIBLE);
                  finalUserTypeOthersVideoHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                  finalUserTypeOthersVideoHolder.retry.setTag("cancel");
                } else if (v.getTag().equals("cancel"))
                {
                  MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
                  finalUserTypeOthersVideoHolder.loading.setVisibility(View.GONE);
                  finalUserTypeOthersVideoHolder.retry.setBackgroundResource(R.drawable.file_download);
                  finalUserTypeOthersVideoHolder.retry.setTag("download");
                } else if (v.getTag().equals("play"))
                {
                  Utils.playVideoInGallery(mContext, Uri.fromFile(new File(videoDestinationUri.getPath())));
                }
              }
            });

            othersVideoHolder.time.setText(time);
          } else if (isFile(message))
          {

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) othersPdfHolder.contentContainer.getLayoutParams();
            params.width = mImageWidth;
            othersPdfHolder.contentContainer.setLayoutParams(params);

            othersPdfHolder.profileName.setText(message.getSenderName());
            final OthersPdfViewHolder finalOthersPdfHolder = othersPdfHolder;
            finalOthersPdfHolder.profilePicture.makeAllDefaults();
            finalOthersPdfHolder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(message.getSenderName()).setImageUrl(Constants.General.BLOB_PROTOCOL + message.getSenderImage());

            othersPdfHolder.name.setText(message.getContentSize());
            final OthersPdfViewHolder finalUserTypeOthersPdfHolder = othersPdfHolder;

            final File localPdf = message.getFilePath() != null ? new File(message.getFilePath().split(",")[2]) : null;

            final Uri downloadPdfUri = Uri.parse(message.getContentAddress());
            String[] strings = message.getContentAddress().split("/");
            Utils.checkForAppPathsExistence(mContext);
            final Uri pdfDestinationUri = Uri.parse(Constants.General.APP_FOLDER_FILE_PATH + strings[strings.length - 1]);

            othersPdfHolder.retry.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (v.getTag().equals("download"))
                {
                  DownloadRequest request = createDownloadRequestForPdf(finalUserTypeOthersPdfHolder, position, message, pdfDestinationUri, downloadPdfUri);
                  MyApplication.getInstance().addDownloadRequest(request, message.getMessageId());
                  finalUserTypeOthersPdfHolder.loading.setVisibility(View.VISIBLE);
                  finalUserTypeOthersPdfHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                  finalUserTypeOthersPdfHolder.retry.setTag("cancel");
                } else if (v.getTag().equals("cancel"))
                {
                  MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
                  finalUserTypeOthersPdfHolder.loading.setVisibility(View.GONE);
                  finalUserTypeOthersPdfHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
                  finalUserTypeOthersPdfHolder.retry.setTag("download");
                } else if (v.getTag().equals("open"))
                {
                  Utils.displayFile(mContext, Uri.fromFile(new File(pdfDestinationUri.getPath())));
                }
              }
            });

            MyApplication.DownloadRequestStruct downloadRequestStruct = MyApplication.getInstance().hasRequestDownload(message.getMessageId());

            if (downloadRequestStruct != null)
            {
              othersPdfHolder.loading.setVisibility(View.VISIBLE);
              othersPdfHolder.retry.setVisibility(View.VISIBLE);
              othersPdfHolder.retry.setTag("cancel");
              othersPdfHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
              othersPdfHolder.loading.setProgress(downloadRequestStruct.progress);
            } else
            {
              othersPdfHolder.loading.setVisibility(View.GONE);
              othersPdfHolder.retry.setVisibility(View.VISIBLE);
              if (localPdf == null || !localPdf.exists())
              {
                othersPdfHolder.retry.setTag("download");
                othersPdfHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
              } else
              {
                othersPdfHolder.retry.setTag("open");
                othersPdfHolder.retry.setBackgroundResource(R.drawable.file_open);
              }
            }

            othersPdfHolder.time.setText(time);
          } else if (isSharedItem(message))
          {

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) othersSharedHolder.contentContainer.getLayoutParams();
            params.width = mImageWidth;
            othersSharedHolder.contentContainer.setLayoutParams(params);

            if (message.getContentType() == EnumMessageContentType.SharedProduct)
            {
              Product product = new Gson().fromJson(message.getContentSize(), Product.class);
              setSharedProduct(othersSharedHolder, message, product);
            } else if (message.getContentType() == EnumMessageContentType.SharedBusiness)
            {
              Shop shop = new Gson().fromJson(message.getContentSize(), Shop.class);
              setSharedShop(othersSharedHolder, message, shop);
            } else if (message.getContentType() == EnumMessageContentType.SharedComplex)
            {
              Complex complex = new Gson().fromJson(message.getContentSize(), Complex.class);
              setSharedComplex(othersSharedHolder, message, complex);
            } else if (message.getContentType() == EnumMessageContentType.SharedPost)
            {

              Post Post = new Gson().fromJson(message.getContentSize(), Post.class);
              setSharedPostTimeline(othersSharedHolder, message, Post);
            }

            final OthersSharedViewHolder finalOthersSharedHolder = othersSharedHolder;
            finalOthersSharedHolder.profilePicture.makeAllDefaults();
            finalOthersSharedHolder.profilePicture.setImageLoader(MyApplication.getInstance().getImageLoader(), false).setText(message.getSenderName()).setImageUrl(Constants.General.BLOB_PROTOCOL + message.getSenderImage());

          } else if (isLocation(message))
          {
            assert otherLocationHolder != null;
            mPickedLocation = message.getText();

            String[] latLngString = mPickedLocation.split(",");
            double lat = Double.parseDouble(latLngString[0]);
            double lng = Double.parseDouble(latLngString[1]);
            Picasso.with(mContext).load(SepehrUtil.getMapStaticUrl(mContext, lat, lng)).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(otherLocationHolder.locationMapView);

          }
        }
      } else
      {

        if (convertView != null && (convertView.getTag() instanceof LoadMoreViewHolder
          || convertView.getTag() instanceof SelfSharedViewHolder
          || convertView.getTag() instanceof OthersSharedViewHolder
          || convertView.getTag() instanceof OthersPdfViewHolder
          || convertView.getTag() instanceof SelfPdfViewHolder
          || convertView.getTag() instanceof SelfImageViewHolder
          || convertView.getTag() instanceof SelfVideoViewHolder
          || convertView.getTag() instanceof OthersVideoViewHolder
          || convertView.getTag() instanceof OthersImageViewHolder
          || convertView.getTag() instanceof SelfViewHolder
          || convertView.getTag() instanceof OthersViewHolder
          || convertView.getTag() instanceof OthersStickerViewHolder
          || convertView.getTag() instanceof OtherLocationViewHolder
          || convertView.getTag() instanceof SelfLocationViewHolder
          || convertView.getTag() instanceof SelfStickerViewHolder))
        {
          convertView = null;
        }

        if (convertView == null)
        {
          convertView = LayoutInflater.from(mContext)
            .inflate(R.layout.item_chat_settings, null, false);

          settingsHolder = new SettingsViewHolder();

          settingsHolder.text = (TextView) convertView.findViewById(R.id.text_view_text);

          convertView.setTag(settingsHolder);

        } else
        {
          settingsHolder = (SettingsViewHolder) convertView.getTag();
        }

        settingsHolder.text.setText(message.getText());
      }
    } else

    {
      // If message is null, it means, a load more button for pagination
      if (convertView != null && (convertView.getTag() instanceof SettingsViewHolder
        || convertView.getTag() instanceof SelfSharedViewHolder
        || convertView.getTag() instanceof OthersSharedViewHolder
        || convertView.getTag() instanceof OthersPdfViewHolder
        || convertView.getTag() instanceof SelfPdfViewHolder
        || convertView.getTag() instanceof SelfImageViewHolder
        || convertView.getTag() instanceof SelfVideoViewHolder
        || convertView.getTag() instanceof OthersVideoViewHolder
        || convertView.getTag() instanceof OthersImageViewHolder
        || convertView.getTag() instanceof SelfViewHolder
        || convertView.getTag() instanceof OthersViewHolder
        || convertView.getTag() instanceof OthersStickerViewHolder
        || convertView.getTag() instanceof OtherLocationViewHolder
        || convertView.getTag() instanceof SelfLocationViewHolder
        || convertView.getTag() instanceof SelfStickerViewHolder))
      {
        convertView = null;
      }

      if (convertView == null)
      {
        convertView = LayoutInflater.from(mContext)
          .inflate(R.layout.item_chat_load_more, null, false);

        loadMoreHolder = new LoadMoreViewHolder();

        loadMoreHolder.loadMore = (Button) convertView.findViewById(R.id.button_load_more);
        loadMoreHolder.loadMore.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mOnLoadMoreClickListener.onLoadMoreClick();

            mMessages.remove(position);
            mFirstMessage = mMessages.get(0);
            notifyDataSetChanged();

            mListView.setSelection(mMessages.indexOf(mFirstMessage));
          }
        });

        convertView.setTag(loadMoreHolder);

      } else
      {
        loadMoreHolder = (LoadMoreViewHolder) convertView.getTag();
      }
    }


    return convertView;
  }

  private DownloadRequest createDownloadRequestForImage(final OthersImageViewHolder finalUserTypeOthersImageHolder, final int pos, final RCompactMessage message, final Uri destinationUri, Uri downloadUri) {

    return new DownloadRequest(downloadUri)
      .setRetryPolicy(new DefaultRetryPolicy())
      .setDestinationURI(destinationUri)
      .setPriority(DownloadRequest.Priority.HIGH)
      .setDownloadListener(new DownloadStatusListener() {
        @Override
        public void onDownloadComplete(int id) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersImageHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersImageHolder.retry.setVisibility(View.GONE);
          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              String s = destinationUri.getPath();
              message.setFilePath(Utils.formatLocalPathsProperty(s, "", ""));
              mRealm.copyToRealmOrUpdate(message);
              mMessages.set(pos, message);
              notifyDataSetChanged();
            }
          });
          finalUserTypeOthersImageHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              ArrayList<Uri> imageList = new ArrayList<Uri>();
              imageList.add(Uri.fromFile(new File(destinationUri.getPath())));
              Utils.displayImageInternalGallery(mContext, imageList, finalUserTypeOthersImageHolder.image, 0);
            }
          });
          Picasso.with(mContext).load(message.getFilePath().split(",")[0]).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
              finalUserTypeOthersImageHolder.image.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
          });
        }

        @Override
        public void onDownloadFailed(int id, int errorCode, String errorMessage) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersImageHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersImageHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersImageHolder.retry.setBackgroundResource(R.drawable.file_download);
          finalUserTypeOthersImageHolder.retry.setTag("download");
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
          float x = (downloadedBytes * 1.0F / totalBytes) * 100.0F;
          MyApplication.getInstance().updateDownloadRequestProgress(message.getMessageId(), x);
          finalUserTypeOthersImageHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersImageHolder.loading.setVisibility(View.VISIBLE);
          finalUserTypeOthersImageHolder.loading.setProgress(x);
        }
      });
  }

  private DownloadRequest createDownloadRequestForVideo(final OthersVideoViewHolder finalUserTypeOthersVideoHolder, final int pos, final RCompactMessage message, final Uri destinationUri, Uri downloadUri) {

    return new DownloadRequest(downloadUri)
      .setRetryPolicy(new DefaultRetryPolicy())
      .setDestinationURI(destinationUri)
      .setPriority(DownloadRequest.Priority.HIGH)
      .setDownloadListener(new DownloadStatusListener() {
        @Override
        public void onDownloadComplete(int id) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersVideoHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersVideoHolder.retry.setBackgroundResource(R.drawable.file_play);
          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              message.setFilePath(Utils.formatLocalPathsProperty(destinationUri.getPath(), destinationUri.getPath(), ""));
              mRealm.copyToRealmOrUpdate(message);
              mMessages.set(pos, message);
              notifyDataSetChanged();
            }
          });
          finalUserTypeOthersVideoHolder.retry.setTag("play");
        }

        @Override
        public void onDownloadFailed(int id, int errorCode, String errorMessage) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersVideoHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersVideoHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersVideoHolder.retry.setBackgroundResource(R.drawable.file_download);
          finalUserTypeOthersVideoHolder.retry.setTag("download");
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
          float x = (downloadedBytes * 1.0F / totalBytes) * 100.0F;
          MyApplication.getInstance().updateDownloadRequestProgress(message.getMessageId(), x);
          finalUserTypeOthersVideoHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersVideoHolder.loading.setVisibility(View.VISIBLE);
          finalUserTypeOthersVideoHolder.loading.setProgress(x);
        }
      });
  }

  private DownloadRequest createDownloadRequestForVideo(final SelfVideoViewHolder finalUserTypeOthersVideoHolder, final int pos, final RCompactMessage message, final Uri destinationUri, Uri downloadUri) {

    return new DownloadRequest(downloadUri)
      .setRetryPolicy(new DefaultRetryPolicy())
      .setDestinationURI(destinationUri)
      .setPriority(DownloadRequest.Priority.HIGH)
      .setDownloadListener(new DownloadStatusListener() {
        @Override
        public void onDownloadComplete(int id) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              message.setFilePath(Utils.formatLocalPathsProperty("", destinationUri.getPath(), ""));
              mRealm.copyToRealmOrUpdate(message);
              mMessages.set(pos, message);
              notifyDataSetChanged();
            }
          });

          finalUserTypeOthersVideoHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersVideoHolder.retry.setBackgroundResource(R.drawable.file_play);
          finalUserTypeOthersVideoHolder.retry.setTag("play");
        }

        @Override
        public void onDownloadFailed(int id, int errorCode, String errorMessage) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersVideoHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersVideoHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersVideoHolder.retry.setBackgroundResource(R.drawable.file_download);
          finalUserTypeOthersVideoHolder.retry.setTag("download");
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
          float x = (downloadedBytes * 1.0F / totalBytes) * 100.0F;
          MyApplication.getInstance().updateDownloadRequestProgress(message.getMessageId(), x);
          finalUserTypeOthersVideoHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersVideoHolder.loading.setVisibility(View.VISIBLE);
          finalUserTypeOthersVideoHolder.loading.setProgress(x);
        }
      });
  }

  private DownloadRequest createDownloadRequestForPdf(final OthersPdfViewHolder finalUserTypeOthersPdfHolder, final int pos, final RCompactMessage message, final Uri destinationUri, Uri downloadUri) {

    return new DownloadRequest(downloadUri)
      .setRetryPolicy(new DefaultRetryPolicy())
      .setDestinationURI(destinationUri)
      .setPriority(DownloadRequest.Priority.HIGH)
      .setDownloadListener(new DownloadStatusListener() {
        @Override
        public void onDownloadComplete(int id) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersPdfHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersPdfHolder.retry.setBackgroundResource(R.drawable.file_open);
          finalUserTypeOthersPdfHolder.retry.setTag("open");
          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              message.setFilePath(Utils.formatLocalPathsProperty("", "", destinationUri.getPath()));
              mRealm.copyToRealmOrUpdate(message);
              mMessages.set(pos, message);
              notifyDataSetChanged();
            }
          });
        }

        @Override
        public void onDownloadFailed(int id, int errorCode, String errorMessage) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersPdfHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersPdfHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersPdfHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
          finalUserTypeOthersPdfHolder.retry.setTag("download");
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
          float x = (downloadedBytes * 1.0F / totalBytes) * 100.0F;
          MyApplication.getInstance().updateDownloadRequestProgress(message.getMessageId(), x);
          finalUserTypeOthersPdfHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersPdfHolder.loading.setVisibility(View.VISIBLE);
          finalUserTypeOthersPdfHolder.loading.setProgress(x);
        }
      });
  }

  private DownloadRequest createDownloadRequestForPdf(final SelfPdfViewHolder finalUserTypeOthersPdfHolder, final int pos, final RCompactMessage message, final Uri destinationUri, Uri downloadUri) {

    return new DownloadRequest(downloadUri)
      .setRetryPolicy(new DefaultRetryPolicy())
      .setDestinationURI(destinationUri)
      .setPriority(DownloadRequest.Priority.HIGH)
      .setDownloadListener(new DownloadStatusListener() {
        @Override
        public void onDownloadComplete(int id) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersPdfHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersPdfHolder.retry.setBackgroundResource(R.drawable.file_open);
          finalUserTypeOthersPdfHolder.retry.setTag("open");
          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              message.setFilePath(Utils.formatLocalPathsProperty("", "", destinationUri.getPath()));
              mRealm.copyToRealmOrUpdate(message);
              mMessages.set(pos, message);
              notifyDataSetChanged();
            }
          });
        }

        @Override
        public void onDownloadFailed(int id, int errorCode, String errorMessage) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersPdfHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersPdfHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersPdfHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
          finalUserTypeOthersPdfHolder.retry.setTag("download");
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
          float x = (downloadedBytes * 1.0F / totalBytes) * 100.0F;
          MyApplication.getInstance().updateDownloadRequestProgress(message.getMessageId(), x);
          finalUserTypeOthersPdfHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersPdfHolder.loading.setVisibility(View.VISIBLE);
          finalUserTypeOthersPdfHolder.loading.setProgress(x);
        }
      });
  }

  @Override
  public int getViewTypeCount() {
    return 1;
  }

  public void onLoadMoreReady(boolean showLoadMore) {
    mOnLoadMoreClickListener.onLoadMoreClick();
  }

  public interface OnProfileClick {
    void onProfileClick(Profile profile);
  }

  public interface OnMessageClick {
    void onMessageLongClick(RCompactMessage message, int itemPosition);

    void onCancelClick(RCompactMessage message, int itemPosition);
  }

  private static class SelfViewHolder {
    public TextView message;
    public TextView time;
    public RelativeLayout container;
    public ImageView status;
  }

  private static class SelfImageViewHolder {
    public TextView time;
    public ImageView status;
    public ImageView image;
    public RelativeLayout container;
    public CircularProgressView loading;
    public ImageButton cancel;
  }

  private static class SelfStickerViewHolder {
    public TextView time;
    public ImageView status;
    public ImageView stickerImage;
    public RelativeLayout container;
  }

  private static class SelfVideoViewHolder {
    public TextView time;
    public ImageView status;
    public ImageView image;
    public CircularProgressView loading;
    public RelativeLayout container;
    public ImageButton retry;
  }

  private static class SelfPdfViewHolder {
    public TextView time;
    public ImageView status;
    public TextView name;
    public CircularProgressView loading;
    public RelativeLayout container;
    public ImageButton retry;
    public RelativeLayout contentContainer;
  }

  private static class SelfSharedViewHolder {
    public ImageView sharedPicture;
    public TextView sharedTitle;
    public TextView sharedDesc;
    public TextView time;
    public ImageView status;
    public RelativeLayout container;
    public RelativeLayout contentContainer;
  }

  private static class SelfLocationViewHolder {
    public ImageView locationMapView;
    public TextView time;
    public ImageView status;
    public RelativeLayout container;
  }

  private static class OtherLocationViewHolder {
    public ImageView locationMapView;
    public TextView time;
    public ImageView status;
    public RelativeLayout container;
    public ComplexAvatarView profilePicture;
  }

  private static class OthersSharedViewHolder {
    public ImageView sharedPicture;
    public ComplexAvatarView profilePicture;
    public TextView sharedTitle;
    public TextView sharedDesc;
    public TextView time;
    public RelativeLayout container;
    public RelativeLayout contentContainer;
  }

  private static class OthersViewHolder {
    public ComplexAvatarView profilePicture;
    public TextView profileName;
    public TextView message;
    public TextView time;
    public RelativeLayout container;
  }

  private static class OthersStickerViewHolder {
    public ImageView stickerImage;
    public TextView message;
    public TextView time;
    public RelativeLayout container;
  }

  private static class OthersImageViewHolder {
    public ComplexAvatarView profilePicture;
    public TextView profileName;
    public ImageView image;
    public TextView time;
    public CircularProgressView loading;
    public ImageButton retry;
    public RelativeLayout container;
  }

  private static class OthersVideoViewHolder {
    public ComplexAvatarView profilePicture;
    public TextView profileName;
    public ImageView image;
    public TextView time;
    public CircularProgressView loading;
    public ImageButton retry;
    public RelativeLayout container;
  }

  private static class OthersPdfViewHolder {
    public ComplexAvatarView profilePicture;
    public TextView profileName;
    public TextView time;
    public TextView name;
    public CircularProgressView loading;
    public ImageButton retry;
    public RelativeLayout container;
    public RelativeLayout contentContainer;
  }

  private static class SettingsViewHolder {
    public TextView text;
  }

  private static class LoadMoreViewHolder {
    public Button loadMore;
  }
}
