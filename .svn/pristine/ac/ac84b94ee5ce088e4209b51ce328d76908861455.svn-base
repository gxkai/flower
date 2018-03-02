package com.interceptor;

import javax.servlet.http.Cookie;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ManageInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		Controller c = inv.getController();
		Record admin = null;
		Cookie[] ci = c.getCookieObjects();
		if(ci.length > 0){
			for(Cookie cookie:ci){
				if("flower".equals(cookie.getName())){
					String id = cookie.getValue();
					admin = Db.findFirst("SELECT a.id,a.phone,a.username,a.password,a.rid,b.name,c.mid FROM f_admin a LEFT JOIN f_role b ON a.rid=b.id LEFT JOIN f_authority c ON b.id=c.rid where a.id=?", id);
				}
			}
		}
		if(admin!=null){
			c.removeCookie("flower", "/manage/");
			c.removeSessionAttr("admin");
			c.setSessionAttr("admin", admin);
			Cookie cookie = new Cookie("flower", ""+admin.getInt("id"));
			cookie.setMaxAge(60*60*2);
			cookie.setPath("/manage/");
			c.setCookie(cookie);
			inv.invoke();
		}else{
			String ck = inv.getControllerKey();
			if("/manage".equals(ck)){
				c.redirect("/manage/login");
			}else{
				c.renderHtml("<script>window.parent.window.loginOut();</script>");
			}
		}
		
	}
}
