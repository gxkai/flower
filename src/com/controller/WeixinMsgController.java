package com.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dao.FCDao;
import com.jfinal.json.FastJson;
import com.jfinal.json.Json;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.CustomServiceApi.Articles;
import com.jfinal.weixin.sdk.api.MediaApi;
import com.jfinal.weixin.sdk.api.MediaApi.MediaType;
import com.jfinal.weixin.sdk.api.TemplateData;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;
import com.jfinal.weixin.sdk.jfinal.MsgControllerAdapter;
import com.jfinal.weixin.sdk.msg.in.InImageMsg;
import com.jfinal.weixin.sdk.msg.in.InLinkMsg;
import com.jfinal.weixin.sdk.msg.in.InLocationMsg;
import com.jfinal.weixin.sdk.msg.in.InShortVideoMsg;
import com.jfinal.weixin.sdk.msg.in.InTextMsg;
import com.jfinal.weixin.sdk.msg.in.InVideoMsg;
import com.jfinal.weixin.sdk.msg.in.InVoiceMsg;
import com.jfinal.weixin.sdk.msg.in.event.EventInMsg;
import com.jfinal.weixin.sdk.msg.in.event.InFollowEvent;
import com.jfinal.weixin.sdk.msg.in.event.InLocationEvent;
import com.jfinal.weixin.sdk.msg.in.event.InMenuEvent;
import com.jfinal.weixin.sdk.msg.in.event.InQrCodeEvent;
import com.jfinal.weixin.sdk.msg.in.event.InTemplateMsgEvent;
import com.jfinal.weixin.sdk.msg.out.OutCustomMsg;
import com.jfinal.weixin.sdk.msg.out.OutTextMsg;
import com.jfinal.weixin.sdk.utils.HttpUtils;
import com.util.Constant;
import com.util.DaiYanURL;
import com.util.DownloadFile;
import com.util.MD5Util;
import com.util.NewImageUtils;
import com.util.QRCodeUtil;

import netscape.javascript.JSObject;

/**
 * @Desc 关注消息
 * @author yeQing
 */
public class WeixinMsgController extends MsgControllerAdapter {

	static Log logger = Log.getLog(WeixinMsgController.class);
	

	
	@Override
	public ApiConfig getApiConfig() {
		ApiConfig ac = new ApiConfig();
        // 配置微信 API 相关常量
        ac.setToken(PropKit.get("token"));
        ac.setAppId(PropKit.get("appId"));
        ac.setAppSecret(PropKit.get("appSecret"));
        ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
        ac.setEncodingAesKey(PropKit.get("encodingAesKey", "setting it in config file"));
        return ac;
	}

	
	/**
	 * 实现父类抽方法，处理关注/取消关注消息
	 */
	protected void processInFollowEvent(InFollowEvent inFollowEvent){
		if (InFollowEvent.EVENT_INFOLLOW_SUBSCRIBE.equals(inFollowEvent.getEvent())){
			logger.debug("关注：" + inFollowEvent.getFromUserName());
			sendInfo2User(inFollowEvent, null, null);
		}
		// 如果为取消关注事件，将无法接收到传回的信息
		else if (InFollowEvent.EVENT_INFOLLOW_UNSUBSCRIBE.equals(inFollowEvent.getEvent())){
			logger.debug("取消关注：" + inFollowEvent.getFromUserName());
			Db.update("update f_account set isfans = 1,xtime=NOW() where openid = ?", inFollowEvent.getFromUserName());
		}
		renderNull();
	}

