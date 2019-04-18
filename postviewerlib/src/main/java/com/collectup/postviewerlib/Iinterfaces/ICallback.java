package com.collectup.postviewerlib.Iinterfaces;

import android.animation.Animator;
import android.widget.VideoView;

import com.collectup.postviewerlib.ImageVolleyView;

/**
 * Created by collect-up3 on 4/28/2016.
 */
public interface ICallback {
    void onImageRemove(Animator var1);

    void onImageCreate(ImageVolleyView var1);

    void onVideoCreate(VideoView var1);
}
