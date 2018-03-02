package com.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class TaskService2 extends TimerTask{

//	每隔6小时 查询时候是否有  一个用户 给另一个用户送花        +  更新用户等级任务
	@Override
	public void run() {
		System.out.println("task2 执行-------------------------");
		
		// 更新送花信息表
		Date d = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String now = df.format(new Date(d.getTime()));
//		String beforeNow = df.format(new Date(d.getTime() - 1 * 6 * 60 * 60 * 1000));
		String beforeNow = "2016-11-01";  //查询所有订单
		List<Record> orderList = Db.find("SELECT price, ordercode, aid, tel, ctime FROM f_order WHERE state in(2,3) AND ctime Between '"+beforeNow+"' And '"+now+"';");
		
		for (int i = 0; i < orderList.size(); i++) {
			Record order = orderList.get(i);
			
			//更新用户等级
			/*double money = Db.queryDouble("SELECT SUM(price) FROM `f_order` WHERE  state IN(1,2,3) AND aid  = ?;",order.get("aid"));
			int  grade = ( (int)money / 500) + 1;
			Db.update("update f_account set grade = ? where id=?",grade,order.get("aid"));*/
			
			//送花信息表 插入数据
			Number num = Db.queryNumber("select count(1) from f_account where tel = ?", order.get("tel"));
			if (num.intValue() != 0) {
				int aid = Db.queryInt("SELECT id FROM f_account WHERE tel = ?",order.get("tel"));
				if (aid != order.getInt("aid")) {
					Number ex = Db.queryNumber("select count(1) from f_flower_received where ordercode = ?", order.get("ordercode"));

					if (ex.intValue()==0) {
						Record receive = new Record();
						receive.set("receiverId", aid);
						receive.set("giverId", order.getInt("aid"));
						receive.set("ordercode", order.getStr("ordercode"));
						Db.save("f_flower_received", receive);
					}
					
				}
			}
			
			
		}
	
	}
}
