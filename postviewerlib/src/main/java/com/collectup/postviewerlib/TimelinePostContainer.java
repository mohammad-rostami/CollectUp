package com.collectup.postviewerlib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.toolbox.ImageLoader;
import com.collect_up.c_up.postviewerlib.R;
import com.collectup.postviewerlib.Iinterfaces.ICallback;
import com.collectup.postviewerlib.Iinterfaces.IDoubleTapListener;
import com.collectup.postviewerlib.Iinterfaces.IImageTypeClickListener;

/**
 * Created by collect-up3 on 4/28/2016.
 */
public class TimelinePostContainer extends FrameLayout implements View.OnClickListener, View.OnTouchListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {
    private static VideoView mCurrentVideoView;
    private static VideoView mPreviousVideoView;
    int lastPlaybackPosition;

    boolean isFirstTime;
    @IdRes
    int imageViewId;
    private String mImagePath;
    private String heighSize;
    private String mVideoPath;
    private Type mType;
    private ProgressBar mProgressBar;
    private Drawable mForeground;
    private boolean mLooping;
    private IImageTypeClickListener mImageTypeClickListener;
    private GestureDetector gestureDet;
    private IDoubleTapListener mDoubleTapListener;
    private boolean videoIsPrepared;
    private ImageLoader mImageLoader;
    private ICallback mCallback;
    private ImageVolleyView mImageView;

    public TimelinePostContainer(Context context) {
        super(context);
        this.initConstructors();
    }

    private void initConstructors() {
        this.gestureDet = new GestureDetector(this.getContext(), new TimelinePostContainer.GestureListener());
        setBackgroundColor(0xffffff);
        this.setForegroundGravity(17);
    }

