package com.controller.manage;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.controller.WeixinApiCtrl;
import com.dao.BbsDao;
import com.interceptor.ManageInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.jfinal.weixin.sdk.api.TemplateData;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;
import com.util.Constant;
import com.util.Constant.seedType;
import com.util.QianNiuUpload;


/**
 * 美美社区管理
 * @author SHICHUNXIANG
 *
 */
@Before(ManageInterceptor.class)
public class ManageBbsCtrl extends Controller{
	/**
	 * 2018-01-16 
	 * GUXUKAI
	 * 提现记录
	 */

	public void withdrawCash() {
		Integer aid = getParaToInt("aid");
    	List<Record> list =Db.find("select *from  f_withdrawCash where aid = ?", aid);
    	setAttr("list", list);
		render("withdrawCash.html");
	}

	/**
	 * 发帖列表
	 * @throws UnsupportedEncodingException 
	 */
	public void postList() throws UnsupportedEncodingException{
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		String aid = getPara("aid");
		String id = getPara("id");
		Integer isTop = getParaToInt("isTop")==null?-1:getParaToInt("isTop");
		Integer type = getParaToInt("type")==null?-1:getParaToInt("type");
		Integer isValid = getParaToInt("isValid")==null?-1:getParaToInt("isValid");
		Integer themeId = getParaToInt("themeId")==null?-1:getParaToInt("themeId");	
		Integer isSelected = getParaToInt("isSelected")==null?-1:getParaToInt("isSelected");	
		String ctime_start = getPara("ctime_start");
		String ctime_end = getPara("ctime_end");
    	Page<Record> page = BbsDao.getpostList(pageno,16,aid,isTop,type,isValid,themeId,ctime_start,ctime_end,isSelected,id);
    	for(Record record : page.getList()) {
    		if (record.getStr("type").equals("非话题贴")) {
				record.set("themeId", "无话题");
			}else {
				record.set("themeId", Db.queryStr("select title from f_theme where id = ?",record.getInt("themeId")));
			}
    	}
    	List<Record> themelist = Db.find("select id,title from f_theme where isValid = 1");
    	setAttr("themelist", themelist);
    	setAttr("pageno", page.getPageNumber());
		setAttr("totalpage", page.getTotalPage());
		setAttr("totalrow", page.getTotalRow());
		setAttr("list", page.getList());
		setAttr("aid", aid);
		setAttr("id", id);
		setAttr("isTop", isTop);
		setAttr("isSelected", isSelected);
		setAttr("isValid", isValid);
		setAttr("type", type);
		setAttr("themeId", themeId);
		setAttr("ctime_start", ctime_start);
		setAttr("ctime_end", ctime_end);
		render("postlist.html");
	}
	/**
	 * 帖子详情
	 */
	      public void postDetail() {
	    	Integer id = getParaToInt(0);
	    	Record list =Db.findById("f_post", id);
	    	setAttr("list", list);
			render("postdetail.html");
		}
      /**
  	 * 帖子详情保存      
  	 */
		public void postSave() {
			Integer seedscount = getParaToInt("seedscount");
  	    	Integer id = getParaToInt("id");
  	    	Integer isValid = getParaToInt("isValid");
  	    	Integer isTop = getParaToInt("isTop");
  	    	Integer isSelected = getParaToInt("isSelected")==null?0:getParaToInt("isSelected");
  	    	Record aRecord= Db.findFirst("select a.aid,a.isSelected,b.openid,b.nick from f_post a left join f_account b on a.aid = b.id where a.id = ?",id);
  	  		Record record = new Record();
  	  		boolean ischange=false;
  	  	    record.set("isSelected", isSelected);//精选贴
		    if (aRecord.getInt("isSelected")==0&&isSelected==1) {
			   ischange=true;
		    }
  	  		record.set("id", id);
  	  		record.set("isVaild", isValid);
  	  		record.set("isTop", isTop);
  	        boolean result = Db.update("f_post", record);
  	        if (result&&ischange) {
  	        	ManageBbsCtrl.Addseeds(aRecord,seedscount);
			}
  	        renderJson(result);
  		}		      
	  /**
	 * 点赞详情
	 */
	      public void fingerDetail() {
	    	Integer id = getParaToInt(0);
	    	List<Record> list =Db.find("select *from f_finger where postId = ?",id);
	    	setAttr("list", list);
			render("fingerdetail.html");
		}
	    /**
		 * 2018-01-16 
		 * GUXUKAI
		 * 打赏详情
		 */

