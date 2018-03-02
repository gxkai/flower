package com.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class PurchaseDao {
	public static Page<Record>getPurchaseList(int pageno, int pagesize,String code,String name,String channel) throws UnsupportedEncodingException{
		String sqlColumnSelect = "SELECT b.name, a.id  , a.code ,case a.channel when 1 then '上海' when 2 then '拍市' when 3 then '市场' when 4 then '基地' end as channel ,a.price,a.num,a.totalNum,(a.price*a.totalNum) totalPrice, a.lossNum,a.purchaser,a.username,a.optime ";
		String sqlExceptionSelect = "FROM f_purchasem a LEFT JOIN f_flower b on a.fid = b.id where 1=1";
		if (StrKit.notBlank(code)) {
			sqlExceptionSelect += String.format(" and code = '%s'", code);
		}
		if (StrKit.notBlank(name)) {
			sqlExceptionSelect += String.format(" and name like '%s'","%"+URLDecoder.decode( name, "utf-8")+"%");
		}
		if (StrKit.notBlank(channel)) {
			sqlExceptionSelect += String.format(" and channel in (%s)", channel);
		}
		if (code==null) {
			String code_new = Db.queryStr("SELECT code FROM f_purchasem ORDER BY code DESC LIMIT 1");
			sqlExceptionSelect = String.format("FROM f_purchasem a LEFT JOIN f_flower b on a.fid = b.id where a.code = '%s'", code_new);
		}
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	public static Number getSumPrice(String code,String name,String channel ) throws UnsupportedEncodingException{
		String sqlColumnSelect = "SELECT sum(a.price*a.totalNum) sumPrice ";
		String sqlExceptionSelect = " FROM f_purchasem a LEFT JOIN f_flower b on a.fid = b.id where 1=1";
		if (StrKit.notBlank(code)) {
			sqlExceptionSelect += String.format(" and code = '%s'", code);
		}
		if (StrKit.notBlank(name)) {
			sqlExceptionSelect += String.format(" and name like '%s'","%"+URLDecoder.decode( name, "utf-8")+"%");
		}
		if (StrKit.notBlank(channel)) {
			sqlExceptionSelect += String.format(" and channel in (%s)", channel);
		}
		if (code==null) {
			String code_new = Db.queryStr("SELECT code FROM f_purchasem ORDER BY code DESC LIMIT 1");
			sqlExceptionSelect = String.format(" FROM f_purchasem a LEFT JOIN f_flower b on a.fid = b.id where a.code = '%s'", code_new);
		}
		Number sumPrice=Db.queryNumber(sqlColumnSelect+sqlExceptionSelect);
		return sumPrice;
	}

    public static Page<Record>getPlatsaleList(int pageno, int pagesize,String code,String name) throws UnsupportedEncodingException{

        String sqlExceptionSelect = "from f_order_info a left join f_order_pro b on a.id = b.oid left join f_product_info c on b.pid = c.pid left join f_flower d on c.fid = d.id  where  b.type = 0  ";
        String sqlColumnSelect = "select a.code, d.name,sum(c.num) sum,count(*) count ";
        if (StrKit.notBlank(code)) {
            sqlExceptionSelect += String.format(" and a.code = '%s'", code);
        }
        if (StrKit.notBlank(name)) {
        sqlExceptionSelect += String.format(" and d.name like '%s'","%"+URLDecoder.decode( name, "utf-8")+"%");
        }
        if (StrKit.isBlank(code)&&StrKit.isBlank(name)) {
        	String code_new = Db.queryStr("SELECT code FROM f_order_info ORDER BY code DESC LIMIT 1");
			sqlExceptionSelect = String.format("from f_order_info a left join f_order_pro b on a.id = b.oid left join f_product_info c on b.pid = c.pid left join f_flower d on c.fid = d.id   where  b.type = 0 and a.code = '%s'", code_new);
		}
        sqlExceptionSelect += " group by d.name,a.code order by a.code desc";
        Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
        return page;
    }
    public static Page<Record>getExcplatsaleList(int pageno, int pagesize,String code,String stime,String etime, int type , int type_detail) throws UnsupportedEncodingException{

        String sqlExceptionSelect = "FROM f_purchaseh  where 1=1 ";
        String sqlColumnSelect = "select id,type_detail,code,htime,area,address,num,username,optime,case type when 0  then '市场单' when 2 then '拍摄单'  else '线下单' end as type";
        if (StrKit.notBlank(code)) {
            sqlExceptionSelect += String.format(" and code = '%s'", code);
        }
        if (type!=10) {
            sqlExceptionSelect += String.format(" and type = %d", type);
        }
        if (type_detail!=10) {
            sqlExceptionSelect += String.format(" and type_detail = %d", type_detail);
        }
        if (StrKit.notBlank(stime)) {
            sqlExceptionSelect += String.format(" and htime >= '%s'", stime);
        }
        if (StrKit.notBlank(etime)) {
            sqlExceptionSelect += String.format(" and htime <= '%s'", etime);
        }
        if (StrKit.isBlank(code)&&StrKit.isBlank(stime)&&StrKit.isBlank(etime)&&type==10&&type_detail==10) {
        	String code_new = Db.queryStr("select code from f_purchaseh order by code desc limit 1");
        	sqlExceptionSelect = String.format("FROM f_purchaseh  where code = '%s'", code_new);
		}
        Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
        for(Record record :page.getList()) {
        	if (record.getInt("type_detail")==0) {
        		String name = Db.queryStr("select b.name from f_purchaseh a left join f_flower b on a.fid = b.id where a.id=?",record.getInt("id"));
				record.set("name",name );
			}else {
				String name = Db.queryStr("select b.fname from f_purchaseh a left join f_product b on a.fid = b.id where a.id=?",record.getInt("id"));
				record.set("name",name );
			}
        }
        return page;
    }
    public static Page<Record>getCompareList(int pageno, int pagesize,String code,String name) throws UnsupportedEncodingException{

        String sqlExceptionSelect = "from f_purchasex a left join f_flower b on a.fid = b.id where 1=1";
        String sqlColumnSelect = "select a.saleNum,a.difNum, a.code,b.name,a.totalNum,a.price,a.totalPrice,a.num,a.singlePrice,a.lossNum,a.lossPercent,case a.channel when 1 then '上海' when 2 then '拍市' when 3 then '市场' when 4 then '基地' else '随机' end as channel  ";    
        if (StrKit.notBlank(code)) {
            sqlExceptionSelect += String.format(" and a.code = '%s'", code);
        }
        if (StrKit.notBlank(name)) {
        sqlExceptionSelect += String.format(" and b.name like '%s'","%"+URLDecoder.decode( name, "utf-8")+"%");
        }
        //sqlExceptionSelect += " order by a.channel desc";
        Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
        return page;
    }
    public static  void getCompareForExcel(HttpServletResponse response,String name) throws UnsupportedEncodingException {
    	 String sqlExceptionSelect = "from f_purchasex a left join f_flower b on a.fid = b.id where 1=1";
         String sqlColumnSelect = "select a.saleNum,a.difNum, a.code,b.name,a.totalNum,a.price,a.totalPrice,a.num,a.singlePrice,a.lossNum,a.lossPercent,case a.channel when 1 then '上海' when 2 then '拍市' when 3 then '市场' when 4 then '基地' else '随机' end as channel   ";
         if (StrKit.notBlank(name)) {
         sqlExceptionSelect += String.format(" and b.name like '%s'","%"+URLDecoder.decode( name, "utf-8")+"%");
         }
        sqlExceptionSelect += " order by a.code desc";
    	String sql = sqlColumnSelect+sqlExceptionSelect;
    	List<Record> comparelist = Db.find(sql);
    	
    	HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet1 = wbook.createSheet();
		//设置列宽
		sheet1.setColumnWidth((short)0, (short)6400);
		sheet1.setColumnWidth((short)1, (short)6400);
		sheet1.setColumnWidth((short)2, (short)6400);
		sheet1.setColumnWidth((short)3, (short)6400);
		sheet1.setColumnWidth((short)4, (short)6400);
		sheet1.setColumnWidth((short)5, (short)6400);
		sheet1.setColumnWidth((short)6, (short)6400);
		sheet1.setColumnWidth((short)7, (short)6400);
		sheet1.setColumnWidth((short)8, (short)6400);
		sheet1.setColumnWidth((short)9, (short)6400);
		sheet1.setColumnWidth((short)10, (short)6400);
		sheet1.setColumnWidth((short)11, (short)6400);
		String sDateSuffix = Db.queryStr("select code from f_purchasex limit 1");
		wbook.setSheetName(0,sDateSuffix );
		//首行 样式1
        HSSFCellStyle cellStyle = wbook.createCellStyle();
        HSSFFont font = wbook.createFont();
        font.setColor(HSSFColor.RED.index);    //红字
        cellStyle.setFont(font);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index); //填亮黄色
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
        //样式2
        HSSFCellStyle cellStyle1 = wbook.createCellStyle();
        cellStyle1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
        cellStyle1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
        cellStyle1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
        cellStyle1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
       //首行 样式2
        HSSFCellStyle cellStyle2 = wbook.createCellStyle();
        font = wbook.createFont();
        font.setColor(HSSFColor.RED.index);    //红字
        cellStyle2.setFont(font);
        cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
        cellStyle2.setFillForegroundColor(HSSFColor.TAN.index); //填橘黄
        cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
          
		HSSFRow row;
		for(int i=0;i<comparelist.size();i++){
			if(i==0){
				row = sheet1.createRow(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle);
				cell0.setCellValue("批号");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("品名");
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("数量(扎)");
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("单扎价格(元)");
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle);
				cell4.setCellValue("合计(元)");
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle);
				cell5.setCellValue("单扎数量(枝)");
				HSSFCell cell6 = row.createCell((short) 6);
				cell6.setCellStyle(cellStyle);
				cell6.setCellValue("单枝价格(元)");
				HSSFCell cell7 = row.createCell((short) 7);
				cell7.setCellStyle(cellStyle);
				cell7.setCellValue("损耗数量(枝)");
				HSSFCell cell8 = row.createCell((short) 8);
				cell8.setCellStyle(cellStyle);
				cell8.setCellValue("发货数量(枝)");
				HSSFCell cell9 = row.createCell((short) 9);
				cell9.setCellStyle(cellStyle);
				cell9.setCellValue("差异数量(枝)");
				HSSFCell cell10 = row.createCell((short) 10);
				cell10.setCellStyle(cellStyle);
				cell10.setCellValue("损耗率");
				HSSFCell cell11 = row.createCell((short) 11);
				cell11.setCellStyle(cellStyle);
				cell11.setCellValue("进货渠道");
				
			}
			row = sheet1.createRow(i+1);
			Record r = comparelist.get(i);
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellStyle(cellStyle1);
			cell0.setCellValue(r.getStr("code"));
			HSSFCell cell1 = row.createCell((short) 1);
			cell1.setCellStyle(cellStyle1);
			cell1.setCellValue(r.getStr("name"));
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellStyle(cellStyle1);
			cell2.setCellValue(r.getInt("totalNum"));
			HSSFCell cell3 = row.createCell((short) 3);
			cell3.setCellStyle(cellStyle1);
			cell3.setCellValue(r.getBigDecimal("price")+"");
			HSSFCell cell4 = row.createCell((short) 4);
			cell4.setCellStyle(cellStyle1);
			cell4.setCellValue(r.getBigDecimal("totalPrice")+"");
			HSSFCell cell5 = row.createCell((short) 5);
			cell5.setCellStyle(cellStyle1);
			cell5.setCellValue(r.getInt("num"));
			HSSFCell cell6 = row.createCell((short) 6);
			cell6.setCellStyle(cellStyle1);
			cell6.setCellValue(r.getBigDecimal("singlePrice")+"");
			HSSFCell cell7 = row.createCell((short) 7);
			cell7.setCellStyle(cellStyle1);
			cell7.setCellValue(r.getInt("lossNum"));
			HSSFCell cell8 = row.createCell((short) 8);
			cell8.setCellStyle(cellStyle1);
			cell8.setCellValue(r.getInt("saleNum"));
			HSSFCell cell9 = row.createCell((short) 9);
			cell9.setCellStyle(cellStyle1);
			cell9.setCellValue(r.getInt("difNum"));
			HSSFCell cell10 = row.createCell((short) 10);
			cell10.setCellStyle(cellStyle1);
			cell10.setCellValue(r.getBigDecimal("lossPercent")+"%");
			HSSFCell cell11 = row.createCell((short) 11);
			cell11.setCellStyle(cellStyle1);
			cell11.setCellValue(r.getStr("channel"));
		}
		response.addHeader("Content-Disposition", "attachment;filename=" +new String("核销列表".getBytes(),"iso-8859-1")+sDateSuffix+ ".xls");
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
    public static Page<Record>getLossList(int pageno, int pagesize,String code,String name) throws UnsupportedEncodingException{
		String sqlColumnSelect = "SELECT b.name,a.code ,round((a.price*a.lossNum),2) lossPrice,round(a.lossNum*100/a.sumNum,2) lossPercent ";
		String sqlExceptionSelect = "FROM (select sum(price*totalNum)/sum(totalNum*num) price,sum(lossNum) lossNum,sum(totalNum*num) as sumNum,fid,code from f_purchasem  group by fid,code)as a LEFT JOIN f_flower b on a.fid = b.id where 1=1";
		if (StrKit.notBlank(code)) {
			sqlExceptionSelect += String.format(" and a.code = '%s'", code);
		}
		if (StrKit.notBlank(name)) {
			sqlExceptionSelect += String.format(" and b.name like '%s'","%"+URLDecoder.decode( name, "utf-8")+"%");
		}
		if (StrKit.isBlank(code)&&StrKit.isBlank(name)) {
			String code_new = Db.queryStr("SELECT code FROM f_purchasem ORDER BY code DESC LIMIT 1");
			sqlExceptionSelect = String.format("FROM (select sum(price*totalNum)/sum(totalNum*num) price,sum(lossNum) lossNum,sum(totalNum*num) as sumNum,fid,code from f_purchasem  group by fid,code) a LEFT JOIN f_flower b on a.fid = b.id where a.code = '%s'", code_new);
		}
		sqlExceptionSelect += " order by a.code,round(a.lossNum*100/a.sumNum,2) desc ";
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
    
    public static boolean getexporttotallistForExcel(HttpServletResponse response,String code_start,String code_end)  {
   	  String sql= "SELECT IFNULL(F.totalprice,0) as zyf,E1.code,IFNULL(A1.totalprice,0) b1,IFNULL(A1.cnt,0) c1,IFNULL(A1.singleprice,0) d1,IFNULL(A2.totalprice,0) b2,IFNULL(A2.cnt,0) c2,IFNULL(A2.singleprice,0) d2,IFNULL(A3.totalprice,0) b3,IFNULL(A3.cnt,0) c3,IFNULL(A3.singleprice,0) d3,IFNULL(A4.totalprice,0) b4,IFNULL(A4.cnt,0) c4,IFNULL(A4.singleprice,0) d4,IFNULL(A5.totalprice,0) b5,IFNULL(A5.cnt,0) c5,IFNULL(A5.singleprice,0) d5,IFNULL(A6.totalprice,0) b6,IFNULL(A6.cnt,0) c6,IFNULL(A6.singleprice,0) d6,IFNULL(A7.totalprice,0) b7,IFNULL(A7.cnt,0) c7,IFNULL(A7.singleprice,0) d7,IFNULL(A8.totalprice,0) b8,IFNULL(A8.cnt,0) c8,IFNULL(A8.singleprice,0) d8,IFNULL(A9.totalprice,0) b9,IFNULL(A9.cnt,0) c9,IFNULL(A9.singleprice,0) d9,ROUND(IFNULL(B1.lossPrice,0),2) lossPrice,ROUND(IFNULL(C1.sumprice,0)+IFNULL(C2.sumprice,0),2) sum1,ROUND(IFNULL(D1.sumprice,0)+IFNULL(D2.sumprice,0),2) sum2  FROM" + 
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
        " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and a.type=1 and b.`type`=2   GROUP by b.`type`,a.code )as D" + 
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
        " (select sum(a.num) cnt,a.code,b.`type` from f_purchaseh  a  left join f_product b on a.fid = b.id     where a.type_detail=1 and b.`type`=9   GROUP by b.`type`,a.code )as D" + 
        " on A.type = D.type  and A.code = D.code"+
  		" WHERE A.type=9) AS A9 " + 
  		"ON E1.code=A9.code " + 
  		"left join " + 
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
  		" on E1.code=D2.code "+
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
   		  sql+=" and E1.code='"+Db.queryStr("select distinct(code) from f_order_info order by code desc limit 1")+"'";
		}
   	  sql +=" order by E1.code desc";
        List<Record>list = Db.find(sql);
		HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet1 = wbook.createSheet();
		//设置列宽
		sheet1.setColumnWidth((short)0, (short)2400);
		sheet1.setColumnWidth((short)1, (short)2400);
		sheet1.setColumnWidth((short)2, (short)2400);
		sheet1.setColumnWidth((short)3, (short)2400);
		sheet1.setColumnWidth((short)4, (short)2400);
		sheet1.setColumnWidth((short)5, (short)2400);
		sheet1.setColumnWidth((short)6, (short)2400);
		sheet1.setColumnWidth((short)7, (short)2400);
		sheet1.setColumnWidth((short)8, (short)2400);
		sheet1.setColumnWidth((short)9, (short)2400);
		sheet1.setColumnWidth((short)10, (short)2400);
		sheet1.setColumnWidth((short)11, (short)2400);
		sheet1.setColumnWidth((short)12, (short)2400);
		sheet1.setColumnWidth((short)13, (short)2400);
		sheet1.setColumnWidth((short)14, (short)2400);
		sheet1.setColumnWidth((short)15, (short)2400);
		sheet1.setColumnWidth((short)16, (short)2400);
		sheet1.setColumnWidth((short)17, (short)2400);
		sheet1.setColumnWidth((short)18, (short)2400);
		sheet1.setColumnWidth((short)19, (short)2400);
		sheet1.setColumnWidth((short)20, (short)2400);
		sheet1.setColumnWidth((short)21, (short)2400);
		sheet1.setColumnWidth((short)22, (short)2400);
		sheet1.setColumnWidth((short)23, (short)2400);
		sheet1.setColumnWidth((short)24, (short)2400);
		sheet1.setColumnWidth((short)25, (short)2400);
		sheet1.setColumnWidth((short)26, (short)2400);
		sheet1.setColumnWidth((short)27, (short)2400);
		sheet1.setColumnWidth((short)28, (short)2400);
		sheet1.setColumnWidth((short)29, (short)2400);
		sheet1.setColumnWidth((short)30, (short)2400);
		sheet1.setColumnWidth((short)31, (short)2400);
		wbook.setSheetName(0, "出货成本"+code_start+"-"+code_end);
		//首行 样式1
        HSSFCellStyle cellStyle = wbook.createCellStyle();
        HSSFFont font = wbook.createFont();
        font.setColor(HSSFColor.RED.index);    //红字
        cellStyle.setFont(font);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index); //填亮黄色
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
        //样式2
        HSSFCellStyle cellStyle1 = wbook.createCellStyle();
        cellStyle1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框    
        cellStyle1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框    
        cellStyle1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框    
        cellStyle1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框    
       //首行 样式2
        HSSFCellStyle cellStyle2 = wbook.createCellStyle();
        font = wbook.createFont();
        font.setColor(HSSFColor.RED.index);    //红字
        cellStyle2.setFont(font);
        cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
        cellStyle2.setFillForegroundColor(HSSFColor.TAN.index); //填橘黄
        cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中    
          
		HSSFRow row;
		for(int i=0;i<list.size();i++){
			if(i==0){
				row = sheet1.createRow(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle);
				cell0.setCellValue("批号");
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("双品成本");
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("双品单数");
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("每单成本");
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle);
				cell4.setCellValue("多品成本");
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle);
				cell5.setCellValue("多品单数");
				HSSFCell cell6 = row.createCell((short) 6);
				cell6.setCellStyle(cellStyle);
				cell6.setCellValue("每单成本");
				HSSFCell cell7 = row.createCell((short) 7);
				cell7.setCellStyle(cellStyle);
				cell7.setCellValue("送花成本");
				HSSFCell cell8 = row.createCell((short) 8);
				cell8.setCellStyle(cellStyle);
				cell8.setCellValue("送花单数");
				HSSFCell cell9 = row.createCell((short) 9);
				cell9.setCellStyle(cellStyle);
				cell9.setCellValue("每单成本");
				HSSFCell cell10 = row.createCell((short) 10);
				cell10.setCellStyle(cellStyle);
				cell10.setCellValue("节日成本");
				HSSFCell cell11 = row.createCell((short) 11);
				cell11.setCellStyle(cellStyle);
				cell11.setCellValue("节日单数");
				HSSFCell cell12 = row.createCell((short) 12);
				cell12.setCellStyle(cellStyle);
				cell12.setCellValue("每单成本");
				HSSFCell cell13 = row.createCell((short) 13);
				cell13.setCellStyle(cellStyle);
				cell13.setCellValue("定制成本");
				HSSFCell cell14 = row.createCell((short) 14);
				cell14.setCellStyle(cellStyle);
				cell14.setCellValue("定制单数");
				HSSFCell cell15 = row.createCell((short) 15);
				cell15.setCellStyle(cellStyle);
				cell15.setCellValue("每单成本");
				HSSFCell cell16 = row.createCell((short) 16);
				cell16.setCellStyle(cellStyle);
				cell16.setCellValue("闪购成本");
				HSSFCell cell17 = row.createCell((short) 17);
				cell17.setCellStyle(cellStyle);
				cell17.setCellValue("闪购单数");
				HSSFCell cell18 = row.createCell((short) 18);
				cell18.setCellStyle(cellStyle);
				cell18.setCellValue("每单成本");
				HSSFCell cell19 = row.createCell((short) 19);
				cell19.setCellStyle(cellStyle);
				cell19.setCellValue("主题成本");
				HSSFCell cell20 = row.createCell((short) 20);
				cell20.setCellStyle(cellStyle);
				cell20.setCellValue("主题单数");
				HSSFCell cell21 = row.createCell((short) 21);
				cell21.setCellStyle(cellStyle);
				cell21.setCellValue("每单成本");
				
				HSSFCell cell22 = row.createCell((short) 22);
				cell22.setCellStyle(cellStyle);
				cell22.setCellValue("拼团成本");
				HSSFCell cell23 = row.createCell((short) 23);
				cell23.setCellStyle(cellStyle);
				cell23.setCellValue("拼团单数");
				HSSFCell cell24 = row.createCell((short) 24);
				cell24.setCellStyle(cellStyle);
				cell24.setCellValue("每单成本");
				
				HSSFCell cell25 = row.createCell((short) 25);
				cell25.setCellStyle(cellStyle);
				cell25.setCellValue("玫瑰套餐成本");
				HSSFCell cell26 = row.createCell((short) 26);
				cell26.setCellStyle(cellStyle);
				cell26.setCellValue("玫瑰套餐单数");
				HSSFCell cell27 = row.createCell((short) 27);
				cell27.setCellStyle(cellStyle);
				cell27.setCellValue("每单成本");
				
				HSSFCell cell28 = row.createCell((short) 28);
				cell28.setCellStyle(cellStyle);
				cell28.setCellValue("损耗");
				HSSFCell cell29 = row.createCell((short) 29);
				cell29.setCellStyle(cellStyle);
				cell29.setCellValue("市场");
				HSSFCell cell30 = row.createCell((short) 30);
				cell30.setCellStyle(cellStyle);
				cell30.setCellValue("拍摄");
				HSSFCell cell31 = row.createCell((short) 31);
				cell31.setCellStyle(cellStyle);
				cell31.setCellValue("杂运费");
				
				
			}
			row = sheet1.createRow(i+1);
			Record r = list.get(i);
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellStyle(cellStyle1);
			cell0.setCellValue(r.getStr("code"));
			HSSFCell cell1 = row.createCell((short) 1);
			cell1.setCellStyle(cellStyle1);
			cell1.setCellValue(r.getBigDecimal("b1").toString());
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellStyle(cellStyle1);
			cell2.setCellValue(r.getBigDecimal("c1").toString());
			HSSFCell cell3 = row.createCell((short) 3);
			cell3.setCellStyle(cellStyle1);
			cell3.setCellValue(r.getBigDecimal("d1").toString());
			HSSFCell cell4 = row.createCell((short) 4);
			cell4.setCellStyle(cellStyle1);
			cell4.setCellValue(r.getBigDecimal("b2").toString());
			HSSFCell cell5 = row.createCell((short) 5);
			cell5.setCellStyle(cellStyle1);
			cell5.setCellValue(r.getBigDecimal("c2").toString());
			HSSFCell cell6 = row.createCell((short) 6);
			cell6.setCellStyle(cellStyle1);
			cell6.setCellValue(r.getBigDecimal("d2").toString());	
			
			HSSFCell cell7 = row.createCell((short) 7);
			cell7.setCellStyle(cellStyle1);
			cell7.setCellValue(r.getBigDecimal("b3").toString());
			HSSFCell cell8 = row.createCell((short) 8);
			cell8.setCellStyle(cellStyle1);
			cell8.setCellValue(r.getBigDecimal("c3").toString());
			HSSFCell cell9 = row.createCell((short) 9);
			cell9.setCellStyle(cellStyle1);
			cell9.setCellValue(r.getBigDecimal("d3").toString());	
			
			HSSFCell cell10 = row.createCell((short) 10);
			cell10.setCellStyle(cellStyle1);
			cell10.setCellValue(r.getBigDecimal("b4").toString());
			HSSFCell cell11 = row.createCell((short) 11);
			cell11.setCellStyle(cellStyle1);
			cell11.setCellValue(r.getBigDecimal("c4").toString());
			HSSFCell cell12 = row.createCell((short) 12);
			cell12.setCellStyle(cellStyle1);
			cell12.setCellValue(r.getBigDecimal("d4").toString());
			
			HSSFCell cell13 = row.createCell((short) 13);
			cell13.setCellStyle(cellStyle1);
			cell13.setCellValue(r.getBigDecimal("b5").toString());
			HSSFCell cell14 = row.createCell((short) 14);
			cell14.setCellStyle(cellStyle1);
			cell14.setCellValue(r.getBigDecimal("c5").toString());
			HSSFCell cell15 = row.createCell((short) 15);
			cell15.setCellStyle(cellStyle1);
			cell15.setCellValue(r.getBigDecimal("d5").toString());
			
			HSSFCell cell16 = row.createCell((short) 16);
			cell16.setCellStyle(cellStyle1);
			cell16.setCellValue(r.getBigDecimal("b6").toString());
			HSSFCell cell17 = row.createCell((short) 17);
			cell17.setCellStyle(cellStyle1);
			cell17.setCellValue(r.getBigDecimal("c6").toString());
			HSSFCell cell18 = row.createCell((short) 18);
			cell18.setCellStyle(cellStyle1);
			cell18.setCellValue(r.getBigDecimal("d6").toString());
			
			HSSFCell cell19 = row.createCell((short) 19);
			cell19.setCellStyle(cellStyle1);
			cell19.setCellValue(r.getBigDecimal("b7").toString());
			HSSFCell cell20 = row.createCell((short) 20);
			cell20.setCellStyle(cellStyle1);
			cell20.setCellValue(r.getBigDecimal("c7").toString());
			HSSFCell cell21 = row.createCell((short) 21);
			cell21.setCellStyle(cellStyle1);
			cell21.setCellValue(r.getBigDecimal("d7").toString());
			
			HSSFCell cell22 = row.createCell((short) 22);
			cell22.setCellStyle(cellStyle1);
			cell22.setCellValue(r.getBigDecimal("b8").toString());
			HSSFCell cell23 = row.createCell((short) 23);
			cell23.setCellStyle(cellStyle1);
			cell23.setCellValue(r.getBigDecimal("c8").toString());
			HSSFCell cell24 = row.createCell((short) 24);
			cell24.setCellStyle(cellStyle1);
			cell24.setCellValue(r.getBigDecimal("d8").toString());
			HSSFCell cell25 = row.createCell((short) 25);
			cell25.setCellStyle(cellStyle1);
			cell25.setCellValue(r.getBigDecimal("b9").toString());
			HSSFCell cell26 = row.createCell((short) 26);
			cell26.setCellStyle(cellStyle1);
			cell26.setCellValue(r.getBigDecimal("c9").toString());
			HSSFCell cell27 = row.createCell((short) 27);
			cell27.setCellStyle(cellStyle1);
			cell27.setCellValue(r.getBigDecimal("d9").toString());
			
			HSSFCell cell28 = row.createCell((short) 28);
			cell28.setCellStyle(cellStyle1);
			cell28.setCellValue(r.get("lossPrice").toString());
			HSSFCell cell29 = row.createCell((short) 29);
			cell29.setCellStyle(cellStyle1);
			cell29.setCellValue( r.getBigDecimal("sum1").toString());
			HSSFCell cell30 = row.createCell((short) 30);
			cell30.setCellStyle(cellStyle1);
			cell30.setCellValue(r.getBigDecimal("sum2").toString());
			HSSFCell cell31 = row.createCell((short) 31);
			cell31.setCellStyle(cellStyle1);
			cell31.setCellValue(r.getBigDecimal("zyf").toString());
		}
		response.addHeader("Content-Disposition", "attachment;filename=" + "cost"+code_start+"-"+code_end+ ".xls");
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		try {
			ServletOutputStream out = response.getOutputStream();
			wbook.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
    /**
	 * 物流成本统计表
	 */
	public static Page<Record> getWlcostlist(int pageno, int pagesize,String code_start,String code_end,String ename){
		 String select = "SELECT COUNT(1) As count,A.ename,A.code,B.price,COUNT(1)*price As totalprice";
		 String sqlExceptSelect = " from (SELECT lognumber,ename,code FROM f_order_info WHERE lognumber is not NULL " + 
		 		"UNION ALL " + 
		 		"SELECT  fNumber as lognumber, fwlName as ename,fCode as code  FROM f_order_info_xx ) As A LEFT JOIN f_express B on A.ename = B.name where 1=1";
		 if (StrKit.notBlank(code_start)) {
			sqlExceptSelect+=String.format(" and A.code >= '%s'", code_start);
		 }
		 if (StrKit.notBlank(code_end)) {
				sqlExceptSelect+=String.format(" and A.code <= '%s'", code_end);
			 }
		 if (StrKit.notBlank(ename)) {
				sqlExceptSelect+=String.format(" and A.ename = '%s'", ename);
	     }
		 sqlExceptSelect+= " group by A.code,A.ename order by A.code desc";
		 Page<Record>page= Db.paginate(pageno, pagesize, select, sqlExceptSelect);
		 return page;
	}
}
