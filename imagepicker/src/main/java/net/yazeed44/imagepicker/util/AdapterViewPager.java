package net.yazeed44.imagepicker.util;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.yazeed44.imagepicker.model.ImageEntry;

import java.util.ArrayList;

/**
 * Created by collect-up3 on 5/16/2016.
 */
public class AdapterViewPager extends PagerAdapter {

    ArrayList<ImageEntry> _Images;
    Context _Context;

    public AdapterViewPager(Context context, ArrayList<ImageEntry> images) {
        _Images = images;
        _Context = context;
    }

    @Override

    public int getCount() {
        return _Images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(_Context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Glide.with(_Context)
                .load(_Images.get(position).path)
                .asBitmap()
                .into(imageView);
        ((ViewPager) container).addView(imageView, 0);
        return imageView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);

    }
}
