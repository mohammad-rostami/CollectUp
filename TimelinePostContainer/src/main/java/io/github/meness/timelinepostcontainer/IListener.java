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

import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.View;

import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

/**
 * Private listeners used only by {@link TimelinePostContainer}
 */
interface IListener extends ImageLoadingProgressListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, ImageLoadingListener {
    boolean onImageTouch(View v, MotionEvent event);

    void prepareVideo(View v);

    void onVideoTouch(View v, MotionEvent event);
}
