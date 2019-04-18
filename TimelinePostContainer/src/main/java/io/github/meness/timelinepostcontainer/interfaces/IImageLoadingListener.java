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

package io.github.meness.timelinepostcontainer.interfaces;

import android.view.View;

import com.todddavies.components.progressbar.ProgressWheel;

public interface IImageLoadingListener {
    /**
     * Is called when image loading progress changed.
     *
     * @param imageUri     Image URI
     * @param progressView Progress view
     * @param view         View for image. Can be <b>null</b>.
     * @param current      Downloaded size in bytes
     * @param total        Total size in bytes
     */
    void onProgressUpdate(String imageUri, ProgressWheel progressView, View view, int current, int total);
}
