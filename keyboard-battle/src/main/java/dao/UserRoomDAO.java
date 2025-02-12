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

	public void createUserRoom(UserRoomDTO userRoom) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		
		String query = "SELECT * FROM user_room WHERE user_id = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userRoom.getUserId());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		query = "INSERT INTO user_room (user_id, room_id, socket_session_id, is_ingame, is_ready) VALUES (?, ?, ?, ?, ?)";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userRoom.getUserId());
			pstmt.setString(2, userRoom.getRoomId());
			pstmt.setString(3, userRoom.getSocketSessionId());
			pstmt.setBoolean(4, userRoom.isIngame());
			pstmt.setBoolean(5, userRoom.isReady());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}

	public List<UserRoomDTO> readUserRoomsByRoomId(String roomId) {
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
				userRoom.setSocketSessionId(rs.getString("socket_session_id"));
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

	public int readUserRoomCountByRoomId(String roomId) {
		int count = 0;
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT COUNT(*) FROM user_room WHERE room_id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, roomId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return count;
	}

	public UserRoomDTO readUserRoomByUserId(int userId) {
		UserRoomDTO userRoom = null;
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user_room WHERE user_id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				userRoom = new UserRoomDTO();
				userRoom.setUserId(rs.getInt("user_id"));
				userRoom.setRoomId(rs.getString("room_id"));
				userRoom.setIngame(rs.getBoolean("is_ingame"));
				userRoom.setReady(rs.getBoolean("is_ready"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return userRoom;
	}

	public UserRoomDTO readUserRoomBySocketSessionId(String socketSessionId, String roomId) {
		UserRoomDTO userRoom = null;
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user_room WHERE socket_session_id = ? AND room_id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, socketSessionId);
			pstmt.setString(2, roomId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				userRoom = new UserRoomDTO();
				userRoom.setUserId(rs.getInt("user_id"));
				userRoom.setRoomId(rs.getString("room_id"));
				userRoom.setIngame(rs.getBoolean("is_ingame"));
				userRoom.setReady(rs.getBoolean("is_ready"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return userRoom;
	}

	public void updateUserRoom(UserRoomDTO userRoom) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "UPDATE user_room SET socket_session_id = ?, is_ingame = ?, is_ready = ? WHERE user_id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, userRoom.getSocketSessionId());
			pstmt.setBoolean(2, userRoom.isIngame());
			pstmt.setBoolean(3, userRoom.isReady());
			pstmt.setInt(4, userRoom.getUserId());
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

	public void deleteUserRoomBySocketSessionId(String socketSessionId, String roomId) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "DELETE FROM user_room WHERE socket_session_id = ? AND room_id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, socketSessionId);
			pstmt.setString(2, roomId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
}
