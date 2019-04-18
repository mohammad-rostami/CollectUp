package com.collect_up.c_up.util;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by collect-up3 on 4/25/2016.
 */
public class MaterialColorPalette {
  private static List<String> mMaterialColors = new ArrayList();

  public MaterialColorPalette() {
  }

  public static int randomColor() {
    return Color.parseColor(randomColorString());
  }

  private static String randomColorString() {
    Random randomGenerator = new Random();
    int randomIndex = randomGenerator.nextInt(mMaterialColors.size());
    return (String) mMaterialColors.get(randomIndex);
  }

  public static int randomColor(String... exceptions) {
    boolean allowed = false;
    String color = null;

    while (true)
    {
      while (!allowed)
      {
        color = randomColorString();
        String[] arr$ = exceptions;
        int len$ = exceptions.length;

        for (int i$ = 0; i$ < len$; ++i$)
        {
          String exception = arr$[i$];
          if (exception.equals(color))
          {
            allowed = false;
            break;
          }

          allowed = true;
        }
      }

      return Color.parseColor(color);
    }
  }

  static
  {
    mMaterialColors.add("#f69988");
    mMaterialColors.add("#f36c60");
    mMaterialColors.add("#e84e40");
    mMaterialColors.add("#e51c23");
    mMaterialColors.add("#dd191d");
    mMaterialColors.add("#d01716");
    mMaterialColors.add("#c41411");
    mMaterialColors.add("#b0120a");
    mMaterialColors.add("#ff5177");
    mMaterialColors.add("#ff2d6f");
    mMaterialColors.add("#e00032");
    mMaterialColors.add("#f06292");
    mMaterialColors.add("#ec407a");
    mMaterialColors.add("#e91e63");
    mMaterialColors.add("#d81b60");
    mMaterialColors.add("#c2185b");
    mMaterialColors.add("#ad1457");
    mMaterialColors.add("#880e4f");
    mMaterialColors.add("#ff4081");
    mMaterialColors.add("#f50057");
    mMaterialColors.add("#c51162");
    mMaterialColors.add("#ba68c8");
    mMaterialColors.add("#ab47bc");
    mMaterialColors.add("#9c27b0");
    mMaterialColors.add("#8e24aa");
    mMaterialColors.add("#7b1fa2");
    mMaterialColors.add("#6a1b9a");
    mMaterialColors.add("#4a148c");
    mMaterialColors.add("#e040fb");
    mMaterialColors.add("#d500f9");
    mMaterialColors.add("#aa00ff");
    mMaterialColors.add("#9575cd");
    mMaterialColors.add("#7e57c2");
    mMaterialColors.add("#673ab7");
    mMaterialColors.add("#5e35b1");
    mMaterialColors.add("#512da8");
    mMaterialColors.add("#4527a0");
    mMaterialColors.add("#311b92");
    mMaterialColors.add("#7c4dff");
    mMaterialColors.add("#651fff");
    mMaterialColors.add("#6200ea");
    mMaterialColors.add("#5c6bc0");
    mMaterialColors.add("#3f51b5");
    mMaterialColors.add("#3949ab");
    mMaterialColors.add("#303f9f");
    mMaterialColors.add("#283593");
    mMaterialColors.add("#1a237e");
    mMaterialColors.add("#536dfe");
    mMaterialColors.add("#3d5afe");
    mMaterialColors.add("#304ffe");
    mMaterialColors.add("#5677fc");
    mMaterialColors.add("#4e6cef");
    mMaterialColors.add("#455ede");
    mMaterialColors.add("#3b50ce");
    mMaterialColors.add("#2a36b1");
    mMaterialColors.add("#6889ff");
    mMaterialColors.add("#4d73ff");
    mMaterialColors.add("#4d69ff");
    mMaterialColors.add("#0288d1");
    mMaterialColors.add("#0277bd");
    mMaterialColors.add("#00b0ff");
    mMaterialColors.add("#0091ea");
    mMaterialColors.add("#0097a7");
    mMaterialColors.add("#00838f");
    mMaterialColors.add("#006064");
    mMaterialColors.add("#26a69a");
    mMaterialColors.add("#009688");
    mMaterialColors.add("#00897b");
    mMaterialColors.add("#00796b");
    mMaterialColors.add("#00695c");
    mMaterialColors.add("#004d40");
    mMaterialColors.add("#42bd41");
    mMaterialColors.add("#2baf2b");
    mMaterialColors.add("#259b24");
    mMaterialColors.add("#0a8f08");
    mMaterialColors.add("#0a7e07");
    mMaterialColors.add("#056f00");
    mMaterialColors.add("#0d5302");
    mMaterialColors.add("#7cb342");
    mMaterialColors.add("#689f38");
    mMaterialColors.add("#558b2f");
    mMaterialColors.add("#33691e");
    mMaterialColors.add("#f9a825");
    mMaterialColors.add("#f57f17");
    mMaterialColors.add("#ffa000");
    mMaterialColors.add("#ff8f00");
    mMaterialColors.add("#ff6f00");
    mMaterialColors.add("#ffab00");
    mMaterialColors.add("#fb8c00");
    mMaterialColors.add("#f57c00");
    mMaterialColors.add("#ef6c00");
    mMaterialColors.add("#e65100");
    mMaterialColors.add("#ff9100");
    mMaterialColors.add("#ff6d00");
    mMaterialColors.add("#ff7043");
    mMaterialColors.add("#ff5722");
    mMaterialColors.add("#f4511e");
    mMaterialColors.add("#e64a19");
    mMaterialColors.add("#d84315");
    mMaterialColors.add("#bf360c");
    mMaterialColors.add("#ff6e40");
    mMaterialColors.add("#ff3d00");
  }
}
