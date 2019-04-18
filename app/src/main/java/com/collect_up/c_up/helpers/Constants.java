/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.os.Environment;

import com.collect_up.c_up.BuildConfig;

import java.io.File;

/**
 * Contains constants
 */
@SuppressWarnings ("EmptyClass")
public class Constants {
  public enum RequestCodes {
    CHANGE_LANGUAGE,
    PICK_IMAGE,
    PICK_LOCATION,
    PICK_VIDEO,
    TAKE_PHOTO,
    UPDATE_SETTINGS,
    PICK_FILE,
    SHARE_PRODUCT,
    UPDATE_UI,
    UPDATE_OBJECT,
    CROP_IMAGE;
  }

  public static final class General {
    public static final String SERVER_DATE_PATTERN = "yyyy-MM-dd kk:mm:ss.SSS";
    public static final String MENTION_USER_SIGN = "@";
    public static final String MENTION_BUSINESS_SIGN = "$";
    public static final String HASHTAG_SIGN = "#";
    public static final String WEBSITE_URL = "www.collect-up.com";
    public static final String PLAYSTOR_URL = "https://play.google.com/store/apps/details?id=com.collect_up.c_up";
    public static final int MAX_SELECT_IMAGE = 4;
    public static final int MAX_SELECT_VIDEO = 1;
    public static final String PROTOCOL = "http://";//// TODO: 9/7/2016 change to release
    public static final String BLOB_PROTOCOL = "http://";
    public static final String DATE_PATTERN = "MMM dd, yyyy kk:mm";
    public static final String BIRTHDAY_PATTERN = "MMMM dd, yyyy";
    public static final String EVENT_PATTERN = "MMMM dd, yyyy kk:mm";

    // public static final String SERVER_URL = "colle.cloudapp.net/";
    public static final String SERVER_URL = BuildConfig.SERVER_URL;//'"192.168.1.7:16905/"; // local path

    public static final int MAX_CHAT_SELECT_IMAGE = 10;
    public static final int MAX_CHAT_SELECT_VIDEO = 10;
    public static final int CHAT_FILE_MAX_SIZE = 100 * 1024 * 1024; //100MB
    public static final String DOWNLOAD_APP_PATH = "collectup.blob.core.windows.net/versions/collect-up_v%1$s.apk";
    public static final String APP_FOLDER_PATH = Environment.getExternalStorageDirectory() + File.separator + "Collect-Up" + File.separator;
    public static final String APP_FOLDER_IMAGE_PATH = APP_FOLDER_PATH + "Images" + File.separator;
    public static final String APP_FOLDER_VIDEO_PATH = APP_FOLDER_PATH + "Videos" + File.separator;
    public static final String APP_FOLDER_VIDEO_THUMB_PATH = APP_FOLDER_VIDEO_PATH + "Thumbs" + File.separator;
    public static final String APP_FOLDER_FILE_PATH = APP_FOLDER_PATH + "Files" + File.separator;
    public static final int TAG_LIMITATION = -1;
    public static final String APP_PHONE_NUMBER = "4759440447";
    public static final int MAX_SELECT_LANGUAGE = 10;
    public static final int UPLOAD_RESPONSE_TIMEOUT = TimeHelper.MINUTE_MILLIS * 30;
    public static final String UPLOAD_TOKEN_REGISTER = "354kljhas8453kjha0943jas";
    public static final long CALL_AFTER = 90;

    public static final String UPDATE_CHAT_UI = "update_chat_ui";
  }

  public static class Chat {
    public static final class MessageStatus {
      public static final int SENT = 0;
      public static final int DELIVERED = 1;
      public static final int SEEN = 2;
      public static final int FAILED = 3;
    }

  }

  /**
   * Server APIs
   */
  public static final class Server {

