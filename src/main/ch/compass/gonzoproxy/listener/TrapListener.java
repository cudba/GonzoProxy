package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.model.ForwardingType;

public interface TrapListener {

	public void checkTrapChanged();
	public void sendOnePacket(ForwardingType type);
}
