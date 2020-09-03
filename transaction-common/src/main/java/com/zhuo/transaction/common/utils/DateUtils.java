package com.zhuo.transaction.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/03
 */
public class DateUtils {

    private static ThreadLocal<SimpleDateFormat> threadLocalYmd = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected synchronized SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private static ThreadLocal<SimpleDateFormat> threadLocalYmdHms = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected synchronized SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };


    /**
     * 获取当前时间后n个小时date
     * @param hour
     * @return
     */
    public static Date getNextHourDate(int hour){
        Date nowdate = new Date(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowdate);
        cal.add(Calendar.HOUR,hour);
        return cal.getTime();
    }

    /**
     * 获取当前时间前n个小时date
     * @param hour
     * @return
     */
    public static Date getPreHourDate(int hour){
        Date nowdate = new Date(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowdate);
        cal.add(Calendar.HOUR,Calendar.HOUR-hour);
        return cal.getTime();
    }

    /**
     *  时间格式化
     * @param date
     * @return
     */
    public static String formatYYYYMMDDHHMMSS(Date date){
        return threadLocalYmdHms.get().format(date);
    }
}
