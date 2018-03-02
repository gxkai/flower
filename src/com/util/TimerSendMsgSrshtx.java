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
 * 根据“f_flower_received”这个表里的数据，抓取T+7天以后过生日的送花人。给收花人推送管家消息
 * 信息内容：“你的好友xxx(微信昵称),曾给你送过暖意满满的鲜花，7天后就是TA的生日了，用一束鲜花带去你对TA的生日祝福吧” 
 * @author guxukai
 *
 */
public class TimerSendMsgSrshtx extends TimerTask {

	@Override
	public void run() {
		String str = "select count(*) 'count' from f_control where date_format(NOW(),'%Y-%m-%d')=date_format(cOpTime,'%Y-%m-%d') and cType='SendMsgSrshtx'";
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
		TimerLog.AddLog("SendMsgSrshtx");
		
	}
	
	/**
	 * 发送手机短信
	 */
	public void sendMsg(){
		String str="SELECT a.receiverId cid,b.openid,b.tel,c.nick,DATE_FORMAT(c.birthday,'%m月%d日') birthday from f_flower_received a LEFT JOIN f_account b on a.receiverId = b.id LEFT JOIN f_account c ON a.giverId=c.id  WHERE a.receiverId<>a.giverId AND DATE_FORMAT(c.birthday,'%m月%d日')= DATE_FORMAT(timestampadd(day, 7, now()) ,'%m月%d日')";
		List<Record> list=Db.find(str);
		for (Record record : list) {
			if(StrKit.isBlank(record.getStr("tel"))==false ){
				String content=String.format("你的好友%s,曾给你送过暖意满满的鲜花，%s就是TA的生日了，用一束鲜花带去你对TA的生日祝福吧",record.getStr("nick"),record.getStr("birthday"));
				try {
					int flag=SendMsgUtil.sendMsg(record.getStr("tel"), content);
					if(flag==1){       
                       Record f_warnlog=new Record();
                       f_warnlog.set("nCode", record.getInt("cid"));
                       f_warnlog.set("nType", "srshtx_msg");
                       f_warnlog.set("nTime", new Date());
                       f_warnlog.set("nContent", "message");
                       f_warnlog.set("nRemark", "success");
                       Db.save("f_warnlog", f_warnlog);
					}else{
						Record f_warnlog=new Record();
	                       f_warnlog.set("nCode", record.getInt("cid"));
	                       f_warnlog.set("nType", "srshtx_msg");
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
		
		String str="SELECT a.receiverId cid,b.openid,b.tel,c.nick,DATE_FORMAT(c.birthday,'%m月%d日') birthday from f_flower_received a LEFT JOIN f_account b on a.receiverId = b.id LEFT JOIN f_account c ON a.giverId=c.id  WHERE a.receiverId<>a.giverId AND DATE_FORMAT(c.birthday,'%m月%d日')= DATE_FORMAT(timestampadd(day, 7, now()) ,'%m月%d日')";
		List<Record> list=Db.find(str);
		for (Record record : list) {
			Articles article = new Articles();
	 		article.setTitle("好友生日提醒");
	 		article.setDescription(String.format("你的好友%s,曾给你送过暖意满满的鲜花，%s就是TA的生日了，用一束鲜花带去你对TA的生日祝福吧",record.getStr("nick"),record.getStr("birthday")));
	 		article.setUrl(Constant.getHost + "/index");
	 	//	article.setPicurl(Constant.getHost + "/image/hp.jpg");
	 		List<Articles> listArt = new ArrayList<>();
	 		listArt.add(article);
	 		ApiResult ar = CustomServiceApi.sendNews(record.getStr("openid"), listArt);
	 		if(ar.getInt("errcode") == 0){
	 			Record f_warnlog=new Record();
                f_warnlog.set("nCode", record.getInt("cid"));
                f_warnlog.set("nType", "srshtx_customMsg");
                f_warnlog.set("nTime", new Date());
                f_warnlog.set("nContent", "customMessage");
                f_warnlog.set("nRemark", "success");
                Db.save("f_warnlog", f_warnlog);
	 		}else{
	 			Record f_warnlog=new Record();
                f_warnlog.set("nCode", record.getInt("cid"));
                f_warnlog.set("nType", "srshtx_customMsg");
                f_warnlog.set("nTime", new Date());
                f_warnlog.set("nContent", "customMessage");
                f_warnlog.set("nRemark", "fail"+ar.getErrorMsg());
                Db.save("f_warnlog", f_warnlog);
	 		}
		}
	}

}
