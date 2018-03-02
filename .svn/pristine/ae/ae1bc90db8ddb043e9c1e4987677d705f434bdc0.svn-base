package com.controller;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.dao.MCDao;
import com.dao.WLDao;
import com.google.gson.Gson;
import com.jd.open.api.sdk.JdException;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.util.DeliveryDateUtil;
import com.util.JDUtil;
import com.util.SendMsgUtil;

/**
 * @Desc 物流测试
 * @author lxx
 * @date 2016/10/08
 * */
public class WuLiuCtrl extends Controller {
	public static String testurl = "http://query.szdod.com/D2DReceiveOrder.aspx";
	public static String Key = "870110";
	public static String custCode = "C108";
	
	public static String Rtn_Code = "2";
	public static String Rtn_Msg = "失败";
	public static Map<?, ?> jsonPara;
	
	// 挑单-将物流信息同步至门对门
	public void synchro_toD2D(){
		int type = getParaToInt();
		Map<String, Object> chooseMap = DeliveryDateUtil.chooseDate();
		String msg = new String();
		if((boolean) chooseMap.get("result")){
			String dateCode = (String) chooseMap.get("datecode");
			//String holistr = type==1?" and b.type=5":" and b.type<>5";
			// 物流状态(0异常,1正常,2发货,9确认收货)
			// 本批次的物流数量
			Number wlNum = Db.queryNumber("select count(1) from f_order_info a left join f_order b on a.ordercode=b.ordercode where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and  a.code=?" , dateCode);
			// 异常物流数量
			Number ycNum = Db.queryNumber("select count(1) from f_order_info a left join f_order b on a.ordercode=b.ordercode where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and  a.code=? and a.state=0" , dateCode);
			// 已发货的物流数量
			Number yfNum = Db.queryNumber("select count(1) from f_order_info a left join f_order b on a.ordercode=b.ordercode  where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and a.code=? and a.state=2 ", dateCode);
			// 挑单记录
			Number tdNum = Db.queryNumber("select count(1) from f_tiaodan where code=?", dateCode);
			if(wlNum.intValue() > 0){
				if(ycNum.intValue() == 0){
					if(yfNum.intValue() == 0){
						if(type==0){
							if(tdNum.intValue() == 0){
								Record record = new Record().set("code", dateCode).set("ctime", new Date());
								boolean result = Db.save("f_tiaodan", record);
								/*if(WLDao.synchro_toD2D(dateCode, type)){
									msg = "挑单成功";
								}else{
									msg = "挑单同步过程出错,请重试";
								}*/
								if (result) {
									msg = "挑单成功";
								}else {
									msg = "挑单失败";
								}
								
							}else{
								msg = "批次"+dateCode+"挑单已完成,无法重复挑单";
							}
						}else{
							/*if(WLDao.synchro_toD2D(dateCode, type)){
								msg = "挑单成功";
							}else{
								msg = "挑单同步过程出错,请重试";
							}*/
							Record record = new Record().set("code", dateCode).set("ctime", new Date());
							boolean result = Db.save("f_tiaodan", record);
							
							if (result) {
								msg = "挑单成功";
							}else {
								msg = "挑单失败";
							}
						}
					}else{
						msg = "批次"+dateCode+"已经发货,无法重复挑单";
					}
				}else{
					msg = "批次"+dateCode+"存在异常物流信息,挑单失败";
				}
			}else{
				msg = "批次"+dateCode+"尚未配单,无法挑单";
			}
		}else{
			msg = "无单可挑！";
		}
		renderJson("{\"msg\":\""+msg+"\"}");
	}
	
	// 接收门对门物流信息完善回调
	public void perfect_ajax(){
		String jsonStr = HttpKit.readData(getRequest());	
		Gson gson = new Gson();
		Map<?, ?> jsonPara = gson.fromJson(jsonStr, HashMap.class);
		Map<String, Object> jsonMap = new HashMap<>();
		String Rtn_Code = "2";
		String Rtn_Msg = "失败";
		try{
			String WorkCode = (String) jsonPara.get("WorkCode");		// 条码号
			String eCode = (String) jsonPara.get("eCode");				// 物流公司编码
			String WorkArea = (String) jsonPara.get("WorkArea");		// 仓储区域
			String WorkStr = (String) jsonPara.get("WorkNumber");		// 仓储编码
			Integer WorkNumber = null;
			try{
				WorkNumber = Integer.parseInt(WorkStr);	// 仓储编码
			}catch(NumberFormatException e){
				
			}
			Record orderinfo = Db.findFirst("select id from f_order_info where state =1 and number = ?", WorkCode);
			if(orderinfo != null){
				Record express = Db.findFirst("select name from f_express where code=?", eCode);	// 获取物流平台名称
				if(express != null){
					orderinfo.set("ename", express.getStr("name"));
					orderinfo.set("ecode", eCode);
					orderinfo.set("workarea", WorkArea);		// 仓储区域
					if(eCode.trim().equals("d2d")==true){
						orderinfo.set("lognumber", WorkCode);
					}
					if(eCode.trim().equals("jd")==true){
						try {
					        JSONArray datajd = JDUtil.getJDdata(1);
							orderinfo.set("lognumber", datajd.get(0));
						} catch (JdException e) {
							e.printStackTrace();
						}
					}
					if(WorkNumber != null){
						orderinfo.set("worknumber", WorkNumber);	// 仓储编码
					}
					if(Db.update("f_order_info", orderinfo)){
						Rtn_Code = "1";
						Rtn_Msg = "成功";
					}
				}
			}
		}catch(NullPointerException e){}
		jsonMap.put("Rtn_Code", Rtn_Code);
		jsonMap.put("Rtn_Msg", Rtn_Msg);
		renderJson("[" + JsonKit.toJson(jsonMap) + "]");
	}
	
