package com.controller.front;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.WordUtils;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.controller.WeixinApiCtrl;
import com.dao.FCDao;
import com.dao.MCDao;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.gson.Gson;
import com.interceptor.FrontInterceptor;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.internal.JSON.JSON;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.MediaApi;
import com.jfinal.weixin.sdk.api.QrcodeApi;
import com.jfinal.weixin.sdk.api.CustomServiceApi.Articles;
import com.jfinal.weixin.sdk.api.MediaApi.MediaType;
import com.util.Constant;
import com.util.DaiYanURL;
import com.util.DeliveryDateUtil;
import com.util.DownloadFile;
import com.util.JDUtil;
import com.util.MD5;
import com.util.MD5Util;
import com.util.NewImageUtils;
import com.util.QRCodeUtil;
import com.util.SendMsgUtil;
import com.util.Sign;
import com.util.WordFiler;
import com.util.XMLParser;

import sensitivewdfilter.WordFilter;
import sun.applet.resources.MsgAppletViewer;
import sun.misc.BASE64Encoder;

/**
 * @Desc 业务相关
 * @author lxx
 * @date 2016/8/30
 */
@Before(FrontInterceptor.class)
public class FrontServiceCtrl extends Controller {

	// 购买
	public void buy() {
		int type = getParaToInt("type"); // 商品类型(1订阅,2送花,3周边,4兑换,5节日送花,6定制花束)
		int pid = getParaToInt("pid"); // f_flower_pro商品ID
		int cycle = getParaToInt("cycle"); // 订阅次数
		Integer vase = getParaToInt("vase"); // 购买的花瓶ID
		String adds = getPara("add"); // 加购商品

		int ptNo = getParaToInt("ptNo") == null ? 0 : getParaToInt("ptNo");
		setAttr("ptNo", ptNo);//团号
		int jsAid=getParaToInt("jsAid") == null ? 0 : getParaToInt("jsAid");
		setAttr("jsAid",jsAid);//入团介绍人的ID号
		

		Record product = Db.findById("f_flower_pro", pid);
		String imgurl = product.getStr("imgurl");
		String[] img = imgurl.split("-");
		product.set("imgurl", img[0]);
		setAttr("product", product); // 主商品
		setAttr("type", type); // 商品类型(1订阅,2送花,3周边,4兑换)
		setAttr("cycle", cycle); // 周期
		
		double yh = 0;

		// 根据产品判断是否赠送花瓶
		int isfirst = product.getInt("isinfirst");

		setAttr("isfirst", isfirst);

		if (type == 1) { // 匹配多买立减

			if (pid == 1) {
				String[] dmlj = product.getStr("dmlj").split("_");
				int[] numArr = { 1, 4, 12, 24,48 };
				for (int i = 0; i < numArr.length; i++) {
					if (numArr[i] == cycle) {
						yh = Double.valueOf(dmlj[i]);
					}
				}
			} else {
				String[] dmlj2 = product.getStr("dmlj").split("_");
				int[] numArr = { 1, 4, 12, 24,48 };
				for (int i = 0; i < numArr.length; i++) {
					if (numArr[i] == cycle) {
						yh = Double.valueOf(dmlj2[i]);
					}
				}
			}
			// 是否 加购商品
			if (getPara("add") != null) {
				setAttr("adds", getPara("add"));
			}
		}
		// 拼团
		if (type == 8) {
			String[] dmlj4 = product.getStr("dmlj").split("_");
			int[] numArr = { 1, 4, 12, 24 };
			for (int i = 0; i < numArr.length; i++) {
				if (numArr[i] == cycle) {
					yh = Double.valueOf(dmlj4[i]);
				}
			}
		}
		// 多双交替
		if (type == 9) {
			String[] dmlj4 = product.getStr("dmlj").split("_");
			int[] numArr = { 4, 12, 24 };
			for (int i = 0; i < numArr.length; i++) {
				if (numArr[i] == cycle) {
					yh = Double.valueOf(dmlj4[i]);
				}
			}
			// 是否 加购商品
			if (getPara("add") != null) {
				setAttr("adds", getPara("add"));
			}
		}
		if (type == 6) {
			String[] dmlj3 = product.getStr("dmlj").split("_");
			int[] numArr = { 1, 4, 12, 24 };
			for (int i = 0; i < numArr.length; i++) {
				if (numArr[i] == cycle) {
					yh = Double.valueOf(dmlj3[i]);
				}
			}
			// 是否 加购商品
			if (getPara("add") != null) {
				setAttr("adds", getPara("add"));
			}
		}

		if (type == 2) { // 送花
			int szdx = getParaToInt("szdx"); // 适赠对象ID
			setAttr("szdx", szdx);
		}

		setAttr("vase", vase); // 花瓶ID
		Record account = getSessionAttr("account"); // 用户信息
		int isbuy = account.getInt("isbuy"); // 是否第一次购买
		setAttr("isbuy", isbuy);

		double price = FCDao.countPrice(product.getDouble("price"), cycle, vase, adds); // 商品总价值
		// 拼团的商品，采用拼团价
		if (type == 8) {
			price = FCDao.countPrice(product.getDouble("ptPrice"), cycle, vase, adds);
		}

		double totalprice = price - yh; // 实际付出金额
		Record activity = FCDao.getActivity(totalprice); // 活动

		String reachtype = product.getStr("reachtype");

		// 日期处理
		String reach[] = reachtype.split(":");
		String time[] = null;
		int festivalId = getParaToInt("festivalId") == null ? 0 : getParaToInt("festivalId");
		switch (reach[0]) {
		case "1":
			// 订阅 ,送花,拼团 // 定制 鲜花
			// 周一 周六
			setAttr("Timetype", 1);

			setAttr("festivalId", festivalId);
			break;
		case "2":
			// 周边
			// t+1
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			Date date = calendar.getTime();
			System.out.println(sdf.format(date));// 若输入为：sdf.format(date)，则输入的为固定格式的字符串

			setAttr("festivalId", festivalId);
			setAttr("Timetype", 2);
			setAttr("hDate", sdf.format(date));
			break;
		case "3":
			// 节日鲜花,闪购、主题花
			// 指定日期

			setAttr("festivalId", festivalId);

			time = reach[1].split("_");
			setAttr("Timetype", 3);
			setAttr("hDatelist", time);

			break;
		}

		if (activity != null) {
			setAttr("activity", activity);
			double benefit = activity.getDouble("benefit");
			totalprice -= benefit;
			yh += benefit;
		}
		setAttr("yh", yh); // 优惠总额
		setAttr("cashlist", FCDao.getCashAble(account.getInt("id"), totalprice, pid)); // 花票
		setAttr("maxcash", FCDao.getMaxCash(account.getInt("id"), totalprice, pid)); // 优惠最多的花票
		setAttr("price", price); // 商品总价值
		setAttr("totalprice", totalprice); // 实际付出金额

		Record address = FCDao.getDefaultAddress(account.getInt("id"), 1, getParaToInt("addr"));
		// 查询用户是否购买过花瓶
		if (address != null) {
			String tel = address.getStr("tel");
			Number tip_vase = Db
					.queryNumber("select count(*)  from f_order a left join f_order_detail b on a.ordercode=b.ordercode"
							+ " left join f_flower_pro c on b.fpid=c.id where a.tel=?"
							+ " and ptid=4 and a.state in(1,2,3) ", tel);
			if (tip_vase.intValue() != 0) {
				setAttr("tip_vase", tip_vase);
			}
		}

		setAttr("address", address); // 收货地址
		setAttr("jhclos", FCDao.getjihuicolor());
		setAttr("jihuis", FCDao.getjihuiType());

		Record user = Db.findById("f_account", account.getInt("id"));
		setAttr("jhflower", user.getStr("jhflower").split("_"));// 忌讳的花类
		setAttr("jhcolor", user.getStr("jhcolor").split("_"));// 忌讳的色系

		// 选择卡面
		List<Record> cardlist = Db.find(
				"select a.id,a.typeId,b.code_name,a.imgurl01 from f_wishcardpic as a left join f_dictionary as b on a.typeId=b.code_value where a.imgOrderId=1 and  b.code_key='wishcardType' and b.state=1 and a.state=1");
		setAttr("cardlist", cardlist);

		if (type == 1 || type == 2 || type == 8) { // 订阅 ,送花,拼团
			render("buy.html");
		}
		if (type == 3) { // 周边
			setAttr("aid",account.getInt("id"));
			setAttr("ptid",product.getInt("ptid"));
			render("buy_around.html");
		}
		if (type == 5 || type == 10 || type == 11) {// 节日鲜花,闪购、主题花,现场活动
			render("buy_festival.html");
		}
		if (type == 6) { // 定制 鲜花
			product.set("imgurl", "/image/flower_custom.jpg");
			product.set("name", "定制花束");
			String fpid = getPara("fpid");
			setAttr("fpid", fpid);
			render("buy.html");
		}
	}

	/***
	 * 通过兑换码查询花票的面额,及ID 面额为0：:已经使用或不存在
	 */
	public void cashIsValid() {
		String cashcode = getPara("cashcode");// 兑换码

		String price = getPara("price") == null ? "0" : getPara("price");// 商品总价，满199减10

		// System.out.println(price);

		Record record = Db.findFirst(
				"select a.id, IF(AVG(money) IS NULL,0, money) as 'money'  from f_cash a left join f_cashclassify b on a.cid=b.id  where code=? and aid=0 and benefit<=? and NOW()<=time_b",
				cashcode, price);

		renderJson(record);
	}

	/**
	 * 根据订单号查询已经下架的商品数量
	 */
	public void unSellCount() {
		String ordercode = getPara(0);
		Long count = Db.queryLong(
				"select count(1) from f_order_detail a left join f_flower_pro b on a.fpid=b.id where ordercode=? and state<>0",
				ordercode);
		renderJson(count);
	}

	// 选择收货地址
	public void chooseaddress() {

		if (getParaToInt("addr") != null) {

			setAttr("addr", getParaToInt("addr"));
			int type = getParaToInt("type"); // 商品类型(1订阅,2送花,3周边,4兑换,41兑换鲜花,43兑换周边)
			int give = 1;
			Record account = getSessionAttr("account");
			setAttr("addresslist", FCDao.getAddressForType(account.getInt("id"), give));
			setAttr("type", type);
			render("address_choose.html");
		} else {
			setAttr("addr", 1);
			render("address_choose.html");
		}

	}

	// 更换收货地址
	public void changeaddress() {
		setAttr("addr", getParaToInt("addr"));
		int type = getParaToInt("type"); // 商品类型(1订阅,2送花,3周边,4兑换)
		int give = 1;
		Record account = getSessionAttr("account");
		setAttr("addresslist", FCDao.getAddressForType(account.getInt("id"), give));

		setAttr("type", type);
		setAttr("ordercode", getPara("ordercode"));
		render("address_change.html");
	}

	/**
	 * 是否可以再次购买公益花束
	 * 
	 * @return
	 */
	public void isValidfesPid6() {
		Boolean result = false;
		Record account = getSessionAttr("account");
		Long count = Db.queryLong("select count(1) from f_order where aid=? and type=7 and state in(0,1,2,3)",
				account.getInt("id"));

		if (count >= 1) {
			result = false;
		} else {
			result = true;
		}
		renderJson(result);
	}

	/**
	 * 活动支付接口
	 * 
	 * @author Glacier
	 * @throws Exception
	 * @date 2017年8月3日
	 */
	public void activity_pay() throws Exception {

		int pid = getParaToInt("pid"); // 商品ID

		int type = 7; // 商品类型 1订阅,2送花,3周边,4兑换,5节日,6定制,7活动
		Integer szdx = null; // 适赠对象ID

		Integer vase = null; // 花瓶ID
		int addressid = 1; // 收货地址ID
		String adds = ""; // 加购商品

		int reach = 3; // 送达日期
		int cycle = 1; // 周期

		// 周期 用途 格调
		int zhouqi = 1;
		int use = 1;
		int style = 1;

		String zhufu = null; // 祝福卡
		String songhua = null; // 送花人
		String jh_list = null; // 忌讳的花
		String jhcolor_list = null; // 忌讳色系
		String recommend = null; // 邀请人手机号
		Integer cash = null; // 用户选择符合条件的花票
		Integer cash2 = null;// 用户通过兑换码使用的花票
		Integer activity = null;// 活动
		Double yh = 0.0; // 优惠总额
		// 创建订单
		Map<String, Object> xmlMap = new HashMap<>();

		int festivalId = 6;
		xmlMap = FCDao.createOrder(getRequest(), getSession(), pid, vase, addressid, reach, cycle, zhouqi, use, style,
				zhufu, songhua, jh_list, jhcolor_list, type, recommend, szdx, cash, activity, yh, festivalId, adds, " ",
				cash2, 0, 1, 0,0);
		// 支付
		if ((boolean) xmlMap.get("result")) {
			setAttr("ordercode", xmlMap.get("ordercode"));
			setAttr("ptNo", "0");
			setAttr("payMap", Sign.sign2(xmlMap.get("prepay_id").toString()));
			setAttr("detaillist", xmlMap.get("detaillist"));
			render("topay.html");
		} else {
			render("error.html");
		}

	}

