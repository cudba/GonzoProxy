package ch.compass.gonzoproxy.model.listener;

import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.model.state.TrapState;

public interface TrapListener {

	public void trapStateChanged(TrapState trapState);
	public void sendOnePacket(PacketType type);
}
