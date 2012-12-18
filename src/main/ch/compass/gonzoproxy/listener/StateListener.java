package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.relay.settings.RelayState;

public interface StateListener {
	
	public void sessionStateChanged(RelayState state);
}
