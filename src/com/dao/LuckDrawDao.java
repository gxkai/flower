package com.dao;

import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class LuckDrawDao {

	/**
	 * 获取奖池列表
	 * @return
	 */
	public static Page<Record> GetPrizeList(int type,int state,int pageno,int pagesize){
		String strSelect="select id,money,wScore,"
				+ " case type "
				+ " when 1 then '大转盘抽奖' "
				+ " when 2 then '双11活动' "
				+ " when 3 then '12星座翻牌' "
				+ " when 4 then '小目标奖品' "
				+ " when 5 then '见证红包' "
				+ " end as type ,"
				+ " pName,pImgurl,pMsg,pNum,pAreaS,pAreaE,CONCAT(pAreaS,'<=X<',pAreaE) 'pArea',origin, case when state=0 then '暂停' else '启用' end 'state',contentId";
		String strWhere=" from f_prizeList where 1=1 ";
		if(state!=10){
			strWhere+=" and state="+state;
		}
		if(type!=0){
			strWhere+=" and type="+type;
		}
		strWhere+=" order by pAreaS";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
		
	}
	
	/**
	 * 获取奖品详情
	 * @param id
	 * @return
	 */
	public static Record QueryPrizeInfo(int id){
		Record rd=Db.findFirst("select id,money,wScore,type,pName,pMsg,pImgurl,pNum,pAreaS,pAreaE,state,origin,contentId from f_prizeList where id=?",id);
		return rd;
		
	}
	
	/**
	 * 中奖列表
	 * @param state
	 * @param pageno
	 * @param pagesize
	 * @return
	 */
	public static Page<Record> GetWinPrizeList(int state,int pageno,int pagesize,int origin,String aid,int type){
		String strSelect="select a.id,b.id 'aid',b.nick,a.pname,luckTime,case when a.state=1 then '已经发放' else '未发放' end 'state',"
				+ " case when origin=1 then '非平台奖品' when origin=2 then '花籽' when origin=3 then '花票' when origin=4 then '红包' else '走心的话' end 'origin',a.money,a.moneyUpdate,"
				+ " case when a.type=1 then '双11专题抽奖' else '大转盘抽奖' end 'type' ";
		String strWhere="from f_luckdraw_result a left join f_account b on a.aid=b.id left join f_prizeList c on a.prizeid=c.id where 1=1 ";
		if(state!=10){
			strWhere+=" and a.state="+state;
		}
		if(origin!=10){
			strWhere+=" and c.origin="+origin;
		}
		if(type!=0){
			strWhere+=" and a.type="+type;
		}
		if(aid!=null&&aid!=""){
			strWhere+=" and a.aid="+aid;
		}
		strWhere+=" order by luckTime desc";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
		
	}
	
	/**
	 * 获取翻牌结果
	 * @param pageno
	 * @param pagesize
	 * @return
	 */
	public static Page<Record> GetTrunResult(int pageno,int pagesize,String aid,String lrId){
		String strSelect="select a.lrId,b.aid 'aid01',c.nick 'nick01',a.aid 'aid02',d.nick 'nick02',a.tlId,a.msg,a.money,a.ctime " ;
		String strWhere="from f_turn_result as a "
				+ " left join  f_luckdraw_result as b on a.lrId=b.id "
				+ " left join f_account as c on b.aid=c.id "
				+ " left join f_account as d on a.aid=d.id where 1=1";
		if(aid!=null&&aid!=""){
			strWhere+=" and b.aid="+aid;
		}
		if(lrId!=null&& lrId!=""){
			strWhere+=" and a.lrId="+lrId;
		}
		strWhere+=" order by ctime desc";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
		
	}
	
	/**
	 * 获取翻牌列表
	 * @return
	 */
	public static Page<Record> GetTurnList(int state,int pageno,int pagesize){
		String strSelect="select id,maxMoney,minMoney, minArea,maxArea,CONCAT(minArea,'<=X<',maxArea) 'Area',msg,money,case when state=0 then '暂停' else '启用' end 'state'  ";
		String strWhere=" from f_trun_list where 1=1 ";
		if(state!=10){
			strWhere+=" and state="+state;
		}
		strWhere+=" order by minArea";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
		
	}
	/**
	 * 获取翻牌详情
	 * @param id
	 * @return
	 */
	public static Record QueryTurnDetailInfo(int id){
		Record rd=Db.findFirst("select  *from f_trun_list where id=?",id);
		return rd;
		
	}
	/**
	 * 获取翻牌图片相关
	 * @return
	 */
	public static Page<Record> GetTurnImgList(int pageno,int pagesize){
		String strSelect="select  id,activeTitle,activeDate,img01,img02,img03,img04,img05,img06,img07,img08,img09,img10,img11,img12,img13,img14,img15";
		String strWhere=" from f_turn_img where 1=1 order by activeDate asc";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
		
	}
	/**
	 * 获取翻牌图片详情
	 * @param id
	 * @return
	 */
	public static Record QueryTurnImgDetailInfo(int id){
		Record rd=Db.findFirst("select  *from f_turn_img where id=?",id);
		return rd;
		
	}
	
	/**
	 * 新年小目标列表
	 */
	/**
	 * 获取翻牌列表
	 * @return
	 */
	public static Page<Record> getTarget(int isClose,String aid,int pageno,int pagesize){
		String strSelect="select id,aid,contentStr,tgtime,wNumNew,wNumOld,wScore,wHours,isClose,prizeId  ";
		String strWhere=" from f_target where 1=1 ";
		if(isClose!=10){
			strWhere+=" and isClose="+isClose;
		}
		if (StrKit.notBlank(aid)) {
			strWhere+=" and aid="+aid;
		}
		strWhere+=" order by wNumNew,wNumOld desc";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
		
	}
}
