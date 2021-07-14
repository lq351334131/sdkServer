package stream.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	/**
	 * 得到当前日期字符串 格式（yyyy-MM-dd）
	 */
	public static String getDate() {
		return getDate("yyyy-MM-dd");
	}

	/**
	 * 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
	 */
	public static String getDate(String pattern) {
		return DateFormatUtils.format(new Date(), pattern);
	}

	/**
	 * first_visit_day_time设置成当天的23:59:60，
	 */
	public static String getFirst() {
		return getDate() + " 23:59:60";
	}

	/**
	 * 时间相差秒
	 * 
	 * @return
	 */
	public static long getExpire(String strDate) {
		long time = getstrDate(strDate).getTime();
		long time2 = getFirst(strDate);
		long t = time2 - time;
		return t / 1000;
	}

	public static Date getstrDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date strtodate = formatter.parse(strDate);
			return strtodate;

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Long getTime(String s, String s1) {
		Date begTime = getstrDate(s1);
		Date afterTime = getstrDate(s);
		Long time = afterTime.getTime() - begTime.getTime();
		return time / 1000;

	}

	public static long getFirst(String strDate) {
		strDate = strDate.substring(0, 10);
		String endTime = strDate + " 23:59:60";
		Date date = getstrDate(endTime);
		return date.getTime();
	}

	public static Date toDate(long millSec) {
		return new Date(millSec);
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

	public static void main(String[] args) {
		//long i = 1582178182849L;
		//Long i=System.currentTimeMillis();
	//	Date date = toDate(i);
		Date day=new Date ();
		System.out.println(toDateString(day));

	}

	public static long getExpireLong(long time) {
		Date date = toDate(time);
		String dateString = toDateString(date);
		String endTimestr = dateString + " 23:59:60";
		Date endTime = getstrDate(endTimestr);
		long end = endTime.getTime() - time;
		return end / 1000;
	}

	// 日期转化为字符串形式
	public static String getString(Long time) {
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}
	public static String getIndex(Long time) {
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		return formatter.format(date);
	}


	public static String getIndexDate() {
		return DateFormatUtils.format(new Date(), "yyyyMMdd");
	}

}
