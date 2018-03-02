package com.util;

import java.util.TimerTask;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import java.util.Date;
import java.util.List;
/**
 * 扣花籽，一天不签，倒扣一粒花籽; 每天早上1~3点执行一次
 * @author SHICHUNXIANG
 *
 */
public class TimerBuckleSeeds extends TimerTask {

	@Override
	public void run() {
		int hours=new Date().getHours();
		if(hours>=1&&hours<=3){
			String str = "select count(*) 'count' from f_control where date_format(NOW(),'%Y-%m-%d')=date_format(cOpTime,'%Y-%m-%d') and cType='BuckleSeeds'";
			Long count=Db.queryLong(str);
			if(count>0){
				return;//每天只执行一次
			}
			BuckleSeeds01();//扣花籽，一天不签，倒扣一粒花籽;
			//BuckleSeeds02();//2个月之前的花籽，自动失效；
			TimerLog.AddLog("BuckleSeeds");
		}
		
	}
	
	public void BuckleSeeds01(){
		//有哪些用户拥有【签到花籽】,2个月内的
		String str01 = "SELECT a.id from f_account as a left join f_flowerseed b on a.id=b.aid"
				+ " where isfans=0 and b.remarks='签到' and b.state=0 and  DATE_ADD(b.ctime, INTERVAL 2 MONTH) >= CURDATE()  group by a.id";
		List<Record> list=Db.find(str01);
		for (Record record : list) {
			String str02 = "select count(*) from f_flowerseed where aid=? and remarks='签到' and date_format(ctime,'%Y-%m-%d')=date_format(date_sub(NOW(),interval 1 day),'%Y-%m-%d')";
		    Long sum=Db.queryLong(str02,record.getInt("id"));
		    //如果昨天没有签到过，让一个【签到】类型的花籽失效
		    if(sum==0){
		    	//将被扣除（失效）的花籽
		    	String str03 = "select id from f_flowerseed where aid=? and remarks='签到' and state=0 and  DATE_ADD(ctime, INTERVAL 2 MONTH) >= CURDATE() order by  ctime LIMIT 1";
		        Record rd=Db.findFirst(str03,record.getInt("id"));
		        if(rd!=null){
		        	Db.update("update f_flowerseed set state=2 where id=?",rd.getInt("id"));
		        }
		    }
		}
		
		
	
	}

	/**
	 * 2个月之前的花籽，自动失效；
	 */
	public void BuckleSeeds02(){
		Db.update("update f_flowerseed set state=3 where DATE_ADD(ctime, INTERVAL 2 MONTH) < CURDATE() and state=0");
	}
	

}
