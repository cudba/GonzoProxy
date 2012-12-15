package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.relay.settings.RelaySettings.SessionState;

public interface TrapListener {

	public void checkTrapChanged(SessionState sessionState);
	public void sendOnePacket(PacketType type);
}
