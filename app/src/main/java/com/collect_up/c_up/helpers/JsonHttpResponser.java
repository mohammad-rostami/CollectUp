package com.collect_up.c_up.helpers;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.collect_up.c_up.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 8/18/2016.
 */
public class JsonHttpResponser extends JsonHttpResponseHandler {

  private final Context mContext;

  public JsonHttpResponser(Context context) {
    mContext = context;

  }

  @Override
  public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
    super.onSuccess(statusCode, headers, response);
  }

  @Override
  public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
    super.onSuccess(statusCode, headers, response);
  }

  @Override
  public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
    super.onFailure(statusCode, headers, throwable, errorResponse);
  }

  @Override
  public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
    super.onFailure(statusCode, headers, throwable, errorResponse);
  }

  @Override
  public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
    onErrorHandler(statusCode, mContext);

  }

  public static void onErrorHandler(int statusCode, final Context context) {
    switch (statusCode)
    {
      case 404:
        break;

      case 401:
        //  case 403:
        //clearApplicationData(context);

        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(context);
        builder.message(R.string.expired_session)
          .messageTextColor(ContextCompat.getColor(context, R.color.primary_text))
          .title(R.string.warning)
          .titleColor(ContextCompat.getColor(context, R.color.colorAccent))
          .positiveAction(R.string.ok)
          .actionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
          .cancelable(false);
        builder.positiveActionClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Utils.clearAppData(context);
          }
        });
        builder.show();

        break;
    }
  }
}
