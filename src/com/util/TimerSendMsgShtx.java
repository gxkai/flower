package com.util;

import java.util.TimerTask;

import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.CustomServiceApi.Articles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 提前一周抓取（即当天抓取t+7）有祝福语或者绑定的电话号码和收花人电话号码不一致的订单发送信息
 * 信息内容：“掐指一算，您在去年x月x日给xxx(收花人)赠送过鲜花，小美提醒您又即将到这今年的一天了，你懂的！” 
 * @author guxukai
 *
 */
public class TimerSendMsgShtx extends TimerTask {

	@Override
	public void run() {
		String str = "select count(*) 'count' from f_control where date_format(NOW(),'%Y-%m-%d')=date_format(cOpTime,'%Y-%m-%d') and cType='SendMsgShtx'";
		Long count=Db.queryLong(str);
		if(count>0){
			return;//每天只执行一次
		}
		// 绑定 apiConfig 到线程之上
		ApiConfig ac = new ApiConfig();
		// 配置微信 API 相关常量
		ac.setToken(PropKit.get("token"));
		ac.setAppId(PropKit.get("appId"));
		ac.setAppSecret(PropKit.get("appSecret"));
		ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
		ac.setEncodingAesKey(PropKit.get("encodingAesKey", "setting it in config file"));
		// 发送模板消息
		ApiConfigKit.setThreadLocalApiConfig(ac);		
		sendMsg();
		sendCustomMsg();
		TimerLog.AddLog("SendMsgShtx");
		
	}
	
	/**
	 * 发送手机短信
	 */
	public void sendMsg(){
		String str="SELECT b.openid, b.tel,a.aid cid,a.name,DATE_FORMAT(a.ctime,'%m月%d日') ctime  FROM f_order a LEFT JOIN f_account b ON a.aid = b.id  WHERE  (a.zhufu is NOT NULL  OR a.tel <> b.tel) AND a.state=3 AND  YEAR(a.ctime)= YEAR(date_sub(now(),interval 1 year)) AND  DATE_FORMAT(a.ctime,'%m月%d日')= DATE_FORMAT(timestampadd(day, 7, now()) ,'%m月%d日')";
		List<Record> list=Db.find(str);
		for (Record record : list) {
			if(StrKit.isBlank(record.getStr("tel"))==false){
				String content=String.format("那年的今天，您赠了一束鲜花给s%。时光绕过一圈又回来了，知道ta还好吗？来继续用鲜花将美好延续。",record.getStr("name"));
				try {
					int flag=SendMsgUtil.sendMsg(record.getStr("tel"), content);
					if(flag==1){       
                       Record f_warnlog=new Record();
                       f_warnlog.set("nCode", record.getInt("cid"));
                       f_warnlog.set("nType", "shtx_msg");
                       f_warnlog.set("nTime", new Date());
                       f_warnlog.set("nContent", "message");
                       f_warnlog.set("nRemark", "success");
                       Db.save("f_warnlog", f_warnlog);
					}else{
						Record f_warnlog=new Record();
	                       f_warnlog.set("nCode", record.getInt("cid"));
	                       f_warnlog.set("nType", "shtx_msg");
	                       f_warnlog.set("nTime", new Date());
	                       f_warnlog.set("nContent", "message");
	                       f_warnlog.set("nRemark", "fail");
	                       Db.save("f_warnlog", f_warnlog);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 发送客服消息
	 */
	public void sendCustomMsg(){
		
		String str="SELECT b.openid, b.tel,a.aid cid,a.name,DATE_FORMAT(a.ctime,'%m月%d日') ctime  FROM f_order a LEFT JOIN f_account b ON a.aid = b.id  WHERE  (a.zhufu is NOT NULL  OR a.tel <> b.tel) AND a.state=3 AND  YEAR(a.ctime)= YEAR(date_sub(now(),interval 1 year)) AND  DATE_FORMAT(a.ctime,'%m月%d日')= DATE_FORMAT(timestampadd(day, 7, now()) ,'%m月%d日')";
		List<Record> list=Db.find(str);
		for (Record record : list) {
			Articles article = new Articles();
	 		article.setTitle("温馨提醒");
	 		article.setDescription(String.format("那年的今天，您赠了一束鲜花给s%。时光绕过一圈又回来了，知道ta还好吗？来继续用鲜花将美好延续。",record.getStr("name")));
	 		article.setUrl(Constant.getHost + "/index");
	 	//	article.setPicurl(Constant.getHost + "/image/hp.jpg");
	 		List<Articles> listArt = new ArrayList<>();
	 		listArt.add(article);
	 		ApiResult ar = CustomServiceApi.sendNews(record.getStr("openid"), listArt);
	 		if(ar.getInt("errcode") == 0){
	 			Record f_warnlog=new Record();
                f_warnlog.set("nCode", record.getInt("cid"));
                f_warnlog.set("nType", "shtx_customMsg");
                f_warnlog.set("nTime", new Date());
                f_warnlog.set("nContent", "customMessage");
                f_warnlog.set("nRemark", "success");
                Db.save("f_warnlog", f_warnlog);
	 		}else{
	 			Record f_warnlog=new Record();
                f_warnlog.set("nCode", record.getInt("cid"));
                f_warnlog.set("nType", "shtx_customMsg");
                f_warnlog.set("nTime", new Date());
                f_warnlog.set("nContent", "customMessage");
                f_warnlog.set("nRemark", "fail"+ar.getErrorMsg());
                Db.save("f_warnlog", f_warnlog);
	 		}
		}
	}

}
