package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import db.MysqlConnection;
import dto.ChatLogDTO;

public class ChatLogDAO {
	MysqlConnection db = new MysqlConnection();

	public void createChatLog(ChatLogDTO chatLog) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "INSERT INTO chat_log (message, place, user_id) VALUES (?, ?, ?)";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, chatLog.getMessage());
			pstmt.setString(2, chatLog.getPlace().name());
			pstmt.setInt(3, chatLog.getUserId());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}

	public void readChatLogByUserId(int userId, boolean isOrderDesc) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "SELECT * FROM chat_log WHERE user_id = ? ORDER BY created_at " + (isOrderDesc ? "DESC" : "ASC");

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			pstmt.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
}
