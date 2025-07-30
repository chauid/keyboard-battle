package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import db.MysqlConnection;
import dto.UserSessionDTO;

public class UserSessionDAO {
	MysqlConnection db = new MysqlConnection();

	public void createUserSession(int userId, String sessionId) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user_session WHERE user_id = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				query = "UPDATE user_session SET session_id = ? WHERE user_id = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, sessionId);
				pstmt.setInt(2, userId);
				pstmt.executeUpdate();
			} else {
				query = "INSERT INTO user_session (session_id, user_id) VALUES (?, ?)";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, sessionId);
				pstmt.setInt(2, userId);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}

	public UserSessionDTO readUserSessionByUserId(int userId) {
		UserSessionDTO userSession = null;
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user_session WHERE user_id = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				userSession = new UserSessionDTO();
				userSession.setUserId(rs.getInt("user_id"));
				userSession.setSessionId(rs.getString("session_id"));
				userSession.setCreatedAt(rs.getTimestamp("created_at"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
		return userSession;
	}

	public void deleteUserSessionByUserId(int userId) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "DELETE FROM user_session WHERE user_id = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
}
