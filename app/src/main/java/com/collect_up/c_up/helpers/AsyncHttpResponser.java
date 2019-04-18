package com.collect_up.c_up.helpers;

import android.content.Context;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.services.RealtimeService;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by collect-up3 on 7/19/2016.
 */
public class AsyncHttpResponser extends AsyncHttpResponseHandler {
  private final Context mContext;

  public AsyncHttpResponser(Context context) {
    mContext = context;

  }

  public AsyncHttpResponser(Context context, Looper looper) {
    super(looper);
    mContext = context;
  }

  @Override
  public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

  }

  @Override
  public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
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
        if (!(context instanceof MyApplication) && !(context instanceof RealtimeService))
        {
          final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(context, R.style.SimpleDialogLight);
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
        }
        break;
    }
  }


}
