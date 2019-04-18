package com.yalantis.ucrop.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 *
 */

public  class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "SurfaceView";
    SurfaceHolder holder;
    static Camera mCamera;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.holder = getHolder();
        this.holder.addCallback(this);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        if (mCamera != null) {
            /*mCamera.startPreview();*/
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        /*mCamera.stopPreview();*/
        if (mCamera != null) {
            mCamera.release();
        }
    }

}

