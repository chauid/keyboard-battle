package dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import db.MysqlConnection;
import dto.RoomDTO;

public class RoomDAO {
	MysqlConnection db = new MysqlConnection();

	public void createRoom(RoomDTO room) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;

		String value = String.valueOf(System.currentTimeMillis());
		value += room.getName();
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
			room.setId(hexString.toString());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String query = "INSERT INTO room (id, user_id, name, password, allow_spectator, is_ingame) VALUES (?, ?, ?, ?, ?, ?)";

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

	public List<RoomDTO> readAllRoom() {
		List<RoomDTO> list = new ArrayList<RoomDTO>();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM room";

		try {
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				RoomDTO room = new RoomDTO();
				room.setId(rs.getString("id"));
				room.setUserId(rs.getInt("user_id"));
				room.setName(rs.getString("name"));
				room.setPassword(rs.getString("password"));
				room.setAllowSpectator(rs.getBoolean("allow_spectator"));
				room.setIngame(rs.getBoolean("is_ingame"));
				list.add(room);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return list;
	}

	public int readRoomCountBySearch(String keyword) {
		int count = 0;
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT COUNT(*) FROM room WHERE name LIKE ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, "%" + keyword + "%");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return count;
	}

	public List<RoomDTO> readRoomBySearch(String keyword, int page) {
		List<RoomDTO> list = new ArrayList<RoomDTO>();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM room WHERE name LIKE ? ORDER BY name ASC LIMIT  ?, 10";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, "%" + keyword + "%");
			pstmt.setInt(2, (page - 1) * 10);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				RoomDTO room = new RoomDTO();
				room.setId(rs.getString("id"));
				room.setUserId(rs.getInt("user_id"));
				room.setName(rs.getString("name"));
				room.setPassword(rs.getString("password"));
				room.setAllowSpectator(rs.getBoolean("allow_spectator"));
				room.setIngame(rs.getBoolean("is_ingame"));
				list.add(room);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return list;
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
