/*
 * Copyright 2016 Alireza Eskandarpour Shoferi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.meness.timelinepostcontainer;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnimRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.todddavies.components.progressbar.ProgressWheel;
import com.wang.avi.AVLoadingIndicatorView;

public class Options {
    private final Context mContext;
    private final ViewGroup mViewGroup;
    public boolean looping;
    public boolean keepScreenOnWhilePlaying;
    public boolean debug;
    public int playDrawable;
    public int pauseDrawable;
    public Animation drawablesAnimation;
    public AVLoadingIndicatorView videoLoadingView;
    public ImageLoader imageLoader;
    public ProgressWheel imageLoadingView;

    public Options(Context context, ViewGroup viewGroup) {
        mContext = context;
        mViewGroup = viewGroup;

        // initialise
        looping = true;
        keepScreenOnWhilePlaying = true;
        // imageLoader = InitClass.imageLoader(context);
        setPlayDrawable(R.drawable.ic_play_circle_filled_black_24dp);
        setPauseDrawable(R.drawable.ic_pause_circle_filled_black_24dp);
        setDrawablesAnimation(R.anim.foreground);
        setVideoLoadingView(R.layout.video_loading);
        setImageLoadingView(R.layout.image_loading);
    }

    public final Options setVideoLoadingView(@LayoutRes int videoLoadingLayout) {
        View view = LayoutInflater.from(mContext).inflate(videoLoadingLayout, mViewGroup, false);
        if (AndroidUtils.isInstanceOf(view, AVLoadingIndicatorView.class, mContext.getResources())) {
            videoLoadingView = (AVLoadingIndicatorView) view;
        }
        return this;
    }

   /* public Options setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }*/

    public final Options setDrawablesAnimation(@AnimRes int res) {
        drawablesAnimation = AnimationUtils.loadAnimation(mContext, res);
        return this;
    }

    public final Options setPauseDrawable(@DrawableRes int res) {
        pauseDrawable = R.drawable.ic_pause_circle_filled_black_48px;//AndroidUtils.getDrawable(mContext, res);
        return this;
    }

    public final Options setPlayDrawable(@DrawableRes int res) {
        playDrawable = R.drawable.ic_play_circle_filled_black_48px;//AndroidUtils.getDrawable(mContext, res);
        return this;
    }

    public final Options setImageLoadingView(@LayoutRes int layout) {
        View view = LayoutInflater.from(mContext).inflate(layout, mViewGroup, false);
        if (AndroidUtils.isInstanceOf(view, ProgressWheel.class, mContext.getResources())) {
            imageLoadingView = (ProgressWheel) view;
        }

        return this;
    }
}
