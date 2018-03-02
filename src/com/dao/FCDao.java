package com.dao;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.controller.WeixinApiCtrl;
import com.controller.WeixinMsgController;
import com.google.gson.Gson;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.MediaApi;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.jfinal.weixin.sdk.api.QrcodeApi;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jfinal.weixin.sdk.api.MediaApi.MediaType;
import com.jfinal.weixin.sdk.api.PaymentApi.TradeType;
import com.jfinal.weixin.sdk.msg.out.OutTextMsg;
import com.sun.org.apache.xpath.internal.operations.And;
import com.jfinal.weixin.sdk.api.TemplateData;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;
import com.util.Constant;
import com.util.Constant.orderState;
import com.util.Constant.seedType;
import com.util.DaiYanURL;

import sun.util.logging.resources.logging;

import com.util.DeliveryDateUtil;
import com.util.NewImageUtils;
import com.util.QRCodeUtil;
import com.util.Signature;
import com.util.XMLParser;


/**
 * @Desc 后台公共数据接口
 * */
public class FCDao {
	
	static Log logger = Log.getLog(FCDao.class);
	static Map<String , Object> resultMap;	//创建订单结果
	static int Pid;
	static Integer Vase;
	static int addressId;
	static int Reach;
	//周期   用途  格调
	static int Zhouqi;
	static int Use;
	static int Style;
	static int FestivalId;
	// 定制鲜花
	static String Fpid;
	//加购商品
	static String Adds;
	
	static int Cycle;
	static String Zhufu;
	static String Songhua;
	static String jh_List;
	static String jhColor_List;
	static int Type;
	static String Recommend;
	static Integer Szdx;
	static Integer Cash;//用户选择符合条件的花票
	static Integer Cash2;//用户通过兑换码使用的花票
	static Integer Activity;
	static Double Yh;
	static Record Account;
	static Record pro_Flower;
	static int isBuy;
	
	static long PtNo;//团号
	static int JsAid;//入团介绍人的ID
	
	static int PicId;//祝福卡ID号
	static int Num;
	
	
	static String sy_code;//节日订单的发货批号
	static Record Order;
	static Record Receipt;
	static String defaultimg = Constant.getHost + "/resource/flower/image/defaultimg.jpg"; //默认头像 
	// 花边好物列表
	public static List<Record> getAround(int isbuy){
		
		return Db.find("SELECT b.id,b.name,b.imgurl,b.describe,b.price FROM f_dictionary a LEFT JOIN f_flower_pro b ON a.code_value=b.ptid "
				+ "where b.state=0 AND a.code_value>=4 and ptid<=50 and ptid!=11 and ptid!=10 and a.code_key = 'ptid'" );
	}
	// 花边好物详情
	public static Record getAroundInfo(int pid){
		return Db.findFirst("SELECT a.id,a.name,a.imgurl,a.itinfo1,a.itinfo2,a.itinfo3,a.itinfo4,a.itinfo5,"
				+ "a.describe,a.price,a.shareDes,a.shareTitle,ptid FROM f_flower_pro a WHERE ptid>=4 and ptid<=50 and ptid!=11  AND id=? and a.state=0 ", pid);
	}
	/**
	 * 获取商品详情（双品多品）
	 * @author Glacier
	 * @date 2017年9月21日
	 * @param ptid
	 * @return
	 */
	public static Record getProductInfo(int ptid){
		Record pro = Db.findFirst("SELECT id,`name`,imgurl,itinfo1,itinfo2,itinfo3,itinfo4,itinfo5,dmlj,reachtype,`describe`,price,shareDes,shareTitle "
								+ "FROM f_flower_pro where ptid = ? and state = 0", ptid);
		return pro;
	}
	
	/**
	 * 获取商品详情  拼团202
	 * @author Glacier
	 * @date 2017年8月17日
	 * @return
	 */
	public static Record getProduct(int id) {
		Record pro = Db.findFirst("SELECT  state,id,ptid,`name`,imgurl,itinfo1,itinfo2,itinfo3,itinfo4,itinfo5,`describe`,price,ptCount,ptHours,ptPrice,shareDes,shareTitle,allownew,isptFree,ptNum from f_flower_pro  where  id= ? and ptid = 202 ",id);
		return pro;
	}
	
	/**
	 * 拼团多买立减
	 * @author Glacier
	 * @date 2017年8月18日
	 * @return
	 */
	public static Record getPintuanDic() {
		return Db.findFirst("SELECT code_value FROM f_dictionary where code_key = 'dmlj4'");
	}
	
	/**
	 * 首页商品列表
	 * @author Glacier
	 * @date 2017年7月12日
	 * @return
	 */
	public static List<Record> getindexInfo() {
		List<Record> pro = Db.find("SELECT id,`name`,imgurl,price,goods_url,goods_describe,isshow,sort "
				+ "from f_index_goods where isshow='1' and type = 1 ORDER BY sort DESC");
		return pro;
	}
	
	/**
	 * 获取闪购商品列表
	 * @author Glacier
	 * @date 2017年11月6日
	 * @return
	 */
	public static List<Record> getShan() {
		List<Record> pro = Db.find("SELECT b.id,b.name,b.imgurl,b.itinfo1,b.itinfo2,b.itinfo3,b.itinfo4,b.itinfo5,b.describe,b.price,a.hDate,a.hReach,a.hState,a.hTitle,b.shareDes,b.shareTitle "
				+ " FROM f_holiday a  LEFT JOIN f_flower_pro b ON a.hPtid=b.ptid  WHERE a.id = 4 and  b.state=0");
		return pro;
	}
	
	/**
	 * 获取主题花商品列表
	 * @author Glacier
	 * @date 2017年11月6日
	 * @return
	 */
	public static List<Record> getZhu() {
		List<Record> pro = Db.find("SELECT b.id,b.name,b.imgurl,b.itinfo1,b.itinfo2,b.itinfo3,b.itinfo4,b.itinfo5,b.describe,b.price,a.hDate,a.hReach,a.hState,a.hTitle,b.shareDes,b.shareTitle "
				+ " FROM f_holiday a  LEFT JOIN f_flower_pro b ON a.hPtid=b.ptid WHERE a.id = 5 and  b.state=0");
		return pro;
	}
	
	/**
	 * 获取花边好物列表
	 * @author Glacier
	 * @date 2017年11月6日
	 * @return
	 */
	public static List<Record> getHuabian() {
		List<Record> pro = Db.find("SELECT id,ptid,type,`name`,imgurl,`describe`,price FROM f_flower_pro WHERE (ptid = 4 or ptid = 5) and state = 0");
		return pro ;
	}
	
	
	/**
	 * 是否已经关注过花美美
	 * @param openid
	 * @return
	 */
	public static int isFoucs(String openid){
		int isfans = Db.queryInt("select isfans from f_account where openid = ?", openid);
		return isfans;
		
	}
	/**
	 * 主页轮换图
	 * @author Glacier
	 * @date 2017年7月19日
	 * @return
	 */
	public static List<Record> getRotation() {
		return Db.find("SELECT id,`name`,imgurl,goods_url,goods_describe from f_index_goods where type = 2");
	}
	
	// 节日商品详情
	public static List<Record> getFestivalProducts2(int fptid){
		return Db.find("SELECT b.id,b.name,b.imgurl,b.itinfo1,b.itinfo2,b.itinfo3,b.itinfo4,b.itinfo5,b.describe,b.price,b.shareDes,b.shareTitle,b.name 'hTitle',c.state 'hState',c.code_name,b.type 'flowerType' "
				+ " FROM  f_flower_pro b  "
				+ " left join f_dictionary as c on b.ptid=c.code_value and code_key='ptid' WHERE b.id = ? and  b.state=0 and b.isAlonePage=0 order by b.id", fptid);
	}
	
	public static List<Record> getFestivalProducts(int ptid){
		return Db.find("SELECT b.id,b.name,b.imgurl,b.itinfo1,b.itinfo2,b.itinfo3,b.itinfo4,b.itinfo5,b.describe,b.price,b.shareDes,b.shareTitle,b.name 'hTitle',c.state 'hState',code_name,b.type 'flowerType' "
				+ " FROM  f_flower_pro b  "
				+ " left join f_dictionary as c on b.ptid=c.code_value and code_key='ptid' WHERE b.ptid = ? and  b.state=0 and b.isAlonePage=0  order by b.id", ptid);
	}
	/**
	 * 显示单个商品
	 * @param id
	 * @return
	 */
	public static List<Record> getFestivalProducts3(int id){
		return Db.find("SELECT b.id,b.name,b.imgurl,b.itinfo1,b.itinfo2,b.itinfo3,b.itinfo4,b.itinfo5,b.describe,b.price,b.shareDes,b.shareTitle,b.name 'hTitle',c.state 'hState',code_name,b.type 'flowerType',b.state 'flowerState' "
				+ " FROM  f_flower_pro b  "
				+ " left join f_dictionary as c on b.ptid=c.code_value and code_key='ptid' WHERE b.id = ?   order by b.id", id);
	}

	/**
	 * 获取送花,花瓶商品列表 
	 * @author Glacier
	 * @date 2017年9月21日
	 * @param ptid 3.送花 4.花瓶
	 * @param isbuy
	 * @return
	 */
	public static List<Record> getProducts(int ptid,int isbuy){
		String typeStr = "";
		if(ptid==4){
			if(isbuy == 0){
				typeStr = " and type in (0,1)";
			}else{
				typeStr = " and type in (0,2)";
			}
		}
		return Db.find("SELECT id,state,`name`,imgurl,itinfo1,itinfo2,itinfo3,itinfo4,itinfo5,`describe`,price,shareDes,shareTitle"
				+ " from f_flower_pro "
				+ " where state != 1 and ptid = ?"+typeStr, ptid);
	}

	// 获取加购商品列表
	public static List<Record> getProductsList(int ptid) {
		return Db.find("SELECT id, `name`, itinfo1, `describe`, `describe2`, price, state FROM f_flower_pro WHERE state = 0 AND ptid = ?", ptid);
	}

	/**
	 * 定制鲜花
	 * @author Glacier
	 * @date 2017年9月21日
	 * @param ptid
	 * @return
	 */
	public static List<Record> getFlowers(int ptid) {
		return Db.find("SELECT id, `name`, imgurl, `describe`,price, itinfo1,dmlj,"
				+ "itinfo2, itinfo3, x, y,shareDes,shareTitle from f_flower_pro where ptid = ? and state != 1 order by x,y",ptid);
	}
	
	// 获得地区数据
	public static String getArea(){
		List<Record> list = Db.find("SELECT a.id, a.name, a.pid, b.pid AS ppid FROM f_area a LEFT JOIN f_area b ON a.pid=b.id WHERE a.state = 1");
		return JsonKit.toJson(list);
	}
	// 获取我的地址
	public static List<Record> getAddress(int aid){
		List<Record> list = Db.find("select id,name,tel,area,addr,state from f_address where aid = ?", aid);
		for(Record address : list){
			if(address.getStr("area").length()>0){
				String addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ address.getStr("area") +")");
				address.set("addr", addr+"-"+address.getStr("addr"));
			}
		}
		return list;
	}
	// 保存地址
	public static boolean saveAddress(Integer id, Integer state, int aid, String name, String tel, String area, String addr, int give){
/*		if(id == null){
			Number count = Db.queryNumber("SELECT COUNT(1) FROM f_address WHERE aid = ?", aid);
			if(count.intValue() >= 10){
				return false;
			}
			//之前限制只能存10条地址，现取消。
		}*/
		Record address = new Record();
		if(state == 1){
			address.set("state", state);
			Db.update("update f_address set state=0 where aid=?", aid);
		}
		address.set("aid", aid);
		address.set("name", name);
		address.set("tel", tel);
		address.set("area", area);
		address.set("addr", addr);
		address.set("give", give);
		if(give == 2){
			address.set("state", 0);
		}
		if(id == null){
			return Db.save("f_address", address);
		}else{
			address.set("id", id);
			return Db.update("f_address", address);
		}
	}
	// 设置默认地址
	public static boolean setDefault(int id, int aid){
		Db.update("update f_address set state=0 where aid=?", aid);
		int count = Db.update("update f_address set state=1 where id=?", id);
		return count==1?true:false;
	}
	// 购买-获取默认地址
	public static Record getDefaultAddress(int aid, int give, Integer addrid){
		Record address = null;
		String addr = new String();
		if(addrid == null){
			List<Record> list = Db.find("select id,name,tel,area,addr,state from f_address where aid = ? and give = ?", aid, give);
			if(list.size() > 0){
				for(Record r : list){
					if(r.getInt("state") == 1){
						// 如果有默认地址则取默认地址
						address = r;
						addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ address.getStr("area") +")");
						address.set("addr", addr+"-"+address.getStr("addr"));
					}
				}
				if(address == null){
					address = list.get(0);	// 默认取第一条
					addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ list.get(0).getStr("area") +")");
					address.set("addr", addr+"-"+address.getStr("addr"));
				}
			}
		}else{
			address = Db.findFirst("select id,name,tel,area,addr,state from f_address where id = ? and aid = ? and give = ?", addrid, aid, give);
			addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ address.getStr("area") +")");
			address.set("addr", addr+"-"+address.getStr("addr"));
		}
		return address;
	}
	// 购买-选择收货地址
	public static List<Record> getAddressForType(int aid, int give){
		List<Record> list = Db.find("select id,name,tel,area,addr,state from f_address where aid = ? and give = ?", aid, give);
		for(Record address : list){
			String addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ address.getStr("area") +")");
			address.set("addr", addr+"-"+address.getStr("addr"));
		}
		return list;
	}
	// 获取所有收货地址
	public static List<Record> getAddresses(int aid){
		List<Record> list = Db.find("SELECT id,area,addr FROM f_address where aid =?", aid);
		for(Record address : list){
			String addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ address.getStr("area") +")");
			address.set("addr", addr+"-"+address.getStr("addr"));
		}
		return list;
	}
	// 签到
	public static boolean signin(String openId,int aid){
		boolean result = false;
		Number num = Db.queryNumber("select count(1) from f_flowerseed where aid=? and type=? and date_format(ctime,'%Y-%m-%d')=curdate()", aid, seedType.sign.name());
		if(num.intValue() == 0){
			for(int i = 0; i<seedType.sign.point; i++){
				Record seed = new  Record();
				seed.set("aid", aid);
				seed.set("send", 1);
				seed.set("type", seedType.sign.name());
				seed.set("remarks", seedType.sign.name);
				seed.set("ctime", new Date());
				result = Db.save("f_flowerseed", seed);
				if (result) {
					// 绑定 apiConfig 到线程之上
					Calendar now = Calendar.getInstance(); 
					WeixinApiCtrl.setApiConfig();
					String message = "签到成功，送上1颗花籽。\n<a href='" + Constant.getHost + "/account/flowerseed'>【查看花籽】</a>";
					if(now.get(Calendar.HOUR_OF_DAY)>=22 || now.get(Calendar.HOUR_OF_DAY)<=4){
						message="夜深了，注意休息哦！"+ message;
					}
					if(now.get(Calendar.HOUR_OF_DAY)>4 && now.get(Calendar.HOUR_OF_DAY)<=5){
						message="愿你今天的心情像花儿一样！"+ message;
					}
					if(now.get(Calendar.HOUR_OF_DAY)>5 && now.get(Calendar.HOUR_OF_DAY)<=7){
						message="亲，记着吃早饭，上班路上开车慢点哦！"+ message;
					}
					if(now.get(Calendar.HOUR_OF_DAY)>7 && now.get(Calendar.HOUR_OF_DAY)<=9){
						message="一束鲜花，一米阳光，生活本该如此！"+ message;
					}
					if(now.get(Calendar.HOUR_OF_DAY)>11 && now.get(Calendar.HOUR_OF_DAY)<=12){
						message="对着花花小眯一会儿，下午工作更有精神哦！"+ message;
					}
					if(now.get(Calendar.HOUR_OF_DAY)>=13 && now.get(Calendar.HOUR_OF_DAY)<=16){
						message="下午好，来杯咖啡，对着桌上的花花发会儿呆吧！"+ message;
					}
					CustomServiceApi.sendText(openId, message);
				}
			}
		}
		return result;
	}
	
	public static boolean sendSeeds18(String openId,int aid){
		boolean result = false;
		//第18位签到
		Long count1=Db.queryLong("select count(1) from f_flowerseed where type='sign' and date_format(ctime,'%Y-%m-%d')=curdate() and id<= (select id from f_flowerseed where  type='sign' and date_format(ctime,'%Y-%m-%d')=curdate() and aid=? ORDER BY id  LIMIT 1)",aid);
		//并且没有发过奖励花籽
		Long count2=Db.queryLong("select count(1) from f_flowerseed where type='sign18' and date_format(ctime,'%Y-%m-%d')=curdate() and aid=? ",aid);
		if(count1==18&&count2==0){
			for(int i = 0; i<seedType.sign18.point; i++){
				Record seed = new  Record();
				seed.set("aid", aid);
				seed.set("send", 1);
				seed.set("type", seedType.sign18.name());
				seed.set("remarks", seedType.sign18.name);
				seed.set("ctime", new Date());
				result = Db.save("f_flowerseed", seed);
			}
			if (result) {
				// 绑定 apiConfig 到线程之上
				Calendar now = Calendar.getInstance(); 
				WeixinApiCtrl.setApiConfig();
				String message = "duang ! 今天太幸运了，成为了第18位签到大侠，被18粒花籽砸中！\n立即使用:<a href='" + Constant.getHost + "/service/getPrizeList'>【去抽奖吧】</a>";
				CustomServiceApi.sendText(openId, message);
		}
		}
		
		return result;
	}
	
	public static Long signinCount(int aid){
		Long count=Db.queryLong("select count(1) from f_flowerseed where type='sign' and date_format(ctime,'%Y-%m-%d')=curdate() and id<= (select id from f_flowerseed where  type='sign' and date_format(ctime,'%Y-%m-%d')=curdate() and aid=? ORDER BY id  LIMIT 1)",aid);
		/*System.out.print(aid);
		System.out.print(count);*/
		return count;//第几位签到的
	}
	/**
	 * 发花籽
	 * @return 
	 */
	public static void Addseeds(int aid,String openId) {
		for(int i = 0; i<seedType.signluck.point; i++){
			Record seed = new  Record();
			seed.set("aid", aid);
			seed.set("send", 1);
			seed.set("type", seedType.signluck.name());
			seed.set("remarks", seedType.signluck.name);
			seed.set("ctime", new Date());
			Db.save("f_flowerseed", seed);
		}
		WeixinApiCtrl.setApiConfig();
		String message = "恭喜您成为幸运签到花粉，额外送上8颗花籽。\n<a href='" + Constant.getHost + "/service/getPrizeList'>【去抽奖吧】</a>";
		CustomServiceApi.sendText(openId, message);
	}
	
	// 订单数量统计
	public static int[] orderCount(int aid){
		List<Record> list = Db.find("SELECT state FROM f_order WHERE aid = ?", aid);
		int a = 0;
		int b = 0;
		int c = 0;
		for(Record order : list){
			if(order.getInt("state")==orderState.STATE0.state){
				a++;
			}else if(order.getInt("state")==orderState.STATE1.state){
				b++;
			}else if(order.getInt("state")==orderState.STATE3.state){
				c++;
			}
		}
		int[] count = {a,b,c};
		return count;
	}
	
