package com.util;


import java.io.IOException;


import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FetchRet;
import com.qiniu.util.Auth;

import sun.misc.BASE64Decoder;

/**
 * Created by pan on 17/1/5 0005.
 * 七牛接口上传工具类，直接通过地址读取本地文件
 * -这种方式由于会涉及到浏览器保护
 * 
 * 
 * 导包：
 * qiniu-java-sdk 7.2.6 √
 * okhttp 3.3.1 √
 * gson 2.6.2 √
 * happy-dns-java 0.1.4 √
 * junit 4.12 √
 * 
 * 还需要导：
 * ikio √
 * 
 */

public class QianNiuUpload {
	//设置好账号的ACCESS_KEY和SECRET_KEY
    public static String ACCESS_KEY = "qYXqO1QnGqVTH-EyLMHFWlLJX3zDbxQ4972A-_sK";
    public static String SECRET_KEY = "cEWwYFfW_C9Wl22VJHobS0me_gxtxDPi7UrW_iz3";
    //要上传的空间
    public static String bucketname = "hmmbbs";
    //密钥配置
    public static Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    ///////////////////////指定上传的Zone的信息//////////////////
    //第一种方式: 指定具体的要上传的zone
    //注：该具体指定的方式和以下自动识别的方式选择其一即可
    //要上传的空间(bucket)的存储区域为华东时
    // Zone z = Zone.zone0();
    //要上传的空间(bucket)的存储区域为华北时
    // Zone z = Zone.zone1();
    //要上传的空间(bucket)的存储区域为华南时
    // Zone z = Zone.zone2();

    //第二种方式: 自动识别要上传的空间(bucket)的存储区域是华东、华北、华南。
    public static Zone z = Zone.autoZone();
    public static Configuration c = new Configuration(z);

    //创建上传对象
    private static UploadManager uploadManager = new UploadManager(c);

    //简单上传，使用默认策略，只需要设置上传的空间名就可以了
    public static String getUpToken() {
    	String token=auth.uploadToken(bucketname);
        return token;
    }
    
	//获取临时素材(视频不能使用https协议)
	public static final String GET_TMP_MATERIAL = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
	//获取临时素材(视频)
	public static final String GET_TMP_MATERIAL_VIDEO = "http://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
	     
	 //获取微信服务器中生成的媒体文件
	 //由于视频使用的是http协议，而图片、语音使用http协议，故此处需要传递media_id和type
	public static String fetchTmpFile(String media_id, String type,String token){
		   try {
			   
		        String url = null;
		        
			    //视频是http协议
			    if("video".equalsIgnoreCase(type)){
			     url = String.format(GET_TMP_MATERIAL_VIDEO, token, media_id);
			    }else{
			     url = String.format(GET_TMP_MATERIAL, token, media_id);;
			    }

			    System.out.println("文件的路径为：" + url);
			    //构造一个带指定Zone对象的配置类
			    Configuration cfg = new Configuration(Zone.autoZone());  
			    
			    Auth me = Auth.create(ACCESS_KEY,SECRET_KEY);
			    BucketManager bucketManager = new BucketManager(me, cfg);
			   // bucketManager.fetch("外源图片地址", "要上传的七牛目标资源文件夹", "文件名");
			    FetchRet fetchRet = bucketManager.fetch(url,bucketname);
			    
			    String bucket = bucketname;
			    String key = fetchRet.key;
			    String fops = "avthumb/mp3";
			    
			    
			    
			    // 上传成功之后把key 写到数据库    
			    return fetchRet.key;
			    
			  } catch (Exception e) {
			   e.printStackTrace();
			  }
		return null;
	}
	

    /**
     * 上传图片到【七牛云】
     * 数据流上传
     * @param imgurl 
     * @throws IOException
     */
    public static String upload(String imgurl ) throws IOException {
    	String url="";
    	try {
            //调用put方法上传
    		String[] str = imgurl.split(",");
        	BASE64Decoder decoder = new BASE64Decoder();
        	byte[] decoderBytes = decoder.decodeBuffer(str[1]);
            Response res = uploadManager.put(decoderBytes, null, getUpToken());
            DefaultPutRet putRet = new Gson().fromJson(res.bodyString(), DefaultPutRet.class);
            url="http://ozdzt401n.bkt.clouddn.com/"+putRet.key;
        } catch (QiniuException e) {
            Response r = e.response;
            // 请求失败时打印的异常的信息
            System.out.println(r.toString());
            try {
                //响应的文本信息
                System.out.println(r.bodyString());
            } catch (QiniuException e1) {
                //ignore
            }
        }
    	return url;
    }
    
    public static String uploadBase64(String imgBase64 ) throws IOException {
    	String url="";
    	try {
    		String [] xx = imgBase64.split(",");
    		if(xx.length==1){
    			imgBase64=xx[0];
    		}else{
    			imgBase64=xx[1];
    		}
            //调用put方法上传
        	BASE64Decoder decoder = new BASE64Decoder();
        	byte[] decoderBytes = decoder.decodeBuffer(imgBase64);
            Response res = uploadManager.put(decoderBytes, null, getUpToken());
            DefaultPutRet putRet = new Gson().fromJson(res.bodyString(), DefaultPutRet.class);
            url="http://ozdzt401n.bkt.clouddn.com/"+putRet.key;
        } catch (QiniuException e) {
            Response r = e.response;
            // 请求失败时打印的异常的信息
            System.out.println(r.toString());
            try {
                //响应的文本信息
                System.out.println(r.bodyString());
            } catch (QiniuException e1) {
                //ignore
            }
        }
    	return url;
    }
    
   
}
