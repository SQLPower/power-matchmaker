package ca.sqlpower.matchmaker.event;

public interface EngineListener {
	public void engineStart(EngineEvent e);
	public void engineEnd(EngineEvent e);
}
