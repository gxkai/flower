package com.controller.manage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.dao.MCDao;
import com.dao.WLDao;
import com.interceptor.ManageInterceptor;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.domain.mall.http.PriceChange;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;


/**
 * @Desc 物流管理
 * @author lxx
 * @date 2016/8/16
 * */
@Before(ManageInterceptor.class)
public class ManageLogisticsCtrl extends Controller {
	/*********************物流平台*********************/
	// 列表
	public void express(){
		Integer pageno = getParaToInt()==null?1:getParaToInt();
		Page<Record> page = MCDao.getExpress(pageno, 16);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("expresslist", page.getList());
		render("express.html");
	}
	// 保存数据
	public void saveExpress(){
		Map<String, Object> map = new HashMap<>();
		boolean result = false;
		String msg = new String();
		String name = getPara("name");
		String code = getPara("code");
		String price = getPara("price");
		String url = getPara("url");
		Number num = Db.queryNumber("select count(1) from f_express where code = ?", code);
		if(num.intValue() > 0){
			msg = "代码不能重复";
		}else{
			Record record = new Record();
			record.set("name", name);
			record.set("code", code);
			record.set("price", price);
			record.set("url", url);
			result = Db.save("f_express", record);
			if(result){
				msg = "保存成功";
			}else{
				msg = "保存失败";
			}
		}
		map.put("result", result);
		map.put("msg", msg);
		renderJson(map);
	}
	// 获取单条数据
	public void getExpress(){
		render("express_detail.html");
	}
	// 删除数据
	public void delExpress(){
		int id = getParaToInt();
		renderJson(Db.deleteById("f_express", id));
	}
	// 更新数据
	public void updateExpress(){
		int id = getParaToInt("id");
		Long    price=getParaToLong("price");
		String  url = getPara("url");
		Record record = new Record();
		record.set("id", id);
		record.set("url", url);
		record.set("price", price);
		renderJson(Db.update("f_express","id", record));
	}
	
   
	
	/*********************物流列表*********************/
	
