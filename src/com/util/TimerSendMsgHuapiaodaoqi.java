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
 * 花票到期提醒,提前2天通知
 * @author SHICHUNXIANG
 *
 */
public class TimerSendMsgHuapiaodaoqi extends TimerTask {

	@Override
	public void run() {
		TimerLog.AddLog("TimerSendMsgHuapiaodaoqi开始执行");
		int hour=new Date().getHours();
		if(hour>=8 && hour<=12){
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
		}
		TimerLog.AddLog("TimerSendMsgHuapiaodaoqi执行结束");
		
	}
	
	/**
	 * 发送手机短信
	 */
	public void sendMsg(){
		String str="select b.id 'aid',b.nick,b.tel,b.openid,date_format(a.time_b,'%Y-%c-%d') 'time_b',count(*) 'count',CONVERT(ceil(TIMESTAMPDIFF(HOUR,NOW(),a.time_b)/24),SIGNED) 'days'"
				+ " from f_cash a left join f_account b on a.aid=b.id "
				+ " where DATE_ADD(NOW(),INTERVAL 2 day)>a.time_b "
				+ " and not exists(select e.nCode,e.nType from f_warnlog e where e.nCode=a.time_b and e.nAid = b.id and e.nType='hptx_msg' ) "
				+ " and a.state=1 and time_b>=NOW() group by b.id ,a.time_b ";
		List<Record> list=Db.find(str);
		for (Record record : list) {
			if(StrKit.notBlank(record.getStr("tel"))){
				String content=String.format("您有%d张花票,将于%d天后过期,请尽快使用或分享给微信好友,赠人玫瑰，手有余香！详情见花美美平台[会员中心->我的花票]", 
						                       record.getLong("count"),record.getLong("days"));
				try {
					int flag=SendMsgUtil.sendMsg(record.getStr("tel"), content);
					if(flag==1){       
                       Record f_warnlog=new Record();
                       f_warnlog.set("nCode", record.getStr("time_b"));
                       f_warnlog.set("nType", "hptx_msg");
                       f_warnlog.set("nTime", new Date());
                       f_warnlog.set("nContent", "message");
                       f_warnlog.set("nRemark", "success");
                       f_warnlog.set("nAid", record.getInt("aid"));
                       Db.save("f_warnlog", f_warnlog);
					}else{
						Record f_warnlog=new Record();
	                       f_warnlog.set("nCode", record.getStr("time_b"));
	                       f_warnlog.set("nType", "hptx_msg");
	                       f_warnlog.set("nTime", new Date());
	                       f_warnlog.set("nContent", "message");
	                       f_warnlog.set("nRemark", "fail");
	                       f_warnlog.set("nAid", record.getInt("aid"));
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
		
		String str="select b.id 'aid',b.nick,b.tel,b.openid,date_format(a.time_b,'%Y-%c-%d') 'time_b',count(*) 'count',CONVERT(ceil(TIMESTAMPDIFF(HOUR,NOW(),a.time_b)/24),SIGNED) 'days'"
				+ " from f_cash a left join f_account b on a.aid=b.id "
				+ " where DATE_ADD(NOW(),INTERVAL 2 day)>a.time_b "
				+ " and not exists(select e.nCode,e.nType from f_warnlog e where e.nCode=a.time_b and e.nAid = b.id and e.nType='hptx_customMsg' ) "
				+ " and a.state=1 and time_b>=NOW() group by b.id ,a.time_b ";
		List<Record> list=Db.find(str);
		for (Record record : list) {
			Articles article = new Articles();
	 		article.setTitle("花票即将到期提醒");
	 		article.setDescription(String.format("您有%d张花票,将于%d天后过期,请尽快使用或分享给微信好友,赠人玫瑰，手有余香!", 
                                                  record.getLong("count"),record.getLong("days")));
	 		article.setUrl(Constant.getHost + "/account/mycash");
	 		article.setPicurl(Constant.getHost + "/image/hp.jpg");
	 		List<Articles> listArt = new ArrayList<>();
	 		listArt.add(article);
	 		ApiResult ar = CustomServiceApi.sendNews(record.getStr("openid"), listArt);
	 		if(ar.getInt("errcode") == 0){
	 			Record f_warnlog=new Record();
                f_warnlog.set("nCode", record.getStr("time_b"));
                f_warnlog.set("nType", "hptx_customMsg");
                f_warnlog.set("nTime", new Date());
                f_warnlog.set("nContent", "customMessage");
                f_warnlog.set("nRemark", "success");
                f_warnlog.set("nAid", record.getInt("aid"));
                Db.save("f_warnlog", f_warnlog);
	 		}else{
	 			Record f_warnlog=new Record();
                f_warnlog.set("nCode", record.getStr("time_b"));
                f_warnlog.set("nType", "hptx_customMsg");
                f_warnlog.set("nTime", new Date());
                f_warnlog.set("nContent", "customMessage");
                f_warnlog.set("nRemark", "fail"+ar.getErrorMsg());
                f_warnlog.set("nAid", record.getInt("aid"));
                Db.save("f_warnlog", f_warnlog);
	 		}
		}
	}

}
