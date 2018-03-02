package com.dao;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.util.Constant;

/**
 * @ses 订单数据访问层
 * @author yetangtang
 * @date 2016/10/29
 */
public class OrderDao {
	public static void exportOrderList(HttpServletResponse response,int time,	int type, String state, String ordercode,int ishas, String receiver, String time_a, String time_b, String aid, String tel,int sendCycle,int app,int style,String piCode,String gName){
		List<Record> list = MCDao.getOrderList(1, 100000000, time, type, state, ordercode, ishas, receiver, time_a, time_b, aid, tel,sendCycle,app,style,piCode,gName,"").getList();
		HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet1 = wbook.createSheet();
		//设置列宽
		sheet1.setColumnWidth((short)0, (short)5500);
		sheet1.setColumnWidth((short)1, (short)3400);
		sheet1.setColumnWidth((short)2, (short)2400);
		sheet1.setColumnWidth((short)3, (short)10000);
		sheet1.setColumnWidth((short)4, (short)10000);
		sheet1.setColumnWidth((short)5, (short)3000);
		sheet1.setColumnWidth((short)6, (short)3000);
		sheet1.setColumnWidth((short)7, (short)3000);
		sheet1.setColumnWidth((short)8, (short)5500);
		sheet1.setColumnWidth((short)9, (short)5500);
		sheet1.setColumnWidth((short)10, (short)5500);
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
				cell0.setCellValue("订单编号");
				
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("商品名称");
				
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("收货人");
				
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("忌讳的花");
				
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle);
				cell4.setCellValue("忌讳色系");
				
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle2);
				cell5.setCellValue("订阅次数");
				
				HSSFCell cell6 = row.createCell((short) 6);
				cell6.setCellStyle(cellStyle2);
				cell6.setCellValue("已发次数");
				
				HSSFCell cell7 = row.createCell((short) 7);
				cell7.setCellStyle(cellStyle2);
				cell7.setCellValue("送达时间");	
				
				HSSFCell cell8 = row.createCell((short) 8);
				cell8.setCellStyle(cellStyle2);
				cell8.setCellValue("发货周期");
				
				HSSFCell cell9 = row.createCell((short) 9);
				cell9.setCellStyle(cellStyle2);
				cell9.setCellValue("用途");
				
				HSSFCell cell10 = row.createCell((short) 10);
				cell10.setCellStyle(cellStyle2);
				cell10.setCellValue("格调");
				
				HSSFCell cell11 = row.createCell((short) 11);
				cell11.setCellStyle(cellStyle);
				cell11.setCellValue("支付总额");
				
				HSSFCell cell12 = row.createCell((short) 12);
				cell12.setCellStyle(cellStyle);
				cell12.setCellValue("类型");
				
				HSSFCell cell13= row.createCell((short) 13);
				cell13.setCellStyle(cellStyle);
				cell13.setCellValue("下单时间");
				
				HSSFCell cell14 = row.createCell((short) 14);
				cell14.setCellStyle(cellStyle);
				cell14.setCellValue("状态");
				
				HSSFCell cell15 = row.createCell((short) 15);
				cell15.setCellStyle(cellStyle);
				cell15.setCellValue("是否首单");
			}
			
			row = sheet1.createRow(i+1);
			Record r = list.get(i);
			
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellStyle(cellStyle1);
			cell0.setCellValue(r.getStr("ordercode"));
			HSSFCell cell1 = row.createCell((short) 1);
			
			cell1.setCellStyle(cellStyle1);
			cell1.setCellValue(r.getStr("gName"));
			
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellStyle(cellStyle1);
			cell2.setCellValue(r.getStr("receiver"));
			
			HSSFCell cell3 = row.createCell((short) 3);
			cell3.setCellStyle(cellStyle1);
			cell3.setCellValue(r.getStr("jihui"));
			
			HSSFCell cell4 = row.createCell((short) 4);
			cell4.setCellStyle(cellStyle1);
			cell4.setCellValue(r.getStr("color"));
			
			HSSFCell cell5 = row.createCell((short) 5);
			cell5.setCellStyle(cellStyle1);
			cell5.setCellValue(r.getInt("cycle"));
			
			HSSFCell cell6 = row.createCell((short) 6);
			cell6.setCellStyle(cellStyle1);
			cell6.setCellValue(r.getInt("ocount"));
			
			HSSFCell cell7 = row.createCell((short) 7);
			cell7.setCellStyle(cellStyle1);
			int reach = r.getInt("reach");
			String reachName=reach==1?"周一送":"周六送";
			if(r.getInt("type")==5){
				reachName=r.getStr("week");
			}
			cell7.setCellValue(reachName);
			
			HSSFCell cell8 = row.createCell((short) 8);
			cell8.setCellStyle(cellStyle1);
			String sendCycleStr = new String();
			switch(r.getInt("sendCycle")){
				case 1: sendCycleStr = "一周一次";break;
				case 2: sendCycleStr = "两周一次";break;
				case 4: sendCycleStr = "四周一次";break;
			}
			cell8.setCellValue(sendCycleStr);
			
			HSSFCell cell9 = row.createCell((short) 9);
			cell9.setCellStyle(cellStyle1);
			String appStr=new String();
			switch(r.getInt("app")){
			case 1: appStr = "居家自用";break;
			case 2: appStr = "办公室自用";break;
			case 3: appStr = "礼物";break;
		    }
			cell9.setCellValue(appStr);
			
			HSSFCell cell10 = row.createCell((short) 10);
			cell10.setCellStyle(cellStyle1);
			String styleStr=new String();
			switch(r.getInt("style")){
			case 1: styleStr = "淡雅";break;
			case 2: styleStr = "亮丽";break;
		    }
			cell10.setCellValue(styleStr);
	
			HSSFCell cell11 = row.createCell((short)11);
			cell11.setCellStyle(cellStyle1);
			cell11.setCellValue(r.getDouble("totalprice"));
			
			HSSFCell cell12 = row.createCell((short) 12);
			cell12.setCellStyle(cellStyle1);
			String ordertype = new String();
			switch(r.getInt("type")){
				case 1: ordertype = "订阅";break;
				case 2: ordertype = "送花";break;
				case 3: ordertype = "周边";break;
				case 4: ordertype = "兑换";break;
			}
			cell12.setCellValue(ordertype);
			
			HSSFCell cell13 = row.createCell((short) 13);
			cell13.setCellStyle(cellStyle1);
			Timestamp timestamp = r.getTimestamp("ctime");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			cell13.setCellValue(sdf.format(timestamp));
			
			HSSFCell cell14 = row.createCell((short) 14);
			cell14.setCellStyle(cellStyle1);
			cell14.setCellValue(Constant.zhbyos(r.getInt("state")));
			
			HSSFCell cell15 = row.createCell((short) 15);
			cell15.setCellStyle(cellStyle1);
			int ishas1 = r.getInt("ishas");
			String ishasName=ishas1==0?"是":"否";
			cell15.setCellValue(ishasName);
		}
		response.addHeader("Content-Disposition", "attachment;filename=" + toUtf8String("订单列表")+ ".xls");
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

	// 导出报表
	public static void getinfoForExcel(HttpServletResponse response, Date time_a, Date time_b) throws ParseException{
		List<Record> printData = getPrintData(time_a, time_b);
		
		HSSFWorkbook wbook = new HSSFWorkbook();
		HSSFSheet sheet1 = wbook.createSheet();
		//设置列宽
		sheet1.setColumnWidth((short)0, (short)5400);
		sheet1.setColumnWidth((short)1, (short)5400);
		sheet1.setColumnWidth((short)2, (short)5400);
		sheet1.setColumnWidth((short)3, (short)5400);
		sheet1.setColumnWidth((short)4, (short)5400);
		sheet1.setColumnWidth((short)5, (short)5400);
		sheet1.setColumnWidth((short)6, (short)5400);
		sheet1.setColumnWidth((short)7, (short)5400);
		sheet1.setColumnWidth((short)8, (short)5400);
		 
		//首行 样式1
	   HSSFCellStyle cellStyle = wbook.createCellStyle();
	   HSSFFont font = wbook.createFont();
	   font.setColor(HSSFColor.BLACK.index);    //红字
       font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
	   cellStyle.setFont(font);
	   cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  //填充单元格
	   cellStyle.setFillForegroundColor(HSSFColor.PLUM.index); //填深红色
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
		for(int i=0;i<printData.size();i++){
			if(i==0){
				row = sheet1.createRow(i);
				HSSFCell cell0 = row.createCell((short) 0);
				cell0.setCellStyle(cellStyle);
				cell0.setCellValue("时间");
				
				HSSFCell cell1 = row.createCell((short) 1);
				cell1.setCellStyle(cellStyle);
				cell1.setCellValue("公众号粉丝 ");
				
				HSSFCell cell2 = row.createCell((short) 2);
				cell2.setCellStyle(cellStyle);
				cell2.setCellValue("发货量");
				
				HSSFCell cell3 = row.createCell((short) 3);
				cell3.setCellStyle(cellStyle);
				cell3.setCellValue("下单用户 ");
				
				HSSFCell cell4 = row.createCell((short) 4);
				cell4.setCellStyle(cellStyle);
				cell4.setCellValue("新增单量");
				
				HSSFCell cell5 = row.createCell((short) 5);
				cell5.setCellStyle(cellStyle);
				cell5.setCellValue("复购单量 ");	
				
				HSSFCell cell6 = row.createCell((short) 6);
				cell6.setCellStyle(cellStyle);
				cell6.setCellValue("复购率 ");
				
				HSSFCell cell7 = row.createCell((short) 7);
				cell7.setCellStyle(cellStyle);
				cell7.setCellValue("投诉单量 ");
				
				HSSFCell cell8= row.createCell((short) 8);
				cell8.setCellStyle(cellStyle);
				cell8.setCellValue("投诉率 ");
			}
			row = sheet1.createRow(i+1);
			Record print = printData.get(i);
			HSSFCell cell0 = row.createCell((short) 0);
			cell0.setCellStyle(cellStyle1);
			cell0.setCellValue(print.getStr("time"));
			
			HSSFCell cell1 = row.createCell((short) 1);
			cell1.setCellStyle(cellStyle1);
			cell1.setCellValue(print.getInt("aNum"));
			
			HSSFCell cell2 = row.createCell((short) 2);
			cell2.setCellStyle(cellStyle1);
			cell2.setCellValue(print.getInt("oiNum"));
			
			HSSFCell cell3 = row.createCell((short) 3);
			cell3.setCellStyle(cellStyle1);
			cell3.setCellValue(print.getInt("oaNum"));
			
			HSSFCell cell4 = row.createCell((short) 4);
			cell4.setCellStyle(cellStyle1);
			cell4.setCellValue(print.getInt("oNum"));
			
			HSSFCell cell5 = row.createCell((short) 5);
			cell5.setCellStyle(cellStyle1);
			cell5.setCellValue(print.getInt("fNum"));
			
			HSSFCell cell6 = row.createCell((short) 6);
			cell6.setCellStyle(cellStyle1);
			cell6.setCellValue(print.getStr("fNum_p"));
			
			HSSFCell cell7 = row.createCell((short) 7);
			cell7.setCellStyle(cellStyle1);
			cell7.setCellValue(print.getInt("cNum"));
			
			HSSFCell cell8 = row.createCell((short) 8);
			cell8.setCellStyle(cellStyle1);
			cell8.setCellValue(print.getStr("cNum_p"));
		}
		response.addHeader("Content-Disposition", "attachment;filename="+ toUtf8String("统计报表") +".xls");
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

	// 获取打印数据
	public static List<Record> getPrintData(Date time_a, Date time_b){
		Calendar[] sae = getStartAndEnd(time_a, time_b);
		Calendar ta = sae[0];	// 开始日期
		Calendar tb = sae[1];	// 结束日期
		List<String> dateList = new ArrayList<>();
		while(ta.getTimeInMillis()<=tb.getTimeInMillis()){
			boolean isFirstSunday = (ta.getFirstDayOfWeek() == Calendar.SUNDAY);
			int weekDay = ta.get(Calendar.DAY_OF_WEEK);	// 获取周几
			// 若一周第一天为星期天，则-1
			if(isFirstSunday){
				weekDay = weekDay - 1;
				if(weekDay == 0){
					weekDay = 7;
				}
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
			switch(weekDay){
				case 1:
					dateList.add(sdf.format(ta.getTime()));
					break;
				case 7:
					String lastD = dateList.get(dateList.size()-1);
					lastD += "," + sdf.format(ta.getTime());
					dateList.remove(dateList.size()-1);
					dateList.add(lastD);
					break;
				default : break;
			}
			ta.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		List<Record> printList = new ArrayList<>();
		for(String date : dateList){
			Record print = new Record();
			String[] dStr = date.split(",");
			print.set("time", date.replace(",", "-"));
			NumberFormat nt = NumberFormat.getPercentInstance();
			nt.setMinimumFractionDigits(2);
			Number aNum = Db.queryNumber("SELECT count(1) FROM f_account WHERE ctime>? AND ctime<DATE_ADD(?,INTERVAL 1 DAY)", dStr[0], dStr[1]);
			print.set("aNum", aNum.intValue());	// 公众号粉丝
			Number oiNum = Db.queryNumber("SELECT count(1) FROM f_order_info WHERE ctime>? AND ctime<DATE_ADD(?,INTERVAL 1 DAY) AND state>=2", dStr[0], dStr[1]);
			print.set("oiNum", oiNum.intValue());	// 发货量
			Number oaNum = Db.queryNumber("SELECT COUNT(1) FROM f_order WHERE ctime>? AND ctime<DATE_ADD(?,INTERVAL 1 DAY) AND state in (1,2,3,4) GROUP BY aid", dStr[0], dStr[1]);
			print.set("oaNum", oaNum==null?0:oaNum.intValue());	// 下单用户
			Number oNum = Db.queryNumber("SELECT COUNT(1) FROM f_order WHERE ctime>? AND ctime<DATE_ADD(?,INTERVAL 1 DAY) AND state in (1,2,3,4)", dStr[0], dStr[1]);
			print.set("oNum", oNum.intValue());	// 新增单量
			Number fNum = Db.queryNumber("SELECT COUNT(1) FROM f_order WHERE ctime>? AND ctime<DATE_ADD(?,INTERVAL 1 DAY) AND state in (1,2,3,4) AND ishas=1", dStr[0], dStr[1]);
			print.set("fNum", fNum.intValue());	// 复购单量
			print.set("fNum_p", oNum.doubleValue()==0?"0.00%":nt.format(fNum.doubleValue()/oNum.doubleValue()));	// 复购率
			Number cNum_t = Db.queryNumber("SELECT COUNT(1) FROM f_comment WHERE ctime>? AND ctime<DATE_ADD(?,INTERVAL 1 DAY)", dStr[0], dStr[1]);
			Number cNum = Db.queryNumber("SELECT COUNT(1) FROM f_comment WHERE ctime>? AND ctime<DATE_ADD(?,INTERVAL 1 DAY) AND point<4", dStr[0], dStr[1]);
			print.set("cNum", cNum.intValue());	// 投诉单量
			print.set("cNum_p", cNum_t.doubleValue()==0?"0.00%":nt.format(cNum.doubleValue()/cNum_t.doubleValue()));	// 投诉率
			printList.add(print);
		}
		return printList;
	}
	
	// 转换开始时间与结束时间
	public static Calendar[] getStartAndEnd(Date time_a, Date time_b){
		Calendar now_a = Calendar.getInstance();
		now_a.setTime(time_a);
		Calendar now_b = Calendar.getInstance();
		now_b.setTime(time_b);
		
		boolean isFirstSunday = (now_a.getFirstDayOfWeek() == Calendar.SUNDAY);
		int weekDay_a = now_a.get(Calendar.DAY_OF_WEEK);	// 获取周几
		int weekDay_b = now_b.get(Calendar.DAY_OF_WEEK);	// 获取周几
		//若一周第一天为星期天，则-1
		if(isFirstSunday){
			weekDay_a = weekDay_a - 1;
			weekDay_b = weekDay_b - 1;
			if(weekDay_a == 0)
				weekDay_a = 7;
			if(weekDay_b == 0)
				weekDay_b = 7;
		}
		now_a.add(Calendar.DAY_OF_MONTH, 1-weekDay_a);	// 开始时间的星期一
		now_b.add(Calendar.DAY_OF_MONTH, 7-weekDay_b);	// 结束时间的星期二
		Calendar[] data = {now_a, now_b};
		return data;
	}
	
}