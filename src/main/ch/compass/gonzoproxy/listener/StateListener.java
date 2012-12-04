package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.model.SessionSettings.SessionState;

public interface StateListener {
	
	public void sessionStateChanged(SessionState state);
}