	// 下单
	public void createorder() throws ParserConfigurationException, IOException, SAXException {

		int type = getParaToInt("type"); // 商品类型 1订阅,2送花,3周边,4兑换,5节日,6定制
		Integer szdx = getParaToInt("szdx"); // 适赠对象ID
		int pid = getParaToInt("pid"); // 商品ID
		int ptNo = getParaToInt("ptNo") == null ? 0 : getParaToInt("ptNo");// 团号
		int jsAid=getParaToInt("jsAid") == null ? 0 : getParaToInt("jsAid");// 入团介绍人的id

		int num = getParaToInt("num") == null ? 1 : getParaToInt("num");// 选择的商品数量，用在周边中
		Integer vase = getParaToInt("vase"); // 花瓶ID
		int addressid = getParaToInt("address"); // 收货地址ID
		String adds = getPara("adds"); // 加购商品

		int reach = getParaToInt("reach"); // 送达日期
		int cycle = getParaToInt("cycle"); // 周期

		// 周期 用途 格调
		int zhouqi = getParaToInt("zhouqi");
		int use = getParaToInt("use");
		int style = getParaToInt("style");

		String zhufu = getPara("zhufu"); // 祝福语
		String songhua = getPara("songhua"); // 送花人
		int picId = getParaToInt("picId") == null ? 0 : getParaToInt("picId");// 祝福卡ID号
		String jh_list = getPara("jh_list"); // 忌讳的花
		String jhcolor_list = getPara("jhcolor_list"); // 忌讳色系
		String recommend = getPara("recommend"); // 邀请人手机号
		Integer cash = getParaToInt("cash"); // 用户选择的符合条件的花票
		Integer cash2 = getParaToInt("cash2");// 用户通过兑换码使用的花票
		Integer activity = getParaToInt("activity");// 活动
		Double yh = Double.parseDouble(getPara("yh")); // 优惠总额

		String jr_picode = getPara("jr_picode");// 节日订单的批号

		//System.out.println(jr_picode);

		// 创建订单
		Map<String, Object> xmlMap = new HashMap<>();
		// 节日鲜花 reach为3 利用cycle传节日id
		int festivalId = 0;
		if (type == 5 || type == 10 || type == 11) {
			festivalId = getParaToInt("festivalId");
		}

		if (type == 6) { // 定制鲜花
			String fpid = getPara("fpid");
			xmlMap = FCDao.createOrder(getRequest(), getSession(), pid, vase, addressid, reach, cycle, zhouqi, use,
					style, zhufu, songhua, jh_list, jhcolor_list, type, recommend, szdx, cash, activity, yh, fpid, adds,
					cash2, picId);
		} else {
			xmlMap = FCDao.createOrder(getRequest(), getSession(), pid, vase, addressid, reach, cycle, zhouqi, use,
					style, zhufu, songhua, jh_list, jhcolor_list, type, recommend, szdx, cash, activity, yh, festivalId,
					adds, jr_picode, cash2, ptNo, num, picId,jsAid);
		}

		if ((boolean) xmlMap.get("result")) {
			setAttr("ordercode", xmlMap.get("ordercode"));
			setAttr("ptNo", xmlMap.get("ptNo"));
			setAttr("payMap", Sign.sign2(xmlMap.get("prepay_id").toString()));
			setAttr("detaillist", xmlMap.get("detaillist"));
			setAttr("num", num);
			setAttr("type", type);
			render("topay.html");
		} else {
			render("error.html");
		}
	}

	/**
	 * 拉新商品是否允许购买
	 */
	public void isAllowBuy(){
		int aid=getParaToInt(0);
		String tjid="6_"+aid;
		Long count=Db.queryLong("select count(1) from f_account where tjid=?  and id not in(select aid from f_redpackets_log)",tjid);
	    boolean result=false;
	    if(count>=10){
	    	result=true;
	    }
	    renderJson(result);
	}
	/*-------------------------------------------------------------------订单------------------------------------------------*/
	// 我的订单
	public void myorder() {
		Integer pageno = getParaToInt(0) == null ? 1 : getParaToInt(0);
		// 订单状态:9全部[0未付款，1服务中，2待评价，3已完成，4退款，5交易取消]
		Integer state = getParaToInt(1) == null ? 9 : getParaToInt(1);
		Record account = getSessionAttr("account");
		Page<Record> page = FCDao.getMyOrder(pageno, 100, account.getInt("id"), state);
		setAttr("seedcount", FCDao.getFlowerSeed(account.getInt("id")));

		if (account.getInt("isbuy") == 0) {
			for (Record r : page.getList()) {
				if (r.getInt("state") == 1 || r.getInt("state") == 2 || r.getInt("state") == 3
						|| r.getInt("state") == 4) {
					account.set("isbuy", 1);
					break;
				}
			}
		} else if (account.getInt("isbuy") == 1) {
			int count = 0;
			for (Record r : page.getList()) {
				if (r.getInt("state") == 1 || r.getInt("state") == 2 || r.getInt("state") == 3
						|| r.getInt("state") == 4) {
					count++;
				}
			}
			if (count >= 2) {
				account.set("isbuy", 2);
			}
		}
		if (pageno == 1) {
			setAttr("state", state == null ? 9 : state);
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("orderlist", page.getList());
			// rstate 判断是否退款

			List<Record> orderlist = page.getList();
			/*
			 * for (Record list : orderlist) { System.out.println(list); }
			 */

			// 去除重复list
			for (int i = 0; i < orderlist.size(); i++) {
				// 去除 type =6 相同的条目
				for (int j = orderlist.size() - 1; j > i; j--) {
					if (orderlist.get(i).getInt("type") == orderlist.get(j).getInt("type")
							&& orderlist.get(i).get("ordercode").equals(orderlist.get(j).get("ordercode"))) {
						// 送花次数 订单号 处理
						orderlist.get(i).set("name", "定制鲜花");
						orderlist.get(i).set("imgurl", "/image/flower_custom.jpg");
						orderlist.remove(j);
					}
				}

			}

			render("order_my.html");
		} else {
			renderJson(page.getList());
		}
	}

	// 订单详情
	public void orderinfo() throws ParseException {
		String ordercode = getPara();
		Record account = getSessionAttr("account");
		Map<String, Object> map = FCDao.getOrderInfo(account.getInt("id"), ordercode);
		int cycle = (int) map.get("cycle");
		double yhje = (double) map.get("yhje");

		String yhfs = (String) map.get("yhfs");
		if (yhfs.charAt(0) == '3') {
			setAttr("yhfs", 3);
		} else if (yhfs.charAt(0) == '4') {
			setAttr("yhfs", 4);
		} else {
			setAttr("yhfs", 0);
		}
	
		String[] dmlj = FCDao.getDmlj(ordercode).split("_");
		int[] numArr = { 1, 4, 12 };
		for (int i = 0; i < numArr.length; i++) {
			if (numArr[i] == cycle) {
				yhje += Double.valueOf(dmlj[i]);
			}
		}

		
		setAttr("yh", yhje); // 优惠总额
		setAttr("allprice", FCDao.getTotalprice(ordercode, cycle));

		Record order = (Record) map.get("order");
		String addrselect = order.get("addr");
		if (addrselect != null) {
			int addrid;
			List<Record> addresses = FCDao.getAddresses(account.getInt("id"));
			for (Record record : addresses) {
				if (record.get("addr").equals(addrselect)) {
					addrid = record.get("id");
					setAttr("addrid", addrid); // 收货地址
				}
			}
		}
		Map<String, Object> pdmap = DeliveryDateUtil.chooseDate();
		if (pdmap.get("reach") == order.get("reach")) {
			if ((boolean) pdmap.get("result")) {
				setAttr("result", 0);
			} else {
				setAttr("result", 1);
			}
		} else {
			setAttr("result", 1);
		}
		setAttr("seedcount", FCDao.getFlowerSeed(account.getInt("id")));
		setAttr("needSeeds", map.get("needSeeds"));

		// 定制 鲜花
		Record tmp = (Record) map.get("order");
		if (tmp.getInt("type") == 6) {
			String gName = Db.queryStr("SELECT gName FROM f_order WHERE ordercode = ?", tmp.getStr("ordercode"));
			tmp.set("name", "定制鲜花");
			tmp.set("gName", gName.replaceFirst("-", "："));
			tmp.set("imgurl", "/image/flower_custom.jpg");
			setAttr("order", tmp);
		} else {
			String gName = Db.queryStr("SELECT gName FROM f_order WHERE ordercode = ?", tmp.getStr("ordercode"));
			tmp.set("gName", gName.replaceFirst("-", "-"));
			setAttr("order", map.get("order"));
		}

		// 是否已经产生物流单，如果已经产生物流单，不允许退款
		Long countwl = Db.queryLong("select count(1) from f_order_info where ordercode=?", ordercode);
		setAttr("countwl", countwl);// 如果该字段大约0，不允许退款

		// 是否支持退款，根据f_flower_pro表中的allowRefund字段【0：不可以退款 1：可以退款】
		int allowRefund = Db
				.queryInt("select b.allowRefund from f_order_detail as a left join f_flower_pro as b on a.fpid=b.id "
						+ " where a.ordercode=? and a.type=1", order.getStr("ordercode"));
		setAttr("allowRefund", allowRefund);

		setAttr("picilist", map.get("picilist"));
		setAttr("isAllowSY", isAllowSY(ordercode));// 是否允许顺延
		render("order_info.html");
	}

	/**
	 * 是否允许顺延
	 * 
	 * @param ordercode
	 * @return
	 */
	private static boolean isAllowSY(String ordercode) {
		boolean flag = false;
		int isinfirst = Db.queryInt("select allowSY from f_flower_pro where id "
				+ " in(select fpid from f_order_detail where type=1 and ordercode=?) LIMIT 1", ordercode);
		if (isinfirst == 1) {
			flag = true;
		}
		return flag;
	}

	// 取消订单
	public void cancelorder() {
		String ordercode = getPara();
		Record account = getSessionAttr("account");
		renderJson(FCDao.cancelOrder(account.getInt("id"), ordercode));
	}

	// 撤销退款
	public void restorefund() {
		String ordercode = getPara();
		boolean result = MCDao.restorefund(ordercode);
		renderJson(result);
	}

	// 更改订单地址
	public void changeorderaddr() throws ParseException {
		String ordercode = getPara("ordercode");
		int addrid = getParaToInt("id");
		boolean result = FCDao.changeorderaddr(ordercode, addrid);
		if (result) {
			Record account = getSessionAttr("account");
			Map<String, Object> map = FCDao.getOrderInfo(account.getInt("id"), ordercode);
			int cycle = (int) map.get("cycle");
			double yhje = (double) map.get("yhje");
			if ((int) map.get("type") == 1) { // 匹配多买立减
				String[] dmlj = FCDao.getDmlj().split("_");
				int[] numArr = { 1, 4, 12 };
				for (int i = 0; i < numArr.length; i++) {
					if (numArr[i] == cycle) {
						yhje += Double.valueOf(dmlj[i]);
					}
				}
			}
			setAttr("yh", yhje); // 优惠总额
			setAttr("allprice", FCDao.getTotalprice(ordercode, cycle));

			Record order = (Record) map.get("order");
			String addrselect = order.get("addr");
			int addrid1;
			if (addrselect != null) {
				List<Record> addresses = FCDao.getAddresses(account.getInt("id"));
				for (Record record : addresses) {
					if (record.get("addr").equals(addrselect)) {
						addrid1 = record.get("id");
						setAttr("addrid", addrid1); // 收货地址
					}
				}
			}
			Map<String, Object> pdmap = DeliveryDateUtil.chooseDate();
			if (pdmap.get("reach") == order.get("reach")) {
				if ((boolean) pdmap.get("result")) {
					setAttr("result", 0);
				} else {
					setAttr("result", 1);
				}
			} else {
				setAttr("result", 1);
			}
			setAttr("seedcount", FCDao.getFlowerSeed(account.getInt("id")));
			setAttr("order", map.get("order"));
			setAttr("picilist", map.get("picilist"));
			setAttr("addrid", addrid);
			setAttr("ordercode", ordercode);
			// render("order_info.html");
			render("postpone.html");
		}
	}

	public void payfororder() throws ParserConfigurationException, IOException, SAXException {
		String ordercode = getPara("ordercode");
		int ptNo = Db.queryInt("select ptNo from f_order where ordercode=?", ordercode);
		Record account = getSessionAttr("account");
		Map<String, Object> xmlMap = FCDao.payForOrder(getRequest(), account.getStr("openid"), account.getInt("id"),
				ordercode);
		setAttr("ordercode", ordercode);
		setAttr("payMap", Sign.sign2(xmlMap.get("prepay_id").toString()));
		setAttr("detaillist", xmlMap.get("detaillist"));
		setAttr("ptNo", ptNo);
		render("topay.html");
	}

	// 退款申请
	public void refund() {
		String ordercode = getPara();
		Record account = getSessionAttr("account");
		Record order = FCDao.refund(account.getInt("id"), ordercode);

		String imgurl = order.getStr("imgurl");
		String[] img = imgurl.split("-");
		order.set("imgurl", img[0]);

		setAttr("order", order);
		render("refund.html");
	}

	/**
	 * 是否允许退款
	 */
	public void isallowResund() {
		String ordercode = getPara();
		boolean flag = FCDao.isAllowRefund(ordercode);
		renderJson(flag);
	}

	// 赠送好友-获取信息
	public void orderreceive() {
		String ordercode = getPara();
		Record account = getSessionAttr("account");
		setAttr("order", FCDao.getOrderToReceive(account.getInt("id"), ordercode));
		render("order_receive.html");
	}

	// 赠送好友-验证与提交
	public void orderreveivevalidandsave() {
		String ordercode = getPara(0);
		int cycle = getParaToInt(1);
		Record account = getSessionAttr("account");
		renderJson(FCDao.orderReveiveValidAndSave(account.getInt("id"), ordercode, cycle));
	}

