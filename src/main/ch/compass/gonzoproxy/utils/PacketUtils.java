package ch.compass.gonzoproxy.utils;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.packet.Field;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.packet.PacketDataSettings;
import ch.compass.gonzoproxy.model.packet.PacketType;

public class PacketUtils {
	
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
		int fieldLength = PacketDataSettings.DEFAULT_FIELDLENGTH;
		if (fields.size() > offset) {
			Field field = fields.get(offset);
			if (field.getValue() != null) {
				String value = field.getValue().replaceAll("\\s", "");
				fieldLength = value.length() / PacketDataSettings.ENCODING_OFFSET;
			}
		}
		return fieldLength;
	}
	
	public static int getRemainingContentSize(int contentStartIndex,
			int contentLength, int offset) {
		return contentLength
				- ((offset - contentStartIndex) / (PacketDataSettings.ENCODING_OFFSET + PacketDataSettings.WHITESPACE_OFFSET));
	}
	
	public static int getSubContentLength(int offset, int nextIdentifier) {
		return (nextIdentifier - offset)
				/ (PacketDataSettings.ENCODING_OFFSET + PacketDataSettings.WHITESPACE_OFFSET);
	}
	
	public static int getRemainingPacketSize(int packetLength, int offset) {
		return (packetLength - offset) / PacketDataSettings.ENCODING_OFFSET;
	}
	
	public static boolean hasCustomLenght(int fieldLength) {
		return fieldLength > PacketDataSettings.DEFAULT_FIELDLENGTH;
	}
	
	public static Packet createPacket(byte[] asciiHexPacketData, PacketType packetType) {
		Packet packet = new Packet();
		packet.setPacketData(asciiHexPacketData);
		packet.setType(packetType);
		
		return packet;
	}
	
	public static Packet getModeFailurePacket() {
		Packet modeFailurePacket = new Packet();
		modeFailurePacket.setPacketData(PacketDataSettings.MODE_FAILURE_PACKET);
		return modeFailurePacket;
	}

	public static Packet getEndOfStreamPacket() {
		Packet eosPacket = new Packet();
		eosPacket.setPacketData(PacketDataSettings.END_OF_STREAM_PACKET);
		return eosPacket;
	}
	
	public static Packet getStopPacket() {
		Packet stopPacket = new Packet();
		stopPacket.setPacketData(PacketDataSettings.STOP_PACKET);	
		return stopPacket;
	}

	public static int getEncodedFieldLength(int fieldLength,
			boolean withWhitespace) {
		if (withWhitespace) {
			return fieldLength * (PacketDataSettings.ENCODING_OFFSET + PacketDataSettings.WHITESPACE_OFFSET);
		} else {
			return fieldLength * (PacketDataSettings.ENCODING_OFFSET + PacketDataSettings.WHITESPACE_OFFSET)
					- PacketDataSettings.WHITESPACE_OFFSET;
		}
	}

}
