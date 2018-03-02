package com.util;

import java.util.HashMap;
import java.util.Map;

import com.controller.WeixinApiCtrl;
import com.google.gson.Gson;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.QrcodeApi;

public class DaiYanURL {
	/**
	 * 生成带颜二维码  
	 * @author Glacier
	 * @date 2017年9月20日
	 * @param aid
	 * @param type 推荐类型 1.用户 2.地推 3.拼团 4.红包 41.礼品卡（第一次分享） 42.礼品卡（第二次转增）  5.花艺课  6.十八号花店
	 * @param id
	 * @return
	 */
	public static String getUrl(int aid,int type,String id){
		WeixinApiCtrl.setApiConfig();
		ApiResult ar = QrcodeApi.createPermanent(type + "_" + aid+"_"+id);
 		Gson gson = new  Gson();
 		Map<?,?> map = gson.fromJson(ar.getJson(), HashMap.class);
 		String url = (String) map.get("url");
 		return url;
	}
	public static String getUrl(int aid,int type){
		WeixinApiCtrl.setApiConfig();
		ApiResult ar = QrcodeApi.createPermanent(type + "_" + aid);
 		Gson gson = new  Gson();
 		Map<?,?> map = gson.fromJson(ar.getJson(), HashMap.class);
 		String url = (String) map.get("url");
 		return url;
	}
}
