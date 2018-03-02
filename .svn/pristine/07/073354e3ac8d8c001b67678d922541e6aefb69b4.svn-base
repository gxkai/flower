package com.controller.front;


import com.controller.WeixinApiCtrl;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.util.Constant;

public class FrontProxyCtrl extends Controller {
	
	public void index(){
		/*String state = getPara("state");
		if("openid".equalsIgnoreCase(state)){
			String code = getPara("code");
			renderJson(code);
		}else{
			String getcodeUrl = Constant.getCode
					.replace("APPID", PropKit.get("appId"))
					.replace("REDIRECT_URI", Constant.getHost+getRequest().getRequestURI())
					.replace("SCOPE", "snsapi_userinfo")
					.replace("STATE", "openid");
			
			redirect(getcodeUrl);
		}*/
		
		String state = getPara("state");
		String source=getPara("source");
		String code=getPara("code");
		if("openid".equalsIgnoreCase(state)){
			String url=source+"?code="+code+"&state="+state;
			System.out.println(url);
			redirect(url);
		}else{
			String getcodeUrl = Constant.getCode
					.replace("APPID", PropKit.get("appId"))
					.replace("REDIRECT_URI", Constant.getHost+getRequest().getRequestURI())
					.replace("SCOPE", "snsapi_userinfo")
					.replace("STATE", "openid");
			
			redirect(getcodeUrl);
		}
		
	}
	
	/**
	 * 获取全局access_token
	 * 从代理缓存中获取
	 */
	public void getToken(){
		WeixinApiCtrl.setApiConfig();
		String token = AccessTokenApi.getAccessToken().getAccessToken();
		renderJson(token);
	}

}
