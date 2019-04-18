package com.collect_up.c_up.listeners;

import com.collect_up.c_up.model.StickerPackage;

/**
 * Created by collect-up3 on 1/7/2017.
 */

public interface IStickerChangeStateListener {
  void onStickerAdded(StickerPackage stickerPackage);

  void onStickerRemoved(StickerPackage stickerPackage);

}
