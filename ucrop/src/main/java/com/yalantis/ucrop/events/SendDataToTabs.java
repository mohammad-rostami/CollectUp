package com.yalantis.ucrop.events;

import com.yalantis.ucrop.model.MediaContent;

import java.util.ArrayList;

/**
 *
 */

public class SendDataToTabs {

    public final ArrayList<MediaContent> contents;
    public final int type;

    public SendDataToTabs(ArrayList<MediaContent> contents, int type) {
        this.contents = contents;
        this.type = type;
    }
}