	// 提交退款申请
	public void saverefund() {

		String ordercode = getPara("ordercode");
		int ocount = Db.queryInt("SELECT ocount FROM f_order WHERE ordercode = ?", ordercode);
		Long seedCount = Db.queryLong("select count(1) from f_flowerseed where username=? and state=1", ordercode);
		boolean R = false;
		String message = "";
		if (ocount > 0) {
			message = "商品已发货，不能退款哦";
		} else if (seedCount > 0) {
			message = "购买所得的花籽,已经被您使用了,故此单不支持退订了哦";
		} else {
			R = true;
			String remark = getPara("remark");
			Record account = getSessionAttr("account");
			FCDao.saveRefund(account.getInt("id"), ordercode, remark);
		}
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("result", R);
		responseMap.put("message", message);

		renderJson(responseMap);
	}

	// 我的物流
	public void mylogistics() {
		Integer pageno = getParaToInt(0) == null ? 1 : getParaToInt(0);
		// 订单状态:9全部[0未付款，1服务中，2待评价，3已完成，4退款，5交易取消]
		Record account = getSessionAttr("account");
		Page<Record> page = FCDao.getMylogistics(pageno, 16, account.getInt("id"));
		if (pageno == 1) {
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("orderlist", page.getList());
			render("logistics_my.html");
		} else {
			renderJson(page.getList());
		}
	}

	// 确认收货
	public void shipconfirm() {
		String workcode = getPara();
		Record account = getSessionAttr("account");
		renderJson(FCDao.shipconfirm(account.getInt("id"), workcode));
	}

	// 物流详情
	public void logisticsinfo() {
		render("logistics_info.html");
	}

	// 去评价
	public void evaluate() throws ParseException {
		String ordercode = getPara();
		Record account = getSessionAttr("account");
		Map<String, Object> map = FCDao.getOrderInfo(account.getInt("id"), ordercode);
		setAttr("order", map.get("order"));
		render("evaluate.html");
	}

	// 保存评价
	public void saveEvaluate() {
		Record account = getSessionAttr("account");
		int aid = account.get("id");
		String openId = account.get("openid");
        String lognumber = getPara("lognumber");
		String ordercode = getPara("ordercode");
		Integer star = getParaToInt("star");
		String eva = getPara("eva");
		Number num = Db.queryNumber("select count(1) from f_comment where ordercode = ? and number=?",ordercode,lognumber);
        if (num.intValue()>0) {
			renderJson(false);return;
		}
		renderJson(FCDao.saveEvaluate(aid, openId, ordercode, star, eva,lognumber));
	}

	// 物流编号查询
	public void getlogistics() {
		String workcode = getPara("workcode");
		boolean result = false;
		String addr;
		Map<String, Object> map = new HashMap<>();
		Record order_info = Db.findFirst("select *from f_order_info where lognumber = ?", workcode);
		String url = Db.queryStr("select url from f_express where code = ?",order_info.getStr("ecode").trim());
		if (order_info.getStr("ecode").trim().equals("jd")) {
			try {
				result = true;
				JSONArray JDlogistics = JDUtil.getJDlogisticdata(workcode);
				map.put("orderinfo", order_info);
				map.put("JDlogistics", JDlogistics);
				map.put("result", result);
				renderJson(map);
				return;
			} catch (JdException e) {
				e.printStackTrace();
				result = false;
				addr = url;
				map.put("orderinfo", order_info);
				map.put("addr", addr);
				map.put("result", result);
				renderJson(map);
				return;
			}
		} else if (order_info.getStr("ecode").trim().equals("d2d"))  {
			result = true;
			List<Record> logistics = FCDao.getlogisticsInfo(workcode);
			map.put("orderinfo", order_info);
			map.put("logistics", logistics);
			map.put("result", result);
			renderJson(map);
			return;
		} else {
			result = false;
			addr = url;
			map.put("orderinfo", order_info);
			map.put("addr", addr);
			map.put("result", result);
			renderJson(map);
		}

	}

	/**
	 * 是否允许修改订单的送达日期
	 */
	public void isAllowChange() {
		String ordercode = getPara(0);
		renderJson(FCDao.isAllowChange(ordercode));
	}

	// 订单顺延
	public void chOrderreach() {
		String ordercode = getPara("ordercode");
		int addrid = getParaToInt("id");
		Record order = Db.findFirst("select reach,addr,name as receiver,tel,type from f_order where ordercode=?",
				ordercode);
		setAttr("addrid", addrid);
		setAttr("order", order);
		setAttr("ordercode", ordercode);
		render("postpone.html");
	}

	// 配送日期顺延
	public void deliverdatepost() throws ParseException {
		String ordercode = getPara(0);
		int addrid = getParaToInt(1);
		setAttr("ordercode", ordercode);
		setAttr("addrid", addrid);
		Record piCode = Db.findFirst(
				"select id,orderCode,piCode,reach, case reach when 1 then '周一' else '周六' end 'reachStr' ,num from f_picode where orderCode=? and str_to_date(piCode,'%Y%m%d')>NOW() order by num  ",
				ordercode);
		setAttr("pnum", piCode.getInt("num"));// 从第几次开始顺延
		setAttr("reach", piCode.getInt("reach"));
		setAttr("reachStr", piCode.getStr("reachStr"));// 周一还是周六
		setAttr("piCodeStart", piCode.getStr("piCode"));// 从哪个批次开始顺延
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date piDate = sdf.parse(piCode.getStr("piCode"));
		Calendar now = Calendar.getInstance();
		now.setTime(piDate);
		now.add(Calendar.DAY_OF_MONTH, 7);
		setAttr("piCodeEnd", sdf.format(now.getTime()));// 顺延到哪个批次
		render("datepost.html");
	}

