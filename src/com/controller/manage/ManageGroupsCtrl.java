package com.controller.manage;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import com.controller.WeixinApiCtrl;
import com.dao.GroupDAO;
import com.dao.MCDao;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.TemplateData;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;
import com.util.Constant;
import com.util.DeliveryDateUtil;

public class ManageGroupsCtrl extends Controller {
	
	
	public void CheckOut(){
		List<Record> list=Db.find("select b.openid,sum(a.jsMoney) 'tMoney',a.jsAid "
				+ " from f_order as  a left join f_account as b on b.id=a.jsAid"
				+ " where jsAid>0 and jsMoney>0 and isCheckout=0 and a.state in(2,3)"
				+ " group by b.openid");
		for (Record rd : list) {
			if(rd.getDouble("tMoney")>=1){
				boolean flag= com.util.SendCashRed.Send(getRequest(), "花美美", rd.getStr("openid"),
					String.valueOf( (int) (rd.getDouble("tMoney")*100)), "1", "新年快乐,大吉大利", "邀请参团得红包活动", "备注信息");
				if(flag){
					Db.update("update f_order set isCheckout=1 where jsAid=? and isCheckout=0",rd.getInt("jsAid"));
				}
			}
		}
		renderJson(list);
	}
	
    /**
     * 拼团列表
     */
 	public void GroupsList()  {
    	Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
    	String ptNo = getPara("ptNo");
    	String aid = getPara("aid");
    	String ptTimeS = getPara("ptTimeS");
    	String ptTimeE = getPara("ptTimeE");
    	String needCount = getPara("needCount");
    	String hadCount = getPara("hadCount");
    	Integer state = getParaToInt("state")==null?2:getParaToInt("state");//默认显示【开团成功，未成团】
    	String fptid = getPara("fptid");
    	String cycle = getPara("cycle");
   	    
		Page<Record> page = GroupDAO.getGroupsList(pageno, 16, ptNo, aid, ptTimeS, ptTimeE, needCount, hadCount, state, fptid, cycle);
        
		
		
		setAttr("list", page.getList());
    	setAttr("ptNo", ptNo);
    	setAttr("aid", aid);
    	setAttr("ptTimeS", ptTimeS);
    	setAttr("ptTimeE", ptTimeE);
    	setAttr("needCount", needCount);
    	setAttr("hadCount", hadCount);
    	setAttr("state", state);
    	setAttr("fptid", fptid);
    	setAttr("proList",GroupDAO.GetGroupsPro());
    	setAttr("cycle", cycle);
    	
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("orderlist", page.getList());
		render("group.html");
	}
	/**
	 * 拼团详情
	 */
    public void QueryDetial() {
    	Integer ptNo = getParaToInt(0);
    	List<Record>list = Db.find("select a.id,a.ptNo,a.aid,b.nick,a.ctime,case when  b.robotis=0 then '非机器人' else '机器人' end  'robotis' from f_pingtuan_detail a left join f_account b on a.aid=b.id where ptNo=? ",ptNo);
    	setAttr("list", list);
		render("group_detail.html");
	}
    
    
    
