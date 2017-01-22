package com.sgk;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.DbUtils;

/**
 * 数据库对象的基类 <br/>
 * 定义了一些实体类的增删改查方法
 */
public class POJO implements Serializable {

	private static final long serialVersionUID = 3517006930794226042L;

	private long ___key_id;

	public long getId() {
		return ___key_id;
	}

	public void setId(long id) {
		this.___key_id = id;
	}

	/**
	 * 分页列出所有对象 <br/>
	 */
	public List<? extends POJO> List(int page, int size) {
		String sql = "SELECT * FROM " + TableName() + " ORDER BY id DESC";
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}

	/**
	 * 统计此对象的总记录数 <br/>
	 */
	public int TotalCount() {
		return (int) QueryHelper.stat("SELECT COUNT(*) FROM " + TableName());
	}

	/**
	 * 查询记录列表
	 * 
	 * @params 可以为null,format filter: WHERE....
	 */
	public List<? extends POJO> Filter(String filter, int page, int size) {
		if (filter == null)
			filter = "";
		String sql = "SELECT * FROM " + TableName() + filter + " ORDER BY id DESC";
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}

	/**
	 * 查询总记录数
	 * 
	 * @params 可以为null,format filter: WHERE...
	 */
	public int TotalCount(String filter) {
		if (filter == null)
			filter = "";
		return (int) QueryHelper.stat("SELECT COUNT(*) FROM " + TableName() + filter);
	}

	/**
	 * 返回默认的对象对应的表名
	 *
	 * @return
	 */
	protected String TableName() {
		return "";
	}

	/**
	 * 返回对象对应的缓存区域名
	 *
	 * @return
	 */
	public String CacheRegion() {
		return this.getClass().getSimpleName();
	}

	/**
	 * @return
	 */
	public long doSave() {
		if (getId() > 0)
			_InsertObject(this);
		else
			setId(_InsertObject(this));
		return getId();
	}

	// 更新对象
	public boolean doUpdate() {
		Map<String, Object> map = ListInsertableFields();
		Object id = map.remove("id");
		Set<Map.Entry<String, Object>> entrys = map.entrySet();
		Object[] params = new Object[entrys.size()];
		StringBuilder sql = new StringBuilder("update ").append(TableName()).append(" set ");
		int index = 0;
		for (Map.Entry<String, Object> entry : entrys) {
			sql.append("`" + entry.getKey() + "`").append("=?,");
			params[index] = entry.getValue();
			index++;
		}
		sql.replace(sql.length() - 1, sql.length(), " where id=");
		sql.append(id);
		return QueryHelper.update(sql.toString(), params) > 0;
	}

	/**
	 * 根据id主键删除对象
	 */
	public boolean doDelete() {
		return QueryHelper.update("DELETE FROM " + TableName() + " WHERE id=?", getId()) == 1;
	}

	/**
	 * 根据主键读取对象详细资料
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends POJO> T Get(long id) {
		if (id <= 0)
			return null;
		String sql = "SELECT * FROM " + TableName() + " WHERE id=?";
		return (T) QueryHelper.read(getClass(), sql, id);
	}

	/**
	 * 批量从数据库中查询
	 * 
	 * @see LoadList
	 */
	public List<? extends POJO> LoadList(List<Long> ids) {
		if (ids == null || ids.size() == 0)
			return null;
		StringBuilder sql = new StringBuilder("SELECT * FROM " + TableName() + " WHERE id IN (");
		for (int i = 1; i <= ids.size(); i++) {
			sql.append('?');
			if (i < ids.size())
				sql.append(',');
		}
		sql.append(')');
		List<? extends POJO> beans = QueryHelper.query(getClass(), sql.toString(), ids.toArray(new Object[ids.size()]));
		return beans;
	}

	/**
	 * 更新某个字段值
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public boolean UpdateField(String field, Object value) {
		String sql = "UPDATE " + TableName() + " SET `" + field + "` = ? WHERE id=?";
		return QueryHelper.update(sql, value, getId()) == 1;
	}

	/**
	 * 插入对象
	 * 
	 * @param obj
	 * @return 返回插入对象的主键
	 */
	private long _InsertObject(POJO obj) {
		Map<String, Object> pojo_bean = obj.ListInsertableFields();
		String[] fields = pojo_bean.keySet().toArray(new String[pojo_bean.size()]);
		StringBuilder sql = new StringBuilder("INSERT IGNORE INTO ");
		sql.append(obj.TableName());
		sql.append("(`");
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				sql.append("`,`");
			sql.append(fields[i]);
		}
		sql.append("`) VALUES(");
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				sql.append(',');
			sql.append('?');
		}
		sql.append(')');
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = QueryHelper.getConnection().prepareStatement(sql.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < fields.length; i++) {
				ps.setObject(i + 1, pojo_bean.get(fields[i]));
			}
			ps.executeUpdate();
			if (getId() > 0)
				return getId();

			rs = ps.getGeneratedKeys();
			return rs.next() ? rs.getLong(1) : -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			sql = null;
			fields = null;
			pojo_bean = null;
		}
	}

	/**
	 * 列出要插入到数据库的域集合，子类可以覆盖此方法
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> ListInsertableFields() {
		try {
			Map<String, Object> props = BeanUtils.describe(this);
			if (getId() <= 0)
				props.remove("id");
			props.remove("class");
			return props;
		} catch (Exception e) {
			throw new RuntimeException("Exception when Fetching fields of " + this);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		// 不同的子类尽管ID是相同也是不相等的
		if (!getClass().equals(obj.getClass()))
			return false;
		POJO wb = (POJO) obj;
		return wb.getId() == getId();
	}
}
