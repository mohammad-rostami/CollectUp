package com.collect_up.c_up.util;

import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;

/**
 * Created by collect-up3 on 7/5/2016.
 */
public class MentionObject {

  public Shop shopObjet;
  public Profile profileObjet;

  public MentionObject(Shop shop, Profile profile) {
    this.shopObjet = shop;
    this.profileObjet = profile;
  }

  public Profile getProfileObjet() {
    return profileObjet;
  }

  public void setProfileObjet(Profile profileObjet) {
    this.profileObjet = profileObjet;
  }

  public Shop getShopObjet() {
    return shopObjet;
  }

  public void setShopObjet(Shop shopObjet) {
    this.shopObjet = shopObjet;
  }

}
