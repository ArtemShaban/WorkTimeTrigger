package com.shaban.worktimetrigger.util;

import java.util.Calendar;

/**
 * Created by Artem on 19.03.2015.
 */
public class DateUtils
{
    public static long getTodayTimestamp()
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
