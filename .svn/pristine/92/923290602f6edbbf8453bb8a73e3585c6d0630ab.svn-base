package com.controller.manage;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random; 

import com.dao.MCDao;
import com.interceptor.ManageInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.util.Constant;
import com.util.MD5Util;

import sun.management.counter.Variability;

/**
 * @Desc 优惠管理
 * @author lxx
 * @date 2016/8/24
 * */
@Before(ManageInterceptor.class)
public class ManageBenefitCtrl extends Controller {
	/*********************花票设置*********************/
	// 列表
	public void cash(){
		Integer pageno = getParaToInt()==null?1:getParaToInt();
		Page<Record> page = MCDao.getCash(pageno, 12);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("cashlist", page.getList());
		render("cash.html");
	}
	// 保存数据
	public void saveCash(){
		boolean result = false;
		String name = getPara("name");
		String etime = getPara("etime");
		int ltime=getParaToInt("ltime");
		int maxcount=getParaToInt("maxcount");
		Record record = new Record();
		record.set("name", name);
		record.set("ltime", ltime);
		record.set("etime", etime);
		record.set("maxcount", maxcount);
		record.set("ctime", new Date());
		
		int numN = Db.queryNumber("select count(1) from f_cashtheme where name=?", name).intValue();
		String urlMd5="";
		if(numN==0){
			result = Db.save("f_cashtheme", record);
			urlMd5=Constant.getHost+"/account/receiveCash/"+record.getLong("id")+"-"+MD5Util.getMd5str(record.getLong("id").intValue());
		}else{
			Integer id = getParaToInt("id");
			record.set("id", id);
			result = Db.update("f_cashtheme", record);
			urlMd5=Constant.getHost+"/account/receiveCash/"+record.getInt("id")+"-"+MD5Util.getMd5str(record.getInt("id"));
		}
		record.set("urlmd5", urlMd5);
		result = Db.update("f_cashtheme", record);
		renderJson("result", result);
	}
	// 获取单条数据
	public void getCash(){
		Integer id = getParaToInt(0);
		if(id != null){
			setAttr("cashtheme", Db.findById("f_cashtheme", id));
		}
		render("cash_detail.html");
	}
	// 获取主题分类
	public void classify(){
		int id = getParaToInt();
		List<Record> classifylist= Db.find("select a.id,a.money,a.benefit,case a.ptid WHEN 0 THEN '所有产品' ELSE b.name END AS name,"
				+ " case when onlyfirst=0 then '否' else '是' end 'onlyfirst'"
				+ " from f_cashclassify a LEFT JOIN f_flower_pro b ON a.ptid = b.id where a.tid = ? and a.state = 0", id);
		List<Record> cashlist = Db.find("SELECT * FROM f_cashtheme WHERE id > 3 AND ltime>0");
		setAttr("state", Db.findById("f_cashtheme", id).getInt("state"));
		setAttr("tid", id);
		setAttr("cashlist", cashlist);
		setAttr("classifylist",classifylist );
		render("classify.html");
	}
	
	// 获取花票兑换码
	public void getExchange(){
		Integer id = getParaToInt(1);//花票分类表f_cashclassify的id
		List<Record> exchangelist = Db.find("SELECT ltime FROM f_cashtheme where id ="+ id);
		setAttr("exchangelist", exchangelist);
		if(id != null){
			setAttr("id", id);
			setAttr("ltime", Db.queryInt("select ltime from f_cashclassify a left join f_cashtheme b on a.tid=b.id where a.id=?",id));
		}else{
			setAttr("ptid", 0);
			setAttr("tid", getParaToInt(0));
		}
		render("cash_code.html");
	}
	
