/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityGalleryPager;
import com.collect_up.c_up.activities.ActivityVideoPlayer;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.listeners.LoginSuccess;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.github.developerpaul123.filepickerlibrary.FilePickerBuilder;
import com.github.developerpaul123.filepickerlibrary.enums.MimeType;
import com.github.developerpaul123.filepickerlibrary.enums.Request;
import com.github.developerpaul123.filepickerlibrary.enums.Scope;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.orhanobut.hawk.Hawk;
import com.yalantis.ucrop.Picker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeMap;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

import static com.collect_up.c_up.MyApplication.context;

public class Utils {

    public static Bitmap getBitmapFromURL(Context context, String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return null;
        }
    }

    public static int getScreenWidthPX(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static String getSimCardCountryCode(Context context, List<String> countries) {
        String output = "";

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simCardCountryCode = manager.getSimCountryIso();

        if (isNullOrEmpty(simCardCountryCode)) {
            simCardCountryCode = context.getResources().getConfiguration().locale.getCountry();
        }

        if (!isNullOrEmpty(simCardCountryCode)) {
            for (String data : countries) {
                // Afghanistan,93,AF
                String[] datas = data.split(",");
                if (datas[2].toLowerCase().equals(simCardCountryCode.toLowerCase())) {
                    output = data;
                    break;
                }
            }
        }

        return output;
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public static String getReadableCount(long count) {
        double million = 1000000;
        double kilo = 1000;
        if (count > million) {
            return String.format("%.1f", count / million) + "m";
        } else if (count > kilo) {
            return String.format("%.1f", count / kilo) + "k";
        }

        return Long.toString(count);
    }

    public static void clearAppData(Context context) {
        Hawk.remove("profile");
        Hawk.remove("Token");
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            boolean result = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                    .clearApplicationUserData();
            if (result) {
                System.exit(0);
            }
        } else {
            MyApplication.getInstance().clearApplicationData();
            Hawk.clear();

            System.exit(0);
        }
    }

    public static long localNow() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getTokenValue() {
        String login = Hawk.get("Token");

        if (login != null) {

            String newLogin = Hawk.get("Token");
            return "Bearer " + newLogin;

        }

        return "Bearer ";
    }

    public static void login(final LoginSuccess loginSuccess, Context context) {
        HttpClient.getWithoutToken(String.format(Constants.Server.Init.GET_LOGIN, Logged.Models.getUserProfile().getId(), Logged.Models.getUserProfile().getUsername()), new AsyncHttpResponser(context) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                loginSuccess.onSuccess(statusCode, headers, responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                loginSuccess.onFailure(statusCode, headers, responseBody, error);
            }
        });
    }

    public static void displayToast(Context context, String text, int gravity, int length) {
        Toast toast = Toast.makeText(context, text, length);
        toast.setGravity(gravity, 0, 0);
        toast.show();
    }

    public static void checkForAppPathsExistence(Context context) {
        File mainPath = new File(Constants.General.APP_FOLDER_PATH);
        if (!mainPath.exists()) {
            if (mainPath.mkdir() || mainPath.isDirectory()) {
                checkForAppPathsExistence(context);
            }
        } else {

            File imagePath = new File(Constants.General.APP_FOLDER_IMAGE_PATH);
            if (!imagePath.exists()) {
                imagePath.mkdir();
            }

            File videoPath = new File(Constants.General.APP_FOLDER_VIDEO_PATH);
            if (!videoPath.exists()) {
                videoPath.mkdir();
            }

            File videoThumbPath = new File(Constants.General.APP_FOLDER_VIDEO_THUMB_PATH);
            if (!videoThumbPath.exists()) {
                videoThumbPath.mkdir();
            }
            // Create a .nomedia file to the thumb path to prevent from be scanned by the Gallery app.
            File noMediaFile = new File(Constants.General.APP_FOLDER_VIDEO_THUMB_PATH + ".nomedia");
            if (!noMediaFile.exists()) {
                try {
                    noMediaFile.createNewFile();
                } catch (IOException e) {
                }
            }

            File filePath = new File(Constants.General.APP_FOLDER_FILE_PATH);
            if (!filePath.exists()) {
                filePath.mkdir();
            }
        }
    }

    public static void showSnack(final ISnackListener snack, Activity activity) {
        if (activity != null) {
            Snackbar snackbar = Snackbar.make(activity.getWindow().getDecorView().findViewById(android.R.id.content), R.string.snack_error_occurred, Snackbar.LENGTH_INDEFINITE);
            snackbar.setActionTextColor(ContextCompat.getColor(activity, R.color.snackbar_color));
            snackbar.setAction(R.string.try_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snack.onClick();
                }
            });
            snackbar.show();
        }
    }

    /**
     * Gets random number from min to max
     *
     * @param min Min number
     * @param max Max number
     * @return Randomized number
     **/
    public static int getRandom(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    /**
     * Get the system status bar height
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String removeExtraQuotations(String text) {
        return text.replace("\"", "");
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String readableVideoDuration(long duration) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(0, 0, 0, 0, 0, 0);
        calendar.setTimeInMillis(duration);

        List<Integer> integers = new ArrayList<>();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        integers.add(hour == 0 ? minute : minute + hour * 60);
        integers.add(calendar.get(Calendar.SECOND));

        return TextUtils.join(":", integers);
    }

    public static void takePicture(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            File file = null;
            try {
                file = createImageFile();
            } catch (IOException e) {
            }
            if (file != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                activity.startActivityForResult(intent, Constants.RequestCodes.TAKE_PHOTO.ordinal());
            }
        }
    }

    private static File createImageFile()
            throws IOException {
        String simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = File.createTempFile("JPEG_" + simpleDateFormat + "_", ".jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        Logged.General.setTempTakePhotoFilePath("file:" + file.getAbsolutePath());
        return file;
    }

    public static void captureVideo(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            File file = null;
            try {
                file = createVideoFile();
            } catch (IOException e) {
            }
            if (file != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                activity.startActivityForResult(intent, 10);
            }
        }
    }

    private static File createVideoFile()
            throws IOException {
        String simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = File.createTempFile("MP4_" + simpleDateFormat + "_", ".mp4", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        Logged.General.setTempTakePhotoFilePath("file:" + simpleDateFormat);


        return file;
    }

    public static void displayImageInGallery(Context context, Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        context.startActivity(intent);
    }

    public static void displayImageInternalGallery(Activity activity, ArrayList<Uri> imagesUri, View view, int page) {
        Intent intent = new Intent(activity, ActivityGalleryPager.class);
        ArrayList<String> images = new ArrayList<String>();
        for (int i = 0; i < imagesUri.size(); i++) {
            if (!URLUtil.isNetworkUrl(imagesUri.get(i).toString())) {
                images.add(imagesUri.get(i).getPath());
            } else {
                images.add(imagesUri.get(i).toString());
            }

        }

        intent.putStringArrayListExtra("images", images);
        intent.putExtra("page", page);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            View statusBar = activity.findViewById(android.R.id.statusBarBackground);
            View navigationBar = activity.findViewById(android.R.id.navigationBarBackground);

            List<Pair<View, String>> pairs = new ArrayList<>();
            if (statusBar != null) {
                pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
            }
            if (navigationBar != null) {
                pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
            }
            pairs.add(Pair.create(view, "profile"));
            Bundle options = null;
            if (pairs != null && pairs.size() > 0) {
                options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        pairs.toArray(new Pair[pairs.size()])).toBundle();
            }
            if (options != null)

            {
                activity.startActivity(intent, options);
            } else {
                activity.startActivity(intent);
            }

        } else {
            activity.startActivity(intent);
        }
    }

    public static void playVideoInGallery(Context context, Uri uri) {
        Intent intent = new Intent(context, ActivityVideoPlayer.class)
                .setData(uri);
        context.startActivity(intent);
    }

    public static String saveBitmap(Context context, Bitmap bitmap, String rootPath) throws IOException {
        Utils.checkForAppPathsExistence(context);
        File file = null;
        try {
            file = Images.createTempFile(context, rootPath);
        } catch (IOException e) {
        }
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();

        return file.getAbsolutePath();
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static com.rey.material.app.SimpleDialog createLoadingDialog(Context context) {
        com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(context);
        builder.setContentView(R.layout.loading_progress_bar);
        builder.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        builder.setCancelable(false);

        return builder;
    }

    public static void galleryAddPic(Activity activity, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(filePath)));
        activity.sendBroadcast(intent);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPxiel(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    public static int pxToDp(Context context, int px) {

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((px / displayMetrics.density) + 0.5);
        // return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String getIpAddress(Context context) throws UnknownHostException {
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    public static String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();

        return wInfo.getMacAddress();
    }


    public static void pickVideo(final Fragment activity) {
        PermissionListener dialogPermissionListener =
                CustomPermissionDialog.Builder
                        .withContext(activity.getContext())
                        .withTitle(R.string.permission_title)
                        .withMessage(R.string.permission_storage)
                        .withButtonText(android.R.string.ok)
                        .build();
        PermissionListener basePermission = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                activity.startActivityForResult(intent, Constants.RequestCodes.PICK_VIDEO.ordinal());
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();

            }
        };

        CompositePermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
        Dexter.withActivity(activity.getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(compositePermissionListener)
                .check();


    }

    public static void pickImage(final Fragment activity) {

        PermissionListener dialogPermissionListener =
                CustomPermissionDialog.Builder
                        .withContext(activity.getContext())
                        .withTitle(R.string.permission_title)
                        .withMessage(R.string.permission_storage)
                        .withButtonText(android.R.string.ok)
                        .build();
        PermissionListener basePermission = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                final Picker.PickerBuilder pickerBuilder = new Picker.PickerBuilder(activity, -1, -1)
                        .setAspectRatio(1, 1)
                        .setOnlyImages(true)
                        .setMultiple(false)
                        .setFreeStyle(false);

                Picker picker = new Picker(pickerBuilder);
                picker.start();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();

            }
        };
        CompositePermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
        Dexter.withActivity(activity.getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(compositePermissionListener)
                .check();


    }

    public static void pickFile(Fragment context, int requestCode) {
        new FilePickerBuilder(context).withColor(R.color.colorAccent)
                .withRequest(Request.FILE)
                .withScope(Scope.ALL)
                .useMaterialActivity(true)
                .launch(requestCode);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static Intent pickImage(boolean multiple) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (multiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        return intent;
    }

    public static void pickPdf(Fragment context, int requestCode) {
        new FilePickerBuilder(context).withColor(R.color.colorAccent)
                .withRequest(Request.FILE)
                .withScope(Scope.ALL)
                .withMimeType(MimeType.PDF)
                .useMaterialActivity(true)
                .launch(requestCode);
    }

    public static void displayPdf(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(context);
            builder.message(R.string.no_app_to_handle_intent)
                    .messageTextColor(ContextCompat.getColor(context, R.color.primary_text))
                    .setCancelable(true);

            builder.show();
        }
    }

    public static void installApk(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void displayFile(Context context, Uri uri) {
        String dataAndType = getMimeType(context, uri);
        if (dataAndType != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, dataAndType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(context);
                builder.message(R.string.no_app_to_handle_intent)
                        .messageTextColor(ContextCompat.getColor(context, R.color.primary_text))
                        .setCancelable(true);

                builder.show();
            }
        } else {
            final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(context);
            builder.message(R.string.unknown_file_type)
                    .messageTextColor(ContextCompat.getColor(context, R.color.primary_text))
                    .setCancelable(true);

            builder.show();
        }
    }

    public static String getMimeType(Context context, Uri uri) {
        String type = null;
        String path = getPath(context, uri);
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (isNullOrEmpty(extension)) {
            String[] splitted = path.split("\\.");
            extension = splitted[splitted.length - 1];
        }
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long
                        .valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else if (uri.getPath() != null) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context,
                                       Uri uri,
                                       String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver()
                    .query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String formatLocalPathsProperty(String imageAddress, String videoAddress, String fileAddress) {
        String[] strings = {imageAddress, videoAddress, fileAddress};
        return TextUtils.join(",", strings);
    }

    public static void saveStringToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message", text);
        clipboard.setPrimaryClip(clip);
    }

    public static Bitmap cropBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap croppedBmp;
        if (height > width) {
            croppedBmp = Bitmap.createBitmap(bitmap, 0, (height - width) / 2, width, width);
        } else if (width > height) {
            croppedBmp = Bitmap.createBitmap(bitmap, (width - height) / 2, 0, height, height);
        } else {
            croppedBmp = bitmap;
        }
        return croppedBmp;
    }

    public static List<String> getContactsMobileNumbers(Context context) {
        List<String> result = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                //String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{
                            id}, null);
                    while (pCur.moveToNext()) {
                        int phoneType = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            result.add(phoneNo);
                        }
                    }
                    pCur.close();
                }
            }
        }
        cur.close();
        return result;
    }

    public static TreeMap<String, String> getContactsMobileNumbersAndNames(Context context) {
        TreeMap<String, String> result = new TreeMap<>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{
                            id}, null);
                    while (pCur.moveToNext()) {
                        int phoneType = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            result.put(phoneNo, name);
                        }
                    }
                    pCur.close();
                }
            }
        }
        cur.close();
        return result;
    }

    public static void sendSms(Context context, String phoneNumber, String textMessage) {

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
        intent.putExtra("sms_body", textMessage);
        context.startActivity(intent);
    }

    public static InputFilter getPhoneNumberInputFilter() {
        return new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                // Prevent from typing '0' at the start
                if (source.length() > 0 && dstart == 0 && source.charAt(0) == '0') {
                    return "";
                } else {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                }
                return null;
            }
        };
    }

    public static InputFilter getActivationCodeInputFilter() {
        return new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
    }

    public static String standardizePhoneNumber(String phoneNumber, int userCountryCode) {
        // If starts with 00, just remove it
        if (phoneNumber.startsWith("00")) {
            phoneNumber = phoneNumber.replaceFirst("^00", "");
            // If starts with 0, add the user country code
        } else if (phoneNumber.startsWith("0")) {
            phoneNumber = phoneNumber.replaceFirst("^0", Integer.toString(userCountryCode));
            // If starts with +, removes it
        } else if (phoneNumber.startsWith("+")) {
            phoneNumber = phoneNumber.replace("+", "");
        }

        // Removes all spaces
        return phoneNumber.replace(" ", "").replace("-", "");
    }

    public static void syncContacts(final Context context, final String joinedList) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                int i = 0;
                CheckOut checkOut = new CheckOut();
                checkOut.setValue(joinedList);
                final boolean[] bool = {true};
                final boolean[] lock = {false};
                while (bool[0]) {
                    if (!lock[0]) {
                        lock[0] = true;
                        i++;

                        HttpClient.post(context, String.format(Constants.Server.Profile.POST_CONTACTS, i), new Gson().toJson(checkOut, CheckOut.class), "application/json", new AsyncHttpResponser(context, Looper.getMainLooper()) {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                final Profile[] profiles = GsonParser.getArrayFromGson(responseBody, Profile[].class);
                                if (profiles != null) {
                                    if (profiles.length == 0) {
                                        bool[0] = false;
                                    }


                                    Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            for (Profile profile : profiles) {
                                                realm.copyToRealmOrUpdate(RToNonR.profileToRProfile(profile));
                                            }
                                        }
                                    });
                                    realm.close();
                                    lock[0] = false;
                                } else {
                                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                super.onFailure(statusCode, headers, responseBody, error);

                                lock[0] = false;
                                bool[0] = false;
                            }
                        });
                    }

                }

                return null;
            }
        }.execute();
    }

    public static void addContact(final Context context, final Profile profile) {
        if (profile.getId().equals(Logged.Models.getUserProfile().getId())) {
            Logged.Models.setUserProfile(profile);

            return;
        }
        boolean existsInMyContacts = false;

        HashMap<String, String> contactsInHawk = Hawk.get("contacts");

        if (contactsInHawk != null) {
            for (Map.Entry entry : contactsInHawk.entrySet()) {
                if (entry.getKey().equals(profile.getPhoneNumber())) {
                    existsInMyContacts = true;
                    break;
                }
            }
        }

        if (existsInMyContacts) {
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(context));
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(RToNonR.profileToRProfile(profile));
                        }
                    });

                    return null;
                }
            };

            asyncTask.execute();
        }
    }

    public static byte[] fileToBytes(File file) {
        FileInputStream fileInputStream;

        byte[] bFile = new byte[(int) file.length()];

        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bFile;
    }

    /**
     * Gets last message body by specific phone number.
     *
     * @param context Context
     * @param number  A phone number
     * @return Message body of last message by specific phone number
     */
    public static String getLastMessageBodyBySpecificNumber(Context context, String number) {
        String result = "";
        Cursor cursor = context.getContentResolver()
                .query(Uri.parse("content://sms/inbox"), new String[]{"body",
                        "address"}, "address = '" + number + "'", null, "date desc limit 1");

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndexOrThrow("address")) == null) {
                    cursor.moveToNext();
                    continue;
                }
                result = cursor.getString(cursor.getColumnIndexOrThrow("body"));

            } while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    public static void playMessageSound(Context context, boolean soundIn) {
        MediaPlayer player;
        player = new MediaPlayer();
        AssetFileDescriptor afd;
        if (soundIn) {
            afd = context.getResources().openRawResourceFd(R.raw.sound_in);

        } else {
            afd = context.getResources().openRawResourceFd(R.raw.sound_out);

        }

        //REVIEW: just one  time, player object was null
        if (player != null) {
            try {
                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
                    player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                    player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                    player.prepare();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.start();
        }
    }

    public static int[] getLocalVideoWidthAndHeight(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bmp = null;
        int[] output = new int[2];

        retriever.setDataSource(path);
        bmp = retriever.getFrameAtTime();
        output[0] = bmp.getWidth();
        output[1] = bmp.getHeight();

        return output;
    }
}
