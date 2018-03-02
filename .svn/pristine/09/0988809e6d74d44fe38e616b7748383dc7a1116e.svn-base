package com.controller.manage;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.List;

import com.config.WeixinConfig;
import com.controller.WeixinApiCtrl;
import com.dao.RedPacketsDao;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.TemplateData;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;
import com.util.Constant;
import com.util.UploadImageUtil;


public class ManageRedPacketsCtrl extends Controller{

	/**
	 * 查询商品
	 */
	public void QueryProudct(){
		Integer state=getParaToInt("state")==null?10:getParaToInt("state");//状态
		Integer userType=getParaToInt("userType")==null?10:getParaToInt("userType");//适用的用户
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=RedPacketsDao.QueryProudct(state, pageno, 16,userType);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("list",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("state", state);//状态
		setAttr("userType",userType);
		render("query_product.html");
	}
	
	/**
	 * 管理商品
	 */
	public void MangeProudct(){
		Integer id = getParaToInt(0);
		List<Record> flowerproductlist=RedPacketsDao.FlowerProductList2();
		setAttr("flowerproductlist", flowerproductlist);
		if(id!=null){
			Record rd=RedPacketsDao.QueryProduct(id);
			setAttr("rd", rd);
		}
		render("mange_product.html");
	}
	
	/**
	 * 编辑更新商品信息
	 * @throws IOException 
	 */
	public void EditProduct() throws IOException{
		boolean result = false;
		int id=getParaToInt("id");//红包商品ID号
		String name=getPara("name");//红包商品简称
		int fpid=getParaToInt("fpid");//商品编号
		int pnum=getParaToInt("pnum");//数量
		double pmoney=Double.parseDouble(getPara("pmoney"));//单价
		
		int state=getParaToInt("state");//状态
		int protype=getParaToInt("protype");//类型
		String img01 = getPara("img01");//图片一
		String img02 = getPara("img02");//图片二
		String img03 = getPara("img03");//图片二
		
		Record record = new Record();

		if (StrKit.notBlank(img01)) {
			String imgurl01 = UploadImageUtil.upLoadBase(img01);
			record.set("imgurl01",imgurl01);
		}
		if (StrKit.notBlank(img02)) {
			String imgurl02 = UploadImageUtil.upLoadBase(img02);
			record.set("imgurl02",imgurl02);
		}
		if (StrKit.notBlank(img03)) {
			String imgurl03 = UploadImageUtil.upLoadBase(img03);
			record.set("imgurl03",imgurl03);
		}
		int orderId=getParaToInt("orderId");//排序序号
		int userType=getParaToInt("userType");//适用的用户
		
		
		record.set("id", id);
		record.set("name", name);
		record.set("fpid",fpid);
		record.set("pnum",pnum);
		record.set("pmoney", pmoney );
		record.set("state", state);
		record.set("protype", protype);
		
		record.set("orderId", orderId);
		record.set("userType", userType);
		
		
		
		result = Db.update("f_redpackets_pro", record);
		
		renderJson("result", result);
	}
	/**
	 * 新增商品
	 * @throws IOException 
	 */
	public void SaveProduct() throws IOException{
		boolean result = false;
		String name=getPara("name");//红包商品简称
		int fpid=getParaToInt("fpid");//商品编号
		int pnum=getParaToInt("pnum");//数量
		double pmoney=Double.parseDouble(getPara("pmoney"));//单价
		
		int state=getParaToInt("state");//状态
		int protype=getParaToInt("protype");//类型
		
		String img01 = getPara("img01");//图片一
		String img02 = getPara("img02");//图片二
		String img03 = getPara("img03");//图片二
		
		int orderId=getParaToInt("orderId");//排序序号
		int userType=getParaToInt("userType");//适用的用户
		
		Record record = new Record();

		if (StrKit.notBlank(img01)) {
			String imgurl01 = UploadImageUtil.upLoadBase(img01);
			record.set("imgurl01",imgurl01);
		}
		if (StrKit.notBlank(img02)) {
			String imgurl02 = UploadImageUtil.upLoadBase(img02);
			record.set("imgurl02",imgurl02);
		}
		if (StrKit.notBlank(img03)) {
			String imgurl03 = UploadImageUtil.upLoadBase(img03);
			record.set("imgurl03",imgurl03);
		}
		record.set("name", name);
		record.set("fpid",fpid);
		record.set("pnum",pnum);
		record.set("pmoney", pmoney );
		record.set("state", state);
		record.set("protype", protype);
		
		record.set("orderId", orderId);
		record.set("userType", userType);
		
		result = Db.save("f_redpackets_pro", record);
		
		renderJson("result", result);
	}

	/**
	 * 查询发包列表
	 */
	public void QueryRedPackets(){
		Integer state=getParaToInt("state")==null?10:getParaToInt("state");
		Integer type=getParaToInt("type")==null?10:getParaToInt("type");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		String nick = getPara("nick");		
		String aid = getPara("aid");
		String stime_start = getPara("stime_start");
		String stime_end = getPara("stime_end");
		Page<Record> page=RedPacketsDao.QueryRedPackets(state,type, pageno, 16,aid,nick,stime_start,stime_end);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("list",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("state", state);//状态
		setAttr("aid",aid);
		setAttr("nick",nick);
		setAttr("type",type);
		setAttr("stime_start",stime_start);
		setAttr("stime_end",stime_end);
		render("query_redpackets.html");
	}
	
	/**
	 * 查看发包详情
	 */
	public void QueryDetial(){
		Integer id=getParaToInt(0);//红包唯一编号
		List<Record> list=RedPacketsDao.QueryDetial(id);
		setAttr("list",list);//结果列表
		render("querydetial.html");
	}
	/**
	 * 立即退款
	 */
	public void QueryDetial2(){
		Integer id=getParaToInt(0);//红包唯一编号
		List<Record> list=RedPacketsDao.QueryDetial2(id);
		setAttr("list",list);//结果列表
		render("querydetial2.html");
	}
	
	
	/**
	 * 查看拆包列表
	 */
	public void QueryGetRedPackets(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Integer isopen=getParaToInt("isopen")==null?10:getParaToInt("isopen");
		Integer isduihuan=getParaToInt("isduihuan")==null?10:getParaToInt("isduihuan");//是否已兑换
		String caid=getPara("caid");//拆包人ID
		String rpid=getPara("rpid");//发包编号
		String faid=getPara("faid");//发包人ID
		String cnick=getPara("cnick");//拆包人昵称
		if(cnick!=null){
			try {
				cnick = URLDecoder.decode(cnick,"utf-8");
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		Page<Record> page=RedPacketsDao.QueryGetRedPackets(isopen,isduihuan,cnick,caid,faid,rpid, pageno, 16);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("list",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("isopen", isopen);//状态,是否已拆
		setAttr("isduihuan",isduihuan);
		setAttr("caid",caid);
		setAttr("cnick",cnick);
		setAttr("faid",faid);
		setAttr("rpid",rpid);
		render("query_getredpackets.html");
	}
	
	/**
	 * 退款处理
	 */
	public void BackMoney(){
		Integer state=getParaToInt("state")==null?0:getParaToInt("state");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");//当前显示页
		Page<Record> page=RedPacketsDao.BackMoney(state,pageno, 16);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("list",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("state", state);//状态
		render("backmoney.html");
	}
	
	/**
	 * 退款提交
	 */		
	public void BackMoney2(){
		Record admin = getSessionAttr("admin");
		Object opusr = admin.get("username");
		String opuser = opusr.toString();
		boolean result = false;
		final Record record = new Record();
		record.set("opuser", opuser);
		record.set("tmoney", getPara("tmoney"));
		record.set("id", getPara("id"));
		record.set("ttime", getPara("ttime"));
		record.set("state", 3);
		result = Db.tx(new IAtom() {
			boolean save_flag =false;
			@Override
			public boolean run() throws SQLException {
				boolean r1=Db.update("f_redpackets", record );
				int r2=Db.update("update f_redpackets_detial set isopen=2 where rpid = ? and isopen=0 ",getPara("id"));
			    if (r1==true&&r2>0) {
			    	save_flag=true;
				}
			    return save_flag;
			}		
		}); 
		//发送模板消息
		//发退款通知
		String openid=Db.queryStr("select b.openid from f_redpackets as a "
				+ " left join f_account as b on a.aid=b.id where a.id=?",getPara("id"));
		String url=Constant.getHost+"/service/seachRedpacketDetails/"+getPara("id");
		WeixinApiCtrl.setApiConfig();
        ApiResult ar = TemplateMsgApi.send(TemplateData.New()
			    // 消息接收者
			    .setTouser(openid)
			    // 模板id
			    .setTemplate_id(WeixinApiCtrl.gettemplateId("退款通知"))
			    .setTopcolor("#eb414a")
			    .setUrl(url)
			    .add("first", "您的红包"+getPara("id")+"已经发起退款申请", "#333")
			    .add("reason", "红包未拆完", "#333")
			    .add("refund", getPara("tmoney"), "#333")
			    .add("remark", "请留意微信退款信息。只退未拆包的金额，点击【详情】可查看具体拆包情况。", "#888")
			    .build());
		
		renderJson("result", result);	
	}	
}
