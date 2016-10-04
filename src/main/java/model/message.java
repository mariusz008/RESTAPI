package model;

public class message {

	private String code;
	private String content;
	
	public message(String _code, String _content){
		this.code = _code;
		this.content = _content;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
