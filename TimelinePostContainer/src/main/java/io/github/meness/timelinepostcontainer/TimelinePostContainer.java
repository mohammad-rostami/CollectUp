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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class TimelinePostContainer extends FrameLayout implements IListener, View.OnClickListener, View.OnTouchListener {

    // previous and current video view fields must be static
    private static VideoView mPreviousVideoView;
    private static VideoView mCurrentVideoView;
    private GestureDetector mGestureDetector;
    private Options mOptions = new Options(getContext(), this);
    private int lastPlaybackPosition;
    private String mImagePath;
    private String mVideoPath;
    private ImageView mImageView;
    private Listeners mListeners = new Listeners();
    private Type mType;
    @IdRes
    private int mImageId;
    private String heighSize;
    private boolean isPreparing;
    private TextView txtCorner;

    public String getVideoProperties() {
        return videoProperties;
    }

    public void setVideoProperties(String videoProperties) {
        this.videoProperties = videoProperties;
    }

    private String videoProperties;

    public TimelinePostContainer(Context context) {
        super(context);
        initProperties();
    }

    public TimelinePostContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initProperties();
    }

    public TimelinePostContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initProperties();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TimelinePostContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs);
        initProperties();
    }

    public TimelinePostContainer setListeners(Listeners listeners) {
        mListeners = listeners;
        return this;
    }

    protected void setProgressText(MediaPlayer mp) {

        final int HOUR = 60 * 60 * 1000;
        final int MINUTE = 60 * 1000;
        final int SECOND = 1000;

        int durationInMillis = mp.getDuration();
        int curPos = mp.getCurrentPosition();

        int durationHour = durationInMillis / HOUR;
        int durationMint = (durationInMillis % HOUR) / MINUTE;
        int durationSec = (durationInMillis % MINUTE) / SECOND;

        int currentHour = curPos / HOUR;
        int currentMint = (curPos % HOUR) / MINUTE;
        int currentSec = (curPos % MINUTE) / SECOND;

        if (durationHour > 0) {
            txtCorner.setText(String.format("%02d:%02d:%02d",
                    currentHour, currentMint, currentSec));
            txtCorner.setText(String.format("%02d:%02d:%02d",
                    durationHour, durationMint, durationSec));
        } else {
            txtCorner.setText(String.format("%02d:%02d",
                    currentMint, currentSec));
            txtCorner.setText(String.format("%02d:%02d",
                    durationMint, durationSec));
        }
    }

    public TimelinePostContainer setOptions(Options options) {
        mOptions = options;
        return this;
    }

    private void initProperties() {
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray customTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TimelinePostContainer);

        mOptions.drawablesAnimation = AnimationUtils.loadAnimation(getContext(), customTypedArray.getResourceId(R.styleable.TimelinePostContainer_tpc_drawablesAnim, R.anim.foreground));
        mOptions.looping = customTypedArray.getBoolean(R.styleable.TimelinePostContainer_tpc_looping, true);
        mOptions.keepScreenOnWhilePlaying = customTypedArray.getBoolean(R.styleable.TimelinePostContainer_tpc_keepOnScreen, true);
        mOptions.debug = customTypedArray.getBoolean(R.styleable.TimelinePostContainer_tpc_debug, false);
        mOptions.setPlayDrawable(customTypedArray.getResourceId(R.styleable.TimelinePostContainer_tpc_playDrawable, R.drawable.ic_play_circle_filled_black_24dp));
        mOptions.setPauseDrawable(customTypedArray.getResourceId(R.styleable.TimelinePostContainer_tpc_pauseDrawable, R.drawable.ic_pause_circle_filled_black_24dp));
        mOptions.setVideoLoadingView(customTypedArray.getResourceId(R.styleable.TimelinePostContainer_tpc_videoLoading, R.layout.video_loading));
        mOptions.setImageLoadingView(customTypedArray.getResourceId(R.styleable.TimelinePostContainer_tpc_imageLoading, R.layout.image_loading));
        setBackgroundColor(0xffffff);

        customTypedArray.recycle();
    }

    public TimelinePostContainer setImageLoader(ImageLoader imageLoader) {
        mOptions.imageLoader = imageLoader;
        return this;
    }

    public void build(Type type) {
        mType = type;

        if (mOptions.imageLoader == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.image_loader_not_null));
        }

        if (mType == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.type_must_defined));
        }

        removeAllViews();

        ImageView view;
        if (mType == Type.IMAGE) {
            TimelinePostContainer.this.setPadding(0, 0, 0, 0);
            view = createImageView();
            addView(createImageView(), 0);
            setOnClickListener(null);
        } else {
            TimelinePostContainer.this.setPadding(0, 0, 0, 0);

            //TimelinePostContainer.this.setPadding(Util.dpToPx(1), 0, Util.dpToPx(1), 0);
            view = createImageView();
            txtCorner = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.corner_textview, this, false);
            addView(view, 0);
            if (videoProperties != null) {
                txtCorner.setText(videoProperties);

                addView(txtCorner);
            }
        }

        if (mListeners.listener != null) {
            mListeners.listener.onImageCreate(view);
        }

        if (mOptions.debug) {
            Log.d(TimelinePostContainer.class.getSimpleName(), mVideoPath);
        }
    }

    public void addVideoSize() {
        new AsyncTask<String, Void, String[]>() {
            @Override
            protected String[] doInBackground(String... params) {

                URLConnection connection;
                String[] contentLengthStr = new String[2];
                try {
                    final URL uri = new URL(params[0]);

                    connection = uri.openConnection();
                    connection.connect();
                    contentLengthStr[0] = connection.getHeaderField("content-length");

                } catch (final IOException exception) {
                }
                return contentLengthStr;
            }

            @Override
            protected void onPostExecute(String[] params) {


                //  txtCorner.setText(Util.humanReadableByteCount(Long.valueOf(params[0]), true));
                addView(txtCorner);
                super.onPostExecute(params);
            }
        }.execute(mVideoPath);
    }

    public void setSize(String size) {
        heighSize = size;
    }

    private ImageView createImageView() {
        double height;
        mImageView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.image_view, this, false);

        if (heighSize != null) {
            if (!heighSize.equalsIgnoreCase("0"))
                height = Double.parseDouble(heighSize) * Util.getScreenWidth(this.getContext());
            else
                height = Util.getScreenWidth(this.getContext());
        } else height = Util.getScreenWidth(this.getContext());

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) height);
        this.mImageView.setLayoutParams(layoutParams);
        this.mImageView.setAdjustViewBounds(true);
        this.mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImageId = AndroidUtils.generateViewId();
        mImageView.setId(mImageId);

        displayImage();

        return mImageView;
    }

    private void addTryAgainView() {
        final TextView view = createExplanatoryView(R.string.unable_load_image);
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(view);
                displayImage();
            }
        });
        addView(view);
    }

    private TextView createExplanatoryView(@StringRes int text) {
        removeImageLoadingView();

        TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.explanatory_view, this, false);
        textView.setText(text);

        return textView;
    }

    private void showImageLoadingView() {
        if (mOptions.imageLoadingView == null) {
            mOptions.imageLoadingView = AndroidUtils.createImageLoading(getContext(), this);

        } else {
            mOptions.imageLoadingView.setProgress(0);
        }

        if (mOptions.imageLoadingView.getParent() == null) {
            addView(mOptions.imageLoadingView);
        }
    }

    private void unablePlayVideo() {
        addView(createExplanatoryView(R.string.unable_play_video));
    }

    private void displayImage() {
        if (mImagePath != null)
            mOptions.imageLoader.displayImage(mImagePath, mImageView, null, this, this);
    }

    public Type getType() {
        return mType;
    }

   /* @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);

        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(widthMeasureSpec));
    }*/

    public String getImagePath() {
        return mImagePath;
    }

    public TimelinePostContainer setImagePath(String imagePath) {
        mImagePath = imagePath;
        return this;
    }

    public String getVideoPath() {
        return mVideoPath;
    }

    public TimelinePostContainer setVideoPath(String videoPath) {
        // ISSUE: https://github.com/danikula/AndroidVideoCache/issues/60
        /*HttpProxyCacheServer proxy = MyApplication.getProxy(getContext());
        this.mVideoPath = proxy.getProxyUrl(mVideoPath);*/
        mVideoPath = videoPath;

        return this;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        removeAllViews();
        addView(createImageView(), 0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v instanceof ImageView) {
            return onImageTouch(v, event);
        }

        if ((v instanceof VideoView) && (event.getAction() == MotionEvent.ACTION_UP)) {
            onVideoTouch(v, event);
        }

        return true;
    }

    private void removeImageLoadingView() {
        if (mOptions.imageLoadingView != null) {
            removeView(mOptions.imageLoadingView);
        }
    }

    private void showPlayDrawable() {
        ImageView view = (ImageView) findViewById(R.id.foreground);
        if (view == null) {
            view = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.foreground, this, false);
            view.setImageResource(mOptions.playDrawable);
            addView(view);
        } else if (isPreparing) {
            view.setImageResource(mOptions.playDrawable);
            view.startAnimation(mOptions.drawablesAnimation);
        }
    }

    private void stopPreviousVideo() {

        if (mPreviousVideoView != null) {
            mPreviousVideoView.pause();
            TimelinePostContainer parentLayout = (TimelinePostContainer) mPreviousVideoView.getParent();
            if (parentLayout != null) {
                parentLayout.showPauseDrawable();
            }
        }
    }

    private void showPauseDrawable() {
        ImageView view = (ImageView) findViewById(R.id.foreground);
        if (view != null) {
            view.setImageResource(mOptions.pauseDrawable);
            view.startAnimation(mOptions.drawablesAnimation);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // this is a workaround because API 16 doesn't support setOnInfoListener()
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) && isImageViewExists()) {
            removeVideoLoadingView();
            removeImage();
        }

        if (percent == 100) {
            removeVideoLoadingView();
            return;
        }

        int duration = mp.getCurrentPosition();

        if ((duration == lastPlaybackPosition) && mp.isPlaying()) {
            showVideoLoading();
        } else if (mp.isPlaying()) {
            removeVideoLoadingView();
        }
        lastPlaybackPosition = duration;
    }

    private boolean isImageViewExists() {
        return findViewById(mImageId) != null;
    }

    private void removeVideoLoadingView() {
        if (mOptions.videoLoadingView != null) {
            removeView(mOptions.videoLoadingView);
        }
    }

    /**
     * Remove the image with fading effect.
     */
    private void removeImage() {
        final View view = findViewById(mImageId);
        if (view != null) {
            view.animate()
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            removeView(view);

                            if (mListeners.listener != null) {
                                mListeners.listener.onImageRemove(animation);
                            }
                        }
                    });
        }
    }

    private void showVideoLoading() {
        if (mOptions.videoLoadingView == null) {
            mOptions.videoLoadingView = AndroidUtils.createVideoLoading(getContext(), this);
        }

        if (mOptions.videoLoadingView.getParent() == null) {
            addView(mOptions.videoLoadingView);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        removeImageLoadingView();
        if (!mOptions.looping) {
            showPlayDrawable();
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ImageView) {
            // clicking on try again plays the video, this workaround prevents that.
            if (((ImageView) v).getDrawable() == null) {
                return;
            }

            if (mType == Type.VIDEO) {
                prepareVideo(v);
            }

            if (mListeners.imageClick != null) {
                mListeners.imageClick.onImageClick(v, mType);
            }
        }
    }

    @Override
    public void onProgressUpdate(String s, View view, int i, int i1) {
        int progress = (360 * i) / i1;
        mOptions.imageLoadingView.setProgress(progress);

        if (mListeners.imageLoading != null) {
            mListeners.imageLoading.onProgressUpdate(s, mOptions.imageLoadingView, view, i, i1);
        }
    }

    @Override
    public boolean onImageTouch(View v, MotionEvent event) {
        return (mType == Type.IMAGE) && mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void prepareVideo(View v) {
        // prevents from preparing the video multiple times by multiple clicking on the image.
        v.setOnClickListener(null);
        isPreparing = true;
        showPlayDrawable();
        showVideoLoading();


        final VideoView videoView = (VideoView) LayoutInflater.from(getContext()).inflate(R.layout.video_view, this, false);
        videoView.setVideoPath(mVideoPath);
        videoView.setKeepScreenOn(mOptions.keepScreenOnWhilePlaying);
        videoView.setOnTouchListener(this);

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                unablePlayVideo();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        removeImageLoadingView();
                        removeImage();
                    }
                    return false;
                }
            });
        }

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnBufferingUpdateListener(TimelinePostContainer.this);
                mp.setOnCompletionListener(TimelinePostContainer.this);
                mp.setLooping(mOptions.looping);
                TimelinePostContainer.this.setPadding(Util.dpToPx(1), 0, Util.dpToPx(1), 0);

                mPreviousVideoView = mCurrentVideoView;
                mCurrentVideoView = videoView;

                stopPreviousVideo();

                mp.start();
            }
        });

        addView(videoView, 0);

        if (mListeners.listener != null) {
            mListeners.listener.onVideoCreate(videoView);
        }
    }

    private void setTime(TextView textView, long elapsed) {
        final String PADDED_TIME_FORMAT = "00%s";
        long duration = elapsed / 1000;
        long hours = duration / 3600;
        long minutes = (duration - hours * 3600) / 60;
        long seconds = duration - (hours * 3600 + minutes * 60);
        final String hoursString = String.format(PADDED_TIME_FORMAT, hours);
        final String minutesString = String.format(PADDED_TIME_FORMAT, minutes);
        final String secondsString = String.format(PADDED_TIME_FORMAT, seconds);
        textView.setText(String.format("%s:%s:%s",
                hoursString.substring(hoursString.length() - 2, hoursString.length()),
                minutesString.substring(minutesString.length() - 2, minutesString.length()),
                secondsString.substring(secondsString.length() - 2, secondsString.length())));
    }

    @Override
    public void onVideoTouch(View v, MotionEvent event) {
        if (((MediaController.MediaPlayerControl) v).isPlaying()) {
            ((MediaController.MediaPlayerControl) v).pause();
            removeImageLoadingView();
            showPauseDrawable();
        } else {
            mPreviousVideoView = mCurrentVideoView;
            mCurrentVideoView = ((VideoView) v);
            stopPreviousVideo();

            showPlayDrawable();
            mCurrentVideoView.start();
        }
    }

    @Override
    public void onLoadingStarted(String s, View view) {
        showImageLoadingView();
    }

    @Override
    public void onLoadingFailed(String s, View view, FailReason failReason) {
        removeImageLoadingView();
        addTryAgainView();
    }

    @Override
    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
        if (mType == Type.VIDEO) {
            mImageView.setOnClickListener(this);

            showPlayDrawable();
            TimelinePostContainer.this.setPadding(0, 0,0, 0);

        } else {
            mImageView.setOnClickListener(this);
            mImageView.setOnTouchListener(this);
        }

        removeImageLoadingView();
    }

    @Override
    public void onLoadingCancelled(String s, View view) {
        // empty, intentional
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mListeners.tap != null) {
                mListeners.tap.onDoubleTap(e, mType, TimelinePostContainer.this.getTag());
            }

            return super.onDoubleTap(e);
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mListeners.tap != null) {
                mListeners.tap.onSingleTap(e, mType);
            }

            return super.onSingleTapConfirmed(e);
        }
    }
}
