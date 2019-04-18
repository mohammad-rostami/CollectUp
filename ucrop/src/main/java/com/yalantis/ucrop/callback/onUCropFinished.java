package com.yalantis.ucrop.callback;

import android.content.Intent;

/**
 *
 */
public interface onUCropFinished {

    void onResult(Intent intent);
    void onError(Throwable throwable);

}
