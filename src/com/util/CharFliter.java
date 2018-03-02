package com.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class CharFliter {
	/**
	 * Java 过滤非汉字的utf8的字符
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String filterOffUtf8Mb4_2(String text) throws UnsupportedEncodingException {
		byte[] bytes = text.getBytes("utf-8");
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		int i = 0;
		while (i < bytes.length) {
		short b = bytes[i];
		if (b > 0) {
		buffer.put(bytes[i++]);
		continue;
		}

		b += 256; //去掉符号位

		if (((b >> 5) ^ 0x06) == 0) {
		buffer.put(bytes, i, 2);
		i += 2;
		/*System.out.println("2");*/
		} else if (((b >> 4) ^ 0x0E) == 0) {
		/*System.out.println("3");*/
		buffer.put(bytes, i, 3);
		i += 3;
		} else if (((b >> 3) ^ 0x1E) == 0) {
		i += 4;
		/*System.out.println("4");*/
		} else if (((b >> 2) ^ 0xBE) == 0) {
		i += 5;
		/*System.out.println("5");*/
		} else {
		i += 6;
		/*System.out.println("6");*/
		}
		}
		buffer.flip();
		return new String(buffer.array(), "utf-8");
		}
	
}
