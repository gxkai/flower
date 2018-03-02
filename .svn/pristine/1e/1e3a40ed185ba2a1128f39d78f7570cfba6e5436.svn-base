package com.dao;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

import com.controller.front.FrontServiceCtrl;
import com.jfinal.plugin.activerecord.Record;
import com.util.Constant;

public class CARDDao {
	
	//设置中文文件名
		public static String toUtf8String(String s){ 
		     StringBuffer sb = new StringBuffer(); 
		       for (int i=0;i<s.length();i++){ 
		          char c = s.charAt(i); 
		          if (c >= 0 && c <= 255){sb.append(c);} 
		        else{ 
		        byte[] b; 
		         try { b = Character.toString(c).getBytes("utf-8");} 
		         catch (Exception ex) { 
		             System.out.println(ex); 
		                  b = new byte[0]; 
		         } 
		            for (int j = 0; j < b.length; j++) { 
		             int k = b[j]; 
		              if (k < 0) k += 256; 
		              sb.append("%" + Integer.toHexString(k).toUpperCase()); 
		              } 
		     } 
		  } 
		  return sb.toString(); 
		}

	
	public static void exportCardList(HttpServletResponse response,int cNo,int isSuccess,String cId,String cMakeTime){
		List<Record> list = MCDao.getCard(1, 10000000,cNo,isSuccess,cId,cMakeTime).getList();
        FrontServiceCtrl fsc= new FrontServiceCtrl();
		
		for( int i=0;i<list.size();i++){
			int cid=list.get(i).getInt("cId"); 
			String cpwd=fsc.getExchangeCode(String.valueOf(cid));
			list.get(i).set("cPwd",cpwd );
		}
		HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet1 = wbook.createSheet();
		//设置列宽
		sheet1.setColumnWidth((short)0, (short)5500);
		sheet1.setColumnWidth((short)1, (short)3400);
		sheet1.setColumnWidth((short)2, (short)2400);
		sheet1.setColumnWidth((short)3, (short)10000);
		sheet1.setColumnWidth((short)4, (short)10000);
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
				cell0.setCellValue("卡号");
				
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("兑换码");

				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("卡名");
				
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("卡面值");
				
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle);
				cell4.setCellValue("商品名称");
				
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle);
				cell5.setCellValue("订阅次数");
				
				HSSFCell cell6 = row.createCell((short) 6);
				cell6.setCellStyle(cellStyle);
				cell6.setCellValue("是否含花瓶");
				
				HSSFCell cell7 = row.createCell((short) 7);
				cell7.setCellStyle(cellStyle);
				cell7.setCellValue("是否兑换成功");
				
				HSSFCell cell8 = row.createCell((short) 8);
				cell8.setCellStyle(cellStyle);
				cell8.setCellValue("兑换人昵称");
				
				HSSFCell cell9 = row.createCell((short) 9);
				cell9.setCellStyle(cellStyle);
				cell9.setCellValue("兑换时间");
				
				HSSFCell cell10 = row.createCell((short) 10);
				cell10.setCellStyle(cellStyle);
				cell10.setCellValue("订单号");
				
				HSSFCell cell11 = row.createCell((short) 11);
				cell11.setCellStyle(cellStyle);
				cell11.setCellValue("制卡账号");
				
				HSSFCell cell12 = row.createCell((short) 12);
				cell12.setCellStyle(cellStyle);
				cell12.setCellValue("制卡时间");
				
				HSSFCell cell13 = row.createCell((short) 13);
				cell13.setCellStyle(cellStyle);
				cell13.setCellValue("截止日期");
				
				HSSFCell cell14 = row.createCell((short) 14);
				cell14.setCellStyle(cellStyle);
				cell14.setCellValue("购买人");
				
				HSSFCell cell15 = row.createCell((short) 15);
				cell15.setCellStyle(cellStyle);
				cell15.setCellValue("购买时间");
				
				HSSFCell cell16 = row.createCell((short) 16);
				cell16.setCellStyle(cellStyle);
				cell16.setCellValue("销售人 ");
				
				HSSFCell cell17 = row.createCell((short) 17);
				cell17.setCellStyle(cellStyle);
				cell17.setCellValue("销售时间 ");
				
				HSSFCell cell18 = row.createCell((short) 18);
				cell18.setCellStyle(cellStyle);
				cell18.setCellValue("二维码 ");

			}
			
			row = sheet1.createRow(i+1);
			Record r = list.get(i);
			
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellStyle(cellStyle1);
			cell0.setCellValue(r.getInt("cId"));
			
			HSSFCell cell1 = row.createCell((short) 1);
			cell1.setCellStyle(cellStyle1);
			cell1.setCellValue(r.getStr("cPwd"));
			
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellStyle(cellStyle1);
			cell2.setCellValue(r.getStr("cName"));
			
			HSSFCell cell3 = row.createCell((short) 3);
			cell3.setCellStyle(cellStyle1);
			cell3.setCellValue(r.getDouble("cMoney"));
			
			HSSFCell cell4 = row.createCell((short) 4);
			cell4.setCellStyle(cellStyle1);
			cell4.setCellValue(r.getStr("name"));
			
			HSSFCell cell5 = row.createCell((short) 5);
			cell5.setCellStyle(cellStyle1);
			cell5.setCellValue(r.getInt("cCycle"));

			HSSFCell cell6 = row.createCell((short) 6);
			cell6.setCellStyle(cellStyle1);
			cell6.setCellValue(r.getStr("cHasVase"));
			
			HSSFCell cell7 = row.createCell((short) 7);
			cell7.setCellStyle(cellStyle1);
			cell7.setCellValue(r.getStr("cIsSuccess"));
			
			HSSFCell cell8 = row.createCell((short) 8);
			cell8.setCellStyle(cellStyle1);
			cell8.setCellValue(r.getStr("nick"));
			
			HSSFCell cell09 = row.createCell((short) 9);
			cell09.setCellStyle(cellStyle1);
			cell09.setCellValue(r.getStr("cExcTime"));
			
			HSSFCell cell10 = row.createCell((short) 10);
			cell10.setCellStyle(cellStyle1);
			cell10.setCellValue(r.getStr("cExcOrderId"));
			
			HSSFCell cell11 = row.createCell((short) 11);
			cell11.setCellStyle(cellStyle1);
			cell11.setCellValue(r.getStr("cMaker"));
			
			HSSFCell cell12 = row.createCell((short) 12);
			cell12.setCellStyle(cellStyle1);
			cell12.setCellValue(r.getStr("cMakeTime"));
			
			HSSFCell cell13 = row.createCell((short) 13);
			cell13.setCellStyle(cellStyle1);
			cell13.setCellValue(r.getStr("cEffDate"));
			
			HSSFCell cell14 = row.createCell((short) 14);
			cell14.setCellStyle(cellStyle1);
			cell14.setCellValue(r.getStr("cPurchaser"));
			
			HSSFCell cell15 = row.createCell((short) 15);
			cell15.setCellStyle(cellStyle1);
			cell15.setCellValue(r.getStr("cPurTime"));
			
			HSSFCell cell16 = row.createCell((short) 16);
			cell16.setCellStyle(cellStyle1);
			cell16.setCellValue(r.getStr("cSeller"));
			
			HSSFCell cell17 = row.createCell((short) 17);
			cell17.setCellStyle(cellStyle1);
			cell17.setCellValue(r.getStr("cSellTime"));
			
			HSSFCell cell18 = row.createCell((short) 18);
			cell18.setCellStyle(cellStyle1);
			cell18.setCellValue("http://www.flowermm.net/account/exchange/"+ r.getInt("cId"));
			
			
			
			
		}
		response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String("兑换卡列表")+ ".xls");
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
	
	
}
