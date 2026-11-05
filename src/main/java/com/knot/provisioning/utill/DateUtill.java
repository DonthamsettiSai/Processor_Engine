package com.knot.provisioning.utill;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Lakshman
 *
 */

public class DateUtill {

	private static final String ENDDATE = "9999-12-31";

	static final String FORMAT = "yyyy-MM-dd";
	static final String FORMAT1 = "MM/dd/yyyy";

	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal
			.withInitial(() -> new SimpleDateFormat(FORMAT1));
	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT1 = ThreadLocal
			.withInitial(() -> new SimpleDateFormat(FORMAT));
	private static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	private static final ThreadLocal<SimpleDateFormat> cntrDateFormat = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmss"));
	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT);
	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT2 = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("dd-MM-yyyy"));
	private static final Logger logger = LogManager.getLogger(DateUtill.class);

	private DateUtill() {

	}

	public static Date getDefaultEndDate() {
		Date endDate = null;
		try {
			endDate = DATE_FORMAT1.get().parse(ENDDATE);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: getDefaultEndDate ", e);
		}
		return endDate;
	}

	public static String getDateFromString(String date) {
		String result = null;
		try {
			Date d = DATE_FORMAT.get().parse(date);
			result = DATE_FORMAT1.get().format(d);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: getDateFromString ", e);
		}
		return result;
	}

	public static Date convertStringToDate(String date) {
		Date val = null;
		try {
			val = DATE_FORMAT.get().parse(date);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: convertStringToDate {}", e.getMessage());
		}
		return val;
	}

	public static String dateToString(Date date) {
		String result = "";
		try {
			result = dateFormat.get().format(date);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: dateToString ", e);
		}
		return result;
	}

	public static String contractFileNameDate(Date date) {
		String result = "";
		try {
			result = cntrDateFormat.get().format(date);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: contractFileNameDate ", e);
		}
		return result;
	}

	public static String dateFormatToString(Date date) {
		String result = "";
		try {
			result = DATE_FORMAT.get().format(date);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: dateFormatToString ", e);
		}
		return result;
	}

	public static Date addMinutes(Date date, int period) {
		Date result = null;
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.MINUTE, period);
			result = calendar.getTime();
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: addMinutes", e);
		}
		return result;
	}

	public static Date getCurrentDate() {
		Date currentDate = null;
		try {
			String date = DATE_FORMAT1.get().format(new Date());
			currentDate = DATE_FORMAT1.get().parse(date);
		} catch (ParseException e) {
			logger.error("The Exception is DateUtill :: getCurrentDate ", e);
		}
		return currentDate;
	}

	public static Date getPreviousDate() {
		Date previous = null;
		try {
			String date = DATE_FORMAT1.get().format(new Date(new Date().getTime() - (1000 * 60 * 60 * 24)));
			previous = DATE_FORMAT1.get().parse(date);
		} catch (ParseException e) {
			logger.error("The Exception is DateUtill :: getPreviousDate ", e);
		}
		return previous;
	}

	public static String getPreviousDateString() {
		String date = "";
		try {
			date = DATE_FORMAT1.get().format(new Date(new Date().getTime() - (1000 * 60 * 60 * 24)));
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: getPreviousDateString ", e);
		}
		return date;
	}

	public static String getPreviousDateString(Date date) {
		String date1 = "";
		try {
			date1 = DATE_FORMAT1.get().format(new Date(date.getTime() - (1000 * 60 * 60 * 24)));
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: getPreviousDateString ", e);
		}
		return date1;
	}

	public static String dateToStringFormat(Date date) {
		String result = "";
		try {
			result = DATE_FORMAT1.get().format(date);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: dateToStringFormat ", e);
		}
		return result;
	}

	public static Date addDays(int period) {
		Date date = null;
		String result = null;
		try {
			DateFormat targetFormat = new SimpleDateFormat(FORMAT);
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, period);
			result = targetFormat.format(c.getTime());
			date = targetFormat.parse(result);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: Add No-of Days to Current Date ", e);
		}
		return date;
	}

	public static LocalDate getLocalCurrentDate() {
		return LocalDate.now();
	}

	public static LocalDate getLocalDefaultEndDate() {
		String date = ENDDATE;
		return LocalDate.parse(date, formatter);
	}

	public static LocalDateTime getCurrentDateTime() {
		return LocalDateTime.now();
	}

	public static LocalDate getLocalPreviousDate() {
		return LocalDate.now().minusDays(1);
	}

	public static LocalDateTime getLocalDateTime(String dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return LocalDateTime.parse(dateTime, formatter);
	}

	public static String getFormatEndDate() {
		Date endDate = null;
		String endDate1 = null;
		try {
			endDate = DATE_FORMAT1.get().parse(ENDDATE);
			endDate1 = DATE_FORMAT.get().format(endDate);
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: getFormatEndDate ", e);
		}
		return endDate1;
	}

	public static String getFormatPreviousDateString() {
		String date = "";
		try {
			date = DATE_FORMAT.get().format(new Date(new Date().getTime() - (1000 * 60 * 60 * 24)));
		} catch (Exception e) {
			logger.error("The Exception is DateUtill :: getFormatPreviousDateString ", e);
		}
		return date;
	}

	public static LocalDate getDateToLocalDate(Date date) {
		LocalDate dt = null;
		try {
			dt = LocalDate.parse(new SimpleDateFormat(FORMAT).format(date));
		} catch (Exception e) {
			logger.error("The Exception is DateUtils :: getLocalDateFromString ", e);
		}
		return dt;
	}

	public static LocalDateTime getDefaultEndDateTime() {
		return LocalDateTime.of(9999, 12, 31, 23, 59, 59);
	}

	public static String dateCustomFormatString(String date) {
		String result = null;
		try {

			java.util.Date d = DATE_FORMAT.get().parse(date);
			result = DATE_FORMAT2.get().format(d);
		} catch (Exception e) {
		}
		return result;
	}

}