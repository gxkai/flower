package com.util;
import java.util.Date;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class TimerLog {

	public static void AddLog(String name){
		Record f_control=new Record();
		f_control.set("cType", name);
		f_control.set("cOpTime", new Date());
		Db.save("f_control", f_control);
	}
}
