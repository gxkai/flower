package com.controller.manage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import com.dao.MCDao;
import com.dao.PurchaseDao;
import com.interceptor.ManageInterceptor;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.Result;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.xpath.internal.functions.Function;
import com.util.UploadImageUtil;

/**
 * @Desc 服务管理
 * @author lxx
 * @date 2016/8/12
 * */
@Before(ManageInterceptor.class)
public class ManageServiceCtrl extends Controller {
	/*********************常见问题*********************/
	// 列表
	public void question(){
		Integer pageno = getParaToInt()==null?1:getParaToInt();		
		Page<Record> page = MCDao.getQuestion(pageno, 16);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("questionlist", page.getList());
		render("question.html");
	}
	// 保存数据
	public void saveQuestion(){
		boolean result = false;
		Record record = new Record();
		record.set("title", getPara("title"));
		record.set("info", getPara("info"));
		Integer id = getParaToInt("id");
		if(id == null){
			result = Db.save("f_question", record);
		}else{
			record.set("id", id);
			result = Db.update("f_question", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getQuestion(){
		Integer id = getParaToInt(0);
		if(id != null){
			setAttr("question", Db.findById("f_question", id));
		}
		render("question_detail.html");
	}
	// 删除数据
	public void delQuestion(){
		int id = getParaToInt();
		renderJson(Db.deleteById("f_question", id));
	}
	
	/*********************养花须知*********************/
	// 列表
	public void knowledge() throws UnsupportedEncodingException{
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		Integer type = getParaToInt(1)==null?0:getParaToInt(1);
		String tcode = getPara(2);
		if(tcode!=null){
			tcode = URLDecoder.decode(tcode,"utf-8");
		}
		Page<Record> page = MCDao.getKnowledge(pageno, 16, type, tcode);
		
		setAttr("type", type);
		setAttr("titlecode", tcode);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("knowledgelist", page.getList());
		render("knowledge.html");
	}
	// 保存数据
	public void saveKnowledge() throws IOException{
		boolean result = false;
		Record record = new Record();
		record.set("title", getPara("title"));
		String basestr = getPara("basestr");
		if(basestr != null && !"".equals(basestr)){
			String imgurl = UploadImageUtil.upLoadBase(basestr);
			record.set("imgurl", imgurl);
		}
		record.set("summary", getPara("summary"));
		record.set("info", getPara("info"));
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("type", getParaToInt("type"));
			result = Db.save("f_knowledge", record);
		}else{
			record.set("id", id);
			result = Db.update("f_knowledge", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getKnowledge(){
		Integer id = getParaToInt();
		if(id != null){
			setAttr("knowledge", Db.findById("f_knowledge", id));
		}
		render("knowledge_detail.html");
	}
	// 添加数据
	public void toAddKnowledge(){
		int type = getParaToInt();
		setAttr("type", type);
		render("knowledge_detail.html");
	}
	// 删除数据
	public void delKnowledge(){
		int id = getParaToInt();
		renderJson(Db.deleteById("f_knowledge", id));
	}
	// 置顶操作
	public void istopKnowledge(){
		int id = getParaToInt(0);
		int top = getParaToInt(1);
		top = top==1?0:1;
		Record record = new Record().set("id", id).set("istop", top);
		boolean result = Db.update("f_knowledge", record);
		renderJson(result);
	}
	//企业定制列表
	public void companyList() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
    	String ctime_start = getPara("ctime_start");
    	String ctime_end =  getPara("ctime_end");
    	Page<Record> page = MCDao.getCompanyList(pageno,16,ctime_start,ctime_end);
    	for(Record record:page.getList()){
    		String area = record.getStr("area");
    		Integer pidprov =Integer.valueOf( area.split(",")[0]);
    		Integer pidcity =Integer.valueOf( area.split(",")[1]);
    		Integer pidtown =Integer.valueOf( area.split(",")[2]);
    		String prov =Db.queryStr("select name from f_area where id = ?",pidprov);
    		String city = Db.queryStr("select name from f_area where id = ?",pidcity);
    		String town = Db.queryStr("select name from f_area where id = ?",pidtown);
    		String newarea = prov + "-" + city + "-" + town ;
    		record.set("area", newarea);
    	}
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("companylist", page.getList());
		setAttr("ctime_start", ctime_start);
		setAttr("ctime_end", ctime_end);
    	render("company_list.html");
	}
	/**
	 * 发票信息列表
	 */
	public void receiptList() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Integer state = getParaToInt("state")==null?10:getParaToInt("state");
		Page<Record> page = MCDao.getReceiptList(pageno,16,state);
		for(Record record:page.getList()){
			if (StrKit.notBlank(record.getStr("area"))) {
				String area = record.getStr("area");
	    		record.set("area", area);
			}	
    	}
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("receiptlist", page.getList());
		setAttr("state", state);
    	render("receipt_list.html");
		
	}
	/**
	 * 编辑发票状态
	 */
	public void receiptEdit() {
		Integer id = getParaToInt(0);
		Record record = Db.findById("f_receipt", id);
		setAttr("list", record);
		render("receipt_edit.html");
	}
	/**
	 * 保存编辑
	 */
	public void receiptEditSave() {
		Integer id = getParaToInt("id");
		Integer state = getParaToInt("state");
		boolean result =false;
		int re = Db.update("update f_receipt set state = ? where id = ?", state,id);
		if (re==1) {
			result=true;
		}
		renderJson(result);
	}
	/**
	 * 发票详情
	 */
	public void receiptDetail() {
		Integer id = getParaToInt(0);
		List<Record>rList = Db.find("SELECT a.company,a.code,a.name, a.fcode,b.ordercode,c.gName FROM f_receipt a LEFT JOIN f_receipt_detail b on a.fcode = b.fcode LEFT JOIN f_order c on b.ordercode = c.ordercode where a.id = ?",id);
		setAttr("list", rList);
		render("receipt_detail.html");
	}
}
