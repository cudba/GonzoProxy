package ch.compass.gonzoproxy.controller.relay.io.wrapper;

import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class AsciiWrapper implements PacketWrapper {

	@Override
	public byte[] wrap(Packet packet) {
		byte[] wrappedPacket = preparePacketForStream(packet);
		return wrappedPacket;
	}

	private byte[] preparePacketForStream(Packet apdu) {
		byte[] packetData = apdu.getPacketData();
		byte[] trailer = apdu.getTrailer();
		byte[] wrappedPacket = ByteArraysUtils.merge(packetData, trailer);
		return wrappedPacket;
	}

	@Override
	public String getName() {
		return "ascii";
	}

}
