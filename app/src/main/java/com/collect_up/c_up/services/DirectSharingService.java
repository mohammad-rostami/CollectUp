package com.collect_up.c_up.services;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;
import android.service.chooser.ChooserTargetService;

import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.fragments.FragmentChat;
import com.collect_up.c_up.adapters.interfaces.ContactsCallback;
import com.collect_up.c_up.adapters.providers.Contacts;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.model.Profile;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by collect-up3 on 6/15/2016.
 */
@TargetApi (Build.VERSION_CODES.M)
public class DirectSharingService extends ChooserTargetService {
  private Paint roundPaint;
  private RectF bitmapRect;

  @Override
  public List<ChooserTarget> onGetChooserTargets(ComponentName targetActivityName, IntentFilter matchedFilter) {
    final ComponentName componentName = new ComponentName(getPackageName(),
      FragmentChat.class.getCanonicalName());
    final ArrayList<ChooserTarget> targets = new ArrayList<>();

    new Contacts().getAllContacts(this, new ContactsCallback() {
      @Override
      public void onContactsReceived(List<Profile> profiles, List<Contacts.UnRegisteredContact> contacts) {


        for (int i = 0; i < profiles.size(); ++i)
        {
          final Bundle extras = new Bundle();
          extras.putBoolean("fromout", true);
          final String friendProfileName = profiles.get(i).getName();
          File file = DiskCacheUtils.findInCache(Constants.General.BLOB_PROTOCOL + profiles.get(i).getImageAddress(), MyApplication.getInstance().getImageLoader().getDiskCache());
          Icon icon = createRoundBitmap(file);
          // MyApplication.getInstance().getImageLoader().getDiskCache().get(profiles.get(0).getImageAddress()));
          if (icon == null)
          {
            icon = Icon.createWithResource(getApplicationContext(), R.drawable.logo_avatar);
          }
          targets.add(new ChooserTarget(
            // The name of this target.
            friendProfileName,
            // The icon to represent this target.
            icon,
            // The ranking score for this target (0.0-1.0); the system will omit items with
            // low scores when there are too many Direct Share items.
            0.5f,
            // The name of the component to be launched if this target is chosen.
            componentName,
            // The extra values here will be merged into the Intent when this target is
            // chosen.
            extras));
        }


      }
    }, true);

    return targets;
  }

  private Icon createRoundBitmap(File path) {
    try
    {
      Bitmap bitmap = BitmapFactory.decodeFile(path.toString());
      if (bitmap != null)
      {
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        result.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(result);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        if (roundPaint == null)
        {
          roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
          bitmapRect = new RectF();
        }
        roundPaint.setShader(shader);
        bitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawRoundRect(bitmapRect, bitmap.getWidth(), bitmap.getHeight(), roundPaint);
        return Icon.createWithBitmap(result);
      }
    } catch (Throwable e)
    {
    }
    return null;
  }
}