    public static class Preferences {
      public static final String GET_USERNAME = "api/Profile/SettingsUsername?value=%1$s";
      public static final String GET_GENDER = "api/Profile/SettingsGender?value=%1$s";
      public static final String GET_JOB = "api/Profile/SettingsJob?value=%1$s";
      public static final String GET_EMAIL = "api/Profile/SettingsEmail?value=%1$s";
      public static final String GET_PHONE_NUMBER = "api/Profile/SettingsPhoneNumber?value=%1$s";
      public static final String GET_BIRTHDAY = "api/Profile/SettingsBirthday?value=%1$s";
      public static final String GET_LOCATION = "api/Profile/SettingsLocation?value=%1$s";
      public static final String GET_LANGUAGES = "api/Profile/SettingsLanguages?value=%1$s";
      public static final String GET_BIOGRAPHY = "api/Profile/SettingsBiography?value=%1$s";
    }

    public static final class Tag {
      public static final String GET_TAGS = "api/Tag/GetTags?tag=%1$s&pageNumber=%2$d";
      public static final String GET_TAGS_POSTS = "api/Tag/GetTagPosts?tag=%1$s&pageNumber=%2$d";

    }

    public static class Notification {
      public static final String GET_NOTIFICATION_REQUEST = "api/Notification/GetRequestsByPage?pageNumber=%1$s";
      public static final String GET_NOTIFICATION = "api/Notification/getByPage?pageNumber=%1$s";
      public static final String GET_INIT_NOTIFICATIONS = "api/Initial/Notifications";
      public static final String REQUEST_COUNT = "api/Notification/GetRequestsCount";

    }

    public static final class Comment {
      public static final String DELETE_BY_ID = "api/Comment/Delete?commentId=%1$s";
      public static final String POST = "api/Comment/Post";
    }

    public static final class Stickers {
      public static final String GET_ALL_PACKAGES = "api/Stickers/GetAllPackages?pageNumber=%1$s";
      public static final String GET_PROFILE_PACKAGES = "api/Stickers/GetProfilePackages?pageNumber=%1$s";
      public static final String ADD_TO_PROFILE_PACKAGES = "api/Stickers/AddToProfilePackages?packageId=%1$s";
      public static final String REMOVE_PACKGE_PROFILE = "api/Stickers/RemovePackageFromProfile?packageId=%1$s";
      public static final String GET_PACKAGE_STICKERS = "api/Stickers/GetPackageStickers?packageId=%1$s";
    }


    public static final class Init {
      public static final String GET_CHECK_NEW_VERSION = "api/Initial/CheckNewVersion?platform=%1$s";
      public static final String GET_LOGIN = "api/Initial/Login?user=%1$s&pwd=%2$s";
    }

    public static final class Complex {
      public static final String DELETE_BY_ID = "api/Complex/Delete?complexId=%1$s";
      public static final String GET_EVENTS = "api/Complex/Events?complexId=%1$s&pageNumber=%2$s";
      public static final String GET_FOLLOW = "api/Complex/Follow?complexId=%1$s";
      public static final String GET_FOLLOWERS = "api/Complex/Followers?complexId=%1$s&pageNumber=%2$d";
      public static final String GET_NEAREST = "api/Complex/Nearest?latitude=%1$s&longitude=%2$s&pageNumber=%3$s";
      public static final String GET_POSTS = "api/Complex/Posts?complexId=%1$s&pageNumber=%2$s";
      public static final String GET_RATE = "api/Complex/AddRate?complexId=%1$s&rate=%2$s";
      public static final String GET_SHOPS = "api/Complex/Shops?complexId=%1$s&pageNumber=%2$s";
      public static final String GET_UNFOLLOW = "api/Complex/Unfollow?complexId=%1$s";
      public static final String POST = "api/Complex/Post?marketerCode=%1$s";
      public static final String POST_ADD_SHOPS = "api/Complex/AddShops?complexId=%1$s";
      public static final String PUT = "api/Complex/Put";
      public static final String POST_FILTERED_SEARCH = "api/Complex/Filter?pageNumber=%1$s";
      public static final String GET_COMPLEX = "api/Complex/Get?complexId=%1$s";
      public static final String POST_ADD_MANAGERS = "api/Complex/AddManagers?complexId=%1$s";

    }

