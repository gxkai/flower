package com.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.helpers.DateTimeDateFormat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.request.etms.EtmsRangeCheckRequest;
import com.jd.open.api.sdk.request.etms.EtmsWaybillSendRequest;
import com.jd.open.api.sdk.request.etms.EtmsWaybillcodeGetRequest;
import com.jd.open.api.sdk.request.etms.LdopReceiveTraceGetRequest;
import com.jd.open.api.sdk.response.etms.EtmsRangeCheckResponse;
import com.jd.open.api.sdk.response.etms.EtmsWaybillSendResponse;
import com.jd.open.api.sdk.response.etms.EtmsWaybillcodeGetResponse;
import com.jd.open.api.sdk.response.etms.LdopReceiveTraceGetResponse;

import sun.text.resources.cldr.FormatData;

public class Jos {
   	static  String SERVER_URL = "https://api.jd.com/routerjson";
	static  String accessToken = "ce3d5233-01fe-4561-8663-988382dbe4ab";
	static  String appKey = "9EB5069ACDB9A3826E7AAD1CCFE3C697";
	static  String appSecret = "cedd0a9e94634536ab20f5516806a244";
	static  String CustomerCode = "021K74255";
	static  String SalePlat = "苏州花美美";
	static  String SenderMobile = "18012698246";
	static  String senderTel = "0512-55253790";
	public static void main(String[] args) throws JdException, ParseException {
		/**
		 * 获取京东物流单号
		 */
		   /* String SERVER_URL = "https://api.jd.com/routerjson";
		    String accessToken = "ce3d5233-01fe-4561-8663-988382dbe4ab";
		    String appKey = "9EB5069ACDB9A3826E7AAD1CCFE3C697";
		    String appSecret = "cedd0a9e94634536ab20f5516806a244";
			JdClient client=new DefaultJdClient(SERVER_URL,accessToken,appKey,appSecret);
			EtmsWaybillcodeGetRequest request=new EtmsWaybillcodeGetRequest();
			request.setPreNum("1"); 
			request.setCustomerCode("021K74255"); 
			request.setOrderType(0); 
			EtmsWaybillcodeGetResponse 	response=client.execute(request);
			JSONObject object = JSON.parseObject(response.getMsg());
			System.out.println(object);
			JSONObject object2 = (JSONObject) object.get("jingdong_etms_waybillcode_get_responce");
			System.out.println(object2);
			JSONObject object3 = (JSONObject) object2.get("resultInfo");
			System.out.println(object3);
			JSONArray data = (JSONArray) object3.get("deliveryIdList");
			System.out.println(data);
			System.out.println(data.get(0));*/

			
		/**
		 * 是否可以京配
		 */
	    /*JdClient client=new DefaultJdClient(SERVER_URL,accessToken,appKey,appSecret);
		EtmsRangeCheckRequest request=new EtmsRangeCheckRequest();
		request.setSalePlat(SalePlat); 
		request.setCustomerCode(CustomerCode); 
		request.setOrderId("20170728175145726");
		request.setGoodsType(7);
		request.setReceiveAddress("江苏省南京栖霞区南大和园65栋205室");
		request.setSenderProvinceId(12);
		request.setSenderCityId(988);
		request.setSenderCountyId(47821);
		request.setSenderTownId(48034); 
		request.setIsCod(0); 
		EtmsRangeCheckResponse response=client.execute(request);
		System.out.println(response);*/
		/*
		 * 青龙接单
		 */
		/* JdClient client=new DefaultJdClient(SERVER_URL,accessToken,appKey,appSecret);
		 EtmsWaybillSendRequest request=new EtmsWaybillSendRequest();
		 request.setDeliveryId("VB35731004960"); 
		 request.setSalePlat("0030001");
		 request.setCustomerCode("021K74255");
		 request.setOrderId("20170728175145726");
		 request.setThrOrderId("20170728175145726"); 
		 request.setSelfPrintWayBill(1);
		 request.setPickMethod("1");
		 request.setPackageRequired("1");
		 request.setSenderName("花美美"); 
		 request.setSenderAddress("江苏省苏州市昆山市张浦镇新吴街185号");
		 request.setSenderMobile("15995628715");
		 request.setReceiveName("李苗");
		 request.setReceiveAddress("江苏省-苏州-姑苏区-传化物流港L506盛仕达物流"); 
		 request.setReceiveMobile("13375184455");
		 request.setPackageCount(1);
		 request.setWeight(0.10); 
		 request.setVloumn(0.00);
		 request.setDescription("鲜花或鲜花周边");
		 request.setGoodsType(7);
		 EtmsWaybillSendResponse response=client.execute(request);
		 JSONObject object = JSON.parseObject(response.getMsg());
		System.out.println(object);
		JSONObject object2 = (JSONObject) object.get("jingdong_etms_waybill_send_responce");
		System.out.println(object2);
		JSONObject object3 = (JSONObject) object2.get("resultInfo");
		System.out.println(object3);
		JSONObject data = (JSONObject) object3.get("code");
		System.out.println(data);
		System.out.println(data.get(0));*/
		/**
		 * 全程物流追踪
		 */
		/*JdClient client=new DefaultJdClient(SERVER_URL,accessToken,appKey,appSecret);
		LdopReceiveTraceGetRequest request=new LdopReceiveTraceGetRequest();
		request.setCustomerCode(CustomerCode); 
		request.setWaybillCode("VB36341393756");
		LdopReceiveTraceGetResponse response=client.execute(request);
		JSONObject object = JSON.parseObject(response.getMsg());
		System.out.println(object);
		JSONObject object2 = (JSONObject) object.get("jingdong_ldop_receive_trace_get_responce");
		System.out.println(object2);
		JSONObject object3 = (JSONObject) object2.get("querytrace_result");
		System.out.println(object3);
		JSONArray data    = (JSONArray) object3.get("data");
		System.out.println(data);*/
	}

}
