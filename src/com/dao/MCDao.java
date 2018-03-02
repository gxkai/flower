package com.dao;

import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

import javax.lang.model.type.PrimitiveType;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.controller.WeixinApiCtrl;
import com.controller.front.FrontServiceCtrl;
import com.google.gson.Gson;
import com.jd.open.api.sdk.JdException;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.MediaApi;
import com.jfinal.weixin.sdk.api.QrcodeApi;
import com.jfinal.weixin.sdk.api.MediaApi.MediaType;
import com.jfinal.weixin.sdk.api.TemplateData;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;
import com.jfinal.weixin.sdk.utils.HttpUtils;
import com.util.Constant;
import com.util.DeliveryDateUtil;
import com.util.JDUtil;
import com.util.NewImageUtils;
import com.util.QRCodeUtil;
import com.util.SendMouldMsgUtil;
import com.util.SendMsgUtil;

import javafx.scene.input.DataFormat;

import com.util.Constant.seedType;

/**
 * @Desc 后台公共数据接口
 * */
public class MCDao{
	//日期编码
	static String dateCode;
	//返回信息
	static Map<String, Object> responseMap;
	//反馈信息
	static String Msg;
	
	static int Id;
	
	static int typeId;
	
	static String fList;
	
	static String fName;
	
	static String sName;
	
	static int aPp; //用途
	static int sTyle;//格调
	static int pRiority;//配花优先级(定制花束适用)
	static int aPpoint;//给指定的订单分配此花束
	
	static Integer iId;
	
	static Integer fPid;
	
	static Map<String, Object> map;
	
	static String Copycode;
	
	static int Type;
	
	static String OrderCode;
	static String Aid;
	static int Pid;
	static Integer Vase;
	static String Area;
	static String Address;
	static int Reach;
	static int Cycle;
	static String Zhufu;
	static String Songhua;
	static String jh_List;
	static String jhColor_List;
	static int TType;
	static String Recommend;
	static Integer Szdx;
	static Integer Cash;
	static Integer Activity;
	static Double Yh;
	static Record Account;
	static Record pro_Flower;
	static int isBuy;
	static Record Order;
	static String Name;
	static String Tel;

	// 获取导航目录
	public static List<Record> getMenu(){
		return Db.find("select id,name,url,pid from f_menu");
	}
	
	// 获取导航目录
	public static Page<Record> getMenuPage(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select a.id,a.name,a.url,a.pid,b.name as pname", "FROM f_menu a LEFT JOIN f_menu b ON a.pid=b.id");
	}
	
	// 获取数据字典
	public static Page<Record> getDictionary(int pageno, int pagesize,String code_name,String code_key){
		String strWhere ="from f_dictionary where 1=1";
		if(code_name!=null && code_name.equals("")==false){
			strWhere+=" and code_name='"+code_name+"'";
		}
		if(code_key!=null&& code_key.equals("")==false){
			strWhere+=" and code_key='"+code_key+"'";
		}
		return Db.paginate(pageno, pagesize, "select id,code_name,code_key,code_value,note,state,orderId", strWhere);
	}
	
	/**
	 * 获取 f_holiday 表
	 * @author Glacier
	 * @date 2017年7月26日
	 * @return
	 */
	public static List<Record> getHoliday() {
		return Db.find("SELECT id,hName,hDate,hWeek,hState,hReach,hPtid,hTitle from f_holiday");
	}
	
	// 获取常见问题
	public static Page<Record> getQuestion(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select id,title,info", "from f_question order by id desc");
	}
	public static List<Record> get_flower_pro_type(){
		return Db.find("select code_name,code_value from f_dictionary where code_key='ptid'");
	}
	
	/**
	 * 本次发货预计送达日期
	 * 当更新【发货批号】时，自动更新。
	 * 当遇到节假日，批号和送达日期不一致时，人工修改。
	 * @return
	 */
	public static String getReachDate(){
		return Db.queryStr("select code_value from f_dictionary where id=32 and  code_name='本次发货预计送达日期'and code_key='reachDate'");
	}
	
	/**
	 * 获取大于当前时间批号，已经发货的批次就不用修改了
	 * @param orderCode
	 * @return
	 */
	public static List<Record> getNextPiCodeList(String orderCode){
		return Db.find("select id,orderCode,piCode,reach,offsetDays from f_picode where orderCode=? and str_to_date(piCode,'%Y%m%d')>NOW()", orderCode);
	}
	/**
	 * 获取批号修改的次数,奇数次和偶数次修改有区别
	 * @param orderCode
	 * @return
	 */
	public static int getModifyPiCodeCount(String orderCode){
		Long count=Db.queryLong("select count(1) 'count' from f_picode_modifylog where orderCode=? and isModifyReach=1",orderCode);
	    return count.intValue();
	}
	/**
	 * 保存修改后的批号
	 * @param hadModifyPiCodeList 修改后的批号列表
	 */
	public static Boolean saveModfiyResult(final List<Record> hadModifyPiCodeList,final String orderCode,final int isSy,final int isModifyReach,final int reachOld,final int reachNew){
		return Db.tx(new IAtom() {
			
			@Override
			public boolean run() throws SQLException {
				boolean result = false;
				Record order=Db.findFirst("select is_sy,type from f_order where ordercode=?",orderCode);
				if(order.getInt("is_sy")>=2 && (order.getInt("type")==43||order.getInt("type")==41)){
					return false;
				}else{
					Db.update(" update f_order set is_sy=is_sy+1 where ordercode=? ",orderCode);
					
					for(int i=hadModifyPiCodeList.size()-1;i>=0;i--){
						Db.update("update f_picode set piCode=?,reach=?,offsetDays=0 where id=?", 
								hadModifyPiCodeList.get(i).getStr("piCode"),
								hadModifyPiCodeList.get(i).getInt("reach"),
								hadModifyPiCodeList.get(i).getInt("id"));
					}
					
					if(reachOld!=reachNew){
						Db.update("update f_order set reach=? where orderCode=?", reachNew,orderCode);
					}
					Record f_picode_modifylog = new Record();
					f_picode_modifylog.set("ordercode", orderCode);
					f_picode_modifylog.set("isSy", isSy);
					f_picode_modifylog.set("isModifyReach", isModifyReach);
					f_picode_modifylog.set("reachOld", reachOld);
					f_picode_modifylog.set("reachNew", reachNew);
					f_picode_modifylog.set("reachNew", reachNew);
					f_picode_modifylog.set("modifyTime", new Date());
					result = Db.save("f_picode_modifylog",f_picode_modifylog );
					return result;
				}
				
			}
		});
		
		
		
		
	}
	
	
	//意见反馈详情
	public static Record getAdviceinfo(int id){
		return Db.findFirst("select title,info from f_feedback where id=?", id);
	}
	
	// 获取养花须知
	public static Page<Record> getKnowledge(int pageno, int pagesize, int type, String tcode){
		String sqlExceptSelect = "FROM f_knowledge where 1=1";
		if(type !=0 )
			sqlExceptSelect += " and type = "+type;
		if(tcode != null)
			sqlExceptSelect += " and title like '%" + tcode + "%'";
		sqlExceptSelect += " order by istop desc,id desc";
		return Db.paginate(pageno, pagesize, "select id,type,title,imgurl,summary,istop", sqlExceptSelect);
	}
	
	// 获取地区
	public static List<Record> getArea(){
		return Db.find("select id, name, pid from f_area");
	}
	
	// 获取子级地区
	public static List<Record> getArea(int pid){
		return Db.find("select id, name, pid from f_area where pid = ?", pid);
	}
	
	// 获取周边产品
	public static List<Record> getAroundPros(){
		return Db.find("SELECT id, name FROM f_flower_pro WHERE ptid > 3 and state = 0");
	}
	
	// 删除地区
	public static boolean delArea(int id){
		Record area = Db.findById("f_area", id);
		if(area.getInt("pid")==0){
			// 一级地区
			Db.deleteById("f_area", id);
			List<Record> list = getArea(id);
			for(Record r : list){
				if(Db.delete("f_area", r)){
					Db.batch("delete from f_area where pid=?", new Object[][]{{r.getInt("id")}}, 50);
				}
			}
		}else{
			// 二级地区或多级地区
			if(Db.deleteById("f_area", id)){
				Db.batch("delete from f_area where pid=?", new Object[][]{{id}}, 50);
			}
		}
		return true;
	}
	
	// 获取系统公告
	public static Page<Record> getNotice(int pageno, int pagesize,String tcode){
		String sqlExceptSelect = "FROM f_notice a";
		if(tcode != null)
			sqlExceptSelect += " where a.title like '%" + tcode + "%'";
		sqlExceptSelect += " order by a.id desc";
		return Db.paginate(pageno, pagesize, "select id,title,ctime,info,a.describe ", sqlExceptSelect);
	}
	
	// 获取生活美学
	public static Page<Record> getEsthetics(int pageno, int pagesize, String code){
		String sqlExceptSelect = "FROM f_esthetics";
		if(code != null)
			sqlExceptSelect += " where title like '%" + code + "%'";
		sqlExceptSelect += " order by istop desc,id desc";
		return Db.paginate(pageno, pagesize, "select id,title,imgurl,ctime,istop", sqlExceptSelect);
	}
	
	// 获取物流平台
	public static Page<Record> getExpress(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select id,name,code,price,url", "from f_express");
	}
	
	/**
	 * 获取弹框信息
	 * @param pageno
	 * @param pagesize
	 * @param state
	 * @return
	 */
	public static Page<Record> getMaskLayer(int pageno,int pagesize,int state){
		String sqlSelect="select id,layeruse,remark,content,img,fpid,case when state=0 then '暂停' else '启用' end 'state' ";
	    String sqlWhere=" from f_masklayer where 1=1";
	    if(state!=10){
	    	sqlWhere+=" and state="+state;
	    }
	    return Db.paginate(pageno, pagesize, sqlSelect, sqlWhere);
	}
	/**
	 * 获取单笔弹框信息
	 * @param id
	 * @return
	 */
	public static Record getMaskLayerById(int id){
		return Db.findById("f_masklayer", id);
	}
	/**
	 * 获取【特殊日子赠送的礼品】
	 * @return
	 */
	public static List<Record> getFlower_pro_401(){
		return Db.find("select a.id,CONCAT(a.name,'(',b.code_name,')') 'name'  from f_flower_pro a left join f_dictionary b on a.ptid=b.code_value"
				        + " where a.ptid=401 and a.state=0");
	}
	/**
	 * 鲜花已发货最大批号
	 * @return
	 */
	public static String getMaxPiciCode(){
		String maxCode=	Db.queryStr("select max(`code`) 'maxCode' from f_order_info as a  where not exists (select ordercode from f_order as b where a.ordercode=b.ordercode and b.type  in(3,43) )");
		return maxCode;
	}
	
	/**
	 * 预配单最大批号
	 * @return
	 */
	public static String getMaxPiciCode_pre(){
		String maxCode=	Db.queryStr("select max(`code`) 'maxCode' from f_order_info_pre");
		return maxCode;
	}
	
	/**
	 * 待发货最小批号
	 * 即下次发货批号
	 * @return
	 */
	public static String getMinPiciCode(){
		String minCode=Db.queryStr("select min(piCode) from f_picode where piCode>(select max(`code`) 'maxCode' from f_order_info)");
	    return minCode;
	}
	
	/**
	 * 根据订单号，获取具体的发货详情
	 * 主意：花束和周边，分两次查询，还是比较复杂的
	 * 作者:chunxiang
	 * @param ordercode  订单编号
	 * @return
	 */
	public static List<Record> getSendGoodsInfo(String ordercode){
		String strSelect="select f_picode.num, piCode,IFNULL(number,'未发货') 'number',IFNULL(fname,'未分配') 'fname' "+
                   " from f_picode   left join (select code,ordercode,GROUP_CONCAT(fname) 'fname',number from ( "+
                   " select * from  ( "+
                   " select a.code,a.ordercode,a.number,c.fname from f_order_info as a left join  f_order_pro as b on a.id=b.oid "+
                   " left  join f_product as c on b.pid=c.id "+
                   " where a.ordercode=? and b.type<>1)  as x "+
                   " union "+
                   " (select a.code,a.ordercode,a.number,c.name from f_order_info as a left join  f_order_pro as b on a.id=b.oid "+
                   " left  join f_flower_pro as c on b.pid=c.id "+
                   " where a.ordercode=? and b.type<>0) ) as  y  group by code) as m on f_picode.piCode=m.code "+
                   " where f_picode.ordercode=?  ORDER BY f_picode.num";
		List<Record> record=Db.find(strSelect, ordercode,ordercode,ordercode);
		return record;
	}
	
	public static List<Record> f_flower_pro_ZB(){
		return Db.find("select id,name from f_flower_pro where ptid in(4,5,6,7,8,9,10,403,404) and state=0 ORDER BY name");
	}
	
	/**
	 * 获取周边物流列表
	 * @param pageno 显示的当前页
	 * @param pagesize 每页显示的记录数
	 * @param picicode 批次号
	 * @param ordercode 订单号
	 * @param logisticcode 物流编号
	 * @param state 状态:异常、正常、已发货、已签收
	 * @param ishas 是否为首单
	 * @param wuliucode 配送公司编号
	 * @param receiver 收件人
	 * @return
	 */
	public static Page<Record> getOrderInfo_ZB(int pageno, int pagesize, String picicode, String ordercode, String logisticcode, int state, int ishas, String wuliucode, String receiver,int fpid){
		String	sqlExceptionSelect="FROM f_order_info a left join f_order_pro as b on b.oid=a.id "
				+ " left join f_flower_pro as c on b.pid=c.id   "
				+ " left join f_order as i on a.ordercode=i.ordercode where 1=1 and i.type in(3,43)";
		if(StrKit.notBlank(picicode)){
			sqlExceptionSelect += " and a.code like '"+picicode+"%'";
		}
		if(StrKit.notBlank(ordercode)){
			sqlExceptionSelect += " and a.ordercode like '%"+ordercode+"%'";
		}
		if(StrKit.notBlank(logisticcode)){
			sqlExceptionSelect += " and a.lognumber like '%"+logisticcode+"%'";
		}
		if(state !=10){
			sqlExceptionSelect += " and a.state =" + state;
		}
		if(!"xzwl".equals(wuliucode)){
			sqlExceptionSelect += " and a.ecode ='" + wuliucode+"'";
		}
		if(StrKit.notBlank(receiver))
			sqlExceptionSelect += " and a.name like '%" + receiver + "%'";
		if(fpid!=0){
			sqlExceptionSelect += " and c.id =" + fpid ;
		}
		sqlExceptionSelect += " GROUP BY a.id,a.code,a.ordercode,a.number,a.NAME,a.tel,a.ename,a.address,a.state order by a.code desc";
		Page<Record> page = Db.paginate(pageno, pagesize, "SELECT a.lognumber,FORMAT(a.price,2) as price, a.id,a.code,a.ordercode,"
				+ " a.number,a.NAME as sname,a.tel,a.ename,a.address,"
				+ " a.ishas,a.ofcycle,a.state,a.ctime,i.gName,i.cycle, "
				+ " GROUP_CONCAT(CONCAT(c.name,b.num,'件')) 'wlname'", sqlExceptionSelect);
		return page;
	}
	
	
	/**
	 * 获取鲜花物流列表
	 * @param pageno 显示的当前页
	 * @param pagesize 每页显示的记录数
	 * @param picicode 批次号
	 * @param ordercode 订单号
	 * @param logisticcode 物流编号
	 * @param state 状态:异常、正常、已发货、已签收
	 * @param ishas 是否为首单
	 * @param wuliucode 配送公司编号
	 * @param receiver 收件人
	 * @return
	 */
	public static Page<Record> getOrderInfo(int pageno, int pagesize, String picicode, String ordercode, String logisticcode, int state, int ishas, String wuliucode, String receiver,int type){
		String sqlExceptionSelect=" FROM f_order_info a left join f_order_pro as b on b.oid=a.id left join f_product as  c on c.id=pid left join f_flower_pro as d on c.fpid=d.id left join  f_order as i on a.ordercode=i.ordercode where 1=1 ";
		//如果是周边，f_order_pro中的pid直接关联f_flower_pro中的id  
		if(type==3)	{
			sqlExceptionSelect="FROM f_order_info a left join f_order_pro as b on b.oid=a.id left join f_flower_pro as c on b.pid=c.id left join f_order as i on a.ordercode=i.ordercode where 1=1";
		}
		
		if(StrKit.notBlank(picicode)){
			sqlExceptionSelect += " and a.code like '"+picicode+"%'";
		}
		if(StrKit.notBlank(ordercode)){
			sqlExceptionSelect += " and a.ordercode like '%"+ordercode+"%'";
		}
		if(StrKit.notBlank(logisticcode)){
			sqlExceptionSelect += " and a.lognumber like '%"+logisticcode+"%'";
		}
		if(state !=10){
			sqlExceptionSelect += " and a.state =" + state;
		}
		if(ishas !=10){
			sqlExceptionSelect += " and a.ishas =" + ishas;
		}
		//选择类型：0双品 ·1多品  2送花 3 周边,4节日,5定制
		//20170312时春香添加
		if(type!=10){
			//双品
			if(type==0){
				sqlExceptionSelect += " and b.type=0 and d.ptid in ('1')";
			}else if(type==1){
				sqlExceptionSelect += " and b.type=0 and d.ptid in ('2')";
			}else if(type==2){
				sqlExceptionSelect += " and b.type=0 and d.ptid in('3') ";
			}else if(type==3){
				sqlExceptionSelect+=" and b.type=1";
			}else if(type==4){
				sqlExceptionSelect+=" and b.type=0 and d.ptid >=100 and d.ptid <=200";
			}else if(type==5){
				sqlExceptionSelect+=" and b.type=0 and d.ptid >200";
			}
			
		}
		
		sqlExceptionSelect+=" and i.type not in(3,43)";//鲜花物流单
		
		if(!"xzwl".equals(wuliucode)){
			sqlExceptionSelect += " and a.ecode ='" + wuliucode+"'";
		}
		if(StrKit.notBlank(receiver))
			sqlExceptionSelect += " and a.name like '%" + receiver + "%'";
		
		sqlExceptionSelect += " GROUP BY a.id,a.code,a.ordercode,a.number,a.NAME,a.tel,a.ename,a.address,a.state order by a.id desc";
		Page<Record> page = Db.paginate(pageno, pagesize, "SELECT a.lognumber,FORMAT(a.price,2) as price, a.id,a.code,a.ordercode,a.number,a.NAME as sname,a.tel,a.ename,a.address,a.ishas,a.ofcycle,a.state,a.ctime,i.gName,i.cycle,b.num  ", sqlExceptionSelect);
		for(Record oi : page.getList()){
			List<Record> oplist = Db.find("select pid,type from f_order_pro where oid=?", oi.getInt("id"));	// 物流详细集合
			String name = new String();	// 物流产品名称
			Double cost = 0.00;			// 成本
			for(int i=0;i<oplist.size();i++){
				Record op = oplist.get(i);
				if(op.getInt("type") == 0){
					Record n = Db.findFirst("select fname,price,type,code from f_product where id=?", op.getInt("pid"));
					Double cost_1 = 0.00;
					if(n!=null){
						if(n.getInt("type") == 1||n.getInt("type") == 2 ||n.getInt("type") == 5 ||n.getInt("type") == 9){
							 cost_1 += Db.queryDouble("SELECT IFNULL( sum(p.num*q.price1),0) as cost FROM f_product o,f_product_info p,f_flower_price q"
									+ "	WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code and o.id=? and q.type=0", op.getInt("pid"));
						}else if(n.getInt("type") == 3){
							 cost_1 += Db.queryDouble("SELECT IFNULL( sum(p.num*q.price1),0) as cost FROM f_product o,f_product_info p,f_flower_price q"
									+ "	WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code and o.id=? and q.type=1", op.getInt("pid"));
						}else{
							cost_1 += Db.queryDouble("SELECT IFNULL( sum(p.num*q.price1),0) as cost FROM f_product o,f_product_info p,f_flower_price q"
									+ "	WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code and o.id=? and q.type=2", op.getInt("pid"));
						}
						cost += cost_1; 
						if(i==0){
							name = n.getStr("fname");
						}else{
							name += "," + n.getStr("fname");
						}
					}
					
				}else if(op.getInt("type") == 1){
					Record n = Db.findFirst("select name,price from f_flower_pro where id=?", op.getInt("pid"));	
					if(i==0){
						name = n.getStr("name")+oi.getInt("num")+"件";
					}else{
						name += "," + n.getStr("name")+oi.getInt("num")+"件";
					}
					cost += n.getDouble("price");
				}
			}
			oi.set("wlname", name);
			oi.set("cost", cost);
		}
		return page;
	}
	
