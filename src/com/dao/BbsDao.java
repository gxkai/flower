package com.dao;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.io.UnsupportedEncodingException;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class BbsDao {

	
	public static Page<Record> GetThemeList(int pageno,int pagesize,int isValid,String title ){
		
		String strSelect="select id,title,content,imgurl,postNum,focusNum,ctime,userId,orderId,isValid ";
		String strWhere=" from f_theme where 1=1";
		if(isValid!=10){
			strWhere=strWhere+" and isValid ="+isValid ;
		}
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
	}
	public static Record QueryThemeInfo(int id){
		Record rd=Db.findFirst(" select id,title,content,imgurl,postNum,focusNum,ctime,userId,orderId,isValid from f_theme where id=?",id);
		return rd;
	}




	public static Page<Record>getpostList(int pageno, int pagesize,String aid,int isTop,int type,int isValid,int themeId,String ctim_start,String ctime_end,int isSelected,String id) throws UnsupportedEncodingException{
		String sqlColumnSelect = "select a.id,b.nick,a.aid,a.content,a.img01,a.img02,a.img03,a.img04,a.img05,a.img06,a.img07,a.img08,a.img09,a.video,a.ctime,case a.isTop when 0 then '不置顶' else '置顶' end as isTop ,a.fingerNum,a.commentNum,a.collectNum,case a.type when 0 then '非话题贴' else '话题贴' end as type,case a.isSelected when 0 then '非精选帖' else '精选帖' end as isSelected,a.themeId,case a.isvaild when 0 then '无效贴' else '有效贴' end as isValid";
		String sqlExceptionSelect = "from f_post a left join f_account b on a.aid = b.id where 1=1";
		if (StrKit.notBlank(aid)) {
			sqlExceptionSelect += String.format(" and a.aid = %s", aid);
		}
		if (StrKit.notBlank(id)) {
			sqlExceptionSelect += String.format(" and a.id = %s", id);
		}
		if (isTop!=-1) {
			sqlExceptionSelect += String.format(" and a.isTop = %d", isTop);
		}
		if (isSelected!=-1) {
			sqlExceptionSelect += String.format(" and a.isSelected = %d", isSelected);
		}
		if (themeId!=-1) {
			sqlExceptionSelect += String.format(" and a.themeId = %d", themeId);
		}
		if (type!=-1) {
			sqlExceptionSelect += String.format(" and a.type = %d", type);
		}
		if (isValid!=-1) {
			sqlExceptionSelect += String.format(" and a.isvaild = %d", isValid);
		}
		if (StrKit.notBlank(ctim_start) || StrKit.notBlank(ctime_end)) {
	    	if (StrKit.notBlank(ctim_start)) {
		    	sqlExceptionSelect +=String.format(" and left(a.ctime,10)>='%s'", ctim_start);
		    }
		    if (StrKit.notBlank(ctime_end)) {
		    	sqlExceptionSelect +=String.format(" and left(a.ctime,10)<='%s'", ctime_end);
		    }
		}
		    else{
		    	sqlExceptionSelect +=" and DATE_FORMAT(a.ctime,'%y-%m-%d')=DATE_FORMAT(now(),'%y-%m-%d')";
		}
		sqlExceptionSelect += " order by a.ctime desc";
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	
	public static Page<Record>getcommentsList(int pageno, int pagesize,String aid,int isValid,int postId,String ctim_start,String ctime_end) throws UnsupportedEncodingException{
		String sqlColumnSelect = "select id,postId,aid,comStr,fingerNum,replyNum,ctime,case isValid when 0 then '无效评论' else '有效评论' end as isValid";
		String sqlExceptionSelect = "from f_comments where 1=1";
		sqlExceptionSelect += String.format(" and postId = %d", postId);
		if (StrKit.notBlank(aid)) {
			sqlExceptionSelect += String.format(" and aid = %s", aid);
		}
		if (isValid!=-1) {
			sqlExceptionSelect += String.format(" and isValid = %d", isValid);
		}
		
		if (StrKit.notBlank(ctim_start)) {
			sqlExceptionSelect +=String.format(" and left(ctime,10)>='%s'", ctim_start);
		}
		if (StrKit.notBlank(ctime_end)) {
			sqlExceptionSelect +=String.format(" and left(ctime,10)<='%s'", ctime_end);
		}
		sqlExceptionSelect += " order by ctime desc";
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	
	public static Page<Record>gettipsList(int pageno, int pagesize,String postId,String tipCode,String tipAid,int state) {
		String sqlColumnSelect = "select id,tipCode,tipAid,postId,tipMoney,cTime,case state when 0 then '未支付' else '已支付' end as state,payTime";
		String sqlExceptionSelect = "from f_tips where 1=1";
		sqlExceptionSelect += String.format(" and postId = %s", postId);
		if (StrKit.notBlank(tipCode)) {
			sqlExceptionSelect += String.format(" and tipCode = '%s'", tipCode);
		}
		if (StrKit.notBlank(tipAid)) {
			sqlExceptionSelect += String.format(" and tipAid = %s", tipAid);
		}
		if (state!=-1) {
			sqlExceptionSelect += String.format(" and state = %d", state);
		}
		
		
		sqlExceptionSelect += " order by cTime desc";
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	
	
	public static Page<Record>getreplyList(int pageno, int pagesize,String aid,int isValid,int comId,String ctim_start,String ctime_end) throws UnsupportedEncodingException{
		String sqlColumnSelect = "select id,comId,repid,aid,replyStr,ctime,case isValid when 0 then '无效跟帖' else '有效跟帖' end as isValid";
		String sqlExceptionSelect = "from f_reply where 1=1";
		sqlExceptionSelect += String.format(" and comId = %d", comId);
		if (StrKit.notBlank(aid)) {
			sqlExceptionSelect += String.format(" and aid = %s", aid);
		}
		if (isValid!=-1) {
			sqlExceptionSelect += String.format(" and isValid = %d", isValid);
		}
		
		if (StrKit.notBlank(ctim_start)) {
			sqlExceptionSelect +=String.format(" and left(ctime,10)>='%s'", ctim_start);
		}
		if (StrKit.notBlank(ctime_end)) {
			sqlExceptionSelect +=String.format(" and left(ctime,10)<='%s'", ctime_end);
		}
		sqlExceptionSelect += " order by ctime desc";
		Page<Record> page=Db.paginate(pageno,pagesize,sqlColumnSelect,sqlExceptionSelect);
		return page;
	}
	
	public static  Page<Record>getintegrallist(int pageno,int pagesize, String aid,int state) {
		String select = "select id,aid,pid,score,gTime,uTime,state ";
		String sqlExceptSelect = " from f_myIntegral where 1=1 ";
		if (StrKit.notBlank(aid)) {
			sqlExceptSelect += " and aid ="+aid;
		}
		if (state!=10) {
			sqlExceptSelect += " and state ="+state;
		}
		sqlExceptSelect+= " order by aid desc";
		Page<Record>page = Db.paginate(pageno, pagesize, select, sqlExceptSelect);
		return page;
	}
}
