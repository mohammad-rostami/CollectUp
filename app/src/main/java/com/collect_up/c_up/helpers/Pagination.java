/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;
import android.widget.Toast;

import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.Comment;
import com.collect_up.c_up.model.CompactChat;
import com.collect_up.c_up.model.CompactChatMember;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Event;
import com.collect_up.c_up.model.EventComplex;
import com.collect_up.c_up.model.Filter;
import com.collect_up.c_up.model.Notification;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.model.StickerPackage;
import com.google.gson.Gson;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class Pagination {
    public static final int PAGE_IN_REQUEST = 10;

    public static void getComplexEvents(final int pageNumber,
                                        String complexId,
                                        final Context context,
                                        final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Complex.GET_EVENTS, complexId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                EventComplex[] compactPosts = GsonParser.getArrayFromGson(responseBody, EventComplex[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getProducts(final int pageNumber,
                                   Filter filter,
                                   final Context context,
                                   final IPaginationCallback callback) {
        HttpClient.post(context, String.format(Constants.Server.Product.POST_FILTERED_SEARCH, pageNumber), new Gson()
                .toJson(filter, Filter.class), "application/json", new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Product[] compactPosts = GsonParser.getArrayFromGson(responseBody, Product[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getShops(final int pageNumber,
                                Filter filter,
                                final Context context,
                                final IPaginationCallback callback) {
        HttpClient.post(context, String.format(Constants.Server.Shop.POST_FILTERED_SEARCH, pageNumber), new Gson()
                .toJson(filter, Filter.class), "application/json", new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Shop[] compactPosts = GsonParser.getArrayFromGson(responseBody, Shop[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getEvents(final int pageNumber,
                                 Filter filter,
                                 final Context context,
                                 final IPaginationCallback callback) {
        HttpClient.post(context, String.format(Constants.Server.Event.POST_FILTERED_SEARCH, pageNumber), new Gson()
                .toJson(filter, Filter.class), "application/json", new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Event[] compactPosts = GsonParser.getArrayFromGson(responseBody, Event[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getGroupMessages(final Context context, final String chatId, final int pageNumber, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Messaging.GET_GROUP_MESSAGES, chatId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                CompactMessage[] compactMessage = GsonParser.getArrayFromGson(responseBody, CompactMessage[].class);
                if (compactMessage != null) {
                    callback.onPageReceived(Arrays.asList(compactMessage));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                callback.onFailure();

            }
        });
    }

    public static void getBirateralMessages(final Context context, final String chatId, final int pageNumber, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Messaging.GET_BILATERAL_MESSAGES, chatId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                CompactMessage[] compactMessage = GsonParser.getArrayFromGson(responseBody, CompactMessage[].class);
                if (compactMessage != null) {
                    callback.onPageReceived(Arrays.asList(compactMessage));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                callback.onFailure();

            }
        });
    }

    public static void getCategoryProducts(final Context context, String shopId, String internalCategoryId, final int pageNumber, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Shop.GET_CATEGORY_PRODUCTS, shopId, internalCategoryId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                Product[] compactMessage = GsonParser.getArrayFromGson(responseBody, Product[].class);
                if (compactMessage != null) {
                    callback.onPageReceived(Arrays.asList(compactMessage));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                callback.onFailure();

            }
        });
    }

    public static void getChatList(final Context context, final int pageNumber, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Messaging.GET_CHATLIST, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                CompactChat[] compactMessage = GsonParser.getArrayFromGson(responseBody, CompactChat[].class);
                if (compactMessage != null) {
                    callback.onPageReceived(Arrays.asList(compactMessage));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                callback.onFailure();

            }
        });
    }

    public static void getGroupMembers(final Context context, final String chatId, final int pageNumber, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Messaging.GET_GROUP_MEMBERS, chatId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                CompactChatMember[] compactMessage = GsonParser.getArrayFromGson(responseBody, CompactChatMember[].class);
                if (compactMessage != null) {
                    callback.onPageReceived(Arrays.asList(compactMessage));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                callback.onFailure();

            }
        });
    }

    public static void getComplexes(final int pageNumber,
                                    Filter filter,
                                    final Context context,
                                    final IPaginationCallback callback) {
        HttpClient.post(context, String.format(Constants.Server.Complex.POST_FILTERED_SEARCH, pageNumber), new Gson()
                .toJson(filter, Filter.class), "application/json", new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody) {
                Complex[] compactPosts = GsonParser.getArrayFromGson(responseBody, Complex[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getStickers(final int pageNumber, final Context context, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Stickers.GET_ALL_PACKAGES, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                StickerPackage[] compactStickers = GsonParser.getArrayFromGson(responseBody, StickerPackage[].class);
                if (compactStickers != null) {
                    callback.onPageReceived(Arrays.asList(compactStickers));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getWorldPosts(final int pageNumber, final Context context, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Post.GET_WORLD, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Post[] compactPosts = GsonParser.getArrayFromGson(responseBody, Post[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getBlockedList(final int pageNumber, final Context context, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Profile.GET_BLOCKED_LIST, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] compactPosts = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getDeniedList(final int pageNumber, final Context context, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Profile.GET_DENIED_LIST, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] compactPosts = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getTagsPosts(String hashTag, final int pageNumber, final Context context, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Tag.GET_TAGS_POSTS, hashTag, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Post[] compactPosts = GsonParser.getArrayFromGson(responseBody, Post[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getFollowingPosts(final int pageNumber,
                                         final Context context,

                                         final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Profile.GET_TIMELINE, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Post[] compactPosts = GsonParser.getArrayFromGson(responseBody, Post[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getComplexPosts(final int pageNumber,
                                       final String complexId,
                                       final Context context,

                                       final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Complex.GET_POSTS, complexId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Post[] compactPosts = GsonParser.getArrayFromGson(responseBody, Post[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getComplexShops(final int pageNumber,
                                       final String complexId,
                                       final Context context,
                                       final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Complex.GET_SHOPS, complexId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Shop[] compactPosts = GsonParser.getArrayFromGson(responseBody, Shop[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getPersonFollowing(final int pageNumber,
                                          final String profileId,
                                          final Context context,

                                          final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Profile.GET_FOLLOWING, profileId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] profiles = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (profiles != null) {
                    callback.onPageReceived(Arrays.asList(profiles));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getPersonFollowers(final int pageNumber,
                                          final String profileId,
                                          final Context context,

                                          final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Profile.GET_FOLLOWERS, profileId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] profiles = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (profiles != null) {
                    callback.onPageReceived(Arrays.asList(profiles));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getPostLikes(final int pageNumber,
                                    final String profileId,
                                    final Context context,

                                    final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Post.GET_LIKES, profileId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] profiles = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (profiles != null) {
                    callback.onPageReceived(Arrays.asList(profiles));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void findPeople(final int pageNumber,
                                  final String query,

                                  final Context context,
                                  final IPaginationCallback callback) {
        String url = String.format(Constants.Server.Profile.GET_FIND_PEOPLE, query, Logged.Models.getUserProfile().getLat(), Logged.Models.getUserProfile().getLong(), pageNumber);
        HttpClient.get(url, new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] profiles = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (profiles != null) {
                    callback.onPageReceived(Arrays.asList(profiles));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public enum MentionMode {
        USER, BUSINESS, HASHTAG;

        public static MentionMode getMode(int mode) {
            switch (mode) {
                case 0:
                    return USER;
                case 1:
                    return BUSINESS;
                case 2:
                    return HASHTAG;
            }
            return USER;
        }
    }


    public static void getHashTags(final int pageNumber,
                                   final String query,
                                   final Context context,
                                   final IPaginationCallback callback) {
        String url = String.format(Constants.Server.Tag.GET_TAGS, query, pageNumber);
        HttpClient.get(url, new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                CheckOut[] checkOuts = GsonParser.getArrayFromGson(responseBody, CheckOut[].class);
                //     if (checkOuts.length > 0)
                if (checkOuts != null) {
                    callback.onPageReceived(Arrays.asList(checkOuts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getMentions(final int pageNumber,
                                   final String query,
                                   final String postId,
                                   final Context context,

                                   final MentionMode mode,
                                   final IPaginationCallback callback) {
        switch (mode) {
            case USER:
                String url = String.format(Constants.Server.Profile.GET_MENTION_USER, query, postId, pageNumber);
                HttpClient.get(url, new AsyncHttpResponser(context) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Profile[] profiles = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                        //  if (profiles.length > 0)
                        if (profiles != null) {
                            callback.onPageReceived(Arrays.asList(profiles));
                        } else {
                            Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          byte[] responseBody,
                                          Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);

                        callback.onFailure();
                    }
                });
                break;
            case BUSINESS:
                String urlBus = String.format(Constants.Server.Profile.GET_MENTION_Business, query.split(Constants.General.MENTION_BUSINESS_SIGN)[0], pageNumber);
                HttpClient.get(urlBus, new AsyncHttpResponser(context) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Shop[] shops = GsonParser.getArrayFromGson(responseBody, Shop[].class);
                        //    if (shops.length > 0)
                        if (shops != null) {
                            callback.onPageReceived(Arrays.asList(shops));
                        } else {
                            Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          byte[] responseBody,
                                          Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);

                        callback.onFailure();
                    }
                });
        }

    }

    public static void getNotifications(final int pageNumber,
                                        final Context context,
                                        final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Notification.GET_NOTIFICATION, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Notification[] profiles = GsonParser.getArrayFromGson(responseBody, Notification[].class);
                if (profiles != null) {
                    callback.onPageReceived(Arrays.asList(profiles));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getNotificationsRequests(final int pageNumber,

                                                final Context context,

                                                final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Notification.GET_NOTIFICATION_REQUEST, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Notification[] profiles = GsonParser.getArrayFromGson(responseBody, Notification[].class);
                if (profiles != null) {
                    callback.onPageReceived(Arrays.asList(profiles));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getShopFollowers(final int pageNumber,
                                        final String profileId,
                                        final Context context,

                                        final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Shop.GET_FOLLOWERS, profileId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] profiles = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (profiles != null) {
                    callback.onPageReceived(Arrays.asList(profiles));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getComplexFollowers(final int pageNumber,
                                           final String profileId,
                                           final Context context,

                                           final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Complex.GET_FOLLOWERS, profileId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] profiles = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (profiles != null) {
                    callback.onPageReceived(Arrays.asList(profiles));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getComplexes(final int pageNumber,
                                    final String lat,
                                    final String lng,
                                    final Context context,
                                    final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Complex.GET_NEAREST, lat, lng, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Complex[] compactPosts = GsonParser.getArrayFromGson(responseBody, Complex[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getProfilePosts(final int pageNumber,
                                       final String profileId,
                                       final Context context,

                                       final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Profile.GET_POSTS, profileId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Post[] compactPosts = GsonParser.getArrayFromGson(responseBody, Post[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getPostComments(final int pageNumber,
                                       final String postId,
                                       final Context context,

                                       final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Post.GET_COMMENTS, postId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Comment[] compactPosts = GsonParser.getArrayFromGson(responseBody, Comment[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getShopEvents(final int pageNumber,
                                     String shopId,
                                     final Context context,

                                     final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Shop.GET_EVENTS, shopId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Event[] compactPosts = GsonParser.getArrayFromGson(responseBody, Event[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getShopPosts(final int pageNumber,
                                    final String shopId,
                                    final Context context,

                                    final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Shop.GET_POSTS, shopId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Post[] compactPosts = GsonParser.getArrayFromGson(responseBody, Post[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getShopProducts(final int pageNumber,
                                       final String shopId,
                                       final Context context,
                                       String catId,
                                       final IPaginationCallback callback) {
        String url;
        if (catId == null) {
            url = String.format(Constants.Server.Shop.GET_PRODUCTS, shopId, pageNumber);

        } else {
            url = String.format(Constants.Server.Shop.GET_CATEGORY_PRODUCTS, shopId, catId, pageNumber);

        }

        HttpClient.get(url, new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Product[] compactPosts = GsonParser.getArrayFromGson(responseBody, Product[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getShops(final int pageNumber,
                                final String lat,
                                final String lng,
                                final Context context,

                                final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Shop.GET_NEAREST, lat, lng, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Shop[] compactPosts = GsonParser.getArrayFromGson(responseBody, Shop[].class);
                if (compactPosts != null) {
                    callback.onPageReceived(Arrays.asList(compactPosts));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getShopManagers(final Context context, String shopId, int pageNumber, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Shop.GET_SHOP_MANAGERS, shopId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] compactProfile = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (compactProfile != null) {
                    callback.onPageReceived(Arrays.asList(compactProfile));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }

    public static void getComplexManagers(final Context context, String shopId, int pageNumber, final IPaginationCallback callback) {
        HttpClient.get(String.format(Constants.Server.Shop.GET_COMPLEX_MANAGERS, shopId, pageNumber), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Profile[] compactProfile = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                if (compactProfile != null) {
                    callback.onPageReceived(Arrays.asList(compactProfile));
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                callback.onFailure();
            }
        });
    }
}