    public TimelinePostContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initAttrs(attrs);
        this.initConstructors();
    }

    private void initAttrs(AttributeSet attrs) {

        TypedArray customTypedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.TimelinePostContainer);
        this.mForeground = customTypedArray.getDrawable(R.styleable.TimelinePostContainer_tpc_foreground);
        this.mLooping = customTypedArray.getBoolean(R.styleable.TimelinePostContainer_tpc_looping, false);
        customTypedArray.recycle();
    }

    public TimelinePostContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initAttrs(attrs);
        this.initConstructors();
    }

    @TargetApi(21)
    public TimelinePostContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initAttrs(attrs);
        this.initConstructors();
    }

    public TimelinePostContainer setDoubleTapListener(IDoubleTapListener listener) {
        this.mDoubleTapListener = listener;
        return this;
    }

    public TimelinePostContainer setImageTypeClickListener(IImageTypeClickListener listener) {
        this.mImageTypeClickListener = listener;
        return this;
    }

    public boolean isLooping() {
        return this.mLooping;
    }

    public TimelinePostContainer setLooping(boolean looping) {
        this.mLooping = looping;
        return this;
    }

    public void build(Type type) {
        this.mType = type;
        if (this.mImageLoader == null) {
            throw new NullPointerException(this.getContext().getString(R.string.image_loader_not_null));
        } else if (this.mType == null) {
            throw new NullPointerException(this.getContext().getString(R.string.type_must_defined));
        } else {
            this.videoIsPrepared = false;
            this.mProgressBar = this.createProgressBar();
            this.removeAllViews();
            ImageVolleyView view;
            if (this.mType == Type.IMAGE) {
                view = this.createImageView();
                this.addView(this.createImageView(), 0);
                this.setOnClickListener((View.OnClickListener) null);
            } else {
                view = this.createImageView();
                this.addView(view, 0);
            }

            if (this.mCallback != null) {
                this.mCallback.onImageCreate(view);
            }

        }
    }

    private void addTryAgainView() {
        final TextView view = this.createExplanatoryView(R.string.try_again);
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TimelinePostContainer.this.addProgressBar();
                TimelinePostContainer.this.mImageView.tryAgain();
                TimelinePostContainer.this.removeView(view);
            }
        });
        this.addView(view);
    }

    private TextView createExplanatoryView(@StringRes int text) {
        this.removeProgressBar();
        TextView textView = new TextView(this.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
        params.gravity = 17;
        textView.setLayoutParams(params);
        textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        textView.setText(text);
        textView.setTextSize(20.0F);
        return textView;
    }

    private void addErrorView() {
        this.addView(this.createExplanatoryView(0));
    }

    private ImageVolleyView createImageView() {
        this.mImageView = new ImageVolleyView(this.getContext());
        this.imageViewId = AndroidUtils.generateViewId();
        this.mImageView.setId(this.imageViewId);
        this.addProgressBar();
        this.mImageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (TimelinePostContainer.this.mType == Type.VIDEO && !TimelinePostContainer.this.isFirstTime) {
                    if (TimelinePostContainer.this.mImageView.getDrawable() != null) {
                        TimelinePostContainer.this.isFirstTime = true;
                        TimelinePostContainer.this.setForeground(TimelinePostContainer.this.mForeground);
                    } else {
                        TimelinePostContainer.this.removeForeground();
                        TimelinePostContainer.this.addProgressBar();
                    }
                }

            }
        });
        this.mImageView.setResponseObserver(new ImageVolleyView.ResponseObserver() {
            public void onError() {
                TimelinePostContainer.this.addTryAgainView();
            }

            public void onSuccess() {
                if (TimelinePostContainer.this.mType == Type.VIDEO) {
                    TimelinePostContainer.this.setPadding(getResources().getDimensionPixelSize(R.dimen.padding), 0, getResources().getDimensionPixelSize(R.dimen.padding), 0);
                    TimelinePostContainer.this.mImageView.setClickable(true);
                    TimelinePostContainer.this.mImageView.setOnClickListener(TimelinePostContainer.this);
                } else {
                    TimelinePostContainer.this.setPadding(0, 0, 0, 0);

                    TimelinePostContainer.this.mImageView.setClickable(false);
                    TimelinePostContainer.this.mImageView.setOnClickListener(TimelinePostContainer.this);
                    TimelinePostContainer.this.mImageView.setOnTouchListener(TimelinePostContainer.this);
                }

                TimelinePostContainer.this.removeProgressBar();
            }
        });
        this.mImageView.setImageUrl(this.mImagePath, this.mImageLoader);
        double height;
        if (heighSize != null) {
            if (!heighSize.equalsIgnoreCase("0"))
                height = Double.parseDouble(heighSize) * Util.getScreenWidth(this.getContext());
            else
                height = Util.getScreenWidth(this.getContext());
        } else height = Util.getScreenWidth(this.getContext());

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) height);
        this.mImageView.setLayoutParams(layoutParams);
        this.mImageView.setAdjustViewBounds(true);
        this.mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.mImageView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                TimelinePostContainer.this.setForeground(TimelinePostContainer.this.mType == Type.IMAGE ? null : TimelinePostContainer.this.mForeground);
            }

            public void onViewDetachedFromWindow(View v) {
            }
        });
        return this.mImageView;
    }

    public Type getType() {
        return this.mType;
    }

    /*  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
          super.onMeasure(widthMeasureSpec, widthMeasureSpec);
          this.setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(widthMeasureSpec));
      }
  */
    public String getImagePath() {
        return this.mImagePath;
    }

    public void setSize(String size) {
        heighSize = size;
    }

    public TimelinePostContainer setImagePath(String imagePath) {
        this.mImagePath = imagePath;
        return this;
    }

    public String getVideoPath() {
        return this.mVideoPath;
    }

    public TimelinePostContainer setVideoPath(String videoPath) {
        this.mVideoPath = videoPath;
        return this;
    }

    public ProgressBar getProgressBar() {
        return this.mProgressBar;
    }

    public TimelinePostContainer setProgressBar(ProgressBar progressBar) {
        this.mProgressBar = progressBar;
        return this;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.removeAllViews();
        this.videoIsPrepared = false;
        this.addView(this.createImageView(), 0);
    }

    public TimelinePostContainer setCallback(ICallback callback) {
        this.mCallback = callback;
        return this;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (!(v instanceof ImageVolleyView)) {
            if (v instanceof VideoView && event.getAction() == 1) {
                if (((VideoView) v).isPlaying()) {
                    ((VideoView) v).pause();
                    this.removeProgressBar();
                    this.setPlayForeground();
                } else {
                    mPreviousVideoView = mCurrentVideoView;
                    mCurrentVideoView = (VideoView) v;
                    this.stopPreviousVideo();
                    this.removeForeground();
                    mCurrentVideoView.start();
                }
            }

            return true;
        } else {
            return this.mType == Type.IMAGE && this.gestureDet.onTouchEvent(event);
        }
    }

    private void removeProgressBar() {
        if (this.mProgressBar != null) {
            this.removeView(this.mProgressBar);
        }

    }

    private void setPlayForeground() {
        this.setForeground(this.mForeground);
    }

    private void stopPreviousVideo() {
        if (mPreviousVideoView != null) {
            mPreviousVideoView.pause();
            FrameLayout parentLayout = (FrameLayout) mPreviousVideoView.getParent();
            if (parentLayout != null) {
                parentLayout.setForeground(this.mForeground);
            }
        }

    }

    private void removeForeground() {
        this.setForeground((Drawable) null);
    }

    public void setForeground(Drawable foreground) {
        super.setForeground(foreground);
    }

    public TimelinePostContainer setImageLoader(ImageLoader imageLoader) {
        if (imageLoader == null) {
            throw new NullPointerException(this.getContext().getString(0));
        } else {
            this.mImageLoader = imageLoader;
            return this;
        }
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (Build.VERSION.SDK_INT < 17 && this.isRemovingImageNeeded()) {
            this.removeForeground();
            this.removeProgressBar();
            this.removeImage();
        }

        if (percent == 100) {
            this.removeProgressBar();
        } else {
            int duration = mp.getCurrentPosition();
            if (duration == this.lastPlaybackPosition && mp.isPlaying()) {
                this.addProgressBar();
            } else if (mp.isPlaying()) {
                this.removeProgressBar();
            }

            this.lastPlaybackPosition = duration;
        }
    }

    private boolean isRemovingImageNeeded() {
        return this.findViewById(this.imageViewId) != null;
    }

    private void removeImage() {
        int childCounts = this.getChildCount();

        for (int child = 0; child < childCounts; ++child) {
            final View view = this.getChildAt(child);
            if (view instanceof ImageVolleyView) {
                view.animate().alpha(0.0F).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        TimelinePostContainer.this.removeView(view);
                        if (TimelinePostContainer.this.mCallback != null) {
                            TimelinePostContainer.this.mCallback.onImageRemove(animation);
                        }

                    }
                });
                break;
            }
        }

    }

    private void addProgressBar() {
        if (this.mProgressBar == null) {
            this.mProgressBar = this.createProgressBar();
        }

        if (this.mProgressBar.getParent() == null) {
            this.removeForeground();
            this.addView(this.mProgressBar);
        }

    }

    private ProgressBar createProgressBar() {
        ProgressBar progressBar = new ProgressBar(this.getContext());
        progressBar.setIndeterminate(true);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.bottomMargin = AndroidUtils.dpToPx(50);
        layoutParams.topMargin = AndroidUtils.dpToPx(50);
        //layoutParams.rightMargin = AndroidUtils.dpToPx(10);
        layoutParams.width = AndroidUtils.dpToPx(42);
        layoutParams.height = AndroidUtils.dpToPx(42);
        progressBar.setLayoutParams(layoutParams);
        return progressBar;
    }

    public void onCompletion(MediaPlayer mp) {
        this.removeProgressBar();
        if (!this.mLooping) {
            this.setPlayForeground();
        }

    }

    public void onClick(View v) {
        if (v instanceof ImageVolleyView && this.mType == Type.VIDEO) {
            if (!this.videoIsPrepared) {
                this.videoIsPrepared = true;
                this.addProgressBar();
                final VideoView videoView = new VideoView(this.getContext());
                FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(-1, -1);
                videoView.setLayoutParams(videoParams);
                videoView.setVideoPath(this.mVideoPath);
                videoView.setKeepScreenOn(true);
                videoView.setOnTouchListener(this);
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        TimelinePostContainer.this.addErrorView();
                        return true;
                    }
                });
                if (Build.VERSION.SDK_INT >= 17) {
                    videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                            if (what == 3) {
                                TimelinePostContainer.this.removeForeground();
                                TimelinePostContainer.this.removeProgressBar();
                                TimelinePostContainer.this.removeImage();
                            }

                            return false;
                        }
                    });
                }

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        mp.setOnBufferingUpdateListener(TimelinePostContainer.this);
                        mp.setOnCompletionListener(TimelinePostContainer.this);
                        mp.setLooping(TimelinePostContainer.this.mLooping);
                        TimelinePostContainer.mPreviousVideoView = TimelinePostContainer.mCurrentVideoView;
                        TimelinePostContainer.mCurrentVideoView = videoView;
                        TimelinePostContainer.this.stopPreviousVideo();
                        mp.start();
                    }
                });
                this.addView(videoView, 0);
                if (this.mCallback != null) {
                    this.mCallback.onVideoCreate(videoView);
                }
            }
        } else if (v instanceof ImageVolleyView && this.mType == Type.IMAGE && this.mImageTypeClickListener != null) {
            this.mImageTypeClickListener.onImageTypeClickListener(v, this.getTag());
        }

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private GestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onDoubleTap(MotionEvent e) {
            if (TimelinePostContainer.this.mDoubleTapListener != null) {
                TimelinePostContainer.this.mDoubleTapListener.doubleTapOnImageType(e, TimelinePostContainer.this.getTag());
            }

            return true;
        }
    }
}
