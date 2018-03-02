package com.controller.manage;




import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.Date;

import com.dao.CARDDao;
import com.dao.MCDao;

import com.interceptor.ManageInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
@Before(ManageInterceptor.class)
public class ManageCardCtrl extends Controller {

	/***
	 * 制作兑换卡
	 */
	
	public void createcard(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Page<Record> page = MCDao.getCardType(pageno, 16);
		setAttr("cardtypelist", page.getList());
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		render("createcard.html");
	}
	
	
	
	/**
	 * 向数据库插入兑换卡信息(卡号顺序排列)
	 */
	public void savecard(){
		boolean result = false;
		int count=getParaToInt("count");//数量
		Date date=getParaToDate("date");//截止日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date);
		int no=getParaToInt("no");//卡编号
		Record admin = getSessionAttr("admin");
		String username=admin.getStr("username");//操作账号
		
		//今天的最大编号
		String maxCId=Db.queryStr("select IFNULL(MAX(cId),CONCAT(SUBSTR(DATE_FORMAT(NOW(),'%Y%m%d') FROM 3),'0000') ) from f_card where  DATE_FORMAT(cMakeTime,'%Y-%m-%d')=DATE_FORMAT(NOW(),'%Y-%m-%d')");
		Long cId=Long.parseLong(maxCId);
		for(int i=0;i<count;i++){
			cId=cId+1;
			Date nowDate=new Date();
			Record record = new Record();
			record.set("cId", cId);
			record.set("cFkNo", no);
			record.set("cMaker", username);
			record.set("cMakeTime",nowDate);
			record.set("cEffDate", dateStr);
			result = Db.save("f_card", record);
		}
		renderJson("result", result);
	}
	
	/**
	 * 向数据库插入兑换卡信息(卡号随机)
	 *//*
	public void savecard(){
		boolean result = false;
		int count=getParaToInt("count");//数量
		Date date=getParaToDate("date");//截止日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date);
		int no=getParaToInt("no");//卡编号
		Record admin = getSessionAttr("admin");
		String username=admin.getStr("username");//操作账号
		

		Date nowDate=new Date();
		for(int i=0;i<count;i++){
			long cId1=getRandNum(10000,99999);//随机产生9位数
			long cId2=getRandNum(1000,9999);
			long cId=Long.parseLong( String.valueOf(cId1)+String.valueOf(cId2));
			long flag=Db.queryLong(String.format("select count(*) from f_card where cId=%d", cId));
			if(flag != 0){
				cId1=getRandNum(1,99999);
				cId2=getRandNum(1,9999);
				cId=Long.parseLong( String.valueOf(cId1)+String.valueOf(cId2));
			}
			Record record = new Record();
			record.set("cId", cId);
			record.set("cFkNo", no);
			record.set("cMaker", username);
			record.set("cMakeTime",nowDate);
			record.set("cEffDate", dateStr);
			result = Db.save("f_card", record);
		}
		renderJson("result", result);
	}
	*/
	public static long getRandNum( long min, long max) {
	    long randNum = min + (long)(Math.random() * ((max - min) + 1));
	    return randNum;
	}


	
	/**
	 * 制卡参数设置
	 */
	public void createcard_set(){
		
		String cNo=getPara("cNo");//卡编号
		String cName=getPara("cName");//卡名
		String cMoney=getPara("cMoney");//卡面值
		String name=getPara("name");//商品名称
		String cCycle=getPara("cCycle");//订阅次数
		String cHasVase=getPara("cHasVase");//是否包含花瓶
		
		setAttr("cNo",cNo);
		setAttr("cName",cName);
		setAttr("cMoney",cMoney);
		setAttr("name",name);
		setAttr("cCycle",cCycle);
		setAttr("cHasVase",cHasVase);
		
		render("createcard_set.html");
	}
	
	/***
	 * 兑换卡列表
	 */
	public void querycard(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Integer cNo = getParaToInt("cNo")==null?1:getParaToInt("cNo");
		Integer isSuccess = getParaToInt("isSuccess")==null?1:getParaToInt("isSuccess");
		String cId = getPara("cId");	
		String cMakeTime = getPara("cMakeTime");
		Page<Record> page = MCDao.getCard(pageno, 16,cNo,isSuccess,cId,cMakeTime);
		setAttr("cardlist", page.getList());
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		
		List<Record> cardtypelist=MCDao.getCardType();
		setAttr("cardtypelist",cardtypelist);
		
		setAttr("cNo",cNo);
		setAttr("isSuccess",isSuccess);
		setAttr("cId",cId);
		setAttr("cMakeTime", cMakeTime); 
		render("querycard.html");
	}
	
	/***
	 * 导出兑换卡
	 */
	public void exportcard(){
		int cNo = getParaToInt("cNo");
		int isSuccess = getParaToInt("isSuccess");
		String cId = getPara("cId");
		String cMakeTime = getPara("cMakeTime");
		CARDDao.exportCardList(getResponse(),cNo,isSuccess,cId,cMakeTime);
		renderNull();
		
	}
	
	
}

