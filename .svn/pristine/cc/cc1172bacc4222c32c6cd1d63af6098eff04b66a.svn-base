package com.util;

import java.util.TimerTask;
import com.jfinal.plugin.activerecord.Db;

public class TaskService extends TimerTask{

	@Override
	public void run() {
		// 订单状态 和 用户状态不一致的要修改一致
        Number numyc = Db.queryNumber("SELECT COUNT(1) FROM f_account a LEFT JOIN f_order b ON a.id = b.aid WHERE a.isbuy = 0 AND b.state IN (1,2,3)");
        if(numyc.intValue() > 0){
        	String ycids = Db.queryStr("SELECT GROUP_CONCAT(a.id) FROM f_account a LEFT JOIN f_order b ON a.id = b.aid WHERE a.isbuy = 0 AND b.state IN (1,2,3)");
        	int numIs = Db.update("update f_account set isbuy = 1 where id in ("+ycids+")");
        	System.out.println("定时器：当前更新了用户表"+numIs+"条异常isbuy数据");
        }
        
        // 每隔四天清理状态为“待评价”的订单，修改成“已完成”
        int numDpj = Db.update("update f_order set state = 3 where state = 2");
        System.out.println("定时器：当前更改了"+numDpj+"条状态为待评价的数据");
	}
}
