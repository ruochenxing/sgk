package com.sgk;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 数据库操作异常
 */
public class DBException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DBException(Exception cause) {
		super(cause);
	}

	@Override
	public String getLocalizedMessage() {
		return getCause().getLocalizedMessage();
	}

	@Override
	public String getMessage() {
		return getCause().getMessage();
	}

	@Override
	public void printStackTrace() {
		getCause().printStackTrace();
	}

	@Override
	public void printStackTrace(PrintStream s) {
		getCause().printStackTrace(s);
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		getCause().printStackTrace(s);
	}

}
