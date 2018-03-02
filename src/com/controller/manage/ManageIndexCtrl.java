package com.controller.manage;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.dao.MCDao;
import com.interceptor.ManageInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.SnsAccessToken;
import com.jfinal.weixin.sdk.api.SnsAccessTokenApi;
import com.jfinal.weixin.sdk.api.UserApi;
import com.util.Constant;
import com.util.MD5Util;
import com.util.SendMsgUtil;

import sun.applet.resources.MsgAppletViewer;

public class ManageIndexCtrl extends Controller {
	// 首页
	@Before(ManageInterceptor.class)
	public void index(){		
		Record admin = getSessionAttr("admin");
		setAttr("username", admin.getStr("username"));
		setAttr("name", admin.getStr("name"));
		
		List<Record> ml = new ArrayList<>();
		String mid = admin.getStr("mid");
		if(mid!=null && mid.length()>0){
			String[] midarr = mid.split(",");
			/*System.out.println(mid);*/
			ml = MCDao.getMenu();
			for(Iterator<Record> it=ml.iterator();it.hasNext();){
				Record menu = it.next(); 
				boolean has = false;
				for(String m : midarr){
					if(menu.getInt("id") == Integer.parseInt(m)){
						has = true;
						break;	
					}
				}
				if(!has){
					it.remove();
				}
			}
		}
		setAttr("menulist", ml);
		render("index.html");
	}
	// 登录页面  --弃用
	public void login(){
		render("login.html");
	}
	public void qrlogin() {
		String code = getPara("code");
		SnsAccessToken access_token = SnsAccessTokenApi.getSnsAccessToken(PropKit.get("webAppId"), PropKit.get("webAppSecret"), code);	
		String openId = access_token.getOpenid();
		Number num = Db.queryNumber("select count(1) from f_admin where openid = ?",openId );
		if (num.intValue()==0) {
			setAttr("openid", openId);
			render("logister.html");return;
		}
		Record admin = Db.findFirst("SELECT a.id,a.phone,a.username,a.password,a.rid,b.name,c.mid FROM f_admin a LEFT JOIN f_role b ON a.rid=b.id LEFT JOIN f_authority c ON b.id=c.rid where a.openid=?", openId);
		getSession().setAttribute("admin", admin);
		Cookie cookie = new Cookie("flower", ""+admin.getInt("id"));
		cookie.setMaxAge(60*60*2);
		cookie.setPath("/manage/");
		getResponse().addCookie(cookie);
		setAttr("username", admin.getStr("username"));
		setAttr("name", admin.getStr("name"));
		
		List<Record> ml = new ArrayList<>();
		String mid = admin.getStr("mid");
		if(mid!=null && mid.length()>0){
			String[] midarr = mid.split(",");
			/*System.out.println(mid);*/
			ml = MCDao.getMenu();
			for(Iterator<Record> it=ml.iterator();it.hasNext();){
				Record menu = it.next(); 
				boolean has = false;
				for(String m : midarr){
					if(menu.getInt("id") == Integer.parseInt(m)){
						has = true;
						break;	
					}
				}
				if(!has){
					it.remove();
				}
			}
		}
		setAttr("menulist", ml);
		render("index.html");
	}
	// 登录操作 --弃用
	public void adminLogin() throws NoSuchAlgorithmException, UnsupportedEncodingException{
		String username = getPara("username");
		String password = getPara("password");
		
		boolean result = false;
		String message = new String();
		
		Record admin = Db.findFirst("SELECT a.id,a.username,a.password,a.rid,b.name,c.mid FROM f_admin a LEFT JOIN f_role b ON a.rid=b.id LEFT JOIN f_authority c ON b.id=c.rid where a.username=?", username);
		if(admin == null){
			message = "用户名或密码错误";
		}else{
			boolean v = MD5Util.validPassword(password, admin.getStr("password"));
			
			if(v){
				result = true;
				message = "登录成功";
				getSession().setAttribute("admin", admin);
				
				Cookie cookie = new Cookie("flower", ""+admin.getInt("id"));
				cookie.setMaxAge(60*60*24*7);
				cookie.setPath("/manage/");
				getResponse().addCookie(cookie);
				
				
			}else{
				message = "用户名或密码错误";
			}
		}
		
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("result", result);
		responseMap.put("message", message);
		renderJson(responseMap);
	}
	// 退出操作
	public void adminExit(){
		String username = getPara(0);
		Record admin = Db.findFirst("SELECT a.id,a.phone,a.username,a.password,a.rid,b.name,c.mid FROM f_admin a LEFT JOIN f_role b ON a.rid=b.id LEFT JOIN f_authority c ON b.id=c.rid where a.username=?", username);
		Cookie cookie = new Cookie("flower", ""+admin.getInt("id"));
	    cookie.setMaxAge(0);
	    cookie.setPath("/manage/");
	    getResponse().addCookie(cookie);
		getSession().removeAttribute("admin");
		redirect("/manage/login");
	}
	
