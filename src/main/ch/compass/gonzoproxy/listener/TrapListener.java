package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.controller.relay.settings.TrapState;
import ch.compass.gonzoproxy.model.packet.PacketType;

public interface TrapListener {

	public void trapStateChanged(TrapState trapState);
	public void sendOnePacket(PacketType type);
}