	// 到达时间修改
	public void reachpost() throws ParseException {
		String ordercode = getPara(0);
		int addrid = getParaToInt(1);
		setAttr("ordercode", ordercode);
		setAttr("addrid", addrid);
		Record piCode = Db.findFirst(
				"select id,orderCode,piCode,reach, case reach when 1 then '周一' else '周六' end 'reachStr' ,num from f_picode where orderCode=? and str_to_date(piCode,'%Y%m%d')>NOW() order by num  ",
				ordercode);
		setAttr("pnum", piCode.getInt("num"));
		setAttr("reach", piCode.getInt("reach"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date piDate = sdf.parse(piCode.getStr("piCode"));
		Calendar now = Calendar.getInstance();
		now.setTime(piDate);
		String saturday = new String();
		String monday = new String();
		if (piCode.getInt("reach") == 1) {// 如果是周一
			now.add(Calendar.DAY_OF_MONTH, 5);// 奇数次修改每个批号+5天
			saturday = sdf.format(now.getTime());
			monday = piCode.getStr("piCode");
		} else {
			saturday = piCode.getStr("piCode");
			now.add(Calendar.DAY_OF_MONTH, 2);// 奇数次修改每个批号+2天
			monday = sdf.format(now.getTime());

		}
		setAttr("mon", monday);
		setAttr("sat", saturday);
		render("reachpost.html");
	}

	// 保存送达时间
	public void saveReachChange() {
		int reach = getParaToInt("reach");
		String ordercode = getPara("ordercode");
		boolean Rnum = false;
		if (reach == 2) {
			Rnum = DeliveryDateUtil.changePiCodeList(ordercode, 1, 2, 0);// 周1变周6
		} else {
			Rnum = DeliveryDateUtil.changePiCodeList(ordercode, 2, 1, 0);// 周6变周1
		}

		renderJson(Rnum);
	}

	// 日期顺延
	public void saveponedate() {
		Map<String, Object> map = new HashMap<>();
		boolean result = false;
		String msg = new String();
		String ordercode = getPara("ordercode");
		boolean Unum = DeliveryDateUtil.changePiCodeList(ordercode, 0, 0, 1);
		if (Unum == true) {
			result = true;
			msg = "顺延成功";
		} else {
			msg = "您最多可以修改/顺延两次哦";
		}
		map.put("result", result);
		map.put("msg", msg);
		renderJson(map);
	}

	/*--------------------------------------------------------兑换卡----------------------------------------------------*/
	// 兑换卡 创建订单 (增加下单验证)
	public void createorderofcard() {
		int cardNum = getParaToInt("cardNum"); // 卡号
		int reach = getParaToInt("reach"); // 送达时间
		String jh_list = getPara("jh_list"); // 忌讳的花
		String jhcolor_list = getPara("jhcolor_list");// 忌讳的色系

		String zhufu = getPara("zhufu"); // 祝福语
		String songhua = getPara("songhua"); // 送花人

		// 判断是否已经兑换
		int cIsSuccess = Db.queryInt("SELECT cIsSuccess FROM f_card WHERE cId= ?", cardNum);
		if (cIsSuccess == 1) {
			// 已经兑换的不可以再兑换了
			renderJson(false);
		} else {
			// 通过卡号查询兑换卡详情
			int cNo = Db.queryInt("SELECT cFkNo FROM f_card WHERE cId=?", cardNum);
			Record card = Db.findFirst("SELECT cPid,cCycle,cMoney FROM f_card_type WHERE cNo=?", cNo);
			Record account = getSessionAttr("account");

			List<Record> address = FCDao.getAddress(account.getInt("id"));
			Record temp = address.get(address.size() - 1);

			int addressid = temp.getInt("id");// 地址id
			int type = card.getInt("cPid"); // 商品类型(1订阅,2送花,3周边,4兑换)
			int cycle = card.getInt("cCycle"); // 订阅次数

			/*
			 * double yh = 0; if (type == 1) { // 匹配多买立减 if (type == 1) {
			 * String[] dmlj = FCDao.getDmlj().split("_"); int[] numArr = { 1,
			 * 4, 12 }; for (int i = 0; i < numArr.length; i++) { if (numArr[i]
			 * == cycle) { yh = Double.valueOf(dmlj[i]); } } } else { String[]
			 * dmlj2 = FCDao.getDmlj2().split("_"); int[] numArr = { 1, 4, 12 };
			 * for (int i = 0; i < numArr.length; i++) { if (numArr[i] == cycle)
			 * { yh = Double.valueOf(dmlj2[i]); } } } }
			 */
			double cMoney = 0;// 2017-11-16倪工说去掉，不用记录

			// 创建订单 更新兑换卡信息
			String orderCode = FCDao.getOrderCode();
			Date cExcTime = new Date();// 下单日期
			FCDao.createCardExcOrder(getSession(), cardNum, orderCode, cExcTime, type, addressid, reach, jh_list,
					jhcolor_list, cMoney, zhufu, songhua, cycle, type);

			Db.update("update f_card set cExcOrderId=? where cId=?", orderCode, cardNum);// 订单号
			Db.update("update f_card set cIsSuccess=1 where cId=?", cardNum);// 是否使用
			Db.update("update f_card set cExcTime=? where cId=?", cExcTime, cardNum);// 使用时间
			Db.update("update f_card set cExcMan=? where cId=?", account.getInt("id"), cardNum);// 使用者id

			renderJson(true);

		}
	}

	// 卡片兑换花束详情页面
	public void exchangeInfo() {
		int cardNum = getParaToInt("cardNum");
		setAttr("cardNum", cardNum);
		int cNo = Db.queryInt("SELECT cFkNo FROM f_card WHERE cId=?", cardNum);
		Record cardInfo = Db.findFirst("SELECT cPid,cCycle FROM f_card_type WHERE cNo=?", cNo);
		int times = cardInfo.getInt("cCycle");
		setAttr("times", times);
		int type = cardInfo.getInt("cPid");// 类型
		Record product = Db.findById("f_flower_pro", type);
		String imgurl = product.getStr("imgurl");

		String[] img = imgurl.split("-");

		product.set("imgurl", img[0]);

		setAttr("product", product); // 主商品

		setAttr("pid", type);

		Record account = getSessionAttr("account"); // 用户信息

		double price = FCDao.countPrice(product.getDouble("price"), times, null, null); // 商品总额
		setAttr("price", price);

		List<Record> address = FCDao.getAddress(account.getInt("id"));
		setAttr("address", address.get(address.size() - 1)); // 获取收货地址列表中最后一个

		setAttr("jihuis", FCDao.getjihuiflo());
		setAttr("jhclos", FCDao.getjihuicolor());
		render("product_exchange.html");
	}

	public void setlog(String remark) {
		Record record = new Record();
		record.set("remark", remark);
		Db.save("f_cslog", record);
	}

	// 验证兑换卡
	public void verifyExchange() {

		String cardNum = getPara("cardNum");
		String code = getPara("code");

		boolean R = false;
		String message = "hello world!";

		if (code.equals(getExchangeCode(cardNum))) {
			Record card = Db.findFirst("SELECT cIsSuccess,cEffDate FROM f_card WHERE cId = ?", cardNum);
			Date effTime = card.getDate("cEffDate");
			Date now = new Date();
			int state = card.getInt("cIsSuccess");
			if (state == 0) {
				if (effTime.after(now)) {
					R = true;
					message = "兑换卡可用";
				} else {
					R = false;
					message = "兑换卡已过期";
				}

			} else {
				R = false;
				message = "兑换卡已使用";
			}

		} else {
			R = false;
			message = "卡号或兑换码不正确";
		}

		// 验证通过保存收货地址
		if (R) {
			Integer id = getParaToInt("id");
			Integer state = 0;// 非默认地址
			String name = getPara("name");
			String tel = getPara("phone");
			String area = getPara("area");
			String addr = getPara("addr");
			int give = 0;
			Record account = getSessionAttr("account");
			// 验证通过保存地址
			R = FCDao.saveAddress(id, state, account.getInt("id"), name, tel, area, addr, give);
		}
		// 返回验证结果
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("result", R);
		responseMap.put("message", message);
		renderJson(responseMap);

	}

	public String getExchangeCode(String original) {

		String originalMd5 = MD5.MD5Encode(original);
		String selectMd5 = originalMd5.substring(3, 15);

		char resultArr[] = { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };// 初始化
		String dict1 = "1234567890qwertyuiopasdfghjklzxcvbnm";
		String dict2 = "098765432109876543213456678899987665";
		for (int i = 0; i < selectMd5.length(); i++) {
			char c = selectMd5.charAt(i);
			int index = dict1.indexOf(c);
			char temp = dict2.charAt(index);
			resultArr[i] = temp;
		}

		String result = String.valueOf(resultArr);
		return result;
	}
	/*---------------------------------------------------------花籽---------------------------------------------*/

	// 花籽兑换花束
	public void exchangeflower() {
		int pid = getParaToInt("pid"); // 产品ID
		Record product = Db.findById("f_flower_pro", pid);
		setAttr("product", product); // 主商品
		setAttr("pid", pid);
		Record account = getSessionAttr("account"); // 用户信息
		double price = FCDao.countPrice(product.getDouble("price"), 1, null, null); // 商品总额
		setAttr("price", price);
		// setAttr("seeds",product.getInt("seeds"));
		setAttr("address", FCDao.getDefaultAddress(account.getInt("id"), 1, getParaToInt("addr"))); // 收货地址
		setAttr("jihuis", FCDao.getjihuiType());
		setAttr("jhclos", FCDao.getjihuicolor());
		render("exchange.html");
	}

	public void exchangeflower_new() {
		Record account = getSessionAttr("account"); // 用户信息
		int yh_seeds = Db.queryInt(
				"select b.seeds from f_account a left join f_update_grade b on a.grade=b.grade where a.id=?",
				account.getInt("id"));// 会员优惠的花籽
		int fpid = getParaToInt("pid"); // 商品ID
		Record product = Db.findById("f_flower_pro", fpid);
		setAttr("product", product); // 主商品
		setAttr("fpid", fpid);
		setAttr("ptid", product.getInt("ptid"));// 商品分类ID
		double price = FCDao.countPrice(product.getDouble("price"), 1, null, null); // 商品总额
		setAttr("price", price);
		setAttr("seeds", product.getInt("seeds") - yh_seeds);// 所需花籽
		setAttr("name", product.getStr("name"));// 名称
		setAttr("describe", product.getStr("describe"));// 描述
		setAttr("address", FCDao.getDefaultAddress(account.getInt("id"), 1, getParaToInt("addr"))); // 收货地址
		setAttr("jihuis", FCDao.getjihuiType());
		setAttr("jhclos", FCDao.getjihuicolor());
		render("exchange_seeds.html");
	}

	// 花籽 兑换 验证
	public void exchangevalid() {
		boolean result = false;
		int pid = getParaToInt();
		Record account = getSessionAttr("account");
		int count = (int) FCDao.getFlowerSeed(account.getInt("id")).get("count");
		if (count >= 40) {
			if (pid == 1) {
				result = true;
			} else if (count >= 60 && pid == 2) {
				result = true;
			} else {
				result = false;
			}
		} else {
			result = false;
		}
		renderJson(result);
	}

	/**
	 * 花籽兑换验证花籽数量是否符合要求
	 * 
	 */
	public void exchangevalid_new() {
		boolean result = false;
		int pid = getParaToInt();
		Record account = getSessionAttr("account");
		int count = (int) FCDao.getFlowerSeed(account.getInt("id")).get("count");// 会员拥有的花籽数
		int seedsCount = FCDao.getSeedCount(pid);// 商品的ID号，商品所需的花籽数
		int yh_seeds = Db.queryInt(
				"select b.seeds from f_account a left join f_update_grade b on a.grade=b.grade where a.id=?",
				account.getInt("id"));// 会员优惠的花籽
		if (count >= seedsCount - yh_seeds) {
			result = true;
		} else {
			result = false;
		}
		renderJson(result);
	}

	public void exchangevalid_new_2() {
		boolean result = false;
		String ordercode = getPara();
		Record account = getSessionAttr("account");
		int count = (int) FCDao.getFlowerSeed(account.getInt("id")).get("count");
		int seedsCount = FCDao.getSeedCount(ordercode);// 商品的ID号
		int yh_seeds = Db.queryInt(
				"select b.seeds from f_account a left join f_update_grade b on a.grade=b.grade where a.id=?",
				account.getInt("id"));// 会员优惠的花籽
		if (count >= seedsCount - yh_seeds) {
			result = true;
		} else {
			result = false;
		}
		renderJson(result);
	}

	/**
	 * 花籽 兑换花束下单
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void createorderforexchange_new() throws ParserConfigurationException, IOException, SAXException {

		int type = getParaToInt("type"); // 商品类型(1订阅,2送花,3周边,4兑换)
		int fpid = getParaToInt("fpid"); // 商品ID
		int addressid = getParaToInt("address"); // 收货地址ID
		int reach = getParaToInt("reach"); // 送达日期
		String jh_list = getPara("jh_list"); // 忌讳的花
		String jhcolor_list = getPara("jhcolor_list"); // 忌讳色系
		Map<String, Object> xmlMap = new HashMap<>();
		xmlMap = FCDao.exccreateOrder_new(getRequest(), getSession(), fpid, addressid, reach, jh_list, jhcolor_list, 1,
				type);
		if ((boolean) xmlMap.get("result")) {
			setAttr("ordercode", xmlMap.get("ordercode"));
			setAttr("payMap", Sign.sign2(xmlMap.get("prepay_id").toString()));
			//setAttr("detaillist", xmlMap.get("detaillist"));
			setAttr("detaillist", null);
			setAttr("ptNo", xmlMap.get("ptNo"));
			render("topay.html");
		} else {
			render("error.html");
		}
	}

	/*----------------------------------------------------红包-------------------------------------------------*/
	/**
	 * 发红包 总裁专用
	 */
	public void sendRedPackets() {

		/* List<Record> redPacketsList = FCDao.getRedPacketsList(); */

		List<Record> redPacketsList = FCDao.getRedPacketsList();

		setAttr("redPacketsList", redPacketsList);

		// System.out.println(redPacketsList);

		/*
		 * List<Record> redPacketsListF = FCDao.getRedPacketsListF();
		 * setAttr("redPacketsListF", redPacketsListF);
		 */

		Record account = getSessionAttr("account");
		setAttr("nick", account.getStr("nick"));

		render("/front/account/redPacket_send.html");
	}

	// 创建红包
	public void createRedpacket() throws ParserConfigurationException, IOException, SAXException {
		int type = getParaToInt("type");
		String pids = getPara("pid");
		// 红包留言留言
		String message = getPara("message");
		// 加购 花瓶
		String is_vase = getPara("is_vase");
		if (is_vase == null) {
			is_vase = "0";
		}

		// 是否需要打印发票
		String invoice = getPara("invoice");

		Map<String, Object> xmlMap = new HashMap<>();
		// 创建红包
		xmlMap = FCDao.createRedpacket(getRequest(), getSession(), type, pids, message, is_vase, invoice);

		if ((boolean) xmlMap.get("result")) {
			Map<String, String> rMap = Sign.sign2(xmlMap.get("prepay_id").toString());
			rMap.put("redpacketcode", (String) xmlMap.get("redpacketcode"));
			renderJson(rMap);
		} else {
			render("error.html");
		}
	}

	/**
	 * 第一步：创建红包 红包创建 和 支付 分两步走 2017-11-28确定
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void createRedpacket_new() throws ParserConfigurationException, IOException, SAXException {
		int type = getParaToInt("type");// 红包类型1：普通红包，送一人2：拼手气红包，送多人
		String pids = getPara("pid");// 红包中的礼物
		String message = getPara("message");// 红包留言留言
		String invoice = getPara("invoice"); // 是否需要打印发票
		// 创建红包
		Map<String, Object> map = FCDao.createRedpacket_new(getRequest(), getSession(), type, pids, message, invoice);
		setAttr("result", map.get("result"));// 创建成功还是失败
		setAttr("rpCode", map.get("rpCode"));// 红包编号，用于支付
		// 如果创建成功，发客服消息
		if ((boolean) map.get("result") == true) {
			WeixinApiCtrl.setApiConfig();
			// [送多个人]：5个红包已经生成成功，查看详情
			String msg = String.format("花美美为您成功创建了%s个红包<a href='%s'>查看详情</a>",
					map.get("rpNum"), Constant.getHost + "/service/getRedpacketInfo/" + map.get("rpCode"));
			CustomServiceApi.sendText(map.get("openid").toString(), msg);
		}
		renderJson(map);
	}

	/**
	 * 查看生成的红包详情
	 */
	public void getRedpacketInfo() {
		int rpCode = getParaToInt(0);
		List<Record> goodsList = Db.find("select imgurl03,`name`,count(1) 'countTotal',SUM(pmoney) 'moneyTotal'"
				+ " from f_redpackets_detial as a "
				+ " left join f_redpackets_pro as b on a.rppid=b.id"
				+ " where rpid=? group by rppid", rpCode);
		Record info = Db.findFirst("select quantity1,msg,invoice,type,state,money from f_redpackets where id=?",
				rpCode);
		long goodsNum = Db.queryLong("select count(1) from f_redpackets_detial where rpid=?", rpCode);

		//处理图片
		for (Record good : goodsList) {
			String imgs = good.getStr("imgurl03");
			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				good.set("imgurl1", ims[0]);
				good.set("imgurl2", ims[1]);
				good.set("imgurl3", ims[2]);
			}else{
				good.set("imgurl1", imgs);
				good.set("imgurl2", imgs);
				good.set("imgurl3", imgs);
			}
		}

		setAttr("rpCode", rpCode);//支付编号
		setAttr("type", (info.getInt("type")==1)?"送一个人":"送多个人");
		setAttr("goodsNum", goodsNum);// 礼物个数
		setAttr("rpNum", info.getInt("quantity1"));// 红包个数
		setAttr("msg", info.get("msg"));// 留言
		setAttr("money", info.getBigDecimal("money").doubleValue());// 红包总金额
		setAttr("invoice", info.getInt("invoice") == 1 ? "是" : "否");// 是否开发票
		setAttr("goodsList", goodsList);// 礼物详情
		setAttr("state", info.getInt("state"));// 红包状态：0未支付，1、2已支付，3、4交易关闭
		render("getRedpacketInfo.html");
	}

	/**
	 * 这是第二步 红包支付
	 */
	public void payforRp() throws ParserConfigurationException, IOException, SAXException {
		String rpCode = getPara("rpCode");// 红包编号
		Record account = getSessionAttr("account");
		Map<String, Object> xmlMap = FCDao.payForRp(getRequest(), account.getStr("openid"), account.getInt("id"),
				rpCode);

		Map<String, String> rMap = Sign.sign2(xmlMap.get("prepay_id").toString());
		renderJson(rMap);

	}

	// 分享 红包
	public void shareRedpacket() throws Exception {
		String redpacketcode = getPara(0); // 红包 编号
		// String redpacketcode = "10000208";// 测试

		Db.update("update f_redpackets set state = 1 where id = ?", redpacketcode);

		Record account = getSessionAttr("account");

		setAttr("headimg", base64Encode(account.getStr("headimg")));
		setAttr("nick", account.get("nick"));

		Record redpacket = Db.findFirst("SELECT type, msg FROM f_redpackets WHERE id = ?", redpacketcode);
		setAttr("msg", redpacket.getStr("msg"));
		if (redpacket.getInt("type") == 1) {
			setAttr("type", "送一个人");
		} else {
			setAttr("type", "送多个人");
		}
		/*
		 * String rpUrl = PropKit.get("host") + "/service/getRedpacket/" +
		 * redpacketcode;
		 */
		String rpUrl = groupUrl(account.getInt("id"), redpacketcode);
		setAttr("qrurl", rpUrl);

		render("/front/account/redPacket_send2.html");
	}

	/**
	 * 代颜二维码
	 * 
	 * @param aid
	 * @param redpacketcode
	 * @return
	 */
	public String groupUrl(int aid, String redpacketcode) {
		WeixinApiCtrl.setApiConfig();
		ApiResult ar = QrcodeApi.createPermanent("4" + "_" + aid + "_" + redpacketcode);
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(ar.getJson(), HashMap.class);
		String url = (String) map.get("url");
		return url;
	}

	// 领取红包
	public void getRedpacket() {
		String redpacketcode = getPara(0); // 红包 编号
		Record redpacket = Db.findFirst("SELECT aid,quantity1, quantity2, msg,state FROM f_redpackets WHERE id = ?",
				redpacketcode);
		// num剩余礼物数量
		setAttr("num", redpacket.getInt("quantity1") - redpacket.getInt("quantity2"));
		setAttr("redpacketcode", redpacketcode);
		setAttr("msg", redpacket.getStr("msg"));
		setAttr("state", redpacket.getInt("state"));

		// 发包人 - 头像、 昵称
		Record account = Db.findFirst("SELECT headimg, nick FROM f_account WHERE id = ?", redpacket.getInt("aid"));
		setAttr("headimg", account.getStr("headimg"));
		setAttr("nick", account.getStr("nick"));

		render("/front/account/redPacket_send3.html");
	}

	// 抢红包
	public void grabRedEnvelope() {

		String redpacketcode = getPara(0); // 红包 编号 或 id
		Record redpacket = Db.findFirst(
				"SELECT aid,quantity1, quantity2, msg,type,state FROM f_redpackets WHERE id = ?", redpacketcode);
		setAttr("type", redpacket.getInt("type"));
		// 当前用户
		Record account = getSessionAttr("account");

		// 发包人 - 头像、 昵称
		Record account2 = Db.findFirst("SELECT openid, headimg, nick FROM f_account WHERE id = ?",
				redpacket.getInt("aid"));
		setAttr("headimg", account2.getStr("headimg"));
		setAttr("nick", account2.getStr("nick"));

		int num = redpacket.getInt("quantity1") - redpacket.getInt("quantity2"); // 红包剩余
																					// 数量
		setAttr("num", num);

		setAttr("quantity1", redpacket.getInt("quantity1")); // 礼物数量
		setAttr("quantity2", redpacket.getInt("quantity2")); // 已 领取 数量

		// 判断用户有没有领取过这个红包
		Number isGrab = Db.queryNumber("select count(1) from f_redpackets_detial where rpid = ? AND aid = ?",
				redpacketcode, account.getInt("id"));

		Number state = redpacket.getInt("state");

		// 红包数量大于0 （并且红包是"已支付，未领完状态" 这边是考虑有"已退款" -> 再次领取的情况） 且 没有领取过红包
		if (num > 0 && isGrab.intValue() == 0 && state.intValue() == 1) {
			// 送一个人
			if (redpacket.getInt("type") == 1) {
				Db.update("update f_redpackets_detial set isopen = 1,aid=?,gtime=? where rpid = ?",
						account.getInt("id"), new Date(), redpacketcode);
				Db.update("update f_redpackets set quantity2 =quantity1,state=2  where id = ?", redpacketcode);

				setAttr("quantity1", redpacket.getInt("quantity1")); // 礼物数量
				setAttr("quantity2", redpacket.getInt("quantity1")); // 已 领取 数量
				setAttr("isGrab", 3);// 不显示商品详情
				setAttr("num", 0);// 剩余红包为0

				// 客服消息接口 调用weixinApi发送客服消息
				WeixinApiCtrl.setApiConfig();
				String msg = account.getStr("nick") + " 领取了你的红包。\n<a href='" + Constant.getHost
						+ "/account/myRedPackets'>点击查看</a>";
				CustomServiceApi.sendText(account2.getStr("openid"), msg);

				msg = "你有一个红包到账，可在会员中心查看使用。\n<a href='" + Constant.getHost + "/account/myRedPackets'>点击查看</a>";
				CustomServiceApi.sendText(account.getStr("openid"), msg);

			}
			// 送多个人，有个【抢】的动作
			else {
				// 获取到红包单身---->>> 一个单头对应多个单身
				List<Record> RPdetialList = Db.find("SELECT * FROM f_redpackets_detial WHERE rpid = ?", redpacketcode);
				for (int i = 0; i < RPdetialList.size(); i++) {
					// 遍历红包
					if (RPdetialList.get(i).getInt("isopen") == 0) {
						// 如果红包没有被拆就进行拆红包操作,写入用户ID，领取时间，
						Db.update("update f_redpackets_detial set isopen = 1,aid=?,gtime=? where id = ?",
								account.getInt("id"), new Date(), RPdetialList.get(i).getInt("id"));
						// 已拆数量 +1
						Db.update("update f_redpackets set quantity2 =quantity2+1  where id = ?", redpacketcode);

						// 判断是否已抢完 0：未支付 1：已支付，未抢完 2：已抢完（终结状态）3：已退款（终结状态）
						if (--num == 0) {
							Db.update("update f_redpackets set state = 2 where id = ?", redpacketcode);
						}
						redpacket = Db.findFirst(
								"SELECT aid,quantity1, quantity2, msg,type,state FROM f_redpackets WHERE id = ?",
								redpacketcode);
						setAttr("quantity1", redpacket.getInt("quantity1")); // 礼物数量
						setAttr("quantity2", redpacket.getInt("quantity2")); // 已
																				// 领取
																				// 数量
						// 客服消息接口 调用weixinApi发送客服消息
						WeixinApiCtrl.setApiConfig();
						String msg = account.getStr("nick") + " 领取了你的红包。\n<a href='" + Constant.getHost
								+ "/account/myRedPackets'>点击查看</a>";
						CustomServiceApi.sendText(account2.getStr("openid"), msg);

						msg = "你有一个红包到账，可在会员中心查看使用。\n<a href='" + Constant.getHost + "/account/myRedPackets'>点击查看</a>";
						CustomServiceApi.sendText(account.getStr("openid"), msg);
						break;
					}
				}
			}

			// 个人获得
			List<Record> giftList = Db.find(
					"SELECT a.id, b.headimg, b.nick, a.gtime, c.`name`, c.pmoney, c.fpid "
							+ "FROM f_redpackets_detial a LEFT JOIN f_account b ON a.aid = b.id "
							+ "LEFT JOIN f_redpackets_pro c ON a.rppid = c.id " + "WHERE a.rpid = ? AND a.aid = ?",
					redpacketcode, account.getInt("id"));

			// 红包领取 列表
			List<Record> allgiftList = Db.find("SELECT b.headimg, b.nick, a.gtime, c.`name`, c.pmoney "
					+ "FROM f_redpackets_detial a LEFT JOIN f_account b ON a.aid = b.id "
					+ "LEFT JOIN f_redpackets_pro c ON a.rppid = c.id " + "WHERE a.rpid = ? order by c.pmoney desc",
					redpacketcode);

			String giftImg = Db.queryStr("SELECT imgurl FROM f_flower_pro WHERE id = ?", giftList.get(0).get("fpid"));
			if (giftImg.indexOf("-") != -1) {
				String[] ims = giftImg.split("-");
				giftImg = ims[0];
			}
			setAttr("imgurl", giftImg);

			setAttr("redpacketcode", giftList.get(0).get("id"));

			// System.out.println("redpacketcode : "+
			// giftList.get(0).get("id"));

			setAttr("isGrab", 1);
			setAttr("flat", 0);
			setAttr("giftList", giftList);
			setAttr("allgiftList", allgiftList);

		} else if (num == 0) {

			// 红包领取 列表
			List<Record> allgiftList = Db.find("SELECT b.headimg, b.nick, a.gtime, c.`name`, c.pmoney "
					+ "FROM f_redpackets_detial a LEFT JOIN f_account b ON a.aid = b.id "
					+ "LEFT JOIN f_redpackets_pro c ON a.rppid = c.id " + "WHERE a.rpid = ? order by c.pmoney desc",
					redpacketcode);
			setAttr("allgiftList", allgiftList);
			setAttr("isGrab", 0);
			setAttr("flat", 0);
		} else {

			// 显示当前用户领取过的这个红包详情
			Record getInfo = Db.findFirst(
					"select id,rpid,rppid,isopen,aid from f_redpackets_detial where rpid = ? and aid = ?",
					redpacketcode, account.getInt("id"));
			// 根据商品id 获取图片
			String giftImg = Db.queryStr("SELECT imgurl FROM f_flower_pro WHERE id = ?", getInfo.get("rppid"));
			if (giftImg.indexOf("-") != -1) {
				String[] ims = giftImg.split("-");
				giftImg = ims[0];
			}
			setAttr("imgurl", giftImg);

			// 个人获得列表
			List<Record> giftList = Db.find(
					"SELECT a.id, b.headimg, b.nick, a.gtime, c.`name`, c.pmoney, c.fpid "
							+ "FROM f_redpackets_detial a LEFT JOIN f_account b ON a.aid = b.id "
							+ "LEFT JOIN f_redpackets_pro c ON a.rppid = c.id " + "WHERE a.rpid = ? AND a.aid = ?",
					redpacketcode, account.getInt("id"));

			// 红包领取 列表
			List<Record> allgiftList = Db.find("SELECT b.headimg, b.nick, a.gtime, c.`name`, c.pmoney "
					+ "FROM f_redpackets_detial a LEFT JOIN f_account b ON a.aid = b.id "
					+ "LEFT JOIN f_redpackets_pro c ON a.rppid = c.id " + "WHERE a.rpid = ? order by c.pmoney desc",
					redpacketcode);

			setAttr("giftList", giftList);
			setAttr("allgiftList", allgiftList);

			setAttr("redpacketcode", getInfo.get("id"));

			setAttr("isGrab", 2);
		}

		setAttr("flat", 0);
		render("/front/account/redPacket_result.html");
	}

	/**
	 * 查看红包领取详情
	 * 
	 * @author Glacier
	 * @date 2017年7月3日
	 */
	public void seachRedpacketDetails() {
		// 红包编号
		String redpacketcode = getPara(0);
		Record account = getSessionAttr("account");
		String nick = account.getStr("nick");
		String headimg = account.getStr("headimg");
		// 红包列表
		List<Record> allgiftList = Db.find("SELECT b.headimg, b.nick, a.gtime, c.`name`, c.pmoney,a.isopen "
				+ "FROM f_redpackets_detial a LEFT JOIN f_account b ON a.aid = b.id "
				+ "LEFT JOIN f_redpackets_pro c ON a.rppid = c.id "
				+ "WHERE a.rpid = ? order by c.pmoney desc,gtime DESC", redpacketcode);

		Record redpacketinfo = Db.findFirst("SELECT aid,quantity1, quantity2, msg,type FROM f_redpackets WHERE id = ?",
				redpacketcode);
		setAttr("quantity1", redpacketinfo.getInt("quantity1"));
		setAttr("quantity2", redpacketinfo.getInt("quantity2"));
		setAttr("type", redpacketinfo.getInt("type"));
		setAttr("headimg", headimg);
		setAttr("nick", nick);
		setAttr("allgiftList", allgiftList);
		setAttr("isGrab", 3);// isGrab 标记 若为3 只显示领取详情

		// 判断是否需要继续派送
		int flat = redpacketinfo.getInt("quantity1") - redpacketinfo.getInt("quantity2"); // 总数
																							// -
																							// 已拆数量

		setAttr("flat", flat);
		setAttr("redpacketcode", redpacketcode);
		render("/front/account/redPacket_result.html");
	}

	// 红包 兑换 传的是 子单
	public void redPacketExchange() {
		int flat = 0;
		Record account = getSessionAttr("account"); // 用户信息
		// 红包兑换的时候进行判断 判断商品的类型
		int redpacket = getParaToInt(0);
		// Db.queryInt("SELECT b.type FROM f_redpackets_detial as a LEFT JOIN
		// f_redpackets as b ON a.rpid = b.id WHERE a.id = ?",redpacket);

		/*
		 * int ptid = Db.queryInt("SELECT c.ptid from f_redpackets_detial as a "
		 * + " LEFT JOIN f_redpackets_pro as b ON a.rppid = b.id " +
		 * " LEFT JOIN f_flower_pro as c ON b.fpid = c.id " + " WHERE a.id = ?"
		 * ,redpacket);
		 */

		setAttr("excId", getParaToInt(0));

		Record gInfo = Db.findFirst("SELECT b.pnum, b.state,b.fpid, c.ptid " + "FROM f_redpackets_detial a "
				+ "LEFT JOIN f_redpackets_pro b ON a.rppid = b.id " + "LEFT JOIN f_flower_pro c ON b.fpid = c.id "
				+ "WHERE a.id = ? and a.aid=?", getParaToInt(0),account.getInt("id"));

		int pid = gInfo.getInt("fpid"); // 产品ID
		int pnum = gInfo.getInt("pnum"); // 订阅次数

		int type = 1; // 商品类型(1订阅,2送花,5节日)
		if (gInfo.getInt("ptid") == 3) {
			type = 2;
		}
		if (gInfo.getInt("ptid") >= 4 && gInfo.getInt("ptid") <= 10) {
			type = 3;// 周边
			// 跳转周边 订单类型 周边是3
			flat = 1;
		}
		if (gInfo.getInt("ptid") >= 101 && gInfo.getInt("ptid") < 200) {
			type = 5;
		}
		String reachtype = Db.queryStr(
				"SELECT reachtype from f_redpackets_detial as a LEFT JOIN f_redpackets_pro as b ON a.rppid = b.id LEFT JOIN f_flower_pro as c ON b.fpid = c.id WHERE a.id = ?",
				redpacket);

		// 日期处理
		String reach[] = reachtype.split(":");
		String time[] = null;
		int festivalId = getParaToInt("festivalId") == null ? 0 : getParaToInt("festivalId");
		switch (reach[0]) {
		case "1":
			// 订阅 ,送花,拼团 // 定制 鲜花
			// 周一 周六
			setAttr("Timetype", 1);

			setAttr("festivalId", festivalId);
			break;
		case "2":
			// 周边
			// t+1
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			Date date = calendar.getTime();
			System.out.println(sdf.format(date));// 若输入为：sdf.format(date)，则输入的为固定格式的字符串

			setAttr("festivalId", festivalId);
			setAttr("Timetype", 2);
			setAttr("hDate", sdf.format(date));
			break;
		case "3":
			// 节日鲜花,闪购、主题花
			// 指定日期

			setAttr("festivalId", festivalId);

			time = reach[1].split("_");
			setAttr("Timetype", 3);
			setAttr("hDatelist", time);

			break;
		}

		Record product = Db.findById("f_flower_pro", pid);

		String imgurl = product.getStr("imgurl");
		String[] img = imgurl.split("-");
		product.set("imgurl", img[0]);

		setAttr("product", product); // 主商品
		setAttr("type", type); // 商品类型(1订阅,2送花,5节日)
		setAttr("cycle", pnum); // 订阅次数

		

		double price = FCDao.countPrice(product.getDouble("price"), pnum, null, null); // 商品总价值
		double totalprice = price; // 实际付出金额
		setAttr("price", price); // 商品总价值
		setAttr("totalprice", totalprice); // 实际付出金额

		Record address = FCDao.getDefaultAddress(account.getInt("id"), 1, getParaToInt(1));
		setAttr("address", address); // 收货地址

		setAttr("jhclos", FCDao.getjihuicolor());
		setAttr("jihuis", FCDao.getjihuiType());

		setAttr("flat", flat);

		// 是否售罄
		setAttr("flag", gInfo.getInt("state"));

		render("exchange_redpacket.html");

	}

	// 红包兑换 下单
	public void createorderforRedPacket() {
		int msg = 0;

		if (getParaToInt("excId") != null) {
			int excId = getParaToInt("excId"); // 订单详情表 id
			long count = Db.queryLong("select count(1) from f_redpackets_detial where id=? and oid is null", excId);
			if (count == 1) {
				int type = getParaToInt("type"); // 商品类型(1订阅,2送花)
				int pid = getParaToInt("pid"); // 商品ID
				int addressid = getParaToInt("address"); // 收货地址ID
				int reach = getParaToInt("reach"); // 送达日期
				String jr_picode = getPara("jr_picode");// 节日订单的批号
				String jh_list = getPara("jh_list"); // 忌讳的花
				String jhcolor_list = getPara("jhcolor_list"); // 忌讳色系
				int cycle = getParaToInt("cycle");
				FCDao.exccreateOrder(getSession(), pid, addressid, reach, jh_list, jhcolor_list, cycle, type, excId,
						jr_picode);
				msg = 1;
			}

		}

		setAttr("msg", msg);
		// 返回一个空
		renderJson();
	}

	public static String base64Encode(String imgurl) throws IOException {
		URL url = new URL(imgurl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(5 * 1000);
		InputStream is = con.getInputStream();

		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = is.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] data = swapStream.toByteArray();
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data);
	}

