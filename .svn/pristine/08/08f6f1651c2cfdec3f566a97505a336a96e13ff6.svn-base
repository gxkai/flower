package com.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.bcel.internal.generic.NEW;



/**
 * 写入昨日粉丝统计
 * @author guxukai
 *
 */
public class TimerFans extends TimerTask {

	@Override
	public void run() {
		Number num = Db.queryNumber("select count(1) from f_fans_statics where DATE_SUB(CURDATE(), INTERVAL 1 DAY) =date(time)");
		if (num.intValue()==0) {
			Add();
		}	  
	}
	
	public static void Add() {
		Integer cfans = Db.queryNumber("select count(1) from f_account where DATE_SUB(CURDATE(), INTERVAL 1 DAY) =date(ctime) and isfans =0").intValue();
		Integer xfans = Db.queryNumber("select count(1) from f_account where DATE_SUB(CURDATE(), INTERVAL 1 DAY) =date(xtime) and isfans =1").intValue();
		Integer jfans = cfans - xfans;
		Integer totalfans = Db.queryNumber("select count(1) from f_account where isfans = 0").intValue();
		Record record = new Record();
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DATE,-1);
		Date time=cal.getTime();
		record.set("time", new SimpleDateFormat("yyyy/MM/dd").format(time));
		record.set("cfans", cfans);
		record.set("xfans", xfans);
		record.set("jfans", jfans);
		record.set("totalfans", totalfans);
		Db.save("f_fans_statics", record);
	}

}
