package server.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author qi.li
 * @create 2020/11/10 15:36
 */
public class SdkDateUtils {

    public static Map<String,Integer> getWeekMonthYear(Long time){

        Map<String, Integer> mapInt = new LinkedHashMap<String, Integer>();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date(time);
        calendar.setTime(today);// 此处可换为具体某一时间
        int year = calendar.get(Calendar.YEAR);//获取年份
        int month = calendar.get(Calendar.MONTH)+1;//获取月份
        int quarter = (month+ 2) / 3;//获取季度
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);//获取一周中的第几天
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);//获取一月中的第几天
        int yearDay = calendar.get(Calendar.DAY_OF_YEAR);//获取一年中第几天
        if (weekDay == 1) {
            weekDay = 7;
        } else {
            weekDay = weekDay - 1;
        }
        mapInt.put("year",year);
        mapInt.put("month",month);
        mapInt.put("quarter",quarter);
        mapInt.put("weekDay", weekDay);
        mapInt.put("monthDay", monthDay);
        mapInt.put("yearDay", yearDay);
//        for (Map.Entry<String, Integer> entry : mapInt.entrySet()) {
//            System.out.println("今天是本" + entry.getKey() + "的第" + entry.getValue() + "天");
//        }
        return mapInt;
    }

    // 日期转化为字符串形式
    public static String toDateString(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
