package com.util;

import java.util.TimerTask;

import com.controller.WeixinApiCtrl;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;

import java.util.Date;
import java.util.List;

/**
 * 拼团成功后给每个团员发客服消息；
 * @author guxukai
 *
 */
public class TimerSendMsgPtcg extends TimerTask {

	@Override
	public void run() {
		TimerLog.AddLog("TimerSendMsgPtcg开始执行");
		// 绑定 apiConfig 到线程之上
		WeixinApiCtrl.setApiConfig();		
		sendMsg();
		sendCustomMsg();
		TimerLog.AddLog("TimerSendMsgPtcg执行结束");		
	}
	
	/**
	 * 发送手机短信
	 */
	public void sendMsg(){
		String str="select a.ptNo, c.id,c.tel  from f_pingtuan a left join f_pingtuan_detail b on a.ptNo = b.ptNo left join f_account c on b.aid = c.id where a.state = 3 and  not exists(select e.nCode,e.nType from f_warnlog e where e.nCode=c.id and e.nType='ptcgtx_msg' and e.nPtNo = a.ptNo)   ";
		List<Record> list=Db.find(str);
		for (Record record : list) {
			if(StrKit.isBlank(record.getStr("tel"))==false ){
				String content=String.format("恭喜您拼团成功,团号为%d",record.getInt("ptNo"));
				try {
					int flag=SendMsgUtil.sendMsg(record.getStr("tel"), content);
					if(flag==1){       
                       Record f_warnlog=new Record();
                       f_warnlog.set("nCode", record.getInt("id"));
                       f_warnlog.set("nType", "ptcgtx_msg");
                       f_warnlog.set("nTime", new Date());
                       f_warnlog.set("nContent", "message");
                       f_warnlog.set("nRemark", "success");
                       f_warnlog.set("nPtNo", record.getInt("ptNo"));	
                       Db.save("f_warnlog", f_warnlog);
					}else{
						Record f_warnlog=new Record();
	                       f_warnlog.set("nCode", record.getInt("id"));
	                       f_warnlog.set("nType", "ptcgtx_msg");
	                       f_warnlog.set("nTime", new Date());
	                       f_warnlog.set("nContent", "message");
	                       f_warnlog.set("nRemark", "fail");
	                       f_warnlog.set("nPtNo", record.getInt("ptNo"));	
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
		
		String str="select a.ptNo, c.id ,c.tel,c.openid  from f_pingtuan a left join f_pingtuan_detail b on a.ptNo = b.ptNo left join f_account c on a.aid = c.id where a.state = 3 and not exists(select e.nCode,e.nType from f_warnlog e where e.nCode=c.id and e.nType='ptcgtx_customMsg') ";
		List<Record> list=Db.find(str);
		for (Record record : list) {
		        String message = String.format("恭喜您拼团成功,团号为%d",record.getInt("ptNo"));
		        String url = "/index";
		        message += "\n\r<a href='"+Constant.getHost+url+"'>点击查看详情</a>";
		        ApiResult result= CustomServiceApi.sendText(record.getStr("openid"), message);
		        int errcode=result.getErrorCode();	
	            if (errcode==0){
	            	Record f_warnlog=new Record();
		            f_warnlog.set("nCode", record.getInt("id"));
		            f_warnlog.set("nType", "ptcgtx_customMsg");
		            f_warnlog.set("nTime", new Date());
		            f_warnlog.set("nContent", "customMessage");
		            f_warnlog.set("nRemark", "success");
		            f_warnlog.set("nPtNo", record.getInt("ptNo"));	
		            Db.save("f_warnlog", f_warnlog);
	            }else{
	            	Record f_warnlog = new Record();
					f_warnlog.set("nCode", record.getInt("id"));
					f_warnlog.set("nType", "ptcgtx_customMsg");
					f_warnlog.set("nTime", new Date());
					f_warnlog.set("nContent", "customMessage");
					f_warnlog.set("nRemark", "fail:"+result.getErrorMsg());
					f_warnlog.set("nPtNo", record.getInt("ptNo"));	
					Db.save("f_warnlog", f_warnlog);
	                
	            }
	 		
		}
	}

}