    public static final class Event {
      public static final String POST_FILTERED_SEARCH = "api/Event/Filter?pageNumber=%1$s";
      public static final String DELETE_BY_ID = "api/Event/Delete?eventId=%1$s";
      public static final String POST = "api/Event/PostEvent";
    }

    public static final class Request {
      public static final String FOLLOW_RESULT = "api/Requests/FollowResult?requestId=%1$s&result=%2$s";
      public static final String CANCEL_REQUEST = "api/Requests/CancelFollowRequest?requestedId=%1$s";
      public static final String CANCEL_REQUEST_COMPLEX_TO_SHOP = "api/Requests/CancelComplexRequestedToShop?shopId=%1$s&complexId=%2$s";
      public static final String CANCEL_REQUEST_SHOP_TO_COMPLEX = "api/Requests/CancelShopRequestedToComplex?shopId=%1$s&complexId=%2$s";
      public static final String SHOP_TO_COMPLEX = "api/Requests/ShopRequestedToComplex?requestId=%1$s&result=%2$s";
      public static final String COMPLEX_TO_SHOP = "api/Requests/ComplexRequestedToShop?requestId=%1$s&result=%2$s";
    }

    public static final class Files {
      public static final String POST_UPLOAD_ASYNC = "Uploader/UploadAsync?overWrite=false&mode=%1$s";
      public static final String POST_UPLOAD_ASYNC_CONVERTCROP = "Uploader/UploadVideoAsync?width=%1$s&height=%2$s";
    }

    public static final class Log {
      public static final String POST = "api/Log/Post";
    }

    public static final class Post {
      public static final String DELETE_BY_ID = "api/Post/Delete?postId=%1$s";
      public static final String GET_COMMENTS = "api/Post/Comments?postId=%1$s&pageNumber=%2$d";
      public static final String GET_LIKE_POST = "api/Post/LikePost?postId=%1$s";
      public static final String GET_LIKES = "api/Post/Likes?postId=%1$s&pageNumber=%2$d";
      public static final String GET_REPORT = "api/Post/Report?postId=%1$s";
      public static final String GET_UNLIKE_POST = "api/Post/UnlikePost?postId=%1$s";
      public static final String POST = "api/Post/Post";
      public static final String PUT_POST = "api/Post/PutPost";
      public static final String GET_WORLD = "api/Post/All?pageNumber=%1$s";
      public static final String GET_POST = "api/Post/GetPost?postId=%1$s";
    }

    public static final class Product {
      public static final String DELETE_BY_ID = "api/Product/Delete?productId=%1$s";
      public static final String GET_RATE = "api/Product/AddRate?productId=%1$s&rate=%2$s";
      public static final String POST = "api/Product/Post";
      public static final String PUT = "api/Product/Put";
      public static final String POST_FILTERED_SEARCH = "api/Product/Filter?pageNumber=%1$s";
      public static final String GET_PRODUCT = "api/Product/Get?productId=%1$s";
      public static final String GET_RELATED_PRODUCTS = "api/Product/GetRelatedProducts?productId=%1$s";
    }

