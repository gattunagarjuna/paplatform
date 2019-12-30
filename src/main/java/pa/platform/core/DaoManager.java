package pa.platform.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.commons.dbcp2.BasicDataSource;

public class DaoManager {
	
	static Logger logger = Logger.getLogger(DaoManager.class);
	
	public static Connection getConnection() {
		
		Connection conn = null;
		BasicDataSource ds = null;
		String dburl;
		String dbuser;
		String dbpwd;
	
		try {	
			ds =  new BasicDataSource();
			dburl = PaConfiguration.getInstance().getConfiguration("dburl");//"jdbc:sqlserver://QA-DB1.fishbowl.com\\SQL05QA";
			dbuser = PaConfiguration.getInstance().getConfiguration("dbuser");//"developer";
			dbpwd = PaConfiguration.getInstance().getConfiguration("dbpwd");//"F1shb0wl!";
			//maxDBconn = ClpConfiguration.getStringProperty("FISHBOWL_MASTER_DB_MAXCON");
			//ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			ds.setDriverClassName(PaConfiguration.getInstance().getConfiguration("driverclassname"));
			ds.setUrl(dburl);
			ds.setUsername(dbuser);
			ds.setPassword(dbpwd);
			ds.setMaxIdle(10);
			ds.setMaxWaitMillis(90000);
			ds.setValidationQuery("SELECT 1");
			ds.setTestOnBorrow(true);
			ds.setTestOnReturn(false);
			ds.setTestWhileIdle(true);
			conn= ds.getConnection();
		}catch(Exception e){
			logger.error(e.getMessage(), e.fillInStackTrace());
			logger.debug(ExceptionUtils.getStackTrace(e));
		}
		Instant start = Instant.now();
		return conn;
		
	}
	
	public static void close(Connection conn) {
		if (conn != null) {
			//logger.info("Closing conn.."+aliveConns.get(conn));
			try {
				//aliveConns.remove(conn);
				conn.close();
				conn = null;
			} catch (SQLException e) {
//				logger.error(e.fillInStackTrace());
//				e.printStackTrace();
			}
		}
	}
	
	public static void close(PreparedStatement prepStmt) {
		if (prepStmt != null) {
			try {
				prepStmt.close();
				prepStmt = null;
			} catch (SQLException e) {
//				e.printStackTrace();
			}
		}
	}
	
	public static void close(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
				rs = null;
			} catch (SQLException e) {
//				e.printStackTrace();
			}
		}
	}
	
public static Connection getImpactSimulatorConnection() {
		
		Connection conn = null;
		BasicDataSource ds = null;
		String dburl;
		String dbuser;
		String dbpwd;
	
		try {	
			ds =  new BasicDataSource();
			dburl = PaConfiguration.getInstance().getConfiguration("impsimdburl");//"jdbc:sqlserver://QA-DB1.fishbowl.com\\SQL05QA";
			dbuser = PaConfiguration.getInstance().getConfiguration("impsimdbuser");//"developer";
			dbpwd = PaConfiguration.getInstance().getConfiguration("impsimdbpwd");//"F1shb0wl!";
			//maxDBconn = ClpConfiguration.getStringProperty("FISHBOWL_MASTER_DB_MAXCON");
			//ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			ds.setDriverClassName(PaConfiguration.getInstance().getConfiguration("driverclassname"));
			ds.setUrl(dburl);
			ds.setUsername(dbuser);
			ds.setPassword(dbpwd);
			ds.setMaxIdle(10);
			ds.setMaxWaitMillis(90000);
			ds.setValidationQuery("SELECT 1");
			ds.setTestOnBorrow(true);
			ds.setTestOnReturn(false);
			ds.setTestWhileIdle(true);
			conn= ds.getConnection();
		}catch(Exception e){
			logger.error(e.getMessage(), e.fillInStackTrace());
			logger.debug(ExceptionUtils.getStackTrace(e));
		}
		Instant start = Instant.now();
		return conn;
		
	}

}