	// 桌面
	public void desktop(){
		Page<Record> page = MCDao.getNotice(1, 16, null);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("noticelist", page.getList());
		render("/manage/iframe/desktop.html");
	}
	//发送短信验证码
	public void sendcode() throws Exception{
		String username = getPara("username");
		String password = getPara("password");
		String code = getPara("code");
        String message = new String();
        int result ;
        Map<String, Object> responseMap = new HashMap<String, Object>();
		Record admin = Db.findFirst("SELECT a.id,a.phone,a.username,a.password,a.rid,b.name,c.mid FROM f_admin a LEFT JOIN f_role b ON a.rid=b.id LEFT JOIN f_authority c ON b.id=c.rid where a.username=?", username);
		if(admin == null){
			result = 1;
			message = "用户名错误";
			responseMap.put("result", result);
			responseMap.put("message", message);
			renderJson(responseMap);
		}else{
			boolean v = MD5Util.validPassword(password, admin.getStr("password"));
			if(v){
				String tel  = admin.getStr("phone");
				if(tel!=null&&tel!=""){
					String msg = String.format("您的短信验证码:%s,请在2分钟内输入验证",code);
					int res =  SendMsgUtil.sendMsg(tel,msg);
					if (res ==1){
						result = 0;
						message = "短信验证码已发送,请注意查收";
						getSession().setAttribute("admin", admin);
						getSession().setAttribute("code", code);
						responseMap.put("result", result);
						responseMap.put("message", message);
						renderJson(responseMap);
					}else {
						result =3;
						message = "短信发送失败";
						responseMap.put("result", result);
						responseMap.put("message", message);
						renderJson(responseMap);
					}
			     }else {
			    	    result =4;
						message = "该管理员手机号未绑定";
						responseMap.put("result", result);
						responseMap.put("message", message);
						renderJson(responseMap);
				}
			}else{
				result = 2;
				message = "密码错误";
				responseMap.put("result", result);
				responseMap.put("message", message);
				renderJson(responseMap);
			}
		}
	}
	//比较短信验证码
	public void comparecode() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String codelast = getPara("codelast");
		String username = getPara("username");
		String password = getPara("password");
		String code = getSessionAttr("code");
		String openid = getPara("openid");
		int result;
		String message = new String();
		Map<String, Object> responseMap = new HashMap<String, Object>();
		Record admin = Db.findFirst("SELECT a.id,a.phone,a.username,a.password,a.rid,b.name,c.mid FROM f_admin a LEFT JOIN f_role b ON a.rid=b.id LEFT JOIN f_authority c ON b.id=c.rid where a.username=?", username);
		if(admin == null){
			result = 1;
			message = "用户名错误";
			responseMap.put("result", result);
			responseMap.put("message", message);
			renderJson(responseMap);
		}else{
			boolean v = MD5Util.validPassword(password, admin.getStr("password"));
			if(v){
				 if (codelast.equals(code)==true) {			 
					   result = 0;
					   message = "验证成功";
					   Cookie cookie = new Cookie("flower", ""+admin.getInt("id"));
					   cookie.setMaxAge(60*60);
					   cookie.setPath("/manage/");
					   getResponse().addCookie(cookie);
					   if (StrKit.notBlank(openid)) {
						   Record record = new Record();
						   record.set("id", admin.getInt("id"));
						   record.set("openid", openid);
						   Db.update("f_admin", record);
						   message = "绑定成功";
					}
					   responseMap.put("message", message);
					   responseMap.put("result", result);
					   renderJson(responseMap);
					}else {
						result = 3;
						message = "验证码不正确";
						responseMap.put("message", message);
					    responseMap.put("result", result);
					    renderJson(responseMap);
					} 	
			}else{
				result = 2;
				message = "密码错误";
				responseMap.put("result", result);
				responseMap.put("message", message);
				renderJson(responseMap);
			}
		}     	
	}
	//清楚指定缓存
	public void clear() {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		getSession().removeAttribute("code");
		boolean result = true;
		responseMap.put("result", result);
		renderJson(responseMap);
	}
	
	
	/**
	 * 根据单号查看订单信息 
	 * @author Glacier
	 * @date 2017年9月7日
	 */
	public  void  Api_get_orderInfo() {
		long  orderCode = getParaToLong(0);
		Record orderInfo = Db.findFirst("SELECT id,ordercode,aid,`name`,tel,addr,reach,price,cycle,isvase,zhufu,songhua,jihui,"
				+ "jh_list,color,jh_color,yhje,yhfs,ctime,type,szdx,state,ocount,ishas,isks,remark,is_sy,sy_code,"
				+ "gName,sendCycle,app,style,paytime,ptNo,ptSusTime "
				+ "from f_order "
				+ "where ordercode = ?",orderCode);
		renderJson(orderInfo);
	}
	
	/**
	 * 根据用户id获取用户地址信息
	 * @author Glacier
	 * @date 2017年9月7日
	 */
	public void Api_get_userInfo() {
		
		
	}
	/**
	 * PC扫描登录
	 */
	public void toWebOauth(){
	    String calbackUrl=Constant.getHost+"/manage/qrlogin";
	    String url=SnsAccessTokenApi.getQrConnectURL(PropKit.get("webAppId"), calbackUrl,"666");
	    redirect(url);
	}
	
}
