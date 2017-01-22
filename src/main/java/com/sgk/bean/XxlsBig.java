package com.sgk.bean;

import java.util.List;

public class XxlsBig extends XxlsAbstract {
	public static int count = 0;

	public static void main(String[] args) throws Exception {
		XxlsBig howto = new XxlsBig();
		howto.process("/Users/xxx/Documents/社工库/当当数据2/DD_SH(104882).xlsx");
	}

	public XxlsBig() {
	}

	public void optRows(int sheetIndex, int curRow, List<String> rowlist) {
		count++;
		System.out.print(count + "\t");
		int size = rowlist.size();
		//邮箱 姓名 地址 未知 未知 电话号码 手机 未知
		if(rowlist.size()==8){
			System.out.printf("%-25s\t%-4s\t%-50s\t%-12s\t%-11s", size > 0 ? rowlist.get(0).toString() : "无",
				size > 1 ? toString(rowlist.get(1)) : "无", size > 2 ? toString(rowlist.get(2)) : "无",
				size > 5 ? toString(rowlist.get(5)) : "无", size > 6 ? toString(rowlist.get(6)) : "无");
			Account a=new Account();
			a.setEmail(rowlist.get(0).toString());
			a.setName(rowlist.get(1).toString());
			a.setAddr(rowlist.get(2).toString());
			a.setTel(rowlist.get(6).toString());
			a.setSource("dangdang");
			a.Save();
		}
		else if(rowlist.size()==7&&rowlist.get(5).matches("1\\d{10}")){
			System.out.printf("%-25s\t%-4s\t%-50s\t%-12s\t%-11s", size > 0 ? rowlist.get(0).toString() : "无",
					size > 1 ? toString(rowlist.get(1)) : "无", size > 2 ? toString(rowlist.get(2)) : "无","00000000",
					size > 5 ? toString(rowlist.get(5)) : "无", size > 6 ? toString(rowlist.get(6)) : "无");
			Account a=new Account();
			a.setEmail(rowlist.get(0).toString());
			a.setName(rowlist.get(1).toString());
			a.setAddr(rowlist.get(2).toString());
			a.setTel(rowlist.get(5).toString());
			a.setSource("dangdang");
			a.Save();
		}
		System.out.println();
	}//28411124

	public String toString(Object o) {
		if (o == null) {
			return "";
		} else {
			return o.toString();
		}
	}
}
