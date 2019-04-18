package com.collect_up.c_up.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.StickerPackage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by collect-up3 on 8/7/2016.
 */
public class SuggestionProductAdapter extends RecyclerView.Adapter<SuggestionProductAdapter.Holder> implements View.OnClickListener {

  private final Product[] products;
  private final Context context;
  private ItemListener mListener;

  public SuggestionProductAdapter setListener(ItemListener listener) {
    mListener = listener;
    return this;
  }

  public SuggestionProductAdapter(Context context, Product[] products)

  {
    this.products = products;
    this.context = context;
  }


  @Override
  public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_suggested, parent, false);
    Holder holder = new Holder(view, true);
    return holder;
  }

  @Override
  public void onBindViewHolder(final Holder holder, int position) {
    if (holder.imgThumb == null)
    {
      RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      holder.itemView.setLayoutParams(params1);

      return;
    }
    Product item = products[position];
    holder.cardView.setTag(item);
    holder.cardView.setOnClickListener(this);
    holder.txtTitle.setText(item.getName());
    ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
    imageLoader.loadImage(Constants.General.BLOB_PROTOCOL + item.getThumb(), new SimpleImageLoadingListener() {
      @Override
      public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        holder.imgThumb.setImageBitmap(loadedImage);
      }
    });


  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public int getItemCount() {
    return products.length;
  }


  @Override
  public void onClick(View v) {
    Product product = (Product) v.getTag();
    FragmentHandler.replaceFragment(context, fragmentType.PRODUCT, product);

  }

  public interface ItemListener {
    void onStickerClick(StickerPackage stickerPackage);
  }


  static class Holder extends RecyclerView.ViewHolder {
    @Bind (R.id.txtTitle)
    TextView txtTitle;
    @Bind (R.id.imgThumb)
    ImageView imgThumb;
    @Bind (R.id.card_view)
    CardView cardView;

    public Holder(View itemView, boolean isItem) {
      super(itemView);
      if (isItem)
      {
        ButterKnife.bind(this, itemView);
      }
    }
  }
}
