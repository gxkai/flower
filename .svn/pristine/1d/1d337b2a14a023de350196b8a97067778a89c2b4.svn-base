package com.controller.manage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dao.MCDao;
import com.interceptor.ManageInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.util.UploadImageUtil;
import com.util.wxRefunds;


/**
 * @Desc 文章管理
 * @author lxx
 * @date 2016/8/12
 * */
@Before(ManageInterceptor.class)
public class ManageArticleCtrl extends Controller {
	/*********************系统公告*********************/
	// 列表
	public void notice(){
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);	 
		String tcode = getPara(1);
		if(tcode!=null){
			try {
				tcode = URLDecoder.decode(tcode, "utf-8");
				Page<Record> page = MCDao.getNotice(pageno, 16, tcode);
				setAttr("titlecode", tcode);
				setAttr("pageno", page.getPageNumber());
				setAttr("totalpage", page.getTotalPage());
				setAttr("totalrow", page.getTotalRow());
				setAttr("noticelist", page.getList());
				render("notice.html");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			Page<Record> page = MCDao.getNotice(pageno, 16, tcode);
			setAttr("titlecode", tcode);
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("noticelist", page.getList());
			render("notice.html");
		}
	}
	// 保存数据
	public void saveNotice(){
		boolean result = false;
		Record record = new Record();
		record.set("title", getPara("title"));
		record.set("describe", getPara("describe"));
		record.set("info", getPara("info"));
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("ctime", new Date());
			result = Db.save("f_notice", record);
		}else{
			record.set("id", id);
			result = Db.update("f_notice", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getNotice(){
		Integer id = getParaToInt(0);
		if(id != null){
			setAttr("notice", Db.findById("f_notice", id));
		}
		render("notice_detail.html");
	}
	// 获取单条数据
	public void getNoticeInfo(){
		int id = getParaToInt(0);
		setAttr("notice", Db.findById("f_notice", id));
		render("notice_detail_check.html");
	}
	// 删除数据
	public void delNotice(){
		int id = getParaToInt();
		renderJson(Db.deleteById("f_notice", id));
	}
	
	/*********************生活美学*********************/
	// 列表
	public void esthetics() throws UnsupportedEncodingException {
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		String tcode = getPara(1);
		if(tcode != null){
			tcode = URLDecoder.decode(tcode, "utf-8");
		}
		Page<Record> page = MCDao.getEsthetics(pageno, 16, tcode);
		
		setAttr("titlecode", tcode);
		setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("estheticslist", page.getList());
		render("esthetics.html");
	}
	// 保存数据
	public void saveEsthetics() throws IOException{
		boolean result = false;
		Record record = new Record();
		record.set("title", getPara("title"));
		String basestr = getPara("basestr");
		if(basestr != null && !"".equals(basestr)){
			String imgurl = UploadImageUtil.upLoadBase(basestr);
			record.set("imgurl", imgurl);
		}
		record.set("info", getPara("info"));
		Integer id = getParaToInt("id");
		if(id == null){
			record.set("ctime", new Date());
			result = Db.save("f_esthetics", record);
		}else{
			record.set("id", id);
			result = Db.update("f_esthetics", record);
		}
		renderJson("result", result);
	}
	// 获取单条数据
	public void getEsthetics(){
		Integer id = getParaToInt(0);
		if(id != null){
			setAttr("esthetics", Db.findById("f_esthetics", id));
		}
		render("esthetics_detail.html");
	}
	// 删除数据
	public void delEsthetics(){
		int id = getParaToInt();
		renderJson(Db.deleteById("f_esthetics", id));
	}
	// 置顶操作
	public void istopEsthetics(){
		int id = getParaToInt(0);
		int top = getParaToInt(1);
		top = top==1?0:1;
		Record record = new Record().set("id", id).set("istop", top);
		boolean result = Db.update("f_esthetics", record);
		renderJson(result);
	}
	
	//退款测试   -- 内測 根据用户id 退款
	public void test() {
		System.out.println("进入");
		int id = getParaToInt(0);
		
		Calendar calendar = Calendar.getInstance();  
	    calendar.setTime(new Date());  
	    calendar.add(Calendar.DAY_OF_MONTH, -2);  
	    Date date = calendar.getTime();  
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(df.format(date));
		//48 小时前
		String oldtime = df.format(date).toString();
		
		//sql 测试
		//String sql = "select id,money,quantity1,quantity2,ctime,ttime,tmoney,state,aid from f_redpackets where state = '1' "
		//		+ "and (ctime >= "+oldtime+" and ctime <= NOW() ) and aid = ?",id);
		
		//获取未抢完的红包 (48小时前~现在 未抢完红包 -- 测试先不加)
		List<Record> list = Db.find( "select id,money,quantity1,quantity2,ctime,ttime,tmoney,state,aid from f_redpackets where state = '1'"
				+ "and aid = ?",id);
		
		//根据单身获取 红包详细信息
		for (int i = 0; i < list.size(); i++) {
			//退款金额
			double sum = 0;
			List<Record> detiallist = Db.find("SELECT id,rpid,rppid,isopen,aid,oid,dmoney FROM f_redpackets_detial where rpid = ? and isopen = 0",list.get(i).getInt("id"));
			//计算退款金额
			for (int j = 0; j < detiallist.size(); j++) {
				sum += detiallist.get(j).getDouble("dmoney"); 
			}
			
			//商户内部退款号 
			String out_refund_no = Integer.toString(list.get(i).getInt("id"));
			//商户订单号
			String out_trade_no =  Integer.toString(list.get(i).getInt("id"));
			//退款金额
			String refund_fee = Integer.toString((int)(sum*100));
			
			//总金额  微信金额不支持小数
			BigDecimal tot1 = list.get(i).get("money");
			BigDecimal tot2 = new BigDecimal("100");
			tot1 = tot1.multiply(tot2);//乘 100
			
			String total_fee =String.valueOf(tot1.intValue());
			
			//进行退款
			Map<String, String> resulet = new wxRefunds().Refunds(out_refund_no, out_trade_no, refund_fee, total_fee);
			
			//退款成功后修改 红包状态
			if (resulet.get("result_code").equals("SUCCESS")) {
				
				//修改f_redpackets 添加退款时间ttime， 退款金额tmoney，状态改为3 已退款状态   退款账号admin(系统退款)
				Db.update("UPDATE f_redpackets SET ttime=NOW(),tmoney=?,state = 3,opuser='admin'  where id = ?",sum,list.get(i).getInt("id"));
				
				//根据单号修改单身状态
				Db.update("UPDATE f_redpackets_detial SET isopen = '2' WHERE rpid = ?",list.get(i).getInt("id"));	
			}
			
			
		}
		renderJson("执行完退款程序");
		
	}

	/**
	 * 测试状态
	 * @author Glacier
	 * @date 2018年1月11日
	 */
	public void testType() {
		System.out.println("进来了");
		Record isfans = Db.findFirst("SELECT isfans FROM f_account LIMIT 1");
		int is = isfans.getInt("isfans");
		renderJson(is);
	}
	
}
