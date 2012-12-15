package ch.compass.gonzoproxy.relay.io.extractor;

import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class AsciiExtractor implements PacketExtractor {

	private static final char END_OF_PACKET = '\n';
	
	@Override
	public byte[] extractPacketsToHandler(byte[] buffer,
			RelayDataHandler relayDataHandler, int readBytes,
			PacketType forwardingType) {
		
		
		byte[] packetInStream = ByteArraysUtils.trim(buffer, 0, readBytes);

		if (packetIsComplete(packetInStream)) {
			Packet packet = extractPacket(forwardingType, packetInStream);
			relayDataHandler.offer(packet);
			return new byte[0];
		} else {
			return packetInStream;
		}
	}

	private Packet extractPacket(PacketType packetType, byte[] plainpacket) {
		byte[] packetData = ByteArraysUtils.trim(plainpacket, 0, plainpacket.length - 1);
		byte[] trailer = new byte[] {END_OF_PACKET};
		Packet packet = PacketUtils.createPacket(packetData, packetType);
		packet.setTrailer(trailer);
		return packet;
	}
	
	private boolean packetIsComplete(byte[] packet) {
		return packet[packet.length - 1] == END_OF_PACKET;
	}

	@Override
	public String getName() {
		return "ascii";
	}

}
