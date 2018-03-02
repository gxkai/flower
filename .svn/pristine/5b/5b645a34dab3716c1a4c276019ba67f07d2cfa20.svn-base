package com.util;

import java.util.UUID;
import com.jfinal.kit.PropKit;
import java.util.Map;
import java.util.HashMap;
import java.util.Formatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;  

public class Sign {
	// config接口注入权限验证
    public static Map<String, String> sign1(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string = "jsapi_ticket=" + jsapi_ticket +
                  "&noncestr=" + nonce_str +
                  "&timestamp=" + timestamp +
                  "&url=" + url;

        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("appId", PropKit.get("appId"));
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }
    
    // 支付签名验证
    public static Map<String, String> sign2(String prepay_id) {
    	Map<String, String> params = new HashMap<>();
    	params.put("appId", PropKit.get("appId"));
    	params.put("timeStamp", create_timestamp());
    	params.put("nonceStr", create_nonce_str());
    	params.put("package", "prepay_id=" + prepay_id);
    	params.put("signType", "MD5");
    	params.put("paySign", Signature.getSign(params));
        return params;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