    /**
     * 一键成团
	 * 加入机器人
	 * 不考虑过期关闭
	 */
	public void robot(){
		boolean flag=false;
		int ptNo=getParaToInt(0);//团号
		List<Record> robots=Db.find("select id,nick from f_account where robotis=1");
		Record rd=Db.findFirst("select ptNo,needCount-hadCount 'robotCount' from f_pingtuan where state=2  and ptNo=?",ptNo);
		if(rd!=null){
			//加入机器人
			for(int i=0;i<rd.getLong("robotCount");i++){
			    int aid=getRandommain(robots);
				Record f_pingtuan_detail=new Record();
				f_pingtuan_detail.set("ptNo", rd.getInt("ptNo"));
				f_pingtuan_detail.set("aid", aid);
				f_pingtuan_detail.set("ctime", new Date());
				flag=Db.save("f_pingtuan_detail", f_pingtuan_detail);
			}
			//拼团成功,修改单头
			flag=(Db.update("update f_pingtuan set hadCount=needCount,state=3 where ptNo=?",rd.getInt("ptNo"))==1)?true:false;
			List<Record> rds=Db.find("select ordercode,reach,cycle,sendCycle,type from f_order where state=6 and ptNo=?",rd.getInt("ptNo"));
			for (Record order : rds) {
				//修改状态、拼团成功时间
				flag=(Db.update("update f_order set state=1,ptSusTime=now() where ordercode=?", order.getStr("ordercode"))==1)?true:false;
				//生成批号
				List<String> piCodeList =DeliveryDateUtil.getPiCodeList(order.getInt("reach"), new Date(), order.getInt("sendCycle"), order.getInt("cycle"),order.getInt("type"),0,null);
				for(int i=0;i<piCodeList.size();i++){
					Record f_picode = new Record();
					f_picode.set("ordercode", order.getStr("ordercode"));
					f_picode.set("piCode", piCodeList.get(i));
					f_picode.set("reach", order.getInt("reach"));
					f_picode.set("num",i+1 );
					flag=Db.save("f_picode", f_picode);
				}
			}
			/*SyHoliday("20171002",7,2);
			SyHoliday("20171007",7,2);*/
			if(flag==true){
				sendPintunMsgToAll(rd.getInt("ptNo"));//拼团成功后，给所有团员发信息
			}
		}else{
			flag=false;
		}
		
		renderJson(flag);
		
		
	}
	
	/**
	 * 拼团成功后，给团中所有人发信息
	 * @param ptNo
	 */
	private  void sendPintunMsgToAll(int ptNo){
		String url=Constant.getHost +"/account/groupDetail/"+ ptNo;
		WeixinApiCtrl.setApiConfig();
		List<Record> openIdlist=Db.find("select c.ptNo,d.name,b.openid,b.id from f_pingtuan_detail as a left join f_account as b on a.aid=b.id left join f_pingtuan as c on a.ptNo=c.ptNo"
				+ " left join f_flower_pro as d on d.id=c.fptid where b.robotis=0 and  a.ptNo=?",ptNo);
		for (Record rd : openIdlist) {
	    	Record order=Db.findFirst("select picode,a.ordercode from f_order a left join f_picode b "
	    			+ " on a.ordercode=b.ordercode where ptNo=? and  aid=? and num=1",rd.getInt("ptNo"),rd.getInt("id"));
	    	String firstDate=order.getStr("picode");
	    	firstDate=firstDate.substring(0, 4)+"-"+firstDate.substring(4, 6)+"-"+firstDate.substring(6,8);
			ApiResult result = TemplateMsgApi.send(TemplateData.New()
				    // 消息接收者
				    .setTouser(rd.getStr("openid"))
				    // 模板id
				    .setTemplate_id(WeixinApiCtrl.gettemplateId("拼团成功通知"))
				    .setTopcolor("#eb414a")
				    .setUrl(url)
				    .add("first", "拼团成功,准备收花", "#333")
				    .add("keyword1", order.getStr("ordercode"), "#333")
				    .add("keyword2", rd.getStr("name"), "#333")
				    .add("remark", "\n鲜花首次送达日期："+firstDate, "#888")
				    .build());
					 WeixinApiCtrl.sendTemplateMsg(result.getJson());
		}
	}
	
	/**
	 * 随机抓个机器人
	 * @param robots
	 * @return
	 */
	private  int getRandommain(List<Record> robots) {
		try {
			int max=robots.size();
	        Random random = new Random();
	        int index = random.nextInt(max);  //nextInt(4)将产生0,1,2,3
	        Record aid=robots.get(index);
	        return aid.getInt("id");
		} catch (Exception e) {
			return 92;
		}
        
    }
	
	
	
