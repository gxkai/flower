/**js日期函数工具类
 * Created by yetangtang on 2016/9/12.
 */


/**
 * @desc 日期格式化函数
 * @param fmt
 * @returns {*}
 * @constructor
 * 对Date的扩展，将 Date 转化为指定格式的String
 *月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
 *年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
 */
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    //返回值
    return fmt;
}

//增加天
function AddDays(date, value) {
    date.setDate(date.getDate() + value);
    return date;
}

//比较日期大小函数
function CompareDate(dateTime1,dateTime2)
{
    //将所有的短横线替换为斜杠
    return ((new Date(dateTime1.replace(/-/g,"\/"))) >= (new Date(dateTime2.replace(/-/g,"\/"))));
}

function getFristTakeDate(choose) {
    //定义系统日期
    var the_date = (new Date()).Format("yyyy-MM-dd HH:mm:ss");
    var the_date2 = (new Date()).Format("yyyy-MM-dd HH:mm:ss");
    //var week = "周" + "日一二三四五六".split("")[new Date().getDay()];
    var week = "0123456".split("")[new Date().getDay()];
    //初始化week,1-7为正常week
    week = week==0?week+7:week;
    //定义系统当前时间变量
    var day = AddDays(new Date(),3-week).Format("yyyy-MM-dd");
    //定义默认时间
    var day2 = day + " 18:00:00";
    //d定义首次送达时间
    var  fristTime = new Date();

    //判断当前周几，做日期运算
    if(choose==1){
    	day = AddDays(new Date(),5-week).Format("yyyy-MM-dd");
    	day2 = day + " 18:00:00";
    	if (CompareDate(the_date,day2)){
    		fristTime = AddDays(new Date(),(14-week)+1).Format("MM月dd日");
    	}else{
    		fristTime = AddDays(new Date(),(7-week)+1).Format("MM月dd日");
    	}
    }else{
        day = AddDays(new Date(),3-week).Format("yyyy-MM-dd");
        day2 = day + " 18:00:00";
        if (CompareDate(the_date,day2)){
            fristTime = AddDays(new Date(),(12-week)+1).Format("MM月dd日");
        }else{
            fristTime = AddDays(new Date(),(5-week)+1).Format("MM月dd日");
        }
    }
    return fristTime;
}
