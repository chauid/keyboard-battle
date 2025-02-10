package dto;

public class UserRoomDTO {
	private int userId;
	private String roomId;
	private boolean isIngame;
	private boolean isReady;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public boolean isIngame() {
		return isIngame;
	}

	public void setIngame(boolean isIngame) {
		this.isIngame = isIngame;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
}
