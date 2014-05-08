package org.ds.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
	public static Date getDate(String dateTimeStr) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = null;
		try {
			date = formatter.parse(dateTimeStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static String getStringFromDate(Date dateObj) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateStr = null;
		dateStr = formatter.format(dateObj);
		return dateStr;
	}
	
	public static String getUserDisplayString(String epochString) {
		long epoch = Long.parseLong( epochString );
		Date dateObj = new Date( epoch);
		//System.out.println(DateUtil.getUserDisplayString(expiry));
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
		String dateStr = null;
		dateStr = formatter.format(dateObj);
		return dateStr;
	}
	
	
}
