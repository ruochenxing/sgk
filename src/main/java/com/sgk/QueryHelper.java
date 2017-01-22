package com.sgk;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 数据库查询助手 <br/>
 * 从DBManager获取Connection来执行SQL语句
 */
@SuppressWarnings("unchecked")
public class QueryHelper {

	final static Log log = LogFactory.getLog(QueryHelper.class);

	private final static QueryRunner _g_runner = new QueryRunner();
	private final static ColumnListHandler _g_columnListHandler = new ColumnListHandler() {
		@Override
		protected Object handleRow(ResultSet rs) throws SQLException {
			Object obj = super.handleRow(rs);
			if (obj instanceof BigInteger)
				return ((BigInteger) obj).longValue();
			return obj;
		}

	};
	private final static ScalarHandler _g_scaleHandler = new ScalarHandler() {
		@Override
		public Object handle(ResultSet rs) throws SQLException {
			Object obj = super.handle(rs);
			if (obj instanceof BigInteger)
				return ((BigInteger) obj).longValue();
			return obj;
		}
	};

	private final static MapHandler _g_mapHander = new MapHandler() {
		@Override
		public Map<String, Object> handle(ResultSet rs) throws SQLException {
			Map<String, Object> result = super.handle(rs);
			for (Map.Entry<String, Object> entry : result.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof BigInteger) {
					result.put(entry.getKey(), ((BigInteger) value).longValue());
				}
			}
			return result;
		}
	};
	private final static MapListHandler _g_mapListHandler = new MapListHandler() {
		@Override
		protected Map<String, Object> handleRow(ResultSet rs) throws SQLException {
			Map<String, Object> result = super.handleRow(rs);
			for (Map.Entry<String, Object> entry : result.entrySet()) {
				if (entry.getValue() instanceof BigInteger) {
					result.put(entry.getKey(), ((BigInteger) entry.getValue()).longValue());
				}
			}
			return result;
		}
	};
	private final static List<Class<?>> PrimitiveClasses = new ArrayList<Class<?>>() {
		private static final long serialVersionUID = 1L;

		{
			add(Long.class);
			add(Integer.class);
			add(Number.class);
			add(String.class);
			add(java.util.Date.class);
			add(java.sql.Date.class);
			add(java.sql.Timestamp.class);
		}
	};

	private final static boolean _IsPrimitive(Class<?> cls) {
		return cls.isPrimitive() || PrimitiveClasses.contains(cls);
	}

	/**
	 * 获取数据库连接
	 */
	public static Connection getConnection() {
		try {
			return DBManager.getConnection();
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 读取某个对象
	 */
	@SuppressWarnings("rawtypes")
	public static <T> T read(Class<T> beanClass, String sql, Object... params) {
		try {
			return (T) _g_runner.query(getConnection(), sql,
					_IsPrimitive(beanClass) ? _g_scaleHandler : new BeanHandler(beanClass), params);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 读取一条数据，结果返回到map中
	 */
	public static Map<String, Object> readMap(String sql, Object... params) {
		try {
			return _g_runner.query(getConnection(), sql, _g_mapHander, params);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 读取一个结果集，返回一个map集合
	 */
	public static List<Map<String, Object>> readMapList(String sql, Object... params) {
		try {
			return _g_runner.query(getConnection(), sql, _g_mapListHandler, params);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 对象查询
	 */
	@SuppressWarnings("rawtypes")
	public static <T> List<T> query(Class<T> beanClass, String sql, Object... params) {
		try {
			return (List<T>) _g_runner.query(getConnection(), sql,
					_IsPrimitive(beanClass) ? _g_columnListHandler : new BeanListHandler(beanClass), params);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 分页查询
	 */
	public static <T> List<T> query_slice(Class<T> beanClass, String sql, int page, int count, Object... params) {
		if (page < 0 || count < 0)
			throw new IllegalArgumentException("Illegal parameter of 'page' or 'count', Must be positive.");
		int from = (page - 1) * count;
		count = (count > 0) ? count : Integer.MAX_VALUE;
		return query(beanClass, sql + " LIMIT ?,?", ArrayUtils.addAll(params, new Integer[] { from, count }));
	}

	/**
	 * 执行统计查询语句，语句的执行结果必须只返回一个数值
	 */
	public static long stat(String sql, Object... params) {
		try {
			Number num = (Number) _g_runner.query(getConnection(), sql, _g_scaleHandler, params);
			return (num != null) ? num.longValue() : -1;
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 执行INSERT/UPDATE/DELETE语句
	 */
	public static int update(String sql, Object... params) {
		try {
			return _g_runner.update(getConnection(), sql, params);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * 批量执行指定的SQL语句
	 * 
	 * @notUse
	 */
	public static int[] batch(String sql, Object[][] params) {
		try {
			Connection conn = getConnection();
			boolean automit = conn.getAutoCommit();
			try {
				conn.setAutoCommit(false);
				int[] m = _g_runner.batch(conn, sql, params);
				conn.commit();
				return m;
			} catch (SQLException e) {
				conn.rollback();
				throw new DBException(e);
			} finally {
				conn.setAutoCommit(automit);
			}
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

}
