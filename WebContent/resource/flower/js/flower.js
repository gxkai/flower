// 花瓶样式选择初始化
function vaseinit(classname, selectclass){
	$('.'+classname).click(function(){
		$('.'+classname).find('.'+selectclass).detach();
		$(this).append('<img src="/resource/flower/image/008.png" class="'+selectclass+'"/>');
		customcycle(0);
	});
}
// 商品详情展示与隐藏
function upordown(classname){
	$('.'+classname).click(function(){
		var $tc = $(this);
		var state = $tc.data('state');
		if(state == 0){
			$tc.data('state', 1);
			$tc.parent().siblings('.pro_f_2').stop().fadeOut(100, function(){
				$tc.find('img').get(0).src='/resource/flower/image/up.png';
			});
		}else{
			$tc.data('state', 0);
			$tc.parent().siblings('.pro_f_2').stop().fadeIn(100, function(){
				$tc.find('img').get(0).src='/resource/flower/image/down.png';
			});
		}
	});
}
// 选择订阅次数
function selectNum(idname){
	var area = new LArea();
    area.init({
        'trigger': '#'+idname,
        'keys': {
            id: 'value',
            name: 'text'
        },
        'data': [data]
    });
}
// 花票展示与隐藏
function cashshoworhide(_obj, classname){
	var $tc = $('.'+classname);
	var state = $tc.data('state');
	if(state == 0){
		$tc.data('state', 1);
		$tc.stop().fadeOut(100, function(){
			$(_obj).find('img').get(0).src='/resource/flower/image/icon/down.png';
		});
	}else{
		$tc.data('state', 0);
		$tc.stop().fadeIn(100, function(){
			$(_obj).find('img').get(0).src='/resource/flower/image/icon/up.png';
		});
	}
}
// 选择所在地区
function selectArea(trigger, valueTo, areadata, areac){
	var provs_data=[],citys_data=[],dists_data=[];
	for(var i=0;i<areadata.length;i++){
		if(areadata[i]['pid']==0){
			// 省级
			var prov = {'text':areadata[i]['name'], 'value':areadata[i]['id'].toString()};
			provs_data.push(prov);
		}else{
			if(areadata[i]['ppid']==0){
				// 市级
				var citys = new Array();
				var city = {'text':areadata[i]['name'], 'value':areadata[i]['id'].toString()};
				if(typeof citys_data[areadata[i]['pid'].toString()] === "undefined"){
					citys.push(city);
					citys_data[areadata[i]['pid'].toString()] = citys;
				}else{
					citys = citys_data[areadata[i]['pid'].toString()];
					citys.push(city);
				}
			}else{
				// 区级
				var dists = new Array();
				var dist = {'text':areadata[i]['name'], 'value':areadata[i]['id'].toString()};
				if(typeof dists_data[areadata[i]['pid'].toString()] === "undefined"){
					dists.push(dist);
					dists_data[areadata[i]['pid'].toString()] = dists;
				}else{
					dists = dists_data[areadata[i]['pid'].toString()];
					dists.push(dist);
				}
			}
		}
	}
	var area = new LArea();
    area.init({
        'trigger': '#' + trigger,
        'valueTo': '#' + valueTo,
        'keys': {
            id: 'value',
            name: 'text'
        },
        'data': [provs_data, citys_data, dists_data]
    });
    if(typeof areac === "undefined"){
    	
    }else{
    	var areavalue = new Array();
    	var addrtext;
    	for(var i in provs_data){
    		if(provs_data[i]['value'] == areac[0]){
    			areavalue.push(i);
    			addrtext = provs_data[i]['text'];
    			break;
    		}
    	}
    	for(var i in citys_data[areac[0]]){
    		if(citys_data[areac[0]][i]['value'] == areac[1]){
    			areavalue.push(i);
    			addrtext += ','+citys_data[areac[0]][i]['text'];
    			break;
    		}
    	}
    	for(var i in dists_data[areac[1]]){
    		if(dists_data[areac[1]][i]['value'] == areac[2]){
    			areavalue.push(i);
    			addrtext += ','+dists_data[areac[1]][i]['text'];
    			break;
    		}
    	}
    	$('#address').val(addrtext);
    	area.value = areavalue;
    }
}

