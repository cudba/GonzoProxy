package ch.compass.gonzoproxy.relay.io.wrapper;

import ch.compass.gonzoproxy.model.Packet;

public interface PacketWrapper {

	public byte[] wrap(Packet apdu);

	public String getName();

}