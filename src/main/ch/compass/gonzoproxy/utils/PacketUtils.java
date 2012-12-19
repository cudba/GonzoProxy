package ch.compass.gonzoproxy.utils;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.packet.Field;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.packet.PacketDataFormat;
import ch.compass.gonzoproxy.model.packet.PacketType;

public class PacketUtils {
	
	public static final byte[] END_OF_STREAM_PACKET = "End Of Stream".getBytes();
	public static final byte[] MODE_FAILURE_PACKET = "Mode loading error".getBytes();
	public static final byte[] STOP_PACKET = "Relay stopped".getBytes();

	
	public static boolean packetStreamContainsMoreFields(byte[] packet, int fieldLength,
			int offset) {
		return (offset + fieldLength * PacketDataFormat.ENCODING_OFFSET) <= packet.length;
	}
	
	public static int findFieldInPlainPacket(byte[] plainPacket, int offset,
			Field field) {
		byte[] nextContentIdentifier = field.getValue().getBytes();

		for (int i = offset; i < plainPacket.length
				- nextContentIdentifier.length; i++) {

			boolean matches = true;
			for (int j = 0; j < nextContentIdentifier.length; j++) {
				if (nextContentIdentifier[j] != plainPacket[i + j])
					matches = false;
			}
			if (matches)
				return i;
		}
		return 0;
	}
	
	public static int computeFieldLength(ArrayList<Field> fields, int offset) {
		int fieldLength = PacketDataFormat.DEFAULT_FIELDLENGTH;
		if (fields.size() > offset) {
			Field field = fields.get(offset);
			if (field.getValue() != null) {
				String value = field.getValue().replaceAll("\\s", "");
				fieldLength = value.length() / PacketDataFormat.ENCODING_OFFSET;
			}
		}
		return fieldLength;
	}
	
	public static int getRemainingContentSize(int contentStartIndex,
			int contentLength, int offset) {
		return contentLength
				- ((offset - contentStartIndex) / (PacketDataFormat.ENCODING_OFFSET + PacketDataFormat.WHITESPACE_OFFSET));
	}
	
	public static int getSubContentLength(int offset, int nextIdentifier) {
		return (nextIdentifier - offset)
				/ (PacketDataFormat.ENCODING_OFFSET + PacketDataFormat.WHITESPACE_OFFSET);
	}
	
	public static int getRemainingPacketSize(int packetLength, int offset) {
		return (packetLength - offset) / PacketDataFormat.ENCODING_OFFSET;
	}
	
	public static boolean hasCustomLenght(int fieldLength) {
		return fieldLength > PacketDataFormat.DEFAULT_FIELDLENGTH;
	}
	
	public static Packet createPacket(byte[] asciiHexPacketData, PacketType packetType) {
		Packet packet = new Packet();
		packet.setPacketData(asciiHexPacketData);
		packet.setType(packetType);
		
		return packet;
	}
	
	public static Packet getModeFailurePacket() {
		Packet modeFailurePacket = new Packet();
		modeFailurePacket.setPacketData(MODE_FAILURE_PACKET);
		return modeFailurePacket;
	}

	public static Packet getEndOfStreamPacket() {
		Packet eosPacket = new Packet();
		eosPacket.setPacketData(END_OF_STREAM_PACKET);
		return eosPacket;
	}
	
	public static Packet getStopPacket() {
		Packet stopPacket = new Packet();
		stopPacket.setPacketData(STOP_PACKET);	
		return stopPacket;
	}

	public static int getEncodedFieldLength(int fieldLength,
			boolean withWhitespace) {
		if (withWhitespace) {
			return fieldLength * (PacketDataFormat.ENCODING_OFFSET + PacketDataFormat.WHITESPACE_OFFSET);
		} else {
			return fieldLength * (PacketDataFormat.ENCODING_OFFSET + PacketDataFormat.WHITESPACE_OFFSET)
					- PacketDataFormat.WHITESPACE_OFFSET;
		}
	}

}
