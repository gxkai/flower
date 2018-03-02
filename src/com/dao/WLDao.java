package com.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import com.google.gson.Gson;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.utils.HttpUtils;
import com.util.Constant.orderType;
import com.util.Constant.singlews;
import com.util.DeliveryDateUtil;
import com.util.MD5;
import com.util.SendMsgUtil;
import com.ws.ChatAnnotation;
import com.ws.ChatAnnotation_ZB;
import com.ws.ChatAnnotation_pre;

/**
 * @Desc 物流数据接口
 * */
public class WLDao {
	// websockt钥匙
	static String code;
	// 日期编码
	static String dateCode;
	// 需要配单的订单集合
	static List<Record> list;
	// websockt通信信息
	static Map<String, Object> resultMap;
	// 发货之物流条件
	static String condition;
	// 双品花束
	static List<Record> spList;//适合所有订单
	static List<Record> spList_sdsc;//适合【首单首次】订单
	static int sp_sdsc_index;
	// 双品花束分配游标
	static int sp_index;
	// 多品花束
	static List<Record> dpList;
	// 多品花束分配游标
	static int dp_index;
	//玫瑰套餐
	static List<Record> wgList;//适合玫瑰套餐的所有产品
	static int wg_index;//玫瑰套餐分配游标
	
	
	static List<Record> dzList;//定制花束本批次配置的产品列表
	
	
	// 节日发货
	static int Lx;
	
	public static String testurl = "http://query.szdod.com/D2DReceiveOrder.aspx";
	public static String Key = "870110";
	public static String custCode = "C108";
	
