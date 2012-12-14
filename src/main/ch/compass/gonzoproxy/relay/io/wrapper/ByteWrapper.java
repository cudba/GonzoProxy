package ch.compass.gonzoproxy.relay.io.wrapper;

import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class ByteWrapper implements PacketWrapper {
	
	/*
	 * Return merged packet contents
	 */

	@Override
	public byte[] wrap(Packet apdu) {
		byte[] wrappedPacket = apdu.getPreamble();
		byte[] packetData = ByteArraysUtils.asciiHexToByteArray(apdu.getPacketDataAsString());
		
		wrappedPacket = ByteArraysUtils.merge(wrappedPacket, packetData);
		wrappedPacket = ByteArraysUtils.merge(wrappedPacket, apdu.getTrailer());
		
		return wrappedPacket;
	}

	@Override
	public String getName() {
		return "byte";
	}

}
