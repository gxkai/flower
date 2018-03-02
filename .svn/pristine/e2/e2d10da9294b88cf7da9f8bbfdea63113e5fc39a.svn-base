package com.controller;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.util.Constant;

/**
 * @Desc 文件上传
 * @author lxx
 * @date 2016/8/15
 * */
public class UploadCtrl extends Controller {
	// king上传图片
	public void kingImage(){
		UploadFile uf = getFile("imgFile");
		String fileExt = uf.getOriginalFileName().substring(uf.getOriginalFileName().lastIndexOf(".")).toLowerCase();
		String newName = new Date().getTime() + fileExt;
		uf.getFile().renameTo(new File(Constant.imgpath + newName));
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("error", 0);
		responseMap.put("url", "/image/"+newName);
		renderJson(responseMap);
	}
}
