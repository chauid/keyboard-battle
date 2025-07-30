package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

	public ChatLogDTO readChatLogByUserId(int userId, boolean isOrderDesc) {
		ChatLogDTO chatLog = null;
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM chat_log WHERE user_id = ? ORDER BY created_at " + (isOrderDesc ? "DESC" : "ASC");

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				chatLog = new ChatLogDTO();
				chatLog.setId(rs.getInt("id"));
				chatLog.setMessage(rs.getString("message"));
				chatLog.setPlace(ChatLogDTO.Place.valueOf(rs.getString("place")));
				chatLog.setUserId(rs.getInt("user_id"));
				chatLog.setCreatedAt(rs.getTimestamp("created_at"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return chatLog;
	}
}
