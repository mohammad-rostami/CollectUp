package com.collectup.postviewerlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
/**
 * Created by collect-up3 on 4/28/2016.
 */
public class ImageVolleyView extends ImageView {
    private ImageVolleyView.ResponseObserver mObserver;
    private String mUrl;
    private int mDefaultImageId;
    private int mErrorImageId;
    private ImageLoader mImageLoader;
    private ImageContainer mImageContainer;

    public ImageVolleyView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ImageVolleyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageVolleyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setResponseObserver(ImageVolleyView.ResponseObserver observer) {
        this.mObserver = observer;
    }

    public void setImageUrl(String url, ImageLoader imageLoader) {
        this.mUrl = url;
        this.mImageLoader = imageLoader;
        this.loadImageIfNecessary(false);
    }

    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = this.getWidth();
        int height = this.getHeight();
        boolean isFullyWrapContent = this.getLayoutParams() != null && this.getLayoutParams().height == -2 && this.getLayoutParams().width == -2;
        if (width != 0 || height != 0 || isFullyWrapContent) {
            if (TextUtils.isEmpty(this.mUrl)) {
                if (this.mImageContainer != null) {
                    this.mImageContainer.cancelRequest();
                    this.mImageContainer = null;
                }

                this.setDefaultImageOrNull();
            } else {
                if (this.mImageContainer != null && this.mImageContainer.getRequestUrl() != null) {
                    if (this.mImageContainer.getRequestUrl().equals(this.mUrl)) {
                        return;
                    }

                    this.mImageContainer.cancelRequest();
                    this.setDefaultImageOrNull();
                }
                ImageContainer newContainer = this.mImageLoader.get(this.mUrl, new ImageListener() {
                    public void onErrorResponse(VolleyError error) {
                        if (ImageVolleyView.this.mErrorImageId != 0) {
                            ImageVolleyView.this.setImageResource(ImageVolleyView.this.mErrorImageId);
                        }

                        if (ImageVolleyView.this.mObserver != null) {
                            ImageVolleyView.this.mObserver.onError();
                        }

                    }

                    public void onResponse(final ImageContainer response, boolean isImmediate) {
                        if (isImmediate && isInLayoutPass) {
                            ImageVolleyView.this.post(new Runnable() {
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                        } else {
                            if (response.getBitmap() != null) {
                                ImageVolleyView.this.setImageBitmap(response.getBitmap());
                            } else if (ImageVolleyView.this.mDefaultImageId != 0) {
                                ImageVolleyView.this.setImageResource(ImageVolleyView.this.mDefaultImageId);
                            }

                            if (ImageVolleyView.this.mObserver != null) {
                                ImageVolleyView.this.mObserver.onSuccess();
                            }

                        }
                    }
                });
                this.mImageContainer = newContainer;
            }
        }
    }

    private void setDefaultImageOrNull() {
        if (this.mDefaultImageId != 0) {
            this.setImageResource(this.mDefaultImageId);
        } else {
            this.setImageBitmap((Bitmap) null);
        }

    }

    public void setDefaultImageResId(int defaultImage) {
        this.mDefaultImageId = defaultImage;
    }

    public void setErrorImageResId(int errorImage) {
        this.mErrorImageId = errorImage;
    }

    public void tryAgain() {
        boolean isInLayoutPass = false;
        ImageContainer newContainer = this.mImageLoader.get(this.mUrl, new ImageListener() {
            public void onErrorResponse(VolleyError error) {
                if (ImageVolleyView.this.mErrorImageId != 0) {
                    ImageVolleyView.this.setImageResource(ImageVolleyView.this.mErrorImageId);
                }

                if (ImageVolleyView.this.mObserver != null) {
                    ImageVolleyView.this.mObserver.onError();
                }

            }

            public void onResponse(ImageContainer response, boolean isImmediate) {
                if (isImmediate) {
                    ;
                }

                if (response.getBitmap() != null) {
                    ImageVolleyView.this.setImageBitmap(response.getBitmap());
                } else if (ImageVolleyView.this.mDefaultImageId != 0) {
                    ImageVolleyView.this.setImageResource(ImageVolleyView.this.mDefaultImageId);
                }

                if (ImageVolleyView.this.mObserver != null) {
                    ImageVolleyView.this.mObserver.onSuccess();
                }

            }
        });
        this.mImageContainer = newContainer;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.loadImageIfNecessary(true);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        this.invalidate();
    }

    protected void onDetachedFromWindow() {
        if (this.mImageContainer != null) {
            this.mImageContainer.cancelRequest();
            this.setImageBitmap((Bitmap) null);
            this.mImageContainer = null;
        }

        super.onDetachedFromWindow();
    }

    public interface ResponseObserver {
        void onError();

        void onSuccess();
    }
}
