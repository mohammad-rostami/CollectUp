package com.collect_up.c_up.helpers;

import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.realm.RPost;
import com.orhanobut.hawk.Hawk;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by collect-up3 on 6/19/2016.
 */
public class PostCacheHandler {

  public static final String FOLLOWING_POSTS = "followingPostCache";
  public static final String EXPLORE_POSTS = "explorePostCache";
  public static final String PRODUCT_POSTS = "productPostCache";

  public static void setFollowingPosts(final List<RPost> followingPosts, final Realm mRealm) {
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        RealmResults<RPost> results = realm.where(RPost.class).findAll();


        for (int i = 0; i < results.size(); i++)
        {

          results.remove(i);
        }

      }
    }, new Realm.Transaction.Callback() {
      @Override
      public void onSuccess() {
        super.onSuccess();
        mRealm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            realm.copyToRealmOrUpdate(followingPosts);
          }
        }, null);


      }

      @Override
      public void onError(Exception e) {
        super.onError(e);
      }
    });

  }


  public static List<Post> getFollowingPosts(Realm realm) {

    RealmResults<RPost> results = realm.where(RPost.class).findAllSorted("InsertTime", Sort.DESCENDING);

    return RToNonR.rPostToPostList(results);
  }

  public static void setExplorePosts(final List<RPost> explorePosts, Realm realm) {
    realm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        RealmResults<RPost> results = realm.where(RPost.class).findAll();
        for (int i = 0; i < results.size(); i++)
        {

          results.remove(i);
        }
        for (int i = 0; i < Pagination.PAGE_IN_REQUEST; i++)
        {
          RPost RPostTimeLine = explorePosts.get(i);
          realm.copyToRealmOrUpdate(RPostTimeLine);
        }
      }
    }, null);
  }


  public static List<Post> getExplorePosts() {
    return Hawk.get(EXPLORE_POSTS);
  }

  public static void setProductPostCache(List<Product> productPostCache) {
    Hawk.remove(PRODUCT_POSTS);
    Hawk.put(PRODUCT_POSTS, productPostCache);
  }


  public static List<Product> getProductPostCache() {
    return Hawk.get(PRODUCT_POSTS);
  }
}
