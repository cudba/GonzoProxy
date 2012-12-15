package ch.compass.gonzoproxy.relay.io.wrapper;

import ch.compass.gonzoproxy.model.packet.Packet;

public interface PacketWrapper {

	public byte[] wrap(Packet packet);

	public String getName();

}
