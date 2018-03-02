package com.util;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.controller.WeixinApiCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
/***
  *1.定时执行，每天上午8:00~8:30点，执行一次。执行周期5分钟，执行日志放在      f_control表中
  *2.等级提升了，发送短信和模板消息。
 * @author SHICHUNXIANG
 *
 */
public class TimerUpdateGrade extends TimerTask {

	@Override
	public void run() {
		int hour=new Date().getHours();
		int minute=new Date().getMinutes();
		if(hour==8 && minute<=30 ){
			String str = "select count(*) 'count' from f_control where date_format(NOW(),'%Y-%m-%d')=date_format(cOpTime,'%Y-%m-%d') and cType='UpdateGrade'";
			Long count=Db.queryLong(str);
			if(count>0){
				return;//每天只执行一次
			}
			TimerLog.AddLog("UpdateGrade");
			UpdateGrade();
		}

	}

	public void UpdateGrade(){
		 WeixinApiCtrl.setApiConfig();
	     List<Record> list=	 Db.query("select grade,tMoneyMin,tMoneyMax from f_update_grade where state=1");
		 if(list.size()==0) return ;
		 
		 List<Record> listUpdate=  Db.find("select a.aid,b.grade 'gradeOld',c.grade 'gradeNew' "
		 		+ " from (select aid,sum(price/cycle*ocount) 'tMoney' from f_order where state in(1,2,3) and price>0 group by aid ORDER BY sum(price)) as a "
		 		+ " left join f_account  as b on a.aid=b.id "
		 		+ " left join f_update_grade c on a.tMoney>=c.tMoneyMin and a.tMoney<c.tMoneyMax "
		 		+ " where b.grade<c.grade");
		 for (Record record : listUpdate) {
			Record f_account=new Record();
			f_account.set("id", record.get("aid"));
			f_account.set("grade", record.get("gradeNew"));
			if(Db.update("f_account",f_account)==true){
				
				//发送客服消息
				String openId_tj = Db.queryStr("SELECT openid FROM f_account WHERE id = ?",record.get("aid"));
				String message = "恭喜您在花美美平台的用户等级提升到了"+record.get("gradeNew")+"星级！您将尊享到如下优惠：\n"
						         + "1.兑换商城的里所需的花籽数又减少了5粒；\n"
						         + "2.签到时有机会获得"+record.get("gradeNew")+"星级用户的专享大奖。\n"
						         + "用鲜花传递爱，花美美感谢您的一路相伴！";
				CustomServiceApi.sendText(openId_tj, message);
				
				
				
			}
			
			
			
			
			
		}
		 
	 }

}


 