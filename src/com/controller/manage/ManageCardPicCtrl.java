package com.controller.manage;

import java.io.IOException;
import java.util.List;

import com.dao.CardPicDao;
import com.dao.LuckDrawDao;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.util.UploadImageUtil;
/**
 * 卡片封面管理
 * @author SHICHUNXIANG
 *
 */
public class ManageCardPicCtrl extends Controller {

	/**
	 * 祝福卡
	 */
	public void wishcardList(){
		Integer state=getParaToInt("state")==null?10:getParaToInt("state");
		Integer type=getParaToInt("type")==null?99:getParaToInt("type");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=CardPicDao.GetWishCardList(type,state, pageno, 16);
		List<Record>typelist = Db.find("select code_name,code_value from f_dictionary where code_key = 'wishcardType' and state=1");
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("cardList",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("state", state);//状态
		setAttr("type", type);//选择的卡面类型
		setAttr("typelist", typelist);//所有卡面类型
		render("wishcardList.html");
	}
	
	/**
	 * 新增祝福卡片
	 * @throws IOException 
	 */
	public void SaveWishCard() throws IOException{
		boolean result = false;
		int typeId=getParaToInt("typeId");//分类ID号
		String name=getPara("name");//卡片名称
		String imgurl01=getPara("imgurl01");//闭合图片
		String imgurl02=getPara("imgurl02");//展开图片
		int imgOrderId=getParaToInt("imgOrderId");//排序序号
		int state=getParaToInt("state");//状态
		Record record = new Record();
		if (StrKit.notBlank(imgurl01)) {
			imgurl01 = UploadImageUtil.upLoadBase(imgurl01);
			record.set("imgurl01",imgurl01);
		}
		if (StrKit.notBlank(imgurl02)) {
			imgurl02 = UploadImageUtil.upLoadBase(imgurl02);
			record.set("imgurl02", imgurl02 );
		}
		record.set("typeId",typeId);
		record.set("name",name);
		record.set("imgOrderId", imgOrderId );
		record.set("state", state);
		result = Db.save("f_wishcardpic", record);
		
		renderJson("result", result);
	}

	
	/**
	 * 编辑祝福卡片
	 * @throws IOException 
	 */
	public void EditWishCard() throws IOException{
		boolean result = false;
		int id=getParaToInt("id");//卡片ID号
		int typeId=getParaToInt("typeId");//分类ID号
		String name=getPara("name");//卡片名称
		String imgurl01=getPara("imgurl01");//闭合图片
		String imgurl02=getPara("imgurl02");//展开图片
		int imgOrderId=getParaToInt("imgOrderId");//排序序号
		int state=getParaToInt("state");//状态
				
		Record record = new Record();
		if (StrKit.notBlank(imgurl01)) {
			imgurl01 = UploadImageUtil.upLoadBase(imgurl01);
			record.set("imgurl01",imgurl01);
		}
		if (StrKit.notBlank(imgurl02)) {
			imgurl02 = UploadImageUtil.upLoadBase(imgurl02);
			record.set("imgurl02",imgurl02);
		}
		record.set("id", id);
		record.set("typeId",typeId);
		record.set("name",name);
		record.set("imgOrderId", imgOrderId );
		record.set("state", state);
		
		result = Db.update("f_wishcardpic", record);
		
		renderJson("result", result);
	}
	
	/**
	 * 祝福卡的详细信息
	 */
	public void wishcardDetail(){
		Integer id=getParaToInt(0);//奖品唯一编号
		if(id!=null){
			Record rd=CardPicDao.QueryWishCardInfo(id);
			setAttr("rd",rd);
		}
		List<Record> dicList=CardPicDao.GetWishCardType();
		setAttr("dicList",dicList);
		render("wishcard_detail.html");
	}
	
	
	
	/**
	 * 红包卡面列表
	 */
	public void redpacketsPicList(){
		Integer state=getParaToInt("state")==null?10:getParaToInt("state");
		Integer type=getParaToInt("type")==null?99:getParaToInt("type");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=CardPicDao.GetRedpacketsPicList(type,state, pageno, 16);
		List<Record>typelist = Db.find("select code_name,code_value from f_dictionary where code_key = 'redpacketsPicType' and state=1");
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("cardList",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("state", state);//状态
		setAttr("type", type);//选择的卡面类型
		setAttr("typelist", typelist);//所有卡面类型
		render("redpacketsPicList.html");
	}
	
	/**
	 * 新增红包卡面
	 * @throws IOException 
	 */
	public void SaveRedpacketsPic() throws IOException{
		boolean result = false;
		int typeId=getParaToInt("typeId");//分类ID号
		String name=getPara("name");//卡片名称
		String imgurl01=getPara("imgurl01");//闭合图片
		String imgurl02=getPara("imgurl02");//展开图片
		int state=getParaToInt("state");//状态
		int imgOrderId=getParaToInt("imgOrderId");//显示顺序
		Record record = new Record();
		if (StrKit.notBlank(imgurl01)) {
			imgurl01 = UploadImageUtil.upLoadBase(imgurl01);
			record.set("imgurl01",imgurl01);

		}
		if (StrKit.notBlank(imgurl02)) {
			imgurl02 = UploadImageUtil.upLoadBase(imgurl02);
			record.set("imgurl02", imgurl02 );
		}
		record.set("typeId",typeId);
		record.set("name",name);
		record.set("state", state);
		record.set("imgOrderId", imgOrderId);
		result = Db.save("f_redpackets_pic", record);
		
		renderJson("result", result);
	}

	
	/**
	 * 编辑红包卡面
	 * @throws IOException 
	 */
	public void EditRedpacketsPic() throws IOException{
		boolean result = false;
		int id=getParaToInt("id");//卡片ID号
		int typeId=getParaToInt("typeId");//分类ID号
		String name=getPara("name");//卡片名称
		String imgurl01=getPara("imgurl01");//闭合图片
		String imgurl02=getPara("imgurl02");//展开图片
		int state=getParaToInt("state");//状态
		int imgOrderId=getParaToInt("imgOrderId");//显示顺序
				
		Record record = new Record();
		if (StrKit.notBlank(imgurl01)) {
			imgurl01 = UploadImageUtil.upLoadBase(imgurl01);
			record.set("imgurl01",imgurl01);
		}
		if (StrKit.notBlank(imgurl02)) {
			imgurl02 = UploadImageUtil.upLoadBase(imgurl02);
			record.set("imgurl02",imgurl02);
		}
		
		
		record.set("id", id);
		record.set("typeId",typeId);
		record.set("name",name);
		record.set("state", state);
		record.set("imgOrderId", imgOrderId);
		
		result = Db.update("f_redpackets_pic", record);
		
		renderJson("result", result);
	}
	
	/**
	 * 红包卡面的详细信息
	 */
	public void redpacketsPicDetail(){
		Integer id=getParaToInt(0);//奖品唯一编号
		if(id!=null){
			Record rd=CardPicDao.QueryRedpacketsPicInfo(id);
			setAttr("rd",rd);
		}
		List<Record> dicList=CardPicDao.GetRedpacketsPicType();
		setAttr("dicList",dicList);
		render("redpacketsPic_detail.html");
	}
}
