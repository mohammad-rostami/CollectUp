package com.collect_up.c_up.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.model.StickerPackage;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by collect-up3 on 8/7/2016.
 */
public class StickersAdapter extends UltimateViewAdapter<StickersAdapter.Holder> implements View.OnClickListener {

  private final List<StickerPackage> stickerPackages;
  private final Context context;
  private ItemListener mListener;

  public StickersAdapter setListener(ItemListener listener) {
    mListener = listener;
    return this;
  }

  public StickersAdapter(Context context, List<StickerPackage> packages)

  {
    this.stickerPackages = packages;
    this.context = context;
  }


  @Override
  public void onBindViewHolder(final Holder holder, int position) {
    if (holder.imgStickers == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }
    StickerPackage item = stickerPackages.get(position);
    holder.stickerLayout.setTag(item);
    holder.stickerLayout.setOnClickListener(this);
    holder.txtStickerName.setText(item.getPackageName());
    holder.txtStickerCount.setText(item.getCount() + " stickers");

    ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
    imageLoader.loadImage(Constants.General.BLOB_PROTOCOL + item.getThumbnailAddress(), new SimpleImageLoadingListener() {
      @Override
      public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        holder.imgStickers.setImageBitmap(loadedImage);
      }
    });


  }

  public List<StickerPackage> getStickerPackages() {
    return this.stickerPackages;
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }


  @Override
  public StickersAdapter.Holder getViewHolder(View view) {
    return new Holder(view, false);
  }


  @Override
  public StickersAdapter.Holder onCreateViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stickers, parent, false);
    Holder holder = new Holder(view, true);
    return holder;
  }

  @Override
  public void onClick(View v) {
    StickerPackage stickerPackage = (StickerPackage) v.getTag();
    if (mListener != null)
    {
      mListener.onStickerClick(stickerPackage);
    }
  }

  public interface ItemListener {
    void onStickerClick(StickerPackage stickerPackage);
  }

  @Override
  public int getAdapterItemCount() {
    return stickerPackages.size();
  }

  @Override
  public long generateHeaderId(int position) {
    return 0;
  }

  @Override
  public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
    return null;

  }

  @Override
  public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

  }

  static class Holder extends UltimateRecyclerviewViewHolder {
    @Bind (R.id.txtStickerCount)
    TextView txtStickerCount;
    @Bind (R.id.txtStickerName)
    TextView txtStickerName;
    @Bind (R.id.imgStickers)
    ImageView imgStickers;
    @Bind (R.id.stickerLayout)
    LinearLayout stickerLayout;

    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }
}
