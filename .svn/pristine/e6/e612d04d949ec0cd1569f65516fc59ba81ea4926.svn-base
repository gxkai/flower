package com.dao;

import java.util.Date;
import java.util.List;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * @Desc 客户满意度调查
 * @author Glacier
 * @time 2017年4月5日
 */
public  class ServiceDao {
	
	/**获取用户满意度问题 f_question**/
	public static List<Record> getClientQuestion() {
		String sql = "select * from f_invest_question";
		return Db.find(sql);
	}
	
	/**将用户满意度答案填入数据库 f_invest_result**/
	public static boolean insertAnswer(String answer,int aid) {
		
		Record result = new Record();
		result.set("answer", answer);
		result.set("inDateTime",new Date());
		result.set("inAid", aid);
		
		return Db.save("f_invest_result", result);
	}
	
}
