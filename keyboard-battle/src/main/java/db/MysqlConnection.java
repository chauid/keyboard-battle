package db;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class MysqlConnection {
	private static final String DB_ENV = "db.properties";
	private static String driver;
	private static String username;
	private static String password;
	private static String host;
	private static String port;
	private static String database;

	public MysqlConnection() {
//		System.out.println(System.getProperty("user.dir"));
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(DB_ENV));
			driver = prop.getProperty("DRIVER");
			username = prop.getProperty("USER");
			password = prop.getProperty("PASSWORD");
			host = prop.getProperty("HOST");
			port = prop.getProperty("PORT");
			database = prop.getProperty("DATABASE");
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public void closeConnection(Connection conn, PreparedStatement pstmt) {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeConnection(Connection conn, PreparedStatement pstmt, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
