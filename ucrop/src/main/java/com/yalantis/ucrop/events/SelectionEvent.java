package com.yalantis.ucrop.events;

import com.yalantis.ucrop.model.MediaContent;

/**
 *
 */

public class SelectionEvent {

    public final MediaContent content;

    public SelectionEvent(MediaContent content) {
        this.content = content;
    }
}