	// 接收门对门推送的订单轨迹接口
	public void notify_ajax() {
		String jsonStr = HttpKit.readData(getRequest());	
		Gson gson = new Gson();
		jsonPara = gson.fromJson(jsonStr, HashMap.class);
		Map<String, Object> jsonMap = new HashMap<>();
		Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean R = false;
				String OrderStatusCode = new String();
				String ClientCode = new String();
				String WorkCode=new String();
				try{
					ClientCode = (String) jsonPara.get("ClientCode");	// 订单号
					WorkCode = (String) jsonPara.get("WorkCode");		// 条码号
					String OpDateTime = (String) jsonPara.get("OpDateTime");	// 操作时间
					OrderStatusCode = (String) jsonPara.get("OrderStatusCode");	// 订单轨迹状态代码
					String OpDescription = (String) jsonPara.get("OpDescription");	// 轨迹的具体描述
					Record logistics = new Record();
					logistics.set("ClientCode", ClientCode);
					logistics.set("WorkCode", WorkCode);
					logistics.set("OpDateTime", OpDateTime);
					logistics.set("OrderStatusCode", OrderStatusCode);
					logistics.set("OpDescription", OpDescription);
					logistics.set("ctime", new Date());
					R = Db.save("f_logistics", logistics);
				}catch(NullPointerException e){}
				if(R){
					Rtn_Code = "1";
					Rtn_Msg = "成功";
					if("46".equals(OrderStatusCode)){
						Db.update("update f_order set state=2 where ordercode=? and ocount=cycle", ClientCode);//订单待评价
						Db.update("update f_order_info set state=9 where number=?", WorkCode);//物流单已签收
						SendMsg(ClientCode);
					}
				}
				return R;
			}
		});
		jsonMap.put("Rtn_Code", Rtn_Code);
		jsonMap.put("Rtn_Msg", Rtn_Msg);
		renderJson("[" + JsonKit.toJson(jsonMap) + "]");
	}


	/**
	 * 服务完成时给客户发模板消息和短信，希望再次续订
	 * 服务完成是指：订阅次数=发货次数
	 * @param ordercode 订单编号
	 */
	 private void SendMsg(String ordercode){
	    	
			Record order=Db.findFirst("select a.ordercode,a.cycle,a.aid,a.ocount,a.tel,date_format(a.ctime,'%Y-%m-%d') 'ctime',d.code_name from f_order as a left join f_order_detail as b on a.ordercode=b.ordercode left join f_flower_pro as c on b.fpid=c.id left join f_dictionary as d on c.ptid=d.code_value where a.ordercode=?", ordercode);
			
			/*String maxTime=Db.queryStr("select  DATE_FORMAT(Max(ctime),'%Y-%m-%d') 'maxTime'  from f_order_info where ordercode=?",ordercode);
			String minTime=Db.queryStr("select  DATE_FORMAT(Min(ctime),'%Y-%m-%d') 'minTime'  from f_order_info where ordercode=?", ordercode);*/
			
			String minTime=order.getStr("ctime");//下单时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String maxTime= sdf.format( new Date());//最后一次收花时间，此处用签收时间
			if (order.getInt("cycle") == order.getInt("ocount")){
				com.util.SendMouldMsgUtil.SendMsgWhereLast(order.getInt("cycle"),order.getInt("aid"),order.getStr("ordercode"),order.getStr("code_name"),minTime,maxTime);
				try {
					//(20171109日倪工通知停掉)
					/*if(order.getStr("tel")!=null && order.getStr("tel").equals("")==false){
						SendMsgUtil.sendMsg(order.getStr("tel"), String.format("亲爱的花粉，您订购的鲜花本周将完成最后一次配送，续订花美美，让我们永远生活在漫花飞舞的世界里。", order.getInt("cycle")));
					}*/
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//每次都发【服务到期提醒】短信
			}
	    }
}


