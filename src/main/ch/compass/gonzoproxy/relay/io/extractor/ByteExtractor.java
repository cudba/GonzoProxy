package ch.compass.gonzoproxy.relay.io.extractor;

import java.util.ArrayList;
import java.util.Arrays;

import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class ByteExtractor implements PacketExtractor {

	private static final byte[] DELIMITER = new byte[] { (byte) 0xaa,
			(byte) 0xaa, (byte) 0xaa, (byte) 0xaa };
	private static final byte[] END_OF_COMMAND = new byte[] { (byte) 0xbb,
			(byte) 0xbb, (byte) 0xbb, (byte) 0xbb };

	@Override
	public byte[] extractPacketsToHandler(byte[] buffer,
			RelayDataHandler relayDataHandler, int readBytes,
			ForwardingType forwardingType) {

		ArrayList<Integer> delimiterIndices = ByteArraysUtils
				.getDelimiterIndices(buffer, DELIMITER);

		int startIndex = 0;
		int endIndex = 0;

		/*
		 * If buffer contains more packets, packets are extracted by delimiter
		 */

		for (int i = 0; i < delimiterIndices.size() - 1; i++) {
			startIndex = delimiterIndices.get(i);
			endIndex = delimiterIndices.get(i + 1);
			int size = endIndex - startIndex;
			byte[] plainpacket = ByteArraysUtils.trim(buffer, startIndex, size);
			Packet packet = splitPacket(forwardingType, plainpacket);
			relayDataHandler.offer(packet);
		}

		/*
		 * only last packet in stream needs to be checked for integrity,
		 * previous packets are extracted by delimiter. for untrusted streams, an
		 * integrity check for every packet may be applied
		 */
		
		byte[] lastPacketInStream = ByteArraysUtils.trim(buffer, 0, readBytes);
		byte[] endOfCommand = ByteArraysUtils.trim(lastPacketInStream,
				lastPacketInStream.length - END_OF_COMMAND.length,
				END_OF_COMMAND.length);
		if (packetIsComplete(endOfCommand)) {
			Packet packet = splitPacket(forwardingType, lastPacketInStream);
			relayDataHandler.offer(packet);
			return new byte[0];
		} else {
			return lastPacketInStream;
		}
	}
	

	/*
	 * The Packet class offers multiple fields for usage. See Packet class
	 * documentation for further information
	 */

	private Packet splitPacket(ForwardingType forwardingType, byte[] plainpacket) {
		byte[] preamble = ByteArraysUtils
				.trim(plainpacket, 0, DELIMITER.length);
		byte[] packetData = ByteArraysUtils.trim(plainpacket, DELIMITER.length,
				plainpacket.length - END_OF_COMMAND.length - DELIMITER.length);
		byte[] trailer = ByteArraysUtils.trim(plainpacket, plainpacket.length
				- END_OF_COMMAND.length, END_OF_COMMAND.length);

		Packet asciiPacket = createPacket(preamble, packetData, trailer);

		asciiPacket.setType(forwardingType);
		return asciiPacket;
	}
	

	/*
	 * Parser expects an ascii representation of the packet data 
	 * Example of an parsable originalPacketData input: "0f a4 72".getBytes()

	 * Note: 	originalPacketData field mandatory (see parser documentation for
	 * 			further details)
	 */

	private Packet createPacket(byte[] preamble, byte[] packetData,
			byte[] trailer) {

		Packet packet = new Packet();
		byte[] asciiHexPacketData = ByteArraysUtils
				.byteToParsableAsciiHex(packetData);

		packet.setPreamble(preamble);
		packet.setPacketData(asciiHexPacketData);
		packet.setTrailer(trailer);

		return packet;
	}

	private boolean packetIsComplete(byte[] trailer) {
		return Arrays.equals(trailer, END_OF_COMMAND);
	}
	
	
	/*
	 * String returned in getName() has to match name in properties file
	 */

	@Override
	public String getName() {
		return "byte";
	}

}