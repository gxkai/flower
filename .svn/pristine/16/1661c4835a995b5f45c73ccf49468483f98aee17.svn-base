function open_window(title, url, w, h){
	layer_show(title, '/manage/iframe'+url, w, h);
}

function open_window_weixin(title, url, w, h){
	layer_show(title, url, w, h);
}

function refresh_iframe(){
	var iframedoc = $('#iframe_box iframe:visible').get(0);
	iframedoc.src = iframedoc.src;
}
function refresh_iframe1(url){
	var iframedoc = $('#iframe_box iframe:visible').get(0);
	iframedoc.src = url;
}
function open_window_full(title, url){
	var index = layer.open({
		type: 2,
		title: title,
		content: '/manage/iframe'+url
	});
	layer.full(index);
}

function open_point_window(title, url, w, h, id){
	layer.open({
		type: 2,
		area: [w+'px', h +'px'],
		fix: false, //不固定
		maxmin: true,
		shade:0.4,
		title: title,
		id: id,
		content: '/manage/iframe'+url
	});
}

function open_schedule(url, w, h){
	layer.open({
		type: 2,
		area: [w+'px', h +'px'],
		fix: true, //不固定
		title: false,
		closeBtn: 0,
		content: '/manage/iframe'+url
	});
}
//展示图片
function showimgage(imgurl){
	$('#yulanimg').empty().append('<img src="'+imgurl+'" width="100%">');
	layer.open({
		type: 1,
		title: false,
		closeBtn: 0,
		area: '516px',
		skin: 'layui-layer-nobg', //没有背景色
		shadeClose: true,
		content: $('#yulanimg')
	});
}

//全屏询问框{
function actconfirm(title, next){
	layer.confirm(title, function(index){
		layer.close(index);
		next();
	});
}

// 打印
function print_show(title,url,w,h){
	layer.open({
		type: 2,
		area: [w, h],
		fix: false, //不固定
		maxmin: true,
		shade:0.4,
		title: title,
		content: url
	});
}

//登录过期
function loginOut(){
	layer.confirm('登录过期，重新登录？',function(index){
		location.replace('/manage/login');
	});
}