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
import com.collect_up.c_up.listeners.SetProgressListener;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.EnumMessageStatus;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.model.realm.RCompactMessage;
import com.collect_up.c_up.view.LinkTransformationMethod;
import com.collect_up.c_up.view.chat.ChatLayoutImage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

public class ChatAdapter extends BaseAdapter {

  private final ListView mListView;
  private Activity mContext;
  private List<RCompactMessage> mMessages = new ArrayList<>();
  private List<RCompactMessage> queueMessages = new ArrayList<>();
  private Realm mRealm;
  private OnMessageClick mOnMessageClickListener;
  private IloadMoreClickListenr mOnLoadMoreClickListener;
  private int mImageWidth;
  private RCompactMessage mFirstMessage;
  private SetProgressListener setProgressListener;
  private static HashMap<String, SetProgressListener> listeners = new HashMap<>();
  private String mPickedLocation;

  public ChatAdapter(Activity context, List<RCompactMessage> messages, ListView listView) {
    mContext = context;
    mRealm = Realm.getInstance(SepehrUtil.getRealmConfiguration(mContext));
    mMessages = messages;
    mListView = listView;

    mImageWidth = (mContext.getResources().getDisplayMetrics().widthPixels / 4) * 3;
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
    // return !Utils.isNullOrEmpty(message.getSharedItem()) && Utils.isNullOrEmpty(message.getContentAddress());
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

  public static boolean isFile(RCompactMessage message) {
    // getSize() be onvane file name estefade mishavad.
    return message.getContentType() == EnumMessageContentType.File ? true : false;

  }

  public static boolean isLocation(RCompactMessage message) {
    return message.getContentType() == EnumMessageContentType.Location ? true : false;
  }

  public void updateStatus(final RCompactMessage compactMessage) {
    if (compactMessage != null)
    {
      for (final RCompactMessage message : mMessages)
      {
        if (message != null && message.getMessageId().equals(message.getMessageId()))
        {
          mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              message.setMessageStatus(compactMessage.getMessageStatus());
              realm.copyToRealmOrUpdate(compactMessage);
            }
          });

          notifyDataSetChanged();
          break;
        }
      }
    }
  }

  public void insertLoadMoreButtonAtTop() {
    mOnLoadMoreClickListener.onLoadMoreClick();

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

  private void setLongClick(RCompactMessage message, int position) {
    if (mOnMessageClickListener != null)
    {
      mOnMessageClickListener.onMessageLongClick(message, position);
    }
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
        realm.where(RCompactMessage.class).equalTo("MessageId", message.getMessageId()).findFirst().removeFromRealm();
      }
    });
    realm.close();

    MyApplication.mUploadingFiles.remove(message.getMessageId());
    MyApplication.getInstance().cancelUploadHandler(message.getMessageId());

    notifyDataSetChanged();
  }

  private void setClick(RCompactMessage message, int position) {
    if (mOnMessageClickListener != null)
    {
      mOnMessageClickListener.onCancelClick(message, position);
    }
  }

  public void insertItemsAtTop(List<RCompactMessage> messages) {
    if (messages.size() > 0)
    {
      if (mMessages.get(0) == null)
      {
        mMessages.remove(0);
      }
      int index = mListView.getLastVisiblePosition();
      View v = mListView.getChildAt(0);
      int top = (v == null) ? 0 : v.getTop();


      mMessages.addAll(0, messages);
      notifyDataSetChanged();

      mListView.setSelectionFromTop(index, top);


    }
  }

  public void insertItem(RCompactMessage message, boolean notify) {
    if (message.getMessageStatus() == 0)
    {
      MyApplication.mUploadingFiles.put(message.getMessageId(), 0.0F);
    }
    mMessages.add(message);
    // mMessageStatuses.add(status);
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

  public void onLoadMoreReady(boolean showLoadMore) {
    if (showLoadMore)
    {
      insertLoadMoreButtonAtTop();
    }
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

        /*if (FragmentComplex.getInstance() != null)
          FragmentComplex.getInstance().finish();
        mContext.startActivity(intent);*/
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

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    final RCompactMessage message = mMessages.get(position);
    SelfViewHolder selfHolder = null;
    SelfImageViewHolder selfImageHolder = null;
    SelfVideoViewHolder selfVideoHolder = null;
    SelfFileViewHolder selfFileHolder = null;
    SelfStickerViewHolder selfStickerHolder = null;
    SelfSharedViewHolder selfSharedHolder = null;
    SelfLocationViewHolder selfLocationHolder = null;
    OtherLocationViewHolder otherLocationHolder = null;
    OthersViewHolder othersHolder = null;
    OthersStickerViewHolder othersStickerHolder = null;
    OthersImageViewHolder othersImageHolder = null;
    OthersFileViewHolder othersFileHolder = null;
    OthersVideoViewHolder othersVideoHolder = null;
    OthersSharedViewHolder othersSharedHolder = null;
    SettingsViewHolder settingsHolder;
    LoadMoreViewHolder loadMoreHolder;

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
            || convertView.getTag() instanceof OthersFileViewHolder
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
            || convertView.getTag() instanceof SelfFileViewHolder
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
              convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_self, null, false);
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
              selfFileHolder = new SelfFileViewHolder();
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
                  setClick(message, position);
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
                  setClick(message, position);
                }
              });
              selfVideoHolder.time.setTag(message.getMessageId());
              convertView.setTag(selfVideoHolder);
            } else if (isFile(message))
            {
              selfFileHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              final SelfFileViewHolder finalSelfFileHolder1 = selfFileHolder;
              selfFileHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfFileHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfFileHolder.contentContainer = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container_content);
              selfFileHolder.contentContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfFileHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfFileHolder.name = (TextView) convertView.findViewById(R.id.text_view_name);
              selfFileHolder.name.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfFileHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfFileHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              selfFileHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfFileHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfFileHolder.status = (ImageView) convertView.findViewById(R.id.image_view_status);
              selfFileHolder.status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  if (finalSelfFileHolder1.loading.getVisibility() == View.GONE)
                  {
                    setLongClick(message, position);
                  }
                  return true;
                }
              });
              selfFileHolder.loading = (CircularProgressView) convertView.findViewById(R.id.progress_bar_loading);
              selfFileHolder.retry = (ImageButton) convertView.findViewById(R.id.image_button_retry);
              selfFileHolder.time.setTag(message.getMessageId());
              convertView.setTag(selfFileHolder);
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
                public void onClick(View v) {

                  Intent intent = new Intent(mContext, ActivityPickLocation.class);
                  intent.putExtra("return", false);
                  intent.putExtra("location", mPickedLocation);
                  mContext.startActivity(intent);
                }
              });

              selfLocationHolder.locationMapView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return false;

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
              selfFileHolder = (SelfFileViewHolder) convertView.getTag();
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
            imageLoader.loadImage(Constants.General.BLOB_PROTOCOL + message.getContentAddress(), new SimpleImageLoadingListener() {
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
              selfHolder.status.setColorFilter(Color.argb(255, 1, 87, 155));
            } else if (message.getMessageStatus() == EnumMessageStatus.Failed)
            {
              selfHolder.status.setImageResource(R.drawable.ic_failed);
              selfHolder.status.clearColorFilter();
            } else if (message.getMessageStatus() == EnumMessageStatus.Draft)
            {
            } else
            {
              selfHolder.status.setImageResource(R.drawable.ic_clock);
              selfHolder.status.setColorFilter(Color.argb(255, 1, 87, 155));
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

            } else if (message.getMessageStatus() == EnumMessageStatus.Draft)
            {
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
              final SelfImageViewHolder finalSelfImageHolder1 = selfImageHolder;
              setProgressListener = new SetProgressListener() {
                @Override
                public void onProgressListener(Float progress) {
                  finalSelfImageHolder1.loading.setProgress(progress);//MyApplication.mUploadingFiles.get(message.getId()));
                }

                @Override
                public void onProgressComplete() {

                }
              };
              listeners.put(message.getMessageId(), setProgressListener);

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
            FrameLayout.LayoutParams videoparams = (FrameLayout.LayoutParams) selfVideoHolder.image.getLayoutParams();
            videoparams.width = mImageWidth;
            videoparams.height = mImageWidth;
            selfVideoHolder.image.setLayoutParams(videoparams);


            selfVideoHolder.retry.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (v.getTag().equals("play"))
                {
                  Utils.playVideoInGallery(mContext, Uri.fromFile(videoFile));
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
            } else if (message.getMessageStatus() == EnumMessageStatus.Draft)
            {
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
                  setClick(message, position);
                }
              });
              finalUserTypeSelfVideoHolder.loading.setVisibility(View.VISIBLE);
              finalUserTypeSelfVideoHolder.loading.setProgress(MyApplication.mUploadingFiles.get(message.getMessageId()));

              setProgressListener = new SetProgressListener() {
                @Override
                public void onProgressListener(final Float progress) {

                  finalUserTypeSelfVideoHolder.loading.setProgress(progress);

                }

                @Override
                public void onProgressComplete() {

                }
              };

              listeners.put(message.getMessageId(), setProgressListener);

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
                  if (v.getTag().equals("play"))
                  {
                    Utils.playVideoInGallery(mContext, Uri.fromFile(new File(message.getFilePath().split(",")[1])));
                  } else if (v.getTag().equals("download"))
                  {
                    String[] videoStrings = message.getContentAddress().split("/");
                    Utils.checkForAppPathsExistence(mContext);
                    File downloadingMediaFile = new File(mContext.getCacheDir(), videoStrings[videoStrings.length - 1]);

                    DownloadRequest downloadRequest = createDownloadRequestForVideo(finalUserTypeSelfVideoHolder, position, message, Uri.fromFile(downloadingMediaFile), Uri.parse(message.getContentAddress()));
                    MyApplication.getInstance().addDownloadRequest(downloadRequest, message.getMessageId());
                    finalUserTypeSelfVideoHolder.loading.setVisibility(View.VISIBLE);
                    finalUserTypeSelfVideoHolder.retry.setTag("cancel");
                    finalUserTypeSelfVideoHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                  } else if (v.getTag().equals("cancel"))
                  {
                    MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
                    finalUserTypeSelfVideoHolder.loading.setVisibility(View.GONE);
                    finalUserTypeSelfVideoHolder.retry.setTag("download");
                    finalUserTypeSelfVideoHolder.retry.setBackgroundResource(R.drawable.file_download);
                  }
                }
              });
            }
          } else if (isFile(message))
          {
            RelativeLayout.LayoutParams fileParams = (RelativeLayout.LayoutParams) selfFileHolder.contentContainer.getLayoutParams();
            fileParams.width = mImageWidth;
            selfFileHolder.contentContainer.setLayoutParams(fileParams);

            final File selfFile = message.getFilePath() != null ? new File(message.getFilePath().split(",")[2]) : null;
            final SelfFileViewHolder finalFileHolder = selfFileHolder;
            selfFileHolder.retry.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (v.getTag().equals("open"))
                {
                  Utils.displayFile(mContext, Uri.fromFile(selfFile));
                } else if (v.getTag().equals("download"))
                {
                  String[] strings = message.getContentAddress().split("/");
                  Utils.checkForAppPathsExistence(mContext);
                  final Uri FileDestinationUri = Uri.parse(Constants.General.APP_FOLDER_FILE_PATH + strings[strings.length - 1]);
                  finalFileHolder.loading.setVisibility(View.VISIBLE);
                  finalFileHolder.retry.setTag("cancel");
                  finalFileHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                  DownloadRequest downloadRequest = createDownloadRequestForFile(finalFileHolder, position, message, FileDestinationUri, Uri.parse(message.getContentAddress()));
                  MyApplication.getInstance().addDownloadRequest(downloadRequest, message.getMessageId());
                } else if (v.getTag().equals("cancel"))
                {
                  MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
                  finalFileHolder.loading.setVisibility(View.GONE);
                  finalFileHolder.retry.setTag("download");
                  finalFileHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
                }
              }
            });

            selfFileHolder.name.setText(message.getContentSize());
            selfFileHolder.time.setText(time);

            if (message.getMessageStatus() == EnumMessageStatus.Sent)
            {
              selfFileHolder.status.setImageResource(R.drawable.ic_single_tick);
              selfFileHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));
            } else if (message.getMessageStatus() == EnumMessageStatus.Delivered)
            {
              selfFileHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color gray
              selfFileHolder.status.setColorFilter(Color.argb(127, 0, 0, 0));

            } else if (message.getMessageStatus() == EnumMessageStatus.Seen)
            {
              selfFileHolder.status.setImageResource(R.drawable.ic_double_tick);
              // Color accent
              selfFileHolder.status.setColorFilter(Color.argb(255, 3, 169, 244));
            } else if (message.getMessageStatus() == EnumMessageStatus.Failed)
            {
              selfFileHolder.status.setImageResource(R.drawable.ic_failed);
              selfFileHolder.status.clearColorFilter();
            } else if (message.getMessageStatus() == EnumMessageStatus.Draft)
            {
            } else
            {
              selfFileHolder.status.setImageResource(R.drawable.ic_clock_white);
              selfFileHolder.status.setColorFilter(Color.argb(255, 3, 169, 244));
            }


            if (MyApplication.mUploadingFiles.containsKey(message.getMessageId()))
            {
              selfFileHolder.retry.setVisibility(View.VISIBLE);
              selfFileHolder.retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  setClick(message, position);
                }
              });
              selfFileHolder.loading.setVisibility(View.VISIBLE);
