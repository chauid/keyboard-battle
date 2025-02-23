package dto;

import java.sql.Timestamp;

public class BattleLogDTO {
	private int id;
	private String roomTitle;
	private int winUser;
	private int firstUser;
	private int secondUser;
	private int firstUserScore;
	private int secondUserScore;
	private int firstUserGainedExp;
	private int secondUserGainedExp;
	private Timestamp createdAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRoomTitle() {
		return roomTitle;
	}

	public void setRoomTitle(String roomTitle) {
		this.roomTitle = roomTitle;
	}

	public int getWinUser() {
		return winUser;
	}

	public void setWinUser(int winUser) {
		this.winUser = winUser;
	}

	public int getFirstUser() {
		return firstUser;
	}

	public void setFirstUser(int firstUser) {
		this.firstUser = firstUser;
	}

	public int getSecondUser() {
		return secondUser;
	}

	public void setSecondUser(int secondUser) {
		this.secondUser = secondUser;
	}

	public int getFirstUserScore() {
		return firstUserScore;
	}

	public void setFirstUserScore(int firstUserScore) {
		this.firstUserScore = firstUserScore;
	}

	public int getSecondUserScore() {
		return secondUserScore;
	}

	public void setSecondUserScore(int secondUserScore) {
		this.secondUserScore = secondUserScore;
	}

	public int getFirstUserGainedExp() {
		return firstUserGainedExp;
	}

	public void setFirstUserGainedExp(int firstUserGainedExp) {
		this.firstUserGainedExp = firstUserGainedExp;
	}

	public int getSecondUserGainedExp() {
		return secondUserGainedExp;
	}

	public void setSecondUserGainedExp(int secondUserGainedExp) {
		this.secondUserGainedExp = secondUserGainedExp;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
}