	/**
	 * 周边物流列表
	 */
	public void orderinfo_ZB(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Integer state = getParaToInt("state")==null?1:getParaToInt("state");//默认显示【未发货的订单】
		Integer fpid=getParaToInt("fpid")==null?0:getParaToInt("fpid");
		String picicode = getPara("picicode");			// 查询批次
		String ordercode = getPara("ordercode");		// 订单编号
		String logisticcode = getPara("logisticcode");	// 物流编号
		String wuliucode = getPara("wuliucode")==null?"xzwl":getPara("wuliucode");	// 选择物流
		String receiver = getPara("receiver");	// 收货人
		
		
		if(receiver!=null){
			try {
				receiver = URLDecoder.decode(receiver,"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		Page<Record> page = MCDao.getOrderInfo_ZB(pageno, 16, picicode, ordercode, logisticcode, state, 10, wuliucode, receiver,fpid);
		List<Record> wuliulist = MCDao.getWuliu();
		setAttr("picicode", picicode);
		setAttr("ordercode", ordercode);
		setAttr("wuliucode", wuliucode);
		setAttr("logisticcode", logisticcode);
		setAttr("wuliulist", wuliulist);
		setAttr("state", state);
		setAttr("fpid",fpid);
		setAttr("proList",MCDao.f_flower_pro_ZB());//在售的周边商品列表
		setAttr("receiver", receiver);
		
		
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("orderinfolist", page.getList());
		render("orderinfo_ZB.html");		
	}
	
	
	/**
	 * 鲜花物流列表
	 */
	public void orderinfo(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Integer state = getParaToInt("state")==null?10:getParaToInt("state");
		Integer ishas = getParaToInt("ishas")==null?10:getParaToInt("ishas");
		Integer type=getParaToInt("type")==null?10:getParaToInt("type");
		
		String picicode = getPara("picicode");			// 查询批次
		String ordercode = getPara("ordercode");		// 订单编号
		String logisticcode = getPara("logisticcode");	// 物流编号
		String wuliucode = getPara("wuliucode")==null?"xzwl":getPara("wuliucode");	// 选择物流
		String receiver = getPara("receiver");	// 收货人
		//首次打开网页，默认显示最近批次的物流单
		if(state==10&&ishas==10&&type==10
				&&(picicode==null||picicode.equals(""))
				&&(ordercode==null||ordercode.equals(""))
				&&(logisticcode==null||logisticcode.equals(""))
				&& wuliucode.equals("xzwl")
				&&(receiver==null||(receiver.equals("")))){
			picicode=	MCDao.getMaxPiciCode();
		}
		int istd = MCDao.validatetd();
		setAttr("istd", istd);
		
		if(receiver!=null){
			try {
				receiver = URLDecoder.decode(receiver,"utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Page<Record> page = MCDao.getOrderInfo(pageno, 16, picicode, ordercode, logisticcode, state, ishas, wuliucode, receiver,type);
		List<Record> wuliulist = MCDao.getWuliu();
		setAttr("picicode", picicode);
		setAttr("ordercode", ordercode);
		setAttr("wuliucode", wuliucode);
		setAttr("logisticcode", logisticcode);
		setAttr("wuliulist", wuliulist);
		setAttr("state", state);
		setAttr("ishas", ishas);
		setAttr("type",type);
		setAttr("receiver", receiver);
		setAttr("reachDate",MCDao.getReachDate());
		
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("orderinfolist", page.getList());
		render("orderinfo.html");		
	}
	
	
	
	
	
	/**
	 * 预配单  物流列表
	 */
	public void orderinfo_pre(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Integer state = getParaToInt("state")==null?10:getParaToInt("state");
		Page<Record> page = MCDao.getOrderInfo_pre(pageno, 16,state);
		setAttr("state", state);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("orderinfolist", page.getList());
		render("orderinfo_pre.html");	
	}
	
	// 物流详情
	public void orderpro() throws JdException{
		int oid  = getParaToInt(0)==null?1:getParaToInt(0);
		String code  = getPara(1);
		Map<String, Object> map = MCDao.getOrderPro(oid,code);
		Integer aid = Db.queryInt("select aid from f_order_info where id = ?",oid);
		List<Record> list4 = Db.find("select id from f_order_info  WHERE aid = ?  AND state in (2,3) ORDER BY ctime DESC LIMIT 4",aid);
		for(Record oi:list4){
			List<Record> oplist = Db.find("select pid,type from f_order_pro where oid=?", oi.getInt("id"));	// 物流详细集合
			String name = new String();	// 物流产品名称
			for(int i=0;i<oplist.size();i++){
				Record op = oplist.get(i);
				if(op.getInt("type") == 0){
					Record n = Db.findFirst("select fname,price,type,code from f_product where id=?", op.getInt("pid"));
					if(i==0){
						name = n.getStr("fname");
					}else{
						name += "," + n.getStr("fname");
					}
				}else if(op.getInt("type") == 1){
					Record n = Db.findFirst("select name,price from f_flower_pro where id=?", op.getInt("pid"));	
					if(i==0){
						name = n.getStr("name")+oi.getInt("num")+"件";
					}else{
						name += "," + n.getStr("name")+oi.getInt("num")+"件";
					}
				}else{
					Record n = Db.findFirst("select name,price from f_flower_pro where id=?", op.getInt("pid"));
					if(i==0){
						name = "（首单赠送）"+n.getStr("name");
					}else{
						name += "," + n.getStr("name");
					}
				}
			}
			oi.set("wlname", name);
		}
		setAttr("list4", list4);
		setAttr("orderinfo", map.get("orderinfo"));
		setAttr("logilist", map.get("logilist"));
		setAttr("jdlogilist", map.get("jdlogilist"));
		setAttr("prolist", map.get("prolist"));
		setAttr("order_jt", map.get("order_jt"));
		setAttr("picilist", map.get("picilist"));
		setAttr("jihuis", map.get("jihuis"));
		setAttr("jhcolors", map.get("jhcolors"));
		setAttr("dgsp", map.get("dgsp"));
		setAttr("cps", map.get("cps"));
		setAttr("ptid", map.get("ptid"));
		render("orderpro.html");
	}
	/**
	 * 正式配单
	 */
	public void startsingle(){
		Integer type = getParaToInt();
		setAttr("key", MCDao.getCode());
		setAttr("type", type);
		render("single.html");
	}
	
	/**
	 * 预配单
	 */
	public void startsingle_pre(){
		Integer type = getParaToInt();
		setAttr("key", MCDao.getCode());
		setAttr("type", type);
		render("single_pre.html");
	}
	
	/**
	 * 周边配单
	 */
	public void startsingle_ZB(){
		Integer type = getParaToInt();
		setAttr("key", MCDao.getCode());
		setAttr("type", type);
		render("single_ZB.html");
	}
	
	
	/**
	 * 鲜花发货
	 */
	public void seedgoods(){
		Map<String, Object> map1 = new HashMap<>();
		boolean result = false;
		String message;
		String code = Db.queryStr("select code_value from f_dictionary where code_key = ?","piCode");
		List<Record> orderinfo = Db.find("select number,lognumber from  f_order_info as a where "
				+ " not EXISTS (select b.ordercode from f_order as  b  where a.ordercode=b.ordercode and b.type in(3,43)) "
				+ " and code = ?",code);
		if (orderinfo!=null){
		for (Record record : orderinfo) {
			if (record.getStr("lognumber")==null||record.getStr("lognumber").equals("")==true) {
				result = true;
				message = "发货失败,批次"+code+"仍有第三方物流单未导入";
				map1.put("result", result);
				map1.put("message", message);
				renderJson(map1);return;
			} 
		}
		}
		String wlcode = getPara(0);
		int ishas = getParaToInt(1);
		int lx = getParaToInt(2)==null?0:getParaToInt(2);
		Map<String, Object> map = WLDao.seedGoods(wlcode, ishas, lx);
		map.put("result", result);
		renderJson(map);
	}
	
	/**
	 * 周边发货
	 */
	public void seedgoods_ZB(){
		Map<String, Object> map1 = new HashMap<>();
		boolean result = false;
		String message;
		String wlcode = getPara(0);
		int fpid=getParaToInt(1);
		String strWhere=" and 1=1 ";
		if(fpid!=0){
			strWhere=" and b.pid="+fpid;
		}
		/*List<Record> orderinfo = Db.find("select number,lognumber from  f_order_info as a "
				+ " left join f_order_pro as b on a.id=b.oid where state=1  "
				+ " and EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43))"
				+strWhere);
		if (orderinfo!=null){
		for (Record record : orderinfo) {
			if (record.getStr("lognumber")==null||record.getStr("lognumber").equals("")==true) {
				result = true;
				message = "发货失败,"+"仍有第三方物流单未导入";
				map1.put("result", result);
				map1.put("message", message);
				renderJson(map1);return;
			} 
		}
		}*/
		
		Map<String, Object> map = WLDao.seedGoods_ZB(wlcode, fpid);
		map.put("result", result);
		renderJson(map);
	}
	
	
	
	/**
	 * 鲜花  发送短信或模板消息
	 * 【订单发货提醒】
	 */
	public void sendMsg(){
		String piCode=getPara(0);
		Map<String, Object> map = WLDao.SendMsg(piCode);
		renderJson(map);
	}
	
	/**
	 *  周边 发送短信或模板消息
	 * 【订单发货提醒】
	 */
	public void sendMsg_ZB(){
		String fpid=getPara("fpid");
		String picicode = getPara("picicode");	
		String ordercode = getPara("ordercode");		
		String logisticcode = getPara("logisticcode");	
		String wuliucode = getPara("wuliucode");	// 选择物流
		String receiver = getPara("receiver");	// 收货人
		
		
		if(receiver!=null){
			try {
				receiver = URLDecoder.decode(receiver,"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		Map<String, Object> map = WLDao.SendMsg_ZB(picicode, ordercode, logisticcode, wuliucode, receiver,fpid);
		renderJson(map);
	}
	/**
	 * 编辑管家信息
	 */
	public void editBSMsg(){
		String piCode=getPara(0);
		setAttr("piCode", piCode);
		render("editBSMsg.html");
	}
	/**
	 * 发送短信或模板消息
	 * 【订单发货提醒之管家服务通知】
	 * @throws UnsupportedEncodingException 
	 */
	public void sendBSMsg() throws UnsupportedEncodingException{
		try {
			String piCode=getPara(0);	
		    String BSMsg =URLDecoder.decode( getPara(1),"UTF-8");
		    Map<String, Object> map = WLDao.SendBSMsg(piCode,BSMsg);
			renderJson(map);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}	
	}
	// 需要打印的物流信息
	public void printlist(){
		setAttr("printlist", WLDao.print());
		render("print.html");
	}
	// 导出物流
	public void exportwuliu() {
		String ecode = getPara(0);
		int ishas = getParaToInt(1);
		String code = getPara(2);
		boolean result = WLDao.getlogisticForExcel(getResponse(), ecode, ishas, code);
		if(result){
			renderNull();
		}else{
			renderText("存在异常订单，导出失败！");
		}
	}
	// 导出团长订单
	public void exportgroupleader() {
		String code=getPara("code");
		boolean result = WLDao.getgroupleaderForExcel(getResponse(),code);
		if(result){
			renderNull();
		}
	}
	
	/***
	 * 周边物流导出
	 */
	public void exportwuliu_ZB() {
		String ecode = getPara(0);
		int ishas = 10;
		int state = getParaToInt(1);
		int fpid=getParaToInt(2);
		boolean result = WLDao.getlogisticForExcel_ZB(getResponse(), ecode, ishas, state,fpid);
		if(result){
			renderNull();
		}else{
			renderText("存在异常订单，导出失败！");
		}
	}
	
	
	// 导入外包物流
	public void importwuliu() {
		List<Record> wuliulist = MCDao.getWuliu();
		setAttr("wuliulist", wuliulist);
		render("importwuliu.html");
	}
	
	public void importExcel() {
		Map<String, Object> map = new HashMap<>();
		boolean result= Db.tx(new IAtom() {
			 boolean save_flag =true;
			@Override
			public boolean run() throws SQLException {
				// TODO Auto-generated method stub
				 try {
	                    UploadFile up = getFile("uploadfile");
	                    String wlcode=getPara("wl");
	                    String wlname= Db.queryStr("select name from f_express where code = ?",wlcode);
	                    List<String[]> list = WLDao.getExcelData(up.getFile());
	                    Object[][] strlist = new Object[list.size()][];
	                    int i=0;
	                    for (String[] strings : list) {
	                    	String[] string = new String[4] ;
	                    	string[0]=strings[1];
	                    	string[1]=wlcode;
	                    	string[2]=wlname;
	                    	string[3]=strings[0];
	                        strlist[i]=string;
	                        i++;
	                    }
	                      Db.batch("update f_order_info set lognumber = ?,ecode=?,ename=? where number = ? ",strlist, 1000);
	                } catch (Exception e) {
	                    save_flag = false;
	                    e.printStackTrace();
	                }
				return save_flag;
			}
	            
	});  
		 String msg;
		 if(result){
			    msg = "导入成功";
			    map.put("result", result);
				map.put("msg", msg);
				renderJson(map);
			}else{
				msg = "导入失败";
			    map.put("result", result);
				map.put("msg", msg);
				renderJson(map);
			}
	}
		 

	// 更改订单产品
	public void changepro(){
		int pid = getParaToInt("pid");
		int orderid = getParaToInt("orderid");
		renderJson("result",MCDao.changeproduct(pid,orderid));
	}
	//出货扫描
	public void shipscan() {
		String picode = Db.queryStr("select code_value from f_dictionary where code_key = 'piCode'"); 
	 	List<Record> typeproductlist = MCDao.getTypeproduct(picode);
		List<Record> typeorderlist = MCDao.getTypeorder(picode);
		List<Record> typewuliulist = MCDao.getTypewuliu(picode);
		Long productsum1 = Db.queryLong("select  count(1)   from  f_order_info c left join  f_order_pro a on a.oid = c.id  left join f_flower_pro b on a.pid = b.id  where c.code = ? and a.type = 1 and c.state=1 ",picode);
		Long productsum0 = Db.queryLong("select  count(1)   from  f_order_info d left join  f_order_pro a  on a.oid = d.id left join f_product b  on a.pid = b.id left join f_flower_pro c on b.fpid = c.id  where b.code = ? and a.type = 0  and d.state=1  ",picode);
		Long productsum = productsum0 + productsum1;
		Long ordersum  = Db.queryLong("select count(1) from f_order_info a left join f_order b  on a.ordercode = b.ordercode  where a.code = ? and a.state=1",picode);
		Long wuliusum  = Db.queryLong("select count(1) from f_order_info where code = ? and state=1",picode);
		setAttr("typeproductlist", typeproductlist);
		setAttr("typeorderlist", typeorderlist);
		setAttr("typewuliulist", typewuliulist);
		setAttr("productsum", productsum);
		setAttr("ordersum", ordersum);
		setAttr("wuliusum", wuliusum);
		render("shipscan.html");
	}
	/**
	 * 物流信息检查
	 */
	public void wuliuinfo() {
		String picode = Db.queryStr("select code_value from f_dictionary where code_key = 'piCode'"); 
		String lognumber = getPara(0);
		String ename = Db.queryStr("select ename from f_order_info where lognumber = ?  and code = ? and state=1 ",lognumber,picode);
		List<Record> pname = Db.find("select name from ("+
		"(select b.name,c.lognumber   from  f_order_pro a  left join f_flower_pro b on a.pid = b.id left join f_order_info c on a.oid = c.id where c.code = ? and a.type = 1 and c.state=1 )"+
		"union"+
		"(select b.fname name ,d.lognumber from   f_order_info d left join  f_order_pro a  on a.oid = d.id left join f_product b  on a.pid = b.id left join f_flower_pro c on b.fpid = c.id where b.code = ? and a.type = 0  and d.state=1   ))X where lognumber =?",picode,picode,lognumber);
		String oname = Db.queryStr("select b.gName from f_order_info a left join f_order b  on a.ordercode = b.ordercode  where a.code = ?  and a.lognumber = ? and a.state=1 ",picode,lognumber);
		Map<String, Object> map = new HashMap<>();
		map.put("ename", ename);
		map.put("pname", pname);
		map.put("oname", oname);
		renderJson(map);
	}
	/**
	 * 物流检查成功反馈
	 */
	public void success() {
		String picode = Db.queryStr("select code_value from f_dictionary where code_key = 'piCode'"); 
		String lognumber = getPara(0);
		Db.update("update f_order_info set state = 4 where lognumber =? and code = ?",lognumber,picode);
		renderNull();
	}
	public void fahuocookie() {
		boolean result = true;
		Cookie[] ci = MCDao.cookie(getRequest());       
		if(ci.length > 0){
			for(Cookie cookie:ci){
				if("fahuo".equals(cookie.getName())){
					result = false;
					renderJson(result);return;
				}
			}
		}
		Cookie cookie = new Cookie("fahuo", "fahuo");
		cookie.setMaxAge(60*5);
		cookie.setPath("/manage/");
		getResponse().addCookie(cookie);
		renderJson("result",result);
	}
	
	public void fahuocookie_ZB() {
		boolean result = true;
		Cookie[] ci = MCDao.cookie(getRequest());       
		if(ci.length > 0){
			for(Cookie cookie:ci){
				if("fahuo-zb".equals(cookie.getName())){
					result = false;
					renderJson(result);return;
				}
			}
		}
		Cookie cookie = new Cookie("fahuo-zb", "fahuo-zb");
		cookie.setMaxAge(30);//30秒
		cookie.setPath("/manage/");
		getResponse().addCookie(cookie);
		renderJson("result",result);
	}
	/**
	 * 	确认收货-鲜花
	 */
	public void confirm() {
		String picode = getPara("picode");
		String strWhere = " and a.code='"+picode+"'";
		List<Record> orderinfo = Db.find("select a.ordercode, a.number,a.lognumber from  f_order_info as a "
				+ " left join f_order_pro as b on a.id=b.oid where a.state=2  "
				+ " and not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43))"
				+strWhere);
		for(Record record:orderinfo){
			Db.update("update f_order set state=2 where ordercode=? and ocount=cycle", record.getStr("ordercode"));//订单待评价
			Db.update("update f_order_info set state=9 where number=?", record.getStr("number"));//物流单已签收
			//SendMsg(ClientCode);
		}
		renderJson("result",true);		
	}
	/**
	 * 	确认收货-周边
	 */
	public void confirm_ZB() {
		Integer fpid= getParaToInt("fpid");
		String picode = getPara("picode");
		String strWhere = " and a.code='"+picode+"'";
		if (fpid!=0) {
			strWhere += " and b.pid="+fpid;
		}
		List<Record> orderinfo = Db.find("select a.ordercode, a.number,a.lognumber from  f_order_info as a "
				+ " left join f_order_pro as b on a.id=b.oid where a.state=2  "
				+ " and EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43))"
				+strWhere);
		for(Record record:orderinfo){
			Db.update("update f_order set state=2 where ordercode=? and ocount=cycle", record.getStr("ordercode"));//订单待评价
			Db.update("update f_order_info set state=9 where number=?", record.getStr("number"));//物流单已签收
			//SendMsg(ClientCode);
		}
		renderJson("result",true);		
	}
/******************************************线下物流f_order_info_xx***********************************************************************/	
	/**
	 * 线下物流列表
	 */
	public void orderinfo_xx() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		String fCode = getPara("fCode"),fNumber=getPara("fNumber"),fwlName= getPara("fwlName")==null?"":getPara("fwlName");
		Page<Record>page =MCDao.getOrderInfo_XX(pageno, 16, fCode, fNumber, fwlName);
		List<Record> wuliulist = MCDao.getWuliu();
		setAttr("wuliulist", wuliulist);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("orderinfolist", page.getList());
		
		setAttr("fCode",fCode);
		setAttr("fNumber", fNumber);
		setAttr("fwlName", fwlName);
		render("order_info_xx.html");
	}
	/**
	 * 单个删除
	 */
	public void orderinfo_xx_del() {
		Integer id = getParaToInt("id");
		boolean  result =   Db.deleteById("f_order_info_xx", id);
		renderJson(result);
	}
	/**
	 * Excel导入模版
	 */
	public void exportExcel_template_xx() {
		boolean result = WLDao.gettemplateForXXWL(getResponse());
		if(result){
			renderNull();
		}else{
			renderText("异常！");
		}
	}
	/**
	 * 导入页面
	 */
	public void importwuliu_xx() {
		List<Record> wuliulist = MCDao.getWuliu();
		setAttr("wuliulist", wuliulist);
		render("importwuliu_xx.html");
	}
	/**
	 * 物流导出
	 */
	public void exportExcel_xx() {
		String fCode = getPara("fCode"),fNumber=getPara("fNumber"),fwlName=getPara("fwlName");
		boolean result = WLDao.getExcelForXXWL(getResponse(),fCode,fNumber,fwlName);
		if(result){
			renderNull();
		}else{
			renderText("异常！");
		}
	}
	public void importExcel_xx() {
		Map<String, Object> map = new HashMap<>();
		boolean result= Db.tx(new IAtom() {
			 boolean save_flag =true;
			@Override
			public boolean run() throws SQLException {
				// TODO Auto-generated method stub
				 try {
	                    UploadFile up = getFile("uploadfile");
	                    String wlcode=getPara("wl");
	                    String wlname= Db.queryStr("select name from f_express where code = ?",wlcode);
	                    List<String[]> list = WLDao.getExcelData(up.getFile());
	                    List<Record>rList=new ArrayList<>();
	                    Record admin = getSessionAttr("admin");
	            		String fUser=admin.getStr("username");//操作账号
	                    Date fOptime = new Date();
	                    for (String[] strings : list) {
	                        Record record =new Record();
	                        record.set("fCode", strings[0]);
	                        record.set("fNumber", strings[1]);
	                        record.set("fGname", strings[2]);
	                        record.set("fDate", strings[3]);
	                        record.set("fwlName",wlname);
	                        record.set("fUser",fUser);
	                        record.set("fOptime",fOptime);
	                        boolean update = Db.update("f_order_info_xx", "fNumber", record);
	                        if (!update) {
	                        	 rList.add(record);
							}         
	                    }
	                    Db.batchSave("f_order_info_xx", rList, 1000);	                    
	                } catch (Exception e) {
	                    save_flag = false;
	                    e.printStackTrace();
	                }
				return save_flag;
			}
	            
	});  
		 String msg;
		 if(result){
			    msg = "导入成功";
			    map.put("result", result);
				map.put("msg", msg);
				renderJson(map);
			}else{
				msg = "导入失败";
			    map.put("result", result);
				map.put("msg", msg);
				renderJson(map);
			}
	}
	/*************************物流价格导入模版****************************/
	/**
	 * Excel导入模版
	 */
	public void exportExcel_template_wlprice() {
		boolean result = WLDao.gettemplateForWLPR(getResponse());
		if(result){
			renderNull();
		}else{
			renderText("异常！");
		}
	}
	/**
	 * 导入页面
	 */
	public void importwuliu_wlprice() {
		render("importwuliu_wlpr.html");
	}
	/*
	 * Excel导入
	 */
	public void importExcel_wlprice() {
		Map<String, Object> map = new HashMap<>();
		boolean result= Db.tx(new IAtom() {
			 boolean save_flag =true;
			@Override
			public boolean run() throws SQLException {
				// TODO Auto-generated method stub
				 try {
	                    UploadFile up = getFile("uploadfile");
	                    List<String[]> list = WLDao.getExcelData(up.getFile());
	                    String lognumber = null;
	                    String price = null;
	                    for (String[] strings : list) {
	                        lognumber = strings[0];
	                        price = strings[1]; 
	                        Db.update("update f_order_info set price = ? where lognumber = ?",price,lognumber );
	                        Db.update("update f_order_info_xx set fPrice = ? where fNumber = ?",price,lognumber );
	                    }
	                } catch (Exception e) {
	                    save_flag = false;
	                    e.printStackTrace();
	                }
				return save_flag;
			}
	            
	});  
		 String msg;
		 if(result){
			    msg = "导入成功";
			    map.put("result", result);
				map.put("msg", msg);
				renderJson(map);
			}else{
				msg = "导入失败";
			    map.put("result", result);
				map.put("msg", msg);
				renderJson(map);
			}
	}
}