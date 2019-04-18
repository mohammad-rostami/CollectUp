package com.collect_up.c_up.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.collect_up.c_up.R;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

/**
 * Created by collect-up3 on 6/22/2016.
 */
public class CustomMultiPermissionDialog extends BaseMultiplePermissionsListener {


  private final Context context;
  private final String title;
  private final String message;
  private final String positiveButtonText;
  private final Drawable icon;

  private CustomMultiPermissionDialog(Context context, String title,
                                      String message, String positiveButtonText, Drawable icon) {
    this.context = context;
    this.title = title;
    this.message = message;
    this.positiveButtonText = positiveButtonText;
    this.icon = icon;
  }

  @Override
  public void onPermissionsChecked(MultiplePermissionsReport report) {
    super.onPermissionsChecked(report);

    if (!report.areAllPermissionsGranted())
    {
      showDialog();
    }
  }

  private void showDialog() {
    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(context);
    builder.message(message)
      .messageTextColor(ContextCompat.getColor(context, R.color.primary_text))
      .title(title)
      .titleColor(ContextCompat.getColor(context, R.color.colorAccent))
      .positiveAction(positiveButtonText)
      .actionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
      .setCancelable(true);
    builder.positiveActionClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        builder.dismiss();

      }
    });
    builder.show();
  }

  /**
   * Builder class to configure the displayed dialog.
   * Non set fields will be initialized to an empty string.
   */
  public static class Builder {
    private final Context context;
    private String title;
    private String message;
    private String buttonText;
    private Drawable icon;

    private Builder(Context context) {
      this.context = context;
    }

    public static Builder withContext(Context context) {
      return new Builder(context);
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withTitle(@StringRes int resId) {
      this.title = context.getString(resId);
      return this;
    }

    public Builder withMessage(String message) {
      this.message = message;
      return this;
    }

    public Builder withMessage(@StringRes int resId) {
      this.message = context.getString(resId);
      return this;
    }

    public Builder withButtonText(String buttonText) {
      this.buttonText = buttonText;
      return this;
    }

    public Builder withButtonText(@StringRes int resId) {
      this.buttonText = context.getString(resId);
      return this;
    }

    public Builder withIcon(Drawable icon) {
      this.icon = icon;
      return this;
    }

    public Builder withIcon(@DrawableRes int resId) {
      this.icon = context.getResources().getDrawable(resId);
      return this;
    }

    public CustomMultiPermissionDialog build() {
      String title = this.title == null ? "" : this.title;
      String message = this.message == null ? "" : this.message;
      String buttonText = this.buttonText == null ? "" : this.buttonText;
      return new CustomMultiPermissionDialog(context, title, message, buttonText, icon);
    }
  }
}