	/**
	 * 一键退款
	 */
	public void refund(){
		WeixinApiCtrl.setApiConfig();
		String url=Constant.getHost +"/service/myorder";
		int ptNo=getParaToInt(0);//团号
		boolean flag=false;
		int state=Db.queryInt("select state from f_pingtuan where ptNo=?",ptNo);
		if(state!=2){
			flag=false;
		}else{
			List<Record> list=Db.find("select aid,ordercode,b.openid,a.price from f_order as a "
					+ "  left join f_account b on a.aid=b.id where a.ptNo=? and a.type=8 and a.state=6",ptNo);
			for (Record rd : list) {
				flag=saveRefund(rd.getInt("aid"), rd.getStr("ordercode"), "拼团不成功");
				if(flag==true){
					//发退款通知
		            ApiResult ar = TemplateMsgApi.send(TemplateData.New()
		    			    // 消息接收者
		    			    .setTouser(rd.getStr("openid"))
		    			    // 模板id
		    			    .setTemplate_id(WeixinApiCtrl.gettemplateId("退款通知"))
		    			    .setTopcolor("#eb414a")
		    			    .setUrl(url)
		    			    .add("first", "您的订单"+rd.getStr("ordercode")+"已经发起退款申请", "#333")
		    			    .add("reason", "拼团失败", "#333")
		    			    .add("refund", rd.getDouble("price").toString(), "#333")
		    			    .add("remark", "请留意微信退款信息", "#888")
		    			    .build());
				}
			}
			Db.update("update f_pingtuan set state=4 where ptNo=?", ptNo);
			
		}
		renderJson(flag);
	}
	
	/**
	 * 2018-01-18 
	 * GUXUKAI
	 * 拼团到期提醒
	 */

	public void expire() {	
		WeixinApiCtrl.setApiConfig();
		int ptNo=getParaToInt(0);//团号
		String url=Constant.getHost +"/account/groupDetail/"+ ptNo;
		Record record = Db.findFirst("select b.openid,a.hadCount,c.name,a.aid,SUBSTR(c.itinfo4 FROM 8) 'img' from f_pingtuan a left join f_account b on a.aid = b.id left join f_flower_pro c on a.fptid=c.id where a.state=2 and a.ptNo=?",ptNo);
		if (record==null) {
			renderJson(false);
		}else {
			ApiResult ar = TemplateMsgApi.send(TemplateData.New()
	 			    // 消息接收者
	 			    .setTouser(record.getStr("openid"))
	 			    // 模板id
	 			    .setTemplate_id(WeixinApiCtrl.gettemplateId("服务状态提醒"))
	 			    .setTopcolor("#eb414a")
	 			    .setUrl(url)
	 			    .add("first", "主人，你有一个团即将过期，我等的好捉急啊，赶快分享你的专属海报给好友一起参团吧！", "#333")
	 			    .add("keyword1", "拼团", "#333")
	 			    .add("keyword2", "即将到期", "#333")
	 			    .add("remark", "请点击查看详情", "#888")
	 			    .build());
			if (ar.isSucceed()) {
				MCDao.sendPicTun(record.getStr("openid"),record.getInt("aid"),ptNo,record.getStr("img"));
				renderJson(true);
			}else {
				renderJson(false);
			}
		}
		
		
		
	}
	/**
	 * 拼团不成功退款
	 */
	public static boolean saveRefund(final int aid, final String ordercode, final String remark){
		boolean flag=false;
		flag=Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				boolean result = false;
				// 待成团的订单可退款
				Record order = Db.findFirst("select id from f_order where aid=? and ordercode=? and type=8 and  state=6", aid, ordercode);
				if(order != null){
					order.set("state", Constant.orderState.STATE4.state);
					if(Db.update("f_order", order)){
						Record refund = new Record();
						refund.set("ordercode", ordercode);
						refund.set("remark_a", remark);
						refund.set("time_a", new Date());
						refund.set("state", 3);//待处理，用户不能撤销
						Record fo = Db.findFirst("select id from f_refund where ordercode=?", ordercode);
						if(fo != null){
							refund.set("id", fo.get("id"));
							refund.set("state", 3);//待处理，用户不能撤销
							result = Db.update("f_refund", refund);
						}else{
							result = Db.save("f_refund", refund);
						}
						if(result){
							//兑换订单没有花籽
							Db.update("update f_flowerseed set state=3 where username=? and state=0",ordercode);//收回花籽
						}
					}
				}
				return result;
			}
		});
		return flag;
		
		
	}
}