	public void company_custom() {
		// 跳转参数
		String queryString = getRequest().getQueryString();
		setAttr("queryString", queryString);
		setAttr("areajson", FCDao.getArea());
		render("company_custom.html");
	}

	/** 将企业定制的数据填入数据库 f_company_custom **/
	public void saveCustom() {
		boolean result = false;
		String ctype = getPara("ctype");
		String name = getPara("name");
		String tel = getPara("tel");
		String area = getPara("area");
		String company = getPara("company");
		String describe = getPara("describe");
		Record record = new Record();
		record.set("ctype", ctype);
		record.set("name", name);
		record.set("tel", tel);
		record.set("area", area);
		record.set("company", company);
		record.set("describe", describe);
		record.set("ctime", new Date());
		result = Db.save("f_company_custom", record);

		try {
			SendMsgUtil.sendMsg("13773103981", "有新的企业订制需求，请及时跟踪处理");
			SendMsgUtil.sendMsg("18012698246", "有新的企业订制需求，请及时跟踪处理");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderJson("result", result);
	}

	// 获取用户IP
	public static String getRemortIP(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-for") == null) {
			return request.getRemoteAddr();
		}
		String ipStr = request.getHeader("x-forwarded-for");
		String[] ipArr = ipStr.split(",");
		String ip = new String();
		for (String i : ipArr) {
			if (!"unknown".equals(ip)) {
				ip = i;
				break;
			}
		}
		return ip;
	}

