package dao;

import db.MysqlConnection;
import dto.UserDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
	MysqlConnection db = new MysqlConnection();
	public List<UserDTO> readAllUser() {
		List<UserDTO> list = new ArrayList<UserDTO>();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user";

		try {
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				UserDTO user = new UserDTO();
				user.setId(rs.getInt("id"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				user.setNickname(rs.getString("nickname"));
				user.setLevel(rs.getInt("level"));
				user.setCurrentExp(rs.getInt("current_exp"));
				user.setHighScore(rs.getInt("high_score"));
				list.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return list;
	}
	
	public UserDTO readUserById(int id) {
		UserDTO user = new UserDTO();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user WHERE id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				user.setId(rs.getInt("id"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				user.setNickname(rs.getString("nickname"));
				user.setLevel(rs.getInt("level"));
				user.setCurrentExp(rs.getInt("current_exp"));
				user.setHighScore(rs.getInt("high_score"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return user;
	}
	
	public UserDTO readUserByEmail(String email) {
		UserDTO user = new UserDTO();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user WHERE email = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, email);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				user.setId(rs.getInt("id"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				user.setNickname(rs.getString("nickname"));
				user.setLevel(rs.getInt("level"));
				user.setCurrentExp(rs.getInt("current_exp"));
				user.setHighScore(rs.getInt("high_score"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return user;
	}
	
	public UserDTO readUserByNickname(String nickname) {
		UserDTO user = new UserDTO();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM user WHERE nickname = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, nickname);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				user.setId(rs.getInt("id"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				user.setNickname(rs.getString("nickname"));
				user.setLevel(rs.getInt("level"));
				user.setCurrentExp(rs.getInt("current_exp"));
				user.setHighScore(rs.getInt("high_score"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return user;
	}
	
	public void createUser(UserDTO user) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "INSERT INTO user (email, password, nickname, level, current_exp, high_score) VALUES (?, ?, ?, ?, ?, ?)";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, user.getEmail());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getNickname());
			pstmt.setInt(4, user.getLevel());
			pstmt.setInt(5, user.getCurrentExp());
			pstmt.setInt(6, user.getHighScore());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
	
	public void updateUser(UserDTO user) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "UPDATE user SET email = ?, password = ?, nickname = ?, level = ?, current_exp = ?, high_score = ? WHERE id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, user.getEmail());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getNickname());
			pstmt.setInt(4, user.getLevel());
			pstmt.setInt(5, user.getCurrentExp());
			pstmt.setInt(6, user.getHighScore());
			pstmt.setInt(7, user.getId());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
	
	public void deleteUser(int id) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "DELETE FROM user WHERE id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
}
