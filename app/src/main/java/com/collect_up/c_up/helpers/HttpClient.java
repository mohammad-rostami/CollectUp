/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;

import com.collect_up.c_up.BuildConfig;
import com.collect_up.c_up.MyApplication;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Contains static methods to perform operations with AsyncHttpClient.
 */
public class HttpClient {
  private static AsyncHttpClient mClient = new AsyncHttpClient();

  private static final String AUTH = "Authorization";
  private static String BASE_URL = BuildConfig.DEBUG ? Constants.General.BLOB_PROTOCOL + Constants.General.SERVER_URL : Constants.General.PROTOCOL + Constants.General.SERVER_URL;

  private static AsyncHttpClient disableSSL(AsyncHttpClient client) {
    AsyncHttpClient mClient = client;

    KeyStore trustStore = null;
    try
    {

      trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
      sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      mClient.setSSLSocketFactory(sf);
    } catch (KeyStoreException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    } catch (CertificateException e)
    {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    } catch (UnrecoverableKeyException e)
    {
      e.printStackTrace();
    } catch (KeyManagementException e)
    {
      e.printStackTrace();
    }
    return mClient;
  }

  /**
   * GET method
   *
   * @param url             Relative URL
   * @param responseHandler A response handler
   */

  public static void get(String url, AsyncHttpResponser responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }
    mClient.addHeader(AUTH, Utils.getTokenValue());
    try
    {
      mClient.get(URLEncoder.encode(getAbsoluteUrl(url), "UTF-8"), null, responseHandler);
    } catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }
  }

  public static void get(String url, JsonHttpResponseHandler responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }

    mClient.addHeader(AUTH, Utils.getTokenValue());
    try
    {
      mClient.get(URLEncoder.encode(getAbsoluteUrl(url), "UTF-8"), null, responseHandler);
    } catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }
  }


  /**
   * Helper method to join base and relative URLs.
   *
   * @param relativeUrl Relative URL
   * @return Joined string
   */
  private static String getAbsoluteUrl(String relativeUrl) {
    // Ending slash needed
    if (!BASE_URL.endsWith("/"))
    {
      BASE_URL += "/";
    }
    return BASE_URL + relativeUrl;
  }

  /**
   * GET method (suitable for login method)
   *
   * @param url             Relative URL
   * @param responseHandler A response handler
   */
  public static void getWithoutToken(String url, AsyncHttpResponser responseHandler) {

    AsyncHttpClient client = new AsyncHttpClient();
    if (BuildConfig.DEBUG)
    {
      client = disableSSL(client);
    }

    client.setTimeout(30000);
    client.get(getAbsoluteUrl(url), null, responseHandler);
  }

  /**
   * GET method
   *
   * @param url             Relative URL
   * @param timeOut         Timeout
   * @param responseHandler A response handler
   */
  public static void get(String url, int timeOut, AsyncHttpResponser responseHandler) {

    AsyncHttpClient client = new AsyncHttpClient();
    if (BuildConfig.DEBUG)
    {
      client = disableSSL(client);
    }
    client.addHeader(AUTH, Utils.getTokenValue());
    client.setTimeout(timeOut);
    client.get(getAbsoluteUrl(url), null, responseHandler);
  }

  /**
   * POST method using RequestParams
   *
   * @param url             Relative URL
   * @param params          Parameters to pass
   * @param responseHandler A response handler
   */
  public static void post(String url,
                          RequestParams params,
                          AsyncHttpResponser responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }
    mClient.addHeader(AUTH, Utils.getTokenValue());
    mClient.post(getAbsoluteUrl(url), params, responseHandler);
  }

  /**
   * POST method using RequestParams
   *
   * @param url             Relative URL
   * @param params          Parameters to pass
   * @param responseHandler A response handler
   */
  public static void post(String url,
                          String tag,
                          RequestParams params, int timeOut,
                          AsyncHttpResponser responseHandler) {

    AsyncHttpClient uploadClient = new AsyncHttpClient();
    if (BuildConfig.DEBUG)
    {
      uploadClient = disableSSL(uploadClient);
    }
    uploadClient.addHeader(AUTH, Utils.getTokenValue());
    uploadClient.setResponseTimeout(timeOut);
    RequestHandle requestHandle = uploadClient.post(getAbsoluteUrl(url), params, responseHandler).setTag(tag);
    MyApplication.mUploadingFileHandlers.add(requestHandle);
  }

  /**
   * POST method using StringEntity
   *
   * @param context         Context
   * @param url             Relative URL
   * @param json            Json string to pass to StringEntity in UTF-8 charset
   * @param responseHandler A response handler
   */
  public static void post(Context context,
                          String url,
                          String json,
                          String contentType,
                          AsyncTextHttpResponser responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }
    mClient.addHeader(AUTH, Utils.getTokenValue());
    mClient.post(context, getAbsoluteUrl(url), new StringEntity(json, HTTP.UTF_8), contentType, responseHandler);
  }

  public static void post(Context context,
                          String url,
                          String json,
                          String contentType,
                          AsyncHttpResponser responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }
    mClient.addHeader(AUTH, Utils.getTokenValue());
    mClient.post(context, getAbsoluteUrl(url), new StringEntity(json, HTTP.UTF_8), contentType, responseHandler);
  }


  /**
   * POST method using StringEntity
   *
   * @param context         Context
   * @param url             Relative URL
   * @param responseHandler A response handler
   */
  public static void post(Context context,
                          String url,
                          String contentType,
                          AsyncHttpResponser responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }
    mClient.addHeader(AUTH, Utils.getTokenValue());
    mClient.post(context, getAbsoluteUrl(url), null, contentType, responseHandler);
  }

  /**
   * PUT method
   *
   * @param url             Relative URL
   * @param params          Parameters to pass
   * @param responseHandler A response handler
   */
  public static void put(String url,
                         RequestParams params,
                         AsyncHttpResponser responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }
    mClient.addHeader(AUTH, Utils.getTokenValue());
    mClient.put(getAbsoluteUrl(url), params, responseHandler);
  }

  /**
   * PUT method
   *
   * @param url             Relative URL
   * @param json            Parameters to pass
   * @param responseHandler A response handler
   */
  public static void put(Context context,
                         String url,
                         String json,
                         String contentType,
                         AsyncHttpResponser responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }
    mClient.addHeader(AUTH, Utils.getTokenValue());
    mClient.put(context, getAbsoluteUrl(url), new StringEntity(json, HTTP.UTF_8), contentType, responseHandler);
  }

  /**
   * DELETE method
   *
   * @param url             Relative URL
   * @param responseHandler A response handler
   */
  public static void delete(String url, AsyncHttpResponser responseHandler) {
    if (BuildConfig.DEBUG)
    {
      mClient = disableSSL(mClient);
    }
    mClient.addHeader(AUTH, Utils.getTokenValue());
    mClient.delete(getAbsoluteUrl(url), responseHandler);
  }
}
