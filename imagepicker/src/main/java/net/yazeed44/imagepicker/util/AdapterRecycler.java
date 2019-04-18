package net.yazeed44.imagepicker.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.collect_up.c_up.imagepicker.R;

import net.yazeed44.imagepicker.model.ImageEntry;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by collect-up3 on 5/16/2016.
 */
public class AdapterRecycler extends RecyclerView.Adapter<AdapterRecycler.ViewHolder> {

    private final Context _Context;
    private final ArrayList<ImageEntry> _Images;
    private final Picker _PickOptions;
    private RecyclerItemClickListener _itemListener;
    private int selectedPos = 0;

    public AdapterRecycler(Context context, ArrayList<ImageEntry> images, RecyclerItemClickListener itemListener) {
        _Context = context;
        _Images = images;
        _itemListener = itemListener;
        _PickOptions = (EventBus.getDefault().getStickyEvent(Events.OnPublishPickOptionsEvent.class)).options;


    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView image;

        public ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.imageThumb);
            image.setBackgroundResource(R.drawable.shape_border);
            int strokeWidth = _Context.getResources().getDimensionPixelSize(R.dimen.image_spacing);
            int strokeColor = _PickOptions.fabBackgroundColor;
            GradientDrawable gd = (GradientDrawable) image.getBackground();
            gd.setColor(strokeColor);
            gd.setStroke(strokeWidth, strokeColor);
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_selected},
                    gd);
            image.setBackgroundDrawable(states);

            image.setOnClickListener(this);
            image.setClickable(true);
        }

        @Override
        public void onClick(View v) {
            _itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
            setSelecton(this.getLayoutPosition());

        }
    }

    public void setSelecton(int pos) {
        notifyItemChanged(selectedPos);
        selectedPos = pos;
        notifyItemChanged(selectedPos);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_images, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(_Context)
                .load(_Images.get(position).path)
                .asBitmap()
                .into(holder.image);
        holder.image.setSelected(selectedPos == position);
     /*   if (holder.image.isSelected())
            holder.image.setBackground(ContextCompat.getDrawable(_Context, R.drawable.selector));
        else
            holder.image.setBackground(null);*/

    }


    @Override
    public int getItemCount() {
        return _Images.size();
    }
}
