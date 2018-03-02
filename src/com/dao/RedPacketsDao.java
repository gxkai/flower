package com.dao;



import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class RedPacketsDao {

	/**
	 * 查看【商品列表】
	 * @param state 状态：0下架 ;1上架
	 * @param pageno 第n页
	 * @param pagesize 每页呈现的记录数
	 * @return
	 */
	public static Page<Record> QueryProudct(int state,int pageno,int pagesize,Integer userType){
		String strSelect="select a.imgurl01,a.imgurl02,a.imgurl03,a.orderId,case when a.userType=1 then '总裁专用' else '普通用户' end 'userType', a.id,a.name,b.name 'fpname',a.pnum,a.pmoney, case when a.state=1 then '上架' else '下架' end 'state',case when a.protype=1 then '主品' else '副品' end 'protype'";
		String strWhere="from f_redpackets_pro a left join f_flower_pro b on a.fpid=b.id where 1=1";
		if(state!=10){
			strWhere+=" and a.state="+state;
		}
		if(userType!=10){
			strWhere+=" and a.userType="+userType;
		}
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
	}
	/**
	 * 根据ID号查询商品信息
	 * @param id
	 * @return
	 */
	public static Record QueryProduct(int id){
		Record rd=Db.findFirst("select imgurl01,imgurl02,imgurl03,userType,orderId,id,name,fpid,pnum,state,pmoney,protype from f_redpackets_pro where id=?",id);
	    return rd;
	}
	public static List<Record> FlowerProductList(){
		List<Record> list=Db.find("select id,name from f_flower_pro where state=0 ");
		return list;
	}
	public static List<Record> FlowerProductList2(){
		List<Record> list=Db.find("select id,name from f_flower_pro  ");
		return list;
	}
	
	/**
	 * 查看【发包列表】
	 * @param state 红包状态：0未支付; 1已支付未抢完;2已抢完（终结状态）;3已退款（终结状态）
	 * @param pageno
	 * @param pagesize
	 * @return
	 */
	public static Page<Record> QueryRedPackets(int state,int type,int pageno,int pagesize ,String aid,String nick,String stime_start,String stime_end){
		String strSelect="select a.id,"
				+ "case when a.type=1 then '普通红包' when a.type=2 then '拼手气红包' when a.type=3 then '代颜红包' when a.type=4 then '中奖红包' end  'type',"
				+ "a.money,a.quantity1,a.quantity2,a.msg,a.ctime,a.stime,a.ttime,a.tmoney,a.opuser,"
				+ "case when a.state=0 then '未支付' when a.state=1 then '已支付未抢完' when a.state=2 then '已抢完' when a.state=3 then '已退款' when a.state=4 then '交易关闭' end 'state',b.nick,a.aid";
		
		String strWhere="from f_redpackets a left join f_account b on a.aid=b.id where 1=1";
		if(state!=10){
			strWhere+=" and a.state="+state;
		}
		if(type!=10){
			strWhere+=" and a.type="+type;
		}
		if(nick!=null&&nick!=""){
			strWhere+=" and b.nick="+"'"+nick+"'";
		}
		if(aid!=""&&aid!=null){
			strWhere+=" and a.aid="+aid;
		}
		if(stime_start!=""&&stime_start!=null&&stime_end!=""&&stime_end!=null){
			strWhere+=String.format(" and a.stime>='%s' and a.stime<='%s'", stime_start,stime_end);
		}
		strWhere += "  order by a.ctime desc";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		
		return pg;
	}
	
	
	/**
	 * 查看红包详情
	 * @param id 红包唯一编号
	 * @return
	 */
	public static List<Record> QueryDetial(int id){
		List<Record> list=Db.find("select a.id,a.rpid,c.name,a.dmoney,case when a.isopen=0 then '未拆'  when a.isopen=1 then '已拆' when a.isopen=2 then '已退款' when a.isopen=3 then '转赠中' when a.isopen=4 then '已兑换'  end 'isopen',a.aid,a.gtime,a.oid,d.nick,e.ctime,a.orderlist  from f_redpackets_detial a left join f_redpackets b on a.rpid=b.id left join f_redpackets_pro c on a.rppid=c.id left join f_account d on a.aid=d.id left join f_order e on a.oid=e.ordercode where a.rpid=?",id);
		return list;
	}
	
	/**
	 * 立即退款
	 * @param id 红包唯一编号
	 * @return
	 */
	public static List<Record> QueryDetial2(int id){
		List<Record> list=Db.find("select a.id,c.nick,sum(b.dmoney) 'money',sum(b.dmoney) 'tmoney' from f_redpackets a left join f_redpackets_detial b on a.id=b.rpid left join f_account c on a.aid=c.id where b.isopen=0 and a.id=?",id);
		return list;
	}
	
	/**
	 * 立即退款
	 * @param id 红包唯一编号
	 * @return
	 */
	public static List<Record> BackMoney2(int id){
		List<Record> list=Db.find("select a.id,c.nick,a.tmoney,sum(b.dmoney) 'money' from f_redpackets a left join f_redpackets_detial b on a.id=b.rpid left join f_account c on a.aid=c.id where b.isopen<>1 and a.id=?",id);
		return list;
	}
	
	/**
	 * 拆包列表
	 * @param isopen
	 * @param pageno
	 * @param pagesize
	 * @return
	 */
	public static Page<Record> QueryGetRedPackets(int isopen,int isduihuan,String cnick,String caid,String faid,String rpid, int pageno,int pagesize){
		String strSelect="select a.id,a.aid 'caid',c.nick 'cnick',a.gtime,a.oid,case when a.isopen=0 then '未拆' when a.isopen=1 then '已拆' when a.isopen=2 then '已退款'   when a.isopen=3 then '转赠中' when a.isopen=4 then  '已兑换' end  'isopen',rpid,b.aid 'faid',d.nick 'fnick',e.name ,a.dmoney,a.orderlist";
		String strWhere="from f_redpackets_detial a left join f_redpackets b on a.rpid=b.id left join f_account c on a.aid=c.id left join f_account d on b.aid=d.id left join f_redpackets_pro e on a.rppid=e.id where 1=1 ";
		if(isopen!=10){
			 strWhere+=" and isopen="+isopen;
		}
		if(isduihuan!=10){
			if(isduihuan==0){
				strWhere+=" and  oid is null";
			}
			if(isduihuan==1){
				strWhere+=" and oid is not null";
			}
		}
		if(cnick!=null&&cnick.equals("")==false){
			strWhere+=" and c.nick='"+cnick+"'";
		}
		if(caid != null && caid.equals("")==false){
			strWhere+=" and a.aid="+caid;
		}
		if(faid != null && faid.equals("")==false){
			strWhere+=" and b.aid="+faid;
		}
		if(rpid != null && rpid.equals("")==false){
			strWhere+=" and a.rpid="+rpid;
		}
		strWhere+=" order by a.aid desc,a.oid desc";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
	}
	
	
	/**
	 * 退款列表
	 * 没有拆的红包，理论上都需要退款
	 * 退款时间再商议
	 * @param state 2已退款  0未退款
	 * @param pageno
	 * @param pagesize
	 * @return
	 */
	public static Page<Record> BackMoney(int state,int pageno,int pagesize){
		String strSelect="select a.id,count(*)'wcai',sum(b.dmoney) 'money',c.id 'fid',c.nick,a.opuser,a.tmoney,a.ttime,a.stime,a.state,b.isopen";
		//查询超过7天，已支付，未拆
		String strWhere="from f_redpackets a left join f_redpackets_detial b on a.id=b.rpid left join f_account c on a.aid=c.id where b.isopen in(0,2) and a.state in(1,3) and a.stime<=DATE_SUB(now(),INTERVAL 7 DAY) ";
		if(state==0){
			strWhere+=" and a.state=1 ";
		}
		if(state==2){
			strWhere+=" and a.state=3 ";
		}
		strWhere+=" group by a.id";
		Page<Record> pg=Db.paginate(pageno, pagesize, strSelect, strWhere);
		return pg;
	} 
}
