package com.controller.manage;

import java.util.List;
import java.util.Map;

import com.dao.MCDao;
import com.dao.PurchaseDao;
import com.dao.RepDao;
import com.dao.WLDao;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import sun.net.www.content.audio.x_aiff;

/***
 *各类分析报表
 * @author SHICHUNXIANG
 *
 */
public class ManageReportCtrl extends Controller {
	/**
	 * 导出每单盈亏
	 * 2018-02-01 
	 * GUXUKAI
	 */
	public void exportExcel_ordercode() {
		String code_start = getPara("code_start"),code_end=getPara("code_end"),ordercode=getPara("ordercode"),ename=getPara("ename");
		boolean result = RepDao.getExcelForORDERCODE(getResponse(),code_start,code_end,ordercode,ename);
		if(result){
			renderNull();
		}else{
			renderText("异常！");
		}
	}
	/**
	 * 2018-01-24 
	 * GUXUKAI
	 * 每单盈亏
	 */

	public void ordercode() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
 		String code_start = getPara("code_start"),code_end=getPara("code_end"),ordercode= getPara("ordercode"),ename=getPara("ename")==null?"":getPara("ename");
 		if(getParaToInt("pageno")==null) {
 			String code=Db.queryStr("select distinct(code) from f_order_info order by code desc limit 1");
 			code_start=code;
 			code_end=code;
 		}
 		Page<Record>page =RepDao.OrderCode(pageno, 16, code_start, code_end,ordercode,ename);
 		//Double sumprice = RepDao.OrderCode(1, 1000000, code_start, code_end,ordercode,ename).getList().stream().mapToDouble(Record ->Double.valueOf(Record.getStr("differ"))).sum();
 		String sumprice = RepDao.getSumPrice(code_start, code_end, ordercode, ename);
 		List<Record> wuliulist = MCDao.getWuliu();
		setAttr("wuliulist", wuliulist);
 		setAttr("pageno", page.getPageNumber());
 		setAttr("totalpage", page.getTotalPage());
 		setAttr("totalrow", page.getTotalRow());
 		setAttr("list", page.getList());
 		setAttr("code_start",code_start);
 		setAttr("code_end", code_end);
 		setAttr("ordercode",ordercode); 
 		setAttr("ename",ename);
 		setAttr("sumprice",sumprice );
		render("ordercode.html");
	}

	/**
	 * 单批发货商品统计表
	 */
	public void RepSendGoods(){
		String picicode=getPara("picicode");//批号(批次号)
		if(picicode==null||picicode.equals("")){
			picicode=	MCDao.getMaxPiciCode();
		}
		List<Record> recordList=RepDao.QuerySendGoods(picicode);
		setAttr("list",recordList);//结果列表
		setAttr("picicode", picicode);//批次号
		render("repSendGoods.html");
	}
	/**
	 * 发货单量统计表
	 */
	public void RepSendOrder(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		String picicodeStart=getPara("picicodeStart");
		String picicodeEnd=getPara("picicodeEnd");
		List<Record> list=RepDao.QuerySendOrder(picicodeStart,picicodeEnd);
		setAttr("list",list);//结果列表
		setAttr("picicodeStart", picicodeStart);
		setAttr("picicodeEnd", picicodeEnd);
		render("repSendOrder.html");
	}
	/**
	 * 单批分配产品统计表
	 */
	public void RepAllotProduct(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Integer type=getParaToInt("type")==null?2:getParaToInt("type");//1预分配 2正式分配
		Integer isprice=getParaToInt("isprice")==null?2:getParaToInt("isprice");//1显示价格 2不显示价格
		String picicode=getPara("picicode");//批号(批次号)
		if(picicode==null||picicode.equals("")){
			picicode=	MCDao.getMaxPiciCode();
		}
		Page<Record> page=RepDao.QueryAllotProduct(pageno, 16, picicode,type,isprice);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("list",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("picicode", picicode);
		setAttr("type",type);
		setAttr("isprice",isprice);
		render("repAllotProduct.html");
	}
	
	/**
	 * 发货预测
	 */
	public void RepSendForecast(){
		String picicode=getPara("picicode");//批号(批次号),需要用户输入
		int type=getParaToInt("type")==null?1:getParaToInt("type");//1:非定制  2：定制
		if(picicode==null||picicode.equals("")){
			picicode=	MCDao.getMinPiciCode();
		}
		List<Record> recordList=RepDao.QuereySendForecast(picicode,type);
		setAttr("list",recordList);//结果列表
		setAttr("picicode",picicode);//批次号
		setAttr("type",type);
		render("repSendForecast.html");
	}
	
	/**
	 * 忌讳花类预测
	 */
	public void RepJhFlower(){
		String picicode=getPara("picicode");//批号(批次号),需要用户输入
		if(picicode==null||picicode.equals("")){
			picicode=	MCDao.getMinPiciCode();
		}
		Map<String, Integer> map=RepDao.QueryJhFlower(picicode);
		setAttr("datas",map);//结果列表
		setAttr("picicode",picicode);//批次号
		render("repJhFlower.html");
	}
	
	/**
	 * 忌讳色系预测
	 */
	public void RepJhColor(){
		String picicode=getPara("picicode");//批号(批次号),需要用户输入
		if(picicode==null||picicode.equals("")){
			picicode=	MCDao.getMinPiciCode();
		}
		Map<String, Integer> map=RepDao.QueryJhColor(picicode);
		setAttr("datas",map);//结果列表
		setAttr("picicode",picicode);//批次号
		render("repJhColor.html");
	}
	
	public void RepCFflower(){
		String picicode=getPara("picicode");//批号(批次号),需要用户输入
		if(picicode==null||picicode.equals("")){
			picicode=	MCDao.getMinPiciCode();
		}
		List<Record> recordList=RepDao.QueryCfForecast(picicode);
		setAttr("list",recordList);//结果列表
		setAttr("picicode",picicode);//批次号
		render("repCfFlower.html");
	}
	public void dtanalysis() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		String stime = getPara("ctime_start");
		String etime = getPara("ctime_end");
		String xiadan = getPara("xiadan");
		Page<Record> page=RepDao.QueryDTanalysis(pageno, 16, stime,etime,xiadan);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("dtanalysislist",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("ctime_start", stime);
		setAttr("ctime_end", etime);
		setAttr("xiadan", xiadan);
		render("dtanalysis.html");
	}
	public void dtanalysis2() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		String stime = getPara("ctime_start");
		String etime = getPara("ctime_end");
		String guanzhu = getPara("guanzhu");
		Page<Record> page=RepDao.QueryDTanalysis2(pageno, 16, stime,etime,guanzhu);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("dtanalysislist",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("ctime_start", stime);
		setAttr("ctime_end", etime);
		setAttr("guanzhu", guanzhu);
		render("dtanalysis2.html");
	}
	/**
	 * 单批单量统计
	 */
	public void salestatics() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		String stime = getPara("ctime_start");
		String etime = getPara("ctime_end");
		int type = getParaToInt("type")==null?99:getParaToInt("type");//当前显示页;
		Page<Record> page=RepDao.QuerySalestatics(pageno, 16, stime,etime,type);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("salestaticslist",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("ctime_start", stime);
		setAttr("ctime_end", etime);
		setAttr("type", type);
		render("salestatics.html");
	}
	/**
	 * 粉丝量统计
	 */
	public void fansstatics() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		String stime = getPara("ctime_start");
		String etime = getPara("ctime_end");
		Page<Record> page=RepDao.QueryFansstatics(pageno, 16, stime,etime);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("fansstaticslist",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("ctime_start", stime);
		setAttr("ctime_end", etime);
		render("fansstatics.html");
	}
}