	      public void tipsDetail() {
	    	Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
	    	String postId = getPara("postId");
	  		String tipCode = getPara("tipCode");
	  		Integer state = getParaToInt("state")==null?-1:getParaToInt("state");
	  		String tipAid = getPara("tipAid");
	      	Page<Record> page = BbsDao.gettipsList(pageno,16,postId,tipCode,tipAid,state);
	      	setAttr("pageno", page.getPageNumber());
	  		setAttr("totalpage", page.getTotalPage());
	  		setAttr("totalrow", page.getTotalRow());
	  		setAttr("list", page.getList());
	  		setAttr("tipCode", tipCode);
	  		setAttr("tipAid", tipAid);
	  		setAttr("postId", postId);
	  		setAttr("state", state);
	  		render("tipsdetail.html");
		}  
	      /**
	  	 * 评论详情
	  	 * @throws UnsupportedEncodingException 
	  	 */
	  	public void commentsDetail() throws UnsupportedEncodingException{
	  		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
	  		String aid = getPara("aid");
	  		Integer postId = getParaToInt("postId");
	  		Integer isValid = getParaToInt("isValid")==null?-1:getParaToInt("isValid");
	  		String ctime_start = getPara("ctime_start");
	  		String ctime_end = getPara("ctime_end");
	      	Page<Record> page = BbsDao.getcommentsList(pageno,16,aid,isValid,postId,ctime_start,ctime_end);
	      	setAttr("pageno", page.getPageNumber());
	  		setAttr("totalpage", page.getTotalPage());
	  		setAttr("totalrow", page.getTotalRow());
	  		setAttr("list", page.getList());
	  		setAttr("aid", aid);
	  		setAttr("postId", postId);
	  		setAttr("isValid", isValid);
	  		setAttr("ctime_start", ctime_start);
	  		setAttr("ctime_end", ctime_end);
	  		render("commentsdetail.html");
	  	}
	  	/**
		 * 评论修改
		 */
		      public void commentsEdit() {
		    	Integer id = getParaToInt(0);
		    	Record list =Db.findById("f_comments", id);
		    	setAttr("list", list);
				render("commentsedit.html");
			}	  	
      /**
    	 * 评论保存      
    	 */
    	      public void commentsSave() {
    	    	Integer id = getParaToInt("id");
    	    	Integer isValid_new = getParaToInt("isValid");
    	    	Integer isValid_old = Db.queryInt("select isValid from f_comments where id = ?",id);
    	        Integer postId = Db.queryInt("select postId from f_comments where id = ?",id);
    	        boolean result = true;
    	    	if (isValid_new==1&&isValid_old==0) {
    	    		result=Db.tx(new IAtom() {	
						@Override
						public boolean run() throws SQLException {
							int count = Db.update("update f_comments set isValid=1 where id = ?",id);
                            int count2 = Db.update("update f_post set commentNum = commentNum+1 where id = ?",postId);
							return count == 1 && count2 ==1;
						}
					});
				}else if (isValid_new==0&&isValid_old==1) {
    	    		result = Db.tx(new IAtom() {
						@Override
						public boolean run() throws SQLException {
							int count = Db.update("update f_comments set isValid=0 where id = ?",id);
                            int count2 = Db.update("update f_post set commentNum = commentNum-1 where id = ?",postId);
							return count == 1 && count2 ==1;
						}
					});
				}  		
    	        renderJson(result);
    		}
	      /**
		  	 * 跟帖详情
		  	 * @throws UnsupportedEncodingException 
		  	 */
		  	public void replyDetail() throws UnsupportedEncodingException{
		  		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		  		String aid = getPara("aid");
		  		Integer comId = getParaToInt("comId");
		  		Integer isValid = getParaToInt("isValid")==null?-1:getParaToInt("isValid");
		  		String ctime_start = getPara("ctime_start");
		  		String ctime_end = getPara("ctime_end");
		      	Page<Record> page = BbsDao.getreplyList(pageno,16,aid,isValid,comId,ctime_start,ctime_end);
		      	setAttr("pageno", page.getPageNumber());
		  		setAttr("totalpage", page.getTotalPage());
		  		setAttr("totalrow", page.getTotalRow());
		  		setAttr("list", page.getList());
		  		setAttr("aid", aid);
		  		setAttr("comId", comId);
		  		setAttr("isValid", isValid);
		  		setAttr("ctime_start", ctime_start);
		  		setAttr("ctime_end", ctime_end);
		  		render("replydetail.html");
		  	}
		  	/**
			 * 跟帖修改
			 */
			      public void replyEdit() {
			    	Integer id = getParaToInt(0);
			    	Record list =Db.findById("f_reply", id);
			    	setAttr("list", list);
					render("replyedit.html");
				}	  	
	      /**
	    	 * 跟帖保存      
	    	 */
	    	      public void replySave() {
	    	    	Integer id = getParaToInt("id");
	    	    	Integer isValid = getParaToInt("isValid");
	    	  		Record record = new Record();
	    	  		record.set("id", id);
	    	  		record.set("isValid", isValid);
	    	        boolean result = Db.update("f_reply", record);
	    	        renderJson(result);
	    		}		  	
	