    public static final class Profile {
      public static final String GET_INITIAL_DATA = "api/Profile/GetInitialData";
      public static final String GET_FOLLOW = "api/Profile/Follow?followingId=%1$s";
      public static final String GET_FOLLOWERS = "api/Profile/Followers?profileId=%1$s&pageNumber=%2$d";
      public static final String GET_FOLLOWING = "api/Profile/Following?profileId=%1$s&pageNumber=%2$d";
      public static final String GET_DENY = "api/Profile/Deny?profileId=%1$s";
      public static final String GET_POSTS = "api/Profile/Posts?profileId=%1$s&pageNumber=%2$s";
      public static final String GET_UNFOLLOW = "api/Profile/Unfollow?unfollowingId=%1$s";
      public static final String GET_ALLOW = "api/Profile/Allow?profileId=%1$s";
      public static final String POST_CONTACTS = "api/Profile/Contacts?pageNumber=%1$d";
      public static final String PUT = "api/Profile/Put";
      public static final String GET_TIMELINE = "api/Profile/Timeline?pageNumber=%1$s";
      public static final String GET_CHANGE_PRIVATE = "api/Profile/ChangeIsPrivate?isPrivate=%1$s";
      public static final String GET_CHANGE_PRIVATE_MESSAGING = "api/Profile/ChangePrivateMessaging?isPrivate=%1$s";
      public static final String GET_FIND_PEOPLE = "api/Profile/FindPeople?searchText=%1$s&locationLat=%2$s&locationLong=%3$s&pageNumber=%4$d";
      public static final String GET_MENTION_USER = "api/Profile/Mention?searchText=%1$s&postId=%2$s&pageNumber=%3$d";
      public static final String GET_MENTION_Business = "api/Profile/MentionBusiness?searchText=%1$s&pageNumber=%2$d";// "api/Profile/Mention?searchText=%1$s&profileId=%2$s&postId=%3$s&pageNumber=%4$d";
      public static final String GET_PROFILE = "api/Profile/Get?profileId=%1$s";
      public static final String GET_ISBLOCK = "api/Profile/IsBlock?id=%1$s";
      public static final String GET_BLOCKED_LIST = "api/Profile/GetBlockList?pageNumber=%1$s";
      public static final String GET_BLOCK = "api/Profile/Block?id=%1$s";
      public static final String GET_UNBLOCK = "api/Profile/UnBlock?id=%1$s";
      public static final String GET_DENIED_LIST = "api/Profile/DeniedList?pageNumber=%1$s";
      public static final String GET_INIT_CHATS = "api/Initial/Chats?isChat=%1$s";
      public static final String GET_DEACTIVATE = "api/profile/Deactivate";

    }

    public static final class Problem {
      public static final String POST_REPORT = "api/Problem/Report?reporterId=%1$s";
    }

    public static final class OAuth {
      public static final String REGISTER = "api/OAuth/Register";
      public static final String LOGIN_CREDENTIAL = "api/OAuth/Login";
      public static final String GET_VERIFY_CODE = "api/OAuth/VerifyCode?phoneNumber=%1$s&verificationCode=%2$s";
      public static final String MAKE_CALL = "api/OAuth/MakeCall?phoneNumber=%1$s";
      public static final String SESSIONS = "api/OAuth/Sessions";
      public static final String LOGOUT = "api/OAuth/Logout";
      public static final String TERMINATE = "api/OAuth/Terminate?sessionId=%1$s";
      public static final String TERMINATE_ALL = "api/OAuth/TerminateAll";
      public static final String SERVER_STATE = "api/OAuth/State";
      public static final String GET_SECURITY_STATE = "api/OAuth/GetUserSecurityState?phoneNumber=%1$s";
      public static final String GET_CHECK_PASSWORD = "api/OAuth/CheckPassword?password=%1$s";
      public static final String GET_RESET_PASSWORD = "api/OAuth/ResetPassword?phoneNumber=%1$s";
      public static final String GET_CHANGE_PASSWORD = "api/OAuth/ChangePassword?currentPassword=%1$s&newPassword=%2$s";
      public static final String GET_SET_PASSWORD = "api/OAuth/SetPassword?newPassword=%1$s";
      public static final String GET_DISABLE_TWOSTEP = "api/OAuth/DisableTwoStepVerification?currentPassword=%1$s";
      public static final String GET_SET_EMAIL = "api/OAuth/SetEmailAddress?email=%1$s";
      public static final String GET_VALIDATE_USERNAME = "api/OAuth/ValidateUsername?username=%1$s";
    }

