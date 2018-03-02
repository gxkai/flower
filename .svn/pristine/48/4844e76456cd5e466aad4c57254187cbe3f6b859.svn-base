package com.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.controller.WeixinApiCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.CustomServiceApi;

public class TimerTarget extends TimerTask {

	@Override
	public void run() {
		WeixinApiCtrl.setApiConfig();
		//找到没有关闭的，大于等于24小时的目标
		List<Record> rdList=Db.find("select a.id,wScore,aid,b.openid,wNumNew+wNumOld 'num' from f_target as a "
				+ " left join f_account as b on a.aid=b.id"
				+ " where isClose=0 and wScore>=15 ");
		
		for (Record rd : rdList) {
		    Record	rdPrize=Db.findFirst("select id,wScore,origin,contentId,pMsg,pName from f_prizeList "
		    		+ " where type=4 and wScore<=? ORDER BY wScore DESC LIMIT 1 ",rd.getInt("wScore"));
		    if(rdPrize!=null){
		    	
		    	//发客服消息
		    	String ss=rd.getLong("num").toString();
		    	String dd=rdPrize.getStr("pMsg");
		    	String msg=String.format("感谢你参与花美美“新年小目标活动”，"
		    			+ "有%s位好友为你做了见证,%s"
		    			+ ",你的小目标花美美已存档，从此每天叫醒你的不是闹钟，是小目标！", 
		    			ss,dd);
		    	if(rdPrize.getInt("origin")==2){
			    	//花籽
			    	sendSeed(rdPrize.getInt("contentId"), rd.getInt("aid"));
			    }else if(rdPrize.getInt("origin")==3){
			    	//花票
			    	sendCash(rdPrize.getInt("contentId"), rd.getInt("aid"));
			    }else if(rdPrize.getInt("origin")==4){
			    	//红包
			    	sendRedpackets(rdPrize.getInt("contentId"),rd.getInt("aid"),
			    			rdPrize.getStr("pMsg"));
			    }
		    	Db.update("update f_target set isClose=1,prizeId=? where id=? ",rdPrize.getInt("id"),rd.getInt("id"));
		        String openid=rd.getStr("openid");
				CustomServiceApi.sendText(openid, msg);
		    }else{
		    	Db.update("update f_target set isClose=1,prizeId=0 where id=? ",rd.getInt("id"));
		    	//发客服消息
		    	String msg="见证人不够，我默默努力";
				CustomServiceApi.sendText(rd.getStr("openid"), msg);
		    }
		    
		    
		}
	}

	/**
	 * 发放奖品花籽
	 * 
	 * @param num
	 * @param aid
	 */
	private void sendSeed(int num, int aid) {
		for (int i = 0; i < num; i++) {
			Record f_flowerseed = new Record();
			f_flowerseed.set("aid", aid);
			f_flowerseed.set("send", 1);
			f_flowerseed.set("type", "Prize");
			f_flowerseed.set("remarks", "中奖");
			f_flowerseed.set("ctime", new Date());
			f_flowerseed.set("state", 0);
			f_flowerseed.set("username", "system");
			Db.save("f_flowerseed", f_flowerseed);
		}
	}
	/**
	 * 发放奖品花票
	 * 
	 * @param cashthemeId
	 * @param aid
	 */
	private void sendCash(int cashthemeId, int aid) {
		List<Record> cashlist = Db
				.find("select a.ltime,b.id from f_cashtheme as a left join f_cashclassify as b on b.tid=a.id "
						+ " where   a.ltime>0 and b.state=0 and a.id=?", cashthemeId);
		if (cashlist.size() > 0) {
			for (Record cash : cashlist) {
				Record c = new Record();
				c.set("aid", aid);
				c.set("cid", cash.getInt("id"));
				c.set("code", "1111");
				Calendar now = Calendar.getInstance();
				c.set("time_a", now.getTime());
				now.add(Calendar.DAY_OF_MONTH, cash.getInt("ltime"));
				c.set("time_b", now.getTime());
				c.set("state", 1);
				c.set("origin", 1);
				Db.save("f_cash", c);
			}
			Db.update("update f_cashtheme set sendcount=sendcount+1 where id=?", cashthemeId);// 发送数量+1
			Db.update("update f_cashtheme set ltime=0 where id=? and maxcount<>0 and sendcount>=maxcount", cashthemeId);// 送完了修改状态

		}
	}
	/**
	 * 发放中奖红包
	 * 
	 * @param redId
	 * @param aid
	 * @param pMsg
	 */
	private void sendRedpackets(int redId, int aid, String pMsg) {
		Record rd = Db.findFirst("select id,pmoney from f_redpackets_pro where id=?", redId);
		// 红包单头
		Record f_redpackets = new Record();
		f_redpackets.set("type", 4);// 中奖红包
		f_redpackets.set("money", rd.getDouble("pmoney"));
		f_redpackets.set("quantity1", 1);
		f_redpackets.set("quantity2", 1);
		f_redpackets.set("msg", pMsg);
		f_redpackets.set("ctime", new Date());
		f_redpackets.set("stime", new Date());
		f_redpackets.set("state", 2);
		f_redpackets.set("aid", 1);
		Db.save("f_redpackets", f_redpackets);
		long nowID = f_redpackets.getLong("id");// 本来是想用事务处理，但是这个会报错，还没有想到好的解决方法
		// 红包单身
		Record f_redpackets_detial = new Record();
		f_redpackets_detial.set("rpid", nowID);
		f_redpackets_detial.set("rppid", rd.getInt("id"));
		f_redpackets_detial.set("isopen", 1);
		f_redpackets_detial.set("aid", aid);
		f_redpackets_detial.set("gtime", new Date());
		f_redpackets_detial.set("orderlist", "");
		Db.save("f_redpackets_detial", f_redpackets_detial);
	}
}
