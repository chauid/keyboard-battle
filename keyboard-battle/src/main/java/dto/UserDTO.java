package dto;

import java.sql.Timestamp;

public class UserDTO {
	private int id;
	private String email;
	private String password;
	private String nickname;
	private int level;
	private int currentExp;
	private int playCount;
	private int winCount;
	private int defeatCount;
	private int highScore;
	private String thumbnailImage;
	private String description;
	private int title;
	private Timestamp createdAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getCurrentExp() {
		return currentExp;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public int getDefeatCount() {
		return defeatCount;
	}

	public void setDefeatCount(int defatCount) {
		this.defeatCount = defatCount;
	}

	public void setCurrentExp(int currentExp) {
		this.currentExp = currentExp;
	}

	public int getHighScore() {
		return highScore;
	}

	public void setHighScore(int highScore) {
		this.highScore = highScore;
	}

	public String getThumbnailImage() {
		return thumbnailImage;
	}

	public void setThumbnailImage(String thumbnailImage) {
		this.thumbnailImage = thumbnailImage;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
}
