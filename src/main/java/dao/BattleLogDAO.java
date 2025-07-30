package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import db.MysqlConnection;
import dto.BattleLogDTO;

public class BattleLogDAO {
	MysqlConnection db = new MysqlConnection();

	public void createBattleLog(BattleLogDTO battleLog) {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		String query = "INSERT INTO battle_log (room_title, win_user, first_user, second_user, first_user_score, second_user_score, first_user_gained_exp, second_user_gained_exp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, battleLog.getRoomTitle());
			pstmt.setInt(2, battleLog.getWinUser());
			pstmt.setInt(3, battleLog.getFirstUser());
			pstmt.setInt(4, battleLog.getSecondUser());
			pstmt.setInt(5, battleLog.getFirstUserScore());
			pstmt.setInt(6, battleLog.getSecondUserScore());
			pstmt.setInt(7, battleLog.getFirstUserGainedExp());
			pstmt.setInt(8, battleLog.getSecondUserGainedExp());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConnection(conn, pstmt);
		}
	}
}
