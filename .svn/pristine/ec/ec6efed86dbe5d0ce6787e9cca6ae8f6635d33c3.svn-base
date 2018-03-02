package com.controller.front;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.controller.WeixinApiCtrl;
import com.dao.FCDao;
import com.dao.MCDao;
import com.interceptor.FrontInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.CustomServiceApi;
import com.util.DesUtil;
import com.util.SignUtil;
import sun.misc.BASE64Encoder;

@Before(FrontInterceptor.class)
public class FrontIndexCtrl extends Controller {
	
	/**
	 * 美美社区入口
	 * 由于微信网页授权域名只支持一个，并且我们也想将社区单独挂在一个服务器上，降低整个系统的耦合度
	 * 达到模块化的效果。
	 * 因此不得不这样做，通过公众平台的跳板，共享用户基本资料（f_account）
	 *注：目前没有想到更好的方法
	 *此处社区上线遇到了如果几个问题
	 *1：微信网页授权域名只支持一个
	 *2：微信js授权域名支持多个，所以只要用的微信js的，都要事先在公众号配置
	 *3：社区的服务器未加入DB的白名单，后续看看能不能整到内网去
	 */
	public void index_toBBS(){
		Record account = getSessionAttr("account");
		setAttr("id", account.getInt("id"));
		render("index_toBBS.html");
	}
	
	// 首页
	public void index(){

		Record account = getSessionAttr("account");
		//主页轮换图
		setAttr("rotation", FCDao.getRotation());
		//主页产品
		setAttr("goodslist", FCDao.getindexInfo());
		
	/*	//双品
		setAttr("shuang", Db.findFirst("SELECT ptid,type,`name`,imgurl,`describe`,price,state FROM f_flower_pro WHERE ptid = 1"));
		//多品
		setAttr("duo", Db.findFirst("SELECT ptid,type,`name`,imgurl,`describe`,price,state FROM f_flower_pro WHERE ptid = 1"));
		
		*/
		
		int  isfans=FCDao.isFoucs(account.getStr("openid"));
		setAttr("isfans",isfans);//是否已经关注
	
		// 闪购  如果没有获取闪购第一条
		List<Record> shan =  FCDao.getShan();
		if (shan.size() < 0) {
			shan = Db.find("SELECT b.id,b.name,b.imgurl,b.itinfo1,b.itinfo2,b.itinfo3,b.itinfo4,b.itinfo5,b.describe,b.price,a.hDate,a.hReach,a.hState,a.hTitle,b.shareDes,b.shareTitle "
					+ "	FROM f_holiday a  "
					+ " LEFT JOIN f_flower_pro b ON a.hPtid=b.ptid WHERE a.id = 4 order by b.id DESC LIMIT 2");
		}
		
		// 如果图片是多张图 切割一下
		for (Record recordShan : shan) {
			String imgs = recordShan.getStr("imgurl");
			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				recordShan.set("imgurl", ims[0]);
			}else{
			}
		}
		
		setAttr("shanlist", shan);
		
		// 主题花  前端要判断一下是否为空
		List<Record> zhu =  FCDao.getZhu();
		//切割一下imgurl
		if (zhu.size()>0) {
			for (Record recordShan : shan) {
				String imgs = recordShan.getStr("imgurl");
				if(imgs.indexOf("-")!= -1){
					String[] ims = imgs.split("-");
					recordShan.set("imgurl", ims[0]);
				}else{
				}
			}
		}
		setAttr("zhulist", zhu);
		
		// 花边好物  默认没有全部下架的情况
		setAttr("huabianlist", FCDao.getHuabian());
		
