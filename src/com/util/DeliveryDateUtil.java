package com.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.dao.MCDao;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class DeliveryDateUtil {

	/**
	 * @see 鲜花首次送达时间方法
	 * @author yeqing
	 * @date 2016/08/11
	 * @param int choose 用户选择
	 * @return String
	 * @throws ParseException
	 */
	@SuppressWarnings("static-access")
	public static String compare_Date(int choose) throws ParseException {

		int defaultDayStep = 0; // 默认的日期步长
		int wednesdayStep = 3; // 周三
		int fridayStep = 5; // 周五
		int defaultWeekStep = 0; // 默认的星期步长
		int lastWeekStep = 0; // 默认下一次送达步长

		// 定义string类型的数组集合
		//String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };

		// 获取当前系统时间
		Date _nowTime = new Date();

		// 日期格式化
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat theFirstDatedf = new SimpleDateFormat("yyyy-MM-dd");

		// 创建日历对象，并初始化
		Calendar calendar = Calendar.getInstance();

		// 获取日期为周几
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 != 0 ? calendar.get(Calendar.DAY_OF_WEEK) - 1 : 0;

		//System.out.println(weekDays[dayOfWeek]);

		// 用户选择周一送
		if (choose == Constant.MondayTime) {

			// 设置截止日期为周五
			defaultDayStep = fridayStep;
			
			// 默认步长为8
			defaultWeekStep = 8;
			
			// 下一次步长为15
			lastWeekStep = 15;

		} else {
			
			// 设置截止日期为周三
			defaultDayStep = wednesdayStep;
			
			// 默认步长为6
			defaultWeekStep = 6;
			
			// 下一次步长为13
			lastWeekStep = 13;
		}
		
		// 计算本周周三日期
		calendar.add(Calendar.DATE, -dayOfWeek + defaultDayStep);

		// 初始化周五日期的时、分、秒为 18：00：00
		calendar.set(calendar.HOUR_OF_DAY, 18);
		calendar.set(calendar.MINUTE, 0);
		calendar.set(calendar.SECOND, 0);

		//System.out.println(df.format(calendar.getTime()));

		// 转换时间格式
		Date d1 = df.parse(df.format(calendar.getTime()));

		// 判定下单时间
		boolean flag = d1.after(_nowTime);

		// 获取日期步长
		int dayStep = flag == true ? defaultWeekStep : lastWeekStep;

		// 判断送达时间
		// if(flag){

		// 计算首次送达日期，并返回
		calendar.setTime(_nowTime);
		calendar.add(calendar.DATE, -dayOfWeek + dayStep);
		
		//获取首次送达日期
		String result = theFirstDatedf.format(calendar.getTime());
		//System.out.println(theFirstDatedf.format(calendar.getTime()));

		// }//else{

		// 计算下下周一的日期
		// calendar.setTime(_nowTime);
		// calendar.add(calendar.DATE, -dayOfWeek+15);
		// System.out.println(theFirstDatedf.format(calendar.getTime()));
		// }

		//
		//System.out.println(dayOfWeek);
		//
		// System.out.println(df.format(_nowTime));

		// 返回首次送达日期
		return result;
	}
	
	/**
	 * @see 获取有效日期方法
	 * @author yeqing
	 * @date 2016/08/11
	 * @param void
	 * @return Map<String,Object> map
	 * @result	Key<effectiveDate> Value<StringDate>
	 * @result	Key<invalidDate> Value<StringDate>
	 */
	public static Map<String,Object> getEffectiveDate(){
		
		//定义返回值变量
		String effectiveDate;
		String invalidDate;
		
		//创建map容器，存储返回值
		Map<String,Object> result = new HashMap<>();
		
		//生效时间,这里为了方便取现在系统的时间
        Date ableTime = new Date();
        
        // 失效时间
        Date disableTime = null;
        
        // 用Calendar类进行日期的计算
        Calendar c = Calendar.getInstance();
        
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        	
            // 设置生效时间
            c.setTime(ableTime);
            
            // 计算失效时间，根据你的计算方法，这里假设两个月后为失效时间
            c.add(Calendar.MONTH, 2);
            
            // 赋值给失效时间
            disableTime = c.getTime();
            
            //给定义的变量赋值
            effectiveDate = sf.format(ableTime);
            invalidDate = sf.format(disableTime);
            
            //添加变量到map容器
            result.put("effectiveDate",effectiveDate);
            result.put("invalidDate",invalidDate);
            
            //输出测试结果
            System.out.println("生效时间：" + sf.format(ableTime) + ",失效时间：" + sf.format(disableTime));
        
        //返回结果集    
		return result;
	}
	
	/**
	 * 获取产品批次
	 * 在数据字典【产品批次】,配花之前由管理员设置
	 * @return
	 */
	public static Map<String,Object> makeCode(){
		Map<String, Object> map = new HashMap<>();
		String strDatecode="select code_value,count(*) 'count' from f_dictionary where code_key='productPiCode' and code_name='产品批次' and state=1";
		Record rdDatecode= Db.findFirst(strDatecode);
		if(rdDatecode.getLong("count")==1){
			map.put("result", true);
			map.put("datecode", rdDatecode.getStr("code_value"));
		}else{
			map.put("result", false);
		}
		return map;
	}
	
	// 获取产品批次
	/*public static Map<String, Object> makeCode(){
		Calendar now = Calendar.getInstance();
		//一周第一天是否为星期天
		boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
		//获取周几
		int weekDay = now.get(Calendar.DAY_OF_WEEK);
		//若一周第一天为星期天，则-1
		if(isFirstSunday){
			weekDay = weekDay - 1;
			if(weekDay == 0){
				weekDay = 7;
			}
		}
		Map<String, Object> map = new HashMap<>();
		boolean result = false;
		String datecode = new String();
		// 周一-周五(18:00)，配本周六产品
		// 周五(18:00)-周日，配下周一产品
		switch(weekDay){
			case 1:
				result = true;
				now.add(Calendar.DAY_OF_MONTH, 5);
				break;
			case 2:
				result = true;
				now.add(Calendar.DAY_OF_MONTH, 4);
				break;
			case 3:
				result = true;
				now.add(Calendar.DAY_OF_MONTH, 3);
				break;
			case 4:
				result = true;
				now.add(Calendar.DAY_OF_MONTH, 2);
				break;
			case 5:
				result = true;
				if(now.get(Calendar.HOUR_OF_DAY) < 18){
					now.add(Calendar.DAY_OF_MONTH, 1);
				}else{
					now.add(Calendar.DAY_OF_MONTH, 3);
				}
				break;
			case 6:
				result = true;
				now.add(Calendar.DAY_OF_MONTH, 2);
				break;
			case 7:
				result = true;
				now.add(Calendar.DAY_OF_MONTH, 1);
				break;
		}
		map.put("result", result);
		if(result){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			datecode = sdf.format(now.getTime());
			map.put("datecode", datecode);
		}
		return map;
	}*/
	
	/**
	 * 配单日期，在数据字典里由管理员设置
	 * 同时设置【发货批次】、【送达日期分类】
	 * @return
	 */
	public static Map<String,Object> chooseDate(){
		Map<String, Object> map = new HashMap<>();
		String strDatecode="select code_value,count(*) 'count' from f_dictionary where code_key='piCode' and code_name='发货批次' and state=1";
		Record rdDatecode= Db.findFirst(strDatecode);
		if(rdDatecode.getLong("count")==1){
			String strReach="select cast(code_value as signed integer) 'code_value',count(*) 'count' from f_dictionary where code_key='reach' and code_name='送达日期分类' and state=1";
			Record rdReach=Db.findFirst(strReach);
			if(rdReach.getLong("count")==1){
				map.put("result", true);
				map.put("reach", rdReach.getLong("code_value").intValue());
				map.put("datecode", rdDatecode.getStr("code_value"));
			}else{
				map.put("result", false);
			}
		}else{
			map.put("result", false);
		}
		return map;
	}
	/**
	 * 预配单发货批次
	 * @param code
	 * @return
	 */
	public static Map<String,Object> choosedate(){
		Map<String, Object> map = new HashMap<>();
		String strDatecode="select code_value,count(*) 'count' from f_dictionary where code_key='piCode_pre' and code_name='预配单发货批次' and state=1";
		Record rdDatecode= Db.findFirst(strDatecode);
		if(rdDatecode.getLong("count")==1){
			String strReach="select cast(code_value as signed integer) 'code_value',count(*) 'count' from f_dictionary where code_key='reach' and code_name='送达日期分类' and state=1";
			Record rdReach=Db.findFirst(strReach);
			if(rdReach.getLong("count")==1){
				map.put("result", true);
				map.put("reach", rdReach.getLong("code_value").intValue());
				map.put("datecode", rdDatecode.getStr("code_value"));
			}else{
				map.put("result", false);
			}
		}else{
			map.put("result", false);
		}
		return map;
	}
	// 配单日期
	/*public static Map<String, Object> chooseDate(){
		Calendar now = Calendar.getInstance();
		//一周第一天是否为星期天
		boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
		//获取周几
		int weekDay = now.get(Calendar.DAY_OF_WEEK);
		//若一周第一天为星期天，则-1
		if(isFirstSunday){
			weekDay = weekDay - 1;
			if(weekDay == 0){
				weekDay = 7;
			}
		}
		Map<String, Object> map = new HashMap<>();
		boolean result = false;//是否可配单
		int reach = 1;//周一
		String datecode = new String();
		// 周三(18:00)-周五(18:00)，配本周六
		// 周五(18:00)-周日，配下周一
		switch(weekDay){
			case 3://周三
				if(now.get(Calendar.HOUR_OF_DAY) >= 18){
					result = true;
					reach = 2;//周六
					now.add(Calendar.DAY_OF_MONTH, 3);//发货日期：3天后，周六
				}
				break;
			case 4://周4
				result = true;
				reach = 2;
				now.add(Calendar.DAY_OF_MONTH, 2);//发货日期：2天后，周六
				break;
			case 5:
				result = true;
				if(now.get(Calendar.HOUR_OF_DAY) < 18){
					reach = 2;
					now.add(Calendar.DAY_OF_MONTH, 1);//发货日期：1天后，周六
				}else{
					reach = 1;
					now.add(Calendar.DAY_OF_MONTH, 3);//发货日期：3天后，下周一
				}
				break;
			case 6:
				result = true;
				reach = 1;
				now.add(Calendar.DAY_OF_MONTH, 2);//发货日期：2天后，下周一
				break;
			case 7:
				result = true;
				reach = 1;
				now.add(Calendar.DAY_OF_MONTH, 1);//发货日期：1天后，下周一
				break;
			default : break;
		}
		map.put("result", result);
		if(result){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			datecode = sdf.format(now.getTime());//发货批号
			map.put("reach", reach);
			map.put("datecode", datecode);
		}
		return map;
	}
	*/
	public static String[] getTimeByCode(String code){
		String[] strArr = new String[2];
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			Date date = sdf.parse(code);
			Calendar now = Calendar.getInstance();
			now.setTime(date);
			boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
			//获取周几
			int weekDay = now.get(Calendar.DAY_OF_WEEK);
			//若一周第一天为星期天，则-1
			if(isFirstSunday){
				weekDay = weekDay - 1;
				if(weekDay == 0){
					weekDay = 7;
				}
			}
			now.add(Calendar.HOUR_OF_DAY, 18);
			if(weekDay == 6){
				now.add(Calendar.DAY_OF_MONTH, -3);
			}else{
				now.add(Calendar.DAY_OF_MONTH, -3);
			}
			SimpleDateFormat sdf_x = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			strArr[1] = sdf_x.format(now.getTime());	// 截止日期
			now.add(Calendar.DAY_OF_MONTH, -7);
			strArr[0] = sdf_x.format(now.getTime());	// 开始日期
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return strArr;
	}
	

	/**
	 * 修改批号
	 * @param ordercode 订单编号
	 * @param reachOld 旧的送达时间
	 * @param reachNew 新的送达时间
	 * @param isSy 1:顺延  0：不顺延
	 */
	public static boolean changePiCodeList(String ordercode,int reachOld,int reachNew,int isSy){
		List<Record> nextPiCodeList=MCDao.getNextPiCodeList(ordercode);
		//int modifyCount=MCDao.getModifyPiCodeCount(ordercode);
		int isModifyReach=0;//默认不修改送达日期
		if(reachOld!=reachNew){isModifyReach=1;}//【旧的送达时间】和【新的送达日期】不相等，表示需要修改
		
		SimpleDateFormat sdf =   new SimpleDateFormat("yyyyMMdd");
		for(int i=0;i<nextPiCodeList.size();i++){
			Date piDate = null;
			try {
				piDate = sdf.parse(nextPiCodeList.get(i).getStr("piCode"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar now = Calendar.getInstance();
			now.setTime(piDate);
			//如果只顺延，不修改（送达时间）
			if(isSy==1 && isModifyReach==0){
				now.add(Calendar.DAY_OF_MONTH, 7+nextPiCodeList.get(i).getInt("offsetDays"));//每个批号+7天+偏移天数
			}else if(isSy==0 && isModifyReach==1){//如果只修改（送达时间）,不做顺延
				if(reachOld==1 && reachNew==2){//如果由【周一】变成【周六】
					now.add(Calendar.DAY_OF_MONTH, 5+nextPiCodeList.get(i).getInt("offsetDays"));
				}else {//如果由【周六】变成【周一】
					now.add(Calendar.DAY_OF_MONTH, 2+nextPiCodeList.get(i).getInt("offsetDays"));
				}
				nextPiCodeList.get(i).set("reach", reachNew);
			}
			nextPiCodeList.get(i).set("piCode", sdf.format(now.getTime()));
			
		}
		return MCDao.saveModfiyResult(nextPiCodeList,ordercode,isSy,isModifyReach,reachOld,reachNew);
		
	}

     /**
	 * 获取发货批次号集合(新版本发布后,此方法废弃)
	 * @param reach 送达时间：1周一，2周六
	 * @param cTime 下单时间
	 * @param sendCycle 发货周期
	 * @param cycle 订阅次数
	 * @param orderTyle 订单类型 1订阅 2送花 3周边 4兑换 5节日
	 * @return
	 */
	public static List<String> getPiCodeList(int reach,Date cTime,int sendCycle,int cycle,int orderType){
		List<String>  piCodeList=  new ArrayList<>();
		SimpleDateFormat sdf =   new SimpleDateFormat("yyyyMMdd");
		//获取首次发货批号
		String firstPiCode=new String();
		if(orderType==5){
			String holidate = Db.queryStr("select code_value from f_dictionary where code_key = 'holiday'");
			firstPiCode = holidate.substring(0, 4)+"-"+holidate.substring(4, 6)+"-"+holidate.substring(6,8);
			piCodeList.add(firstPiCode);
		}else{
			firstPiCode=getDateByOrder(reach,cTime).replace("-", "");
			piCodeList.add(firstPiCode);
			for(int i=1;i<cycle;i++){
				try {
					Date piDate = sdf.parse(firstPiCode);
					Calendar now = Calendar.getInstance();
					now.setTime(piDate);
					now.add(Calendar.DAY_OF_MONTH, 7*i*sendCycle);
					String piCode=sdf.format(now.getTime());
					piCodeList.add(piCode);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		
		return piCodeList;
	}
	
	 /**
		 * 获取发货批次号集合
		 * @param reach 送达时间：1周一，2周六,3节日当天送达,参见f_holiday表
		 * @param cTime 下单时间
		 * @param sendCycle 发货周期
		 * @param cycle 订阅次数
		 * @param orderTyle 订单类型 1订阅 2送花 3周边 4兑换 5节日6定制
		 * @param hId 当orderType=5时，这里传f_holiday中的id字段值；当当orderType!=5时,这里传0
		 * @return
		 */
		public static List<String> getPiCodeList(int reach,Date cTime,int sendCycle,int cycle,int orderType,int hId,String firstPiCode){
			System.out.println("firstPiCode:"+firstPiCode);
			List<String>  piCodeList=  new ArrayList<>();
			SimpleDateFormat sdf =   new SimpleDateFormat("yyyyMMdd");
			//获取首次发货批号
			//如果客户没有指定，直接计算
			if(firstPiCode==null||firstPiCode.equals("")){
				firstPiCode=getDateByOrder(reach,cTime).replace("-", "");
			}
			piCodeList.add(firstPiCode);
			for(int i=1;i<cycle;i++){
				try {
					Date piDate = sdf.parse(firstPiCode);
					Calendar now = Calendar.getInstance();
					now.setTime(piDate);
					now.add(Calendar.DAY_OF_MONTH, 7*i*sendCycle);
					String piCode=sdf.format(now.getTime());
					piCodeList.add(piCode);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
			return piCodeList;
		}

	/**
	 * 获取首次发货时间
	 * @param orderCode
	 * @return
	 */
    public static String getFirstDate(String orderCode){
    	String firstDate = Db.queryStr("select piCode from f_picode where orderCode=? and num=1",orderCode);
    	if(firstDate!=null){
    		return firstDate.substring(0, 4)+"-"+firstDate.substring(4, 6)+"-"+firstDate.substring(6,8);
    	}else{
    		return "暂无";
    	}
		
    }

	/**
	 * 根据订单创建时间获取首次送达日期,针对非节日订单
	 * @param reach
	 * @param ctime
	 * @return
	 */
	public static String getDateByOrder(int reach, Date ctime){
		Calendar now = Calendar.getInstance();
		now.setTime(ctime);
		boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
		//获取周几
		int weekDay = now.get(Calendar.DAY_OF_WEEK);
		//若一周第一天为星期天，则-1
		if(isFirstSunday){
			weekDay = weekDay - 1;
			if(weekDay == 0){
				weekDay = 7;
			}
		}
		switch(weekDay){
			case 1:
				if(reach==1){
					now.add(Calendar.DAY_OF_MONTH, 7);//如果是周一下单，并且选择周一发货，则首次发货日期=下单日期+7
				}else{
					now.add(Calendar.DAY_OF_MONTH, 5);//如果是周一下单，并且选择周六发货，则首次发货日期=下单日期+5
				}
				break;
			case 2:
				if(reach==1){
					now.add(Calendar.DAY_OF_MONTH, 6);
				}else{
					now.add(Calendar.DAY_OF_MONTH, 4);
				}
				break;
			case 3:
				if(reach==1){
					now.add(Calendar.DAY_OF_MONTH, 5);
				}else{
					if(now.get(Calendar.HOUR_OF_DAY) < 18){
						now.add(Calendar.DAY_OF_MONTH, 3);
					}else{
						now.add(Calendar.DAY_OF_MONTH, 10);
					}
				}
				break;
			case 4:
				if(reach==1){
					now.add(Calendar.DAY_OF_MONTH, 4);
				}else{
					now.add(Calendar.DAY_OF_MONTH, 9);
				}
				break;
			case 5:
				if(reach==1){
					if(now.get(Calendar.HOUR_OF_DAY) < 18){
						now.add(Calendar.DAY_OF_MONTH, 3);
					}else{
						now.add(Calendar.DAY_OF_MONTH, 10);
					}
				}else{
					now.add(Calendar.DAY_OF_MONTH, 8);
				}
				break;
			case 6:
				if(reach==1){
					now.add(Calendar.DAY_OF_MONTH, 9);
				}else{
					now.add(Calendar.DAY_OF_MONTH, 7);
				}
				break;
			case 7:
				if(reach==1){
					now.add(Calendar.DAY_OF_MONTH, 8);
				}else{
					now.add(Calendar.DAY_OF_MONTH, 6);
				}
				break;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(now.getTime());
	}
	
	// 获得月份第一天的日期
	public static String getFirstDayOfMon(String time){
		int year = Integer.parseInt(time.substring(0,4));
		int month = Integer.parseInt(time.substring(5));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		return sdf.format(cal.getTime());
	}
	
	// 获得月份最后一天的日期
	public static String getLastDayOfMon(String time){
		int year = Integer.parseInt(time.substring(0,4));
		int month = Integer.parseInt(time.substring(5));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DATE, 1);
		cal.roll(Calendar.DATE, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		return sdf.format(cal.getTime());
	}
}