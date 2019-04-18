/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import android.content.Context;

import com.collect_up.c_up.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Contains helper static methods to work with times.
 */
public class TimeHelper {
  public static final int SECOND_MILLIS = 1000;
  public static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
  public static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
  public static final int DAY_MILLIS = 24 * HOUR_MILLIS;
  public static final int DAY_MILLI_SEC = 86400000;

  public static long utcToTimezone(Context context, String s) {
    int i = TimeZone.getDefault().getOffset(System.currentTimeMillis());
    return getDateFromServerDatePattern(context, s).getTime() + i;
  }

  public static String getServerStringDate(Date date) {
    return new SimpleDateFormat(Constants.General.SERVER_DATE_PATTERN.replace(" ", "'T'"), Locale.getDefault()).format(date);
  }

  public static String convertLongToServerDate(long l) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(l);

    return new SimpleDateFormat(Constants.General.SERVER_DATE_PATTERN.replace(" ", "'T'"), Locale.getDefault()).format(new Date(calendar.getTimeInMillis()));
  }

  public static Date getDateFromServerDatePattern(Context context, String s) {

    if(Utils.isNullOrEmpty(s) || context==null)
    {
      return new Date(System.currentTimeMillis());
    }
   if (!s.contains("."))
    {
      s += ".000";
    }
    try
    {
    String[] splitted = s.split("T");
    String[] beforeDot = splitted[1].split("\\.");
    if (beforeDot[1].contains("Z"))
    {
      beforeDot[1] = beforeDot[1].replace("Z", "");
    }
    if (beforeDot[1].length() > 3)
    {
      beforeDot[1] = String.valueOf(beforeDot[1].charAt(0)) + String.valueOf(beforeDot[1].charAt(1)) + String.valueOf(beforeDot[1].charAt(2));
    }
    String dateString = splitted[0] + " " + beforeDot[0] + "." + beforeDot[1];

      SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.General.SERVER_DATE_PATTERN, Locale.getDefault());
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      dateFormat.setCalendar(calendar);
       Date date = dateFormat.parse(dateString);
      return date;
    }

    catch (Exception e)
    {
      return new Date(System.currentTimeMillis());

    }


  }

  public static String getTimeAgo(Context context, long time) {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    long now = calendar.getTimeInMillis();
    if (time > now || time <= 0)
    {
      return context.getString(R.string.just_now);
    }

    final long diff = now - time;
    if (diff < MINUTE_MILLIS)
    {
      return context.getString(R.string.just_now);
    } else if (diff < 2 * MINUTE_MILLIS)
    {
      return "1m";
    } else if (diff < 50 * MINUTE_MILLIS)
    {
      return diff / MINUTE_MILLIS + "m";
    } else if (diff < 90 * MINUTE_MILLIS)
    {
      return "1h";
    } else if (diff < 24 * HOUR_MILLIS)
    {
      return diff / HOUR_MILLIS + "h";
    } else
    {
      return diff / DAY_MILLIS + "d";
    }
  }

  private static Calendar getYesterdayCalendar() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    return cal;
  }

  public static String getChatTimeAgo(Context context, Date comingDate, boolean shortStrings) {
    Calendar current = Calendar.getInstance();
    Calendar date = Calendar.getInstance();
    date.setTime(comingDate);

    long now = current.getTimeInMillis();
    long time = date.getTimeInMillis();

    if (time <= 0)
    {
      return null;
    }

    if ((time > now))
    {
      if (time - now > 10000)
      {
        return context.getString(R.string.just_now);
      }
    }

    final long diff = now - time;

    if (current.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH))
    {
      return String.format("%1$s:%2$s %3$s", date.get(Calendar.HOUR) == 0 && date.get(Calendar.AM_PM) == 1 ? 12 : date.get(Calendar.HOUR), date.get(Calendar
        .MINUTE), date.get(Calendar.AM_PM) == 0 ? context.getString(R.string.am) : context.getString(R.string.pm));
    } else if (date.get(Calendar.WEEK_OF_MONTH) == current.get(Calendar.WEEK_OF_MONTH))
    {
      if (getYesterdayCalendar().get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH))
      {
        return context.getString(R.string.yesterday);
      } else
      {
        return new SimpleDateFormat(shortStrings ? "EEE" : "EEEE", Locale.getDefault()).format(date.getTimeInMillis());
      }
    } else if (date.get(Calendar.MONTH) != current.get(Calendar.MONTH))
    {
      // Example: July 30 / Jul 30
      return String.format("%1$s %2$s", new SimpleDateFormat(shortStrings ? "MMM" : "MMMM", Locale.getDefault()).format(date.getTimeInMillis()), date.get(Calendar.DAY_OF_MONTH));
    } else
    {
      int i = (int) diff / DAY_MILLIS;
      return context.getResources().getQuantityString(R.plurals.days_ago, i, i);
    }
  }

  public static String makeLastSeen(Context context, Date comingDate) {
    Calendar current = Calendar.getInstance();
    Calendar date = Calendar.getInstance();
    date.setTime(comingDate);

    long now = current.getTimeInMillis();
    long time = date.getTimeInMillis();

    if (time <= 0)
    {
      return null;
    }

    if ((time > now))
    {
      if (time - now > 10000)
      {
        return context.getString(R.string.recently).toLowerCase();
      }
    }

    final long diff = now - time;

    if (diff < MINUTE_MILLIS)
    {
      return context.getString(R.string.recently).toLowerCase();
    } else if (diff < 2 * MINUTE_MILLIS)
    {
      return context.getString(R.string.a_minute_ago);
    } else if (diff < 50 * MINUTE_MILLIS)
    {
      return String.format(context.getString(R.string.x_minutes_ago), diff / MINUTE_MILLIS);
    } else if (diff < 90 * MINUTE_MILLIS)
    {
      return context.getString(R.string.an_hour_ago_2);
    } else if (diff < 24 * HOUR_MILLIS)
    {
      return String.format(context.getString(R.string.x_hours_ago), diff / HOUR_MILLIS);
    } else if (date.get(Calendar.YEAR) != current.get(Calendar.YEAR))
    {
      // Example: July 30, 2014 8:54 PM
      return String.format("%1$s %2$s, %3$s at %4$s:%5$s %6$s", new SimpleDateFormat("MMM", Locale.getDefault()).format(date.getTimeInMillis()), date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.YEAR), date.get(Calendar.HOUR) == 0 && date.get(Calendar.AM_PM) == 1 ? 12 : date.get(Calendar.HOUR), date.get(Calendar
        .MINUTE), date.get(Calendar.AM_PM) == 0 ? context.getString(R.string.am) : context.getString(R.string.pm));
    } else if (current.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH))
    {
      return String.format("at %1$s:%2$s %3$s", date.get(Calendar.HOUR) == 0 && date.get(Calendar.AM_PM) == 1 ? 12 : date.get(Calendar.HOUR), date.get(Calendar
        .MINUTE), date.get(Calendar.AM_PM) == 0 ? context.getString(R.string.am) : context.getString(R.string.pm));
    } else if (diff == DAY_MILLIS)
    {
      // yesterday at 5:84 PM
      return String.format("%1$s at %2$s:%3$s %4$s", context.getString(R.string.yesterday).toLowerCase(), date.get(Calendar.HOUR) == 0 && date.get(Calendar.AM_PM) == 1 ? 12 : date.get(Calendar.HOUR), date.get(Calendar
        .MINUTE), date.get(Calendar.AM_PM) == 0 ? context.getString(R.string.am) : context.getString(R.string.pm));
    } else
    {
      // Example: July 30, 8:54 PM
      return String.format("%1$s %2$s at %3$s:%4$s %5$s", new SimpleDateFormat("MMM", Locale.getDefault()).format(date.getTimeInMillis()), date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.HOUR) == 0 && date.get(Calendar.AM_PM) == 1 ? 12 : date.get(Calendar.HOUR), date.get(Calendar
        .MINUTE), date.get(Calendar.AM_PM) == 0 ? context.getString(R.string.am) : context.getString(R.string.pm));
    }
  }

  public static String getChatSettingsTimeAgo(Context context, Date comingDate) {
    Calendar current = Calendar.getInstance();
    Calendar date = Calendar.getInstance();
    date.setTime(comingDate);

    long now = current.getTimeInMillis();
    long time = date.getTimeInMillis();

    if (time <= 0)
    {
      return null;
    }

    if ((time > now))
    {
      if (time - now > 10000)
      {
        return String.format("%1$s %2$s", new SimpleDateFormat("MMMM", Locale.getDefault()).format(date.getTimeInMillis()), date.get(Calendar.DAY_OF_MONTH));
      }
    }

    String output;
    if (current.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH))
    {
      output = context.getString(R.string.today);
    } else if (getYesterdayCalendar().get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH))
    {
      output = context.getString(R.string.yesterday);
    } else
    {
      output = String.format("%1$s %2$s", new SimpleDateFormat("MMMM", Locale.getDefault()).format(date.getTimeInMillis()), date.get(Calendar.DAY_OF_MONTH));
    }

    return output;
  }
}