	/**
	 * 开票金额低于300元 邮费支付
	 * 
	 * @throws Exception
	 */
	public void post_pay() throws Exception {
		Record user = getSessionAttr("account");
		String company = getPara("company");
		String code = getPara("code");
		String content = getPara("content");
		double money = Double.parseDouble(getPara("money"));
		String name = getPara("name");
		String tel = getPara("tel");
		String addr = getPara("addr");
		String area = getPara("area");
		String orderList = getPara("orderList");
		String ordernum = getPara("ordernum");
		double postage = Double.parseDouble(getPara("postage"));

		// 支付码
		String fcode = FCDao.getFCode();
		// 创建发票信息 -> 往数据库写一条信息
		Record f_receipt = new Record();
		f_receipt.set("company", company);
		f_receipt.set("code", code);
		f_receipt.set("content", content);
		f_receipt.set("money", money);
		f_receipt.set("name", name);
		f_receipt.set("tel", tel);
		f_receipt.set("area", area);
		f_receipt.set("addr", addr);
		f_receipt.set("postage", postage);
		f_receipt.set("ctime", new Date());
		f_receipt.set("aid", user.getInt("id"));

		f_receipt.set("fcode", fcode);
		f_receipt.set("ordernum", ordernum);
		Db.save("f_receipt", f_receipt);

		// 单身信息
		String[] oList = orderList.split(",");
		for (String ol : oList) {
			Record f_receipt_detail = new Record();
			f_receipt_detail.set("fcode", fcode);
			f_receipt_detail.set("ordercode", ol);
			Db.save("f_receipt_detail", f_receipt_detail);
		}

		Record account = getSessionAttr("account");
		// 微信支付
		String xml = FCDao.wxPushOrder("发票", fcode, postage, getRemortIP(getRequest()), account.getStr("openid"));

		Map<String, Object> resultMap = new HashMap<>();
		resultMap = XMLParser.getMapFromXML(xml);

		if (resultMap != null) {
			setAttr("fcode", fcode);
			setAttr("ptNo", "post0");
			setAttr("payMap", Sign.sign2(resultMap.get("prepay_id").toString()));
			System.err.println(Sign.sign2(resultMap.get("prepay_id").toString()));
			setAttr("detaillist", null);
			render("topay.html");
		} else {
			render("error.html");
		}
	}

	/**
	 * //待支付状态下的邮费
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void post_repay() throws ParserConfigurationException, IOException, SAXException {
		String fcode = getPara(0);
		Record account = getSessionAttr("account");
		double postage = 10.00;
		String xml = FCDao.wxPushOrder("发票", fcode, postage, getRemortIP(getRequest()), account.getStr("openid"));

		Map<String, Object> resultMap = new HashMap<>();
		resultMap = XMLParser.getMapFromXML(xml);

		setAttr("fcode", fcode);
		setAttr("ptNo", "post0");
		setAttr("payMap", Sign.sign2(resultMap.get("prepay_id").toString()));
		setAttr("detaillist", null);
		render("topay.html");
	}

	/**
	 * 获取奖池
	 */
	public void getPrizeList() {
		List<Record> list = Db
				.find("select id,pImgurl from f_prizeList where state=1 and id > 0 and type=1 order by id ASC");
		// List<Record> list=Db.find("select * from f_prizeList where state=1
		// and id > 0 order by id ASC");
		String lucArr[] = new String[12];
		for (int i = 0; i < list.size(); i++) {
			lucArr[i] = list.get(i).get("pImgurl");
		}

		setAttr("lucky", lucArr);

		// 获奖名单
		List<Record> lucklist = Db.find("SELECT b.nick,a.pname,a.state,c.origin" + " from f_luckdraw_result a "
				+ " LEFT JOIN f_prizeList c  on a.prizeid = c.id  " + " LEFT JOIN f_account b ON a.aid = b.id "
				+ " WHERE c.origin <> 5 and a.type =1" + " ORDER BY a.luckTime DESC LIMIT 20 ");
		setAttr("lucklist", lucklist);

		// 获取花籽数量
		Long seedMy = Db.queryLong("select count(1) from f_flowerseed where aid=? and state=0",
				((Record) getSessionAttr("account")).getInt("id"));
		setAttr("seeds", seedMy);

		// 获取抽奖所需的花籽数
		int seedNum = Integer
				.valueOf(Db.queryStr("select code_value from f_dictionary where code_key='fsn' and state=1"));
		// System.out.println(seedNum);
		setAttr("seedNum", seedNum);

		render("luckyDraw.html");
	}

	/**
	 * 获取我的奖品
	 * 
	 * @author Glacier
	 * @date 2017年10月9日
	 */
	public void getMyPrizeList() {

		List<Record> mylucklist = Db.find(
				"SELECT a.aid,b.nick,a.pname,a.state,a.luckTime,c.origin from f_luckdraw_result a "
						+ " LEFT JOIN f_prizeList c  on a.prizeid = c.id " + " LEFT JOIN f_account b ON a.aid = b.id "
						+ " WHERE c.origin <> 5 and a.aid = ? and a.type=1 " + " ORDER BY a.luckTime DESC LIMIT 20 ",
				((Record) getSessionAttr("account")).getInt("id"));
		setAttr("user", getSessionAttr("account"));
		setAttr("lucklist", mylucklist);
		render("luckyDraw_my.html");
	}

	/**
	 * 双11，我抽到的启动金
	 */
	public void getMyPrizeList11() {
		List<Record> mylucklist = Db.find(
				"SELECT d.img02,a.id,a.moneyUpdate, a.aid,b.nick,a.pname,a.state,date_format(a.luckTime,'%Y年%m月%d日') luckTime from f_luckdraw_result a "
						+ " LEFT JOIN f_account b ON a.aid = b.id left join f_turn_img d on date_format(a.luckTime,'%Y-%m-%d')=d.activeDate and d.activeTitle = '双11活动'"
						+ " WHERE  a.aid = ? and a.type=2 " + " ORDER BY a.luckTime DESC LIMIT 20 ",
				((Record) getSessionAttr("account")).getInt("id"));
		setAttr("user", getSessionAttr("account"));
		setAttr("lucklist", mylucklist);
		render("luckyDraw_my11.html");
	}

	/**
	 * 每一笔启动金的翻牌明细
	 */
	public void getTurnDetail11() {
		int lrId = getParaToInt(0);
		List<Record> list = Db.find("select d.nick ,a.msg,a.money,a.ctime from f_turn_result as a  "
				+ " left join f_account as d on a.aid=d.id where 1=1 and lrId=?", lrId);
		setAttr("list", list);
		render("turnDetail11.html");
	}

	/**
	 * 双11专题 抽奖
	 */
	public void luckDraw11() {
		int aid = ((Record) getSessionAttr("account")).getInt("id");
		String imgurl = Db
				.queryStr("select img01 from f_turn_img where activeDate = curdate() and activeTitle = '双11活动'");
		String iconurl = Db
				.queryStr("select img02 from f_turn_img where activeDate = curdate() and activeTitle = '双11活动'");

		setAttr("imgurl", imgurl);
		setAttr("iconurl", iconurl);

		Number num = Db.queryNumber(
				"select count(1) from f_luckdraw_result where aid = ? and date_format(luckTime,'%Y-%m-%d')=curdate() and type=2",
				aid);
		if (num.intValue() > 0) {
			Record record = Db.findFirst(
					"select a.id,a.money,b.nick from f_luckdraw_result a left join f_account b on a.aid = b.id where a.aid = ? and date_format(a.luckTime,'%Y-%m-%d')=curdate() and a.type=2",
					aid);
			setAttr("result", "false");
			setAttr("rd", record);
			render("luckDraw11.html");
			return;
		}

		// 中奖区间
		Long countYM = Db.queryLong("select count(1) from f_turn_result as a "
				+ " left join f_luckdraw_result as b on a.lrId=b.id where b.aid=?", aid);// 翻牌人数超过100的人视为羊毛党
		Record rd = Db.findFirst("select id,pName,pMsg,origin,contentId,money from f_prizeList where id=29 and type=2");// 羊毛党直接给29那个奖池
		if (countYM < 100) {
			Long count = Db.queryLong("select count(1) from f_luckdraw_result where type=2 and aid=?", aid);// 本人已抽奖次数
			Long pArea = Db.queryLong(
					"select MIN(pAreaS)+MAX(pAreaE)-1 'pArea' from f_prizeList where state=1 and type=2 and id>0");
			int randNum = 0;
			if (pArea <= 10000) {
				randNum = new Random().nextInt(Integer.parseInt(String.valueOf(pArea)) - 1);// 随机数
			} else {
				// 第一次抽奖，落入10000以内
				if (count == 0) {
					randNum = new Random().nextInt(Integer.parseInt(String.valueOf(10000)));// 随机数
				}
				// 第二次及以上抽奖，落入10000之外
				else {
					int min = 10001;// 最小值
					int max = Integer.parseInt(String.valueOf(pArea)) - 1;// 最大值
					randNum = new Random().nextInt(max) % (max - min + 1) + min;// 随机数
				}
			}

			rd = Db.findFirst(
					"select id,pName,pMsg,origin,contentId,money from f_prizeList where pAreaS<=? and pAreaE>? and pNum>0 and type=2",
					randNum, randNum);
		}

		if (rd != null) {
			// 记录奖品
			Record f_luckdraw_result = new Record();
			f_luckdraw_result.set("aid", aid);
			f_luckdraw_result.set("prizeid", rd.getInt("id"));
			f_luckdraw_result.set("pname", rd.getStr("pName"));
			f_luckdraw_result.set("luckTime", new Date());
			f_luckdraw_result.set("state", 1);// 已经领取
			f_luckdraw_result.set("type", 2);// 2表示双11专题抽奖
			f_luckdraw_result.set("money", rd.getDouble("money"));// 抽到的金额
			f_luckdraw_result.set("moneyUpdate", rd.getDouble("money"));// 经过翻牌之后的金额
			boolean flag = Db.save("f_luckdraw_result", f_luckdraw_result);

			if (flag == true) {
				// 扣库存
				Db.update(" update f_prizeList set pNum=pNum-1 where id=?", rd.getInt("id"));
			}
			rd = Db.findFirst(
					"select a.id,a.money,b.nick from f_luckdraw_result a left join f_account b on a.aid = b.id where a.aid = ? and date_format(a.luckTime,'%Y-%m-%d')=curdate() and a.type=2",
					aid);

		} else {
			// 没有中奖时,返回的信息
			rd = new Record().set("money", 0).set("id", 0).set("nick", "花小妹");
		}

		setAttr("result", "true");// 是否当天已经抽过
		setAttr("rd", rd);
		render("luckDraw11.html");

	}