// 打开我的物流
function open_query_mylogistics(workCode){
	$('.container').addClass('body_hide');
	$.post('/service/getlogistics', {'workcode':workCode}, function(data){
		if(data.orderinfo){
			if(data.result){
				if(data.orderinfo.ecode=="d2d"){
						if(data.logistics.length){
							$('.query_logistics').empty();
							$('.query_logistics').append('<div class="ls_body">'+
									'<div class="body_1">'+
									'<span><i1>物流编号:</i1><i2 class="workcode">'+workCode+'</i2></span>'+
									'<span onclick="close_query_logistics()"><img src="/resource/flower/image/icon/close.png" width="100%"></span>'+
									'</div><div class="body_2"></div></div>');
							$.each(data.logistics, function(index,item){
								if (index == 0){
									$('.body_2').append(
											'<div class="ls_state ls_state_top">'+
											'<span class="ls_round"></span>'+
											'<span class="ls_state_1">'+item.OpDateTime+'</span>'+
											'<p class="ls_state_2"><span></span><span>'+item.OpDescription+'</span></p>'+
											'</div>'
									);
								}else{
									$('.body_2').append(
											'<div class="ls_state">'+
											'<span class="ls_round"></span>'+
											'<span class="ls_state_1">'+item.OpDateTime+'</span>'+
											'<p class="ls_state_2"><span></span><span>'+item.OpDescription+'</span></p>'+
											'</div>'
									);
								}
							});
							$('.query_logistics').show();
						}else{
							$('.query_logistics').empty();
							$('.query_logistics').append('<div class="ls_body">'+
									'<div class="body_1">'+
									'<span><i1>物流编号:</i1><i2 class="workcode">'+data.orderinfo.lognumber+'</i2></span>'+
									'<span onclick="close_query_logistics()"><img src="/resource/flower/image/icon/close.png" width="100%"></span>'+
									'</div><div class="body_2"><span style="width: 100%;text-align: center;padding-top: 45px;padding-left:80px">您当前没有物流信息</span></div></div>');
							$('.query_logistics').show();
						}
				}else if(data.orderinfo.ecode=="jd"){
						  if (data.JDlogistics.length) {
							$('.query_logistics').empty();
							$('.query_logistics').append('<div class="ls_body">'+
									'<div class="body_1">'+
									'<span><i1>物流编号:</i1><i2 class="workcode">'+data.orderinfo.lognumber+'</i2></span>'+
									'<span onclick="close_query_logistics()"><img src="/resource/flower/image/icon/close.png" width="100%"></span>'+
									'</div><div class="body_2"></div></div>');
							$.each(data.JDlogistics, function(index,item){
								if (index == 0){
									$('.body_2').append(
											'<div class="ls_state ls_state_top">'+
											'<span class="ls_round"></span>'+
											'<span class="ls_state_1">'+item.opeTime+'</span>'+
											'<p class="ls_state_2"><span></span><span>'+item.opeRemark+'</span></p>'+
											'</div>'
									);
								}else{
									$('.body_2').append(
											'<div class="ls_state">'+
											'<span class="ls_round"></span>'+
											'<span class="ls_state_1">'+item.opeTime+'</span>'+
											'<p class="ls_state_2"><span></span><span>'+item.opeRemark+'</span></p>'+
											'</div>'
									);
								}
							});
							$('.query_logistics').show();
						}else{
							$('.query_logistics').empty();
							$('.query_logistics').append('<div class="ls_body">'+
									'<div class="body_1">'+
									'<span><i1>物流编号:</i1><i2 class="workcode">'+data.orderinfo.lognumber+'</i2></span>'+
									'<span onclick="close_query_logistics()"><img src="/resource/flower/image/icon/close.png" width="100%"></span>'+
									'</div><div class="body_2"><span style="width: 100%;text-align: center;padding-top: 45px;padding-left:80px">您当前没有物流信息</span></div></div>');
							$('.query_logistics').show();
						}
				}else{
						$('.query_logistics').empty();
						$('.query_logistics').append('<div class="ls_body">'+
								'<div class="body_1">'+
								'<span><i1>物流编号:</i1><i2 class="workcode">'+data.orderinfo.lognumber+'</i2></span>'+
								'<span onclick="close_query_logistics()"><img src="/resource/flower/image/icon/close.png" width="100%"></span>'+
								'</div><div class="body_2"><span style="width: 100%;text-align: center;padding-top: 45px;padding-left:80px">您当前没有物流信息</span></div></div>');
						$('.query_logistics').show();
					 }
				}else {
					$('.query_logistics').empty();
					$('.query_logistics').append('<div class="ls_body">'+
							'<div class="body_1">'+
							'<span><i1>'+data.orderinfo.ename+'编号:</i1><input style = "height:30px;border-radius:20px;border-left:0px;border-top:0px;border-right:0px;border-bottom:1px;color:blue;font-size:13px " type = "text"  readonly= "true " name="lognumber" id="lognumber" value='+data.orderinfo.lognumber+'  /></span>'+
							'<span onclick="close_query_logistics()"><img src="/resource/flower/image/icon/close.png" width="80%"></span></div>'+
							'<div class="body_2"><span><i3>请长按'+data.orderinfo.ename+'编号复制并点击进入链接粘贴查询:</i3><a href = '+data.addr+'target="_blank">点击进入链接</a></span></div>');
					$("#lognumber").focus().select();
					$('.query_logistics').show();
				}
		}else {
			$('.query_logistics').empty();
			$('.query_logistics').append('<div class="ls_body">'+
					'<div class="body_1">'+
					'<span><i1>物流编号:</i1><i2 class="workcode">'+data.orderinfo.lognumber+'</i2></span>'+
					'<span onclick="close_query_logistics()"><img src="/resource/flower/image/icon/close.png" width="100%"></span>'+
					'</div><div class="body_2"><span style="width: 100%;text-align: center;padding-top: 45px;padding-left:80px">您当前没有物流信息</span></div></div>');
			$('.query_logistics').show();
		}
	});
}
// 关闭物流查询
function close_query_logistics(){
	$('.query_logistics').hide();
	$('.container').removeClass('body_hide');
}
// 确认收货
function Ship_comfirm(workcode){
	layer.open({
		content: '确认收货?'
	    ,btn: ['确定', '取消']
	    ,yes: function(index){
	    	$.post('/service/shipconfirm/'+workcode, function(data){
	    		if(data){
	    			location.replace('/service/mylogistics');
				}else{
					layer.open({
					    content: '确认收货失败'
					    ,skin: 'msg'
					    ,time: 2 //2秒后自动关闭
					});
				}
			});
	    }
	});
}
// 获取短信验证码
function getBindingCode(){
	var tel = $('.binding_tel').val();
	var reg = /^1(3|4|5|7|8)\d{9}$/;
	if($('#send').data('lock') == 0){
		if($.trim(tel) == ''){
			layer.tips('请录入手机号码', '.binding_tel', {tips: [1, '#34495E']});return;
		}else if(!reg.test(tel)){
			layer.tips('手机号格式错误', '.binding_tel', {tips: [1, '#34495E']});return;
		}
		$.post('/account/getBindingCode', {'number':tel}, function(data){
			if(data == 1){
				$('#send').data('lock', 1);
				$('#send').removeClass('binding_send');
				$('#send').addClass('binding_send_lock');
				msgTime(90);
			}else{
				layer.msg('验证码发送失败', {time: 2000});return;
			}
		});
	}
}
// 短信倒计时
function msgTime(second){
	if(second>0){
		$('#send').text(second-- + 's后重新获取');
		setTimeout('msgTime('+second+')', 1000);
	}else{
		$('#send').text('获取验证码');
		$('#send').data('lock', 0);
		$('#send').removeClass('binding_send_lock');
		$('#send').addClass('binding_send');
	}
}
//保存个人信息
function savePersonInfo(_obj){
	
	var sex = $("input[name='sex']").val();
	var birthday = $("input[name='birthday']").val();

		
	if($(_obj).data('lock') == 0){
		$(_obj).data('lock', 1);
		
		var load = layer.load();
		$.post('/account/changeMyInfo', { 'sex':sex, 'birthday':birthday}, function(data){
			layer.close(load);
			$(_obj).data('lock', 0);
			if(!data.reslut){
				layer.msg(data.msg, {time: 2000});	
				location.replace('/account/myInfo');
			}else{
				layer.msg(data.msg, {time: 2000});
			}
		});
	}
}
// 保存绑定号码
function saveBinding(_obj){
	var tel = $('.binding_tel').val();
	var msm = $('.binding_msm').val();
	var reg = /^1(3|4|5|7|8|9)\d{9}$/;
	var code = /^\d{6}$/;
		
	if($(_obj).data('lock') == 0){
		if($.trim(tel) == ''){
			layer.tips('请录入手机号码', '.binding_tel', {tips: [1, '#34495E']});return;
		}else if(!reg.test(tel)){
			layer.tips('手机号格式错误', '.binding_tel', {tips: [1, '#34495E']});return;
		}
		if($.trim(msm) == ''){
			layer.tips('请录入验证码', '.binding_msm', {tips: [1, '#34495E']});return;
		}else if(!code.test(msm)){
			layer.tips('验证码为6位数字', '.binding_msm', {tips: [1, '#34495E']});return;
		}
		$(_obj).data('lock', 1);
		var load = layer.load();
		$.post('/account/saveBinding', {'number':tel,'msgcode':msm}, function(data){
			layer.close(load);
			$(_obj).data('lock', 0);
			if(data.reslut){
				layer.msg(data.msg, {time: 1000},function(){
					location.replace('/account/myInfo');
				});
			}else{
				if(data.msg=="保存成功"){
					layer.msg(data.msg, {time: 1000},function(){
						location.replace('/account/myInfo');
					});
				}else{
					layer.msg(data.msg);
				}
			}
		});
	}
}
// 保存意见反馈
function saveFeedback(_obj){
	var title = $('.feedback_title').val();
	var info = $('.feedback_info').val();
	if($(_obj).data('lock') == 0){
		if($.trim(title) == ''){
			layer.tips('请录入标题', '.feedback_title', {tips: [1, '#34495E']});return;
		}else if($.trim(title)=='null' || $.trim(title)=='NULL'){
			layer.tips('不能为NULL或者null', '.feedback_title', {tips: [1, '#34495E']});return;
		}
		if($.trim(info) == ''){
			layer.tips('请录入验证码', '.feedback_info', {tips: [1, '#34495E']});return;
		}else if($.trim(info)=='null' || $.trim(info)=='NULL'){
			layer.tips('不能为NULL或者null', '.feedback_info', {tips: [1, '#34495E']});return;
		}
		$(_obj).data('lock', 1);
		var load = layer.load();
		$.post('/account/saveFeedback', {'title':title,'info':info}, function(data){
			layer.close(load);
			$(_obj).data('lock', 0);
			if(data){
				layer.msg('提交成功');
			}else{
				layer.msg('提交失败');
			}
		});
	}
}
// config权限验证
function wxconfig(url){
	$.post('/weixin/configvalid', {'url':url.toString()}, function(data){
		wx.config({
		    debug: false,
		    appId: data.appId,
		    timestamp: data.timestamp,
		    nonceStr: data.nonceStr,
		    signature: data.signature,
		    /*jsApiList: ['onMenuShareTimeline','onMenuShareAppMessage']*/
		    jsApiList: [
		                'checkJsApi',
		                'onMenuShareTimeline',
		                'onMenuShareAppMessage',
		                'onMenuShareQQ',
		                'onMenuShareWeibo',
		                'onMenuShareQZone',
		                'hideMenuItems',
		                'showMenuItems',
		                'hideAllNonBaseMenuItem',
		                'showAllNonBaseMenuItem',
		                'translateVoice',
		                'startRecord',
		                'stopRecord',
		                'onVoiceRecordEnd',
		                'playVoice',
		                'onVoicePlayEnd',
		                'pauseVoice',
		                'stopVoice',
		                'uploadVoice',
		                'downloadVoice',
		                'chooseImage',
		                'previewImage',
		                'uploadImage',
		                'downloadImage',
		                'getNetworkType',
		                'openLocation',
		                'getLocation',
		                'hideOptionMenu',
		                'showOptionMenu',
		                'closeWindow',
		                'scanQRCode',
		                'chooseWXPay',
		                'openProductSpecificView',
		                'addCard',
		                'chooseCard',
		                'openCard',
		                'openAddress',
		                'getLocalImgData'
		            ]
		});
	});
}
// 送花分享绑定
function givetoshow(imgurl, desc, host){
	wx.onMenuShareTimeline({
		title: 'FlowerMM',
		link: host+'/product/3',
		imgUrl: imgurl
	});
	wx.onMenuShareAppMessage({
	    title: 'FlowerMM', // 分享标题
	    desc: desc, // 分享描述
	    link: host+'/product/3', // 分享链接
	    imgUrl: imgurl, // 分享图标
	    type: 'link', // 分享类型,music、video或link，不填默认为link
	    dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空
	    success: function () {
	        // 用户确认分享后执行的回调函数
	    },
	    cancel: function () {
	        // 用户取消分享后执行的回调函数
	    }
	});
}
// 签到
function signin(_obj){
    var hour=new Date().getHours();//当前时间小时
    var msgSuccess="";
    if(hour>=22 || hour<=4){
    	msgSuccess="夜深了，注意休息哦！";
    }else if(hour>4 && hour<=5){
    	msgSuccess="愿你今天的心情像花儿一样！";
    }else if(hour>5 && hour<=7){
    	msgSuccess="亲，记着吃早饭，上班路上开车慢点哦！";
    }else if(hour>7 && hour<=9){
    	msgSuccess="一束鲜花，一米阳光，生活本该如此！";
    }else if(hour>11 && hour<=12){
    	msgSuccess="对着花花小眯一会儿，下午工作更有精神啦！"; 	
    }else if(hour>=13 && hour<=16){
    	msgSuccess="下午好，来杯咖啡，对着桌上的花花发会儿呆吧！";
    }
    msgSuccess=msgSuccess+"签到成功，赠送1粒花籽！";
	if($(_obj).data('lock') == 0){
		$(_obj).data('lock', 1);
		$.post('/account/signin', function(data){
			if(data){
				$(_obj).empty().append('<img src="/resource/flower/image/icon/016.png"/><span>已签到</span>');
				layer.open({
					content: '签到成功，赠送1粒花籽！',
				    content: msgSuccess,
				    skin: 'msg',
				    time: 3 //2秒后自动关闭
				});
				dis18();
			}else{
				$(_obj).data('lock', 0);
				layer.open({
				    content: '签到失败',
				    skin: 'msg',
				    time: 2 //2秒后自动关闭
				});
			}
		});
	}
}

