package com.yalantis.ucrop.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.R;
import com.yalantis.ucrop.callback.onFilterClickListener;
import com.yalantis.ucrop.model.Filters;
import com.yalantis.ucrop.util.RoundBitmap;

import java.util.ArrayList;

/**
 *
 */
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder>{

    private ArrayList<Filters> filters = new ArrayList<>();
    private final onFilterClickListener onFilterClickListener;

    public FilterAdapter(ArrayList<Filters> filters, com.yalantis.ucrop.callback.onFilterClickListener onFilterClickListener) {
        this.filters = filters;
        this.onFilterClickListener = onFilterClickListener;
    }

    private Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.ucrop_filter_recyclerview, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Filters filter = filters.get(position);

        ImageView imageView = holder.filterImage;
        TextView textView = holder.description;
        LinearLayout l = holder.layout;

        final Bitmap bitmap = filter.getImage().copy(Bitmap.Config.ARGB_8888, true);

        if (filter.getFilter() != null) {
            imageView.setImageBitmap(filter.getFilter().processFilter(bitmap));
        } else {
            imageView.setImageBitmap(RoundBitmap.getRoundedCornerBitmap(bitmap, 10));
        }

        textView.setText(filter.getDescription());

        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onFilterClickListener.onClick(filter.getFilter());
                    }
                });
                thread.run();
            }
        });

    }

    @Override
    public int getItemCount() {
        return filters.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView filterImage;
        TextView description;
        LinearLayout layout;

        ViewHolder(View itemView) {
            super(itemView);

            filterImage = (ImageView) itemView.findViewById(R.id.recycler_image);
            description = (TextView) itemView.findViewById(R.id.description);
            layout = (LinearLayout) itemView.findViewById(R.id.filterLayout);
        }
    }
}
