package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.relay.settings.TrapState;

public interface TrapListener {

	public void trapStateChanged(TrapState trapState);
	public void sendOnePacket(PacketType type);
}