	/**
	 * 预配单 获取物流列表
	 * @param pageno
	 * @param pagesize
	 * @param picicode
	 * @param ordercode
	 * @param logisticcode
	 * @param state
	 * @param ishas
	 * @param wuliucode
	 * @param receiver
	 * @param type
	 * @return
	 */
	public static Page<Record> getOrderInfo_pre(int pageno, int pagesize,   int state){
		Map<String, Object> chooseMap = DeliveryDateUtil.choosedate();
		String picicode=(String) chooseMap.get("datecode");
		String sqlExceptionSelect=" FROM f_order_info_pre a   left join  f_order as i on a.ordercode=i.ordercode where 1=1 and  a.code='"+picicode+"'";
		if(state !=10){
			sqlExceptionSelect += " and a.state =" + state;
		}
		sqlExceptionSelect += " GROUP BY a.id,a.code,a.ordercode,a.number,a.NAME,a.tel,a.ename,a.address,a.state order by a.id desc";
		Page<Record> page = Db.paginate(pageno, pagesize, "SELECT a.lognumber, a.id,a.code,a.ordercode,a.number,a.NAME as sname,a.tel,a.ename,a.address,a.ishas,a.ofcycle,a.state,a.ctime,i.gName,i.cycle  ", sqlExceptionSelect);
		//拼接发货产品名称
		for(Record oi : page.getList()){
			List<Record> oplist = Db.find("select pid,type from f_order_pro_pre where oid=?", oi.getInt("id"));	// 物流详细集合
			String name = new String();	// 物流产品名称
			for(int i=0;i<oplist.size();i++){
				Record op = oplist.get(i);
				//type=0表示花束，type=1表示周边
				if(op.getInt("type") == 0){
					Record n = Db.findFirst("select fname,price,type,code from f_product where id=?", op.getInt("pid"));
					if(n!=null){
						if(i==0){
							name = n.getStr("fname");
						}else{
							name += "," + n.getStr("fname");
						}
					}
				}else if(op.getInt("type") == 1){
					Record n = Db.findFirst("select name,price from f_flower_pro where id=?", op.getInt("pid"));	
					if(i==0){
						name = n.getStr("name");
					}else{
						name += "," + n.getStr("name");
					}
				}
			}
			oi.set("wlname", name);
		}
		return page;
	}
	
	// 获取物流详情
	public static Map<String, Object> getOrderPro(int oid, String code) throws JdException{
		Map<String, Object> map = new HashMap<>();
		Record orderinfo = Db.findById("f_order_info", oid);	// 物流详情
		String piCode=orderinfo.getStr("code");
		int ofcycle=orderinfo.getInt("ofcycle");//第几次发货
		Record order_jt = Db.findFirst("select jh_list,type,jh_color,ishas,ocount,style from f_order where ordercode = ?", orderinfo.getStr("ordercode"));		// 订单信息
		// 购买商品类型
		Integer ptid = Db.queryInt("SELECT b.ptid FROM f_order_detail a LEFT JOIN f_flower_pro b ON a.fpid=b.id WHERE a.ordercode=? AND a.type = 1", orderinfo.getStr("ordercode"));
		List<Record> cps = new ArrayList<>();//可选产品
		if(ptid == 1 || ptid==402 || (ptid==203&&(ofcycle%2==0))){	// 双品订阅
			Long count=Db.queryLong("select count(1) from f_product where type=1 and code=? and appoint=1", orderinfo.getStr("code"));
			//首单首次（指定产品）,也要排除忌讳花
			if(order_jt.getInt("ishas")==0&&order_jt.getInt("ocount")==0&&count>=1&&ptid==1){
				String sql="select id,fname from f_product as a where type=1 and appoint=1 and code=?";
				if(order_jt.getStr("jh_list") != null){
					//忌讳的花类对应的产品
					
					String cp_list = Db.queryStr(" SELECT GROUP_CONCAT(DISTINCT a.id)  FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid LEFT JOIN f_flower c ON b.fid = c.id WHERE a.code=? and c.tid IN (" + order_jt.getStr("jh_list") + ")",piCode);
					if(cp_list != null){					
						sql += " AND a.id NOT IN (" + cp_list + ")";
					}
				}
				//格调的筛选
				if(order_jt.getInt("style")!=3){
					sql+=" and a.style="+order_jt.getInt("style");
				}
				cps = Db.find(sql, orderinfo.getStr("code"));
			}else{
				String sql = "SELECT a.id,a.fname FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid left join f_flower c on b.fid = c.id WHERE a.type=1  and a.appoint=0 AND a.code=?";
				if(order_jt.getStr("jh_list") != null){
					//忌讳的花类对应的产品
					
					String cp_list = Db.queryStr(" SELECT GROUP_CONCAT(DISTINCT a.id)  FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid LEFT JOIN f_flower c ON b.fid = c.id WHERE a.code=? and c.tid IN (" + order_jt.getStr("jh_list") + ")",piCode);
					if(cp_list != null){					
						sql += " AND a.id NOT IN (" + cp_list + ")";
					}
				}
				if(order_jt.getStr("jh_color") != null){
					//忌讳的颜色
					String co_list = Db.queryStr("SELECT GROUP_CONCAT(DISTINCT a.id)  FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid LEFT JOIN f_flower c ON b.fid = c.id WHERE a.code=? and c.cid IN (" + order_jt.getStr("jh_color") + ")",piCode);
					if(co_list != null){
						sql += " AND a.id NOT IN (" + co_list + ")";
					}
					
				}
				//格调的筛选
				if(order_jt.getInt("style")!=3){
					sql+=" and a.style in ("+order_jt.getInt("style")+",3)";
				}
				cps = Db.find(sql, orderinfo.getStr("code"));	// 获取满足条件的本批次产品

				cps = getHasGet(cps, orderinfo.getStr("ordercode"));// 获取已送过的花并排除
			}
			
		}else if(ptid == 2 || ptid==405 || (ptid==203&&(ofcycle%2==1))){
			cps = Db.find("select id,fname from f_product where type=2 and code=? and style in (?,3)", orderinfo.getStr("code"),order_jt.getInt("style"));	// 获取本批次产品

			// 获取已送过的花并排除
			cps = getHasGet(cps, orderinfo.getStr("ordercode"));
		}else if(ptid == 3){
			Integer szdx = Db.queryInt("select szdx from f_order where ordercode=?", orderinfo.getStr("ordercode"));
			cps = Db.find("SELECT b.id,b.fname FROM f_order_detail a LEFT JOIN f_product b ON a.fpid=b.fpid WHERE a.ordercode=? AND a.type=1 AND b.code=? AND b.type=3 AND b.iid=?", orderinfo.getStr("ordercode"), orderinfo.getStr("code"), szdx);	// 获取本批次产品

		}else if(ptid==201){
			cps=Db.find("select id,fname from f_product a where code=? and type=5 and fpid in(select fpid from f_order_detail  where ordercode=? and isAllot=1)",orderinfo.getStr("code"),orderinfo.getStr("ordercode"));
		}else if(ptid == Constant.Hid){
			cps = Db.find("SELECT b.id,b.fname FROM f_order_detail a LEFT JOIN f_product b ON a.fpid=b.fpid WHERE a.ordercode=? AND a.type=1 AND b.code=? AND b.type=4", orderinfo.getStr("ordercode"), orderinfo.getStr("code"));
		}
		map.put("cps", cps);
		
		if(order_jt.getStr("jh_list")!=null && !"".equals(order_jt.getStr("jh_list"))){
			String jihuis = Db.queryStr("select GROUP_CONCAT(name) as names from f_flower_type where id in ("+order_jt.getStr("jh_list")+")");
			map.put("jihuis", jihuis);	// 忌讳的花
		}

		if(order_jt.get("jh_color")!=null && !"".equals(order_jt.get("jh_color"))){
			String jhcolors = Db.queryStr("select GROUP_CONCAT(name) as names from f_color where id in ("+order_jt.getStr("jh_color")+")");
			map.put("jhcolors", jhcolors);	// 忌讳的花
		}
		
		List<Record> oprolist = Db.find("select id,pid,type from f_order_pro where oid = ?", oid);	// 派单产品集合
		List<Record> prolist = new ArrayList<>();
		for(Record opro : oprolist){
			Record pro;
			if(opro.getInt("type") == 0){	// 花束
				pro = Db.findFirst("SELECT a.type,b.type as ptype,c.name,b.fname FROM f_order_pro a LEFT JOIN f_product b ON a.pid=b.id left join f_flower_pro c on b.fpid=c.id where a.id = ?", opro.getInt("id"));
			}else{	// 周边
				pro = Db.findFirst("SELECT a.type,b.name,b.describe FROM f_order_pro a LEFT JOIN f_flower_pro b ON a.pid=b.id where a.id = ?", opro.getInt("id"));
			}
			prolist.add(pro);
		}
		List<Record> jdlogistics = new ArrayList<>() ;
		if (orderinfo.getStr("ecode").trim().equals("jd")) {
			JSONArray JDlogistics = JDUtil.getJDlogisticdata(orderinfo.getStr("lognumber"));
			int i =0;
			for(Object jdobject : JDlogistics) {
				JSONObject jdObject = (JSONObject) jdobject;
				Record jdrecord = new Record().set("opeTime", jdObject.get("opeTime")).set("opeRemark", jdObject.get("opeRemark"));
				jdlogistics.add(i, jdrecord);
				i++;
			}
			
		}
		List<Record> logilist = Db.find("select WorkCode,OpDateTime,OpDescription,OrderStatusCode from f_logistics where WorkCode =? order by OpDateTime desc", code);
		
		
		String dgsp = Db.queryStr("select GROUP_CONCAT(b.name) from f_order_detail a LEFT JOIN f_flower_pro b on a.fpid = b.id where a.ordercode = ? and a.type <> 3", orderinfo.getStr("ordercode"));
		List<Record> picilist = Db.find("SELECT a.code,a.number,c.fname FROM f_order_info a LEFT JOIN f_order_pro b ON a.id = b.oid "
					+ "LEFT JOIN f_product c ON b.pid = c.id WHERE a.ordercode = ? AND b.type=0 AND a.state>=2 ORDER BY a.code", orderinfo.getStr("ordercode"));
		
		Record picivase = Db.findFirst("SELECT a.code,a.number,c.name from f_order_info a LEFT JOIN f_order_pro b on a.id = b.oid "
					+ "LEFT JOIN f_flower_pro c on b.pid = c.id where a.state>=2 and b.type=1 and a.ordercode = ?", orderinfo.getStr("ordercode"));
		String vasegive = "";
		if(picivase != null){
			for(Record pici : picilist){
				if(pici.getStr("number").equals(picivase.getStr("number"))){
					vasegive = picivase.getStr("name");
					pici.set("fname", pici.getStr("fname")+","+vasegive);
				}
			}
		}
		//赠品
		Record givepro = Db.findFirst("SELECT a.code,a.number,c.name from f_order_info a LEFT JOIN f_order_pro b on a.id = b.oid "
				+ "LEFT JOIN f_flower_pro c on b.pid = c.id where a.state>=2 and b.type=2 and a.ordercode = ?", orderinfo.getStr("ordercode"));
		if(givepro != null){
			for(Record pici : picilist){
				if(pici.getStr("number").equals(givepro.getStr("number"))){
					vasegive += ("（首单赠品）"+givepro.getStr("name"));
					pici.set("fname", pici.getStr("fname")+","+vasegive);
				}
			}
		}
		
		map.put("ptid", ptid);
		map.put("picilist", picilist);	// 派单历史
		map.put("logilist", logilist);	// 物流轨迹
		map.put("jdlogilist", jdlogistics);	// 物流轨迹
		map.put("orderinfo", orderinfo);
		map.put("order_jt", order_jt);
		map.put("prolist", prolist);	// 派单产品集合
		map.put("dgsp", dgsp);			// 订购商品
		return map;
	}
	
	// 获取已送过的花并排除
	public static List<Record> getHasGet(List<Record> proList, String ordercode){
		if(proList.size() > 0){
			// 获取已送出的花束(花材id集合)
			List<Record> list = Db.find("SELECT GROUP_CONCAT(c.fid ORDER BY c.fid ASC) as gfid FROM f_order_info a LEFT JOIN f_order_pro b ON a.id=b.oid LEFT JOIN f_product_info c ON b.pid=c.pid WHERE b.type=0 AND a.ordercode=? GROUP BY a.number", ordercode);
			String idStr = new String();
			for(int i=0;i<proList.size();i++){
				if(i==0){
					idStr += proList.get(i).getInt("id");
				}else{
					idStr += "," + proList.get(i).getInt("id");
				}
			}
			proList = Db.find("SELECT pid as id,GROUP_CONCAT(fid ORDER BY fid ASC) AS gfid FROM f_product_info WHERE pid IN (" + idStr + ") GROUP BY pid");
			for(Iterator<Record> it = proList.iterator();it.hasNext();){	// 匹配已送出的花束并排除
				Record pro = it.next();
				for(Record hg : list){
					if(pro.getStr("gfid").equals(hg.getStr("gfid"))){
						it.remove();
						break;
					}
				}
			}
			
		}
		for(Record r : proList){
			String name = Db.findById("f_product", r.getInt("id")).getStr("fname");
			r.set("fname", name);
		}
		return proList;
	}
	
	
	// 获取会员
	public static Page<Record> getAccount(int pageno, int pagesize, int state, int isbuy, int isgive, String code, int gid, String time_a, String time_b, String aid,String nick,int isfans){
		String sqlExceptSelect = " from f_account where 1=1";
		if(state != 9){
			sqlExceptSelect += " and state =" + state;
		}
		if(isbuy != 9){
			sqlExceptSelect += " and isbuy =" + isbuy;
		}
		if(isgive != 9){
			sqlExceptSelect += " and isgive =" + isgive;
		}
		if(code != null && code != ""){
			sqlExceptSelect += " and tel like '%"+code+"%'";
		}
		if(gid != 9999){
			sqlExceptSelect += " and gid = " + gid;
		}
		if(isfans!=10){
			sqlExceptSelect+=" and isfans= "+isfans;
		}
		if(aid != null && aid != ""){
			sqlExceptSelect += " and id = " + aid;
		}
		if(nick != null && nick != ""){
			sqlExceptSelect += " and nick like '%" + nick+ "%'";
		}
		if(time_a == null && time_b == null){
			sqlExceptSelect += "";
		}else{
			if(time_a != "" && "".equals(time_b)){
				sqlExceptSelect += " and ctime >='"+time_a+"'";
			}else if("".equals(time_a) && time_b != ""){
				sqlExceptSelect += " and ctime <='"+time_b+" 23:59:59'";
			}else if(time_a != "" &&time_b != ""){
				sqlExceptSelect += " and ctime between '"+time_a+"' and '"+time_b+" 23:59:59'";
			}
		}
		sqlExceptSelect += " order by ctime desc";
		
		
		Page<Record> page = Db.paginate(pageno, pagesize, "select wallet,id,gid,nick,isfans,headimg,tel,ctime,isbuy,isgive,state,grade,substring(tjid,3) as tjid", sqlExceptSelect);
		String groups = HttpUtils.get(Constant.getHost + "/weixin/getGroups");
		Gson gson = new Gson();
		List<Map<String, Object>> gl = gson.fromJson(groups, ArrayList.class);
		for(Record account : page.getList()){
			if (account.getStr("tjid")!=null) {
				Record tj = Db.findById("f_account", Integer.parseInt(account.getStr("tjid")));
				if(tj!=null){
				if(tj.getStr("nick")!=null){
				account.set("tjnick", tj.getStr("nick"));
				}
				if(tj.get("tel")!=null){
				account.set("tjtel", tj.get("tel"));
				}
				}
			}
			for(Map<String, Object> g : gl){
				if(account.getInt("gid").equals(((Double) g.get("id")).intValue())){
					account.set("groupname", g.get("name"));
				}
			}
		}
		return page;
	}
	
