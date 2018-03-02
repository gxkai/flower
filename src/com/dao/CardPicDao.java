package com.dao;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class CardPicDao {

	/**
	 * 获取祝福卡列表
	 * @return
	 */
	public static Page<Record> GetWishCardList(int type,int state,int pageno,int pagesize){
		String strSelect="select a.id, a.name,b.code_name,b.code_value,a.imgurl01,a.imgurl02,case when a.state=0 then '不显示' else '显示' end 'state',imgOrderId ";
		String strWhere=" from f_wishcardpic a left join f_dictionary b on a.typeId=b.code_value and code_key='wishcardType' and b.state=1 where 1=1 ";
		if(state!=10){
			strWhere+=" and a.state="+state;
		}
		if(type!=99){
			strWhere+=" and a.typeId="+type;
		}
		strWhere+=" order by a.typeId,a.imgOrderId";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
		
	}
	/**
	 * 获取祝福卡片详情
	 * @param id
	 * @return
	 */
	public static Record QueryWishCardInfo(int id){
		Record rd=Db.findFirst("select id,CAST(typeId AS CHAR) 'typeId' ,name,imgurl01,imgurl02,state from f_wishcardpic where id=?",id);
		return rd;
		
	}
	/**
	 * 获取祝福卡片的分类信息
	 * @return
	 */
	public static List<Record> GetWishCardType(){
	 List<Record> dicList=Db.find("select code_value,code_name from f_dictionary where code_key='wishcardType' and state=1");	
	 return dicList;
	}
	
	
	/**
	 * 获取红包卡面列表
	 * @return
	 */
	public static Page<Record> GetRedpacketsPicList(int type,int state,int pageno,int pagesize){
		String strSelect="select a.id, a.name,b.code_name,b.code_value,a.imgurl01,a.imgurl02,case when a.state=0 then '不显示' else '显示' end 'state',imgOrderId ";
		String strWhere=" from f_redpackets_pic a left join f_dictionary b on a.typeId=b.code_value and code_key='redpacketsPicType' and b.state=1 where 1=1 ";
		if(state!=10){
			strWhere+=" and a.state="+state;
		}
		if(type!=99){
			strWhere+=" and a.typeId="+type;
		}
		strWhere+=" order by a.typeId,a.imgOrderId";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
		
	}
	/**
	 * 获取红包卡面详情
	 * @param id
	 * @return
	 */
	public static Record QueryRedpacketsPicInfo(int id){
		Record rd=Db.findFirst("select id,CAST(typeId AS CHAR) 'typeId' ,name,imgurl01,imgurl02,state,imgOrderId from f_redpackets_pic where id=?",id);
		return rd;
		
	}
	/**
	 * 获取红包卡面的分类信息
	 * @return
	 */
	public static List<Record> GetRedpacketsPicType(){
	 List<Record> dicList=Db.find("select code_value,code_name from f_dictionary where code_key='redpacketsPicType' and state=1 order by orderId");	
	 return dicList;
	}
}
