package com.util;

/*import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeKit {

    public static final String QRCODE_DEFAULT_CHARSET = "UTF-8";

    public static final int QRCODE_DEFAULT_HEIGHT = 3000;

    public static final int QRCODE_DEFAULT_WIDTH = 3000;

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    public static void main(String[] args) throws IOException {
    	String data = "http://weixin.qq.com/q/2zsyWEXm8IKJKqOAoBOK";
        String logpath = "C:/QRImage/head/0.jpg";
        String outpath = "C:/QRImage/1.jpg";
        File logoFile = new File(logpath);
        BufferedImage image = QRCodeKit.createQRCodeWithLogo(data, logoFile);
        ImageIO.write(image, "jpg", new File(outpath));
        System.out.println("done");
	}

    public static void Code(String data,String logpath,String outpath) throws IOException, NotFoundException{
        String data = "http://weixin.qq.com/q/2zsyWEXm8IKJKqOAoBOK";
        String logpath = "C:/QRImage/head/0.jpg";
        String outpath = "C:/QRImage/1.jpg";
        File logoFile = new File(logpath);
        BufferedImage image = QRCodeKit.createQRCodeWithLogo(data, logoFile);
        ImageIO.write(image, "jpg", new File(outpath));
        System.out.println("done");
    }

    *//**
     * Create qrcode with default settings
     *
     * @author stefli
     * @param data
     * @return
     *//*
    public static BufferedImage createQRCode(String data) {
        return createQRCode(data, QRCODE_DEFAULT_WIDTH, QRCODE_DEFAULT_HEIGHT);
    }

    *//**
     * Create qrcode with default charset
     *
     * @author stefli
     * @param data
     * @param width
     * @param height
     * @return
     *//*
    public static BufferedImage createQRCode(String data, int width, int height) {
        return createQRCode(data, QRCODE_DEFAULT_CHARSET, width, height);
    }

    *//**
     * Create qrcode with specified charset
     *
     * @author stefli
     * @param data
     * @param charset
     * @param width
     * @param height
     * @return
     *//*
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static BufferedImage createQRCode(String data, String charset, int width, int height) {
        Map hint = new HashMap();
        hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hint.put(EncodeHintType.CHARACTER_SET, charset);

        return createQRCode(data, charset, hint, width, height);
    }

    *//**
     * Create qrcode with specified hint
     *
     * @author stefli
     * @param data
     * @param charset
     * @param hint
     * @param width
     * @param height
     * @return
     *//*
    public static BufferedImage createQRCode(String data, String charset, Map<EncodeHintType, ?> hint, int width,
            int height) {
        BitMatrix matrix;
        try {
            matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE,
                    width, height, hint);
            return toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }
    *//**
     * Create qrcode with default settings and logo
     *
     * @author stefli
     * @param data
     * @param logoFile
     * @return
     * @throws IOException 
     *//*
    public static BufferedImage createQRCodeWithLogo(String data, File logoFile) throws IOException {
        return createQRCodeWithLogo(data, QRCODE_DEFAULT_WIDTH, QRCODE_DEFAULT_HEIGHT, logoFile);
    }

    *//**
     * Create qrcode with default charset and logo
     *
     * @author stefli
     * @param data
     * @param width
     * @param height
     * @param logoFile
     * @return
     * @throws IOException 
     *//*
    public static BufferedImage createQRCodeWithLogo(String data, int width, int height, File logoFile) throws IOException {
        return createQRCodeWithLogo(data, QRCODE_DEFAULT_CHARSET, width, height, logoFile);
    }

    *//**
     * Create qrcode with specified charset and logo
     *
     * @author stefli
     * @param data
     * @param charset
     * @param width
     * @param height
     * @param logoFile
     * @return
     * @throws IOException 
     *//*
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static BufferedImage createQRCodeWithLogo(String data, String charset, int width, int height, File logoFile) throws IOException {
        Map hint = new HashMap();
        hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hint.put(EncodeHintType.CHARACTER_SET, charset);

        return createQRCodeWithLogo(data, charset, hint, width, height, logoFile);
    }

    *//**
     * Create qrcode with specified hint and logo
     *
     * @author stefli
     * @param data
     * @param charset
     * @param hint
     * @param width
     * @param height
     * @param logoFile
     * @return
     * @throws IOException 
     *//*
    public static BufferedImage createQRCodeWithLogo(String data, String charset, Map<EncodeHintType, ?> hint,
            int width, int height, File logoFile) throws IOException {
        try {
            BufferedImage qrcode = createQRCode(data, charset, hint, width, height);
            BufferedImage logo = ImageIO.read(logoFile);
            int deltaHeight = height - logo.getHeight();
            int deltaWidth = width - logo.getWidth();

            BufferedImage combined = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) combined.getGraphics();
            g.drawImage(qrcode, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.drawImage(logo, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);

            return combined;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

   
}*/
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;  
  
