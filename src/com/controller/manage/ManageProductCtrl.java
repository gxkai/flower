package com.controller.manage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dao.FCDao;
import com.dao.MCDao;
import com.interceptor.ManageInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.bcel.internal.generic.Select;
import com.util.Constant;
import com.util.UploadImageUtil;

/**
 * @Desc 商品管理
 * @author lxx
 * @date 2016/8/18
 * */
@Before(ManageInterceptor.class)
public class ManageProductCtrl extends Controller {
	/*********************花材分类*********************/
	// 列表
	public void flower_type(){
		Integer pageno = getParaToInt()==null?1:getParaToInt();
		Page<Record> page = MCDao.getFlowerType(pageno, 16);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("typelist", page.getList());
		render("flower_type.html");
	}
	//花材分类查询(仅仅分类查询)
	public void flower_searchType(){
		try {
			String ftype = getPara(0);
			ftype = URLDecoder.decode(ftype==null?"":ftype, "utf-8");
			Page<Record> page = MCDao.getFlowerftype(1, 16, ftype);
			List<Record> fls = page.getList();
			setAttr("ftype", ftype);
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("typelist", fls);			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		render("flower_type.html");
	}
	
	// 保存数据
	public void saveFlower_type(){
		boolean result = false;
		Record record = new Record();
		record.set("name", getPara("name"));
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("ctime", new Date());
			result = Db.save("f_flower_type", record);
		}else{
			record.set("id", id);
			result = Db.update("f_flower_type", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getFlower_type(){
		Integer id = getParaToInt(0);
		if(id != null){
			setAttr("type", Db.findById("f_flower_type", id));
		}
		render("flower_type_detail.html");
	}
	// 设置忌讳的花材分类
	public void set_type_jh(){
		Integer id = getParaToInt("id");
		Integer jh = getParaToInt("jh");
		Db.update("update f_flower_type set jh = ? where id =?", jh, id);
	}
	
	/*********************花材管理*********************/
	// 列表
	public void flower(){
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		String ftype = getPara(1);
		String fname = getPara(2);
		try {
			ftype = URLDecoder.decode(ftype==null?"":ftype, "utf-8");
			fname = URLDecoder.decode(fname==null?"":fname, "utf-8");
			Page<Record> page = MCDao.getFlower(pageno, 16, ftype, fname);
			List<Record> fls = page.getList();
			for (Record fl : fls) {
				fl.set("cname", Db.queryStr("select name from f_color where id ="+fl.getInt("cid")));
			}
			setAttr("ftype", ftype);
			setAttr("fname", fname);
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("flowerlist", fls);
			render("flower.html");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// 保存数据
	public void saveFlower() throws IOException{
		boolean result = false;
		Record record = new Record();
		record.set("name", getPara("name"));
		record.set("tid", getParaToInt("tid"));
		record.set("cid", getParaToInt("cid"));
		record.set("loss", Double.parseDouble(getPara("loss")));
		record.set("dozen", getParaToInt("dozen"));
		record.set("ftype", getParaToInt("ftype"));
		String basestr = getPara("basestr");
		if(basestr != null && !"".equals(basestr)){
			String imgurl = UploadImageUtil.upLoadBase(basestr);
			record.set("imgurl", imgurl);
		}
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("ctime", new Date());
			result = Db.save("f_flower", record);
		}else{
			record.set("id", id);
			result = Db.update("f_flower", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getFlower(){
		setAttr("typelist", Db.find("select id,name from f_flower_type"));
		setAttr("colors", Db.find("select id,name from f_color"));
		Integer id = getParaToInt(0);
		if(id != null){
			setAttr("flower", Db.findById("f_flower", id));
		}
		render("flower_detail.html");
	}
	// 上架与下架
	public void upordownFlower(){
		int id = getParaToInt(0);
		int state = getParaToInt(1);
		renderJson(MCDao.upordownFlower(id, state));
	}
	// 忌讳的花设置
	public void set_jh_flower(){
		int id = getParaToInt("id");
		int jh = getParaToInt("jh");
		MCDao.setJhFlower(id, jh);
		renderJson();
	}
	
	/*********************商品管理*********************/
	// 列表
	public void flowerpro(){
		
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		String ptid=getPara("ptid")==null?"0":getPara("ptid");
		Integer state=getParaToInt("state")==null?0:getParaToInt("state");
		Page<Record> page = MCDao.getFlowerPro(pageno, 16,ptid,state);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("flowerprolist", page.getList());
		List<Record> typeList=MCDao.get_flower_pro_type();
		setAttr("typelist",typeList);
		setAttr("ptid",ptid);
		setAttr("state",state);
		render("flower_pro.html");
	}
	// 保存数据
	public void saveFlower_pro() throws IOException{
		boolean result = false;
		boolean XYrepeat = false;
		if (getParaToInt("id")==null) {
			if (getParaToInt("x")!=0 && getParaToInt("y")!=0 ) {
				Number num = Db.queryNumber("select count(1) from f_flower_pro where state = 0  and x = ? and y = ? ",getParaToInt("x"),getParaToInt("y"));
				if (num.intValue()>0) {
					XYrepeat = true;
					renderJson("result", result);
					renderJson("XYrepeat",XYrepeat );return;
				}
			}
		}else {
			if (getParaToInt("x")!=0 && getParaToInt("y")!=0 ) {
				Number num = Db.queryNumber("select count(1) from f_flower_pro where state = 0  and x = ? and y = ? and id!= ? ",getParaToInt("x"),getParaToInt("y"),getParaToInt("id"));
				if (num.intValue()>0) {
					XYrepeat = true;
					renderJson("result", result);
					renderJson("XYrepeat",XYrepeat);return;
				}
			}
		}
		Record record = new Record();
		Integer ptid = getParaToInt("ptid");
		record.set("ptid", ptid);
		if(ptid.intValue() > 3){
			record.set("type", getParaToInt("protype"));
		}else{
			record.set("type", 0);
		}
		record.set("name", getPara("name"));
		String basestr1 = getPara("basestr1");
		String basestr1_1 = getPara("basestr1_1");
		String basestr1_2 = getPara("basestr1_2");
		String basestr1_3 = getPara("basestr1_3");
		//双品、多品、多双交替、玫瑰套餐有banner轮播图
		if(ptid > 2 && ptid!=203&& ptid!=11){
			if(basestr1 != null && !"".equals(basestr1)){
				String imgurl = UploadImageUtil.upLoadBase(basestr1);
				record.set("imgurl", imgurl);
			}
		}else{
			Integer id = getParaToInt("id");
			if(id != null){
				String img = Db.queryStr("SELECT imgurl FROM f_flower_pro WHERE id =?", id);
				if(img.indexOf("-")!=-1){
					String[] im = img.split("-");
					String img_1 = im[0];
					String img_2 = im[1];
					String img_3 = im[2];
					
					if(basestr1_1 != null && !"".equals(basestr1_1)){
						String imgurl_1 = UploadImageUtil.upLoadBase(basestr1_1);
						img_1 = imgurl_1;
					}
					if(basestr1_2 != null && !"".equals(basestr1_2)){
						String imgurl_2 = UploadImageUtil.upLoadBase(basestr1_2);
						img_2 = imgurl_2;
					}
					if(basestr1_3 != null && !"".equals(basestr1_3)){
						String imgurl_3 = UploadImageUtil.upLoadBase(basestr1_3);
						img_3 = imgurl_3;
					}
					record.set("imgurl", img_1 +"-"+ img_2 +"-"+ img_3);
				}else{
					String img_2 = "";
					String img_3 = "";
					if(basestr1_2 != null && !"".equals(basestr1_2)){
						String imgurl_2 = UploadImageUtil.upLoadBase(basestr1_2);
						img_2 = imgurl_2;
					}else{
						img_2 = img;
					}
					if(basestr1_3 != null && !"".equals(basestr1_3)){
						String imgurl_3 = UploadImageUtil.upLoadBase(basestr1_3);
						img_3 = imgurl_3;
					}else{
						img_3 = img;
					}
					record.set("imgurl", img +"-"+ img_2 +"-"+ img_3);
				}
			}else{
				String im = "";
				if(basestr1_1 != null && !"".equals(basestr1_1)){
					String imgurl_1 = UploadImageUtil.upLoadBase(basestr1_1);
					im = imgurl_1;
				}
				if(basestr1_2 != null && !"".equals(basestr1_2)){
					String imgurl_2 = UploadImageUtil.upLoadBase(basestr1_2);
					im += ("-"+imgurl_2);
				}
				if(basestr1_3 != null && !"".equals(basestr1_3)){
					String imgurl_3 = UploadImageUtil.upLoadBase(basestr1_3);
					im += ("-"+imgurl_3);
				}
				record.set("imgurl", im);
			}
		}
		
		String basestr2_1 = getPara("basestr2_1");	// 产品描述
		if(basestr2_1 != null && !"".equals(basestr2_1)){
			String itinfo = UploadImageUtil.upLoadBase(basestr2_1);
			record.set("itinfo1", itinfo);
		}
		String basestr2_2 = getPara("basestr2_2");	// 发货详情
		if(basestr2_2 != null && !"".equals(basestr2_2)){
			String itinfo = UploadImageUtil.upLoadBase(basestr2_2);
			record.set("itinfo2", itinfo);
		}
		String basestr2_3 = getPara("basestr2_3");	// 购花须知
		if(basestr2_3 != null && !"".equals(basestr2_3)){
			String itinfo = UploadImageUtil.upLoadBase(basestr2_3);
			record.set("itinfo3", itinfo);
		}
		String basestr2_4 = getPara("basestr2_4");	// 品质保障
		if(basestr2_4 != null && !"".equals(basestr2_4)){
			String itinfo = UploadImageUtil.upLoadBase(basestr2_4);
			record.set("itinfo4", itinfo);
		}
		String basestr2_5 = getPara("basestr2_5");	// 养护建议
		if(basestr2_5 != null && !"".equals(basestr2_5)){
			String itinfo = UploadImageUtil.upLoadBase(basestr2_5);
			record.set("itinfo5", itinfo);
		}
		
		record.set("price", getPara("price"));
		record.set("describe", getPara("describe"));
		record.set("priority", getPara("priority"));
		record.set("x", getPara("x"));
		record.set("y", getPara("y"));
		record.set("describe2", getPara("describe2"));
		record.set("state", getParaToInt("state"));
		record.set("seeds", getParaToInt("seeds"));
		//拼团的参数
		record.set("ptCount", getParaToInt("ptCount"));
		record.set("ptHours", getParaToInt("ptHours"));
		record.set("ptPrice", getPara("ptPrice"));
		//是否计入首单，是否允许顺延
		record.set("isinfirst", getParaToInt("isinfirst"));
		record.set("allowSY", getParaToInt("allowSY"));
		record.set("fpc", getParaToInt("fpc"));
		record.set("shareTitle", getPara("shareTitle"));
		record.set("shareDes", getPara("shareDes"));
		record.set("dmlj", getPara("dmlj"));
		record.set("reachtype", getPara("reachtype"));
		record.set("allownew", getPara("allownew"));
		record.set("isptFree", getPara("isptFree"));//团长是否免单
		record.set("allowRefund", getPara("allowRefund"));//是否支持退款
		record.set("allowRobotis", getPara("allowRobotis"));//是否支持机器人
		record.set("fundper", getPara("fundper"));//公益基金百分比
		record.set("ptNum", getPara("ptNum"));//拼团体验次数
		record.set("jsMoney", getPara("jsMoney"));//入团介绍人佣金
		record.set("isAlonePage", getPara("isAlonePage"));//是否独立页面
		
		
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("ctime", new Date());
			result = Db.save("f_flower_pro", record);
		}else{
			record.set("id", id);
			result = Db.update("f_flower_pro", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getFlower_pro(){
		setAttr("typelist", Db.find("select code_name,code_value from f_dictionary where code_key=?", "ptid"));
		Integer id = getParaToInt();
		if(id != null){
			Record flopro = Db.findById("f_flower_pro", id);
			//待成团数量
			Number number = Db.queryNumber("select count(1) from f_pingtuan where state = 2 and fptid =?",id);
			setAttr("number", number);
			String imgurl = flopro.getStr("imgurl");
			if(imgurl!=null&&imgurl.indexOf("-")!=-1){
				String[] img = imgurl.split("-");
				flopro.set("imgurl1", img[0]);
				flopro.set("imgurl2", img[1]);
				flopro.set("imgurl3", img[2]);
			}else{
				flopro.set("imgurl1", imgurl);
			}
			setAttr("flowerpro", flopro);
		}
		setAttr("hid", Constant.Hid);
		render("flower_pro_detail.html");
	}
	// 删除
	public void delFlower_pro(){
		int id = getParaToInt();
		Record record = new Record();
		record.set("id", id);
		record.set("state", 1);
		renderJson(Db.update("f_flower_pro", record));
	}
	
	/*********************产品设置*********************/
	/**
	 * 产品列表
	 * type 1:双品系列2:多品系列3:送花系列4:节日送花5:定制花束
	 */
	public void product(){
		int type = getParaToInt("type");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		String code = getPara("code");
 		String minprice = getPara("minprice");
 		String maxprice = getPara("maxprice");
		Page<Record> page = MCDao.getProduct(type, pageno, 16, code, minprice, maxprice);
		List<Record> pros = page.getList();
		if(type != 3){
			for (Record pro : pros) {
				String cname = Db.queryStr("select name from f_color where id =?", pro.getInt("cid"));
				pro.set("cname", cname==null?"无":cname);
			}
		}
		setAttr("batchcode", code);
		setAttr("minprice", minprice);
		setAttr("maxprice", maxprice);
		setAttr("type", type);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("productlist", pros);
		render("product.html");
	}
	
	
	
	
	// 产品详情
	public void getProduct() throws UnsupportedEncodingException{
		int typeid = getParaToInt(0);
		String flotype = getPara(2);
		if(flotype != null){
			flotype = URLDecoder.decode(flotype, "utf-8");
		}
		List<Record> proinfolist = new ArrayList<>();
		// typeid为0修改数据，否则添加数据
		if(typeid == 0){
			int id = getParaToInt(1);
			// 需要修改的商品属性
			Record product = Db.findById("f_product", id);
			setAttr("typeid", product.getInt("type"));
			typeid = product.getInt("type");
			setAttr("id", id);
			setAttr("typeid", product.getInt("type"));
			setAttr("fname", product.getStr("fname"));
			setAttr("sname", product.getStr("sname"));
			setAttr("app",product.getInt("app"));
			setAttr("style",product.getInt("style"));
			setAttr("priority",product.getInt("priority"));
			setAttr("cid", product.getInt("cid"));
			setAttr("fpid", product.getInt("fpid"));
			setAttr("iid", product.getInt("iid"));
			setAttr("appoint",product.getInt("appoint"));
			// 商品用的花材
			proinfolist = Db.find("select fid,num,ftype from f_product_info where pid=?", id);
		}else{
			setAttr("app",1);
			setAttr("style",1);
			setAttr("priority",0);
			setAttr("appoint",0);
		}
		setAttr("typeid", typeid);
		
		if(typeid == 3){
			// 送花系列商品类型
			setAttr("ptlist", Db.find("select id,name from f_flower_pro where ptid = 3"));
			// 适赠对象
			setAttr("idoneitylist", Db.find("select id,title from f_idoneity where state = 0"));
		}else if(typeid == 4){//节日送花
			setAttr("ptlist",Db.find("select a.id,CONCAT(a.name,'(',b.code_name,')') 'name' from f_flower_pro a  left join f_dictionary b on a.ptid= b.code_value where b.state=1 and ptid>100 and ptid<=200 and  ptid not in(104,105)"));
		}else if(typeid==5){//定制花束
			setAttr("ptlist",Db.find("select a.id,CONCAT(a.name,'(',b.code_name,')') 'name' from f_flower_pro a  left join f_dictionary b on a.ptid= b.code_value where b.state=1 and  ptid>200 and ptid<=300 "));
		}else if(typeid==6){//闪购花束
			setAttr("ptlist",Db.find("select a.id,CONCAT(a.name,'(',b.code_name,')') 'name' from f_flower_pro a  left join f_dictionary b on a.ptid= b.code_value where b.state=1 and  ptid=104"));
		}else if(typeid==7){//主题花束
			setAttr("ptlist",Db.find("select a.id,CONCAT(a.name,'(',b.code_name,')') 'name' from f_flower_pro a  left join f_dictionary b on a.ptid= b.code_value where b.state=1 and  ptid=105"));
		}else if(typeid==8){//拼团花束
			setAttr("ptlist",Db.find("select a.id,CONCAT(a.name,'(',b.code_name,')') 'name' from f_flower_pro a  left join f_dictionary b on a.ptid= b.code_value where b.state=1 and  ptid=202"));
		}else if(typeid==9){//玫瑰套餐花束
			setAttr("ptlist",Db.find("select a.id,CONCAT(a.name,'(',b.code_name,')') 'name' from f_flower_pro a  left join f_dictionary b on a.ptid= b.code_value where b.state=1 and  ptid=11 and b.code_key='ptid'"));
		}
		
		// 花材
		String sql = "select id,name,imgurl from f_flower_view where state=?";
		if(flotype!=null && !"".equals(flotype)){
			sql += " and name like '%"+flotype+"%'";
		}
		List<Record> flowerlist = Db.find(sql, 1);
		for(Record fl : flowerlist){
			for(Record pi : proinfolist){
				if(fl.getInt("id").equals(pi.getInt("fid"))){
					fl.set("num", pi.getInt("num"));
					fl.set("ftype", pi.getInt("ftype"));
					break;
				}
			}
		}
		
		// 全量花材
		String all_sql = "select id,name,imgurl from f_flower_view where state=?";
		List<Record> floalllist = Db.find(all_sql, 1);
		for(Record fl : floalllist){
			for(Record pi : proinfolist){
				if(fl.getInt("id").equals(pi.getInt("fid"))){
					fl.set("num", pi.getInt("num"));
					fl.set("ftype", pi.getInt("ftype"));
					break;
				}
			}
		}
		setAttr("flowerlist", flowerlist);
		setAttr("floalllist", floalllist);
		setAttr("flotype", flotype);
		List<Record> clist = Db.find("select id,name,jh from f_color");
		setAttr("clist", clist);
		render("product_detail.html");
	}
	
	// 保存商品信息
	public void saveProduct(){
		Integer id = getParaToInt("id");		// 产品ID
		int typeid = getParaToInt("typeid");	// 商品类型ID
		String flist = getPara("flist");		// 花材列表
		String fname = getPara("fname");		// 商品名称
		String sname = getPara("sname");		// 商品简称
		Integer iid = getParaToInt("iid");		// 适赠对象ID
		Integer fpid = getParaToInt("fpid");	// 产品分类ID
		Integer app=getParaToInt("app");//用途
		Integer style=getParaToInt("style");//格调
		Integer priority=getParaToInt("priority")==null?0:getParaToInt("priority");//配花优先级（定制花束适用）
		Integer appoint=getParaToInt("appoint")==null?0:getParaToInt("appoint");//指定订单(多品花束适用，满足条件的订单分配此花)
		if(id == null){
			// 添加产品
			renderJson(MCDao.saveProduct(typeid, flist, fname, sname, iid, fpid,app,style,priority,appoint));
		}else{
			// 修改产品
			renderJson(MCDao.editProduct(id, typeid, flist, fname, sname, iid, fpid,app,style,priority,appoint));
		}
	}
	// 删除商品
	public void delProduct(){
		int id = getParaToInt();
		renderJson("result", MCDao.delProduct(id));
	}
	// 复制粘贴产品
	public void copypaste(){
		String copycode = getPara("copycode");
		Integer type = getParaToInt("type");
		Map<String, Object> map = MCDao.copypasteCode(copycode, type);
		renderJson(map);
	}
	/*********************花价管理*********************/
	// 获取花材批次列表
	public void price(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		String code = getPara("code");
		Page<Record> page = MCDao.getPriceForBatch(pageno, 16, code);
		setAttr("batchcode", code);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("pricebatchlist", page.getList());
		render("price.html");
	}
	// 获取花价详情
	public void priceDetail(){
		int type = getParaToInt();
	
		if(type==1 || type==2 || type==5){	// 订阅级,包括（双品、多品、定制）
			setAttr("type", 0);
			setAttr("flowerMap", MCDao.getFlowerForCode(0));
		}else{
			setAttr("type", 1);	// 送花级
			setAttr("flowerMap", MCDao.getFlowerForCode(1));
		}
		render("price_detail.html");
	}
	// 获取节日送花花价详情
	public void priceHoliday(){
		setAttr("type", 2);	// 节日送花级
		setAttr("flowerMap", MCDao.getFlowerHoliCode());
		render("price_detail.html");
	}
	// 保存花价
	public void savePrice(){
		int type = getParaToInt("type");
		String flist = getPara("flist");
		if(type!=2){
			renderJson(MCDao.savePrice(type, flist));
		}else{
			renderJson(MCDao.saveHoPrice(type, flist));
		}
	}
	// 花材价格查看
	public void seePrice(){
		String code = getPara();
		setAttr("pricelist", MCDao.seePrice(code));
		render("price_see.html");
	}
	// 花材价格查看中的实际花价
	public void savePriceSee(){
		String flist = getPara("flist");
		renderJson(MCDao.savePriceSee(flist));
	}
	
	/*********************适赠对象*********************/
	// 适赠对象列表
	public void idoneity(){
		setAttr("idoneitylist", Db.find("select id,title,imgurl,ctime from f_idoneity where state = ?", 0));
		render("idoneity.html");
	}
	// 保存数据
	public void saveIdoneity() throws IOException{
		boolean result = false;
		Record record = new Record();
		record.set("title", getPara("title"));
		String basestr = getPara("basestr");
		if(basestr != null && !"".equals(basestr)){
			String imgurl = UploadImageUtil.upLoadBase(basestr);
			record.set("imgurl", imgurl);
		}
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("ctime", new Date());
			result = Db.save("f_idoneity", record);
		}else{
			record.set("id", id);
			result = Db.update("f_idoneity", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getIdoneity(){
		Integer id = getParaToInt();
		if(id != null){
			setAttr("idoneity", Db.findById("f_idoneity", id));
		}
		render("idoneity_detail.html");
	}
	// 删除
	public void delIdoneity(){
		int id = getParaToInt();
		Record record = new Record();
		record.set("id", id);
		record.set("state", 1);
		renderJson(Db.update("f_idoneity", record));
	}
	
	/*********************色系管理*********************/
	//管理色系
	public void color(){
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		String fcolor = getPara(1);
		try {
			fcolor = URLDecoder.decode(fcolor==null?"":fcolor, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Page<Record> page = MCDao.getColor(pageno, 16, fcolor);
		setAttr("fcolor", fcolor);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("colorlist", page.getList());
		render("color.html");
	
	}
	
	/**
	 * 主页商品显示管理
	 * @author Glacier
	 * @date 2017年7月13日
	 */
	public void indexShow() {
		 List<Record> showlist = Db.find("SELECT id,name,imgurl,price,goods_url,goods_describe,isshow,sort from f_index_goods where type = 1");
		 long totalrow = Db.queryLong("SELECT COUNT(id) from f_index_goods ");
		 setAttr("showlist", showlist);
		 setAttr("totalrow", totalrow);
		 
		 //轮播
		 List<Record> rotation = new FCDao().getRotation();
		 setAttr("rotation", rotation);
		 
		 render("indexMange.html");
	}
	
	/**
	 * 查看主页商品
	 * @author Glacier
	 * @date 2017年7月13日
	 */
	public void seeIndexGoods(){
		if (getPara() == null) {
			render("indexGoodsChange.html");
		}
		String id = getPara();
		setAttr("goods",Db.findById("f_index_goods", id));
		render("indexGoodsChange.html");
	}
	
	/**
	 * 查看轮换图信息
	 * @author Glacier
	 * @date 2017年7月19日
	 */
	public void seeRotation() {
		String id = getPara();
		setAttr("goods",Db.findById("f_index_goods", id));
		render("indexRotationChange.html");
	}
	
	/**
	 * 更改轮换图
	 * @author Glacier
	 * @date 2017年7月21日
	 * @throws IOException
	 */
	public void changeRotation() throws IOException {
		
		/*boolean result = false;
		Record f_index_goods = new Record();
		*/
		
		/*String imgurl1 = getPara("url");
		String imgurl2 = getPara("imgurl1");
		String imgurl3 = getPara("imgurl2");
		String describe = getPara("describe");
		
		if(imgurl1!=null&&imgurl1.equals("")==false&&imgurl2!=null&&imgurl2.equals("")==false&&imgurl3!=null&&imgurl3.equals("")==false){
		 	String img1 = UploadImageUtil.upLoadBase(imgurl1);
		 	String img2 = UploadImageUtil.upLoadBase(imgurl2);
		 	String img3 = UploadImageUtil.upLoadBase(imgurl3);
			Db.update("UPDATE f_index_goods SET imgurl = ?,imgurl1 =?,imgurl2=?, goods_describe = ? where id =?",img1,img2,img3,describe,8);
		}
		renderJson();*/
		
		boolean result = false;
		Record goodsInfo = new Record();
		int id = getParaToInt(0);
		goodsInfo.set("id", id);
		goodsInfo.set("name", getPara("title"));
		
		String imgurl ="";
		String img1 = getPara("img1");
		if(img1!=null&&img1.equals("")==false){
			imgurl=UploadImageUtil.upLoadBase(img1);
			goodsInfo.set("imgurl", imgurl );
		}
		
		String img2 = getPara("img2");
		if(img2!=null&&img2.equals("")==false){
			imgurl=UploadImageUtil.upLoadBase(img2);
			goodsInfo.set("imgurl1", imgurl );
		}
		
		String img3 = getPara("img3");
		if(img3!=null&&img3.equals("")==false){
			imgurl=UploadImageUtil.upLoadBase(img3);
			goodsInfo.set("imgurl2", imgurl );
		}
		
		goodsInfo.set("price", getPara("price"));
		goodsInfo.set("goods_url", getPara("url"));
		goodsInfo.set("goods_describe", getPara("describe"));
		goodsInfo.set("isshow", getPara("isshow"));
		goodsInfo.set("sort", getPara("sort"));
		
		result = Db.update("f_index_goods", goodsInfo);
		renderJson("result",result);
	}
	
	
	/**
	 * 添加主页显示商品
	 * @author Glacier
	 * @date 2017年7月14日
	 */
	public void addIndexGoods() throws Exception{
		
		boolean result = false;
		
		Record f_index_goods = new Record();
		f_index_goods.set("name", getPara("title"));
		String img = getPara("img");
		String imgurl ="";
		if(img!=null&&img.equals("")==false){
			imgurl=UploadImageUtil.upLoadBase(img);
			f_index_goods.set("imgurl", imgurl );
		}
		
		f_index_goods.set("price", getPara("price"));
		f_index_goods.set("goods_url", getPara("url"));
		f_index_goods.set("goods_describe", getPara("describe"));
		f_index_goods.set("isshow", getPara("isshow"));
		f_index_goods.set("sort", getPara("sort"));
		
		result =  Db.save("f_index_goods", f_index_goods);
		renderJson("result",result);
	}
	
	/**
	 * 修改主页商品信息
	 * @author Glacier
	 * @date 2017年7月13日
	 */
	public void changeIndexGoods() throws Exception {
		boolean result = false;
		Record goodsInfo = new Record();
		int id = getParaToInt(0);
		goodsInfo.set("id", id);
		goodsInfo.set("name", getPara("title"));
		
		String img = getPara("img");
		String imgurl ="";
		if(img!=null&&img.equals("")==false){
			imgurl=UploadImageUtil.upLoadBase(img);
			goodsInfo.set("imgurl", imgurl );
		}
		
		goodsInfo.set("price", getPara("price"));
		goodsInfo.set("goods_url", getPara("url"));
		goodsInfo.set("goods_describe", getPara("describe"));
		goodsInfo.set("isshow", getPara("isshow"));
		goodsInfo.set("sort", getPara("sort"));
		
		result = Db.update("f_index_goods", goodsInfo);
		renderJson("result",result);
	}
	
	
//	public void color(){
//	Integer pageno = getParaToInt()==null?1:getParaToInt();
//	Page<Record> page = MCDao.getColor(pageno, 16);
//	setAttr("pageno", page.getPageNumber());
//	setAttr("totalpage", page.getTotalPage());
//	setAttr("totalrow", page.getTotalRow());
//	setAttr("colorlist", page.getList());
//	render("color.html");
//	}
//	
	
	
	
	// 保存数据
	public void saveProductColor(){
		boolean result = false;
		Record record = new Record();
		record.set("name", getPara("name"));
		Integer id = getParaToInt("id");
		if(id == null){
			result = Db.save("f_color", record);
		}else{
			record.set("id", id);
			result = Db.update("f_color", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getProductColor(){
		Integer id = getParaToInt(0);
		if(id != null){
			setAttr("color", Db.findById("f_color", id));
		}
		render("color_detail.html");
	}
	// 设置忌讳的花材分类
	public void set_color_jh(){
		Integer id = getParaToInt("id");
		Integer jh = getParaToInt("jh");
		Db.update("update f_color set jh = ? where id =?", jh, id);
	}
	
	public void updateVaseState(){
		boolean result = false;
		int id = getParaToInt(0);
		int state = getParaToInt(1);
		Record record = new Record();
		record.set("id", id);
		record.set("state", state);
		result = Db.update("f_flower_pro", record);
		renderJson("result", result);
	}
}
