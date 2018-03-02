package com.controller.manage;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.controller.WeixinApiCtrl;
import com.dao.MCDao;
import com.dao.OrderDao;
import com.google.gson.Gson;
import com.interceptor.ManageInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.MediaApi;
import com.jfinal.weixin.sdk.api.MediaApi.MediaType;
import com.jfinal.weixin.sdk.utils.HttpUtils;
import com.util.Constant;
import com.util.Constant.seedType;
import com.util.SendMouldMsgUtil;
import com.util.UploadImageUtil;

/**
 * @Desc 会员管理
 * @author lxx
 * @date 2016/8/16
 * */
@Before(ManageInterceptor.class)
public class ManageMemberCtrl extends Controller {
	/*********************会员管理*********************/
	// 列表
	public void account(){	
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Integer state = getParaToInt("state")==null?9:getParaToInt("state");
		Integer isbuy = getParaToInt("isbuy")==null?9:getParaToInt("isbuy");
		Integer isgive = getParaToInt("isgive")==null?9:getParaToInt("isgive");
		
		String code = getPara("code");
		String aid = getPara("aid");
		String nick=getPara("nick");
		Integer gid = getParaToInt("groupid")==null?9999:getParaToInt("groupid");
		String time_a = getPara("time_a");
		String time_b = getPara("time_b");
		Integer isfans=getParaToInt("isfans")==null?10:getParaToInt("isfans");
		Page<Record> page;
		//首次打开显示最近2天内关注的会员
		if(state==9&&isbuy==9&&isgive==9&&gid==9999	&&isfans==10	
				&&(code==null||code.equals(""))
				&&(aid==null||aid.equals(""))
				&&(nick==null||nick.equals(""))
				&&(time_a==null||time_a.equals(""))
				&&(time_b==null||time_b.equals(""))){
			page =MCDao.getAccount(pageno, 16);
		}else{
			page=MCDao.getAccount(pageno, 16, state, isbuy, isgive, code, gid, time_a, time_b, aid,nick,isfans);
		}
		String groups = HttpUtils.get(Constant.getHost+"/weixin/getGroups");
		Gson gson = new Gson();
		List<Map<String, Object>> glist = gson.fromJson(groups, ArrayList.class);
		Integer cashid = Db.queryInt("select id from f_cashtheme where tpid = 4");
		List<Record> cashlist = Db.find("SELECT * FROM f_cashtheme WHERE id > 3 AND ltime>0");
		setAttr("cashid", cashid);
		setAttr("cashlist", cashlist);
		setAttr("glist", glist);
		setAttr("state", state);
		setAttr("isbuy", isbuy);
		setAttr("isgive", isgive);
		setAttr("code", code);
		setAttr("gid", gid);
		setAttr("time_a", time_a);
		setAttr("time_b", time_b);
		setAttr("aid", aid);
		setAttr("nick",nick);
		setAttr("isfans",isfans);
		
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("accountlist", page.getList());
		render("account.html");
	}
	// 花籽列表
	public void flowerseed() {
	//花籽来源seedtype_list
		List<String>    seedtype_list    =    new    ArrayList<String>();
	    for(seedType seedtype : seedType.values()){
	    	seedtype_list.add(seedtype.name);
	    }
	    String seedtype_list_ = JsonKit.toJson(seedtype_list);
	    setAttr("seedtype_list", seedtype_list);
	    setAttr("seedtype_list_", seedtype_list_);
	//    
	    Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
	//花籽来源
	    String remarks = getPara("remarks")==null?"":getPara("remarks");
	//会员ID
		String aid = getPara("aid");
	//会员昵称
		String nick=getPara("nick");
	//会员手机号
		String tel=getPara("tel");
    //获取时间
		String ctime_start=getPara("ctime_start");
		String ctime_end=getPara("ctime_end");
	//获取花籽状态
		Integer state = getParaToInt("state")==null?9:getParaToInt("state");
	//查询信息	
		Page<Record> page =  MCDao.getFlowerseed(pageno, 16, remarks, aid,nick,tel,ctime_start,ctime_end,state);;
	    setAttr("flowerseedlist", page.getList());
	    setAttr("remarks", remarks);
	    setAttr("aid", aid);
	    setAttr("nick", nick);
	    setAttr("tel", tel);
	    setAttr("ctime_start", ctime_start);
	    setAttr("ctime_end", ctime_end);
	    setAttr("state", state);
	    setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
	    setAttr("totalrow", page.getTotalRow());
		render("flowerseed_list.html");
	}
	
