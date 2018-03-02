package com.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sun.misc.BASE64Encoder;

public class SFUtil {

	public static String loadFile(String fileName) {
		InputStream fis;
		try {
			fis = new FileInputStream(fileName);
			byte[] bs = new byte[fis.available()];
			fis.read(bs);
			String res = new String(bs, "utf8");
			fis.close();
			return res;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String md5EncryptAndBase64(String str) {
		return encodeBase64(md5Encrypt(str));
	}
	
	private static byte[] md5Encrypt(String encryptStr) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(encryptStr.getBytes("utf8"));
			return md5.digest();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String encodeBase64(byte[] b) {
		sun.misc.BASE64Encoder base64Encode = new BASE64Encoder();
		String str = base64Encode.encode(b);
		return str;
	}
	
	private static HttpClient getHttpClient(int port){
		PoolingClientConnectionManager pcm = new PoolingClientConnectionManager();
		SSLContext ctx=null;
		try{
			ctx = SSLContext.getInstance("TLS");
			X509TrustManager x509=new X509TrustManager(){
				public void checkClientTrusted(X509Certificate[] xcs, String string)
					throws CertificateException {
				}
				public void checkServerTrusted(X509Certificate[] xcs, String string)
					throws CertificateException {
				}
				public X509Certificate[] getAcceptedIssuers(){
					return null;
				}
			};
			ctx.init(null, new TrustManager[]{x509}, null);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme("https", port, ssf);
		pcm.getSchemeRegistry().register(sch);
		return new DefaultHttpClient(pcm);
	}
	
	/**
	 * 花美美主动请求顺丰接口，下发订单
	 * @param ordercode
	 * @param mailno
	 * @param contact
	 * @param tel
	 * @param province
	 * @param city
	 * @param county
	 * @param address
	 * @param j_contact
	 * @param j_tel
	 * @param j_address
	 * @return
	 * @throws Exception
	 */
	public static boolean synchroSF(String ordercode, String mailno, String contact, String tel, String province, String city, String county, String address, 
			String j_contact, String j_tel, String j_address) throws Exception {
		boolean result = false;
		int port = 11443;
		//String url = "https://bspoisp.sit.sf-express.com/bsp-oisp/sfexpressService";
		String url="http://bsp-ois.sit.sf-express.com:9080/bsp-ois/sfexpressService";//开发测试地址
		String checkword = "j8DzkIFgmlomPt0aLuwU";
		
		String[] addArr = j_address.split("-", 4);
		String j_province="未填",j_city="未填",j_county="未填",j_addr="未填";
		try {
			j_province = addArr[0];
			j_city = addArr[1];
			j_county = addArr[2];
			j_addr = addArr[3];
		} catch (Exception e) {}
		StringBuilder requestData = new StringBuilder("<Request service='OrderService' lang='zh-CN'>");
		requestData.append("<Head>BSPdevelop</Head><Body>");
		requestData.append("<Order orderid ='" + ordercode + "'"
				+ " mailno='" + mailno + "'"
				+ " is_gen_bill_no='2'"
				+ " j_company='苏州花美美网络科技有限公司'"
				+ " j_contact='" + j_contact + "'"
				+ " j_tel='" + j_tel + "'"
				+ " j_province='" + j_province + "'"
				+ " j_city='" + j_city + "'"
				+ " j_county='" + j_county + "'"
				+ " j_address='" + j_addr + "'"
				
				+ " d_company='花美美'"
				+ " d_contact='" + contact + "'"
				+ " d_tel='" + tel + "'"
				+ " d_province='" + province + "'"
				+ " d_city='" + city + "'"
				+ " d_county='" + county + "'"
				+ " d_address='" + address + "'"
				
				+ " custid='7551878519'>" //FIXME 
				+ "<Cargo Name='花束'></Cargo></Order>");
		requestData.append("</Body></Request>");
		String xml = requestData.toString();
		String verifyCode = SFUtil.md5EncryptAndBase64(xml + checkword);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("xml", xml));
		nvps.add(new BasicNameValuePair("verifyCode", verifyCode));
		
		HttpClient httpclient=getHttpClient(port);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
		HttpResponse response = httpclient.execute(httpPost);

		if(response.getStatusLine().getStatusCode() == 200){
			String xmlResponse = EntityUtils.toString(response.getEntity());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(XMLParser.getStringStream(xmlResponse));
			Element element = document.getDocumentElement();
			String head = element.getElementsByTagName("Head").item(0).getFirstChild().getNodeValue();

			if("OK".equalsIgnoreCase(head)){
				NodeList node = document.getElementsByTagName("OrderResponse");
				Element ele = (Element)node.item(0);
				String destcode = ele.getAttribute("destcode");//目的地区域代码，可用于顺丰电子运单标签打印
				String origincode=ele.getAttribute("origincode");//原寄地区域代码，可用于顺丰电子运单标签打印
				result = true;
			}else{
				result = false;
				
			}
		}else{
			EntityUtils.consume(response.getEntity());
			throw new RuntimeException("response status error: " + response.getStatusLine().getStatusCode());
		}
		return result;
	}
	
	// 物流查询接口
	/**
	 * 花美美根据运单号主动查询
	 * @param number
	 * @throws Exception
	 */
	public static void searchRoute(String number) throws Exception {
		int port = 11443;
		//String url = "https://bspoisp.sit.sf-express.com/bsp-oisp/sfexpressService";
		String url="http://bsp-ois.sit.sf-express.com:9080/bsp-ois/sfexpressService";//开发测试地址
		String checkword = "j8DzkIFgmlomPt0aLuwU";
		
		StringBuilder requestData = new StringBuilder("<Request service='RouteService' lang='zh-CN'>");
		requestData.append("<Head>BSPdevelop</Head><Body>");
		requestData.append("<RouteRequest tracking_type ='1' method_type='1' tracking_number='" + number + "' />");
		requestData.append("</Body></Request>");
		String xml = requestData.toString();
		String verifyCode = SFUtil.md5EncryptAndBase64(xml + checkword);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("xml", xml));
		nvps.add(new BasicNameValuePair("verifyCode", verifyCode));
		
		HttpClient httpclient=getHttpClient(port);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
		HttpResponse response = httpclient.execute(httpPost);

		if(response.getStatusLine().getStatusCode() == 200){
			String xmlResponse = EntityUtils.toString(response.getEntity());
			System.out.println(xmlResponse);
		}else{
			EntityUtils.consume(response.getEntity());
			throw new RuntimeException("response status error: " + response.getStatusLine().getStatusCode());
		}
	}
	
	public static void main(String args[]) throws Exception {
		searchRoute("20161123194940901");
	}
}