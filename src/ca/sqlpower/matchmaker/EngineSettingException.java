package ca.sqlpower.matchmaker;

public class EngineSettingException extends Exception {
	public EngineSettingException(String message) {
		super(message);
	}
	
	public EngineSettingException(String message,Throwable cause){
		super(message,cause);
	}
}
