package com.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.RedPackApi;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import com.jfinal.weixin.sdk.utils.HttpUtils;

public class SendCashRed {
	private static String certPath="//usr//local//flower//image//cert//apiclient_cert.p12";//正式
	
	//private static String certPath="D:\\upload\\flower\\image\\cert\\apiclient_cert.p12";//测试
	
	/**
	 * 发普通红包:红包金额大于等于1元
	 * 使用方法直接拷贝修改：com.util.SendCashRed.Send(getRequest(), "花美美", "oTrkFwIlaGvDpHmGaZHCTD9zk0r4",
			   "100", "1", "新年快乐,大吉大利", "新年祝福", "备注信息")
	 * @param send_name 商户名称
	 * @param re_openid 用户OPENID
	 * @param total_amount 付款现金(单位分) 
	 * @param total_num 红包发放总人数  必须等于1
	 * @param wishing 红包祝福语
	 * @param act_name 活动名称
	 * @param remark 备注
	 * 
	 */
	public static boolean Send(HttpServletRequest request,String send_name,
			String re_openid,String total_amount,
			String total_num,String wishing,String act_name,String remark){
		
		// 商户订单号
        String mchBillno ="HB"+System.currentTimeMillis() + "";
        String ip = getRemortIP(request);//获取用户ip

        Map<String, String> params = new HashMap<String, String>();
        // 随机字符串
        params.put("nonce_str", System.currentTimeMillis() / 1000 + "");
        // 商户订单号
        params.put("mch_billno", mchBillno);
        // 商户号
        params.put("mch_id", PropKit.get("mchId"));
        // 公众账号ID
        params.put("wxappid", PropKit.get("appId"));
        // 商户名称
        params.put("send_name", send_name);
        // 用户OPENID
        params.put("re_openid", re_openid);
        // 付款现金(单位分)
        params.put("total_amount",total_amount);
        // 红包发放总人数
        params.put("total_num", total_num);
        // 红包祝福语
        params.put("wishing", wishing);
        // 终端IP
        params.put("client_ip", ip);
        // 活动名称
        params.put("act_name", act_name);
        // 备注
        params.put("remark", remark);

      //创建签名
        String sign = PaymentKit.createSign(params, PropKit.get("key"));
        params.put("sign", sign);
 
        String xmlResult=RedPackApi.sendRedPack(params, certPath, PropKit.get("mchId"));
        
        Map<String, String> result = PaymentKit.xmlToMap(xmlResult);
        
        //此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
        String return_code = result.get("return_code");
        //业务结果
        String result_code = result.get("result_code");
        
        if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
        	Record f_test=new Record();
	    	f_test.set("str1", xmlResult);
	    	f_test.set("str2", "红包失败");
	    	f_test.set("ctime", new Date());
	    	Db.save("f_test", f_test);
            return false;
        }
        if (StrKit.notBlank(result_code) && "SUCCESS".equals(result_code)) {
            
            return true;
        }
        return false;
		
	}
	
	/**
	 * 发裂变红包:每个红包的金额大于等于1元
	 * * 使用方法直接拷贝修改：com.util.SendCashRed.sendGroup(getRequest(), "花美美", "oTrkFwIlaGvDpHmGaZHCTD9zk0r4",
			   "300", "3", "新年快乐,大吉大利", "新年祝福", "备注信息")
	 * @param request
	 * @param send_name 商户名称
	 * @param re_openid 用户OPENID
	 * @param total_amount 付款现金(单位分) 
	 * @param total_num 红包发放总人数  必须等于1
	 * @param wishing 红包祝福语
	 * @param act_name 活动名称
	 * @param remark 备注
	 */
	public static boolean sendGroup(String send_name,
			String re_openid,String total_amount,
			String total_num,String wishing,String act_name,String remark){
        String mchBillno ="LBHB"+System.currentTimeMillis() + "";
        
        Map<String, String> params = new HashMap<String, String>();
        // 随机字符串
        params.put("nonce_str", System.currentTimeMillis() / 1000 + "");
        // 商户订单号
        params.put("mch_billno", mchBillno);
        // 商户号
        params.put("mch_id",  PropKit.get("mchId"));
        // 公众账号ID
        params.put("wxappid", PropKit.get("appId"));
        // 商户名称
        params.put("send_name", send_name);
        // 用户OPENID
        params.put("re_openid", re_openid);
        // 付款现金(单位分)
        params.put("total_amount", total_amount);
        // 红包发放总人数
        params.put("total_num", total_num);
        //红包金额设置方式
        params.put("amt_type", "ALL_RAND");
        // 红包祝福语
        params.put("wishing", wishing);
        // 活动名称
        params.put("act_name", act_name );
        // 备注
        params.put("remark", remark);
        
        //创建签名
        String sign = PaymentKit.createSign(params, PropKit.get("key"));
        params.put("sign", sign);

        String xmlResult=RedPackApi.sendGroupRedPack(params,certPath, PropKit.get("mchId"));
        //System.out.println(xmlResult);

        Map<String, String> result = PaymentKit.xmlToMap(xmlResult);
        
        //此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
        String return_code = result.get("return_code");
        //业务结果
        String result_code = result.get("result_code");
        
        if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
            return false;
        }
        if (StrKit.notBlank(result_code) && "SUCCESS".equals(result_code)) {
            
            return true;
        }
        return false;
	}
	
	/**
	 * 企业付款到个人零钱
	 * com.util.SendCashRed.ComToUser(getRequest(), "oB_7EwDrolCk3yXP38m7IeZmBfLw",
			   "100","打赏提现")
	 * @param request
	 * @param openid
	 * @param amount 付款金额，单位：分
	 * @param desc 企业付款描述信息
	 * @return
	 */
	public static boolean ComToUser(HttpServletRequest request,String openid,
			String amount,String desc){
		String ip = getRemortIP(request);//获取用户ip
		String mchBillno ="CU"+System.currentTimeMillis() + "";
		Map<String, String> params = new HashMap<String, String>();
		params.put("mch_appid", PropKit.get("appId"));//公众号appid
		params.put("mchid", PropKit.get("mchId"));//商户号
		params.put("nonce_str", System.currentTimeMillis() / 1000 + "");//随机字符串
		params.put("partner_trade_no", mchBillno);// 商户订单号
		params.put("openid", openid);//用户公众号唯一识别
		params.put("check_name", "NO_CHECK");//校验用户姓名选项。NO_CHECK：不校验真实姓名;FORCE_CHECK：强校验真实姓名
		params.put("re_user_name", "");//收款用户姓名。当check_name为FORCE_CHECK的时候必填
		params.put("amount", amount);// 因为微信的amount的单位是分，所有需要乘以100
		params.put("desc", desc); //企业付款描述信息
		params.put("spbill_create_ip", ip);// Ip地址

		String sign = PaymentKit.createSign(params, PropKit.get("key"));
		params.put("sign", sign);

		String xmlResult = HttpUtils.postSSL("https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers", PaymentKit.toXml(params), certPath, PropKit.get("mchId"));

        Map<String, String> result = PaymentKit.xmlToMap(xmlResult);
        
        //此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
        String return_code = result.get("return_code");
        //业务结果
        String result_code = result.get("result_code");
        
        if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
            return false;
        }
        if (StrKit.notBlank(result_code) && "SUCCESS".equals(result_code)) {
            
            return true;
        }
        return false;


		
		
		
	}
	
	
	
	/**
	 * 获取请求的IP地址
	 * @param request
	 * @return
	 */
	public static String getRemortIP(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-for") == null) {
			return request.getRemoteAddr(); 
		}
		String ipStr = request.getHeader("x-forwarded-for");
		String[] ipArr = ipStr.split(",");
		String ip = new String();
		for(String i : ipArr){
			if(!"unknown".equals(ip)){
				ip = i;
				break;
			}
		}
		return ip;
	}


}
