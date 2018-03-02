package com.controller.manage;

import java.io.IOException;
import java.util.List;

import com.dao.LuckDrawDao;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.util.QianNiuUpload;
import com.util.UploadImageUtil;

public class ManageLuckDrawCtrl extends Controller{
	/**
	 * 奖池管理
	 */
	public void managePrizeList(){
		Integer state=getParaToInt("state")==null?10:getParaToInt("state");
		Integer type=getParaToInt("type")==null?0:getParaToInt("type");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=LuckDrawDao.GetPrizeList(type,state, pageno, 16);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("prizeList",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("state", state);//状态
		setAttr("type", type);//适用活动
		render("prizeList.html");
	}
	
	/**
	 * 新增奖品
	 * @throws IOException 
	 */
	public void SavePrize() throws IOException{
		boolean result = false;
		String pName=getPara("pName");//奖品简称
		String pMsg=getPara("pMsg");//中奖信息
		String pImgurl=getPara("pImgurl");//奖品图片地址
		int pNum=getParaToInt("pNum");//库存/权重
		int pAreaS=getParaToInt("pAreaS");//中奖区间开始
		int pAreaE=getParaToInt("pAreaE");//中奖区间结束
		int origin=getParaToInt("origin");//奖品来源
		int contentId=getParaToInt("contentId");//奖品内容
		int state=getParaToInt("state");//状态
		int type=getParaToInt("type");//类型，双11使用
		int wScore=getParaToInt("wScore");//所需见证分数
		Double money = Double.parseDouble( getPara("money"));//中奖金额，双11使用
		
		if (StrKit.notBlank(pImgurl)) {
			pImgurl = UploadImageUtil.upLoadBase(pImgurl);
		}
		Record record = new Record();
		record.set("pName",pName);
		record.set("pMsg",pMsg);
		record.set("pimgurl",pImgurl);
		record.set("pNum", pNum );
		record.set("pAreaS", pAreaS);
		record.set("pAreaE", pAreaE);
		record.set("origin", origin);
		record.set("contentId", contentId);
		record.set("state", state);
		record.set("type", type);
		record.set("money", money);
		record.set("wScore", wScore);
		
		result = Db.save("f_prizeList", record);
		
		renderJson("result", result);
	}

	
	/**
	 * 编辑更新奖品信息
	 * @throws IOException 
	 */
	public void EditPrize() throws IOException{
		boolean result = false;
		int id=getParaToInt("id");//奖品ID号
		String pName=getPara("pName");//奖品简称
		String pMsg=getPara("pMsg");//中奖信息
		String pImgurl=getPara("pImgurl");//奖品图片地址
		int pNum=getParaToInt("pNum");//库存/权重
		int pAreaS=getParaToInt("pAreaS");//中奖区间开始
		int pAreaE=getParaToInt("pAreaE");//中奖区间结束
		int origin=getParaToInt("origin");//奖品来源
		int contentId=getParaToInt("contentId");//奖品内容
		int state=getParaToInt("state");//状态
		int type=getParaToInt("type");//类型
		int wScore=getParaToInt("wScore");//所需见证分数
		Double money = Double.parseDouble( getPara("money"));
				
		Record record = new Record();
		if (StrKit.notBlank(pImgurl)) {
			pImgurl = UploadImageUtil.upLoadBase(pImgurl);
			record.set("pimgurl",pImgurl);
		}
		
		
		record.set("id", id);
		record.set("pName",pName);
		record.set("pMsg",pMsg);
		
		record.set("pNum", pNum );
		record.set("pAreaS", pAreaS);
		record.set("pAreaE", pAreaE);
		record.set("origin", origin);
		record.set("contentId", contentId);
		record.set("state", state);
		record.set("money", money);
		record.set("type", type);
		record.set("wScore", wScore);
		
		result = Db.update("f_prizeList", record);
		
		renderJson("result", result);
	}
	
	/**
	 * 奖品的详细信息
	 */
	public void prizeDetail(){
		Integer id=getParaToInt(0);//奖品唯一编号
		if(id!=null){
			Record rd=LuckDrawDao.QueryPrizeInfo(id);
			setAttr("rd",rd);
		}
		render("prize_detail.html");
	}
	
	/**
	 * 查询中奖结果
	 */
	public void queryWinPrize(){
		Integer state=getParaToInt("state")==null?10:getParaToInt("state");
		Integer origin=getParaToInt("origin")==null?10:getParaToInt("origin");
		String aid=getPara("aid");
		Integer type=getParaToInt("type")==null?0:getParaToInt("type");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=LuckDrawDao.GetWinPrizeList(state, pageno, 16,origin,aid,type);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("prizeList",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("state", state);//状态
		setAttr("origin", origin);//奖品来源
		setAttr("aid",aid);//会员ID号
		setAttr("type",type);//适用什么活动
		render("winPrize.html");
	}
	/**
	 * 修改成【已经发放】
	 */
	public void updateState(){
		int id = getParaToInt();
		Record account = Db.findById("f_luckdraw_result", id);
		account.set("state", 1);
		renderJson(Db.update("f_luckdraw_result", account));
	}
	
	/**
	 * 双11专题
	 * 翻牌结果查询
	 */
	public void  queryTurnResult(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		String aid=getPara("aid");//中奖用户的ID
		String lrId=getPara("lrId");;
		Page<Record> page=LuckDrawDao. GetTrunResult(pageno,16,aid,lrId);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("prizeList",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("aid",aid);
		setAttr("lrId",lrId);
		render("trunResult.html");
	}
	/**
	 * 翻牌控制
	 */ 
	public void queryTurnControl() {
		Integer state=getParaToInt("state")==null?10:getParaToInt("state");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=LuckDrawDao.GetTurnList(state, pageno, 16);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("turnList",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("state", state);//状态
		render("turn_list.html");
		
	}
	/**
	 * 翻牌列表详情
	 */
	public void turnDetail(){
		Integer id=getParaToInt(0);//奖品唯一编号
		if(id!=null){
			Record rd=LuckDrawDao.QueryTurnDetailInfo(id);
			setAttr("rd",rd);
		}
		render("turn_list_detail.html");
	}
	/**
	 * 新增翻牌金额
	 * @throws IOException 
	 */
	public void SaveTurn() throws IOException{
		boolean result = false;
		String msg = getPara("msg");
		Double money=Double.parseDouble(getPara("money"));
		Double minMoney=Double.parseDouble(getPara("minMoney"));
		Double maxMoney=Double.parseDouble(getPara("maxMoney"));
		int minArea=getParaToInt("minArea");//中奖区间开始
		int maxArea=getParaToInt("maxArea");//中奖区间结束
		int state=getParaToInt("state");//状态
		
		Record record = new Record();
		record.set("msg",msg);
		record.set("money",money);
		record.set("minMoney",minMoney);
		record.set("maxMoney", maxMoney );
		record.set("minArea", minArea);
		record.set("maxArea", maxArea);
		record.set("state", state);
	
		result = Db.save("f_trun_list", record);
		
		renderJson("result", result);
	}
	/**
	 * 编辑更新奖品信息
	 * @throws IOException 
	 */
	public void EditTurn() throws IOException{
		boolean result = false;
		int id=getParaToInt("id");//奖品ID号
		String msg = getPara("msg");
		Double money=Double.parseDouble(getPara("money"));
		Double minMoney=Double.parseDouble(getPara("minMoney"));
		Double maxMoney=Double.parseDouble(getPara("maxMoney"));
		int minArea=getParaToInt("minArea");//中奖区间开始
		int maxArea=getParaToInt("maxArea");//中奖区间结束
		int state=getParaToInt("state");//状态
				
		Record record = new Record();
		
		
		record.set("id", id);
		record.set("msg",msg);
		record.set("money",money);
		record.set("minMoney",minMoney);
		record.set("maxMoney", maxMoney );
		record.set("minArea", minArea);
		record.set("maxArea", maxArea);
		record.set("state", state);
		
		result = Db.update("f_trun_list", record);
		
		renderJson("result", result);
	}
	/**
	 * 翻牌图片相关
	 */ 
	public void queryTurnImg() {
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=LuckDrawDao.GetTurnImgList(pageno, 16);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("turnimgList",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		render("turnimg_list.html");
		
	}
	/**
	 * 翻牌图片详情
	 */
	public void turnimgDetail(){
		Integer id=getParaToInt(0);//奖品唯一编号
		if(id!=null){
			Record rd=LuckDrawDao.QueryTurnImgDetailInfo(id);
			setAttr("rd",rd);
		}
		render("turnimg_list_detail.html");
	}
	/**
	 * 新增翻牌图片
	 * @throws IOException 
	 */
	public void SaveTurnImg() throws IOException{
		boolean result = false;
		String activeTitle = getPara("activeTitle");
		String activeDate = getPara("activeDate");
		Record record = new Record();
		
		String img01 = getPara("img01");
		if (StrKit.notBlank(img01)) {
			img01=QianNiuUpload.upload(img01);
			record.set("img01",img01);
			
		}
		String img02 = getPara("img02");
		if (StrKit.notBlank(img02)) {
			img02=QianNiuUpload.upload(img02);
			record.set("img02",img02);
		}
		String img03 = getPara("img03");
		if (StrKit.notBlank(img03)) {
			img03=QianNiuUpload.upload(img03);
			record.set("img03",img03);
		}
		String img04 = getPara("img04");
		if (StrKit.notBlank(img04)) {
			img04=QianNiuUpload.upload(img04);
			record.set("img04",img04);
		}
		String img05 = getPara("img05");
		if (StrKit.notBlank(img05)) {
			img05=QianNiuUpload.upload(img05);
			record.set("img05",img05);
		}
		String img06 = getPara("img06");
		if (StrKit.notBlank(img06)) {
			img06=QianNiuUpload.upload(img06);
			record.set("img06",img06);
		}
		
		String img07 = getPara("img07");
		if (StrKit.notBlank(img07)) {
			img07=QianNiuUpload.upload(img07);
			record.set("img07",img07);
		}
		String img08= getPara("img08");
		if (StrKit.notBlank(img08)) {
			img08=QianNiuUpload.upload(img08);
			record.set("img08",img08);
		}
		
		String img09 = getPara("img09");
		if (StrKit.notBlank(img09)) {
			img09=QianNiuUpload.upload(img09);
			record.set("img09",img09);
		}
		
		String img10 = getPara("img10");
		if (StrKit.notBlank(img10)) {
			img10=QianNiuUpload.upload(img10);
			record.set("img10",img10);
		}
		
		
		String img11 = getPara("img11");
		if (StrKit.notBlank(img11)) {
			img11=QianNiuUpload.upload(img11);
			record.set("img11",img11);
		}
		
		String img12 = getPara("img12");
		if (StrKit.notBlank(img12)) {
			img12=QianNiuUpload.upload(img12);
			record.set("img12",img12);
		}
		
		String img13 = getPara("img13");
		if (StrKit.notBlank(img13)) {
			img13=QianNiuUpload.upload(img13);
			record.set("img13",img13);
		}
		
		String img14 = getPara("img14");
		if (StrKit.notBlank(img14)) {
			img14=QianNiuUpload.upload(img14);
			record.set("img14",img14);
		}
		
		String img15 = getPara("img15");
		if (StrKit.notBlank(img15)) {
			img15=QianNiuUpload.upload(img15);
			record.set("img15",img15);
		}
		
		record.set("activeTitle",activeTitle);
		record.set("activeDate",activeDate);
		
	
		result = Db.save("f_turn_img", record);
		
		renderJson("result", result);
	}
	/**
	 * 编辑更新翻牌图片信息
	 * @throws IOException 
	 */
	public void EditTurnImg() throws IOException{
		boolean result = false;
		int id=getParaToInt("id");
		String activeTitle = getPara("activeTitle");
		String activeDate = getPara("activeDate");
		Record record = new Record();
		String img01 = getPara("img01");
		if (StrKit.notBlank(img01)) {
			img01=QianNiuUpload.upload(img01);
			record.set("img01",img01);
		}
		String img02 = getPara("img02");
		if (StrKit.notBlank(img02)) {
			img02=QianNiuUpload.upload(img02);
			record.set("img02",img02);
		}
		String img03 = getPara("img03");
		if (StrKit.notBlank(img03)) {
			img03=QianNiuUpload.upload(img03);
			record.set("img03",img03);
		}
		String img04 = getPara("img04");
		if (StrKit.notBlank(img04)) {
			img04=QianNiuUpload.upload(img04);
			record.set("img04",img04);
		}
		String img05 = getPara("img05");
		if (StrKit.notBlank(img05)) {
			img05=QianNiuUpload.upload(img05);
			record.set("img05",img05);
		}
		String img06 = getPara("img06");
		if (StrKit.notBlank(img06)) {
			img06=QianNiuUpload.upload(img06);
			record.set("img06",img06);
		}
		String img07 = getPara("img07");
		if (StrKit.notBlank(img07)) {
			img07=QianNiuUpload.upload(img07);
			record.set("img07",img07);
		}
		
		String img08 = getPara("img08");
		if (StrKit.notBlank(img08)) {
			img08=QianNiuUpload.upload(img08);
			record.set("img08",img08);
		}
		
		String img09 = getPara("img09");
		if (StrKit.notBlank(img09)) {
			img09=QianNiuUpload.upload(img09);
			record.set("img09",img09);
		}
		
		String img10 = getPara("img10");
		if (StrKit.notBlank(img10)) {
			img10=QianNiuUpload.upload(img10);
			record.set("img10",img10);
		}
		
		String img11 = getPara("img11");
		if (StrKit.notBlank(img11)) {
			img11=QianNiuUpload.upload(img11);
			record.set("img11",img11);
		}
		
		String img12 = getPara("img12");
		if (StrKit.notBlank(img12)) {
			img12=QianNiuUpload.upload(img12);
			record.set("img12",img12);
		}
		
		String img13 = getPara("img13");
		if (StrKit.notBlank(img13)) {
			img13=QianNiuUpload.upload(img13);
			record.set("img13",img13);
		}
		
		String img14 = getPara("img14");
		if (StrKit.notBlank(img14)) {
			img14=QianNiuUpload.upload(img14);
			record.set("img14",img14);
		}
		
		
		String img15 = getPara("img15");
		if (StrKit.notBlank(img15)) {
			img15=QianNiuUpload.upload(img15);
			record.set("img15",img15);
		}
		
        record.set("id", id);		
		record.set("activeTitle",activeTitle);
		record.set("activeDate",activeDate);

		
		result = Db.update("f_turn_img", record);
		
		renderJson("result", result);
	}
	/****************************************新年小目标*********************************************************/
	/**
	 * 小目标
	 */
	public void target(){
		Integer isClose=getParaToInt("isClose")==null?10:getParaToInt("isClose");
		String aid = getPara("aid");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=LuckDrawDao.getTarget(isClose,aid, pageno, 16);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("list",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("isClose", isClose);
		setAttr("aid", aid);
		render("target_list.html");
	}
	/***
	 * 见证小目标
	 */
	public void targetdetail() {
		Integer tgid = getParaToInt("id");
		List<Record> list = Db.find("select *from f_target_witness where tgid = ?",tgid);
		setAttr("list", list);
		render("target_list_detail.html");
	}
}
