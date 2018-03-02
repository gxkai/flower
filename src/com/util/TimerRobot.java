package com.util;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import com.controller.WeixinApiCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.TemplateData;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;

public class TimerRobot extends TimerTask {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		robot();
		//double11();
	}
	
	
	/**
	 * 双11花票叠加
	 */
	private void double11(){
		WeixinApiCtrl.setApiConfig();
		String url=Constant.getHost +"/account/mycash";
		List<Record> list=Db.find("select aid,SUM(moneyUpdate) 'sumMoney',openid,nick "
				+ " from  f_luckdraw_result as a left join f_account as b on a.aid=b.id where type=2 and isEnd=0 and luckTime>='2017-10-31 00:00:00'"
				+ " and luckTime<'2017-11-11 00:00:00'  group by aid");
		for (Record rd : list) {
			//生成花票分类
			Record f_cashclassify=new Record();
			f_cashclassify.set("tid", 43);
			f_cashclassify.set("money", rd.getDouble("sumMoney"));
			f_cashclassify.set("benefit", 0);
			f_cashclassify.set("state", 1);
			f_cashclassify.set("ptid", 0);
			f_cashclassify.set("onlyfirst", 0);
			Db.save("f_cashclassify", f_cashclassify);
            long nowID=f_cashclassify.getLong("id");//花票分类的ID
			//生成花票
            Record f_cash=new Record();
            f_cash.set("aid", rd.getInt("aid"));
            f_cash.set("cid", nowID);
            f_cash.set("code", "870110");
            f_cash.set("time_a", "2017-11-10");
            f_cash.set("time_b", "2017-11-11");
            f_cash.set("state", 1);//已激活未使用
            f_cash.set("aid_f", 0);
            f_cash.set("origin", 4);
            f_cash.set("pushid", 0);
            f_cash.set("uTime", new Date());
            Db.save("f_cash", f_cash);
            
            Db.update("update f_luckdraw_result set isEnd=1 where type=2 and  aid=?",rd.getInt("aid"));
            
			//发模板消息
            ApiResult ar = TemplateMsgApi.send(TemplateData.New()
    			    // 消息接收者
    			    .setTouser(rd.getStr("openid"))
    			    // 模板id
    			    .setTemplate_id(WeixinApiCtrl.gettemplateId("充值通知"))
    			    .setTopcolor("#eb414a")
    			    .setUrl(url)
    			    .add("first", "您好，双11叠加现金花票已累加后到账", "#333")
    			    .add("accountType", "账户名称", "#333")
    			    .add("account", rd.getStr("nick"), "#333")
    			    .add("amount", rd.getDouble("sumMoney").toString(), "#333")
    			    .add("result", "充值成功", "#333")
    			    .add("remark", "此花票仅限2017年双11当天使用,逾期作废,赶紧下单吧!花票仅限花美美平台消费,不提现,不找零", "#888")
    			    .build());
		}
		
		
		
		
	}
	
	
	/**
	 * 加入机器人
	 * f_flower_pro表中【是否支持机器人】，商品上架时设置
	 */
	private void robot(){
		List<Record> robots=Db.find("select id,nick from f_account where robotis=1");
		List<Record> list=Db.find("select ptNo,needCount-hadCount 'robotCount' from f_pingtuan as a left join f_flower_pro as b on a.fptid=b.id  where b.allowRobotis=1 and a.state=2 and NOW()>=ptTimeE  ");
		for (Record rd : list) {
			//加入机器人
			for(int i=0;i<rd.getLong("robotCount");i++){
			    int aid=getRandommain(robots);
				Record f_pingtuan_detail=new Record();
				f_pingtuan_detail.set("ptNo", rd.getInt("ptNo"));
				f_pingtuan_detail.set("aid", aid);
				f_pingtuan_detail.set("ctime", new Date());
				Db.save("f_pingtuan_detail", f_pingtuan_detail);
			}
			//拼团成功,修改单头
			Db.update("update f_pingtuan set hadCount=needCount,state=3 where ptNo=?",rd.getInt("ptNo"));
			List<Record> rds=Db.find("select ordercode,reach,cycle,sendCycle,type from f_order where state=6 and ptNo=?",rd.getInt("ptNo"));
			for (Record order : rds) {
				//修改状态、拼团成功时间
				Db.update("update f_order set state=1,ptSusTime=now() where ordercode=?", order.getStr("ordercode"));
				//生成批号
				List<String> piCodeList =DeliveryDateUtil.getPiCodeList(order.getInt("reach"), new Date(), order.getInt("sendCycle"), order.getInt("cycle"),order.getInt("type"),0,null);
				for(int i=0;i<piCodeList.size();i++){
					Record f_picode = new Record();
					f_picode.set("ordercode", order.getStr("ordercode"));
					f_picode.set("piCode", piCodeList.get(i));
					f_picode.set("reach", order.getInt("reach"));
					f_picode.set("num",i+1 );
					Db.save("f_picode", f_picode);
				}
			}
			/*SyHoliday("20171002",7,2);
			SyHoliday("20171007",7,2);*/
			sendPintunMsgToAll(rd.getInt("ptNo"));//拼团成功后，给所有团员发信息
		}
		
	}
	
	/**
	 * 拼团成功后，给团中所有人发信息
	 * @param ptNo
	 */
	public static void sendPintunMsgToAll(int ptNo){
		String url=Constant.getHost +"/account/groupDetail/"+ ptNo;
		WeixinApiCtrl.setApiConfig();
		List<Record> openIdlist=Db.find("select c.ptNo,d.name,b.openid,b.id from f_pingtuan_detail as a left join f_account as b on a.aid=b.id left join f_pingtuan as c on a.ptNo=c.ptNo"
				+ " left join f_flower_pro as d on d.id=c.fptid where b.robotis=0 and  a.ptNo=?",ptNo);
		for (Record rd : openIdlist) {
	    	Record order=Db.findFirst("select picode,a.ordercode from f_order a left join f_picode b "
	    			+ " on a.ordercode=b.ordercode where ptNo=? and  aid=? and num=1",rd.getInt("ptNo"),rd.getInt("id"));
	    	String firstDate=order.getStr("picode");
	    	firstDate=firstDate.substring(0, 4)+"-"+firstDate.substring(4, 6)+"-"+firstDate.substring(6,8);
			ApiResult result = TemplateMsgApi.send(TemplateData.New()
				    // 消息接收者
				    .setTouser(rd.getStr("openid"))
				    // 模板id
				    .setTemplate_id(WeixinApiCtrl.gettemplateId("拼团成功通知"))
				    .setTopcolor("#eb414a")
				    .setUrl(url)
				    .add("first", "拼团成功,准备收花", "#333")
				    .add("keyword1", order.getStr("ordercode"), "#333")
				    .add("keyword2", rd.getStr("name"), "#333")
				    .add("remark", "\n鲜花首次送达日期："+firstDate, "#888")
				    .build());
					 WeixinApiCtrl.sendTemplateMsg(result.getJson());
		}
	}
	
	/**
	 * 随机抓个机器人
	 * @param robots
	 * @return
	 */
	public static int getRandommain(List<Record> robots) {
		try {
			int max=robots.size();
	        Random random = new Random();
	        int index = random.nextInt(max);//nextInt(4)将产生0,1,2,3
	        Record aid=robots.get(index);
	        return aid.getInt("id");
		} catch (Exception e) {
			return 92;
		}
        
    }

}
