package ch.compass.gonzoproxy.relay.io.wrapper;

import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class ByteWrapper implements PacketWrapper {
	
	/*
	 * Return merged packet contents
	 */

	@Override
	public byte[] wrap(Packet packet) {
		byte[] wrappedPacket = packet.getPreamble();
		byte[] packetData = ByteArraysUtils.asciiHexToByteHex(packet.getPacketDataAsString());
		
		wrappedPacket = ByteArraysUtils.merge(wrappedPacket, packetData);
		wrappedPacket = ByteArraysUtils.merge(wrappedPacket, packet.getTrailer());
		
		return wrappedPacket;
	}

	@Override
	public String getName() {
		return "byte";
	}

}