	// 保存生成兑换码详情
	public void saveExchange(){
		boolean result = false;
		int num = getParaToInt("num");//数量
		int ltime=getParaToInt("ltime");//有效天数
		int id=getParaToInt("id");//花票分类id
		
		Long count=Db.queryLong("select count(1) from f_cashclassify as a "
				+ " left join f_cashtheme as b on a.tid=b.id where a.id=? and maxcount>sendcount",id);
		if(count>0){
			Date d = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String time_a = format.format(d);
			
			Calendar ca = Calendar.getInstance();
			ca.add(Calendar.DATE, ltime);// num为增加的天数，可以改变的
			d = ca.getTime();
			String time_b = format.format(d);
			
			for(int i=0;i<num;i++){
				Record f_cash = new Record();
				f_cash.set("aid", 0);
				f_cash.set("cid", id);
				f_cash.set("code", genCodes(6,1));//6位数字和字符的随机码
				f_cash.set("time_a", time_a);//有效时间（开始）
				f_cash.set("time_b",time_b);//有效时间（结束）
				f_cash.set("state", 1);
				f_cash.set("origin", 4);
				result = Db.save("f_cash", f_cash);
			}
			Db.update("update f_cashtheme set sendcount=sendcount+?"
					+ " where id in(select tid from f_cashclassify where id=?)",num,id);
		}
		
		renderJson("result", result);
	}
	/**
	 * 生成6位数的随机兑换码
	 * @param length
	 * @param num
	 * @return
	 */
	public static String genCodes(int length,long num){
	    List<String> cash_code=new ArrayList<String>();
	    for(int j=0;j<num;j++){
	      String val = "";   
	      Random random = new Random();   
	      for(int i = 0; i < length; i++)   
	      {   
	        String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字   	              
	        if("char".equalsIgnoreCase(charOrNum)) // 字符串   
	        {   
	          int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; //取得大写字母还是小写字母   
	          val += (char) (choice + random.nextInt(26));   
	        }   
	        else if("num".equalsIgnoreCase(charOrNum)) // 数字   
	        {   
	          val += String.valueOf(random.nextInt(10));   
	        }   
	      }
	      val=val.toLowerCase().replace('o', '0');  //把字符串转换为小写,并把小写字母o用0替换
	      if(cash_code.contains(val)){
	        continue;
	      }else{
	    	  cash_code.add(val);
	      }
	    }
	    return cash_code.toString().substring(1,7);
	}  

	
	// 获取主题分类详情
	public void getClassify(){
		Integer id = getParaToInt(1);
		List<Record> flowerprolist = Db.find("SELECT * FROM f_flower_pro ");
		setAttr("flowerprolist", flowerprolist);
		if(id != null){
			setAttr("classify", Db.findById("f_cashclassify", id));
		}else{
			setAttr("ptid", 0);
			setAttr("tid", getParaToInt(0));
		}
		render("classify_detail.html");
	}	
	
	// 保存主题分类详情
	public void saveClassify(){
		boolean result = false;
		Record record = new Record();
		String money = getPara("money");
		String benefit = getPara("benefit");
		int ptid =  getParaToInt("ptid");
		int onlyfirst=getParaToInt("onlyfirst");//限首单使用吗？
		record.set("money",money );
		record.set("benefit", benefit);
        record.set("ptid", ptid);
        record.set("onlyfirst", onlyfirst);
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("tid", getParaToInt("tid"));
			result = Db.save("f_cashclassify", record);
		}else{
			record.set("id", id);
			result = Db.update("f_cashclassify", record);
		}
		renderJson("result", result);
	}
	// 删除主题分类
	public void delClassify(){
		int id = getParaToInt();
		boolean result = false;
		Record record = Db.findFirst("SELECT b.id FROM f_cashclassify a LEFT JOIN f_cashtheme b ON a.tid=b.id WHERE a.id=? AND b.state=0", id);
		if(record != null){
			Db.update("update f_cashclassify set state = 1 where id=?",id);
			result = true;
		}
		renderJson(result);
	}
	
	/*********************花票统计*********************/
	// 列表
	public void cashsatistic(){
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		Integer themeid = getParaToInt(1)==null?9999:getParaToInt(1);
		Integer state=getParaToInt(4)==null?2:getParaToInt(4);
		String pushid = getPara(2);
		String aid = getPara(3);
		Page<Record> page = MCDao.getCashsatistic(pageno, 12, themeid, pushid, aid,state);
		List<Record> cashthemelist = MCDao.getCashthemes();
		setAttr("themeid", themeid);
		setAttr("pushid", pushid);
		setAttr("aid", aid);
		setAttr("cashthemelist", cashthemelist);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("cashsatlist", page.getList());
		setAttr("state",state);
		render("cash_statistic.html");
	}
	
	/*********************活动设置*********************/
	// 列表
	public void activity(){
		Integer pageno = getParaToInt()==null?1:getParaToInt();
		Page<Record> page = MCDao.getActivity(pageno, 16);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("activitylist", page.getList());
		render("activity.html");
	}
	// 保存数据
	public void saveActivity(){
		boolean result = false;
		Record record = new Record();
		record.set("name", getPara("name"));
		record.set("money", getPara("money"));
		record.set("benefit", getPara("benefit"));
		record.set("time_a", getParaToDate("time_a"));
		record.set("time_b", getParaToDate("time_b"));
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("ctime", new Date());
			result = Db.save("f_activity", record);
		}else{
			record.set("id", id);
			if(id == 1){
				record.set("gid", getParaToInt("gid"));
			}
			result = Db.update("f_activity", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getActivity(){
		Integer id = getParaToInt(0);
		List<Record> ars = MCDao.getAroundPros();
		setAttr("ars", ars);
		if(id != null){
			setAttr("activity", Db.findById("f_activity", id));
		}
		render("activity_detail.html");
	}
	// 删除数据
	public void delActivity(){
		int id = getParaToInt();
		Record record = new Record();
		record.set("id", id);
		record.set("state", 1);
		renderJson(Db.update("f_activity", record));
	}
}
