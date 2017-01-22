package com.sgk;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.sgk.bean.Account;

public class FileUtil {

	public static final int BUFSIZE = 1024 * 8;
	public static final String DIR = "/Users/xxx/Documents/社工库/163/";
	public static final String KEY = "xxx@163.com";
	public static int count = 0;
	public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(10000);

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// 合并文件
		// File dir = new File(DIR);
		// mergeFiles(DIR + "merge.txt", dir.listFiles());
		// 去重
		// partFile(new File(DIR + "merge.txt"), Integer.MAX_VALUE,
		// Integer.MIN_VALUE);
		// 搜索
		// largeFileIO(DIR + "merge_result.txt");
		// 55355139
		long start = System.currentTimeMillis();
		new Thread(() -> {
			run();
		}).start();
		new Thread(() -> {
			run();
		}).start();
		largeFileIO(DIR + "163com_163mail1_merge.txt");
		long end = System.currentTimeMillis();
		System.out.println(count + "\t time:" + (end - start) / 1000);// 82574330
	}

	public static void run() {
		while (true) {
			try {
				String line = queue.take();
				String[] arr = line.split("----|\\s+|\\|");
				if (arr.length == 2 && Util.is_email(arr[0]) && arr[1].length() >= 5) {
					Account a = new Account();
					a.setEmail(arr[0]);
					a.setPassword1(arr[1]);
					a.setSource("163");
					a.Save();
				} else if (arr.length == 3 && Util.is_email(arr[1]) && arr[2].length() >= 5) {
					Account a = new Account();
					a.setAccount(arr[0]);
					a.setEmail(arr[1]);
					a.setPassword1(arr[2]);
					a.setSource("163");
					a.Save();
				} else if (arr.length > 3) {
					if (Util.is_email(arr[0]) && arr[1].length() >= 5) {
						Account a = new Account();
						a.setEmail(arr[0]);
						a.setPassword1(arr[1]);
						a.setSource("163");
						a.Save();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 合并文件 <br/>
	 * 将files合并到outFile
	 */
	public static void mergeFiles(String outFile, File[] files) throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(outFile); FileChannel outChannel = fos.getChannel();) {
			for (File f : files) {
				if (!f.getName().endsWith(".txt")) {
					continue;
				}
				FileInputStream fis = new FileInputStream(f);
				FileChannel fc = fis.getChannel();
				ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);
				while (fc.read(bb) != -1) {
					bb.flip();
					outChannel.write(bb);
					bb.clear();
				}
				fc.close();
				fis.close();
			}
		}
	}

	/***************************************************************/
	/**
	 * 大文件读取
	 */
	public static void largeFileIO(String inputFile) throws IOException {
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(inputFile)));
				BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);) {// 10M缓存
			while (in.ready()) {
				String line = in.readLine();
				if (count <= 50699000) {
					count++;
					continue;
				}
				try {
					queue.put(line);
					count++;
					if (count % 100000 == 0) {
						Thread.sleep(3000);
					}
					if (count % 1000 == 0) {
						System.out.println(count);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 文件读取
	 */
	public static void readFileByIO(String filePath) throws IOException {
		try (FileInputStream fis = new FileInputStream(filePath);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				BufferedReader br = new BufferedReader(isr);) {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] arr = line.split("----|\\s+|\\|");
				if (arr.length == 2 && Util.is_email(arr[0])) {
					count++;
				}
				if (count % 10000 == 0) {
					System.out.println(count);
				}
			}
		}
	}

	/***************************************************************/
	// 内存监控
	final static Runtime currRuntime = Runtime.getRuntime();
	// 最小的空闲空间，额，可以用来 智能控制，- -考虑到GC ，暂时没用
	final static long MEMERY_LIMIT = 1024 * 1024 * 3;
	// 内存限制，我内存最多容纳的文件大小
	static final long FILE_LIMIT_SIZE = 1024 * 1024 * 100;
	// 文件写入缓冲区 ，我默认1M
	static final int CACHE_SIZE = 1024 * 1024;
	// 默认文件后缀
	static final String FILE_SUFFIX = ".txt";
	// 临时分割的文件目录，可以删除~。~
	static final String FILE_PREFIX = DIR + "test/";
	// 汇总的文件名
	static final String REQUST_FILE_NAME = DIR + "resultFile.txt";
	// 存放大文件 引用，以及分割位置
	static List<ChildFile> bigChildFiles = new ArrayList<ChildFile>();
	// 存放小文件的，驱除重复数据
	static Map<String, String> fileLinesMap = new HashMap<String, String>(10000);

	/**
	 * 文件去重 按hashCode 范围分割
	 * 
	 * @param origFile
	 *            待去重文件
	 * @param maxNum
	 *            Integer.MAX_VALUE
	 * @param minNum
	 *            Integer.MIN_VALUE
	 */
	public static void partFile(File origFile, long maxNum, long minNum) {
		String line = null;
		long hashCode = 0;
		long max_left_hashCode = 0;
		long min_left_hashCode = 0;
		long max_right_hashCode = 0;
		long min_right_hashCode = 0;
		BufferedWriter rightWriter = null;
		BufferedWriter leftWriter = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(origFile));
			long midNum = (maxNum + minNum) / 2;
			// 以文件hashCode 范围作为子文件名
			File leftFile = new File(FILE_PREFIX + minNum + "_" + midNum + FILE_SUFFIX);
			File rightFile = new File(FILE_PREFIX + midNum + "_" + maxNum + FILE_SUFFIX);
			leftWriter = new BufferedWriter(new FileWriter(leftFile), CACHE_SIZE);
			rightWriter = new BufferedWriter(new FileWriter(rightFile), CACHE_SIZE);
			ChildFile leftChild = new ChildFile(leftFile);
			ChildFile rightChild = new ChildFile(rightFile);
			// hashCode 的范围作为分割线
			while ((line = reader.readLine()) != null) {
				hashCode = line.hashCode();
				if (hashCode > midNum) {
					if (max_right_hashCode < hashCode || max_right_hashCode == 0) {
						max_right_hashCode = hashCode;
					} else if (min_right_hashCode > hashCode || min_right_hashCode == 0) {
						min_right_hashCode = hashCode;
					}
					// 按行写入缓存
					writeToFile(rightWriter, line);
				} else {
					if (max_left_hashCode < hashCode || max_left_hashCode == 0) {
						max_left_hashCode = hashCode;
					} else if (min_left_hashCode > hashCode || min_left_hashCode == 0) {
						min_left_hashCode = hashCode;
					}
					writeToFile(leftWriter, line);
				}
			}
			// 保存子文件信息
			leftChild.setHashCode(min_left_hashCode, max_left_hashCode);
			rightChild.setHashCode(min_right_hashCode, max_right_hashCode);
			closeWriter(rightWriter);
			closeWriter(leftWriter);
			closeReader(reader);
			// 删除临时文件，保留原文件
			if (!origFile.getName().equals("merge.txt")) {
				origFile.delete();
			}
			// 分析子文件信息，是否写入或者迭代
			analyseChildFile(rightChild, leftChild);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 分析子文件信息
	public static void analyseChildFile(ChildFile rightChild, ChildFile leftChild) {
		// 将分割后 还是大于内存的文件保存 继续分割
		File rightFile = rightChild.getChildFile();
		if (isSurpassFileSize(rightFile)) {
			bigChildFiles.add(rightChild);
		} else if (rightFile.length() > 0) {
			orderAndWriteToFiles(rightFile);
		}
		File leftFile = leftChild.getChildFile();
		if (isSurpassFileSize(leftFile)) {
			bigChildFiles.add(leftChild);
		} else if (leftFile.length() > 0) {
			orderAndWriteToFiles(leftFile);
		}
		// 未超出直接内存排序，写入文件，超出继续分割，从末尾开始，不易栈深度溢出
		if (bigChildFiles.size() > 0) {
			ChildFile e = bigChildFiles.get(bigChildFiles.size() - 1);
			bigChildFiles.remove(e);
			// 迭代分割
			partFile(e.getChildFile(), e.getMaxHashCode(), e.getMinHashCode());
		}
	}

	// 将小文件读到内存排序除重复
	public static void orderAndWriteToFiles(File file) {
		BufferedReader reader = null;
		String line = null;
		BufferedWriter totalWriter = null;
		StringBuilder sb = new StringBuilder(1000000);
		try {
			totalWriter = new BufferedWriter(new FileWriter(REQUST_FILE_NAME, true), CACHE_SIZE);
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				if (!fileLinesMap.containsKey(line)) {
					fileLinesMap.put(line, null);
					sb.append(line + "\r\n");
					// totalWriter.write(line+"\r\n");
				}
			}
			totalWriter.write(sb.toString());
			fileLinesMap.clear();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeReader(reader);
			closeWriter(totalWriter);
			// 删除子文件
			file.delete();
		}
	}

	// 判断该文件是否超过 内存限制
	public static boolean isSurpassFileSize(File file) {
		return FILE_LIMIT_SIZE < file.length();
	}

	// 将数据写入文件
	public static void writeToFile(BufferedWriter writer, String writeInfo) {
		try {
			writer.write(writeInfo + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 关闭流
	public static void closeReader(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 关闭流
	public static void closeWriter(Writer writer) {
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 内部类，记录子文件信息
	static class ChildFile {
		// 文件 和 内容 hash 分布
		File childFile;
		long maxHashCode;
		long minHashCode;

		public ChildFile(File childFile) {
			this.childFile = childFile;
		}

		public ChildFile(File childFile, long maxHashCode, long minHashCode) {
			super();
			this.childFile = childFile;
			this.maxHashCode = maxHashCode;
			this.minHashCode = minHashCode;
		}

		public File getChildFile() {
			return childFile;
		}

		public void setChildFile(File childFile) {
			this.childFile = childFile;
		}

		public long getMaxHashCode() {
			return maxHashCode;
		}

		public void setMaxHashCode(long maxHashCode) {
			this.maxHashCode = maxHashCode;
		}

		public long getMinHashCode() {
			return minHashCode;
		}

		public void setMinHashCode(long minHashCode) {
			this.minHashCode = minHashCode;
		}

		public void setHashCode(long minHashCode, long maxHashCode) {
			this.setMaxHashCode(maxHashCode);
			this.setMinHashCode(minHashCode);
		}
	}

	public static void displayMemory() {
		System.out.println("最大可用内存=" + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "m");
		System.out.println("当前空闲内存=" + Runtime.getRuntime().freeMemory() / 1024 / 1024 + "m");
		System.out.println("当前总内存=" + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "m");
	}
}
