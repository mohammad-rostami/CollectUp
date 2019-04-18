/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.collect_up.c_up.BuildConfig;
import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.listeners.IUploadCallback;
import com.collect_up.c_up.model.UploadResponse;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

import static com.collect_up.c_up.MyApplication.context;

public class Upload {
    private final File mFile;
    private final String mContentType;
    private final Context mContext;
    private final String mTag;

    /**
     * @param file        The file
     * @param contentType the file content type
     */
    public Upload(Context context, File file, String tag, String contentType) {

        mFile = file;
        mContentType = contentType;
        mContext = context;
        mTag = tag;
    }

    public void uploadImage(final IUploadCallback callback) {
        HttpClient.post(getFinalUrl(false, 0, 0), mTag, makeRequestParams(), Constants.General.UPLOAD_RESPONSE_TIMEOUT, new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                UploadResponse[] uploadResponses = GsonParser.getArrayFromGson(responseBody, UploadResponse[].class);
                if (uploadResponses != null) {
                    String fileName = uploadResponses[0].getFileName();
                    String uploadedPath = uploadResponses[0].getContent();
                    MyApplication.getInstance().cancelUploadHandler(mTag);
                    callback.onFileReceived(fileName, uploadedPath);

                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
                MyApplication.getInstance().cancelUploadHandler(mTag);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                callback.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Exceed the maximum size
                if ((statusCode == 400) || (statusCode == 500)) {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_upload_failed_exceeds_max_size), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_error_upload_image_failed), Toast.LENGTH_SHORT)
                            .show();
                }
                MyApplication.getInstance().cancelUploadHandler(mTag);
                callback.onFailed(statusCode);
            }
        });
    }

    /**
     * Joins required mode to the base URL.
     *
     * @return Joined string
     */
    private String getFinalUrl(boolean convertCrop, int width, int height) {
        String baseUrl;
        if (mContentType.startsWith("video/")) {
            baseUrl = Constants.Server.Files.POST_UPLOAD_ASYNC;
            baseUrl = String.format(baseUrl, 2);

            if (convertCrop) {
                baseUrl = Constants.Server.Files.POST_UPLOAD_ASYNC_CONVERTCROP;
                baseUrl = String.format(baseUrl, width, height);
            }
        } else if (mContentType.startsWith("application/pdf")) {
            baseUrl = Constants.Server.Files.POST_UPLOAD_ASYNC;
            baseUrl = String.format(baseUrl, 3);
        } else {

            baseUrl = Constants.Server.Files.POST_UPLOAD_ASYNC;
            baseUrl = String.format(baseUrl, 1);

        }

        return baseUrl;
    }

    /**
     * Makes necessary request parameters for file uploading.
     *
     * @return Made request parameters
     */
    public RequestParams makeRequestParams() {
        RequestParams params = new RequestParams();
        try {
            params.put("file", mFile);
        } catch (FileNotFoundException e) {
        }

        return params;
    }

    public void uploadPdf(final IUploadCallback callback) {
        HttpClient.post(getFinalUrl(false, 0, 0), mTag, makeRequestParams(), Constants.General.UPLOAD_RESPONSE_TIMEOUT, new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                UploadResponse[] uploadResponses = GsonParser.getArrayFromGson(responseBody, UploadResponse[].class);
                if (uploadResponses != null) {
                    String fileName = uploadResponses[0].getFileName();
                    String uploadedPath = uploadResponses[0].getContent();
                    MyApplication.getInstance().cancelUploadHandler(mTag);
                    callback.onFileReceived(fileName, uploadedPath);
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
                MyApplication.getInstance().cancelUploadHandler(mTag);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                callback.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Exceed the maximum size
                if ((statusCode == 400) || (statusCode == 500)) {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_upload_failed_exceeds_max_size), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_upload_pdf_failed), Toast.LENGTH_SHORT)
                            .show();
                }
                MyApplication.getInstance().cancelUploadHandler(mTag);
                callback.onFailed(statusCode);
            }
        });
    }


    public void uploadVideo(final IUploadCallback callback) {
        HttpClient.post(getFinalUrl(false, 0, 0), mTag, makeRequestParams(), Constants.General.UPLOAD_RESPONSE_TIMEOUT, new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                UploadResponse[] uploadResponses = GsonParser.getArrayFromGson(responseBody, UploadResponse[].class);
                if (uploadResponses != null) {
                    String fileName = uploadResponses[0].getFileName();
                    String uploadedPath = uploadResponses[0].getContent();
                    MyApplication.getInstance().cancelUploadHandler(mTag);
                    callback.onFileReceived(fileName, uploadedPath);
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
                MyApplication.getInstance().cancelUploadHandler(mTag);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                callback.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Exceed the maximum size
                if ((statusCode == 400) || (statusCode == 500)) {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_upload_failed_exceeds_max_size), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_upload_video_failed), Toast.LENGTH_SHORT)
                            .show();
                }
                MyApplication.getInstance().cancelUploadHandler(mTag);
                callback.onFailed(statusCode);
            }
        });
    }

    public void uploadVideoWithConvertCrop(int width, int height, final IUploadCallback callback) {
        String path = getFinalUrl(true, width, height);

        if (BuildConfig.DEBUG) {
            Log.d("Uploading video path", path);
        }

        HttpClient.post(path, mTag, makeRequestParams(), Constants.General.UPLOAD_RESPONSE_TIMEOUT, new AsyncHttpResponser(mContext) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                UploadResponse[] uploadResponses = GsonParser.getArrayFromGson(responseBody, UploadResponse[].class);
                if (uploadResponses != null) {
                    String fileName = uploadResponses[0].getFileName();
                    String uploadedPath = uploadResponses[0].getContent();
                    MyApplication.getInstance().cancelUploadHandler(mTag);
                    callback.onFileReceived(fileName, uploadedPath);
                } else {
                    Toast.makeText(context, "Internal Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
                MyApplication.getInstance().cancelUploadHandler(mTag);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                callback.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                // Exceed the maximum size
                if (statusCode == 400 || statusCode == 500) {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_upload_failed_exceeds_max_size), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_upload_video_failed), Toast.LENGTH_SHORT)
                            .show();
                }
                MyApplication.getInstance().cancelUploadHandler(mTag);
                callback.onFailed(statusCode);
            }
        });
    }
}
