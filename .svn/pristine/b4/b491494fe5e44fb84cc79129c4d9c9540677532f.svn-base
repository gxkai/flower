package com.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFile {

	/**
	 * @author Glacier
	 * @date 2017年10月24日
	 * @param fileUrl 
	 * @param downPath
	 */
	 public static void downUrlFile(String fileUrl,String downPath){
	    	File savePath = new File(downPath);
	    	      if (!savePath.exists()) {   
	    	          savePath.mkdir();   
	    	      }  
	    	      String[] urlname = fileUrl.split("/");  
	    	      int len = urlname.length-1;  
	    	      String uname = urlname[len]+".jpg";//获取文件名  
	    	      try {  
	    	          File file = new File(savePath+"/"+uname);//创建新文件  
	    	          if(file!=null && !file.exists()){  
	    	              file.createNewFile();  
	    	          }  
	    	          OutputStream oputstream = new FileOutputStream(file);  
	    	          URL url = new URL(fileUrl);  
	    	          HttpURLConnection uc = (HttpURLConnection) url.openConnection();  
	    	          uc.setDoInput(true);//设置是否要从 URL 连接读取数据,默认为true  
	    	          uc.connect();  
	    	          InputStream iputstream = uc.getInputStream();  
	    	          System.out.println("file size is:"+uc.getContentLength());//打印文件长度  
	    	          byte[] buffer = new byte[4*1024];  
	    	          int byteRead = -1;     
	    	          while((byteRead=(iputstream.read(buffer)))!= -1){  
	    	              oputstream.write(buffer, 0, byteRead);  
	    	          }  
	    	          oputstream.flush();    
	    	          iputstream.close();  
	    	          oputstream.close();  
	    	      } catch (Exception e) {  
	    	          System.out.println("读取失败！");  
	    	          e.printStackTrace();  
	    	      }        
	    
	    	}
}