	/**
	 * 话题列表
	 */
	public void themeList(){
		String title=getPara("title");//标题
		int isValid=getParaToInt("isValid")==null?10:getParaToInt("isValid");//是否有效
		Page<Record> page=BbsDao.GetThemeList(1, 16, isValid, title);
		setAttr("totalpage", page.getTotalPage());//总页数
		setAttr("totalrow", page.getTotalRow());//总行数
		setAttr("themeList",page.getList());//结果列表
		setAttr("pageno", page.getPageNumber());//当前页号
		setAttr("title", title);
		setAttr("isValid", isValid);
		render("themelist.html");
	}
	/**
	 * 新增话题列表
	 * @throws IOException
	 */
	public void SaveTheme() throws IOException{
		boolean result = false;
		String title=getPara("title");//标题
		String content=getPara("content");//内容
		String imgurl=getPara("imgurl");//奖品图片地址
		int orderId=getParaToInt("orderId");//排序序号
		int isValid=getParaToInt("isValid");//是否有效
		if (StrKit.notBlank(imgurl)) {
			imgurl=QianNiuUpload.upload(imgurl);
		}
		Record record = new Record();
		record.set("title",title);
		record.set("content",content);
		record.set("imgurl",imgurl);
		record.set("orderId", orderId );
		record.set("isValid", isValid);
		record.set("ctime",new Date());
		
		Record admin = getSessionAttr("admin");
    	String username=admin.getStr("username");//操作账号
		record.set("userId", username);
		result = Db.save("f_theme", record);
		renderJson("result", result);
	}
	/**
	 * 编辑话题列表
	 * @throws IOException
	 */
	public void EditTheme() throws IOException{
		boolean result = false;
		int id=getParaToInt("id");//ID号	
		String title=getPara("title");//标题
		String content=getPara("content");//内容
		String imgurl=getPara("imgurl");//奖品图片地址
		int orderId=getParaToInt("orderId");//排序序号
		int isValid=getParaToInt("isValid");//是否有效
		
		Record record = new Record();
		record.set("id", id);
		record.set("title",title);
		record.set("content",content);
		if (imgurl!=null && imgurl!="") {
			imgurl=QianNiuUpload.upload(imgurl);//图片上传到【七牛云】
			record.set("imgurl",imgurl);
		}
		record.set("orderId", orderId );
		record.set("isValid", isValid);
		result = Db.update("f_theme", record);
		
		renderJson("result", result);
	}
	
	/**
	 * 奖品的详细信息
	 */
	public void themeDetail(){
		Integer id=getParaToInt(0);//奖品唯一编号
		if(id!=null){
			Record rd=BbsDao.QueryThemeInfo(id);
			setAttr("rd",rd);
		}
		render("theme_detail.html");
	}
	
	/**
	 * 精选帖发花籽
	 * @return 
	 */
	public static void Addseeds(Record record,int seedscount) {
		/**
		 * 花籽数手动设置seedscount
		 */
		for(int i = 0; i<seedscount; i++){
			Record seed = new  Record();
			seed.set("aid",record.getInt("aid"));
			seed.set("send", 1);
			seed.set("type", seedType.featuredpost.name());
			seed.set("remarks", seedType.featuredpost.name);
			seed.set("ctime", new Date());
			Db.save("f_flowerseed", seed);
		}
		WeixinApiCtrl.setApiConfig();
		/**
		String message = "您的帖子入选了精选帖，小美奖励您2颗花籽。\n<a href='" + Constant.getHost + "/service/getPrizeList'>【去抽奖吧】</a>";
		CustomServiceApi.sendText(openId, message);
		*/
		String url=Constant.getHost +"/account/flowerseed/";
		ApiResult result = TemplateMsgApi.send(TemplateData.New()
			    // 消息接收者
			    .setTouser(record.getStr("openid"))
			    // 模板id
			    .setTemplate_id(WeixinApiCtrl.gettemplateId("到账通知"))
			    .setTopcolor("#eb414a")
			    .setUrl(url)
			    .add("first", "花籽到账通知", "#333")
			    .add("keyword1", record.getStr("nick"), "#333")
			    .add("keyword2", seedscount+"", "#333")
			    .add("keyword3",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), "#333")
			    .add("remark", "\n亲爱的，恭喜你在美美社区的帖子入选精选贴，赠送"+seedscount+"粒花籽，品味+才华=无敌！", "#888")//备注
			    .build());
				 WeixinApiCtrl.sendTemplateMsg(result.getJson());
	}
   /**
    * 积分列表	
    */
	public void integrallist() {
		Integer state = getParaToInt("state")==null?10:getParaToInt("state");
		Integer pageno = getParaToInt("pageno")==null?1:getParaToInt("pageno");
		String aid = getPara("aid");
		Page<Record> page =BbsDao.getintegrallist(pageno,16,aid,state);
		setAttr("pageno", page.getPageNumber());
  		setAttr("totalpage", page.getTotalPage());
  		setAttr("totalrow", page.getTotalRow());
		setAttr("list", page.getList());
		setAttr("aid", aid);
		setAttr("state", state);
		render("integral_list.html");
	}
	
}
