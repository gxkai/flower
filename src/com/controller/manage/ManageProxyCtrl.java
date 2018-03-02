package com.controller.manage;

import com.controller.WeixinApiCtrl;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.util.Constant;
/**
 * 代理层，以后打算单独用一个服务器做代理
 * 目前，考虑到成本，先和商城放在一起
 * @author SHICHUNXIANG
 *
 */
public class ManageProxyCtrl extends Controller {
	public void index(){
		String state = getPara("state");
		String source=getPara("source");
		String code=getPara("code");
		if("openid".equalsIgnoreCase(state)){
			String url=source+"?code="+code+"&state="+state;
			//System.out.println(url);
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
