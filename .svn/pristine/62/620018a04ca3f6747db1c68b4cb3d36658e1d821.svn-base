package com.util;

import sensitivewdfilter.WordFilter;

/**
 * 敏感词过滤
 * @author glacier
 */
public class WordFiler {

	/**
	 * 敏感词过滤
	 * @author Glacier
	 * @date 2017年10月11日
	 * @param content
	 * @return
	 */
	public static String afterFiler(String content) {
		
		String s = content;
		//System.out.println("解析字符串： " + s);
		//System.out.println("解析字数 : " + s.length());
		String re;
		//	long nano = System.nanoTime();
		re = WordFilter.doFilter(s);
		//nano = (System.nanoTime() - nano);
		//System.out.println("解析时间 : " + nano + "ns");
		//System.out.println("解析时间 : " + nano / 1000000 + "ms");

		//nano = System.nanoTime();
		//System.out.println("是否包含敏感词： " + WordFilter.isContains(s));
		//nano = (System.nanoTime() - nano);
		//System.out.println("解析时间 : " + nano + "ns");
		//System.out.println("解析时间 : " + nano / 1000000 + "ms");
		
		return re;
	}
	
	
	/**
	 * 判断是否含有敏感词
	 * @author Glacier
	 * @date 2017年10月11日
	 * @param content
	 * @return
	 */
	public static boolean needFiler(String content) {
		
		String s = content;
		//System.out.println("解析字符串： " + s);
		//System.out.println("解析字数 : " + s.length());
		//String re;
		//	long nano = System.nanoTime();
		//re = WordFilter.doFilter(s);
		//nano = (System.nanoTime() - nano);
		//System.out.println("解析时间 : " + nano + "ns");
		//System.out.println("解析时间 : " + nano / 1000000 + "ms");

		//nano = System.nanoTime();
		System.out.println("是否包含敏感词： " + WordFilter.isContains(s));
		//nano = (System.nanoTime() - nano);
		//System.out.println("解析时间 : " + nano + "ns");
		//System.out.println("解析时间 : " + nano / 1000000 + "ms");
		
		return WordFilter.isContains(s);
	}
	
}
