package com.util;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.request.etms.EtmsWaybillSendRequest;
import com.jd.open.api.sdk.request.etms.EtmsWaybillcodeGetRequest;
import com.jd.open.api.sdk.request.etms.LdopReceiveTraceGetRequest;
import com.jd.open.api.sdk.response.etms.EtmsWaybillSendResponse;
import com.jd.open.api.sdk.response.etms.EtmsWaybillcodeGetResponse;
import com.jd.open.api.sdk.response.etms.LdopReceiveTraceGetResponse;

public class JDUtil {
	static  String SERVER_URL = "https://api.jd.com/routerjson";
	static  String accessToken = "ce3d5233-01fe-4561-8663-988382dbe4ab";
	static  String appKey = "9EB5069ACDB9A3826E7AAD1CCFE3C697";
	static  String appSecret = "cedd0a9e94634536ab20f5516806a244";
	static  String CustomerCode = "021K74255";
	static  String SalePlat = "0030001";
	static  int  OrderType = 0;
	static  String SenderName = "花小美";
	static  String SenderAddress ="江苏省苏州市昆山市张浦镇新吴街185号";
	static  String SenderMobile = "18012698246";
	static  String senderTel = "0512-55253790";
	public static JSONArray getJDdata(int jdNumber) throws JdException {
		JdClient client=new DefaultJdClient(SERVER_URL,accessToken,appKey,appSecret);
		EtmsWaybillcodeGetRequest request=new EtmsWaybillcodeGetRequest();
		request.setPreNum(String.valueOf(jdNumber)); 
		request.setCustomerCode(CustomerCode); 
		request.setOrderType(OrderType); 
		EtmsWaybillcodeGetResponse 	response=client.execute(request);
		JSONObject object = JSON.parseObject(response.getMsg());
		JSONObject object2 = (JSONObject) object.get("jingdong_etms_waybillcode_get_responce");
		JSONObject object3 = (JSONObject) object2.get("resultInfo");
		JSONArray data = (JSONArray) object3.get("deliveryIdList");
		return data;
	}
	public static void jiedan(String DeliveryId,String ordercode,String ReceiveName,String ReceiveAddress,String ReceiveMobile,String Description ) throws JdException {
		JdClient client=new DefaultJdClient(SERVER_URL,accessToken,appKey,appSecret);
		 EtmsWaybillSendRequest request=new EtmsWaybillSendRequest();
		 request.setDeliveryId(DeliveryId); //京东订单
		 request.setSalePlat(SalePlat);  
		 request.setCustomerCode(CustomerCode);
		 request.setOrderId(ordercode);//关联订单
		 request.setSenderName(SenderName); 
		 request.setSenderAddress(SenderAddress);
		 request.setSenderMobile(SenderMobile);
		 request.setSenderTel(senderTel);
		 //收件人信息
		 request.setReceiveName(ReceiveName);//姓名
		 request.setReceiveAddress(ReceiveAddress);//地址 
		 request.setReceiveMobile(ReceiveMobile);//手机
		 request.setPackageCount(1); //包裹数量
		 request.setWeight(1.00);//重量 
		 request.setDescription(Description);//描述
		 request.setVloumn(0.00);
		 request.setGoodsType(2);//配送产品类型（1：普通  2：生鲜常温 3：填仓 4：特配 5：鲜活 6：控温 7：冷藏 8：冷冻 9：深冷）
		 request.setRemark(Description);//备注
		 EtmsWaybillSendResponse response=client.execute(request);
		 System.out.println(response.getMsg());
	}
	public static JSONArray getJDlogisticdata(String lognumber) throws JdException {
		JdClient client=new DefaultJdClient(SERVER_URL,accessToken,appKey,appSecret);
		LdopReceiveTraceGetRequest request=new LdopReceiveTraceGetRequest();
		request.setCustomerCode(CustomerCode); 
		request.setWaybillCode(lognumber);
		LdopReceiveTraceGetResponse response=client.execute(request);
		JSONObject object = JSON.parseObject(response.getMsg());
		System.out.println(object);
		JSONObject object2 = (JSONObject) object.get("jingdong_ldop_receive_trace_get_responce");
		System.out.println(object2);
		JSONObject object3 = (JSONObject) object2.get("querytrace_result");
		System.out.println(object3);
		JSONArray data    = (JSONArray) object3.get("data");
		System.out.println(data);
		return data;
	}
}
