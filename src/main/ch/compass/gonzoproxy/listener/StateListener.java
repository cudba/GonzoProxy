package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.relay.RelaySettings.SessionState;

public interface StateListener {
	
	public void sessionStateChanged(SessionState state);
}
