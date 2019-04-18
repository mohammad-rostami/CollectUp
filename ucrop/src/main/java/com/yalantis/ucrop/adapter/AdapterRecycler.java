package com.yalantis.ucrop.adapter;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.R;
import com.yalantis.ucrop.callback.RecyclerItemClickListener;
import com.yalantis.ucrop.events.Events;
import com.yalantis.ucrop.util.ImageEntry;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class AdapterRecycler extends RecyclerView.Adapter<AdapterRecycler.ViewHolder> {

    private final Context _Context;
    private final ArrayList<ImageEntry> _Images;
/*
*/
    private RecyclerItemClickListener _itemListener;
    private int selectedPos = 0;

    static int positions = -1;

    public AdapterRecycler(Context context, ArrayList<ImageEntry> images, RecyclerItemClickListener itemListener) {
        _Context = context;
        _Images = images;
        _itemListener = itemListener;
/*
        _PickOptions = (EventBus.getDefault().getStickyEvent(Events.OnPublishPickOptionsEvent.class)).options;
*/


    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView image;
        public TextView time;

        ViewHolder(View view) {
            super(view);
            time = (TextView) view.findViewById(R.id.time_preview);
            image = (ImageView) view.findViewById(R.id.imageThumb);
            image.setBackgroundResource(R.drawable.shape_border);
            int strokeWidth = _Context.getResources().getDimensionPixelSize(R.dimen.image_spacing);
/*
            int strokeColor = _PickOptions.fabBackgroundColor;
*/
            /*GradientDrawable gd = (GradientDrawable) image.getBackground();
            gd.setColor(strokeColor);
            gd.setStroke(strokeWidth, strokeColor);*/
           /* StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_selected},
                    gd);
            image.setBackgroundDrawable(states);*/

            image.setOnClickListener(this);
            image.setClickable(true);

            time.setVisibility(View.GONE);

        }

        void bind (ImageEntry imageEntry , int position) {

            Glide.with(_Context)
                    .load(imageEntry.path)
                    .asBitmap()
                    .into(image);
            image.setSelected(selectedPos == position);

            if (_Images.get(position).isVideo) {
                time.setText(getTime(getMediaTime(Uri.parse(_Images.get(position).path))));
                time.setVisibility(View.VISIBLE);
            } else {
                time.setVisibility(View.GONE);
            }

        }

        @Override
        public void onClick(View v) {
            _itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
            setSelecton(this.getLayoutPosition());

            if (_Images.get(this.getLayoutPosition()).isVideo) {
                EventBus.getDefault().post(new Events.OnMenuChange(true));
                EventBus.getDefault().post(new Events.StopOnBack());
            } else {
                EventBus.getDefault().post(new Events.OnMenuChange(false));
            }

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
                .inflate(R.layout.ucrop_list_item_images, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        positions = position;

       holder.bind(_Images.get(position), position);
     /*   if (holder.image.isSelected())
            holder.image.setBackground(ContextCompat.getDrawable(_Context, R.drawable.selector));
        else
            holder.image.setBackground(null);*/

    }


    @Override
    public int getItemCount() {
        if (_Images != null) {
            return _Images.size();
        } else {
            return 0;
        }

    }

    private String getTime(long milliseconds) {

        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));

        if (minutes > 9 && seconds > 9) {
            return String.format(Locale.getDefault(), " %d:%d",
                    minutes,seconds);
        } else if (minutes > 9 && seconds <= 9) {
            return String.format(Locale.getDefault(), " %d:0%d",
                    minutes,seconds);
        } else if (minutes <= 9 && seconds > 9) {
            return String.format(Locale.getDefault(), " 0%d:%d",
                    minutes,seconds);
        } else {
            return String.format(Locale.getDefault(), " 0%d:0%d",
                    minutes,seconds);
        }
    }

    private long getMediaTime (Uri uri) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(_Context, uri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Long.parseLong(time);

    }
}
