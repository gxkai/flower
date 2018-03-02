 function get_time()
 {
    var date=new Date();
    var year="",month="",day="",week="",hour="",minute="",second="";
    year=date.getFullYear();
    month=add_zero(date.getMonth()+1);
    day=add_zero(date.getDate());
    week=date.getDay();
    switch (date.getDay()) {
    case 0:val="周日";break
    case 1:val="周一";break
    case 2:val="周二";break
    case 3:val="周三";break
    case 4:val="周四";break
    case 5:val="周五";break
    case 6:val="周六";break
      }
    hour=add_zero(date.getHours());
    minute=add_zero(date.getMinutes());
    second=add_zero(date.getSeconds());
    document.getElementById("timetable").innerHTML=" "+year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second+" "+val;
  }

  function add_zero(temp)
  {
    if(temp<10) return "0"+temp;
    else return temp;
}

$(document).ready(function(){
	$("#temp_1").addClass("Hui-iconfont-vip");
	$("#temp_2").addClass("Hui-iconfont-goods");
	$("#temp_3").addClass("Hui-iconfont-order");
	$("#temp_4").addClass("Hui-iconfont-wuliu");
	$("#temp_5").addClass("Hui-iconfont-dianpu");
	$("#temp_6").addClass("Hui-iconfont-news");
	$("#temp_7").addClass("Hui-iconfont-hongbao2");
	$("#temp_8").addClass("Hui-iconfont-zhuanfa");
	$("#temp_9").addClass("Hui-iconfont-manage");
	$("#temp_1000").addClass("Hui-iconfont-shujutongji");
	$("#temp_1001").addClass("Hui-iconfont-hongbao");
	$("#temp_1002").addClass("Hui-iconfont-list");
	$("#temp_1003").addClass("Hui-iconfont-money");
	$("aside").niceScroll({
		cursorcolor:"#333",
		cursoropacitymin: 0.6,
		cursoropacitymax:0.6,
		demode: false
	});
})
 
				 