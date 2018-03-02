package com.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.swetake.util.Qrcode;

/**
 * 二维码工具类
 * 
 */
public class QRCodeUtil {

	private static final String FORMAT_NAME = "JPG";
	// 二维码尺寸
	private static int QRCODE_SIZE = 120;
	
	// logo宽度
	private static final int WIDTH = 30;
	// logo高度
	private static final int HEIGHT = 30;

	private static BufferedImage createImage(String content, String imgPath, boolean needCompress) throws Exception {
		 BufferedImage bufImg = null;  
	        try {  
	            Qrcode qrcodeHandler = new Qrcode();  
	            // 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小  
	            qrcodeHandler.setQrcodeErrorCorrect('M');  
	            qrcodeHandler.setQrcodeEncodeMode('B');  
	            // 设置设置二维码尺寸，取值范围1-40，值越大尺寸越大，可存储的信息越大  
	            qrcodeHandler.setQrcodeVersion(5);  
	            // 获得内容的字节数组，设置编码格式  
	            byte[] contentBytes = content.getBytes("utf-8");  
	            // 图片尺寸  
	            int imgSize = QRCODE_SIZE - 4;  
	            bufImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);  
	            Graphics2D gs = bufImg.createGraphics();  
	            // 设置背景颜色  
	            gs.setBackground(Color.WHITE);  
	            gs.clearRect(0, 0, imgSize, imgSize);  
	   
	            // 设定图像颜色> BLACK  
	            gs.setColor(Color.BLACK);  
	            // 设置偏移量，不设置可能导致解析出错  
	            int pixoff = 2;  
	            // 输出内容> 二维码  
	            if (contentBytes.length > 0 && contentBytes.length < 800) {  
	                boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);  
	                for (int i = 0; i < codeOut.length; i++) {  
	                    for (int j = 0; j < codeOut.length; j++) {  
	                        if (codeOut[j][i]) {  
	                            gs.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);  
	                        }  
	                    }  
	                }  
	            } else {  
	                throw new Exception("QRCode content bytes length = " + contentBytes.length + " not in [0, 800].");  
	            }  
	            gs.dispose();  
	            bufImg.flush();  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	        QRCodeUtil.insertImage(bufImg, imgPath, needCompress);
	        return bufImg;  
	}


	/**
	 * 插入logo
	 * 
	 * @param source  二维码图片
	 * @param imgPath logo图片地址
	 * @param needCompress  是否压缩
	 * @throws Exception
	 */
	private static void insertImage(BufferedImage source, String imgPath, boolean needCompress) throws Exception {
		File file = new File(imgPath);
		if (!file.exists()) {
			System.err.println(""+imgPath+"   该文件不存在！");
			return;
		}
		Image src = ImageIO.read(new File(imgPath));
		int width = src.getWidth(null);
		int height = src.getHeight(null);
		if (needCompress) { // 压缩logo
			if (width > WIDTH) {
				width = WIDTH;
			}
			if (height > HEIGHT) {
				height = HEIGHT;
			}
			Image image = src.getScaledInstance(width, height,
					Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			src = image;
		}
		// 插入logo
		Graphics2D graph = source.createGraphics();
		int x = (QRCODE_SIZE - width) / 2 - 2;
		int y = (QRCODE_SIZE - height) / 2 - 2;
		graph.drawImage(src, x, y, width, height, null);
		Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
		graph.setStroke(new BasicStroke(3f));
		graph.draw(shape);
		graph.dispose();
	}

	/**
	 * 生成二维码(内嵌logo)
	 * 
	 * @param content   内容
	 * @param imgPath   logo地址
	 * @param destPath  存放目录
	 * @param needCompress  是否压缩logo
	 * @throws Exception
	 */
	public static void encode(String content, String imgPath, String destPath, boolean needCompress) throws Exception {
		BufferedImage image = QRCodeUtil.createImage(content, imgPath,needCompress);
		ImageIO.write(image, FORMAT_NAME, new File(destPath));
	}
	
	/**
	 * 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
	 * @author zl
	 * @param destPath 存放目录
	 */
	public static void mkdirs(String destPath) {
		File file =new File(destPath);    
		//当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
		if (!file.exists() && !file.isDirectory()) {
			file.mkdirs();
		}
	}
}
