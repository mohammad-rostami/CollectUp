package com.collect_up.c_up.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

/**
 * Created by collect-up3 on 8/8/2016.
 */
public class StickersBottomSheetAdapter extends RecyclerView.Adapter<StickersBottomSheetAdapter.ViewHolder> {
  private List<String> stickers;
  private Context context;

  public StickersBottomSheetAdapter(Context context, List<String> stickers) {
    this.stickers = stickers;
    this.context = context;
  }

  @Override
  public StickersBottomSheetAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_stickers_icons, viewGroup, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final StickersBottomSheetAdapter.ViewHolder viewHolder, int position) {
    String item = stickers.get(position);

    ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
    imageLoader.loadImage(Constants.General.BLOB_PROTOCOL + item, new SimpleImageLoadingListener() {
      @Override
      public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        viewHolder.imgStickerIcon.setImageBitmap(loadedImage);
      }
    });

  }

  @Override
  public int getItemCount() {
    return stickers.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    private ImageView imgStickerIcon;

    public ViewHolder(View view) {
      super(view);

      imgStickerIcon = (ImageView) view.findViewById(R.id.imgStickers);
    }
  }

}