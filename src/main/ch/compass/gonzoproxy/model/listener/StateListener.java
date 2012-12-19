package ch.compass.gonzoproxy.model.listener;

import ch.compass.gonzoproxy.model.state.RelayState;

public interface StateListener {
	
	public void sessionStateChanged(RelayState state);
}
