package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.relay.settings.RelaySettings.SessionState;

public interface StateListener {
	
	public void sessionStateChanged(SessionState state);
}
