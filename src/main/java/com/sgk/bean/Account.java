package com.sgk.bean;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import com.sgk.Entity;
import com.sgk.Util;

abstract class DataHelper {
	public abstract void exec(String path) throws Exception;
}

public class Account extends Entity {

	private static final long serialVersionUID = -3767512439695365207L;
	public static final Account ME = new Account();

	public static void main(String[] args) throws IOException {
//		test3();
		
		Long l1=128L;
		Long l2=128L;
		System.out.println(l1==l2);
		
		l1=127L;
		l2=127L;
		System.out.println(l1==l2);
	}

	public static void test3() throws IOException {// 1 5
		parse("/Users/xxx/Documents/社工库/社工裤子/偷的别人的一点14年qq老密.txt", new DataHelper() {
			@Override
			public void exec(String path) throws Exception {
				FileInputStream inputStream = null;
				Scanner sc = null;
				try {
					inputStream = new FileInputStream(path);
					sc = new Scanner(inputStream, "UTF-8");
					while (sc.hasNextLine()) {
						String s = sc.nextLine();
						parseRenRen(s);
					}
					if (sc.ioException() != null) {
						throw sc.ioException();
					}
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					if (sc != null) {
						sc.close();
					}
				}
			}
		});
	}

	public static void parse2(String s) {
		count++;
		String[] arr = s.split("\\s+");
		if (arr.length == 3) {
			Account a = new Account();
			a.setAccount(arr[0]);
			a.setPassword1(arr[1]);
			a.setEmail(arr[2]);
			a.setSource("tianya");
			a.Save();
		} else {
			System.out.println(count+"\terror\t" + s);
//			int last = s.lastIndexOf(",");
//			int first = s.indexOf(",");
//			if (first < 0 || last < 0 || first >= last) {
//				System.out.println("error\t" + s);
//				return;
//			}
//			String s1 = s.substring(0, first);
//			String s2 = s.substring(first + 1, last);
//			String s3 = s.substring(last + 1);
//			System.out.println(count + "\t" + s + "\t" + s1 + "====" + s2 + "====" + s3);
//			Account a = new Account();
//			a.setAccount(s1);
//			a.setPassword1(s2);
//			a.setEmail(s3);
//			a.setSource("tianya");
//			a.Save();
		}
	}

	public static void parse1(String s) {
		count++;
		String[] arr = s.split("\\s+");
		if (arr.length == 4 && Util.is_email(arr[2])) {
			Account a = new Account();
			a.setEmail(arr[2]);
			a.setPassword1(arr[3]);
			a.Save();
		} else {
			System.out.println(s);
		}
	}

	public static void parse12306(String s) {
		count++;
		String[] arr = s.split("----");
		if (arr.length == 7) {
			Account a = new Account();
			a.setEmail(arr[0]);
			a.setPassword1(arr[1]);
			a.setName(arr[2]);
			a.setIdcard(arr[3]);
			a.setAccount(arr[4]);
			a.setTel(arr[5]);
			a.setSource("12306");
			a.Save();
		} else {
			System.out.println(s);
		}
		System.out.println(count);
	}

	public static void parseGmail(String s) {
		count++;
		String[] arr = s.split(":");
		if (arr.length == 2) {
			Account a = new Account();
			a.setEmail(arr[0]);
			a.setPassword1(arr[1]);
			a.setSource("gmail");
			a.Save();
		} else {
			System.out.println(count + "\t" + s);
		}
		System.out.println(count);
	}

	public static void parseJd(String s) {
		count++;
		String[] arr = s.split("----");
		if (arr.length == 2) {
			Account a = new Account();
			if (Util.is_email(arr[0])) {
				a.setEmail(arr[0]);
			} else {
				a.setAccount(arr[0]);
			}
			a.setPassword1(arr[1]);
			a.setSource("jd");
			a.Save();
		} else {
			System.out.println(count + "\t" + s);
		}
		System.out.println(count);
	}

	public static void parse163(String s) {
		count++;
		String[] arr = s.split("----");
		if (arr.length == 2) {
			Account a = new Account();
			a.setEmail(arr[0]);
			a.setPassword1(arr[1]);
			a.setSource("163");
			a.Save();
		} else {
			System.out.println(count + "\t" + s);
		}
		System.out.println(count);
	}

	public static int count = 0;

	public static void parseRenRen1(String s) {
		count++;
		String[] arr = s.split("\\s+");
		if (arr.length == 2 && !Util.is_email(arr[0])) {
			String email = arr[0].replace("@.", "@");
			email = email.replace("@|", "@");
			email = email.replace(".@", "@");
			if (Util.is_email(email)) {
				Account a = new Account();
				a.setEmail(email);
				a.setPassword1(arr[1]);
				a.setSource("renren");
				a.Save();
			}
		} else {
			System.out.println(s);
		}
		System.out.println(count);
	}

	/**
	 * renren/Users/xxx/Documents/社工库/xh-2.txt <br/>
	 * /Users/xxx/Documents/社工库/1000W/新建文本文档.txt
	 */
	public static void parseRenRen(String s) {
		String[] arr = s.split("----");
		if (arr.length == 2 && Util.is_email(arr[0] + "@qq.com")) {
			Account a = new Account();
			a.setEmail(arr[0] + "@qq.com");
			a.setPassword1(arr[1]);
			a.setSource("qq");
			a.Save();
		} else {
			System.out.println(s);
		}
	}

	public static void parseCSDN(String s) {
		String[] arr = s.split("\\s+#\\s+");
		if (arr.length == 3 && Util.is_email(arr[2])) {
			Account a = new Account();
			a.setAccount(arr[0]);
			a.setPassword1(arr[1]);
			a.setEmail(arr[2]);
			a.setSource("csdn");
			a.Save();
		} else {
			System.out.println(s);
		}
	}

	@Deprecated
	public static void test1() {
		parse("/Users/xxx/Documents/社工库/1000W/新建文本文档.txt", new DataHelper() {
			@Override
			public void exec(String path) {
				List<String> lines = Util.readContentsByFilename(path);
				lines.parallelStream().forEach(s -> {
					String[] arr = s.split("\\s+");
					if (arr.length == 2) {
						Account a = new Account();
						a.setEmail(arr[0]);
						a.setPassword1(arr[1]);
						a.Save();
					} else {
						System.out.println(s);
					}
				});
				System.out.println(lines.size());
			}
		});
	}

	public static void parse(String path, DataHelper helper) {
		long start = System.currentTimeMillis();
		try {
			helper.exec(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("共耗时:" + (end - start) / 1000);
	}

	/**
	 * 返回默认的对象对应的表名
	 * 
	 * @return
	 */
	public String TableName() {
		return "account";
	}

	private long id;
	private String account;// 账号名
	private String name;// 姓名
	private String email;// 邮箱
	private String addr;// 地址
	private String tel;
	private String idcard;
	private String password1;// 未加密密码
	private String password2;// 加密密码
	private String source;// 来源

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email != null)
			email = email.toLowerCase();
		this.email = email;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password1) {
		this.password1 = password1;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