/*-----------------------------------------------------花票 ------------------------------------------------*/
	// 花票列表
	public static Map<String, Object> getCashList(int aid){
		//state:0未领取/1未使用/2已使用
		Map<String, Object> map = new HashMap<>();
		List<Record> list1 = Db.find("SELECT a.time_a,a.time_b,b.money,b.benefit,a.id,c.id 'cid',case b.ptid when 0 then '所有产品' else d.name end as name  FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id left join f_cashtheme as c on b.tid=c.id left join f_flower_pro d on b.ptid = d.id  where a.aid=? and a.state=1 and CURDATE()>=a.time_a AND CURDATE()<=a.time_b", aid);
		List<Record> list2 = Db.find("SELECT a.time_a,a.time_b,b.money,b.benefit,a.id,c.id 'cid',case b.ptid when 0 then '所有产品' else d.name end as name FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id left join f_cashtheme as c on b.tid=c.id left join f_flower_pro d on b.ptid = d.id where a.aid=? and a.state=2", aid);
		List<Record> list3 = Db.find("SELECT a.time_a,a.time_b,b.money,b.benefit,a.id,c.id 'cid',case b.ptid when 0 then '所有产品' else d.name end as name FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id left join f_cashtheme as c on b.tid=c.id left join f_flower_pro d on b.ptid = d.id where a.aid=? and a.state=1 and CURDATE()>a.time_b", aid);
		List<Record> list4 = Db.find("SELECT a.time_a,a.time_b,b.money,b.benefit,a.id,c.id 'cid',case b.ptid when 0 then '所有产品' else d.name end as name FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id left join f_cashtheme as c on b.tid=c.id left join f_flower_pro d on b.ptid = d.id where a.aid=? and a.state=1 and CURDATE()>=a.time_a AND CURDATE()<=a.time_b", aid);
		map.put("list1", list1);	// 1未使用
		map.put("list2", list2);	// 2已使用
		map.put("list3", list3);	// 3已过期
		map.put("list4", list4);	// 4花票分享
		return map;
	}
	// 
	/**
	 * 获得未使用的花票,
	 * f_cashclassify中onlyfirst=1表示：仅限首单可以使用
	 * @param aid
	 * @param totalprice
	 * @return
	 */
	public static List<Record> getCashAble(int aid, double totalprice,int pid){
		int isbuy=Db.queryInt("select isbuy from f_account where id=?",aid);//0未购买
		if(isbuy==0){
			return Db.find("SELECT a.id,a.time_a,a.time_b,b.money,b.benefit,b.ptid FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id "
					+ "where a.aid=? and b.benefit<=? and a.state=1 and b.ptid in(0,?) and CURDATE()>=a.time_a AND CURDATE()<=a.time_b order by b.money desc ", aid, totalprice,pid);
	
		}else{
			return Db.find("SELECT a.id,a.time_a,a.time_b,b.money,b.benefit,b.ptid FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id "
					+ "where a.aid=? and b.benefit<=? and a.state=1 and b.ptid in(0,?) and CURDATE()>=a.time_a AND CURDATE()<=a.time_b and b.onlyfirst!=1  order by b.money desc", aid, totalprice,pid);	
		}

	}
	// 获得未使用的优惠最大的花票
	/**
	 * 
	 * @param aid
	 * @param totalprice
	 * @param pid
	 * @return
	 */
	public static Record getMaxCash(int aid, double totalprice,int pid){
		int isbuy=Db.queryInt("select isbuy from f_account where id=?",aid);//0未购买
		if(isbuy==0){
			return Db.findFirst("SELECT a.id,a.time_a,a.time_b,b.money money,b.benefit,b.ptid FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id "
					+ "where a.aid=? and b.benefit<=? and a.state=1 and b.ptid in(0,?) and CURDATE()>=a.time_a AND CURDATE()<=a.time_b order by b.money desc ", aid, totalprice,pid);
	
			}else{
			 return Db.findFirst("SELECT a.id,a.time_a,a.time_b,b.money money,b.benefit,b.ptid FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id "
					+ "where a.aid=? and b.benefit<=? and a.state=1 and b.ptid in(0,?) and CURDATE()>=a.time_a AND CURDATE()<=a.time_b and b.onlyfirst!=1 order by b.money desc ", aid, totalprice,pid);
		}

	}
	/**
	 * 通过软文连接主动领取花票
	 * @param aid
	 * @param themeId
	 * @return
	 */
	public static List<Record> receiveCash(int aid,int themeId){
		
		List<Record> cashlist = Db.find("SELECT a.`name`,b.id as cid,b.money,b.benefit,c.name 'pname' FROM f_cashtheme a "
				+ " LEFT JOIN f_cashclassify b ON a.id=b.tid  "
				+ " left join f_flower_pro c on c.id=b.ptid"
				+ "  WHERE a.id=? and a.state=0",themeId);
		for(Record cash : cashlist){
			Record mycash = Db.findFirst("select id,code,state from f_cash where aid=? and cid=?", aid, cash.getInt("cid"));
			if(mycash == null){
				mycash = new Record();
				
				mycash.set("aid", aid);
				mycash.set("cid", cash.getInt("cid"));
				mycash.set("code", getCode());
				Db.save("f_cash", mycash);
				cash.set("state", 0);
				cash.set("id", mycash.getLong("id"));
			}else{
				cash.set("state", mycash.getInt("state"));
				cash.set("id", mycash.getInt("id"));
			}
			cash.set("pname", cash.getStr("pname"));
			cash.set("code", mycash.getStr("code"));
		}
		return cashlist;
	}
	// 我的种植花束
	public static Map<String, Object> getFlowerSeed(int aid){
		Map<String, Object> map = new HashMap<>();
		Number number = Db.queryNumber("SELECT count(1) FROM f_flowerseed WHERE aid=?  and state = 0", aid);
		int count = number.intValue();	// 花籽总数
		int a = 0;	// 花籽
		int b = 0;	// 花苗	10个花籽
		int c = 0;	// 花束	40个花籽
		if(count > 0){
			c = count/40;
			b = (count%40)/10;
			a = count - c*40 - b*10;
		}
		map.put("a", a);
		map.put("b", b);
		map.put("c", c);
		map.put("count", count);
		return map;
	}
	public static int getFlowerSeed_new(int aid){
		Map<String, Object> map = new HashMap<>();
		Number number = Db.queryNumber("SELECT count(1) FROM f_flowerseed WHERE aid=?  and state = 0", aid);
		int count = number.intValue();	// 花籽总数
		return count;
	}
	
	public static int getSeedCount(int fpid){
		return Db.queryInt("select seeds from f_flower_pro where  id=?",fpid);
	}
	
	public static int getSeedCount(String ordercode){
		return Db.queryInt("select seeds from f_order_detail where ordercode=? and type=1",ordercode);
	}
	
	/*-----------------------------------------------------个人信息------------------------------------------------*/
    // 修改个人信息
	public static Map<String, Object> changePersonInfo( String birthday, int sex, HttpSession session) {
		Map<String, Object> responseMap = new HashMap<>();
		String msg = new String();
		boolean result = false;
		Record account = (Record) session.getAttribute("account");
		
		int aid = account.getInt("id");
		Date oldBirth = Db.queryDate("SELECT birthday FROM f_account WHERE id = ?",aid);
		int oldSex = Db.queryInt("SELECT sex FROM f_account WHERE id = ?",aid);
		boolean b = birthday.equals("未填写");
		if (oldBirth == null || oldSex == 0) {
	       
			if (!b && sex != 0) {
				//送花籽 并发送通知
				for(int i = 0; i<seedType.fillInformation.point; i++){
					Record seed = new  Record();
					seed.set("aid", aid);
					seed.set("send", 1);
					seed.set("type", seedType.fillInformation.name());
					seed.set("remarks", seedType.fillInformation.name);
					seed.set("ctime", new Date());
					result = Db.save("f_flowerseed", seed);
				}
				if (result) {
					WeixinApiCtrl.setApiConfig();
					String message = "完善信息成功，特意送上送上2颗花籽。立即使用:<a href='" + Constant.getHost + "/account/flowerseed'>【花籽兑换】</a>";
					CustomServiceApi.sendText(account.getStr("openid"), message);
				}
			}
			
		}
		
		
		if (!b) {
			String dateString = birthday.replace(',', '-');
			dateString+=" ";
			Date birth = new Date();
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
				birth = sdf.parse(dateString);
				account.set("birthday", birth);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}
		}
		
		
		account.set("sex", sex);
		
		result = Db.update("f_account", account);
		if (result) {
			msg = "保存成功！";
		}else {
			msg = "保存失败！";
		}
		
		responseMap.put("result", result);
		responseMap.put("msg", msg);
		return responseMap;
	}
	// 保存绑定号码
	public static Map<String, Object> saveBinding(String number, String msgcode, String bindingcode, HttpSession session){
		Map<String, Object> responseMap = new HashMap<>();
		Record account = (Record) session.getAttribute("account");
		boolean result = false;
		String msg = new String();
		if(bindingcode == null){
			msg = "验证码错误";
		}else{
			if(bindingcode.equals(msgcode)){
				if(account.getStr("tel") == null){
					boolean R = false;
					for(int i = 0; i<seedType.binding.point; i++){
						Record seed = new  Record();
						seed.set("aid", account.get("id"));
						seed.set("send", 1);
						seed.set("type", seedType.binding.name());
						seed.set("remarks", seedType.binding.name);
						seed.set("ctime", new Date());
					    R = Db.save("f_flowerseed", seed);
					}
					if (R) {
						WeixinApiCtrl.setApiConfig();
						String openId = account.getStr("openid");
						String message = "绑定成功，特意送上2颗花籽。立即使用:<a href='" + Constant.getHost + "/account/flowerseed'>【花籽兑换】</a>";
				 	    CustomServiceApi.sendText(openId, message);
					}
				}
				account.set("tel", number);
				account.set("isfans", 0);
				result = Db.update("f_account", account);
				msg = "保存成功";
			}else{
				msg = "验证码错误";
			}
		}
		responseMap.put("result", result);
		responseMap.put("msg", msg);
		return responseMap;
	}
	// 提交反馈
	public static boolean saveFeedback(String title, String info, int id){
		Record record = new Record();
		record.set("aid", id);
		record.set("title", title);
		record.set("info", info);
		record.set("ctime", new Date());
		return Db.save("f_feedback", record);
	}
	//提交评价
	public static boolean saveEvaluate(int aid, String openId, String id, int point, String descripte,String lognumber){
		Record record = new Record();
		record.set("ordercode", id);
		record.set("point", point);
		record.set("descripte", descripte);
		record.set("ctime", new Date());
		record.set("number",lognumber);
		//Db.update("update f_order set state = 3 where ordercode = ?", id);
		boolean R = Db.save("f_comment", record);
		//赠送花籽  发送提醒消息
		if (R) {
			for(int i=0;i<seedType.comment.point;i++){
				Record seed = new  Record();
				seed.set("aid", aid);
				seed.set("send", 1);
				seed.set("type", seedType.comment.name());
				seed.set("remarks", seedType.comment.name);
				seed.set("ctime", new Date());
				R = Db.save("f_flowerseed", seed);
			}
			if (R) {
				WeixinApiCtrl.setApiConfig();
				String message = "感谢您的评价，特意送上1颗花籽。立即使用:<a href='" + Constant.getHost + "/account/flowerseed'>【花籽兑换】</a>";
		 	    CustomServiceApi.sendText(openId, message);
			}
		}
		return R;
	}
	// 生活美学
	public static Page<Record> getEstheticsPage(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select id,title,imgurl,DATE_FORMAT(ctime,'%Y-%m-%d') as ctime", "FROM f_esthetics order by istop desc,id desc");
	}
	// 获取活动信息
	public static Record getActivity(Double price){
		return Db.findFirst("SELECT id,name,money,benefit FROM f_activity WHERE time_a < NOW() AND time_b > NOW() AND state=0 AND money <= ? ORDER BY money desc", price);
	}
	// 获取活动信息集合
	public static List<Record> getActivityList(){
		return Db.find("SELECT id,name,money,benefit FROM f_activity WHERE time_a < NOW() AND time_b > NOW() AND state=0 ORDER BY money desc");
	}
	// 计算订单总额
	public static double countPrice(double price, int cycle, Integer vaseid, String adds){
		double price1 = price * cycle;
		if(vaseid != null&&vaseid!=0){
			price1 += Db.queryDouble("select price from f_flower_pro where id = ?", vaseid);
		}
		// 加购商品
		if (adds != null && (!adds.equals(""))) {
			String[] addPids = adds.split("-");
			for (int i = 0; i < addPids.length; i++) {
				double addPrice = Db.queryDouble("SELECT price FROM f_flower_pro WHERE id = ?", addPids[i]);
				price1 += addPrice;
			}
		}
		return price1;
	}
	
	/*-----------------------------------------------------拼团------------------------------------------------*/
	/**
	 * 创建新团
	 * @param aid 会员id 关联f_account
	 * @param fptid 商品id 关联f_flower_pro
	 * @param cycle 订阅次数
	 * @return 团号
	 */
	public static long CreateTuan(int aid,int fptid,int cycle){
		
		Record flowerpro=Db.findFirst("select ptCount, ptHours,ptPrice from f_flower_pro where id=?",fptid);
		//团单头
		Record f_pingtuan=new Record();
		f_pingtuan.set("aid", aid);
		f_pingtuan.set("needCount", flowerpro.getInt("ptCount"));//几人成团
		f_pingtuan.set("maxhours", flowerpro.getInt("ptHours"));//多少小时之后结束
		f_pingtuan.set("price", flowerpro.getDouble("ptPrice"));//当时的拼团单价
		f_pingtuan.set("fptid", fptid);
		f_pingtuan.set("cycle", cycle);//订阅次数
		Db.save("f_pingtuan", f_pingtuan);
		
		long ptNo=f_pingtuan.getLong("id");//自动生成的团号

		return ptNo;
	} 
	
	
	/**
	 * 获取拼团信息
	 * @author Glacier
	 * @date 2017年8月21日
	 * @return
	 */
	public static Record getPinTuan(int ptNo) {
		return Db.findFirst("SELECT a.ptNo,a.aid,a.ptTimeS,a.ptTimeE,a.needCount,a.hadCount,a.state,a.fptid,a.cycle,a.price,b.ptPrice,b.price 'yprice',b.ptNum, "
				+ "b.id,b.`name`,b.imgurl,b.itinfo4,b.`describe`,b.state 'pro_state',b.allownew from f_pingtuan a LEFT JOIN f_flower_pro b ON a.fptid = b.id where ptNo = ? ", ptNo);
	}
	
	
	/**
	 * 根据用户id获取拼团信息
	 * @author Glacier
	 * @date 2017年8月28日
	 * @param userid
	 * @return
	 */
	public static List<Record> getPintuanlist(String sql) {

		return Db.find("SELECT a.ptNo,a.aid,a.ptTimeS,a.ptTimeE,a.needCount,a.hadCount,a.state,a.fptid,"
				+ "a.cycle,a.price,b.id,b.`name`,b.imgurl,b.itinfo4,b.`describe`,b.state "
				+ "from f_pingtuan a "
				+ "LEFT JOIN f_flower_pro b ON a.fptid = b.id "
				+ "LEFT JOIN f_pingtuan_detail c on a.ptNo = c.ptNo " +sql
				);

	}
	
