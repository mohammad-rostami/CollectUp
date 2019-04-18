/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;

public class PostUtils {
  public static Post convertToPostTimeline(Post postTimeline) {
    Post post = new Post();
    post.setVideoAddress(postTimeline.getVideoAddress());
    post.setImageAddress(postTimeline.getImageAddress());
    post.setId(postTimeline.getId());
    post.setInsertTime(postTimeline.getInsertTime());
    post.setLikes(postTimeline.getLikes());
    if (postTimeline.getSender() instanceof Profile)
    {
      post.setSenderProfile((Profile) postTimeline.getSender());

    } else if (postTimeline.getSender() instanceof Shop)
    {
      post.setSenderShop((Shop) postTimeline.getSender());

    } else
    {
      post.setSenderComplex((Complex) postTimeline.getSender());

    }

    post.setText(postTimeline.getText());
    post.setSize(postTimeline.getSize());
    post.setCommentsCount(postTimeline.getCommentsCount());

    return post;
  }


}
