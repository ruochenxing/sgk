package com.sgk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 数据库管理 <br/>
 * 数据库配置，线程池管理。获取数据库连接Connection给QueryHelper使用
 */
public class DBManager {

	private final static Log log = LogFactory.getLog(DBManager.class);
	private final static ThreadLocal<Connection> conns = new ThreadLocal<Connection>();
	private static DataSource dataSource;
	private static boolean show_sql = false;
	private static Properties cp_props = new Properties();
	final static ConcurrentHashMap<Long, ConnectionContext> conn_context;

	public static class ConnectionContext {
		public Exception exception;
		public Map<String, String[]> params;
		public String thread;
		public String uri;
		public String ip;

		public ConnectionContext(Exception excp, String ip, String uri, Map<String, String[]> params) {
			this.exception = excp;
			this.params = params;
			this.thread = Thread.currentThread().getName();
			this.uri = uri;
			this.ip = ip;
		}

		public Exception getException() {
			return this.exception;
		}

		public Map<String, String[]> getParams() {
			return this.params;
		}

		public String getThread() {
			return this.thread;
		}

		public String getUri() {
			return this.uri;
		}

		public String getIp() {
			return this.ip;
		}
	}

	public static class PoolStatus {
		public int total = -1; // 总连接数
		public int busy = -1; // 活动连接
		public int idle = -1; // 空闲连接
		public Map<Long, ConnectionContext> conns;

		public int getTotal() {
			return total;
		}

		public int getBusy() {
			return busy;
		}

		public int getIdle() {
			return idle;
		}

		public Map<Long, ConnectionContext> getConns() {
			return conns;
		}
	}

	static {
		conn_context = new ConcurrentHashMap<Long, ConnectionContext>();
		initDataSource(null);
	}

	/**
	 * 初始化连接池
	 * 
	 * @param props
	 * @param show_sql
	 */
	private final static void initDataSource(Properties dbProperties) {
		try {
			if (dbProperties == null) {
				dbProperties = new Properties();
				dbProperties.load(DBManager.class.getResourceAsStream("druid.properties"));
			}
			for (Object key : dbProperties.keySet()) {
				String skey = (String) key;
				if (skey.startsWith("jdbc.")) {
					String name = skey.substring(5);
					cp_props.put(name, dbProperties.getProperty(skey));
					if ("show_sql".equalsIgnoreCase(name)) {
						show_sql = "true".equalsIgnoreCase(dbProperties.getProperty(skey));
					}
				}
			}
			dataSource = (DataSource) Class.forName(cp_props.getProperty("datasource")).newInstance();
			if (dataSource.getClass().getName().indexOf("c3p0") > 0) {
				// Disable JMX in C3P0
				System.setProperty("com.mchange.v2.c3p0.management.ManagementCoordinator",
						"com.mchange.v2.c3p0.management.NullManagementCoordinator");
			}
			log.info("Using DataSource : " + dataSource.getClass().getName());
			BeanUtils.populate(dataSource, cp_props);// 用来将一些 key-value 的值（例如
														// hashmap）映射到 bean 中的属性

			Connection conn = getConnection();
			DatabaseMetaData mdm = conn.getMetaData();
			log.info("Connected to " + mdm.getDatabaseProductName() + " " + mdm.getDatabaseProductVersion());
			closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static PoolStatus getPoolStatus() {
		PoolStatus pool = new PoolStatus();
		try {
			if (dataSource.getClass().getName().indexOf("c3p0") > 0) {
				pool.total = (Integer) PropertyUtils.getProperty(dataSource, "numConnectionsDefaultUser");
				pool.busy = (Integer) PropertyUtils.getProperty(dataSource, "numBusyConnectionsDefaultUser");
				pool.idle = (Integer) PropertyUtils.getProperty(dataSource, "numIdleConnectionsDefaultUser");
			} else if (dataSource.getClass().getName().indexOf("bonecp") > 0) {
				pool.total = (Integer) PropertyUtils.getProperty(dataSource, "maxConnectionsPerPartition")
						* (Integer) PropertyUtils.getProperty(dataSource, "partitionCount");
				// pool.busy = (Integer)PropertyUtils.getProperty(dataSource,
				// "numBusyConnectionsDefaultUser");
				// pool.idle = (Integer)PropertyUtils.getProperty(dataSource,
				// "numIdleConnectionsDefaultUser");
			} else if (dataSource.getClass().getName().indexOf("druid") > 0) {
				pool.total = (Integer) PropertyUtils.getProperty(dataSource, "maxActive");
				pool.busy = (Integer) PropertyUtils.getProperty(dataSource, "activeCount");
				pool.idle = (Integer) PropertyUtils.getProperty(dataSource, "poolingCount");
			}
			pool.conns = conn_context;
		} catch (Exception e) {
		}
		return pool;
	}

	/**
	 * 断开连接池
	 */
	public final static void closeDataSource() {
		try {
			dataSource.getClass().getMethod("close").invoke(dataSource);
		} catch (NoSuchMethodException e) {
		} catch (Exception e) {
			log.error("Unabled to destroy DataSource!!! ", e);
		}
	}

	public final static Connection getConnection() throws SQLException {
		Connection conn = conns.get();
		if (conn == null || conn.isClosed()) {
			conn = _getConnection();
			if (conn == null)
				throw new SQLException("Unabled to get connection.");
			conns.set(conn);
		}
		return (show_sql && !Proxy.isProxyClass(conn.getClass())) ? new _DebugConnection(conn).getConnection() : conn;
	}

	private static Connection _getConnection() throws SQLException {
		try {
			return dataSource.getConnection();
		} catch (Exception e) {
			log.error("Unable to get connection from datasource", e);
			return null;
			/**
			 * return DriverManager.getConnection( cp_props.getProperty("url"),
			 * cp_props.getProperty("username"),
			 * cp_props.getProperty("password"));
			 */
		}
	}

	/**
	 * 关闭连接
	 */
	public final static void closeConnection() {
		Connection conn = conns.get();
		try {
			if (conn != null && !conn.isClosed()) {
				conn.setAutoCommit(true);
				conn.close();
				conn_context.remove(Thread.currentThread().getId());
			}
		} catch (SQLException e) {
			log.error("Unabled to close connection!!! ", e);
		}
		conns.set(null);
	}

	/**
	 * 用于跟踪执行的SQL语句
	 * 
	 * @author
	 */
	static class _DebugConnection implements InvocationHandler {

		private final static Log log = LogFactory.getLog(_DebugConnection.class);

		private Connection conn = null;

		public _DebugConnection(Connection conn) {
			this.conn = conn;
		}

		/**
		 * Returns the conn.
		 * 
		 * @return Connection
		 */
		public Connection getConnection() {
			return (Connection) Proxy.newProxyInstance(conn.getClass().getClassLoader(),
					conn.getClass().getInterfaces(), this);
		}

		public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
			try {
				String method = m.getName();
				if ("prepareStatement".equals(method) || "createStatement".equals(method))
					log.info("[SQL] >>> " + args[0]);
				return m.invoke(conn, args);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

	}

}
