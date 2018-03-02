package com.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletInputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import com.dao.FCDao;
import com.dao.MCDao;
import com.google.gson.Gson;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.CustomServiceApi.Articles;
import com.jfinal.weixin.sdk.api.GroupsApi;
import com.jfinal.weixin.sdk.api.JsTicket;
import com.jfinal.weixin.sdk.api.JsTicketApi;
import com.jfinal.weixin.sdk.api.MediaApi;
import com.jfinal.weixin.sdk.api.MediaApi.MediaType;
import com.jfinal.weixin.sdk.api.JsTicketApi.JsApiType;
import com.jfinal.weixin.sdk.api.MenuApi;
import com.jfinal.weixin.sdk.api.MessageApi;
import com.jfinal.weixin.sdk.api.QrcodeApi;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jfinal.weixin.sdk.utils.HttpUtils;
import com.util.Constant;
import com.util.MD5;
import com.util.Sign;
import com.util.Signature;
import com.util.XMLParser;


/**
 * @Desc 微信设置
 * @author lxx
 * @date 2016/8/23
 * */
public class WeixinApiCtrl extends ApiController {
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
    
    // 创建自定义菜单
    public void createmenu() {
    	String path = Constant.getHost;
    	String jsonstr = "{" +
				"    \"button\": [" +
				"        {" +
				"            \"name\": \"鲜花订阅\"," +
				"            \"sub_button\": [" +
				"			 	{\"name\": \"倾心-59.99元|灵感混搭\",\"type\": \"view\",\"url\": \"" + path + "/product/2\"}," +
				"			 	{\"name\": \"遇见-39.99元|简约双搭\",\"type\": \"view\",\"url\": \"" + path + "/product/1\"}," +
				"			 	{\"name\": \"闪购花束|当季最爱\",\"type\": \"view\",\"url\": \"" + path + "/festivalProduct/4\"}," +
				"				{\"name\": \"定制购花|情有独钟\",\"type\": \"view\",\"url\": \"" + path + "/product/201\"}," +
				"				{\"name\": \"全部商品\",\"type\": \"view\",\"url\": \"" + path + "/index\"}]" +
				"        }," +
				"        {" +
                "            \"name\": \"小美秘密\"," +
				"            \"sub_button\": [" +
				"				{\"name\": \"主题花束\",\"type\": \"view\",\"url\": \"" + path + "/festivalProduct/5\"}," +
				"			 	{\"name\": \"我要送花\",\"type\": \"view\",\"url\": \"" + path + "/product/3\"}," +
                "				{\"name\": \"晒 晒 晒\",\"type\": \"view\",\"url\": \"http://www.myfans.cc/1b434a4e7d\"}," +
                "				{\"name\": \"养护|搭配\",\"type\": \"view\",\"url\": \"" + path + "/knowledge\"}," +
                "				{\"name\": \"我要带颜\",\"type\": \"view\",\"url\": \""+ path + "/account/invitefri\"}]" +
				"        }," +
				"        {" +
				"    		 \"name\": \"为您服务\"," +
				"    		 \"sub_button\": ["+
				"			 	{\"name\": \"会员中心\",\"type\": \"view\",\"url\": \"" + path + "/account/center\"}," +
				"			 	{\"name\": \"在线客服\",\"type\": \"click\",\"key\": \"32\"}," +
				"           	{\"name\": \"联系我们\",\"type\": \"view\",\"url\": \""+ path + "/contactus\"},"+
				"			 	{\"name\": \"物流查询\",\"type\": \"view\",\"url\": \"" + path + "/logistics_query\"}," +
				"			 	{\"name\": \"常见问题\",\"type\": \"view\",\"url\": \"" + path + "/question\"}]" +
				"        }" +
				"    ]" +
				"}";
    	ApiResult apiResult = MenuApi.createMenu(jsonstr);
    	renderText(apiResult.getJson());
    }
    
    // config接口注入权限验证配置
    public void configvalid(){
    	JsTicket jt = JsTicketApi.getTicket(JsApiType.jsapi);
    	Map<String, String> map = Sign.sign1(jt.getTicket(), getPara("url"));
    	renderJson(map);
    }
    
