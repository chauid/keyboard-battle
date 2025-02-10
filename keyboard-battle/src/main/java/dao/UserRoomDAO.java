package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import db.MysqlConnection;
import dto.UserRoomDTO;

public class UserRoomDAO {
	MysqlConnection db = new MysqlConnection();

	public void createUserRoom(int userId, String roomId) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;

		String query = "SELECT * FROM user_room WHERE user_id = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		query = "INSERT INTO user_room (user_id, room_id) VALUES (?, ?)";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			pstmt.setString(2, roomId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}

	public List<UserRoomDTO> readUserRoomByRoomId(String roomId) {
		List<UserRoomDTO> list = new ArrayList<UserRoomDTO>();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user_room WHERE room_id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, roomId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				UserRoomDTO userRoom = new UserRoomDTO();
				userRoom.setUserId(rs.getInt("user_id"));
				userRoom.setRoomId(rs.getString("room_id"));
				userRoom.setIngame(rs.getBoolean("is_ingame"));
				userRoom.setReady(rs.getBoolean("is_ready"));
				list.add(userRoom);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return list;
	}

	public void updateUserRoomReady(UserRoomDTO userRoom) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "UPDATE user_room SET is_ingame = ?, is_ready = ? WHERE user_id = ? AND room_id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setBoolean(1, userRoom.isIngame());
			pstmt.setBoolean(2, userRoom.isReady());
			pstmt.setInt(3, userRoom.getUserId());
			pstmt.setString(4, userRoom.getRoomId());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}

	public void deleteUserRoomByUserId(int userId) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "DELETE FROM user_room WHERE user_id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}

	public void deleteUserRoomByRoomId(String roomId) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "DELETE FROM user_room WHERE room_id = ?";

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
