package com.controller.manage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import com.dao.FCDao;
import com.dao.MCDao;
import com.interceptor.ManageInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.util.Constant;
import com.util.DesUtil;

/**
 * @Desc 推广管理
 * @author lxx
 * @date 2016/11/10
 * */
@Before(ManageInterceptor.class)
public class ManageSpreadCtrl extends Controller{
	// 人员列表
	public void index(){
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		Integer state = getParaToInt("state")==null?0:getParaToInt("state");
		String number = getPara("number");
		String tel = getPara("tel");
		String name = getPara("name");
		String time_a = getPara("time_a");
		String time_b = getPara("time_b");
		
		if(name != null){
			try {
				name = URLDecoder.decode(name,"utf-8");
				Page<Record> page = MCDao.getSpreadList(pageno, 16, state, number, name, tel, time_a, time_b);
				setAttr("state", state);
				setAttr("number", number);
				setAttr("name", name);
				setAttr("tel", tel);
				setAttr("time_a", time_a);
				setAttr("time_b", time_b);
				
				List<Record> spreadlist = page.getList();
				for (Record spread : spreadlist) {
					String addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ spread.getStr("area") +")");
					spread.set("addr", addr+"-"+spread.getStr("address"));
				}
				setAttr("pageno", page.getPageNumber());
				setAttr("totalpage", page.getTotalPage());
				setAttr("totalrow", page.getTotalRow());
				setAttr("spreadlist", spreadlist);
				render("spread_list.html");
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			Page<Record> page = MCDao.getSpreadList(pageno, 16, state, number, name, tel, time_a, time_b);
			
			setAttr("state", state);
			setAttr("number", number);
			setAttr("name", name);
			setAttr("tel", tel);
			setAttr("time_a", time_a);
			setAttr("time_b", time_b);
			
			List<Record> spreadlist = page.getList();
			for (Record spread : spreadlist) {
				String addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ spread.getStr("area") +")");
				spread.set("addr", addr+"-"+spread.getStr("address"));
			}
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("spreadlist", spreadlist);
			render("spread_list.html");
		}
	}
	
	// 获得人员详情
	public void getSpreadperson(){
		Integer id = getParaToInt(0);
		if(id != null){
			setAttr("spread", Db.findById("f_spread", id));
			Record record = Db.findById("f_spread", id);
			String area = record.getStr("area");
     		String[] areas = area.split(",");
     		
     		Record prov = Db.findById("f_area", areas[0]);
     		Record city = Db.findById("f_area", areas[1]);
     		Record county = Db.findById("f_area", areas[2]);
     		
     		setAttr("prov", prov);
     		setAttr("city", city);
     		setAttr("county", county);
		}
		String areas =  FCDao.getArea();
		List<Record> themelist = Db.find("select name,id from f_cashtheme");
		setAttr("arealist", areas);
		setAttr("themelist", themelist);
		render("spread_detail.html");
	}
	
	// 冻结人员
	public void freezeSpread(){
		int id = getParaToInt(0);
		int state = getParaToInt(1);
		renderJson(MCDao.freezeSpread(id, state));
	}
	
	// 保存人员信息
	public void saveSpreadPreson(){
		boolean result = false;
		Record record = new Record();
		record.set("number", getPara("number"));
		record.set("name", getPara("name"));
		record.set("sex", getParaToInt("sex"));
		record.set("tel", getPara("tel"));
		record.set("address", getPara("address"));
	//	record.set("cashthemeId", getPara("themeId"));
		String area = getPara("prov")+","+getPara("city")+","+getPara("county");
		record.set("area", area);
		
		Integer id = getParaToInt("id");
		if(id == null){
			if(Db.save("f_spread", record)){
				long sprid =  record.getLong("id");
				String number = "DT" + String.format("%05d", sprid);
				String url = HttpKit.get(Constant.getHost + "/weixin/createQrCode/2-" + sprid);
				Db.update("update f_spread set number=?,qrurl=? where id=?", number, url, sprid);
				result = true;
			}
		}else{
			record.set("id", id);
			result = Db.update("f_spread", record);
		}
		renderJson("result", result);
	}
	
	// 查看二维码
	public void spreadQrCord() throws Exception{
		Integer id = getParaToInt();
		Record spread = Db.findById("f_spread", id);
		String idStr = new DesUtil().encrypt(id.toString());
		setAttr("number", spread.get("number"));
		setAttr("name", spread.get("name"));
		setAttr("qrurl", Constant.getHost + "/spread/" + idStr);
		render("spread_qrcode.html");
	}
}