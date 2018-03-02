package com.dao;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

/**
 * 拼团DAO
 * @author glacier
 *
 */
public class GroupDAO {
	/**
	 * 获取拼团列表
	 */
	public static Page<Record>getGroupsList(int pageno, int pagesize, String ptNo,String aid,String ptTimeS,String ptTimeE,String needCount,String hadCount,int state,String fptid,String cycle){
		String sqlColumnSelect = "select ptNo,aid,nick,ptTimeS,ptTimeE,needCount,hadCount,case when a.state=1 then '已建团,但团长未付款' when a.state=2 then '开团成功,未成团' when a.state=3 then '已成团' when a.state=4 then '过期关闭' end 'state',maxhours,a.price,c.name ";
		String sqlExceptionSelect = "from f_pingtuan a left join f_account b on a.aid=b.id left join f_flower_pro c on a.fptid=c.id where 1=1";
		if (StrKit.notBlank(ptNo)) {
			sqlExceptionSelect += String.format(" and a.ptNo=%s", ptNo);
		}
		if (StrKit.notBlank(aid)) {
			sqlExceptionSelect += String.format(" and a.aid=%s", aid);
		}
		if (StrKit.notBlank(needCount)) {
			sqlExceptionSelect += String.format(" and a.needCount=%s", needCount);
		}
		if (StrKit.notBlank(hadCount)) {
			sqlExceptionSelect += String.format(" and a.hadCount=%s", hadCount);
		}
		if (state!=10) {
			sqlExceptionSelect += String.format(" and a.state=%d", state);
		}
		if (StrKit.notBlank(fptid)) {
			sqlExceptionSelect += String.format(" and a.fptid=%s", fptid);
		}
		if (StrKit.notBlank(cycle)) {
			sqlExceptionSelect += String.format(" and a.cycle=%s", cycle);
		}
		if (StrKit.notBlank(ptTimeS)) {
			sqlExceptionSelect += String.format(" and a.ptTimeS like '%s' ", ptTimeS+"%");
		}
		if (StrKit.notBlank(ptTimeE)) {
			sqlExceptionSelect += String.format(" and a.ptTimeE like '%s' ", ptTimeE+"%");
		}
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
  		return page;
	}
	
	/**
	 * 拼团商品列表
	 * @return
	 */
	public static List<Record> GetGroupsPro(){
		return Db.find("select id,name from f_flower_pro where ptid=202");
	}
	
	/**
	 * 拼团列表
	 * @author Glacier
	 * @date 2017年8月14日
	 * @param pageSize 每页显示的数据条数
	 * @param curPage 当前显示的页数
	 * @param sql 增加查询条件
	 * @return
	 */
	public static  List<Record> groupList(int pageSize,int curPage,String sql,int aid) {
		
	/*	 String str = "SELECT a.ptNo,a.aid,a.ptTimeS,a.ptTimeE,a.needCount,a.hadCount,a.state,a.fptid,"
					+ "a.cycle,b.nick,b.headimg,b.isbuy,b.tjid "
					+ "	from f_pingtuan a "
					+ "	LEFT JOIN f_account b on a.aid = b.id"+ sql
					+ " ORDER BY a.ptTimeS DESC LIMIT "+(curPage-1)*pageSize+","+pageSize+"";
		  
		 System.err.println(str);*/
		
		
		List<Record> list = Db.find("SELECT a.ptNo,a.aid,a.ptTimeS,a.ptTimeE,a.needCount,a.hadCount,a.state,a.fptid,"
				+ "a.cycle,b.nick,b.headimg,b.isbuy,b.tjid,0 'isIn' "
				+ "	from f_pingtuan a "
				+ "	LEFT JOIN f_account b on a.aid = b.id"+ sql
				+ " ORDER BY a.ptTimeS DESC LIMIT ?,?",(curPage-1)*pageSize,pageSize);
		for (Record rd : list) {
			long count=Db.queryLong("select count(1) from f_pingtuan_detail where ptNo=? and aid=?",rd.getInt("ptNo"),aid);
			if(count>=1){
				rd.set("isIn", 1);//是否包含自己
			}
			
		}
		
		return list;
	}
	
	
	
	
	
	
	
}