/*-----------------------------------------------------订单 ------------------------------------------------*/
	
	/**  
	 *   创建订单-订阅 -送花 -节日-拼团
	 * @param request
	 * @param session
	 * @param pid 商品id
	 * @param vase 花瓶
	 * @param addressid 地址id	
	 * @param reach  送达时间
	 * @param cycle  订阅次数
	 * @param zhouqi  周期
	 * @param use  用途
	 * @param style  格调
	 * @param zhufu  祝福语 
	 * @param songhua  送花人
	 * @param jh_list  忌讳的花
	 * @param jhcolor_list  忌讳的颜色
	 * @param type 类型
	 * @param recommend
	 * @param szdx 适赠对象
	 * @param cash  花票
	 * @param activity  活动
	 * @param yh 优惠
	 * @param festivalId 节日id
	 * @return map
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Map<String, Object> createOrder(HttpServletRequest request, HttpSession session, int pid,
			Integer vase, int addressid, int reach, int cycle, int zhouqi, int use, int style, String zhufu,
			String songhua, String jh_list, String jhcolor_list, int type, String recommend, Integer szdx, Integer cash,
			Integer activity, Double yh, int festivalId, String adds,String jr_picode,Integer cash2,int ptNo,int num,Integer picId,int jsAid)
			throws ParserConfigurationException, IOException, SAXException {

		Pid = pid;
		Vase = vase;
		addressId = addressid;
		Reach = reach;

		// 周期 用途 格调
		Zhouqi = zhouqi;
		Use = use;
		Style = style;

		//节日id
		FestivalId = festivalId;
		//加购商品 列表
		Adds = adds;
		
		Cycle = cycle;
		
		Zhufu = zhufu;
		Songhua = songhua;
		PicId=picId;
		
		jh_List = jh_list;
		jhColor_List = jhcolor_list;
		Type = type;
		Recommend = recommend;
		Szdx = szdx;
		Cash = cash;
		Cash2=cash2;
		Activity = activity;
		Yh = yh;
		PtNo=ptNo;
		JsAid=jsAid;
		Num=num;

		resultMap = new HashMap<>();
		Account = (Record) session.getAttribute("account");
		pro_Flower = Db.findById("f_flower_pro", pid);
		isBuy = Account.getInt("isbuy");
		sy_code=jr_picode;//节日订单的发货批号
		Order = new Record(); // 订单对象
		boolean R = false;
		
		R = Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean result = false;
				boolean isHead=false;
				int ptid=Db.queryInt("select ptid from f_flower_pro where id=?",Pid);
				if(PtNo==0){
					if(ptid==202){
						PtNo=CreateTuan(Account.getInt("id"),Pid,Cycle);//团号
					}
					isHead=Db.queryLong("select count(1) from f_pingtuan_detail where ptNo=?",PtNo)>=1?false:true;
				}
				
				
				Map<String, Object> priceMap = countPriceForOrder(Account.getInt("id"), Pid, Cycle, Vase, Cash,
						Activity, Yh,Cash2,Num,isHead);
				
				
				
				Order.set("ptNo", PtNo);
				Order.set("jsAid", JsAid);
				if(JsAid!=0){
					Order.set("jsMoney", priceMap.get("jsMoney"));
				}
				
				Order.set("ordercode", getOrderCode());
				Order.set("aid", Account.getInt("id"));
				Record address = Db.findById("f_address", addressId);
				Order.set("name", address.getStr("name")); // 收货人姓名
				Order.set("tel", address.getStr("tel")); // 收货人电话
				Order.set("addr", getAddressArea(address.getStr("area")) + address.getStr("addr")); // 收货人地址
				Order.set("reach", Reach); // 送达时间(1周一/2周六)

				// 周期 用途 格调
				Order.set("sendCycle", Zhouqi);
				Order.set("app", Use);
				Order.set("style", Style);
				Order.set("sy_code", sy_code);//节日订单的发货批号、用户指定的首次收花日期

				Order.set("price", (double) priceMap.get("price")); // 总价
				Order.set("fund", (double) priceMap.get("fund"));//公益基金
				
				if ((double) priceMap.get("price") <= 0) {
					return result;
				}
				// 订单存入商品名称
				String gName = Db.queryStr("SELECT name FROM f_flower_pro WHERE id = ?", Pid);
				//周边的加上件数
				if(Type==3){
					gName =gName+ Num+"件";
				}
				
				//花瓶
				if (Vase != null&&Vase!=0) {
					Order.set("isvase", 1); // 购买花瓶
				}
				if (Vase != null&&Vase!=0) {
					String vaseName = Db.queryStr("SELECT name FROM f_flower_pro WHERE id = ?", Vase);
					gName = gName + "-" + vaseName;

				}
				//加购商品
				String[] addPids = Adds.split("-");
				if (!Adds.equals("")) {
					for (int i = 0; i < addPids.length; i++) {
						Record pro_add = Db.findById("f_flower_pro", addPids[i]);
						double sumPrice = Order.getDouble("price") + pro_add.getDouble("price");
						gName = gName + "-" + pro_add.getStr("name");
						Order.set("price", sumPrice);
					}
					Order.set("isvase", 1); // 购买花瓶
				}
				if(Adds.equals("-")){
					Order.set("isvase", 0);
				}
				
				Order.set("gName", gName);
				Order.set("cycle", Cycle);
	
				Order.set("picId", PicId);//祝福卡ID号
				Order.set("zhufu", "".equals(Zhufu) ? null : Zhufu); // 祝福卡
				Order.set("songhua", "".equals(Songhua) ? null : Songhua); // 赠花人
				Order.set("jh_list", "".equals(jh_List) ? null : jh_List); // 忌讳的花
				Order.set("jh_color", "".equals(jhColor_List) ? null : jhColor_List); // 忌讳的色系
				if (!"".equals(jh_List)) {
					Order.set("jihui",
							Db.queryStr("select group_concat(NAME) from f_flower_type where id in (" + jh_List + ")"));// 忌讳的花材分类
				}
				if (!"".equals(jhColor_List)) {
					Order.set("color",
							Db.queryStr("select group_concat(NAME) from f_color where id in (" + jhColor_List + ")"));// 忌讳的色系
				}
				Order.set("yhje", priceMap.get("benefit")); // 优惠金额
				Order.set("yhfs", priceMap.get("yhfs")); // 优惠方式
				Order.set("ctime", new Date()); // 下单日期
				Order.set("type", Type); // 商品类型
				if (Type == 4) {
					Order.set("state", 1); // 兑换的订单直接为服务中
				}
				Order.set("szdx", Szdx); // 适赠对象ID
				
				//测试将价格改为0
//				Order.set("price", (double)0.01);
				// 保存订单
				result = Db.save("f_order", Order);
				if (result) {
					//拉新商品,记录下已经用的被代颜人
					if(ptid==10){
						String tjid="6_"+Account.getInt("id");
						List<Record> list=Db.find("select id from f_account where tjid=?  and id not in(select aid from f_redpackets_log) LIMIT 10",tjid);
						for (Record record : list) {
							Record f_redpackets_log=new Record();
			    			f_redpackets_log.set("aid", record.getInt("id"));
			    			f_redpackets_log.set("optime", new Date());
			    			Db.save("f_redpackets_log", f_redpackets_log);
							
						}
					}
					
					
					//更新用户忌讳花类和色系
					if(Type==1){
						Db.update(" update f_account set jhflower=?,jhcolor=? where id=?","".equals(jh_List) ? "0" : jh_List,"".equals(jhColor_List) ? "0" : jhColor_List,Account.getInt("id"));
					}
					
					
					//花票使用之后，绑定的订单号
					if(Cash!=null){
						Db.update("update f_cash set oid=? where id=?",Order.getStr("ordercode"),Cash);
					}
					if(Cash2!=null){
						Db.update("update f_cash set oid=? where id=?",Order.getStr("ordercode"),Cash2);
					}
					
					// 订单订购明细-主件(花束or花边好物)
					Record order_detail_flower = new Record();
					order_detail_flower.set("ordercode", Order.getStr("ordercode"));
					order_detail_flower.set("fpid", Pid);
					order_detail_flower.set("price", (ptid==202)?pro_Flower.getDouble("ptPrice"):pro_Flower.getDouble("price"));
					order_detail_flower.set("type", 1);
					order_detail_flower.set("num", Num);
					result = Db.save("f_order_detail", order_detail_flower);

					if (Vase != null&&Vase!=0) {
						Record pro_vase = Db.findById("f_flower_pro", Vase);
						// 订单订购明细-附件(花瓶)
						Record order_detail_vase = new Record();
						order_detail_vase.set("ordercode", Order.getStr("ordercode"));
						order_detail_vase.set("fpid", Vase);
						order_detail_vase.set("price", pro_vase.getDouble("price"));
						order_detail_vase.set("type", 2);
						result = Db.save("f_order_detail", order_detail_vase);
					}
					//加购商品
					if (!Adds.equals("")) {
						for (int i = 0; i < addPids.length; i++) {
							Record pro_add = Db.findById("f_flower_pro", addPids[i]);
							
							Record order_detail_adds = new Record();
							order_detail_adds.set("ordercode", Order.getStr("ordercode"));
							order_detail_adds.set("fpid", addPids[i]);
							order_detail_adds.set("price", pro_add.getDouble("price"));
							order_detail_adds.set("type", 2);
							result = Db.save("f_order_detail", order_detail_adds);
						}
					}
				}
				return result;
			}
		});
		if (R) {
			List<Record> detaillist = Db.find(
					"SELECT a.cycle,a.price AS totalprice,c.imgurl,c.name,b.price,b.type,b.num,a.type 'ordertype' "
					+ "FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id "
					+ "WHERE a.ordercode=?",
					Order.getStr("ordercode"));
			// 微信统一下单
			String xml = FCDao.wxPushOrder(pro_Flower.getStr("name"), Order.getStr("ordercode"),
					Order.getDouble("price"), getRemortIP(request), Account.getStr("openid"));
			resultMap = XMLParser.getMapFromXML(xml);
			for (Record detail : detaillist) {
				String imgurl = detail.getStr("imgurl");
				if (imgurl.indexOf("-") > 0) {
					String[] img = imgurl.split("-");
					detail.set("imgurl", img[0]);
				}
			}
			resultMap.put("detaillist", detaillist);
			resultMap.put("ordercode", Order.getStr("ordercode"));
			resultMap.put("ptNo", Order.getLong("ptNo"));
		} else {
			Account.set("isbuy", isBuy);
		}
		resultMap.put("result", R);
		return resultMap;
	}

	// 创建定制鲜花订单
	public static Map<String, Object> createOrder(HttpServletRequest request, HttpSession session, int pid,
			Integer vase, int addressid, int reach, int cycle, int zhouqi, int use, int style, String zhufu,
			String songhua, String jh_list, String jhcolor_list, int type, String recommend, Integer szdx, Integer cash,
			Integer activity, Double yh, String fpid, String adds,Integer cash2,Integer picId) throws ParserConfigurationException, IOException, SAXException {

		Pid = pid;
		Vase = vase;
		addressId = addressid;
		Reach = reach;

		// 周期 用途 格调
		Zhouqi = zhouqi;
		Use = use;
		Style = style;

		// 定制鲜花
		Fpid = fpid;
		Adds = adds;
		
		Cycle = cycle;
		
		Zhufu = zhufu;
		Songhua = songhua;
		jh_List = jh_list;
		jhColor_List = jhcolor_list;
		Type = type;
		Recommend = recommend;
		Szdx = szdx;
		Cash = cash;
		Cash2=cash2;
		Activity = activity;
		Yh = yh;
		PicId=picId;

		resultMap = new HashMap<>();
		Account = (Record) session.getAttribute("account");
		pro_Flower = Db.findById("f_flower_pro", pid);
		isBuy = Account.getInt("isbuy");
		Order = new Record(); // 订单对象
		boolean R = false;
		
		R = Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean result = false;

				Map<String, Object> priceMap = countPriceForOrder(Account.getInt("id"), Pid, Cycle, Vase, Cash,
						Activity, Yh,Cash2,1,false);
				Order.set("ordercode", getOrderCode());
				Order.set("aid", Account.getInt("id"));
				Record address = Db.findById("f_address", addressId);
				Order.set("name", address.getStr("name")); // 收货人姓名
				Order.set("tel", address.getStr("tel")); // 收货人电话
				Order.set("addr", getAddressArea(address.getStr("area")) + address.getStr("addr")); // 收货人地址
				Order.set("reach", Reach); // 送达时间(1周一/2周六)

				// 周期 用途 格调
				Order.set("sendCycle", Zhouqi);
				Order.set("app", Use);
				Order.set("style", Style);

				Order.set("price", (double) priceMap.get("price")); // 总价
				if ((double) priceMap.get("price") <= 0) {
					return result;
				}
				Order.set("fund", (double) priceMap.get("fund"));//公益基金
				Order.set("cycle", Cycle); // 周期
				
				
				if (Vase != null) {
					Order.set("isvase", 1); // 购买花瓶
				}
				Order.set("picId", PicId);//祝福卡ID号
				Order.set("zhufu", "".equals(Zhufu) ? null : Zhufu); // 祝福卡
				Order.set("songhua", "".equals(Songhua) ? null : Songhua); // 赠花人
				Order.set("jh_list", "".equals(jh_List) ? null : jh_List); // 忌讳的花
				Order.set("jh_color", "".equals(jhColor_List) ? null : jhColor_List); // 忌讳的花
				if (!"".equals(jh_List)) {
					Order.set("jihui",
							Db.queryStr("select group_concat(NAME) from f_flower_type where id in (" + jh_List + ")"));// 忌讳的花材分类
				}
				if (!"".equals(jhColor_List)) {
					Order.set("color",
							Db.queryStr("select group_concat(NAME) from f_color where id in (" + jhColor_List + ")"));// 忌讳的色系
				}
				Order.set("yhje", priceMap.get("benefit")); // 优惠金额
				Order.set("yhfs", priceMap.get("yhfs")); // 优惠方式
				Order.set("ctime", new Date()); // 下单日期
				Order.set("type", Type); // 商品类型(订阅/赠送/周边/兑换)
				if (Type == 4) {
					Order.set("state", 1); // 兑换的订单直接为服务中
				}
				Order.set("szdx", Szdx); // 适赠对象ID
				
				// 订单存入商品名称
				String gName = "定制花束";
				//主商品
				String[] fpids = Fpid.split("-");
				for (int i = 0; i < fpids.length; i++) {
					String pName = Db.queryStr("SELECT name FROM f_flower_pro WHERE id = ?", fpids[i]);
					gName = gName+ "-" + pName;
				}
				//花瓶
				if (Vase != null) {
					String vaseName = Db.queryStr("SELECT name FROM f_flower_pro WHERE id = ?", Vase);
					gName = gName + "-" + vaseName;

				}
				//加购商品
				String[] addPids = Adds.split("-");
				if (!Adds.equals("")) {
					for (int i = 0; i < addPids.length; i++) {
						Record pro_add = Db.findById("f_flower_pro", addPids[i]);
						double sumPrice = Order.getDouble("price") + pro_add.getDouble("price");
						gName = gName + "-" + pro_add.getStr("name");
						Order.set("price", sumPrice);
					}
					Order.set("isvase", 1); // 购买花瓶
				}
							
				Order.set("gName", gName);

				// 保存订单
				result = Db.save("f_order", Order);
				if (result) {
					// 订单详情
					for (int i = 0; i < fpids.length; i++) {
	
						// 订单订购明细-主件(花束or花边好物)
						Record order_detail_flower = new Record();
						order_detail_flower.set("ordercode", Order.getStr("ordercode"));
						order_detail_flower.set("fpid", fpids[i]);
						order_detail_flower.set("price", pro_Flower.getDouble("price"));
						order_detail_flower.set("type", 1);  
						order_detail_flower.set("isAllot", 1); 
						result = Db.save("f_order_detail", order_detail_flower);
					}
					

					if (Vase != null) {
						Record pro_vase = Db.findById("f_flower_pro", Vase);
						// 订单订购明细-附件(花瓶)
						Record order_detail_vase = new Record();
						order_detail_vase.set("ordercode", Order.getStr("ordercode"));
						order_detail_vase.set("fpid", Vase);
						order_detail_vase.set("price", pro_vase.getDouble("price"));
						order_detail_vase.set("type", 2);
						result = Db.save("f_order_detail", order_detail_vase);
					}
					
					//加购商品
					if (!Adds.equals("")) {
						for (int i = 0; i < addPids.length; i++) {
							Record pro_add = Db.findById("f_flower_pro", addPids[i]);
							
							Record order_detail_adds = new Record();
							order_detail_adds.set("ordercode", Order.getStr("ordercode"));
							order_detail_adds.set("fpid", addPids[i]);
							order_detail_adds.set("price", pro_add.getDouble("price"));
							order_detail_adds.set("type", 2);
							result = Db.save("f_order_detail", order_detail_adds);
						}
					}
				}
				return result;
			}
		});
		if (R) {
			List<Record> detaillist = Db.find(
					"SELECT a.cycle,a.price AS totalprice,c.imgurl,c.name,c.ptid,b.price,b.type,b.num,a.type 'ordertype' FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id WHERE a.ordercode=?",
					Order.getStr("ordercode"));
			// 微信统一下单
			String xml = FCDao.wxPushOrder(pro_Flower.getStr("name"), Order.getStr("ordercode"),
					Order.getDouble("price"), getRemortIP(request), Account.getStr("openid"));
			resultMap = XMLParser.getMapFromXML(xml);
			for (Record detail : detaillist) {
				String imgurl = detail.getStr("imgurl");
				if (imgurl.indexOf("-") > 0) {
					String[] img = imgurl.split("-");
					detail.set("imgurl", img[0]);
				}
				if (detail.getInt("ptid") == 201) {
					detail.set("cycle", 1);
				}
			}
			resultMap.put("detaillist", detaillist);
			resultMap.put("ordercode", Order.getStr("ordercode"));
			resultMap.put("ptNo", 0);//团号默认为0
		} else {
			Account.set("isbuy", isBuy);
		}
		resultMap.put("result", R);
		return resultMap;
	}
	

	/**
	 * 创建花籽兑换订单
	 * @param request
	 * @param session
	 * @param fpid
	 * @param addressid
	 * @param reach
	 * @param jh_list
	 * @param jhcolor_list
	 * @param cycle
	 * @param type
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Map<String, Object> exccreateOrder_new(HttpServletRequest request,final HttpSession session, final int fpid, final int addressid, final int reach, final String jh_list, final String jhcolor_list, final int cycle,final int type) throws ParserConfigurationException, IOException, SAXException{
		
		boolean R=false;
		Order = new Record(); // 订单对象
		R = Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean result = false;
				if(type!=41 && type!=43){
					result=false;
					return result;//类型不对，不创建订单
				}
				Account = (Record) session.getAttribute("account");
				int yh_seeds=Db.queryInt("select b.seeds from f_account a left join f_update_grade b on a.grade=b.grade where a.id=?",Account.getInt("id"));//会员优惠的花籽
				pro_Flower = Db.findById("f_flower_pro", fpid);
				
				Order.set("ordercode", getOrderCode());	
				Order.set("aid", Account.get("id"));
				Order.set("price", pro_Flower.get("price"));
				
				//存入商品名称
				Order.set("gName", pro_Flower.get("name"));
		        
				Record address = Db.findById("f_address", addressid);
				Order.set("name", address.getStr("name"));	// 收货人姓名
				Order.set("tel", address.getStr("tel"));	// 收货人电话
				Order.set("addr", getAddressArea(address.getStr("area")) + address.getStr("addr"));	// 收货人地址
				Order.set("reach", reach);		// 送达时间(1周一/2周六)
				Order.set("jh_list", "".equals(jh_list)?null:jh_list);	// 忌讳的花
				Order.set("jh_color", "".equals(jhcolor_list)?null:jhcolor_list);	// 忌讳的色系
				if(!"".equals(jh_list)){
					Order.set("jihui", Db.queryStr("select group_concat(NAME) from f_flower_type where id in ("+jh_list+")"));// 忌讳的花材分类
				}
				if(!"".equals(jhcolor_list)){
					Order.set("color", Db.queryStr("select group_concat(NAME) from f_color where id in ("+jhcolor_list+")"));// 忌讳的色系
				}
				Order.set("cycle", cycle);		// 周期
				Order.set("ctime", new Date());	// 下单日期
				Order.set("type", type);		// 商品类型(兑换)
				Order.set("state", 0);		 
				int[] yhfs = {0,0};
				Order.set("yhfs",  yhfs[0]+","+yhfs[1]);   //兑换订单无优惠方式
				
				// 保存订单
				result=Db.save("f_order", Order);
				if(result){
					Record order_detail_flower = new Record();
					order_detail_flower.set("ordercode", Order.getStr("ordercode"));
					order_detail_flower.set("fpid", fpid);
					order_detail_flower.set("price", pro_Flower.getDouble("price"));
					order_detail_flower.set("type", 1);
					order_detail_flower.set("seeds", pro_Flower.getInt("seeds")-yh_seeds);
					result=Db.save("f_order_detail", order_detail_flower);
				}
				return result;
				
			}});
		if (R) {
			List<Record> detaillist = Db.find(
					"SELECT a.cycle,a.price AS totalprice,c.imgurl,c.name,c.ptid,b.price,b.type FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id WHERE a.ordercode=?",
					Order.getStr("ordercode"));
			// 微信统一下单
			String xml = FCDao.wxPushOrder(pro_Flower.getStr("name"), Order.getStr("ordercode"),
					Order.getDouble("price"), getRemortIP(request), Account.getStr("openid"));
			resultMap = XMLParser.getMapFromXML(xml);
			for (Record detail : detaillist) {
				String imgurl = detail.getStr("imgurl");
				if (imgurl.indexOf("-") > 0) {
					String[] img = imgurl.split("-");
					detail.set("imgurl", img[0]);
				}
			}
			resultMap.put("detaillist", detaillist);
			resultMap.put("ordercode", Order.getStr("ordercode"));
			resultMap.put("ptNo", 0);
		} 
		resultMap.put("result", R);
		return resultMap;
		
	}
	
	
	
	/**
	 * 下单应付总额
	 * @param aid
	 * @param pid
	 * @param cycle
	 * @param vaseid
	 * @param cash 用户选择的花票ID
	 * @param activity
	 * @param yh 多买立减优惠
	 * @param cash2 通过兑换码使用的花票ID
	 * @param num 数量，用于周边
	 * @param isHead 是否是团长
	 * @return
	 */
	public static Map<String, Object> countPriceForOrder(int aid, int pid, int cycle, Integer vaseid, Integer cash, Integer activity, Double yh,Integer cash2,int num,Boolean isHead){
		Map<String, Object> map = new HashMap<>();
		Record rd=Db.findFirst("select price * ?*? 'priceTotal',ptPrice*?*? 'ptPriceTotal',ptid,isptFree,fundper,jsMoney from f_flower_pro where id = ?", cycle,num,num,cycle, pid);
		BigDecimal price= new BigDecimal(0.00);
		if(rd.getInt("ptid")==202){
			price= new BigDecimal(rd.getDouble("ptPriceTotal"));//拼团鲜花总价
		}else {
			price= new BigDecimal(rd.getDouble("priceTotal"));//普通鲜花总价
		}
		
		if(rd.getInt("isptFree")==1 && isHead==true){
			//0：不免单（默认）1：免单，支付0.01元
			price=new BigDecimal(0.01);
		}
		/*BigDecimal price = new BigDecimal(Db.queryDouble("select price * ?,ptPrice*?,ptid from f_flower_pro where id = ?", cycle, pid));//鲜花总价*/
		BigDecimal benefit = new BigDecimal(0.00);//优惠价格
		int[] yhfs = {0,0,0};
		if(vaseid != null&& vaseid!=0){
			BigDecimal vaseprice = new BigDecimal(Db.queryDouble("select price from f_flower_pro where id = ?", vaseid));
			price = price.add(vaseprice);//总价=鲜花总价+花瓶金额
		}
		yh = yh==null?0.00:yh;
		price = price.subtract(new BigDecimal(yh));//总价=原总价-多买立减优惠
		benefit=benefit.add(new BigDecimal(yh));
		if(activity != null){
			BigDecimal activity_money = new BigDecimal(Db.queryDouble("SELECT benefit FROM f_activity WHERE id=?", activity));
			benefit = benefit.add(activity_money);
			yhfs[0] = activity;
		}
		//用户选择的符合条件的花票
		if(cash != null){
			Record mycash = Db.findFirst("SELECT money FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id WHERE a.aid=? AND a.id=? AND a.state=1", aid, cash);
			if(mycash != null){
				Db.update("update f_cash set state = 2 where id = ?", cash);
				price = price.subtract(new BigDecimal(mycash.getDouble("money")));
				benefit = benefit.add(new BigDecimal(mycash.getDouble("money")));
				yhfs[1] = cash;
			}
		}
		//用户输入兑换码使用的花票，与滴滴打车合作
		if(cash2 != null){
			Record mycash = Db.findFirst("SELECT money FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id WHERE a.id=? AND a.state=1 and aid=0", cash2);
			if(mycash != null){
				Db.update("update f_cash set state = 2,aid=?,uTime=NOW() where id = ?", aid,cash2);
				price =price.subtract(new BigDecimal(mycash.getDouble("money")));
				benefit = benefit.add(new BigDecimal(mycash.getDouble("money")));
				yhfs[2] = cash2;
			}
		}
		Double price_d = price.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		Double benefit_d = benefit.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		map.put("price", price_d<=0?0.01:price_d);				// 总额
		map.put("benefit", benefit_d);			// 优惠金额
		BigDecimal fund= new BigDecimal((price_d<=0?0.01:price_d)*rd.getDouble("fundper")/100);
		map.put("fund",fund.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() );//公益基金
		map.put("jsMoney", rd.getDouble("jsMoney"));
		map.put("yhfs", yhfs[0]+","+yhfs[1]+"-"+yhfs[2]);	// 优惠方式
		return map;
	}
	
	// 生成订单编号
	public static String getOrderCode(){
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return sdf.format(now.getTime());
	}

	// 获取收货地址区域
	public static String getAddressArea(String area){
		List<Record> arealist = Db.find("select name from f_area where id in (" + area + ") ORDER BY FIELD (id, " + area + ")");
		String areaname = new String();
		for(Record name:arealist){
			areaname += name.getStr("name")+"-";
		}
		return areaname;
	}
	
	/**
	 * 我的订单
	 * 交易取消：state=5的订单不显示
	 * @param pageno
	 * @param pagesize
	 * @param aid
	 * @param state
	 * @return
	 */
	public static Page<Record> getMyOrder(int pageno, int pagesize, int aid, Integer state){
		String select = "SELECT e.state as lstate ,e.ofcycle, a.ishas,a.ordercode,a.yhfs,b.fpid,c.ptid,c.name,c.imgurl,b.price,a.cycle,a.reach,a.isvase,a.price AS totalprice,a.state,a.ctime,d.money,d.state as rstate,a.type,a.ocount,a.sendCycle,b.num ";
		String sqlExceptSelect = "FROM f_order a left join f_order_info e on e.ordercode = a.ordercode LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id left join f_refund d on a.ordercode=d.ordercode WHERE a.aid=? AND b.type=1 and a.state<>5";
		Page<Record> page;
		if(state == 9){
			sqlExceptSelect += " order by a.id desc";
			page = Db.paginate(pageno, pagesize, select, sqlExceptSelect, aid);
		}else{
			if (state==2) {
				sqlExceptSelect = "FROM f_order_info e left join f_order a on e.ordercode = a.ordercode LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id left join f_refund d on a.ordercode=d.ordercode WHERE a.aid=? AND b.type=1 and a.state<>5 and not exists (select ordercode from f_comment where ordercode = a.ordercode and number = e.lognumber) and not exists (select ordercode from f_comment where ordercode = a.ordercode and number is null) "; 
				sqlExceptSelect += " AND e.state=?  order by a.id desc";
				page = Db.paginate(pageno, pagesize, select, sqlExceptSelect, aid, 9);
			}else {
				sqlExceptSelect += " AND a.state=? order by a.id desc";
				page = Db.paginate(pageno, pagesize, select, sqlExceptSelect, aid, state);
			}
		}		
		for(Record order : page.getList()){
			if (state==2) {
				order.set("state", 2);
			}
			String firstDate = DeliveryDateUtil.getFirstDate(order.getStr("ordercode"));
			if(order.getInt("type")==8&&order.getInt("state")==6){
				firstDate ="成团后确定";
			}
			order.set("firstDate", firstDate);
			String imgurl = order.getStr("imgurl");
			if (imgurl.indexOf("-")!= -1) {
				String[] img = imgurl.split("-");
				order.set("imgurl", img[0]);
			}else{
				order.set("imgurl", imgurl);
			}
			

			if (order.getStr("yhfs") != null) {
				String string = order.getStr("yhfs");
				char yhfs = string.charAt(0);
				if (yhfs=='3' || yhfs=='4') {
					order.set("yhfs", yhfs);
				}
			}
			//是否已经产生物流单，如果已经产生物流单，不允许退款
			Long countwl=Db.queryLong("select count(1) from f_order_info where ordercode=?",order.getStr("ordercode"));
			order.set("countwl", countwl);//如果该字段大约0，不允许退款
			
			//是否支持退款，根据f_flower_pro表中的allowRefund字段【0：不可以退款 1：可以退款】
            int allowRefund=Db.queryInt("select b.allowRefund from f_order_detail as a left join f_flower_pro as b on a.fpid=b.id "
            		+ " where a.ordercode=? and a.type=1",order.getStr("ordercode"));
            order.set("allowRefund", allowRefund);
			
		}
		System.out.println(page.getList());
		return page;
	}
	
	// 订单详情
	public static Map<String, Object> getOrderInfo(int aid, String ordercode) throws ParseException{
		Map<String, Object> map = new HashMap<>();
		Record order = Db.findFirst("SELECT e.code,e.lognumber,e.ofcycle, a.ishas,a.ocount,a.is_sy,a.sy_code,a.yhje,yhfs,a.ordercode,b.fpid,c.name,a.name as receiver,a.tel,a.addr,a.cycle,a.reach,a.price AS totalprice,c.describe,b.price,a.isvase,a.jihui,a.color,a.songhua,a.zhufu,a.type,a.ctime,a.state,c.imgurl,a.sendCycle,b.num "+
				"FROM  f_order a left join f_order_info e on e.ordercode = a.ordercode  LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id "+
				"where a.aid=? and a.ordercode=? and b.type=1",
				aid, ordercode);
		String FirstDate = DeliveryDateUtil.getFirstDate(order.getStr("ordercode"));//首次发货日期
		if(order.getInt("type")==8&&order.getInt("state")==6){
			FirstDate ="成团后确定";
		}
		order.set("firstDate", FirstDate);
		String imgurl = order.getStr("imgurl");
		String[] img = imgurl.split("-");
		order.set("imgurl", img[0]);
		//该订单历史批次信息
		List<Record> picilist=Db.find("select  CONCAT( SUBSTR(f_picode.piCode FROM 1 FOR 4),'-', SUBSTR(f_picode.piCode FROM 5 FOR 2),'-', SUBSTR(f_picode.piCode FROM 7 FOR 2))  'piCode',IFNULL(c.lognumber,'') 'number',IFNULL(c.state,0) 'state'  from f_picode   left join (select a.picode,b.number,b.lognumber,b.state from f_picode as a left join f_order_info  as b on a.ordercode=b.ordercode where a.ordercode=? and a.picode=b.code ) as c on f_picode.piCode=c.picode where f_picode.ordercode=?", ordercode,ordercode);
		Record  needSeeds=Db.findFirst("select SUM(seeds) 'seeds' from f_order_detail where ordercode=?", ordercode);
		if(order != null){
			order.set("needSeeds", needSeeds.getBigDecimal(("seeds")).intValue());
			map.put("order", order);
			map.put("cycle", order.get("cycle"));
			map.put("yhfs", order.get("yhfs"));
			map.put("yhje", order.get("yhje"));
			map.put("type", order.get("type"));
			map.put("totalprice", order.get("totalprice"));
			map.put("picilist", picilist);
			
		}
		return map;
	}
	
	/**
	 * 立即支付
	 * 订单未支付，再次付款
	 * @param request
	 * @param openid
	 * @param aid
	 * @param ordercode
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Map<String, Object> payForOrder(HttpServletRequest request, String openid, int aid, String ordercode) throws ParserConfigurationException, IOException, SAXException{
		Map<String, Object> map = new HashMap<>();
		Record order = Db.findFirst("select id,price from f_order where aid=? and ordercode=? and state=0", aid, ordercode);
		if(order != null){
			List<Record> detaillist = Db.find("SELECT a.cycle,a.price AS totalprice,c.imgurl,c.name,c.ptid,b.price,b.type,b.num,a.type 'ordertype' FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id WHERE a.ordercode=?", ordercode);
			// 微信统一下单
			for(Record detail:detaillist){
				if(detail.getInt("type") == 1){
					String xml = FCDao.wxPushOrder(detail.getStr("name"), ordercode, order.getDouble("price"), getRemortIP(request), openid);
					map = XMLParser.getMapFromXML(xml);
					
					String imgurl = detail.getStr("imgurl");
					if (imgurl.indexOf("-") > 0) {
						String[] img = imgurl.split("-");
						detail.set("imgurl", img[0]);
					}
					
				}
			}
			
			map.put("detaillist", detaillist);
		}
		return map;
	}
	
	/**
	 * 红包支付
	 * @param request
	 * @param openid
	 * @param aid
	 * @param ordercode
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Map<String, Object> payForRp(HttpServletRequest request, String openid, int aid, String rpCode) throws ParserConfigurationException, IOException, SAXException{
		Map<String, Object> map = new HashMap<>();
		Record order = Db.findFirst("select quantity1,type,money from f_redpackets where id=? and state=0 and aid=?", rpCode,aid);
		String xml = FCDao.wxPushOrder("红包", rpCode, order.getBigDecimal("money").doubleValue(), getRemortIP(request), openid);
		map = XMLParser.getMapFromXML(xml);
		return map;
	}
	
	
	
	
	
	/**
	 * 活动支付接口
	 * @author Glacier
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @date 2017年8月3日
	 */
	public static Map<String, Object> actopay(HttpServletRequest request,String body,String ordercode,String price,String openid) throws ParserConfigurationException, IOException, SAXException {
		String xml = FCDao.wxPushOrder(body, ordercode, Double.valueOf(price), getRemortIP(request), openid);
		resultMap = XMLParser.getMapFromXML(xml);
		
		return resultMap;
	}
	
	
	
	// 取消订单
	/**
	 * 待付款的订单可以取消
	 * @param aid
	 * @param ordercode
	 * @return
	 */
	public static boolean cancelOrder(int aid, String ordercode){
		boolean result = false;
		Record order = Db.findFirst("select id,yhfs from f_order where ordercode=? and aid=? and state=0", ordercode, aid);
		if(order != null){
			order.set("state", 5);
			result = Db.update("f_order", order);
			if(order.get("yhfs") != null){
				// 优惠方式
				String yhfs = order.getStr("yhfs");
				String[] ids = yhfs.split(",");
				yhfs = null;
				if(Integer.parseInt(ids[0]) == 0){
					String[] cashList=ids[1].split("-");
					if(cashList[0].equals("0")==false){
						Db.update("update f_cash set state = 1 where id =?", cashList[0]);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 是否允许修改订单的送达日期
	 * 当大于当前时间的批号已经产生物流单时，便不可修改。
	 * 当大于当前时间的批号没有产生物流单时，可以修改。
	 * 注意：偶数次修改，时间是-2或-5，所以需要判断修改的批号是否已经产生物流单
	 * 
	 * 送达日期之前54小时之内不能修改
	 * @param ordercode
	 * @return
	 */
	public static boolean isAllowChange(String ordercode){

	    String nextPiCode=Db.queryStr("select piCode from f_picode where orderCode=? "
				+ "and str_to_date(piCode,'%Y%m%d')>NOW() ORDER BY piCode LIMIT 1", ordercode);//待修改的批号(下次发货批号)
	    Long count3=Db.queryLong("select count(*) from f_order_info where code=? and orderCode=?",nextPiCode,ordercode);
		if(count3==0){//如果没有产生物流单，再判断一下是否在允许的修改日期范围内
			
			Long isAllow=Db.queryLong("select DATE_ADD(NOW(), INTERVAL 54 HOUR)>(select piCode from f_picode where orderCode=? "
					+ "and str_to_date(piCode,'%Y%m%d')>NOW() ORDER BY piCode LIMIT 1)",ordercode);
			if(isAllow==0){
				return true;
			}else{
				return false;
			}
			
		}else{
			return false;
		}
	}
	
	// 退款申请
	public static Record refund(int aid, String ordercode){
		return Db.findFirst("SELECT a.ordercode,c.name,c.imgurl,b.price,a.cycle,a.reach,a.isvase,a.price AS totalprice FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id WHERE a.aid=? AND a.ordercode=? AND b.type=1", aid, ordercode);
	}
	/**
	 * 是否允许退款
	 * @param ordercode
	 * @return
	 */
	public static boolean isAllowRefund(String ordercode){
		boolean flag=false;
		flag=Db.queryLong("select count(1) from f_flowerseed where state=1 and username=?",ordercode)==0?true:false;
	    return flag;
	}
	
	// 赠送好友-获取信息
	public static Record getOrderToReceive(int aid, String ordercode){
		Record order = Db.findFirst("SELECT a.ordercode,c.name,c.imgurl "+
				"FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id "+
				"where a.aid=? and a.ordercode=? and b.type=1", aid, ordercode);
		return order;
	}
	
	// 赠送好友-验证与提交
	public static Map<String, Object> orderReveiveValidAndSave(int aid, String ordercode, int cycle){
		Map<String, Object> map = new HashMap<>();
		boolean result = false;
		String msg = new String();
		Record order = Db.findFirst("select id from f_order where ordercode=? and aid=?", ordercode, aid);
		if(order != null){
			Record share = Db.findFirst("select aid,code from f_share where ordercode = ?", ordercode);
			if(share != null){
				if(share.getInt("aid") == null){
					msg = "分享记录已存在，尚未领取，提取码:"+share.getStr("code");
				}else{
					msg = "分享记录已存在，并已成功领取";
				}
			}else{
				share = new Record();
				share.set("ordercode", ordercode);
				share.set("cycle", cycle);
				share.set("code", getCode());
				share.set("ctime", new Date());
				if(Db.save("f_share", share)){
					msg = "在右上角选择&lceil;发送给朋友&rfloor;进行分享，提取码:"+share.getStr("code");
					result = true;
				}else{
					msg = "操作失败";
				}
			}
		}else{
			msg = "订单不存在";
		}
		map.put("result", result);
		map.put("msg", msg);
		return map;
	}
	
	// 领花-提取码验证
	public static boolean validCode(String ordercode, String code){
		Record share = Db.findFirst("select id from f_share where ordercode=? and code=?", ordercode, code);
		return share==null?false:true;
	}
	
	// 领花
	public static Map<String, Object> getOrderReceive(int aid, String ordercode, String code,String name, String tel, String area,String addr){
		Map<String, Object> map = new HashMap<>();
		boolean result = false;
		String msg = new String();
		Record share = Db.findFirst("select id,aid from f_share where ordercode=? and code=?", ordercode, code);
		if(share == null){
			msg = "提取码错误";
		}else{
			if(share.getInt("aid") == null){
				share.set("aid", aid);
				share.set("name", name);
				share.set("tel", tel);
				String address = FCDao.getAddressArea(area);
				share.set("addr", address+addr);
				share.set("gtime", new Date());
				if(Db.update("f_share", share)){
					result = true;
					msg = "领取成功";
				}else{
					msg = "领取失败";
				}
			}else{
				msg = "领取失败，此赠送已被领取";
			}
		}
		map.put("result", result);
		map.put("msg", msg);
		return map;
	}
	
	// 保存退款申请
	public static boolean saveRefund(final int aid, final String ordercode, final String remark){
		boolean flag=false;
		Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean result = false;
				// 服务中的订单可退款
				Record order = Db.findFirst("select id from f_order where aid=? and ordercode=? and state=1", aid, ordercode);
				if(order != null){
					order.set("state", Constant.orderState.STATE4.state);
					if(Db.update("f_order", order)){
						Record refund = new Record();
						refund.set("ordercode", ordercode);
						refund.set("remark_a", remark);
						refund.set("time_a", new Date());
						Record fo = Db.findFirst("select id from f_refund where ordercode=?", ordercode);
						if(fo != null){
							refund.set("id", fo.get("id"));
							refund.set("state", 0);
							result = Db.update("f_refund", refund);
						}else{
							result = Db.save("f_refund", refund);
						}
						if(result){
							//兑换订单没有花籽
							Db.update("update f_flowerseed set state=3 where username=? and state=0",ordercode);//收回花籽
						}
					}
				}
				return result;
			}
		});
		return flag;
		
		
	}
	
	// 微信统一下单
	public static String wxPushOrder(String body, String ordercode, double price, String ip, String openid){
		Map<String, String> params = new HashMap<>();
    	params.put("appid", PropKit.get("appId"));
    	params.put("mch_id", PropKit.get("mchId"));
    	params.put("nonce_str", System.currentTimeMillis() + "");
    	params.put("body", "FlowerMM");
    	params.put("out_trade_no", ordercode);
    	params.put("total_fee", (int)mul(price,100) + "");
    	/*params.put("total_fee", (int)(price*100) + ""); 这个问题有点迷茫，64.99*100变成了6498*/
    	params.put("spbill_create_ip", ip);
    	params.put("notify_url", Constant.getHost + "/weixin/wxpayback");
    	params.put("trade_type", TradeType.JSAPI.name());
    	params.put("openid", openid);
    	params.put("sign", Signature.getSign(params));
    	String xml = PaymentApi.pushOrder(params);
    	return xml;
	}
	
	public static double mul(double d1,double d2){  
        BigDecimal b1=new BigDecimal(Double.toString(d1));  
        BigDecimal b2=new BigDecimal(Double.toString(d2));  
        return b1.multiply(b2).doubleValue();  
          
    }
	
	// 获取openId
	public static Map<String, Object> getAccessToken(String code) {
		String rUrl = Constant.getAccess_Token
				.replace("APPID", PropKit.get("appId"))
				.replace("SECRET", PropKit.get("appSecret"))
				.replace("CODE", code);
		Map<String, Object> map = new HashMap<>();
		try {
			URL url = new URL(rUrl);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line);
			}

			br.close();
			isr.close();
			is.close();
			
			ApiResult ar = new ApiResult(builder.toString());
			map.put("access_token", ar.get("access_token"));
			map.put("openid", ar.get("openid"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	
	/**
	 * 注册或启用用户
	 * @param access_token
	 * @param openId
	 * @param typeId
	 * @param eventUserId
	 * @return
	 */
	public static Record setAccount(String access_token, String openId, String typeId, String eventUserId){
		Record account = Db.findFirst("select id,openid,nick,headimg,tel,recommend,isbuy,qrurl,state,isfans,ctime from f_account where openid = ?", openId);
		if(account == null){
			ApiResult ar;
			account = new Record();
			account.set("openid", openId);
			if(access_token == null){
				ar = UserApi.getUserInfo(openId);
				account.set("isfans", 0);
			}else{
				String userinfo = HttpKit.get(Constant.getSnsapi_userinfo
												.replace("ACCESS_TOKEN", access_token)
												.replace("OPENID", openId));
				ar = new ApiResult(userinfo);
				account.set("isfans", 3);
			}
			account.set("nick",ar.get("nickname").toString());
			/*try {
				account.set("nick", filterOffUtf8Mb4_2(ar.get("nickname").toString()));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}*/
			//  存入用户  unionid 
			/*if (ar.get("unionid") != null) {
				account.set("unionid", ar.get("unionid"));
			}*/
			if (ar.get("headimgurl").toString().length()<10) {
				account.set("headimg", defaultimg);
			}else {
				account.set("headimg", ar.get("headimgurl"));	
			}
			account.set("isbuy", 0);
			account.set("ctime", new Date());
			account.set("state", 0);
			account.set("province", ar.get("province").toString());//用户所在省份
			account.set("city", ar.get("city").toString());//用户所在城市
			account.set("unionid", ar.get("unionid").toString());
			if(typeId!=null && eventUserId!=null){
				account.set("tjid", typeId+"_"+eventUserId);
				
			}
			
			boolean result = Db.save("f_account", account);
			if(result){
				//给代颜人发花籽和红包
				sendSeeds(typeId, eventUserId,ar.get("province").toString(),account.getLong("id").intValue(),ar.get("nickname").toString());
				account.set("id", account.getLong("id").intValue());//添加数据库后，获取自增长的ID号
				
				if(account.getInt("isfans")==0){
					sendSeedsUser(account.getInt("id"),openId);
				}else if(account.getInt("isfans")==3){
					sendSeedsUser_first(account.getInt("id"));//为了能抽奖
				}
				
			}
		}else{
			//isfans=3只是注册了，但是没有关注，例如自己通过连接打开了主页
			if(account.getInt("isfans")==3){
				int i=0;
				if(typeId!=null && eventUserId!=null){
					i=Db.update("update f_account set isfans = 0,state = 0,tjid=? where openid = ?",typeId+"_"+eventUserId, openId);
				}else{
					i=Db.update("update f_account set isfans = 0,state = 0 where openid = ?", openId);
				}
				if(i==1){
					sendSeedsUser(account.getInt("id"),openId);
					
					
				}
			}
			//isfans=1 表示以前关注过，但是后来取消了关注
			if(account.getInt("isfans")==1){
				Db.update("update f_account set isfans = 0,state = 0 where openid = ?", openId);
				
			}
		}
		return account;
	}
	
	/**
	 * 给代颜人送花籽
	 * @param typeId 代颜类型 1个人代颜  2推广代颜
	 * @param eventUserId 代颜人id号
	 * @param provice 被代颜人所在省份
	 */
	public static void sendSeeds(String typeId,  String eventUserId,String provice,int byAid,String byNick){
		new Thread(){
			public void run(){
				//给代颜人发裂变红包，每人每天限发3个
				String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
				
				
				if(code_value.equals("1")&&typeId.equals("1")){
					String openId_tj = Db.queryStr("SELECT openid FROM f_account WHERE id = ?",eventUserId);
					if(provice.contains("江苏")||provice.contains("浙江")||provice.contains("上海")){
						String tMoney=Db.queryStr("select code_value from f_dictionary where code_key='tMoney'");//设置的总额度
						double tMoneySend=Db.queryDouble("select IFNULL(sum(getMoney),0)/100.0 from f_fissionRP");//已发的金额
						if(tMoneySend<Double.valueOf(tMoney).doubleValue()){
							boolean flag=com.util.SendCashRed.sendGroup("花美美", openId_tj,
									   "300", "3", "成为花美美带颜人,成功邀请江浙沪地区的好友即可领微信红包；每人每天限领3个", "新年微信红包活动", "备注信息");
							if(flag){
								Record f_fissionRP=new Record();
								f_fissionRP.set("geterAid", eventUserId);
								f_fissionRP.set("byAid", byAid);
								f_fissionRP.set("byProvice", provice);
								f_fissionRP.set("getMoney", 300);
								f_fissionRP.set("cTime", new Date());
								Db.save("f_fissionRP", f_fissionRP);
							}
						}else{
							WeixinApiCtrl.setApiConfig();
							String msg=Db.queryStr("select note from f_dictionary where code_key='tMoney'");
							CustomServiceApi.sendText(openId_tj, msg);//50万已经瓜分完毕
						}
					}else{
						WeixinApiCtrl.setApiConfig();
						String message = byNick+"在微信中设置的地址不属于【江浙沪地区】,或者没有进行设置.";
						CustomServiceApi.sendText(openId_tj, message);
					}
				}
				
				
				
				
				
				Long countSeed=Db.queryLong("select count(1) from f_flowerseed where aid=? and type='invite'",eventUserId);
				//限制最多送200个花籽
				if(typeId.equals("1") && countSeed<200){
					// 通过代言二维码关注  获赠花籽
					boolean seedR = false;
					for(int i=0;i<seedType.invite.point;i++){
						Record seed = new Record();
						seed.set("aid", eventUserId);
						seed.set("send", 1);
						seed.set("type", seedType.invite.name());
						seed.set("remarks", seedType.invite.name);
						seed.set("ctime", new Date());
						seedR = Db.save("f_flowerseed", seed);
					}
					if (seedR) {
						// 绑定 apiConfig 到当前线程
						WeixinApiCtrl.setApiConfig();
						String openId_tj = Db.queryStr("SELECT openid FROM f_account WHERE id = ?",eventUserId);
						String message = "您成功为“美美”带颜，特意送上2颗花籽。立即使用:<a href='" + Constant.getHost + "/account/flowerseed'>【花籽兑换】</a>";
						CustomServiceApi.sendText(openId_tj, message);
					}
				}
				if(typeId.equals("5") && countSeed<200){
					// 通过代言二维码关注  获赠花籽
					boolean seedR = false;
					for(int i=0;i<seedType.invite.point;i++){
						Record seed = new Record();
						seed.set("aid", eventUserId);
						seed.set("send", 1);
						seed.set("type", seedType.invite.name());
						seed.set("remarks", seedType.invite.name);
						seed.set("ctime", new Date());
						seedR = Db.save("f_flowerseed", seed);
					}
					if (seedR) {
						// 绑定 apiConfig 到当前线程
						WeixinApiCtrl.setApiConfig();
						String openId_tj = Db.queryStr("SELECT openid FROM f_account WHERE id = ?",eventUserId);
						String message = "您成功为“花艺课”带颜，特意送上2颗花籽。立即使用:<a href='" + Constant.getHost + "/account/flowerseed'>【花籽兑换】</a>";
						CustomServiceApi.sendText(openId_tj, message);
					}
				}
			}
		}.start();
		
	}
	
	/**
	 * 新人注册送花籽
	 * @param aid 接收花籽的账号ID
	 * @param openId 接收花籽的openid
	 */
	public static void sendSeedsUser(int aid,String openId){
		boolean result=false;
		int seednum=seedType.register.point;
		for(int i=0;i<seednum;i++){
			Record seed = new Record();
			seed.set("aid", aid);
			seed.set("send", 1);
			seed.set("type", seedType.register.name());
			seed.set("remarks", seedType.register.name);
			seed.set("ctime", new Date());
			result = Db.save("f_flowerseed", seed);
		}
		if (result) {
			// 绑定 apiConfig 到线程之上
			String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
			
			if(code_value.equals("1")){
				WeixinApiCtrl.setApiConfig();
				String message01 = "嗨！新年初次见面，美好鲜花也不及你的娇俏\n\n";
				message01+="欢迎成为花美美带颜人,成功邀请江浙沪地区的好友即可领取微信红包，每人每天限领3个\n";
				message01+="<a href='" + Constant.getHost + "/account/invitefri'>【我要带颜领微信红包】</a>";
				CustomServiceApi.sendText(openId, message01);
				
				//送新春花票
				Record c = new Record();
				c.set("aid", aid);
				c.set("cid", 2030);
				c.set("code", "1111");
				Calendar now = Calendar.getInstance();
				c.set("time_a", now.getTime());
				now.add(Calendar.DAY_OF_MONTH, 10);
				c.set("time_b", now.getTime());
				c.set("state", 1);
				c.set("origin", 1);
				Db.save("f_cash", c);	
				//发送客服消息通知
				WeixinApiCtrl.setApiConfig();
				String message03 = "20元新春花票已入账，一束鲜花仅需19.9元（包邮）,<a href='" + Constant.getHost + "/festivalProduct/104'>【立即使用】</a>";
				CustomServiceApi.sendText(openId, message03);
				
			        
			
				
			}else{
				WeixinApiCtrl.setApiConfig();
				String message = "首次关注花美美，送上"+seednum+"颗花籽。<a href='" + Constant.getHost + "/account/flowerseed'>【查看花籽】</a>";
				CustomServiceApi.sendText(openId, message);
			}
			
			
			
			}
	}
	/**
	 * 只要注册就送两粒花籽，为了抽奖
	 * @param aid
	 * @param openId
	 */
	public static void sendSeedsUser_first(int aid){
		int seedNum= Integer.valueOf(Db.queryStr("select code_value from f_dictionary where code_key='fsn' and state=1"));
		for(int i=0;i<seedNum;i++){
			Record seed = new Record();
			seed.set("aid", aid);
			seed.set("send", 1);
			seed.set("type", "luck");
			seed.set("remarks", "非关注注册");
			seed.set("ctime", new Date());
		    Db.save("f_flowerseed", seed);
		}
	}
	
	
	
	
	//刷新个人信息
	public static boolean freshAccount(String openId) {
		
		// 绑定 apiConfig 到当前线程
		WeixinApiCtrl.setApiConfig();
		ApiResult ar = UserApi.getUserInfo(openId);
		//更新昵称
		String nick=ar.get("nickname")==null?"亲爱的花粉":ar.get("nickname");
		
		
        //更新头像
		String headimg=ar.get("headimgurl");
		
		
		int i = Db.update("update f_account set nick=? ,headimg = ? where openid = ?",nick,headimg,openId);
		boolean R = i==1?true:false;
		return R;
	}
	
	
	/**
	 * Java 过滤非汉字的utf8的字符
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String filterOffUtf8Mb4_2(String text) throws UnsupportedEncodingException {
		byte[] bytes = text.getBytes("utf-8");
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		int i = 0;
		while (i < bytes.length) {
		short b = bytes[i];
		if (b > 0) {
		buffer.put(bytes[i++]);
		continue;
		}

		b += 256; //去掉符号位

		if (((b >> 5) ^ 0x06) == 0) {
		buffer.put(bytes, i, 2);
		i += 2;
		/*System.out.println("2");*/
		} else if (((b >> 4) ^ 0x0E) == 0) {
		/*System.out.println("3");*/
		buffer.put(bytes, i, 3);
		i += 3;
		} else if (((b >> 3) ^ 0x1E) == 0) {
		i += 4;
		/*System.out.println("4");*/
		} else if (((b >> 2) ^ 0xBE) == 0) {
		i += 5;
		/*System.out.println("5");*/
		} else {
		i += 6;
		/*System.out.println("6");*/
		}
		}
		buffer.flip();
		return new String(buffer.array(), "utf-8");
		}
	
	/**
	 * 存在死循环，慎用
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String filterOffUtf8Mb4(String text) throws UnsupportedEncodingException {
        byte[] bytes = text.getBytes("utf-8");
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        int i = 0;
        while (i < bytes.length) {
            short b = bytes[i];
            if (b > 0) {
                buffer.put(bytes[i++]);
                continue;
            }
            b += 256;
            if ((b ^ 0xC0) >> 4 == 0) {
                buffer.put(bytes, i, 2);
                i += 2;
            }
            else if ((b ^ 0xE0) >> 4 == 0) {
                buffer.put(bytes, i, 3);
                i += 3;
            }
            else if ((b ^ 0xF0) >> 4 == 0) {
                i += 4;
            }
        }
        buffer.flip();
        return new String(buffer.array(), "utf-8");
    }
	
	// 获取用户IP
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
	
	//生成4位随机数
	public static String getCode(){
		Random r = new Random();
		StringBuffer vc = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			String ch = String.valueOf(r.nextInt(10));
			vc.append(ch);
		}
		return vc.toString();
	}
	
	//更改花籽状态
	public static void chanseedstate(int id, int pid){
		int count = pid==1?40:60;
		Db.update("update f_flowerseed set state=1 where aid=? and DATE_ADD(ctime, INTERVAL 2 MONTH)>=CURDATE() and state=0 ORDER BY ctime LIMIT ?", id, count);
	}
	//更改花籽状态(新)
	public static void chanseedstate_new(int id, String ordercode){
		int count=Db.queryInt("select seeds from f_order_detail where ordercode=?",ordercode);
		Db.update("update f_flowerseed set state=1 where aid=? and state=0 ORDER BY ctime LIMIT ?", id, count);
	}
	
	//获得物流信息
	public static List<Record> getlogisticsInfo(String code){
		return Db.find("SELECT WorkCode,OpDateTime,OpDescription from f_logistics where WorkCode =? order by ctime desc",code);
	}
	
	//我的物流信息
	public static Page<Record> getMylogistics(int pageno, int pagesize, int id){
		String select = "SELECT e.code,a.aid,e.lognumber, e.state,e.number,a.ordercode,b.fpid,c.name,a.cycle,a.reach,a.price AS totalprice,b.price,a.isvase,a.type,a.ctime,a.state,c.imgurl";
		String sqlExceptSelect = "FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id"+
								 " left JOIN f_order_info e on a.ordercode = e.ordercode"+
								 " WHERE a.aid =? AND e.state = 2"+
								 " group by e.number order by a.id";
		return Db.paginate(pageno, pagesize,select,sqlExceptSelect,id);
		
	}
	//确认收货
	public static boolean shipconfirm(int id,String workcode){
		boolean result = false;
		Record order_info = Db.findFirst("select id from f_order_info where lognumber=? and aid=?",workcode,id);
		if(order_info != null){
			order_info.set("state", 9);
			if(Db.update("f_order_info", order_info)){
				result = true;
			}
		}
		return result;
	}
	
	
	/**---------------------------------------字典表  多买立减 即将废弃-----------------------------------------------------------------**/
	
	public static String getDmlj(String ordercode){
		Record rd = Db.findFirst("select dmlj from f_order_detail as a "
				+ " left join f_flower_pro as b on a.fpid=b.id where a.ordercode=? and a.type=1",ordercode);
		return rd.get("dmlj");
	}
	
	//单品多买立减
	public static String getDmlj(){
		Record code_value = Db.findFirst("select code_value from f_dictionary where code_key = 'dmlj'");
		return code_value.get("code_value");
	}
	//多品多买立减
	public static String getDmlj2(){
		Record code_value = Db.findFirst("select code_value from f_dictionary where code_key = 'dmlj2'");
		return code_value.get("code_value");
	}

	// 定制多买立减
	public static String getDmlj3() {
		Record code_value = Db.findFirst("select code_value from f_dictionary where code_key = 'dmlj3'");
		return code_value.get("code_value");
	}
	// 拼团多买立减
	public static String getDmlj4() {
		Record code_value = Db.findFirst("select code_value from f_dictionary where code_key = 'dmlj4'");
		return code_value.get("code_value");
	}
   // 拼团多买立减
	public static String getDmlj5() {
		Record code_value = Db.findFirst("select code_value from f_dictionary where code_key = 'dmlj5'");
		return code_value.get("code_value");
	}
	
	//获得商品总额
	public static Double getTotalprice(String ordercode,int cycle){
		Double totalprice = 0.00;
		List<Record> prices = Db.find("SELECT type,price,isAllot from f_order_detail  where ordercode = ? order by type",ordercode);
		for (Record record : prices) {
			if(record.getInt("type")==1){
				if (record.getInt("isAllot") > 0) {
					totalprice += record.getDouble("price");
				}else {
					totalprice += record.getDouble("price")*cycle;
				}
				
			}else{
				totalprice += record.getDouble("price");
			}
		} 
		return totalprice;
	}
	
	//获得忌讳花材
	public static List<Record> getjihuiflo(){
		return Db.find("SELECT id,name FROM f_flower WHERE jh = 1 AND state = 1");
	}
	
	//获得忌讳色系
	public static List<Record> getjihuicolor(){
		return Db.find("SELECT id,name FROM f_color WHERE jh = 1");
	}
	
	//获得忌讳花材分类
	public static List<Record> getjihuiType(){
		return Db.find("SELECT id,name FROM f_flower_type WHERE jh = 1");
	}
	
	//更改订单地址
	public static boolean changeorderaddr(String ordercode,int addrid){
		Record address = Db.findById("f_address", addrid);
		String newaddr = getAddressArea(address.getStr("area")) + address.getStr("addr");
		int result = Db.update("update f_order set addr = '"+newaddr+"',name = '"+address.getStr("name")+"',tel = '"+address.getStr("tel")+"' where ordercode = "+ordercode);
		return result==1?true:false;
	}
	
	//获得邀请好友内容
	public static String getInviteFriend(){
		String yqhy = Db.queryStr("SELECT code_value FROM f_dictionary WHERE code_key = 'yqhy'");
		return yqhy;
	}
	
	//获取推广的好友信息
	public static Page<Record> findOrderMember(int pageno, int pagesize, int id, int type){  //type: 1 会员：2 递推人员
		Page<Record> list = Db.paginate(pageno, pagesize,"SELECT a.id,headimg, nick,date_format(b.ctime,'%Y-%m-%d') 'ctime' "," FROM f_account a left join f_order b on a.id=b.aid WHERE SUBSTRING(tjid,1,1) = "+type+" AND SUBSTRING(tjid,3) ="+id+" and isbuy > 0 and b.ishas=0 and b.state in(1,2,3) order by b.ctime");
		List<Record> li = list.getList();
		for(Iterator<Record> it=li.iterator();it.hasNext();){
			Record ditui = it.next(); 
			long num = Db.queryLong("SELECT COUNT(1) FROM f_order WHERE state IN (1,2,3) and aid = ?",ditui.getInt("id"));
			ditui.set("num", num);
			if(num == 0){
				it.remove();
			}
		}
		return list ;
	}
	
	//赠送好友的花票信息
	public static List<Record> getFriendCash(String cl){
		return Db.find("SELECT a.id,a.time_a,a.time_b,b.money,b.benefit FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id where a.id in ("+cl+") and a.state=1 and CURDATE()>=a.time_a AND CURDATE()<=a.time_b");
	}
	//获得好友赠送的花票信息
	public static List<Record> getCashFriend(String cl){
		return Db.find("SELECT a.id,a.time_a,a.time_b,b.money,b.benefit,case b.ptid when 0 then '所有产品' else c.name end as name  "
				+ " FROM f_cash a "
				+ " LEFT JOIN f_cashclassify b ON a.cid=b.id "
				+ " LEFT JOIN f_flower_pro c on b.ptid = c.ptid "
				+ " where a.id in ("+cl+") and a.state=0 and CURDATE()>=a.time_a AND CURDATE()<=a.time_b");
	}
	//获得花票的赠送人ID
	public static Integer getcashforId(String cl){
		return Db.queryInt("select distinct aid_f from f_cash where id in ("+cl+")");
	}
	
	//号码对应的服务中的订单
	public static List<Record> getCards(String tel){
		//List<Record> cards = Db.find("SELECT ordercode,ctime FROM f_order WHERE tel = ? AND state in (1,2) and zhufu is not null ORDER BY ctime DESC", tel);
		List<Record> cards = Db.find("SELECT ordercode,ctime,b.imgurl01,a.picId,a.songhua FROM f_order as a "
				+ " left join f_wishcardpic as b on a.picId=b.id "
				+ " WHERE a.tel = ? AND a.state in (1,2,3) and a.zhufu is not null ORDER BY a.ctime DESC", tel);
		/*for (Record card : cards) {
			Date cdate = card.getDate("ctime");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
			String ctime = sdf.format(cdate);
			card.set("ctime", ctime);
		}*/
		return cards;
	}
	//获得祝福语
	public static Record getCardInfo(String ordercode){
		Record zhufu = Db.findFirst("SELECT zhufu,songhua,name,ordercode FROM f_order WHERE ordercode = ?", ordercode);
		return zhufu;
	}
	
	/**
	 * 鲜花卡创建兑换订单
	 * @param session
	 * @param cardNum
	 * @param orderCode
	 * @param cExcTime
	 * @param fpid
	 * @param addressid
	 * @param reach
	 * @param jh_list
	 * @param jhcolor_list
	 * @param yhje
	 * @param zhufu
	 * @param songhua
	 * @param cycle
	 * @param type
	 */
	public static void createCardExcOrder(HttpSession session, int cardNum, String orderCode, Date cExcTime, int fpid, int addressid, int reach, String jh_list,
			String jhcolor_list, Double yhje, String zhufu, String songhua,int cycle, int type) {
		Record account = (Record) session.getAttribute("account");
		Record pro_flower = Db.findById("f_flower_pro", fpid);
		Record order = new Record();
		
		//保存商品名称
		order.set("gName", pro_flower.get("name"));
		
		order.set("ordercode", orderCode);
		order.set("aid", account.get("id"));
		order.set("price", 0);//兑换订单 实际金额为0
		Record address = Db.findById("f_address", addressid);
		order.set("name", address.getStr("name")); // 收货人姓名
		order.set("tel", address.getStr("tel")); // 收货人电话
		order.set("addr", getAddressArea(address.getStr("area")) + address.getStr("addr")); // 收货人地址
		order.set("reach", reach); // 送达时间(1周一/2周六)
		order.set("zhufu", zhufu);
		order.set("songhua", songhua);
		order.set("jh_list", "".equals(jh_list) ? null : jh_list); // 忌讳的花
		order.set("jh_color", "".equals(jhcolor_list) ? null : jhcolor_list); // 忌讳的色系
		order.set("yhje", yhje);
		if (!"".equals(jh_list)) {
			order.set("jihui",
					Db.queryStr("select group_concat(NAME) from f_flower_type where id in (" + jh_list + ")"));// 忌讳的花材分类
		}
		if (!"".equals(jhcolor_list)) {
			order.set("color",
					Db.queryStr("select group_concat(NAME) from f_color where id in (" + jhcolor_list + ")"));// 忌讳的色系
		}
		
		order.set("cycle", cycle); // 周期
		order.set("ctime", cExcTime); // 下单日期
		order.set("type", 1); // 商品类型(兑换)
		order.set("state", 1); // 兑换订单成功直接去评价该订单
		int[] yhfs = { 3, cardNum };   //优惠方式三 +卡号
		order.set("yhfs", yhfs[0] + "," + yhfs[1]); // 兑换订单无优惠方式
		
		//判断是否是首单
		Record member=Db.findById("f_account",account.get("id"));
		if(member.getInt("isbuy")==0&&MCDao.isFirstOrder(fpid)==true){
		   member.set("isbuy",1);
		   order.set("ishas",0);
		}else if(member.getInt("isbuy")==1){
		   member.set("isbuy",2);
		}
		Db.update("f_account",member);
		// 保存订单批号
		List<String> piCodeList = DeliveryDateUtil.getPiCodeList(reach, new Date(), 1, cycle, type);
		for (int i = 0; i < piCodeList.size(); i++) {
			Record f_picode = new Record();
			f_picode.set("ordercode", order.getStr("ordercode"));
			f_picode.set("piCode", piCodeList.get(i));
			f_picode.set("reach", reach);
			f_picode.set("num", i + 1);
			Db.save("f_picode", f_picode);
		}
		// 保存订单
		if (Db.save("f_order", order)) {
			Record order_detail_flower = new Record();
			order_detail_flower.set("ordercode", order.getStr("ordercode"));
			order_detail_flower.set("fpid", fpid);
			order_detail_flower.set("price", pro_flower.getDouble("price"));
			order_detail_flower.set("type", 1);
			Db.save("f_order_detail", order_detail_flower);
		}
		
		// 绑定 apiConfig 到线程之上
		WeixinApiCtrl.setApiConfig();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(cExcTime);

		ApiResult result = TemplateMsgApi.send(TemplateData.New()
				// 消息接收者
				.setTouser(account.getStr("openid"))
				// 模板id
				.setTemplate_id(WeixinApiCtrl.gettemplateId("兑换成功通知"))
				.setTopcolor("#eb414a")
				.setUrl(Constant.getHost + "/service/myorder")
				.add("keyword1", pro_flower.getStr("name"), "#FF8040")
				.add("keyword2", "0", "#FF8040").add("keyword3", orderCode, "#FF8040")
				.add("keyword4", dateString.toString(), "#FF8040")
				.add("keyword5", "鲜花卡兑换", "#FF8040").build());
		WeixinApiCtrl.sendTemplateMsg(result.getJson());
		
	}

	// 获取送我花的人
	public static List<Record> getUsers(int reciverId) {
		return Db.find("SELECT a.receiverId, a.giverId ,a.ordercode,b.nick,b.headimg,b.sex,b.birthday "
				+ "FROM f_flower_received a  LEFT JOIN f_account b "
				+ "ON b.id=a.giverId WHERE a.receiverId = ? ",reciverId);
	}
	
	// 获取我送出的鲜花
	public static List<Record> getUser(int aid) {
		return Db.find("SELECT a.gName,a.name,a.ctime,c.imgurl FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode "
					+"LEFT JOIN f_flower_pro c ON b.fpid=c.id WHERE a.aid=? and a.state in (1,2,3) "
					+"and a.tel <> (SELECT tel from f_account WHERE id=?)",aid,aid);
	}
/*-----------------------------------------------------红包 ------------------------------------------------*/
	
	
	/***
	 * 获取红包商品列表(主品)
	 * 总裁专用
	 * @author Glacier
	 * @date 2017年6月27日
	 */
	public static List<Record> getRedPacketsList() {
		List<Record> redlist = Db.find("SELECT a.id, a.`name`,a.pnum, a.pmoney, a.imgurl03,a.imgurl02,b.ptid,b.id as 'bid'  "
				+ "FROM f_redpackets_pro a LEFT JOIN f_flower_pro b ON a.fpid = b.id "
				+ "WHERE a.state = 1 and protype=1 and userType=1 order by a.orderId ");
		
		for (Record re : redlist) {
			String imgs = re.getStr("imgurl03");

			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				re.set("imgurl1", ims[0]);
				re.set("imgurl2", ims[1]);
				re.set("imgurl3", ims[2]);
			}else{
				re.set("imgurl1", imgs);
				re.set("imgurl2", imgs);
				re.set("imgurl3", imgs);
			}
		}
		return redlist;
	}
	/**
	 * 鲜花卡
	 * @return
	 */
	public static List<Record> getRedPacketsList2() {
		return Db.find("SELECT a.id, a.`name`,a.pnum, a.pmoney, a.imgurl03 "
				+ "FROM f_redpackets_pro a LEFT JOIN f_flower_pro b ON a.fpid = b.id "
				+ "WHERE a.state = 1 and protype=1 and userType=2 order by a.orderId ");
	}
	/**
	 * // 获取红包商品列表(副品)
	 * @author Glacier
	 * @date 2017年6月27日
	 * @return
	 */
	public static List<Record> getRedPacketsListF() {
		return Db.find("SELECT a.id, a.`name`,a.pnum, a.pmoney, b.imgurl "
				+ "FROM f_redpackets_pro a LEFT JOIN f_flower_pro b ON a.fpid = b.id "
				+ "WHERE a.state = 1 and protype=2");
	}
	
	/**
	 * 1.我发的红包
	 * 2.我收到的红包
	 * @param pageno
	 * @param aid
	 * @return
	 */
	public static List<Record> getMyRedPackets(int pageno, int aid) {
		List<Record> redPacketList;
		
		if (pageno == 1) {
			/*
			 redPacketList =  Db.find("select a.id, b.id AS RPid,b.ctime,b.type,a.isopen, b.msg, d.`name`, d.imgurl "
					+ "from f_redpackets_detial  a "
					+ "left join f_redpackets b on a.rpid = b.id "
					+ "left join f_redpackets_pro c on a.rppid = c.id "
					+ "left join f_flower_pro d on c.fpid = d.id "
					+ "where b.state IN(1,2) AND b.aid = ?  ",
					aid);*/
			
			redPacketList = Db.find("select id,type,money,quantity1,quantity2,msg,ctime,stime,ttime,tmoney,opuser,state,aid from f_redpackets where aid = ? "
					+ "and state in(1,2,3) AND picId =0 GROUP BY ctime DESC",aid);
		}else {
			redPacketList =  Db.find("select a.id, b.id AS RPid,b.ctime,b.type,a.oid, b.msg, d.`name`, d.imgurl, b.aid,a.isopen "
					+ "from f_redpackets_detial  a  "
					+ "left join f_redpackets b on a.rpid = b.id "
					+ "left join f_redpackets_pro c on a.rppid = c.id "
					+ "left join f_flower_pro d on c.fpid = d.id "
					+ "where c.userType = 1 and a.aid = ? ",
					aid);
		}
		return redPacketList;
	}
	/**
	 * 红包兑换 创建订单
	 * @param session
	 * @param fpid
	 * @param addressid
	 * @param reach
	 * @param jh_list
	 * @param jhcolor_list
	 * @param cycle
	 * @param type
	 * @param redDetailId
	 * @param jr_picode 节日订单的批号
	 */
	public static void exccreateOrder(HttpSession session, int fpid, int addressid, int reach, String jh_list, String jhcolor_list, int cycle,int type, int redDetailId,String jr_picode){
		Record account = (Record) session.getAttribute("account");
		Record pro_flower = Db.findById("f_flower_pro", fpid);
		Record order = new Record();
		
		order.set("ordercode", getOrderCode());	
		order.set("aid", account.get("id"));
		order.set("price", 0);
		//存入商品名称
        order.set("gName", pro_flower.get("name"));
        
		Record address = Db.findById("f_address", addressid);
		order.set("name", address.getStr("name"));	// 收货人姓名
		order.set("tel", address.getStr("tel"));	// 收货人电话
		order.set("addr", getAddressArea(address.getStr("area")) + address.getStr("addr"));	// 收货人地址
		
		order.set("reach", reach);		// 送达时间(1周一/2周六)
		order.set("jh_list", "".equals(jh_list)?null:jh_list);	// 忌讳的花
		order.set("jh_color", "".equals(jhcolor_list)?null:jhcolor_list);	// 忌讳的色系
		if(!"".equals(jh_list)){
			order.set("jihui", Db.queryStr("select group_concat(NAME) from f_flower_type where id in ("+jh_list+")"));// 忌讳的花材分类
		}
		if(!"".equals(jhcolor_list)){
			order.set("color", Db.queryStr("select group_concat(NAME) from f_color where id in ("+jhcolor_list+")"));// 忌讳的色系
		}
		order.set("cycle", cycle);		// 周期
		order.set("ctime", new Date());	// 下单日期
		order.set("type", type);		// 商品类型(兑换)
		order.set("state", 1);		// 兑换订单成功直接去评价该订单 

		order.set("yhfs",  "4," + redDetailId);   //红包兑换订单 4 + 订单详情表 id
		order.set("sy_code", jr_picode);//节日订单的批号
		
		//判断是否是首单
		Record member=Db.findById("f_account",account.get("id"));
		if(member.getInt("isbuy")==0&& MCDao.isFirstOrder(fpid)==true){
			member.set("isbuy",1);
			order.set("ishas",0);
		}else if(member.getInt("isbuy")==1){
			member.set("isbuy",2);
		}
		Db.update("f_account",member);
		
		//保存订单批号
		savePiCode(order.getStr("ordercode"),pro_flower.getStr("reachtype"),cycle,type,jr_picode,reach);

		// 保存订单
		if(Db.save("f_order", order)){
			Record order_detail_flower = new Record();
			order_detail_flower.set("ordercode", order.getStr("ordercode"));
			order_detail_flower.set("fpid", fpid);
			order_detail_flower.set("price", pro_flower.getDouble("price"));
			order_detail_flower.set("type", 1);
			boolean R = Db.save("f_order_detail", order_detail_flower);
			//订单创建 成功  修改 红包状态  发送模板消息
			if (R) {
				Db.update("update f_redpackets_detial set oid = ?,isopen = 4 where id = ?", order.getStr("ordercode"),redDetailId);
				// 绑定 apiConfig 到当前线程
				WeixinApiCtrl.setApiConfig();

				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateString = formatter.format(new Date());
				//给兑换红包的人发模板消息
				ApiResult result = TemplateMsgApi.send(TemplateData.New()
						// 消息接收者
						.setTouser(account.getStr("openid"))
						// 模板id
						.setTemplate_id(WeixinApiCtrl.gettemplateId("兑换成功通知"))
						.setTopcolor("#eb414a")
						.setUrl(Constant.getHost + "/service/myorder")
						.add("keyword1", pro_flower.getStr("name"), "#FF8040")
						.add("keyword2", "0", "#FF8040").add("keyword3", order.getStr("ordercode"), "#FF8040")
						.add("keyword4", dateString.toString(), "#FF8040")
						.add("keyword5", "红包兑换", "#FF8040").build());
				WeixinApiCtrl.sendTemplateMsg(result.getJson());
				//给发红包的人发模板消息
				Record sender=Db.findFirst("select c.openid,c.nick,b.id from f_redpackets_detial as a "
						+ "left join f_redpackets as b on a.rpid=b.id "
						+ "left join f_account as c on b.aid=c.id where a.id=?",redDetailId);
				ApiResult result1 = TemplateMsgApi.send(TemplateData.New()
						// 消息接收者
						.setTouser(sender.getStr("openid"))
						// 模板id
						.setTemplate_id(WeixinApiCtrl.gettemplateId("服务状态提醒"))
						.setTopcolor("#eb414a")
						.setUrl(Constant.getHost + "/service/seachRedpacketDetails/"+sender.getInt("id"))
						.add("first", "您的好友【"+account.getStr("nick")+"】兑换了红包", "#FF8040")
						.add("keyword1", "红包兑换:"+pro_flower.get("name"), "#FF8040")
						.add("keyword2", "兑换成功", "#FF8040")
						.add("remark", "请点击详细查看，谢谢！", "#FF8040").build());
				WeixinApiCtrl.sendTemplateMsg(result1.getJson());
			}
		}
	}
	
	
	
	/**
	 * 保存订单批号
	 * @param reachtype
	 * @return
	 */
	public static boolean savePiCode(String ordercode,String reachtype,int cycle,int orderType,String picode,int orderreach){
		boolean flag=false;
		List<String> piCodeList =new ArrayList<>() ;
		String reach= reachtype.split(":")[0];//根据商品列表找到[送达日期分类]
		if(reach.equals("3")){
			List<String> picodes= new ArrayList<>();
			picodes.add(picode);
			piCodeList=picodes;
		}else if(reach.equals("2")){
			Calendar ca = Calendar.getInstance();
			ca.add(Calendar.HOUR, 24);
			List<String> picodes= new ArrayList<>();
			picodes.add(new SimpleDateFormat("yyyyMMdd").format(ca.getTime()));//周边T+1天发货
			piCodeList=picodes;
		}
		else{
			piCodeList=DeliveryDateUtil.getPiCodeList(orderreach, new Date(), 1, cycle,orderType,0,null);
		}
		//*****保存发货批号
		for(int i=0;i<piCodeList.size();i++){
			Record f_picode = new Record();
			f_picode.set("ordercode", ordercode);
			f_picode.set("piCode", piCodeList.get(i));
			f_picode.set("reach", reach);
			f_picode.set("num",i+1 );
			flag=Db.save("f_picode", f_picode);
		}
		return flag;
	}
	
	
	// 创建红包
	public static Map<String, Object> createRedpacket(HttpServletRequest request, HttpSession session, int type, String pids, String message,String is_vase,String invoice) 
			throws ParserConfigurationException, IOException, SAXException {
		Record account = (Record) session.getAttribute("account");
		double money = 0.00;
		int num = 0;
		
		Record redPacket = new Record();
		
		if (type == 1) {
			// 闺蜜 红包
			/*money = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pids);
			num = 1;*/
			
			// 拼手气 红包
			String[] pidsArr = pids.split("\\+");
			
			
			for (int i = 0; i < pidsArr.length; i++) {
				String[] pidArr = pidsArr[i].split("_");
				for (String str : pidArr) {
					System.out.println(str);
				}
				if (!pidArr[1].equals("0")) {
					double price = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pidArr[0]);
					int tmp = Integer.parseInt(pidArr[1]);
					price *= tmp;

					money += price;
					num=num+tmp;
				}
			}
			//num = 1;
		}else {
			// 拼手气 红包
			String[] pidsArr = pids.split("\\+");
			for (int i = 0; i < pidsArr.length; i++) {
				String[] pidArr = pidsArr[i].split("_");
				if (!pidArr[1].equals("0")) {
					double price = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pidArr[0]);
					int tmp = Integer.parseInt(pidArr[1]);
					price *= tmp;

					money += price;
					num=num+tmp;
				}
			}
		}
		
		redPacket.set("invoice", invoice);// 是否需要开发票
		redPacket.set("type", type);   // 红包类型
		redPacket.set("money", money);  // 支付金额
		redPacket.set("quantity1", num); 
		redPacket.set("quantity2", 0);
		redPacket.set("msg", message);
		redPacket.set("ctime", new Date());
		redPacket.set("state", 0);
		redPacket.set("aid", account.get("id"));
		boolean R = Db.save("f_redpackets", redPacket); // 保存 红包
		//创建红包并获取到红包id
		long RPid = redPacket.get("id");
		
		//判断是否加购花瓶  这边有点问题   这个判断一直成立
	/*	if (is_vase.equals("1")) {
			money += 9.9;
			
			//获取花瓶价格
			double vasemoney = 9.9;
			
			//将信息写入 f_redpackets_detial
			Record vase  = new Record();
			vase.set("rpid", RPid); 
			vase.set("rppid", pids);
			vase.set("dmoney", vasemoney);
			vase.set("isopen", 0);
			Db.save("f_redpackets_detial", vase);
		}*/
		
		if (R) {
			if (type == 1) {
				/*Record redPacketDetail = new Record();
//				redPacketDetail.set("redpacketcode", redpacketcode); // 红包编号
				redPacketDetail.set("rpid", RPid); 
				redPacketDetail.set("rppid", pids); 
				redPacketDetail.set("dmoney", money);  
				redPacketDetail.set("isopen", 0);
				R = Db.save("f_redpackets_detial", redPacketDetail);*/
				
				// 单人红包
				String[] pidsArr = pids.split("\\+");
				
				for (int i = 0; i < pidsArr.length; i++) {
					String[] pidArr = pidsArr[i].split("_");
					if (!pidArr[1].equals("0")) {
						money = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pidArr[0]);
						
						int x = Integer.valueOf(pidArr[1]).intValue();
						for (int j = 0; j < x; j++) {
							
							Record redPacketDetail = new Record();
//							redPacketDetail.set("redpacketcode", redpacketcode); // 红包编号
							redPacketDetail.set("rpid", RPid); 
							redPacketDetail.set("rppid", pidArr[0]); // 红包 礼物id
							redPacketDetail.set("dmoney", money);  
							redPacketDetail.set("isopen", 0);
							R = Db.save("f_redpackets_detial", redPacketDetail);
						}											
					}
				}
				
			}else {
				// 拼手气 红包
				String[] pidsArr = pids.split("\\+");
				
				for (int i = 0; i < pidsArr.length; i++) {
					String[] pidArr = pidsArr[i].split("_");
					if (!pidArr[1].equals("0")) {
						money = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pidArr[0]);
						
						int x = Integer.valueOf(pidArr[1]).intValue();
						for (int j = 0; j < x; j++) {
							
							Record redPacketDetail = new Record();
//							redPacketDetail.set("redpacketcode", redpacketcode); // 红包编号
							redPacketDetail.set("rpid", RPid); 
							redPacketDetail.set("rppid", pidArr[0]); // 红包 礼物id
							redPacketDetail.set("dmoney", money);  
							redPacketDetail.set("isopen", 0);
							R = Db.save("f_redpackets_detial", redPacketDetail);
						}											
					}
				}
			}
		}
		
		
		Map<String, Object> rMap = new HashMap<>();
		if (R) {
			// 微信统一下单
			String RPidStr = RPid + "";
			String xml = FCDao.wxPushOrder("红包", RPidStr, redPacket.getDouble("money"), getRemortIP(request), account.getStr("openid"));
			rMap = XMLParser.getMapFromXML(xml);
			rMap.put("redpacketcode", RPidStr);
		}
		rMap.put("result", R);
		return rMap;
	}

	/**
	 * 创建红包
	 * 分两步
	 * @param request
	 * @param session
	 * @param type
	 * @param pids
	 * @param message
	 * @param invoice
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Map<String, Object> createRedpacket_new(HttpServletRequest request, HttpSession session, int type, String pids, String message,String invoice) 
			throws ParserConfigurationException, IOException, SAXException {
		Record account = (Record) session.getAttribute("account");
		double money = 0.00;
		int num = 0;
		
		Record redPacket = new Record();
		String[] pidsArr = pids.split("\\+");
		for (int i = 0; i < pidsArr.length; i++) {
			String[] pidArr = pidsArr[i].split("_");
			if (!pidArr[1].equals("0")) {
				double price = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pidArr[0]);
				int tmp = Integer.parseInt(pidArr[1]);
				price *= tmp;
				money += price;
				num=num+tmp;
			}
		}
		
		redPacket.set("invoice", invoice);// 是否需要开发票
		redPacket.set("type", type);   // 红包类型
		redPacket.set("money", money);  // 支付金额
		redPacket.set("quantity1",(type==1)?1:num);//送一人，quantity1=1；送多人，quantity1=礼物总数 
		redPacket.set("quantity2", 0);
		redPacket.set("msg", message);
		redPacket.set("ctime", new Date());
		redPacket.set("state", 0);
		redPacket.set("aid", account.get("id"));
		boolean R = Db.save("f_redpackets", redPacket); // 保存 红包单头
		//创建红包并获取到红包id
		long RPid = redPacket.get("id");
		
		if (R) {
			for (int i = 0; i < pidsArr.length; i++) {
				String[] pidArr = pidsArr[i].split("_");
				if (!pidArr[1].equals("0")) {
					money = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pidArr[0]);
					int x = Integer.valueOf(pidArr[1]).intValue();
					for (int j = 0; j < x; j++) {
						Record redPacketDetail = new Record();
						redPacketDetail.set("rpid", RPid); 
						redPacketDetail.set("rppid", pidArr[0]); // 红包 礼物id
						redPacketDetail.set("dmoney", money);  
						redPacketDetail.set("isopen", 0);
						R = Db.save("f_redpackets_detial", redPacketDetail);//保存红包单身
					}											
				}
			}
		}
		
		
		Map<String, Object> rMap = new HashMap<>();
		rMap.put("result", R);
		rMap.put("rpCode", RPid+"");//红包编号，用于支付
		rMap.put("type", type);//红包类型
		rMap.put("openid", account.get("openid"));
		rMap.put("rpNum", (type==1)?1:num);//红包数量
		return rMap;
	}
	
	
	
	
	// 支付成功-修改订单状态
	public static boolean paySuccess_RP(String redpacketcode) {
		
		//System.out.println("支付成功!单号是："+ redpacketcode);
		
		Record redInfo = Db.findFirst("SELECT a.aid,a.invoice,b.openid,a.quantity1,a.type FROM f_redpackets as a LEFT JOIN f_account as b ON a.aid = b.id WHERE a.id = ?",redpacketcode);
		
		//开发票
		if (redInfo.getInt("invoice") == 1) {
			WeixinApiCtrl.setApiConfig();
			String message = "需要开发票请<a href='" + Constant.getHost + "/account/receipt_apply'>点击这里</a>";
			CustomServiceApi.sendText(redInfo.getStr("openid"), message);
		}
		
		int count=1;
		if(redInfo.getInt("type")!=1){
			count=redInfo.getInt("quantity1");
		}
		
		
		
		// 调用客服接口 返回图片
		int userType=Db.queryInt("select max(userType) 'userType' from f_redpackets as a left join  f_redpackets_detial as b on a.id=b.rpid "
				+ " left join f_redpackets_pro  as c on b.rppid=c.id where a.id=?",redpacketcode);
		if(userType==2){
			sendRedCardMsg(redpacketcode);
		}else if(userType==1){
			String urlInfo=Constant.getHost+"/service/seachRedpacketDetails/"+redpacketcode;
			CustomServiceApi.sendText(redInfo.getStr("openid"), 
					"您有"+count+"个红包已准备好，等待发送给好友，好友7天内未领取的红包会给您做退款处理。一键送红包，快捷又省心！"
					+"<a href='"+urlInfo+"'>点击查看领取详情</a>");
		}
		
		boolean result = false;
		int tmp = 0;
		tmp += Db.update("update f_redpackets set state = 1 where id = ? and state = 0",redpacketcode);
		tmp += Db.update("update f_redpackets set stime = ? where id = ?",new Date(),redpacketcode);
		if (tmp == 2) {
			result = true;
		}
		return result;
	}	
	
	
	/**
	 * 给redCard 购买者发消息
	 * @param ptNo
	 */
	public static void sendRedCardMsg(String redpacketcode){
		
		//获取图片链接
		Record card = Db.findFirst("SELECT a.id,a.type,a.money,a.msg,a.ctime,a.aid,a.picId,b.typeId,b.imgurl01,b.imgurl02 from f_redpackets a "
				+ " LEFT JOIN  f_redpackets_pic b on b.id = a.picId where a.id = ?",redpacketcode) ;
		
		String imageurl = card.getStr("imgurl02");
		String str[]=imageurl.split("/");
		
		Record user = Db.findById("f_account", card.getInt("aid"));
		String openid = user.getStr("openid");
		
		//System.out.println(str[2]);
		sendPicCard(openid,card.getInt("aid"),redpacketcode,str[2]);
	}
	
	
	/**
	 * redCard 支付成功后给用户发消息
	 * @author Glacier
	 * @date 2017年10月25日
	 * @param openid
	 * @param aid
	 * @param ptNo
	 * @param picUrl
	 */
	public static void sendPicCard(final String openid, final int aid, final String redpacketcode,final String picUrl){
		new Thread(){
			public void run(){
				WeixinApiCtrl.setApiConfig();
				
				//生成二维码
				String url = DaiYanURL.getUrl(aid, 41, redpacketcode);
				//System.out.println(url);
				
		 		String outPath="/QRImage/" + aid+"-"+redpacketcode + "_1.jpg";
		 		try {
					QRCodeUtil.encode(url,"", outPath, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
		 		
		 		File file =new File(outPath);//二维码图片地址
		 		File bg=new File(Constant.imgpath+ picUrl);
		 		
		 		//System.out.println(Constant.imgpath+ picUrl);
		 		
		 		float a = (float) 0.9;
		 		BufferedImage buffImg;
				try {
					buffImg = NewImageUtils.watermark(bg, file,  580, 280, a);
					NewImageUtils.generateWaterFile(buffImg, outPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
		 		
		 		ApiResult arPic = MediaApi.uploadMedia(MediaType.IMAGE, file);
				CustomServiceApi.sendImage(openid, arPic.getStr("media_id"));

			}
		}.start();
	}
	
	
	/**
	 * 按订单开票,获取订单
	 * @param pageno
	 * @param pagesize
	 * @param aid
	 * @param state
	 * @return
	 */
	public static Page<Record> getByMyOrder(int pageno, int pagesize, int aid, Integer state){
		String select = "SELECT a.ishas,a.ordercode,a.yhfs,b.fpid,c.ptid,c.name,c.imgurl,b.price,a.cycle,a.reach,a.isvase,a.price AS totalprice,a.state,a.ctime,a.type,a.ocount,a.sendCycle,b.num ";
		String sqlExceptSelect = "FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id WHERE a.aid=? AND b.type=1 and a.state in(2,3)"
				+ " and  NOT EXISTS (SELECT d.ordercode FROM f_receipt_detail as d LEFT JOIN f_receipt as e on d.fcode=e.fcode WHERE e.state in (1,2,3) and d.ordercode=a.ordercode)";
		Page<Record> page;
		if(state == 9){
			sqlExceptSelect += " order by a.id desc";
			page = Db.paginate(pageno, pagesize, select, sqlExceptSelect, aid);
		}else{
			sqlExceptSelect += " AND a.state=? order by a.id desc";
			page = Db.paginate(pageno, pagesize, select, sqlExceptSelect, aid, state);
		}
		for(Record order : page.getList()){

			String firstDate = DeliveryDateUtil.getFirstDate(order.getStr("ordercode"));
			if(order.getInt("type")==8&&order.getInt("state")==6){
				firstDate ="成团后确定";
			}
			order.set("firstDate", firstDate);
			String imgurl = order.getStr("imgurl");
			if (imgurl.indexOf("-")!= -1) {
				String[] img = imgurl.split("-");
				order.set("imgurl", img[0]);
			}else{
				order.set("imgurl", imgurl);
			}
		}
		return page;
	}
	/**
	 * 按订单开票,获取红包/鲜花卡
	 * @param pageno
	 * @param pagesize
	 * @param aid
	 * @param state
	 * @return
	 */
	public static Page<Record> getByMyRed(int pageno, int pagesize, int aid, Integer state){
		String select = "select a.id,a.money-a.tmoney 'totalprice',a.stime,GROUP_CONCAT(c.name) 'name',max(c.userType) 'userType'";
		String sqlExceptSelect = "from f_redpackets as a  left join f_redpackets_detial b on a.id=b.rpid left join f_redpackets_pro c on b.rppid=c.id where a.aid=? and a.state=2 and stime is not null "
				+ " and  NOT EXISTS (select a.id from f_receipt_detail as d where a.id=d.ordercode)"
				+ "group by a.id";
		Page<Record> page = Db.paginate(pageno, pagesize, select, sqlExceptSelect, aid);;
		return page;
	}
	
	// 生成发票编号
	public static String getFCode(){
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return "fc"+sdf.format(now.getTime());
	}
	
	public static Map<String, Object> createReceipt(HttpServletRequest request, HttpSession session, String company, String code, String content, String name, String tel, String area, String addr) throws ParserConfigurationException, IOException, SAXException {
		// TODO Auto-generated method stub
		
		resultMap = new HashMap<>();
		Account = (Record) session.getAttribute("account");
		Receipt = new Record(); // 发票对象
		boolean R = false;
		
		R = Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean result = false;

				Map<String, Object> priceMap = countPriceForOrder(Account.getInt("id"), Pid, Cycle, Vase, Cash,
						Activity, Yh,Cash2,Num,false);
				int ptid=Db.queryInt("select ptid from f_flower_pro where id=?",Pid);
				return result;
			}
		});
		if (R) {
			List<Record> detaillist = Db.find(
					"SELECT * FROM f_receipt WHERE a.fcode=?",
					Receipt.getStr("fcode"));
			// 微信统一下单
			String xml = FCDao.wxPushOrder(Receipt.getStr("name"), Receipt.getStr("fcode"),
					Receipt.getDouble("postage"), getRemortIP(request), Account.getStr("openid"));
			resultMap = XMLParser.getMapFromXML(xml);
			for (Record detail : detaillist) {
				String imgurl = detail.getStr("imgurl");
				if (imgurl.indexOf("-") > 0) {
					String[] img = imgurl.split("-");
					detail.set("imgurl", img[0]);
				}
			}
			resultMap.put("detaillist", detaillist);
			resultMap.put("fcode", Receipt.getStr("fcode"));
			resultMap.put("ptNo", Receipt.getLong("ptNo"));
		} else {
			Account.set("isbuy", isBuy);
		}
		resultMap.put("result", R);
		return resultMap;
	}
	
	/**
	 * 发票中所含订单
	 * @param pageno
	 * @param pagesize
	 * @param aid
	 * @param state
	 * @return
	 */
	public static Page<Record> getMyOrderCon(int pageno, int pagesize, int aid, Integer state){
		String select = "SELECT a.ishas,a.ordercode,c.name,c.imgurl,b.price,a.cycle,a.reach,a.isvase,a.price AS totalprice,a.ctime,a.type,a.sendCycle,b.num,d.fcode";
		String sqlExceptSelect = "FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id left join f_receipt_detail d on a.ordercode=d.ordercode LEFT JOIN f_receipt e on d.fcode=e.fcode WHERE e.aid=? AND d.fcode=?";
		Page<Record> page;
		sqlExceptSelect += " order by a.id desc";
		page = Db.paginate(pageno, pagesize, select, sqlExceptSelect, aid);
		for(Record order : page.getList()){

			String firstDate = DeliveryDateUtil.getFirstDate(order.getStr("ordercode"));
			order.set("firstDate", firstDate);
			String imgurl = order.getStr("imgurl");
			if (imgurl.indexOf("-")!= -1) {
				String[] img = imgurl.split("-");
				order.set("imgurl", img[0]);
			}else{
				order.set("imgurl", imgurl);
			}
		}
		return page;
	}
		
	
	
	/**
	 * 创建卡片
	 * @author Glacier
	 * @date 2017年10月18日
	 * @param request
	 * @param session
	 * @param type
	 * @param pids
	 * @param message
	 * @param is_vase
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Map<String, Object> createRedcard(HttpServletRequest request, HttpSession session, int type, String pids, String message,String is_vase,String cardid) 
			throws ParserConfigurationException, IOException, SAXException {
		Record account = (Record) session.getAttribute("account");
		double money = 0.00;
		int num = 0;
		
		Record redPacket = new Record();
		
		if (type == 1) {
			// 闺蜜 红包
			money = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pids);
			num = 1;
			
		}else {
			// 拼手气 红包
			String[] pidsArr = pids.split("\\+");
			for (int i = 0; i < pidsArr.length; i++) {
				String[] pidArr = pidsArr[i].split("_");
				if (!pidArr[1].equals("0")) {
					double price = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pidArr[0]);
					int tmp = Integer.parseInt(pidArr[1]);
					price *= tmp;

					money += price;
					num++;
				}
			}
		}
		
		redPacket.set("type", type);   // 红包类型
		redPacket.set("money", money);  // 支付金额
		redPacket.set("quantity1", num); 
		redPacket.set("quantity2", 0);
		redPacket.set("msg", message);
		redPacket.set("ctime", new Date());
		redPacket.set("state", 0);
		redPacket.set("picId", cardid);
		redPacket.set("aid", account.get("id"));
		boolean R = Db.save("f_redpackets", redPacket); // 保存 红包
		//创建红包并获取到红包id
		long RPid = redPacket.get("id");
		
		//判断是否加购花瓶  这边有点问题   这个判断一直成立
	/*	if (is_vase.equals("1")) {
			money += 9.9;
			
			//获取花瓶价格
			double vasemoney = 9.9;
			
			//将信息写入 f_redpackets_detial
			Record vase  = new Record();
			vase.set("rpid", RPid); 
			vase.set("rppid", pids);
			vase.set("dmoney", vasemoney);
			vase.set("isopen", 0);
			Db.save("f_redpackets_detial", vase);
		}*/
		
		if (R) {
			if (type == 1) {
				Record redPacketDetail = new Record();
//				redPacketDetail.set("redpacketcode", redpacketcode); // 红包编号
				redPacketDetail.set("rpid", RPid); 
				redPacketDetail.set("rppid", pids); 
				redPacketDetail.set("dmoney", money);  
				redPacketDetail.set("isopen", 0);
				R = Db.save("f_redpackets_detial", redPacketDetail);
			}else {
				// 拼手气 红包
				String[] pidsArr = pids.split("\\+");
				
				for (int i = 0; i < pidsArr.length; i++) {
					String[] pidArr = pidsArr[i].split("_");
					if (!pidArr[1].equals("0")) {
						money = Db.queryDouble("SELECT pmoney FROM f_redpackets_pro WHERE id = ?",pidArr[0]);
						
						int x = Integer.valueOf(pidArr[1]).intValue();
						for (int j = 0; j < x; j++) {
							
							Record redPacketDetail = new Record();
//							redPacketDetail.set("redpacketcode", redpacketcode); // 红包编号
							redPacketDetail.set("rpid", RPid); 
							redPacketDetail.set("rppid", pidArr[0]); // 红包 礼物id
							redPacketDetail.set("dmoney", money);  
							redPacketDetail.set("isopen", 0);
							R = Db.save("f_redpackets_detial", redPacketDetail);
						}											
					}
				}
			}
		}
		
		
		Map<String, Object> rMap = new HashMap<>();
		if (R) {
			// 微信统一下单
			String RPidStr = RPid + "";
			String xml = FCDao.wxPushOrder("红包", RPidStr, redPacket.getDouble("money"), getRemortIP(request), account.getStr("openid"));
			rMap = XMLParser.getMapFromXML(xml);
			rMap.put("redpacketcode", RPidStr);
		}
		rMap.put("result", R);
		return rMap;
	}
}


