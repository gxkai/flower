package com.util;

import com.jfinal.kit.PropKit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Signature {
	
	public static String getSign(Map<String, String> map){
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String, String> entry:map.entrySet()){
            if(entry.getValue()!=""){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result += "key=" + PropKit.get("key");
     
        result = MD5.MD5Encode(result).toUpperCase();
   
        return result;
    }
	
	/**
     * 验证支付结果签名
     * @param responseString API返回的XML数据字符串
     * @return API签名是否合法
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static boolean SignValidIsFromWeXin(String xmlStr) throws ParserConfigurationException, IOException, SAXException {
        Map<String,Object> map = XMLParser.getMapFromXML(xmlStr);
        String sign = map.get("sign").toString();
        if(sign=="" || sign == null){
            return false;
        }
       
        //清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
        map.put("sign","");
        
        //将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String, Object> entry:map.entrySet()){
            if(entry.getValue()!=""){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String newSign = sb.toString();
        newSign += "key=" + PropKit.get("key");
        newSign = MD5.MD5Encode(newSign).toUpperCase();
        if(sign.equals(newSign)){
        	return true;
        }else{
        	//签名验不过，表示这个API返回的数据有可能已经被篡改了
        	return false;
        }
    }

}