/** 
 * 二维码工具类 
 *  
 */  
public class QRCodeKit {  
    private static final String CHARSET = "utf-8";  
    private static final String FORMAT = "JPG";  
    // 二维码尺寸  
    private static final int QRCODE_SIZE = 120;  
    // LOGO宽度  
    private static final int LOGO_WIDTH = 30;  
    // LOGO高度  
    private static final int LOGO_HEIGHT = 30;  
  
    private static BufferedImage createImage(String content, String logoPath, boolean needCompress) throws Exception {  
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();  
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);  
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);  
        hints.put(EncodeHintType.MARGIN, 1);  
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE,  
                hints);
        bitMatrix = deleteWhite(bitMatrix);
        int width = bitMatrix.getWidth();  
        int height = bitMatrix.getHeight();  
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
        for (int x = 0; x < width; x++) {  
            for (int y = 0; y < height; y++) {  
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);  
            }  
        }  
        if (logoPath == null || "".equals(logoPath)) {  
            return image;  
        }  
        // 插入图片  
        QRCodeKit.insertImage(image, logoPath, needCompress);  
        return image;  
    }
    public static BitMatrix deleteWhite(BitMatrix matrix){  
        int[] rec = matrix.getEnclosingRectangle();  
        int resWidth = rec[2] + 1;  
        int resHeight = rec[3] + 1;  
      
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);  
        resMatrix.clear();  
        for (int i = 0; i < resWidth; i++) {  
            for (int j = 0; j < resHeight; j++) {  
                if (matrix.get(i + rec[0], j + rec[1]))  
                    resMatrix.set(i, j);  
            }  
        }  
        return resMatrix;  
    }  
  
    /** 
     * 插入LOGO 
     *  
     * @param source 
     *            二维码图片 
     * @param logoPath 
     *            LOGO图片地址 
     * @param needCompress 
     *            是否压缩 
     * @throws Exception 
     */  
    private static void insertImage(BufferedImage source, String logoPath, boolean needCompress) throws Exception {  
        File file = new File(logoPath);  
        if (!file.exists()) {  
            throw new Exception("logo file not found.");  
        }  
        Image src = ImageIO.read(new File(logoPath));  
        int width = src.getWidth(null);  
        int height = src.getHeight(null);  
        if (needCompress) { // 压缩LOGO  
            if (width > LOGO_WIDTH) {  
                width = LOGO_WIDTH;  
            }  
            if (height > LOGO_HEIGHT) {  
                height = LOGO_HEIGHT;  
            }  
            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);  
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
            Graphics g = tag.getGraphics();  
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图  
            g.dispose();  
            src = image;  
        }  
        // 插入LOGO  
        Graphics2D graph = source.createGraphics();  
        int x = (QRCODE_SIZE - width) / 2-18;  
        int y = (QRCODE_SIZE - height) / 2-18;  
        graph.drawImage(src, x, y, width, height, null);  
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);  
        graph.setStroke(new BasicStroke(3f));  
        graph.draw(shape);  
        graph.dispose();  
    }  
  
    /** 
     * 生成二维码(内嵌LOGO) 
     * 二维码文件名随机，文件名可能会有重复 
     *  
     * @param content 
     *            内容 
     * @param logoPath 
     *            LOGO地址 
     * @param destPath 
     *            存放目录 
     * @param needCompress 
     *            是否压缩LOGO 
     * @throws Exception 
     */  
    public static String encode(String content, String logoPath, String destPath, boolean needCompress) throws Exception {  
        BufferedImage image = QRCodeKit.createImage(content, logoPath, needCompress);  
       /* mkdirs(destPath);  
        String fileName = new Random().nextInt(99999999) + "." + FORMAT.toLowerCase();  */
        ImageIO.write(image, FORMAT, new File(destPath));  
        return destPath;  
    }  
      
    /** 
     * 生成二维码(内嵌LOGO) 
     * 调用者指定二维码文件名 
     *  
     * @param content 
     *            内容 
     * @param logoPath 
     *            LOGO地址 
     * @param destPath 
     *            存放目录 
     * @param fileName 
     *            二维码文件名 
     * @param needCompress 
     *            是否压缩LOGO 
     * @throws Exception 
     */  
    public static String encode(String content, String logoPath, String destPath, String fileName, boolean needCompress) throws Exception {  
        BufferedImage image = QRCodeKit.createImage(content, logoPath, needCompress);  
        mkdirs(destPath);  
        fileName = fileName.substring(0, fileName.indexOf(".")>0?fileName.indexOf("."):fileName.length())   
                + "." + FORMAT.toLowerCase();  
        ImageIO.write(image, FORMAT, new File(destPath + "/" + fileName));  
        return fileName;  
    }  
  
    /** 
     * 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir． 
     * (mkdir如果父目录不存在则会抛出异常) 
     * @param destPath 
     *            存放目录 
     */  
    public static void mkdirs(String destPath) {  
        File file = new File(destPath);  
        if (!file.exists() && !file.isDirectory()) {  
            file.mkdirs();  
        }  
    }  
  
    /** 
     * 生成二维码(内嵌LOGO) 
     *  
     * @param content 
     *            内容 
     * @param logoPath 
     *            LOGO地址 
     * @param destPath 
     *            存储地址 
     * @throws Exception 
     */  
    public static String encode(String content, String logoPath, String destPath) throws Exception {  
        return QRCodeKit.encode(content, logoPath, destPath, false);  
    }  
  
    /** 
     * 生成二维码 
     *  
     * @param content 
     *            内容 
     * @param destPath 
     *            存储地址 
     * @param needCompress 
     *            是否压缩LOGO 
     * @throws Exception 
     */  
    public static String encode(String content, String destPath, boolean needCompress) throws Exception {  
        return QRCodeKit.encode(content, null, destPath, needCompress);  
    }  
  
    /** 
     * 生成二维码 
     *  
     * @param content 
     *            内容 
     * @param destPath 
     *            存储地址 
     * @throws Exception 
     */  
    public static String encode(String content, String destPath) throws Exception {  
        return QRCodeKit.encode(content, null, destPath, false);  
    }  
  
    /** 
     * 生成二维码(内嵌LOGO) 
     *  
     * @param content 
     *            内容 
     * @param logoPath 
     *            LOGO地址 
     * @param output 
     *            输出流 
     * @param needCompress 
     *            是否压缩LOGO 
     * @throws Exception 
     */  
    public static void encode(String content, String logoPath, OutputStream output, boolean needCompress)  
            throws Exception {  
        BufferedImage image = QRCodeKit.createImage(content, logoPath, needCompress);  
        ImageIO.write(image, FORMAT, output);  
    }  
  
    /** 
     * 生成二维码 
     *  
     * @param content 
     *            内容 
     * @param output 
     *            输出流 
     * @throws Exception 
     */  
    public static void encode(String content, OutputStream output) throws Exception {  
        QRCodeKit.encode(content, null, output, false);  
    }  
  
    /** 
     * 解析二维码 
     *  
     * @param file 
     *            二维码图片 
     * @return 
     * @throws Exception 
     */  
    public static String decode(File file) throws Exception {  
        BufferedImage image;  
        image = ImageIO.read(file);  
        if (image == null) {  
            return null;  
        }  
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);  
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
        Result result;  
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();  
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);  
        result = new MultiFormatReader().decode(bitmap, hints);  
        String resultStr = result.getText();  
        return resultStr;  
    }  
  
    /** 
     * 解析二维码 
     *  
     * @param path 
     *            二维码图片地址 
     * @return 
     * @throws Exception 
     */  
    public static String decode(String path) throws Exception {  
        return QRCodeKit.decode(new File(path));  
    }  
  
   /* public static void main(String[] args) throws Exception {  
        String text = "http://weixin.qq.com/q/02H-QwNz7-f4010000007i";  
        //不含Logo  
        //QRCodeKit.encode(text, null, "e:\\", true);  
        //含Logo，不指定二维码图片名  
        QRCodeKit.encode(text, "C:/QRImage/head/0.jpg", "C:/QRImage/1.jpg", true);  
        //含Logo，指定二维码图片名  
        //QRCodeKit.encode(text, "e:\\csdn.jpg", "e:\\", "qrcode", true);
     // 二维码 添加背景 （对应目录 “/QRImage/” 下 有添加 invite.jpg 作为背景图片 ）
        try {
			float a = (float) 0.9;
			File bg = new File("C:/QRImage/invite.jpg");
			BufferedImage buffImg = NewImageUtils.watermark(bg,new File("C:/QRImage/1.jpg"), 450, 450, a);
			NewImageUtils.generateWaterFile(buffImg, "C:/QRImage/1.jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }  */
} 