    public static final class Shop {
      public static final String DELETE_BY_ID = "api/Shop/Delete?shopId=%1$s";
      public static final String GET_EVENTS = "api/Shop/Events?shopId=%1$s&pageNumber=%2$s";
      public static final String GET_FOLLOW = "api/Shop/Follow?followingId=%1$s";
      public static final String GET_FOLLOWERS = "api/Shop/Followers?shopId=%1$s&pageNumber=%2$d";
      public static final String GET_NEAREST = "api/Shop/Nearest?latitude=%1$s&longitude=%2$s&pageNumber=%3$s";
      public static final String GET_POSTS = "api/Shop/Posts?shopId=%1$s&pageNumber=%2$s";
      public static final String GET_PRODUCTS = "api/Shop/Products?shopId=%1$s&pageNumber=%2$s";
      public static final String GET_RATE = "api/Shop/AddRate?shopId=%1$s&rate=%2$s";
      public static final String GET_UNFOLLOW = "api/Shop/Unfollow?unfollowingId=%1$s";
      public static final String POST = "api/Shop/Post?marketerCode=%1$s";
      public static final String PUT = "api/Shop/Put";
      public static final String POST_ADD_MANAGERS = "api/Shop/AddManagers?shopId=%1$s";
      public static final String GET_SHOP_MANAGERS = "api/Shop/GetManagers?shopId=%1$s&pageNumber=%2$s";
      public static final String GET_COMPLEX_MANAGERS = "api/Complex/GetManagers?complexId=%1$s&pageNumber=%2$s";
      public static final String GET_SET_SHOP_ADMIN = "api/Shop/SetAdmin?shopId=%1$s&newAdminId=%2$s";
      public static final String GET_SET_COMPLEX_ADMIN = "api/Complex/SetAdmin?complexId=%1$s&newAdminId=%2$s";
      public static final String POST_ADD_COMPLEX = "api/Shop/AddComplex?shopId=%1$s&complexId=%2$s";
      public static final String POST_FILTERED_SEARCH = "api/Shop/Filter?pageNumber=%1$s";
      public static final String GET_SHOP = "api/Shop/Get?shopId=%1$s";
      public static final String GET_CATEGORIES = "api/Shop/Categories";
      public static final String GET_INTERNALCATEGORY = "api/Shop/GetInternalCategories?shopId=%1$s";
      public static final String GET_ADD_INTERNALCATEGORY = "api/Shop/AddInternalCategory?shopId=%1$s&categoryName=%2$s";
      public static final String GET_REMOVE_INTERNALCATEGORY = "api/Shop/RemoveInternalCategory?shopId=%1$s&categoryId=%2$s";
      public static final String GET_CATEGORY_PRODUCTS = "api/Shop/GetCategoryProducts?shopId=%1$s&internalCategoryId=%2$s&pageNumber=%3$s";


    }

    public static final class Messaging {
      public static final String GET_CHATLIST = "api/Messaging/GetChatsList?pageNumber=%1$s";//return compactChat list
      public static final String GET_GROUP_MESSAGES = "api/Messaging/GetGroupMessages?chatId=%1$s&pageNumber=%2$s";//return CompactMessage list
      public static final String GET_GROUP_MEMBERS = "api/Messaging/GetGroupMembers?chatId=%1$s&pageNumber=%2$s";//return CompactChatMember list
      public static final String GET_BILATERAL_MESSAGES = "api/Messaging/GetBilateralMessages?chatId=%1$s&pageNumber=%2$s";//return CompactMessage list
      public static final String POST_NEW_GROUP = "api/Messaging/NewGroup";
      public static final String GET_LEAVE_GROUP = "api/Messaging/LeaveGroup?chatId=%1$s";
      public static final String GET_JOIN_GROUP = "api/Messaging/JoinGroup?chatId=%1$s";
      public static final String GET_ADDMEMBER_TOGROUP = "api/Messaging/AddMemberToGroup?chatId=%1$s&profileId=%2$s";
      public static final String GET_REMOVE_GROUP_MEMBER = "api/Messaging/RemoveGroupMember?chatId=%1$s&profileId=%2$s";
      public static final String GET_ADDMANAGER_TOGROUP = "api/Messaging/AddManagerToGroup?chatId=%1$s&profileId=%2$s";
      public static final String GET_SET_GROUP_TITlE = "api/Messaging/SetGroupTitle?chatId=%1$s&newTitle=%2$s";
      public static final String GET_SET_GROUP_IMAGE = "api/Messaging/SetGroupImage?chatId=%1$s&imageAddress=%2$s";
    }
  }
}