	@Override
	protected void processInQrCodeEvent(InQrCodeEvent inQrCodeEvent){
		
		if (InQrCodeEvent.EVENT_INQRCODE_SUBSCRIBE.equals(inQrCodeEvent.getEvent())){
			logger.debug("扫码未关注：" + inQrCodeEvent.getFromUserName());
			
			//切割字符串
			String[] eventArray = inQrCodeEvent.getEventKey().split("_");
			//获取二维码参数
			String typeId = eventArray[1];          //推荐类型
			String eventUserId = eventArray[2];     //推荐用户ID
			final String openId = inQrCodeEvent.getFromUserName();//扫描人的openid
			//添加用户数据进入推广action
			//添加成功完成关注公众号操作
			//调用函数处理，最好添加事务处理机制
			
			//发送消息给关注用户
			sendInfo2User(inQrCodeEvent, typeId, eventUserId);
			//type==3表示拼团
			if(typeId!=null&&typeId.equals("3")&&eventArray.length==4){
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String ptNo=eventArray[3];
				
				Record rd=Db.findFirst("select a.fptid,b.allownew from f_pingtuan as a LEFT JOIN f_flower_pro  as b on a.fptid=b.id where a.ptNo=?",ptNo);
				int isbuy=Db.queryInt("select isbuy from f_account where openid=?",inQrCodeEvent.getFromUserName());
				String msg =nick+"邀请您拼团" +"<a href='" + Constant.getHost +"/account/groupDetail/"+ ptNo+"-"+eventUserId + "'>【点击立即参团】</a>"+ptNo;
				//当该商品只允许新用户参团，并且当前是老用户时，只可以去开团
				if(rd.getInt("allownew")==1 && isbuy>0){
					msg =nick+"邀请您做团长" +"<a href='" + Constant.getHost +"/account/groupIndex/"+ rd.getInt("fptid") + "'>【点击立即开团】</a>"+ptNo;
				}
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
			}
			//type==4表示红包
			if(typeId!=null&&typeId.equals("4")&&eventArray.length==4){
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String redpacketcode=eventArray[3];
				String msg="您收到了来自"+nick+"的红包,"+"<a href='" + Constant.getHost +"/service/getRedpacket/"+ redpacketcode + "'>【请点击领取】</a>";
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
			}
			
			if (typeId !=null && typeId.equals("1")&&eventArray.length==4) {
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String memoryId=eventArray[2];
				String msg = "您可以"+"<a href='" + Constant.getHost +"/service/showMemory/"+ memoryId + "'>点击这里</a>"+"查看"+nick+"留下的悄悄话哦！";
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
			}
			
			if (typeId !=null && typeId.equals("41")&&eventArray.length==4) {
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String memoryId=eventArray[3];//单头的ID
				String type=typeId;
				String msg = "您可以"+"<a href='" + Constant.getHost +"/account/blessing_getcard/"+ memoryId+"-"+type + "'>点击这里</a>"+"查看"+nick+"赠送的礼品卡";
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
				
			}
			if (typeId !=null && typeId.equals("42")&&eventArray.length==4) {
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String memoryId=eventArray[3];//单身的ID
				String type=typeId;
				System.out.println(memoryId+"\t"+type);
				
				String msg = "您可以"+"<a href='" + Constant.getHost +"/account/blessing_getcard/"+ memoryId +"-"+type+ "'>点击这里</a>"+"查看"+nick+"赠送的礼品卡";
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
				
			}
			//满足指定人数，可购买拉新商品
			if(typeId !=null && typeId.equals("6")){
				//给扫码人发模板消息（服务状态提醒）
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				ApiResult result1 = TemplateMsgApi.send(TemplateData.New()
						// 消息接收者
						.setTouser(inQrCodeEvent.getFromUserName())
						// 模板id
						.setTemplate_id(WeixinApiCtrl.gettemplateId("服务状态提醒"))
						.setTopcolor("#eb414a")
						.setUrl(Constant.getHost + "/index")
						.add("first", "欢迎成为花美美的花粉", "#FF8040")
						.add("keyword1", "0元领取精美花剪", "#FF8040")
						.add("keyword2", "您通过"+nick+"的二维码成为我们的花粉", "#FF8040")
						.add("remark", "谢谢您对我们的支持", "#FF8040").build());
				//WeixinApiCtrl.sendTemplateMsg(result1.getJson());
				//给发海报人发模板消息（服务状态提醒）
				String nick1=Db.queryStr("select nick from f_account where openid=?",inQrCodeEvent.getFromUserName());
				String tjid="6_"+eventUserId;
				Long count=Db.queryLong("select count(1) from f_account where tjid=?  and id not in(select aid from f_redpackets_log)",tjid);
				String keyword2="已经完成"+count+"人,任务目标：10人;"
						        +nick1+"通过您的二维码成为我们的花粉";
				String remark="你还差"+(10-count)+"位小伙伴的支持，就可以获得 精美花剪 活动奖励，快快喊上你的亲朋好友来为你呐喊助威吧！";
				if(count==10){
					remark="您的任务已完成，赶紧点击详情兑换吧";
				}
				String openid2=Db.queryStr("select openid from f_account where id=?",eventUserId);
				ApiResult result2 = TemplateMsgApi.send(TemplateData.New()
						// 消息接收者
						.setTouser(openid2)
						// 模板id
						.setTemplate_id(WeixinApiCtrl.gettemplateId("服务状态提醒"))
						.setTopcolor("#eb414a")
						.setUrl(Constant.getHost + "/product/162-10")
						.add("first", "任务完成进度通知", "#FF8040")
						.add("keyword1", "0元领取精美花剪", "#FF8040")
						.add("keyword2", keyword2, "#FF8040")
						.add("remark", remark, "#FF8040").build());
				//WeixinApiCtrl.sendTemplateMsg(result2.getJson());
			}
			if(typeId!=null&&typeId.equals("7")&&eventArray.length==4){
				String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
				if(code_value.equals("1")){
					String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
					String targetId=eventArray[3];//目标id号
					int aid= Db.queryInt("select aid from f_target where id=?",targetId);
					int aid2=Db.queryInt("select id from f_account where openid=?",openId);//扫描人的id
					if(aid!=aid2){
						String msg="你好，新朋友，日后请多指教！\n"+"<a href='" + Constant.getHost +"/account/readyWitness/"+ targetId + "'>【戳我】</a>"+"给【"+nick+"】的小目标见证吧~有掌声也有奖励哦";
						CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
					}else{
						String msg="自己不可以给自己见证哦!";
						CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
					}
				}else{
					String msg="客官,来的不巧,小目标活动暂停了";
					CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
				}
				
			}
			
			
		}
		if (InQrCodeEvent.EVENT_INQRCODE_SCAN.equals(inQrCodeEvent.getEvent())){
			logger.debug("扫码已关注：" + inQrCodeEvent.getFromUserName());
			//切割字符串
			String[] eventArray = inQrCodeEvent.getEventKey().split("_");
			//获取二维码参数
			String typeId = eventArray[0];          //推荐类型
			String eventUserId = eventArray[1];     //推荐用户ID
			final String openId = inQrCodeEvent.getFromUserName();//扫描人的openid
			
			
//			System.err.println(typeId);
//			System.err.println(eventArray);
			
			//type==3表示拼团
			if(typeId!=null&&typeId.equals("3")&&eventArray.length==3){
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String ptNo=eventArray[2];
				
				Record rd=Db.findFirst("select a.fptid,b.allownew from f_pingtuan as a LEFT JOIN f_flower_pro  as b on a.fptid=b.id where a.ptNo=?",ptNo);
				int isbuy=Db.queryInt("select isbuy from f_account where openid=?",inQrCodeEvent.getFromUserName());
				String msg =nick+"邀请您参加拼团" +"<a href='" + Constant.getHost +"/account/groupDetail/"+ ptNo +"-"+eventUserId+ "'>【点击立即参团】</a>"+ptNo;
				//当该商品只允许新用户参团，并且当前是老用户时，只可以去开团
				if(rd.getInt("allownew")==1 && isbuy>0){
					boolean isin=(Db.queryLong("select count(1) from f_pingtuan_detail a left join f_account b on a.aid=b.id where ptNo=? and openid=?",ptNo,openId))>0?true:false;
					if(isin==true){
						msg ="您已参加过此团" +"<a href='" + Constant.getHost +"/account/groupDetail/"+ ptNo + "'>【点击查看详情】</a>"+ptNo;
					}else{
						msg =nick+"邀请您做团长" +"<a href='" + Constant.getHost +"/account/groupIndex/"+ rd.getInt("fptid") + "'>【点击立即开团】</a>"+ptNo;
					}
				}
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
			}
			//type==4表示红包
			else if(typeId!=null&&typeId.equals("4")&&eventArray.length==3){
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String redpacketcode=eventArray[2];
				String msg="您收到了来自"+nick+"的红包,"+"<a href='" + Constant.getHost +"/service/getRedpacket/"+ redpacketcode + "'>【请点击领取】</a>";
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);	
			}else if (typeId !=null && typeId.equals("1")&&eventArray.length==3) {
				//System.err.println("已经进入====================");
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String memoryId=eventArray[2];
				String msg = "您可以"+"<a href='" + Constant.getHost +"/service/showMemory/"+ memoryId + "'>点击这里</a>"+"查看"+nick+"留下的悄悄话哦！";
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
			}else if (typeId !=null && typeId.equals("41")&&eventArray.length==3) {
				
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String memoryId=eventArray[2];//单头ID
				String type=typeId;
				String msg = "您可以"+"<a href='" + Constant.getHost +"/account/blessing_getcard/"+ memoryId+"-"+type + "'>点击这里</a>"+"查看"+nick+"赠送的礼品卡";
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
			}else if (typeId !=null && typeId.equals("42")&&eventArray.length==3) {
	
				String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
				String memoryId=eventArray[2];//单身的ID
				String type=typeId;
				
				String msg = "您可以"+"<a href='" + Constant.getHost +"/account/blessing_getcard/"+ memoryId +"-"+type+ "'>点击这里</a>"+"查看"+nick+"赠送的礼品卡";
				CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
				
			}else if(typeId!=null&&typeId.equals("7")&&eventArray.length==3){
				String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
				if(code_value.equals("1")){
					String nick=Db.queryStr("select nick from f_account where id=?",eventUserId);
					String targetId=eventArray[2];//目标id号
					int aid= Db.queryInt("select aid from f_target where id=?",targetId);
					int aid2=Db.queryInt("select id from f_account where openid=?",openId);//扫描人的id
					if(aid!=aid2){
						String msg="你好，老朋友！\n"+"<a href='" + Constant.getHost +"/account/readyWitness/"+ targetId + "'>【戳我】</a>"+"给【"+nick+"】的小目标见证吧~有掌声也有奖励哦";
						CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
					}else{
						String msg="自己不可以给自己见证哦!";
						CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
					}
				}else{
					String msg="客官,来的不巧，小目标活动暂停了";
					CustomServiceApi.sendText(inQrCodeEvent.getFromUserName(), msg);
				}
				
				
			}
			else{
				sendActivityMsg(typeId,eventUserId,openId);//发送活动信息
			}
		}
		renderNull();
		
	}
	

	@Override
	protected void processInMenuEvent(InMenuEvent inMenuEvent) {
		String key = inMenuEvent.getEventKey();
		if("32".equalsIgnoreCase(key)){
			CustomServiceApi.sendText(inMenuEvent.getFromUserName(), "请输入“转接客服”");
		}
		renderNull();
	}
	
	@Override
	protected void processInTemplateMsgEvent(InTemplateMsgEvent inTemplateMsgEvent) {
		 String fromUserName = inTemplateMsgEvent.getFromUserName();
		 String msgId = inTemplateMsgEvent.getMsgId();
		 String status = inTemplateMsgEvent.getStatus();
		 System.out.println("购买成功通知：fromUserName: "+fromUserName+"msgId: "+msgId+"status: "+status);
		 renderNull();
	}

	/**
	 * 推送图片
	 * @author Glacier
	 * @date 2017年10月23日
	 */
	public void pushImg(final String redpacketcode) {
		
		new Thread() {
			
		public void run() {
		WeixinApiCtrl.setApiConfig();

		//获取图片链接
		Record card = Db.findFirst("SELECT a.id,a.type,a.money,a.msg,a.ctime,a.aid,a.picId,b.typeId,b.imgurl01,b.imgurl02 from f_redpackets a "
				+ " LEFT JOIN  f_redpackets_pic b on b.id = a.picId where a.id = ?",redpacketcode) ;
		
		Record user = Db.findById("f_account", card.getInt("aid"));
		String openid_f = user.getStr("openid");
		
		String destPath = "/QRImage/" + openid_f + "_1.jpg";
		File file =new File(destPath); 

			//生成二维码
			String url = DaiYanURL.getUrl(card.getInt("aid"), 41, redpacketcode);
			String outPath = "/QRImage/" + openid_f + "_1.jpg";
			String headIamge = Db.queryStr("SELECT headimg FROM f_account WHERE id = ?",user.getInt("id")) ;
			//String headIamge = Db.queryStr("SELECT headimg FROM f_account WHERE id = ?",user.getInt("id")) ;
			// 如果目录不存在 创建对应目录
			QRCodeUtil.mkdirs("/QRImage/card");
			DownloadFile.downUrlFile(headIamge, "/QRImage/card");

			// 产生 带中间logo 的二维码图片
			String logoPath = "/QRImage/card/0.jpg";
			try {
				QRCodeUtil.encode(url, logoPath, outPath, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String imageurl = card.getStr("imgurl02");
			
			// 二维码 添加背景 （对应目录 “/QRImage/” 下 有添加 invite.jpg 作为背景图片 ）
			try {
				float a = (float) 0.9;
				File bg = new File("/upload/flower" + imageurl);
				BufferedImage buffImg = NewImageUtils.watermark(bg, file, 580, 280, a);
				NewImageUtils.generateWaterFile(buffImg, outPath);
			} catch (Exception e) {
				System.err.println("背景图片异常啦啦啦啦！！！！！");
				e.printStackTrace();
			}
			
			// 客服消息 发送图片给用户
			ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
			CustomServiceApi.sendImage(openid_f, ar.getStr("media_id"));
		}
		}.start();
		
	}
	
	/**
	 * @Desc 接收文本消息
	 */
	@Override
	protected void processInTextMsg(InTextMsg inTextMsg) {
		String textMsg = inTextMsg.getContent().trim();
		String openid = inTextMsg.getFromUserName();
		
		int aid = Db.queryInt("select id from f_account where openid =?", openid);
		
		// 异步线程 变量
		final String openid_f = openid;
		final int aid_f = aid;
		
		//根据用户发送的消息自动回复
		OutTextMsg outTextMsg = new OutTextMsg(inTextMsg);
		if("转接客服".equalsIgnoreCase(textMsg)){
			if((new Date().getHours())>=9 && (new Date().getHours())<=18){
				CustomServiceApi.sendText(openid, "正在连接客服，请等待...");
			}else{
				CustomServiceApi.sendText(openid, "很抱歉，目前不在人工客服服务时间。您可以留言，上班后会给您及时回复，谢谢！");
			}
			
			outTextMsg.setContent("改地址或配送顺延，查看订单详情、物流信息、花票信息可以在菜单【为您服务->会员中心】里操作。\n您也可以发送以下关键词了解相关内容:\n【养护知识】了解鲜花养护知识\n【配送说明】了解配送说明\n【我要带颜】获得专属二维码\n【转接客服】进入人工客服模式(仅限9:00~18:00)\n其它时间您也可以留言，小美上班后会给您回复。");
			render(outTextMsg);
			
			//render(new OutCustomMsg(inTextMsg));
		}else if ("我要带颜".equalsIgnoreCase(textMsg) || "我要代言".equalsIgnoreCase(textMsg)) {
			
			
			// 异步 执行
			new Thread() {
				
				public void run() {
					/*  当前进程 沉睡5s
					 try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/

					WeixinApiCtrl.setApiConfig();
					
					String destPath = "/QRImage/" + openid_f + ".jpg";
					File file =new File(destPath); 
					if (file.exists()) {

						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						CustomServiceApi.sendImage(openid_f, ar.getStr("media_id"));

					} else {

						//String url = HttpKit.get(Constant.getHost + "/weixin/createQrCode/1-" + aid_f);
						String url = DaiYanURL.getUrl(aid_f, 1);
						String outPath = "/QRImage/" + openid_f + ".jpg";
						String headIamge = Db.queryStr("SELECT headimg FROM f_account WHERE id = ?", aid_f) ;

						// 如果目录不存在 创建对应目录
						QRCodeUtil.mkdirs("/QRImage/head");
						DownloadFile.downUrlFile(headIamge, "/QRImage/head/");

						// 产生 带中间logo 的二维码图片
					    String[] urlname = headIamge.split("/");  
		    	        int len = urlname.length-1;  
		    	        String uname = urlname[len]+".jpg";//获取文件名  
						String logoPath = "/QRImage/head/"+uname;
						try {
							QRCodeUtil.encode(url, logoPath, outPath, true);//Zxing from Usa
							//QRCodeUtil.encode(url, logoPath, outPath, true);//Qrcode from Japan
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	

						// 二维码 添加背景 （对应目录 “/QRImage/” 下 有添加 invite.jpg 作为背景图片 ）
						try {
							float a = (float) 0.9;
							File bg = new File("/QRImage/invite.jpg");
							BufferedImage buffImg = NewImageUtils.watermark(bg, file, 450, 450, a);
							NewImageUtils.generateWaterFile(buffImg, outPath);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						// 客服消息 发送图片给用户
						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						CustomServiceApi.sendImage(openid_f, ar.getStr("media_id"));

					}
					
				}
			}.start();
			renderNull();
		}else if ("花艺课".equalsIgnoreCase(textMsg)) {
			
			
			// 异步 执行
			new Thread() {
				
				public void run() {
					/*  当前进程 沉睡5s
					 try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/
					String nick = Db.queryStr("SELECT nick FROM f_account WHERE id = ?", aid_f);
					String message = "Hi,@"+nick+" 爱花的小仙女，终于等到你！\n\n"
					+"欢迎来到花美美，奉上一份见面礼《最全的鲜花养护方法资料包》，领取地址：<a href='http://pan.baidu.com/s/1pKASUkb'>http://pan.baidu.com/s/1pKASUkb</a>\n\n"
+"还有个不情之请：如果你还蛮喜欢鲜花，可否顺手转发下方海报到朋友圈或微信群？做鲜花不易，还请多多支持！\n\n"
+"PS:邀请好友关注，有机会领取299元线下首席花艺师亲授花艺课门票1张。后台回复“花艺课”可查看带颜实时排行。\n\n"
+"↓↓邀请卡正在生成中↓↓";
					String message2 = "转发图片来邀请好友吧，邀请数排名靠前的有奖品哦，排名详情"+"<a href='" + Constant.getHost +"/account/hykdy'>【请查看排行榜】</a>";
					WeixinApiCtrl.setApiConfig();
					String destPath = "/QRImage_HYK/" + openid_f + ".jpg";
					File file =new File(destPath); 
					if (file.exists()) {
						CustomServiceApi.sendText(openid_f, message);
						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						CustomServiceApi.sendImage(openid_f, ar.getStr("media_id"));
						CustomServiceApi.sendText(openid_f, message2);

					} else {

						//String url = HttpKit.get(Constant.getHost + "/weixin/createQrCode/5-" + aid_f);
						String url = DaiYanURL.getUrl(aid_f, 5);
						String outPath = "/QRImage_HYK/" + openid_f + ".jpg";
						String headIamge = Db.queryStr("SELECT headimg FROM f_account WHERE id = ?", aid_f) ;
						//String nick = Db.queryStr("SELECT nick FROM f_account WHERE id = ?", aid_f);

						// 如果目录不存在 创建对应目录
						QRCodeUtil.mkdirs("/QRImage_HYK/head");
						DownloadFile.downUrlFile(headIamge, "/QRImage_HYK/head/");
						String[] urlname = headIamge.split("/");  
		    	        int len = urlname.length-1;  
		    	        String uname = urlname[len]+".jpg";//获取文件名  
						File file_head =new File("/QRImage_HYK/head/"+uname); 
						// 产生 带中间logo 的二维码图片
						
						String logoPath = "/QRImage_HYK/logo.jpg";
						try {
							QRCodeUtil.encode(url, logoPath, outPath, true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// 二维码 添加背景 （对应目录 “/QRImage/” 下 有添加 invite.jpg 作为背景图片 ）
						try {
							float a = (float) 0.9;
							File bg = new File("/QRImage_HYK/invite.jpg");
							BufferedImage buffImg = NewImageUtils.watermarkHYK(bg, file,file_head, 170, 730, a,nick);
							NewImageUtils.generateWaterFile(buffImg, outPath);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						// 客服消息 发送图片、文字给用户
						CustomServiceApi.sendText(openid_f, message);
						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						CustomServiceApi.sendImage(openid_f, ar.getStr("media_id"));
						CustomServiceApi.sendText(openid_f, message2);
					}
					
				}
			}.start();
			renderNull();
		}else if ("我要花剪".equalsIgnoreCase(textMsg)) {
			
			
			// 异步 执行
			new Thread() {
				
				public void run() {
					/*  当前进程 沉睡5s
					 try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/
					String nick = Db.queryStr("SELECT nick FROM f_account WHERE id = ?", aid_f);
					String message="您的【花剪】领取海报已生成！\n\n"
					          +"完成以下任务即可免费兑换【精美花剪】一只\n"
							  +"（原价69.9元，现只需9.9元即可领取,每人限5把）\n\n"
					          +"1、把专属海报发送给好友，邀请好友扫描图中二维码并关注。\n"
							  +"2、扫码新关注好友达10人时，即可开放兑换通道。\n\n"
					          +"温馨提醒：本次活动限量200份，先到先得。邀请好友按照人数统计，同一好友无论关注几次都只记录一次。\n\n"
							  +"活动期限：12月11日至16日\n"
					          +"发货时间：12月18日统一发货";
					WeixinApiCtrl.setApiConfig();
					String destPath = "/QRImage_XYZL/" + openid_f + ".jpg";
					File file =new File(destPath); 
					if (file.exists()) {
						CustomServiceApi.sendText(openid_f, message);
						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						CustomServiceApi.sendImage(openid_f, ar.getStr("media_id"));
						

					} else {

						//String url = HttpKit.get(Constant.getHost + "/weixin/createQrCode/6-" + aid_f);
						String url = DaiYanURL.getUrl(aid_f, 6);
						String outPath = "/QRImage_XYZL/" + openid_f + ".jpg";
						String headIamge = Db.queryStr("SELECT headimg FROM f_account WHERE id = ?", aid_f) ;
						//String nick = Db.queryStr("SELECT nick FROM f_account WHERE id = ?", aid_f);

						// 如果目录不存在 创建对应目录
						QRCodeUtil.mkdirs("/QRImage_XYZL/head/");
						DownloadFile.downUrlFile(headIamge, "/QRImage_XYZL/head/");
						String[] urlname = headIamge.split("/");  
		    	        int len = urlname.length-1;  
		    	        String uname = urlname[len]+".jpg";//获取文件名  
						File file_head =new File("/QRImage_XYZL/head/"+uname); 
						// 产生 带中间logo 的二维码图片
						String logoPath = "/QRImage_XYZL/logo.jpg";
						try {
							QRCodeUtil.encode(url, logoPath, outPath, true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// 二维码 添加背景 （对应目录 “/QRImage/” 下 有添加 invite.jpg 作为背景图片 ）
						try {
							float a = (float) 0.9;
							File bg = new File("/QRImage_XYZL/invite.jpg");
							BufferedImage buffImg = NewImageUtils.watermarkHYK(bg, file,file_head, 260, 760, a,nick);
							NewImageUtils.generateWaterFile(buffImg, outPath);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						// 客服消息 发送图片、文字给用户
						CustomServiceApi.sendText(openid_f, message);
						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						CustomServiceApi.sendImage(openid_f, ar.getStr("media_id"));
						
					}
					
				}
			}.start();
			renderNull();
		}else {
			List<Record>zdhflist = Db.find("Select *from f_zdhf where type=0");
			WeixinApiCtrl.setApiConfig();
		    for(final Record list:zdhflist){
		    	if (list.getStr("sendText").equalsIgnoreCase(textMsg)) {
		    		Timestamp nowtime = new Timestamp(System.currentTimeMillis()); 
					if (nowtime.compareTo((Timestamp) list.get("expdate"))>0) {
						renderNull();
						return;
					}
					if (StrKit.notBlank(list.getStr("returnText"))) {
			    		String message = list.getStr("returnText");
						if (StrKit.notBlank(list.getStr("returnUrlname"))) {
							 message = list.getStr("returnText") +  "\n\r<a href='"+list.getStr("returnUrl")+"'>"+list.getStr("returnUrlname")+"</a>";
						}
						CustomServiceApi.sendText(openid_f, message);
					}
					
					if (StrKit.notBlank(list.getStr("returnPicture"))) {
						new Thread() {
							public void run() {
								WeixinApiCtrl.setApiConfig();
								File file =new File(Constant.imgpath+(list.getStr("returnPicture")).substring(7));
								ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
								CustomServiceApi.sendImage(openid_f, ar.getStr("media_id"));
							}		
						}.start();
					}
					
					if (StrKit.notBlank(list.getStr("returnCashtheme"))) {
						int cashNum = Db.queryNumber("select count(1) from f_cashtheme where name = ?", list.getStr("returnCashtheme")).intValue();
						if (cashNum>0) {
								int cashid = Db.queryInt("select id from f_cashtheme where name = ?", list.getStr("returnCashtheme"));
								int numN = Db.queryNumber("SELECT count(1) FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid = b.id WHERE a.origin = 6 AND a.aid =? and b.tid=?", aid, cashid).intValue();
								if(numN==0){
									Articles article = new Articles();
							 		article.setTitle("您的花票已送达，点击领取");
							 		article.setDescription("遇见你，生活美美！");
							 		
							 		article.setUrl(Constant.getHost + "/account/cashexact/"+cashid+"-"+MD5Util.getMd5str(cashid));
							 		article.setPicurl(Constant.getHost + "/image/hp.jpg");
							 		List<Articles> listA = new ArrayList<>();
							 		listA.add(article);
							 		ApiResult ar = CustomServiceApi.sendNews(openid, listA);
							 		if(ar.getInt("errcode") == 0){
							 			List<Record> cashlist = Db.find("SELECT a.id,a.ltime,a.etime,b.id FROM f_cashtheme a LEFT JOIN f_cashclassify b ON a.id=b.tid WHERE a.ltime>0 and b.state=0 and a.id=?", cashid);
							 			for(Record cash : cashlist){
								    		Record c = new Record();						    		
								    		c.set("aid", aid);
								    		c.set("cid", cash.getInt("id"));
								    		c.set("code", "5555");
								    		Calendar now = Calendar.getInstance();
								    		c.set("time_a", now.getTime());
								    		now.add(Calendar.DAY_OF_MONTH, cash.getInt("ltime"));
								    		Date newTime = now.getTime();
								    		if (now.getTime().compareTo(cash.getDate("etime"))>=0) {
								    			newTime = cash.getDate("etime");
								    		}	
								    		c.set("time_b", newTime);
								    		c.set("state", 0);
								    		c.set("pushid", 1);
								    		c.set("origin", 6);
								    		Db.save("f_cash", c);
								    	}
							 			Db.update("update f_cashtheme set sendcount=sendcount+1 where id=?",cashid);//发送数量+1
										Db.update("update f_cashtheme set ltime=0 where id=? and maxcount<>0 and sendcount>=maxcount",cashid);//送完了修改状态
							 		}
							 		renderNull();
								}else{
									outTextMsg.setContent("活动花票你已经领过了");
									render(outTextMsg);
								}
						}else {
							renderNull();
						}
						
					}
				}
		    }
		}
		
		
		/*else{
			//outTextMsg.setContent("端午节收花时间调整通知：5月29日（周一）收花时间调整为5月31日（周三）\n\n改地址或配送顺延，查看订单详情、物流信息、花票信息可以在菜单【为您服务->会员中心】里操作。\n您也可以发送以下关键词了解相关内容:\n【养护知识】了解鲜花养护知识\n【配送说明】了解配送说明\n【我要带颜】获得专属二维码\n【转接客服】进入人工客服模式(仅限9:00~18:00)\n其它时间您也可以留言，小美上班后会给您回复。");
			//outTextMsg.setContent("五一假期（4月29日~5月1日）收花时间调整如下：5月1日（周一）收花时间调整为5月2日（周二）\n\n改地址或配送顺延，查看订单详情、物流信息、花票信息可以在菜单【为您服务->会员中心】里操作。\n您也可以发送以下关键词了解相关内容:\n【养护知识】了解鲜花养护知识\n【配送说明】 了解配送说明\n【转接客服】进入人工客服模式(仅限9:00~18:00)，避免收到自动回复消息影响您的心情\n其它时间您也可以留言，小美上班后会给您回复。");
			//render(outTextMsg);
		}*/
		//renderNull();
		
	}
	
	
	
	// 接收图片消息事件
	@Override
	protected void processInImageMsg(InImageMsg inImageMsg) {
		render(new OutCustomMsg(inImageMsg));
	}
	
	// 接收语音消息事件
	@Override
	protected void processInVoiceMsg(InVoiceMsg inVoiceMsg) {
		render(new OutCustomMsg(inVoiceMsg));
	}
	
	// 接收视频消息事件
	@Override
	protected void processInVideoMsg(InVideoMsg inVideoMsg) {
		render(new OutCustomMsg(inVideoMsg));
	}
	
	// 接收地理位置消息事件
	protected void processInLocationMsg(InLocationMsg inLocationMsg) {
		render(new OutCustomMsg(inLocationMsg));
	}
	//定位
	protected void processInLocationEvent(InLocationEvent inLocationEvent){
		try {
			/*String url=String.format("http://api.map.baidu.com/geocoder/v2/?output=json&ak=3rUfpQ0dbXZaxsTX6gzjaa3VmPH5FEzQ&location=%s,%s", 
					inLocationEvent.getLatitude(),inLocationEvent.getLongitude());
			String jsonStr= HttpUtils.get(url);
			JSONObject object01 = JSON.parseObject(jsonStr);
			JSONObject object02 = JSON.parseObject(object01.getString("result"));
			JSONObject object03 = JSON.parseObject(object02.getString("addressComponent"));
			Db.update("update f_account set province=?,city=? where openid=?",
					object03.getString("province"),
					object03.getString("city"),
					inLocationEvent.getFromUserName());*/
			
			//System.out.println(object03.getString("province")+";"+object03.getString("city"));
			//发红包
			/*Number count=Db.queryNumber("select count(1) from f_fissionrp  as a left join f_account as b "
					+ " on a.byAid=b.id where b.openid=? and b.tjid like '1_%' ",inLocationEvent.getFromUserName());
			if(count.intValue()==0){
				
				//给代颜人发裂变红包，每人每天限发3个
				String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
				if(code_value.equals("1")){
					Record rd=Db.findFirst("select substring(tjid,3) 'tjid',id,nick from f_account where openid=?",inLocationEvent.getFromUserName());
					String openId_tj = Db.queryStr("SELECT openid FROM f_account WHERE id = ?",rd.getStr("tjid"));
					if(object03.getString("province").contains("江苏")
							||object03.getString("province").contains("浙江")
							||object03.getString("province").contains("上海")){
						String tMoney=Db.queryStr("select code_value from f_dictionary where code_key='tMoney'");//设置的总额度
						double tMoneySend=Db.queryDouble("select IFNULL(sum(getMoney),0)/100.0 from f_fissionRP");//已发的金额
						if(tMoneySend<Double.valueOf(tMoney).doubleValue()){
							boolean flag=com.util.SendCashRed.sendGroup("花美美", openId_tj,
									   "300", "3", "成为花美美带颜人,成功邀请江浙沪地区的好友即可领微信红包；每人每天限领3个", "新年微信红包活动", "备注信息");
							if(flag){
								Record f_fissionRP=new Record();
								f_fissionRP.set("geterAid", Integer.valueOf(rd.getStr("tjid")) );
								f_fissionRP.set("byAid", rd.getInt("id"));
								f_fissionRP.set("byProvice",object03.getString("province"));
								f_fissionRP.set("getMoney", 300);
								f_fissionRP.set("cTime", new Date());
								Db.save("f_fissionRP", f_fissionRP);
							}
						}else{
							WeixinApiCtrl.setApiConfig();
							String msg=Db.queryStr("select note from f_dictionary where code_key='tMoney'");
							CustomServiceApi.sendText(openId_tj, msg);//50万已经瓜分完毕
						}
					}else{
						WeixinApiCtrl.setApiConfig();
						String message = rd.getStr("nick")+"在微信中设置的地址不属于【江浙沪地区】,不发红包";
						CustomServiceApi.sendText(openId_tj, message);
					}
				}
			}*/
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		renderNull();
	}
	
	// 接收链接消息事件
	protected void processInLinkMsg(InLinkMsg inLinkMsg) {
		render(new OutCustomMsg(inLinkMsg));
	}
	
	// 接收小视频消息
	protected void processInShortVideoMsg(InShortVideoMsg inShortVideoMsg) {
		render(new OutCustomMsg(inShortVideoMsg));
	}
	
	/**
	 * @Desc 关注事件的消息反馈
	 * @author yetangtang
	 * @date 2016/11/09
	 * @param Event
	 * @param typeId 代颜类型 1个人代颜，2推广代颜
	 * @param eventUserId 代颜人的Id号
	 */
	private void sendInfo2User(EventInMsg Event, String typeId, String eventUserId) {
		final String openId = Event.getFromUserName();
		FCDao.setAccount(null, openId, typeId, eventUserId);//注册或启用用户
		
		sendMsgAll(openId);//发送通用信息
		//sendActivityPic(openId);//发送活动海报
		sendActivityMsg(typeId,eventUserId,openId);//发送活动消息
		
	}
	
	/**
	 * 发送通用信息
	 * @param openId
	 */
    private void sendMsgAll(String openId){
    	String message1 = "用鲜花传递爱！\n"+
				 "花美美，每周一束，品种随机\n"+
				 "订阅<a href='" + Constant.getHost + "/product/1'>【遇见花束】</a>，简约大方\n"+
				 "订阅<a href='" + Constant.getHost + "/product/2'>【倾心花束】</a>，丰富多彩\n"+
				 "订阅<a href='" + Constant.getHost + "/product/3'>【定制花束】</a>，如你所愿\n"+
				 "花田直采，花量大、花新鲜、花期长。\n"+
    	         "海不枯，石不烂，我们不取关~";
    	CustomServiceApi.sendText(openId, message1);
    	
    	
    	/*if( new Date().getYear()==2017&&new Date().getMonth()==11 
				&& new Date().getDay()>=7 && new Date().getDay()<=11){
    		String msg2="因为双11期间快递时效不能保障，11月13日收花的用户系统将自动顺延一周（无需人工操作）。并给当天发货用户赠送5粒花籽。";
    		CustomServiceApi.sendText(openId, msg2);
		}*/
    	
    	/*String message = "花美美， 用鲜花传递爱，100份七夕花束等你来约。\n"+
				 "<a href='" + Constant.getHost + "/festivalProduct/7'>【点击立即抢购】</a>";
		CustomServiceApi.sendText(openId, message);*/
    }
	
	/**
	 * 818活动消息
	 * @param typeId
	 * @param eventUserId
	 * @param openId
	 */
	public  void sendActivityMsg(String typeId,final String  eventUserId,final String openId){
		sendActivityPic(openId);
		if(typeId!=null&&typeId.equals("5")){
			// 异步 执行
						new Thread() {
							
							public void run() {
								/*  当前进程 沉睡5s
								 try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}*/
								String nick = Db.queryStr("SELECT nick FROM f_account WHERE openid = ?", openId);
								Integer UserId = Db.queryInt("SELECT id FROM f_account WHERE openid = ?", openId);
								String message = "Hi,@"+nick+" 爱花的小仙女，终于等到你！\n\n"
								+"欢迎来到花美美，奉上一份见面礼《最全的鲜花养护方法资料包》，领取地址：<a href='http://pan.baidu.com/s/1pKASUkb'>http://pan.baidu.com/s/1pKASUkb</a>\n\n"
+"还有个不情之请：如果你还蛮喜欢鲜花，可否顺手转发下方海报到朋友圈或微信群？做鲜花不易，还请多多支持！\n\n"
+"PS:邀请好友关注，有机会领取299元线下首席花艺师亲授花艺课门票1张。后台回复“花艺课”可查看带颜实时排行。\n\n"
+"↓↓邀请卡正在生成中↓↓";
								String message2 = "转发图片来邀请好友吧，邀请数排名靠前的有奖品哦，排名详情"+"<a href='" + Constant.getHost +"/account/hykdy'>【请查看排行榜】</a>";
								WeixinApiCtrl.setApiConfig();
								String destPath = "/QRImage_HYK/" + openId + ".jpg";
								File file =new File(destPath); 
								if (file.exists()) {
									CustomServiceApi.sendText(openId, message);
									ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
									CustomServiceApi.sendImage(openId, ar.getStr("media_id"));
									CustomServiceApi.sendText(openId, message2);

								} else {

									//String url = HttpKit.get(Constant.getHost + "/weixin/createQrCode/5-" + UserId);
									String url = DaiYanURL.getUrl(UserId, 5);
									String outPath = "/QRImage_HYK/" + openId + ".jpg";
									String headIamge = Db.queryStr("SELECT headimg FROM f_account WHERE id = ?", UserId);
									//String nick = Db.queryStr("SELECT nick FROM f_account WHERE id = ?", eventUserId);

									// 如果目录不存在 创建对应目录
									QRCodeUtil.mkdirs("/QRImage_HYK/head/");
									DownloadFile.downUrlFile(headIamge, "/QRImage_HYK/head/");
									File file_head =new File("/QRImage_HYK/head/0.jpg"); 
									// 产生 带中间logo 的二维码图片
									String logoPath = "/QRImage_HYK/logo.jpg";
									try {
										QRCodeUtil.encode(url, logoPath, outPath, true);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									// 二维码 添加背景 （对应目录 “/QRImage/” 下 有添加 invite.jpg 作为背景图片 ）
									try {
										float a = (float) 0.9;
										File bg = new File("/QRImage_HYK/invite.jpg");
										BufferedImage buffImg = NewImageUtils.watermarkHYK(bg, file,file_head, 170, 730, a,nick);
										NewImageUtils.generateWaterFile(buffImg, outPath);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									// 客服消息 发送图片、文字给用户
									CustomServiceApi.sendText(openId, message);
									ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
									CustomServiceApi.sendImage(openId, ar.getStr("media_id"));
									CustomServiceApi.sendText(openId, message2);
								}
								
							}
						}.start();
			
		}
		if(typeId!=null&&typeId.equals("6")){
			// 异步 执行
						new Thread() {
							
							public void run() {
								/*  当前进程 沉睡5s
								 try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}*/
								String nick = Db.queryStr("SELECT nick FROM f_account WHERE openid = ?", openId);
								Integer UserId = Db.queryInt("SELECT id FROM f_account WHERE openid = ?", openId);
								String message="您的【花剪】领取海报已生成！\n\n"
								          +"完成以下任务即可免费兑换【精美花剪】一只\n"
										  +"（原价69.9元，现只需9.9元即可领取,每人限5把）\n\n"
								          +"1、把专属海报发送给好友，邀请好友扫描图中二维码并关注。\n"
										  +"2、扫码新关注好友达10人时，即可开放兑换通道。\n\n"
								          +"温馨提醒：本次活动限量200份，先到先得。邀请好友按照人数统计，同一好友无论关注几次都只记录一次。\n\n"
										  +"活动期限：12月11日至16日\n"
								          +"发货时间：12月18日统一发货";
								WeixinApiCtrl.setApiConfig();
								String destPath = "/QRImage_XYZL/" + openId + ".jpg";
								File file =new File(destPath); 
								if (file.exists()) {
									CustomServiceApi.sendText(openId, message);
									ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
									CustomServiceApi.sendImage(openId, ar.getStr("media_id"));
								} else {

									//String url = HttpKit.get(Constant.getHost + "/weixin/createQrCode/6-" + UserId);
									String url = DaiYanURL.getUrl(UserId, 6);
									String outPath = "/QRImage_XYZL/" + openId + ".jpg";
									String headIamge = Db.queryStr("SELECT headimg FROM f_account WHERE id = ?", UserId);
									//String nick = Db.queryStr("SELECT nick FROM f_account WHERE id = ?", eventUserId);

									// 如果目录不存在 创建对应目录
									QRCodeUtil.mkdirs("/QRImage_XYZL/head/");
									DownloadFile.downUrlFile(headIamge, "/QRImage_XYZL/head/");
									File file_head =new File("/QRImage_XYZL/head/0.jpg"); 
									// 产生 带中间logo 的二维码图片
									String logoPath = "/QRImage_XYZL/logo.jpg";
									try {
										QRCodeUtil.encode(url, logoPath, outPath, true);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									// 二维码 添加背景 （对应目录 “/QRImage/” 下 有添加 invite.jpg 作为背景图片 ）
									try {
										float a = (float) 0.9;
										File bg = new File("/QRImage_XYZL/invite.jpg");
										BufferedImage buffImg = NewImageUtils.watermarkHYK(bg, file,file_head, 170, 730, a,nick);
										NewImageUtils.generateWaterFile(buffImg, outPath);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									// 客服消息 发送图片、文字给用户
									CustomServiceApi.sendText(openId, message);
									ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
									CustomServiceApi.sendImage(openId, ar.getStr("media_id"));
								}
								
							}
						}.start();
			
		}
		if (typeId!=null&&typeId.equals("2")==true) {
			List<Record>qrList=Db.find("select *from f_zdhf where type = 1");
			for( final Record qr:qrList ){
				if( eventUserId.equals(qr.getStr("sendText"))==true){
		    		Timestamp nowtime = new Timestamp(System.currentTimeMillis()); 
					if (nowtime.compareTo((Timestamp) qr.get("expdate"))>0) {
						return;
					}
					if (StrKit.notBlank(qr.getStr("returnText"))) {
			    		String message = qr.getStr("returnText");
			    		if (StrKit.notBlank(qr.getStr("returnUrlname"))) {
							 message = qr.getStr("returnText") +  "\n\r<a href='"+qr.getStr("returnUrl")+"'>"+qr.getStr("returnUrlname")+"</a>";
						}
						CustomServiceApi.sendText(openId, message);
					}
					
					if (StrKit.notBlank(qr.getStr("returnPicture"))) {
						new Thread() {
							public void run() {
								WeixinApiCtrl.setApiConfig();
								File file =new File(Constant.imgpath+(qr.getStr("returnPicture")).substring(7));
								ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
								CustomServiceApi.sendImage(openId, ar.getStr("media_id"));
							}		
						}.start();
					}
					
					if (StrKit.notBlank(qr.getStr("returnCashtheme"))) {
						int aid = Db.queryInt("select id from f_account where openid = ?", openId);
						int cashNum = Db.queryNumber("select count(1) from f_cashtheme where name = ?", qr.getStr("returnCashtheme")).intValue();
						if (cashNum>0) {
								int cashid = Db.queryInt("select id from f_cashtheme where name = ?", qr.getStr("returnCashtheme"));
								int numN = Db.queryNumber("SELECT count(1) FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid = b.id WHERE a.origin = 6 AND a.aid =? and b.tid=?", aid, cashid).intValue();
								if(numN==0){
									Articles article = new Articles();
							 		article.setTitle("您的花票已送达，点击领取");
							 		article.setDescription("遇见你，生活美美！");
							 		article.setUrl(Constant.getHost + "/account/cashexact/"+cashid+"-"+MD5Util.getMd5str(cashid));
							 		article.setPicurl(Constant.getHost + "/image/hp.jpg");
							 		List<Articles> listA = new ArrayList<>();
							 		listA.add(article);
							 		ApiResult ar = CustomServiceApi.sendNews(openId, listA);
							 		if(ar.getInt("errcode") == 0){
							 			List<Record> cashlist = Db.find("SELECT a.id,a.ltime,b.id FROM f_cashtheme a LEFT JOIN f_cashclassify b ON a.id=b.tid WHERE a.ltime>0 and b.state=0 and a.id=?", cashid);
							 			for(Record cash : cashlist){
								    		Record c = new Record();
								    		c.set("aid", aid);
								    		c.set("cid", cash.getInt("id"));
								    		c.set("code", "5555");
								    		Calendar now = Calendar.getInstance();
								    		c.set("time_a", now.getTime());
								    		now.add(Calendar.DAY_OF_MONTH, cash.getInt("ltime"));	
								    		c.set("time_b", now.getTime());
								    		c.set("state", 0);
								    		c.set("pushid", 1);
								    		c.set("origin", 6);
								    		Db.save("f_cash", c);
								    	}
							 			Db.update("update f_cashtheme set sendcount=sendcount+1 where id=?",cashid);//发送数量+1
										Db.update("update f_cashtheme set ltime=0 where id=? and maxcount<>0 and sendcount>=maxcount",cashid);//送完了修改状态
							 		}
								}else{
									CustomServiceApi.sendText(openId, "活动花票你已经领过了");
								}
						}
						
					}
				}
		   }
		}
	}
	
	/**
	 * 活动海报
	 * @param openId
	 */
	public void sendActivityPic(final String openId){
		//618活动图片
				new Thread() {
					public void run() {
						WeixinApiCtrl.setApiConfig();
						String imgurl=Db.queryStr("select SUBSTR(img FROM 8) 'img' from f_masklayer where layeruse=1 and state=1");
						if(imgurl!=null &&imgurl.equals("")==false){
							File bg = new File(Constant.imgpath+imgurl);
							 BufferedReader br;
							try {
								br = new BufferedReader(new FileReader(bg));
								String str;
						        try {
									while ((str = br.readLine()) != null) {
									    System.out.println(str);
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						        try {
									br.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						        
							ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, bg);
							CustomServiceApi.sendImage(openId, ar.getStr("media_id"));
						}
					}}.start();
	}
}

