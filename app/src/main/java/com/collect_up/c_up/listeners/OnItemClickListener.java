package com.collect_up.c_up.listeners;

import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;

/**
 * Created by collect-up3 on 5/11/2016.
 */
public interface OnItemClickListener {
  void onClick(Profile profile);

  void onClick(Shop shop);

  void onClick(String hashTag);

}
