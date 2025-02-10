package dto;

public class RoomDTO {
	private String id;
	private int userId;
	private String name;
	private String password;
	private boolean allowSpectator;
	private boolean isIngame;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAllowSpectator() {
		return allowSpectator;
	}

	public void setAllowSpectator(boolean allowSpectator) {
		this.allowSpectator = allowSpectator;
	}

	public boolean isIngame() {
		return isIngame;
	}

	public void setIngame(boolean isIngame) {
		this.isIngame = isIngame;
	}

}