		render("index.html");
	}
	// 花边好物列表
	public void around(){
		Record account = getSessionAttr("account");
		int isbuy = account.getInt("isbuy");
		setAttr("aroundlist", FCDao.getAround(isbuy));
		render("around.html");
	}
	// 花边好物详情
	public void aroundinfo(){
		
		int isred = 0;
		setAttr("isred", isred);
		
		int pid = getParaToInt();
		Record rd=FCDao.getAroundInfo(pid);
		setAttr("flower", rd);
		int isAllowBuy=1;//是否允许购买，针对拉新商品，如果满足10人，可以购买
		
		setAttr("isAllowBuy",isAllowBuy);
		Record account = getSessionAttr("account");
		setAttr("aid",account.getInt("id"));
		setAttr("ptid",rd.getInt("ptid"));
		render("product_around.html");
	}
	// 商品详情
	public void product(){
		// pid f_flower_pro   ptid 
		int pid = getParaToInt(0);
		int ptid = getParaToInt(1)==null?pid:getParaToInt(1);
		int isred = getParaToInt(2)==null?0:getParaToInt(2);
		
		//如果是苹果 则 显示5斤 8斤页面
		if(pid == 133){
			ptid = 9;
		}
		
		setAttr("isred", isred);
		
		Record account = getSessionAttr("account");
		int isbuy = Db.findFirst("select isbuy from f_account where id=?",account.getInt("id")).getInt("isbuy");
		setAttr("isbuy", isbuy);	// 判断是否第一次购物
		//适合对象0：所有用户1：首单用户 2：多单用户 3：无单用户
		setAttr("flower_pro_type",Db.findFirst("select type from f_flower_pro where ptid=? and state=0",ptid).get("type"));
		setAttr("activitylist", JsonKit.toJson(FCDao.getActivityList()));	//有效时间内的活动
		
		
		
		//双品 ，多品 ,玫瑰套餐
		if(ptid==1 || ptid==2 || ptid==11){                 
			Record pro = FCDao.getProductInfo(pid);
			String imgs = pro.getStr("imgurl");
			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				pro.set("imgurl1", ims[0]);
				pro.set("imgurl2", ims[1]);
				pro.set("imgurl3", ims[2]);
			}else{
				pro.set("imgurl1", imgs);
				pro.set("imgurl2", imgs);
				pro.set("imgurl3", imgs);
			}
			setAttr("flower", pro);	// 双品与多品
			setAttr("vaselist", FCDao.getProducts(4, isbuy));		// 花瓶
			setAttr("addlist", FCDao.getProductsList(301));		// 加购商品列表
		
			//System.err.println(pro);
			
			setAttr("dmlj", pro.get("dmlj"));				// 多加一次，价格立减(双品)
			//setAttr("dmlj2", pro.get("dmlj"));				// 多加一次，价格立减(多品)
			setAttr("ptid",ptid);
			render("product.html");
		// 送 花 
		}else if(ptid == 3){    
			setAttr("givelist", FCDao.getProducts(3, isbuy));		// 送花系列
			setAttr("vaselist", FCDao.getProducts(4, isbuy));		// 花瓶
			setAttr("idoneitylist", Db.find("select id,title,imgurl from f_idoneity where state = 0"));	// 适赠对象
			render("product_give.html");
		// 定制鲜花
		}else if (ptid == 201) {   
		
			List<Record> flowerList = FCDao.getFlowers(pid);
			
			setAttr("flowerList", flowerList);
			
			setAttr("flower", flowerList.get(0));
			setAttr("vaselist", FCDao.getProducts(4, isbuy));		// 花瓶
			setAttr("addlist", FCDao.getProductsList(301));		// 加购商品列表
			
			//setAttr("dmlj", FCDao.getDmlj3());				// 定制鲜花 多买立减
			setAttr("dmlj",flowerList.get(0).get("dmlj"));
			render("product_custom.html");
		}
		//单双交替
		else if(ptid==203){
			Record pro = FCDao.getProductInfo(pid);
			String imgs = pro.getStr("imgurl");
			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				pro.set("imgurl1", ims[0]);
				pro.set("imgurl2", ims[1]);
				pro.set("imgurl3", ims[2]);
			}else{
				pro.set("imgurl1", imgs);
				pro.set("imgurl2", imgs);
				pro.set("imgurl3", imgs);
			}
			setAttr("flower", pro);	// 双品与多品
			setAttr("vaselist", FCDao.getProducts(4, isbuy));		// 花瓶
			setAttr("addlist", FCDao.getProductsList(301));		// 加购商品列表
			//setAttr("dmlj5", FCDao.getDmlj5());				// 多加一次，价格立减(多品)
			setAttr("dmlj5", pro.get("dmlj"));
			render("product_more.html");
		}
		
		//神奇的苹果
		else if (ptid == 9 && isred == 0) {
			List<Record> pro = Db.find("SELECT id,ptid,`name`,imgurl,itinfo1,itinfo2,itinfo3,itinfo4,itinfo5,"
					+ "	`describe`,price,ctime,state,shareDes,shareTitle,dmlj,reachtype"
					+ "	FROM f_flower_pro WHERE ptid = ? and type = 0 and state= 0",ptid);
			
			if(pro.size()>0){
				
			for (Record record : pro) {
				String imgs = record.getStr("imgurl");
				if(imgs.indexOf("-")!= -1){
					String[] ims = imgs.split("-");
					record.set("imgurl1", ims[0]);
					record.set("imgurl2", ims[1]);
					record.set("imgurl3", ims[2]);
				}else{
					record.set("imgurl1", imgs);
					record.set("imgurl2", imgs);
					record.set("imgurl3", imgs);
				}
			}
			
			  // 主题id
			  setAttr("festivalId", 5);
			  setAttr("isbuy", isbuy);	// 判断是否第一次购物

			  int  isfans=FCDao.isFoucs(account.getStr("openid"));
			  setAttr("isfans",isfans);//是否已经关注

			  // 在售
			  setAttr("pState",1);
			  setAttr("holilist",pro);	
			  //主题
			  setAttr("festivalTitle","高原野生苹果");	
			  //type=3 是周边
			  setAttr("type",3);	
			  render("product_festival.html");

			}else {
				pro = Db.find("SELECT id,ptid,`name`,imgurl,itinfo1,itinfo2,itinfo3,itinfo4,itinfo5,"
						+ "	`describe`,price,ctime,state,shareDes,shareTitle,dmlj,reachtype"
						+ "	FROM f_flower_pro WHERE ptid = ? and type = 0 ",ptid);
				
				for (Record record : pro) {
					String imgs = record.getStr("imgurl");
					if(imgs.indexOf("-")!= -1){
						String[] ims = imgs.split("-");
						record.set("imgurl1", ims[0]);
						record.set("imgurl2", ims[1]);
						record.set("imgurl3", ims[2]);
					}else{
						record.set("imgurl1", imgs);
						record.set("imgurl2", imgs);
						record.set("imgurl3", imgs);
					}
				}
				
				for (Record record : pro) {
					String imgs = record.getStr("imgurl");
					if(imgs.indexOf("-")!= -1){
						String[] ims = imgs.split("-");
						record.set("imgurl1", ims[0]);
						record.set("imgurl2", ims[1]);
						record.set("imgurl3", ims[2]);
					}else{
						record.set("imgurl1", imgs);
						record.set("imgurl2", imgs);
						record.set("imgurl3", imgs);
					}
				}
				
				// 主题id
				setAttr("festivalId", 5);
				setAttr("isbuy", isbuy);	// 判断是否第一次购物
				
				int  isfans=FCDao.isFoucs(account.getStr("openid"));
				setAttr("isfans",isfans);//是否已经关注

				// 在售
				setAttr("pState",0);
				setAttr("holilist",pro);	
				//主题
				setAttr("festivalTitle","高原野生苹果");	
				//type=3 是周边
				setAttr("type",3);	
				render("product_festival.html");
	
			}
			
		}
		
		// 花边好物(ptid>=4 && ptid<=50 );
		else if(ptid>=4 && ptid<=50 && ptid!=11){
			setAttr("flower", FCDao.getAroundInfo(pid));
			int isAllowBuy=1;//是否允许购买，针对拉新商品，如果满足10人，可以购买
			if(ptid==10){
				//ptid==10为拉新商品
				String tjid="6_"+account.getInt("id");
				Long count=Db.queryLong("select count(1) from f_account where tjid=?  and id not in(select aid from f_redpackets_log)",tjid);
			    if(count<10){
			    	isAllowBuy=0;//不满10人，不允许购买
			    }
			}
			setAttr("isAllowBuy",isAllowBuy);
			setAttr("aid",account.getInt("id"));
			setAttr("ptid",ptid);
			render("product_around.html");
		}
	}
	
	/**
	 * 闪购&主题
	 * @author Glacier
	 * @date 2017年9月19日
	 */
	public void szProduct() {
		
		// 0：在售  1：售罄，不显示  2：售罄，显示
		Record shan =  Db.findFirst("SELECT a.`name`,a.imgurl,a.`describe`,a.price,a.state,b.state 'hState' from f_flower_pro a LEFT JOIN  f_dictionary  b on b.code_value = a.ptid where ptid = '104' ORDER BY a.state ASC limit 1");
		Record zhu =  Db.findFirst("SELECT a.`name`,a.imgurl,a.`describe`,a.price,a.state,b.state 'hState' from f_flower_pro a LEFT JOIN  f_dictionary  b on b.code_value = a.ptid where ptid = '105' ORDER BY a.state ASC limit 1");
		
		if (shan.getInt("state") != 0 || shan == null) {
			shan =  Db.findFirst("SELECT a.`name`,a.imgurl,a.`describe`,a.price,a.state,b.state 'hState' from f_flower_pro a LEFT JOIN  f_dictionary  b on b.code_value= a.ptid where ptid = '104' ORDER BY a.id desc limit 1");
		}
		
		if (zhu.getInt("state") != 0 || zhu == null) {
			zhu =  Db.findFirst("SELECT a.`name`,a.imgurl,a.`describe`,a.price,a.state,b.state 'hState' from f_flower_pro a LEFT JOIN  f_dictionary  b on b.code_value = a.ptid where ptid = '105' ORDER BY a.id desc limit 1");
		}
		
		
		String imgs = shan.getStr("imgurl");
		if(imgs.indexOf("-")!= -1){
			String[] ims = imgs.split("-");
			shan.set("imgurl1", ims[0]);
			shan.set("imgurl2", ims[1]);
			shan.set("imgurl3", ims[2]);
		}else{
			shan.set("imgurl1", imgs);
			shan.set("imgurl2", imgs);
			shan.set("imgurl3", imgs);
		}
		
		imgs = zhu.getStr("imgurl");
		if(imgs.indexOf("-")!= -1){
			String[] ims = imgs.split("-");
			zhu.set("imgurl1", ims[0]);
			zhu.set("imgurl2", ims[1]);
			zhu.set("imgurl3", ims[2]);
		}else{
			zhu.set("imgurl1", imgs);
			zhu.set("imgurl2", imgs);
			zhu.set("imgurl3", imgs);
		}
		
		/*System.err.println(shan);
		System.out.println(zhu);*/
		
		setAttr("shan", shan);
		setAttr("zhu", zhu);
		render("product_shanAzhu.html");
		
	}
	
	/**
	 * 订阅整合
	 * @author Glacier
	 * @date 2017年11月16日
	 */
	public void all_dy() {
		
		//倾心 -多品
		Record qingxin = Db.findById("f_flower_pro", 2);
			String imgs = qingxin.getStr("imgurl");
			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				qingxin.set("imgurl1", ims[0]);
				qingxin.set("imgurl2", ims[1]);
				qingxin.set("imgurl3", ims[2]);
			}else{
				qingxin.set("imgurl1", imgs);
				qingxin.set("imgurl2", imgs);
				qingxin.set("imgurl3", imgs);
			}
		
		//遇见 -双品
		Record yujian = Db.findById("f_flower_pro", 1);
			imgs = yujian.getStr("imgurl");
			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				yujian.set("imgurl1", ims[0]);
				yujian.set("imgurl2", ims[1]);
				yujian.set("imgurl3", ims[2]);
			}else{
				yujian.set("imgurl1", imgs);
				yujian.set("imgurl2", imgs);
				yujian.set("imgurl3", imgs);
			}
		
		//小别
		Record xiaobie = Db.findById("f_flower_pro", 104);
			imgs = xiaobie.getStr("imgurl");
			if(imgs.indexOf("-")!= -1){
				String[] ims = imgs.split("-");
				xiaobie.set("imgurl1", ims[0]);
				xiaobie.set("imgurl2", ims[1]);
				xiaobie.set("imgurl3", ims[2]);
			}else{
				xiaobie.set("imgurl1", imgs);
				xiaobie.set("imgurl2", imgs);
				xiaobie.set("imgurl3", imgs);
			}
		
		//定制
		/*Record dingzhi = Db.findFirst("SELECT id,ptid,`name`,imgurl,describe2 FROM f_flower_pro WHERE ptid = 201 and state = 0 ORDER BY x,y ASC LIMIT 1");
		*/
		setAttr("qingxin", qingxin);
		setAttr("yujian", yujian);
		setAttr("xiaobie", xiaobie);
		/*setAttr("dingzhi", dingzhi);*/
		
		render("product_dy.html");
	}
	
	
	
	/**
	 * （闪购，主题）
	 * 多个商品显示在一个页面
	 * 从之前的【节日页面】演变而来
	 */
	public void festivalProduct(){
		int fesPid = getParaToInt(0);//数据字典里的code_value，f_flower_pro中的ptid
		int isred = getParaToInt(1)==null?0:getParaToInt(1);//是否从红包页面跳转过来
        int id=getParaToInt(2)==null?0:getParaToInt(2);//f_flower_pro中的id
		setAttr("isred", isred);//是否是从红包页面跳转过来的？
		
		setAttr("festivalId", fesPid);
		
		Record account = getSessionAttr("account");
		int isbuy = Db.findFirst("select isbuy from f_account where id=?",account.getInt("id")).getInt("isbuy");
		setAttr("isbuy", isbuy);	// 判断是否第一次购物
		//适合对象0：所有用户1：首单用户 2：多单用户 3：无单用户
		if(id==0){
			setAttr("flower_pro_type",Db.findFirst("select type from f_flower_pro where ptid=? and state=0 order by id ",fesPid).getInt("type"));
		}else{
			Record rd=Db.findFirst("select type,state from f_flower_pro where id=? ",id);
			setAttr("flower_pro_type",rd.getInt("type"));
			setAttr("pState",rd.getInt("state"));
		}
		
		// 花瓶列表
		setAttr("vaselist", FCDao.getProducts(4, isbuy));
					
		
		List<Record> productList = FCDao.getFestivalProducts(fesPid);
		if(id!=0){
			productList = FCDao.getFestivalProducts3(id);
		}
		if(isred==1){
			productList=FCDao.getFestivalProducts2(fesPid);
		}
		
		if(productList.size()>0){
		
				String festivalTitle = productList.get(0).get("code_name");
				int pState =productList.get(0).getInt("hState");
				//现场活动的商品做个限制
				if(fesPid==6){
					Long totality=Db.queryLong("select count(1) 'totality' from f_order where type=7 and state=3");
					setAttr("totality",totality);//目前已有xxx位爱心人士购买了爱心玫瑰。
					
					
					Long count=	Db.queryLong("select count(1) from f_order where aid=? and type=7 and state in(0,1,2,3)",account.getInt("id"));
					if(count>=1) {
						pState=0;
						WeixinApiCtrl.setApiConfig();
						String message ="公益活动的花束，每个用户限购1次。您已经下了1笔订单，若未支付，请到【我的订单】中完成付款。";
						CustomServiceApi.sendText(account.getStr("openid"), message);
						}
					}
				
				/*System.err.println(productList);*/
				
				int  isfans=FCDao.isFoucs(account.getStr("openid"));
				setAttr("isfans",isfans);//是否已经关注
				
				setAttr("pState",pState);
				setAttr("holilist",productList);	
				setAttr("festivalTitle",festivalTitle );	
				
				int type=5;//节日
				if(fesPid==106){
					type=7;//现场活动
				}else if(fesPid==104) {
					type=11;//闪购花
				}else if(fesPid==105) {
					type=10;//主题花
				}else if(fesPid>=4&&fesPid<=50){
					type=3;//周边
				}
				setAttr("type",type);	
				render("product_festival.html");
				
		}else {
			//System.out.println("list 没有参数");
			
			List<Record> productList1 = Db.find("SELECT b.id,b.name,b.imgurl,b.itinfo1,b.itinfo2,b.itinfo3,b.itinfo4,b.itinfo5,b.describe,b.price,a.hDate,a.hReach,a.hState,a.hTitle,b.shareDes,b.shareTitle "
					+ " FROM f_holiday a "
					+ " LEFT JOIN f_flower_pro b ON a.hPtid=b.ptid "
					+ " WHERE a.id = 4 order by b.id DESC LIMIT 1");
			
			String festivalTitle = productList1.get(0).get("hTitle");
			int pState = productList1.get(0).get("hState");
			
			//现场活动的商品做个限制
			if(fesPid==6){
				Long totality=Db.queryLong("select count(1) 'totality' from f_order where type=7 and state=3");
				setAttr("totality",totality);//目前已有xxx位爱心人士购买了爱心玫瑰。
				
				
				Long count=	Db.queryLong("select count(1) from f_order where aid=? and type=7 and state in(0,1,2,3)",account.getInt("id"));
				if(count>=1) {
					pState=0;
					WeixinApiCtrl.setApiConfig();
					String message ="公益活动的花束，每个用户限购1次。您已经下了1笔订单，若未支付，请到【我的订单】中完成付款。";
					CustomServiceApi.sendText(account.getStr("openid"), message);
					}
				}
			
			/*System.err.println(productList);*/
			
			int  isfans=FCDao.isFoucs(account.getStr("openid"));
			setAttr("isfans",isfans);//是否已经关注
			
			setAttr("pState",0);
			setAttr("holilist",productList1);	
			setAttr("festivalTitle",festivalTitle );	
			
			
			
			int type=5;//节日
			if(fesPid==106){
				type=7;//现场活动
			}else if(fesPid==104) {
				type=11;//闪购花
			}else if(fesPid==105) {
				type=10;//主题花
			}else if(fesPid>=4&&fesPid<=50){
				type=3;//周边
			}
			setAttr("type",type);	
			render("product_festival.html");
			
		}	
				
	
	}
	
	
	// 定制鲜花  详情图片
	public void getIntroImg() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String itinfo4 = Db.queryStr("SELECT itinfo4 FROM f_flower_pro WHERE id = ?",getParaToInt("proId"));
		boolean R = false;
		if (itinfo4 != null) {
			R = true;
		}
		resultMap.put("result", R);
		resultMap.put("itinfo4", itinfo4);
		renderJson(resultMap);
	}
	// 生活美学
	public void esthetics(){
		Integer pageno = getParaToInt()==null?1:getParaToInt();
		Page<Record> page = FCDao.getEstheticsPage(pageno, 16);
		if(pageno == 1){
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("estheticslist", page.getList());
			render("esthetics.html");
		}else{
			renderJson(page.getList());
		}
	}
	// 生活美学详情
	public void esthetics_detail(){
		int id = getParaToInt();
		setAttr("esthetics", Db.findById("f_esthetics", id));
		render("esthetics_detail.html");
	}
	// 投稿须知
	public void esthetics_notice(){
		render("esthetics_notice.html");
	}
	// 常见问题
	public void question(){
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		Page<Record> page = MCDao.getQuestion(pageno, 16);
		if(pageno == 1){
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("questionlist", page.getList());
			render("question.html");
		}else{
			renderJson(page.getList());
		}
	}
	/**
	 * 养花须知 1：养护须知2：每周搭配
	 */
	public void knowledge(){
		Integer pageno = getParaToInt(0)==null?1:getParaToInt(0);
		Integer type = getParaToInt(1)==null?2:getParaToInt(1);
		setAttr("type", type);
		Page<Record> page = null;
		if(type == 1){
			page = MCDao.getKnowledge(pageno, 28, type, null);
		}else{
			page = MCDao.getKnowledge(pageno, 16, type, null);
		}
		if(pageno == 1){
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("viewlist",page.getList());
			render("knowledge.html");
		}else{
			renderJson(page.getList());
		}
	}
	// 养花须知详情
	public void knowledge_detail(){
		int id = getParaToInt();
		setAttr("knowledge", Db.findById("f_knowledge", id));
		render("knowledge_detail.html");
	}
	// 物流查询
	public void logistics_query(){
		Integer pageno = getParaToInt(0) == null ? 1 : getParaToInt(0);
		// 订单状态:9全部[0未付款，1服务中，2待评价，3已完成，4退款，5交易取消]
		Record account = getSessionAttr("account");
		Page<Record> page = FCDao.getMylogistics(pageno, 16, account.getInt("id"));
		if (pageno == 1) {
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("orderlist", page.getList());
			render("logistics_my.html");
		} else {
			renderJson(page.getList());
		}
	}
	// 领花引导页
	public void receive_order(){
		setAttr("ordercode", getPara());
		setAttr("account", getSessionAttr("account"));
		setAttr("areajson", FCDao.getArea());
		render("receive.html");
	}
	// 领花提取码验证
	public void receive_code_valid(){
		String ordercode = getPara("ordercode");
		String code = getPara("code");
		renderJson(FCDao.validCode(ordercode, code));
	}
	// 领花
	public void getorderreceive(){
		String ordercode = getPara("ordercode");
		String code = getPara("code");
		String name = getPara("name");
		String tel = getPara("tel");
		String area = getPara("area");
		String addr = getPara("addr");
		Record account = getSessionAttr("account");
		renderJson(FCDao.getOrderReceive(account.getInt("id"), ordercode, code, name, tel, area, addr));
	}
	// 地推人员推广页面
	@Clear
	public void spread() throws Exception{
		String idStr = getPara(0);
		Integer pageno = getParaToInt(1)==null?1:getParaToInt(1);
		Integer type = getParaToInt(2)==null?1:getParaToInt(2);
		Page<Record> page = null;
		setAttr("type", type);
		setAttr("scancode", idStr);
		setAttr("dimem", 2);   //区分 地推人员二维码 和 会员二维码
		Record spread = Db.findById("f_spread", Integer.parseInt(new DesUtil().decrypt(idStr)));
		if(type == 1){
			setAttr("qrurl", spread.getStr("qrurl"));
			// 绑定 apiConfig 到线程之上
			WeixinApiCtrl.setApiConfig();
			
			String msg = spread.getStr("name") + "：" + spread.getStr("qrurl");		
			CustomServiceApi.sendText("oTrkFwHlNrz32KmW2g9nBhBlCDmg", msg); // url  发送给  倪工
//			CustomServiceApi.sendText("oFEYtuH1vyMheg4Z2pVOWbCGjqfM", msg); //测试
			
		}else{
			int id = spread.getInt("id"); 
			page = FCDao.findOrderMember(pageno, 16, id, 2);
			setAttr("pageno", page.getPageNumber());
			setAttr("totalpage", page.getTotalPage());
			setAttr("totalrow", page.getTotalRow());
			setAttr("ormems",page.getList());
		}
		setAttr("yqhy", FCDao.getInviteFriend());
		if(pageno == 1){
			render("account/invitefriend.html");
		}else{
			renderJson(page.getList());
		}
	}
	
	// 分享二维码
	@Clear
	public void shareqcode() throws NumberFormatException, Exception{
		int dimem = getParaToInt(0);
		String scancode = getPara(1);
		int id = Integer.parseInt(new DesUtil().decrypt(scancode));
		String qrurl = new String();
		if(dimem == 1){
			Record record = Db.findFirst("select headimg,qrurl from f_account where id = ?", id);
			qrurl = record.getStr("qrurl");
			setAttr("headimg", base64Encode(record.getStr("headimg")));
		}else{
			qrurl = Db.queryStr("select qrurl from f_spread where id = ?", id);
		}
		setAttr("yqhy", FCDao.getInviteFriend());
		setAttr("qrurl", qrurl);
		render("shareqcode.html");
	}
	
	public static String base64Encode(String imgurl) throws IOException {
		URL url = new URL(imgurl);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(5 * 1000);
		InputStream is = con.getInputStream();
        
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = is.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] data = swapStream.toByteArray();
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
	}
	
	// 禁用账号进入页面
	@Clear
	public void freeze(){
		render("freeze.html");
	}
	@Clear
	public void focuson(){
		render("focuson.html");
	}
	
	// 微信握手验证
	@Clear
	public void CoreServlet(){
		// 微信加密签名
		String signature = getPara("signature");
		// 时间戳
		String timestamp = getPara("timestamp");
		// 随机数
		String nonce = getPara("nonce");
		// 随机字符串
		String echostr = getPara("echostr");

		// 通过检验signature 对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
		if (SignUtil.checkSignature(signature, timestamp, nonce)) {
			renderText(echostr);
		}else{
			renderNull();
		}
	}
	
	// js域名验证
	@Clear
	public void mp_verify(){
		renderText("TfvqjMOIvPJVUR0D");
	}
	
	@Clear
	public void dbtest(){
		List<Record> list = Db.find("select id,jihui,jh_list from f_order where jh_list is not null");
		for(Record record : list){
			Record jh = Db.findFirst("SELECT GROUP_CONCAT(DISTINCT a.tid) AS id,GROUP_CONCAT(DISTINCT b.name) AS name FROM f_flower a LEFT JOIN f_flower_type b ON a.tid=b.id WHERE a.id IN ("+record.getStr("jh_list")+")");
			record.set("jihui", jh.getStr("name"));
			record.set("jh_list", jh.getStr("id"));
			//Db.update("f_order", record);
		}
		renderJson(list);
	}
	
	@Clear
	public void canvas(){
		render("canvas.html");
	}
	
	//联系我们
	public void contactus(){
		render("contactus.html");
	}
	
	
	
	
	/***
	 * 活动页面
	 * @author Glacier
	 * @date 2017年8月3日
	 */
	public void activity() {
		int fesPid = getParaToInt(0);
		setAttr("festivalId", fesPid);
		
		Record account = getSessionAttr("account");
		int isbuy = account.getInt("isbuy");
		setAttr("isbuy", isbuy);	// 判断是否第一次购物
	
		List<Record> productList = FCDao.getFestivalProducts(fesPid);
		String festivalTitle = productList.get(0).get("hTitle");
		int pState = productList.get(0).get("hState");
		setAttr("id", productList.get(0).get("id"));
		setAttr("pState",pState);
		setAttr("holilist",productList);	
		setAttr("festivalTitle",festivalTitle );	
		render("product_festival.html");
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}