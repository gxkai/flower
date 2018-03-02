package com.util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
/**
 * 定时发送红包，用户下单触发
 * 我要代颜：连续邀请3位好友,且好友成功下单,您将获得1束双品鲜花红包
 * @author SHICHUNXIANG
 *
 */
public class TimerSendRedPacket extends TimerTask {

	@Override
	public void run() {
		TimerLog.AddLog("TimerSendRedPacket开始执行");
		//7天未支付的红包，交易关闭
		Db.update("update f_redpackets set state=4 where  ctime<=DATE_SUB(now(),INTERVAL 7 DAY) and state=0");
		
		
		//618当天大促,推荐一个人下首单，便发一个红包
		Date date=new Date();
	    SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
	    if(sdf.format(date).equals("20170618")==true){
       	   Send618();
        }else{
       	   Send();
        }
		
        TimerLog.AddLog("TimerSendRedPacket执行结束");
	}
	
	
	public void Send(){
		//查询5分钟内下单的用户（触发）
        //针对下单用户，找到他推荐人(扫个人二维码)
        //统计推荐人名下成功购买首单的人数，如果>=3符合条件
        String str01="select substring(tjid,3) tjid from f_account "
        		+ " where id in(select aid from f_order where ctime>='2017-05-20' and ishas=0 and price>=0 and state in(1,2,3) "
        		+ " and aid not in(select aid from f_redpackets_log)) and tjid like '1_%' "
        		+ " group by tjid having count(*)>=3";
        
        List<Record>  list=Db.find(str01);
        //针对推荐人，发红包（1束双品鲜花）
        for (final Record record : list) {
        	/*Db.tx(new IAtom() {
				@Override
				public boolean run() throws SQLException {}
			});*/
        	

			String str02=" select aid,ordercode from f_order where ctime>='2017-05-20' and  ishas=0 and price>=0 and state in(1,2,3) "
					+ " and aid in(select id from f_account where substring(tjid,3)=?  "
					+ " and tjid like '1_%' and id not in(select aid from f_redpackets_log)) limit  3";
            //标记已经处理
			String orderlist="";
			List<Record> list02=Db.find(str02,record.getStr("tjid"));
            for (final Record record2 : list02) {
            	orderlist=orderlist+record2.getStr("ordercode")+",";
            	Record f_redpackets_log=new Record();
    			f_redpackets_log.set("aid", record2.getInt("aid"));
    			f_redpackets_log.set("optime", new Date());
    			Db.save("f_redpackets_log", f_redpackets_log);
    		}
            orderlist= orderlist.substring(0,orderlist.length()-1);//去掉末尾的逗号
            //发包
            //红包单头
            Record  f_redpackets=new Record();
            f_redpackets.set("type", 3);//代颜红包
            f_redpackets.set("money", 39.99);
            f_redpackets.set("quantity1", 1);
            f_redpackets.set("quantity2", 1);
            f_redpackets.set("msg", "您有3位好友首单购买成功");
            f_redpackets.set("ctime", new Date());
            f_redpackets.set("stime", new Date());
            f_redpackets.set("state", 2);
            f_redpackets.set("aid", 1);
            Db.save("f_redpackets", f_redpackets);
            long nowID=f_redpackets.getLong("id");//本来是想用事务处理，但是这个会报错，还没有想到好的解决方法
            //红包单身
            Record f_redpackets_detial=new Record();
            f_redpackets_detial.set("rpid", nowID);
            f_redpackets_detial.set("rppid", 1);
            f_redpackets_detial.set("isopen", 1);
            f_redpackets_detial.set("aid", record.getStr("tjid"));
            f_redpackets_detial.set("gtime", new Date());
            f_redpackets_detial.set("orderlist",orderlist);
            Db.save("f_redpackets_detial", f_redpackets_detial);
            
		
        	
		}
	}
	/**
	 * 618当天大促,推荐一个人下首单，便发一个红包
	 */
	public void Send618(){
		//查询5分钟内下单的用户（触发）
        //针对下单用户，找到他推荐人(扫个人二维码)
        //统计推荐人名下成功购买首单的人数，如果>=1符合条件
		  String str01="select substring(tjid,3) tjid from f_account "
	        		+ " where id in(select aid from f_order where ctime>='2017-06-18' and ishas=0 and price>=0 and state in(1,2,3) "
	        		+ " and aid not in(select aid from f_redpackets_log)) and tjid like '1_%' "
	        		+ " group by tjid having count(*)>=1";
        
        List<Record>  list=Db.find(str01);
        //针对推荐人，发红包（1束双品鲜花）
        for (final Record record : list) {
        	Db.tx(new IAtom() {
				@Override
				public boolean run() throws SQLException {
					String str02=" select aid from f_order where ctime>='2017-06-18' and  ishas=0 and price>=0 and state in(1,2,3) "
							+ " and aid in(select id from f_account where substring(tjid,3)=?  "
							+ " and tjid like '1_%' and id not in(select aid from f_redpackets_log)) limit  1";
		            //标记已经处理
					String orderlist="";
					List<Record> list02=Db.find(str02,record.getStr("tjid"));
		            for (final Record record2 : list02) {
		            	orderlist=record2.getStr("ordercode")+",";
		            	Record f_redpackets_log=new Record();
		    			f_redpackets_log.set("aid", record2.getInt("aid"));
		    			f_redpackets_log.set("optime", new Date());
		    			Db.save("f_redpackets_log", f_redpackets_log);
		    		}
		            orderlist= orderlist.substring(0,orderlist.length()-1);//去掉末尾的逗号
		            //发包
		            
		            
		            //红包单头
                    Record  f_redpackets=new Record();
                    f_redpackets.set("type", 1);
                    f_redpackets.set("money", 39.99);
                    f_redpackets.set("quantity1", 1);
                    f_redpackets.set("quantity2", 1);
                    f_redpackets.set("msg", "618大促,您有1位好友首单购买成功");
                    f_redpackets.set("ctime", new Date());
                    f_redpackets.set("stime", new Date());
                    f_redpackets.set("state", 2);
                    f_redpackets.set("aid", 1);
                    Db.save("f_redpackets", f_redpackets);
                    int nowID=f_redpackets.getInt("id");
                    //红包单身
                    Record f_redpackets_detial=new Record();
                    f_redpackets_detial.set("rpid", nowID);
                    f_redpackets_detial.set("rppid", 1);
                    f_redpackets_detial.set("isopen", 1);
                    f_redpackets_detial.set("aid", record.getStr("tjid"));
                    f_redpackets_detial.set("gtime", new Date());
                    f_redpackets_detial.set("orderlist",orderlist);
                    Db.save("f_redpackets_detial", f_redpackets_detial);
                    return true;
				}
			});
        	
		}
	}

}