    // 微信支付异步通知回调地址
 	public void wxpayback() throws IOException, ParserConfigurationException, SAXException{
 		ServletInputStream in = getRequest().getInputStream();
		int size = getRequest().getContentLength();
		if(size>0){
			byte[] bdata = new byte[size];
			in.read(bdata);
			String xmlStr = new String(bdata, XMLParser.getCharacterEncoding(getRequest(), getResponse()));
			//记录日志
			/*Record f_payLog=new Record();
			f_payLog.set("xmlStr", xmlStr);
			f_payLog.set("postTime", new Date());
			Db.save("f_payLog",f_payLog);*/
			
			if(Signature.SignValidIsFromWeXin(xmlStr)){
				Map<String,Object> paramsMap = XMLParser.getMapFromXML(xmlStr);
				String return_code = paramsMap.get("return_code").toString();
				String out_trade_no = paramsMap.get("out_trade_no").toString();
				if ("SUCCESS".equals(return_code)) {
					
					// 红包 
					if (out_trade_no.length() == 8) {
						
						if(FCDao.paySuccess_RP(out_trade_no)){
							renderJson("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
						}else{
							renderJson("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[ERR]]></return_msg></xml>");
						}
						
					}
					//开发票，支付运费成功回调
					else if(out_trade_no.length() == 19) {
						if(MCDao.paySuccessFc(out_trade_no)){
							renderJson("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
						}else{
							renderJson("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[ERR]]></return_msg></xml>");
						}
					}
					else {
						// 订单
						if(MCDao.paySuccessNew(out_trade_no)){
							renderJson("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
						}else{
							renderJson("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[ERR]]></return_msg></xml>");
						}
						
					}
					
					
				}else{
					renderJson("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[ERR]]></return_msg></xml>");
				}
			}
		}
		// 不是来自微信的回调通知
		//renderJson("bitch");
 	}
 	// 花票推送页面
 	public void toPushCash(){
 		int id = getParaToInt();
 		ApiResult ar = GroupsApi.get();
 		setAttr("id", id);
 		setAttr("groupList", ar.getList("groups"));
 		render("/manage/iframe/benefit/cash_push.html");
 	}
 	// 花票推送
 	public void pushcash(){
 		int id = getParaToInt("id");
 		Integer gid = getParaToInt("gid");
 		Map<String, Object> jsonMap = new HashMap<>();
 		boolean result = false;
 		String msg = new String();
 		// 获取要推送的花票主题
 		Record cashtheme = Db.findFirst("select id,state from f_cashtheme where id=? and state=0 and curdate()<=etime", id);
 		if(cashtheme != null){
 			// 获取花票主题下的花票类型
 			List<Record> list = Db.find("select id from f_cashclassify where tid = ? and state=0", id);
 			if(list.size() > 0){
 				if(MCDao.pushvalid()){
	 				String media_id = "ABmIJqX6-ZFJ4BE4zjZ0H1cYbi5HiAk_YsxyAzVpkwY";	// 素材ID  ABmIJqX6-ZFJ4BE4zjZ0H1cYbi5HiAk_YsxyAzVpkwY
	 				String jsonStr = new String();
	 				if(gid == null){
		 				jsonStr = "{" +
		 		 				"	\"filter\":{\"is_to_all\":true},"+
		 		 				"	\"mpnews\":{\"media_id\":\"" + media_id + "\"},"+
		 		 				"	\"msgtype\":\"mpnews\"}";
	 				}else{
	 					jsonStr = "{" +
		 		 				"	\"filter\":{\"is_to_all\":false,\"tag_id\":\"" + gid + "\"},"+
		 		 				"	\"mpnews\":{\"media_id\":\"" + media_id + "\"},"+
		 		 				"	\"msgtype\":\"mpnews\"}";
	 				}
	 		 		ApiResult apiResult = MessageApi.sendAll(jsonStr);
	 		 		if(apiResult.getInt("errcode")==0){
	 		 			cashtheme.set("state", 1);
	 		 			cashtheme.set("ptime", new Date());
	 		 			Db.update("f_cashtheme", cashtheme);
	 		 			result = true;
	 		 			msg = "推送提交成功";
	 		 		}else{
 		 				msg = "推送提交失败";
 		 			}
 				}else{
 					msg = "每月最多推送4次";
 				}
 			}else{
 				msg = "请录入花票类型";
 			}
 		}else{
 			msg = "花票主题不存在";
 		}
 		jsonMap.put("result", result);
 		jsonMap.put("msg", msg);
 		renderJson(jsonMap);
 	}
 	// 花票精确投送
 	public void pushcashexact(){
 		
 		int id = getParaToInt(0);
 		int themeId = getParaToInt(1);
 		
 		Map<String, Object> map = new HashMap<>();
 		boolean result = false;
 		String msg = new String();
 		
 		
 		Record admin = getSessionAttr("admin");
 		String openid = Db.queryStr("select openid from f_account where id = ?", id);
 		List<Record> cashlist = Db.find("SELECT a.ltime,b.id FROM f_cashtheme a LEFT JOIN f_cashclassify b ON a.id=b.tid WHERE a.ltime>0 and b.state=0 and a.tpid=4");
 		
 		String msgUrl = Constant.getHost + "/account/cashexact/"+themeId+"-"+getMd5str(themeId);
 		if(cashlist.size() > 0){
 			Articles article = new Articles();
 			article.setTitle("您有新的花票需要领取");
 			article.setDescription("遇见你，生活美美！");
 			article.setUrl(msgUrl);
 			article.setPicurl(Constant.getHost + "/image/hp.jpg");
 			List<Articles> list = new ArrayList<>();
 			list.add(article);
 			ApiResult ar = CustomServiceApi.sendNews(openid, list);
 			if(ar.getInt("errcode") == 0){
 				// 精确投放花票
 				for(Record cash : cashlist){
 					Record c = new Record();
 					c.set("aid", id);
 					c.set("cid", cash.getInt("id"));
 					c.set("code", "4444");
 					Calendar now = Calendar.getInstance();
 					c.set("time_a", now.getTime());
 					now.add(Calendar.DAY_OF_MONTH, cash.getInt("ltime"));
 					c.set("time_b", now.getTime());
 					c.set("state", 0);
 					c.set("pushid", admin.getInt("id"));
 					c.set("origin", 4);
 					Db.save("f_cash", c);
 				}
 				Db.update("update f_cashtheme set sendcount=sendcount+1 where id=?",themeId);//发送数量+1
				Db.update("update f_cashtheme set ltime=0 where id=? and maxcount<>0 and sendcount>=maxcount",themeId);//送完了修改状态
 				result = true;
 				msg = "操作成功";
 			}else{
 				msg = "操作失败";
 			}
 		}else{
 			msg = "当前主题花票下没有花票！";
 		}
 		map.put("result", result);
 		map.put("msg", msg);
 		renderJson(map);
 	}
 	/**
	 * 获取MD5加密字符串
	 * @param themeId
	 * @return
	 */
	private String getMd5str(int themeId){ 
		String original =String.valueOf(themeId);
		String originalMd5 = MD5.MD5Encode(original);
		String selectMd5 =  originalMd5.substring(3, 15);
		
		char resultArr[] = {'0','0','0','0','0','0','0','0','0','0','0','0'};//初始化
		String dict1 = "1234567890qwertyuiopasdfghjklzxcvbnm";
		String dict2 = "098765432109876543213456678899987665";
		for (int i = 0; i < selectMd5.length(); i++) {
			char c = selectMd5.charAt(i);
			int index = dict1.indexOf(c);
			char temp  = dict2.charAt(index);
			resultArr[i] = temp;
		}
		
		String result =  String.valueOf(resultArr);
		return result;
	}
 	
 	// 领取花票推送链接
 	public void sendinfocash(){
 		int aid = getParaToInt();
 		String openid = Db.queryStr("select openid from f_account where id = ?", aid);
 		String message = "花票领取成功，可在会员中心查看。立即去使用:<a href='" + Constant.getHost + "/product/1'>【订阅双品花束】</a>、<a href='" + Constant.getHost + "/product/2'>【订阅多品花束】</a>、<a href='" + Constant.getHost + "/product/3'>【我要送花】</a>";
 		ApiResult ar = CustomServiceApi.sendText(openid, message);
 		renderJson(ar.getJson());
 	}
 	
 	// 获取素材列表
 	public void getmedia(){
 		ApiResult apiResult = MediaApi.batchGetMaterial(MediaType.NEWS, 0, 10);
 		renderJson(apiResult.getJson());
 	}
 	
 	// 获取模板ID
 	public static String gettemplateId(String title){
 		String url = "https://api.weixin.qq.com/cgi-bin/template/get_all_private_template?access_token="+AccessTokenApi.getAccessToken().getAccessToken(); //String token = AccessTokenApi.getAccessToken().getAccessToken()
 		String json = HttpUtils.get(url);
 		String temp = json.substring(json.indexOf("["), json.indexOf("]")+1);
 		Gson gson = new Gson();
 		String tid = new String();
		List<Map<String, Object>> tlist = gson.fromJson(temp, ArrayList.class);
		for (Map<String, Object> map : tlist) {
			if(title.equalsIgnoreCase((String) map.get("title"))){
				tid = (String)map.get("template_id");
			}
		}
		return tid;
 	}
 	
 	// 获得模版消息
 	public void gettemplate(){
 		String url = "https://api.weixin.qq.com/cgi-bin/template/get_all_private_template?access_token="+AccessTokenApi.getAccessToken().getAccessToken();
 		String json = HttpUtils.get(url);
 		renderJson(json);
 	}
 	
 	//发送模板消息
    public static ApiResult sendTemplateMsg(String jsonStr) {
        String jsonResult = HttpKit.post("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + AccessTokenApi.getAccessToken(), jsonStr);
        return new ApiResult(jsonResult);
    }
 	
 	/**
 	 * @Desc 查询用户分组列表
 	 * @author yetangtang
 	 * @dete 2016/11/10
 	 * @param null
 	 * @return null
 	 */
 	public void getGroups(){
 		ApiResult ar = GroupsApi.get();
 		renderJson(ar.getList("groups"));
 	}
 	public void getUserGroupList(){
 		ApiResult ar = GroupsApi.get();
 		setAttr("groupList", ar.getList("groups"));
 		render("member/group_list.html");
 	}
 	// 获取分组详情
 	public void getGroup() throws UnsupportedEncodingException{
 		Integer id = getParaToInt(0);
 		String name = getPara(1);
 		if(name != null){
 			name = URLDecoder.decode(name, "utf-8");
 		}
 		setAttr("id", id);
 		setAttr("name", name);
 		render("member/group_detail.html");
 	}
 	// 保存分组信息
 	public void saveUserGroup(){
 		Integer id = getParaToInt("id");
 		String name = getPara("name");
 		if(id == null){
 			GroupsApi.create(name);
 		}else{
 			GroupsApi.update(id, name);
 		}
 		renderNull();
 	}
 	// 删除分组
 	public void delUserGroup(){
 		Integer id = getParaToInt();
 		GroupsApi.delete(id);
 		Db.update("update f_account set gid= 0 where gid= ?",id);
 		renderNull();
 	}
 	// 移动用户分组
 	public void editUserGroup(){
 		Integer id = getParaToInt(0);
 		Integer rid = getParaToInt(1);
 		String openid = Db.queryStr("select openid from f_account where id=?", id);
 		GroupsApi.membersUpdate(openid, rid);
 		renderNull();
 	}
 	// 创建二维码
 	public void createQrCode(){
 		int type= getParaToInt(0);	// 推荐人类型(1微信用户/2地推人员/5花艺课)
 		int id = getParaToInt(1);	// 用户ID
 		ApiResult ar = QrcodeApi.createPermanent(type + "_" + id);
 		Gson gson = new  Gson();
 		Map<?,?> map = gson.fromJson(ar.getJson(), HashMap.class);
 		String url = (String) map.get("url");
 		renderText(url);
 	}
 	
	
 	/**
 	 * 绑定 apiConfig 到当前 线程
 	 */
	public static void setApiConfig() {

		ApiConfig ac = new ApiConfig();
		// 配置微信 API 相关常量
		ac.setToken(PropKit.get("token"));
		ac.setAppId(PropKit.get("appId"));
		ac.setAppSecret(PropKit.get("appSecret"));
		ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
		ac.setEncodingAesKey(PropKit.get("encodingAesKey", "setting it in config file"));

		ApiConfigKit.setThreadLocalApiConfig(ac);

	}

 	//客服接口  群发消息    （调用时 需异步执行 ）
/* 	public void groupSendMsg() {
		ApiResult users = UserApi.getFollows();
		Map<?, ?> data = users.getMap("data");
		ArrayList<String> openids = (ArrayList<String>) data.get("openid");
		for (int i = 0; i < openids.size(); i++) {
			//发送消息内容	
			Number num = Db.queryNumber("select count(1) from f_account where openid = ?", openids.get(i));
			if(num.intValue() == 0){
				CustomServiceApi.sendText(openids.get(i), "鲜花是爱的使者，送花是爱的告白，立即回复“爱的名义”，收取爱心大礼包");//文本消息
			}else{
				int aid = Db.queryInt("SELECT id FROM f_account WHERE openid = ?",openids.get(i));	
				boolean flag = true;
				List<Record> oedreList = Db.find("SELECT gName FROM f_order WHERE aid = ? ",aid);
				for (int j = 0; j < oedreList.size(); j++) {
					if (oedreList.get(j).getStr("gName").equals("赤子之心 | 119.9")) {
						flag = false;
					}
				}
				if (flag) {
					CustomServiceApi.sendText(openids.get(i), "鲜花是爱的使者，送花是爱的告白，立即回复“爱的名义”，收取爱心大礼包");//文本消息
				}
			}
			
		}

	    renderJson(true);
	}*/
 	
 	// 执行一次 存入所有用户的  unionid
 	/*public void saveUnionid() {
 		
 		ApiResult users = UserApi.getFollows();
		Map<?, ?> data = users.getMap("data");
		ArrayList<String> openids = (ArrayList<String>) data.get("openid");
		for (int i = 0; i < openids.size(); i++) {
			ApiResult ar = UserApi.getUserInfo(openids.get(i));
			Db.update("update f_account set unionid = ? where openid = ?", ar.get("unionid"),openids.get(i));
		}
	    renderJson(true);
	}*/
 	
}