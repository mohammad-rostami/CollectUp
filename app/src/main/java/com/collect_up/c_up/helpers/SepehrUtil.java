package com.collect_up.c_up.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collect_up.c_up.NonUnderlineClickableSpan;
import com.collect_up.c_up.R;
import com.collect_up.c_up.model.PostProfileTag;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.util.MentionObject;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmConfiguration;

/**
 * Created by collect-up3 on 5/1/2016.
 */
public class SepehrUtil {
  public static RealmConfiguration getRealmConfiguration(Context context) {
    RealmConfiguration config2 = new RealmConfiguration.Builder(context)
      .name("default2")
      .schemaVersion(3)
      .deleteRealmIfMigrationNeeded()
      .build();
    return config2;
  }

  public static void addPeople(final Context context, final FrameLayout root, final PostProfileTag postProfileTag) {
    LayoutInflater inflater = LayoutInflater.from(context);
    final RelativeLayout tagLayout = (RelativeLayout) inflater.inflate(R.layout.tagable_textview, root, false);

    tagLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        FragmentHandler.replaceFragment(context, fragmentType.PROFILE, postProfileTag.getProfile());
      }
    });
    TextView txtTag = (TextView) tagLayout.findViewById(R.id.txtTag);
    txtTag.setText(postProfileTag.getProfile().getUsername());

    ImageView btnRemove = (ImageView) tagLayout.findViewById(R.id.btnRemove);
    btnRemove.setVisibility(View.GONE);

    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tagLayout.getLayoutParams();

    final float screenX = Utils.dpToPxiel(context, postProfileTag.getX());
    final float screenY = Utils.dpToPxiel(context, postProfileTag.getY());

    params.leftMargin = (int) screenX;
    params.topMargin = (int) screenY + Utils.dpToPxiel(context, 30); ;


    root.addView(tagLayout, params);
    tagLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        tagLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        tagLayout.getWidth();
        if (screenX >= root.getWidth() - tagLayout.getWidth())
        {
          FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) tagLayout.getLayoutParams();
          param.leftMargin = root.getWidth() - tagLayout.getWidth();
          tagLayout.setLayoutParams(param);
        }

        if (screenY >= root.getHeight() - tagLayout.getHeight())
        {
          FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) tagLayout.getLayoutParams();
          param.topMargin = root.getHeight() - tagLayout.getHeight();
          tagLayout.setLayoutParams(param);
        }
      }
    });
  }


  public static String getMapStaticUrl(Context context, double lat, double lng) {
    String url = "https://maps.googleapis.com/maps/api/staticmap?center="
      + lat + "," + lng +
      "&zoom=17&format=png8&size=200x200&scale=2&maptype=roadmap&markers=color:red%7Clabel:%7C" + lat + "," + lng + "&key="
      + context.getResources().getString(R.string.GOOGLE_GEO_API_KEY);
    return url;
  }

  public static boolean validatePhonenumber(String phNumber) {
    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    Phonenumber.PhoneNumber phoneNumber = null;
    try
    {
      phoneNumber = phoneNumberUtil.parse("+" + phNumber, "");
    } catch (NumberParseException e)
    {
      System.err.println(e);
    }
    if (phoneNumber == null)
    {
      return false;
    }
    boolean isValid = phoneNumberUtil.isValidNumber(phoneNumber);
    if (isValid)
    {
      return true;
    } else
    {
      return false;
    }
  }

  public static String getPhoneName() {
    return android.os.Build.MODEL;
  }

  public static String showSizeFile(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit)
    {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  public static int getScreenWith(Activity activity) {
    Display display = activity.getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    int width = size.x;
    return width;
  }

  public static int getScreenHeight(Activity activity) {
    Display display = activity.getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    int height = size.y;
    return height;
  }

  public static String getVersionName(Context context) {
    PackageInfo pInfo = null;
    try
    {
      pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e)
    {
      e.printStackTrace();
    }
    String version = pInfo.versionName;
    return version;
  }

  public static int distFrom(float lat1, float lng1, float lat2, float lng2) {
    double earthRadius = 6371000; //meters
    double dLat = Math.toRadians(lat2 - lat1);
    double dLng = Math.toRadians(lng2 - lng1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    float dist = (float) (earthRadius * c);

    return (int) (Math.floor(dist / 1000) + 1);
  }

  public static String getIpAddress(Context context) {
    WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    return ip;
  }

  public static String uriToRatio(Uri uri) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
    int imageHeight = options.outHeight;
    int imageWidth = options.outWidth;
    return (imageHeight * 1.0 / imageWidth * 1.0) + "";

  }

  public static boolean contactExists(Context context, String number) {
    Uri lookupUri = Uri.withAppendedPath(
      ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
      Uri.encode(number));
    String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
    Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
    try
    {
      if (cur.moveToFirst())
      {
        return true;
      }
    } finally
    {
      if (cur != null)
      {
        cur.close();
      }
    }
    return false;
  }

  public static SpannableString decryptMention(final Context context, String encreptedText, boolean isComment) {

    String orgText = encreptedText;

    ArrayList<Integer> startPositions;
    ArrayList<Integer> endPositions;
    ArrayList<Integer> tempStartPositions = new ArrayList();
    ArrayList<Integer> tempEndPositions = new ArrayList();

    final ArrayList<Integer> startPositionHashtags = new ArrayList<>();
    final ArrayList<Integer> endPositionsHashtags = new ArrayList<>();

    final String[] mentionSplites = orgText.split("<mention>");
    final MentionObject[] mentionObject = new MentionObject[mentionSplites.length];


    for (int i = 1; i < mentionSplites.length; i++)
    {
      startPositions = new ArrayList<>();
      endPositions = new ArrayList<>();

      //startPositions matcher
      Pattern startPattern = Pattern.compile("<mention>");
      Matcher startMatcher = startPattern.matcher(orgText);
      String encryptMention = mentionSplites[i].split("</mention>")[0];
      if (encryptMention.contains("SettingsBirthday")
        && encryptMention.contains("SettingsGender")
        && encryptMention.contains("SettingsLanguages"))
      {

        Profile profile = new Gson().fromJson(encryptMention, Profile.class);
        mentionObject[i] = new MentionObject(null, profile);
      } else
      {

        Shop shop = new Gson().fromJson(encryptMention, Shop.class);
        mentionObject[i] = new MentionObject(shop, null);
      }
      while (startMatcher.find())
      {
        startPositions.add(startMatcher.start());
      }

      ArrayList<Integer> endpositionBuilder = new ArrayList<>();
      //endPositions matcher
      Pattern endtPattern = Pattern.compile("</mention>");
      Matcher endtMatcher = endtPattern.matcher(orgText);
      while (endtMatcher.find())
      {
        endpositionBuilder.add(endtMatcher.start() + 10);
      }
      if (mentionObject[i].getProfileObjet() == null)
      {
        endPositions.add(startPositions.get(0) + mentionObject[i].getShopObjet().getUsername().length() + 1);
        tempEndPositions.add(endPositions.get(0));
        tempStartPositions.add(startPositions.get(0));

        orgText = orgText.substring(0, startPositions.get(0))
          + Constants.General.MENTION_BUSINESS_SIGN + mentionObject[i].getShopObjet().getUsername().toString() +
          orgText.substring(endpositionBuilder.get(0), orgText.length());
      } else
      {
        endPositions.add(startPositions.get(0) + mentionObject[i].getProfileObjet().getUsername().length() + 1);
        tempEndPositions.add(endPositions.get(0));
        tempStartPositions.add(startPositions.get(0));

        orgText = orgText.substring(0, startPositions.get(0))
          + Constants.General.MENTION_USER_SIGN + mentionObject[i].getProfileObjet().getUsername().toString() +
          orgText.substring(endpositionBuilder.get(0), orgText.length());
      }


    }
    SpannableString textSpannable = new SpannableString(orgText);

    if (!isComment)
    {

      Pattern startPatternHashtag = Pattern.compile("#\\w*");
      Matcher startMatcherHashtag = startPatternHashtag.matcher(orgText);
      while (startMatcherHashtag.find())
      {
        startPositionHashtags.add(startMatcherHashtag.start());
        endPositionsHashtags.add(startMatcherHashtag.end());
      }

      for (int i = 0; i < startPositionHashtags.size(); i++)
      {
        final String text = orgText;
        final int startPos = startPositionHashtags.get(i);
        final int endPos = endPositionsHashtags.get(i);
        textSpannable.setSpan(new NonUnderlineClickableSpan() {
          @Override
          public void onClick(View widget) {
            FragmentHandler.replaceFragment(context, fragmentType.HASHTAG, text.substring(startPos + 1, endPos));
          }
        }, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
    if (tempStartPositions.size() > 0)
    {
      for (int i = 1; i < mentionSplites.length; i++)
      {
        if (mentionObject[i].getProfileObjet() == null)
        {
          final Shop tempShop = mentionObject[i].getShopObjet();
          textSpannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorAccent)),
            tempStartPositions.get(i - 1), tempEndPositions.get(i - 1),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

          textSpannable.setSpan(new StyleSpan(Typeface.NORMAL),
            tempStartPositions.get(i - 1), tempEndPositions.get(i - 1),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

          textSpannable.setSpan(new NonUnderlineClickableSpan() {
            @Override
            public void onClick(View widget) {
              FragmentHandler.replaceFragment(context, fragmentType.BUSINESS, tempShop);

            }
          }, tempStartPositions.get(i - 1), tempEndPositions.get(i - 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else
        {
          final Profile tempProfile = mentionObject[i].getProfileObjet();
          textSpannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorAccent)),
            tempStartPositions.get(i - 1), tempEndPositions.get(i - 1),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

          textSpannable.setSpan(new StyleSpan(Typeface.NORMAL),
            tempStartPositions.get(i - 1), tempEndPositions.get(i - 1),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

          textSpannable.setSpan(new NonUnderlineClickableSpan() {
            @Override
            public void onClick(View widget) {
              FragmentHandler.replaceFragment(context, fragmentType.PROFILE, tempProfile);
            }
          }, tempStartPositions.get(i - 1), tempEndPositions.get(i - 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

      }
    }


    return textSpannable;
  }


  public static int getLastCharPos(String character, String inputText, int cursorPosition) {
    int finalPos = 0;
    ArrayList<Integer> positions = new ArrayList();
    Pattern p = Pattern.compile(character);
    Matcher m = p.matcher(inputText.toString());
    while (m.find())
    {
      positions.add(m.start());
    }

    for (int i = positions.size() - 1; i >= 0; i--)
    {
      if (cursorPosition > positions.get(i))
      {
        finalPos = positions.get(i);
        break;
      }
    }
    return finalPos;
  }

  private static Bitmap RGB565toARGB888(Bitmap img) throws Exception {
    int numPixels = img.getWidth() * img.getHeight();
    int[] pixels = new int[numPixels];

    //Get JPEG pixels.  Each int is the color values for one pixel.
    img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

    //Create a Bitmap of the appropriate format.
    Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

    //Set RGB pixels.
    result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
    return result;
  }

  public static boolean isEmailValid(String email) {
    boolean isValid = false;

    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
    CharSequence inputStr = email;

    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputStr);
    if (matcher.matches())
    {
      isValid = true;
    }
    return isValid;
  }

  public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius) {

    if (Build.VERSION.SDK_INT > 16)
    {
      try
      {
        sentBitmap = RGB565toARGB888(sentBitmap);
      } catch (Exception e)
      {
        e.printStackTrace();
      }


      Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

      final RenderScript rs = RenderScript.create(context);
      final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
        Allocation.USAGE_SCRIPT);
      final Allocation output = Allocation.createTyped(rs, input.getType());
      final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
      script.setRadius(radius /* e.g. 3.f */);
      script.setInput(input);
      script.forEach(output);
      output.copyTo(bitmap);
      return bitmap;
    }

    // Stack Blur v1.0 from
    // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
    //
    // Java Author: Mario Klingemann <mario at quasimondo.com>
    // http://incubator.quasimondo.com
    // created Feburary 29, 2004
    // Android port : Yahel Bouaziz <yahel at kayenko.com>
    // http://www.kayenko.com
    // ported april 5th, 2012

    // This is a compromise between Gaussian Blur and Box blur
    // It creates much better looking blurs than Box Blur, but is
    // 7x faster than my Gaussian Blur implementation.
    //
    // I called it Stack Blur because this describes best how this
    // filter works internally: it creates a kind of moving stack
    // of colors whilst scanning through the image. Thereby it
    // just has to add one new block of color to the right side
    // of the stack and remove the leftmost color. The remaining
    // colors on the topmost layer of the stack are either added on
    // or reduced by one, depending on if they are on the right or
    // on the left side of the stack.
    //
    // If you are using this algorithm in your code please add
    // the following line:
    //
    // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

    Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

    if (radius < 1)
    {
      return (null);
    }

    int w = bitmap.getWidth();
    int h = bitmap.getHeight();

    int[] pix = new int[w * h];
    Log.e("pix", w + " " + h + " " + pix.length);
    bitmap.getPixels(pix, 0, w, 0, 0, w, h);

    int wm = w - 1;
    int hm = h - 1;
    int wh = w * h;
    int div = radius + radius + 1;

    int r[] = new int[wh];
    int g[] = new int[wh];
    int b[] = new int[wh];
    int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
    int vmin[] = new int[Math.max(w, h)];

    int divsum = (div + 1) >> 1;
    divsum *= divsum;
    int dv[] = new int[256 * divsum];
    for (i = 0; i < 256 * divsum; i++)
    {
      dv[i] = (i / divsum);
    }

    yw = yi = 0;

    int[][] stack = new int[div][3];
    int stackpointer;
    int stackstart;
    int[] sir;
    int rbs;
    int r1 = radius + 1;
    int routsum, goutsum, boutsum;
    int rinsum, ginsum, binsum;

    for (y = 0; y < h; y++)
    {
      rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
      for (i = -radius; i <= radius; i++)
      {
        p = pix[yi + Math.min(wm, Math.max(i, 0))];
        sir = stack[i + radius];
        sir[0] = (p & 0xff0000) >> 16;
        sir[1] = (p & 0x00ff00) >> 8;
        sir[2] = (p & 0x0000ff);
        rbs = r1 - Math.abs(i);
        rsum += sir[0] * rbs;
        gsum += sir[1] * rbs;
        bsum += sir[2] * rbs;
        if (i > 0)
        {
          rinsum += sir[0];
          ginsum += sir[1];
          binsum += sir[2];
        } else
        {
          routsum += sir[0];
          goutsum += sir[1];
          boutsum += sir[2];
        }
      }
      stackpointer = radius;

      for (x = 0; x < w; x++)
      {

        r[yi] = dv[rsum];
        g[yi] = dv[gsum];
        b[yi] = dv[bsum];

        rsum -= routsum;
        gsum -= goutsum;
        bsum -= boutsum;

        stackstart = stackpointer - radius + div;
        sir = stack[stackstart % div];

        routsum -= sir[0];
        goutsum -= sir[1];
        boutsum -= sir[2];

        if (y == 0)
        {
          vmin[x] = Math.min(x + radius + 1, wm);
        }
        p = pix[yw + vmin[x]];

        sir[0] = (p & 0xff0000) >> 16;
        sir[1] = (p & 0x00ff00) >> 8;
        sir[2] = (p & 0x0000ff);

        rinsum += sir[0];
        ginsum += sir[1];
        binsum += sir[2];

        rsum += rinsum;
        gsum += ginsum;
        bsum += binsum;

        stackpointer = (stackpointer + 1) % div;
        sir = stack[(stackpointer) % div];

        routsum += sir[0];
        goutsum += sir[1];
        boutsum += sir[2];

        rinsum -= sir[0];
        ginsum -= sir[1];
        binsum -= sir[2];

        yi++;
      }
      yw += w;
    }
    for (x = 0; x < w; x++)
    {
      rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
      yp = -radius * w;
      for (i = -radius; i <= radius; i++)
      {
        yi = Math.max(0, yp) + x;

        sir = stack[i + radius];

        sir[0] = r[yi];
        sir[1] = g[yi];
        sir[2] = b[yi];

        rbs = r1 - Math.abs(i);

        rsum += r[yi] * rbs;
        gsum += g[yi] * rbs;
        bsum += b[yi] * rbs;

        if (i > 0)
        {
          rinsum += sir[0];
          ginsum += sir[1];
          binsum += sir[2];
        } else
        {
          routsum += sir[0];
          goutsum += sir[1];
          boutsum += sir[2];
        }

        if (i < hm)
        {
          yp += w;
        }
      }
      yi = x;
      stackpointer = radius;
      for (y = 0; y < h; y++)
      {
        // Preserve alpha channel: ( 0xff000000 & pix[yi] )
        pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

        rsum -= routsum;
        gsum -= goutsum;
        bsum -= boutsum;

        stackstart = stackpointer - radius + div;
        sir = stack[stackstart % div];

        routsum -= sir[0];
        goutsum -= sir[1];
        boutsum -= sir[2];

        if (x == 0)
        {
          vmin[y] = Math.min(y + r1, hm) * w;
        }
        p = x + vmin[y];

        sir[0] = r[p];
        sir[1] = g[p];
        sir[2] = b[p];

        rinsum += sir[0];
        ginsum += sir[1];
        binsum += sir[2];

        rsum += rinsum;
        gsum += ginsum;
        bsum += binsum;

        stackpointer = (stackpointer + 1) % div;
        sir = stack[stackpointer];

        routsum += sir[0];
        goutsum += sir[1];
        boutsum += sir[2];

        rinsum -= sir[0];
        ginsum -= sir[1];
        binsum -= sir[2];

        yi += w;
      }
    }

    Log.e("pix", w + " " + h + " " + pix.length);
    bitmap.setPixels(pix, 0, w, 0, 0, w, h);
    return (bitmap);
  }


}