	/**
	 * 双11专题 翻牌
	 */
	public void turn11() {
		int lrId = getParaToInt(0);// f_luckdraw_result抽奖结果的ID号
		Record img = Db.findFirst(
				"select b.img02, b.img03,b.img04,b.img05,b.img06,b.img07 from f_luckdraw_result a  left join f_turn_img b on  date_format(a.luckTime,'%Y-%m-%d') = b.activeDate where  b.activeTitle = '双11活动' and a.id = ? and a.type =2",
				lrId);
		Record result = Db.findFirst("select COUNT(*) as num from f_luckdraw_result");
		String nick = Db.queryStr(
				"select b.nick from f_luckdraw_result a left join f_account b on a.aid = b.id where  a.type=2 and a.id=?",
				lrId);

		setAttr("result", result);
		setAttr("lrId", lrId);
		setAttr("img", img);
		setAttr("nick", nick);
		render("turn11.html");
	}

	/**
	 * 双11专题 获取翻牌结果
	 */
	public void turn11_result() {
		Map<String, Object> map = new HashMap<>();
		map.put("isoutnum", false);// 控制翻牌数量
		map.put("isoutdate", false);// 控制翻牌截止日
		String curdate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		if (curdate.compareTo("2017-11-10") > 0) {
			map.put("isoutdate", true);
			renderJson(map);
			return;
		}
		int lrId = getParaToInt("lrId"); // f_luckdraw_result抽奖结果的ID号
		int said = Db.queryInt("select aid from f_luckdraw_result where id = ?", lrId);
		Number lrnum = Db.queryNumber("select count(1) from f_turn_result where lrId = ?", lrId);
		Number num = Db.queryNumber(
				"SELECT  COUNT(1) FROM f_luckdraw_result a LEFT JOIN f_turn_result b on a.id = b.lrId WHERE a.aid = ? and b.aid =? ",
				said, ((Record) getSessionAttr("account")).getInt("id"));
		if (num.intValue() > 0) {
			Record rd = Db.findFirst(
					"SELECT  *FROM f_luckdraw_result a LEFT JOIN f_turn_result b on a.id = b.lrId WHERE a.aid = ? and b.aid =? ",
					said, ((Record) getSessionAttr("account")).getInt("id"));
			map.put("result", false);
			map.put("rd", rd);
			renderJson(map);
			return;
		}
		if (lrnum.intValue() > 99) {
			map.put("isoutnum", true);
			renderJson(map);
			return;
		}
		double moneyUpdate = Db.queryDouble("select moneyUpdate from f_luckdraw_result  where id=? and type =2", lrId);// 目前的金额
		Record pArea = Db.findFirst("select min(minArea)'minArea' ,max(maxArea) 'maxArea' from f_trun_list "
				+ " where minMoney<=? and maxMoney>? and state=1", moneyUpdate, moneyUpdate);// 计算中奖区间
		int min = pArea.getInt("minArea");// 最小值
		int max = pArea.getInt("maxArea") - 1;// 最大值
		int randNum = new Random().nextInt(max) % (max - min + 1) + min;// 随机数
		Record rd = Db.findFirst(
				"select id, money,msg from f_trun_list " + " where minArea<=? and maxArea>? and state=1", randNum,
				randNum);// 随机数落入哪个区间内
		if (rd != null) {
			// 记录翻牌结果
			Record f_turn_result = new Record();
			f_turn_result.set("lrId", lrId);// 抽奖结果的ID号
			f_turn_result.set("aid", ((Record) getSessionAttr("account")).getInt("id"));// 翻牌人ID
			f_turn_result.set("tlId", rd.getInt("id"));// 翻牌列表ID，关联f_trun_list
			f_turn_result.set("msg", rd.getStr("msg"));// 翻出的一句话
			f_turn_result.set("money", rd.getDouble("money"));// 翻出的金额
			f_turn_result.set("ctime", new Date());// 翻牌的时间
			boolean flag = Db.save("f_turn_result", f_turn_result);
			if (flag == true) {
				// 修改金额
				Db.update("update f_luckdraw_result set moneyUpdate=moneyUpdate+? where id=? and type =2",
						rd.getDouble("money"), lrId);
				// 给邀请人发客服消息
				String openid = Db.queryStr("select b.openid from f_luckdraw_result as a "
						+ " left join f_account as b on a.aid=b.id where a.id=? and a.type =2", lrId);// 邀请人openid
				String nick = Db.queryStr("select nick from f_account where id=?",
						((Record) getSessionAttr("account")).getInt("id"));// 翻牌人昵称
				WeixinApiCtrl.setApiConfig();
				CustomServiceApi.sendText(openid, nick + "翻牌后为您加了" + rd.getDouble("money") + "元");
			}
		} else {
			// 没有抽中时,金额保持不变
			rd = new Record().set("money", 0).set("msg", "haha,您的贡献值为0");
			String openid = Db.queryStr("select b.openid from f_luckdraw_result as a "
					+ " left join f_account as b on a.aid=b.id where a.id=? and a.type =2", lrId);// 邀请人openid
			String nick = Db.queryStr("select nick from f_account where id=?",
					((Record) getSessionAttr("account")).getInt("id"));// 翻牌人昵称
			WeixinApiCtrl.setApiConfig();
			CustomServiceApi.sendText(openid, nick + "翻牌后为您加了" + rd.getDouble("money") + "元");
		}
		map.put("result", true);
		map.put("rd", rd);
		renderJson(map);
	}
	
/***************************************双12翻牌******************************************************/	
	/**
	 * 双12专题:12星座 翻牌页面
	 */
	public void turn12() {
		Record img = Db.findFirst(
				"select img02,img03,img04,img05,img06,img07,img08,img09,img10,img11,img12,img13,img14,img15 "
				+ "from  f_turn_img b where activeTitle='12星座翻牌'");
		Record result = Db.findFirst("select COUNT(*) as num from f_luckdraw_result");
		setAttr("img", img);
		setAttr("result", result);
		render("turn12.html");
	} 
	
	
	/**
	 * 双12专题:12星座 翻牌结果
	 */
	public void turn12_result() {
		Map<String, Object> map = new HashMap<>();
		map.put("isoutnum", false);// 控制翻牌数量，每人只能翻一次
		map.put("isoutdate", false);// 控制翻牌截止日期
		String curdate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		if (curdate.compareTo("2017-12-12") > 0) {
			map.put("isoutdate", true);
			renderJson(map);
			return;
		}
		Record account=getSessionAttr("account");
		Number num = Db.queryNumber("select count(*) from f_luckdraw_result where type=3 and aid = ? ",account.getInt("id"));
		if (num.intValue()>0) {
			map.put("isoutnum", true);
			renderJson(map);
			return;
		}
		int isbuy=account.getInt("isbuy");//isbuy==0  抽新用户花票，isbuy!=0抽老用户花票
		String ids;
		int themeId;
        if (isbuy==0) {
        	themeId=57;
			 ids = Db.queryStr(" SELECT  GROUP_CONCAT(a.id) ids FROM f_prizeList a  LEFT JOIN f_cashclassify b on a.contentId = b.id LEFT JOIN f_cashtheme c on b.tid = c.id  WHERE a.TYPE =3 and c.id = 57 ");
		}else {
			themeId=58;
			 ids = Db.queryStr(" SELECT  GROUP_CONCAT(a.id) ids FROM f_prizeList a  LEFT JOIN f_cashclassify b on a.contentId = b.id LEFT JOIN f_cashtheme c on b.tid = c.id  WHERE a.TYPE =3 and c.id = 58 ");	
		} 
        Record pArea = Db.findFirst(
				"select min(pAreaS)'minArea' ,max(pAreaE) 'maxArea' from f_prizeList where state=1 and type=3 and id in ("+ids+")");
    	int min = pArea.getInt("minArea");// 最小值
		int max = pArea.getInt("maxArea") - 1;// 最大值
		int randNum = new Random().nextInt(max) % (max - min + 1) + min;// 随机数
        Record rd = Db.findFirst(
				"select id,pName,pMsg,origin,contentId from f_prizeList where pAreaS<=? and pAreaE>? and pNum>0 and type=3",
				randNum, randNum);
    	if (rd != null) {
			// 记录奖品
			int state = 1;// 已经领取
			Record f_luckdraw_result = new Record();
			f_luckdraw_result.set("aid", account.getInt("id"));
			f_luckdraw_result.set("prizeid", rd.getInt("id"));
			f_luckdraw_result.set("pname", rd.getStr("pName"));
			f_luckdraw_result.set("luckTime", new Date());
			f_luckdraw_result.set("state", state);
			f_luckdraw_result.set("type", 3);
			Double cashmoney = Db.queryDouble("select money from  f_cashclassify where id = ?",rd.getInt("contentId"));
			rd.set("msg", cashmoney.intValue()+"元花票");
			boolean flag = Db.save("f_luckdraw_result", f_luckdraw_result);

			if (flag == true) {
					// 发花票
					sendCash12(rd.getInt("contentId"),account.getInt("id"),themeId,account);
				// 扣库存
				Db.update(" update f_prizeList set pNum=pNum-1 where id=?", rd.getInt("id"));
			}

		} else {
			// 没有中奖时,返回的信息
			rd = new Record().set("msg", "谢谢参与").set("pMsg", "谢谢参与");
		}
    	map.put("result", true);
		map.put("rd", rd);
		renderJson(map);
	}
	/**
	 * 发放奖品花票
	 * 
	 * @param cashthemeId
	 * @param aid
	 */
	private void sendCash12(int cashId, int aid,int themeId,Record account) {
			Record c = new Record();
			c.set("aid", aid);
			c.set("cid", cashId);
			c.set("code", "1111");
			Calendar now = Calendar.getInstance();
			c.set("time_a", now.getTime());
			c.set("time_b", "2017-12-12");
			c.set("state", 1);
			c.set("origin", 4);
			Db.save("f_cash", c);				
			Db.update("update f_cashtheme set sendcount=sendcount+1 where id=?", themeId);// 发送数量+1
			WeixinApiCtrl.setApiConfig();
			String message = "您通过星座翻牌游戏得到了一张花票，请及时使用\n\r<a href='"+Constant.getHost+"/account/mycash"+"'>查看详情</a>";
			CustomServiceApi.sendText(account.getStr("openid"), message);
	}
/*********************************************************************************************************/
	/**
	 * 抽奖
	 */
	public void luckDraw() {

		// System.out.println(getPara("tt"));

		Map<String, Object> map = new HashMap<>();
		// 判断是否可以参加抽奖
		int seedNum = Integer
				.valueOf(Db.queryStr("select code_value from f_dictionary where code_key='fsn' and state=1"));// 获取抽奖所需的花籽数
		Long seedMy = Db.queryLong("select count(1) from f_flowerseed where aid=? and state=0",
				((Record) getSessionAttr("account")).getInt("id"));
		if (seedMy < seedNum) {
			map.put("result", false);
			map.put("rd", null);
		} else {
			Long pArea = Db.queryLong(
					"select MIN(pAreaS)+MAX(pAreaE) 'pArea' from f_prizeList where state=1 and type=1 and id>0");
			int randNum = new Random().nextInt(Integer.parseInt(String.valueOf(pArea)));
			Record rd = Db.findFirst(
					"select id,pName,pMsg,origin,contentId from f_prizeList where pAreaS<=? and pAreaE>? and pNum>0 and type=1",
					randNum, randNum);
			if (rd != null) {
				long rowno=Db.queryLong("select count(1) from f_prizeList where type=1 and state=1 and id<=?",rd.getInt("id"));				
				rd.set("rowno", rowno);
				// System.err.println(rd);
				// 记录奖品
				int state = 1;// 已经领取
				if (rd.getInt("origin") == 1) {
					state = 2;// 非花美美平台的，等待配送
				}
				Record f_luckdraw_result = new Record();
				f_luckdraw_result.set("aid", ((Record) getSessionAttr("account")).getInt("id"));
				f_luckdraw_result.set("prizeid", rd.getInt("id"));
				f_luckdraw_result.set("pname", rd.getStr("pName"));
				f_luckdraw_result.set("luckTime", new Date());
				f_luckdraw_result.set("state", state);
				f_luckdraw_result.set("type", 1);
				boolean flag = Db.save("f_luckdraw_result", f_luckdraw_result);

				if (flag == true) {
					// 如果是花美美平台的奖品，直接通过平台发放
					if (rd.getInt("origin") == 2) {
						// 发花籽
						sendSeed(rd.getInt("contentId"), ((Record) getSessionAttr("account")).getInt("id"));
					} else if (rd.getInt("origin") == 3) {
						// 发花票
						sendCash(rd.getInt("contentId"), ((Record) getSessionAttr("account")).getInt("id"));
					} else if (rd.getInt("origin") == 4) {
						// 发红包
						sendRedpackets(rd.getInt("contentId"), ((Record) getSessionAttr("account")).getInt("id"),
								rd.getStr("pMsg"));
					}
					// 扣库存
					Db.update(" update f_prizeList set pNum=pNum-1 where id=?", rd.getInt("id"));
				}

			} else {
				// 没有中奖时,返回的信息
				rd = Db.findFirst("select id,pName,pMsg,origin,contentId from f_prizeList where id=0", randNum,
						randNum);
				rd.set("rowno", 0);
			}

			// 扣花籽
			// 找到前n粒花籽
			List<Record> list = Db.find("select id from f_flowerseed where aid=? and state=0 ORDER BY ctime LIMIT ?",
					((Record) getSessionAttr("account")).getInt("id"), seedNum);
			for (Record record : list) {
				Db.update("update f_flowerseed set state=4 where id =?", record.getInt("id"));
			}
			map.put("result", true);
			map.put("rd", rd);
		}
		renderJson(map);

	}