function dis18(){
	 $.ajax({ 
        type : "post", 
        url : "/account/signinCount18", 
        data : "" , 
        async :false, 
        success : function(data){ 
        	if (data) {
				location.replace("/account/sharesign18");
			}
           }
	 })
}




//减法
function accSub(arg1, arg2) {
    var r1, r2, m, n;
    try {
        r1 = arg1.toString().split(".")[1].length;
    }
    catch (e) {
        r1 = 0;
    }
    try {
        r2 = arg2.toString().split(".")[1].length;
    }
    catch (e) {
        r2 = 0;
    }
    m = Math.pow(10, Math.max(r1, r2)); //last modify by deeka //动态控制精度长度
    n = (r1 >= r2) ? r1 : r2;
    return ((arg1 * m - arg2 * m) / m).toFixed(n);
}
//加法
function accAdd(arg1,arg2){  
	var r1,r2,m;  
	try{r1=arg1.toString().split(".")[1].length}catch(e){r1=0}  
	try{r2=arg2.toString().split(".")[1].length}catch(e){r2=0}  
	m=Math.pow(10,Math.max(r1,r2))  
	return (arg1*m+arg2*m)/m  
	} 


//加法运算
function numAdd(num1, num2) {   
   var baseNum, baseNum1, baseNum2;   
   try {   
      baseNum1 = num1.toString().split(".")[1].length;   
   } catch (e) {    
     baseNum1 = 0;  
   }   
   try {  
       baseNum2 = num2.toString().split(".")[1].length;   
   } catch (e) {  
     baseNum2 = 0;   
   }   
   baseNum = Math.pow(10, Math.max(baseNum1, baseNum2));  
   var precision = (baseNum1 >= baseNum2) ? baseNum1 : baseNum2;//精度  
   return ((num1 * baseNum + num2 * baseNum) / baseNum).toFixed(precision);;   
};  
//减法运算
function numSub(num1, num2) {   
    var baseNum, baseNum1, baseNum2;   
    try {   
        baseNum1 = num1.toString().split(".")[1].length;   
    } catch (e) {   
        baseNum1 = 0;   
    }   
    try {   
        baseNum2 = num2.toString().split(".")[1].length;   
    } catch (e) {   
        baseNum2 = 0;   
    }   
    baseNum = Math.pow(10, Math.max(baseNum1, baseNum2));   
    var precision = (baseNum1 >= baseNum2) ? baseNum1 : baseNum2;   
    return ((num1 * baseNum - num2 * baseNum) / baseNum).toFixed(precision);   
}; 

// 乘法运算
function accMul(arg1, arg2) {
    var m = 0, s1 = arg1.toString(), s2 = arg2.toString();
    try {
        m += s1.split(".")[1].length;
    }
    catch (e) {
    }
    try {
        m += s2.split(".")[1].length;
    }
    catch (e) {
    }
    return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m);
}

