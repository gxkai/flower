package com.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import sun.misc.BASE64Decoder;

public class UploadImageUtil {
	/**
	 * @throws IOException 
	 * @Desc 上传base64图片
	 * @author lxx
	 * */
	public static String upLoadBase(String basestr) throws IOException{
		String imgpath = Constant.imgpath;
		File fileDir = new File(imgpath);
    	//判断文件夹是否存在，如果不存在就创建目录
    	if(!fileDir.exists() && !fileDir.isDirectory()){
    		//创建文件目录
       	  	fileDir.mkdirs();
       	}
    	String[] str = basestr.split(",");
    	String str0 = str[0];
    	String suffix = "." + str0.substring(str0.indexOf("/")+1, str0.indexOf(";"));
    	BASE64Decoder decoder = new BASE64Decoder();
    	byte[] decoderBytes = decoder.decodeBuffer(str[1]);
		String newFileName = new Date().getTime() + suffix;
        FileOutputStream write = new FileOutputStream(new File(imgpath, newFileName));
        write.write(decoderBytes);
        write.flush();
        write.close();
		return "/image/" + newFileName;
	}

}
