package com.sgk.bean;

import java.io.File;
import java.io.IOException;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

public class MDBTest {
	public static void main(String[] args) throws IOException {
		Database db = DatabaseBuilder.open(new File("/Users/xxx/Documents/社工库/前程无忧-51job.mdb"));
		Table table = db.getTable("baseinfo");
		int count = 0;
		for (Row row : table) {
			count++;
			System.out.println("id" + ": " + row.get("id"));
//			System.out.println("cname" + ": " + row.get("cname"));
//			System.out.println("gender" + ": " + row.get("gender"));
//			System.out.println("birth" + ": " + row.get("birth"));
//			System.out.println("region" + ": " + row.get("region"));
//			System.out.println("hukou" + ": " + row.get("hukou"));
//			System.out.println("salary" + ": " + row.get("salary"));
//			System.out.println("workyear" + ": " + row.get("workyear"));
//			System.out.println("address" + ": " + row.get("address"));
//			System.out.println("postcode" + ": " + row.get("postcode"));
//			System.out.println("email" + ": " + row.get("email"));
//			System.out.println("mob" + ": " + row.get("mob"));
//			System.out.println("hometel" + ": " + row.get("hometel"));
//			System.out.println("worktel" + ": " + row.get("worktel"));
//			System.out.println("website" + ": " + row.get("website"));
//			System.out.println("worktype" + ": " + row.get("worktype"));
//			System.out.println("industry" + ": " + row.get("industry"));
//			System.out.println("location" + ": " + row.get("location"));
//			System.out.println("monsalary" + ": " + row.get("monsalary"));
//			System.out.println("jobpost" + ": " + row.get("jobpost"));
//			System.out.println("remark" + ": " + row.get("remark"));
		}
		System.out.println(count);
	}
}
