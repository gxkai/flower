package com.util;

import com.jfinal.kit.PropKit;

/**
 * @see 系统常量设计
 * @author yeqing
 * @date 2016/08/09
 */
public class Constant {
	
   public static int MondayTime = 1;      //周一送
   public static int SaturdayTime = 2;    //周六送
   
   // 域名
   public static String getHost = PropKit.get("host");
   // 图片保存地址
   public static String imgpath = PropKit.get("imgpath");
   
   // 花籽类型
   public static enum seedType{
	   custom("客服花籽", 1),//花小美从后台发送，花籽数人工输入
	   sign("签到", 1),
	   register("注册", 2),
	   binding("绑定手机号", 2),
	   invite("邀请好友", 2),
	   
	   buy01("付款成功01", 6),
	   buy04("付款成功04",15),
	   buy12("付款成功12",40),
	   buy24("付款成功24",80),
	   
	   comment("订单评价",1),
	   fillInformation("完善信息",2),
	   sign18("第18位签到并分享",18),
	   signluck("幸运签到",8),
	   featuredpost("精选帖",2);
	   
	   
       public String name;
       public int point;

       private seedType(String name, int point) {
           this.name = name;
           this.point = point;
       }
   }
   
   // 订单状态
   public static enum orderState{
	   STATE0("未付款", 0),
	   STATE1("服务中", 1),
	   STATE2("待评价", 2),
	   STATE3("已完成", 3),
	   STATE4("退款", 4),
	   STATE5("交易取消", 5);
	  
       public String name;
       public int state;

       private orderState(String name, int state) {
           this.name = name;
           this.state = state;
       }
   }
   
   	// 订单状态转换
   	public static String zhbyos(int state){
   		String os = new String();
	   	switch(state){
	   		case 0: os = "未付款";break;
	   		case 1: os = "服务中";break;
	   		case 2: os = "待评价";break;
	   		case 3: os = "已完成";break;
	   		case 4: os = "退款";break;
	   		case 5: os = "交易取消";break;
	   	}
	   return os;
   	}
   
   // 订单类型
   public static enum orderType{
	   TYPE1(1, "订阅"),
	   TYPE2(2, "送花"),
	   TYPE3(3, "周边"),
	   TYPE4(4, "兑换"),
	   TYPE5(5, "节日送花"),
	   TYPE6(6, "定制花束"),
	   TYPE7(41, "兑换订阅鲜花"),
	   TYPE8(43, "兑换周边"),
	   TYPE9(9, "多双交替"),
	   TYPE10(10, "主题花"),
	   TYPE11(11, "闪购花"),
	   TYPE12(8, "拼团"),
	   TYPE13(42, "兑换礼品鲜花"),
	   TYPE14(45, "兑换节日鲜花"),
	   TYPE15(410, "兑换主题鲜花"),
	   TYPE16(411, "兑换闪购鲜花");
	   
	   
	   public int type;
       public String desc;

       private orderType(int type, String desc) {
           this.type = type;
           this.desc = desc;
       }
   }
   
   // 配单反馈信息
   public static enum singlews{
	   CODE0(0, "未到配单时间"),
	   CODE1(1, "等待配单的订单数量"),
	   CODE2(2, "处理进度"),
	   CODE3(3, "结束"),
	   CODE4(4, "配单出错，终止操作");
	  
       public int code;
       public String desc;

       private singlews(int code, String desc) {
           this.code = code;
           this.desc = desc;
       }
   }
   
   
   
   // 用户同意授权，获取code
   public static String getCode = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";
   // 通过code换取网页授权access_token
   public static String getAccess_Token = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
   // 拉取用户信息(需scope为 snsapi_userinfo)
   public static String getSnsapi_userinfo = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
   // 节日送花字典
   public static int Hid = 101;

}