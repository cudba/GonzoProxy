package ch.compass.gonzoproxy.relay.io.wrapper;

import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class AsciiWrapper implements PacketWrapper {

	@Override
	public byte[] wrap(Packet apdu) {
		byte[] wrappedPacket = preparePacketForStream(apdu);
		return wrappedPacket;
	}

	private byte[] preparePacketForStream(Packet apdu) {
		byte[] packetData = apdu.getPacketDataAsBytes();
		byte[] trailer = apdu.getTrailer();
		byte[] wrappedPacket = ByteArraysUtils.merge(packetData, trailer);
		return wrappedPacket;
	}

	@Override
	public String getName() {
		return "ascii";
	}

}
