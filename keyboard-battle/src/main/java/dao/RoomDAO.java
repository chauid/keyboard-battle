package dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import db.MysqlConnection;
import dto.RoomDTO;

public class RoomDAO {
	MysqlConnection db = new MysqlConnection();

	public void createRoom(RoomDTO room) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;

		String value = String.valueOf(System.currentTimeMillis());
		value += room.getName();
		String hashedValue = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedHash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

			StringBuilder hexString = new StringBuilder();
			for (byte b : encodedHash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			room.setId(hashedValue);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String query = "INSERT INTO room (id, user_id, name, password, allow_visitor, is_ingame) VALUES (?, ?, ?, ?, ?, ?)";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, room.getId());
			pstmt.setInt(2, room.getUserId());
			pstmt.setString(3, room.getName());
			pstmt.setString(4, room.getPassword());
			pstmt.setBoolean(5, room.isAllowSpectator());
			pstmt.setBoolean(6, room.isIngame());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
	
	public void deleteRoom(String roomId) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "DELETE FROM room WHERE id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, roomId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
}
