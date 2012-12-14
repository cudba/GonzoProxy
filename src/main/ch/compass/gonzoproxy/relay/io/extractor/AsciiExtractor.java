package ch.compass.gonzoproxy.relay.io.extractor;

import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class AsciiExtractor implements PacketExtractor {

	private static final char END_OF_PACKET = '\n';
	
	@Override
	public byte[] extractPacketsToHandler(byte[] buffer,
			RelayDataHandler relayDataHandler, int readBytes,
			ForwardingType forwardingType) {
		
		
		// no delimiters
		
//		ArrayList<Integer> indices = ByteArraysUtils.getDelimiterIndices(buffer, DELIMITER.getBytes());
//		
//		int startIndex = 0;
//		int endIndex = 0;

//		for (int i = 0; i < indices.size() - 1; i++) {
//			startIndex = indices.get(i);
//			endIndex = indices.get(i + 1);
//			int size = endIndex - startIndex;
//			byte[] plainpacket = ByteArraysUtils.trim(buffer, startIndex, size);
//			Packet packet = splitPacket(forwardingType, plainpacket);
//			relayDataHandler.offer(packet);
//		}

		byte[] singlePacket = ByteArraysUtils.trim(buffer, 0, readBytes);

		if (packetIsComplete(singlePacket)) {
			Packet packet = splitPacket(forwardingType, singlePacket);
			relayDataHandler.offer(packet);
			return new byte[0];
		} else {
			return singlePacket;
		}
	}

	private Packet splitPacket(ForwardingType forwardingType, byte[] plainpacket) {
		byte[] packetData = ByteArraysUtils.trim(plainpacket, 0, plainpacket.length - 1);
		System.out.println(new String(packetData));
		byte[] trailer = new byte[] {END_OF_PACKET};
		Packet packet = new Packet();
		packet.setPacketData(packetData);
		packet.setTrailer(trailer);
		packet.setType(forwardingType);
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
