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

import android.support.v7.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class RecyclerPauseOnScrollListener extends RecyclerView.OnScrollListener {
    private final boolean pauseOnScroll;
    private final boolean pauseOnSettling;
    private final RecyclerView.OnScrollListener externalListener;
    private final ImageLoader imageLoader;
    private boolean stopped;

    public RecyclerPauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnSettling) {
        this(imageLoader, pauseOnScroll, pauseOnSettling, null);
    }

    public RecyclerPauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnSettling,
                                         RecyclerView.OnScrollListener customListener) {
        this.imageLoader = imageLoader;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnSettling = pauseOnSettling;
        externalListener = customListener;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                imageLoader.resume();
                stopped = false;
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if (pauseOnScroll) {
                    imageLoader.pause();
                    stopped = true;
                } else if (stopped) {
                    imageLoader.resume();
                    stopped = false;
                }
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                if (pauseOnSettling) {
                    imageLoader.pause();
                    stopped = true;
                } else if (stopped) {
                    imageLoader.resume();
                    stopped = false;
                }
                break;
            default:
                // empty, intentional
                break;
        }
        if (externalListener != null) {
            externalListener.onScrollStateChanged(recyclerView, newState);
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (externalListener != null) {
            externalListener.onScrolled(recyclerView, dx, dy);
        }
    }
}