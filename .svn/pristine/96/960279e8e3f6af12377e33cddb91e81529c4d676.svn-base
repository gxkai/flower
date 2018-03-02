package com.controller.front;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.controller.WeixinApiCtrl;
import com.dao.FCDao;
import com.dao.GroupDAO;
import com.google.gson.Gson;
import com.interceptor.FrontInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.json.JFinalJson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.MediaApi;
import com.jfinal.weixin.sdk.api.MediaApi.MediaType;
import com.jfinal.weixin.sdk.api.QrcodeApi;
import com.jfinal.weixin.sdk.api.UserApi;
import com.util.CharFliter;
import com.util.Constant;
import com.util.Constant.seedType;
import com.util.DaiYanURL;
import com.util.DataCal;
import com.util.DesUtil;
import com.util.DownloadFile;
import com.util.MD5Util;
import com.util.NewImageUtils;
import com.util.QRCodeUtil;
import com.util.QianNiuUpload;
import com.util.SendMsgUtil;

import sun.misc.BASE64Encoder;

/**
 * @Desc 会员相关
 * @author lxx
 * @date 2016/8/31
 * */
@Before(FrontInterceptor.class)
public class FrontAccountCtrl extends Controller {
	// 会员中心
	public void center(){
		Record account_1 = getSessionAttr("account");
		WeixinApiCtrl.setApiConfig();
		ApiResult ar = UserApi.getUserInfo(account_1.getStr("openid"));
		account_1.set("nick",ar.get("nickname").toString());//昵称
//		account_1.set("province", ar.get("province").toString());//用户所在省份
//		account_1.set("city", ar.get("city").toString());//用户所在城市
		/*try {
			account_1.set("nick", CharFliter.filterOffUtf8Mb4_2(ar.get("nickname").toString()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
		account_1.set("headimg", ar.get("headimgurl"));
		Db.update("f_account", account_1);
		Record account = Db.findFirst("select * from f_account where id=?",account_1.getInt("id"));
		setAttr("account", account);
		// 检测今日是否签到
		Number num = Db.queryNumber("select count(1) from f_flowerseed where aid=? and type=? and date_format(ctime,'%Y-%m-%d')=curdate()", account.getInt("id"), seedType.sign.name());
		setAttr("sign", num.intValue());
		setAttr("ordercount", FCDao.orderCount(account.getInt("id")));
		setAttr("yqhy", FCDao.getInviteFriend());
		setAttr("cashcount", Db.queryLong("SELECT COUNT(id) FROM f_cash WHERE state=1 AND aid=? and CURDATE()>=time_a AND CURDATE()<=time_b", account.getInt("id")));
		setAttr("img",Db.queryStr("select img from f_masklayer where layeruse=1 and state=1"));
		setAttr("img2",Db.queryStr("select img from f_masklayer where layeruse=2 and state=1"));//点签到
		render("center.html");
	}
	
	//幸运榜
	public void luckylist(){
		//今天
		List<Record> signlist = Db.find("select a.id, a.content,a.figs,DATE_FORMAT(a.ctime,'%Y.%m.%d') as ctime,b.headimg,left(b.nick,6) as nick from f_sign18 a left join f_account b ON a.aid = b.id WHERE to_days(a.ctime) = to_days(now()) ORDER BY figs DESC");
		setAttr("signlist", signlist);
		//本周
		List<Record> signlist_week = Db.find("select a.id, a.content,a.figs,DATE_FORMAT(a.ctime,'%Y.%m.%d') as ctime,b.headimg,left(b.nick,6) as nick from f_sign18 a left join f_account b ON a.aid = b.id WHERE YEARWEEK(date_format(a.ctime,'%Y-%m-%d')) = YEARWEEK(now()) ORDER BY figs DESC");
		setAttr("signlist_week", signlist_week);
		render("luckylist.html");
	}

	//花艺课代颜排行榜
	public void hykdy(){
		List<Record> hykdylist = Db.find("select id,countM,nick,headimg from (select SUBSTRING(tjid,3) 'tjid',count(1) 'countM' from f_account where tjid like '5_%' and isfans=0 group by SUBSTRING(tjid,5)) as a left join f_account as b on a.tjid=b.id ORDER BY countM DESC");
		setAttr("hykdylist", hykdylist);
		render("hykdy.html");
	} 
	
	// 我的资料
	public void myInfo() {
		Record account = getSessionAttr("account");	
		Record info = Db.findFirst("SELECT tel, birthday, sex FROM f_account WHERE id = ?",account.getInt("id"));
		if (info.get("tel") != null) {
			setAttr("tel", info.get("tel"));
		}
		if (info.get("birthday") != null) {
			Date birthday = info.get("birthday");
			String birth = (new SimpleDateFormat("yyyy年MM月dd日")).format(birthday);
			setAttr("birthday", birth);
		}
		
		setAttr("sex", info.get("sex"));
		render("person_info.html");
	}
	//修改个人信息
	public void changeMyInfo() {
		String birthday = getPara("birthday");
		int sex = getParaToInt("sex");	
		renderJson(FCDao.changePersonInfo( birthday, sex, getSession()));
	}
	// 签到
	public void signin(){
		Record account = getSessionAttr("account");
		renderJson(FCDao.signin(account.getStr("openid"),account.getInt("id")));
	}
	
	/**
	 * 今天第几位签到成功的
	 */
	public void signinCount(){
		Record account = getSessionAttr("account");
		renderJson(FCDao.signinCount(account.getInt("id")));
	}
	/**
	 * 今天幸运签到成功的
	 */
	public void signinCount18(){
		Record account = getSessionAttr("account");
		Integer aid = account.getInt("id");
		Long data= FCDao.signinCount(aid);
		String xyqd = Db.queryStr("select code_value from f_dictionary where code_key = 'xyqd'");
		String[] arr = xyqd.split(",");
		boolean result= Arrays.asList(arr).contains(data.toString());
		if (result) {
			renderJson(result);return;
		}
		renderJson(result);
	}
	/**
	 * 写心情
	 */
	public void sharesign18(){
		Integer id = getParaToInt(0);
		if (id!=null) {
			Record record = Db.findById("f_sign18", id);
			setAttr("id", id);
			setAttr("content", record.getStr("content"));
			setAttr("figs", record.getInt("figs"));
			Date date = record.getTimestamp("ctime");
			setAttr("date", new SimpleDateFormat("yyyy年MM月dd日").format(date));
			setAttr("year", new SimpleDateFormat("yy").format(date));
			setAttr("month", new SimpleDateFormat("MMMM",Locale.ENGLISH).format(date));
			setAttr("day", new SimpleDateFormat("dd").format(date));
		}else {
			Date date = new Date();
			setAttr("date", new SimpleDateFormat("yyyy年MM月dd日").format(date));
			setAttr("year", new SimpleDateFormat("yy").format(date));
			setAttr("month", new SimpleDateFormat("MMMM",Locale.ENGLISH).format(date));
			setAttr("day", new SimpleDateFormat("dd").format(date));
		}
		render("sharesign18.html");
	}
	/**
	 * 提交心情
	 * @throws Exception 
	 */
	public void sharesign18save() throws Exception {
		Map<String, Object> map = new HashMap<>();
		Record account = getSessionAttr("account");
		Integer aid = account.getInt("id");
		String openId = account.getStr("openid");
		Long data= FCDao.signinCount(aid);
		Number num = Db.queryNumber("select count(1) from f_sign18 where aid = ? and date_format(ctime,'%Y-%m-%d')=curdate() ",aid);
		String xyqd = Db.queryStr("select code_value from f_dictionary where code_key = 'xyqd'");
		String[] arr = xyqd.split(",");
		boolean result= Arrays.asList(arr).contains(data.toString());
		if (result&&num.intValue()==0) {
			String content = getPara("content");
			Record record = new Record();
			record.set("aid", aid);
			record.set("content", content);
			record.set("ctime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			record.set("figs", 0);
			result= Db.save("f_sign18", record);
			if (result) {
				FCDao.Addseeds(aid,openId);
				Integer id = Db.queryInt("select id from f_sign18 where aid = ? order by ctime desc limit 1",aid);
				map.put("id", id);
				String msg = String.format("以下是会员%d提交的心情,请审核,若不通过,请到会员管理-幸运签到列表修改其状态:%s",aid,content);
				SendMsgUtil.sendMsg("18012698246",msg);
			}
		}else {
			result=false;
		}
		map.put("result", result);
		renderJson(map);
		
	}
	
	/**
	 * 分享心情
	 */
	public void getsharesign18() {
		Integer id = getParaToInt(0);
		if (id==null) {
			renderText("未提交的分享= ="); return;
		}
		Record record = Db.findById("f_sign18", id);
		if (record.getInt("state")==1) {
			renderText("言论不和谐,已被管理员删除"); return;
		}
		Record account_dz = getSessionAttr("account");
		Number num = Db.queryNumber("select count(1) from f_sign18_detail where sid=? and aidfig=?",id,account_dz.getInt("id"));
		Date date = record.getTimestamp("ctime");
		String luckday = new SimpleDateFormat("yyyy-MM-dd").format(date);
		setAttr("date",new SimpleDateFormat("yyyy年MM月dd日").format(date) );
		Date lucktime = Db.queryTimestamp("select ctime from f_flowerseed where  type='sign' and date_format(ctime,'%Y-%m-%d')=? and aid=? ORDER BY id  LIMIT 1",luckday,record.getInt("aid"));
		setAttr("lucktime", new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒").format(lucktime));
		Record account = Db.findFirst("select * from f_account where id=?",record.getInt("aid"));
		List<Record>list = Db.find("SELECT b.headimg from f_sign18_detail a  LEFT JOIN f_account b on a.aidfig = b.id  WHERE sid =?",id);
		setAttr("list", list);
		setAttr("account", account);
		setAttr("id", id);
		setAttr("content", record.getStr("content"));
		setAttr("figs", record.getInt("figs"));
		setAttr("num", num.intValue());
		render("getsharesign18.html");
	   
	}
	/**
	 * 点赞
	 */
	public void sharesign18zhan() {
		Map<String, Object> map = new HashMap<>();
		boolean result = false;
		Record account = getSessionAttr("account");
		Integer aid = account.getInt("id");
		Integer id = getParaToInt(0);
		Number num = Db.queryNumber("select count(1) from f_sign18_detail where sid = ? and aidfig = ?",id,aid);
		if (num.intValue()==0) {
			Integer figs = Db.queryInt("select figs from f_sign18 where id = ?",id);
			figs = figs+1;
			Record record = new Record();
			record.set("figs",figs );
			record.set("id",id );
		    Db.update("f_sign18", record);
			Record newRecord = new Record();
			newRecord.set("aidfig", aid);
			newRecord.set("sid", id);
			newRecord.set("figtime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			Db.save("f_sign18_detail", newRecord);
			map.put("figs",figs );
			String headimg = Db.queryStr("SELECT b.headimg from f_sign18_detail a  LEFT JOIN f_account b on a.aidfig = b.id  WHERE a.aidfig =?",aid);
			map.put("headimg", headimg);
			result = true;
		}	
		map.put("result",result );
		renderJson(map);
		
	}

	/**
	 * 发送18粒花籽
	 */
	public void sendSeeds18(){
		Record account = getSessionAttr("account");
		
		renderJson(FCDao.sendSeeds18(account.getStr("openid"),account.getInt("id")));
	}
	// 绑定手机号
	public void binding(){
		render("binding.html");
	}
	// 获取验证码
	public void getBindingCode() throws Exception{
		String number = getPara("number");
		int result = SendMsgUtil.sendMsg(number, getSession());
		renderJson(result);
	}
	// 保存绑定号码
	public void saveBinding(){
		String number = getPara("number");
		String msgcode = getPara("msgcode");
		String bindingcode = getSessionAttr("bindingcode");
		renderJson(FCDao.saveBinding(number, msgcode, bindingcode, getSession()));
	}
/******************************************************地址相关*******************************************************/
	// 我的地址
	public void address(){
		Record account = getSessionAttr("account");
		setAttr("addresslist", FCDao.getAddress(account.getInt("id")));
		render("address.html");
	}
	// 新增地址
	public void addAddress(){
		// 跳转参数
		String queryString = getRequest().getQueryString();
		setAttr("queryString", queryString);
		setAttr("areajson", FCDao.getArea());
		render("address_add.html");
	}
	// 修改地址-获得详细
	public void getAddress(){
		int id = getParaToInt("id");
		// 跳转参数
		String queryString = getRequest().getQueryString();
		setAttr("queryString", queryString);
		setAttr("areajson", FCDao.getArea());
		setAttr("address", Db.findById("f_address", id));
		render("address_detail.html");
	}
	// 保存地址
	public void saveAddress(){
		Integer id = getParaToInt("id");
		Integer state = getParaToInt("state");
		String name = getPara("name");
		String tel = getPara("tel");
		String area = getPara("area");
		String addr = getPara("addr").trim().replace(" ", "");
		int give = getParaToInt("give");
		Record account = getSessionAttr("account");
		// boolean result = FCDao.saveAddress(id, state, account.getInt("id"), name, tel, area, addr, give);
		// 保存的地址  即"默认地址"
		boolean result = FCDao.saveAddress(id, 1, account.getInt("id"), name, tel, area, addr, give);
		
		renderJson(result);
	}
	// 设置默认地址
	public void setDefault(){
		int id = getParaToInt("id");
		Record account = getSessionAttr("account");
		renderJson(FCDao.setDefault(id, account.getInt("id")));
	}
	// 删除地址
	public void delAddress(){
		int id = getParaToInt("id");
		renderJson(Db.deleteById("f_address", id));
	}
	// 意见反馈
	public void feedback(){
		render("feedback.html");
	}
	// 提交反馈
	public void saveFeedback(){
		String title = getPara("title");
		String info = getPara("info");
		Record account = getSessionAttr("account");
		renderJson(FCDao.saveFeedback(title, info, account.getInt("id")));
	}
	// 兑换卡 鲜花兑换
	public void exchange() {
		String cardNum = getPara();
		setAttr("cardNum", cardNum);

		setAttr("areajson", FCDao.getArea());
		render("exchange_code.html");
	}
/******************************************************花票相关*******************************************************/
	// 我的花票
	public void mycash(){
		Record account = getSessionAttr("account");
		setAttr("mycash", FCDao.getCashList(account.getInt("id")));
		render("mycash.html");
	}
	/**
	 * 花票分享 ---> 带颜url  让没有关注的点进来关注 可能是多张花票
	 * @author Glacier
	 * @date 2017年9月21日
	 */
	public void sharecash(){
		String cashid = getPara();
		List<Record> sharelist = FCDao.getFriendCash(cashid);
		setAttr("account", getSessionAttr("account"));
		setAttr("sharelist", sharelist);
		setAttr("cashid", cashid);
		
		Record user = getSessionAttr("account");
		String url = DaiYanURL.getUrl(user.getInt("id"), 51, cashid);
		
		setAttr("url", url);
		//System.out.println("生成的url:"+ url);
		render("sharecash.html");
	}
	// 删除赠送好友的花票
	public void deletecash(){
		String cl = getPara();
		Record account = getSessionAttr("account");
		int uNum = Db.update("update f_cash set state = 0,origin = 5,aid_f = ? where id in ("+cl+") and aid =?", account.getInt("id"), account.getInt("id")); // 赠送好友的花票状态为0：未激活
		renderJson(uNum==0?false:true);
	}
	// 领花票引导页
	public void getgfcash(){
		String cl = getPara();
		List<Record> fcashlist = FCDao.getCashFriend(cl);
		
		Integer aid = FCDao.getcashforId(cl);
		Record account = getSessionAttr("account");
		if(account.getInt("id").equals(aid)){
			setAttr("self", false);
		}else{
			setAttr("self", true);
		}
		setAttr("cl",cl);
		setAttr("fcashlist", fcashlist);
		
		// 带颜二维码生成
		String url = DaiYanURL.getUrl(account.getInt("id"), 51, cl);
		//System.err.println("url:"+ url);
		setAttr("url", url);
		
		render("friendcash.html");
	}
	// 好友领取花票
	public void receiveFriCash(){
		String cid = getPara();
		boolean result = false;
		String msg = new String();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Record account_session = getSessionAttr("account");
		int aid_f = Db.queryInt("select distinct aid_f from f_cash where id in ("+cid+") and state = 0 and origin = 5");
		if(aid_f == account_session.getInt("id")){
			msg = "此花票已赠送";
		}else{
			Record account = Db.findById("f_account", account_session.getInt("id"));
			if(account.getInt("isfans")==0){
				int gNum = Db.update("update f_cash set aid =?,state=1 where id in ("+cid+") and state=0 and origin = 5", account.getInt("id"));
				result = gNum==0?false:true;
				if(result){
					msg = "领取成功";
				}else{
					msg = "不能重复领哦";
				}
			}else{
				msg = "先关注花美美才能领取好友花票哦！";
			}
		}
		resultMap.put("result", result);
		resultMap.put("msg", msg);
		renderJson(resultMap);
	}
	// 领取花票
	public void receiveCash(){
		Record account = getSessionAttr("account");
		int  themeId=getParaToInt(0);
		String md5Str=getPara(1);
		//很无奈的加了这个判断，这个bug找了一天也没找到原因
		if(md5Str.equals("scx870110")){
			setAttr("list", FCDao.receiveCash(account.getInt("id"),themeId));
		}
		else if(md5Str.equals(MD5Util.getMd5str(themeId))){
			setAttr("list", FCDao.receiveCash(account.getInt("id"),themeId));
		}
		
		setAttr("themeId", themeId);
		setAttr("md5Str",md5Str);
		
		String url = DaiYanURL.getUrl(account.getInt("id"), 5, themeId+"-"+MD5Util.getMd5str(themeId));
		System.err.println("url:"+ url);
		
		setAttr("url", url);
		render("receivecash.html");
	}
	
	// 激活花票
	public void activateCash(){
		int id = getParaToInt("id");
		String code = getPara("code");
		Calendar now = Calendar.getInstance();
		// 有效天数
		Record record = Db.findFirst("SELECT c.etime, c.ltime FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id LEFT JOIN f_cashtheme c ON b.tid=c.id WHERE a.id=?", id);
		now.add(Calendar.DAY_OF_MONTH, record.getInt("ltime"));
		Date newTime = now.getTime();
		if (now.getTime().compareTo(record.getDate("etime"))>=0) {
			newTime = record.getDate("etime");
		}
		int result = Db.update("update f_cash set state=1,time_a=?,time_b=? where id=? and code=? and state=0", new Date(), newTime, id, code);
		renderJson(result==1?true:false);
	}
	// 领取花票页面   注释部分没有测试 参数md5加密 id+密文
	public void cashexact(){
		Record account = getSessionAttr("account");
		int themeId = getParaToInt(0);
		String MD5str = getPara(1);
		
		if (MD5Util.getMd5str(themeId).equals(MD5str)) {
			
			List<Record> list = Db.find("SELECT a.state,a.time_a,a.time_b,b.money,b.benefit,b.ptid, IFNULL(c.`name`,'通用') as `name` "
					+ " FROM f_cash a "
					+ " LEFT JOIN f_cashclassify b ON a.cid=b.id "
					+ " LEFT JOIN f_flower_pro c ON c.id = b.ptid "
					+ " where a.aid = ? and CURDATE()>=a.time_a AND CURDATE()<=a.time_b AND b.tid = ?", account.getInt("id"),themeId);
			if (list != null) {
				setAttr("list", list);
			}
			setAttr("type", 1);
			setAttr("themeId", themeId);
			setAttr("MD5str",MD5str);
		
			render("cash_exact.html");
		}else {
			setAttr("type", 1);
			setAttr("themeId", themeId);
			setAttr("MD5str",MD5str);
			render("cash_exact.html");
		}
	}
	// 激活花票
	public void getcashexact(){
		Record account = getSessionAttr("account");
		int type = getParaToInt("type");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String msg = "";
		boolean result = false;
		String ids = new String();
		if(type==1){
			int themeId = getParaToInt("themeId");
			Date etime = Db.queryDate("select etime from f_cashtheme where id =?",themeId);
			Date dt = new Date();
			if (etime.compareTo(dt)<0) {
				msg = "花票已过期";
				resultMap.put("result", result);
				resultMap.put("msg", msg);
				resultMap.put("aid", account.getInt("id"));
				renderJson(resultMap);return;
			}
			ids = Db.queryStr("SELECT GROUP_CONCAT(b.id) FROM f_cashtheme a LEFT JOIN f_cashclassify b ON a.id = b.tid WHERE b.state = 0 AND a.id = ?",themeId);
		}else{
			ids = Db.queryStr("SELECT GROUP_CONCAT(b.id) FROM f_cashtheme a LEFT JOIN f_cashclassify b ON a.id = b.tid left join f_dictionary c on a.name = c.code_value WHERE c.code_key = 'cash' AND b.state = 0");
		}
		Number hpNum = Db.queryNumber("select count(1) from f_cash where state = 0 and aid =? and cid in ("+ids+")", account.getInt("id"));
		
		if(hpNum.intValue() > 0){
			Number gqNum = Db.queryNumber("select count(1) from f_cash where state = 0 and aid =? and cid in ("+ids+") and time_b > now()", account.getInt("id"));
			if(gqNum.intValue() > 0){
				Db.update("update f_cash set state = 1 where aid =? and cid in ("+ids+") and state = 0", account.getInt("id"));
				result = true;
				msg = "花票领取成功";
				
				WeixinApiCtrl.setApiConfig();
				String message =String.format("您已成功领取了%d张花美美的花票\n<a href='http://www.flowermm.net/account/mycash'>【点击查看】</a>\n", ids.split(",").length) ;
				CustomServiceApi.sendText(account.getStr("openid"), message);
			}else{
				msg = "该花票已过期";
			}
		}else{
			msg = "你已领取了该花票";
		}
		resultMap.put("result", result);
		resultMap.put("msg", msg);
		resultMap.put("aid", account.getInt("id"));
		renderJson(resultMap);
	}
	// 我的种植花束(花籽兑换)
	public void flowerseed(){
		Record account = getSessionAttr("account");
		setAttr("account", account);
		setAttr("seedscount", FCDao.getFlowerSeed_new(account.getInt("id")));
		render("flowerseed.html");
	}
	// 兑换花束页面
	/*public void flowersubs(){
		Record account = getSessionAttr("account");
		setAttr("seedcount", FCDao.getFlowerSeed(account.getInt("id")));
		render("flowerchange.html");
	}*/
	
	// 邀请好友
	public void invitefri() throws Exception{
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		Integer type = getParaToInt(1)==null?1:getParaToInt(1);
		setAttr("type", type);
		Record account = getSessionAttr("account");
		String idStr = new DesUtil().encrypt(account.getInt("id").toString());
		setAttr("scancode", idStr);
		setAttr("dimem", 1);  //区分 地推人员二维码 和 会员二维码
		Page<Record> page = null;
		if(type == 1){
			/*String url = account.getStr("qrurl");
			if(url == null){
				url = DaiYanURL.getUrl(account.getInt("id"), 1);
				account.set("qrurl", url);
				Db.update("f_account", account);
			}*/
			String url = DaiYanURL.getUrl(account.getInt("id"), 1);
			account.set("qrurl", url);
			Db.update("f_account", account);
			setAttr("qrurl", account.getStr("qrurl"));
			setAttr("headimg", base64Encode(account.getStr("headimg")));
		}else{
			int aid = account.getInt("id"); 
			page = FCDao.findOrderMember(pageno, 16, aid, 1);
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("ormems",page.getList());
		}
		setAttr("yqhy", FCDao.getInviteFriend());
		if(pageno == 1){
			render("invitefriend.html");
		}else{
			renderJson(page.getList());
		}
	}
	public void freshPersonInfo() {
		Record account = getSessionAttr("account"); 
		boolean R =FCDao.freshAccount(account.getStr("openid"));
		renderJson(R);
	}
	
	/**
	 * 查看祝福卡
	 * http://www.flowermm.net/account/seecardinfo
	 */
	public void seecardinfo(){
		int type = getParaToInt()==null?2:getParaToInt(); // 1:使用其他手机；2:扫二维码
		if(type==2){
			Record account = getSessionAttr("account");
			Record user = Db.findFirst("select tel,isfans from f_account where id=?", account.getInt("id"));
			if(user.getInt("isfans")==1){
				setAttr("msg", "请先关注花美美，再查看祝福卡");
			}
			setAttr("tel", user.getStr("tel"));
		}
		render("seecard.html");
	}
	
	// 获得祝福卡
	public void cardget(){
		String tel = getPara("tel");
		List<Record> cards = FCDao.getCards(tel);
		Long num = Db.queryLong("SELECT COUNT(*) FROM f_order WHERE tel = ? AND state in (1,2,3) and zhufu is not null", tel) ;
		setAttr("cards", cards);
		setAttr("num", num);
		render("card_get.html");
	}
	
	// 祝福卡内容
	public void cardcontent(){
		String ordercode = getPara();
		Record account = getSessionAttr("account");
		Integer id = account.getInt("id");
		int state=0;//默认为收货人的手机号和用户手机号不相等
		String tel1=Db.queryStr("SELECT tel from f_order WHERE ordercode=?",ordercode);
		String tel2=Db.queryStr("SELECT tel from f_account WHERE id=?",id);
		if(tel1.equals(tel2)){
			state=1;
		}
		Record wish=Db.findFirst("SELECT a.zhufu,a.songhua,b.imgurl01,a.picId FROM f_order as a left join f_wishcardpic as b on a.picId=b.id WHERE ordercode=?",ordercode);		
		setAttr("state", state);
		setAttr("wish", wish);
		render("card_detail.html");
	}
	
/*************************************************************送出的鲜花****************************************************************/

	//获取我送出的鲜花
	public void getUsers() {
		Record account = getSessionAttr("account");
		Integer id = account.getInt("id");
		List<Record> userList = FCDao.getUser(id);
		
		for( Record user : userList){
			String imgurl = user.getStr("imgurl");
			if (imgurl.indexOf("-")!= -1) {
				String[] img = imgurl.split("-");
				user.set("imgurl", img[0]);
			}else{
				user.set("imgurl", imgurl);
			}
		}
		
		List<Record> cards = Db.find("SELECT a.name,a.ordercode,a.ctime,b.imgurl01,a.picId,a.tel FROM f_order as a "
				+"left join f_wishcardpic as b on a.picId=b.id "
				+"WHERE a.aid=? and a.state in (1,2,3) and a.zhufu is not NULL and a.tel <> (SELECT tel from f_account WHERE id=?)",id,id);
		
		setAttr("userList", userList);
		setAttr("cards", cards);
		render("recive_flower.html");
	}
	
	//获取礼物详情
	public void getGiftsInfo() {
		
		String ordercodes = getPara();
		String[] ordercode = ordercodes.split("-");
		List<Record> infoList = new LinkedList<>();
		for (int i = 0; i < ordercode.length; i++) {
			Record info = Db.findFirst("SELECT a.gName, a.ctime, a.zhufu, b.fpid,c.imgurl "
					+ "FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id  "
					+ "WHERE a.ordercode = ?;",ordercode[i]);
			String imgs = info.getStr("imgurl");

			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				info.set("imgurl1", ims[0]);
				info.set("imgurl2", ims[1]);
				info.set("imgurl3", ims[2]);
			}else{
				info.set("imgurl1", imgs);
				info.set("imgurl2", imgs);
				info.set("imgurl3", imgs);
			}
			Date cTime = info.getDate("ctime");
			String tmp = (new SimpleDateFormat("yyyy年MM月dd日")).format(cTime);	
			info.set("ctime", tmp);
			infoList.add(i, info); 
		}
		setAttr("infoList", infoList);
		render("recive_flower_detail.html");
	}
/***************************************************************红包********************************************************/
	// 我的红包 -（收到的红包   发送的红包）
	public void myRedPackets () {
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		Record account = getSessionAttr("account");
		int id = account.getInt("id");
		
		if (pageno == 1) {
			/*  我发的红包  */
			setAttr("pageno", pageno);
			List<Record> redPacketList = FCDao.getMyRedPackets(pageno,id);
			/*for (int i = 0; i < redPacketList.size(); i++) {
				String imgs = redPacketList.get(i).getStr("imgurl");
				if(imgs.indexOf("-")!= -1){
					String[] ims = imgs.split("-");
					redPacketList.get(i).set("imgurl", ims[0]);
				}
			}*/
			setAttr("redPacketList",redPacketList);
		}else {
			/* 我收到的红包 */
			setAttr("pageno", pageno);
			
			List<Record> redPacketList = FCDao.getMyRedPackets(pageno,id);
			for (int i = 0; i < redPacketList.size(); i++) {
				
				String nick = Db.queryStr("SELECT nick FROM f_account WHERE id = ? ",redPacketList.get(i).getInt("aid"));
				redPacketList.get(i).set("nick", nick);
				
				
				//就算过期时间
				Date dd = redPacketList.get(i).get("ctime");
				String dt = DataCal.dateToString(dd);
				String time =  DataCal.dataAdd(dt, 7);
				redPacketList.get(i).set("time", time);
				
				String imgs = redPacketList.get(i).getStr("imgurl");
				if(imgs.indexOf("-")!= -1){
					String[] ims = imgs.split("-");
					redPacketList.get(i).set("imgurl", ims[0]);
				}else{
					redPacketList.get(i).set("imgurl", imgs);
				}
				// 是否已兑换订单
				if (redPacketList.get(i).getStr("oid") != null) {
					redPacketList.get(i).set("oid",0); // 已兑换
				}else {
					redPacketList.get(i).set("oid",1);
				}
			}
			setAttr("redPacketList",redPacketList);
		}
		render("redPacket_my.html");
	}
	
	/**
	 * 红包玩法详情
	 * @author Glacier
	 * @date 2017年9月13日
	 */
	public void redPacket_playInfo() {
		render("redPacket_playInfo.html");
	}
	
	public static String base64Encode(String imgurl) throws IOException {
		URL url = new URL(imgurl);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
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

	//验证码
	public void code(){ 
		renderCaptcha(); 
	}
	
	
	/***
	 * 花籽商城页面渲染
	 * @author Glacier
	 * @date 2017年7月4日
	 */
	public void seedshoplist() {
		//兑换鲜花
		List<Record> flowerlist = Db.find("select id,ptid,name,imgurl,itinfo1,`describe`,describe2,price,seeds,41 'type' from f_flower_pro where state=0 and  ptid in(402,405,406,407,408,409)");
		//兑换花瓶
		List<Record> vaselist = Db.find("select id,ptid,name,imgurl,itinfo1,`describe`,describe2,price,seeds ,43 'type' from f_flower_pro where state=0 and  ptid=403");
		//兑换花边好物
		List<Record> lacelist = Db.find("select id,ptid,name,imgurl,itinfo1,`describe`,describe2,price,seeds ,43 'type' from f_flower_pro where state=0 and  ptid=404");
		
		Record account = getSessionAttr("account");
		
		long seeds = Db.queryLong("SELECT count(1) FROM f_flowerseed WHERE aid=?  and state = 0", account.getInt("id")) ;
		
		int yh_seeds=Db.queryInt("select b.seeds from f_account a left join f_update_grade b on a.grade=b.grade where a.id=?",account.getInt("id"));//会员优惠的花籽
		setAttr("seeds", seeds);
		setAttr("yh_seeds",yh_seeds);
		setAttr("flowerlist", flowerlist);
		setAttr("vaselist", vaselist);
		setAttr("lacelist", lacelist);
		
		render("seedshop.html");
	}
	
	public void seedgoodshow() {
		String id = getPara(0);
		//根据id 获取图片
		Record info = Db.findById("f_flower_pro", id);
		
		setAttr("imgurl", info.get("itinfo1"));
		setAttr("id", id);
		render("seedgoodshow.html");
	}
	
	
	/*****************************************  拼团     **************************************************************************/
	
	/**
	 * 拼团框架
	 * @author Glacier
	 * @date 2017年8月17日
	 */
	public void groupIndex() {
		
		// 获取商品id
		int id = getParaToInt(0);
		// 商品
		Record pro = FCDao.getProduct(id);
		
		setAttr("ptTime", pro.get("ptNum"));
		
		String imgs = pro.getStr("imgurl");
		if(imgs.indexOf("-")!= -1){
			String[] ims = imgs.split("-");
			pro.set("imgurl1", ims[0]);
			pro.set("imgurl2", ims[1]);
			pro.set("imgurl3", ims[2]);
		}else{
			pro.set("imgurl1", imgs);
			pro.set("imgurl2", imgs);
			pro.set("imgurl3", imgs);
		}
		Record user = getSessionAttr("account");
		//List<Record> grouplist =  GroupDAO.groupList(3, 1," where a.state=2 and ptNo not in(select ptNo from f_pingtuan_detail where aid="+user.getInt("id")+") and needCount <> hadCount");
		List<Record> grouplist =  GroupDAO.groupList(3, 1," where a.state=2 and a.ptTimeE>now() and a.fptid = "+id+"" ,((Record)getSessionAttr("account")).getInt("id"));
		
		int isShow=1;//是否显示【去参团】，默认显示
		if(pro.getInt("allownew")==1&&user.getInt("isbuy")>0){
			isShow=0;//当该商品只允许新用户参加，并且当前是老用户时，【不显示】
		}
		
		setAttr("isptFree", pro.getInt("isptFree"));//团长是否免单
		
		setAttr("pid",pro.getInt("id"));
		
		setAttr("isShow", isShow);
		setAttr("state", pro.getInt("state"));
		//System.out.println("是否售罄："+pro.getInt("state"));
		
		setAttr("flower", pro);
		
		setAttr("dmlj", FCDao.getPintuanDic().get("code_value"));
		setAttr("grouplist", grouplist);
		render("group.html");
	}
	
	
	/**
	 * 获取凑团列表
	 * @author Glacier
	 * @date 2017年8月15日
	 */
	public void getGrouplist() {
	
	 // 获取商品id
	 int id = getParaToInt(0);
	 //抓取最新的20条 团购信息
	 Record user = getSessionAttr("account");
	 //不包含自己参加过的团
	 //List<Record> grouplist = GroupDAO.groupList(20, 1, " where a.state=2 and ptNo not in(select ptNo from f_pingtuan_detail where aid="+user.getInt("id")+")");
	 List<Record> grouplist = GroupDAO.groupList(300, 1, " where a.state=2 and a.ptTimeE>now() and a.fptid = "+id+"",((Record)getSessionAttr("account")).getInt("id") );
		
	 setAttr("grouplist", grouplist);
	 render("group_list.html");
	// render("group_detail.html");
	}
	
	/**
	 * 是否有未成团的团
	 */
	public void isHasTuan(){
		boolean flag=false;
		Record user = getSessionAttr("account");
		List<Record> grouplist = GroupDAO.groupList(20, 1, " where a.state=2 and ptNo not in(select ptNo from f_pingtuan_detail where aid="+user.getInt("id")+")",user.getInt("id"));
		if(grouplist.size()>0){
			flag=true;
		}	
		renderJson(flag);
	}
	
	
	/**
	 * 闺蜜团
	 * @author Glacier
	 * @throws Exception 
	 * @date 2017年8月17日
	 */
	public void groupDetail() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int ptNo = getParaToInt(0);
		int jsAid=getParaToInt(1)==null?0:getParaToInt(1);//介绍人id
		int isshow = 0; //是否显示弹出层
		
		setAttr("ptNo", ptNo);
		
		//拼团信息   产品在售（state=0）
		Record pin = FCDao.getPinTuan(ptNo);
		//System.err.println("打印拼团信息："+ pin);
		// 可能 信息没有写进来 等待1秒
		if (pin.get("ptTimeE") == null) {
			// System.err.println("拼团信息 还没有写进来");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pin = FCDao.getPinTuan(ptNo);
		}
		
		setAttr("ptTime", pin.get("ptNum"));
		
		setAttr("pintuan", pin);
		//商品id
		setAttr("id", pin.get("id"));
		setAttr("pro_state",pin.getInt("pro_state"));
		/*
		System.out.println(pin.getStr("imgurl"));*/
		
		double nCount = pin.getInt("needCount").doubleValue();
		double hCount = pin.getInt("hadCount").doubleValue();
		double cent = (hCount/nCount*1.0) * 100;
		
		// 进度条 
		setAttr("cent", (int)Math.floor(cent)+"%");
		
		int pinCount = pin.getInt("needCount")-pin.getInt("hadCount");
		pinCount =  pinCount<=0 ? 0 : pinCount;
		setAttr("needCount", pinCount);
		
		Record user = getSessionAttr("account");
		setAttr("isbuy",user.getInt("isbuy"));
		
		//查看用户是否已经参加此团  且此团没有满
		Record ispintuan = Db.findFirst("SELECT ptNo,aid from f_pingtuan_detail where ptNo = ? and aid = ?",ptNo,user.get("id"));
		if ( ispintuan != null && pinCount != 0 ) {
			isshow = 1;
		}
		setAttr("isshow", isshow);
		
		
		// 0 所有用户都可以参加    1 只有新用户参加
		setAttr("allownew", pin.get("allownew"));
		
		//二维码链接
		setAttr("url", groupUrl(user.getInt("id"),ptNo));
		//二维码背景图片
		setAttr("BgUrl",pin.get("itinfo4"));
		
		
		//字典信息 
		String code_value = FCDao.getPintuanDic().get("code_value");
		setAttr("dmlj", code_value);
		
		//团购详情
		List<Record> pinlist = Db.find("SELECT a.ptNo,a.aid,a.ctime,b.nick,b.headimg from f_pingtuan_detail a "
				+ "LEFT JOIN f_account b on a.aid = b.id where ptNo = ? order by a.ctime asc",ptNo);
		setAttr("pinlist", pinlist);
		
		//计算时间差
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try
		{
		    Date d1 = df.parse(df.format(new Date()));
		    Date d2 = df.parse(pin.get("ptTimeE").toString());
		    long diff = d2.getTime() - d1.getTime();
		    
		    diff = diff <= 0 ? 0:diff;
		    setAttr("diff", diff);
		    
		   /* 
		    System.err.println( d2 + "\t" + d1 );*/
		    	
		   /* System.out.println(diff);*/
		    
		}
		catch (Exception ex){
			ex.printStackTrace();
			System.err.println(" Error  倒计时计算");
		}
		
		boolean isin=(Db.queryLong("select count(1) from f_pingtuan_detail where ptNo=? and aid=?",ptNo,user.getInt("id")))>0?true:false;
		setAttr("isin", isin);//自己是否在当前团里;限制一个团，只能参加一次
		
		setAttr("jsAid",jsAid);//介绍人ID
		render("group_detail.html");
	}
	
	/**
	 * 个人代颜二维码
	 * @param aid
	 * @param ptNo
	 * @return
	 */
	public String groupUrl(int aid,int ptNo){
		WeixinApiCtrl.setApiConfig();
		ApiResult ar = QrcodeApi.createPermanent("3" + "_" + aid+"_"+ptNo);
 		Gson gson = new  Gson();
 		Map<?,?> map = gson.fromJson(ar.getJson(), HashMap.class);
 		String url = (String) map.get("url");
 		return url;
	}
	
	
	
	/**
	 * 获取团信息
	 * @author Glacier
	 * @date 2017年8月28日
	 */
	public void getTuanInfo() {
		
		Record user = getSessionAttr("account");
		//待成团
		List<Record> group_wait = FCDao.getPintuanlist(" where a.state = 2 and c.aid = "+user.get("id")+" ORDER BY ptTimeS DESC LIMIT 10");
		//已成团
		List<Record> group_finish = FCDao.getPintuanlist(" where a.state = 3 and c.aid = "+user.get("id")+" ORDER BY ptTimeS DESC LIMIT 10");
		
		setAttr("group_wait", group_wait);
		setAttr("group_finish", group_finish);
		
		// System.out.println(group_wait);
		// System.err.println(group_finish);
		
		render("mytuan.html");
	}
	
	//申请发票
	public void receipt_apply(){
		Record account = getSessionAttr("account");
		//Record receipt = Db.findFirst("SELECT count(*) 'ordernum',SUM(a.price) 'allMoney',GROUP_CONCAT(a.ordercode) 'orderList' FROM f_order a WHERE a.state in(2,3) and a.aid =? AND NOT EXISTS (select b.ordercode from f_receipt_detail as b where b.ordercode=a.ordercode)",account.getInt("id"));		
		
		Record receipt=Db.findFirst("select sum(ordernum) 'ordernum',convert(sum(allMoney),decimal(10,2)) 'allMoney',GROUP_CONCAT(orderList) 'orderList' from "
				+ "(SELECT count(*) 'ordernum',SUM(a.price) 'allMoney',GROUP_CONCAT(a.ordercode) 'orderList' FROM f_order a WHERE a.state in(2,3) and a.aid =? AND NOT EXISTS (SELECT b.ordercode FROM f_receipt_detail as b LEFT JOIN f_receipt as c on b.fcode=c.fcode WHERE c.state in (1,2,3) and b.ordercode=a.ordercode) "
				+ "UNION select count(*) 'ordernum',sum(money-tmoney) 'allMoney',GROUP_CONCAT(id) from f_redpackets as a where a.aid=? and a.state=2 and a.stime is not null AND NOT EXISTS (select a.id from f_receipt_detail as b where a.id=b.ordercode)) as c",account.getInt("id"),account.getInt("id"));
		
		setAttr("receipt", receipt);
		render("receipt_apply.html");
	}
	
	// 按订单开票
	public void receipt_byorder() {
		Integer pageno = getParaToInt(0) == null ? 1 : getParaToInt(0);
		Integer state = getParaToInt(1) == null ? 9 : getParaToInt(1);
		Record account = getSessionAttr("account");
		Page<Record> page = FCDao.getByMyOrder(pageno, 100, account.getInt("id"), state);//订单列表
		Page<Record> pageRed=FCDao.getByMyRed(pageno, 100, account.getInt("id"), state);//红包/鲜花卡列表
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
			setAttr("totalpage", page.getTotalPage()+page.getTotalPage());
			setAttr("orderlist", page.getList());
			setAttr("redlist", pageRed.getList());//红包/鲜花卡列表
			
			List<Record> orderlist = page.getList();
			//去除重复list
			for (int i = 0; i < orderlist.size(); i++) {
				//去除 type =6  相同的条目
				for (int j = orderlist.size() - 1; j > i; j--) {
					if ( orderlist.get(i).getInt("type") == orderlist.get(j).getInt("type") && orderlist.get(i).get("ordercode").equals(orderlist.get(j).get("ordercode"))) {					
						//送花次数   订单号 处理
						orderlist.get(i).set("name", "定制鲜花");
						orderlist.get(i).set("imgurl", "/image/flower_custom.jpg");
						orderlist.remove(j);
					}
				}
				
			}
			
			render("receipt_byorder.html");
		} else {
			renderJson(page.getList());
		}		
	}
	
	
	//发票信息
	public void receipt_info(){
		setAttr("allMoney",Double.valueOf(getParaToInt(0))/100);//发票金额
		setAttr("ordernum",getParaToInt(1));//订单数目
		setAttr("orderList",getPara(2));//订单号列表
		// 跳转参数
		String queryString = getRequest().getQueryString();
		setAttr("queryString", queryString);
		setAttr("areajson", FCDao.getArea());
		render("receipt_info.html");
	}
	
	
	/**当发票金额大于或等于300时，无需支付邮费，直接将申请发票的数据填入数据库 f_receipt**/
	public void saveReceipt(){
		Record user= getSessionAttr("account");
		boolean result = false;
		String company = getPara("company");
		String code = getPara("code");
		String content = getPara("content");
		String money =getPara("money");
		String name = getPara("name");
		String tel = getPara("tel");
		String area = getPara("area");
		String addr = getPara("addr").trim().replace(" ", "");
		String fcode = FCDao.getFCode();
		String ordernum=getPara("ordernum");
		String orderList=getPara("orderList");
		Record record = new Record();
		record.set("company",company);
		record.set("code",code);
		record.set("content",content);
		record.set("money",money);
		record.set("name",name);
		record.set("tel",tel);
		record.set("area",area);
		record.set("addr",addr);
		record.set("ctime",new Date());
		record.set("fcode",fcode);
		record.set("ordernum",ordernum);
		record.set("aid",user.getInt("id"));
		record.set("state",2);
		
		result =Db.save("f_receipt", record);
		
	   	try {
	   		SendMsgUtil.sendMsg("17751658515", "有新的申请发 票需求，请及时跟踪处理");
			SendMsgUtil.sendMsg("13773103981", "有新的申请发 票需求，请及时跟踪处理");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderJson("result",result);
		
		//单身信息
		String [] oList=orderList.split(",");
		for (String ol : oList) {
			Record f_receipt_detail=new  Record();
			f_receipt_detail.set("fcode", fcode);
			f_receipt_detail.set("ordercode", ol);
			Db.save("f_receipt_detail", f_receipt_detail);
		}
	}
	
		
	
	//开票记录
	public void receipt_record(){
		Record account = getSessionAttr("account");
		List<Record> receiptlist = Db.find("SELECT id,state,money,DATE_FORMAT(ctime,'%Y.%m.%d %H:%i') as ctime_a,DATE_FORMAT(ctime,'%Y.%m.%d') as ctime_b FROM f_receipt WHERE aid =?",account.getInt("id"));		
		setAttr("receiptlist", receiptlist);
		render("receipt_record.html");
	}
	
	// 开票详情
	public void receipt_detail(){
		int id=getParaToInt(0); 
		List<Record> receiptlist = Db.find("SELECT id,state,area,addr,name,company,code,content,money,ctime,ordernum,DATE_FORMAT(ctime,'%Y.%m.%d') as ctime_b,fcode FROM f_receipt WHERE id=?",id);
		setAttr("receiptlist", receiptlist);
		
		Record account = getSessionAttr("account");
		Record ctime = Db.findFirst("SELECT GROUP_CONCAT(DATE_FORMAT(a.ctime,'%Y.%m.%d %H:%i')) 'ctimeList' FROM f_order a LEFT JOIN f_receipt_detail b on a.ordercode=b.ordercode LEFT JOIN f_receipt c on b.fcode=c.fcode WHERE c.aid=? AND c.id=?",account.getInt("id"),id);
		setAttr("ctime", ctime);
		render("receipt_detail.html");
	}
	
	/**取消发票订单**/
	public void cancelReceipt(){
		int id=getParaToInt("id");
		Db.update("UPDATE f_receipt SET state=4 where state=1 and id=?",id);
		Map<String, Object> map = new HashMap<>();
		renderJson(map);
	}
	
	
	// 发票中所含订单
	public void receipt_ordercon() {
	    Record account = getSessionAttr("account");
	    int id=getParaToInt(0);
		List<Record> orderlist = Db.find("SELECT a.ordercode,c.name,c.imgurl,b.price,a.price AS totalprice,a.ctime,d.fcode FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id left join f_receipt_detail d on a.ordercode=d.ordercode LEFT JOIN f_receipt e on d.fcode=e.fcode WHERE e.aid=? AND e.id=?",account.getInt("id"),id);
		for (Record detail : orderlist) {
			String imgurl = detail.getStr("imgurl");
			if (imgurl.indexOf("-") > 0) {
				String[] img = imgurl.split("-");
				detail.set("imgurl", img[0]);
			}
			
		}
		
		setAttr("orderlist", orderlist);
		render("receipt_ordercon.html");
	}
	
	
	
	/*-----------------------------礼品卡------------------------------------*/
	
	/**
	 * 鲜花卡 
	 * @author lmy
	 * @date 2017年11月22日
	 */
	public void blesscard() {
		
Record account = getSessionAttr("account");
		
		// 显示所有的卡片(已拆，转赠 )
		List<Record> redCard = Db.find(" SELECT a.id,a.rpid,a.aid,a.dmoney,a.gtime,b.`name`,b.pnum,b.fpid,c.msg,d.`name` imgname,d.imgurl01,d.imgurl02,a.isopen "
									 + " from f_redpackets_detial a "
									 + " LEFT JOIN f_redpackets_pro b on a.rppid = b.id "
									 + " LEFT JOIN f_redpackets c on c.id = a.rpid "
									 + " LEFT JOIN f_redpackets_pic d on d.id = c.picId "
									 + " where a.aid = ? and c.picId <> 0 and a.isopen in(1,3) ORDER BY a.gtime DESC",account.getInt("id"));
		setAttr("isshow", 0);
		setAttr("redCard", redCard);
		
		List<Record> cardHis = Db.find(" select a.id,a.type,a.money,a.quantity1,a.quantity2,a.msg,a.ctime,a.stime,a.ttime,a.tmoney,a.opuser,a.state,a.aid,a.picId,b.imgurl01,b.imgurl02,b.name 'name'"
				+ " from f_redpackets a  "
				+ " LEFT JOIN f_redpackets_pic b on a.picId = b.id "
				+ " LEFT JOIN f_dictionary c on c.code_value = b.typeId "
				+ " where a.aid = ? and  a.state <>0 and a.picId<>0 and c.code_key='redpacketsPicType' "
				+ " GROUP BY ctime DESC",account.getInt("id"));
		
		setAttr("isshow", 0);
		setAttr("cardHis", cardHis);
		render("blesscard.html");
	}
	
	
	
	/**
	 * 礼品卡 
	 * @author Glacier
	 * @date 2017年10月17日
	 */
	public void blessing() {
		/*List<Record> imglist = Db.find("select  a.code_name,a.code_value ,b.imgurl01 from f_dictionary a "
				+ " left join f_redpackets_pic b on a.code_value=b.typeId "
				+ " where code_key='redpacketsPicType' and a.state=1 and b.imgOrderId=1 order by orderId");
		*/
		
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
		setAttr("isshow", 0);
		render("blessing.html");
		
	}
	
	/**
	 * 购买记录
	 * @author Glacier
	 * @date 2017年10月19日
	 */
	public void blessing_history() {
		
		Record  account= getSessionAttr("account");
		
		List<Record> redCard = Db.find(" select a.id,a.type,a.money,a.quantity1,a.quantity2,a.msg,a.ctime,a.stime,a.ttime,a.tmoney,a.opuser,a.state,a.aid,a.picId,b.imgurl01,b.imgurl02,b.name 'name'"
				+ " from f_redpackets a  "
				+ " LEFT JOIN f_redpackets_pic b on a.picId = b.id "
				+ " LEFT JOIN f_dictionary c on c.code_value = b.typeId "
				+ " where a.aid = ? and  a.state <>0 and a.picId<>0 and c.code_key='redpacketsPicType' "
				+ " GROUP BY ctime DESC",account.getInt("id"));
		
		/*System.out.println("redCard: :"+ redCard);*/
		
		setAttr("isshow", 0);
		setAttr("redCard", redCard);
		render("blessing_history.html");
	}
	
	/**
	 * 卡包
	 * @author Glacier
	 * @date 2017年10月19日
	 */
	public void blessing_cards() {
		
		Record account = getSessionAttr("account");
		
		// 显示所有的卡片(已拆，转赠 )
		List<Record> redCard = Db.find(" SELECT a.id,a.rpid,a.aid,a.dmoney,a.gtime,b.`name`,b.pnum,b.fpid,c.msg,d.`name` imgname,d.imgurl01,d.imgurl02,a.isopen "
									 + " from f_redpackets_detial a "
									 + " LEFT JOIN f_redpackets_pro b on a.rppid = b.id "
									 + " LEFT JOIN f_redpackets c on c.id = a.rpid "
									 + " LEFT JOIN f_redpackets_pic d on d.id = c.picId "
									 + " where a.aid = ? and c.picId <> 0 and a.isopen in(1,3) ORDER BY a.gtime DESC",account.getInt("id"));
		setAttr("isshow", 0);
		setAttr("redCard", redCard);
		render("blessing_cards.html");
	}
	
	/**
	 * 领取卡片
	 * @author Glacier
	 * @date 2017年10月19日
	 */
	public void blessing_getcard() {
		
		//是否已经领取
		int isget = 0;
		//是否是领取人
		int isgetone = 0;
		int redType=1;//默认闺蜜红包
		
		String cardCode = getPara(0);
		String type=getPara(1);//41表示第一次发卡领取；42表示转赠之后领取
		Record account = getSessionAttr("account");
		
		if (type.equals("42")) {
			redType = Db.queryInt("SELECT b.type FROM f_redpackets_detial a LEFT JOIN f_redpackets b on a.rpid =b.id WHERE a.id=?",cardCode);
		}else{
			Record redpacket = Db.findById("f_redpackets", cardCode);
			redType=redpacket.getInt("type");
		}
		
		if (redType==1) {
			
			Record card = Db.findFirst("SELECT a.id,a.isopen,a.aid,a.gtime,b.ctime,b.msg,b.type,b.picId,c.imgurl01,c.imgurl02 "
					+ " from f_redpackets_detial a "
				    + " LEFT JOIN f_redpackets b on a.rpid = b.id "
					+ " LEFT JOIN f_redpackets_pic c on c.id = b.picId "
				    + " where a.rpid = ?",cardCode);
			if(type.equals("42")){
				card = Db.findFirst("SELECT a.id,a.isopen,a.aid,a.gtime,b.ctime,b.msg,b.type,b.picId,c.imgurl01,c.imgurl02 "
						+ " from f_redpackets_detial a "
					    + " LEFT JOIN f_redpackets b on a.rpid = b.id "
						+ " LEFT JOIN f_redpackets_pic c on c.id = b.picId "
					    + " where a.id = ?",cardCode);
			}
			
			if (card.getInt("isopen") != 0 && card.getInt("isopen")!=3) {
				isget = 1;
			}

			if (account.getInt("id").equals(card.getInt("aid"))) {
				isgetone = 1;
			}
			setAttr("cardimg", card.getStr("imgurl01"));
			setAttr("cardmsg", card.getStr("msg"));
			setAttr("ctime", card.getDate("ctime"));
		}else if(redType==2){
			//拼手气红包
		}
		setAttr("isgetone", isgetone);
		setAttr("cardCode", cardCode);
		setAttr("isget", isget);
		setAttr("type",type);
		setAttr("nick", account.getStr("nick"));
		
		render("blessing_getcard.html");
	}
	
	
	/**
	 * 购买记录 -获取二维码
	 * @author Glacier
	 * @date 2017年10月25日
	 */
	public void getGiftCode() {
		
		String code = getPara("cardCode");
		String cardUrl = getPara("imgurl");
		
		Record account = getSessionAttr("account");
		//生成二维码
		String url = DaiYanURL.getUrl(account.getInt("id"), 41, code);
		
		List<Record> redCard = Db.find(" select a.id,a.type,a.money,a.quantity1,a.quantity2,a.msg,a.ctime,a.stime,a.ttime,a.tmoney,a.opuser,a.state,a.aid,a.picId,b.imgurl01,b.imgurl02,b.name 'name'"
				+ " from f_redpackets a  "
				+ " LEFT JOIN f_redpackets_pic b on a.picId = b.id "
				+ " LEFT JOIN f_dictionary c on c.code_value = b.typeId "
				+ " where a.aid = ? and  a.state <>0 and a.picId<>0 and c.code_key='redpacketsPicType' "
				+ " GROUP BY ctime DESC",account.getInt("id"));
		
		setAttr("redCard", redCard);
		setAttr("isshow",1);
		setAttr("cardUrl", cardUrl);
		setAttr("url", url);
		render("blessing_history.html");
	}
	
	/**
	 * 我的卡包 - 获取二维码 （这边可以优化 和blessing_cards方法放一起 判断是否有传值）
	 * @author Glacier
	 * @date 2017年10月25日
	 */
	public void getMyGiftCode() {
	
		String code = getPara("cardCode");//单头
		String cardUrl = getPara("imgurl");
		String codeRpid = getPara("codeRpid"); //单身
		
		Record account = getSessionAttr("account");
		//生成二维码
		String url = DaiYanURL.getUrl(account.getInt("id"), 42, codeRpid);//转赠好友
		//修改状态为【转赠中】，转送人为【自己】
		Db.update("update f_redpackets_detial set isopen=3,aid2=? where id=? and aid=? ",account.getInt("id"),codeRpid,account.getInt("id"));
		//显示所有的卡片
		List<Record> redCard = Db.find(" SELECT a.id,a.rpid,a.aid,a.dmoney,a.gtime,b.`name`,b.pnum,b.fpid,c.msg,d.`name` imgname,d.imgurl01,d.imgurl02,a.isopen "
									 + " from f_redpackets_detial a "
									 + " LEFT JOIN f_redpackets_pro b on a.rppid = b.id "
									 + " LEFT JOIN f_redpackets c on c.id = a.rpid "
									 + " LEFT JOIN f_redpackets_pic d on d.id = c.picId "
									 + " where a.aid = ? and c.picId <> 0 and a.isopen in(1,3) ORDER BY a.gtime DESC",account.getInt("id"));
		
		setAttr("redCard", redCard);
		setAttr("isshow",1);
		setAttr("cardUrl", cardUrl);
		setAttr("url", url);
		render("blessing_cards.html");
	}
	
	/**
	 * 领取详情
	 * @author Glacier
	 * @date 2017年10月30日
	 */
	public void redCardGetInfo() {
		
		String code = getPara(0);//单头
		//System.out.println(code);
		
		//Record account = getSessionAttr("account");
		// isopen = 0 未拆
		List<Record> redCardList = Db.find("SELECT a.id,a.rpid,a.rppid,a.isopen,a.aid,a.gtime,a.dmoney,a.aid3,b.`name`,c.nick "
				+ " from f_redpackets_detial a "
				+ " LEFT JOIN f_redpackets_pro b on a.rppid = b.id "
				+ " LEFT JOIN f_account c on c.id = a.aid3"
				+ " where a.rpid = ?",code);
		
		//System.err.println(redCardList);
		
		setAttr("redCardList", redCardList);
		render("blessing_getinfo.html");
		
	}
	
	
	/**
	 * 红包&鲜花卡
	 * @author Glacier
	 * @date 2017年11月1日
	 */
	public void redpackageAndRedcard() {
		
		render("redpAndredc.html");
	}
	
	public void myfund(){
		Record account = getSessionAttr("account");
		List<Record> fundlist=Db.find("select c.imgurl,c.name,a.price,a.fund from f_order as a "
				+ "left join f_order_detail as b on a.ordercode=b.ordercode "
				+ "left join f_flower_pro as c on b.fpid=c.id where a.aid=?  "
				+ "and a.state in(1,2,3) and  b.type=1 and a.fund>0 group by a.ordercode",account.getInt("id"));
		
		setAttr("nick", account.getStr("nick"));//昵称
		setAttr("headimg",account.getStr("headimg"));//头像
		setAttr("count",fundlist.size());//累计捐款次数
		double allfund=0;
		for (Record rd : fundlist) {
			allfund+=rd.getDouble("fund");
			String imgurl = rd.getStr("imgurl");
			if (imgurl.indexOf("-")!= -1) {
				String[] img = imgurl.split("-");
				rd.set("imgurl", img[0]);
			}else{
				rd.set("imgurl", imgurl);
			}
		}
		BigDecimal   b   =   new   BigDecimal(allfund);  
		double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue(); //保留2位小数，四舍五入
		setAttr("allfund",f1);
		setAttr("fundlist",fundlist);
		render("myfund.html");
	}
	public void introduce() {
		render("introduce_fund.html");
	}
	
	public void getPic(){
		render("getPic.html");
	}
	public void memory(){
		Record account = getSessionAttr("account");
		List<Record> memoryList=Db.find("SELECT id,date,type FROM f_memory where aid=?",account.getInt("id"));
		setAttr("memoryList", memoryList);
		render("memory_day.html");
	}
	public void memory_add(){
		render("memory_add.html");
	}
	public void memory_edit(){
		int id=getParaToInt(0);//纪念日ID号
		Record memory=Db.findFirst("SELECT id,date,type FROM f_memory where id=?",id);
		setAttr("memory", memory);
		render("memory_edit.html");
	}
	
	/**
	 *  添加、保存纪念日
	 * @throws Exception 
	 */
	public void saveMemory() throws Exception{
		Record account = getSessionAttr("account");
		int id=getParaToInt("id");
		Integer type = getParaToInt("type");//类型
		Integer format = getParaToInt("format");//格式:公历或农历
		String date=getPara("date");//日期
		date =URLDecoder.decode(date, "UTF-8");
		
		Record f_memory = new Record();
		f_memory.set("aid",account.getInt("id"));
		f_memory.set("date",date);
		f_memory.set("type",type);
		f_memory.set("format",format);
		
		if(id == 0){
			Db.save("f_memory", f_memory);
		}else{
			f_memory.set("id", id);
			Db.update("f_memory", f_memory);
		}
		Map<String, Object> map = new HashMap<>();
		renderJson(map);
	}
	
	
	/**
	 *  删除纪念日
	 */
	public void deleMemory(){
		Integer id = getParaToInt(0);
		Db.update("delete from f_memory where id=?",id);
		Map<String, Object> map = new HashMap<>();
		renderJson(map);
	}
	
	
	
	
	/*
	 * 更新头像
	 */
	public void resetUserInfo() {
		Record account = getSessionAttr("account");
		WeixinApiCtrl.setApiConfig();
		ApiResult ar = UserApi.getUserInfo(account.getStr("openid"));
		try {
			account.set("nick", CharFliter.filterOffUtf8Mb4_2(ar.get("nickname").toString()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		account.set("headimg", ar.get("headimgurl"));
		boolean result= Db.update("f_account", account);
		Map<String, Object> map = new HashMap<>();
		map.put("result", result);
		renderJson(result);
	}
	/***************************************新年小目标活动******************************************************/
	
	/**
	 * 测试 aims_share
	 * @author Glacier
	 * @date 2017年12月30日
	 */
	/*public void aims_share() {
		render("aims_share.html");
	}*/
	/**
	 * 测试 aims_index
	 * @author Glacier
	 * @date 2017年12月30日
	 */
	public void aims_index() {
		render("aims_index.html");
	}
	
	/**
	 * 活动是否关闭
	 */
	public void isClose(){
		String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
		if(code_value.equals("1")){
			renderJson(true);
		}else{
			renderJson(false);
		}
	}
	
	/**
	 * 提交之前判断是否允许设置小目标
	 * 每个用户只允许设置一次小目标
	 * true允许，false不允许
	 * ajax调用
	 */
	public void isAllowSaveAims(){
		String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
		if(code_value.equals("1")){
			int aid=((Record) getSessionAttr("account")).getInt("id");
			Long count=Db.queryLong("select count(1) from f_target where aid=?",aid);
			renderJson((count==0)?true:false);
		}else{
			renderJson(false);
		}
		
	}
	/**
	 * 提交小目标
	 * 传入参数：contentStr
	 * @throws UnsupportedEncodingException 
	 */
	public void saveAims() throws UnsupportedEncodingException{
		String content=getPara("contentStr");//目标内容
		setAttr("contentStr", content);
		
		final int aid=((Record) getSessionAttr("account")).getInt("id");
		final String openid=((Record) getSessionAttr("account")).getStr("openid");
		final String nick=((Record) getSessionAttr("account")).getStr("nick").replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]","");
		Record f_target=new Record();
		f_target.set("aid",aid );//定目标的人
		f_target.set("contentStr", content);//目标内容
		f_target.set("tgtime", new Date());//定目标的时间
		boolean flag=Db.save("f_target", f_target);
		Long targetId = f_target.getLong("id");
		final String url = DaiYanURL.getUrl(aid, 7,targetId+"");
		final String contentStr = content.replace("</br>", "");
		
		if(flag){
			//一段引导文字，一张海报，一个查看链接
			WeixinApiCtrl.setApiConfig();
			String message="分享你的小目标，让ta做个“严肃”的见证人吧~";
			CustomServiceApi.sendText(openid, message);
			new Thread() {
				public void run() {
					WeixinApiCtrl.setApiConfig();
					String destPath = "/QRImage_XMB/" + openid + ".jpg";
					File file =new File(destPath); 
					if (file.exists()) {
						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						CustomServiceApi.sendImage(openid, ar.getStr("media_id"));	
					} else {
						
						String outPath = "/QRImage_XMB/" + openid + ".jpg";
						String headIamge = Db.queryStr("SELECT headimg FROM f_account WHERE id = ?", aid) ;
						// 如果目录不存在 创建对应目录
						QRCodeUtil.mkdirs("/QRImage_XMB/head/");
						DownloadFile.downUrlFile(headIamge, "/QRImage_XMB/head/");
						File file_head =new File("/QRImage_XMB/head/0.jpg"); 
						// 产生 带中间logo 的二维码图片
						String logoPath = "/QRImage_XMB/head/0.jpg";
						try {
							QRCodeUtil.encode(url, logoPath, outPath, true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// 二维码 添加背景 （对应目录 “/QRImage_XMB/” 下 有添加 invite.jpg 作为背景图片 ）
						try {
							float a = (float) 0.9;
							File bg = new File("/QRImage_XMB/invite.jpg");
							BufferedImage buffImg = NewImageUtils.watermarkXMB(bg, file, 200, 920, a,nick,contentStr);
							NewImageUtils.generateWaterFile(buffImg, outPath);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						// 客服消息 发送说明文字、专属海报给用户
						
						ApiResult ar = MediaApi.uploadMedia(MediaType.IMAGE, file);
						CustomServiceApi.sendImage(openid, ar.getStr("media_id"));
						
					}
				}
			}.start();

			//发客服消息，可以查询我的目标
			String msg="“小目标”人气爆棚了没？"+"<a href='" + Constant.getHost +"/account/myTarget/"+ ((Record) getSessionAttr("account")).getInt("id") + "'>戳我速看</a>";
			CustomServiceApi.sendText(openid, msg);
		}
		setAttr("nick", nick);
		
		setAttr("url", url);
		render("aims_share.html");
	}
	/**
	 * 查看我的目标【见证进度】
	 */
	public void myTarget(){
		int aid=((Record) getSessionAttr("account")).getInt("id");
		Record rd=Db.findFirst("select wNumNew,wNumOld,REPLACE(contentStr,'</br>','') 'contentStr',TIMESTAMPADD(HOUR,24,tgtime) 'tgtime',wScore,isClose from f_target where aid=? ",aid);		
		setAttr("rd", rd);
		
		
		render("myTarget.html");
	}
	/**
	 * 见证人打开小目标，准备见证
	 */
	public void readyWitness(){
		int tgid=getParaToInt(0);
		Record rd=Db.findFirst("select  a.wNumNew,a.wNumOld,a.id,b.nick,REPLACE(a.contentStr,'</br>','') 'contentStr' from f_target as a "
				+ " left join f_account as b "
				+ " on a.aid=b.id"
				+ " where a.id=?",tgid);
		setAttr("rd", rd);
		render("readyTarget.html");
	}
	/**
	 * 是否允许见证
	 * 每个用户只有一次给好友见证的机会
	 * 目标没有关闭才可以见证
	 * ajax调用
	 */
	public void isAllowSaveWitness(){
		String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
		if(code_value.equals("1")){
			int tgid=getParaToInt(0);
			int aid=((Record) getSessionAttr("account")).getInt("id");//见证人id
			Long count1=Db.queryLong("select count(1) from f_target_witness where wAid=?",aid);//每个人只能见证一次
			
			Long count2=Db.queryLong("select count(1) from f_target where isClose=0 and id=?",tgid);//超过24小时的不能见证
			
			Long count3=Db.queryLong("select count(1) from f_target where  id=? and aid=?",tgid,aid);//不能给自己见证
			
			renderJson((count1==0&&count2==1&&count3==0)?true:false);//没有见证过，并且目标没有关闭，返回true
		}else{
			renderJson(false);
		}
		
	}
	public boolean isAllowSaveWitness(int tgid){
		String code_value=Db.queryStr("select code_value from f_dictionary where code_key='target'");
		if(code_value.equals("1")){
			int aid=((Record) getSessionAttr("account")).getInt("id");//见证人id
			Long count1=Db.queryLong("select count(1) from f_target_witness where wAid=?",aid);//每个人只能见证一次
			
			Long count2=Db.queryLong("select count(1) from f_target where isClose=0 and id=?",tgid);//超过24小时的不能见证
			
			Long count3=Db.queryLong("select count(1) from f_target where  id=? and aid=?",tgid,aid);//不能给自己见证
			
			return((count1==0&&count2==1&&count3==0)?true:false);//没有见证过，并且目标没有关闭，返回true
		}else{
			return false;
		}
		
	}
	
	/**
	 * 提交见证留言
	 */
	public void saveWitness(){
		int tgid=getParaToInt("tgid");//小目标的id
		String wMsg=getPara("wMsg");//见证留言
	
		/*int tgid=104;//小目标的id
		String wMsg="加油";//见证留言*/
		
		//后台再做一次判断，防止直接调用函数冒领红包
		if(isAllowSaveWitness(tgid)==false){
			renderJson(false);
			return;
		}
			
		int aid=((Record) getSessionAttr("account")).getInt("id");
		String openid=((Record) getSessionAttr("account")).getStr("openid");
		Record f_target_witness=new Record();
		f_target_witness.set("wAid", aid);//见证人
		f_target_witness.set("tgid", tgid);//小目标
		f_target_witness.set("wTime", new Date());//见证时间
		f_target_witness.set("wMsg", wMsg);//见证留言
		boolean flag=Db.save("f_target_witness", f_target_witness);
		if(flag){
			//抽一个红包发送给见证人
			int redId=0;
			double money=100;//默认1块钱
			Long count=Db.queryLong("select count(1) from f_account where id=? and ctime>='2018-01-01'",aid);
			//更新目标的见证人气
	        if(count==1){
	        	//新用户
	        	Db.update("update f_target set wNumNew=wNumNew+1,wScore=wScore+3 where id=?",tgid);
	        }else{
	        	//老用户
	        	Db.update("update f_target set wNumOld=wNumOld+1,wScore=wScore+1 where id=?",tgid);
	        }
	        //发红包
			if(count==1){
				//新用户
				Record pArea = Db.findFirst(
						"select min(pAreaS)'minArea' ,max(pAreaE) 'maxArea' from f_prizeList where type=5 and pName like '%新%'");
		    	int min = pArea.getInt("minArea");// 最小值
				int max = pArea.getInt("maxArea") - 1;// 最大值
				int randNum = new Random().nextInt(max) % (max - min + 1) + min;// 随机数
		        Record rd = Db.findFirst(
						"select id,money from f_prizeList where pAreaS<=? and pAreaE>? and pNum>0 and type=5 and pName like '%新%'",
						randNum, randNum);
		        redId=rd.getInt("id");
		        money=rd.getDouble("money");
			}
			else{
				//老用户
				Record pArea = Db.findFirst(
						"select min(pAreaS)'minArea' ,max(pAreaE) 'maxArea' from f_prizeList where type=5 and pName like '%老%'");
		    	int min = pArea.getInt("minArea");// 最小值
				int max = pArea.getInt("maxArea") - 1;// 最大值
				int randNum = new Random().nextInt(max) % (max - min + 1) + min;// 随机数
		        Record rd = Db.findFirst(
						"select id,money from f_prizeList where pAreaS<=? and pAreaE>? and pNum>0 and type=5 and pName like '%老%'",
						randNum, randNum);
		        redId=rd.getInt("id");
		        money=rd.getDouble("money");
			}
			boolean	flag2=false;
			//红包金额必须大于等于1块钱
			if(money>=100&&money<200){
				/*String tMoney=Db.queryStr("select code_value from f_dictionary where code_key='tMoney'");//设置的总额度
				double tMoneySend=Db.queryDouble("select sum(wMoney)/100.0 from f_target_witness");//已发的金额
				if(tMoneySend<Double.valueOf(tMoney).doubleValue()){
					flag2= com.util.SendCashRed.Send(getRequest(), "花美美", openid,
							String.valueOf((int) money), "1", "感谢你的见证，我又多了一点信心~", "新年小目标", "备注信息");
				}else{
					String msg=Db.queryStr("select note from f_dictionary where code_key='tMoney'");
					WeixinApiCtrl.setApiConfig();
					CustomServiceApi.sendText(openid, msg);//50万已经瓜分完毕
				}*/
			    
			}
		  //记录发送的红包信息
		    if(flag2){
		    	Db.update(" update f_target_witness set wMoney=?,prizeId=?,sTime=now() where id=?",
		        		money,redId,f_target_witness.getLong("id"));
	        }
		    WeixinApiCtrl.setApiConfig();
			String message="<a href='" + Constant.getHost +"/account/aims_index"+"'>我也来定个新年小目标，万一实现了呢</a>";
			CustomServiceApi.sendText(openid, message);
		}
		renderJson(flag);
	}
	
	/**
	 * 情人节活动
	 * @author Glacier
	 * @date 2018年1月26日
	 */
	public void valentine_day() {
		render("valentine_day.html");
	}
	public void valentine_editor() {
		
		render("valentine_editor.html");
	}
	public void valentine_share() {
		render("valentine_share.html");
	}
	
	/**
	 * 将语音上传到七牛云
	 * @author Glacier
	 * @date 2018年1月28日
	 */
	public void uploadVoice() {
		String voice = getPara("voiceid");
		
		WeixinApiCtrl.setApiConfig();
		String token = AccessTokenApi.getAccessToken().getAccessToken();
		
		// 从临时素材库下载语音 然后保存到数据库
		QianNiuUpload qiNiu = new QianNiuUpload();
		String string = qiNiu.fetchTmpFile(voice,"video",token);
		System.out.println(string);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultMsg", true);
		
		String json = new JFinalJson().toJson(resultMap);
		renderJson(json);
		
	}
	
	
	/**
	 * 生成小纸条
	 */
	public void submitInfo(){

		String dearNick=getPara("dearNick")==null?"":getPara("dearNick");//寄给：李女士
		String msg = getPara("msg")==null?"":getPara("msg");//一句话
		String imgBase64=getPara("imgBase64")==null?"":getPara("imgBase64");//一张图片
		String myNick=getPara("myNick")==null?"":getPara("myNick");//来自：张先生
		System.out.println(imgBase64);
		String imgUrl="";
		if(imgBase64!=null&&imgBase64!=""){
			try {
				imgUrl=QianNiuUpload.uploadBase64(imgBase64);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		int aid=((Record) getSessionAttr("account")).getInt("id");
		String openid=((Record) getSessionAttr("account")).getStr("openid");
		Record f_valentine_day=new Record();
		f_valentine_day.set("aid", aid);
		f_valentine_day.set("msg", msg);
		f_valentine_day.set("dearNick", dearNick);
		f_valentine_day.set("imgUrl", imgUrl);
		f_valentine_day.set("myNick", myNick);
		f_valentine_day.set("ctime", new Date());
		
		boolean flag=Db.save("f_valentine_day", f_valentine_day);
		int id=0;
		if(flag){
			//发花票
			WeixinApiCtrl.setApiConfig();
			String msgStr="你的诚意感动了小美，给您送上2张花票，给ta送束花为爱保鲜"+"<a href='" + Constant.getHost +"/account/receiveCash/64-596087513989"+ "'>【立即领取】</a>";
			CustomServiceApi.sendText(openid, msgStr);
			
			id=f_valentine_day.getLong("id").intValue();
			
			String msgStr2="<a href='" + Constant.getHost +"/account/valentine_poster/"+id+"-"+MD5Util.getMd5str(id)+ "'>【查看我送ta的卡片】</a>";
			CustomServiceApi.sendText(openid, msgStr2);
			
		}
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultMsg", flag);//true or flase
		resultMap.put("id", id);//id编号
		resultMap.put("md5Str", MD5Util.getMd5str(id));//id号的MD5加密字符串
		String json = new JFinalJson().toJson(resultMap);
		
		
		//System.out.println(json);
		renderJson(json);
		//resultMsg==true时,直接跳转到/account/valentine_poster/64-596087513989
	}
	
	/**
	 * 情人节海报
	 */
	public void valentine_poster() {
		int id=getParaToInt(0);
		String md5Str=getPara(1);//id号的MD5加密字符串
		if(md5Str.equals(MD5Util.getMd5str(id))){
			Record valentine=Db.findFirst("SELECT id,aid,dearNick,msg,imgUrl,myNick from f_valentine_day where id=?",id);
			setAttr("valentine", valentine);
			render("valentine_poster.html");
		}else{
			redirect("/freeze");
		}
		
	}
	

}