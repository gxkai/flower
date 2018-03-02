package com.util;

import java.io.FileInputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.sdk.api.PaymentApi;

/**
 * 微信退款
 * @author glacier
 *
 */
public class wxRefunds {

	/**
	 * 
	 * @author Glacier
	 * @date 2017年7月25日
	 * @param out_refund_no 商户内部退款号 
	 * @param out_trade_no 商户订单号
	 * @param refund_fee 退款金额
	 * @param total_fee 总金额
	 */
	public Map<String, String> Refunds(String out_refund_no,String out_trade_no,String refund_fee,String total_fee) {
	     
	     Map<String,String> params = new HashMap<String,String>();
	     
	     params.put("appid", PropKit.get("appId"));
	     params.put("mch_id", PropKit.get("mchId"));//商户号
	     params.put("out_refund_no", out_refund_no);//商户内部退款号
	     params.put("out_trade_no", out_trade_no);//商户订单号
	     params.put("refund_fee", refund_fee); //退款金额
	     params.put("total_fee", total_fee);//总金额
	   //params.put("transaction_id", "4009472001201707211901857993");//微信订单号      和商户订单号二选一 即可
	     params.put("op_user_id", "admin");
	     																	   //证书地址需要修改  \\usr\\local\\flower\\image\\apiclient_cert.p12
		 //Map<String, String>  result = PaymentApi.refund(params, PropKit.get("key"), "D:\\upload\\flower\\image\\apiclient_cert.p12");
	     Map<String, String>  result = PaymentApi.refund(params, PropKit.get("key"), "//usr//local//flower//image//cert//apiclient_cert.p12");

		 return result;
	    }
	
	
	// 申请退款文档地址：https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_4
	public static String requestUrl = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	
	/**
	 * 微信退款 解析证书
	 * @author Glacier
	 * @date 2017年12月28日
	 * @return
	 * @throws Exception 
	 */
//	public Map<String, String> Refunds2(String out_refund_no,String out_trade_no,String refund_fee,String total_fee) throws Exception {
//		// 证书文件(微信商户平台-账户设置-API安全-API证书-下载证书)
//		String keyStorePath = "D:\\upload\\flower\\image\\apiclient_cert.p12";
//		// 证书密码（默认为商户ID）
//		String password = PropKit.get("mchId");
//		// 实例化密钥库
//		KeyStore ks = KeyStore.getInstance("PKCS12");  
//		// 获得密钥库文件流
//		FileInputStream fis = new FileInputStream(keyStorePath);  
//		// 加载密钥库
//		ks.load(fis, password.toCharArray());
//		// 关闭密钥库文件流
//		fis.close();
//		 
//		// 实例化密钥库
//		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());  
//		// 初始化密钥工厂
//		kmf.init(ks, password.toCharArray());
//		 
//		// 创建SSLContext
//		SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
//		sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
//		// 获取SSLSocketFactory对象
//		SSLSocketFactory ssf = sslContext.getSocketFactory();
//		 
//		URL url = new URL(requestUrl);
//		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//		conn.setRequestMethod("POST");
//		// 设置当前实例使用的SSLSocketFactory
//		conn.setSSLSocketFactory(ssf);
//		
//		Map<String,String> params = new HashMap<String,String>();
//	     
//	     params.put("appid", PropKit.get("appId"));
//	     params.put("mch_id", PropKit.get("mchId"));//商户号
//	     params.put("out_refund_no", out_refund_no);//商户内部退款号
//	     params.put("out_trade_no", out_trade_no);//商户订单号
//	     params.put("refund_fee", refund_fee); //退款金额
//	     params.put("total_fee", total_fee);//总金额
//	   //params.put("transaction_id", "4009472001201707211901857993");//微信订单号      和商户订单号二选一 即可
//	     params.put("op_user_id", "admin");
//		
//		conn.getOutputStream();
//		conn.setDoOutput(true);
//		conn.setDoInput(true);
//		conn.connect();
//		return null;
//	}
	
	
	
	
	
	
	
	
	
}
