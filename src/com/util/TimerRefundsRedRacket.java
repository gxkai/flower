package com.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * @dec 超过48小时的红包给用户退款
 * @author glacier
 *
 */
public class TimerRefundsRedRacket extends TimerTask{

	@Override
	public void run() {
		TimerLog.AddLog("TimerRefundsRedRacket 红包退款开始执行");
		
		Calendar calendar = Calendar.getInstance();  
	    calendar.setTime(new Date());  
	    calendar.add(Calendar.DAY_OF_MONTH, -2);  
	    Date date = calendar.getTime();  
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(df.format(date));
		//48 小时前
		String oldtime = df.format(date).toString();
		
		//获取未抢完的红包 (48小时前~现在 未抢完红包 -- 测试先不加)
				List<Record> list = Db.find( "select id,money,quantity1,quantity2,ctime,ttime,tmoney,state,aid from f_redpackets where state = '1'"
						+ "and (ctime >= "+oldtime+" and ctime <= NOW() ) ");
				
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
					/*
					BigDecimal tot2 = new BigDecimal("100");
					tot1 = tot1.multiply(tot2);//乘 100
					*/					
					String total_fee =String.valueOf(tot1.intValue());
					
					//进行退款
					//Map<String, String> resulet = new wxRefunds().Refunds(out_refund_no, out_trade_no, refund_fee, total_fee);
					
					//生成退款单
					Record f_refund = new Record();
					
					f_refund.set("ordercode", out_trade_no);
					f_refund.set("money", total_fee);
					f_refund.set("time_a", df.format(new Date()));
					f_refund.set("remark_b", "系统红包退款");
					f_refund.set("state", 0);
					
					boolean flat =  Db.save("f_refund", f_refund);
					
					if (flat) {
						//修改f_redpackets 添加退款时间ttime， 退款金额tmoney，状态改为3 已退款状态   退款账号admin(系统退款)
						Db.update("UPDATE f_redpackets SET ttime=NOW(),tmoney=?,state = 3,opuser='admin'  where id = ?",sum,list.get(i).getInt("id"));
						//根据单号修改单身状态
						Db.update("UPDATE f_redpackets_detial SET isopen = '2' WHERE rpid = ?",list.get(i).getInt("id"));	
					}else {
						System.out.println("生成退款单失败！");
					}
				}
		
		
		/*
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
		//		+ 		"and (ctime >= "+oldtime+" and ctime <= NOW() )";
		
		//获取未抢完的红包 (48小时前~现在 未抢完红包 -- 测试先不加)
		List<Record> list = Db.find( "select id,money,quantity1,quantity2,ctime,ttime,tmoney,state,aid from f_redpackets where state = '1'"
				+ "and (ctime >= "+oldtime+" and ctime <= NOW() ) ");
		
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
			
			System.out.println(resulet);
			
			//退款成功后修改 红包状态
			if (resulet.get("result_code").equals("SUCCESS")) {
				
				//修改f_redpackets 添加退款时间ttime， 退款金额tmoney，状态改为3 已退款状态   退款账号admin(系统退款)
				Db.update("UPDATE f_redpackets SET ttime=NOW(),tmoney=?,state = 3,opuser='admin'  where id = ?",sum,list.get(i).getInt("id"));
				
				//根据单号修改单身状态
				Db.update("UPDATE f_redpackets_detial SET isopen = '2' WHERE rpid = ?",list.get(i).getInt("id"));	
			}
		}*/
		 TimerLog.AddLog("TimerRefundsRedRacket 红包退款执行结束");
	}
	
	//获取48小时前时间
	public String gettime() {
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -2); //得到前一天
		Date date = calendar.getTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return df.toString();
	}
	
	
	
	
	
}
