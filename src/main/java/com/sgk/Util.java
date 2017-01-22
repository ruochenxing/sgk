package com.sgk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class Util {
	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */
	public static List<String> readContentsByFilename(String filePath) {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				lines.add(tempString);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return lines;
	}

	private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

	/**
	 * 判断是不是一个合法的电子邮件地址
	 *
	 * @param email
	 * @return
	 */
	public static boolean is_email(String email) {
		if (StringUtils.isBlank(email))
			return false;
		email = email.toLowerCase();
		if (email.endsWith("nepwk.com"))
			return false;
		if (email.endsWith(".con"))
			return false;
		if (email.endsWith(".cm"))
			return false;
		if (email.endsWith("@gmial.com"))
			return false;
		if (email.endsWith("@gamil.com"))
			return false;
		if (email.endsWith("@gmai.com"))
			return false;
		return emailer.matcher(email).matches();
	}
}
