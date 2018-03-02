package com.controller.front;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.dao.ServiceDao;
import com.interceptor.FrontInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@Before(FrontInterceptor.class)
public class ServiceCtrl extends Controller {
	
	/**
	 * 客户满意度调查
	 */
	public void ClientSatisfaction() {
		List<Record> question = ServiceDao.getClientQuestion();
		setAttr("questionList", question);
		render("ClientSatSur.html");
	}
	
	/**
	 * 将客户满意度答案写入数据库
	 */
	public void saveResult() {
		Record account = getSessionAttr("account");	
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean R = false;
		String msg = ""; //反馈信息
		
		//首先判断用户是否已经填写过
		Number flag = Db.queryNumber("select count(1) from f_invest_result where inAid = ?", account.getInt("id")); 

	 	if (flag.intValue() == 1 ) {
	 		msg = "您已经填写过了哦！";
		}else {
			String answer = "";
			//问题个数决定插入次数  leg==>问题个数
			String sql = "select * from f_invest_question";
			List<Record> list = Db.find(sql);
			int leg = list.size();
			for (int i = 1; i <= leg; i++) {
			    answer += getPara("" + i + "");
			}

			R = ServiceDao.insertAnswer(answer, account.getInt("id"));
			msg = "感谢您的评价!";
		}
		
	 	resultMap.put("R", R);
		resultMap.put("msg", msg);
		renderJson(resultMap);
	}

}
