package dto;

import java.sql.Timestamp;

public class BattleLogDTO {
	private int id;
	private int winUser;
	private int defeatUser;
	private int winUserScore;
	private int defeatUserScore;
	private int winUserGainedExp;
	private int defeatUserGainedExp;
	private Timestamp createdAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getWinUser() {
		return winUser;
	}

	public void setWinUser(int winUser) {
		this.winUser = winUser;
	}

	public int getDefeatUser() {
		return defeatUser;
	}

	public void setDefeatUser(int defeatUser) {
		this.defeatUser = defeatUser;
	}

	public int getWinUserScore() {
		return winUserScore;
	}

	public void setWinUserScore(int winUserScore) {
		this.winUserScore = winUserScore;
	}

	public int getDefeatUserScore() {
		return defeatUserScore;
	}

	public void setDefeatUserScore(int defeatUserScore) {
		this.defeatUserScore = defeatUserScore;
	}

	public int getWinUserGainedExp() {
		return winUserGainedExp;
	}

	public void setWinUserGainedExp(int winUserGainedExp) {
		this.winUserGainedExp = winUserGainedExp;
	}

	public int getDefeatUserGainedExp() {
		return defeatUserGainedExp;
	}

	public void setDefeatUserGainedExp(int defeatUserGainedExp) {
		this.defeatUserGainedExp = defeatUserGainedExp;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

}