//                                selfFileHolder.loading.setProgress(MyApplication.mUploadingFiles.get(message.getId()));
              selfFileHolder.loading.setVisibility(View.VISIBLE);

              final SelfFileViewHolder finalSelfFileHolder2 = selfFileHolder;
              setProgressListener = new SetProgressListener() {
                @Override
                public void onProgressListener(Float progress) {
                  finalSelfFileHolder2.loading.setProgress(progress);//MyApplication.mUploadingFiles.get(message.getId()));

                }

                @Override
                public void onProgressComplete() {

                }
              };
              listeners.put(message.getMessageId(), setProgressListener);

            } else
            {
              selfFileHolder.loading.setVisibility(View.GONE);
              if (selfFile != null && selfFile.exists())
              {
                selfFileHolder.retry.setBackgroundResource(R.drawable.file_open);
                selfFileHolder.retry.setTag("open");
              } else
              {
                selfFileHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
                selfFileHolder.retry.setTag("download");
              }
            }
          } else if (message.getContentType() == EnumMessageContentType.SharedBusiness)
          {

            Shop shop = new Gson().fromJson(message.getContentSize(), Shop.class);
            setSharedShop(selfSharedHolder, message, shop);
            ChatLayoutImage.LayoutParams sharedParams = (ChatLayoutImage.LayoutParams) selfSharedHolder.contentContainer.getLayoutParams();
            sharedParams.width = mImageWidth;
            selfSharedHolder.contentContainer.setLayoutParams(sharedParams);
          } else if (message.getContentType() == EnumMessageContentType.SharedComplex)
          {
            Complex complex = new Gson().fromJson(message.getContentSize(), Complex.class);
            setSharedComplex(selfSharedHolder, message, complex);
            selfSharedHolder = (SelfSharedViewHolder) convertView.getTag();
            ChatLayoutImage.LayoutParams sharedComplexParams = (ChatLayoutImage.LayoutParams) selfSharedHolder.contentContainer.getLayoutParams();
            sharedComplexParams.width = mImageWidth;
            selfSharedHolder.contentContainer.setLayoutParams(sharedComplexParams);
          } else if (message.getContentType() == EnumMessageContentType.SharedEvent)
          {
          } else if (message.getContentType() == EnumMessageContentType.SharedPost)
          {
            Post Post = new Gson().fromJson(message.getContentSize(), Post.class);
            setSharedPostTimeline(selfSharedHolder, message, Post);
            selfSharedHolder = (SelfSharedViewHolder) convertView.getTag();
            ChatLayoutImage.LayoutParams sharedPostParams = (ChatLayoutImage.LayoutParams) selfSharedHolder.contentContainer.getLayoutParams();
            sharedPostParams.width = mImageWidth;
            selfSharedHolder.contentContainer.setLayoutParams(sharedPostParams);
          } else if (message.getContentType() == EnumMessageContentType.SharedProduct)
          {
            Product product = new Gson().fromJson(message.getContentSize(), Product.class);
            setSharedProduct(selfSharedHolder, message, product);
            selfSharedHolder = (SelfSharedViewHolder) convertView.getTag();
            ChatLayoutImage.LayoutParams sharedProductParams = (ChatLayoutImage.LayoutParams) selfSharedHolder.contentContainer.getLayoutParams();
            sharedProductParams.width = mImageWidth;
            selfSharedHolder.contentContainer.setLayoutParams(sharedProductParams);
          } else if (message.getContentType() == EnumMessageContentType.SharedProfile)
          {
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
            } else if (message.getMessageStatus() == EnumMessageStatus.Draft)
            {
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
            || convertView.getTag() instanceof OthersFileViewHolder
            || convertView.getTag() instanceof SelfFileViewHolder
            || convertView.getTag() instanceof SelfViewHolder
            || convertView.getTag() instanceof SelfVideoViewHolder
            || convertView.getTag() instanceof SelfImageViewHolder
            || convertView.getTag() instanceof SettingsViewHolder
            || convertView.getTag() instanceof SelfLocationViewHolder
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
                .inflate(R.layout.item_chat_others, null, false);
              othersHolder = new OthersViewHolder();
            } else if (isImage(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_chat_others_image, null, false);
              othersImageHolder = new OthersImageViewHolder();
            } else if (isVideo((message)))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_chat_others_video, null, false);
              othersVideoHolder = new OthersVideoViewHolder();
            } else if (isFile(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_chat_others_file, null, false);
              othersFileHolder = new OthersFileViewHolder();
            } else if (isSharedItem(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_chat_others_share_item, null, false);
              othersSharedHolder = new OthersSharedViewHolder();
            } else if (isLocation(message))
            {
              convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_chat_other_location, null, false);
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
              othersVideoHolder.time.setTag(message.getMessageId());

              convertView.setTag(othersVideoHolder);
            } else if (isFile(message))
            {
              othersFileHolder.container = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container);
              othersFileHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersFileHolder.contentContainer = (RelativeLayout) convertView.findViewById(R.id.relative_layout_container_content);
              othersFileHolder.contentContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersFileHolder.name = (TextView) convertView.findViewById(R.id.text_view_name);
              othersFileHolder.name.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersFileHolder.loading = (CircularProgressView) convertView.findViewById(R.id.progress_bar_loading);
              othersFileHolder.loading.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersFileHolder.time = (TextView) convertView.findViewById(R.id.text_view_time);
              othersFileHolder.time.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersFileHolder.retry = (ImageButton) convertView.findViewById(R.id.image_button_retry);
              othersFileHolder.retry.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return true;
                }
              });
              othersFileHolder.time.setTag(message.getMessageId());

              convertView.setTag(othersFileHolder);
            }
            if (isSharedItem(message))
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
              otherLocationHolder.locationMapView = (ImageView) convertView.findViewById(R.id.map_view);
              otherLocationHolder.locationMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                  Intent intent = new Intent(mContext, ActivityPickLocation.class);
                  intent.putExtra("return", false);
                  intent.putExtra("location", mPickedLocation);
                  mContext.startActivity(intent);
                }
              });

              otherLocationHolder.locationMapView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                  setLongClick(message, position);
                  return false;

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
              othersFileHolder = (OthersFileViewHolder) convertView.getTag();
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
            imageLoader.loadImage(Constants.General.BLOB_PROTOCOL + message.getContentAddress(), new SimpleImageLoadingListener() {
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
          } else if (isImage(message))
          {
            othersImageHolder.image.setImageURI(null);
            othersImageHolder.image.setImageDrawable(null);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) othersImageHolder.image.getLayoutParams();
            params.height = mImageWidth;
            params.width = mImageWidth;
            othersImageHolder.image.setLayoutParams(params);
            final OthersImageViewHolder finalUserTypeOthersImageHolder = othersImageHolder;

            String localImagePath = message.getFilePath() != null ? message.getFilePath().split(",")[0] : null;
            final File file = localImagePath != null ? new File(localImagePath) : null;
            if (file == null || !file.exists())
            {
              final Uri downloadUri = Uri.parse(message.getContentAddress());
              String[] strings = message.getContentAddress().split("/");
              Utils.checkForAppPathsExistence(mContext);
              File downloadingMediaFile = new File(mContext.getCacheDir(), strings[strings.length - 1]);
              final Uri destinationUri = Uri.fromFile(downloadingMediaFile);

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
                if (file == null || !file.exists())
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
                  imageList.add(Uri.fromFile(file));
                  Utils.displayImageInternalGallery(mContext, imageList, finalUserTypeOthersImageHolder.image, 0);

                }
              });
              Picasso.with(mContext).load(Uri.fromFile(file)).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(finalUserTypeOthersImageHolder.image);
            }

            othersImageHolder.time.setText(time);
          } else if (isVideo(message))
          {
            othersVideoHolder.image.setImageURI(null);
            othersVideoHolder.image.setImageDrawable(null);
            FrameLayout.LayoutParams othersVideParams = (FrameLayout.LayoutParams) othersVideoHolder.image.getLayoutParams();
            othersVideParams.height = mImageWidth;
            othersVideParams.width = mImageWidth;
            othersVideoHolder.image.setLayoutParams(othersVideParams);
            final OthersVideoViewHolder finalUserTypeOthersVideoHolder = othersVideoHolder;

            Picasso.with(mContext).load(message.getThumbnailAddress()).noFade().noPlaceholder().priority(Picasso.Priority.HIGH).into(othersVideoHolder.image);

            final Uri downloadVideoUri = Uri.parse(message.getContentAddress());
            String[] videoStrings = message.getContentAddress().split("/");
            Utils.checkForAppPathsExistence(mContext);
            File downloadingMediaFile = new File(mContext.getCacheDir(), videoStrings[videoStrings.length - 1]);
            final Uri videoDestinationUri = Uri.fromFile(downloadingMediaFile);

//                        final Uri videoDestinationUri = Uri.parse(Constants.General.APP_FOLDER_VIDEO_PATH + videoStrings[videoStrings.length - 1]);

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
              if (message.getFilePath() != null && (message.getFilePath().split(",").length >= 2) && new File(message.getFilePath().split(",")[1]).exists())
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
            RelativeLayout.LayoutParams othersFileparams = (RelativeLayout.LayoutParams) othersFileHolder.contentContainer.getLayoutParams();
            othersFileparams.width = mImageWidth;
            othersFileHolder.contentContainer.setLayoutParams(othersFileparams);

            othersFileHolder.name.setText(message.getContentSize());
            final OthersFileViewHolder finalUserTypeOthersFileHolder = othersFileHolder;

            final File localFile = message.getFilePath() != null ? new File(message.getFilePath().split(",")[2]) : null;

            final Uri downloadFileUri = Uri.parse(message.getContentAddress());
            String[] strings = message.getContentAddress().split("/");
            Utils.checkForAppPathsExistence(mContext);
            final Uri FileDestinationUri = Uri.parse(Constants.General.APP_FOLDER_FILE_PATH + strings[strings.length - 1]);

            othersFileHolder.retry.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (v.getTag().equals("download"))
                {
                  DownloadRequest request = createDownloadRequestForFile(finalUserTypeOthersFileHolder, position, message, FileDestinationUri, downloadFileUri);
                  MyApplication.getInstance().addDownloadRequest(request, message.getMessageId());
                  finalUserTypeOthersFileHolder.loading.setVisibility(View.VISIBLE);
                  finalUserTypeOthersFileHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
                  finalUserTypeOthersFileHolder.retry.setTag("cancel");
                } else if (v.getTag().equals("cancel"))
                {
                  MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
                  finalUserTypeOthersFileHolder.loading.setVisibility(View.GONE);
                  finalUserTypeOthersFileHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
                  finalUserTypeOthersFileHolder.retry.setTag("download");
                } else if (v.getTag().equals("open"))
                {
                  Utils.displayFile(mContext, Uri.fromFile(new File(FileDestinationUri.getPath())));
                }
              }
            });

            MyApplication.DownloadRequestStruct downloadRequest = MyApplication.getInstance().hasRequestDownload(message.getMessageId());

            if (downloadRequest != null)
            {
              othersFileHolder.loading.setVisibility(View.VISIBLE);
              othersFileHolder.retry.setVisibility(View.VISIBLE);
              othersFileHolder.retry.setTag("cancel");
              othersFileHolder.retry.setBackgroundResource(R.drawable.file_download_cancel);
              othersFileHolder.loading.setProgress(downloadRequest.progress);
            } else
            {
              othersFileHolder.loading.setVisibility(View.GONE);
              othersFileHolder.retry.setVisibility(View.VISIBLE);
              if (localFile == null || !localFile.exists())
              {
                othersFileHolder.retry.setTag("download");
                othersFileHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
              } else
              {
                othersFileHolder.retry.setTag("open");
                othersFileHolder.retry.setBackgroundResource(R.drawable.file_open);
              }
            }

            othersFileHolder.time.setText(time);
          } else if (message.getContentType() == EnumMessageContentType.SharedBusiness)
          {
            Shop shop = new Gson().fromJson(message.getContentSize(), Shop.class);
            setSharedShop(othersSharedHolder, message, shop);
            othersSharedHolder = (OthersSharedViewHolder) convertView.getTag();
            RelativeLayout.LayoutParams othersSharedBusParams = (RelativeLayout.LayoutParams) othersSharedHolder.contentContainer.getLayoutParams();
            othersSharedBusParams.width = mImageWidth;
            othersSharedHolder.contentContainer.setLayoutParams(othersSharedBusParams);
          } else if (message.getContentType() == EnumMessageContentType.SharedComplex)
          {
            Complex complex = new Gson().fromJson(message.getContentSize(), Complex.class);
            setSharedComplex(othersSharedHolder, message, complex);
            othersSharedHolder = (OthersSharedViewHolder) convertView.getTag();
            RelativeLayout.LayoutParams othersSharedcompParams = (RelativeLayout.LayoutParams) othersSharedHolder.contentContainer.getLayoutParams();
            othersSharedcompParams.width = mImageWidth;
            othersSharedHolder.contentContainer.setLayoutParams(othersSharedcompParams);
          } else if (message.getContentType() == EnumMessageContentType.SharedEvent)
          {
          } else if (message.getContentType() == EnumMessageContentType.SharedPost)
          {
            Post Post = new Gson().fromJson(message.getContentSize(), Post.class);
            setSharedPostTimeline(othersSharedHolder, message, Post);
            othersSharedHolder = (OthersSharedViewHolder) convertView.getTag();
            RelativeLayout.LayoutParams othersSharedPostParams = (RelativeLayout.LayoutParams) othersSharedHolder.contentContainer.getLayoutParams();
            othersSharedPostParams.width = mImageWidth;
            othersSharedHolder.contentContainer.setLayoutParams(othersSharedPostParams);
          } else if (message.getContentType() == EnumMessageContentType.SharedProduct)
          {
            Product product = new Gson().fromJson(message.getContentSize(), Product.class);
            setSharedProduct(othersSharedHolder, message, product);
            othersSharedHolder = (OthersSharedViewHolder) convertView.getTag();
            RelativeLayout.LayoutParams othersSharedProductParams = (RelativeLayout.LayoutParams) othersSharedHolder.contentContainer.getLayoutParams();
            othersSharedProductParams.width = mImageWidth;
            othersSharedHolder.contentContainer.setLayoutParams(othersSharedProductParams);
          } else if (message.getContentType() == EnumMessageContentType.SharedProfile)
          {
          } else if (isLocation(message))
          {
            assert otherLocationHolder != null;
            mPickedLocation = message.getText();
            Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(mPickedLocation);
            String[] latLngString = new String[2];
            while (m.find())
            {
              latLngString = m.group(1).split(",");
            }
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
          || convertView.getTag() instanceof OthersFileViewHolder
          || convertView.getTag() instanceof SelfFileViewHolder
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
        || convertView.getTag() instanceof OthersFileViewHolder
        || convertView.getTag() instanceof SelfFileViewHolder
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
            notifyDataSetChanged();
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
              realm.copyToRealmOrUpdate(message);
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

  private DownloadRequest createDownloadRequestForFile(final OthersFileViewHolder finalUserTypeOthersFileHolder, final int pos, final RCompactMessage message, final Uri destinationUri, Uri downloadUri) {

    return new DownloadRequest(downloadUri)
      .setRetryPolicy(new DefaultRetryPolicy())
      .setDestinationURI(destinationUri)
      .setPriority(DownloadRequest.Priority.HIGH)
      .setDownloadListener(new DownloadStatusListener() {
        @Override
        public void onDownloadComplete(int id) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersFileHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersFileHolder.retry.setBackgroundResource(R.drawable.file_open);
          finalUserTypeOthersFileHolder.retry.setTag("open");
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
          finalUserTypeOthersFileHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersFileHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersFileHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
          finalUserTypeOthersFileHolder.retry.setTag("download");
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
          float x = (downloadedBytes * 1.0F / totalBytes) * 100.0F;
          MyApplication.getInstance().updateDownloadRequestProgress(message.getMessageId(), x);
          finalUserTypeOthersFileHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersFileHolder.loading.setVisibility(View.VISIBLE);
          finalUserTypeOthersFileHolder.loading.setProgress(x);
        }
      });
  }

  private DownloadRequest createDownloadRequestForFile(final SelfFileViewHolder finalUserTypeOthersFileHolder, final int pos, final RCompactMessage message, final Uri destinationUri, Uri downloadUri) {

    return new DownloadRequest(downloadUri)
      .setRetryPolicy(new DefaultRetryPolicy())
      .setDestinationURI(destinationUri)
      .setPriority(DownloadRequest.Priority.HIGH)
      .setDownloadListener(new DownloadStatusListener() {
        @Override
        public void onDownloadComplete(int id) {
          MyApplication.getInstance().cancelDownloadRequest(message.getMessageId());
          finalUserTypeOthersFileHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersFileHolder.retry.setBackgroundResource(R.drawable.file_open);
          finalUserTypeOthersFileHolder.retry.setTag("open");
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
          finalUserTypeOthersFileHolder.loading.setVisibility(View.GONE);
          finalUserTypeOthersFileHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersFileHolder.retry.setBackgroundResource(R.drawable.file_download_colorful_file);
          finalUserTypeOthersFileHolder.retry.setTag("download");
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
          float x = (downloadedBytes * 1.0F / totalBytes) * 100.0F;
          MyApplication.getInstance().updateDownloadRequestProgress(message.getMessageId(), x);
          finalUserTypeOthersFileHolder.retry.setVisibility(View.VISIBLE);
          finalUserTypeOthersFileHolder.loading.setVisibility(View.VISIBLE);
          finalUserTypeOthersFileHolder.loading.setProgress(x);
        }
      });
  }

  @Override
  public int getViewTypeCount() {
    return 1;
  }

  public HashMap<String, SetProgressListener> getListener() {
    return listeners;
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

  private static class SelfFileViewHolder {
    public TextView time;
    public ImageView status;
    public TextView name;
    public CircularProgressView loading;
    public RelativeLayout container;
    public ImageButton retry;
    public RelativeLayout contentContainer;
  }

  private static class LoadMoreViewHolder {
    public Button loadMore;
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
  }

  private static class OthersSharedViewHolder {
    public ImageView sharedPicture;
    public TextView sharedTitle;
    public TextView sharedDesc;
    public TextView time;
    public RelativeLayout container;
    public RelativeLayout contentContainer;
  }

  private static class OthersViewHolder {
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
    public ImageView image;
    public TextView time;
    public CircularProgressView loading;
    public ImageButton retry;
    public RelativeLayout container;
  }

  private static class OthersVideoViewHolder {
    public ImageView image;
    public TextView time;
    public CircularProgressView loading;
    public ImageButton retry;
    public RelativeLayout container;
  }

  private static class OthersFileViewHolder {
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
}
