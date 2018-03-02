package com.controller.manage;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.dao.FCDao;
import com.dao.MCDao;
import com.dao.PurchaseDao;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;


public class ManagePurchaseCtrl extends Controller{
/**
 * 采购列表	
 * @throws UnsupportedEncodingException
 */
      public void purchaseList() throws UnsupportedEncodingException {
    	Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
    	String code = getPara("code");
    	String name =  getPara("name");
		String channel = (String) (getPara("channel")==null?"1":getPara("channel"));
    	Page<Record> page = PurchaseDao.getPurchaseList(pageno,16,code,name,channel);
    	Number sumPrice = PurchaseDao.getSumPrice(code,name,channel);
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("purchaselist", page.getList());
		setAttr("sumPrice", sumPrice);
		setAttr("code", code);
		setAttr("name", name);
		setAttr("channel",channel);
    	render("purchase_list.html");
	}
	  /**
	   * 采购单拆分编辑
	   */
		public void purchaseSplit() {
			Integer id = getParaToInt(0);
			Record list = Db.findFirst("select id,totalNum from f_purchasem where id = ?", id);	
			setAttr("list", list);
			render("purchase_split.html");
		}
		/**
		   * 采购单拆分保存
		   */	
		public void purchaseSaveSplit() {
			Record admin = getSessionAttr("admin");
			String username = admin.getStr("username");
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currenttime =  dt.format(new Date());
			
			Integer id = getParaToInt("id");
			String  splitCode = getPara("splitCode");
			Integer totalNum = getParaToInt("totalNum");
			Integer splitNum = getParaToInt("splitNum");
			Record record = Db.findById("f_purchasem", id);//原采购单
			Record recordNew = new Record().setColumns(record);//拆分出的采购单  使用深拷贝
			recordNew.set("id", null);
			recordNew.set("lossNum", 0);
			recordNew.set("totalNum", splitNum);
			recordNew.set("code", splitCode);
			recordNew.set("username", username);
			recordNew.set("optime", currenttime);
			
			record.set("totalNum", totalNum-splitNum);
			record.set("eusername", username);
			record.set("eoptime", currenttime);
			boolean result = Db.tx(new IAtom() {
				
				@Override
				public boolean run() throws SQLException {
					boolean result1 = Db.update("f_purchasem", record);
					boolean result2 = Db.save("f_purchasem", recordNew);
					return result1&&result2;
				}
			});
			renderJson(result);
		}
/**
 * 采购列表编辑
 */
      public void purchaseEdit() {
    	Integer id = getParaToInt(0);
    	List<Record> flowerlist = Db.find("select id,name from f_flower where state=1");
    	Record list = Db.findFirst("select b.name,a.id,a.code,a.channel,a.fid,a.price,a.num,a.totalNum,a.lossNum,a.purchaser from f_purchasem a left join f_flower b on a.fid = b.id where a.id = ? ", id);	
    	setAttr("flowerlist", flowerlist);
    	setAttr("list", list);
		render("purchase_edit.html");
	}
/**
 * 采购列表编辑保存      
 */
      public void purchaseSaveEdit() {
    	Integer id = getParaToInt("id");
		String code = getPara("code");
		Integer fid = getParaToInt("fid");
		Integer channel = getParaToInt("channel");
		
		BigDecimal price = new BigDecimal(getPara("price")).setScale(2, BigDecimal.ROUND_DOWN);
		Integer num = getParaToInt("num");
		Integer totalNum = getParaToInt("totalNum");
		Integer lossNum = getParaToInt("lossNum");
		String purchaser = getPara("purchaser");
		
		Record admin = getSessionAttr("admin");
  		String username=admin.getStr("username");//操作账号
  		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  		
  		Record record = new Record();
  		record.set("id", id);
  		record.set("code", code);
  		record.set("fid", fid);
  		record.set("channel", channel);
  		record.set("price", price);
  		record.set("num", num);
  		record.set("totalNum", totalNum);
  		record.set("lossNum", lossNum);
  		record.set("purchaser", purchaser);
  		record.set("username", username);
  		record.set("optime", dt.format(new Date()));
        boolean result = Db.update("f_purchasem", record);
        renderJson(result);
		
	}
/**
 * 采购列表增加      
 */
      public void purchaseAdd() {
      	List<Record> flowerlist = Db.find("select id,name from f_flower where state=1");
      	setAttr("flowerlist", flowerlist);
  		render("purchase_add.html");
  	}
/**
 * 采购列表增加保存      
 */
      public void purchaseSaveAdd() {
    	String code = getPara("code");
  		Integer fid = getParaToInt("fid");
  		Integer channel = getParaToInt("channel");
  		
  		BigDecimal price = new BigDecimal(getPara("price")).setScale(2, BigDecimal.ROUND_DOWN);
  		Integer num = getParaToInt("num");
  		Integer totalNum = getParaToInt("totalNum");
  		Integer lossNum = getParaToInt("lossNum");
  		String purchaser = getPara("purchaser");
  		
  		Record admin = getSessionAttr("admin");
		String username=admin.getStr("username");//操作账号
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Record record = new Record();
  		record.set("code", code);
  		record.set("fid", fid);
  		record.set("channel", channel);
  		record.set("price", price);
  		record.set("num", num);
  		record.set("totalNum", totalNum);
  		record.set("lossNum", lossNum);
  		record.set("purchaser", purchaser);
  		record.set("username", username);
  		record.set("optime", dt.format(new Date()));
  		
  	  boolean result = Db.save("f_purchasem", record);
      renderJson(result);
		
	}
/**
 * 采购列表删除      
 */
      public void purchaseDel() {
    	  Integer id = getParaToInt(0);
    	  boolean result = Db.deleteById("f_purchasem", id);
    	  renderJson(result);
	}
/**
 * 平台销售列表      
 * @throws UnsupportedEncodingException
 */
    public void platsaleList() throws UnsupportedEncodingException {
        Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
        String code = getPara("code");
        String name =  getPara("name");
        Page<Record> page = PurchaseDao.getPlatsaleList(pageno,16,code,name);
        setAttr("pageno", page.getPageNumber());
        setAttr("totalpage", page.getTotalPage());
        setAttr("totalrow", page.getTotalRow());
        setAttr("platsalelist", page.getList());
        setAttr("code", code);
        setAttr("name", name);
        render("platsale_list.html");
    }
/**
 * 平台外销售列表    
 * @throws UnsupportedEncodingException
 */
    public void excplatsaleList() throws UnsupportedEncodingException {
    	Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
    	String code = getPara("code");
    	String stime = getPara("stime");
    	String etime = getPara("etime");
    	Integer type = getParaToInt("type")==null?10:getParaToInt("type");
    	Integer type_detail = getParaToInt("type_detail")==null?10:getParaToInt("type_detail");
    	Page<Record> page = PurchaseDao.getExcplatsaleList(pageno,16,code,stime,etime,type,type_detail);
    	List<Record> purchasehlist = page.getList();
		for (Record purchaseh : purchasehlist) {
			String addr = Db.queryStr("SELECT GROUP_CONCAT(NAME ORDER BY pid ASC SEPARATOR '-') as addr FROM f_area WHERE id IN ("+ purchaseh.getStr("area") +")");
			purchaseh.set("addr", addr+"-"+purchaseh.getStr("address"));
		}
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("excplatsalelist", purchasehlist);
		setAttr("code", code);
		setAttr("type_detail", type_detail);
		setAttr("stime",stime);
		setAttr("etime",etime);
		setAttr("type", type);
    	render("excplatsale_list.html");
	}
/**
 * 平台外销售列表增加    
 */
    public void excplatsaleAdd() {
    	List<Record> flowerlist = Db.find("select id,name from f_flower where state=1");
		String areas =  FCDao.getArea();
		setAttr("arealist", areas);
    	setAttr("flowerlist", flowerlist);
		render("excplatsale_add.html");
	}
/**
 * 平台外销售列表增加保存    
 */
    public void excplatsaleSaveAdd() {
    	Integer fid;
    	String code = getPara("code");
    	Integer type_detail = getParaToInt("type_detail");
    	if (type_detail==0) {
    		 fid = getParaToInt("fid");
		}else {
			 fid = getParaToInt("ptid");
		}	
  		Integer type = getParaToInt("type");
  		Integer num = getParaToInt("num");
        String htime = getPara("htime");
        String address = getPara("address"); 
		String area = getPara("prov")+","+getPara("city")+","+getPara("county");

  		Record admin = getSessionAttr("admin");
		String username=admin.getStr("username");//操作账号
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Record record = new Record();
  		record.set("code", code);
  		record.set("type_detail", type_detail);
  		record.set("fid", fid);
  		record.set("type", type);
  		record.set("htime", htime);
  		record.set("num", num);
  		record.set("address",address);
		record.set("area", area);
  		record.set("username", username);
  		record.set("optime", dt.format(new Date()));
  		
  	  boolean result = Db.save("f_purchaseh", record);
      renderJson(result);		
	}
/**
 * 平台外销售列表编辑    
 */
    public void excplatsaleEdit() {
    	Integer id = getParaToInt(0);
    	List<Record> flowerlist = Db.find("select id,name from f_flower where state=1");
    	String arealist =  FCDao.getArea();
    	Integer type_detail = Db.queryInt("select type_detail from f_purchaseh where id = ?",id);
    	Record list;
    	if (type_detail==0) {
       	 list = Db.findFirst("select b.name,a.id,a.code,a.htime,a.area,a.address,a.num,a.type,a.fid FROM f_purchaseh a left join f_flower b on a.fid = b.id  where a.id = ? ", id);
		}else {
	       	 list = Db.findFirst("select b.fname,a.id,a.code,a.htime,a.area,a.address,a.num,a.type,a.fid FROM f_purchaseh a left join f_product b on a.fid = b.id  where a.id = ? ", id);
		}
		String area = list.getStr("area");
 		String[] areas = area.split(",");
 		
 		Record prov = Db.findById("f_area", areas[0]);
 		Record city = Db.findById("f_area", areas[1]);
 		Record county = Db.findById("f_area", areas[2]);
		setAttr("type_detail", type_detail);
 		setAttr("arealist", arealist);
 		setAttr("prov", prov);
 		setAttr("city", city);
 		setAttr("county", county);
    	setAttr("flowerlist", flowerlist);
    	setAttr("list", list);
		render("excplatsale_edit.html");
	}
/**
 * 平台外销售列表编辑保存    
 */
    public void excplatsaleSaveEdit() {
    	String code = getPara("code");
    	Integer id = getParaToInt("id");
    	Integer type_detail = getParaToInt("type_detail");
  		Integer fid ;
  		if (type_detail==0) {
   		 fid = getParaToInt("fid");
		}else {
			 fid = getParaToInt("ptid");
		}
  		Integer type = getParaToInt("type");
  		Integer num = getParaToInt("num");
        String htime = getPara("htime");
        String address = getPara("address"); 
		String area = getPara("prov")+","+getPara("city")+","+getPara("county");

  		Record admin = getSessionAttr("admin");
		String username=admin.getStr("username");//操作账号
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Record record = new Record();
		record.set("id", id);
  		record.set("code", code);
  		record.set("fid", fid);
  		record.set("type", type);
  		record.set("htime", htime);
  		record.set("num", num);
  		record.set("address",address);
		record.set("area", area);
  		record.set("username", username);
  		record.set("optime", dt.format(new Date()));
  		record.set("type_detail", type_detail);
  		
  	  boolean result = Db.update("f_purchaseh", record);
      renderJson(result);		
	}
/**
 * 平台外销售列表删除    
 */
    public void excplatsaleDel() {
  	  Integer id = getParaToInt(0);
  	  boolean result = Db.deleteById("f_purchaseh", id);
  	  renderJson(result);
	}
/**
 * 核销列表    
 * @throws UnsupportedEncodingException 
 */
    public void compareList() throws UnsupportedEncodingException {
    	Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
    	String code = getPara("code");
    	String name =  getPara("name");
    	Page<Record> page = PurchaseDao.getCompareList(pageno,16,code,name);	
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("camparelist",page.getList());
		setAttr("code", code);
		setAttr("name", name);
    	render("compare_list.html");
	}
/**
 * 核销数据生成    
 */
    public void compare() {
    	String code = getPara("code");
    	String sqlColumnSelect = "select A.code,A.fid,A.channel,A.price,A.num,A.totalNum,sprice as singlePrice,(A.price*A.totalNum) as totalPrice,round((IFNULL(A.lossNum,0)*100/IFNULL(A.purNum,0)),2) as lossPercent ,IFNULL(A.purNum,0) as purNum,IFNULL(A.lossNum,0) as lossNum,(IFNULL(B.aNum,0)+IFNULL(C.bNum,0)+IFNULL(D.bNum,0)) as saleNum,(IFNULL(A.purNum,0)-IFNULL(A.lossNum,0)-IFNULL(B.aNum,0)-IFNULL(C.bNum,0)-IFNULL(D.bNum,0)) as difNum   ";
    	String sqlExceptionSelect = " from ((select sum(num*totalNum) as purNum,(sum(num*totalNum)/sum(totalNum)) as num,sum(lossNum) as lossNum,fid,code,IFNULL(sum(price*totalNum)/sum(totalNum),0) price,IFNULL(sum(price*totalNum)/sum(num*totalNum),0) sprice,sum(totalNum) totalNum,case when count(fid)>1 then 5 else channel end as channel from f_purchasem group by fid,code) as A)" + 
        		" left join" + 
        		" ((select sum(c.num) as aNum,  c.fid,a.code from f_order_info a left join f_order_pro b on a.id = b.oid left join f_product_info c on b.pid = c.pid where  b.type = 0 group by a.code,c.fid)as B)" + 
        		" on A.code = B.code and A.fid=B.fid" + 
        		" left join " + 
        		" ((select sum(num) as bNum,code,fid  from f_purchaseh where type_detail=0 group by code,fid) as C)" + 
        		" on A.code = C.code and A.fid = C.fid "+
        		" left join " + 
        		" ((select  sum(b.num*a.num) as bNum,b.fid,a.code from f_purchaseh a left join f_product_info b on a.fid = b.pid where a.type_detail=1 group by a.code,b.fid) as D)" + 
        		" on A.code = D.code and A.fid = D.fid "+
        		"where A.code = '"+code+"'" ;
    	List<Record>recordList = Db.find(sqlColumnSelect+sqlExceptionSelect);
		Db.update("delete from f_purchasex ");
		boolean result=false;
		int[] size = Db.batchSave("f_purchasex", recordList, 100);
			 if(size!=null&&size.length>0){
				 result=true;
				 }
    	renderJson(result);
	}
/**
 * 核销数据导出    
 * @throws UnsupportedEncodingException 
 */
    public void exportCompare() throws UnsupportedEncodingException {
    	String name = getPara("name");
    	PurchaseDao.getCompareForExcel(getResponse(),name);
    	renderNull();
	}
/**
 * 花材损耗列表	
 * @throws UnsupportedEncodingException
 */
      public void lossList() throws UnsupportedEncodingException {
    	Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
    	String code = getPara("code");
    	String name =  getPara("name");
    	Page<Record> page = PurchaseDao.getLossList(pageno,16,code,name);
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("losslist", page.getList());
		setAttr("code", code);
		setAttr("name", name);
    	render("loss_list.html");
	}
/**
 * 按批次获取产品      
 */
      public void getProduct() {
    	  String code = getPara(0);
    	  List<Record>list = Db.find("select *from f_product where code = ?",code);
    	  renderJson(list);
      }
/**
 * 出货日成本统计      
 */
      public void totalList() {
    	  String  code_start=getPara("code_start");
    	  String  code_end=getPara("code_end");
    	  String sql= "SELECT F.totalprice as zyf,E1.code, A1.totalprice b1,A1.cnt c1,A1.singleprice d1,A2.totalprice b2,A2.cnt c2,A2.singleprice d2,A3.totalprice b3,A3.cnt c3,A3.singleprice d3,A4.totalprice b4,A4.cnt c4,A4.singleprice d4,A5.totalprice b5,A5.cnt c5,A5.singleprice d5,A6.totalprice b6,A6.cnt c6,A6.singleprice d6,A7.totalprice b7,A7.cnt c7,A7.singleprice d7,A8.totalprice b8,A8.cnt c8,A8.singleprice d8,A9.totalprice b9,A9.cnt c9,A9.singleprice d9,ROUND(IFNULL(B1.lossPrice,0),2) lossPrice,ROUND(IFNULL(C1.sumprice,0)+IFNULL(C2.sumprice,0),2) sum1,ROUND(IFNULL(D1.sumprice,0)+IFNULL(D2.sumprice,0),2) sum2  FROM" + 
    	  		" (SELECT DISTINCT(code) from f_order_info) AS E1" + 
    	  		" LEFT JOIN" + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=1   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=1) AS A1 " + 
    	  		"ON E1.code = A1.code " + 
    	  		"LEFT JOIN " + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and  b.`type`=2   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=2) AS A2 " + 
    	  		"ON E1.code=A2.code " + 
    	  		"LEFT JOIN " + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=3   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=3) AS A3 " + 
    	  		"ON E1.code=A3.code " + 
    	  		"LEFT JOIN " + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=4   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=4) AS A4 " +  
    	  		"ON E1.code=A4.code " + 
    	  		"LEFT JOIN " + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=5   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=5) AS A5 " + 
    	  		"ON E1.code=A5.code " + 
    	  		"LEFT JOIN " + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=6   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=6) AS A6 " + 
    	  		"ON E1.code=A6.code " + 
    	  		"LEFT JOIN " + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=7   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=7) AS A7 " + 
    	  		"ON E1.code=A7.code " + 
    	  		"LEFT JOIN " + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=8   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=8) AS A8 " + 
    	  		"ON E1.code=A8.code " + 
    	  		"LEFT JOIN " + 
    	  		" (SELECT  A.code,ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2) as totalprice,(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)) cnt, ROUND(ROUND(IFNULL(A.totalprice,0)+IFNULL(C.totalprice,0),2)/(IFNULL(B.cnt,0)+IFNULL(D.cnt,0)),2) as singleprice,A.type FROM  " + 
    	  		" (SELECT  c.`type`,a.code,sum(e.price*d.num) totalprice FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  LEFT JOIN f_product_info d on c.id = d.pid LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) e on d.fid = e.fid and a.code = e.code WHERE  b.`type`=0  GROUP by c.`type`,a.code)AS A " + 
    	  		" LEFT JOIN " + 
    	  		"(SELECT COUNT(1) cnt, c.`type`,a.code FROM f_order_info a  LEFT JOIN f_order_pro b on a.id =b.oid  LEFT JOIN f_product c on b.pid = c.id  WHERE  b.`type`=0  GROUP by c.`type`,a.code ) AS B " + 
    	  		"ON A.type = B.type and A.code = B.code "+
                "left join " + 
                " (select  b.`type`,a.code,sum(d.price*c.num*a.num) totalprice  from  f_purchaseh  a  left join f_product b on a.fid = b.id  left join f_product_info c on b.id = c.pid  LEFT JOIN (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) d on c.fid = d.fid and a.code = d.code   where a.type_detail=1   GROUP by b.`type`,a.code) as C" + 
                " on A.type = C.type  and A.code = C.code" + 
                " left join" + 
                " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=9   GROUP by b.`type`,a.code )as D" + 
                " on A.type = D.type  and A.code = D.code"+
    	  		" WHERE A.type=9) AS A9 " + 
    	  		" ON E1.code=A9.code " + 
    	  		" left join " + 
    	  		"(select a.code, sum(a.price*a.lossNum) lossPrice FROM (select sum(price*totalNum)/sum(totalNum*num) price,sum(lossNum) lossNum,fid,code from f_purchasem  group by fid,code) as a  group by code) as B1 " + 
    	  		"on E1.code=B1.code " + 
    	  		" left join " + 
    	  		"(select  sum(a.aNum*b.price) sumprice,a.code from   (select  sum(num) as aNum,fid,code from f_purchaseh  where type_detail=0 and `type`=0 group by code,fid) a left join (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) b on a.code = b.code and a.fid = b.fid group by a.code) as C1 " + 
    	  		"on E1.code=C1.code " + 
    	  		" left join " + 
    	  		"(select sum(a.aNum*b.price) sumprice,a.code from (select  sum(b.num*a.num) as aNum,b.fid,a.code from f_purchaseh a left join f_product_info b on a.fid = b.pid where a.type_detail=1 and a.`type` =0 group by a.code,b.fid) a left join (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) b on a.code = b.code and a.fid = b.fid group by a.code ) as C2 " + 
    	  		"on E1.code=C2.code " + 
    	  		" left join " + 
    	  		"(select  sum(a.aNum*b.price) sumprice,a.code from   (select  sum(num) as aNum,fid,code from f_purchaseh  where type_detail=0 and `type`=2 group by code,fid) a left join (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) b on a.code = b.code and a.fid = b.fid group by a.code ) as D1 " + 
    	  		"on E1.code=D1.code" +
    	  		" left join " + 
    	  		"(select sum(a.aNum*b.price) sumprice,a.code from (select  sum(b.num*a.num) as aNum,b.fid,a.code from f_purchaseh a left join f_product_info b on a.fid = b.pid where a.type_detail=1 and a.`type` =2 group by a.code,b.fid) a left join (select IFNULL(sum(price*totalNum)/sum(num*totalNum),0) price, code,fid from f_purchasem  group by fid,code) b on a.code = b.code and a.fid = b.fid group by a.code ) as D2 " + 
    	  		" on E1.code=D2.code " +
    	  		" left join " + 
    	  		"( select  sum(a.price*a.totalNum) as totalprice,a.code from f_purchasem a left join f_flower b on a.fid = b.id  where b.name in ('包装费','运输费')  group by a.code ) As F "+
    	  		" on E1.code = F.code  where 1=1 ";
    	  if (StrKit.notBlank(code_start)) {
			sql+=" and E1.code>='"+code_start+"'";
		}
    	  if (StrKit.notBlank(code_end)) {
  			sql+=" and E1.code<='"+code_end+"'";
  		}
    	  if (StrKit.isBlank(code_start)&&StrKit.isBlank(code_end)) {
    		  setAttr("code_start", code_start);
        	  setAttr("code_end", code_end);
        	  render("totallist.html");
        	  return;
    	  }else {
			 sql +=" order by E1.code desc";
	          List<Record>list = Db.find(sql);
	    	  setAttr("list", list);
		}
    	 
    	  setAttr("code_start", code_start);
    	  setAttr("code_end", code_end);
    	  render("totallist.html");
	}
      public void exporttotalList() {
    	  String  code_start=getPara("code_start");
    	  String  code_end=getPara("code_end");
    	  boolean result = PurchaseDao.getexporttotallistForExcel(getResponse(),code_start,code_end);
	  		if(result){
	  			renderNull();
	  		}
	}
      /*************************物流成本统计*************************************/
     public void wlcostlist() {
 		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
 		String code_start = getPara("code_start"),code_end=getPara("code_end"),ename= getPara("ename")==null?"":getPara("ename");
 		Page<Record>page =PurchaseDao.getWlcostlist(pageno, 16, code_start, code_end, ename);
 		List<Record> wuliulist = MCDao.getWuliu();
 		setAttr("wuliulist", wuliulist);
 		setAttr("pageno", page.getPageNumber());
 		setAttr("totalpage", page.getTotalPage());
 		setAttr("totalrow", page.getTotalRow());
 		setAttr("list", page.getList());
 		
 		setAttr("code_start",code_start);
 		setAttr("code_end", code_end);
 		setAttr("ename", ename); 
		render("wlcostlist.html");
	} 
}
