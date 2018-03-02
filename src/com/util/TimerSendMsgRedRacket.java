package com.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.controller.WeixinApiCtrl;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.TemplateData;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;

/**
 * 定时发送红包到账提醒
 * @author SHICHUNXIANG
 *
 */
public class TimerSendMsgRedRacket extends TimerTask {


	@Override
	public void run() {
		TimerLog.AddLog("TimerSendMsgRedRacket开始执行");
		//618当天大促,推荐一个人下首单，便发一个红包
		 Date date=new Date();
	     SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
	     if(sdf.format(date).equals("20170618")==true){
        	Send618();
        }else{
        	Send();
        }
	    TimerLog.AddLog("TimerSendMsgRedRacket执行结束");
	}
	
	public void Send(){
		String strSelect = "select a.id,b.nick,a.gtime,b.openid from f_redpackets_detial a "
				+ " left join f_account b on a.aid=b.id "
				+ " left join f_redpackets c on a.rpid=c.id "
				+ " where isopen=1 and oid is null and c.aid=1 and msg='您有3位好友首单购买成功' "
				+ " and  not exists(select e.nCode,e.nType from f_warnlog e where e.nCode=a.id and e.nType='hbtx_templateMsg')";
		
		List<Record> list=Db.find(strSelect);
		
	    // 绑定 apiConfig 到线程之上
	 	ApiConfig ac = new ApiConfig();
	 	// 配置微信 API 相关常量
	 	ac.setToken(PropKit.get("token"));
	 	ac.setAppId(PropKit.get("appId"));
	 	ac.setAppSecret(PropKit.get("appSecret"));
	 	ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
	 	ac.setEncodingAesKey(PropKit.get("encodingAesKey", "setting it in config file"));
	 	SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    for (Record record : list) {
			// 发送模板消息
			ApiConfigKit.setThreadLocalApiConfig(ac);
			ApiResult result = TemplateMsgApi.send(TemplateData.New()
					// 消息接收者
					.setTouser(record.getStr("openid"))
					// 模板id
					.setTemplate_id(WeixinApiCtrl.gettemplateId("到账通知"))
					.setTopcolor("#eb414a").setUrl( "http://www.flowermm.net/account/myRedPackets/2")
					.add("first","红包到账通知\n", "#333")//标题
					.add("keyword1", record.getStr("nick"), "#0000FF")//昵称
					.add("keyword2", "1", "#0000FF")//数量
					.add("keyword3", dateFormater.format(record.getDate("gtime")), "#0000FF")//时间
					.add("remark", "\n亲爱的，有三位好友通过你的带颜败家成功，小美给你呈上一个红包【点击查看】。今天，你最美！", "#FF8000")//备注
					.build());
			int errcode=result.getErrorCode();
			if (errcode==0){
				Record f_warnlog = new Record();
				f_warnlog.set("nCode", record.get("id"));
				f_warnlog.set("nType", "hbtx_templateMsg");
				f_warnlog.set("nTime", new Date());
				f_warnlog.set("nContent", "templateMessage");
				f_warnlog.set("nRemark", "success");
				Db.save("f_warnlog", f_warnlog);
            }
            else
            {
            	Record f_warnlog = new Record();
				f_warnlog.set("nCode", record.get("id"));
				f_warnlog.set("nType", "hbtx_templateMsg");
				f_warnlog.set("nTime", new Date());
				f_warnlog.set("nContent", "templateMessage");
				f_warnlog.set("nRemark", "fail:"+result.getErrorMsg());
				Db.save("f_warnlog", f_warnlog);
                
            }
			
		}
	}
	

	
	public void Send618(){
		String strSelect = "select a.id,b.nick,a.gtime,b.openid from f_redpackets_detial a "
				+ " left join f_account b on a.aid=b.id "
				+ " left join f_redpackets c on a.rpid=c.id "
				+ " where isopen=1 and oid is null and c.aid=1 and msg='618大促,您有1位好友首单购买成功' "
				+ " and  not exists(select e.nCode,e.nType from f_warnlog e where e.nCode=a.id and e.nType='hbtx_templateMsg')";
		
		List<Record> list=Db.find(strSelect);
		
	    // 绑定 apiConfig 到线程之上
	 	ApiConfig ac = new ApiConfig();
	 	// 配置微信 API 相关常量
	 	ac.setToken(PropKit.get("token"));
	 	ac.setAppId(PropKit.get("appId"));
	 	ac.setAppSecret(PropKit.get("appSecret"));
	 	ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
	 	ac.setEncodingAesKey(PropKit.get("encodingAesKey", "setting it in config file"));
	 	SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    for (Record record : list) {
			// 发送模板消息
			ApiConfigKit.setThreadLocalApiConfig(ac);
			ApiResult result = TemplateMsgApi.send(TemplateData.New()
					// 消息接收者
					.setTouser(record.getStr("openid"))
					// 模板id
					.setTemplate_id(WeixinApiCtrl.gettemplateId("到账通知"))
					.setTopcolor("#eb414a").setUrl( "http://www.flowermm.net/account/myRedPackets/2")
					.add("first","红包到账通知\n", "#333")//标题
					.add("keyword1", record.getStr("nick"), "#0000FF")//昵称
					.add("keyword2", "1", "#0000FF")//数量
					.add("keyword3", dateFormater.format(record.getDate("gtime")), "#0000FF")//时间
					.add("remark", "\n亲爱的，有1位好友通过你的带颜败家成功，小美给你呈上一个红包【点击查看】。今天，你最美！", "#FF8000")//备注
					.build());
			int errcode=result.getErrorCode();
			if (errcode==0){
				Record f_warnlog = new Record();
				f_warnlog.set("nCode", record.get("id"));
				f_warnlog.set("nType", "hbtx_templateMsg");
				f_warnlog.set("nTime", new Date());
				f_warnlog.set("nContent", "templateMessage");
				f_warnlog.set("nRemark", "success");
				Db.save("f_warnlog", f_warnlog);
            }
            else
            {
            	Record f_warnlog = new Record();
				f_warnlog.set("nCode", record.get("id"));
				f_warnlog.set("nType", "hbtx_templateMsg");
				f_warnlog.set("nTime", new Date());
				f_warnlog.set("nContent", "templateMessage");
				f_warnlog.set("nRemark", "fail:"+result.getErrorMsg());
				Db.save("f_warnlog", f_warnlog);
                
            }
			
		}
	}
}
