package com.util;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataCal {

	/**
	 * 增加天数
	 * @author Glacier
	 * @date 2017年9月27日
	 * @param dateStr 日期
	 * @param days 增加天数
	 * @return string yyyy-MM-dd
	 */
	public static String dataAdd(String dateStr,int days) {
		 try {    
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");    

            Calendar cd = Calendar.getInstance();    
            cd.setTime(sdf.parse(dateStr));    
            cd.add(Calendar.DATE, days);//增加一天    
            //cd.add(Calendar.MONTH, n);//增加一个月    

            return sdf.format(cd.getTime());    
   
        } catch (Exception e) {    
            return null;    
        }    
	}
	
	/**
	 * date -> string
	 * @author Glacier
	 * @date 2017年9月27日
	 * @param currentTime 需要转换的时间
	 * @return
	 */
	public static String dateToString(Date currentTime) {
		   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		   String dateString = formatter.format(currentTime);
		   return dateString;
	}
	
	/**
	 * string -> date
	 * @author Glacier
	 * @date 2017年9月27日
	 * @param strDate 需要转换的时间
	 * @param 
	 * @return
	 */
	public static Date strToDate(String strDate) {
		   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		   ParsePosition pos = new ParsePosition(0);
		   Date strtodate = formatter.parse(strDate, pos);
		   return strtodate;
		}
	
	/**
	 * 格式化当前时间
	 * @author Glacier
	 * @date 2017年9月27日
	 * @return
	 */
	public static Date getNowDate() {
		   Date currentTime = new Date();
		   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		   String dateString = formatter.format(currentTime);
		   ParsePosition pos = new ParsePosition(8);
		   Date currentTime_2 = formatter.parse(dateString, pos);
		   return currentTime_2;
		}
	
	
}
