package com.yalantis.ucrop.model;

import android.graphics.Bitmap;

import com.zomato.photofilters.imageprocessors.Filter;

/**
 * the model of each filter
 */
public class Filters {

    private Bitmap image;
    private String Description;
    private Filter filter;

    public Filters(Bitmap image, String description, Filter filter) {
        this.image = image;
        Description = description;
        this.filter = filter;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getDescription() {
        return Description;
    }

    public Filter getFilter() {
        return filter;
    }
}