	/**
	 *生成周边物流单
	 *增量配，不删除
	 */
	public static void singleWL_ZB(String key) {
		code = key;
		list=Db.find("select a.id,a.ordercode,a.aid,a.name,a.tel,a.addr,a.cycle,a.isvase,a.jh_list,a.jh_color,a.type,a.szdx,a.ocount,a.ishas,a.is_sy,a.sy_code,b.num,a.style,b.piCode from "
				+ "f_order as a left join f_picode as b on a.ordercode=b.ordercode "
				+ " where a.state=1 and  a.type in(3,43) and not exists(select c.ordercode from f_order_info as c where a.ordercode=c.ordercode ) ");
		if(list.size() == 0){
			resultMap = new HashMap<>();
			resultMap.put("code", singlews.CODE0.code);
			resultMap.put("msg", "&nbsp;没有新增的订单</span>");
			ChatAnnotation_ZB.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
			return;
		}
		resultMap = new HashMap<>();
		resultMap.put("code", singlews.CODE1.code);
		resultMap.put("msg", list.size());
		ChatAnnotation_ZB.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
		
		//配单是个【原子操作】，要么成功，要么不成功
		boolean tx_result = Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean oi_result = false;
				String orderError="";//问题订单，当发生异常时跳出单号，以便查到问题解决问题。现在通过打断点调试，以后空的时候可以直接写成日志
			    try {
					for(int i=0;i<list.size();i++){
						Record order = list.get(i);
						dateCode=order.getStr("piCode");
						oi_result = saveOrderInfo_zb(order,0);
						
						if(oi_result){
							resultMap = new HashMap<>();
							resultMap.put("code", singlews.CODE2.code);
							resultMap.put("msg", i+1);
							ChatAnnotation_ZB.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
						}else{
							break;
						}
					}
					
				} catch (Exception e) {
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE4.code);
					resultMap.put("msg", "<span>配单出错，操作终止</span>"+orderError);
					ChatAnnotation_ZB.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
					return false;
				}
			    return oi_result;
				
			}
		});
		if(tx_result){
			resultMap = new HashMap<>();
			resultMap.put("code", singlews.CODE3.code);
			ChatAnnotation_ZB.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
		}else{
			resultMap = new HashMap<>();
			resultMap.put("code", singlews.CODE4.code);
			resultMap.put("msg", "<span>配单出错，操作终止</span>");
			ChatAnnotation_ZB.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息*/
		}
	
	
	}
	
	
	
	
	/**
	 * @Desc 物流配单,只配鲜花
	 * @param key 4位随机码
	 * */
	public static void singleWL(String key) {
		code = key;
		Map<String, Object> chooseMap = DeliveryDateUtil.chooseDate();
		// 判断是否在可配单时间范围内
		if((boolean) chooseMap.get("result")){
			dateCode = (String) chooseMap.get("datecode");//发货批号
			//本批次的产品数量
			Number fpnum = Db.queryNumber("select count(1) from f_product where code = ?", dateCode);
			if(fpnum.intValue() == 0){
				
				resultMap = new HashMap<>();
				resultMap.put("code", singlews.CODE0.code);
				resultMap.put("msg", "<span>批次&nbsp;" + dateCode + "&nbsp;鲜花产品尚未选配</span>");
				ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
			}else{
				Number oinum = Db.queryNumber("select count(1) from f_order_info a  where not EXISTS (select b.ordercode from f_order as  b  where a.ordercode=b.ordercode and b.type in(3,43)) and a.state>=2 and a.code=?", dateCode);
				if(oinum.intValue() > 0){	// 发货已完成
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE0.code);
					resultMap.put("msg", "<span>批次&nbsp;" + dateCode + "&nbsp;发货已完成</span>");
					ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
				}else{
					list=Db.find("select a.id,a.ordercode,a.aid,a.name,a.tel,a.addr,a.cycle,a.isvase,a.jh_list,a.jh_color,a.type,a.szdx,a.ocount,a.ishas,a.is_sy,a.sy_code,b.num,a.style from "
							+ "f_order as a left join f_picode as b on a.ordercode=b.ordercode "
							+ " where a.state=1 and  b.piCode=? and a.type not in(3,43) ",dateCode);
					if(list.size() == 0){
						resultMap = new HashMap<>();
						resultMap.put("code", singlews.CODE0.code);
						resultMap.put("msg", "<span>批次&nbsp;" + dateCode + "&nbsp;没有新增的订单</span>");
						ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
						return;
					}
					/*if(!flowerPriceValid(dateCode)){
						resultMap = new HashMap<>();
						resultMap.put("code", singlews.CODE0.code);
						resultMap.put("msg", "<span>批次&nbsp;" + dateCode + "&nbsp;花材价格不完善</span>");
						ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
						return;
					}*/
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE1.code);
					resultMap.put("msg", list.size());
					ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
					
					//配单是个【原子操作】，要么成功，要么不成功
					boolean tx_result = Db.tx(new IAtom() {
						@Override
						public boolean run() throws SQLException {
							boolean oi_result = false;
							String orderError="";//问题订单，当发生异常时跳出单号，以便查到问题解决问题。现在通过打断点调试，以后空的时候可以直接写成日志
						    try {
						    	Db.update("delete from f_order_info where code = ?",dateCode);
								// 获取待分配产品信息
								getProMap();
								for(int i=0;i<list.size();i++){
									Record order = list.get(i);
									orderError=order.getStr("ordercode");
									Number preNum = Db.queryNumber("select count(1) from f_order_info_pre where code = ? and ordercode = ?",dateCode,orderError);
									if (preNum.intValue()>0) {
										oi_result = copyPre(order);
										if(oi_result){
											resultMap = new HashMap<>();
											resultMap.put("code", singlews.CODE2.code);
											resultMap.put("msg", i+1);
											ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
										}else{
											break;
										}
									}else {
									// 订阅,兑换订阅鲜花，多双交替
										if(order.getInt("type") == orderType.TYPE1.type ||order.getInt("type") == orderType.TYPE7.type||order.getInt("type") == orderType.TYPE9.type){	
											oi_result = saveOrderInfo_dy(order,0);
										}else if(order.getInt("type") == orderType.TYPE2.type||order.getInt("type") == orderType.TYPE13.type){   // 送花，兑换送花
											oi_result = saveOrderInfo_sh(order,0);
										}else if(order.getInt("type") == orderType.TYPE3.type || order.getInt("type") == orderType.TYPE8.type){   // 周边，兑换周边
											oi_result = saveOrderInfo_zb(order,0);
										}else if(order.getInt("type") == orderType.TYPE5.type||order.getInt("type") == orderType.TYPE14.type){  //节日送花，兑换节日送花
											oi_result=saveOrderInfo_jr(order,0,4,dateCode);
										}else if(order.getInt("type") == orderType.TYPE10.type||order.getInt("type") == orderType.TYPE15.type){  //主题花，兑换主题花
											oi_result=saveOrderInfo_jr(order,0,7,dateCode);
										}else if(order.getInt("type") == orderType.TYPE11.type||order.getInt("type") == orderType.TYPE16.type){  //闪购花,兑换闪购花
											oi_result=saveOrderInfo_jr(order,0,6,dateCode);
										}else if(order.getInt("type") == orderType.TYPE12.type){  //拼团花束
											oi_result=saveOrderInfo_jr(order,0,8,dateCode);
										}
										else if(order.getInt("type") == orderType.TYPE6.type){  //定制花束
											oi_result=saveOrderInfo_dz(order,0);
										}
										if(oi_result){
											resultMap = new HashMap<>();
											resultMap.put("code", singlews.CODE2.code);
											resultMap.put("msg", i+1);
											ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
										}else{
											break;
										}
									}	
								}
								
							} catch (Exception e) {
								resultMap = new HashMap<>();
								resultMap.put("code", singlews.CODE4.code);
								resultMap.put("msg", "<span>配单出错，操作终止</span>"+orderError);
								ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
								return false;
							}
						    return oi_result;
							
							
						}
					});
					if(tx_result){
						resultMap = new HashMap<>();
						resultMap.put("code", singlews.CODE3.code);
						ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
					}else{
						resultMap = new HashMap<>();
						resultMap.put("code", singlews.CODE4.code);
						resultMap.put("msg", "<span>配单出错，操作终止</span>");
						ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息*/
					}
				}
			}
		}else{	// 不在可配单时间范围内
			resultMap = new HashMap<>();
			resultMap.put("code", singlews.CODE0.code);
			resultMap.put("msg", "<span>截单后,配单前，请在数据字典里正确设置【发货批次】</span>");
			ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
		}
	}
	
	/**
	 * 预配单
	 * 预配单应该在正式配单之前使用。
	 * 否则，发货次数和异常单 会存在差异。
	 */
	public static void singleWL_pre(String key) {
		code = key;
		Map<String, Object> chooseMap = DeliveryDateUtil.choosedate();
		// 判断是否在可配单时间范围内
		if((boolean) chooseMap.get("result")){
			dateCode = (String) chooseMap.get("datecode");//发货批号
			Number ispro = Db.queryNumber("select count(1) from f_order_info as a  where code=? "
					+ " and not EXISTS ( select * from f_order as b where a.ordercode=b.ordercode and b.type in(3,43) ) ", dateCode);
			if(ispro.intValue()>0){
				resultMap = new HashMap<>();
				resultMap.put("code", singlews.CODE0.code);
				resultMap.put("msg", "<span>批次&nbsp;" + dateCode + "&nbsp;已经配过单,不能再预配</span>");
				ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
				return;
			}
			Number oinum = Db.queryNumber("select count(1) from f_order_info_pre where state>=2 and code=?", dateCode);
			if(oinum.intValue() > 0){	// 发货已完成
				resultMap = new HashMap<>();
				resultMap.put("code", singlews.CODE0.code);
				resultMap.put("msg", "<span>批次&nbsp;" + dateCode + "&nbsp;发货已完成</span>");
				ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
			}else{
				list=Db.find("select a.style,a.id,a.ordercode,a.aid,a.name,a.tel,a.addr,a.cycle,a.isvase,a.jh_list,a.jh_color,a.type,a.szdx,a.ocount,a.ishas,a.is_sy,a.sy_code,b.num from "
						+ "f_order as a left join f_picode as b on a.ordercode=b.ordercode "
						+ " where a.state=1 and  b.piCode=? and a.type not in(3,43)  ",dateCode);
				if(list.size() == 0){
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE0.code);
					resultMap.put("msg", "<span>批次&nbsp;" + dateCode + "&nbsp;没有符合条件的订单</span>");
					ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
					return;
				}
				/*if(!flowerPriceValid(dateCode)){
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE0.code);
					resultMap.put("msg", "<span>批次&nbsp;" + dateCode + "&nbsp;花材价格不完善</span>");
					ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
					return;
				}*/
				resultMap = new HashMap<>();
				resultMap.put("code", singlews.CODE1.code);
				resultMap.put("msg", list.size());
				ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
				
				//配单是个【原子操作】，要么成功，要么不成功
				boolean tx_result = Db.tx(new IAtom() {
					@Override
					public boolean run() throws SQLException {
						boolean oi_result = false;
						String orderError="";//问题订单，当发生异常时跳出单号，以便查到问题解决问题。现在通过打断点调试，以后空的时候可以直接写成日志
					    try {
							Db.update("delete from f_order_info_pre where code=?", dateCode);
							// 获取待分配产品信息
							getProMap();
							for(int i=0;i<list.size();i++){
								Record order = list.get(i);
								orderError=order.getStr("ordercode");
								try {
									if(order.getInt("type") == orderType.TYPE1.type ||order.getInt("type") == orderType.TYPE7.type||order.getInt("type") == orderType.TYPE9.type){	// 订阅,兑换订阅鲜花
										oi_result = saveOrderInfo_dy(order,1);
									}else if(order.getInt("type") == orderType.TYPE2.type||order.getInt("type") == orderType.TYPE13.type){   // 送花，兑换送花
										oi_result = saveOrderInfo_sh(order,1);
									}else if(order.getInt("type") == orderType.TYPE5.type||order.getInt("type") == orderType.TYPE14.type){  //节日送花，兑换节日送花
										oi_result=saveOrderInfo_jr(order,1,4,dateCode);
									}else if(order.getInt("type") == orderType.TYPE10.type||order.getInt("type") == orderType.TYPE15.type){  //主题花，兑换主题花
										oi_result=saveOrderInfo_jr(order,1,7,dateCode);
									}else if(order.getInt("type") == orderType.TYPE11.type||order.getInt("type") == orderType.TYPE16.type){  //闪购花,兑换闪购花
										oi_result=saveOrderInfo_jr(order,1,6,dateCode);
									}else if(order.getInt("type") == orderType.TYPE12.type){  //拼团花束
										oi_result=saveOrderInfo_jr(order,1,8,dateCode);
									}
									else if(order.getInt("type") == orderType.TYPE6.type){  //定制花束
										oi_result=saveOrderInfo_dz(order,1);
									}
								} catch (Exception e) {
									System.out.println(e.getMessage());
								}
								
								if(oi_result){
									resultMap = new HashMap<>();
									resultMap.put("code", singlews.CODE2.code);
									resultMap.put("msg", i+1);
									ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
								}else{
									break;
								}
							}
							
						} catch (Exception e) {
							// TODO: handle exceptionorderError
							resultMap = new HashMap<>();
							resultMap.put("code", singlews.CODE4.code);
							resultMap.put("msg", "<span>配单出错，操作终止</span>"+orderError);
							ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
							return false;
						}
					    return oi_result;
						
						
					}
				});
				if(tx_result){
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE3.code);
					ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
				}else{
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE4.code);
					resultMap.put("msg", "<span>配单出错，操作终止</span>");
					ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息*/
				}
			}
		
		}else{	// 不在可配单时间范围内
			resultMap = new HashMap<>();
			resultMap.put("code", singlews.CODE0.code);
			resultMap.put("msg", "<span>截单后,配单前，请在数据字典里正确设置【发货批次】</span>");
			ChatAnnotation_pre.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
		}
	}
	/**
	 * 复制预配单到配单
	 */
	public static boolean copyPre(Record order) {
		boolean result;
		Record preRecord = Db.findFirst("select *from f_order_info_pre a where  a.ordercode = ? and code = ? and EXISTS (SELECT b.piCode FROM f_picode b WHERE b.ordercode = a.ordercode and b.piCode = a.code )   ",order.getStr("ordercode"),dateCode);
		Record infoRecord = new Record();
		infoRecord.set("code", preRecord.getStr("code"));
		infoRecord.set("ordercode", preRecord.getStr("ordercode"));
		infoRecord.set("number", preRecord.getStr("number"));
		infoRecord.set("ename", preRecord.getStr("ename"));
		infoRecord.set("ecode", preRecord.getStr("ecode"));
		infoRecord.set("workarea", preRecord.getStr("workarea"));
		infoRecord.set("worknumber", preRecord.getStr("worknumber"));
		infoRecord.set("name", preRecord.getStr("name"));
		infoRecord.set("tel", preRecord.getStr("tel"));
		infoRecord.set("address", preRecord.getStr("address"));
		infoRecord.set("aid", preRecord.getInt("aid"));
		infoRecord.set("ishas", preRecord.getInt("ishas"));
		infoRecord.set("ofcycle",preRecord.getInt("ofcycle"));
		infoRecord.set("ctime", preRecord.getTimestamp("ctime"));
		infoRecord.set("state", preRecord.getInt("state"));
		infoRecord.set("lognumber", preRecord.getStr("lognumber"));
		result= Db.save("f_order_info", infoRecord);
		if (!result) {
			 return result;
		}
		Integer infoid = Db.queryInt("select id from f_order_info where ordercode = ? and code = ?",preRecord.getStr("ordercode"),dateCode);
		List<Record>pRecords = Db.find("select pid,type,num from f_order_pro_pre where oid = ?",preRecord.getInt("id"));
		for(Record pRecord:pRecords ) {
			Record proRecord = new Record();
			proRecord.set("oid", infoid);
			proRecord.set("pid", pRecord.getInt("pid"));
			proRecord.set("type", pRecord.getInt("type"));
			proRecord.set("num", pRecord.getInt("num"));
			result=Db.save("f_order_pro", proRecord);
			if (!result) {
			    break;
			}
		}
	    return result;
	}
	// 获取待分配产品信息
	/**
	 * 
	 */
	public static void getProMap(){
		spList = Db.find("select id,style from f_product where type=1 and code=? and appoint=0", dateCode);// 双品花束(适合所有订单)
		spList_sdsc=Db.find("select id,style from f_product where type=1 and code=? and appoint=1", dateCode);// 双品花束(适合首单首次订单)
		dpList = Db.find("select id,style from f_product where type=2 and code=?", dateCode);// 多品花束
		dzList=Db.find("select id,fpid from f_product where type=5 and code=? order by priority desc",dateCode);//定制花束
		
		wgList=Db.find("select id,fpid,style from f_product where type=9 and code=? ",dateCode);//玫瑰套餐花束
		
		for(int i=0;i<spList.size();i++){
			// 添加计数标识
			spList.get(i).set("c_index", i);
		}
		for(int i=0;i<spList_sdsc.size();i++){
			// 添加计数标识
			spList_sdsc.get(i).set("c_index", i);
		}
		
		for(int i=0;i<dpList.size();i++){
			// 添加计数标识
			dpList.get(i).set("c_index", i);
		}
		
		for(int i=0;i<wgList.size();i++){
			// 添加计数标识
			wgList.get(i).set("c_index", i);
		}
		sp_index = 0;
		dp_index = 0;
		sp_sdsc_index=0;
		wg_index=0;
	}
	
	/**
	 * @Desc 订阅1
	 * */
	public static boolean saveOrderInfo_dy(Record order,int ispre){
		// 订单编号
		String ordercode = order.getStr("ordercode");
		// 订阅周期
		int cycle = order.getInt("cycle");
		// 已发次数
		int ocount = order.getInt("ocount");
		// 获取分享订单
		Record share = Db.findFirst("select id,ordercode,aid,name,tel,addr,cycle,ocount from f_share where aid is not null and ordercode=? and ocount < cycle", ordercode);
		return dingyue(1, order, share, cycle-ocount,ispre);
	}
	
	/**
	 * @Desc 送花2
	 * */
	public static boolean saveOrderInfo_sh(Record order,int ispre){
		boolean result = false;
		// 获取本批次产品
		List<Record> proList = Db.find("SELECT b.id FROM f_order_detail a LEFT JOIN f_product b ON a.fpid=b.fpid WHERE a.ordercode=? AND a.type=1 AND b.code=? AND b.type=3 AND b.iid=?", order.getStr("ordercode"), dateCode, order.getInt("szdx"));
		if(proList.size() > 0){
			// 创建物流信息
			Map<String, Object> map = saveOrderInfo(order.getStr("ordercode"), order.getStr("name"), order.getStr("tel"), order.getStr("addr"), order.getInt("aid"), 1, order.getInt("ishas"),ispre);
			if((boolean) map.get("result")){
				// 保存物流详情
				result = saveOrderPro((int) map.get("id"), randomId(3, proList,false), 0,1,ispre);
				
				// 判断是否购买花瓶(换购之后,副品可能是多个)
				if(order.getInt("isvase")==1){
					List<Record> vaseList=Db.find("select fpid from f_order_detail where ordercode=? and type=2", order.getStr("ordercode"));
					// 保存物流详情
					for (Record record : vaseList) {
						result = saveOrderPro((int) map.get("id"), record.getInt("fpid"), 1,1,ispre);
					}
					
				}
			}
		}else{
			// 异常订单处理
			Map<String, Object> ycMap = saveOrderInfo(order.getStr("ordercode"), order.getStr("name"), order.getStr("tel"), order.getStr("addr"), order.getInt("aid"), 0, 1,ispre);
			result = (boolean) ycMap.get("result");
		}
		return result;
	}
	
	/**
	 * @Desc 周边3
	 * */
	public static boolean saveOrderInfo_zb(Record order,int ispre){
		boolean result = false;
		// 创建物流信息
		Map<String, Object> map = saveOrderInfo(order.getStr("ordercode"), order.getStr("name"), order.getStr("tel"), order.getStr("addr"), order.getInt("aid"), 1, order.getInt("ishas"),ispre);
		if((boolean) map.get("result")){
			Record rd = Db.findFirst("select fpid,num from f_order_detail where ordercode = ?", order.getStr("ordercode"));
			// 保存物流详情
			result = saveOrderPro((int) map.get("id"), rd.getInt("fpid"), 1,rd.getInt("num"),ispre);
		}
		return result;
	}
	
	/**
	 * @Desc 兑换4
	 * */
	public static boolean saveOrderInfo_dh(Record order,int ispre){
		return dingyue(4, order, null, 1,ispre);
	}
	
	/**
	 * 节日送花
	 * @param order
	 * @return
	 */
	public static boolean saveOrderInfo_jr(Record order,int ispre,int type,String code){
		boolean result = false;
		/*String code = Db.queryStr("select code_value from  f_dictionary where code_key='piCode' and state=1 and code_name='发货批次'");*/
		// 获取本批次产品
		List<Record> proList = Db.find("SELECT b.id FROM f_order_detail a LEFT JOIN f_product b ON a.fpid=b.fpid WHERE a.ordercode=? AND a.type=1 AND b.code=? AND b.type=?", order.getStr("ordercode"), code,type);
		if(proList.size() > 0){
			// 创建物流信息
			Map<String, Object> map = saveOrderInfo(order.getStr("ordercode"), order.getStr("name"), order.getStr("tel"), order.getStr("addr"), order.getInt("aid"), 1, order.getInt("ishas"),ispre);
			if((boolean) map.get("result")){
				// 保存物流详情
				result = saveOrderPro((int) map.get("id"), randomId(3, proList,false), 0,1,ispre);
				// 判断是否购买花瓶(换购之后,副品可能是多种)
				if(order.getInt("isvase")==1){
					List<Record> vaseList=Db.find("select fpid from f_order_detail where ordercode=? and type=2", order.getStr("ordercode"));
					// 保存物流详情
					for (Record record : vaseList) {
						result = saveOrderPro((int) map.get("id"), record.getInt("fpid"), 1,1,ispre);
					}
					
				}
			}
		}else{
			// 异常订单处理
			Map<String, Object> ycMap = saveOrderInfo(order.getStr("ordercode"), order.getStr("name"), order.getStr("tel"), order.getStr("addr"), order.getInt("aid"), 0, 1,ispre);
			result = (boolean) ycMap.get("result");
		}
		return result;
	}
	
	/**
	 * 配置定制花束
	 * @param order
	 * @return
	 */
	public static boolean saveOrderInfo_dz(Record order,int ispre){
		boolean result = false;
		//查询未分配的花束(主品)
		List<Record> order_detail=Db.find("select fpid from f_order_detail where ordercode =? and isAllot=1 and type=1",order.getStr("ordercode"));
		int pid=0;//分配到的产品ID
		for (Record product : dzList) {
			if(pid==0){
				for (Record detail : order_detail) {
					int fpid01=product.getInt("fpid");
					int fpid02=detail.getInt("fpid");
					if(fpid01==fpid02){
						pid=product.getInt("id");
						break;
					}
				}
			}else{
				break;
			}
			
		}
		if( pid > 0){
			// 创建物流信息
			Map<String, Object> map = saveOrderInfo(order.getStr("ordercode"), order.getStr("name"), order.getStr("tel"), order.getStr("addr"), order.getInt("aid"), 1, order.getInt("ishas"),ispre);
			if((boolean) map.get("result")){
				// 保存物流详情
				result = saveOrderPro((int) map.get("id"), pid, 0,1,ispre);
				// 判断是否购买花瓶(是否购买过副品，新增换购功能之后，副品可能有多种)
				Long count=Db.queryLong("select count(*) from f_order_info where  ordercode=? and state>1",order.getStr("ordercode"));//是否是首次物流配单
				if(count==0&&order.getInt("isvase")==1){
					List<Record> vaseList=Db.find("select fpid from f_order_detail where ordercode=? and type=2", order.getStr("ordercode"));
					// 保存物流详情
					for (Record record : vaseList) {
						result = saveOrderPro((int) map.get("id"), record.getInt("fpid"), 1,1,ispre);
					}
					
				}
			}
		}else{
			// 异常订单处理
			Map<String, Object> ycMap = saveOrderInfo(order.getStr("ordercode"), order.getStr("name"), order.getStr("tel"), order.getStr("addr"), order.getInt("aid"), 0, 1,ispre);
			result = (boolean) ycMap.get("result");
		}
		return result;
	}
	
	
	
	/**
	 * 订阅花束
	 * @param type
	 * @param order
	 * @param share
	 * @param num 还剩下多少次没发货
	 * @param ispre 0正式配单，1预配单
	 * @return
	 */
	public static boolean dingyue(int type, Record order, Record share, int num,int ispre){
		boolean result = false;
		// 订单编号
		String ordercode = order.getStr("ordercode");
		// 收货人姓名
		String name = order.getStr("name");
		// 收货人电话
		String tel = order.getStr("tel");
		// 收货人地址
		String address = order.getStr("addr");
		// 用户ID
		int aid = order.getInt("aid");
		// 获取订单主件商品的ID(1双品花束,2多品花束)
		int fpid = Db.queryInt("select fpid from f_order_detail where ordercode=? and type=1", ordercode);//商品ID
		Integer ptid = Db.queryInt("SELECT b.ptid FROM f_order_detail a LEFT JOIN f_flower_pro b ON a.fpid=b.id WHERE a.ordercode=? AND a.type = 1", ordercode);//商品分类ID
		//多双交替订单比较特殊，通过改变ptid来控制发双品还是多品，原则是奇数次发多品，偶数次发双品
		if(order.getInt("type")==9){
			if(order.getInt("num")%2==1){
				ptid=2; //多品
			}else{
				ptid=1; //双品
			}
		}
		
		List<Record> nowProList=new ArrayList<>();
		Map<String, Object> overMap;
		
		//主商城，双品鲜花  首单首次指定固定的产品,在配置产品时设置的。20170703时春香增加
		if(order.getInt("ishas")==0&&order.getInt("ocount")==0&&spList_sdsc.size()>=1 && ptid==1){
			overMap=new HashMap<>();
			overMap.put("hasgetsize", 0);
			overMap.put("proList",spList_sdsc);
			nowProList.addAll(spList_sdsc);
			nowProList =selectStyle(order, nowProList);//格调筛选
     		nowProList = tabooAndExclude(order, nowProList);//排除忌讳
			
			
		}else{
			if(ptid == 1||ptid==402){
				nowProList.addAll(spList);
				nowProList =selectStyle(order, nowProList);//格调筛选
				overMap = getHasGet(nowProList, ordercode,fpid);// 获取已送过的花并排除
				nowProList = (List<Record>) overMap.get("proList");
				// 只有双品可以设置忌讳
				nowProList = tabooAndExclude(order, nowProList);
				
			}else if(ptid==2 || ptid==405){	
				nowProList.addAll(dpList);
				nowProList =selectStyle(order, nowProList);//格调筛选
				overMap = getHasGet(dpList, ordercode,fpid);// 获取已送过的花并排除
				nowProList = (List<Record>) overMap.get("proList");
				
			}else{
				nowProList.addAll(wgList);
				nowProList =selectStyle(order, nowProList);//格调筛选
				overMap = getHasGet(wgList, ordercode,fpid);// 获取已送过的花并排除
				nowProList = (List<Record>) overMap.get("proList");
			}
		}
		
		
		if(nowProList.size() > 0){
			// 分享订单物流信息
			Map<String, Object> shareMap = new HashMap<>();
			// 普通订单物流信息
			Map<String, Object> orderMap = new HashMap<>();
			// 是否已存在物流信息
			int hassize = ((int)overMap.get("hasgetsize"))==0?0:1;
			// 是否首单首次物流
			int ishas = 1;
			// 是否首单首次物流
			if(hassize==0 && order.getInt("ishas")==0){
				ishas = 0;
			}
			if(share != null){
				shareMap = saveOrderInfo(share.getStr("ordercode"), share.getStr("name"), share.getStr("tel"), share.getStr("addr"), share.getInt("aid"), 1, 1,ispre);
				if(num-share.getInt("cycle") > 0){
					orderMap = saveOrderInfo(ordercode, name, tel, address, aid, 1, ishas,ispre);
				}
			}else{
				orderMap = saveOrderInfo(ordercode, name, tel, address, aid, 1, ishas,ispre);
			}
			// 循环分配花束
			int pid=0;
			if(order.getInt("ishas")==0&&order.getInt("ocount")==0&&spList_sdsc.size()>=1&& fpid==1){
				pid = randomId(fpid, nowProList,true);
			}else{
				pid = randomId(fpid, nowProList,false);
			}
			
			// 订单第一次物流
			if(((int)overMap.get("hasgetsize")) == 0){
				// 判断是否购买花瓶(是否购买了副品，新增换购之后，副品可能有多个)
				if(order.getInt("isvase")==1){
					//int vaseid = Db.queryInt("select fpid from f_order_detail where ordercode=? and type=2", ordercode);
					List<Record> vaseidlist=Db.find("select fpid from f_order_detail where ordercode=? and type=2", ordercode);
					if(orderMap.get("result") != null){
						if((boolean) orderMap.get("result")){
							// 保存物流详情
							//saveOrderPro((int) vaseMap.get("id"), vaseid, 1);
							for (Record vase : vaseidlist) {
								saveOrderPro((int) orderMap.get("id"), vase.getInt("fpid"), 1,1,ispre);
							}
							
						}
					}else{
						Map<String, Object> vaseMap = saveOrderInfo(ordercode, name, tel, address, aid, 1, ishas,ispre);
						if((boolean) vaseMap.get("result")){
							// 保存物流详情
							//saveOrderPro((int) vaseMap.get("id"), vaseid, 1);
							for (Record vase : vaseidlist) {
								saveOrderPro((int) orderMap.get("id"), vase.getInt("fpid"), 1,1,ispre);
							}
						}
					}
				}
				// 订阅首单第一次物流赠送周边
				if(type == 1){
					// 保存首单赠送的周边
					List<Record> details = Db.find("select fpid,type from f_order_detail where ordercode=?", order.getStr("ordercode"));
					if(details.size() > 1){
						for (Record detail : details) {
							if(detail.getInt("type") == 3){
								saveOrderPro((int) orderMap.get("id"), detail.getInt("fpid"), 2,1,ispre);	// 保存 赠品 物流详情
							}
						}
					}
				}
			}
			if(orderMap.get("result")!=null && (boolean) orderMap.get("result")){
				// 保存物流详情
				result = saveOrderPro((int) orderMap.get("id"), pid, 0,1,ispre);
			}
			if(shareMap.get("result")!=null && (boolean) shareMap.get("result")){
				// 保存物流详情
				result = saveOrderPro((int) shareMap.get("id"), pid, 0,1,ispre);
			}
		}else{
			// 异常订单处理
			Map<String, Object> ycMap = saveOrderInfo(ordercode, name, tel, address, aid, 0, 1,ispre);
			result = (boolean) ycMap.get("result");
		}
		return result;
	}
	
	/**
	 * 创建物流信息
	 * @param ordercode
	 * @param name
	 * @param tel
	 * @param addr
	 * @param aid
	 * @param state
	 * @param ishas
	 * @param isPre 0正式配单  1预配单
	 * @return
	 */
	public static Map<String, Object> saveOrderInfo(String ordercode, String name, String tel, String addr, int aid, int state, int ishas,int isPre){
		Map<String, Object> resultMap = new HashMap<>();
		boolean result = false;
		Record orderinfo = new Record();
		String number = "FMM" + System.currentTimeMillis();
		orderinfo.set("code", dateCode);
		orderinfo.set("ordercode", ordercode);
		orderinfo.set("number", number);
		String ecode = "sf";	// 默认顺丰速递
		orderinfo.set("ecode", ecode);
		Record express = Db.findFirst("select name from f_express where code=?", ecode);	// 获取物流平台名称
		orderinfo.set("ename", express.getStr("name"));
		orderinfo.set("name", name);
		orderinfo.set("tel", tel);
		orderinfo.set("address", addr);
		orderinfo.set("aid", aid);
		orderinfo.set("ctime", new Date());
		orderinfo.set("state", state);	// 物流状态(0异常,1正常,2发货,9确认收货)
		orderinfo.set("ishas", ishas);	// 是否首单首条物流(0是/1否)
		Number ofcycle = Db.queryNumber("select count(1) from f_order_info where ordercode=?", ordercode);
		orderinfo.set("ofcycle", ofcycle.intValue()+1);
		if(isPre==0){
			try {
				if(Db.save("f_order_info", orderinfo)){
					result = true;
					resultMap.put("id", (int) ((long) orderinfo.getLong("id")));
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
		}else{
			if(Db.save("f_order_info_pre", orderinfo)){
				result = true;
				resultMap.put("id", (int) ((long) orderinfo.getLong("id")));
			}
		}
		
		resultMap.put("result", result);
		return resultMap;
	}
	
	// 循环分配花束
	public static int randomId(int fpid, List<Record> proList,Boolean isSDSC) {
		int now_index = 0;
		int max_index = 0;
		if(fpid == 1){
			now_index = sp_index;
			if(isSDSC==true){
				now_index=sp_sdsc_index;
				max_index = spList_sdsc.size()-1;
			}else{
				max_index = spList.size()-1;
			}
			
		}else if(fpid == 2){
			now_index = dp_index;
			max_index = dpList.size()-1;
		}
		if(fpid < 3){
			// 当前索引大于最大索引，从0开始
			if(now_index > max_index){
				now_index = 0;
			}
			boolean R = false;
			for(Record record:proList){
				if(record.getInt("c_index")==now_index){
					R = true;
				}
			}
			if(!R){
				now_index = proList.get(0).getInt("c_index");
			}
			if(fpid==1){
				
				if(isSDSC==true){
					sp_sdsc_index=now_index+1;
				}else{
					 sp_index=now_index+1;
				}
				
				 
				}else if(fpid==2){
				  dp_index=now_index+1;
				}
			
		}else{
			Random random=new Random();
			now_index = random.nextInt(proList.size());
		}
		Record pro = new Record();
		if(proList.size()==1){
			pro = proList.get(0);
		}else{
			if(fpid==1 || fpid==2){
				for(Record record:proList){
					if(record.getInt("c_index")==now_index){
						pro = record;
					}
				}
			}else{
				pro = proList.get(now_index);
			}
		}
		return pro.getInt("id");
	}
	
	/**
	 * 保存物流详情
	 * @param oid
	 * @param pid
	 * @param type 0:花束1：周边
	 * @param num   数量
	 * @param ispre 是否是预配单
	 * @return
	 */
	public static boolean saveOrderPro(int oid, int pid, int type,int num,int ispre){
		Record orderpro = new Record();
		orderpro.set("oid", oid);
		orderpro.set("pid", pid);		// 随机分配花束
		orderpro.set("type", type);		// 花束
		orderpro.set("num", num);//数量
		if(ispre==0){
			return Db.save("f_order_pro", orderpro);
		}else{
			return Db.save("f_order_pro_pre", orderpro);
		}
		
	}
	
	// 获取已送过的花并排除
	public static Map<String, Object> getHasGet(List<Record> proList, String ordercode,int fpid){
		Map<String, Object> map = new HashMap<>();
		List<Record> nowProList = new ArrayList<>();
		if(proList.size() > 0){
			// 获取已送出的花束(花材ID集合)(最近4条记录)
			List<Record> list = Db.find("select group_concat(c.fid order by c.fid asc) as gfid from f_order_info a"
					+ " left join f_order_pro b on a.id=b.oid"
					+ " left join f_product_info c on b.pid=c.pid"
					+ " where b.type=0 and a.ordercode=? group by a.number order by a.id desc limit 0,4", ordercode);
			map.put("hasgetsize", list.size());
			String idStr = new String();
			for(int i=0;i<proList.size();i++){
				if(i==0){
					idStr += proList.get(i).getInt("id");
				}else{
					idStr += "," + proList.get(i).getInt("id");
				}
			}
			
			nowProList = Db.find("select pid as id,group_concat(fid order by fid asc) as gfid from f_product_info where pid in (" + idStr + ") group by pid");
			
			for(Record nowpro:nowProList){
				for(Record pro:proList){
					if(nowpro.getInt("id").equals(pro.getInt("id"))){
						nowpro.set("c_index", pro.getInt("c_index"));
					}
				}
			}
			//双品排重；多品不排重（时春香修改）
			if(fpid==1){
				for(Iterator<Record> it=nowProList.iterator();it.hasNext();){	// 匹配已送出的花束并排除
					Record pro = it.next();
					for(Record hg : list){
						if(pro.getStr("gfid").equals(hg.getStr("gfid"))){
							it.remove();
							break;
						}
					}
				}
			}
			
		}
		map.put("proList", nowProList);
		return map;
	}
	
	// 排除忌讳的花类与色系
	public static List<Record> tabooAndExclude(Record order, List<Record> proList){
		// 忌讳的花类
		String jh_list = order.getStr("jh_list");
		// 忌讳的色系
		String jh_color = order.getStr("jh_color");
		
		//20170312添加c.id<>28,配草不考虑在内
		//20170427 a.ftype=1 只考虑【主花材】
		String sql = "SELECT count(1) FROM f_product_info a LEFT JOIN f_flower b ON a.fid = b.id LEFT JOIN f_flower_type c ON b.tid=c.id WHERE a.ftype=1 and a.pid=?";
		if(jh_list!=null && jh_color!=null){
			sql += " AND (b.cid IN("+jh_color+") OR c.id IN("+jh_list+"))";
		}else if(jh_list!=null && jh_color==null){
			sql += " AND c.id IN("+jh_list+")";
		}else if(jh_list==null && jh_color!=null){
			sql += " AND b.cid IN("+jh_color+")";
		}
		if(jh_list!=null || jh_color!=null){
			for(Iterator<Record> it = proList.iterator();it.hasNext();){
				Record pro = it.next();
				// 计算产品中是否包含忌讳的花材
				Number jh = Db.queryNumber(sql, pro.getInt("id"));
				if(jh.intValue() > 0){
					it.remove();
				}
			}
		}
		return proList;
	}
	/**
	 * 格调筛选
	 * @param order 来自f_order
	 * @param proList 来自f_product
	 * @return
	 */
	public static List<Record> selectStyle(Record order, List<Record> proList){
		if(order.getInt("style")!=3){
			for(Iterator<Record> it = proList.iterator();it.hasNext();){
				Record pro = it.next();
				if(pro.getInt("style")!=3&&pro.getInt("style")!=order.getInt("style")){
					it.remove();
				}
			}
		}
		
		return proList;
	}
	
	/**
	 * 创建批号
	 */
	public static void CreatePiCodList(){
		List<Record> ordreList=Db.find("select * from f_order where ordercode not in(select ordercode from f_order_info ) ");
		for(Record order:ordreList){
			List<String> piCodeList=DeliveryDateUtil.getPiCodeList(order.getInt("reach"), order.getDate("ctime"), order.getInt("sendCycle"), order.getInt("cycle"),order.getInt("type"));
			for(int i=0;i< piCodeList.size();i++){
				Record f_picode = new Record();
				f_picode.set("ordercode", order.getStr("ordercode"));
				f_picode.set("piCode", piCodeList.get(i));
				f_picode.set("reach", order.getInt("reach"));
				f_picode.set("num", i+1);
				Db.save("f_picode", f_picode);
			}
			
		}
		
	}
	/**
	 * 单独给某个批次发短信,
	 * 只有【1正常状态的发送】
	 * @param piCode
	 * @param BSMsg
	 */
	public static Map<String, Object> SendBSMsg(final String piCode,final String BSMsg ){
		Map<String, Object> resultMap = new HashMap<>();
		boolean R = false;
		String M = new String();
		final List<Record> wuliulist = Db.find("select a.id,a.lognumber,a.ordercode,a.number,a.aid,a.ecode,a.name,a.tel,a.address,a.ofcycle,a.ishas,a.ename,b.reach,a.code from f_order_info a left join f_order b on a.ordercode=b.ordercode where a.code=? and a.state=1 and b.type not in(2,5,3,43) and not exists (select c.wlNumber from f_msglog c where a.number=c.wlNumber and c.msgType='gj' and remark=?)",piCode,BSMsg);
		if(wuliulist.size()==0){
			M="该发货批次未生成物流信息或该信息已经发送过";
			resultMap.put("R", R);
			resultMap.put("M", M);
			return resultMap;
		}
		String reachDate=Db.queryStr("select code_value from f_dictionary where id=32 and code_key='reachDate' and state=1");//预计送达日期
		String errorOrder="";//存放异常的订单
		for(Record wuliu : wuliulist){
			String ordercode = wuliu.getStr("ordercode");
			String tel=wuliu.getStr("tel");
			int aid = wuliu.getInt("aid");
			String number= wuliu.getStr("lognumber");
			try {
			//发送短信
			if(tel!=null && tel.equals("")==false){
			String msg = String.format("亲爱的花粉，您的订单%s：%s",ordercode,BSMsg );
			SendMsgUtil.sendMsg(wuliu.getStr("tel"), msg);//发送【管家提醒】短信
			}
			com.util.SendMouldMsgUtil.SendBSMsg(aid,number,ordercode,reachDate,BSMsg);//发【管家服务通知】模板消息
			 //添加发送日志，以免重复发送
		    Record f_msglog = new Record();
		    f_msglog.set("wlNumber", number);
		    f_msglog.set("msgType", "gj");
		    f_msglog.set("sendTime", new Date());
		    f_msglog.set("remark",BSMsg );
			Db.save("f_msglog ", f_msglog);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				errorOrder=errorOrder+","+ordercode;
			}
		}
		if(errorOrder.equals("")==true){
			R=true;
			M="发送成功";
		}else{
			R=true;
			M="发送完毕,但有异常单:"+errorOrder;
		}
		resultMap.put("R", R);
		resultMap.put("M", M);
		return resultMap;
	}
	
	/**
	 * 发货提醒，周边
	 * @return
	 */
	public static Map<String, Object> SendMsg_ZB(String picicode,String ordercode_,String logisticcode,String wuliucode,String receiver,String fpid){
		Map<String, Object> resultMap = new HashMap<>();
		boolean R = false;
		String M = new String();
		String sql = "select a.id,a.ordercode,a.number,a.lognumber,a.aid,a.ecode,a.name,a.tel,a.address,a.ofcycle,a.ishas,a.ename,b.reach,a.code from f_order_info a left join f_order b on a.ordercode=b.ordercode left join f_order_pro as c on c.oid=a.id"
				+ " where  a.state=2 and b.type  in(3,43) and a.lognumber is not null "
				+ " and not exists (select wlNumber from f_msglog  where a.lognumber= wlNumber and  msgType='fh')";
		if (StrKit.notBlank(picicode)) {
			sql += String.format(" and a.code = '%s'", picicode);
		}
		if (StrKit.notBlank(ordercode_)) {
			sql += String.format(" and a.ordercode = '%s'", ordercode_);
		}
		if (StrKit.notBlank(logisticcode)) {
			sql += String.format(" and a.lognumber = '%s'", logisticcode);
		}
		if (!wuliucode.equals("xzwl")) {
			sql += String.format(" and a.ecode = '%s'", wuliucode);
		}
		if (StrKit.notBlank(receiver)) {
			sql += String.format(" and a.name = '%s'", receiver);
		}
		if (!fpid.equals("0")) {
			sql += String.format(" and c.pid = %s", fpid);
		}
		
		final List<Record> wuliulist = Db.find(sql) ;
		
		if(wuliulist.size()==0){
			R=true;
			M="没有需要发送的信息";
			resultMap.put("R", R);
			resultMap.put("M", M);
			return resultMap;
		}
		
		
		
		String errorOrder="";//存放异常的订单
		for(Record wuliu : wuliulist){
			String ordercode = wuliu.getStr("ordercode");
			int id = wuliu.getInt("id");
			// 物流所属用户ID
			int aid = wuliu.getInt("aid");
			// 获取主单信息
			Record order = Db.findFirst("select id,jihui,zhufu,songhua,cycle,ocount,aid,type,reach,name from f_order where ordercode=?", ordercode);
			// 已送次数
			int ocount = order.getInt("ocount");
			String tel=wuliu.getStr("tel");
			String goodsname ="";//品名=产品名称+花瓶+卡片
			String pName="";//产品名称
			List<Record> opList = Db.find("select pid,type from f_order_pro where oid=?", id);
			//分配的产品名称
			for(int i=0;i<opList.size();i++){
				Record op = opList.get(i);
				int pid = op.getInt("pid");
				int type = op.getInt("type");//0:花束；1：周边
				String name = new String();
				if(type == 0){
					name = Db.queryStr("select fname from f_product where id=?", pid);//花束名称（简称）
				    name=name.replaceAll("\\d+", "");//去掉数字
				}else{
					name = Db.queryStr("select name from f_flower_pro where id=?", pid);
				}
				if(i!=opList.size()-1){
					goodsname += name+";";
				}else{
					goodsname += name;
				}
			}
			pName=goodsname;
			try {
				//发送短信(20171109日倪工通知停掉)
				/*if(tel!=null && tel.equals("")==false){
					String msg=String.format("亲爱的花粉，本周是您订单的%d/%d次配送，名称为【%s】,预计%s送达,收花人：%s。请您收到花后及时修剪，加水养护，包装盒内有鲜花养护卡片，请您参照养护，鲜花收到后立即插养护瓶,每天换水并加保鲜剂。更多鲜花养护知识可在花美美平台【养护搭配】查看", ocount,order.getInt("cycle"),pName,reachDate,order.getStr("name"));
					tel="18962631169";
					SendMsgUtil.sendMsg(tel, msg);//每次都发【收花提醒】短信
				}*/
				//发送模板消息
				/*aid=92;*/
				String wuliunumber= wuliu.getStr("lognumber");
				if (StrKit.notBlank(wuliunumber)) {
					 com.util.SendMouldMsgUtil.SendMsgEvery_ZB(aid,ordercode,pName,wuliu.getStr("ename"), order.getInt("cycle"),ocount,wuliunumber,order.getStr("name"));//每次都发【订单送达提醒】模板消息
					    //添加发送日志，以免重复发送
					    Record f_msglog = new Record();
					    f_msglog.set("wlNumber", wuliunumber);
					    f_msglog.set("msgType", "fh");
					    f_msglog.set("sendTime", new Date());
						Db.save("f_msglog ", f_msglog);
				}
			    
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				errorOrder=errorOrder+","+ordercode;
				
			}
		}
		if(errorOrder.equals("")==true){
			R=true;
			M="发送成功";
		}else{
			R=true;
			M="发送完毕,但有异常单:"+errorOrder;
		}
		resultMap.put("R", R);
		resultMap.put("M", M);
		return resultMap;
	}
	
	/**
	 * 单独给某个批次发短信,
	 * 只有【2已发货状态的发送】
	 * 每次发货后的的那条发货提醒的模板消息。礼品鲜花和节日专款订单过滤一下，不要发送
	 * 这次七夕有顾客反映破坏了他准备的惊喜，体验反而不好
	 * 周边的单独发,这里也剔除
	 * @param piCode
	 */
	public static Map<String, Object> SendMsg(final String piCode){
		Map<String, Object> resultMap = new HashMap<>();
		boolean R = false;
		String M = new String();
		final List<Record> wuliulist = Db.find("select a.id,a.ordercode,a.number,a.lognumber,a.aid,a.ecode,a.name,a.tel,a.address,a.ofcycle,a.ishas,a.ename,b.reach,a.code from f_order_info a left join f_order b on a.ordercode=b.ordercode "
				+ " where a.code=? and a.state=2 and b.type not in(2,5,3,43) and a.lognumber is not null"
				+ " and not exists (select c.wlNumber from f_msglog c where a.lognumber=c.wlNumber and c.msgType='fh')",piCode);
		if(wuliulist.size()==0){
			R=true;
			M="没有需要发送的信息";
			resultMap.put("R", R);
			resultMap.put("M", M);
			return resultMap;
		}
		String reachDate=Db.queryStr("select code_value from f_dictionary where id=32 and code_key='reachDate' and state=1");//预计送达日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
		try {
		   Date date1 = sdf.parse(reachDate);
		   Date date2=new Date();
		   if(date1.getTime()<date2.getTime() ){
			    R=false;
				M="【送达日期】小于【当前日期】,请认真设置【本次发货预计送达日期】";
				resultMap.put("R", R);
				resultMap.put("M", M);
				return resultMap;
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		String errorOrder="";//存放异常的订单
		for(Record wuliu : wuliulist){
			String ordercode = wuliu.getStr("ordercode");
			int id = wuliu.getInt("id");
			// 物流所属用户ID
			int aid = wuliu.getInt("aid");
			// 获取主单信息
			Record order = Db.findFirst("select id,jihui,zhufu,songhua,cycle,ocount,aid,type,reach,name from f_order where ordercode=?", ordercode);
			// 已送次数
			int ocount = order.getInt("ocount");
			String tel=wuliu.getStr("tel");
			String goodsname ="";//品名=产品名称+花瓶+卡片
			String pName="";//产品名称
			List<Record> opList = Db.find("select pid,type from f_order_pro where oid=?", id);
			//分配的产品名称
			for(int i=0;i<opList.size();i++){
				Record op = opList.get(i);
				int pid = op.getInt("pid");
				int type = op.getInt("type");//0:花束；1：周边
				String name = new String();
				if(type == 0){
					name = Db.queryStr("select fname from f_product where id=?", pid);//花束名称（简称）
				    name=name.replaceAll("\\d+", "");//去掉数字
				}else{
					name = Db.queryStr("select name from f_flower_pro where id=?", pid);
				}
				if(i!=opList.size()-1){
					goodsname += name+";";
				}else{
					goodsname += name;
				}
			}
			pName=goodsname;
			try {
				//发送短信(20171109日倪工通知停掉)
				/*if(tel!=null && tel.equals("")==false){
					String msg=String.format("亲爱的花粉，本周是您订单的%d/%d次配送，名称为【%s】,预计%s送达,收花人：%s。请您收到花后及时修剪，加水养护，包装盒内有鲜花养护卡片，请您参照养护，鲜花收到后立即插养护瓶,每天换水并加保鲜剂。更多鲜花养护知识可在花美美平台【养护搭配】查看", ocount,order.getInt("cycle"),pName,reachDate,order.getStr("name"));
					tel="18962631169";
					SendMsgUtil.sendMsg(tel, msg);//每次都发【收花提醒】短信
				}*/
				//发送模板消息
				/*aid=92;*/
				String wuliunumber= wuliu.getStr("lognumber");
			    com.util.SendMouldMsgUtil.SendMsgEvery(aid,ordercode,pName,wuliu.getStr("ename"), order.getInt("cycle"),ocount,wuliunumber,wuliu.getStr("code"),reachDate,order.getStr("name"));//每次都发【订单送达提醒】模板消息
			    //添加发送日志，以免重复发送
			    Record f_msglog = new Record();
			    f_msglog.set("wlNumber", wuliunumber);
			    f_msglog.set("msgType", "fh");
			    f_msglog.set("sendTime", new Date());
				Db.save("f_msglog ", f_msglog);
			    
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				errorOrder=errorOrder+","+ordercode;
				
			}
		}
		if(errorOrder.equals("")==true){
			R=true;
			M="发送成功";
		}else{
			R=true;
			M="发送完毕,但有异常单:"+errorOrder;
		}
		resultMap.put("R", R);
		resultMap.put("M", M);
		return resultMap;
	}
	
	// 发货
	/**
	 * 鲜花发货
	 * @param wlcode
	 * @param ishas
	 * @param lx
	 * @return
	 */
	public static Map<String, Object> seedGoods(String wlcode, int ishas, int lx){
		Lx = lx;
		Map<String, Object> resultMap = new HashMap<>();
		boolean R = false;
		String M = new String();
		Map<String, Object> chooseMap = DeliveryDateUtil.chooseDate();
		boolean result = (boolean) chooseMap.get("result");
		if(result || lx==1){
			dateCode =(String) chooseMap.get("datecode");
			condition = new String();
			if(!"xzwl".equalsIgnoreCase(wlcode)){
				condition += " and a.ecode='" + wlcode + "'";
			}
			if(ishas != 10){
				condition += " and a.ishas='" + ishas + "'";
			}
			// 物流状态(0异常,1正常,2发货,9确认收货)
			//String holistr = lx==1?" and b.type=5":" and b.type<>5";
			// 本批次的物流数量
			Number wl = Db.queryNumber("select count(1) from f_order_info a left join f_order b on a.ordercode=b.ordercode where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and a.code=?" , dateCode);
			// 异常物流数量
			Number yc = Db.queryNumber("select count(1) from f_order_info a left join f_order b on a.ordercode=b.ordercode where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and a.code=? and a.state=0" , dateCode);
			// 已发货的物流数量
			Number yf = Db.queryNumber("select count(1) from f_order_info a left join f_order b on a.ordercode=b.ordercode where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and a.code=? and a.state=2" + condition , dateCode);
			if(wl.intValue() > 0){
				if(yc.intValue() == 0){
					if(yf.intValue() == 0){
						R = Db.tx(new IAtom() {
								@Override
								public boolean run() throws SQLException {
									// 顺丰发货方名字
									String j_contact = Db.queryStr("select code_value from f_dictionary where code_key=?", "j_contact");
									// 顺丰发货方联系电话
									String j_tel = Db.queryStr("select code_value from f_dictionary where code_key=?", "j_tel");
									// 顺丰发货方地址
									String j_address = Db.queryStr("select code_value from f_dictionary where code_key=?", "j_address");
									boolean fahuo_result = true;
									//正常物流信息列表(等待发货)
									List<Record> wuliulist = Db.find("select a.id,a.ordercode,a.number,a.lognumber,a.aid,a.ecode,a.name,a.tel,a.address,a.ofcycle,a.ishas,a.ename from f_order_info a left join f_order b on a.ordercode=b.ordercode where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and a.code=?" + condition, dateCode);
									for(Record wuliu : wuliulist){
										// 物流记录ID
										int id = wuliu.getInt("id");
										// 物流所属订单编号
										String ordercode = wuliu.getStr("ordercode");
										// 物流所属用户ID
										int aid = wuliu.getInt("aid");
										// 获取主单信息
										Record order = Db.findFirst("select id,jihui,zhufu,songhua,cycle,ocount,aid,type,ishas,reach from f_order where ordercode=?", ordercode);
										// 已送次数
										int ocount = order.getInt("ocount");
										String tel=wuliu.getStr("tel");
										// 修改已送次数
										order.set("ocount", ++ocount);
										// 主订单余量修改
										if(Db.update("f_order", order)){
											// 分享订单余量修改(用户ID用来防止主订单干扰)
											Db.update("update f_share set ocount=ocount+1 where ordercode=? and aid=?", ordercode, aid);
											// 物流记录修改状态(已发货)
											int rn = Db.update("update f_order_info set state=2 where id=?", id);
											if(rn == 0){
												fahuo_result = false;
												break;
											}
											//定制花束，修改f_order_detil表，isAllot=2已经分配过
											if(order.getInt("type")==6){
												Record detial=Db.findFirst("select d.ordercode,d.fpid from f_order_info a left join f_order_pro b on a.id=b.oid left join f_product c on b.pid=c.id LEFT JOIN f_order_detail d on c.fpid=d.fpid and a.ordercode=d.ordercode where a.ordercode=? and a.number=?",wuliu.getStr("ordercode"),wuliu.getStr("number"));
											    int cc=Db.update("update f_order_detail set isAllot=2 where ordercode=? and fpid=?",detial.getStr("ordercode"),detial.getInt("fpid"));
											    if(cc == 0){
													fahuo_result = false;
													break;
												}
											}
										}
										// 商家系统同步订单信息,暂时都用门对门接口
										/*if("sf1".equalsIgnoreCase(wuliu.getStr("ecode"))){
											String[] addArr = wuliu.getStr("address").split("-", 4);
											String province="未填",city="未填",county="未填";
											try {
												province = addArr[0];
												city = addArr[1];
												county = addArr[2];
												fahuo_result = SFUtil.synchroSF(ordercode, wuliu.getStr("number"), wuliu.getStr("name"), wuliu.getStr("tel"), 
																province, city, county, addArr[addArr.length-1], j_contact, j_tel, j_address);
											} catch (Exception e) {
												e.printStackTrace();
											}
											if(!fahuo_result){
												break;
											}
										}else if("d2d".equalsIgnoreCase(wuliu.getStr("ecode"))||"sf".equalsIgnoreCase(wuliu.getStr("ecode"))||"jd".equalsIgnoreCase(wuliu.getStr("ecode"))){
											int ishas = wuliu.getInt("ishas");
											String first = ishas==0?"首单":"非首单";
											first += "第" + wuliu.getInt("ofcycle") + "/" + order.getInt("cycle") + "次";
											String jihui = order.getStr("jihui");
											jihui = jihui==null?"":(" | 忌讳的花:"+jihui);
											String remark = first+jihui;
											// 首单
											
											String goodsname ="";//品名=产品名称+花瓶+卡片
											String pName="";//产品名称
											List<Record> opList = Db.find("select pid,type from f_order_pro where oid=?", id);
											//分配的产品名称
											for(int i=0;i<opList.size();i++){
												Record op = opList.get(i);
												int pid = op.getInt("pid");
												int type = op.getInt("type");//0:花束；1：周边
												String name = new String();
												if(type == 0){
													name = Db.queryStr("select fname from f_product where id=?", pid);//花束名称（简称）
												    name=name.replaceAll("\\d+", "");//去掉数字
												}else{
													name = Db.queryStr("select name from f_flower_pro where id=?", pid);
												}
												if(i!=opList.size()-1){
													goodsname += name+";";
												}else{
													goodsname += name;
												}
											}
											pName=goodsname;
											//赠送的产品：前提是【订阅鲜花、定制鲜花，首单首次】
											//如果订阅次数<4，【流光】,20170630改成了【菠萝】
											//如果订阅次数>=4,【雪山玉瓷】
											if(order.getInt("ishas")==0 && order.getInt("ocount")==1&& (order.getInt("type")==1||order.getInt("type")==6)){
												if(order.getInt("cycle")<4){
													goodsname +="+菠萝";
												}else if(order.getInt("cycle")>=4){
													goodsname +="+雪山玉瓷";
												}
											}
											//是否有卡片，当有【祝福语】时，需要卡片
											String isCard="";
											if(order.getInt("ocount")==1&&(order.getStr("zhufu"))!=null && (order.getStr("zhufu"))!=""){
												isCard="+卡片";
												goodsname +=isCard;
											}
											if ("jd".equalsIgnoreCase(wuliu.getStr("ecode"))) {
												try {
													JDUtil.jiedan(wuliu.getStr("lognumber"), wuliu.getStr("number"),wuliu.getStr("name"),wuliu.getStr("address"), wuliu.getStr("tel"),goodsname);
												} catch (JdException e) {
													e.printStackTrace();
												} 
											}
											//抛送订单给物流
											fahuo_result = synchroD2D(ordercode, wuliu.getStr("number"), wuliu.getStr("name"), wuliu.getStr("address"), wuliu.getStr("tel"), remark, goodsname);
											if(!fahuo_result){
												break;
											}
										}else{
											fahuo_result = false;
											break;
										}*/
									}
									return fahuo_result;
								}
							});
						if(R){
							M = "批次"+dateCode+"发货成功";
						}else{
							M = "批次"+dateCode+"发货失败";
						}
					}else{
						M = "批次"+dateCode+"已经发货,无法重复发货";
					}
				}else{
					M = "批次"+dateCode+"存在异常物流信息,发货失败";
				}
			}else{
				M = "批次"+dateCode+"尚未配单,无法发货";
			}
		}else{
			M = "不在发货时间内";
		}
		resultMap.put("R", R);
		resultMap.put("M", M);
		return resultMap;
	}
	/**
	 * 周边发货
	 * @param wlcode
	 * @param ishas
	 * @param lx
	 * @return
	 */
	public static Map<String, Object> seedGoods_ZB(String wlcode,int fpid){
		Map<String, Object> resultMap = new HashMap<>();
		boolean R = false;
		String M = new String();
		boolean result = true;
		if(result){
			condition = new String();
			String strSelect="select count(1) from f_order_info a left join f_order b on a.ordercode=b.ordercode"
					+ "  left join f_order_pro as c on c.oid=a.id  left join f_flower_pro as d on c.pid=d.id"
					+ " where 1=1 ";
			condition=" and  a.state=1 and b.type in(3,43) and a.lognumber is not null";
			if(fpid!=0){
				condition+=" and d.id="+fpid;
			}
			if(!"xzwl".equalsIgnoreCase(wlcode)){
				condition += " and a.ecode='" + wlcode + "'";
			}
			// 本批次的物流数量
			Number wl = Db.queryNumber(strSelect+condition);
			
			if(wl.intValue() > 0){
				R = Db.tx(new IAtom() {
					@Override
					public boolean run() throws SQLException {
						// 顺丰发货方名字
						String j_contact = Db.queryStr("select code_value from f_dictionary where code_key=?", "j_contact");
						// 顺丰发货方联系电话
						String j_tel = Db.queryStr("select code_value from f_dictionary where code_key=?", "j_tel");
						// 顺丰发货方地址
						String j_address = Db.queryStr("select code_value from f_dictionary where code_key=?", "j_address");
						boolean fahuo_result = true;
						//正常物流信息列表(等待发货)
						List<Record> wuliulist = Db.find("select a.id,a.ordercode,a.number,a.lognumber,a.aid,a.ecode,"
								+ " a.name,a.tel,a.address,a.ofcycle,a.ishas,a.ename,d.name 'gName' "
								+ " from f_order_info a "
								+ " left join f_order b on a.ordercode=b.ordercode"
								+ " left join f_order_pro as c on c.oid=a.id "
								+ " left join f_flower_pro as d on c.pid=d.id where 1=1 "
							    + condition);
						for(Record wuliu : wuliulist){
							// 物流记录ID
							int id = wuliu.getInt("id");
							// 物流所属订单编号
							String ordercode = wuliu.getStr("ordercode");
							// 物流所属用户ID
							int aid = wuliu.getInt("aid");
							// 获取主单信息
							Record order = Db.findFirst("select id,jihui,zhufu,songhua,cycle,ocount,aid,type,ishas,reach from f_order where ordercode=?", ordercode);
							// 已送次数
							int ocount = order.getInt("ocount");
							// 修改已送次数
							order.set("ocount", ++ocount);
							// 主订单余量修改
							if(Db.update("f_order", order)){
								// 分享订单余量修改(用户ID用来防止主订单干扰)
								Db.update("update f_share set ocount=ocount+1 where ordercode=? and aid=?", ordercode, aid);
								// 物流记录修改状态(已发货)
								int rn = Db.update("update f_order_info set state=2 where id=?", id);
								if(rn == 0){
									fahuo_result = false;
									break;
								}
							}
							// 商家系统同步订单信息,暂时都用门对门接口
							/*if("sf1".equalsIgnoreCase(wuliu.getStr("ecode"))){
								String[] addArr = wuliu.getStr("address").split("-", 4);
								String province="未填",city="未填",county="未填";
								try {
									province = addArr[0];
									city = addArr[1];
									county = addArr[2];
									fahuo_result = SFUtil.synchroSF(ordercode, wuliu.getStr("number"), wuliu.getStr("name"), wuliu.getStr("tel"), 
													province, city, county, addArr[addArr.length-1], j_contact, j_tel, j_address);
								} catch (Exception e) {
									e.printStackTrace();
								}
								if(!fahuo_result){
									break;
								}
							}else if("d2d".equalsIgnoreCase(wuliu.getStr("ecode"))||"zt".equalsIgnoreCase(wuliu.getStr("ecode"))||"sf".equalsIgnoreCase(wuliu.getStr("ecode"))||"jd".equalsIgnoreCase(wuliu.getStr("ecode"))){
								String remark ="";
								String goodsname =wuliu.getStr("gName");
								//抛送订单给物流
								fahuo_result = synchroD2D(ordercode, wuliu.getStr("number"), wuliu.getStr("name"), wuliu.getStr("address"), wuliu.getStr("tel"), remark, goodsname);
								if(!fahuo_result){
									break;
								}
							}else{
								fahuo_result = false;
								break;
							}*/
						}
						return fahuo_result;
						
					}
					
				});
				if(R){
					M = "发货成功";
				}else{
					M = "发货失败";
				}
			}else{
				M = "没有需要发货订单";
			}
		}else{
			M = "不在发货时间内";
		}
		resultMap.put("R", R);
		resultMap.put("M", M);
		return resultMap;
	}

	// 打印门对门物流信息
	public static List<Record> print(){
		Map<String, Object> chooseMap = DeliveryDateUtil.chooseDate();
		boolean result = (boolean) chooseMap.get("result");
		List<Record> list = new ArrayList<>();
		if(result){
			dateCode = (String) chooseMap.get("datecode");
			list = Db.find("select ordercode from f_order_info where code=?", dateCode);
		}
		return list;
	}
	
	/**
	 * 分析excel的内容
	 * @param path
	 * @return
	 */
	public static List<String[]> getExcelData(File file){
        return getData(file).get(0);//选择sheet1
    }
    private static List<List<String[]>> getData(File file){
        HSSFWorkbook workbook;
        List<List<String[]>> data = new ArrayList<List<String[]>>();
        try {
            workbook = new HSSFWorkbook(new FileInputStream(file));
            HSSFSheet sheet=null;
            //循环sheet
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheet=workbook.getSheetAt(i);
                List<String[]> rows = new ArrayList<String[]>();
                int colsnum = 0;
                //循环每一行
                for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                    HSSFRow row=sheet.getRow(j);
                    if(null != row){
                        //列数以excel第二行为准
                        colsnum = sheet.getRow(1).getPhysicalNumberOfCells();
                        String[] cols = new String[colsnum];
                        //循环每一个单元格，以一行为单位，组成一个数组
                        for (int k = 0; k < colsnum; k++) {
                            //判断单元格是否为null，若为null，则置空
                            if(null != row.getCell(k)) {
                                int type = row.getCell(k).getCellType();
                                //判断单元格数据是否为数字
                                if(type == HSSFCell.CELL_TYPE_NUMERIC){
                                    //判断该数字的计数方法是否为科学计数法，若是，则转化为普通计数法
                                    if(String.valueOf(row.getCell(k).getNumericCellValue()).matches(".*[E|e].*")) {
                                        DecimalFormat df = new DecimalFormat("#.#");
                                        //指定最长的小数点位为10
                                        df.setMaximumFractionDigits(10);
                                        cols[k] = df.format(row.getCell(k).getNumericCellValue());
                                    //判断该数字是否是日期,若是则转成字符串
                                    } else if(HSSFDateUtil.isCellDateFormatted(row.getCell(k))){
                                        Date d = row.getCell(k).getDateCellValue();
                                        DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
                                        cols[k] = formater.format(d);
                                    }else{
                                        cols[k] = (row.getCell(k)+"").trim();
                                    }
                                } else {
                                    cols[k] = (row.getCell(k)+"").trim();
                                }
                            } else {
                                cols[k] = "";
                            }
                        }
                        //以一行为单位，加入list
                        rows.add(cols);
                    }
                }
                //返回所有数据，第一个list表示sheet，第二个list表示sheet内所有行数据，第三个string[]表示单元格数据
                data.add(rows);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  data;
    }
	
	// 导出物流单
	public static boolean getlogisticForExcel(HttpServletResponse response, String ecode, int ishas, String code)  {
		String sql = "SELECT c.gName, a.ordercode,c.aid,a.number,a.name,a.tel,a.address,a.ofcycle,GROUP_CONCAT(CONCAT(b.pid,'-',b.type)) AS pit,c.jihui,c.color,a.ishas,0 AS price,c.cycle,c.songhua,c.zhufu"
				+ " FROM f_order_info a LEFT JOIN f_order_pro b ON a.id=b.oid"
				+ " LEFT JOIN f_order c ON a.ordercode=c.ordercode WHERE a.code=? AND a.ecode=?  and c.type not in(3,43)";
		if(ishas != 10){
			sql += " AND a.ishas="+ishas;
		}
		sql += " GROUP BY number";
		List<Record> wllist = Db.find(sql, code, ecode);	// 导出物流
		for(Record wl : wllist){
			String pit = wl.getStr("pit");
			if(pit != null){
				String[] pitArr = pit.split(",");
				String pname = new String();
				for(int i=0;i<pitArr.length;i++){
					String p = pitArr[i];
					String name = new String();
					if(Integer.parseInt(p.split("-")[1]) == 0){
						name = Db.queryStr("SELECT fname FROM f_product WHERE id=?", Integer.parseInt(p.split("-")[0]));
					}else{
						if(Integer.parseInt(p.split("-")[1]) == 1){
							name = Db.queryStr("SELECT name FROM f_flower_pro WHERE id=?", Integer.parseInt(p.split("-")[0]));
						}else{
							name = "(首单赠品)"+Db.queryStr("SELECT name FROM f_flower_pro WHERE id=?", Integer.parseInt(p.split("-")[0]));
						}
					}
					if(i==0){
						pname = name;
					}else{
						pname += ","+name;
					}
				}
				wl.set("pname", pname);
			}else{
				return false;
			}
		}
		HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet1 = wbook.createSheet();
		//设置列宽
		sheet1.setColumnWidth((short)0, (short)6400);
		sheet1.setColumnWidth((short)1, (short)6400);
		sheet1.setColumnWidth((short)2, (short)2400);
		sheet1.setColumnWidth((short)3, (short)2400);
		sheet1.setColumnWidth((short)4, (short)5400);
		sheet1.setColumnWidth((short)5, (short)12800);
		sheet1.setColumnWidth((short)6, (short)6400);
		sheet1.setColumnWidth((short)7, (short)6400);
		sheet1.setColumnWidth((short)8, (short)6400);
		sheet1.setColumnWidth((short)9, (short)3000);
		sheet1.setColumnWidth((short)10, (short)2400);
		sheet1.setColumnWidth((short)11, (short)2400);
		sheet1.setColumnWidth((short)12, (short)2400);
		sheet1.setColumnWidth((short)13, (short)6400);
		sheet1.setColumnWidth((short)14, (short)6400);
		String ename = Db.queryStr("select name from f_express where code=?", ecode);
		wbook.setSheetName(0, ename);
		//首行 样式1
        HSSFCellStyle cellStyle = wbook.createCellStyle();
        HSSFFont font = wbook.createFont();
        font.setColor(HSSFColor.RED.index);    //红字
        cellStyle.setFont(font);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index); //填亮黄色
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
        //样式2
        HSSFCellStyle cellStyle1 = wbook.createCellStyle();
        cellStyle1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
        cellStyle1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
        cellStyle1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
        cellStyle1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
       //首行 样式2
        HSSFCellStyle cellStyle2 = wbook.createCellStyle();
        font = wbook.createFont();
        font.setColor(HSSFColor.RED.index);    //红字
        cellStyle2.setFont(font);
        cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
        cellStyle2.setFillForegroundColor(HSSFColor.TAN.index); //填橘黄
        cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
          
		HSSFRow row;
		for(int i=0;i<wllist.size();i++){
			if(i==0){
				row = sheet1.createRow(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle);
				cell0.setCellValue("用户订单号");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("物流编码");
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("用户ID");
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("联系人");
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle);
				cell4.setCellValue("联系电话");
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle);
				cell5.setCellValue("详细地址");
				HSSFCell cell6 = row.createCell((short) 6);
				cell6.setCellStyle(cellStyle2);
				cell6.setCellValue("品名");
				HSSFCell cell7 = row.createCell((short) 7);
				cell7.setCellStyle(cellStyle);
				cell7.setCellValue("忌讳的花");
				HSSFCell cell8 = row.createCell((short) 8);
				cell8.setCellStyle(cellStyle);
				cell8.setCellValue("忌讳色系");
				HSSFCell cell9 = row.createCell((short) 9);
				cell9.setCellStyle(cellStyle);
				cell9.setCellValue("首单");
				HSSFCell cell10 = row.createCell((short) 10);
				cell10.setCellStyle(cellStyle);
				cell10.setCellValue("金额");
				HSSFCell cell11 = row.createCell((short) 11);
				cell11.setCellStyle(cellStyle);
				cell11.setCellValue("送花人");
				HSSFCell cell12 = row.createCell((short) 12);
				cell12.setCellStyle(cellStyle);
				cell12.setCellValue("祝福语");
				HSSFCell cell13 = row.createCell((short) 13);
				cell13.setCellStyle(cellStyle);
				cell13.setCellValue("发货次数");
				HSSFCell cell14 = row.createCell((short) 14);
				cell13.setCellStyle(cellStyle);
				cell13.setCellValue("订购商品");
			}
			row = sheet1.createRow(i+1);
			Record r = wllist.get(i);
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellStyle(cellStyle1);
			cell0.setCellValue(r.getStr("ordercode"));
			HSSFCell cell1 = row.createCell((short) 1);
			cell1.setCellStyle(cellStyle1);
			cell1.setCellValue(r.getStr("number"));
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellStyle(cellStyle1);
			cell2.setCellValue(r.getInt("aid"));
			HSSFCell cell3 = row.createCell((short) 3);
			cell3.setCellStyle(cellStyle1);
			cell3.setCellValue(r.getStr("name"));
			HSSFCell cell4 = row.createCell((short) 4);
			cell4.setCellStyle(cellStyle1);
			cell4.setCellValue(r.getStr("tel"));
			HSSFCell cell5 = row.createCell((short) 5);
			cell5.setCellStyle(cellStyle1);
			cell5.setCellValue(r.getStr("address"));
			HSSFCell cell6 = row.createCell((short) 6);
			cell6.setCellStyle(cellStyle1);
			cell6.setCellValue(r.getStr("pname"));
			HSSFCell cell7 = row.createCell((short) 7);
			cell7.setCellStyle(cellStyle1);
			cell7.setCellValue(r.getStr("jihui")==null?"无":r.getStr("jihui"));
			HSSFCell cell8 = row.createCell((short) 8);
			cell8.setCellStyle(cellStyle1);
			cell8.setCellValue(r.getStr("color")==null?"无":r.getStr("color"));
			HSSFCell cell9 = row.createCell((short) 9);
			cell9.setCellStyle(cellStyle1);
			cell9.setCellValue(r.getInt("ishas")==0?"首单":"普通");
			HSSFCell cell10 = row.createCell((short) 10);
			cell10.setCellStyle(cellStyle1);
			cell10.setCellValue(r.getLong("price"));
			HSSFCell cell11 = row.createCell((short) 11);
			cell11.setCellStyle(cellStyle1);
			cell11.setCellValue(r.getStr("songhua"));
			HSSFCell cell12 = row.createCell((short) 12);
			cell12.setCellStyle(cellStyle1);
			cell12.setCellValue(r.getStr("zhufu"));
			HSSFCell cell13 = row.createCell((short) 13);
			cell13.setCellStyle(cellStyle1);
			cell13.setCellValue(r.getInt("ofcycle")+"/"+r.getInt("cycle"));
			HSSFCell cell14 = row.createCell((short) 14);
			cell14.setCellStyle(cellStyle1);
			cell14.setCellValue(r.getStr("gName"));
		}
		response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String(ename+"-物流订单")+ ".xls");
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		try {
			ServletOutputStream out = response.getOutputStream();
			wbook.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * 导出周边物流订单
	 * @param response
	 * @param ecode
	 * @param ishas
	 * @param state
	 * @return
	 */
	public static boolean getlogisticForExcel_ZB(HttpServletResponse response, String ecode, int ishas,int state,int fpid)  {
		String sql = "SELECT c.gName,a.ordercode,c.aid,a.number,a.name,a.tel,a.address,a.ofcycle,GROUP_CONCAT(CONCAT(b.pid,'-',b.type)) AS pit,c.jihui,c.color,a.ishas,0 AS price,c.cycle,c.songhua,c.zhufu"
				+ " FROM f_order_info a LEFT JOIN f_order_pro b ON a.id=b.oid"
				+ " LEFT JOIN f_order c ON a.ordercode=c.ordercode "
				+ " left join f_flower_pro as d on b.pid=d.id WHERE a.state=? AND a.ecode=? and c.type in(3,43)";
		if(ishas != 10){
			sql += " AND a.ishas="+ishas;
		}
		if(fpid!=0){
			sql+=" and d.id="+fpid;
		}
		sql += " GROUP BY number";
		List<Record> wllist = Db.find(sql, state, ecode);	// 导出物流
		for(Record wl : wllist){
			String pit = wl.getStr("pit");
			if(pit != null){
				String[] pitArr = pit.split(",");
				String pname = new String();
				for(int i=0;i<pitArr.length;i++){
					String p = pitArr[i];
					String name = new String();
					if(Integer.parseInt(p.split("-")[1]) == 0){
						name = Db.queryStr("SELECT fname FROM f_product WHERE id=?", Integer.parseInt(p.split("-")[0]));
					}else{
						if(Integer.parseInt(p.split("-")[1]) == 1){
							name = Db.queryStr("SELECT name FROM f_flower_pro WHERE id=?", Integer.parseInt(p.split("-")[0]));
						}else{
							name = "(首单赠品)"+Db.queryStr("SELECT name FROM f_flower_pro WHERE id=?", Integer.parseInt(p.split("-")[0]));
						}
					}
					if(i==0){
						pname = name;
					}else{
						pname += ","+name;
					}
				}
				wl.set("pname", pname);
			}else{
				return false;
			}
		}
		HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet1 = wbook.createSheet();
		//设置列宽
		sheet1.setColumnWidth((short)0, (short)6400);
		sheet1.setColumnWidth((short)1, (short)6400);
		sheet1.setColumnWidth((short)2, (short)2400);
		sheet1.setColumnWidth((short)3, (short)2400);
		sheet1.setColumnWidth((short)4, (short)5400);
		sheet1.setColumnWidth((short)5, (short)12800);
		sheet1.setColumnWidth((short)6, (short)6400);
		sheet1.setColumnWidth((short)7, (short)6400);
		sheet1.setColumnWidth((short)8, (short)6400);
		sheet1.setColumnWidth((short)9, (short)3000);
		sheet1.setColumnWidth((short)10, (short)2400);
		sheet1.setColumnWidth((short)11, (short)2400);
		sheet1.setColumnWidth((short)12, (short)2400);
		sheet1.setColumnWidth((short)13, (short)6400);
		sheet1.setColumnWidth((short)14, (short)6400);
		String ename = Db.queryStr("select name from f_express where code=?", ecode);
		wbook.setSheetName(0, ename);
		//首行 样式1
        HSSFCellStyle cellStyle = wbook.createCellStyle();
        HSSFFont font = wbook.createFont();
        font.setColor(HSSFColor.RED.index);    //红字
        cellStyle.setFont(font);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index); //填亮黄色
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
        //样式2
        HSSFCellStyle cellStyle1 = wbook.createCellStyle();
        cellStyle1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
        cellStyle1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
        cellStyle1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
        cellStyle1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
       //首行 样式2
        HSSFCellStyle cellStyle2 = wbook.createCellStyle();
        font = wbook.createFont();
        font.setColor(HSSFColor.RED.index);    //红字
        cellStyle2.setFont(font);
        cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
        cellStyle2.setFillForegroundColor(HSSFColor.TAN.index); //填橘黄
        cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
          
		HSSFRow row;
		for(int i=0;i<wllist.size();i++){
			if(i==0){
				row = sheet1.createRow(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle);
				cell0.setCellValue("用户订单号");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("物流编码");
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("用户ID");
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("联系人");
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle);
				cell4.setCellValue("联系电话");
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle);
				cell5.setCellValue("详细地址");
				HSSFCell cell6 = row.createCell((short) 6);
				cell6.setCellStyle(cellStyle2);
				cell6.setCellValue("品名");
				HSSFCell cell7 = row.createCell((short) 7);
				cell7.setCellStyle(cellStyle);
				cell7.setCellValue("忌讳的花");
				HSSFCell cell8 = row.createCell((short) 8);
				cell8.setCellStyle(cellStyle);
				cell8.setCellValue("忌讳色系");
				HSSFCell cell9 = row.createCell((short) 9);
				cell9.setCellStyle(cellStyle);
				cell9.setCellValue("首单");
				HSSFCell cell10 = row.createCell((short) 10);
				cell10.setCellStyle(cellStyle);
				cell10.setCellValue("金额");
				HSSFCell cell11 = row.createCell((short) 11);
				cell11.setCellStyle(cellStyle);
				cell11.setCellValue("送花人");
				HSSFCell cell12 = row.createCell((short) 12);
				cell12.setCellStyle(cellStyle);
				cell12.setCellValue("祝福语");
				HSSFCell cell13 = row.createCell((short) 13);
				cell13.setCellStyle(cellStyle);
				cell13.setCellValue("发货次数");
				HSSFCell cell14 = row.createCell((short) 14);
				cell13.setCellStyle(cellStyle);
				cell13.setCellValue("订购商品");
			}
			row = sheet1.createRow(i+1);
			Record r = wllist.get(i);
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellStyle(cellStyle1);
			cell0.setCellValue(r.getStr("ordercode"));
			HSSFCell cell1 = row.createCell((short) 1);
			cell1.setCellStyle(cellStyle1);
			cell1.setCellValue(r.getStr("number"));
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellStyle(cellStyle1);
			cell2.setCellValue(r.getInt("aid"));
			HSSFCell cell3 = row.createCell((short) 3);
			cell3.setCellStyle(cellStyle1);
			cell3.setCellValue(r.getStr("name"));
			HSSFCell cell4 = row.createCell((short) 4);
			cell4.setCellStyle(cellStyle1);
			cell4.setCellValue(r.getStr("tel"));
			HSSFCell cell5 = row.createCell((short) 5);
			cell5.setCellStyle(cellStyle1);
			cell5.setCellValue(r.getStr("address"));
			HSSFCell cell6 = row.createCell((short) 6);
			cell6.setCellStyle(cellStyle1);
			cell6.setCellValue(r.getStr("pname"));
			HSSFCell cell7 = row.createCell((short) 7);
			cell7.setCellStyle(cellStyle1);
			cell7.setCellValue(r.getStr("jihui")==null?"无":r.getStr("jihui"));
			HSSFCell cell8 = row.createCell((short) 8);
			cell8.setCellStyle(cellStyle1);
			cell8.setCellValue(r.getStr("color")==null?"无":r.getStr("color"));
			HSSFCell cell9 = row.createCell((short) 9);
			cell9.setCellStyle(cellStyle1);
			cell9.setCellValue(r.getInt("ishas")==0?"首单":"普通");
			HSSFCell cell10 = row.createCell((short) 10);
			cell10.setCellStyle(cellStyle1);
			cell10.setCellValue(r.getLong("price"));
			HSSFCell cell11 = row.createCell((short) 11);
			cell11.setCellStyle(cellStyle1);
			cell11.setCellValue(r.getStr("songhua"));
			HSSFCell cell12 = row.createCell((short) 12);
			cell12.setCellStyle(cellStyle1);
			cell12.setCellValue(r.getStr("zhufu"));
			HSSFCell cell13 = row.createCell((short) 13);
			cell13.setCellStyle(cellStyle1);
			cell13.setCellValue(r.getInt("ofcycle")+"/"+r.getInt("cycle"));
			HSSFCell cell14 = row.createCell((short) 14);
			cell14.setCellStyle(cellStyle1);
			cell14.setCellValue(r.getStr("gName"));
		}
		response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String(ename+"-物流订单")+ ".xls");
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		try {
			ServletOutputStream out = response.getOutputStream();
			wbook.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * 花材价格检查
	 * 0：订阅级花材（双品、多品 定制）
	 * 1：送花花材
	 * 2：节日送花花材
	 * @param datecode
	 * @return
	 */
	public static boolean flowerPriceValid(String datecode){
		List<Record> pricelist = Db.find("SELECT fid,type FROM f_flower_price WHERE code = ?", datecode);
		// 订阅级
		List <Record> listA = Db.find("SELECT a.type,b.fid FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid WHERE a.code=? and (a.type=1 or a.type=2 or a.type=5) GROUP BY b.fid", datecode);
		// 送花级
		List <Record> listB = Db.find("SELECT a.type,b.fid FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid WHERE a.code=? AND a.type=3 GROUP BY b.fid", datecode);
		//节日级
		List <Record> listC = Db.find("SELECT a.type,b.fid FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid WHERE a.code=? AND a.type=4 GROUP BY b.fid", datecode);
		//订阅级验证
		for(Record A : listA){
			boolean rA = false;
			for(Record price : pricelist){
				if(price.getInt("fid").equals(A.getInt("fid")) && price.getInt("type")==0){
					rA = true;
				}
			}
			if(!rA){
				return false;
			}
		}
		//送花级验证
		for(Record B : listB){
			boolean rB = false;
			for(Record price : pricelist){
				if(price.getInt("fid").equals(B.getInt("fid")) && price.getInt("type")==1){
					rB = true;
				}
			}
			if(!rB){
				return false;
			}
		}
		//节日级验证
		for(Record C : listC){
			boolean rC = false;
			for(Record price : pricelist){
				if(price.getInt("fid").equals(C.getInt("fid")) && price.getInt("type")==2){
					rC = true;
				}
			}
			if(!rC){
				return false;
			}
		}
		return true;
	}
	
	// 验证节日花材价格
	public static boolean HoliPriceValid(String datecode){
		List<Record> pricelist = Db.find("SELECT fid,type FROM f_flower_price WHERE code = ?", datecode);
		List <Record> list = Db.find("SELECT a.type,b.fid FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid WHERE a.code=? AND a.type=4 GROUP BY b.fid", datecode);
		for(Record B : list){
			boolean rB = false;
			for(Record price : pricelist){
				if(price.getInt("fid").equals(B.getInt("fid")) && price.getInt("type")==2){
					rB = true;
				}
			}
			if(!rB){
				return false;
			}
		}
		return true;
	}
	
	// 挑单
	public static boolean synchro_toD2D(String dateCode, int lx){
		boolean result = false;
		// 正常物流信息列表(等待挑单)
		List<Record> wllist = Db.find("select a.id,a.ordercode,a.number,a.aid,a.name,a.tel,a.address,a.ishas,a.ofcycle,b.cycle,b.jihui "
				+ "from f_order_info a left join f_order b on a.ordercode=b.ordercode where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and  a.code=?", dateCode);
		for(Record wuliu : wllist){
			// 物流记录ID
			int id = wuliu.getInt("id");
			// 物流所属订单编号
			String ordercode = wuliu.getStr("ordercode");
			// 主单信息
			Record order = Db.findFirst("select id,jihui,zhufu,songhua,cycle,ocount,aid from f_order where ordercode=?", ordercode);
			// 物流编码
			String number = wuliu.getStr("number");
			
			int ishas = wuliu.getInt("ishas");
			String first = ishas==0?"首单":"非首单";
			first += "第" + wuliu.getInt("ofcycle") + "/" + wuliu.getInt("cycle") + "次";
			String jihui = wuliu.getStr("jihui");
			jihui = jihui==null?"":(" | 忌讳的花:"+jihui);
			String remark = first+jihui;
			// 首单
			int isbuy = Db.queryInt("select isbuy from f_account where id=?", order.getInt("aid"));
			// 商品名
			String goodsname = Db.queryStr("select group_concat(b.name) from f_order_detail a left join f_flower_pro b on a.fpid=b.id where a.ordercode=? and a.type=1", ordercode);
			goodsname = goodsname + ";产品名称：";
			List<Record> opList = Db.find("select pid,type from f_order_pro where oid=?", id);
			for(int i=0;i<opList.size();i++){
				Record op = opList.get(i);
				int pid = op.getInt("pid");
				int type = op.getInt("type");
				String name = new String();
				if(type == 0){
					name = Db.queryStr("select fname from f_product where id=?", pid);
				}else{
					name = Db.queryStr("select name from f_flower_pro where id=?", pid);
				}
				if(i==0){
					goodsname += name;
				}else{
					if(order.getInt("ocount")==1){
						if(isbuy==0){
							goodsname += ";（首单赠品）："+name;
						}else{
							goodsname += ","+name;
						}
					}
				}
			}
			// 祝福语
			String zhufu = order.getStr("zhufu");
			// 送花人
			String songhua = order.getStr("songhua");
			if(((zhufu!=null && zhufu!="") || (songhua!=null && songhua!="")) && order.getInt("ocount")==1){
				goodsname += ";祝福："+zhufu+";送花人："+songhua;
			}
			System.out.println(id);
			result = synchroD2D(ordercode, number, wuliu.getStr("name"), wuliu.getStr("address"), wuliu.getStr("tel"), remark, goodsname);
			if(!result){
				break;
			}
		}
		if(result){
			Record record = new Record().set("code", dateCode).set("ctime", new Date());
			result = Db.save("f_tiaodan", record);
		}
		return result;
	}
	
	// 花美美订单信息同步至门对门商家系统
	public static boolean synchroD2D(String ClientCode, String WorkCode, String GetPerson, String GetAddress, String GetPhone, String remark, String GoodsName) {
		Map<String, String> params = new HashMap<>();
		params.put("ClientCode", ClientCode);	// 订单号
		params.put("WorkCode", WorkCode);		// 条码号(追溯号)
		params.put("GetPerson", GetPerson);		// 收件人
		
		String[] addArr = GetAddress.split("-", 4);
		String GetProvice="未填",GetCity="未填",GetCounty="未填";
		GetProvice = addArr[0];
		GetCity = addArr[1];
		GetCounty = addArr[2];
		
		params.put("GetProvice", GetProvice);	// 收件(省)
		params.put("GetCity", GetCity);		// 收件(市)
		params.put("GetCounty", GetCounty);	// 收件(区/县)
		
		params.put("GetAddress", GetAddress);	// 收件人详细地址
		params.put("GetPhone", GetPhone);		// 收件人联系电话
		params.put("OrderHav", "1.0");			// 订单重量
		params.put("ReplCost", "0");			// 代收货款
		params.put("WorkType", "0");			// 订单类型
		params.put("Payment", "1");				// 支付方式
		params.put("OrderCodeType", "1");		// 单号类型
		params.put("remark", remark);			// 备注
		params.put("GoodsName", GoodsName);		// 商品名称
		String jsonStr = JsonKit.toJson(params).replace("&", "");
		String signedStr = MD5.MD5Encode(jsonStr + Key);
		String requestStr = jsonStr + "&" + signedStr + "&" + custCode;
		String rtnStr = HttpUtils.post(testurl, requestStr);
		rtnStr = rtnStr.substring(1, rtnStr.length()-1);
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(rtnStr, HashMap.class);
		if(Integer.parseInt(map.get("Rtn_Code").toString()) == 1){
			return true;
		}else{
			return false;
		}
	}
	
	// 设置中文文件名
	public static String toUtf8String(String s){
		StringBuffer sb = new StringBuffer();
    	for (int i=0;i<s.length();i++){
    		char c = s.charAt(i);
    		if (c >= 0 && c <= 255){
    			sb.append(c);}
    		else{
    			byte[] b;
    			try {
    				b = Character.toString(c).getBytes("utf-8");
    			}catch (Exception ex) {
    				b = new byte[0];
    			}
    			for (int j = 0; j < b.length; j++) {
    				int k = b[j];
    				if (k < 0) k += 256;
    					sb.append("%" + Integer.toHexString(k).toUpperCase());
    				}
    			}
    	}
    	return sb.toString();
	}
	
/*	*//***
	 * 节日配单
	 * @param key
	 *//*
	public static void singleHo(String key){
		code = key;
		dateCode = Db.queryStr("SELECT code_value FROM f_dictionary WHERE code_key = 'holiday'");
		// 本批次的产品数量
		Number fpnum = Db.queryNumber("select count(1) from f_product where code = ? and type = 4", dateCode);
		if(fpnum.intValue() == 0){
			resultMap = new HashMap<>();
			resultMap.put("code", singlews.CODE0.code);
			resultMap.put("msg", "<span>节日批次&nbsp;" + dateCode + "&nbsp;鲜花产品尚未选配</span>");
			ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
		}else{
			Number oinum = Db.queryNumber("select count(1) from f_order_info where state>=2 and code=?", dateCode);
			if(oinum.intValue() > 0){	// 发货已完成
				resultMap = new HashMap<>();
				resultMap.put("code", singlews.CODE0.code);
				resultMap.put("msg", "<span>节日批次&nbsp;" + dateCode + "&nbsp;发货已完成</span>");
				ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
			}else{
				list = Db.find("select id,ordercode,aid,name,tel,addr,cycle,isvase,jh_list,jh_color,type,szdx,ocount,ishas,is_sy,sy_code from f_order where state=1 and type=5");
				if(list.size() == 0){
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE0.code);
					resultMap.put("msg", "<span>节日批次&nbsp;" + dateCode + "&nbsp;没有符合条件的订单</span>");
					ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
					return;
				}
				if(!HoliPriceValid(dateCode)){
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE0.code);
					resultMap.put("msg", "<span>节日批次&nbsp;" + dateCode + "&nbsp;花材价格不完善</span>");
					ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
					return;
				}
				resultMap = new HashMap<>();
				resultMap.put("code", singlews.CODE1.code);
				resultMap.put("msg", list.size());
				ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
				boolean tx_result = Db.tx(new IAtom() {
					@Override
					public boolean run() throws SQLException {
						boolean oi_result = false;
						Db.update("DELETE FROM f_order_info USING f_order_info,f_order WHERE f_order_info.ordercode = f_order.ordercode AND f_order.type=5 AND f_order_info.code =?", dateCode);
						// 获取待分配产品信息
						getProMap();
						for(int i=0;i<list.size();i++){
							Record order = list.get(i);
							oi_result = saveOrderInfo_jr(order);
							if(oi_result){
								resultMap = new HashMap<>();
								resultMap.put("code", singlews.CODE2.code);
								resultMap.put("msg", i+1);
								ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
							}else{
								break;
							}
						}
						return oi_result;
					}
				});
				if(tx_result){
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE3.code);
					ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
				}else{
					resultMap = new HashMap<>();
					resultMap.put("code", singlews.CODE4.code);
					resultMap.put("msg", "<span>配单出错，操作终止</span>");
					ChatAnnotation.broadcast(code, JsonKit.toJson(resultMap));	// 配单反馈信息
				}
			}
		}
	
	}*/
	// 导出物流单
		public static boolean getgroupleaderForExcel(HttpServletResponse response,String code)  {
		    String sql = "SELECT c.ordercode ,a.aid ,c.name , c.addr ,c.tel ,c.gName  FROM f_pingtuan a LEFT JOIN f_account b on a.aid = b.id LEFT JOIN f_order c on a.ptNo=c.ptNo and a.aid = c.aid " + 
		    		"LEFT JOIN f_order_info d on c.ordercode = d.ordercode WHERE  c.ocount=0 and  d.`code` = ?";
			List<Record> wllist = Db.find(sql,code);
			HSSFWorkbook wbook = new HSSFWorkbook();
			HSSFSheet sheet1 = wbook.createSheet();
			//设置列宽
			sheet1.setColumnWidth((short)0, (short)6400);
			sheet1.setColumnWidth((short)1, (short)6400);
			sheet1.setColumnWidth((short)2, (short)2400);
			sheet1.setColumnWidth((short)3, (short)18000);
			sheet1.setColumnWidth((short)4, (short)5400);
			sheet1.setColumnWidth((short)5, (short)6400);
			wbook.setSheetName(0, "团长"+code);
			//首行 样式1
	        HSSFCellStyle cellStyle = wbook.createCellStyle();
	        HSSFFont font = wbook.createFont();
	        font.setColor(HSSFColor.RED.index);    //红字
	        cellStyle.setFont(font);
	        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index); //填亮黄色
	        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
	        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
	        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
	        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
	        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
	        //样式2
	        HSSFCellStyle cellStyle1 = wbook.createCellStyle();
	        cellStyle1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
	        cellStyle1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
	        cellStyle1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
	        cellStyle1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
	       //首行 样式2
	        HSSFCellStyle cellStyle2 = wbook.createCellStyle();
	        font = wbook.createFont();
	        font.setColor(HSSFColor.RED.index);    //红字
	        cellStyle2.setFont(font);
	        cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	        cellStyle2.setFillForegroundColor(HSSFColor.TAN.index); //填橘黄
	        cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
	          
			HSSFRow row;
			for(int i=0;i<wllist.size();i++){
				if(i==0){
					row = sheet1.createRow(i);
					HSSFCell cell0 = row.createCell((short) 0);
					cell0.setCellStyle(cellStyle);
					cell0.setCellValue("团长订单号");
					HSSFCell cell1 = row.createCell((short) 1);
					cell1.setCellStyle(cellStyle);
					cell1.setCellValue("团长");
					HSSFCell cell2 = row.createCell((short) 2);
					cell2.setCellStyle(cellStyle);
					cell2.setCellValue("收货人姓名");
					HSSFCell cell3 = row.createCell((short) 3);
					cell3.setCellStyle(cellStyle);
					cell3.setCellValue("收货人地址");
					HSSFCell cell4 = row.createCell((short) 4);
					cell4.setCellStyle(cellStyle);
					cell4.setCellValue("收货人电话");
					HSSFCell cell5 = row.createCell((short) 5);
					cell5.setCellStyle(cellStyle);
					cell5.setCellValue("商品名称");
				}
				row = sheet1.createRow(i+1);
				Record r = wllist.get(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle1);
				cell0.setCellValue(r.getStr("ordercode"));
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle1);
				cell1.setCellValue(r.getInt("aid"));
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle1);
				cell2.setCellValue(r.getStr("name"));
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle1);
				cell3.setCellValue(r.getStr("addr"));
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle1);
				cell4.setCellValue(r.getStr("tel"));
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle1);
				cell5.setCellValue(r.getStr("gName"));				
			}
			response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String("groupleaders"+code)+ ".xls");
			response.setContentType("application/vnd.ms-excel;charset=utf-8");
			try {
				ServletOutputStream out = response.getOutputStream();
				wbook.write(out);
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
/************************************线下物流*********************************************************/
		public static boolean gettemplateForXXWL(HttpServletResponse response)  {
			HSSFWorkbook wbook = new HSSFWorkbook();
			HSSFSheet sheet1 = wbook.createSheet();
			//设置列宽
			sheet1.setColumnWidth((short)0, (short)6400);
			sheet1.setColumnWidth((short)1, (short)6400);
			sheet1.setColumnWidth((short)2, (short)6400);
			sheet1.setColumnWidth((short)3, (short)6400);
			sheet1.setColumnWidth((short)4, (short)6400);
			sheet1.setColumnWidth((short)5, (short)6400);
			sheet1.setColumnWidth((short)6, (short)6400);
			wbook.setSheetName(0, "线下物流单");
			//首行 样式1
	        HSSFCellStyle cellStyle = wbook.createCellStyle();
	        HSSFFont font = wbook.createFont();
	        font.setColor(HSSFColor.RED.index);    //红字
	        cellStyle.setFont(font);
	        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index); //填亮黄色
	        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
	        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
	        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
	        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
	        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
	        //样式2
	        HSSFCellStyle cellStyle1 = wbook.createCellStyle();
	        cellStyle1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
	        cellStyle1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
	        cellStyle1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
	        cellStyle1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
	       //首行 样式2
	        HSSFCellStyle cellStyle2 = wbook.createCellStyle();
	        font = wbook.createFont();
	        font.setColor(HSSFColor.RED.index);    //红字
	        cellStyle2.setFont(font);
	        cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	        cellStyle2.setFillForegroundColor(HSSFColor.TAN.index); //填橘黄
	        cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
	          
			HSSFRow row;
			for(int i=0;i<1;i++){
				if(i==0){
					row = sheet1.createRow(i);
					HSSFCell cell0 = row.createCell((short) 0);
					cell0.setCellStyle(cellStyle);
					cell0.setCellValue("发货批次");
					HSSFCell cell1 = row.createCell((short) 1);
					cell1.setCellStyle(cellStyle);
					cell1.setCellValue("第三方物流单号");
					HSSFCell cell2 = row.createCell((short) 2);
					cell2.setCellStyle(cellStyle);
					cell2.setCellValue("品名");
					HSSFCell cell3 = row.createCell((short) 3);
					cell3.setCellStyle(cellStyle);
					cell3.setCellValue("发货日期");
				}
				row = sheet1.createRow(i+1);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle);
				cell0.setCellValue("20171223");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("467100326733");
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("圣诞主题花");
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("20171223");
			}
			response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String("template_offline")+ ".xls");
			response.setContentType("application/vnd.ms-excel;charset=utf-8");
			try {
				ServletOutputStream out = response.getOutputStream();
				wbook.write(out);
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		public static boolean getExcelForXXWL(HttpServletResponse response,String fCode,String fNumber,String fwlName)  {
			 String select = "select id,fCode,fNumber,fGname,fDate,fwlName,fOptime,fUser,FORMAT(fPrice,2) as fPrice";
			 String sqlExceptSelect = " from f_order_info_xx where 1=1";
			 if (StrKit.notBlank(fCode)) {
				sqlExceptSelect+=String.format(" and fCode = '%s'", fCode);
			 }
			 if (StrKit.notBlank(fNumber)) {
					sqlExceptSelect+=String.format(" and fNumber = '%s'", fNumber);
		     }
			 if (StrKit.notBlank(fwlName)) {
					sqlExceptSelect+=String.format(" and fwlName = '%s'", fwlName);
		     }
			 sqlExceptSelect+= " order by fDate desc";
			 String sql = select + sqlExceptSelect;
			List<Record> wllist = Db.find(sql);
			HSSFWorkbook wbook = new HSSFWorkbook();
			HSSFSheet sheet1 = wbook.createSheet();
			//设置列宽
			sheet1.setColumnWidth((short)0, (short)6400);
			sheet1.setColumnWidth((short)1, (short)6400);
			sheet1.setColumnWidth((short)2, (short)6400);
			sheet1.setColumnWidth((short)3, (short)6400);
			sheet1.setColumnWidth((short)4, (short)6400);
			sheet1.setColumnWidth((short)5, (short)6400);
			sheet1.setColumnWidth((short)6, (short)6400);
			sheet1.setColumnWidth((short)7, (short)6400);
			wbook.setSheetName(0, "线下物流单");
			//首行 样式1
	        HSSFCellStyle cellStyle = wbook.createCellStyle();
	        HSSFFont font = wbook.createFont();
	        font.setColor(HSSFColor.RED.index);    //红字
	        cellStyle.setFont(font);
	        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index); //填亮黄色
	        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
	        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
	        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
	        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
	        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
	        //样式2
	        HSSFCellStyle cellStyle1 = wbook.createCellStyle();
	        cellStyle1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
	        cellStyle1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
	        cellStyle1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
	        cellStyle1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
	       //首行 样式2
	        HSSFCellStyle cellStyle2 = wbook.createCellStyle();
	        font = wbook.createFont();
	        font.setColor(HSSFColor.RED.index);    //红字
	        cellStyle2.setFont(font);
	        cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	        cellStyle2.setFillForegroundColor(HSSFColor.TAN.index); //填橘黄
	        cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
	          
			HSSFRow row;
			if (wllist.size()==0) {
				row = sheet1.createRow(0);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle);
				cell0.setCellValue("发货批次");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("第三方物流单号");
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("第三方物流成本");
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("品名");
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle);
				cell4.setCellValue("发货日期");
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle);
				cell5.setCellValue("物流商");
			}
			for(int i=0;i<wllist.size();i++){
				if(i==0){
					row = sheet1.createRow(i);
					HSSFCell cell0 = row.createCell((short) 0);
					cell0.setCellStyle(cellStyle);
					cell0.setCellValue("发货批次");
					HSSFCell cell1 = row.createCell((short) 1);
					cell1.setCellStyle(cellStyle);
					cell1.setCellValue("第三方物流单号");
					HSSFCell cell2 = row.createCell((short) 2);
					cell2.setCellStyle(cellStyle);
					cell2.setCellValue("第三方物流成本");
					HSSFCell cell3 = row.createCell((short) 3);
					cell3.setCellStyle(cellStyle);
					cell3.setCellValue("品名");
					HSSFCell cell4 = row.createCell((short) 4);
					cell4.setCellStyle(cellStyle);
					cell4.setCellValue("发货日期");
					HSSFCell cell5 = row.createCell((short) 5);
					cell5.setCellStyle(cellStyle);
					cell5.setCellValue("物流商");
				}
				row = sheet1.createRow(i+1);
				Record r = wllist.get(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle1);
				cell0.setCellValue(r.getStr("fCode"));
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle1);
				cell1.setCellValue(r.getStr("fNumber"));
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle1);
				cell2.setCellValue(r.getStr("fPrice"));
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle1);
				cell3.setCellValue(r.getStr("fGname"));
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle1);
				cell4.setCellValue(r.getStr("fDate"));
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle1);
				cell5.setCellValue(r.getStr("fwlName"));
			}
			response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String("offline")+ ".xls");
			response.setContentType("application/vnd.ms-excel;charset=utf-8");
			try {
				ServletOutputStream out = response.getOutputStream();
				wbook.write(out);
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		/*************************************物流单价导入**************************************************/
		public static boolean gettemplateForWLPR(HttpServletResponse response)  {
			HSSFWorkbook wbook = new HSSFWorkbook();
			HSSFSheet sheet1 = wbook.createSheet();
			//设置列宽
			sheet1.setColumnWidth((short)0, (short)6400);
			sheet1.setColumnWidth((short)1, (short)6400);
			wbook.setSheetName(0, "鲜花周边线下物流单价统一导入模版");
			//首行 样式1
	        HSSFCellStyle cellStyle = wbook.createCellStyle();
	        HSSFFont font = wbook.createFont();
	        font.setColor(HSSFColor.RED.index);    //红字
	        cellStyle.setFont(font);
	        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index); //填亮黄色
	        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
	        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
	        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
	        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
	        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
	        //样式2
	        HSSFCellStyle cellStyle1 = wbook.createCellStyle();
	        cellStyle1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
	        cellStyle1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
	        cellStyle1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
	        cellStyle1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
	       //首行 样式2
	        HSSFCellStyle cellStyle2 = wbook.createCellStyle();
	        font = wbook.createFont();
	        font.setColor(HSSFColor.RED.index);    //红字
	        cellStyle2.setFont(font);
	        cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	        cellStyle2.setFillForegroundColor(HSSFColor.TAN.index); //填橘黄
	        cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
	          
			HSSFRow row;
			for(int i=0;i<1;i++){
				if(i==0){
					row = sheet1.createRow(i);
					HSSFCell cell0 = row.createCell((short) 0);
					cell0.setCellStyle(cellStyle);
					cell0.setCellValue("第三方物流单号");
					HSSFCell cell1 = row.createCell((short) 1);
					cell1.setCellStyle(cellStyle);
					cell1.setCellValue("第三方物流单价(元)");
				}
				row = sheet1.createRow(i+1);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle);
				cell0.setCellValue("467100326733");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("10.00");
			}
			response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String("template_wlpr")+ ".xls");
			response.setContentType("application/vnd.ms-excel;charset=utf-8");
			try {
				ServletOutputStream out = response.getOutputStream();
				wbook.write(out);
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
}