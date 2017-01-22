package com.sgk;

public abstract class Entity extends POJO {
	private static final long serialVersionUID = -4743916220496398471L;

	public abstract String TableName();
	/**
	 * 创建实体
	 */
	public long Save() {
		long id = super.doSave();
		return id;
	}

	/**
	 * 更新实体
	 */
	public boolean Update() {
		boolean result = super.doUpdate();
		return result;
	}

	/**
	 * 删除实体
	 */
	public boolean Delete() {
		boolean result = super.doDelete();
		return result;
	}
}
