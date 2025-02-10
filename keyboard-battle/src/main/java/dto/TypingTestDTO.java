package dto;

public class TypingTestDTO {
	public enum Language {
		KOREAN, ENGLISH
	}
	
	private int id;
	private String sentence;
	private Language language;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

}
