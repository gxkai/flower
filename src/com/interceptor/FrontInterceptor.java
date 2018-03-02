package com.interceptor;

import java.util.Map;
import com.dao.FCDao;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.util.Constant;

public class FrontInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		Controller c = inv.getController();
		Record account = c.getSessionAttr("account");
		if(account == null){
			String state = c.getPara("state");
			if("openid".equalsIgnoreCase(state)){
				String code = c.getPara("code");
				Map<String, Object> map = FCDao.getAccessToken(code);
				
				// 根据openId获取用户
				account = FCDao.setAccount(map.get("access_token").toString(), map.get("openid").toString(), null, null);
				c.setSessionAttr("account", account);
				int state_user = Db.queryInt("select state from f_account where id=?", account.getInt("id"));
				if(state_user == 1){
					c.redirect("/freeze");
				}else{
					inv.invoke();
				}
				
				/*Long count = Db.queryLong("select count(1) from f_account where openid = ?", map.get("openid").toString());
				if(count  == 0){
					c.redirect("/focuson");//如果么有关注过，提示用户长按二维码关注
				}else{
					// 根据openId获取用户
					account = FCDao.setAccount(map.get("access_token").toString(), map.get("openid").toString(), null, null);
					c.setSessionAttr("account", account);
					int state_user = Db.queryInt("select state from f_account where id=?", account.getInt("id"));
					if(state_user == 1){
						c.redirect("/freeze");
					}else{
						inv.invoke();
					}
				}*/
				
			}else{
				String code = Constant.getCode
						.replace("APPID", PropKit.get("appId"))
						.replace("REDIRECT_URI", Constant.getHost + c.getRequest().getRequestURI())
						.replace("SCOPE", "snsapi_userinfo")
						.replace("STATE", "openid");
				c.redirect(code);
			}
		}else{
			int state_user = Db.queryInt("select state from f_account where id=?", account.getInt("id"));
			//System.out.println(state_user);
			if(state_user == 1){
				c.redirect("/freeze");
			}else{
				inv.invoke();
			}
		}
	}

}