	/**
	 * 客服人工发送花籽给指定的用户
	 * @param aid 用户ID
	 * @param seedscount  花籽数量
	 */
	public void sendseeds_custom(){
		Map<String, Object> jsonMap = new HashMap<>();
		try {
			int aid=getParaToInt(0);
			int seedscount=getParaToInt(1);
			String remarks=getPara(2);
		    Record admin = getSessionAttr("admin");
	    	String username=admin.getStr("username");//操作账号
			for(int i = 0; i<seedscount; i++){	// 付款成功送花籽，1个花籽1笔记录，方便兑换时扣除
				Record seed = new Record();
				seed.set("aid", aid);
				seed.set("send", 1);
				seed.set("type", "custom");
				seed.set("remarks", URLDecoder.decode(remarks,"utf-8"));
				seed.set("ctime", new Date());
				seed.set("username", username);
				Db.save("f_flowerseed", seed);
			}
			WeixinApiCtrl.setApiConfig();
			String openId_tj = Db.queryStr("SELECT openid FROM f_account WHERE id = ?",aid);
			String message = "小美偷偷送了你"+seedscount+"粒花籽。立即使用:<a href='" + Constant.getHost + "/account/flowerseed'>【花籽兑换】</a>";
			CustomServiceApi.sendText(openId_tj, message);
			
			jsonMap.put("result", true);
	 		jsonMap.put("msg","发送成功");
		} catch (Exception e) {
			jsonMap.put("result", false);
	 		jsonMap.put("msg","发送失败："+e.getMessage());
		}
		renderJson(jsonMap);
	}
	// 冻结&启用
	public void freezeAccount(){
		int id = getParaToInt(0);
		int state = getParaToInt(1);
		renderJson(MCDao.freezeAccount(id, state));
	}
	// 设为已赠送
	public void setGive(){
		int id = getParaToInt();
		Record account = Db.findById("f_account", id);
		account.set("isgive", 1);
		renderJson(Db.update("f_account", account));
	}
	// 导出报表
	public void exportinfo(){
		Date time_a = getParaToDate("time_a");
		Date time_b = getParaToDate("time_b");
		try {
			OrderDao.getinfoForExcel(getResponse(), time_a, time_b);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		renderNull();
	}
	//设置分组
	public void setgroup(){
		int id = getParaToInt(0);
		int gid = getParaToInt(1);
		String groups = HttpUtils.get(Constant.getHost + "/weixin/getGroups");
		Gson gson = new Gson();
		List<Map<String, Object>> glist = gson.fromJson(groups, ArrayList.class);
		setAttr("glist", glist);
		setAttr("id", id);
		setAttr("gid", gid);
		render("group_set.html");
	}
	//保存用户信息
	public void saveGroupInfo(){
		int id = getParaToInt("id");
		int gid = getParaToInt("gid");
		boolean result = MCDao.updateMemGroup(id, gid);
		renderJson(result);
	}
	//推送花票
	public void pushcash(){
		Map<String, Object> map = new HashMap<>();
		int cashid = getParaToInt("cashid");
		Db.update("UPDATE f_cashtheme SET tpid = 0 WHERE id > 3 AND ltime > 0");
		Db.update("UPDATE f_cashtheme SET tpid = 4 WHERE id > 3 AND ltime > 0 AND id = ?", cashid);
		String theme = Db.queryStr("select name from f_cashtheme where id=?", cashid);
		map.put("result", true);
		map.put("theme", theme);
		renderJson(map);
	}
	
	//查看带颜详情
	public void daiyan_detail(){
		int id = getParaToInt(0); 
		Record dyrs = Db.findFirst("SELECT count(1) 'allCount',sum( if( (b.ishas=0 and b.state in(1,2,3)),1,0) ) 'successCount' FROM f_account a left join f_order b on a.id=b.aid WHERE SUBSTRING(tjid,1,1) ='1' AND SUBSTRING(tjid,3) =?",id);
		setAttr("dyrs", dyrs);
		
		List<Record> dyxqlist = Db.find("SELECT a.id,a.nick,a.tel 'tel_a',a.ctime 'ctime_a',a.state,b.ordercode,b.name,b.tel 'tel_b',b.addr,b.price,b.ctime 'ctime_b',ishas FROM f_account a left join f_order b on a.id=b.aid WHERE SUBSTRING(tjid,1,1) ='1' AND SUBSTRING(tjid,3) =? order by b.ctime desc",id);
		setAttr("dyxqlist", dyxqlist);
		render("daiyan_detail.html");
	}
	
	
	/*********************意见反馈*********************/
	// 列表
	public void feedback(){
		Integer pageno = getParaToInt()==null?1:getParaToInt();
		Page<Record> page = MCDao.getFeedback(pageno, 16);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("feedbacklist", page.getList());
		render("feedback.html");
	}
	// 意见反馈详情
	public void advice(){
		Integer id = getParaToInt()==null?9999:getParaToInt();
		Record advice = MCDao.getAdviceinfo(id);
		setAttr("advice", advice);
		render("advice.html");
	}
	/****************************群发客服或模版消息******************************************/
	/**
	 * 编辑文字+链接 发送客服消息
	 */
	public void SendKfmsg() {
		List<Record>list= Db.find("select name,url url1 from f_wxlink");
		for(Record record:list){
			record.set("url",PropKit.get("host")+record.getStr("url1"));
		}
		setAttr("list",list);
		render("SendKfmsg.html");
	}
	/**
	 * 单个测试
	 * @throws UnsupportedEncodingException 
	 */
	public void KfTest2() throws UnsupportedEncodingException {
		    Map<String, Object> jsonMap = new HashMap<>();
		    UploadFile up = getFile("uploadfile");
		    Integer id = getParaToInt("id");
			String content=URLDecoder.decode(getPara("content"), "UTF-8");
			String url = getPara("link");
			String name = getPara("name");
		    Integer isMb = getParaToInt("isMb");//0:客服，1:模版
		    if (isMb==0) {
				String message = content ;
				if(StrKit.notBlank(name)){
					 name=URLDecoder.decode(name, "UTF-8");
					 message = content +  "\n\r<a href='"+url+"'>"+name+"</a>";
				}
			
				WeixinApiCtrl.setApiConfig();
				String openId = Db.queryStr("SELECT openid FROM f_account WHERE id = ?",id);
				boolean sf =false;
				boolean sm=false;
				// 客服消息 发送图片给用户
				if(StrKit.notBlank(message)) {
				    ApiResult ar=CustomServiceApi.sendText(openId, message);
				    if (ar.getInt("errcode")==0) {
						sm=true;
					}
				}
		        if (up!=null) {
		        	File file = up.getFile();
		        	if (file.exists()&& file.length()!= 0) {
						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						 ar=CustomServiceApi.sendImage(openId, ar.getStr("media_id"));
						 if (ar.getInt("errcode")==0) {
							sf=true;
						}
						   
					}
				}
				if (sm||sf) {
			 		jsonMap.put("msg","发送成功");
				}else {
			 		jsonMap.put("msg","发送失败");
				}	
			}else {
				WeixinApiCtrl.setApiConfig();
				String openid = Db.queryStr("select openid from f_account where id = ?",id);
				String msg =  SendMouldMsgUtil.SendKfMsg(openid, url, content)==true?"发送成功":"发送失败";
				jsonMap.put("msg", msg);
			}
				
		renderJson(jsonMap);
	}
	
	
	
	/**
	 * 群发
	 * @throws UnsupportedEncodingException 
	 */
	public void KfSendAll2() throws UnsupportedEncodingException {
		String id_start = getPara("id_start");
		String id_end = getPara("id_end");
		String content=URLDecoder.decode(getPara("content"), "UTF-8");
		String url = getPara("link");
		String name = getPara("name");
		String message = content ;
		Integer isMb = getParaToInt("isMb");
		UploadFile up = getFile("uploadfile");
		File file = null;
		boolean hasFile=false;
		if (isMb==0) {
			if(StrKit.notBlank(name)){
				 name=URLDecoder.decode(name, "UTF-8");
				 message = content +  "\n\r<a href='"+url+"'>"+name+"</a>";
			}
			if (up!=null) {
				file = up.getFile();
				if (file.exists()&& file.length()!= 0) {
					hasFile=true;
				}  
			}
		}	
		MCDao.KfSendAll2(getResponse(),file,id_start,id_end,message,hasFile,getSession(),isMb,url);
		renderNull();
	}
	public void setSession() {
		    MCDao.setSession(getSession());
		    renderNull();
	}
	/**
	 * 群发按钮cookie
	 */
	public void kftextcookie() {
		boolean result = true;
		Cookie[] ci = MCDao.cookie(getRequest());       
		if(ci.length > 0){
			for(Cookie cookie:ci){
				if("kf".equals(cookie.getName())){
					//result = false;
					renderJson(result);return;
				}
			}
		}
		Cookie cookie = new Cookie("kf","kf");
		cookie.setMaxAge(60*60*24);
		cookie.setPath("/manage/");
		getResponse().addCookie(cookie);
		renderJson("result",result);
	}
	public void GetMemberCount() {
		String id_start = getPara("id_start");
		String id_end = getPara("id_end");
		Integer isMb = getParaToInt("isMb");
		String sql;
		if (isMb==0) {
		    sql = "select count(1) count from f_account  WHERE isfans=0 ";
			if (StrKit.notBlank(id_start)) {
				sql += " and id>="+id_start; 
			}
			if (StrKit.notBlank(id_end)) {
				sql += " and id<="+id_end; 
			}
		}else {
			sql = "select count(1) count from f_account_selected";
		}	
		Number count = Db.queryNumber(sql);
		renderJson(count);
	}
	/***********************************自动回复配置**********************************************/
	/**
	 * 自动回复配置
	 */
	public void ConfigAutoResp() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Integer type = getParaToInt("type")==null?10:getParaToInt("type");
		Page<Record> page = MCDao.getAutoResp(pageno, 16, type);
		List<Record>zdhflist = page.getList();
		for(Record list:zdhflist ){
			if (list.getInt("type")==1) {
				list.set("sendText", Db.queryStr("select name from f_spread where id = ?",Integer.parseInt(list.getStr("sendText")) ));
			}
		}
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("zdhflist", zdhflist);
		setAttr("type",type);
		render("ConfigAutoResp.html");
	}
	/**
	 * 自动回复编辑
	 */
	public void AutoRespEdit() {
		String sendText1 = "";
		String sendText = "";
		int type = getParaToInt(1);
		List<Record>urlList= Db.find("select name,url url1 from f_wxlink");
		for(Record record:urlList){
			record.set("url",PropKit.get("host")+record.getStr("url1"));
		}
		
		try {
			sendText1 = URLDecoder.decode(getPara(0), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (type==1) {
			sendText =String.valueOf(Db.queryInt("select id from f_spread where name = ?", sendText1));
		}else {
			sendText = sendText1;
		}
		Record record = Db.findById("f_zdhf", "sendText,type", sendText,type);
		if (type==1) {
			record.set("sendText", sendText1);
		}
		List<Record>cashthemeList=Db.find("select a.name,count(1) from f_cashtheme a LEFT JOIN f_cashclassify b ON a.id=b.tid WHERE a.ltime>0 and a.etime>= curdate() and b.state=0 and (a.sendcount<a.maxcount OR a.maxcount=0) GROUP BY a.name");
		setAttr("cashthemeList", cashthemeList);
		setAttr("list", record);
		setAttr("urlList", urlList);
		render("AutoRespEdit.html");
	}
	/**
	 * 自动回复增加
	 */
	public void AutoRespAdd() {
		List<Record>list= Db.find("select name,url url1 from f_wxlink");
		for(Record record:list){
			record.set("url",PropKit.get("host")+record.getStr("url1"));
		}
		List<Record>qrList = Db.find("select id,name from f_spread");
		List<Record>cashthemeList=Db.find("select a.name,count(1) from f_cashtheme a LEFT JOIN f_cashclassify b ON a.id=b.tid WHERE a.ltime>0 and a.etime>= curdate() and b.state=0 and (a.sendcount<a.maxcount OR a.maxcount=0) GROUP BY a.name ");
		setAttr("cashthemeList", cashthemeList);
		setAttr("qrlist",qrList );
		setAttr("list",list);
		render("addAutoResp.html");
	}
	/**
	 * 自动回复编辑保存
	 * @throws IOException
	 */
	public void AutoRespEditSave() throws IOException {
		String returnText = getPara("returnText");
		String sendText = getPara("sendText");
		Integer type = getParaToInt("type");
		String returnUrlname = getPara("returnUrlname");
		String returnUrl = getPara("returnUrl");
		String expdate = getPara("expdate");
		String img = getPara("img");
		String returnCashtheme = getPara("returnCashtheme");
		Record record = new Record();
		if (type==1) {
			sendText =Integer.toString(Db.queryInt("select id from f_spread where name = ?", sendText));
		}
		String imgurl ="";
		if(img!=null&&img.equals("")==false){
			imgurl=UploadImageUtil.upLoadBase(img);
			record.set("returnPicture", imgurl );
		}
		record.set("returnText", returnText);
		record.set("expdate", expdate);
		record.set("sendText", sendText);
		record.set("returnUrlname", returnUrlname);
		record.set("returnUrl", returnUrl);
		record.set("type",type);
		record.set("returnCashtheme",returnCashtheme);
		
		record.set("sendText", sendText);
		record.set("type",type);
		boolean result = Db.update("f_zdhf","sendText,type",record);
		renderJson(result);
	}
	/**
	 * 自动回复删除保存
	 */
	public void AutoRespDel() {
		String sendText1 = "";
		String sendText = "";
		int type = getParaToInt(1);
		try {
			sendText1 = URLDecoder.decode(getPara(0), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (type==1) {
			sendText =String.valueOf(Db.queryInt("select id from f_spread where name = ?", sendText1));
		}else {
			sendText = sendText1;
		}
		boolean result =  Db.deleteById("f_zdhf", "sendText,type", sendText,type);
		renderJson(result);
	}
	/**
	 * 自动回复批量增加
	 */
	public void SubConfigAutoResp() {
		final String content = getPara("content");
        String sendtext = getPara("sendtext");
        final String name = getPara("name");
		final String url = getPara("link");
	    String img = getPara("img");
	    String qrurl = getPara("qrurl");
	    String expdate1 = getPara("expdate");
	    final String cashtheme = getPara("cashtheme");
	    if (StrKit.isBlank(expdate1)) {
			expdate1 = "2099-01-01";
		}
	    final String expdate = expdate1;
		String mgurl ="";
		if(StrKit.notBlank(img)){
			try {
				mgurl=UploadImageUtil.upLoadBase(img);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		final String imgurl = mgurl; 
		boolean result ;
		if (StrKit.notBlank(sendtext)) {
			Record isfind = Db.findById("f_zdhf", "sendText,type", sendtext,0);
			Record record = new Record();
			record.set("sendtext", sendtext);
			record.set("type", 0);
			record.set("returnText",content);
			record.set("returnUrl",url);
			record.set("returnUrlname",name);
			record.set("returnPicture", imgurl);
			record.set("expdate", expdate);
			record.set("returnCashtheme", cashtheme);
			if (isfind!=null) {
				record.set("sendtext", sendtext);
				record.set("type", 0);
				result =  Db.update("f_zdhf", "sendtext,type", record);
			}else {
				result = Db.save("f_zdhf", record);
			}
			renderJson(result);return;
		}
		if (StrKit.notBlank(qrurl)) {
		  final String[]  qrid = qrurl.split(",");
				result=Db.tx(new IAtom() {	
					@Override
					public boolean run() throws SQLException {
						boolean DBresult = false;
						for(int i=0;i<qrid.length;i++){
							Record isfind = Db.findById("f_zdhf", "sendText,type", qrid[i],1);
							Record record = new Record();
							record.set("sendtext", qrid[i]);
							record.set("type", 1);
							record.set("returnText",content);
							record.set("returnUrl",url);
							record.set("returnUrlname",name);
							record.set("returnPicture", imgurl);
							record.set("expdate", expdate);
							record.set("returnCashtheme", cashtheme);
							if (isfind!=null) {
								record.set("sendtext", qrid[i]);
								record.set("type", 1);
								DBresult =  Db.update("f_zdhf", "sendtext,type", record);
							}else {
								DBresult = Db.save("f_zdhf", record);
							}
						}
						return DBresult;
					}
				});
				renderJson(result);return;
	       }	
	}
	/**
	 * 18位签到列表
	 * @throws UnsupportedEncodingException 
	 */
	public void sign18List() throws UnsupportedEncodingException {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		String aid = getPara("aid");
		String ctime_start = getPara("ctime_start");
		String ctime_end = getPara("ctime_end");
    	Page<Record> page = MCDao.getSign18List(pageno,16,aid,ctime_start,ctime_end);
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("sign18list", page.getList());
		setAttr("aid", aid);
		setAttr("ctime_start", ctime_start);
		setAttr("ctime_end", ctime_end);
		render("sign18_list.html");
	}
	public void sign18Edit() {
		Integer id = getParaToInt(0);
    	Record list = Db.findFirst("select aid,id,state from f_sign18 where id = ? ", id);	
    	setAttr("list", list);
		render("sign18_edit.html");
	}
	public void sign18SaveEdit() {
    	Integer id = getParaToInt("id");
		Integer state = getParaToInt("state");
  		
  		Record record = new Record();
  		record.set("id", id);
  		record.set("state", state);
        boolean result = Db.update("f_sign18", record);
        renderJson(result);
		
	}
	/***************快闪花店(我的回忆)
	 * @throws UnsupportedEncodingException **********************/
	public void mermoryList() throws UnsupportedEncodingException{
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		String aid=getPara("aid");
		Page<Record> page = MCDao.getMyMermory(pageno,16,aid);
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("mermorylist", page.getList());
		setAttr("aid", aid);
		render("mermory_list.html");		
	}
	public void updateMermoryState(){
		int id = getParaToInt();
		Record account = Db.findById("f_myMemory", id);
		account.set("state", 0);
		renderJson(Db.update("f_myMemory", account));
	}
}