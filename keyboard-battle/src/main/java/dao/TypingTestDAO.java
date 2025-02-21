package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import db.MysqlConnection;
import dto.TypingTestDTO;

public class TypingTestDAO {
	MysqlConnection db = new MysqlConnection();

	public void createTypingTest(TypingTestDTO typingTest) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "INSERT INTO typing_test (sentence, language) VALUES (?, ?)";

		try {
			pstmt = db.getConnection().prepareStatement(query);
			pstmt.setString(1, typingTest.getSentence());
			pstmt.setString(2, typingTest.getLanguage().name());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}

	public List<TypingTestDTO> readAllTypingTest() {
		List<TypingTestDTO> list = new ArrayList<TypingTestDTO>();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM typing_test";

		try {
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				TypingTestDTO typingTest = new TypingTestDTO();
				typingTest.setId(rs.getInt("id"));
				typingTest.setSentence(rs.getString("sentence"));
				typingTest.setLanguage(TypingTestDTO.Language.valueOf(rs.getString("language")));
				list.add(typingTest);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		return list;
	}
	
	
	public TypingTestDTO readRandomTypingTest() {
		List<TypingTestDTO> list = new ArrayList<TypingTestDTO>();
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM typing_test";

		try {
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				TypingTestDTO typingTest = new TypingTestDTO();
				typingTest.setId(rs.getInt("id"));
				typingTest.setSentence(rs.getString("sentence"));
				typingTest.setLanguage(TypingTestDTO.Language.valueOf(rs.getString("language")));
				list.add(typingTest);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt, rs);
		}
		
		int randomIndex = (int) (Math.random() * list.size());
		TypingTestDTO typingTest = list.get(randomIndex);
		return typingTest;
	}
}
