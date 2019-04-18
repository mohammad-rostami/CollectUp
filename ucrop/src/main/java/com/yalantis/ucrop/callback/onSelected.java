package com.yalantis.ucrop.callback;

import com.yalantis.ucrop.model.MediaContent;

/**
 *
 */
public interface onSelected {

    void onSelect(int position, int type);
    void onDeSelect(MediaContent mediaContent);

}
