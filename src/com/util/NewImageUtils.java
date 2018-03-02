package com.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import magick.ImageInfo;
import magick.MagickImage;
public class NewImageUtils {
    /**
     * 
     * @Title: 构造图片
     * @Description: 生成水印并返回java.awt.image.BufferedImage
     * @param file
     *            源文件(图片)
     * @param waterFile
     *            水印文件(图片)
     * @param x
     *            距离右下角的X偏移量
     * @param y
     *            距离右下角的Y偏移量
     * @param alpha
     *            透明度, 选择值从0.0~1.0: 完全透明~完全不透明
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage watermark(File file, File waterFile, int x, int y, float alpha) throws IOException {
        // 获取底图
        BufferedImage buffImg = ImageIO.read(file);
        // 获取层图
        BufferedImage waterImg = ImageIO.read(waterFile);
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();
        int waterImgWidth = waterImg.getWidth();// 获取层图的宽度
        int waterImgHeight = waterImg.getHeight();// 获取层图的高度
        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        // 绘制
        g2d.drawImage(waterImg, x, y, waterImgWidth, waterImgHeight, null);
        /*g2d.drawImage(waterImg, x, y, 200, 200, null);*/
        g2d.dispose();// 释放图形上下文使用的系统资源
        return buffImg;
    }
    public static BufferedImage watermarkHYK(File file, File waterFile,File file_head, int x, int y, float alpha,String nick) throws IOException {
        // 获取底图
        BufferedImage buffImg = ImageIO.read(file);
        // 获取层图
        BufferedImage waterImg = ImageIO.read(waterFile);
        BufferedImage headImg = ImageIO.read(file_head);
        String msg = "我正在领取";
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();
        int waterImgWidth = waterImg.getWidth();// 获取层图的宽度
        int waterImgHeight = waterImg.getHeight();// 获取层图的高度
        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        // 绘制
        g2d.drawImage(waterImg, x, y, waterImgWidth, waterImgHeight, null);
        g2d.drawImage(headImg, 30, 30, waterImgWidth, waterImgHeight, null);
        /* 设置2D画笔的画出的文字颜色 */  
        g2d.setColor(Color.pink);
        /* 设置2D画笔的画出的文字背景色 */  
        g2d.setBackground(Color.white);
        
        AttributedString ats = new AttributedString(nick); //设置nick位置
        AttributedString ats2 = new AttributedString(msg); //设置msg位置
       Font font = new Font("Microsoft YaHei,微软雅黑", Font.BOLD, 30);
        g2d.setFont(font); 

        /* 消除java.awt.Font字体的锯齿 */  
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                                  RenderingHints.VALUE_ANTIALIAS_ON);  
        /* 消除java.awt.Font字体的锯齿 */  
        ats.addAttribute(TextAttribute.FONT, font, 0, nick.length());  
        AttributedCharacterIterator iter = ats.getIterator();  
        /* 添加水印的文字和设置水印文字出现的内容 ----位置 */  
        g2d.drawString(iter, waterImgWidth+40, waterImgHeight-40);  
        
        /* 消除java.awt.Font字体的锯齿 */  
        ats2.addAttribute(TextAttribute.FONT, font, 0, msg.length());  
        AttributedCharacterIterator iter2 = ats2.getIterator();  
        /* 添加水印的文字和设置水印文字出现的内容 ----位置 */  
        g2d.drawString(iter2, waterImgWidth+40, waterImgHeight);  
        /*g2d.drawImage(waterImg, x, y, 200, 200, null);*/
        g2d.dispose();// 释放图形上下文使用的系统资源
        return buffImg;
    }
    
    public static BufferedImage watermarkXMB(File file, File waterFile, int x, int y, float alpha,String nick,String contentStr) throws IOException {
        // 获取底图
        BufferedImage buffImg = ImageIO.read(file);
        // 获取层图
        BufferedImage waterImg = ImageIO.read(waterFile);
        String msg = nick+"的新年小目标";
        String msg2 = contentStr;
        String msg3 = "长按识别二维码";
        String msg4 = "给"+nick+"的小目标见证吧";
        String msg5 = "有神秘惊喜哦";
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();
        int buffImgWidth = buffImg.getWidth();// 获取底图的宽度
        int buffImgHeight = buffImg.getHeight();// 获取底图的高度
        int waterImgWidth = waterImg.getWidth();// 获取层图的宽度
        int waterImgHeight = waterImg.getHeight();// 获取层图的高度
        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        // 绘制
        g2d.drawImage(waterImg, x, y, waterImgWidth, waterImgHeight, null);
        //g2d.drawImage(headImg, 30, 30, waterImgWidth, waterImgHeight, null);
        /* 设置2D画笔的画出的文字颜色 */  
        g2d.setColor(Color.black);
        /* 设置2D画笔的画出的文字背景色 */  
        g2d.setBackground(Color.white);
        
        AttributedString ats = new AttributedString(msg); 
        AttributedString ats2 = new AttributedString(msg2);
        AttributedString ats3 = new AttributedString(msg3); 
        AttributedString ats4 = new AttributedString(msg4); 
        AttributedString ats5 = new AttributedString(msg5); 
        Font font = new Font("Microsoft YaHei,微软雅黑", Font.BOLD, 30);
        Font font2 = new Font("Microsoft YaHei,微软雅黑", Font.BOLD, 45);
        Font font3 = new Font("Microsoft YaHei,微软雅黑", Font.BOLD, 20);

        /* 消除java.awt.Font字体的锯齿 */  
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                                  RenderingHints.VALUE_ANTIALIAS_ON); 
        
        /* 消除java.awt.Font字体的锯齿 */
        g2d.setFont(font); 
        int strWidth = g2d.getFontMetrics().stringWidth(msg);
        ats.addAttribute(TextAttribute.FONT, font, 0, msg.length());  
        AttributedCharacterIterator iter = ats.getIterator();  
        g2d.drawString(iter, (buffImgWidth-strWidth)/2, waterImgHeight+180);
        
        g2d.setFont(font3); 
        ats3.addAttribute(TextAttribute.FONT, font3, 0, msg3.length());  
        AttributedCharacterIterator iter3 = ats3.getIterator();  
        g2d.drawString(iter3, waterImgWidth+230, waterImgHeight+830);
        
        ats4.addAttribute(TextAttribute.FONT, font3, 0, msg4.length());  
        AttributedCharacterIterator iter4 = ats4.getIterator();  
        g2d.drawString(iter4, waterImgWidth+230, waterImgHeight+870);
        
        ats5.addAttribute(TextAttribute.FONT, font3, 0, msg5.length());  
        AttributedCharacterIterator iter5 = ats5.getIterator();  
        g2d.drawString(iter5, waterImgWidth+230, waterImgHeight+910);
        
        g2d.setFont(font2);
        int strWidth2 = g2d.getFontMetrics(font2).stringWidth(msg2);
        
        g2d.setColor(Color.red);
        ats2.addAttribute(TextAttribute.FONT, font2, 0, msg2.length());  
        AttributedCharacterIterator iter2 = ats2.getIterator();
   	    g2d.drawString(iter2, (buffImgWidth-strWidth2)/2, waterImgHeight+280);
        
        g2d.dispose();// 释放图形上下文使用的系统资源
        return buffImg;
    }

    /**
     * 输出水印图片
     * 
     * @param buffImg
     *            图像加水印之后的BufferedImage对象
     * @param savePath
     *            图像加水印之后的保存路径
     */
    public static void generateWaterFile(BufferedImage buffImg, String savePath) {
        int temp = savePath.lastIndexOf(".") + 1;
        try {
            ImageIO.write(buffImg, savePath.substring(temp), new File(savePath));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}