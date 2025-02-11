package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import db.MysqlConnection;
import dto.TitleDTO;

public class TitleDAO {
	MysqlConnection db = new MysqlConnection();

	public void createTitle(TitleDTO title) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "INSERT INTO title (name, condition) VALUES (?, ?)";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, title.getName());
			pstmt.setString(2, title.getCondition());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}

	public List<TitleDTO> readAllTitle() {
		List<TitleDTO> list = new ArrayList<TitleDTO>();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM title";

		try {
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				TitleDTO title = new TitleDTO();
				title.setId(rs.getInt("id"));
				title.setName(rs.getString("name"));
				title.setCondition(rs.getString("condition"));
				list.add(title);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return list;
	}

	public TitleDTO readTitleById(int id) {
		TitleDTO title = null;
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM title WHERE id = ?";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				title = new TitleDTO();
				title.setId(rs.getInt("id"));
				title.setName(rs.getString("name"));
				title.setCondition(rs.getString("condition"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return title;
	}
}