	/**
	 * 发放奖品花籽
	 * 
	 * @param num
	 * @param aid
	 */
	private void sendSeed(int num, int aid) {
		for (int i = 0; i < num; i++) {
			Record f_flowerseed = new Record();
			f_flowerseed.set("aid", aid);
			f_flowerseed.set("send", 1);
			f_flowerseed.set("type", "Prize");
			f_flowerseed.set("remarks", "中奖");
			f_flowerseed.set("ctime", new Date());
			f_flowerseed.set("state", 0);
			f_flowerseed.set("username", "system");
			Db.save("f_flowerseed", f_flowerseed);
		}
	}

	/**
	 * 发放奖品花票
	 * 
	 * @param cashthemeId
	 * @param aid
	 */
	private void sendCash(int cashthemeId, int aid) {
		List<Record> cashlist = Db
				.find("select a.ltime,b.id from f_cashtheme as a left join f_cashclassify as b on b.tid=a.id "
						+ " where   a.ltime>0 and b.state=0 and a.id=?", cashthemeId);
		if (cashlist.size() > 0) {
			for (Record cash : cashlist) {
				Record c = new Record();
				c.set("aid", aid);
				c.set("cid", cash.getInt("id"));
				c.set("code", "1111");
				Calendar now = Calendar.getInstance();
				c.set("time_a", now.getTime());
				now.add(Calendar.DAY_OF_MONTH, cash.getInt("ltime"));
				c.set("time_b", now.getTime());
				c.set("state", 1);
				c.set("origin", 1);
				Db.save("f_cash", c);
			}
			Db.update("update f_cashtheme set sendcount=sendcount+1 where id=?", cashthemeId);// 发送数量+1
			Db.update("update f_cashtheme set ltime=0 where id=? and maxcount<>0 and sendcount>=maxcount", cashthemeId);// 送完了修改状态

		}
	}

	/**
	 * 发放中奖红包
	 * 
	 * @param redId
	 * @param aid
	 * @param pMsg
	 */
	private void sendRedpackets(int redId, int aid, String pMsg) {
		Record rd = Db.findFirst("select id,pmoney from f_redpackets_pro where id=?", redId);
		// 红包单头
		Record f_redpackets = new Record();
		f_redpackets.set("type", 4);// 中奖红包
		f_redpackets.set("money", rd.getDouble("pmoney"));
		f_redpackets.set("quantity1", 1);
		f_redpackets.set("quantity2", 1);
		f_redpackets.set("msg", pMsg);
		f_redpackets.set("ctime", new Date());
		f_redpackets.set("stime", new Date());
		f_redpackets.set("state", 2);
		f_redpackets.set("aid", 1);
		Db.save("f_redpackets", f_redpackets);
		long nowID = f_redpackets.getLong("id");// 本来是想用事务处理，但是这个会报错，还没有想到好的解决方法
		// 红包单身
		Record f_redpackets_detial = new Record();
		f_redpackets_detial.set("rpid", nowID);
		f_redpackets_detial.set("rppid", rd.getInt("id"));
		f_redpackets_detial.set("isopen", 1);
		f_redpackets_detial.set("aid", aid);
		f_redpackets_detial.set("gtime", new Date());
		f_redpackets_detial.set("orderlist", "");
		Db.save("f_redpackets_detial", f_redpackets_detial);
	}
	
	
	
	
	 
	/*********************************************************************************************/

	/********** 十八号花店 *************/

	/**
	 * 获取myMemory页面参数
	 * 
	 * @author Glacier
	 * @date 2017年10月10日
	 */
	public void myMemory() {
		int isshow = 0;
		String url = "";
		String content = "";
		// 判断用户是否已经填写 如果已经填写 则isshow =1

		Record user = Db.findFirst("SELECT id,aid,contentStr,subTime,state,urlStr from f_myMemory where aid = ?",
				((Record) getSessionAttr("account")).getInt("id"));
		if (user != null) {
			isshow = 1;

			// System.out.println(user);
			// 有数据的话获取代言二维码
			url = user.getStr("urlStr");
			// 获取内容
			content = user.getStr("contentStr");
		}

		setAttr("isshow", isshow);
		setAttr("url", url);
		setAttr("content", content);

		/*
		 * System.out.println("isshow: "+ isshow); System.out.println("url: "+
		 * url); System.out.println("content: "+ content);
		 */

		render("myMemory.html");
	}

	/**
	 * 扫二维码进入 myMemory页面
	 * 
	 * @author Glacier
	 * @date 2017年10月13日
	 */
	public void showMemory() {
		int id = getParaToInt();

		int isshow = 0;
		String url = "";
		String content = "";
		// 判断用户是否已经填写 如果已经填写 则isshow =1

		Record user = Db.findFirst("SELECT id,aid,contentStr,subTime,state,urlStr from f_myMemory where id = ?", id);
		if (user != null) {
			isshow = 1;

			// System.out.println(user);
			// 有数据的话获取代言二维码
			url = user.getStr("urlStr");
			// 获取内容
			content = user.getStr("contentStr");
		}

		setAttr("isshow", isshow);
		setAttr("url", url);
		setAttr("content", content);

		render("myMemory.html");

	}

	/**
	 * 提交生成二维码图片
	 * 
	 * @author Glacier
	 * @date 2017年10月10日
	 */
	public void postMemory() {

		// 获取言论
		String contentStr = getPara("contentStr");
		// 敏感词过滤
		String reString = new WordFiler().afterFiler(contentStr);

		int aid = ((Record) getSessionAttr("account")).getInt("id");

		Record f_myMemory = new Record();
		f_myMemory.set("aid", aid);
		f_myMemory.set("contentStr", reString);
		f_myMemory.set("subTime", new Date());
		f_myMemory.set("state", 1); // 言论默认合法

		boolean flat = Db.save("f_myMemory", f_myMemory);

		// 获取id
		long id = f_myMemory.getLong("id");
		System.out.println("id : " + id);

		new DaiYanURL();

		// 生成带颜二维码
		String url = DaiYanURL.getUrl(aid, 1, id + "");

		// 修改urlStr
		int flag = Db.update("UPDATE f_myMemory SET urlStr = ? where id = ?", url, id);

		// 渲染页面
		setAttr("isshow", 1);
		setAttr("url", url);

		render("myMemory.html");
	}

	/**
	 * 是否可以提交
	 * 
	 * @author Glacier
	 * @date 2017年10月10日
	 */
	public void canPost() {
		boolean result = true; // 返回 true 表示没有填写过
		JSONObject obj = new JSONObject();

		// 根据aid 判断用户是否提交过
		Record user = Db.findFirst("SELECT id,aid,contentStr,subTime,state from f_myMemory where aid = ?",
				((Record) getSessionAttr("account")).getInt("id"));
		if (user != null) {
			result = false; // 表示填写过
		}
		System.out.println(result);
		System.out.println(user);

		renderJson(result);
	}

	/**
	 * ------------------------------鲜花卡----------------------------------------
	 * -
	 ***/

	/**
	 * 花卡
	 * 
	 * @author Glacier
	 * @date 2017年10月17日
	 */
	public void nextblessing() {

		// 商品id
		String proid = getPara("id");

		// 图片列表
		List<Record> imglist = Db
				.find("SELECT id,`name`,imgurl01 FROM f_redpackets_pic WHERE state = 1 ORDER BY typeId,imgOrderId ASC");
		setAttr("imglist", imglist);
		setAttr("proid", proid);

		render("/front/account/blessing_info.html");
	}

	/**
	 * 创建卡片
	 * 
	 * @author Glacier
	 * @date 2017年10月18日
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void createRedcard() throws ParserConfigurationException, IOException, SAXException {
		int type = getParaToInt("type");
		String pids = getPara("pid");
		// 红包留言留言
		String message = getPara("message");
		// 加购 花瓶
		String is_vase = getPara("is_vase");
		if (is_vase == null) {
			is_vase = "0";
		}
		String cardid = getPara("cardid");

		Map<String, Object> xmlMap = new HashMap<>();
		// 创建红包
		xmlMap = FCDao.createRedcard(getRequest(), getSession(), type, pids, message, is_vase, cardid);

		if ((boolean) xmlMap.get("result")) {
			Map<String, String> rMap = Sign.sign2(xmlMap.get("prepay_id").toString());
			rMap.put("redpacketcode", (String) xmlMap.get("redpacketcode"));

			renderJson(rMap);
		} else {
			render("error.html");
		}
	}

	/**
	 * 生成卡片
	 * 
	 * @author Glacier
	 * @date 2017年10月18日
	 */
	public void shareRedcard() {

		String code = getPara(0);

		Record account = getSessionAttr("account");
		int aid = account.getInt("id");

	/*	List<Record> imglist = Db.find(
				"select  a.code_name,a.code_value ,b.imgurl01 from f_dictionary a left join f_redpackets_pic b on a.code_value=b.typeId where code_key='redpacketsPicType' and a.state=1 and b.imgOrderId=1 order by orderId");

		setAttr("imglist", imglist);*/
		
		
		// 获取订阅商品
		List<Record> dingyuelist = Db.find("SELECT a.id,a.`name`,a.fpid,a.pnum,a.imgurl03,b.ptid"
				+ " FROM f_redpackets_pro as a "
				+ " LEFT JOIN f_flower_pro as b ON a.fpid = b.id "
				+ " WHERE a.userType = 2 and a.state = 1 and b.ptid in(1,2)"
				+ " ORDER BY a.orderId ASC");
		
		//获取礼品花
		List<Record> lipinlist = Db.find("SELECT a.id,a.`name`,a.fpid,a.pnum,a.imgurl03,b.ptid"
				+ " FROM f_redpackets_pro as a "
				+ " LEFT JOIN f_flower_pro as b ON a.fpid = b.id "
				+ " WHERE a.userType = 2 and a.state = 1 and b.ptid = 3"
				+ " ORDER BY a.orderId ASC");
		
		setAttr("dingyuelist", dingyuelist);
		setAttr("lipinlist", lipinlist);
		

		// 生成二维码 4.红包 41.礼品卡
		String url = DaiYanURL.getUrl(aid, 41, code);

		// 要根据code 获取 - 这把不管state=1 因为异步操作 weixin那边支付成功之后操作很多这边的state
		// 后台还没来得及改过来所以先不管state
		Record Card = Db.findFirst("SELECT id,type,money,quantity1,msg,picId,aid,state from f_redpackets where id = ?",
				code);

		int isshow = 0;
		if (Card != null) {
			isshow = 1;

			// 获取卡片地址 - 获取卡片没有考虑state
			Record redCardimg = Db.findById("f_redpackets_pic", Card.getInt("picId"));

			// 修改为生成二维码用imgurl02
			setAttr("cardUrl", redCardimg.getStr("imgurl02"));
			// url地址
			setAttr("url", url);

		} else {
			System.err.println("没有查询到card");
			System.out.println(Card);
		}

		setAttr("isshow", isshow);
		render("/front/account/blessing.html");
	}

	/**
	 * 获取卡片
	 * 
	 * @author Glacier
	 * @date 2017年10月20日
	 */
	public void getRedcard() {

		String cardCode = getPara("cardCode");
		String type = getPara("type");// 41表示第一次发卡领取；42表示转赠之后领取

		Map<String, Object> data = new HashMap<>();

		/**
		 * 分两种情况 42 转赠 1. 42 转赠 2. 41 第一次
		 */
		int flat = 0;
		Record user = getSessionAttr("account");
		// 查看是否已经被领取 单身
		// 现在是闺蜜的 所以看rpid 就行了
		Record card = Db.findFirst(
				"SELECT id,rpid,rppid,isopen,aid,gtime,oid,dmoney from f_redpackets_detial WHERE rpid = ? and isopen=0 for update",
				cardCode);

		if (type.equals("42")) {
			card = Db.findFirst(
					"SELECT id,rpid,rppid,isopen,aid,gtime,oid,dmoney from f_redpackets_detial WHERE id = ? for update",
					cardCode);
			// 直接修改
			flat = Db.update("UPDATE f_redpackets_detial SET isopen = 1,aid = ?,gtime=? where id = ?",
					user.getInt("id"), new Date(), card.getInt("id"));

		} else if (type.equals("41")) {
			// 41表示第一次发卡领取 第一次赠送的时候才会有已被领取的情况

			// 已被领取
			if (card.getInt("aid") == null) {

				flat = Db.update("UPDATE f_redpackets_detial SET isopen = 1,aid = ?,gtime=?,aid3=? where id = ?",
						user.getInt("id"), new Date(), user.getInt("id"), card.getInt("id"));
				flat = Db.update("update f_redpackets set quantity2=quantity2+1 where id=?", cardCode);// 已拆数量+1
				Record rd = Db.findFirst("select quantity1,quantity2 from f_redpackets where id=?", cardCode);

				// 发包人信息
				Record redpacket = Db.findFirst("SELECT aid FROM f_redpackets where id = ?", cardCode);
				Record account2 = Db.findFirst("SELECT openid, headimg, nick FROM f_account WHERE id = ?",
						redpacket.getInt("aid"));
				// 发送客服消息
				Record account = getSessionAttr("account");
				WeixinApiCtrl.setApiConfig();
				String msg = account.getStr("nick") + " 领取了你的礼品卡。\n<a href='" + Constant.getHost
						+ "/account/blessing_history'>点击查看</a>";
				CustomServiceApi.sendText(account2.getStr("openid"), msg);

				// 包含数量<=已拆数量
				if (rd.getInt("quantity1") <= rd.getInt("quantity2") + 1) {
					Db.update("update f_redpackets set state=2 where id=?", cardCode);// 已经抢完
				}

			} else {
				data.put("result", false);
				data.put("msg", "手慢啦！已被他人领取！");
			}

		}

		if (flat == 1) {
			data.put("result", true);
			data.put("msg", "成功领取");
		} else {
			data.put("result", false);
			data.put("msg", "系统繁忙，稍后再试！");
		}


		renderJson(data);
	}

}