	// 获取会员
		public static Page<Record> getAccount(int pageno, int pagesize){
			String sqlExceptSelect = " from f_account where 1=1 and DATE_ADD(ctime,INTERVAL 2 day)>NOW() ";
			sqlExceptSelect += " order by ctime desc";
			Page<Record> page = Db.paginate(pageno, pagesize, "select wallet,id,gid,nick,isfans,headimg,tel,ctime,isbuy,isgive,state,grade,substring(tjid,3) as tjid", sqlExceptSelect);
			String groups = HttpUtils.get(Constant.getHost + "/weixin/getGroups");
			Gson gson = new Gson();
			List<Map<String, Object>> gl = gson.fromJson(groups, ArrayList.class);
			for(Record account : page.getList()){
				if (account.getStr("tjid")!=null) {
					Record tj = Db.findById("f_account", Integer.parseInt(account.getStr("tjid")));
					if(tj!=null){
					if(tj.getStr("nick")!=null){
					account.set("tjnick", tj.getStr("nick"));
					}
					if(tj.get("tel")!=null){
					account.set("tjtel", tj.get("tel"));
					}
					}
				}
				for(Map<String, Object> g : gl){
					if(account.getInt("gid").equals(((Double) g.get("id")).intValue())){
						account.set("groupname", g.get("name"));
					}
				}
			}
			return page;
		}
	//获取花籽信息
	public static Page<Record> getFlowerseed(int pageno, int pagesize, String remarks,String aid,String nick,String tel,String ctime_start,String ctime_end,int state){
		String sqlExceptSelect = " from f_flowerseed a LEFT JOIN f_account d on a.aid = d.id where 1=1 and  DATE_ADD(a.ctime, INTERVAL 2 MONTH) >= CURDATE()  ";
		if(StrKit.notBlank(remarks) ){
			sqlExceptSelect += String.format(" and a.remarks = '%s'", remarks);
		}
		if(StrKit.notBlank(aid)){
			sqlExceptSelect += String.format(" and aid = %s", aid);
		}
		if(StrKit.notBlank(nick)){
			sqlExceptSelect += String.format(" and nick = '%s'", nick);
		}
		if(StrKit.notBlank(tel)){
			sqlExceptSelect += String.format(" and tel = '%s'", tel);
		}
		if(StrKit.notBlank(ctime_start) ){
			sqlExceptSelect +=   String.format(" and left(a.ctime,10) >= '%s'", ctime_start);
		}
		if(StrKit.notBlank(ctime_end) ){
			sqlExceptSelect +=   String.format(" and left(a.ctime,10) <= '%s'", ctime_end);
		}
		if(state != 9 ){
			sqlExceptSelect += String.format(" and a.state= %d", state);
		}
		sqlExceptSelect += " order by a.ctime asc"; 
		Page<Record> page = Db.paginate(pageno,pagesize,"select a.remarks,a.aid,d.nick,d.tel,a.ctime,a.state,a.username",sqlExceptSelect);	
		return page;
	}	
	// 获取意见反馈
	public static Page<Record> getFeedback(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "SELECT a.id,b.nick,a.title,a.info,a.ctime", "FROM f_feedback a LEFT JOIN f_account b ON a.aid=b.id order by a.id desc");
	}
	
	// 获取会员电话
	public static Page<Record> getnumber (int pageno, int pagesize, String code){
		String sqlExceptSelect = "FROM f_account where 1=1 ";
		if(code != null){
			sqlExceptSelect += " and tel like '%" + code + "%'";
		}
		return Db.paginate(pageno, pagesize, "SELECT id,nick,headimg,tel,ctime,state ", sqlExceptSelect);
	}
	
	// 冻结用户
	public static boolean freezeAccount(int id, int state){
		Record record = new Record();
		record.set("state", state);
		record.set("id", id);
		return Db.update("f_account", record);
	}
	
	// 获取管理员
	public static Page<Record> getAdmin(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select a.id,a.username,a.rid,a.ctime,b.name", "from f_admin a left join f_role b on a.rid=b.id");
	}
		
	// 获取角色
	public static Page<Record> getRole(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select id,name", "from f_role");
	}
	
	// 保存权限
	public static boolean saveAuthority(Integer id, String mid, int rid){
		Record record = new Record();
		record.set("rid", rid);
		if(rid == 1){
			mid = Db.queryStr("SELECT GROUP_CONCAT(id) FROM f_menu");
		}
		record.set("mid", mid);
		if(id == null){
			return Db.save("f_authority", record);
		}else{
			record.set("id", id);
			return Db.update("f_authority", record);
		}
	}
	
	// 获取花材分类
	public static Page<Record> getFlowerType(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select id,name,ctime,jh", " from f_flower_type_view");
	}
	
	// 获取花材分类
	public static Page<Record> getFlower(int pageno, int pagesize, String ftype, String fname){
		String sqlExceptSelect = " from f_flower_view a left join f_flower_type b on a.tid=b.id where 1=1";
		if(ftype != ""){
			sqlExceptSelect += " and b.name like '%"+ ftype +"%'";
		}
		if(fname != ""){
			sqlExceptSelect += " and a.name like '%"+ fname +"%'";
		}
		return Db.paginate(pageno, pagesize, "select a.id,b.name as tname,a.name,a.imgurl,a.cid,a.ctime,a.state,a.ftype", sqlExceptSelect);
	}
	
	// 获取花材分类(仅类别查找)
	public static Page<Record> getFlowerftype(int pageno, int pagesize, String ftype){
		String sqlExceptSelect = " from  f_flower_type where 1=1";
		if(ftype != ""){
			sqlExceptSelect += " and name like '%"+ ftype +"%'";
		}
		return Db.paginate(pageno, pagesize, "select id,name,ctime,jh", sqlExceptSelect);
	}
	
	// 花材上架与下架
	public static boolean upordownFlower(int id, int state){
		Record record = new Record();
		record.set("state", state);	// 【1上架，0下架】
		record.set("id", id);
		return Db.update("f_flower", record);
	}

	public static void setJhFlower(int id, int jh){
		Db.update("update f_flower set jh=? where id=?", jh, id);
	}
	
	// 获取商品分类
	public static Page<Record> getFlowerPro(int pageno, int pagesize,String ptid,int state){
		String strSelect = "SELECT a.code_name,b.ptid,b.id,b.name,b.imgurl,b.describe,b.price,b.ctime,b.state,case when b.state = 0 then '在售' when b.state = 1 then '售罄，不显示' when b.state = 2 then '售罄，显示' end 'isstate'  ,b.type,b.priority,b.x,b.y,b.seeds,b.ptCount,b.ptHours,b.ptPrice,"
				+ " case when b.isinfirst=1 then '是' else '否' end 'isinfirst',"
				+ " case when b.allowSY=1 then '允许' else '不允许' end 'allowSY',"
				+ " case when b.fpc=0 then '订阅级' when b.fpc=1 then '送花级' when b.fpc=2 then '节日级' end 'fpc',shareTitle,shareDes,dmlj,reachtype,case when allownew=0 then '不限制' else '只允许新用户参团' end 'allownew',"
				+ " case when b.isptFree=0 then '不免单' else '免单支付0.01' end 'isptFree',"
				+ " case when b.allowRefund=0 then '不支持退款' else '支持退款' end 'allowRefund',"
				+ " case when b.allowRobotis=0 then '不允许' else '允许' end 'allowRobotis',"
				+ " fundper,ptNum,jsMoney,"
				+ " case when b.isAlonePage=0 then '不是' else '是' end  'isAlonePage'";
		String strWhere = "FROM f_dictionary a LEFT JOIN f_flower_pro b ON a.code_value=b.ptid WHERE a.code_key='ptid' AND b.id IS NOT NULL ";
		if(state!=10){
			strWhere +=" and b.state="+state;
		}
		
		Page<Record> flopros = null;		
		if(ptid.equals("0")==false){
			strWhere+=" and a.code_value=?  ";
			flopros = Db.paginate(pageno, pagesize,strSelect,strWhere,ptid);
		}else{
			strWhere+=" ORDER BY a.code_key,b.ptid ";
			flopros = Db.paginate(pageno, pagesize, strSelect, strWhere);
		}
		
		List<Record> flos = flopros.getList();
		//双品、多品、多双交替有轮播图
		for (Record flo : flos) {
			if(flo.getInt("ptid")==1||flo.getInt("ptid")==2||flo.getInt("ptid")==203){
				String imgs = flo.getStr("imgurl");
				if(imgs.indexOf("-")!= -1){
					String[] img = imgs.split("-");
					flo.set("imgurl", img[0]);
				}else{
					flo.set("imgurl", imgs);
				}
			}
		}
		return flopros;
	}
	
	// 获取花票主题
	public static Page<Record> getCash(int pageno, int pagesize){
		Page<Record> page = Db.paginate(pageno, pagesize, "select etime,id,name,ltime,ctime,state,type,maxcount,sendcount ", "from f_cashtheme where type = 0 order by id desc");
		List<Record> themes = Db.find("select id,name,ltime,ctime,state,type,maxcount,sendcount from f_cashtheme where type = 1");
		for(int i=0;i<themes.size();i++){
			page.getList().add(i, themes.get(i));	
		}
		return page;
	}
		
	// 获取花票统计
	public static Page<Record> getCashsatistic(int pageno, int pagesize, int themeid, String pushid, String aid,int state){
		String sqlExceptSelect = "FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid = b.id LEFT JOIN f_cashtheme c ON b.tid = c.id where 1=1 ";
		if(themeid != 9999){
			sqlExceptSelect += " and c.id = "+themeid;
		}
		if(pushid != null && pushid != ""){
			sqlExceptSelect += " and a.pushid = "+pushid;
		}
		if(aid != null && aid != ""){
			sqlExceptSelect += " and a.aid = "+aid;
		}
		if(state!=9){
			sqlExceptSelect+=" and a.state="+state;
		}
		sqlExceptSelect+=" order by a.time_a desc" ;
		Page<Record> page = Db.paginate(pageno, pagesize, "SELECT c.name,a.time_a,a.aid,a.pushid,a.aid,a.state,a.code", sqlExceptSelect);
		for(Record statis: page.getList()){
			String aname = Db.queryStr("select nick from f_account where id=?", statis.getInt("aid"));
			String tname = Db.queryStr("select username from f_admin where id=?", statis.getInt("pushid"));
			statis.set("aname", aname);
			statis.set("tname", tname);
		}
		return page;
	}
	
	// 获取花票统计
	public static List<Record> getCashthemes(){
		List<Record> themes = Db.find("select id,name from f_cashtheme");
		return themes;
	}
	
	// 获取活动列表
	public static Page<Record> getActivity(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select id,name,money,benefit,time_a,time_b,ctime,state", "from f_activity where state = 0 order by id desc");
	}
	
	// 保存商品信息
	public static Map<String, Object> saveProduct(int typeid, String flist, String fname, String sname, Integer iid, Integer fpid,int app,int style,int priority,int appoint){
		responseMap = new HashMap<>();
		boolean R = false;
		Msg = "操作失败";
		Map<String, Object> map = DeliveryDateUtil.makeCode();
		boolean result = (boolean) map.get("result");
		dateCode = (String) map.get("datecode");
		typeId = typeid;
		fList = flist;
		fName = fname;
		sName = sname;
		iId = iid;
		fPid = fpid;
		aPp=app; //用途
		sTyle=style;
		pRiority=priority;//
		aPpoint=appoint;
		//挑单之前可以添加，挑单之后不允许添加
		Number tdNum = Db.queryNumber("select count(1) from f_tiaodan where code=?", dateCode);
		/*if(tdNum.intValue() != 0){
		     result=false;
			 Msg = "批次"+dateCode+"挑单已完成,不允许再添加产品";
		}*/
		
		// 判断是否在可操作时间范围内
		if(result){
			R = Db.tx(new IAtom() {
				@Override
				public boolean run() throws SQLException {
					boolean save_result = false;
					String ystr = typeId==4?" and b.type=5":" and b.type<>5";
					Number yfh = Db.queryNumber("select count(1) from f_order_info a LEFT JOIN f_order b ON a.ordercode=b.ordercode where not EXISTS (select c.ordercode from f_order as  c  where a.ordercode=c.ordercode and c.type in(3,43)) and a.code=? and a.state=2"+ystr, dateCode);	// 已发货
					if(yfh.intValue() == 0 || typeId ==4){
						String[] list = fList.split(",");
						Record product = new Record();
						/*if(typeId == 4){
							dateCode = Db.queryStr("select code_value from f_dictionary where code_key = 'holiday'");
						}*/
						product.set("code", dateCode);
						product.set("type", typeId);
						if(Db.queryNumber("select count(1) from f_product where fname = ? and code=? and appoint = ? ", fName,dateCode,aPpoint).intValue() > 0){
							Msg = "同一批次产品名称重复";
							return save_result;
						}
						if(Db.queryNumber("select count(1) from f_product where sname = ? and code=? and appoint = ? ", sName,dateCode,aPpoint).intValue() > 0){
							Msg = "同一批次产品简称重复";
							return save_result;
						}
						product.set("fname", fName);
						product.set("sname", sName);
						product.set("app", aPp);
						product.set("style", sTyle);
						product.set("priority", pRiority);
						product.set("appoint", aPpoint);
						
					
						// 商品分类
						List<Record> fpl = Db.find("SELECT id,price FROM f_flower_pro where state = 0 AND ptid = ?", typeId);
						
						//送花系列
						/*if(typeId == 3){
							if(!validSongHua(null, iId, fPid, dateCode)){
								Msg = "适赠类型已存在";
								return save_result;
							}else{
								for(Record fp : fpl){
									// 添加分类ID与价格
									int id=fp.getInt("id");
									if(id == fPid){
										product.set("fpid", fp.getInt("id"));
										product.set("price", fp.getDouble("price"));
										break;
									}
								}
							}
						}
						//节日系列
						else if(typeId == 4){
							fpl = Db.find("SELECT id,price FROM f_flower_pro where  ptid in(select hPtid from f_holiday where id not in(4,5))");
							for(Record fp : fpl){
								// 添加分类ID与价格
								int id=fp.getInt("id");
								if( id== fPid){
									product.set("fpid", fp.getInt("id"));
									product.set("price", fp.getDouble("price"));
									break;
								}
							}
						}
						// 
						else if(typeId == 5){
							fpl = Db.find("SELECT id,price FROM f_flower_pro where  ptid=201");
							for(Record fp : fpl){
								// 添加分类ID与价格
								int id=fp.getInt("id");
								if(id == fPid){
									product.set("fpid", fp.getInt("id"));
									product.set("price", fp.getDouble("price"));
									break;
								}
							}
						}else if(typeId == 6){//闪购
							fpl = Db.find("SELECT id,price FROM f_flower_pro where  ptid in(select hPtid from f_holiday where id=4)");
							for(Record fp : fpl){
								// 添加分类ID与价格
								int id=fp.getInt("id");
								if(id == fPid){
									product.set("fpid", fp.getInt("id"));
									product.set("price", fp.getDouble("price"));
									break;
								}
							}
						}else if(typeId == 7){//主题花
							fpl = Db.find("SELECT id,price FROM f_flower_pro where  ptid in(select hPtid from f_holiday where id =5)");
							for(Record fp : fpl){
								// 添加分类ID与价格
								int id=fp.getInt("id");
								if(id == fPid){
									product.set("fpid", fp.getInt("id"));
									product.set("price", fp.getDouble("price"));
									break;
								}
							}
						}else if(typeId==8){
							fpl = Db.find("SELECT id,price FROM f_flower_pro where  ptid=202");
							for(Record fp : fpl){
								// 添加分类ID与价格
								int id=fp.getInt("id");
								if(id == fPid){
									product.set("fpid", fp.getInt("id"));
									product.set("price", fp.getDouble("price"));
									break;
								}
							}
						}else if(typeId==9){
							fpl = Db.find("SELECT id,price FROM f_flower_pro where  ptid=11");
							for(Record fp : fpl){
								// 添加分类ID与价格
								int id=fp.getInt("id");
								if(id == fPid){
									product.set("fpid", fp.getInt("id"));
									product.set("price", fp.getDouble("price"));
									break;
								}
							}
						}
						else{
							product.set("fpid", fpl.get(0).getInt("id"));
							product.set("price", fpl.get(0).getDouble("price"));
						}*/
						if (fpid==null) {
							product.set("fpid", fpl.get(0).getInt("id"));
							product.set("price", fpl.get(0).getDouble("price"));
						}else {
							Record fp = Db.findFirst("SELECT id,price FROM f_flower_pro where  id=?",fPid);
							product.set("fpid", fPid);
							product.set("price",fp.getDouble("price"));
						}
						
						product.set("iid", iId);
						// 添加产品成功
						if(Db.save("f_product", product)){
							Long pid = product.getLong("id");
							for(String flower : list){
								String[] strings=flower.split("-");
								int fid = Integer.parseInt(strings[0]);
								int num = Integer.parseInt(strings[1]);
								int ftype = Integer.parseInt(strings[2]);
								Record proinfo = new Record();
								proinfo.set("pid", pid); //f_product 自增ID
								proinfo.set("fid", fid); //花材ID
								proinfo.set("num", num); //花材数量
								proinfo.set("ftype", ftype);
								// 添加商品详情
								Db.save("f_product_info", proinfo);
							}
							Msg = "操作成功";
							save_result = true;
						}
					}else{
						Msg = "该批次已发货";
					}
					return save_result;
				}
			});
		}else{
			Msg = "不在可操作时间内";
		}
		responseMap.put("R", R);
		responseMap.put("msg", Msg);
		return responseMap;
	}
	
	// 送花系列添加商品验证
	public static boolean validSongHua(Integer id, int iid, int fpid, String datecode){
		String sql = "SELECT COUNT(1) FROM f_product WHERE TYPE=3 AND fpid=? AND iid=? AND CODE=?";
		if(id != null){
			sql += " AND id!=?";
			Number number = Db.queryNumber(sql, fpid, iid, datecode, id);
			return number.intValue()>0?false:true;
		}else{
			Number number = Db.queryNumber(sql, fpid, iid, datecode);
			return number.intValue()>0?false:true;
		}
	}
	
	// 修改商品信息
	public static Map<String, Object> editProduct(int id, int typeid, String flist, String fname, String sname, Integer iid, Integer fpid,int app,int style,int priority,int appoint){
		responseMap = new HashMap<>();
		boolean R = false;
		Msg = "操作失败";
		Map<String, Object> map = DeliveryDateUtil.makeCode();
		boolean result = (boolean) map.get("result");
		dateCode = (String) map.get("datecode");
		Id = id;
		typeId = typeid;
		fList = flist;
		fName = fname;
		sName = sname;
		iId = iid;
		fPid = fpid;
		aPp=app;
		sTyle=style;
		pRiority=priority;
		aPpoint=appoint;
		
		//挑单之前可以修改，挑单之后不允许修改
		/*Number tdNum = Db.queryNumber("select count(1) from f_tiaodan where code=?", dateCode);
		if(tdNum.intValue() != 0){
			result=false;
			Msg = "批次"+dateCode+"挑单已完成,不允许再做修改";
		}*/
		/*Number yfh = Db.queryNumber("select count(1) from f_order_info where code=? and state=2", dateCode);
		if(yfh.intValue() != 0){
			result=false;
			Msg = "批次"+dateCode+"已发货,不允许再做修改";
		}*/
		// 判断是否在可操作时间范围内
//		if(result){
			R = Db.tx(new IAtom() {
				@Override
				public boolean run() throws SQLException {
					boolean ch_result = false;
					Record pi = Db.findById("f_product", Id);
					// 判断是否是本批次产品
					boolean isho = pi.getStr("code").equals(Db.queryStr("select code_value from f_dictionary where code_key = 'holiday'"));
					// 判断是否属于最新的发货的两个批次产品
					boolean isnew2 = false;
					List<Record>isnew2list = Db.find(" select distinct(code) from f_order_info  a  where  not EXISTS (select b.ordercode from f_order as  b  where a.ordercode=b.ordercode and b.type in(3,43)) order by a.code DESC LIMIT 2");
					for(Record list:isnew2list) {
						if (list.get("code").equals(pi.getStr("code"))) {
							isnew2 = true;
						}
					} 
					if(typeId == 4){
						dateCode = Db.queryStr("select code_value from f_dictionary where code_key = 'holiday'");
					}
					if(dateCode.equals(pi.getStr("code")) || (typeId == 4 && isho || isnew2)){
//						Number yfh = Db.queryNumber("select count(1) from f_order_info where code=? and state=2", dateCode);	// 已发货
//						if(yfh.intValue() == 0){
							String[] list = fList.split(",");
							Record product = new Record();
							product.set("id", Id);
							product.set("type", typeId);
							if(Db.queryNumber("select count(1) from f_product where id != ? and fname = ? and code=? and appoint = ? ", Id, fName,dateCode,aPpoint).intValue() > 0){
								Msg = "同一批次产品名称重复";
								return ch_result;
							}
							if(Db.queryNumber("select count(1) from f_product where id != ? and sname = ? and code=? and appoint = ?", Id, sName,dateCode,aPpoint).intValue() > 0){
								Msg = "同一批次产品简称重复";
								return ch_result;
							}
							product.set("fname", fName);
							product.set("sname", sName);
							product.set("app", aPp);
							product.set("style", sTyle);
							product.set("priority", pRiority);
							product.set("appoint", aPpoint);
							Db.batch("delete from f_product_info where pid=?", new Object[][]{{Id}}, 10);	// 删除旧商品详情数据
							
							// 商品
							List<Record> fpiList = Db.find("SELECT id,price FROM f_flower_pro where state = 0 AND ptid = ?", typeId);
							/*if(typeId == 3){
								if(!validSongHua(Id, iId, fPid, dateCode)){
									Msg = "适赠类型已存在";
									return ch_result;
								}else{
									for(Record fp : fpiList){
										// 添加分类ID与价格
										if(fp.getInt("id").equals(fPid)){
											product.set("fpid", fp.getInt("id"));
											product.set("price", fp.getDouble("price"));
											break;
										}
									}
								}
							}else if(typeId == 4){
								fpiList = Db.find("SELECT id,price FROM f_flower_pro where state = 0 and ptid in(select hPtid from f_holiday)");
								for(Record fp : fpiList){
									// 添加分类ID与价格
									if(fp.getInt("id") == fPid){
										product.set("fpid", fp.getInt("id"));
										product.set("price", fp.getDouble("price"));
										break;
									}
								}
							}else if(typeId == 5){
								fpiList = Db.find("SELECT id,price FROM f_flower_pro where state = 0 and ptid=201");
								for(Record fp : fpiList){
									// 添加分类ID与价格
									if(fp.getInt("id") == fPid){
										product.set("fpid", fp.getInt("id"));
										product.set("price", fp.getDouble("price"));
										break;
									}
								}
							}else if(typeId == 6){//闪购
								fpiList = Db.find("SELECT id,price FROM f_flower_pro where  ptid in(select hPtid from f_holiday where id=4)");
								for(Record fp : fpiList){
									// 添加分类ID与价格
									if(fp.getInt("id") == fPid){
										product.set("fpid", fp.getInt("id"));
										product.set("price", fp.getDouble("price"));
										break;
									}
								}
							}else if(typeId == 7){//主题花
								fpiList = Db.find("SELECT id,price FROM f_flower_pro where  ptid in(select hPtid from f_holiday where id =5)");
								for(Record fp : fpiList){
									// 添加分类ID与价格
									if(fp.getInt("id") == fPid){
										product.set("fpid", fp.getInt("id"));
										product.set("price", fp.getDouble("price"));
										break;
									}
								}
							}else if(typeId==8){
								fpiList = Db.find("SELECT id,price FROM f_flower_pro where  ptid=202");
								for(Record fp : fpiList){
									// 添加分类ID与价格
									if(fp.getInt("id") == fPid){
										product.set("fpid", fp.getInt("id"));
										product.set("price", fp.getDouble("price"));
										break;
									}
								}
							}
							else if(typeId==9){
								fpiList = Db.find("SELECT id,price FROM f_flower_pro where  ptid=11");
								for(Record fp : fpiList){
									// 添加分类ID与价格
									if(fp.getInt("id") == fPid){
										product.set("fpid", fp.getInt("id"));
										product.set("price", fp.getDouble("price"));
										break;
									}
								}
							}
							else{
								product.set("fpid", fpiList.get(0).getInt("id"));
								product.set("price", fpiList.get(0).getDouble("price"));
							}*/
							if (fpid==null) {
								product.set("fpid", fpiList.get(0).getInt("id"));
								product.set("price", fpiList.get(0).getDouble("price"));
							}else {
								Record fp = Db.findFirst("SELECT id,price FROM f_flower_pro where  id=?",fPid);
								product.set("fpid", fPid);
								product.set("price",fp.getDouble("price"));
							}
							product.set("iid", iId);
							// 修改产品成功
							if(Db.update("f_product", product)){
								for(String flower : list){
									String[] strings=flower.split("-");
									int fid = Integer.parseInt(strings[0]);
									int num = Integer.parseInt(strings[1]);
									int ftype = Integer.parseInt(strings[2]);
									Record proinfo = new Record();
									proinfo.set("pid", Id); //f_product 自增ID
									proinfo.set("fid", fid); //花材ID
									proinfo.set("num", num); //花材数量
									proinfo.set("ftype", ftype);
									// 添加商品详情
									Db.save("f_product_info", proinfo);
								}
								ch_result = true;
								Msg = "操作成功";
							}
//						}
					}else{
						Msg = "该产品既不属于最近两个已发货批次,也不属于其他未挑单完成批次,不能修改";
					}
					return ch_result;
				}
			});
//		}else{
//			Msg = "不在可操作时间内,或者已经挑单完成,不允许再做修改";
//		}
		responseMap.put("R", R);
		responseMap.put("msg", Msg);
		return responseMap;
	}
	
	// 删除产品
	public static boolean delProduct(int id){
		boolean R = false;
		
		Number tdNum = Db.queryNumber("select count(1) from f_tiaodan where code=?", dateCode);
		if(tdNum.intValue() != 0){
		     R=false;
		     return R;//挑单已完成,不允许再删除产品
		}
		
		Map<String, Object> map = DeliveryDateUtil.makeCode();
		boolean result = (boolean) map.get("result");
		String datecode = (String) map.get("datecode");
		Record pi = Db.findById("f_product", id);
		if(result){
			// 判断是否是本批次产品
			if(datecode.equals(pi.getStr("code")) && (int)pi.get("type")!=4){
				Number yfh = Db.queryNumber("select count(1) from f_order_info a where not EXISTS (select b.ordercode from f_order as  b  where a.ordercode=b.ordercode and b.type in(3,43)) and a.code=? and a.state=2", datecode);	// 已发货
				if(yfh.intValue() == 0){
					if(Db.deleteById("f_product", id)){
						Db.batch("delete from f_product_info where pid = ?", new Object[][]{{id}}, 10);	// 删除旧商品详情数据
						Db.update("DELETE FROM f_order_info USING f_order_info,f_order WHERE f_order_info.ordercode = f_order.ordercode AND f_order.type<>5 and f_order.type<>3 and f_order.type<>43  AND f_order_info.code =?", datecode);	// 删除已生成的物流配单
						R = true;
					}
				}
			}
		}
		String holicode = Db.queryStr("select code_value from f_dictionary where code_key = 'holiday'");
		if((int)pi.get("type")==4 && pi.get("code").equals(holicode)){
			Number yfh = Db.queryNumber("select count(1) from f_order_info a where not EXISTS (select b.ordercode from f_order as  b  where a.ordercode=b.ordercode and b.type in(3,43)) and  a.code=?  and a.state=2", holicode);	// 已发货
			if(yfh.intValue() == 0){
				if(Db.deleteById("f_product", id)){
					Db.batch("delete from f_product_info where pid = ?", new Object[][]{{id}}, 10);	// 删除旧商品详情数据
					Db.update("DELETE FROM f_order_info USING f_order_info,f_order WHERE f_order_info.ordercode = f_order.ordercode AND f_order.type=5 AND f_order_info.code =?", holicode);	// 删除已生成的物流配单
					R = true;
				}
			}
		}
		return R;
	}
	
	
	
	
	
	/**
	 * 获取产品分页
	 * @param type 1:双品系列  2：多品系列 3：送花系列 4：节日送花5定制花束
	 * @param pageno
	 * @param pagesize
	 * @param code 批次
	 * @param minprice 最低价格
	 * @param maxprice 最高价格
	 * @return
	 */
	public static Page<Record> getProduct(int type, int pageno, int pagesize,String code,String minprice,String maxprice){
		String sqlSelect="";//需查询的列
		String sqlExceptSelect ="";//From子句
		String sqlWhere="";//附加的Where条件
		String sqlOrderBy="";//排序
		//如果是首次打开网页,现在最近的批号
		if((code==null || code.equals(""))
			&&(minprice == null||minprice.equals(""))
			&&(maxprice == null||maxprice.equals(""))){
			String maxCode=Db.queryStr("select max(code) 'maxCode' from f_product where type="+type);
			sqlWhere=" and  a.code="+maxCode;
			
		}else{
			if(code!=null && !code.equals("") ){
				sqlWhere += " and a.code like '" + code + "%'";
			}
			if(minprice != null&&!minprice.equals("")&&maxprice != null&&!maxprice.equals("")){
				sqlWhere += " and cost between " + minprice + " and "+maxprice;
			}
		}
		if(type==9){
			sqlExceptSelect = " FROM f_product a LEFT JOIN f_flower_pro b ON a.fpid=b.id LEFT JOIN ("+
					" SELECT o.code, p.pid,sum(p.num*q.price) cost FROM f_product o,f_product_info p,(select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code) q WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code"+
					" AND  o.type = 9  GROUP BY o.code,p.pid ) d ON a.code = d.code AND a.id = d.pid  WHERE a.type= 9";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,ROUND(IFNULL(d.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style',a.priority";
			sqlOrderBy=" order by a.priority desc, a.code desc,d.cost desc";
		}
		else if(type==8)	{
			sqlExceptSelect = " FROM f_product a LEFT JOIN f_flower_pro b ON a.fpid=b.id LEFT JOIN ("+
					" SELECT o.code, p.pid,sum(p.num*q.price) cost FROM f_product o,f_product_info p,(select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code) q WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code"+
					" AND  o.type = 8  GROUP BY o.code,p.pid ) d ON a.code = d.code AND a.id = d.pid  WHERE a.type= 8";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,ROUND(IFNULL(d.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style',a.priority";
			sqlOrderBy=" order by a.priority desc, a.code desc,ROUND(IFNULL(d.cost,0),2) desc";
		}else if(type==7)	{
			sqlExceptSelect = " FROM f_product a LEFT JOIN f_flower_pro b ON a.fpid=b.id LEFT JOIN ("+
					" SELECT o.code, p.pid,sum(p.num*q.price) cost FROM f_product o,f_product_info p,(select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code) q WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code"+
					" AND  o.type = 7  GROUP BY o.code,p.pid ) d ON a.code = d.code AND a.id = d.pid  WHERE a.type= 7";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,ROUND(IFNULL(d.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style',a.priority";
			sqlOrderBy=" order by a.priority desc, a.code desc,d.cost desc";
		}else if(type==6){
			sqlExceptSelect = " FROM f_product a LEFT JOIN f_flower_pro b ON a.fpid=b.id LEFT JOIN ("+
					" SELECT o.code, p.pid,sum(p.num*q.price) cost FROM f_product o,f_product_info p,(select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code) q WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code"+
					" AND  o.type = 6  GROUP BY o.code,p.pid ) d ON a.code = d.code AND a.id = d.pid  WHERE a.type= 6";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,ROUND(IFNULL(d.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style',a.priority";
			sqlOrderBy=" order by a.priority desc, a.code desc,d.cost desc";
		}else if(type == 5){
			sqlExceptSelect = " FROM f_product a LEFT JOIN f_flower_pro b ON a.fpid=b.id LEFT JOIN ("+
					" SELECT o.code, p.pid,sum(p.num*q.price) cost FROM f_product o,f_product_info p,(select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code) q WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code"+
					" AND  o.type = 5  GROUP BY o.code,p.pid ) d ON a.code = d.code AND a.id = d.pid  WHERE a.type= 5";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,ROUND(IFNULL(d.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style',a.priority";
			sqlOrderBy=" order by a.priority desc, a.code desc,d.cost desc";
		}else if(type == 4){
			sqlExceptSelect = " FROM f_product a LEFT JOIN f_flower_pro b ON a.fpid=b.id LEFT JOIN ("+
					" SELECT o.code, p.pid,sum(p.num*q.price)  cost FROM f_product o,f_product_info p,(select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code) q WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code"+
					" AND o.type = 4  GROUP BY o.code,p.pid ) d ON a.code = d.code AND a.id = d.pid  WHERE a.type= 4";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,ROUND(IFNULL(d.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style'";
			sqlOrderBy=" order by a.code desc,d.cost desc";
		}else if(type == 3){//送花
			sqlExceptSelect = " FROM f_product a LEFT JOIN f_flower_pro b ON a.fpid=b.id LEFT JOIN f_idoneity c ON a.iid=c.id  LEFT JOIN ("+
					" SELECT o.code, p.pid,sum(p.num*q.price) cost FROM f_product o,f_product_info p,(select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code)  q WHERE o.id = p.pid AND p.fid = q.fid AND o.code = q.code"+
					" AND o.type = 3  GROUP BY o.code,p.pid ) d ON a.code = d.code AND a.id = d.pid  WHERE a.type= 3";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,c.title,ROUND(IFNULL(d.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style'";
			sqlOrderBy=" order by a.code desc,d.cost desc";
		}else if(type==2){//多品
			sqlExceptSelect = " FROM f_product a left join f_flower_pro b on a.fpid=b.id LEFT JOIN "+
					"(select o.code,p.pid,sum(p.num*q.price) as cost from f_product o left join f_product_info p on o.id=p.pid left join (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code) q on p.fid=q.fid and o.code=q.code where o.type=2 group by o.code,p.pid)"
					+ " c ON a.code=c.code AND a.id=c.pid WHERE a.type=2";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,ROUND(IFNULL(c.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style'";
			sqlOrderBy=" order by a.code desc,c.cost desc";
		}
		else{//双品
			sqlExceptSelect = " FROM f_product a left join f_flower_pro b on a.fpid=b.id LEFT JOIN "+
			          "(select o.code,p.pid,sum(p.num*q.price) as cost from f_product o left join f_product_info p on o.id=p.pid left join (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price,code,fid from f_purchasem  group by fid,code) q on p.fid=q.fid and o.code=q.code where o.type=1 group by o.code,p.pid)"
					  + " c ON a.code=c.code AND a.id=c.pid WHERE a.type=1";
			sqlSelect="select a.id,a.code,a.type,b.name,a.fname,a.sname,a.price,ROUND(IFNULL(c.cost,0),2) cost,case a.app when 1 then '自用' when 2 then '自用' else '礼物' end 'app', case a.style when 1 then '淡雅' when 2 then '亮丽' else '中性' end 'style', case when a.appoint=1 then '首单首次' else '非首单首次' end 'appoint'";
			sqlOrderBy=" order by a.code desc,c.cost desc";
		}
		sqlExceptSelect=sqlExceptSelect+sqlWhere+sqlOrderBy;
		return Db.paginate(pageno, pagesize, sqlSelect, sqlExceptSelect);
	}
	
	
	
	// 获取花材批次
	public static Page<Record> getPriceForBatch(int pageno, int pagesize, String batchcode){
		String sqlExceptSelect = "FROM f_flower_price where 1=1";
		if(batchcode != null){
			sqlExceptSelect += " and code like '" + batchcode + "%'";
		}
		sqlExceptSelect += " group by code order by code desc";
		return Db.paginate(pageno, pagesize, "SELECT code ", sqlExceptSelect);
	}
	
	// 获取批次花材
	public static Map<String, Object> getFlowerForCode(int type){
		Map<String, Object> responseMap = new HashMap<>();
		Map<String, Object> map = DeliveryDateUtil.makeCode();
		boolean result = (boolean) map.get("result");
		String datecode = (String) map.get("datecode");
		List<Record> flowerlist = new ArrayList<>();
		
		if(result){
			if(type == 0){
				flowerlist = Db.find("SELECT a.type,b.fid,c.name FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid LEFT JOIN f_flower c ON b.fid=c.id WHERE a.code=? and (a.type=1 or a.type=2 or a.type=5) GROUP BY b.fid", datecode);
			}else{
				flowerlist = Db.find("SELECT a.type,b.fid,c.name FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid LEFT JOIN f_flower c ON b.fid=c.id WHERE a.code=? and a.type = 3 GROUP BY b.fid", datecode);
			}
			List<Record> pricelist = Db.find("select fid,price1,price2 from f_flower_price where code = ? and type = ?", datecode, type);
			for(Iterator<Record> it = flowerlist.iterator();it.hasNext();){
				Record t = it.next();
				if(type == 0){
					// (订阅)普通花材
					if(t.getInt("type")==1 || t.getInt("type")==2 ||t.getInt("type")==5){
						for(Record price : pricelist){
							Integer fid1 = t.getInt("fid");
							Integer fid2 = price.getInt("fid");
							if(fid1.equals(fid2)){
								t.set("price1", price.getDouble("price1"));
								t.set("price2", price.getDouble("price2"));
								break;
							}
						}
					}else{
						it.remove();
					}
				}else{
					// (送花)上品花材
					if(t.getInt("type")==3){
						for(Record price : pricelist){
							if(t.getInt("fid").equals(price.getInt("fid"))){
								t.set("price1", price.getDouble("price1"));
								t.set("price2", price.getDouble("price2"));
								break;
							}
						}
					}else{
						it.remove();
					}
				}
			}
		}
		responseMap.put("flowerlist", flowerlist);
		responseMap.put("datecode", datecode);
		return responseMap;
	}
	// 获取批次花材
	public static Map<String, Object> getFlowerHoliCode(){
		Map<String, Object> responseMap = new HashMap<>();
		List<Record> flowerlist = new ArrayList<>();
		String code = Db.queryStr("select code_value from f_dictionary where id=27");
		flowerlist = Db.find("SELECT a.type,b.fid,c.name FROM f_product a LEFT JOIN f_product_info b ON a.id=b.pid LEFT JOIN f_flower c ON b.fid=c.id WHERE a.code=? and a.type in(4,6,7) GROUP BY b.fid", code);
		List<Record> pricelist = Db.find("select fid,price1,price2 from f_flower_price where code = ? and type = ?", code, 2);
		for(Iterator<Record> it = flowerlist.iterator();it.hasNext();){
			Record t = it.next();
			for(Record price : pricelist){
				if(t.getInt("fid").equals(price.getInt("fid"))){
					t.set("price1", price.getDouble("price1"));
					t.set("price2", price.getDouble("price2"));
					break;
				}
			}
		}
		responseMap.put("flowerlist", flowerlist);
		responseMap.put("datecode", code);
		return responseMap;
	}
	
	// 保存花价
	public static boolean savePrice(int type, String flist){
		Map<String, Object> map = DeliveryDateUtil.makeCode();
		boolean result = (boolean) map.get("result");
		String datecode = (String) map.get("datecode");
		if(result){
			List<Record> pricelist = new ArrayList<>();
			String[] list = flist.split(",");
			// 删除旧数据
			Db.batch("delete from f_flower_price where code = ? and type = ?", new Object[][]{{datecode, type}}, 100);
			for(String p : list){
				String[] param = p.split("-");
				Record price = new Record();
				price.set("code", datecode);
				price.set("type", type);
				price.set("fid", Integer.parseInt(param[0]));
				price.set("price1", Double.parseDouble(param[1]));
				if(param.length==3){
					price.set("price2", Double.parseDouble(param[2]));
				}
				pricelist.add(price);
			}
			// 保存数据
			Db.batchSave("f_flower_price", pricelist, 100);
		}
		return result;
	}
	
	// 保存节日花价
	public static boolean saveHoPrice(int type, String flist){
		String code = Db.queryStr("select code_value from f_dictionary where id=27");
		List<Record> pricelist = new ArrayList<>();
		String[] list = flist.split(",");
		// 删除旧数据
		Db.batch("delete from f_flower_price where code = ? and type = ?", new Object[][]{{code, type}}, 100);
		for(String p : list){
			String[] param = p.split("-");
			Record price = new Record();
			price.set("code", code);
			price.set("type", type);
			price.set("fid", Integer.parseInt(param[0]));
			price.set("price1", Double.parseDouble(param[1]));
			if(param.length==3){
				price.set("price2", Double.parseDouble(param[2]));
			}
			pricelist.add(price);
		}
		// 保存数据
		Db.batchSave("f_flower_price", pricelist, 100);
		return true;
	}
	
	// 保存花价查看中的实际花价
	public static boolean savePriceSee(String flist){
		String[] list = flist.split(",");
		int num = 0;
		for(String p : list){
			String[] param = p.split("-");
			String code = param[0];
			int type = Integer.parseInt(param[1]);
			int fid = Integer.parseInt(param[2]);
			Double price2 = Double.parseDouble(param[3]);
			int f = Db.update("update f_flower_price set price2 = ? where code =? and type=? and fid =?", price2, code, type, fid);
			num += f;
		}
		if(num > 0){
			return true;
		}else{
			return false;
		}
	}
	
	// 查看花价
	public static List<Record> seePrice(String code){
		List<Record> list = Db.find("SELECT a.fid,a.code,a.type,b.name,b.imgurl,a.price1,a.price2 FROM f_flower_price a LEFT JOIN f_flower_view b ON a.fid=b.id where a.code=?", code);
		return list;
	}
	
	/**
	 * 订单列表
	 * @param pageno
	 * @param pagesize
	 * @param time 送达时间 1周一送  2周六送  当Type=5(节日)时,查select code_value from f_dictionary where code_key = 'holiday'
	 * @param type 1订阅 2送花 3周边 4兑换 5节日
	 * @param state 
	 * @param ordercode
	 * @param ishas
	 * @param receiver
	 * @param time_a
	 * @param time_b
	 * @param aid
	 * @param tel
	 * @param sendCycle 发货周期
	 * @param app 用途
	 * @param style 格调
	 * @param piCode  特定的送达日期(批号)
	 * @param ptNo  团号 
	 * @return
	 */
	public static Page<Record> getOrderList(int pageno, int pagesize, int time,	int type, String state, String ordercode, int ishas,
			String receiver, String time_a, String time_b, String aid, String tel,int sendCycle,int app,int style,String piCode,String ptNo,String gName){
		// f_order 新增ptNo 字段  团号 
		String sqlExceptSelect = "FROM f_order a  left join f_picode d on d.ordercode=a.ordercode LEFT JOIN f_pingtuan t on a.ptNo = t.ptNo  where  IFNULL(d.num ,0)<=1";
		
		if(sendCycle!=10){
			sqlExceptSelect+=" and a.sendCycle="+sendCycle;
		}
		if(app!=10){
			sqlExceptSelect+=" and a.app="+app;
		}
		if(style!=10){
			sqlExceptSelect+=" and a.style="+style;
		}
		if(time != 0)
			sqlExceptSelect += "  and a.reach=" + time;
		if(time==3){
			sqlExceptSelect += "  and d.piCode='" + piCode+"'";
		}
		if(type != 0)
			sqlExceptSelect += " and a.type=" + type;
		sqlExceptSelect += " and a.state in (" + state+")";
		if(ordercode != null)
			sqlExceptSelect += " and a.ordercode like '%" + ordercode + "%'";
		if(ishas != 2)
			sqlExceptSelect += " and a.ishas =" + ishas;
		
		if(receiver != null && receiver.equals("")==false)
			sqlExceptSelect += " and a.name like '%" + receiver + "%'";
		if(aid != null && aid != "")
			sqlExceptSelect += " and a.aid = " + aid;
		if(tel != null && tel != "")
			sqlExceptSelect += " and a.tel = " + tel;
		
		if (ptNo != null && ptNo != "") {
			sqlExceptSelect += " and a.ptNo = " + ptNo;
		}
		
		if (StrKit.notBlank(gName)) {
			sqlExceptSelect += " and a.gName like '%" + gName+"%'";
		}
		
		
		if(time_a == null && time_b == null){
			sqlExceptSelect += "";
		}else{
			if(time_a != "" && "".equals(time_b)){
				sqlExceptSelect += " and a.ctime >='"+time_a+"'";
			}else if("".equals(time_a) && time_b != ""){
				sqlExceptSelect += " and a.ctime <='"+time_b+"'";
			}else if(time_a != "" &&time_b != ""){
				sqlExceptSelect += " and a.ctime between '"+time_a+"' and '"+time_b+"'";
			}
		}
		sqlExceptSelect += " order by a.id desc";
		
		// System.err.println( sqlExceptSelect);
		
		
		Page<Record> page = Db.paginate(pageno, pagesize, "SELECT a.ptNo,a.ordercode,a.aid,a.gName,a.name as receiver,a.tel,a.jihui,a.color,a.cycle,a.reach,a.price AS totalprice,a.type,a.ctime,a.state,a.ishas,a.is_sy,a.sendCycle,a.app,a.style,a.ocount,a.ptNo,a.ptSusTime,a.fund,a.jsAid,a.jsMoney ", sqlExceptSelect);
		List<Record> orders = page.getList();
		for (Record order : orders) {
			if(order.getInt("reach")==3){
				String holidate = Db.queryStr("select piCode from f_picode where ordercode=? and num=1",order.getStr("ordercode"));
				order.set("week", holidate);
			}
		}
		return page;
	}
	// 订单列表(用于订单信息修改)
	public static Page<Record> getChangeList(int pageno, int pagesize,String receiver, String tel,String piCode,int state,String ordercode){
		String sqlExceptSelect = "FROM f_order a  left join f_picode d on d.ordercode=a.ordercode where  IFNULL(d.num ,0)<=1";
		if(receiver != null && receiver.equals("")==false){
			sqlExceptSelect += " and a.name like '%" + receiver + "%'";
		}
		if(tel != null && tel != ""){
			sqlExceptSelect += " and a.tel = " + tel;
		}
		if(piCode != null && piCode != ""){
			sqlExceptSelect += " and d.piCode = " + piCode;
		}
		if(ordercode != null && ordercode != ""){
			sqlExceptSelect += " and a.ordercode = " + ordercode;
		}
		if(state != 9){
			sqlExceptSelect += " and a.state=" + state;
		}
		sqlExceptSelect += " order by a.id desc";
		Page<Record> page = Db.paginate(pageno, pagesize, "SELECT a.type,a.ordercode,a.name,a.tel,a.addr,a.zhufu,a.reach,a.id,d.piCode,a.state ", sqlExceptSelect);
		List<Record> orders = page.getList();
		for (Record order : orders) {
			if(order.getInt("type")==5){
				String holidate = Db.queryStr("select piCode from f_picode where ordercode=?",order.getStr("ordercode"));
				order.set("week", holidate);
			}
		}
		return page;
	}
	
	// 订单评分列表
	public static Page<Record> getCommentList(int pageno, int pagesize){
		return Db.paginate(pageno, pagesize, "select a.ordercode,a.point,a.descripte,a.ctime,b.lognumber,c.gName ", "from f_comment a left join f_order_info b on a.ordercode = b.ordercode left join f_order c on a.ordercode = c.ordercode   order by a.ctime desc");
	}
	
	// 采购订单
	public static Page<Record> getPurchaseList(int pageno, int pagesize, String ordercode){
		String sqlExceptSelect = " from f_purchase where 1=1 ";
		if(ordercode != null){
			sqlExceptSelect += " and code like '%"+ordercode+"%'";
		}
		sqlExceptSelect += " group by code,ctime order by code desc";
		return Db.paginate(pageno, pagesize, "SELECT code,SUM(num_a) AS numa,SUM(num_b) AS numb,SUM(price*num_b) AS totalprice,ctime ", sqlExceptSelect);
	}
	
	// 生成采购订单
	public static Map<String, Object> createPurchase(){
		Map<String, Object> resultMap = new HashMap<>();
		boolean R = false;
		String M = new String();
		Map<String, Object> dateMap = DeliveryDateUtil.chooseDate();
		if((boolean) dateMap.get("result")){
			String datecode = (String) dateMap.get("datecode");
			Number fyc = Db.queryNumber("select count(1) from f_order_info where state>=1 and code=?", datecode);	// 非异常物流
			if(fyc.intValue() > 0){	// 必须发货之后才能生成采购单
				Db.update("delete from f_purchase where code=?", datecode);
				Number errorNum = Db.queryNumber("SELECT COUNT(1) FROM f_order_info a LEFT JOIN f_order_pro b ON a.id=b.oid "
						+ "LEFT JOIN f_product_info c ON b.pid=c.pid LEFT JOIN f_flower_price d ON c.fid=d.fid"
						+ " WHERE a.code=? AND b.type=0 AND d.price1 IS null", datecode);
				if(errorNum.intValue() == 0){
					Number dozens = Db.queryNumber("SELECT COUNT(1) FROM f_flower a LEFT JOIN f_flower_price b ON a.id = b.fid WHERE CODE = ? AND a.dozen = 0", datecode);
					if(dozens.intValue() == 0){
						List<Record> flist = Db.find("SELECT c.fid,ROUND((SUM(c.num)*f.loss+SUM(c.num))/f.dozen) AS num,d.price1,d.price2,d.type FROM f_order_info a " +
								"LEFT JOIN f_order_pro b ON a.id=b.oid " +
								"LEFT JOIN f_product_info c ON b.pid=c.pid " +
								"LEFT JOIN f_flower_price d ON c.fid=d.fid " +
								"LEFT JOIN f_flower f ON c.fid=f.id " +
								"LEFT JOIN (SELECT id,CASE WHEN TYPE<=2 THEN 0 ELSE 1 END AS TYPE FROM f_product) e ON b.pid=e.id " +
								"WHERE a.code=? AND d.code=? AND b.type=0 AND d.type=e.type GROUP BY c.fid,d.type" , datecode, datecode);
						Date ctime = new Date();
						for(Record f : flist){
							Record purchase = new Record();
							purchase.set("code", datecode);
							purchase.set("type", f.getInt("type")==0?1:2);
							purchase.set("fid", f.getInt("fid"));
							purchase.set("price", f.getDouble("price1"));
							purchase.set("num_a", f.get("num"));
							purchase.set("num_b", f.get("num"));
							purchase.set("ctime", ctime);
							Db.save("f_purchase", purchase);
						}
						R = true;
						M = "批次" + datecode + "采购订单生成完成";
					}else{
						M = "花材成打量未完善";
					}
				}else{
					M = "批次" + datecode + "花价未完善";
				}
			}else{
				M = "批次" + datecode + "暂无法生成采购订单";
			}
		}else{
			M = "新批次订单尚未生成";
		}
		resultMap.put("R", R);
		resultMap.put("M", M);
		return resultMap;
	}
	
	// 订单详情
	public static Map<String, Object> getPurchaseInfo(String code){
		Map<String, Object> flowerMap = new HashMap<>();
		String sql = "SELECT a.fid,b.imgurl,c.name as tname,b.name,a.price,a.num_a,a.num_b FROM f_purchase a LEFT JOIN f_flower b ON a.fid=b.id LEFT JOIN f_flower_type c ON b.tid=c.id WHERE a.code=? AND a.type=?";
		List<Record> listA = Db.find(sql, code, 1);
		List<Record> listB = Db.find(sql, code, 2);
		List<Record> listC = Db.find("SELECT ax.st,COUNT(ax.st) AS stnum "
				+ "FROM (SELECT CASE WHEN b.fpid < 3 THEN b.fpid ELSE 3 END AS st "
				+ "FROM f_order_info a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode WHERE a.code=? AND b.type=1) ax GROUP BY ax.st", code);
		flowerMap.put("listA", listA);
		flowerMap.put("listB", listB);
		int[] stnum = {0,0,0};
		for(Record r : listC){
			stnum[r.getLong("st").intValue()-1] = r.getLong("stnum").intValue();
		}
		flowerMap.put("stnum", stnum);
		return flowerMap;
	}
	
	// 修改采购数量
	public static boolean updatePurchase(String code, String numarr){
		String[] arr = numarr.split(",");
		for(String num : arr){
			String[] p = num.split("-");
			Db.update("update f_purchase set num_b=? where code=? and type=? and fid=?", p[2], code, p[0], p[1]);
		}
		return true;
	}
	
	// 导出采购单-one
	public static void getPurchaseInfoForExcel_a(HttpServletResponse response, String code){
		String sql = "SELECT b.name,a.price,a.num_b FROM f_purchase a LEFT JOIN f_flower b ON a.fid=b.id WHERE a.code=? and a.type=?";
		List<Record> listA = Db.find(sql, code, 1);	// 订阅级
		List<Record> listB = Db.find(sql, code, 2);	// 送花级
		HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet1 = wbook.createSheet();
		wbook.setSheetName(0, "订阅级");
		HSSFSheet sheet2 = wbook.createSheet();
		wbook.setSheetName(1, "送花级");
		HSSFRow row;
		for(int i=0;i<listA.size();i++){
			if(i==0){
				row = sheet1.createRow(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellValue("名称");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellValue("单价");
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellValue("采购量");
			}
			row = sheet1.createRow(i+1);
			Record r = listA.get(i);
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellValue(r.getStr("name"));
			HSSFCell cell1 = row.createCell((short) 1);
			cell1.setCellValue(r.getDouble("price"));
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellValue(r.getInt("num_b"));
		}
		for(int i=0;i<listB.size();i++){
			if(i==0){
				row = sheet2.createRow(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellValue("名称");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellValue("单价");
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellValue("采购量");
			}
			row = sheet2.createRow(i+1);
			Record r = listB.get(i);
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellValue(r.getStr("name"));
			HSSFCell cell1 = row.createCell((short) 1);
			cell1.setCellValue(r.getDouble("price"));
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellValue(r.getInt("num_b"));
		}
		response.addHeader("Content-Disposition", "attachment;filename=" + code + ".xls");
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		try {
			ServletOutputStream out = response.getOutputStream();
			wbook.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 导出采购单-two
	public static void getPurchaseInfoForExcel_b(HttpServletResponse response, Record account, String code){
		String[] strArr = DeliveryDateUtil.getTimeByCode(code);
		List<Record> listA = Db.find("SELECT ax.st,COUNT(ax.st) AS stnum "
				+ "FROM (SELECT CASE WHEN b.fpid < 3 THEN b.fpid ELSE 3 END AS st "
				+ "FROM f_order_info a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode WHERE a.code=? AND b.type=1) ax GROUP BY ax.st", code);
		int[] stnum = {0,0,0};
		for(Record r : listA){
			stnum[r.getLong("st").intValue()-1] = r.getLong("stnum").intValue();
		}
		// 使用到的花材统计
		List<Record> listB = Db.find("SELECT a.fid,c.name AS tname,b.name,SUM(a.num_a) as num FROM f_purchase a "
				+ "LEFT JOIN f_flower b ON a.fid=b.id LEFT JOIN f_flower_type c ON b.tid=c.id WHERE a.code=? GROUP BY a.fid", code);
		for(Record r : listB){
			int fid = r.getInt("fid");
			Number num_a = Db.queryNumber("SELECT COUNT(1) FROM f_order_info a LEFT JOIN f_order_pro b ON a.id=b.oid LEFT JOIN f_product_info c ON b.pid=c.pid LEFT JOIN f_product d ON c.pid=d.id WHERE a.code=? AND b.type=0 AND c.fid=? AND d.fpid=1", code, fid);
			Number num_b = Db.queryNumber("SELECT COUNT(1) FROM f_order_info a LEFT JOIN f_order_pro b ON a.id=b.oid LEFT JOIN f_product_info c ON b.pid=c.pid LEFT JOIN f_product d ON c.pid=d.id WHERE a.code=? AND b.type=0 AND c.fid=? AND d.fpid=2", code, fid);
			Number num_c = Db.queryNumber("SELECT COUNT(1) FROM f_order_info a LEFT JOIN f_order_pro b ON a.id=b.oid LEFT JOIN f_product_info c ON b.pid=c.pid LEFT JOIN f_product d ON c.pid=d.id WHERE a.code=? AND b.type=0 AND c.fid=? AND d.fpid>2", code, fid);
			r.set("numa", num_a.intValue());
			r.set("numb", num_b.intValue());
			r.set("numc", num_c.intValue());
		}
		HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet = wbook.createSheet();
		
		sheet.setColumnWidth((short) 0, (short) 4500);
		sheet.setColumnWidth((short) 1, (short) 4500);
		sheet.setColumnWidth((short) 2, (short) 9000);
		sheet.setColumnWidth((short) 3, (short) 9000);
		sheet.setColumnWidth((short) 4, (short) 9000);
		sheet.setColumnWidth((short) 5, (short) 9000);
		sheet.setColumnWidth((short) 6, (short) 9000);
		HSSFRow row;
		HSSFCell cell;
		// 样式一
		HSSFCellStyle cellStyle1 = wbook.createCellStyle();
		HSSFFont font1 = wbook.createFont();
		font1.setFontHeightInPoints((short) 11);
		font1.setFontName("微软雅黑");
		cellStyle1.setFont(font1);
		cellStyle1.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);	//垂直居中
		cellStyle1.setAlignment(HSSFCellStyle.ALIGN_CENTER);			//水平居中
		// 样式二
		HSSFCellStyle cellStyle2 = wbook.createCellStyle();
		HSSFFont font2 = wbook.createFont();
		font2.setFontHeightInPoints((short) 12);
		font2.setFontName("微软雅黑");
		font2.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);					//粗体显示
		cellStyle2.setFont(font2);
		cellStyle2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);	//垂直居中
		cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER);			//水平居中
		cellStyle2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyle2.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyle2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyle2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		// 样式三
		HSSFCellStyle cellStyle3 = wbook.createCellStyle();
		HSSFFont font3 = wbook.createFont();
		font3.setFontHeightInPoints((short) 12);
		font3.setFontName("微软雅黑");
		cellStyle3.setFont(font3);
		cellStyle3.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);	//垂直居中
		cellStyle3.setAlignment(HSSFCellStyle.ALIGN_CENTER);			//水平居中
		cellStyle3.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyle3.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyle3.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyle3.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		for(int i=0;i<listB.size();i++){
			if(i==0){
				row = sheet.createRow(0);
				cell = row.createCell((short) 0);
				cell.setCellStyle(cellStyle1);
				cell.setCellValue("计算人：" + account.getStr("username"));
				cell = row.createCell((short) 1);
				cell.setCellStyle(cellStyle1);
				Calendar now = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				cell.setCellValue("计算时间：" + sdf.format(now.getTime()));
				cell = row.createCell((short) 2);
				cell.setCellStyle(cellStyle1);
				cell.setCellValue("订单起始时间：" + strArr[0]);
				cell = row.createCell((short) 3);
				cell.setCellStyle(cellStyle1);
				cell.setCellValue("订单截止时间：" + strArr[1]);
				cell = row.createCell((short) 4);
				cell.setCellStyle(cellStyle1);
				cell.setCellValue("包含双品：" + stnum[0] + "束");
				cell = row.createCell((short) 5);
				cell.setCellStyle(cellStyle1);
				cell.setCellValue("包含多品：" + stnum[1] + "束");
				cell = row.createCell((short) 6);
				cell.setCellStyle(cellStyle1);
				cell.setCellValue("包含送花：" + stnum[2] + "束");
				
				sheet.addMergedRegion(new Region(1, (short) 0, 1, (short) 1));
				row = sheet.createRow(1);
				cell = row.createCell((short) 0);
				cell.setCellStyle(cellStyle2);
				cell.setCellValue("种类");
				cell = row.createCell((short) 1);
				cell.setCellStyle(cellStyle2);
				cell = row.createCell((short) 2);
				cell.setCellStyle(cellStyle2);
				cell.setCellValue("花材名称");
				cell = row.createCell((short) 3);
				cell.setCellStyle(cellStyle2);
				cell.setCellValue("双品单量");
				cell = row.createCell((short) 4);
				cell.setCellStyle(cellStyle2);
				cell.setCellValue("多品单量");
				cell = row.createCell((short) 5);
				cell.setCellStyle(cellStyle2);
				cell.setCellValue("送花");
				cell = row.createCell((short) 6);
				cell.setCellStyle(cellStyle2);
				cell.setCellValue("需采购的花材打量");
			}
			
			sheet.addMergedRegion(new Region((i*2+2), (short) 0, (i*2+3), (short) 1));
			sheet.addMergedRegion(new Region((i*2+2), (short) 2, (i*2+3), (short) 2));
			sheet.addMergedRegion(new Region((i*2+2), (short) 3, (i*2+3), (short) 3));
			sheet.addMergedRegion(new Region((i*2+2), (short) 4, (i*2+3), (short) 4));
			sheet.addMergedRegion(new Region((i*2+2), (short) 5, (i*2+3), (short) 5));
			sheet.addMergedRegion(new Region((i*2+2), (short) 6, (i*2+3), (short) 6));
			
			row = sheet.createRow(i*2+2);
			Record exa = listB.get(i);
			
			cell = row.createCell((short) 0);
			cell.setCellStyle(cellStyle3);
			cell.setCellValue(exa.getStr("tname"));
			
			cell = row.createCell((short) 2);
			cell.setCellStyle(cellStyle3);
			cell.setCellValue(exa.getStr("name"));
			
			cell = row.createCell((short) 3);
			cell.setCellStyle(cellStyle3);
			cell.setCellValue(exa.getInt("numa"));
			
			cell = row.createCell((short) 4);
			cell.setCellStyle(cellStyle3);
			cell.setCellValue(exa.getInt("numb"));
			
			cell = row.createCell((short) 5);
			cell.setCellStyle(cellStyle3);
			cell.setCellValue(exa.getInt("numc"));
			
			cell = row.createCell((short) 6);
			cell.setCellStyle(cellStyle3);
			cell.setCellValue(exa.getBigDecimal("num").intValue());
			
			row = sheet.createRow(i*2+3);
			cell = row.createCell((short) 0);
			cell.setCellStyle(cellStyle3);
			cell = row.createCell((short) 1);
			cell.setCellStyle(cellStyle3);
			cell = row.createCell((short) 2);
			cell.setCellStyle(cellStyle3);
			cell = row.createCell((short) 3);
			cell.setCellStyle(cellStyle3);
			cell = row.createCell((short) 4);
			cell.setCellStyle(cellStyle3);
			cell = row.createCell((short) 5);
			cell.setCellStyle(cellStyle3);
			cell = row.createCell((short) 6);
			cell.setCellStyle(cellStyle3);
		}
		response.addHeader("Content-Disposition", "attachment;filename=" + code + ".xls");
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		try {
			ServletOutputStream out = response.getOutputStream();
			wbook.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 订单详情
	public static Map<String, Object> getorderInfo(String ordercode){
		Map<String, Object> map = new HashMap<>();
		Record order = Db.findFirst("select a.aid,a.ordercode,a.name,a.tel,a.addr,a.reach,a.price,a.cycle,a.zhufu,a.songhua,a.jihui,a.color,a.yhje,a.yhfs,a.ctime,a.type,a.state,a.jh_list,a.jh_color,b.title,a.szdx,a.remark "
				+ "from f_order a left join f_idoneity b on a.szdx = b.id where a.ordercode=?", ordercode);
		List<Record> detaillist = Db.find("SELECT b.name,a.price,a.type,a.fpid,a.seeds,a.num FROM f_order_detail a LEFT JOIN f_flower_pro b ON a.fpid=b.id where a.ordercode=?", ordercode);
		List<Record> flowerSjlist = Db.find("select id,name from f_flower where state = 1");
		// 优惠方式
		String yhfs = order.getStr("yhfs");
		String[] ids = yhfs.split(",");
		yhfs = null;
		if(Integer.parseInt(ids[0]) == 3){
			yhfs="兑换卡,卡号："+ids[1];
		}
		if(Integer.parseInt(ids[0]) == 4){
			yhfs="红包,红包ID："+ids[1];
		}
		if(Integer.parseInt(ids[0]) == 0){
			String[] cashList=ids[1].split("-");
			if(cashList[0].equals("0")==false){
				Record cash = Db.findFirst("SELECT b.money,b.benefit,c.name FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id LEFT JOIN f_cashtheme c ON b.tid=c.id where a.id=?", cashList[0]);
				if(yhfs == null){
					yhfs = cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
				}else{
					yhfs += "、" + cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
				}
			}
			if(cashList.length==2){
				if(cashList[1].equals("0")==false){
					String ss=cashList[1];
					Record cash = Db.findFirst("SELECT b.money,b.benefit,c.name FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id LEFT JOIN f_cashtheme c ON b.tid=c.id where a.id=?", cashList[1]);
					if(yhfs == null){
						yhfs = cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
					}else{
						yhfs += "、" + cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
					}
				}
			}
			
			
			
		}
		if(order.getInt("type") == 5){
			String holidate = Db.queryStr("select piCode from f_picode where orderCode=?",order.getStr("ordercode"));
			order.set("week", holidate);
			/*try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				Date d = sdf.parse(holidate);
				SimpleDateFormat sweek = new SimpleDateFormat("EEEE");
				String week = sweek.format(d);
				order.set("week", week);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		List<Record> picilist = Db.find("select a.code,a.number,c.fname from f_order_info a LEFT JOIN f_order_pro b on a.id = b.oid "
				                 + "LEFT JOIN f_product c on b.pid = c.id where b.type=0 and a.state>=2 and ordercode =? order by a.code desc",ordercode);
		
		Record picivase = Db.findFirst("select a.code,a.number,c.name,b.num from f_order_info a LEFT JOIN f_order_pro b on a.id = b.oid "
								  + "LEFT JOIN f_flower_pro c on b.pid = c.id  where a.state>=2 and b.type=1 and a.ordercode = ?",ordercode);
		
		/*List<Record> picodeList=Db.find("select num, piCode from f_picode where orderCode=?",ordercode);*/
		List<Record> picodeList=getSendGoodsInfo(ordercode);
		List<Record> picodeModifyLog=Db.find("select modifyTime,case isSy when 0 then '未顺延' else '顺延' end 'isSy',case isModifyReach when 0 then '未修改送达日期' else '修改送达日期' end 'isModifyReach',case reachNew when 0 then '未修改' when 1 then '周一' else '周六' end 'reachNew' ,case reachOld when 0 then '未修改' when 1 then '周一' else '周六' end  'reachOld'from f_picode_modifylog where orderCode=? ORDER BY modifyTime",ordercode);
		
		map.put("picilist", picilist);
		map.put("picivase", picivase);
		order.set("yhfs", yhfs);
		map.put("order", order);
		map.put("detaillist", detaillist);
		map.put("flowerSjlist", flowerSjlist);
		map.put("picodeList", picodeList);
		map.put("picodeModifyLog", picodeModifyLog);
		
		return map;
	}
	
	// 退款申请
	public static Page<Record> getRefundList(int pageno, int pagesize, int state, String ordercode){
		String sqlExceptSelect = "FROM f_refund a LEFT JOIN f_order b ON a.ordercode=b.ordercode where 1=1";
		if(state != 9)
			sqlExceptSelect += " and a.state=" + state;
		if(ordercode != null)
			sqlExceptSelect += " and a.ordercode like '%" + ordercode + "%'";
		return Db.paginate(pageno, pagesize, "SELECT a.id,a.ordercode,a.money, a.remark_a,a.time_a,a.state,b.price,b.price*(1-b.ocount/b.cycle) cprice", sqlExceptSelect);
	}
	
	// 退款详情
	public static Map<String, Object> getRefundInfo(String ordercode){
		Map<String, Object> map = new HashMap<>();
		Record refund = Db.findFirst("select money,remark_a,time_a,remark_b,time_b,state from f_refund where ordercode=?", ordercode);
		Record order = Db.findFirst("select ordercode,name,tel,addr,reach,price,cycle,zhufu,songhua,jihui,yhje,yhfs,ctime,type,state,jh_list,price*(1-ocount/cycle) cprice from f_order where ordercode=?", ordercode);
		List<Record> detaillist = Db.find("SELECT b.name,a.price,a.type FROM f_order_detail a LEFT JOIN f_flower_pro b ON a.fpid=b.id where a.ordercode=?", ordercode);
		// 优惠方式
		String yhfs = order.getStr("yhfs");
		String[] ids = yhfs.split(",");
		yhfs = null;
		if(Integer.parseInt(ids[0]) == 3){
			yhfs="兑换卡,卡号:"+ids[1];
		}
		if(Integer.parseInt(ids[0]) == 0){
			String[] cashList=ids[1].split("-");
			if(cashList[0].equals("0")==false){
				Record cash = Db.findFirst("SELECT b.money,b.benefit,c.name FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id LEFT JOIN f_cashtheme c ON b.tid=c.id where a.id=?", cashList[0]);
				if(yhfs == null){
					yhfs = cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
				}else{
					yhfs += "、" + cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
				}
			}
			if(cashList.length==2){
				if(cashList[1].equals("0")==false){
					String ss=cashList[1];
					Record cash = Db.findFirst("SELECT b.money,b.benefit,c.name FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id LEFT JOIN f_cashtheme c ON b.tid=c.id where a.id=?", cashList[1]);
					if(yhfs == null){
						yhfs = cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
					}else{
						yhfs += "、" + cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
					}
				}
			}
			
			
			
		}
		order.set("yhfs", yhfs);
		map.put("refund", refund);
		map.put("order", order);
		map.put("detaillist", detaillist);
		return map;
	}
	
	// 退款处理
	public static boolean refundAction(final String ordercode, final double money){
		boolean result = false;

		result= Db.tx( new IAtom() {
			boolean flag = false;
			@Override
			public boolean run() throws SQLException {
				
				flag = Db.update("update f_refund set money=?,time_b=?,state=1 where ordercode=?", money, new Date(),ordercode)==1?true:false;
				if(flag){
					WeixinApiCtrl.setApiConfig();
					String openId = Db.queryStr("SELECT openid FROM f_account WHERE id = ?",Db.queryInt("select aid from f_order where ordercode = ?",ordercode));
					String message = "您的订单\r("+ordercode+")有一笔退款已受理，请留意微信退款信息。顺便告诉你个秘密，您可以带颜得花籽，免费领鲜花！" +  "\n\r<a href='"+Constant.getHost+"/account/invitefri'>"+"我要带颜"+"</a>"; ;
					CustomServiceApi.sendText(openId, message);
					Record record = Db.findFirst("SELECT a.id,a.isbuy FROM f_account a LEFT JOIN f_order b ON a.id = b.aid WHERE b.ordercode =?", ordercode);
				//如果是首单用户，退款时，改成无单用户
				//9月21日和倪工讨论的结果，关于首单退款，其实还存在问题，由于逻辑复杂，暂时忽略
					
					//退款之后，公益基金置0,介绍佣金置0
				   Record rd=Db.findFirst("select jsAid,fund,jsMoney from f_order where ordercode = ?",ordercode);
				   boolean flag1=	Db.update("update f_order set fund=0,jsMoney=0 where ordercode=?",ordercode)==1?true:false;
				   if(flag1){
				       if(rd.getInt("jsAid")!=0){
				    	   String jsOpenId=Db.queryStr("SELECT openid FROM f_account WHERE id = ?",rd.getInt("jsAid"));
				    	   double tMoney=Db.queryDouble("select sum(jsMoney) 'tMoney' from f_order where jsAid=? and isCheckout=0",rd.getInt("jsAid"));
				    	   //发【服务通知】模板消息
				    	   ApiResult result = TemplateMsgApi.send(TemplateData.New()
									// 消息接收者
									.setTouser(jsOpenId)
									// 模板id
									.setTemplate_id(WeixinApiCtrl.gettemplateId("服务状态提醒"))
									.setTopcolor("#eb414a")
									.setUrl(Constant.getHost)
									.add("first", "有一位好友的订单已退款,单号为："+ordercode, "#FF8040")
									.add("keyword1", "扣佣金:"+rd.getDouble("jsMoney")+"元,待发放佣金总额:"+tMoney, "#FF8040")
									.add("keyword2", "已扣除", "#FF8040").build());
				       }
				   }
				   
				   if(record.getInt("isbuy")==1){
						flag = Db.update("update f_account set isbuy=0 where id=?", record.getInt("id"))==1?true:false;
					}
				}
				return flag ;
			}
		});
		return result;
	}
	
	/**
	 * 是否计入首单
	 * isinfirst =1：计入首单
	 * isinfirst =0：不计入首单
	 * @param ordercode
	 * @return
	 */
	private static boolean isFirstOrder(String ordercode){
		boolean flag=false;
		int isinfirst=Db.queryInt("select isinfirst from f_flower_pro where id "
				+ " in(select fpid from f_order_detail where type=1 and ordercode=?) LIMIT 1",ordercode);
		if(isinfirst==1){
			flag=true;
		}
		return flag;
	}
	/**
	 * 是否计入首单
	 * isinfirst =1：计入首单
	 * isinfirst =0：不计入首单
	 * @param ordercode
	 * @return
	 */
	public static boolean isFirstOrder(int fpid){
		boolean flag=false;
		int isinfirst=Db.queryInt("select isinfirst from f_flower_pro where id=? ",fpid);
		if(isinfirst==1){
			flag=true;
		}
		return flag;
	}
	/**
	 * 开发票,支付成功回调
	 * @param fcode 支付码
	 * @return
	 */
	public static boolean paySuccessFc(String fcode){
	   	boolean flag= (Db.update("update f_receipt set state=2 where fcode=?",fcode))==1?true:false;
	   	try {
			SendMsgUtil.sendMsg("17751658515", "有新的申请发 票需求，请及时跟踪处理");
			SendMsgUtil.sendMsg("13773103981", "有新的申请发 票需求，请及时跟踪处理");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 当微信调用WeixinApiCtrl中的wxpayback()方法
	 * 支付成功-修改订单状态
	 * 被final修饰的变量就是一个常量，只能赋值一次
	 * 当run方法执行完后，线程就会退出
	 * @param ordercode
	 * @return
	 */
	public static boolean paySuccessNew(final String ordercode){
		final Record order = Db.findFirst("select id,aid,type,reach,sendCycle,cycle,ordercode,ptNo,jsAid,jsMoney,sy_code from f_order where state=0 and ordercode=? ", ordercode);
		if(order == null){
			return true;
		}
		final Record member = Db.findById("f_account", order.getInt("aid"));
		final String dateStr=new SimpleDateFormat("yyyyMMdd").format(new Date());//将当期日期转换成指定的格式
		boolean flag=false;
		flag= Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean flag=false;
				if(order != null){
					if (order.getInt("type") == 7){
						order.set("state", 3);//现场活动订单直接【已完成】
					}else if(order.getInt("type") == 8){
						order.set("state", 6);//拼团订单，待成团
					}else{
						order.set("state", 1);//其他订单，服务中
					}
					order.set("paytime",new Date());//支付成功时间
					//718当天大促,下单4次加送一次
					if(dateStr.equals("20170718")==true&&order.getInt("cycle")==4){
				        	order.set("cycle", order.getInt("cycle")+1);
				        }
					//主题花，体验次数  放在 商品列表的ptNum字段
					Record rd=Db.findFirst("select ptid,ptNum from f_order_detail a left join f_flower_pro b on a.fpid=b.id "
							+ " where ordercode=? and a.type=1",order.getStr("ordercode"));
					if(rd.getInt("ptid")==105){
						order.set("cycle", rd.getInt("ptNum"));
						order.set("sendCycle", 1);
					}
					boolean isfirstorder=isFirstOrder(ordercode);
					if(member.getInt("isbuy") == 0&&isfirstorder==true){
						order.set("ishas", 0);//计入首单
					}
					//拼团订单在成团后加批号,现场活动不用产生批号
					if(order.getInt("type")!=8&&order.getInt("type")!=7){
						flag=savePiCode(order);//计算保存发货批号
					}
					flag=aboutFlowerSeed(order);//处理花籽
					//修改首单用户状态 和 首单状态
					//0未购买  1 已经购买首单  2 已经购买非首单
					if(member.getInt("isbuy") == 0&&isfirstorder==true){
						member.set("isbuy", 1);
					}else if(member.getInt("isbuy") == 1){
						member.set("isbuy", 2);	
					}
					flag=Db.update("f_account", member);
					flag=Db.update("f_order", order);
				}
				return flag;
			}
		});
		//更新团信息
		if(order.getInt("ptNo")!=0){
			flag=updatePtOrder(order.getInt("ptNo"),order.getInt("aid"),order.getInt("jsAid"),order.getDouble("jsMoney"));
		}
		return flag;
	}
	
	/**
	 * 支付成功后，处理花籽
	 * @param order
	 * @return
	 */
	public static boolean aboutFlowerSeed(Record order){
		boolean flag=false;
		//兑换商城的订单，扣除花籽
		if(order.getInt("type")==41||order.getInt("type")==43){
		   FCDao.chanseedstate_new(order.getInt("aid"), order.getStr("ordercode"));
		   sendOrderSuccessMsg(order.getInt("aid"), order.getStr("ordercode"),0);//购买成功通知	
		}
		//非兑换订单，送花籽
		if(order.getInt("type")!=41&&order.getInt("type")!=43&&order.getInt("type")!=7){
			seedType  st=seedType.buy01;
			if(order.getInt("cycle")==1){
				st=seedType.buy01;
			}else if(order.getInt("cycle")==4||order.getInt("cycle")==5){
				st=seedType.buy04;
			}else if(order.getInt("cycle")==12){
				st=seedType.buy12;
			}else if(order.getInt("cycle")==24){
				st=seedType.buy24;
			}
			for(int i = 0; i<st.point; i++){	// 付款成功送花籽，1个花籽1笔记录，方便兑换时扣除
				Record seed = new Record();
				seed.set("aid", order.getInt("aid"));
				seed.set("send", 1);
				seed.set("type", st.name());
				seed.set("remarks", st.name);
				seed.set("ctime", new Date());
				seed.set("username", order.getStr("ordercode"));
				flag=Db.save("f_flowerseed", seed);
			}
			sendOrderSuccessMsg(order.getInt("aid"), order.getStr("ordercode"),st.point);//购买成功通知	
		}
		return flag;
	}
	
	/**
	 * 支付成功之后
	 * 保存发货批号
	 * 根据f_flower_pro确定
	 * @param order
	 * @return
	 */
	public static boolean savePiCode(Record order){
		boolean flag=false;
		List<String> piCodeList =new ArrayList<>() ;
		String reachtype=Db.queryStr("select reachtype from f_order_detail a left join f_flower_pro b on a.fpid=b.id "
				+ " where ordercode=? and a.type=1",order.getStr("ordercode"));
		String reach= reachtype.split(":")[0];//根据商品列表找到[送达日期分类]
		if(reach.equals("3")){
			String picode=Db.queryStr("select sy_code from f_order where ordercode=?",order.getStr("ordercode"));
			List<String> picodes= new ArrayList<>();
			picodes.add(picode);
			piCodeList=picodes;
		}else if(reach.equals("2")){
			Calendar ca = Calendar.getInstance();
			ca.add(Calendar.HOUR, 24);
			String picode=new SimpleDateFormat("yyyyMMdd").format(ca.getTime());;//周边T+1天发货
			List<String> picodes= new ArrayList<>();
			picodes.add(picode);
			piCodeList=picodes;
		}
		else{
			piCodeList=DeliveryDateUtil.getPiCodeList(order.getInt("reach"), new Date(), 
					order.getInt("sendCycle"), order.getInt("cycle"),order.getInt("type"),0,
					order.getStr("sy_code"));
			
		}
		//*****保存发货批号
		for(int i=0;i<piCodeList.size();i++){
			Record f_picode = new Record();
			f_picode.set("ordercode", order.getStr("ordercode"));
			f_picode.set("piCode", piCodeList.get(i));
			f_picode.set("reach", order.getInt("reach"));
			f_picode.set("num",i+1 );
			flag=Db.save("f_picode", f_picode);
		}
		//遇到节假日顺延、修改发货日期
		/*SyHoliday("20180217",7,2);
		SyHoliday("20180219",7,2);
		SyHoliday("20180224",7,2);*/
		
		return flag;
	}
	
	/**
	 * 前提是：包含picodeStart这个批号
	 * 当type=1时，表示单批号修改，新批号=picodeStart+days,偏移天数=-days
	 * 当type=2时，表示从picodeStart开始，每个批号加上days
	 * @param picodeStart
	 * @param days
	 * @param type
	 */
	public static void SyHoliday(String picodeStart,int days,int type){
		if(type==1){
			Db.update("update f_picode set picode=DATE_FORMAT(date_sub(DATE_FORMAT(picode, '%Y%m%d'),interval -? day),'%Y%m%d'),offsetDays=? where picode=?",days,days*(-1),picodeStart);
		}else if(type==2){
			Db.update("update f_picode set  picode=DATE_FORMAT(date_sub(DATE_FORMAT(picode, '%Y%m%d'),interval -? day),'%Y%m%d') where picode>=? and ordercode in (select a.ordercode from ( (select ordercode from f_picode  where picode=? ) as a)) ORDER BY picode desc",days,picodeStart,picodeStart);
		}
		
	}
	
	/**
	 * 拼团
	 * 修改本团所有订单的状态,拼团成功时间;本团所有订单统一生成批号
	 * @return
	 */
	public static boolean updatePtOrder( final int ptNo,final int aid,int jsAid,double jsMoney){
		return Db.tx(new IAtom(){
			@Override
			public boolean run() throws SQLException {
				boolean result=false;
				//加入团
				Record f_pingtuan_detail=new Record();
				f_pingtuan_detail.set("ptNo", ptNo);
				f_pingtuan_detail.set("aid", aid);
				f_pingtuan_detail.set("ctime", new Date());
				result=Db.save("f_pingtuan_detail", f_pingtuan_detail);
				
				Record rd=Db.findFirst("select maxhours,needCount,hadCount from f_pingtuan where ptNo=?",ptNo);
				//如果团里没有人,表示当前支付的是团长
				if(rd.getInt("hadCount")==0){
					int hours=rd.getInt("maxhours");//拼团时限
					
					Date d = new Date();
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time_a = format.format(d);//拼团开始时间
					
					Calendar ca = Calendar.getInstance();
					ca.add(Calendar.HOUR, hours);
					d = ca.getTime();
					String time_b = format.format(d);//拼团结束时间
					
					result=Db.update("update f_pingtuan set ptTimeS=?,ptTimeE=?,hadCount=1,state=2 where ptNo=?",time_a,time_b,ptNo)>0?true:false;
					sendPintunMsgToFirst(ptNo);
				}else if(rd.getInt("hadCount")+1<rd.getInt("needCount")){//普通团员参团后，团未满
					result=Db.update("update f_pingtuan set hadCount=hadCount+1 where ptNo=?",ptNo)>0?true:false;
					sendPintunMsgToOther(aid,ptNo);
				}
				else{
					//普通团员参团后，团已满
					result=Db.update("update f_pingtuan set hadCount=hadCount+1,state=3 where ptNo=?",ptNo)>0?true:false;
					//修改本团所有订单的状态,拼团成功时间;本团所有订单统一生成批号
					if(result==true){
						List<Record> rds=Db.find("select ordercode,reach,cycle,sendCycle,type from f_order where state=6 and ptNo=? for update",ptNo);
						for (Record order : rds) {
							//修改状态、拼团成功时间
							result=Db.update("update f_order set state=1,ptSusTime=now() where ordercode=?", order.getStr("ordercode"))>0?true:false;
							savePiCode(order);
						}
						//遇到节假日顺延、修改发货日期
						
						sendPintunMsgToAll(ptNo);//拼团成功后，给所有团员发信息
					}
				}
				
				//给入团介绍人发佣金提醒
				if(jsAid!=0&&jsMoney!=0){

					   
					   String nick=Db.queryStr("SELECT nick FROM f_account WHERE id = ?",aid);
			    	   String jsOpenId=Db.queryStr("SELECT openid FROM f_account WHERE id = ?",jsAid);
			    	   double tMoney=Db.queryDouble("select sum(jsMoney) 'tMoney' from f_order where jsAid=? and isCheckout=0",jsAid);
			    	   //发【服务通知】模板消息
			    	   TemplateMsgApi.send(TemplateData.New()
								// 消息接收者
								.setTouser(jsOpenId)
								// 模板id
								.setTemplate_id(WeixinApiCtrl.gettemplateId("服务状态提醒"))
								.setTopcolor("#eb414a")
								.setUrl(Constant.getHost)
								.add("first", nick+"通过您的介绍成功下单", "#FF8040")
								.add("keyword1", "新增佣金:"+jsMoney+"元,待发放佣金总额:"+tMoney, "#FF8040")
								.add("keyword2", "已入账", "#FF8040").build());
			       
				}
				
				return result;
			}});	
	}
	
	/**
	 * 给团长发信息
	 * @param ptNo
	 */
	public static void sendPintunMsgToFirst(int ptNo){
		String url=Constant.getHost +"/account/groupDetail/"+ ptNo;
		Record rd=Db.findFirst("select b.openid,a.hadCount,c.name,a.aid,SUBSTR(c.itinfo4 FROM 8) 'img' from f_pingtuan as a left join f_account as b on a.aid=b.id"
				+ " left join f_flower_pro c on a.fptid=c.id  where a.ptNo=?",ptNo);
		WeixinApiCtrl.setApiConfig();
		String message = String.format("您发起的【%s】已开团成功,赶快邀请好友来参团吧。\n",rd.getStr("name"))+
				String.format("<a href='%s'>【查看详情】</a>\n", url); 
		CustomServiceApi.sendText(rd.getStr("openid"), message);
		
		sendPicTun(rd.getStr("openid"),rd.getInt("aid"),ptNo,rd.getStr("img"));
	}
	
	/**
	 * 普通团员参团后，发送信息
	 * @param aid
	 * @param ptNo
	 */
	public static void sendPintunMsgToOther(int aid,int ptNo){
		String url=Constant.getHost +"/account/groupDetail/"+ ptNo;
		Record rd=Db.findFirst("select a.hadCount,c.name,SUBSTR(c.itinfo4 FROM 8) 'img' from f_pingtuan as a left join f_account as b on a.aid=b.id"
				+ " left join f_flower_pro c on a.fptid=c.id  where a.ptNo=?",ptNo);
		WeixinApiCtrl.setApiConfig();
		String message = String.format("您参加的【%s】已有%d人参团，赶快邀请好友来参团吧。\n",rd.getStr("name"),rd.getInt("hadCount"))+
				String.format("<a href='%s'>【查看详情】</a>\n", url);
		String openid=Db.queryStr("select openid from f_account where id=?",aid);
		CustomServiceApi.sendText(openid, message);
		sendPicTun(openid,aid,ptNo,rd.getStr("img"));
	}
	
	
	/**
	 * 拼团成功后，给团中所有人发信息
	 * @param ptNo
	 */
	public static void sendPintunMsgToAll(int ptNo){
		String url=Constant.getHost +"/account/groupDetail/"+ ptNo;
		WeixinApiCtrl.setApiConfig();
		List<Record> openIdlist=Db.find("select c.ptNo,d.name,b.openid,b.id from f_pingtuan_detail as a left join f_account as b on a.aid=b.id left join f_pingtuan as c on a.ptNo=c.ptNo left join f_flower_pro as d on d.id=c.fptid where a.ptNo=? and not exists(select 1 from f_warnlog as c where c.nPtNo=a.ptNo and b.id=c.nAid and c.ntype='ptMessage' and c.nContent='mbMessage' and nRemark='success')",ptNo);
	    for (Record rd : openIdlist) {
	    	Record order=Db.findFirst("select picode,a.ordercode from f_order a left join f_picode b "
	    			+ " on a.ordercode=b.ordercode where ptNo=? and  aid=? and num=1 ",rd.getInt("ptNo"),rd.getInt("id"));
	    	String firstDate=order.getStr("picode");
	    	firstDate=firstDate.substring(0, 4)+"-"+firstDate.substring(4, 6)+"-"+firstDate.substring(6,8);
			ApiResult ar = TemplateMsgApi.send(TemplateData.New()
				    // 消息接收者
				    .setTouser(rd.getStr("openid"))
				    // 模板id
				    .setTemplate_id(WeixinApiCtrl.gettemplateId("拼团成功通知"))
				    .setTopcolor("#eb414a")
				    .setUrl(url)
				    .add("first", "拼团成功,准备收花", "#333")
				    .add("keyword1", order.getStr("ordercode"), "#333")
				    .add("keyword2", rd.getStr("name"), "#333")
				    .add("remark", "\n鲜花首次送达日期："+firstDate, "#888")
				    .build());
			//WeixinApiCtrl.sendTemplateMsg(ar.getJson());
			//3人团，允许5人参加，所以此处记录日志，避免重复发送
			if(ar.getInt("errcode") == 0){
				 Record f_warnlog=new Record();
			     f_warnlog.set("nCode", ptNo);
			     f_warnlog.set("nType", "ptMessage");
			     f_warnlog.set("nTime", new Date());
			     f_warnlog.set("nContent", "mbMessage");
			     f_warnlog.set("nRemark", "success");
			     f_warnlog.set("nPtNo", ptNo);
			     f_warnlog.set("nAid", rd.getInt("id"));
			     Db.save("f_warnlog", f_warnlog);
			}
		}
	}
	
	/**
	 * 发送拼团邀请图片
	 * @param aid
	 * @param ptNo
	 * @param picUrl
	 */
	public static void sendPicTun(final String openid, final int aid, final int ptNo,final String picUrl){
		new Thread(){
			public void run(){
				WeixinApiCtrl.setApiConfig();
				ApiResult ar = QrcodeApi.createPermanent("3" + "_" + aid+"_"+ptNo);
		 		Gson gson = new  Gson();
		 		Map<?,?> map = gson.fromJson(ar.getJson(), HashMap.class);
		 		String url = (String) map.get("url");
		 		String outPath="/QRImage/" + aid+"-"+ptNo + ".jpg";
		 		try {
					QRCodeUtil.encode(url, "", outPath, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
		 		
		 		File file =new File(outPath);//二维码图片地址
		 		File bg=new File(Constant.imgpath+ picUrl);
		 		float a = (float) 0.9;
		 		BufferedImage buffImg;
				try {
					buffImg = NewImageUtils.watermark(bg, file, 450,450, a);
					NewImageUtils.generateWaterFile(buffImg, outPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
		 		
		 		ApiResult arPic = MediaApi.uploadMedia(MediaType.IMAGE, file);
				CustomServiceApi.sendImage(openid, arPic.getStr("media_id"));

			}
		}.start();
	}
	
	
	
	
	
	
	
	
	
	/**
	 * 购买成功通知
	 * @param aid
	 * @param ordercode
	 * @param point 赠送的花籽数量
	 */
	public static void sendOrderSuccessMsg(int aid, String ordercode,int point){
 		Record account = Db.findById("f_account", aid);
 		List<Record> list = Db.find("SELECT a.type,c.name FROM f_order a LEFT JOIN f_order_detail b ON a.ordercode=b.ordercode LEFT JOIN f_flower_pro c ON b.fpid=c.id where a.ordercode=? and a.aid=? ORDER BY b.type ASC", ordercode, aid);
 		Record o = Db.findFirst("select ishas,reach,isvase,cycle,sendCycle,price,ctime,type,fund from f_order where ordercode=?", ordercode);
 		String product = new String();//商品信息
 		String price = String.valueOf(o.getDouble("price"));//支付金额
 		String fund=String.valueOf(o.getDouble("fund"));//公益基金
 		Date cd = o.getDate("ctime");
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
 		String ctime = sdf.format(cd);//购买时间
 		
 		String cycle = String.valueOf(o.getInt("cycle"));//配送次数
 		
 		//718大促，订4次，加送一次
 		Date date=new Date();
 		SimpleDateFormat sdf2=new SimpleDateFormat("yyyyMMdd"); 
 		if(sdf2.format(date).equals("20170718")==true&&o.getInt("cycle")==4){
 			cycle = String.valueOf(o.getInt("cycle")+1);//配送次数
 		}
 		

 		String firstDate=DeliveryDateUtil.getFirstDate(ordercode);
 		//如果是拼团
 		if(o.getInt("type")==8){
 			firstDate="成团后确定";
 		}
 		
 		String vase_str = o.getInt("isvase")==0?"无花瓶":"有花瓶";//有无花瓶
 		if (o.getInt("ishas")==0) {
 			vase_str = "首单送花瓶";
		}
 		
 		String sendCycle  = " ";
 		if (o.getInt("cycle") > 1) {
 			sendCycle = o.get("sendCycle")+"周一送。";
		}
 		
 		if(account!=null && list.size()>0){
 			for(int i=0;i<list.size();i++){
 				if(i==0){
 					product = list.get(i).getStr("name");
 				}else{
 					product += "," + list.get(i).getStr("name");
 				}
 			}
 			
 			String name=""+product+"\n支付金额："+price+"元"+"\n购买时间："+ctime+"\n赠送花籽："+point+"粒";
			String remark="\n小主，我们已收到了您的订单哦，总共配送"+cycle+"次," +sendCycle+"鲜花首次送达日期："+firstDate+"，"+"改地址，改时间，查看订单，物流信息，花票等请点击菜单：为您服务->会员中心";
 			//如果是周边
			if(o.getInt("type")==3||o.getInt("type")==43){
 				remark="\n小主，我们已收到了您的订单哦，总共配送"+cycle+"次," +"预计发货日期："+firstDate+"，"+"改地址，改时间，查看订单，物流信息，花票等请点击菜单：为您服务->会员中心";
 			}
			if(o.getDouble("fund")>0){
 				name=""+product+"\n支付金额："+price+"元;其中"+fund+"元进入公益基金"+"\n购买时间："+ctime+"\n赠送花籽："+point+"粒";
 				remark="\n小主，我们已收到了您的订单哦，总共配送"+cycle+"次," +sendCycle+"首次送达日期："+firstDate+"，"+"改地址，改时间，查看订单，物流信息，花票等请点击菜单：为您服务->会员中心";
 				//如果是周边
 				if(o.getInt("type")==3||o.getInt("type")==43){
 	 				remark="\n小主，我们已收到了您的订单哦，总共配送"+cycle+"次," +"预计发货日期："+firstDate+"，"+"改地址，改时间，查看订单，物流信息，花票等请点击菜单：为您服务->会员中心";
 	 			}
 				remark+="\n\n感谢您的爱心公益，如果发生退款行为，相应比例的公益基金一并退回。公益活动详情可在“会员中心->我的公益”查看";
 			}
			
			ApiResult result = TemplateMsgApi.send(TemplateData.New()
		    // 消息接收者
		    .setTouser(account.getStr("openid"))
		    // 模板id
		    .setTemplate_id(WeixinApiCtrl.gettemplateId("购买成功通知"))
		    .setTopcolor("#eb414a")
		    .setUrl(Constant.getHost + "/account/center")
		    .add("name", name, "#333")
		    
		    .add("remark",remark, "#888")
		    .build());
			 WeixinApiCtrl.sendTemplateMsg(result.getJson());
 		}
 	}
	
	
	
	// 判断花票是否适合推送
	public static boolean pushvalid(){
		boolean result = false;
		// 本月推送数量
		Number count = Db.queryNumber("SELECT COUNT(1) FROM f_cashtheme WHERE DATE_FORMAT(ptime,'%Y-%m') = DATE_FORMAT(CURDATE(),'%Y-%m')");
		if(count.intValue() < 4){
			result = true;
		}
		return result;
	}
	
	/**
	 * 生成4位随机数
	 * @return
	 */
	public static String getCode(){
		Random r = new Random();
		StringBuffer vc = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			String ch = String.valueOf(r.nextInt(10));
			vc.append(ch);
		}
		return vc.toString();
	}
	
	//获得物流公司
	public static List<Record> getWuliu(){
		 return Db.find("select name,code from f_express group by name,code");
	}
	
	//更换订单产品
	public static boolean changeproduct(Integer pid,Integer orderid){
		boolean result = false;
		if(!"".equals(pid) && pid!=null && !"".equals(orderid) && orderid!=null){
			int us = Db.update("update f_order_pro set pid =? where oid=? and type = 0", pid,orderid);
			return us==1?true:false;
		}else{
			return result;
		}
	}
	
	//获得分享订单列表
	public static Page<Record> getShareList(int pageno, int pagesize, int time,	String ordercode){
		String sqlExceptSelect = "FROM f_share a LEFT JOIN f_order b ON a.ordercode = b.ordercode LEFT JOIN f_order_detail c ON a.ordercode = c.ordercode LEFT JOIN f_flower_pro d ON c.fpid = d.id WHERE c.type=1";
		if(time != 0)
			sqlExceptSelect += " and b.reach=" + time;
		if(ordercode != null)
			sqlExceptSelect += " and a.ordercode like '%" + ordercode + "%'";
		sqlExceptSelect += " order by a.id desc";
		return Db.paginate(pageno, pagesize, "SELECT a.ordercode,d.name,a.name AS receiver,b.jihui,b.color,a.cycle,a.ocount,d.ctime,b.type,b.reach,a.ocount ", sqlExceptSelect);
	}
	
	// 分享订单详情
	public static Map<String, Object> getShareInfo(String ordercode){
		Map<String, Object> map = new HashMap<>();
		Record order = Db.findFirst("select a.ordercode,c.name,c.tel,c.addr,a.reach,a.price,a.cycle,a.zhufu,a.songhua,a.jihui,a.yhje,a.yhfs,a.ctime,a.type,a.state,a.jh_list,a.jh_color,b.title,c.ocount,c.ctime,c.gtime "
				+ "from f_order a left join f_idoneity b on a.szdx = b.id left join f_share c on a.ordercode = c.ordercode where a.ordercode=?", ordercode);
		List<Record> detaillist = Db.find("SELECT b.name,a.price,a.type,a.fpid FROM f_order_detail a LEFT JOIN f_flower_pro b ON a.fpid=b.id where a.ordercode=?", ordercode);
		List<Record> flowerSjlist = Db.find("select id,name from f_flower where state = 1");
		// 优惠方式
		String yhfs = order.getStr("yhfs");
		String[] ids = yhfs.split(",");
		yhfs = null;
		if(Integer.parseInt(ids[0]) != 0 && Integer.parseInt(ids[0]) != 3){
			Record activity = Db.findById("f_activity", ids[0]);
			yhfs = activity.getStr("name");
		}
		if(Integer.parseInt(ids[1]) != 0 && Integer.parseInt(ids[0]) != 3){
			Record cash = Db.findFirst("SELECT b.money,b.benefit,c.name FROM f_cash a LEFT JOIN f_cashclassify b ON a.cid=b.id LEFT JOIN f_cashtheme c ON b.tid=c.id where a.id=?", ids[1]);
			if(yhfs == null){
				yhfs = cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
			}else{
				yhfs += "、" + cash.getStr("name") + ",满" + cash.getDouble("benefit") + "减" + cash.getDouble("money");
			}
		}
		String jh_list = order.getStr("jh_list");
		String jihuis = Db.queryStr("select group_concat(NAME) from f_flower_type where id in ("+jh_list+")");
		order.set("jihui", jihuis);
		String jh_color = order.getStr("jh_color");
		String colors = Db.queryStr("select group_concat(NAME) from f_color where id in ("+jh_color+")");
		order.set("colors", colors);
		List<Record> picilist = Db.find(" SELECT a.code,a.number,c.fname from f_order_info a LEFT JOIN f_order_pro b on a.id = b.oid "
				                 + "LEFT JOIN f_product c on b.pid = c.id where fname is not null and a.state>=2 and ordercode =? order by code",ordercode);
		Record picivase = Db.findFirst("select a.code,a.number,c.name from f_order_info a LEFT JOIN f_order_pro b on a.id = b.oid "
								  + "LEFT JOIN f_flower_pro c on b.pid = c.id  where a.state>=2 and b.type=1 and a.ordercode = ?",ordercode);
		map.put("picilist", picilist);
		map.put("picivase", picivase);
		order.set("yhfs", yhfs);
		map.put("order", order);
		map.put("detaillist", detaillist);
		map.put("flowerSjlist", flowerSjlist);
		return map;
	}
	
	// 获得递推人员
	public static Page<Record> getSpreadList(int pageno, int pagesize, int state, String number, String name, String tel, String time_a, String time_b){
		String sqlExceptSelect = " FROM  f_spread a left join f_cashtheme b on a.cashthemeId = b.id where 1=1";
		if(state != 9){
			sqlExceptSelect += " and a.state ="+state;
		}
		if(number!= null){
			sqlExceptSelect += " and a.number like '%"+number+"%'";
		}
		if(name!= null){
			sqlExceptSelect += " and a.name like '%"+name+"%'";
		}
		if(tel!= null){
			sqlExceptSelect += " and a.tel like '%"+tel+"%'";
		}
		Page<Record> page = Db.paginate(pageno, pagesize, "SELECT a.id,a.number,a.name,a.sex,a.tel,a.address,a.area,a.state,b.name cashtheme ", sqlExceptSelect);
		for (Record sp : page.getList()) {
			SimpleDateFormat sdft = new SimpleDateFormat("yyyy年MM月");
			List<Record> speadlist = null;
			String timearea = "";
			if(time_a == null && time_b == null){
				speadlist = Db.find("SELECT SUBSTRING(tjid, 3) AS sid,COUNT(a.ordercode) as num FROM f_order a LEFT JOIN f_account b ON a.aid = b.id "
						+ " WHERE a.state IN (1,2,3) AND SUBSTRING(b.tjid,1,1) = 2 GROUP BY SUBSTRING(tjid, 3)");
			}else{
				if(time_a != "" && "".equals(time_b)){
					timearea += " and a.ctime >='"+time_a+"'";
				}else if("".equals(time_a) && time_b != ""){
					timearea += " and a.ctime <='"+time_b+" 23:59:59'";
				}else if(time_a != "" &&time_b != ""){
					timearea += " and a.ctime between '"+time_a+"' and '"+time_b+" 23:59:59'";
				}
				speadlist = Db.find("SELECT SUBSTRING(tjid, 3) AS sid,COUNT(a.ordercode) as num FROM f_order a LEFT JOIN f_account b ON a.aid = b.id "
						+ " WHERE a.state IN (1,2,3) AND SUBSTRING(b.tjid,1,1) = 2 "+timearea+" GROUP BY SUBSTRING(tjid, 3)");
			}
			for (Record spread : speadlist) {
				if(Integer.parseInt(spread.getStr("sid")) == sp.getInt("id")){
					sp.set("tgnum", spread.get("num"));
				}
			}
			List<Record> memlist = Db.find("SELECT SUBSTRING(tjid, 3) AS sid,COUNT(id) as tgmem FROM f_account a WHERE SUBSTRING(tjid,1,1) = 2 "+timearea+" GROUP BY SUBSTRING(tjid, 3)");
			for (Record mem : memlist) {
				if(Integer.parseInt(mem.getStr("sid")) == sp.getInt("id")){
					sp.set("tgmem", mem.get("tgmem"));
				}
			}
		}
		return page;
	}
	
	// 冻结人员
	public static boolean freezeSpread(int id, int state){
		Record record = new Record();
		record.set("state", state);
		record.set("id", id);
		return Db.update("f_spread", record);
	}
	
	// 设置分组
	public static boolean updateMemGroup(int id, int gid){
		HttpKit.get(Constant.getHost + "/weixin/editUserGroup/"+id+"-"+gid);
		Record record = new Record();
		record.set("gid", gid);
		record.set("id", id);
		return Db.update("f_account", record);
	}
	
	//获得色系列表
	public static Page<Record> getColor(int pageno, int pagesize, String fcolor){
		String sqlExceptSelect = " from f_color a where 1=1";
		if(fcolor != ""){
			sqlExceptSelect += " and a.name like '%"+ fcolor +"%'";
		}
		return Db.paginate(pageno, pagesize, "select id,name,jh", sqlExceptSelect);
	}
	

	public static Page<Record> getColor(int pageno, int pagesize){
		return getColor(pageno, pagesize, "");
	}
	
	
	// 复制粘贴批次产品
	public static Map<String, Object> copypasteCode(String copycode, int type){
		responseMap = new HashMap<>();
		boolean R = false;
		map = DeliveryDateUtil.makeCode();
		Msg = "复制失败";
		Copycode = copycode;
		Type = type;
		if((boolean)map.get("result")){
			R = Db.tx(new IAtom() {
					boolean result = false;
					@Override
					public boolean run() throws SQLException {
						Number num = Db.queryNumber("SELECT COUNT(1) FROM f_product WHERE CODE = ? AND TYPE = ?", Copycode, Type);
						if(num.intValue() > 0){
							if(Copycode.equals(map.get("datecode"))){
								Msg = "不能复制当前批次";
								return result;
							}else{
								// 更新产品表f_product
								int num_1 = Db.update("INSERT INTO f_product(CODE,TYPE,fname,sname,price,fpid,iid) "
										+ "SELECT ?, TYPE, fname, sname, price, fpid,iid FROM f_product WHERE CODE = ? AND TYPE = ?", map.get("datecode"), Copycode, Type);
								if(num_1 > 0){
									// 更新产品明细表 f_product_info
									int num_2 = Db.update("INSERT INTO f_product_info(pid, fid, num) "
											+ "SELECT c.id,a.fid,a.num FROM f_product_info a LEFT JOIN  f_product b ON a.pid = b.id LEFT JOIN f_product c ON b.fname = c.fname AND b.sname = c.sname "
											+ "WHERE b.CODE = ? AND c.code = ? AND b.TYPE = ?", Copycode, map.get("datecode"), Type);
									if(num_2 > 0){
										Db.update("UPDATE f_product SET fname = REPLACE(fname,'（"+map.get("datecode")+"）',''), sname = REPLACE(sname,'（"+map.get("datecode")+"）','')  WHERE CODE = ? and type = ?", map.get("datecode"), Type);
										Db.update("UPDATE f_product SET fname = CONCAT(fname, '（"+map.get("datecode")+"）'), sname = CONCAT(sname, '（"+map.get("datecode")+"）')  WHERE CODE = ? and type = ?", map.get("datecode"), Type);
										Msg = "复制成功";
										result = true;
										return result;
									}else{
										Msg = "复制的批次中的产品没有花材";
										return result;
									}
								}else{
									Msg = "复制的批次中没有产品";
									return result;
								}
							}
						}else{
							Msg = "复制批次不存在";
							return result;
						}
					}
				});
		}else{
			Msg = "不在操作时间内";
		}
		responseMap.put("Msg", Msg);
		responseMap.put("R", R);
		return responseMap;
	}
	
	// 客诉补单 验证订单是否存在
	public static boolean validateorder(String code){
		Number rNum = Db.queryNumber("select count(1) from f_order where ordercode =?", code);
		return rNum.intValue()==0?false:true;
	}
	
	// 订单花瓶
	public static Integer getVase(String code){
		return Db.queryInt("SELECT fpid FROM f_order_detail where type = 2 and ordercode =?", code);
	}
	
	// 花瓶
	public static List<Record> getVases(){
		return Db.find("SELECT id,name FROM f_flower_pro WHERE ptid = 4 AND state = 0");
	}
	
	// 订单商品
	public static Integer getFlowerPro(String code){
		return Db.queryInt("SELECT fpid FROM f_order_detail where type = 1 and ordercode =?", code);
	}
	
	// 商品
	public static List<Record> getFlowerPros(){
		return Db.find("SELECT id,name FROM f_flower_pro");
	}
	
	// 创建客诉补单
	public static boolean createorderks(String ordercode, String aid, int pid, Integer vase,String area,String address, int reach, 
			  int cycle, String zhufu, String songhua, String jh_list, String jhcolor_list, int type, String recommend, Integer szdx, Integer cash, Integer activity, Double yh, String name, String tel){
		OrderCode = ordercode;
		Aid = aid;
		Pid = pid;
		Vase = vase;
		Area = area;
		Address = address;
		Reach = reach;
		Cycle = cycle;
		Zhufu = zhufu;
		Songhua = songhua;
		jh_List = jh_list;
		jhColor_List = jhcolor_list;
		TType = type;
		Recommend = recommend;
		Szdx = szdx;
		Cash = cash;
		Activity = activity;
		Yh = yh;
		Name = name;
		Tel = tel;
		
		pro_Flower = Db.findById("f_flower_pro", Pid);
		Order = new Record();	//订单对象
		boolean R = false;
		R = Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean result = false;
				Order.set("ordercode", getorderCode());
				Order.set("aid", Aid);
				Order.set("addr", getAddressArea(Area) + Address);	// 收货人地址
				Order.set("name", Name);	// 收货人姓名
				Order.set("tel", Tel);	// 收货人电话
				Order.set("reach", Reach);		// 送达时间(1周一/2周六)
				Order.set("price", 0.00);		// 总价
			
				Order.set("cycle", Cycle);		// 周期
				if(Vase != null){
					Order.set("isvase", 1);		// 购买花瓶
				}
				Order.set("zhufu", "".equals(Zhufu)?null:Zhufu);		// 祝福卡
				Order.set("songhua", "".equals(Songhua)?null:Songhua);	// 赠花人
				Order.set("jh_list", "".equals(jh_List)?null:jh_List);	// 忌讳的花
				Order.set("jh_color", "".equals(jhColor_List)?null:jhColor_List);	// 忌讳的花
				if(!"".equals(jh_List)){
					Order.set("jihui", Db.queryStr("select group_concat(NAME) from f_flower_type where id in ("+jh_List+")"));// 忌讳的花材分类
				}
				if(!"".equals(jhColor_List)){
					Order.set("color", Db.queryStr("select group_concat(NAME) from f_color where id in ("+jhColor_List+")"));// 忌讳的色系
				}
				Order.set("yhje", 0.00);		// 优惠金额
				Order.set("yhfs", "0,0");		// 优惠方式
				Order.set("ctime", new Date());	// 下单日期
				Order.set("type", TType);		// 商品类型(订阅/赠送/周边/兑换)
				Order.set("state", 1);	// 客诉补单直接为服务中
				Order.set("szdx", Szdx);		// 适赠对象ID
				Order.set("isks", 1);		// 对应原订单
				Order.set("remark", OrderCode);		// 对应原订单
				// 保存订单
				result = Db.save("f_order", Order);
				if(result){
					// 订单订购明细-主件(花束or花边好物)
					Record order_detail_flower = new Record();
					order_detail_flower.set("ordercode", Order.getStr("ordercode"));
					order_detail_flower.set("fpid", Pid);
					order_detail_flower.set("price", pro_Flower.getDouble("price"));
					order_detail_flower.set("type", 1);
					result = Db.save("f_order_detail", order_detail_flower);
					
					if(Vase != null){
						Record pro_vase = Db.findById("f_flower_pro", Vase);
						// 订单订购明细-附件(花瓶)
						Record order_detail_vase = new Record();
						order_detail_vase.set("ordercode", Order.getStr("ordercode"));
						order_detail_vase.set("fpid", Vase);
						order_detail_vase.set("price", pro_vase.getDouble("price"));
						order_detail_vase.set("type", 2);
						result = Db.save("f_order_detail", order_detail_vase);
					}
				}
				return result;
			}
		});
		return R;
	}
	// 生成订单编号
	public static String getorderCode(){
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return sdf.format(now.getTime());
	}
	// 获取收货地址区域
	public static String getAddressArea(String area){
		List<Record> arealist = Db.find("select name from f_area where id in (" + area + ") ORDER BY FIELD (id, " + area + ")");
		String areaname = new String();
		for(Record name:arealist){
			areaname += name.getStr("name")+"-";
		}
		return areaname;
	}
	
	//撤销退款
	public static boolean restorefund(final String ordercode){
		boolean flag=false;
		flag=Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean result = false;
				int state = Db.queryInt("SELECT state FROM f_refund WHERE ordercode =?", ordercode);
				if(state!=2){
					int num = Db.update("UPDATE f_refund SET state = 2 WHERE ordercode =?", ordercode);
					result = num==1?true:false;
					if(result){
						Db.update("update f_order set state = 1 where ordercode=?", ordercode);
						Record record = Db.findFirst("SELECT a.id,a.isbuy FROM f_account a LEFT JOIN f_order b ON a.id = b.aid WHERE b.ordercode =?", ordercode);
						if(record.getInt("isbuy")==0){
							result=Db.update("update f_account set isbuy=1 where id=?", record.getInt("id"))==1?true:false;
						}
					}
				}
			    boolean result_flowerseed =Db.update("update f_flowerseed set state=0 where username=? and state=3",ordercode)>0?true:false;//返还【收回的花籽】
				return result&&result_flowerseed;
			}
		});
		return flag;
		
	}
	
	//是否挑单
	public static int validatetd(){
		int result = 0;
		Map<String, Object> chooseMap = DeliveryDateUtil.chooseDate();
		String code = (String) chooseMap.get("datecode");
		Number numC = Db.queryNumber("select count(1) from f_tiaodan where code=?", code);
		if(numC.intValue() > 0){
			result = 1;
		}
		return result;
	}
	
	/**
	 * 获取兑换卡种类,下拉列表
	 * @return
	 */
	public static List<Record> getCardType(){
		return Db.find("select cNo,cName,cMoney,name,cCycle,cHasVase from f_card_type left join f_flower_pro on  f_card_type.cPid=f_flower_pro.id where cIsValid='Y'");
	}
	
	/***
	 * 兑换卡类型，用于制卡
	 * @param pageno
	 * @param pagesize
	 * @return
	 */
	public static Page<Record> getCardType(int pageno,int pagesize){
		Page<Record> page=Db.paginate(pageno,pagesize,
				"select cNo,cName,cMoney,name,cCycle,cHasVase",
				"from f_card_type left join f_flower_pro on  f_card_type.cPid=f_flower_pro.id where cIsValid='Y'");
		return page;
	}
	
	/***
	 * 兑换卡列表
	 * @param pageno
	 * @param pagesize
	 * @return
	 */
	public static Page<Record> getCard(int pageno,int pagesize,int cNo,int isSuccess,String cId,String cMakeTime){
		String sqlColumnSelect="select cId, '' cPwd,cName,cMoney,f_flower_pro.name,cCycle,cHasVase,case when cIsSuccess=0 then 'N' else 'Y' end 'cIsSuccess', IFNULL(nick,'') 'nick',IFNULL(date_format(cExcTime,'%Y-%m-%d %H:%i:%s'),'') 'cExcTime',IFNULL(cExcOrderId,'') 'cExcOrderId',f_flower_pro.price,cMaker,IFNULL(date_format(cMakeTime,'%Y-%m-%d %H:%i:%s'),'') 'cMakeTime',IFNULL(date_format(cEffDate,'%Y-%m-%d'),'') 'cEffDate',IFNULL(cPurchaser,'') 'cPurchaser',IFNULL(date_format(cPurTime,'%Y-%m-%d %H:%i:%s'),'') 'cPurTime',IFNULL(cSeller,'') cSeller,IFNULL(date_format(cSellTime,'%Y-%m-%d %H:%i:%s'),'')  'cSellTime'";
		String sqlExceptionSelect="from f_card left join  f_card_type on cFkNo=f_card_type.cNo left join f_flower_pro on f_card_type.cPid=f_flower_pro.id LEFT JOIN f_account on cExcMan=f_account.id where 1=1";
		if(cNo!=1){
			sqlExceptionSelect+=String.format(" and f_card_type.cNo=%d", cNo);
		}
		if(isSuccess!=10){
			sqlExceptionSelect+=String.format(" and cIsSuccess=%d", isSuccess);
		}
		if(cId!=null && cId!=""){
			sqlExceptionSelect+=String.format(" and cId=%s", cId);
		}
		if(StrKit.isBlank(cMakeTime)==false ){
			sqlExceptionSelect+=String.format(" and cMakeTime like '%s' ", cMakeTime+'%');
		}
			
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		FrontServiceCtrl fsc= new FrontServiceCtrl();
		
		for( int i=0;i<page.getList().size();i++){
			int cid=page.getList().get(i).getInt("cId"); 
			String cpwd=fsc.getExchangeCode(String.valueOf(cid));
			page.getList().get(i).set("cPwd",cpwd );
		}
		return page;
	}
	/**
	 * 获取产品名称
	 * @return
	 */
	public static List<Record> getTypeproduct(String picode){
		 return Db.find("(select b.name,count(b.name) count   from  f_order_info c left join  f_order_pro a on a.oid = c.id  left join f_flower_pro b on a.pid = b.id  where c.code = ? and a.type = 1 and c.state = 1 group by b.name) union all"+
        "(select b.fname name ,count(b.fname) count from f_order_info d left join f_order_pro a on d.id= a.oid left join f_product b  on a.pid = b.id left join f_flower_pro c on b.fpid = c.id where b.code = ? and a.type = 0 and d.state=1   group by b.fname ) ",picode,picode);
	}
	/**
	 * 获取订单类型
	 * @return
	 */
	public static List<Record> getTypeorder(String picode){
		 return Db.find("select b.gName,count(b.gName) count from f_order_info a left join f_order b  on a.ordercode = b.ordercode  where a.code = ? and a.state=1 group by b.gName ",picode);
	}
	/**
	 * 获取物流公司
	 * @return
	 */
	public static List<Record> getTypewuliu(String picode){
		 return Db.find("select ename ,count(ename) count from f_order_info where code = ? and state=1 group by ename ",picode);
	}

	public static Cookie[] cookie(HttpServletRequest request) {
		return  request.getCookies();
	}
	
	public static void KfSendAll2(HttpServletResponse response,java.io.File file,String id_start,String id_end,String message,boolean hasFile,HttpSession session,int isMb,String url) {
		if (isMb==0) {
			String sql = "select id,openid from f_account  WHERE isfans=0 ";
			if (StrKit.notBlank(id_start)) {
				sql += " and id>="+id_start; 
			}
			if (StrKit.notBlank(id_end)) {
				sql += " and id<="+id_end; 
			}
			List<Record>list = Db.find(sql);
			WeixinApiCtrl.setApiConfig();
			ApiResult arr = null ;
			if (hasFile) {
				 arr = MediaApi.uploadMedia(MediaType.IMAGE, file);
			}
			int i=1;
			int j=1;
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			for(Record record : list){
				Object sessionObj = session.getAttribute("qfmsg");
				if (sessionObj!=null) {
					session.removeAttribute("qfmsg");
					return;
				}
				boolean sf =false;
				boolean sm=false;
				if (hasFile) {
					ApiResult ar = CustomServiceApi.sendImage(record.getStr("openid"), arr.getStr("media_id"));
					if (ar.getInt("errcode")==0) {
						sf=true;
					}
				}
				if (StrKit.notBlank(message)) {
					ApiResult ar = CustomServiceApi.sendText(record.getStr("openid"), message);
					if (ar.getInt("errcode")==0) {
						sm=true;
					}
				}
				if(sf||sm){
					Record f_warnlog=new Record();
	                f_warnlog.set("nCode", record.getInt("id"));
	                f_warnlog.set("nType", "customMsg");
	                f_warnlog.set("nTime", new Date());
	                f_warnlog.set("nContent", "customMessage");
	                f_warnlog.set("nRemark", "success");
	                Db.save("f_warnlog", f_warnlog);
	                try {
	                	ServletOutputStream out = response.getOutputStream();
	                	String s = "{\'successP\':\'"+i+"\'}";//实时统计成功数量
						out.println(s);
					    out.flush();
					    i++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else {
					Record f_warnlog=new Record();
	                f_warnlog.set("nCode", record.getInt("id"));
	                f_warnlog.set("nType", "customMsg");
	                f_warnlog.set("nTime", new Date());
	                f_warnlog.set("nContent", "customMessage");
	                f_warnlog.set("nRemark", "fail");
	                Db.save("f_warnlog", f_warnlog);
	                try {
	                	ServletOutputStream out = response.getOutputStream();
	                	String s = "{\'failP\':\'"+j+"\'}";//实时统计失败数量
						out.println(s);
					    out.flush();
					    j++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else {
			List<Record>list = Db.find("select b.openid,a.id from f_account_selected a left join f_account b on a.aid = b.id");
			WeixinApiCtrl.setApiConfig();
			int i=1;
			int j=1;
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			for(Record record : list){
				Object sessionObj = session.getAttribute("qfmsg");
				if (sessionObj!=null) {
					session.removeAttribute("qfmsg");
					return;
				}
				boolean result = SendMouldMsgUtil.SendKfMsg(record.getStr("openid"), url, message);
				if(result){
					Record f_warnlog=new Record();
	                f_warnlog.set("nCode", record.getInt("id"));
	                f_warnlog.set("nType", "customMsg");
	                f_warnlog.set("nTime", new Date());
	                f_warnlog.set("nContent", "customMessage");
	                f_warnlog.set("nRemark", "success");
	                Db.save("f_warnlog", f_warnlog);
	                try {
	                	ServletOutputStream out = response.getOutputStream();
	                	String s = "{\'successP\':\'"+i+"\'}";//实时统计成功数量
						out.println(s);
					    out.flush();
					    i++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else {
					Record f_warnlog=new Record();
	                f_warnlog.set("nCode", record.getInt("id"));
	                f_warnlog.set("nType", "customMsg");
	                f_warnlog.set("nTime", new Date());
	                f_warnlog.set("nContent", "customMessage");
	                f_warnlog.set("nRemark", "fail");
	                Db.save("f_warnlog", f_warnlog);
	                try {
	                	ServletOutputStream out = response.getOutputStream();
	                	String s = "{\'failP\':\'"+j+"\'}";//实时统计失败数量
						out.println(s);
					    out.flush();
					    j++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	
	//获取自动回复列表
	public static Page<Record> getAutoResp(int pageno, int pagesize, int type){
		String sqlExceptSelect = "from f_zdhf where 1=1";
		if (type!=10) {
			sqlExceptSelect += String.format(" and type=%d", type);
		}
		return Db.paginate(pageno, pagesize, "select sendText,returnText,returnPicture,returnUrlname,returnUrl,type,expdate,returnCashtheme",sqlExceptSelect);
	}
	//获取企业定制列表
	public static Page<Record>getCompanyList(int pageno, int pagesize,String ctime_start,String ctime_end) {
		String sqlColumnSelect = "SELECT id,name,tel,area,company,`describe`,case ctype when 1 then '商务合作' when 2 then '批量采购' when 3 then '花艺课' when 4 then '其他' end as ctype,ctime";
		String sqlExceptionSelect = "FROM f_company_custom  where 1=1";
		if (StrKit.notBlank(ctime_start)) {
			sqlExceptionSelect += String.format(" and left(ctime,10) >= '%s'", ctime_start);
		}
		if (StrKit.notBlank(ctime_end)) {
			sqlExceptionSelect += String.format(" and left(ctime,10) <= '%s'", ctime_end);
		}
		sqlExceptionSelect += " order by ctime desc,name desc";
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	/**
	 * 获取发票列表
	 */
	public static Page<Record>getReceiptList(int pageno, int pagesize,int state) {
		String sqlColumnSelect = "select id,company,code,content,money,name,tel,area,addr,ordernum,postage,case state when 1 then '待支付' when 2 then '待开票' when 3 then '已开票' else '已关闭' end as state,fcode,ctime,aid";
		String sqlExceptionSelect = "FROM f_receipt  where 1=1 and state in (2,3)";
		if (state!=10) {
			sqlExceptionSelect += String.format(" and state =%d ",state);
		}
		sqlExceptionSelect += " order by ctime desc";
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	/**
	 * 18位签到列表
	 */
	public static Page<Record>getSign18List(int pageno, int pagesize,String aid,String ctim_start,String ctime_end) throws UnsupportedEncodingException{
		String sqlColumnSelect = "select id,aid,content,ctime,figs,state,case state when 0 then '通过' else '未通过' end as state   ";
		String sqlExceptionSelect = "from f_sign18 where 1=1";
		if (StrKit.notBlank(aid)) {
			sqlExceptionSelect += String.format(" and aid = %s", aid);
		}
		if (StrKit.notBlank(ctim_start)) {
			sqlExceptionSelect +=String.format(" and left(ctime,10)>='%s'", ctim_start);
		}
		if (StrKit.notBlank(ctime_end)) {
			sqlExceptionSelect +=String.format(" and left(ctime,10)<='%s'", ctime_end);
		}
		sqlExceptionSelect += " order by figs desc";
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	public static Page<Record> getMyMermory(int pageno, int pagesize,String aid) throws UnsupportedEncodingException{
		String sqlColumnSelect = "select id,aid,contentStr,subTime, case when state=0 then '非法' else '合法' end 'state'  ";
		String sqlExceptionSelect = "from f_myMemory where 1=1";
		if (StrKit.notBlank(aid)) {
			sqlExceptionSelect += String.format(" and aid = %s", aid);
		}
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	
	public static void setSession(HttpSession session) {
		session.setAttribute("qfmsg", "qfmsg");
	}
	/**
	 * 线下物流列表
	 */
	public static Page<Record> getOrderInfo_XX(int pageno, int pagesize,String fCode,String fNumber,String fwlName){
		 String select = "select id,fCode,fNumber,FORMAT(fPrice,2) as fPrice,fGname,fDate,fwlName,fOptime,fUser";
		 String sqlExceptSelect = " from f_order_info_xx where 1=1";
		 if (StrKit.notBlank(fCode)) {
			sqlExceptSelect+=String.format(" and fCode = '%s'", fCode);
		 }
		 if (StrKit.notBlank(fNumber)) {
				sqlExceptSelect+=String.format(" and fNumber = '%s'", fNumber);
	     }
		 if (StrKit.notBlank(fwlName)) {
				sqlExceptSelect+=String.format(" and fwlName = '%s'", fwlName);
	     }
		 sqlExceptSelect+= " order by fDate desc";
		 Page<Record>page= Db.paginate(pageno, pagesize, select, sqlExceptSelect);
		 return page;
	}	
}